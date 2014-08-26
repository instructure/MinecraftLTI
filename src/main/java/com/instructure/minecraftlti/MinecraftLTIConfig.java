package com.instructure.minecraftlti;

public class MinecraftLTIConfig {
  public int port;
  public static MinecraftLTIConfig createDefault() {
    MinecraftLTIConfig config = new MinecraftLTIConfig();
    config.port = 8133;
    return config;
  }
}
