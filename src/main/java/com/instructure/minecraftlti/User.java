package com.instructure.minecraftlti;

import java.util.Date;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotEmpty;
import com.avaje.ebean.validation.NotNull;

@Entity()
@Table(name="users", uniqueConstraints = {@UniqueConstraint(columnNames={"user_id", "tool_id", "consumer_id"})})
public class User {
  @Id
  private int id;
  
  @Length(max=100)
  @NotEmpty
  private String userId;
  
  @Length(max=100)
  @Nullable
  private String toolId;
  
  @NotNull
  private int consumerId;
  
  @Nullable
  @Column(unique=true)
  private UUID uuid;
  
  @Length(max=32)
  @NotEmpty
  @Column(unique=true)
  private String token;
  
  @Length(max=200)
  @Nullable
  private String sourcedid;
  
  @Length(max=1023)
  @Nullable
  private String serviceUrl;
  
  @Length(max=1023)
  @Nullable
  private String xapiUrl;
  
  @Nullable
  private Date startDate;
  
  @Nullable
  private Boolean instructor;
  
  @Nullable
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
  
  private static ExpressionList<User> where() {
    return MinecraftLTI.getDb().find(User.class).where();
  }
  
  public static User byUuid(UUID uuid) {
    return User.where().eq("uuid", uuid).findUnique();
  }
  
  public static User byToken(String token) {
    return User.where().eq("token", token).findUnique();
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
    return MinecraftLTI.getDb().find(LTIConsumer.class).where().eq("id", getConsumerId()).findUnique();
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
    return MinecraftLTI.getDb().find(Assignment.class).where().eq("id", getAssignmentId()).findUnique();
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
    if (getSourcedid() == null || getXapiUrl() == null) {return false;}
    long duration = ((new Date()).getTime() - getStartDate().getTime())/1000;
    DurationRunner runner = new DurationRunner(this, duration);
    Thread t = new Thread(runner);
    t.start();
    return true;
  }
  
  public void save() {
    MinecraftLTI.getDb().save(this);
  }
}
