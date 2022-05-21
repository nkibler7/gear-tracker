package com.example.dps;

import com.example.GearTrackerConfig;
import com.example.attackstyle.AttackStyleSubscriber;
import com.example.attackstyle.MeleeBonusType;
import com.example.npcs.NpcInfoCache;
import com.github.nkibler7.osrswikiscraper.NpcInfo;
import java.util.Arrays;
import java.util.function.Function;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.ItemContainer;
import net.runelite.client.game.ItemManager;
import net.runelite.http.api.item.ItemEquipmentStats;
import net.runelite.http.api.item.ItemStats;

/**
 * Calculates damage-per-second (or DPS) for various gear setups for the current player.
 *
 * <p>Note that these DPS calculations assume optimal play that may not match reality. For example,
 * if the player is flicking Piety to boost their DPS, over time, missing ticks will reduce their
 * overall DPS below the optimal value calculated by this class.
 */
// TODO: Continue breaking this apart as needed; it should probably be split into separate classes
// for handling melee, range, and mage setups.
@Slf4j
public final class DpsCalculator {

  private final ItemManager itemManager;
  private final NpcInfoCache npcInfoCache;
  private final AttackStyleSubscriber attackStyleSubscriber;
  private final EffectiveLevelCalculator effectiveLevelCalculator;
  private final GearBonusMultiplier gearBonusMultiplier;

  @Inject
  DpsCalculator(
      ItemManager itemManager,
      NpcInfoCache npcInfoCache,
      AttackStyleSubscriber attackStyleSubscriber,
      EffectiveLevelCalculator effectiveLevelCalculator,
      GearBonusMultiplier gearBonusMultiplier) {
    this.itemManager = itemManager;
    this.npcInfoCache = npcInfoCache;
    this.attackStyleSubscriber = attackStyleSubscriber;
    this.effectiveLevelCalculator = effectiveLevelCalculator;
    this.gearBonusMultiplier = gearBonusMultiplier;
  }

  /**
   * Returns the average damage-per-second inflicted by the current player on the given target NPC,
   * assuming the gear used is contained within the given {@link ItemContainer}.
   */
  // TODO: Change ItemContainer to a more generic type that could be used in finding BiS setups
  // across all of a player's item set. Unfortunately ItemContainers cannot be arbitrarily created
  // by plugins.
  public double calculateMeleeDps(ItemContainer itemContainer, int targetNpcId) {
    NpcInfo targetNpcInfo = npcInfoCache.getNpcInfo(targetNpcId);
    if (targetNpcInfo == null) {
      log.error("Could not find NpcInfo in cache for target ID: {}", targetNpcId);
      return 0;
    }

    int effectiveStrengthLvl = effectiveLevelCalculator.calculateEffectiveStrengthLevel();
    int effectiveAttackLvl = effectiveLevelCalculator.calculateEffectiveAttackLevel();

    double maxHit =
        Math.floor(
            (effectiveStrengthLvl
                        * (getEquipmentStat(itemContainer, ItemEquipmentStats::getStr) + 64)
                    + 320)
                / 640.0);
    maxHit *= Math.floor(gearBonusMultiplier.getMultiplier(targetNpcInfo));

    MeleeBonusType currentMeleeBonusType = attackStyleSubscriber.getCurrentMeleeBonusType();
    int attackRoll =
        effectiveAttackLvl
            * (getEquipmentStat(itemContainer, currentMeleeBonusType.getAttackStatFunction()) + 64);
    attackRoll *= Math.floor(gearBonusMultiplier.getMultiplier(targetNpcInfo));

    int defenceRoll =
        (targetNpcInfo.getDef() + 9)
            * (getNpcDefensiveStat(currentMeleeBonusType, targetNpcInfo) + 64);

    double hitChance;
    if (attackRoll > defenceRoll) {
      hitChance = 1 - ((double) (defenceRoll + 2) / (2 * attackRoll + 1));
    } else {
      hitChance = (double) attackRoll / (2 * defenceRoll + 1);
    }

    double damagePerHit = (maxHit * hitChance) / 2;
    return damagePerHit / getAttackSpeed(itemContainer);
  }

  private int getEquipmentStat(
      ItemContainer itemContainer, Function<ItemEquipmentStats, Integer> statFunction) {
    return Arrays.stream(itemContainer.getItems())
        .map(item -> itemManager.canonicalize(item.getId()))
        .map(this::getItemStats)
        .mapToInt(statFunction::apply)
        .sum();
  }

  private int getNpcDefensiveStat(MeleeBonusType meleeBonusType, NpcInfo npcInfo) {
    switch (meleeBonusType) {
      case UNSPECIFIED:
        break;
      case SLASH:
        return npcInfo.getDslash();
      case CRUSH:
        return npcInfo.getDcrush();
      case STAB:
        return npcInfo.getDstab();
    }

    log.warn("Found unhandled melee bonus type: {}", meleeBonusType.name());
    return 0;
  }

  private double getAttackSpeed(ItemContainer itemContainer) {
    return Arrays.stream(itemContainer.getItems())
            .map(item -> itemManager.canonicalize(item.getId()))
            .map(this::getItemStats)
            .filter(stats -> stats.getSlot() == EquipmentInventorySlot.WEAPON.getSlotIdx())
            .findFirst()
            .map(ItemEquipmentStats::getAspeed)
            .orElse(6) // Attack speed of unarmed combat.
        * 0.6; // Converts game ticks to seconds.
  }

  private ItemEquipmentStats getItemStats(int id) {
    ItemStats stats = itemManager.getItemStats(id, /* allowNote= */ false);
    if (stats != null) {
      return stats.getEquipment();
    }
    return ItemEquipmentStats.builder().build();
  }
}
