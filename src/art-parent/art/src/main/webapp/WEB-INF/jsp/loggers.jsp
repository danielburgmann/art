<%-- 
    Document   : loggers
    Created on : 02-Mar-2014, 12:31:10
    Author     : Timothy Anyona

Display current loggers
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.loggers" var="pageTitle"/>

<spring:message code="dataTables.text.showAllRows" var="showAllRowsText" javaScriptEscape="true"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-6 col-md-offset-3"
					 hasTable="true">

	<jsp:attribute name="javascript">
		<script type="text/javascript">
			$(document).ready(function() {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="loggers"]').parent().addClass('active');

				var tbl = $("#loggers");
				
				var pageLength = 10; //pass undefined to use the default
				var showAllRowsText = "${showAllRowsText}";
				var contextPath = "${pageContext.request.contextPath}";
				var localeCode = "${pageContext.response.locale}";
				var addColumnFilters = false; //pass undefined to use the default
				var columnDefs = undefined; //pass undefined to use the default

				//initialize datatable
				initBasicTable(tbl, pageLength, showAllRowsText, contextPath,
						localeCode, addColumnFilters, columnDefs);

			}); //end document ready
		</script>
	</jsp:attribute>

	<jsp:body>
		<c:if test="${error != null}">
			<div class="alert alert-danger alert-dismissable">
				<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
				<p><spring:message code="page.message.errorOccurred"/></p>
				<c:if test="${showErrors}">
					<p>${encode:forHtmlContent(error)}</p>
				</c:if>
			</div>
		</c:if>
		<c:if test="${not empty recordSavedMessage}">
			<div class="alert alert-success alert-dismissable">
				<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
				<spring:message code="${recordSavedMessage}"/>: ${encode:forHtmlContent(recordName)}
			</div>
		</c:if>

		<div id="ajaxResponse">
		</div>

		<div style="margin-bottom: 10px;">
			<a class="btn btn-default" href="${pageContext.request.contextPath}/addLogger">
				<i class="fa fa-plus"></i>
				<spring:message code="page.action.add"/>
			</a>
		</div>

		<table id="loggers" class="table table-bordered table-striped table-condensed">
			<thead>
				<tr>
					<th><spring:message code="page.text.name"/></th>
					<th><spring:message code="page.text.level"/></th>
					<th class="noFilter actionCol"><spring:message code="page.text.action"/></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="logger" items="${loggers}">
					<tr data-name="${encode:forHtmlAttribute(logger.name)}">
						
						<td>${encode:forHtmlContent(logger.name)}</td>
						<td>${encode:forHtmlContent(logger.level)}</td>
						<td>
							<div class="btn-group">
								<spring:url var="editUrl" value="/editLogger">
									<spring:param name="name" value="${logger.name}"/>
								</spring:url>
								<a class="btn btn-default" href="${editUrl}">
									<i class="fa fa-pencil-square-o"></i>
									<spring:message code="page.action.edit"/>
								</a>
							</div>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</jsp:body>
</t:mainPageWithPanel>
