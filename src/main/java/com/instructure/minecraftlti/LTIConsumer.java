package com.instructure.minecraftlti;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
@Table(name="consumers", uniqueConstraints = {@UniqueConstraint(columnNames={"key"})})
public class LTIConsumer {
  public static Dao<LTIConsumer, Integer> dao;
  
  @Id
  @GeneratedValue
  public Integer id;

  @Column(nullable=false)
  public UUID key;

  @Column(nullable=false)
  public UUID secret;
  
  @Version
  public long version;

  public LTIConsumer() {}

  public LTIConsumer(UUID key, UUID secret) {
    this.key = key;
    this.secret = secret;
  }
  
  public static LTIConsumer byId(int id) {
    try {
      return LTIConsumer.dao.queryForId(id);
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
  }
  
  public static LTIConsumer byColumn(String name, Object value) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put(name, value);
    return byColumns(map);
  }
  
  public static LTIConsumer byColumns(Map<String, Object> constraints) {
    try {
      Where<LTIConsumer, Integer> where = LTIConsumer.dao.queryBuilder().where();
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
      return LTIConsumer.dao.queryForFirst(where.prepare());
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
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
