<%-- 
    Document   : permissionsConfig
    Created on : 27-Jun-2018, 18:29:31
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.permissionsConfiguration" var="pageTitle"/>

<spring:message code="page.message.errorOccurred" var="errorOccurredText" javaScriptEscape="true"/>
<spring:message code="page.message.permissionsAdded" var="permissionsAddedText" javaScriptEscape="true"/>
<spring:message code="page.message.permissionsRemoved" var="permissionsRemovedText" javaScriptEscape="true"/>
<spring:message code="page.message.selectUserOrUserGroup" var="selectUserOrUserGroupText" javaScriptEscape="true"/>
<spring:message code="permissions.message.selectRoleOrPermission" var="selectRoleOrPermissionText" javaScriptEscape="true"/>
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
				$('a[href*="permissionsConfig"]').parent().addClass('active');
					
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

				$('#actionsDiv').on('click', '.updatePermissions', function () {
					var action = $(this).data('action');

					var users = $('#users').val();
					var userGroups = $('#userGroups').val();
					var roles = $('#roles').val();
					var permissions = $('#permissions').val();

					if (users === null && userGroups === null) {
						bootbox.alert("${selectUserOrUserGroupText}");
						return;
					}
					if (roles === null && permissions === null) {
						bootbox.alert("${selectRoleOrPermissionText}");
						return;
					}

					var permissionsUpdatedMessage;
					if (action === 'add') {
						permissionsUpdatedMessage = "${permissionsAddedText}";
					} else {
						permissionsUpdatedMessage = "${permissionsRemovedText}";
					}

					$.ajax({
						type: "POST",
						dataType: "json",
						url: "${pageContext.request.contextPath}/updatePermissions",
						data: {action: action, users: users, userGroups: userGroups,
							roles: roles, permissions: permissions},
						success: function (response) {
							if (response.success) {
								notifyActionSuccessReusable(permissionsUpdatedMessage);
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
					<label class="control-label col-md-2" for="roles">
						<spring:message code="page.text.roles"/>
					</label>
					<div class="col-md-10">
						<select name="roles" id="roles" multiple="multiple" class="form-control multi-select">
							<c:forEach var="role" items="${roles}">
								<option value="${role.roleId}">
									<encode:forHtmlContent value="${role.name}"/>
								</option>
							</c:forEach>
						</select>
						<a href="#" class="select-all" data-item="#roles"><spring:message code="page.text.selectAll"/></a> / 
						<a href="#" class="deselect-all" data-item="#roles"><spring:message code="page.text.deselectAll"/></a>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-2" for="permissions">
						<spring:message code="page.text.permissions"/>
					</label>
					<div class="col-md-10">
						<select name="permissions" id="permissions" multiple="multiple" class="form-control multi-select">
							<c:forEach var="permission" items="${permissions}">
								<option value="${permission.permissionId}">
									<encode:forHtmlContent value="${permission.name}"/>
								</option>
							</c:forEach>
						</select>
						<a href="#" class="select-all" data-item="#permissions"><spring:message code="page.text.selectAll"/></a> / 
						<a href="#" class="deselect-all" data-item="#permissions"><spring:message code="page.text.deselectAll"/></a>
					</div>
				</div>
				<div class="form-group">
					<div class="col-md-12">
						<div id="actionsDiv" class="pull-right">
							<button type="button" class="btn btn-default updatePermissions" data-action="add">
								<spring:message code="page.action.add"/>
							</button>
							<button type="button" class="btn btn-default updatePermissions" data-action="remove">
								<spring:message code="page.action.remove"/>
							</button>
						</div>
					</div>
				</div>
			</fieldset>
		</form>
	</jsp:body>
</t:mainPageWithPanel>
