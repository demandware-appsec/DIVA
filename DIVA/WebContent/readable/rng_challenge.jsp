<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="com.demandware.vulnapp.flags.*" %>
<%@ page import="com.demandware.vulnapp.sessionmgmt.*" %>
<%@ page import="com.demandware.vulnapp.servlet.*" %>
<%@ page import="com.demandware.vulnapp.challenge.*" %>
<%@ page import="com.demandware.vulnapp.challenge.impl.*"%>
<%@ page import="com.demandware.vulnapp.util.*" %>
<%@ page import="com.demandware.vulnapp.servlet.Dictionary" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.AbstractMap.*" %>
<%--

Random Number Generator Crack Challenge

 --%>
<%
DIVAServletRequestWrapper req = (DIVAServletRequestWrapper)request; 
SessionStorage sessionStore = (SessionStorage)req.getInformation(Dictionary.SESSION_STORE_OBJ);
String flag = (String)req.getInformation(Dictionary.FLAG_VALUE);
RNGChallenge chall = (RNGChallenge)req.getInformation(Dictionary.CURRENT_CHALLENGE_OBJ);
Boolean isCorrect = chall.handleChallengeRequest(req);
boolean firstTime = false;
SimpleEntry<String,String> values = null;

if(!chall.hasAllGuesses(req)){
	firstTime = true;
}
values = chall.getNextRNGs(req);

String rng1 = values.getKey();
String rng2 = values.getValue();
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
		<h4>Good luck on this challenge. You must crack the next two random numbers in this combination to proceed.</h4>
		<h4>You'll have to be quick, however, as the combinations change every few minutes.</h4>
		<br/>
		<%
		if(!firstTime && !isCorrect){
			out.print("<h4>Incorrect</h4>");	
		}else if(!firstTime && isCorrect){
			out.print("<h4>Correct! Flag: " + flag + "</h4>");
		}
		
		%>
		<form method=POST autocomplete="off">
			<div class="form-group"><input type="text" name="<%=RNGChallenge.GIVEN_FIRST %>" value="<%=rng1 %>" readonly></div>
			<div class="form-group"><input type="text" name="<%=RNGChallenge.GIVEN_SECOND %>" value="<%=rng2 %>" readonly></div>
			<div class="form-group"><input type="text" name="<%=RNGChallenge.GUESS_FIRST %>"></div>
			<div class="form-group"><input type="text" name="<%=RNGChallenge.GUESS_SECOND %>"></div>
			<input type="submit" value="Guess">
		</form>
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