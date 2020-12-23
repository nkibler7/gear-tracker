package com.example;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class GearTrackerPluginTest {

  public static void main(String[] args) throws Exception {
    ExternalPluginManager.loadBuiltin(GearTrackerPlugin.class);
    RuneLite.main(args);
  }
}
