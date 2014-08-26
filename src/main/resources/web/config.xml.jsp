<%@page contentType="application/xml" %><?xml version="1.0" encoding="UTF-8"?>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<cartridge_basiclti_link xmlns="http://www.imsglobal.org/xsd/imslticc_v1p0"
    xmlns:blti = "http://www.imsglobal.org/xsd/imsbasiclti_v1p0"
    xmlns:lticm ="http://www.imsglobal.org/xsd/imslticm_v1p0"
    xmlns:lticp ="http://www.imsglobal.org/xsd/imslticp_v1p0"
    xmlns:xsi = "http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation = "http://www.imsglobal.org/xsd/imslticc_v1p0 http://www.imsglobal.org/xsd/lti/ltiv1p0/imslticc_v1p0.xsd
    http://www.imsglobal.org/xsd/imsbasiclti_v1p0 http://www.imsglobal.org/xsd/lti/ltiv1p0/imsbasiclti_v1p0.xsd
    http://www.imsglobal.org/xsd/imslticm_v1p0 http://www.imsglobal.org/xsd/lti/ltiv1p0/imslticm_v1p0.xsd
    http://www.imsglobal.org/xsd/imslticp_v1p0 http://www.imsglobal.org/xsd/lti/ltiv1p0/imslticp_v1p0.xsd">
    <blti:title>Minecraft</blti:title>
    <blti:description>Minecraft LMS integration</blti:description>
    <blti:icon>http://<c:out value="${address}" />/minecraft-16x16.png</blti:icon>
    <blti:launch_url>http://<c:out value="${address}" />/lti</blti:launch_url>
    <blti:custom>
      <lticm:property name="custom_canvas_xapi_url">$Canvas.xapi.url</lticm:property>
    </blti:custom>
    <blti:extensions platform="canvas.instructure.com">
      <lticm:property name="tool_id">minecraftlti</lticm:property>
      <lticm:property name="privacy_level">anonymous</lticm:property>
      <lticm:options name="homework_submission">
        <lticm:property name="url">http://<c:out value="${address}" />/lti</lticm:property>
        <lticm:property name="icon_url">http://<c:out value="${address}" />/minecraft-16x16.png</lticm:property>
        <lticm:property name="text">Minecraft</lticm:property>
        <lticm:property name="selection_width">400</lticm:property>
        <lticm:property name="selection_height">300</lticm:property>
        <lticm:property name="enabled">true</lticm:property>
      </lticm:options>
      <lticm:options name="course_navigation">
        <lticm:property name="url">http://<c:out value="${address}" />/lti</lticm:property>
        <lticm:property name="text">Minecraft</lticm:property>
        <lticm:property name="visibility">public</lticm:property>
        <lticm:property name="default">enabled</lticm:property>
        <lticm:property name="enabled">true</lticm:property>
      </lticm:options>
    </blti:extensions>
    <cartridge_bundle identifierref="BLTI001_Bundle"/>
    <cartridge_icon identifierref="BLTI001_Icon"/>
</cartridge_basiclti_link>
