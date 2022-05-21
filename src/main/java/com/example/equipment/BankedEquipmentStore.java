package com.example.equipment;

import com.google.common.collect.ImmutableList;
import java.util.HashSet;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.client.game.ItemManager;
import net.runelite.http.api.item.ItemStats;

/** A cache that contains all of a player's wearable equipment. */
@Slf4j
@Singleton
final class BankedEquipmentStore {

  private final Client client;
  private final ItemManager itemManager;
  private final HashSet<WearableItem> bankedEquipment = new HashSet<>();

  @Inject
  BankedEquipmentStore(Client client, ItemManager itemManager) {
    this.client = client;
    this.itemManager = itemManager;
  }

  /**
   * Updates the cached equipment to the wearable items contained within the given list.
   *
   * <p>This may be invoked from any thread, but note that this implementation obtains a lock on
   * this instance (via the "synchronized" keyword) along with all cache accesses so that they
   * always obtain the full, updated view of the player's equipment.
   */
  public synchronized void updateEquipment(ImmutableList<Item> items) {
    bankedEquipment.clear();

    for (Item item : items) {
      ItemComposition itemComposition = itemManager.getItemComposition(item.getId());

      // Only adds the item to the cache if it's *not* a placeholder. Without this, the call to
      // canonicalize() will convert the placeholder's ID to the real item ID.
      // TODO: Consider making this a user-selected option in the config.
      if (itemComposition.getPlaceholderTemplateId() == -1) {
        addItem(itemManager.canonicalize(item.getId()));
      }
    }
  }

  private void addItem(int itemId) {
    // Technically the value of allowNote doesn't matter since we already expect the given itemId to
    // be canonicalized (and thus, noted item IDs should not be given here).
    ItemStats itemStats = itemManager.getItemStats(itemId, /* allowNote= */ true);
    if (itemStats == null || !itemStats.isEquipable()) {
      return;
    }

    bankedEquipment.add(WearableItem.create(itemId, itemStats.getEquipment()));
  }
}
