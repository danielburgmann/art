<% if (request.getParameter("_isFragment")==null) { %>

<%	
 java.util.ResourceBundle fMessages = java.util.ResourceBundle.getBundle("art.i18n.ArtMessages",request.getLocale());
%>

<p>
<hr style="width:100%;height:1px">

<div style="float: left; text-align: left">
	<span style="font-size:75%"><i><a href="http://art.sourceforge.net">ART</a></i></span>
</div>

<div style="float: right; text-align: right">
	<span style="font-size:75%">
	 ART Reporting Tool ver. <%=art.servlets.ArtDBCP.getArtVersion()%> <img src="<%=request.getContextPath() + art.servlets.ArtDBCP.getArtSetting("bottom_logo")%>" alt="">
	  <i><a href="mailto:<%=art.servlets.ArtDBCP.getArtSetting("administrator")%>"><%=fMessages.getString("artSupport")%></a></i>
	</span>
</div>
</p>

 </body>
</html>

<% } %>
