package com.instructure.minecraftlti;

import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Logger;

public interface MinecraftLTIAdapter {
  public Path getStorageDirectory();
  public Logger getLogger();
  public String getServerAddress();
  public boolean isPlayerPresent(UUID uuid);
  public String getPlayerTp(UUID uuid);
  public void teleportPlayer(UUID uuid, Assignment assignment);
  public void setAssignmentLocation(UUID uuid, Assignment assignment);
  public void sendPlayerMessage(UUID uuid, String msg);
  public void sendPlayerError(UUID uuid, String msg);
}