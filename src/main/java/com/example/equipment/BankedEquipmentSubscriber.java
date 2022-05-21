package com.example.equipment;

import com.google.common.collect.ImmutableList;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.WidgetID;
import net.runelite.client.eventbus.Subscribe;

/** A subscriber for caching the player's banked equipment when the bank interface is open. */
public final class BankedEquipmentSubscriber {

  private final Client client;
  private final BankedEquipmentStore bankedEquipmentStore;

  @Inject
  BankedEquipmentSubscriber(Client client, BankedEquipmentStore bankedEquipmentStore) {
    this.client = client;
    this.bankedEquipmentStore = bankedEquipmentStore;
  }

  @Subscribe
  public void onWidgetLoaded(WidgetLoaded widgetLoaded) {
    if (widgetLoaded.getGroupId() != WidgetID.BANK_GROUP_ID) {
      return;
    }

    ItemContainer bankContainer = client.getItemContainer(InventoryID.BANK);
    ItemContainer inventoryContainer = client.getItemContainer(InventoryID.INVENTORY);
    ItemContainer equipmentContainer = client.getItemContainer(InventoryID.EQUIPMENT);


    bankedEquipmentStore.updateEquipment(bankContainer, inventoryContainer);
  }

  private static ImmutableList<Item> extractItems(ItemContainer... containers) {
    ImmutableList.<Item>Builder
  }
}
