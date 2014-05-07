<%-- 
    Document   : userGroups
    Created on : 12-Feb-2014, 09:24:43
    Author     : Timothy Anyona

Display user groups
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.userGroups" var="pageTitle"/>

<spring:message code="datatables.text.showAllRows" var="dataTablesAllRowsText"/>
<spring:message code="datatables.text.showHideColumns" var="showHideColumnsText"/>
<spring:message code="datatables.text.restoreOriginal" var="restoreOriginalText"/>
<spring:message code="datatables.text.selectAll" var="selectAllText"/>
<spring:message code="datatables.text.deselectAll" var="deselectAllText"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>
<spring:message code="dialog.button.cancel" var="cancelText"/>
<spring:message code="dialog.button.ok" var="okText"/>
<spring:message code="dialog.message.deleteRecord" var="deleteRecordText"/>
<spring:message code="page.message.recordDeleted" var="recordDeletedText"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-8 col-md-offset-2">

	<jsp:attribute name="css">
		<style type="text/css">
			/* ensure tabletools div is right aligned */
			/* see the css section in https://datatables.net/release-datatables/extensions/TableTools/examples/bootstrap.html */
			body { font-size: 140%; }
			div.DTTT { margin-bottom: 0.5em; float: right; }
			div.dataTables_wrapper { clear: both; }
		</style>
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/notify-combined-0.3.1.min.js"></script>
		<script type="text/javascript">
			$(document).ready(function() {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="userGroups.do"]').parent().addClass('active');

				var tbl = $('#userGroups');

				//add row to thead to enable column filtering
				//use clone so that plugins work properly? e.g. colvis
				var headingRow = tbl.find('thead tr:first');
				var columnFilterRow = headingRow.clone();
				//insert cloned row as first row because datatables will put heading styling on the last thead row
				columnFilterRow.insertBefore(headingRow);
				//put search fields into cloned row
				columnFilterRow.find('th').each(function() {
					var title = $(this).text();
					$(this).html('<input type="text" class="form-control input-sm" placeholder="' + title + '">');
				});

				//use initialization that returns a jquery object. to be able to use plugins
				var oTable = tbl.dataTable({
//					"scrollX": true, 
					"orderClasses": false,
					"pagingType": "full_numbers",
					"lengthMenu": [[5, 10, 25, -1], [5, 10, 25, "${dataTablesAllRowsText}"]],
					"pageLength": 10,
					"language": {
						"url": "${pageContext.request.contextPath}/js/dataTables-1.10.0/i18n/dataTables_${pageContext.response.locale}.txt"
					},
					"initComplete": function() {
						$('div.dataTables_filter input').focus();
					}
				});

				//move column filter row after heading row
				columnFilterRow.insertAfter(columnFilterRow.next());

				//get datatables api object
				var table = oTable.api();

				// Apply the column filter
				//https://datatables.net/examples/api/multi_filter.html
				tbl.find('thead input').on('keyup change', function() {
					table
							.column($(this).parent().index() + ':visible')
							.search(this.value)
							.draw();
				});

				//insert tabletools extension to enable multi select
				var tableTools = new $.fn.dataTable.TableTools(table, {
					"sRowSelect": "multi",
					"aButtons": [
						{
							"sExtends": "select_all",
							"sButtonText": "${selectAllText}"
						},
						{
							"sExtends": "select_none",
							"sButtonText": "${deselectAllText}"
						}
					],
					"sSwfPath": "${pageContext.request.contextPath}/js/dataTables-1.10.0/extensions/TableTools/swf/copy_csv_xls_pdf.swf"
				});
				$(tableTools.fnContainer()).insertAfter('#headerActions');

				//insert colvis extension
				var colvis = new $.fn.dataTable.ColVis(table, {
//					"activate": "mouseover",
					"buttonText": "${showHideColumnsText}",
					"restore": "${restoreOriginalText}"
				});
				$(colvis.button()).insertAfter('#headerActions');


				//delete record
				tbl.find('tbody').on('click', '.delete', function() {
					var row = $(this).closest("tr"); //jquery object
					var name = escapeHtmlContent(row.data("name"));
					var id = row.data("id");
					bootbox.confirm({
						message: "${deleteRecordText}: <b>" + name + "</b>",
						buttons: {
							'cancel': {
								label: "${cancelText}"
							},
							'confirm': {
								label: "${okText}"
							}
						},
						callback: function(result) {
							if (result) {
								$.ajax({
									type: "POST",
									dataType: "json",
									url: "${pageContext.request.contextPath}/app/deleteUserGroup.do",
									data: {id: id},
									success: function(response) {
										var msg;
										if (response.success) {
											table.row(row).remove().draw(false);
											msg = alertCloseButton + "${recordDeletedText}: " + name;
											$("#ajaxResponse").attr("class", "alert alert-success alert-dismissable").html(msg);
											$.notify("${recordDeletedText}", "success");
										} else {
											msg = alertCloseButton + "<p>${errorOccurredText}</p><p>" + escapeHtmlContent(response.errorMessage) + "</p>";
											$("#ajaxResponse").attr("class", "alert alert-danger alert-dismissable").html(msg);
											$.notify("${errorOccurredText}", "error");
										}
									},
									error: function(xhr) {
										bootbox.alert(xhr.responseText);
									}
								}); //end ajax
							} //end if result
						} //end bootbox callback
					}); //end bootbox confirm
				}); //end on click

			}); //end document ready
		</script>
	</jsp:attribute>

	<jsp:body>
		<c:if test="${error != null}">
			<div class="alert alert-danger alert-dismissable">
				<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
				<p><spring:message code="page.message.errorOccurred"/></p>
				<c:if test="${showErrors}">
					<p><encode:forHtmlContent value="${error}"/></p>
				</c:if>
			</div>
		</c:if>
		<c:if test="${not empty recordSavedMessage}">
			<div class="alert alert-success alert-dismissable">
				<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
				<spring:message code="${recordSavedMessage}"/>: <encode:forHtmlContent value="${recordName}"/>
			</div>
		</c:if>

		<div id="ajaxResponse">
		</div>

		<div id="headerActions" class="dtHeader">
			<a class="btn btn-default" href="${pageContext.request.contextPath}/app/addUserGroup.do">
				<i class="fa fa-plus"></i>
				<spring:message code="page.action.add"/>
			</a>
		</div>

		<table id="userGroups" class="table table-bordered table-striped table-condensed">
			<thead>
				<tr>
					<th><spring:message code="page.text.id"/></th>
					<th><spring:message code="page.text.name"/></th>
					<th><spring:message code="page.text.description"/></th>
					<th><spring:message code="page.text.action"/></th>
				</tr>
			</thead>

			<tbody>
				<c:forEach var="group" items="${groups}">
					<tr data-id="${group.userGroupId}" 
						data-name="${encode:forHtmlAttribute(group.name)}">

						<td>${group.userGroupId}</td>
						<td>${encode:forHtmlContent(group.name)} &nbsp;
							<t:displayNewLabel creationDate="${group.creationDate}"
											   updateDate="${group.updateDate}"/>
						</td>
						<td>${encode:forHtmlContent(group.description)}</td>
						<td>
							<div class="btn-group">
								<a class="btn btn-default" 
								   href="${pageContext.request.contextPath}/app/editUserGroup.do?id=${group.userGroupId}">
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
