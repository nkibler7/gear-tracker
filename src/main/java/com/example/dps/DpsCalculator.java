package com.example.dps;

import static com.example.attackstyle.AttackStyle.ACCURATE;
import static com.example.attackstyle.AttackStyle.AGGRESSIVE;
import static com.example.attackstyle.AttackStyle.CONTROLLED;

import com.example.GearTrackerConfig;
import com.example.attackstyle.AttackStyleSubscriber;
import com.example.attackstyle.MeleeBonusType;
import com.example.npcs.NpcInfoCache;
import com.github.nkibler7.osrswikiscraper.NpcInfo;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.mu.util.stream.BiStream;
import com.google.re2j.Pattern;
import java.util.Arrays;
import java.util.function.Function;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.PlayerComposition;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.kit.KitType;
import net.runelite.client.game.ItemManager;
import net.runelite.http.api.item.ItemEquipmentStats;
import net.runelite.http.api.item.ItemStats;

@Slf4j
public final class DpsCalculator {

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
  private static final ImmutableSet<Integer> VOID_MELEE_HELM_IDS =
      ImmutableSet.of(ItemID.VOID_MELEE_HELM, ItemID.VOID_MELEE_HELM_OR);
  private static final ImmutableSet<Integer> VOID_GLOVE_IDS =
      ImmutableSet.of(ItemID.VOID_KNIGHT_GLOVES, ItemID.VOID_KNIGHT_GLOVES_OR);
  private static final ImmutableSet<Integer> VOID_TOP_IDS =
      ImmutableSet.of(
          ItemID.VOID_KNIGHT_TOP,
          ItemID.VOID_KNIGHT_TOP_OR,
          ItemID.ELITE_VOID_TOP,
          ItemID.ELITE_VOID_TOP_OR);
  private static final ImmutableSet<Integer> VOID_ROBE_IDS =
      ImmutableSet.of(
          ItemID.VOID_KNIGHT_ROBE,
          ItemID.VOID_KNIGHT_ROBE_OR,
          ItemID.ELITE_VOID_ROBE,
          ItemID.ELITE_VOID_ROBE_OR);
  private static final ImmutableSet<Integer> SLAYER_BONUS_IDS =
      ImmutableSet.of(
          ItemID.BLACK_MASK,
          ItemID.BLACK_MASK_1,
          ItemID.BLACK_MASK_2,
          ItemID.BLACK_MASK_3,
          ItemID.BLACK_MASK_4,
          ItemID.BLACK_MASK_5,
          ItemID.BLACK_MASK_6,
          ItemID.BLACK_MASK_7,
          ItemID.BLACK_MASK_8,
          ItemID.BLACK_MASK_9,
          ItemID.BLACK_MASK_10,
          ItemID.BLACK_MASK_I,
          ItemID.BLACK_MASK_1_I,
          ItemID.BLACK_MASK_2_I,
          ItemID.BLACK_MASK_3_I,
          ItemID.BLACK_MASK_4_I,
          ItemID.BLACK_MASK_5_I,
          ItemID.BLACK_MASK_6_I,
          ItemID.BLACK_MASK_7_I,
          ItemID.BLACK_MASK_8_I,
          ItemID.BLACK_MASK_9_I,
          ItemID.BLACK_MASK_10_I,
          ItemID.SLAYER_HELMET,
          ItemID.BLACK_SLAYER_HELMET,
          ItemID.GREEN_SLAYER_HELMET,
          ItemID.HYDRA_SLAYER_HELMET,
          ItemID.PURPLE_SLAYER_HELMET,
          ItemID.RED_SLAYER_HELMET,
          ItemID.TURQUOISE_SLAYER_HELMET,
          ItemID.TWISTED_SLAYER_HELMET,
          ItemID.TZKAL_SLAYER_HELMET,
          ItemID.TZTOK_SLAYER_HELMET,
          ItemID.VAMPYRIC_SLAYER_HELMET,
          ItemID.SLAYER_HELMET_I,
          ItemID.BLACK_SLAYER_HELMET_I,
          ItemID.GREEN_SLAYER_HELMET_I,
          ItemID.HYDRA_SLAYER_HELMET_I,
          ItemID.PURPLE_SLAYER_HELMET_I,
          ItemID.RED_SLAYER_HELMET_I,
          ItemID.TURQUOISE_SLAYER_HELMET_I,
          ItemID.TWISTED_SLAYER_HELMET_I,
          ItemID.TZKAL_SLAYER_HELMET_I,
          ItemID.TZTOK_SLAYER_HELMET_I,
          ItemID.VAMPYRIC_SLAYER_HELMET_I);

  // TODO: Make this more robust, it should work at first but definitely confirm with real-world
  // wiki values.
  private static final Pattern UNDEAD_ATTRIBUTE_PATTERN = Pattern.compile("undead");

  private final Client client;
  private final ItemManager itemManager;
  private final NpcInfoCache npcInfoCache;
  private final AttackStyleSubscriber attackStyleSubscriber;
  private final GearTrackerConfig gearTrackerConfig;

  @Inject
  DpsCalculator(
      Client client,
      ItemManager itemManager,
      NpcInfoCache npcInfoCache,
      AttackStyleSubscriber attackStyleSubscriber,
      GearTrackerConfig gearTrackerConfig) {
    this.client = client;
    this.itemManager = itemManager;
    this.npcInfoCache = npcInfoCache;
    this.attackStyleSubscriber = attackStyleSubscriber;
    this.gearTrackerConfig = gearTrackerConfig;
  }

  public double calculateMeleeDps(ItemContainer itemContainer, int targetNpcId) {
    NpcInfo targetNpcInfo = npcInfoCache.getNpcInfo(targetNpcId);
    if (targetNpcInfo == null) {
      log.warn("Could not find NpcInfo in cache for target ID: {}", targetNpcId);
      return 0;
    }

    int effectiveStrengthLvl = calculateEffectiveStrengthLevel();
    int effectiveAttackLvl = calculateEffectiveAttackLevel();

    double maxHit =
        Math.floor(
            (effectiveStrengthLvl
                        * (getEquipmentStat(itemContainer, ItemEquipmentStats::getStr) + 64)
                    + 320)
                / 640.0);
    maxHit *= Math.floor(getGearBonusMultiplier(targetNpcInfo));

    int attackRoll =
        effectiveAttackLvl * (getEquipmentStat(itemContainer, getCurrentStatFunction()) + 64);
    attackRoll *= Math.floor(getGearBonusMultiplier(targetNpcInfo));

    int defenceRoll = (targetNpcInfo.getDef() + 9) * (getNpcDefensiveStat(targetNpcInfo) + 64);

    double hitChance;
    if (attackRoll > defenceRoll) {
      hitChance = 1 - ((double) (defenceRoll + 2) / (2 * attackRoll + 1));
    } else {
      hitChance = (double) attackRoll / (2 * defenceRoll + 1);
    }

    double damagePerHit = (maxHit * hitChance) / 2;
    return damagePerHit / getAttackSpeed(itemContainer);
  }

  private int calculateEffectiveStrengthLevel() {
    double effectiveStrengthLvl =
        Math.floor(
            (client.getBoostedSkillLevel(Skill.STRENGTH)
                * getPrayerMultiplier(STRENGTH_PRAYER_MULTIPLIERS)));

    if (attackStyleSubscriber.getCurrentAttackStyle().equals(AGGRESSIVE)) {
      effectiveStrengthLvl += 3.0;
    } else if (attackStyleSubscriber.getCurrentAttackStyle().equals(CONTROLLED)) {
      effectiveStrengthLvl += 1.0;
    }

    effectiveStrengthLvl += 8.0;

    if (isWearingFullMeleeVoid()) {
      effectiveStrengthLvl *= 1.1;
    }

    return (int) Math.floor(effectiveStrengthLvl);
  }

  private int calculateEffectiveAttackLevel() {
    double effectiveAttackLevel =
        Math.floor(
            (client.getBoostedSkillLevel(Skill.ATTACK)
                * getPrayerMultiplier(ATTACK_PRAYER_MULTIPLIERS)));

    if (attackStyleSubscriber.getCurrentAttackStyle().equals(ACCURATE)) {
      effectiveAttackLevel += 3.0;
    } else if (attackStyleSubscriber.getCurrentAttackStyle().equals(CONTROLLED)) {
      effectiveAttackLevel += 1.0;
    }

    effectiveAttackLevel += 8.0;

    if (isWearingFullMeleeVoid()) {
      effectiveAttackLevel *= 1.1;
    }

    return (int) Math.floor(effectiveAttackLevel);
  }

  private double getPrayerMultiplier(ImmutableMap<Prayer, Double> multipliers) {
    return BiStream.from(multipliers)
        .filterKeys(client::isPrayerActive)
        .values()
        .findFirst()
        .orElse(1.0);
  }

  private Function<ItemEquipmentStats, Integer> getCurrentStatFunction() {
    switch (attackStyleSubscriber.getCurrentMeleeBonusType()) {
      case UNSPECIFIED:
        break;
      case SLASH:
        return ItemEquipmentStats::getAslash;
      case CRUSH:
        return ItemEquipmentStats::getAcrush;
      case STAB:
        return ItemEquipmentStats::getAstab;
    }

    return stats -> 0;
  }

  private int getEquipmentStat(
      ItemContainer itemContainer, Function<ItemEquipmentStats, Integer> statFunction) {
    return Arrays.stream(itemContainer.getItems())
        .map(item -> itemManager.canonicalize(item.getId()))
        .map(this::getItemStats)
        .mapToInt(statFunction::apply)
        .sum();
  }

  private int getNpcDefensiveStat(NpcInfo npcInfo) {
    MeleeBonusType meleeBonusType = attackStyleSubscriber.getCurrentMeleeBonusType();
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

  private boolean isWearingFullMeleeVoid() {
    PlayerComposition playerComposition = client.getLocalPlayer().getPlayerComposition();
    return VOID_MELEE_HELM_IDS.contains(playerComposition.getEquipmentId(KitType.HEAD))
        && VOID_TOP_IDS.contains(playerComposition.getEquipmentId(KitType.TORSO))
        && VOID_ROBE_IDS.contains(playerComposition.getEquipmentId(KitType.LEGS))
        && VOID_GLOVE_IDS.contains(playerComposition.getEquipmentId(KitType.HANDS));
  }

  private double getGearBonusMultiplier(NpcInfo targetNpcInfo) {
    PlayerComposition playerComposition = client.getLocalPlayer().getPlayerComposition();
    if (isUndead(targetNpcInfo)) {
      int amuletId = playerComposition.getEquipmentId(KitType.AMULET);
      if (amuletId == ItemID.SALVE_AMULET || amuletId == ItemID.SALVE_AMULETI) {
        return (7.0 / 6.0);
      } else if (amuletId == ItemID.SALVE_AMULET_E || amuletId == ItemID.SALVE_AMULETEI) {
        return 1.2;
      }
    } else if (gearTrackerConfig.isOnSlayerTask()
        && SLAYER_BONUS_IDS.contains(playerComposition.getEquipmentId(KitType.HEAD))) {
      return (7.0 / 6.0);
    }

    return 1.0;
  }

  private static boolean isUndead(NpcInfo npcInfo) {
    return UNDEAD_ATTRIBUTE_PATTERN.matches(npcInfo.getAttributes());
  }
}
