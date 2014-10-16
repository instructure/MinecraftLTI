package com.instructure.minecraftlti;

import java.sql.SQLException;
import java.util.Date;
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
@Table(name="users", uniqueConstraints = {@UniqueConstraint(columnNames={"user_id", "tool_id", "consumer_id"})})
public class User {
  public static Dao<User, Integer> dao;
  
  @Id
  @GeneratedValue
  private Integer id;
  
  @Column(length=100, nullable=false)
  private String userId;
  
  @Column(length=100)
  private String toolId;
  
  @Column(nullable=false)
  private Integer consumerId;
  
  @Column(unique=true)
  private UUID uuid;
  
  @Column(length=32, nullable=false, unique=true)
  private String token;
  
  @Column(length=200)
  private String sourcedid;
  
  @Column(length=1023)
  private String serviceUrl;
  
  @Column(length=1023)
  private String xapiUrl;
  
  @Column
  private Date startDate;

  @Column
  private Boolean instructor;
  
  @Column
  private Integer assignmentId;
  
  @Version
  public long version;
  
  public User() {}
  
  public User(String userId, String toolId, LTIConsumer consumer, String token) {
    this.userId = userId;
    this.toolId = toolId;
    setConsumer(consumer);
    this.token = token;
  }
  
  public static User byId(int id) {
    try {
      return User.dao.queryForId(id);
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
  }
  
  public static User byColumn(String name, Object value) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put(name, value);
    return byColumns(map);
  }
  
  public static User byColumns(Map<String, Object> constraints) {
    try {
      Where<User, Integer> where = User.dao.queryBuilder().where();
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
      return User.dao.queryForFirst(where.prepare());
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
  }
  
  public static User byUuid(UUID uuid) {
    return User.byColumn("uuid", uuid);
  }
  
  public static User byToken(String token) {
    return User.byColumn("token", token);
  }
  
  public int getId() {
    return id;
  }
  
  public void setId(int id) {
    this.id = id;
  }
  
  public String getUserId() {
    return userId;
  }
  
  public void setUserId(String userId) {
    this.userId = userId;
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
  
  public UUID getUuid() {
    return uuid;
  }
  
  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }
  
  public String getToken() {
    return token;
  }
  
  public void setToken(String token) {
    this.token = token;
  }
  
  public String getSourcedid() {
    return sourcedid;
  }
  
  public void setSourcedid(String sourcedid) {
    this.sourcedid = sourcedid;
  }
  
  public Boolean getInstructor() {
    return instructor;
  }
  
  public void setInstructor(Boolean instructor) {
    this.instructor = instructor;
  }
  
  public String getServiceUrl() {
    return serviceUrl;
  }
  
  public void setServiceUrl(String serviceUrl) {
    this.serviceUrl = serviceUrl;
  }
  
  public String getXapiUrl() {
    return xapiUrl;
  }
  
  public void setXapiUrl(String xapiUrl) {
    this.xapiUrl = xapiUrl;
  }
  
  public Date getStartDate() {
    return startDate;
  }
  
  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }
  
  public void setStartDate() {
    setStartDate(new Date());
  }
  
  public Integer getAssignmentId() {
    return assignmentId;
  }
  
  public Assignment getAssignment() {
    if (getAssignmentId() == null) {return null;}
    return Assignment.byId(getAssignmentId());
  }
  
  public void setAssignmentId(Integer assignmentId) {
    this.assignmentId = assignmentId;
  }
  
  public void setAssignment(Assignment assignment) {
    setAssignmentId(assignment == null ? null : assignment.getId());
  }
  
  public long getVersion() {
    return version;
  }
  
  public void setVersion(long version) {
    this.version = version;
  }
  
  public void assignmentAction(String effect) {
    MinecraftLTIAdapter adapter = MinecraftLTI.instance.adapter;
    if (!adapter.isPlayerPresent(uuid)) {throw new IllegalStateException("Cannot find player.");}
    Assignment assignment = getAssignment();
    if (assignment == null) {throw new IllegalStateException("No current assignment.");}
    
    if (effect.equals("begin")) {
      if (assignment.getWorldName() == null) {throw new IllegalStateException("This assignment has no location.");}
      adapter.teleportPlayer(uuid, assignment);
      adapter.sendPlayerMessage(uuid, "Began assignment.");
      return;
    }
    if (effect.equals("submit")) {
      submitResult(adapter.getPlayerTp(uuid));
      adapter.sendPlayerMessage(uuid, "Submitted assignment.");
      return;
    }
    if (effect.equals("set")) {
      if (!getInstructor()) {throw new IllegalStateException("Only instructors may use this command.");}
      adapter.setAssignmentLocation(uuid, assignment);
      assignment.save();
      adapter.sendPlayerMessage(uuid, "Set assignment location.");
      return;
    }
    throw new IllegalStateException("Accepted actions: begin, submit, set");
  }
  
  public void grade(String result) {
    double grade = Double.parseDouble(result);
    if (grade < 0 || grade > 1) {
      MinecraftLTI.instance.getLogger().warning("Grade must be between 0 and 1.");
      return;
    }

    Assignment assignment = getAssignment();
    if (assignment == null) {
      MinecraftLTI.instance.getLogger().warning("Player not in an assignment.");
      return;
    }
    
    if (submitResult(grade)) {
      MinecraftLTI.instance.adapter.sendPlayerMessage(uuid, String.format("Received grade: %.0f%%", grade*100));
    }
  }
  
  public void register(UUID uuid) {
    User existing = User.byUuid(uuid);
    if (existing != null) {
      existing.setUuid(null);
      existing.save();
    }
    setUuid(uuid);
    setStartDate();
    save();
  }
  
  public Boolean submitResult(String text) {
    return submitResult(null, text);    
  }
  
  public Boolean submitResult(double grade) {
    return submitResult(grade, null);
  }
  
  public Boolean submitResult(Double grade, String text) {
    if (getSourcedid() == null) {return false;}
    ResultRunner runner = new ResultRunner(this, grade, text);
    Thread t = new Thread(runner);
    t.start();
    return true;
  }
  
  public Boolean handleQuit() {
    if (getXapiUrl() == null) {return false;}
    long duration = ((new Date()).getTime() - getStartDate().getTime())/1000;
    DurationRunner runner = new DurationRunner(this, duration);
    Thread t = new Thread(runner);
    t.start();
    return true;
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
