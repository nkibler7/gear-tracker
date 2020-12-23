package com.example.loadouts;

import com.example.GearItem;
import net.runelite.api.ItemID;

import java.util.Comparator;

final class SlayerRangeLoadout implements Loadout {

  @Override
  public String getLabel() {
    return "Slayer (Range)";
  }

  @Override
  public Comparator<GearItem> defaultComparator() {
    return Comparator.<GearItem, Integer>comparing(gearItem -> gearItem.stats().getArange())
        .thenComparing(gearItem -> gearItem.stats().getDmagic());
  }

  @Override
  public Comparator<GearItem> head() {
    return ExplicitGearComparator.of(ItemID.SLAYER_HELMET_I).thenComparing(defaultComparator());
  }
}
