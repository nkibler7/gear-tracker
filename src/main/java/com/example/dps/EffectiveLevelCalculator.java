package com.example.dps;

import static com.example.attackstyle.AttackStyle.ACCURATE;
import static com.example.attackstyle.AttackStyle.AGGRESSIVE;
import static com.example.attackstyle.AttackStyle.CONTROLLED;

import com.example.attackstyle.AttackStyleSubscriber;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.mu.util.stream.BiStream;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.PlayerComposition;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.kit.KitType;

/** Calculates the "effective" level used in DPS calculations for various combat skills. */
final class EffectiveLevelCalculator {

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

  private final Client client;
  private final AttackStyleSubscriber attackStyleSubscriber;

  @Inject
  EffectiveLevelCalculator(Client client, AttackStyleSubscriber attackStyleSubscriber) {
    this.client = client;
    this.attackStyleSubscriber = attackStyleSubscriber;
  }

  /**
   * Returns the current player's effective attack level after accounting for bonuses, like active
   * prayers, Void Knight equipment, etc.
   *
   * <p>The logic within this method matches the formula provided by the wiki:
   * https://oldschool.runescape.wiki/w/Damage_per_second/Melee#Step_three:_Calculate_the_effective_attack_level
   */
  public int calculateEffectiveAttackLevel() {
    // We could cast to int to truncate the decimal, but keeping it as a double (and using
    // Math.floor()) ensures we don't lose precision in math that follows.
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

  /**
   * Returns the current player's effective strength level after accounting for bonuses, like active
   * prayers, Void Knight equipment, etc.
   *
   * <p>The logic within this method matches the formula provided by the wiki:
   * https://oldschool.runescape.wiki/w/Damage_per_second/Melee#Step_one:_Calculate_the_effective_strength_level
   */
  public int calculateEffectiveStrengthLevel() {
    // We could cast to int to truncate the decimal, but keeping it as a double (and using
    // Math.floor()) ensures we don't lose precision in math that follows.
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

  private double getPrayerMultiplier(ImmutableMap<Prayer, Double> multipliers) {
    return BiStream.from(multipliers)
        .filterKeys(client::isPrayerActive)
        .values()
        .findFirst()
        .orElse(1.0);
  }

  // TODO: Parameterize the use of PlayerComposition to allow any arbitrary set of items to be
  // checked for Void items. This should probably be a new class representing a gear set instead of
  // depending on ItemContainer, since ItemContainer is too generic and new instances can't be
  // created by plugins.
  private boolean isWearingFullMeleeVoid() {
    PlayerComposition playerComposition = client.getLocalPlayer().getPlayerComposition();
    return VOID_MELEE_HELM_IDS.contains(playerComposition.getEquipmentId(KitType.HEAD))
        && VOID_TOP_IDS.contains(playerComposition.getEquipmentId(KitType.TORSO))
        && VOID_ROBE_IDS.contains(playerComposition.getEquipmentId(KitType.LEGS))
        && VOID_GLOVE_IDS.contains(playerComposition.getEquipmentId(KitType.HANDS));
  }
}
