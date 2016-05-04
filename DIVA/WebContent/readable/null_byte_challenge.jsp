<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="com.demandware.vulnapp.flags.*" %>
<%@ page import="com.demandware.vulnapp.sessionmgmt.*" %>
<%@ page import="com.demandware.vulnapp.servlet.*" %>
<%@ page import="com.demandware.vulnapp.challenge.*" %>
<%@ page import="com.demandware.vulnapp.challenge.impl.*" %>
<%@ page import="com.demandware.vulnapp.util.*" %>
<%@ page import="com.demandware.vulnapp.servlet.Dictionary" %>
<%@ page import="java.util.*" %>
<%--

Null Byte String Challenge

 --%>
<%
DIVAServletRequestWrapper req = (DIVAServletRequestWrapper)request;
SessionStorage sessionStore = (SessionStorage)req.getInformation(Dictionary.SESSION_STORE_OBJ);
NullStringChallenge chall = (NullStringChallenge)req.getInformation(Dictionary.CURRENT_CHALLENGE_OBJ);
String contents = chall.handleChallengeRequest(req);
%>

<!DOCTYPE html>
<html lang="en">
<head>
<meta http-equiv="content-type" content="text/html; charset=UTF-8">
<meta charset="utf-8">
<title>DIVA - Overview</title>
<meta name="generator" content="Bootply" />
<meta name="viewport"
	content="width=device-width, initial-scale=1, maximum-scale=1">
<jsp:include page="../jspincludes/include_head.jsp" flush="true"></jsp:include>
</head>
<body>
	<div class="wrapper">
		<div class="box">
			<div class="row row-offcanvas row-offcanvas-left">
				<jsp:include page="../jspincludes/sidebar.jsp" flush="true"></jsp:include>

				<!-- main right col -->
				<div class="column col-sm-10 col-xs-11" id="main">

					<jsp:include page="../jspincludes/topnav.jsp" flush="true"></jsp:include>

					<div class="padding">
						<div class="full col-sm-9">

							<!-- content -->
							<div class="row">

								<!-- main col right -->
								<div class="col">

									<div class="well">
										<h3>Challenge</h3>
									</div>

									<div class="panel panel-default">
									
										<div class="panel-body">
											<div id="challenge">
		<h2>Choose from this selection of Hacker articles</h2>
		<form method=POST autocomplete="off">
		<div class="radio"><input type="radio" name="<%=NullStringChallenge.FILE_PARAM %>" value="<%=NullStringChallenge.PHRACK %>">The Hacker Manifesto
		</div>
		<div class="radio"><input type="radio" name="<%=NullStringChallenge.FILE_PARAM %>" value="<%=NullStringChallenge.WHAT_IS %>">What is a Hacker?
		</div>
		<div class="radio"><input type="radio" name="<%=NullStringChallenge.FILE_PARAM %>" value="<%=NullStringChallenge.HACKER_ATT %>">A Hacker's Attitude
		</div>
		<!-- 
		<div class="radio"><input type="radio" name="<%=NullStringChallenge.FILE_PARAM %>" value="<%=NullStringChallenge.FLAG_FILE%>">Flag</div>
		 -->
		<input type="submit" value="Read">
		</form>
		<br/>
		<pre><%=contents%></pre>
	</div>

										</div>
									</div>

							</div>
							<!--/row-->

							<jsp:include page="../jspincludes/footer.jsp" flush="true"></jsp:include>

						</div>
						<!-- /col-9 -->
					</div>
					<!-- /padding -->
				</div>
				<!-- /main -->

			</div>
		</div>
	</div>

</body>
</html>