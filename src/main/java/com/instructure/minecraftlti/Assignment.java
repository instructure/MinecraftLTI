package com.instructure.minecraftlti;

import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotEmpty;
import com.avaje.ebean.validation.NotNull;

@Entity()
@Table(name="assignments", uniqueConstraints = {@UniqueConstraint(columnNames={"name", "context_id", "tool_id", "consumer_id"})})
public class Assignment {
  @Id
  private int id;

  @Length(max=100)
  @Nullable
  private String name;

  @Length(max=100)
  @NotEmpty
  private String contextId;

  @Length(max=100)
  @Nullable
  private String toolId;
  
  @NotNull
  private int consumerId;
  
  @Length(max=100)
  @Nullable
  private String worldName;
  
  @Nullable
  private double x;

  @Nullable
  private double y;

  @Nullable
  private double z;

  @Nullable
  private float pitch;

  @Nullable
  private float yaw;
  
  @Version
  public long version;
  
  public Assignment() {}
  
  public Assignment(String name, String contextId, String toolId, LTIConsumer consumer) {
    this.name = name;
    this.contextId = contextId;
    this.toolId = toolId;
    setConsumer(consumer);
  }
  
  public int getId() {
    return id;
  }
  
  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getContextId() {
    return contextId;
  }
  
  public void setContextId(String contextId) {
    this.contextId = contextId;
  }
  
  public String getToolId() {
    return toolId;
  }
  
  public void setToolId(String toolId) {
    this.toolId = toolId;
  }
  
  public int getConsumerId() {
    return consumerId;
  }
  
  public void setConsumerId(int consumerId) {
    this.consumerId = consumerId;
  }
  
  public LTIConsumer getConsumer() {
    return MinecraftLTI.getDb().find(LTIConsumer.class).where().eq("id", getConsumerId()).findUnique();
  }
  
  public void setConsumer(LTIConsumer consumer) {
    setConsumerId(consumer == null ? null : consumer.getId());
  }
  
  public String getWorldName() {
    return worldName;
  }

  public void setWorldName(String worldName) {
    this.worldName = worldName;
  }
  
  public double getX() {
    return x;
  }

  public void setX(double x) {
    this.x = x;
  }

  public double getY() {
    return y;
  }

  public void setY(double y) {
    this.y = y;
  }

  public double getZ() {
    return z;
  }

  public void setZ(double z) {
    this.z = z;
  }

  public float getPitch() {
    return pitch;
  }

  public void setPitch(float pitch) {
    this.pitch = pitch;
  }

  public float getYaw() {
    return yaw;
  }

  public void setYaw(float yaw) {
    this.yaw = yaw;
  }
  
  public void setLocation(String worldName, double x, double y, double z, float yaw, float pitch) {
    setWorldName(worldName);
    setX(x);
    setY(y);
    setZ(z);
    setYaw(yaw);
    setPitch(pitch);
  }
  
  public long getVersion() {
    return version;
  }
  
  public void setVersion(long version) {
    this.version = version;
  }
  
  public void save() {
    MinecraftLTI.getDb().save(this);
  }
}
