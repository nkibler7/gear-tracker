package com.example;

import com.google.auto.value.AutoValue;
import net.runelite.http.api.item.ItemEquipmentStats;

@AutoValue
public abstract class GearItem {

  public abstract int id();

  public abstract String name();

  public abstract GearSlot slot();

  public abstract ItemEquipmentStats stats();

  public static GearItem create(int id, String name, ItemEquipmentStats stats) {
    GearSlot slot = GearSlot.forIndex(stats.getSlot());
    return new AutoValue_GearItem(id, name, slot, stats);
  }
}
