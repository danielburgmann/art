<%-- 
    Document   : archives
    Created on : 27-Mar-2016, 03:37:17
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<spring:message code="page.title.archives" var="pageTitle"/>

<spring:message code="dataTables.text.showAllRows" var="showAllRowsText" javaScriptEscape="true"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-10 col-md-offset-1"
					 hasTable="true">

	<jsp:attribute name="javascript">
		<script type="text/javascript">
			$(document).ready(function () {
				$('a[href*="archives"]').parent().addClass('active');

				var tbl = $("#archives");

				var pageLength = 10; //pass undefined to use the default
				var showAllRowsText = "${showAllRowsText}";
				var contextPath = "${pageContext.request.contextPath}";
				var localeCode = "${pageContext.response.locale}";
				var addColumnFilters = undefined; //pass undefined to use the default
				var columnDefs = undefined; //pass undefined to use the default

				//initialize datatable
				initBasicTable(tbl, pageLength, showAllRowsText, contextPath,
						localeCode, addColumnFilters, columnDefs);

			}); //end document ready
		</script>

	</jsp:attribute>

	<jsp:body>
		<c:if test="${not empty message}">
			<div class="alert alert-success alert-dismissable">
				<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
				<spring:message code="${message}"/>
			</div>
		</c:if>
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

		<table id="archives" class="table table-bordered table-striped table-condensed">
			<thead>
				<tr>
					<th class="jobIdCol"><spring:message code="jobs.text.jobId"/></th>
					<th class="jobNameCol"><spring:message code="jobs.text.jobName"/></th>
					<th><spring:message code="logs.text.date"/></th>
					<th><spring:message code="jobs.text.result"/></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="archive" items="${archives}">
					<tr data-id="${archive.archiveId}" 
						data-name="${encode:forHtmlAttribute(archive.archiveId)}">

						<td>${archive.job.jobId}</td>
						<td>${encode:forHtmlContent(archive.job.name)}</td>
						<td data-sort="${archive.endDate.time}">
							<fmt:formatDate value="${archive.endDate}" pattern="${dateDisplayPattern}"/>
						</td>
						<td>
							<c:if test="${not empty archive.fileName}">
								<a type="application/octet-stream" 
								   href="${pageContext.request.contextPath}/export/jobs/${archive.fileName}">
									${archive.fileName}
								</a>
								<br>
							</c:if>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</jsp:body>
</t:mainPageWithPanel>


