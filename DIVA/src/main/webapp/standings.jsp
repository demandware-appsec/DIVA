<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="com.demandware.vulnapp.servlet.DIVAServletRequestWrapper"%>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="com.demandware.vulnapp.user.*" %>
<%@ page import="com.demandware.vulnapp.sessionmgmt.*" %>
<%@ page import="com.demandware.vulnapp.servlet.*" %>
<%@ page import="com.demandware.vulnapp.challenge.*" %>
<%@ page import="com.demandware.vulnapp.challenge.impl.ChallengeFactory.*" %>

<%
DIVAServletRequestWrapper req = (DIVAServletRequestWrapper)request;
SessionStorage sessionStore = (SessionStorage)req.getInformation(Dictionary.SESSION_STORE_OBJ);
ArrayList<User> users = Standings.handleRequest(req);
int numUsers = users.size();
int totUsers = UserManager.getInstance().getTotalUsers();
int totChal = Standings.getTotalChallenges();
int totPts = Standings.getMaxPoints();
int numActive = SessionManager.getInstance().getActiveSessions();
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
<jsp:include page="jspincludes/include_head.jsp" flush="true"></jsp:include>
</head>
<body>
	<div class="wrapper">
		<div class="box">
			<div class="row row-offcanvas row-offcanvas-left">
				<jsp:include page="jspincludes/sidebar.jsp" flush="true"></jsp:include>

				<!-- main right col -->
				<div class="column col-sm-10 col-xs-11" id="main">

					<jsp:include page="jspincludes/topnav.jsp" flush="true"></jsp:include>

					<div class="padding">
						<div class="full col-sm-9">

							<!-- content -->
							<div class="row">

								<!-- main col right -->
								<div class="col">

									<div class="well">
										<%
										if(!UserManager.getInstance().showLogin(sessionStore.getUser())){
										 %>
										<form class="navbar-form navbar-right" method="POST" autocomplete="off" action="/DIVA/overview.jsp"
												onsubmit="return confirm('You cannot undo this. Are you sure your want to delete?');">
											<input type="hidden" name="<%=Dictionary.DELETE_PARAM %>">
											<button type="submit" class="btn round btn-danger" value="Delete Me">
												Delete Me <span class="glyphicon glyphicon-remove"></span>
											</button>
										</form>
										<%
										}
										 %>
										<h3>Standings</h3>
									</div>

									<div class="panel panel-default">
										<div class="panel-heading">								
											<form class="form-inline" method=POST>
											<div class="form-group">
												<input type="radio" name="<%=Standings.TOP_PARAM %>" checked value="10"/>&nbsp;10
											</div>
											<div class="form-group">
												<input type="radio" name="<%=Standings.TOP_PARAM %>" value="25"/>&nbsp;25
											</div>
											<div class="form-group">
												<input type="radio" name="<%=Standings.TOP_PARAM %>" value="50"/>&nbsp;50<br/>
											</div>	
												<button type="submit" class="btn btn-default">Show Top Users</button>
											</form>
										</div>
										<div class="panel-body">
		<h4>Showing Top <%=numUsers %> of <%= totUsers%> Users</h4>
		<span><%=numActive %> <%=(numActive == 1) ? "Challenger" : "Challengers" %> Competing Now</span>
		
		<table width="100%" class="table table-striped table-bordered">
			<colgroup>
    			<col style="width:5%">
				<col style="width:40%">
				<col style="width:5%">
				<col style="width:10%">
				<col style="width:10%">
				<col style="width:15%">
				<col style="width:15%">
  			</colgroup>
		<tr>
			<th>
				Place
			</th>
			<th>
				Username
			</th>
			<th>
				Points<br/>out of<br/><%=totPts %>
			</th>
			<th>
				Completed<br/>Challenges<br/>of <%=totChal %>
			</th>
			<th>
				Online Now
			</th>
			<th>
				Last Activity
			</th>
			<th>
				Last Used IP
			</th>
		</tr>
		<%
		int place = 1;
		for(User user : users){
		%>
		<tr>
			<td>
				<%=place %>
			</td>
			<td>
				<%=user.getUserName() %>
				<%
					List<String> challengeNames = new ArrayList<String>();
					for(ChallengeType cType : user.getCompletedChallenges()){
						ChallengeInfo cInfo = ChallengePlan.getInstance().getChallengeForType(cType);
						challengeNames.add(cInfo.getName());
					}
				%>
				<!-- <%=challengeNames.toString() %> -->
			</td>
			<td>
				<%=user.getPoints() %>
			</td>
			<td>
				<%=user.getCompletedChallenges().size() %>
			</td>
			<td>
				<%
					if(!UserManager.isFakeUser(user)){
						out.print(SessionManager.getInstance().isUserActive(user) ? "Yes" : "No");	
					}
				%>
			</td>
			<td>
				<%=UserManager.isFakeUser(user) ? "" : user.getLastActivity() %>
			</td>
			<td>
				<%=UserManager.isFakeUser(user) ? "" : user.getLastIP() %>
			</td>
		</tr>
		<%
			place++;
		} 
		%>
		</table>
										</div>
									</div>

							</div>
							<!--/row-->

							<jsp:include page="jspincludes/footer.jsp" flush="true"></jsp:include>

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