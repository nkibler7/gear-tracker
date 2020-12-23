package com.example;

import com.example.loadouts.Loadout;
import com.google.common.collect.ImmutableMap;
import net.runelite.api.Item;
import net.runelite.client.game.ItemManager;
import net.runelite.http.api.item.ItemStats;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;

final class GearCache {

  private final ItemManager itemManager;
  private final ArrayList<GearItem> availableGear = new ArrayList<>();

  @Inject
  GearCache(ItemManager itemManager) {
    this.itemManager = itemManager;
  }

  void add(Item item) {
    int id = itemManager.canonicalize(item.getId());
    ItemStats stats = itemManager.getItemStats(id, false);
    if (stats == null || !stats.isEquipable()) {
      return;
    }

    String name = itemManager.getItemComposition(id).getName();
    availableGear.add(GearItem.create(id, name, stats.getEquipment()));
  }

  ImmutableMap<GearSlot, GearItem> getBisGear(Loadout loadout) {
    HashMap<GearSlot, GearItem> bisGear = new HashMap<>();

    for (GearItem item : availableGear) {
      GearItem currentBisItem = bisGear.get(item.slot());
      if (currentBisItem == null) {
        bisGear.put(item.slot(), item);
        continue;
      }

      int comparisonResult = item.slot().getComparator(loadout).compare(currentBisItem, item);
      if (comparisonResult < 0) {
        bisGear.put(item.slot(), item);
      }
    }

    return ImmutableMap.copyOf(bisGear);
  }
}
