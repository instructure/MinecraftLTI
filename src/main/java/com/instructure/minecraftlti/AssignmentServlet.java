package com.instructure.minecraftlti;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AssignmentServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  
  public AssignmentServlet(MinecraftLTI plugin) {}
  
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    HttpSession session = request.getSession();
    Object id = session.getAttribute("id");
    if (id == null) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    User user = User.byId((int)id);
    user.assignmentAction(request.getParameter("effect"));
    response.sendRedirect("/token");
  }
}
