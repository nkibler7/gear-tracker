package com.example;

import com.example.loadouts.Loadout;
import com.example.loadouts.LoadoutPanel;
import com.example.loadouts.LoadoutSelectionListener;
import com.example.loadouts.LoadoutSelector;
import net.runelite.client.ui.PluginPanel;

import java.awt.image.BufferedImage;

public final class GearPanel extends PluginPanel {

  private final LoadoutSelector loadoutSelector;
  private final LoadoutPanel loadoutPanel;

  protected GearPanel() {
    super();

    loadoutPanel = new LoadoutPanel();
    loadoutSelector = new LoadoutSelector();

    add(loadoutSelector);
    add(loadoutPanel);
  }

  public void addLoadoutSelectionListener(LoadoutSelectionListener listener) {
    loadoutSelector.addListener(listener);
  }

  public Loadout getSelectedLoadout() {
    return (Loadout) loadoutSelector.getSelectedItem();
  }

  public void drawImageInSlot(BufferedImage image, GearSlot slot) {
    loadoutPanel.drawImageInSlot(image, slot);
  }
}
