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

public class ConsumerServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private static final ClassLoader classLoader = ConsumerServlet.class.getClassLoader();
  private static final InputStream stream = classLoader.getResourceAsStream("web/consumer.jmte");
  private static final String template = Util.streamToString(stream, "UTF-8");

  public ConsumerServlet(MinecraftLTI plugin) {}

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    LTIConsumer consumer = LTIConsumer.random();
    consumer.save();

    Map<String, Object> model = new HashMap<String, Object>();
    model.put("key", consumer.getKey().toString());
    model.put("secret", consumer.getSecret().toString());
    String document = MinecraftLTI.jmte.transform(template, model);
    response.getWriter().write(document);
  }
}
