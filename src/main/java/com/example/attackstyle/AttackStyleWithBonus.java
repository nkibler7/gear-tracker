package com.example.attackstyle;

/**
 * A combination of {@link AttackStyle} and {@link MeleeBonusType} values that represents valid
 * combinations present on all weapon types in the game.
 */
public enum AttackStyleWithBonus {
  ACCURATE_SLASH(AttackStyle.ACCURATE, MeleeBonusType.SLASH),
  ACCURATE_CRUSH(AttackStyle.ACCURATE, MeleeBonusType.CRUSH),
  ACCURATE_STAB(AttackStyle.ACCURATE, MeleeBonusType.STAB),
  ACCURATE_UNSPECIFIED(AttackStyle.ACCURATE, MeleeBonusType.UNSPECIFIED),
  AGGRESSIVE_SLASH(AttackStyle.AGGRESSIVE, MeleeBonusType.SLASH),
  AGGRESSIVE_CRUSH(AttackStyle.AGGRESSIVE, MeleeBonusType.CRUSH),
  AGGRESSIVE_STAB(AttackStyle.AGGRESSIVE, MeleeBonusType.STAB),
  AGGRESSIVE_UNSPECIFIED(AttackStyle.AGGRESSIVE, MeleeBonusType.UNSPECIFIED),
  DEFENSIVE_SLASH(AttackStyle.DEFENSIVE, MeleeBonusType.SLASH),
  DEFENSIVE_CRUSH(AttackStyle.DEFENSIVE, MeleeBonusType.CRUSH),
  DEFENSIVE_STAB(AttackStyle.DEFENSIVE, MeleeBonusType.STAB),
  DEFENSIVE_UNSPECIFIED(AttackStyle.DEFENSIVE, MeleeBonusType.UNSPECIFIED),
  CONTROLLED_SLASH(AttackStyle.CONTROLLED, MeleeBonusType.SLASH),
  CONTROLLED_CRUSH(AttackStyle.CONTROLLED, MeleeBonusType.CRUSH),
  CONTROLLED_STAB(AttackStyle.CONTROLLED, MeleeBonusType.STAB),
  CONTROLLED_UNSPECIFIED(AttackStyle.CONTROLLED, MeleeBonusType.UNSPECIFIED),
  RANGING(AttackStyle.RANGING, MeleeBonusType.UNSPECIFIED),
  LONGRANGE(AttackStyle.LONGRANGE, MeleeBonusType.UNSPECIFIED),
  CASTING(AttackStyle.CASTING, MeleeBonusType.UNSPECIFIED),
  DEFENSIVE_CASTING(AttackStyle.DEFENSIVE_CASTING, MeleeBonusType.UNSPECIFIED),
  OTHER(AttackStyle.OTHER, MeleeBonusType.UNSPECIFIED);

  private final AttackStyle attackStyle;
  private final MeleeBonusType meleeBonusType;

  AttackStyleWithBonus(AttackStyle attackStyle, MeleeBonusType meleeBonusType) {
    this.attackStyle = attackStyle;
    this.meleeBonusType = meleeBonusType;
  }

  /** Returns the {@link AttackStyle} portion of this combination. */
  public AttackStyle getAttackStyle() {
    return attackStyle;
  }

  /** Returns the {@link MeleeBonusType} portion of this combination. */
  public MeleeBonusType getMeleeBonusType() {
    return meleeBonusType;
  }
}
