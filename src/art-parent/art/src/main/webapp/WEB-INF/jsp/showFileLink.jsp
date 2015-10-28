<%-- 
    Document   : showTemplateReportResult
    Created on : 30-Oct-2014, 15:49:41
    Author     : Timothy Anyona

Display result (link to file) e.g. with jasper report, jxls report, chart pdf or png report
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<a type="application/octet-stream" href="${pageContext.request.contextPath}/export/reports/${fileName}">
	<spring:message code="page.link.download"/>
</a>
