package com.example.attackstyle;

import static com.example.attackstyle.AttackStyleWithBonus.ACCURATE_CRUSH;
import static com.example.attackstyle.AttackStyleWithBonus.ACCURATE_SLASH;
import static com.example.attackstyle.AttackStyleWithBonus.ACCURATE_STAB;
import static com.example.attackstyle.AttackStyleWithBonus.ACCURATE_UNSPECIFIED;
import static com.example.attackstyle.AttackStyleWithBonus.AGGRESSIVE_CRUSH;
import static com.example.attackstyle.AttackStyleWithBonus.AGGRESSIVE_SLASH;
import static com.example.attackstyle.AttackStyleWithBonus.AGGRESSIVE_STAB;
import static com.example.attackstyle.AttackStyleWithBonus.AGGRESSIVE_UNSPECIFIED;
import static com.example.attackstyle.AttackStyleWithBonus.CASTING;
import static com.example.attackstyle.AttackStyleWithBonus.CONTROLLED_CRUSH;
import static com.example.attackstyle.AttackStyleWithBonus.CONTROLLED_SLASH;
import static com.example.attackstyle.AttackStyleWithBonus.CONTROLLED_STAB;
import static com.example.attackstyle.AttackStyleWithBonus.CONTROLLED_UNSPECIFIED;
import static com.example.attackstyle.AttackStyleWithBonus.DEFENSIVE_CASTING;
import static com.example.attackstyle.AttackStyleWithBonus.DEFENSIVE_CRUSH;
import static com.example.attackstyle.AttackStyleWithBonus.DEFENSIVE_SLASH;
import static com.example.attackstyle.AttackStyleWithBonus.DEFENSIVE_STAB;
import static com.example.attackstyle.AttackStyleWithBonus.DEFENSIVE_UNSPECIFIED;
import static com.example.attackstyle.AttackStyleWithBonus.LONGRANGE;
import static com.example.attackstyle.AttackStyleWithBonus.OTHER;
import static com.example.attackstyle.AttackStyleWithBonus.RANGING;

import java.util.Optional;
import net.runelite.api.Varbits;

/**
 * The total set of unique weapon types in the game.
 *
 * <p>This was originally forked from the "Attack Styles" core plugin, but modified to include melee
 * bonus types and expanded to support newer weapon types.
 *
 * <p>Note: This list will likely become outdated as new weapons are introduced to the game.
 */
enum WeaponType {
  TYPE_0(ACCURATE_CRUSH, AGGRESSIVE_CRUSH, null, DEFENSIVE_CRUSH),
  TYPE_1(ACCURATE_SLASH, AGGRESSIVE_SLASH, AGGRESSIVE_CRUSH, DEFENSIVE_SLASH),
  TYPE_2(ACCURATE_CRUSH, AGGRESSIVE_CRUSH, null, DEFENSIVE_CRUSH),
  TYPE_3(RANGING, RANGING, null, LONGRANGE),
  TYPE_4(ACCURATE_SLASH, AGGRESSIVE_SLASH, CONTROLLED_STAB, DEFENSIVE_SLASH),
  TYPE_5(RANGING, RANGING, null, LONGRANGE),
  TYPE_6(AGGRESSIVE_SLASH, RANGING, CASTING, null),
  TYPE_7(RANGING, RANGING, null, LONGRANGE),
  TYPE_8(OTHER, AGGRESSIVE_CRUSH, null, null),
  TYPE_9(ACCURATE_SLASH, AGGRESSIVE_SLASH, CONTROLLED_STAB, DEFENSIVE_SLASH),
  TYPE_10(ACCURATE_SLASH, AGGRESSIVE_SLASH, AGGRESSIVE_CRUSH, DEFENSIVE_SLASH),
  TYPE_11(ACCURATE_STAB, AGGRESSIVE_STAB, AGGRESSIVE_CRUSH, DEFENSIVE_STAB),
  TYPE_12(CONTROLLED_STAB, AGGRESSIVE_SLASH, null, DEFENSIVE_STAB),
  TYPE_13(ACCURATE_CRUSH, AGGRESSIVE_CRUSH, null, DEFENSIVE_CRUSH),
  TYPE_14(ACCURATE_SLASH, AGGRESSIVE_SLASH, AGGRESSIVE_CRUSH, DEFENSIVE_SLASH),
  TYPE_15(CONTROLLED_STAB, CONTROLLED_SLASH, CONTROLLED_CRUSH, DEFENSIVE_STAB),
  TYPE_16(ACCURATE_CRUSH, AGGRESSIVE_CRUSH, CONTROLLED_STAB, DEFENSIVE_CRUSH),
  TYPE_17(ACCURATE_STAB, AGGRESSIVE_STAB, AGGRESSIVE_SLASH, DEFENSIVE_STAB),
  TYPE_18(ACCURATE_CRUSH, AGGRESSIVE_CRUSH, null, DEFENSIVE_CRUSH, CASTING, DEFENSIVE_CASTING),
  TYPE_19(RANGING, RANGING, null, LONGRANGE),
  TYPE_20(ACCURATE_SLASH, CONTROLLED_SLASH, null, DEFENSIVE_SLASH),
  // TODO: Figure this one out.
  TYPE_21(
      ACCURATE_UNSPECIFIED,
      AGGRESSIVE_UNSPECIFIED,
      null,
      DEFENSIVE_UNSPECIFIED,
      CASTING,
      DEFENSIVE_CASTING),
  // TODO: Figure this one out.
  TYPE_22(
      ACCURATE_UNSPECIFIED, AGGRESSIVE_UNSPECIFIED, AGGRESSIVE_UNSPECIFIED, DEFENSIVE_UNSPECIFIED),
  TYPE_23(CASTING, CASTING, null, DEFENSIVE_CASTING),
  TYPE_24(ACCURATE_STAB, AGGRESSIVE_SLASH, CONTROLLED_CRUSH, DEFENSIVE_STAB),
  // TODO: Figure this one out.
  TYPE_25(CONTROLLED_UNSPECIFIED, AGGRESSIVE_UNSPECIFIED, null, DEFENSIVE_UNSPECIFIED),
  TYPE_26(AGGRESSIVE_CRUSH, AGGRESSIVE_CRUSH, null, AGGRESSIVE_CRUSH),
  TYPE_27(ACCURATE_CRUSH, null, null, OTHER),
  // TODO: Figure this one out.
  TYPE_28(null, null, null, null),
  TYPE_29(ACCURATE_STAB, AGGRESSIVE_STAB, AGGRESSIVE_CRUSH, DEFENSIVE_STAB);

  private final AttackStyleWithBonus[] attackStyles;

  WeaponType(AttackStyleWithBonus... attackStyles) {
    this.attackStyles = attackStyles;
  }

  /** Returns the set of {@link AttackStyleWithBonus} values associated with this weapon type. */
  public AttackStyleWithBonus[] getAttackStylesWithBonuses() {
    return attackStyles;
  }

  /**
   * Optionally returns the {@link WeaponType} associated with the given ID, which is a varbit keyed
   * by {@link Varbits#EQUIPPED_WEAPON_TYPE}. If there is no weapon type associated with the given
   * ID, {@link Optional#empty()} is returned.
   */
  public static Optional<WeaponType> getWeaponType(int id) {
    if (id >= 0 && id < WeaponType.values().length) {
      return Optional.of(WeaponType.values()[id]);
    }
    return Optional.empty();
  }
}
