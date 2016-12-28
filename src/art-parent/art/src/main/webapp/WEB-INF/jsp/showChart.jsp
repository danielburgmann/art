<%-- 
    Document   : showChart
    Created on : 30-Oct-2014, 15:58:59
    Author     : Timothy Anyona

Display a chart report
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://cewolf.sourceforge.net/taglib/cewolf.tld" prefix="cewolf" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>


<script type="text/javascript">
	//must use jQuery rather than $ because of conflict with prototype.js when chart shown in dashboard
	jQuery(document).ready(function () {
		jQuery("[data-toggle='tooltip']").tooltip({container: 'body'});
	});
</script>

<div align="center" style="width: 90%;">
	<cewolf:chart 
		id="${htmlElementId}" 
		title="${chart.title}" 
		type="${chart.type}" 
		xaxislabel="${chart.xAxisLabel}" 
		yaxislabel="${chart.yAxisLabel}"
		showlegend="${chart.chartOptions.showLegend}"
		>

		<cewolf:colorpaint color="${chart.backgroundColor}"/>

		<cewolf:data>
			<cewolf:producer id="chart"/>
		</cewolf:data>

		<%-- run internal post processor. parameters are already set in the chart object, chartOptions property --%>
		<cewolf:chartpostprocessor id="chart"/>
	</cewolf:chart>

	<cewolf:img 
		chartid="${htmlElementId}" 
		renderer="/cewolf" 
		width="${chart.chartOptions.width}" 
		height="${chart.chartOptions.height}"
		removeAfterRender="true"
		>

		<c:choose>
			<c:when test="${(not empty chart.hyperLinks) || (not empty chart.drilldownLinks)}">
				<cewolf:map tooltipgeneratorid="chart" linkgeneratorid="chart"
							target="${chart.openLinksInNewWindow ? '_blank' : '_self'}"/> 
			</c:when>
			<c:when test="${chart.hasTooltips}">
				<cewolf:map tooltipgeneratorid="chart"/> 
			</c:when>
		</c:choose>
	</cewolf:img>
</div>
