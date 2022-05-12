package com.example;

import com.example.npcs.NpcInfoCache;
import com.github.nkibler7.osrswikiscraper.NpcInfo;
import com.google.common.collect.ImmutableMap;
import com.google.mu.util.stream.BiStream;
import java.util.Arrays;
import java.util.function.Function;
import javax.annotation.Nullable;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.ItemContainer;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.client.game.ItemManager;
import net.runelite.http.api.item.ItemEquipmentStats;
import net.runelite.http.api.item.ItemStats;

@Slf4j
final class DpsCalculator {

  private static final ImmutableMap<Prayer, Double> ATTACK_PRAYER_MULTIPLIERS =
      ImmutableMap.<Prayer, Double>builder()
          .put(Prayer.CLARITY_OF_THOUGHT, 1.05)
          .put(Prayer.IMPROVED_REFLEXES, 1.10)
          .put(Prayer.INCREDIBLE_REFLEXES, 1.15)
          .put(Prayer.CHIVALRY, 1.15)
          .put(Prayer.PIETY, 1.20)
          .build();
  private static final ImmutableMap<Prayer, Double> STRENGTH_PRAYER_MULTIPLIERS =
      ImmutableMap.<Prayer, Double>builder()
          .put(Prayer.BURST_OF_STRENGTH, 1.05)
          .put(Prayer.SUPERHUMAN_STRENGTH, 1.10)
          .put(Prayer.ULTIMATE_STRENGTH, 1.15)
          .put(Prayer.CHIVALRY, 1.18)
          .put(Prayer.PIETY, 1.23)
          .build();

  private final Client client;
  private final ItemManager itemManager;
  private final NpcInfoCache npcInfoCache;

  @Inject
  DpsCalculator(Client client, ItemManager itemManager, NpcInfoCache npcInfoCache) {
    this.client = client;
    this.itemManager = itemManager;
    this.npcInfoCache = npcInfoCache;
  }

  double calculateMeleeDps(ItemContainer itemContainer, int npcId) {
    // TODO: Account for attack style and void.
    int effectiveStrengthLvl =
        (int)
                (client.getBoostedSkillLevel(Skill.STRENGTH)
                    * getPrayerMultiplier(STRENGTH_PRAYER_MULTIPLIERS))
            + 8;

    // TODO: Account for gear bonus.
    int maxHit =
        (effectiveStrengthLvl * (getEquipmentStat(itemContainer, ItemEquipmentStats::getStr) + 64)
                + 320)
            / 640;

    // TODO: Account for attack style and void.
    int effectiveAttackLvl =
        (int)
                (client.getBoostedSkillLevel(Skill.ATTACK)
                    * getPrayerMultiplier(ATTACK_PRAYER_MULTIPLIERS))
            + 3 // assumes accurate
            + 8;

    // TODO: Account for correct attack bonus based on style, and account for gear bonus.
    int attackRoll =
        effectiveAttackLvl * (getEquipmentStat(itemContainer, ItemEquipmentStats::getAslash) + 64);

    @Nullable NpcInfo npcInfo = npcInfoCache.getNpcInfo(npcId);
    if (npcInfo == null) {
      log.warn("Could not find NpcInfo in cache for ID: {}", npcId);
      return 0;
    }

    // TODO: Account for attack style.
    int defenceRoll = (npcInfo.getDef() + 9) * (npcInfo.getDslash() + 64);

    double hitChance;
    if (attackRoll > defenceRoll) {
      hitChance = 1 - ((double) (defenceRoll + 2) / (2 * attackRoll + 1));
    } else {
      hitChance = (double) attackRoll / (2 * defenceRoll + 1);
    }

    double damagePerHit = (maxHit * hitChance) / 2;
    return damagePerHit / getAttackSpeed(itemContainer);
  }

  private double getPrayerMultiplier(ImmutableMap<Prayer, Double> multipliers) {
    return BiStream.from(multipliers)
        .filterKeys(client::isPrayerActive)
        .values()
        .findFirst()
        .orElse(1.0);
  }

  private int getEquipmentStat(
      ItemContainer itemContainer, Function<ItemEquipmentStats, Integer> statFunction) {
    return Arrays.stream(itemContainer.getItems())
        .map(item -> itemManager.canonicalize(item.getId()))
        .map(this::getItemStats)
        .mapToInt(statFunction::apply)
        .sum();
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
