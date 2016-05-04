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

Timing Challenge

 --%>
<%
DIVAServletRequestWrapper req = (DIVAServletRequestWrapper)request;
SessionStorage sessionStore = (SessionStorage)req.getInformation(Dictionary.SESSION_STORE_OBJ); 
TimingChallenge chall = (TimingChallenge)req.getInformation(Dictionary.CURRENT_CHALLENGE_OBJ);
String result = chall.handleChallengeRequest(req);
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
		<h4>Welcome rhood</h4>
		
		<h4>Execute Bank Transfer</h4>
		<br/>
		<form method=POST autocomplete="off">
			<div class="row">
			<div class="form-group" >
				<label>Username to transfer from</label>
				<input type="text" class="form-control" name="<%=TimingChallenge.UNAME_PARAM %>" > 
			</div>
			</div>
			<div class="row">
			<div class="form-group">
				<label>Transfer Pin number</label>
				<input type="text" class="form-control" name="<%=TimingChallenge.PIN_PARAM %>" > 
			</div>
			</div>
			<input type="submit" value="Transfer">
		</form>
		<br/>
		<pre><%=result %></pre>
		
		<h4>Messages:</h5>
		<p>
			From littlejohn:<br/>
			Robin, I've tracked Prince John's username to one of these <%=TimingChallenge.possibleUserNames.length %> possible values:<br/>
			<%for(String s : TimingChallenge.possibleUserNames){ out.print(s + " "); }%><br/>
			Remember that PINs are <%=TimingChallenge.PIN_SIZE %> characters long, but only contain the numbers 0-9. <br/>
			Hopefully you can guess his username and pin to make the bank transfer.
		</p> 
		
		
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