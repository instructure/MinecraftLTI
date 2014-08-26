package com.instructure.minecraftlti;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.PersistenceException;

import org.apache.jasper.servlet.JspServlet;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.SimpleInstanceManager;
import org.eclipse.jetty.annotations.ServletContainerInitializersStarter;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.plus.annotation.ContainerInitializer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.session.HashSessionIdManager;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.SQLitePlatform;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.ddl.DdlGenerator;
import com.avaje.ebeaninternal.server.lib.sql.TransactionIsolation;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MinecraftLTI {
	private Server webserver = null;
	public MinecraftLTIAdapter adapter = null;
  private EbeanServer ebean = null;
  private final ClassLoader classLoader = this.getClass().getClassLoader();
  
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
    saveDefaultConfig();
    setupDatabase();
    startWebserver();
  }
 
  public void close() {
    stopWebserver();
  }
  
  private File getConfigFile() {
    Path configPath = adapter.getStorageDirectory().resolve("config.json");
    File configFile = new File(configPath.toString());
    return configFile;
  }
  
  private void saveDefaultConfig() {
    File configFile = getConfigFile();
    try {
      if (!configFile.createNewFile()) {return;}
      FileWriter writer = new FileWriter(configFile);
      MinecraftLTIConfig config = MinecraftLTIConfig.createDefault();
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      writer.write(gson.toJson(config));
      writer.close();
    } catch (IOException e) {
      getLogger().warning("Failed to create config.");
    }
  }
  
  private MinecraftLTIConfig getConfig() {
    File configFile = getConfigFile();
    try {
      Gson gson = new Gson();
      String contents = Files.toString(configFile, Charsets.UTF_8);
      MinecraftLTIConfig config = gson.fromJson(contents, MinecraftLTIConfig.class);
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
    ServerConfig db = new ServerConfig();
    db.setDefaultServer(false);
    db.setRegister(false);
    db.setClasses(getDatabaseClasses());
    db.setName("MinecraftLTI");
    db.setDatabasePlatform(new SQLitePlatform());
    db.getDatabasePlatform().getDbDdlSyntax().setIdentity("");
    
    DataSourceConfig ds = new DataSourceConfig();
    Path dbPath = adapter.getStorageDirectory().resolve("database.db");
    ds.setDriver("org.sqlite.JDBC");
    ds.setUrl(String.format("jdbc:sqlite:%s", dbPath.toString()));
    ds.setUsername("username");
    ds.setPassword("password");
    ds.setIsolationLevel(TransactionIsolation.getLevel("SERIALIZABLE"));
    db.setDataSourceConfig(ds);
    

    ClassLoader previous = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(classLoader);
    ebean = EbeanServerFactory.create(db);
    Thread.currentThread().setContextClassLoader(previous);
    
    try {
      ebean.find(LTIConsumer.class).findRowCount();
    } catch (PersistenceException ex) {
      logger.info("Installing database for MinecraftLTI due to first time usage");
      DdlGenerator gen = ((SpiEbeanServer)ebean).getDdlGenerator();
      gen.runScript(false, gen.generateCreateDdl());
    }
  }
  
  public EbeanServer getDatabase() {
    return ebean;
  }
  
  private void startWebserver() {
    MinecraftLTIConfig config = getConfig();
    int port = config.port;

    webserver = new Server(port);
    webserver.setSessionIdManager(new HashSessionIdManager());
    
    WebAppContext dynamicHandler = new WebAppContext();
    String webDir = this.getClass().getClassLoader().getResource("web").toExternalForm();
    dynamicHandler.setResourceBase(webDir);
    dynamicHandler.setAttribute(InstanceManager.class.getName(), new SimpleInstanceManager());
    ClassLoader jspClassLoader = new URLClassLoader(new URL[0], this.getClass().getClassLoader());
    dynamicHandler.setClassLoader(jspClassLoader);
    
    dynamicHandler.addServlet(new ServletHolder(new LTIServlet(this)),"/lti");
    dynamicHandler.addServlet(new ServletHolder(new TokenServlet(this)),"/token");
    dynamicHandler.addServlet(new ServletHolder(new AssignmentServlet(this)),"/assignment");
    dynamicHandler.addServlet(new ServletHolder(new ConsumerServlet(this)),"/consumer");
    dynamicHandler.addServlet(new ServletHolder(new LTIConfigServlet()),"/config.xml");
    
    //Ensure the jsp engine is initialized correctly
    JettyJasperInitializer sci = new JettyJasperInitializer();
    ServletContainerInitializersStarter sciStarter = new ServletContainerInitializersStarter(dynamicHandler);
    List<ContainerInitializer> initializers = new ArrayList<ContainerInitializer>();
    initializers.add(new ContainerInitializer(sci, null));
    dynamicHandler.setAttribute("org.eclipse.jetty.containerInitializers", initializers);
    dynamicHandler.addBean(sciStarter, true);

    ServletHolder holderJsp = new ServletHolder("jsp",JspServlet.class);
    holderJsp.setInitOrder(0);
    holderJsp.setInitParameter("fork","false");
    holderJsp.setInitParameter("keepgenerated","true");
    dynamicHandler.addServlet(holderJsp,"*.jsp");
    
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
   
   public static EbeanServer getDb() {
     return MinecraftLTI.instance.getDatabase();
   }
   
   public Logger getLogger() {
     return adapter != null ? adapter.getLogger() : logger;
   }
}
