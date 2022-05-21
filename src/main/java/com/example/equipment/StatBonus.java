package com.example.equipment;

import java.util.function.Function;
import net.runelite.http.api.item.ItemEquipmentStats;

enum StatBonus {
  STAB_ATTACK(ItemEquipmentStats::getAstab),
  SLASH_ATTACK(ItemEquipmentStats::getAslash),
  CRUSH_ATTACK(ItemEquipmentStats::getAcrush),
  MAGIC_ATTACK(ItemEquipmentStats::getAmagic),
  RANGE_ATTACK(ItemEquipmentStats::getArange),
  STAB_DEFENSE(ItemEquipmentStats::getDstab),
  SLASH_DEFENSE(ItemEquipmentStats::getDslash),
  CRUSH_DEFENSE(ItemEquipmentStats::getDcrush),
  MAGIC_DEFENSE(ItemEquipmentStats::getDmagic),
  RANGE_DEFENSE(ItemEquipmentStats::getDrange),
  MELEE_STRENGTH(ItemEquipmentStats::getStr),
  RANGE_STRENGTH(ItemEquipmentStats::getRstr),
  MAGIC_DAMAGE(ItemEquipmentStats::getMdmg),
  PRAYER_BONUS(ItemEquipmentStats::getPrayer),
  ATTACK_SPEED(ItemEquipmentStats::getAspeed);

  private final Function<ItemEquipmentStats, Integer> statFunction;

  StatBonus(Function<ItemEquipmentStats, Integer> statFunction) {
    this.statFunction = statFunction;
  }
}
