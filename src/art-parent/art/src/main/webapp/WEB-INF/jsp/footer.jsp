<%-- 
    Document   : footer
    Created on : 15-Sep-2013, 09:15:02
    Author     : Timothy Anyona

Footer that appears on all pages
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div id="footer">
	<div class="container"> 
		<span class="navbar-text text-muted">
			<a href="http://art.sourceforge.net">ART</a>
			&nbsp; A Reporting Tool 
			&nbsp; version ${artVersion}
		</span>
		<c:if test="${not empty administratorEmail}">
			<span class="navbar-text pull-right">
				<a href="mailto:${administratorEmail}?subject=ART">
					<spring:message code="footer.link.contactSupport"/>
				</a>
			</span>
		</c:if>
	</div>
</div>
