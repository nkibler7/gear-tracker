package com.example.dps;

import com.example.GearTrackerConfig;
import com.github.nkibler7.osrswikiscraper.NpcInfo;
import com.google.common.collect.ImmutableSet;
import com.google.re2j.Pattern;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.PlayerComposition;
import net.runelite.api.kit.KitType;

/**
 * Calculates the multiplier to use in DPS calculations when including specific gear bonuses, like
 * Slayer Helms or Salve Amulets.
 */
final class GearBonusMultiplier {

  // TODO: Make this more robust, it should work at first but definitely confirm with real-world
  // wiki values.
  private static final Pattern UNDEAD_ATTRIBUTE_PATTERN = Pattern.compile("undead");
  private static final ImmutableSet<Integer> SLAYER_BONUS_IDS = createSlayerBonusIds();

  private final Client client;
  private final GearTrackerConfig gearTrackerConfig;

  @Inject
  GearBonusMultiplier(Client client, GearTrackerConfig gearTrackerConfig) {
    this.client = client;
    this.gearTrackerConfig = gearTrackerConfig;
  }

  /**
   * Returns the multiplier to use when including specific gear bonuses in DPS calculations, like
   * Slayer Helms or Salve Amulets. If there is no gear bonus to apply, then 1.0 is returned for
   * convenience.
   */
  public double getMultiplier(NpcInfo targetNpcInfo) {
    PlayerComposition playerComposition = client.getLocalPlayer().getPlayerComposition();
    if (isUndead(targetNpcInfo)) {
      int amuletId = playerComposition.getEquipmentId(KitType.AMULET);
      if (amuletId == ItemID.SALVE_AMULET || amuletId == ItemID.SALVE_AMULETI) {
        return (7.0 / 6.0);
      } else if (amuletId == ItemID.SALVE_AMULET_E || amuletId == ItemID.SALVE_AMULETEI) {
        return 1.2;
      }
    } else if (gearTrackerConfig.isOnSlayerTask()
        && SLAYER_BONUS_IDS.contains(playerComposition.getEquipmentId(KitType.HEAD))) {
      return (7.0 / 6.0);
    }

    return 1.0;
  }

  private static boolean isUndead(NpcInfo npcInfo) {
    return UNDEAD_ATTRIBUTE_PATTERN.matches(npcInfo.getAttributes());
  }

  private static ImmutableSet<Integer> createSlayerBonusIds() {
    return ImmutableSet.of(
        ItemID.BLACK_MASK,
        ItemID.BLACK_MASK_1,
        ItemID.BLACK_MASK_2,
        ItemID.BLACK_MASK_3,
        ItemID.BLACK_MASK_4,
        ItemID.BLACK_MASK_5,
        ItemID.BLACK_MASK_6,
        ItemID.BLACK_MASK_7,
        ItemID.BLACK_MASK_8,
        ItemID.BLACK_MASK_9,
        ItemID.BLACK_MASK_10,
        ItemID.BLACK_MASK_I,
        ItemID.BLACK_MASK_1_I,
        ItemID.BLACK_MASK_2_I,
        ItemID.BLACK_MASK_3_I,
        ItemID.BLACK_MASK_4_I,
        ItemID.BLACK_MASK_5_I,
        ItemID.BLACK_MASK_6_I,
        ItemID.BLACK_MASK_7_I,
        ItemID.BLACK_MASK_8_I,
        ItemID.BLACK_MASK_9_I,
        ItemID.BLACK_MASK_10_I,
        ItemID.SLAYER_HELMET,
        ItemID.BLACK_SLAYER_HELMET,
        ItemID.GREEN_SLAYER_HELMET,
        ItemID.HYDRA_SLAYER_HELMET,
        ItemID.PURPLE_SLAYER_HELMET,
        ItemID.RED_SLAYER_HELMET,
        ItemID.TURQUOISE_SLAYER_HELMET,
        ItemID.TWISTED_SLAYER_HELMET,
        ItemID.TZKAL_SLAYER_HELMET,
        ItemID.TZTOK_SLAYER_HELMET,
        ItemID.VAMPYRIC_SLAYER_HELMET,
        ItemID.SLAYER_HELMET_I,
        ItemID.BLACK_SLAYER_HELMET_I,
        ItemID.GREEN_SLAYER_HELMET_I,
        ItemID.HYDRA_SLAYER_HELMET_I,
        ItemID.PURPLE_SLAYER_HELMET_I,
        ItemID.RED_SLAYER_HELMET_I,
        ItemID.TURQUOISE_SLAYER_HELMET_I,
        ItemID.TWISTED_SLAYER_HELMET_I,
        ItemID.TZKAL_SLAYER_HELMET_I,
        ItemID.TZTOK_SLAYER_HELMET_I,
        ItemID.VAMPYRIC_SLAYER_HELMET_I);
  }
}
