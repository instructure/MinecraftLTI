package com.instructure.minecraftlti;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LTIConfigServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setAttribute("address", request.getServerName()+":"+request.getServerPort());
    request.getRequestDispatcher("/config.xml.jsp").forward(request, response);
  }
}
