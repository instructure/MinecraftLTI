package com.instructure.minecraftlti;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import com.avaje.ebean.validation.NotNull;

@Entity()
@Table(name="consumers", uniqueConstraints = {@UniqueConstraint(columnNames={"key"})})
public class LTIConsumer {
  @Id
  private int id;

  @NotNull
  private UUID key;

  @NotNull
  private UUID secret;
  
  @Version
  public long version;

  public LTIConsumer() {}

  public LTIConsumer(UUID key, UUID secret) {
    this.key = key;
    this.secret = secret;
  }

  public static LTIConsumer random() {
    return new LTIConsumer(UUID.randomUUID(), UUID.randomUUID());
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public UUID getKey() {
    return key;
  }

  public void setKey(UUID key) {
    this.key = key;
  }

  public UUID getSecret() {
    return secret;
  }

  public void setSecret(UUID secret) {
    this.secret = secret;
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
