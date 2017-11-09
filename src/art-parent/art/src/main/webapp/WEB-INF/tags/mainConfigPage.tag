<%-- 
    Document   : mainConfigPage
    Created on : 29-Oct-2017, 14:14:26
    Author     : Timothy Anyona
--%>

<%@tag description="Template for main config pages" pageEncoding="UTF-8"%>
<%@tag trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="title" required="true" %>
<%@attribute name="css" fragment="true" %>
<%@attribute name="javascript" fragment="true" %>
<%@attribute name="mainColumnClass" required="true" %>
<%@attribute name="mainPanelTitle" %>
<%@attribute name="aboveMainPanel" fragment="true" %>
<%@attribute name="belowMainPanel" fragment="true" %>
<%@attribute name="leftMainPanel" fragment="true" %>
<%@attribute name="rightMainPanel" fragment="true" %>
<%@attribute name="headContent" fragment="true" %>

<%-- any content can be specified here e.g.: --%>
<t:mainPage title="${title}">
	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dataTables/Select-1.2.0/css/select.bootstrap.min.css"/>
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dataTables/Buttons-1.2.4/css/buttons.dataTables.min.css"/>
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dataTables/Buttons-1.2.4/css/buttons.bootstrap.min.css"/>
		<!--<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dataTables/Responsive-2.0.2/css/responsive.bootstrap.min.css"/>-->

		<jsp:invoke fragment="css"/>
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Select-1.2.0/js/dataTables.select.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Buttons-1.2.4/js/dataTables.buttons.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Buttons-1.2.4/js/buttons.bootstrap.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/JSZip-2.5.0/jszip.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/pdfmake-0.1.18/pdfmake.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/pdfmake-0.1.18/vfs_fonts.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Buttons-1.2.4/js/buttons.html5.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Buttons-1.2.4/js/buttons.print.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Buttons-1.2.4/js/buttons.colVis.min.js"></script>
<!--		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Responsive-2.0.2/js/dataTables.responsive.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Responsive-2.0.2/js/responsive.bootstrap.min.js"></script>-->

		<script type="text/javascript" src="${pageContext.request.contextPath}/js/notify-combined-0.3.1.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootbox-4.4.0.min.js"></script>

		<jsp:invoke fragment="javascript"/>
	</jsp:attribute>

	<jsp:attribute name="headContent">
		<jsp:invoke fragment="headContent"/>
	</jsp:attribute>

	<jsp:body>
		<jsp:invoke fragment="aboveMainPanel"/>

		<div class="row">
			<jsp:invoke fragment="leftMainPanel"/>

			<div class="${mainColumnClass}">
				<div class="panel panel-success">
					<div class="panel-heading">
						<h4 class="panel-title text-center">
							<c:choose>
								<c:when test="${empty mainPanelTitle}">
									${encode:forHtmlContent(title)}
								</c:when>
								<c:otherwise>
									${encode:forHtmlContent(mainPanelTitle)}
								</c:otherwise>
							</c:choose>
						</h4>
					</div>
					<div class="panel-body">
						<jsp:doBody/>
					</div>
				</div>
			</div>

			<jsp:invoke fragment="rightMainPanel"/>
		</div>

		<jsp:invoke fragment="belowMainPanel"/>
    </jsp:body>
</t:mainPage>