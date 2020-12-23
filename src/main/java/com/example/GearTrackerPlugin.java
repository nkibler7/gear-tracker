package com.example;

import com.example.loadouts.Loadout;
import com.example.loadouts.LoadoutSelectionListener;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Map.Entry;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.WidgetID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;

@Slf4j
@PluginDescriptor(name = "Gear Tracker")
public class GearTrackerPlugin extends Plugin implements LoadoutSelectionListener {

  @Inject private Client client;
  @Inject private ClientToolbar clientToolbar;
  @Inject private ItemManager itemManager;
  @Inject private GearCache gearCache;

  private GearPanel gearPanel;

  @Override
  protected void startUp() throws Exception {
    gearPanel = new GearPanel();
    gearPanel.addLoadoutSelectionListener(this);

    BufferedImage icon = ImageIO.read(getClass().getResourceAsStream("gear_panel_icon.png"));
    clientToolbar.addNavigation(
        NavigationButton.builder()
            .tooltip("Gear Tracker")
            .priority(10)
            .icon(icon)
            .panel(gearPanel)
            .build());
  }

  @Subscribe
  public void onWidgetLoaded(WidgetLoaded widgetLoaded) {
    if (widgetLoaded.getGroupId() != WidgetID.BANK_GROUP_ID) {
      return;
    }

    ItemContainer bankContainer = client.getItemContainer(InventoryID.BANK);
    if (bankContainer == null) {
      return;
    }

    Arrays.asList(bankContainer.getItems()).forEach(gearCache::add);
    updateLoadout(gearPanel.getSelectedLoadout());
  }

  @Provides
  GearTrackerConfig provideConfig(ConfigManager configManager) {
    return configManager.getConfig(GearTrackerConfig.class);
  }

  @Override
  public void onLoadoutSelected(Loadout selectedLoadout) {
    updateLoadout(selectedLoadout);
  }

  private void updateLoadout(Loadout selectedLoadout) {
    log.info("Loading the following loadout: {}", selectedLoadout.getLabel());

    ImmutableMap<GearSlot, GearItem> bisGear = gearCache.getBisGear(selectedLoadout);
    for (Entry<GearSlot, GearItem> bisEntry : bisGear.entrySet()) {
      BufferedImage image = itemManager.getImage(bisEntry.getValue().id());
      gearPanel.drawImageInSlot(image, bisEntry.getKey());
      log.info("BiS item for {} slot is: {}", bisEntry.getKey().name(), bisEntry.getValue().name());
    }
  }
}
