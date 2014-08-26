package com.instructure.minecraftlti;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class TokenServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private final MinecraftLTI plugin;
  
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
    User user = plugin.getDatabase().find(User.class).where().eq("id", id).findUnique();
    Boolean isInstructor = user.getInstructor();
    Boolean isAssignment = user.getAssignmentId() != null;
    String address = plugin.adapter != null ? plugin.adapter.getServerAddress() : "";
    request.setAttribute("unregistered", user.getUuid() == null);
    request.setAttribute("studentAssignment", isAssignment && !isInstructor);
    request.setAttribute("teacherAssignment", isAssignment && isInstructor);
    request.setAttribute("token", user.getToken());
    request.setAttribute("address", address);
    request.getRequestDispatcher("/token.jsp").forward(request, response);
  }
}
