package com.example.equipment;

import com.google.auto.value.AutoValue;
import net.runelite.http.api.item.ItemEquipmentStats;

@AutoValue
abstract class WearableItem {

  abstract int itemId();

  abstract ItemEquipmentStats itemEquipmentStats();

  public static WearableItem create(int itemId, ItemEquipmentStats itemEquipmentStats) {
    return new AutoValue_WearableItem(itemId, itemEquipmentStats);
  }
}
