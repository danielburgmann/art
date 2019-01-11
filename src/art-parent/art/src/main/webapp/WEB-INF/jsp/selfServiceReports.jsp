<%-- 
    Document   : selfServiceReports
    Created on : 24-Dec-2018, 18:13:00
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.selfServiceReports" var="pageTitle"/>

<spring:message code="page.text.search" var="searchText"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>
<spring:message code="select.text.nothingSelected" var="nothingSelectedText"/>
<spring:message code="select.text.noResultsMatch" var="noResultsMatchText"/>
<spring:message code="select.text.selectedCount" var="selectedCountText"/>
<spring:message code="multiselect.button.undo" var="undoText"/>
<spring:message code="multiselect.button.redo" var="redoText"/>

<t:mainPage title="${pageTitle}">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/jQuery-QueryBuilder-2.5.2/css/query-builder.default.min.css" /> 
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jQuery-QueryBuilder-2.5.2/js/query-builder.standalone.min.js"></script>
		<c:if test="${not empty languageFileName}">
			<script type="text/javascript" src="${pageContext.request.contextPath}/js/jQuery-QueryBuilder-2.5.2/i18n/${encode:forHtmlAttribute(languageFileName)}"></script>
		</c:if>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/multiselect-2.5.5/js/multiselect.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootbox-4.4.0.min.js"></script>

		<script>
			$(function () {
				$('a[id="selfService"]').parent().addClass('active');
				$('a[href*="selfServiceReports"]').parent().addClass('active');

				$('.selectpicker').selectpicker({
					liveSearch: true,
					noneSelectedText: '${nothingSelectedText}',
					noneResultsText: '${noResultsMatchText}',
					countSelectedText: '${selectedCountText}'
				});

				loadViews();

				function loadViews() {
					$.ajax({
						type: 'GET',
						dataType: "json",
						url: '${pageContext.request.contextPath}/getViews',
						success: function (response) {
							if (response.success) {
								//https://github.com/silviomoreto/bootstrap-select/issues/1151
								var reports = response.data;
								var options = "<option value='0'>--</option>";
								$.each(reports, function (index, report) {
									options += "<option value=" + report.reportId + ">" + report.name2 + "</option>";
								});
								var select = $("#views");
								select.empty();
								select.append(options);
								select.selectpicker('refresh');
							} else {
								notifyActionErrorReusable("${errorOccurredText}", response.errorMessage, ${showErrors});
							}
						},
						error: function (xhr) {
							showUserAjaxError(xhr, '${errorOccurredText}');
						}
					});
				}

				$("#views").on('changed.bs.select', function (event, clickedIndex, newValue, oldValue) {
					//https://stackoverflow.com/questions/36944647/bootstrap-select-on-click-get-clicked-value
					var reportId = $(this).find('option').eq(clickedIndex).val();

					$("#reportOutput").empty();

					//https://stackoverflow.com/questions/27347004/jquery-val-integer-datatype-comparison
					if (reportId === '0') {
						$('#multiselect').empty();
						$('#multiselect_to').empty();
						$("#whereDiv").hide();
					} else {
						$.ajax({
							type: 'GET',
							url: '${pageContext.request.contextPath}/getViewDetails',
							data: {reportId: reportId},
							success: function (response) {
								if (response.success) {
									var columns = response.data;
									var options = "";
									$.each(columns, function (index, column) {
										options += "<option value='" + column.label + "' data-type='" + column.type + "' title='" + column.description + "'>" + column.userLabel + "</option>";
									});
									var select = $("#multiselect");
									select.empty();
									select.append(options);
									updateBuilder();
									$("#whereDiv").show();
								} else {
									notifyActionErrorReusable("${errorOccurredText}", response.errorMessage, ${showErrors});
								}
							},
							error: function (xhr) {
								showUserAjaxError(xhr, '${errorOccurredText}');
							}
						});
					}
				});

				$('#ajaxResponseContainer').on("click", ".alert .close", function () {
					$(this).parent().hide();
				});

				$('#multiselect').multiselect({
					search: {
						left: '<input type="text" class="form-control" placeholder="${searchText}" />',
						right: '<input type="text" class="form-control" placeholder="${searchText}" />'
					},
					fireSearch: function (value) {
						return value.length > 0;
					},
					sort: false
				});

				$('.parse-sql').on('click', function () {
					var result = $('#builder').queryBuilder('getSQL', $(this).data('stmt'));

					if (result !== null && result.sql.length) {
						console.log(result);
						bootbox.alert({
							title: $(this).text(),
							message: '<pre class="code-popup">' + result.sql + (result.params ? '\n\n' + result.params : '') + '</pre>'
						});
					}
				});

				$('#selected').on('click', function () {
					var values = '';
					$('#multiselect_to option').each(function (index, element) {
						values += element.value + ' - ' + element.text + '\n';
					});

					bootbox.alert({
						message: '<pre class="code-popup">' + values + '</pre>'
					});
				});

				$('#preview').on('click', function () {
					$('#preview').prop('disabled', true);

					var viewId = $("#views").val();
					var limit = $("#limit").val();
					if (!limit) {
						limit = "0";
					}

					var selectedColumns = $("#multiselect_to option").map(function () {
						return $(this).val();
					}).get();

					//https://stackoverflow.com/questions/24403732/check-if-array-is-empty-or-does-not-exist-js
					if (!selectedColumns.length === 0) {
						selectedColumns = $("#multiselect option").map(function () {
							return $(this).val();
						}).get();
					}

					var selfServiceOptions = {};
					selfServiceOptions.columns = selectedColumns;

					var selfServiceOptionsString = JSON.stringify(selfServiceOptions);

					//https://stackoverflow.com/questions/10398783/jquery-form-serialize-and-other-parameters
					$.ajax({
						type: "POST",
						url: "${pageContext.request.contextPath}/runReport",
						data: {selfServicePreview: true, reportFormat: "htmlDataTable",
							dummyBoolean: true, reportId: viewId,
							selfServiceOptions: selfServiceOptionsString,
							basicReport2: true, showInline: true, limit: limit},
						success: function (data) {
							$("#reportOutput").html(data);
						},
						error: function (xhr) {
							//https://stackoverflow.com/questions/6186770/ajax-request-returns-200-ok-but-an-error-event-is-fired-instead-of-success
							ajaxErrorHandler(xhr);
						},
						complete: function () {
							$('#preview').prop('disabled', false);
						}
					});
				});

				initializeBuilder();

			});

			function initializeBuilder() {
				$('#builder').queryBuilder({
					filters: [{
							id: 'placeholder',
							type: 'string'
						}
					]
				});
			}

			function updateBuilder() {
				var filters = createFilters();
				var force = true;
				$('#builder').queryBuilder('setFilters', force, filters);
				$('#builder').queryBuilder('reset');
			}

			function createFilters() {
				var filters = [];
				var ids = [];

				$('#multiselect option').each(function (index, element) {
					//console.log(index);
					//console.log(element.value);
					//console.log(element.text);
					var value = element.value;
					var text = element.text;
					var fieldType = $(element).data("type");
					if (fieldType === undefined) {
						fieldType = 'string';
					}

					if ($.inArray(value, ids) !== -1) {
						value += index;
					}

					ids.push(value);

					var filter = {
						id: value,
						label: text,
						type: fieldType
					};

					filters.push(filter);
				});

				return filters;
			}
		</script>
	</jsp:attribute>

	<jsp:body>
		<div class="row">
			<div class="col-md-12">
				<c:if test="${error != null}">
					<div class="alert alert-danger alert-dismissable">
						<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
						<p><spring:message code="page.message.errorOccurred"/></p>
						<c:if test="${showErrors}">
							<p>${encode:forHtmlContent(error)}</p>
						</c:if>
					</div>
				</c:if>

				<div id="ajaxResponseContainer">
					<div id="ajaxResponse">
					</div>
				</div>
			</div>
		</div>

		<div class="row" style="margin-bottom: 20px">
			<div class="col-md-4">
				<select id="views" class="form-control selectpicker">
					<option value="0">--</option>
				</select>
			</div>
		</div>

		<div class="row">
			<div class="col-md-5">
				<select name="from" id="multiselect" class="form-control" size="11" multiple="multiple">
				</select>
			</div>

			<div class="col-md-2">
				<button type="button" id="multiselect_undo" class="btn btn-primary btn-block">${undoText}</button>
				<button type="button" id="multiselect_rightAll" class="btn btn-block btn-default"><i class="glyphicon glyphicon-forward"></i></button>
				<button type="button" id="multiselect_rightSelected" class="btn btn-block btn-default"><i class="glyphicon glyphicon-chevron-right"></i></button>
				<button type="button" id="multiselect_leftSelected" class="btn btn-block btn-default"><i class="glyphicon glyphicon-chevron-left"></i></button>
				<button type="button" id="multiselect_leftAll" class="btn btn-block btn-default"><i class="glyphicon glyphicon-backward"></i></button>
				<button type="button" id="multiselect_redo" class="btn btn-warning btn-block">${redoText}</button>
			</div>

			<div class="col-md-5">
				<select name="to" id="multiselect_to" class="form-control" size="11" multiple="multiple"></select>

				<div class="row">
					<div class="col-md-6">
						<button type="button" id="multiselect_move_up" class="btn btn-block btn-default"><i class="glyphicon glyphicon-arrow-up"></i></button>
					</div>
					<div class="col-md-6">
						<button type="button" id="multiselect_move_down" class="btn btn-block btn-default col-sm-6"><i class="glyphicon glyphicon-arrow-down"></i></button>
					</div>
				</div>
			</div>
		</div>

		<div class="row" id="whereDiv" style="margin-top: 20px; display: none">
			<div class="col-md-12">
				<div class="row">
					<div class="col-md-12">
						<button class="btn btn-primary parse-sql" data-target="import_export" data-stmt="false">SQL</button>
						<button class="btn btn-primary parse-sql" data-target="import_export" data-stmt="question_mark">SQL statement
							(?)
						</button>
						<button class="btn btn-primary parse-sql" data-target="import_export" data-stmt="named">SQL statement (named)
						</button>
						<button class="btn btn-default" id="selected">Selected</button>
						<spring:message code="selfService.text.limit"/>&nbsp;
						<input id="limit" type="number" value="10">
						<button id="preview" class="btn btn-default">
							<spring:message code="reports.action.preview"/>
						</button>
					</div>
				</div>
				<div class="row">
					<div class="col-md-12">
						<div id="builder"></div>
					</div>
				</div>
			</div>
		</div>

		<div class="row">
			<div class="col-md-12">
				<div id="reportOutput"></div>
			</div>
		</div>
	</jsp:body>
</t:mainPage>
