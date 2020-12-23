package com.example;

import com.example.loadouts.Loadout;
import com.google.common.collect.ImmutableMap;
import net.runelite.api.EquipmentInventorySlot;

import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Function;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;

public enum GearSlot {
  HEAD(EquipmentInventorySlot.HEAD, new Point(61, 5), Loadout::head),
  CAPE(EquipmentInventorySlot.CAPE, new Point(20, 44), Loadout::cape),
  AMULET(EquipmentInventorySlot.AMULET, new Point(61, 44), Loadout::amulet),
  WEAPON(EquipmentInventorySlot.WEAPON, new Point(5, 83), Loadout::weapon),
  BODY(EquipmentInventorySlot.BODY, new Point(61, 83), Loadout::body),
  SHIELD(EquipmentInventorySlot.SHIELD, new Point(117, 83), Loadout::shield),
  LEGS(EquipmentInventorySlot.LEGS, new Point(61, 123), Loadout::legs),
  GLOVES(EquipmentInventorySlot.GLOVES, new Point(5, 163), Loadout::gloves),
  BOOTS(EquipmentInventorySlot.BOOTS, new Point(61, 163), Loadout::boots),
  RING(EquipmentInventorySlot.RING, new Point(117, 163), Loadout::ring),
  AMMO(EquipmentInventorySlot.AMMO, new Point(102, 44), Loadout::ammo);

  /** Indexes all {@link GearSlot} values by {@link EquipmentInventorySlot#getSlotIdx()}. */
  private static final ImmutableMap<Integer, GearSlot> REVERSE_INDEX = createReverseIndex();

  private static final Dimension SLOT_DIMENSION = new Dimension(26, 26);

  private final EquipmentInventorySlot equipmentInventorySlot;
  private final Point drawableAreaTopLeft;
  private final Function<Loadout, Comparator<GearItem>> comparatorFunction;

  GearSlot(
      EquipmentInventorySlot equipmentInventorySlot,
      Point drawableAreaTopLeft,
      Function<Loadout, Comparator<GearItem>> comparatorFunction) {
    this.equipmentInventorySlot = equipmentInventorySlot;
    this.drawableAreaTopLeft = drawableAreaTopLeft;
    this.comparatorFunction = comparatorFunction;
  }

  public static GearSlot forIndex(int index) {
    return REVERSE_INDEX.get(index);
  }

  public EquipmentInventorySlot getEquipmentInventorySlot() {
    return equipmentInventorySlot;
  }

  public Rectangle getDrawableArea() {
    return new Rectangle(drawableAreaTopLeft, SLOT_DIMENSION);
  }

  public Comparator<GearItem> getComparator(Loadout loadout) {
    return comparatorFunction.apply(loadout);
  }

  private static ImmutableMap<Integer, GearSlot> createReverseIndex() {
    ImmutableMap<EquipmentInventorySlot, GearSlot> equipmentToGearSlot =
        Arrays.stream(GearSlot.values())
            .collect(toImmutableMap(GearSlot::getEquipmentInventorySlot, identity()));
    return Arrays.stream(EquipmentInventorySlot.values())
        .collect(toImmutableMap(EquipmentInventorySlot::getSlotIdx, equipmentToGearSlot::get));
  }
}
