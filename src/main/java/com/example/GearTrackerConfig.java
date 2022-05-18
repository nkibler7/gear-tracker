package com.example;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("gear-tracker")
public interface GearTrackerConfig extends Config {

  @ConfigItem(
      keyName = "greeting",
      name = "Welcome Greeting",
      description = "The message to show to the user when they login")
  default String greeting() {
    return "Hello";
  }

  @ConfigItem(
      keyName = "gearCache",
      name = "",
      description = "",
      hidden = true
  )
  default byte[] gearCache()
  {
    return new byte[0];
  }

  @ConfigItem(
      keyName = "gearCache",
      name = "",
      description = ""
  )
  void gearCache(byte[] gearCache);

  @ConfigItem(
      keyName = "isOnSlayerTask",
      name = "",
      description = "",
      hidden = true
  )
  default boolean isOnSlayerTask() {
    return false;
  }

  @ConfigItem(
      keyName = "isOnSlayerTask",
      name = "",
      description = ""
  )
  void isOnSlayerTask(boolean isOnSlayerTask);
}
