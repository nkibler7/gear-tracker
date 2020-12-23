package com.example.loadouts;

import com.google.common.collect.ImmutableList;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public final class LoadoutSelector extends JComboBox<Loadout> implements ActionListener {

  private static final ImmutableList<Loadout> LOADOUTS =
      ImmutableList.of(new SlayerMeleeLoadout(), new SlayerRangeLoadout());

  private final ArrayList<LoadoutSelectionListener> listeners = new ArrayList<>();

  public LoadoutSelector() {
    addActionListener(this);
    setRenderer(new LoadoutRenderer());
    LOADOUTS.forEach(this::addItem);
  }

  public void addListener(LoadoutSelectionListener listener) {
    listeners.add(listener);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == this) {
      Loadout selectedLoadout = (Loadout) getSelectedItem();
      listeners.forEach(listener -> listener.onLoadoutSelected(selectedLoadout));
    }
  }

  private static final class LoadoutRenderer implements ListCellRenderer<Loadout> {

    @Override
    public Component getListCellRendererComponent(
        JList<? extends Loadout> list,
        Loadout value,
        int index,
        boolean isSelected,
        boolean cellHasFocus) {
      JLabel label = new JLabel(value.getLabel());
      label.setBorder(new EmptyBorder(5, 5, 5, 0));

      if (isSelected) {
        label.setBackground(ColorScheme.DARK_GRAY_COLOR);
        label.setForeground(Color.WHITE);
      } else {
        label.setBackground(list.getBackground());
        label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
      }

      return label;
    }
  }
}
