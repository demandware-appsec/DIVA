<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="com.demandware.vulnapp.flags.*" %>
<%@ page import="com.demandware.vulnapp.sessionmgmt.*" %>
<%@ page import="com.demandware.vulnapp.servlet.*" %>
<%@ page import="com.demandware.vulnapp.challenge.*" %>
<%@ page import="com.demandware.vulnapp.util.*" %>
<%@ page import="com.demandware.vulnapp.servlet.Dictionary" %>
<%@ page import="java.util.*" %>
<%--

 Challenge

 --%>
<%
DIVAServletRequestWrapper req = (DIVAServletRequestWrapper)request;
SessionStorage sessionStore = (SessionStorage)req.getInformation(Dictionary.SESSION_STORE_OBJ); 
String flag = (String)req.getInformation(Dictionary.FLAG_VALUE);
%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title><%=((ChallengeInfo)req.getInformation(Dictionary.CURRENT_CHALLENGE_INFO_OBJ)).getName() %></title>
	<jsp:include page="../jspincludes/include_head.jsp" flush="true"/>
</head>
<body>
	<jsp:include page="../jspincludes/include_top.jsp" flush="true"/>

	<div id="challenge">

	</div>
	<script src="../js/bootstrap.min.js"></script>
</body>
</html> 