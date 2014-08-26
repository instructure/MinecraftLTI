package com.instructure.minecraftlti;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AssignmentServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private final MinecraftLTI plugin;
  
  public AssignmentServlet(MinecraftLTI plugin) {
    this.plugin = plugin;
  }
  
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    HttpSession session = request.getSession();
    Object id = session.getAttribute("id");
    if (id == null) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    User user = plugin.getDatabase().find(User.class).where().eq("id", id).findUnique();
    user.assignmentAction(request.getParameter("effect"));
    response.sendRedirect("/token");
  }
}
