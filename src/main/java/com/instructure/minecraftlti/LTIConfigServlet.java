package com.instructure.minecraftlti;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;

import com.floreysoft.jmte.Engine;
import com.floreysoft.jmte.util.Util;

public class LTIConfigServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private static final ClassLoader classLoader = LTIConfigServlet.class.getClassLoader();
  private static final InputStream stream = classLoader.getResourceAsStream("web/config.xml.jmte");
  private static final String template = Util.streamToString(stream, "UTF-8");

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    Map<String, Object> model = new HashMap<String, Object>();
    String address = request.getServerName()+":"+request.getServerPort();
    model.put("address", StringEscapeUtils.escapeXml(address));
    Engine engine = Engine.createDefaultEngine();
    String document = engine.transform(template, model);
    response.setContentType("application/xml");
    response.getWriter().write(document);
  }
}
