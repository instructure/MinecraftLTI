package com.instructure.minecraftlti;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONObject;

public class DurationRunner implements Runnable {
  private User user;
  private long duration;
  
  public DurationRunner(User user, long duration) {
    this.user = user;
    this.duration = duration;
  }
  
  public void run() {
    JSONObject statement = buildStatement();
    
    LTIConsumer consumer = user.getConsumer();
    String key = consumer.getKey().toString();
    String secret = consumer.getSecret().toString();
    OAuthConsumer signer = new CommonsHttpOAuthConsumer(key, secret);
    
    try {
      HttpPost request = new HttpPost(user.getXapiUrl());
      request.setHeader("Content-Type", "application/json");
      request.setEntity(new StringEntity(statement.toString(), "UTF-8"));
      signer.sign(request);
      DefaultHttpClient client = new DefaultHttpClient();
      HttpResponse response = client.execute(request);
      if (response.getStatusLine().getStatusCode() >= 400) {
        throw new Exception(response.getStatusLine().getReasonPhrase());
      }
    } catch (Exception e) {
      MinecraftLTI.instance.getLogger().warning("Failed to send duration: "+e.getMessage());
    }
  }
  
  @SuppressWarnings("unchecked")
  private JSONObject buildStatement() {
    JSONObject statement = new JSONObject();
    
    // TODO: this account and object smell funny
    JSONObject actor = new JSONObject();
    statement.put("actor", actor);
    JSONObject account = new JSONObject();
    actor.put("account", account);
    account.put("homepage", user.getXapiUrl());
    account.put("name", user.getSourcedid());
    
    JSONObject verb = new JSONObject();
    statement.put("verb", verb);
    verb.put("id", "http://adlnet.gov/expapi/verbs/interacted");
    JSONObject display = new JSONObject();
    verb.put("display", display);
    display.put("en-US", "interacted");
    
    JSONObject object = new JSONObject();
    statement.put("object", object);
    object.put("id", "http://www.minecraft.net/");
    JSONObject definition = new JSONObject();
    object.put("definition", definition);
    JSONObject name = new JSONObject();
    definition.put("name", name);
    name.put("en-US", "Minecraft");
    
    JSONObject result = new JSONObject();
    statement.put("result", result);
    result.put("duration", String.format("PT%dS", duration));
    
    return statement;
  }
}
