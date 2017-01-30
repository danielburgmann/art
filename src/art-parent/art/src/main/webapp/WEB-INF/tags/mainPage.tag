<%-- 
    Document   : mainPage
    Created on : 17-Sep-2013, 10:08:05
    Author     : Timothy Anyona

Template for any main application page.
Includes bootstrap css, page header (navbar), page footer
bootstrap js, jquery js, datatables css, datatables js
--%>

<%@tag description="Main Page Template" pageEncoding="UTF-8"%>
<%@tag trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="title" %>
<%@attribute name="css" fragment="true" %>
<%@attribute name="javascript" fragment="true" %>
<%@attribute name="headContent" fragment="true" %>

<%-- any content can be specified here e.g.: --%>
<t:genericPage title="ART - ${title}">
	<jsp:attribute name="headContent">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-1.12.4.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-3.3.6/js/bootstrap.min.js"></script>

		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-hover-dropdown-2.0.3.min.js"></script>
		
		<jsp:invoke fragment="headContent"/>
	</jsp:attribute>

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dataTables/DataTables-1.10.13/css/dataTables.bootstrap.min.css"/>
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dataTables/Select-1.2.0/css/select.bootstrap.min.css"/>
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dataTables/Buttons-1.2.4/css/buttons.dataTables.min.css"/>
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dataTables/Buttons-1.2.4/css/buttons.bootstrap.min.css"/>
		<!--<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dataTables/Responsive-2.0.2/css/responsive.bootstrap.min.css"/>-->

		<jsp:invoke fragment="css"/>
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/art.js"></script>

		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/DataTables-1.10.13/js/jquery.dataTables.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/DataTables-1.10.13/js/dataTables.bootstrap.min.js"></script>
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

		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootbox-4.4.0.min.js"></script>

		<script type="text/javascript">
			$(document).ajaxStart(function () {
				$('#spinner').show();
			}).ajaxStop(function () {
				$('#spinner').hide();
			});
		</script>

		<jsp:invoke fragment="javascript"/>
	</jsp:attribute>

	<jsp:attribute name="header">
		<jsp:include page="/WEB-INF/jsp/header.jsp"/>
	</jsp:attribute>

	<jsp:attribute name="footer">
		<jsp:include page="/WEB-INF/jsp/footer.jsp"/>
	</jsp:attribute>

	<jsp:body>
		<div id="spinner">
			<img src="${pageContext.request.contextPath}/images/spinner.gif" alt="Processing..." />
		</div>
		<jsp:doBody/>
	</jsp:body>
</t:genericPage>