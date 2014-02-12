<%-- 
    Document   : users
    Created on : 05-Nov-2013, 14:58:19
    Author     : Timothy Anyona

Display user configuration page
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.users" var="pageTitle"/>

<spring:message code="datatables.text.showAllRows" var="dataTablesAllRowsText"/>
<spring:message code="users.message.userDeleted" var="userDeletedText"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>
<spring:message code="dialog.button.cancel" var="cancelText"/>
<spring:message code="dialog.button.delete" var="deleteText"/>
<spring:message code="dialog.message.deleteUser" var="deleteUserText"/>
<spring:message code="dialog.title.confirm" var="confirmText"/>
<spring:message code="users.activeStatus.active" var="activeText"/>
<spring:message code="users.activeStatus.disabled" var="disabledText"/>
<spring:message code="users.text.updated" var="updatedText"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-8 col-md-offset-2">

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/notify-combined-0.3.1.min.js"></script>
		<script type="text/javascript" charset="utf-8">
			$(document).ready(function() {
				$(function() {
					$('a[id="configure"]').parent().addClass('active');
					$('a[href*="users.do"]').parent().addClass('active');
				});
				var oTable = $('#users').dataTable({
					"sPaginationType": "bs_full",
//					"bPaginate": false,
//					"sScrollY": "365px",
					"aaSorting": [],
					"aLengthMenu": [[5, 10, 25, -1], [5, 10, 25, "${dataTablesAllRowsText}"]],
					"iDisplayLength": -1,
					"oLanguage": {
						"sUrl": "${pageContext.request.contextPath}/dataTables/dataTables_${pageContext.response.locale}.txt"
					},
					"fnInitComplete": function() {
						$('div.dataTables_filter input').focus();
					}
				});

				$('#users tbody').on('click', '.delete', function() {
					var row = $(this).closest("tr"); //jquery object
					var nRow = row[0]; //dom element/node
					var aPos = oTable.fnGetPosition(nRow);
					var username = escapeHtmlContent(row.data("username"));
					var userId = row.data("id");
					var msg;
					bootbox.confirm({
						message: "${deleteUserText}: " + username,
						title: "${confirmText}",
						buttons: {
							'cancel': {
								label: "${cancelText}"
							},
							'confirm': {
								label: "${deleteText}"
							}
						},
						callback: function(result) {
							if (result) {
								$.ajax({
									type: "POST",
									url: "${pageContext.request.contextPath}/app/deleteUser.do",
									data: {id: userId},
									success: function(response) {
										if (response.success) {
											msg = alertCloseButton + "${userDeletedText}";
											$("#ajaxResponse").addClass("alert alert-success alert-dismissable").html(msg);
											oTable.fnDeleteRow(aPos);
											$.notify("${userDeletedText}", "success");
										} else {
											msg = alertCloseButton + "<p>${errorOccurredText}</p><p>" + escapeHtmlContent(response.errorMessage) + "</p>";
											$("#ajaxResponse").addClass("alert alert-danger alert-dismissable").html(msg);
											$.notify("${errorOccurredText}", "error");
										}
									},
									error: function(xhr, status, error) {
										alert(xhr.responseText);
									}
								});
							}
						}
					});
				});

			});

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
				<p>${fn:escapeXml(error)}</p>
			</div>
		</c:if>

		<div id="ajaxResponse">
		</div>

		<div style="margin-bottom: 10px;">
			<a class="btn btn-default" href="${pageContext.request.contextPath}/app/addUser.do">
				<i class="fa fa-plus"></i>
				<spring:message code="page.action.add"/>
			</a>
		</div>

		<table id="users" class="table table-bordered table-striped table-condensed">
			<thead>
				<tr>
					<th><spring:message code="page.text.id"/></th>
					<th><spring:message code="users.text.username"/></th>
					<th><spring:message code="users.text.fullName"/></th>
					<th><spring:message code="users.text.active"/></th>
					<th><spring:message code="page.text.action"/></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="user" items="${users}">
					<tr data-username="${encode:forHtmlAttribute(user.username)}"
						data-id="${user.userId}">
						<td>${user.userId}</td>
						<td>${encode:forHtmlContent(user.username)} &nbsp;
							<t:displayNewLabel creationDate="${user.creationDate}"
											   updateDate="${user.updateDate}"
											   updatedText="${updatedText}"/>
						</td>
						<td>${user.fullName}</td>
						<td><t:displayActiveStatus active="${user.active}"
											   activeText="${activeText}"
											   disabledText="${disabledText}"/>
						</td>
						<td>
							<div class="btn-group">
								<a class="btn btn-default" href="${pageContext.request.contextPath}/app/editUser.do?id=${user.userId}">
									<i class="fa fa-pencil-square-o"></i>
									<spring:message code="page.action.edit"/>
								</a>
								<button type="button" class="btn btn-default delete">
									<i class="fa fa-trash-o"></i>
									<spring:message code="page.action.delete"/>
								</button>
							</div>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</jsp:body>
</t:mainPageWithPanel>

