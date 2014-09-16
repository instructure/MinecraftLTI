package com.instructure.minecraftlti;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.session.HashSessionIdManager;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.floreysoft.jmte.Engine;
import com.floreysoft.jmte.encoder.XMLEncoder;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.logger.LocalLog;
import com.j256.ormlite.logger.Log;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class MinecraftLTI {
	private Server webserver = null;
	public MinecraftLTIAdapter adapter = null;
	public static final Engine jmte = Engine.createDefaultEngine();
  
  private static final Logger logger =
      Logger.getLogger(MinecraftLTI.class.getName());
  public static MinecraftLTI instance = null;

	
	public static void main(String[] args){
	  new MinecraftLTI();
	}
	 
	public MinecraftLTI() {
	  new MinecraftLTI(null);
	}
	
	public MinecraftLTI(MinecraftLTIAdapter adapter) {
	  this.adapter = adapter;
	  MinecraftLTI.instance = this;
	  MinecraftLTI.jmte.setEncoder(new XMLEncoder());
    saveDefaultConfig();
    setupDatabase();
    startWebserver();
  }
 
  public void close() {
    stopWebserver();
  }
  
  public Path getStorageDirectory() {
    if (adapter != null) {
      return adapter.getStorageDirectory();
    } else {
      try {
        Path jarPath = Paths.get(MinecraftLTI.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path storagePath = jarPath.getParent().resolve("MinecraftLTI");
        storagePath.toFile().mkdir();
        return storagePath;
      } catch (URISyntaxException e) {
        return null;
      }
    }
  }
  
  private File getConfigFile() {
    Path configPath = getStorageDirectory().resolve("config.json");
    File configFile = new File(configPath.toString());
    return configFile;
  }
  
  @SuppressWarnings("unchecked")
  private JSONObject createDefaultConfig() {
    JSONObject obj = new JSONObject();
    obj.put("port", "8133");
    return obj;
  }
  
  private void saveDefaultConfig() {
    File configFile = getConfigFile();
    try {
      if (!configFile.createNewFile()) {return;}
      FileWriter fw = new FileWriter(configFile);
      JSONObject config = createDefaultConfig();
      JsonWriter jw = new JsonWriter();
      config.writeJSONString(jw);
      String json = jw.toString();
      fw.write(json);
      fw.close();
    } catch (IOException e) {
      getLogger().warning("Failed to create config.");
    }
  }
  
  private JSONObject getConfig() {
    File configFile = getConfigFile();
    try {
      byte[] encoded = Files.readAllBytes(Paths.get(configFile.toURI()));
      String contents = new String(encoded, StandardCharsets.UTF_8);
      JSONObject config = (JSONObject)JSONValue.parse(contents);
      return config;
    } catch (IOException e) {
      getLogger().warning("Failed to read config.");
      return null;
    }
  }
  
  public List<Class<?>> getDatabaseClasses() {
      List<Class<?>> list = new ArrayList<Class<?>>();
      list.add(User.class);
      list.add(Assignment.class);
      list.add(LTIConsumer.class);
      return list;
  }
  
  private void setupDatabase() {
    try {
      System.setProperty(LocalLog.LOCAL_LOG_LEVEL_PROPERTY, Log.Level.WARNING.toString());
      
      String databaseUrl = "jdbc:h2:file:"+getStorageDirectory().resolve("database");
      ConnectionSource connectionSource;
        connectionSource = new JdbcConnectionSource(databaseUrl);
      
      TableUtils.createTableIfNotExists(connectionSource, Assignment.class);
      Assignment.dao = DaoManager.createDao(connectionSource, Assignment.class);
      
      TableUtils.createTableIfNotExists(connectionSource, LTIConsumer.class);
      LTIConsumer.dao = DaoManager.createDao(connectionSource, LTIConsumer.class);;
      
      TableUtils.createTableIfNotExists(connectionSource, User.class);
      User.dao = DaoManager.createDao(connectionSource, User.class);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
  
  private void startWebserver() {
    JSONObject config = getConfig();
    int port = Integer.parseInt((String)config.get("port"));

    org.eclipse.jetty.util.log.Log.setLog(new NullLogger());
    
    webserver = new Server(port);
    webserver.setSessionIdManager(new HashSessionIdManager());
    
    WebAppContext dynamicHandler = new WebAppContext();
    String webDir = this.getClass().getClassLoader().getResource("web").toExternalForm();
    dynamicHandler.setResourceBase(webDir);
    
    dynamicHandler.addServlet(new ServletHolder(new LTIServlet(this)),"/lti");
    dynamicHandler.addServlet(new ServletHolder(new TokenServlet(this)),"/token");
    dynamicHandler.addServlet(new ServletHolder(new AssignmentServlet(this)),"/assignment");
    dynamicHandler.addServlet(new ServletHolder(new ConsumerServlet(this)),"/consumer");
    dynamicHandler.addServlet(new ServletHolder(new LTIConfigServlet()),"/config.xml");
    
    ResourceHandler staticHandler = new ResourceHandler();
    String staticDir = this.getClass().getClassLoader().getResource("static").toExternalForm();
    staticHandler.setResourceBase(staticDir);
    
    HandlerList handlers = new HandlerList();
    handlers.setHandlers(new Handler[] { staticHandler, dynamicHandler, new DefaultHandler() });
    webserver.setHandler(handlers);
    try {
      webserver.start();
    } catch (Exception e) {
      getLogger().severe("Failed to start server.");
    }
  }
 
   private void stopWebserver() {
     try {
       webserver.stop();
       for(int i = 0; i < 100; i++) {  /* Limit wait to 10 seconds */
         if(webserver.isStopping())
           Thread.sleep(100);
       }
       if(webserver.isStopping()) {
         getLogger().warning("Graceful shutdown timed out - continuing to terminate");
       }
     } catch (Exception e) {
       getLogger().severe("Failed to stop server.");
     }
     webserver = null;
   }
   
   public Logger getLogger() {
     return adapter != null ? adapter.getLogger() : logger;
   }
}
