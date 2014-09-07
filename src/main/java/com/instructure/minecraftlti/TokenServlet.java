package com.instructure.minecraftlti;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringEscapeUtils;

import com.floreysoft.jmte.Engine;
import com.floreysoft.jmte.util.Util;

public class TokenServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private final MinecraftLTI plugin;
  private static final ClassLoader classLoader = TokenServlet.class.getClassLoader();
  private static final InputStream stream = classLoader.getResourceAsStream("web/token.jmte");
  private static final String template = Util.streamToString(stream, "UTF-8");
  
  public TokenServlet(MinecraftLTI plugin) {
    this.plugin = plugin;
  }
  
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    HttpSession session = request.getSession();
    Object id = session.getAttribute("id");
    if (id == null) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    User user = User.byId((int)id);
    Boolean isInstructor = user.getInstructor();
    Boolean isAssignment = user.getAssignmentId() != null;
    String address = plugin.adapter != null ? plugin.adapter.getServerAddress() : "";
    
    Map<String, Object> model = new HashMap<String, Object>();
    model.put("unregistered", user.getUuid() == null);
    model.put("studentAssignment", isAssignment && !isInstructor);
    model.put("teacherAssignment", isAssignment && isInstructor);
    model.put("token", StringEscapeUtils.escapeXml(user.getToken()));
    model.put("address", StringEscapeUtils.escapeXml(address));
    Engine engine = Engine.createDefaultEngine();
    String document = engine.transform(template, model);
    response.getWriter().write(document);
  }
}
