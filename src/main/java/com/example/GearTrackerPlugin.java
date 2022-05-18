package com.example;

import com.example.attackstyle.AttackStyleSubscriber;
import com.example.dps.DpsCalculator;
import com.example.loadouts.Loadout;
import com.example.loadouts.LoadoutSelectionListener;
import com.example.npcs.NpcInfoCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.SpriteID;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.kit.KitType;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetPositionMode;
import net.runelite.api.widgets.WidgetSizeMode;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.SessionOpen;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.NPCManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;

@Slf4j
@PluginDescriptor(name = "Gear Tracker")
public class GearTrackerPlugin extends Plugin implements LoadoutSelectionListener {

  @Inject private Client client;
  @Inject private ClientToolbar clientToolbar;
  @Inject private ClientThread clientThread;
  @Inject private ItemManager itemManager;
  @Inject private NPCManager npcManager;
  @Inject private GearCache gearCache;
  @Inject private DpsCalculator dpsCalculator;
  @Inject private NpcInfoCache npcInfoCache;
  @Inject private AttackStyleSubscriber attackStyleSubscriber;
  @Inject private EventBus eventBus;

  private GearPanel gearPanel;

  @Override
  protected void startUp() throws Exception {
    eventBus.register(attackStyleSubscriber);
    npcInfoCache.fillCache();

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

  @Override
  protected void shutDown() throws IOException {
    npcInfoCache.shutDown();
  }

  @Subscribe
  public void onSessionOpen(SessionOpen sessionOpen) {
    gearCache.loadFromConfig();
  }

  @Subscribe
  public void onWidgetLoaded(WidgetLoaded widgetLoaded) {
    for (KitType kitType : KitType.values()) {
      log.info(
          "Equipment ID worn in KitType {}: {}",
          kitType.name(),
          client.getLocalPlayer().getPlayerComposition().getEquipmentId(kitType));
    }

    updateDpsText();
    if (widgetLoaded.getGroupId() != WidgetID.BANK_GROUP_ID) {
      return;
    }

    ItemContainer bankContainer = client.getItemContainer(InventoryID.BANK);
    if (bankContainer == null) {
      return;
    }

    Arrays.asList(bankContainer.getItems()).forEach(gearCache::add);
    updateLoadout(gearPanel.getSelectedLoadout());
    gearCache.saveToConfig();
  }

  @Provides
  GearTrackerConfig provideConfig(ConfigManager configManager) {
    return configManager.getConfig(GearTrackerConfig.class);
  }

  @Provides
  @GearTrackerExecutor
  ListeningExecutorService provideGearTrackerExecutor() {
    return MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));
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

  private void updateDpsText() {
    Widget equipmentStats = client.getWidget(WidgetInfo.EQUIPMENT_INVENTORY_ITEMS_CONTAINER);
    ItemContainer equipmentContainer = client.getItemContainer(InventoryID.EQUIPMENT);

    if (equipmentStats != null && !equipmentStats.isHidden() && equipmentContainer != null) {
      createDpsButtonIfNeeded();
      //        double dps =
      //            dpsCalculator.calculateMeleeDps(equipmentContainer, NpcID.GARGOYLE);
      //        String message = String.format("DPS against Gargoyles is: %f", dps);
      //        log.debug(message);
      //
      //        Widget dpsText = equipmentPanel.createChild(-1, WidgetType.TEXT);
      //
      //        dpsText.setOriginalX(6);
      //        dpsText.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER);
      //
      //        dpsText.setOriginalWidth(12);
      //        dpsText.setWidthMode(WidgetSizeMode.MINUS);
      //
      //        dpsText.setOriginalY(36);
      //        dpsText.setYPositionMode(WidgetPositionMode.ABSOLUTE_LEFT);
      //
      //        dpsText.setOriginalHeight(24);
      //        dpsText.setHeightMode(WidgetSizeMode.ABSOLUTE);
      //
      //        dpsText.setFontId(FontID.BOLD_12);
      //        dpsText.setTextColor(Color.WHITE.getRGB());
      //        dpsText.setText(message);
      //
      //        dpsText.setXTextAlignment(WidgetTextAlignment.LEFT);
      //        dpsText.setYTextAlignment(WidgetTextAlignment.CENTER);
      //
      //        dpsText.revalidate();
    }
  }

  private void createDpsButtonIfNeeded() {
    Widget setBonusButton = client.getWidget(84, 22);
    if (setBonusButton != null && !setBonusButton.isHidden()) {
      int space = 8;
      int newX = setBonusButton.getOriginalX() - (setBonusButton.getWidth() / 2) - space;
      setBonusButton.setOriginalX(newX);
      setBonusButton.revalidate();

      Widget equipmentPanel = setBonusButton.getParent().getParent();
      equipmentPanel.setChildren(new Widget[1]);

      Widget dpsLayer = equipmentPanel.createChild(-1, WidgetType.LAYER);
      Widget dpsButton = dpsLayer.createChild(-1, WidgetType.LAYER);
      dpsButton.setOriginalX(newX + setBonusButton.getWidth() + space);
      dpsButton.setOriginalY(setBonusButton.getOriginalY());
      dpsButton.setOriginalWidth(setBonusButton.getWidth());
      dpsButton.setOriginalHeight(setBonusButton.getHeight());

      Widget dpsButtonBackground = dpsButton.createChild(-1, WidgetType.GRAPHIC);
      dpsButtonBackground.setSpriteId(SpriteID.DIALOG_BACKGROUND_BRIGHTER);
      dpsButtonBackground.setSpriteTiling(true);
      dpsButtonBackground.setOriginalX(0);
      dpsButtonBackground.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER);
      dpsButtonBackground.setOriginalY(0);
      dpsButtonBackground.setYPositionMode(WidgetPositionMode.ABSOLUTE_CENTER);
      dpsButtonBackground.setOriginalWidth(2);
      dpsButtonBackground.setWidthMode(WidgetSizeMode.MINUS);
      dpsButtonBackground.setOriginalHeight(2);
      dpsButtonBackground.setHeightMode(WidgetSizeMode.MINUS);

      dpsLayer.revalidate();
    }
  }
}
