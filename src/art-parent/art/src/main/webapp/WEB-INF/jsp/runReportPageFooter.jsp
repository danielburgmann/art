<%-- 
    Document   : runReportPageFooter
    Created on : 30-May-2014, 16:16:56
    Author     : Timothy Anyona

Html page footer fragment when displaying report output in a new page
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:choose>
	<c:when test="${allowSelectParameters}">
	</div>
</div>
</div>

<script type="text/javascript">
	$(document).ajaxStart(function () {
		$('#spinner').show();
	}).ajaxStop(function () {
		$('#spinner').hide();
	});

	var token = $("meta[name='_csrf']").attr("content");
	var header = $("meta[name='_csrf_header']").attr("content");
	$(document).ajaxSend(function (e, xhr, options) {
		if (header) {
			xhr.setRequestHeader(header, token);
		}
	});
</script>
</c:when>
<c:otherwise>
	<script>
		$(document).ready(function () {
			var httpMethod = "${httpMethod}";
			if (httpMethod === "GET") {
				var mainRefreshPeriodSeconds = ${refreshPeriodSeconds};
				if (mainRefreshPeriodSeconds >= 5) {
					var mainRefreshPeriodMilliseconds = mainRefreshPeriodSeconds * 1000;
					setInterval(function () {
						location.reload(true);
					}, mainRefreshPeriodMilliseconds);
				}
			}
		});
	</script>
</c:otherwise>
</c:choose>

</div>
</div>
<div id="push"></div>
</div>

<jsp:include page="/WEB-INF/jsp/footer.jsp"/>

</body>
</html>
