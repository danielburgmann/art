<%-- 
    Document   : drilldowns
    Created on : 12-Apr-2014, 16:29:55
    Author     : Timothy Anyona

Display report drilldowns
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.drilldowns" var="pageTitle"/>

<spring:message code="dataTables.text.showAllRows" var="showAllRowsText"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>
<spring:message code="dialog.button.cancel" var="cancelText"/>
<spring:message code="dialog.button.ok" var="okText"/>
<spring:message code="dialog.message.deleteRecord" var="deleteRecordText"/>
<spring:message code="page.message.recordDeleted" var="recordDeletedText"/>
<spring:message code="page.message.recordMoved" var="recordMovedText"/>
<spring:message code="page.help.dragToReorder" var="dragToReorderText"/>
<spring:message code="page.message.recordsDeleted" var="recordsDeletedText"/>
<spring:message code="dialog.message.selectRecords" var="selectRecordsText"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-8 col-md-offset-2">

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/notify-combined-0.3.1.min.js"></script>

		<script type="text/javascript">
			//enable use of bootstrap tooltips. both jquery ui and bootstrap define the tooltip function
			$.fn.bsTooltip = $.fn.tooltip.noConflict();
		</script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-ui-1.11.4-all-smoothness/jquery-ui.min.js"></script>

		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.dataTables.rowReordering-1.2.1.js"></script>

		<script type="text/javascript">
			$(document).ready(function () {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="reportsConfig"]').parent().addClass('active');

				//{container: 'body'} needed if tooltips shown on input-group element or button
				$("[data-toggle='tooltip']").bsTooltip({container: 'body'});

				var tbl = $('#drilldowns');

				var oTable = tbl.dataTable({
					columnDefs: [
						{orderable: true, targets: 1},
						{orderable: false, targets: '_all'},
						{
							orderable: false,
							className: 'select-checkbox',
							targets: 0
						}
					],
					order: [[1, 'asc']],
					dom: 'lBfrtip',
					buttons: [
						'selectAll',
						'selectNone'
					],
					select: {
						style: 'multi',
						selector: 'td:first-child'
					},
					orderClasses: false,
					pagingType: "full_numbers",
					lengthMenu: [[5, 10, 25, -1], [5, 10, 25, "${showAllRowsText}"]],
					pageLength: 10,
					language: {
						url: "${pageContext.request.contextPath}/js/dataTables/i18n/dataTables_${pageContext.response.locale}.json"
					},
					initComplete: datatablesInitComplete
				});

				tbl.find('tbody').on('click', '.deleteRecord', function () {
					var row = $(this).closest("tr"); //jquery object
					var recordName = escapeHtmlContent(row.data("name"));
					var recordId = row.data("id");
					bootbox.confirm({
						message: "${deleteRecordText}: <b>" + recordName + "</b>",
						buttons: {
							cancel: {
								label: "${cancelText}"
							},
							confirm: {
								label: "${okText}"
							}
						},
						callback: function (result) {
							if (result) {
								//user confirmed delete. make delete request
								$.ajax({
									type: "POST",
									dataType: "json",
									url: "${pageContext.request.contextPath}/deleteDrilldown",
									data: {id: recordId},
									success: function (response) {
										if (response.success) {
											notifyActionSuccess("${cacheClearedText}", recordName);
										} else {
											notifyActionError("${errorOccurredText}", escapeHtmlContent(response.errorMessage));
										}
									},
									error: ajaxErrorHandler
								});
							} //end if result
						} //end callback
					}); //end bootbox confirm
				});

				//enable changing of drilldown position using drag and drop
				oTable.rowReordering({
					iIndexColumn: 1,
					sURL: "moveDrilldown",
					sRequestType: "POST",
					fnSuccess: function (response) {
						if (response.success) {
							notifyActionSuccess("${recordMovedText}", escapeHtmlContent(response.data));
						} else {
							notifyActionError("${errorOccurredText}", escapeHtmlContent(response.errorMessage));
						}
					},
					fnAlert: function (message) {
						bootbox.alert(message);
					}
				});

				var table = oTable.api();

				$('#deleteRecords').click(function () {
					var selectedRows = table.rows({selected: true});
					var data = selectedRows.data();
					if (data.length > 0) {
						var ids = $.map(data, function (item) {
							return item[2];
						});
						bootbox.confirm({
							message: "${deleteRecordText}: <b>" + ids + "</b>",
							buttons: {
								cancel: {
									label: "${cancelText}"
								},
								confirm: {
									label: "${okText}"
								}
							},
							callback: function (result) {
								if (result) {
									//user confirmed delete. make delete request
									$.ajax({
										type: "POST",
										dataType: "json",
										url: "${pageContext.request.contextPath}/deleteDrilldowns",
										data: {ids: ids},
										success: function (response) {
											if (response.success) {
												selectedRows.remove().draw(false);
												notifyActionSuccess("${recordsDeletedText}", ids);
											} else {
												notifyActionError("${errorOccurredText}", escapeHtmlContent(response.errorMessage));
											}
										},
										error: ajaxErrorHandler
									});
								} //end if result
							} //end callback
						}); //end bootbox confirm
					} else {
						bootbox.alert("${selectRecordsText}");
					}
				});

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

		<div class="text-center">
			<b><spring:message code="drilldowns.text.parentReport"/>:</b> ${parentReportName}
		</div>
		<div style="margin-bottom: 10px;">
			<a class="btn btn-default" href="${pageContext.request.contextPath}/addDrilldown?parent=${parentReportId}">
				<i class="fa fa-plus"></i>
				<spring:message code="page.action.add"/>
			</a>
			<button type="button" id="deleteRecords" class="btn btn-default">
				<i class="fa fa-trash-o"></i>
				<spring:message code="page.action.delete"/>
			</button>
		</div>

		<table id="drilldowns" class="table table-bordered table-striped table-condensed">
			<thead>
				<tr>
					<th class="noFilter"></th>
					<th><spring:message code="page.text.position"/></th>
					<th><spring:message code="page.text.id"/></th>
					<th><spring:message code="drilldowns.text.drilldownReport"/></th>
					<th class="noFilter"><spring:message code="page.text.action"/></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="drilldown" items="${drilldowns}">
					<tr data-id="${drilldown.drilldownId}" 
						data-name="${encode:forHtmlAttribute(drilldown.drilldownReport.getLocalizedName(pageContext.response.locale))}"
						id="${drilldown.drilldownId}">

						<td></td>
						<td>${drilldown.position}</td>
						<td>${drilldown.drilldownId}</td>
						<td data-toggle="tooltip" title="${dragToReorderText}">
							${encode:forHtmlContent(drilldown.drilldownReport.getLocalizedName(pageContext.response.locale))}
						</td>
						<td>
							<div class="btn-group">
								<a class="btn btn-default" 
								   href="${pageContext.request.contextPath}/editDrilldown?id=${drilldown.drilldownId}">
									<i class="fa fa-pencil-square-o"></i>
									<spring:message code="page.action.edit"/>
								</a>
								<button type="button" class="btn btn-default deleteRecord">
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
