package com.example.loadouts;

import com.example.GearItem;
import com.google.common.collect.ImmutableList;
import java.util.Comparator;

/**
 * A comparator that prefers the given item IDs over other gear, always. If the given items to
 * compare are not in the given list, they are considered equal by this comparator.
 */
final class ExplicitGearComparator implements Comparator<GearItem> {

  private final ImmutableList<Integer> preferredItemIds;

  private ExplicitGearComparator(ImmutableList<Integer> preferredItemIds) {
    this.preferredItemIds = preferredItemIds;
  }

  /**
   * Returns a new comparator that prefers the given item IDs over other gear.
   *
   * <p>The order of the {@code itemIds} matters; the first item is the most preferred item.
   */
  public static ExplicitGearComparator of(Integer... itemIds) {
    return new ExplicitGearComparator(ImmutableList.copyOf(itemIds));
  }

  @Override
  public int compare(GearItem o1, GearItem o2) {
    for (int itemId : preferredItemIds) {
      if (o1.id() == itemId) {
        return 1;
      } else if (o2.id() == itemId) {
        return -1;
      }
    }

    return 0;
  }
}
