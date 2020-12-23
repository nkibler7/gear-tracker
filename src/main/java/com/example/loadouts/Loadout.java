package com.example.loadouts;

import com.example.GearItem;
import java.util.Comparator;

public interface Loadout {

  String getLabel();

  Comparator<GearItem> defaultComparator();

  default Comparator<GearItem> head() {
    return defaultComparator();
  }

  default Comparator<GearItem> body() {
    return defaultComparator();
  }

  default Comparator<GearItem> legs() {
    return defaultComparator();
  }

  default Comparator<GearItem> boots() {
    return defaultComparator();
  }

  default Comparator<GearItem> weapon() {
    return defaultComparator();
  }

  default Comparator<GearItem> shield() {
    return defaultComparator();
  }

  default Comparator<GearItem> ammo() {
    return defaultComparator();
  }

  default Comparator<GearItem> cape() {
    return defaultComparator();
  }

  default Comparator<GearItem> gloves() {
    return defaultComparator();
  }

  default Comparator<GearItem> amulet() {
    return defaultComparator();
  }

  default Comparator<GearItem> ring() {
    return defaultComparator();
  }
}
