package com.example.attackstyle;

import java.util.function.Function;
import net.runelite.http.api.item.ItemEquipmentStats;

/** The type of stat bonus associated with certain melee attack styles. */
public enum MeleeBonusType {
  UNSPECIFIED(unused -> 0, unused -> 0),
  SLASH(ItemEquipmentStats::getAslash, ItemEquipmentStats::getDslash),
  CRUSH(ItemEquipmentStats::getAcrush, ItemEquipmentStats::getDcrush),
  STAB(ItemEquipmentStats::getAstab, ItemEquipmentStats::getDstab);

  private final Function<ItemEquipmentStats, Integer> attackStatFunction;
  private final Function<ItemEquipmentStats, Integer> defenseStatFunction;

  MeleeBonusType(
      Function<ItemEquipmentStats, Integer> attackStatFunction,
      Function<ItemEquipmentStats, Integer> defenseStatFunction) {
    this.attackStatFunction = attackStatFunction;
    this.defenseStatFunction = defenseStatFunction;
  }

  /**
   * Returns a function that maps the given {@link ItemEquipmentStats} to the corresponding stat
   * bonus value when attacking.
   */
  public Function<ItemEquipmentStats, Integer> getAttackStatFunction() {
    return attackStatFunction;
  }

  /**
   * Returns a function that maps the given {@link ItemEquipmentStats} to the corresponding stat
   * bonus value when defending.
   */
  public Function<ItemEquipmentStats, Integer> getDefenseStatFunction() {
    return defenseStatFunction;
  }
}
