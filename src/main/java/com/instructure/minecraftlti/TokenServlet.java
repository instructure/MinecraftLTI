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
    model.put("token", user.getToken());
    model.put("address", address);
    String document = MinecraftLTI.jmte.transform(template, model);
    response.getWriter().write(document);
  }
}
