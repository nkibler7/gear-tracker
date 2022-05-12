package com.example;

import com.example.loadouts.Loadout;
import com.google.common.collect.ImmutableMap;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.runelite.client.game.ItemManager;
import net.runelite.http.api.item.ItemStats;

@Slf4j
final class GearCache {

  private final ItemManager itemManager;
  private final GearTrackerConfig config;
  private final HashSet<GearItem> availableGear = new HashSet<>();

  @Inject
  GearCache(ItemManager itemManager, GearTrackerConfig config) {
    this.itemManager = itemManager;
    this.config = config;
  }

  void loadFromConfig() {
    availableGear.clear();

    log.debug("Loading GearCache from config!");
    try (ByteArrayInputStream bis = new ByteArrayInputStream(config.gearCache())) {
      try (ObjectInputStream input = new ObjectInputStream(bis)) {
        while (true) {
          try {
            add(input.readInt());
          } catch (EOFException e) {
            break;
          }
        }
      }
    } catch (IOException e) {
      log.debug("Exception thrown while loading GearCache from config.", e);
    }
    log.debug("Loaded a total of {} items from config!", availableGear.size());
  }

  void saveToConfig() {
    byte[] bytes = new byte[0];
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
      try (ObjectOutputStream output = new ObjectOutputStream(bos)) {
        for (GearItem item : availableGear) {
          output.writeInt(item.id());
        }
        output.flush();
      }
      bytes = bos.toByteArray();
    } catch (IOException e) {
      log.debug("Exception thrown while saving GearCache to config.", e);
    }

    config.gearCache(bytes);
  }

  void add(Item item) {
    add(itemManager.canonicalize(item.getId()));
  }

  void add(int itemId) {
    ItemStats stats = itemManager.getItemStats(itemId, /* allowNote= */ false);
    if (stats == null || !stats.isEquipable()) {
      return;
    }

    String name = itemManager.getItemComposition(itemId).getName();
    availableGear.add(GearItem.create(itemId, name, stats.getEquipment()));
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
