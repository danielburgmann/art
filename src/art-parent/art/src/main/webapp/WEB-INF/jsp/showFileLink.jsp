<%-- 
    Document   : showTemplateReportResult
    Created on : 30-Oct-2014, 15:49:41
    Author     : Timothy Anyona

Display result (link to file) e.g. with jasper report, jxls report, chart pdf or png report
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<div style="text-align: center">
	<a type="application/octet-stream" href="${pageContext.request.contextPath}/export/reports/${encode:forUriComponent(fileName)}">
		<spring:message code="page.link.download"/>
	</a>
</div>

<c:set var="reportFileName" value="${fileName}" scope="session"/>

<script type="text/javascript">
	var url = "${pageContext.request.contextPath}/export/reports/${encode:forJavaScript(encode:forUriComponent(fileName))}";
		window.open(url);
</script>
