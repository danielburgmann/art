<%-- 
    Document   : accessRightsConfig
    Created on : 22-Apr-2014, 12:03:03
    Author     : Timothy Anyona

Access rights configuration
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.accessRightsConfiguration" var="pageTitle"/>

<spring:message code="page.message.errorOccurred" var="errorOccurredText" javaScriptEscape="true"/>
<spring:message code="page.message.rightsRevoked" var="rightsRevokedText" javaScriptEscape="true"/>
<spring:message code="page.message.rightsGranted" var="rightsGrantedText" javaScriptEscape="true"/>
<spring:message code="page.message.selectUserOrUserGroup" var="selectUserOrUserGroupText" javaScriptEscape="true"/>
<spring:message code="accessRights.message.selectReportOrReportGroupOrJob" var="selectReportOrReportGroupOrJobText" javaScriptEscape="true"/>
<spring:message code="page.text.available" var="availableText" javaScriptEscape="true"/>
<spring:message code="page.text.selected" var="selectedText" javaScriptEscape="true"/>
<spring:message code="page.text.search" var="searchText" javaScriptEscape="true"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-8 col-md-offset-2"
					 hasNotify="true">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/multi-select-0.9.12/css/multi-select.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/multiSelect.css">
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/multi-select-0.9.12/js/jquery.multi-select.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.quicksearch.js"></script>

		<script type="text/javascript">
			$(document).ready(function () {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="accessRightsConfig"]').parent().addClass('active');

				$('.multi-select').multiSelect({
					cssClass: 'wide-multi-select',
					selectableHeader: "<div>${availableText}</div>\n\
					<input type='text' class='form-control input-sm' autocomplete='off' placeholder='${searchText}'>",
					selectionHeader: "<div>${selectedText}</div>\n\
					<input type='text' class='form-control input-sm' autocomplete='off' placeholder='${searchText}'>",
					afterInit: function (ms) {
						var that = this,
								$selectableSearch = that.$selectableUl.prev(),
								$selectionSearch = that.$selectionUl.prev(),
								selectableSearchString = '#' + that.$container.attr('id') + ' .ms-elem-selectable:not(.ms-selected)',
								selectionSearchString = '#' + that.$container.attr('id') + ' .ms-elem-selection.ms-selected';
						that.qs1 = $selectableSearch.quicksearch(selectableSearchString)
								.on('keydown', function (e) {
									if (e.which === 40) {
										that.$selectableUl.trigger("focus");
										return false;
									}
								});
						that.qs2 = $selectionSearch.quicksearch(selectionSearchString)
								.on('keydown', function (e) {
									if (e.which === 40) {
										that.$selectionUl.trigger("focus");
										return false;
									}
								});
					},
					afterSelect: function () {
						this.qs1.cache();
						this.qs2.cache();
					},
					afterDeselect: function () {
						this.qs1.cache();
						this.qs2.cache();
					}
				}); //end multiselect

				$('#actionsDiv').on('click', '.updateRights', function () {
					var action = $(this).data('action');

					var users = $('#users').val();
					var userGroups = $('#userGroups').val();
					var reports = $('#reports').val();
					var reportGroups = $('#reportGroups').val();
					var jobs = $('#jobs').val();

					if (users === null && userGroups === null) {
						bootbox.alert("${selectUserOrUserGroupText}");
						return;
					}
					if (reports === null && reportGroups === null && jobs === null) {
						bootbox.alert("${selectReportOrReportGroupOrJobText}");
						return;
					}

					var rightsUpdatedMessage;
					if (action === 'grant') {
						rightsUpdatedMessage = "${rightsGrantedText}";
					} else {
						rightsUpdatedMessage = "${rightsRevokedText}";
					}

					$.ajax({
						type: "POST",
						dataType: "json",
						url: "${pageContext.request.contextPath}/updateAccessRight",
						data: {action: action, users: users, userGroups: userGroups,
							reports: reports, reportGroups: reportGroups,
							jobs: jobs},
						success: function (response) {
							if (response.success) {
								notifyActionSuccessReusable(rightsUpdatedMessage);
							} else {
								notifyActionErrorReusable("${errorOccurredText}", response.errorMessage, ${showErrors});
							}
						},
						error: ajaxErrorHandler
					}); //end ajax
				}); //end on click

				//handle select all/deselect all
				addSelectDeselectAllHandler();

				$('#ajaxResponseContainer').on("click", ".alert .close", function () {
					$(this).parent().hide();
				});

			}); //end document ready
		</script>
	</jsp:attribute>

	<jsp:body>
		<form class="form-horizontal" method="POST" action="">
			<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
			<fieldset>
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

				<div class="form-group">
					<label class="control-label col-md-2" for="users">
						<spring:message code="page.text.users"/>
					</label>
					<div class="col-md-10">
						<select name="users" id="users" multiple="multiple" class="form-control multi-select">
							<c:forEach var="user" items="${users}">
								<option value="${user.userId}">
									${encode:forHtmlContent(user.username)} ${empty user.fullName? "": " (".concat(encode:forHtmlContent(user.fullName)).concat(")")}
								</option>
							</c:forEach>
						</select>
						<a href="#" class="select-all" data-item="#users"><spring:message code="page.text.selectAll"/></a> / 
						<a href="#" class="deselect-all" data-item="#users"><spring:message code="page.text.deselectAll"/></a>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-2" for="userGroups">
						<spring:message code="page.text.userGroups"/>
					</label>
					<div class="col-md-10">
						<select name="userGroups" id="userGroups" multiple="multiple" class="form-control multi-select">
							<c:forEach var="userGroup" items="${userGroups}">
								<option value="${userGroup.userGroupId}">
									<encode:forHtmlContent value="${userGroup.name}"/>
								</option>
							</c:forEach>
						</select>
						<a href="#" class="select-all" data-item="#userGroups"><spring:message code="page.text.selectAll"/></a> / 
						<a href="#" class="deselect-all" data-item="#userGroups"><spring:message code="page.text.deselectAll"/></a>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-2" for="reports">
						<spring:message code="page.text.reports"/>
					</label>
					<div class="col-md-10">
						<select name="reports" id="reports" multiple="multiple" class="form-control multi-select">
							<c:forEach var="report" items="${reports}">
								<option value="${report.reportId}">
									<encode:forHtmlContent value="${report.name}"/>
								</option>
							</c:forEach>
						</select>
						<a href="#" class="select-all" data-item="#reports"><spring:message code="page.text.selectAll"/></a> / 
						<a href="#" class="deselect-all" data-item="#reports"><spring:message code="page.text.deselectAll"/></a>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-2" for="reportGroups">
						<spring:message code="page.text.reportGroups"/>
					</label>
					<div class="col-md-10">
						<select name="reportGroups" id="reportGroups" multiple="multiple" class="form-control multi-select">
							<c:forEach var="reportGroup" items="${reportGroups}">
								<option value="${reportGroup.reportGroupId}">
									<encode:forHtmlContent value="${reportGroup.name}"/>
								</option>
							</c:forEach>
						</select>
						<a href="#" class="select-all" data-item="#reportGroups"><spring:message code="page.text.selectAll"/></a> / 
						<a href="#" class="deselect-all" data-item="#reportGroups"><spring:message code="page.text.deselectAll"/></a>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-2" for="jobs">
						<spring:message code="accessRights.text.jobs"/>
					</label>
					<div class="col-md-10">
						<select name="jobs" id="jobs" multiple="multiple" class="form-control multi-select">
							<c:forEach var="job" items="${jobs}">
								<option value="${job.jobId}">
									${encode:forHtmlContent(job.name)} (${job.jobId})
								</option>
							</c:forEach>
						</select>
						<a href="#" class="select-all" data-item="#jobs"><spring:message code="page.text.selectAll"/></a> / 
						<a href="#" class="deselect-all" data-item="#jobs"><spring:message code="page.text.deselectAll"/></a>
					</div>
				</div>
				<div class="form-group">
					<div class="col-md-12">
						<div id="actionsDiv" class="pull-right">
							<a class="btn btn-default" 
							   href="${pageContext.request.contextPath}/accessRights">
								<spring:message code="page.action.show"/>
							</a>
							<button type="button" class="btn btn-default updateRights" data-action="grant">
								<spring:message code="page.action.grant"/>
							</button>
							<button type="button" class="btn btn-default updateRights" data-action="revoke">
								<spring:message code="page.action.revoke"/>
							</button>
						</div>
					</div>
				</div>
			</fieldset>
		</form>
	</jsp:body>
</t:mainPageWithPanel>
