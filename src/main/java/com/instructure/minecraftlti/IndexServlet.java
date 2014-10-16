package com.instructure.minecraftlti;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.floreysoft.jmte.util.Util;

public class IndexServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private final MinecraftLTI plugin;
  private static final ClassLoader classLoader = TokenServlet.class.getClassLoader();
  private static final InputStream stream = classLoader.getResourceAsStream("web/index.jmte");
  private static final String template = Util.streamToString(stream, "UTF-8");
  
  public IndexServlet(MinecraftLTI plugin) {
    this.plugin = plugin;
  }
  
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String address = plugin.getServerAddress();
    
    Map<String, Object> model = new HashMap<String, Object>();
    model.put("address", address);
    String document = MinecraftLTI.jmte.transform(template, model);
    response.getWriter().write(document);
  }
}
