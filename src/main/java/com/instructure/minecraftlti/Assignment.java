package com.instructure.minecraftlti;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.Where;

@Entity()
@Table(name="assignments", uniqueConstraints = {@UniqueConstraint(columnNames={"name", "context_id", "tool_id", "consumer_id"})})
public class Assignment {
  public static Dao<Assignment, Integer> dao;
  
  @Id
  @GeneratedValue
  private Integer id;

  @Column(length=100)
  private String name;

  @Column(length=100, nullable=false)
  private String contextId;

  @Column(length=100)
  private String toolId;
  
  @Column(nullable=false)
  private Integer consumerId;
  
  @Column(length=100)
  private String worldName;
  
  @Column
  private double x;

  @Column
  private double y;

  @Column
  private double z;

  @Column
  private float pitch;

  @Column
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
  
  public static Assignment byId(int id) {
    try {
      return Assignment.dao.queryForId(id);
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
  }
  
  public static Assignment byColumn(String name, Object value) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put(name, value);
    return byColumns(map);
  }
  
  public static Assignment byColumns(Map<String, Object> constraints) {
    try {
      Where<Assignment, Integer> where = Assignment.dao.queryBuilder().where();
      Boolean first = true;
      for (Map.Entry<String, Object> constraint: constraints.entrySet()) {
        if (!first) {
          where = where.and();
        }
        if (constraint.getValue() == null) {
          where = where.isNull(constraint.getKey());
        } else {
        	where = where.eq(constraint.getKey(), constraint.getValue());
        }
        first = false;
      }
      return Assignment.dao.queryForFirst(where.prepare());
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
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
    return LTIConsumer.byId(getConsumerId());
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
    try {
      if (this.id == null) {
        dao.create(this);
      } else {
        dao.update(this);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
