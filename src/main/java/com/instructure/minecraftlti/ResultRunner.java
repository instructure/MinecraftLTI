package com.instructure.minecraftlti;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.imsglobal.pox.IMSPOXRequest;

public class ResultRunner implements Runnable {
  private User user;
  private Double grade;
  private String text;
  
  public ResultRunner(User user, Double grade, String text) {
    this.user = user;
    this.grade = grade;
    this.text = text;
  }
  
  public void run() {
    LTIConsumer consumer = user.getConsumer();
    
    try {
      String url = user.getServiceUrl();
      String key = consumer.getKey().toString();
      String secret = consumer.getSecret().toString();
      String sourcedid = user.getSourcedid();
      String gradeStr = grade == null ? "" : grade.toString();
      IMSPOXRequest.sendReplaceResult(url, key, secret, sourcedid, gradeStr, text);
    } catch (Exception e) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      MinecraftLTI.instance.getLogger().warning("Failed to send submission ("+e.getClass().getSimpleName()+"): "
        +sw.toString());
    }
  }
}
