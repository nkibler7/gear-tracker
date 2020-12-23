package com.example.loadouts;

import com.example.GearItem;
import net.runelite.api.ItemID;

import java.util.Comparator;

final class SlayerMeleeLoadout implements Loadout {

  @Override
  public String getLabel() {
    return "Slayer (Melee)";
  }

  @Override
  public Comparator<GearItem> defaultComparator() {
    return Comparator.<GearItem, Integer>comparing(gearItem -> gearItem.stats().getAstab())
        .thenComparing(gearItem -> gearItem.stats().getAspeed())
        .thenComparing(gearItem -> gearItem.stats().getDcrush())
        .thenComparing(gearItem -> gearItem.stats().getDslash())
        .thenComparing(gearItem -> gearItem.stats().getDstab());
  }

  @Override
  public Comparator<GearItem> head() {
    return ExplicitGearComparator.of(ItemID.SLAYER_HELMET_I, ItemID.SLAYER_HELMET)
        .thenComparing(defaultComparator());
  }
}
