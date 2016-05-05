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
<%--

XSS Challenge

 --%>
<%
DIVAServletRequestWrapper req = (DIVAServletRequestWrapper)request; 
SessionStorage sessionStore = (SessionStorage)req.getInformation(Dictionary.SESSION_STORE_OBJ);
String flag = (String)req.getInformation(Dictionary.FLAG_VALUE);
XSSChallenge chall = (XSSChallenge)req.getInformation(Dictionary.CURRENT_CHALLENGE_OBJ);
SessionStorage store = ((SessionStorage)req.getInformation(Dictionary.SESSION_STORE_OBJ));
boolean isAdminSession = chall.isAdminSession(req);
chall.handleChallengeRequest(req);
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
		<script>
			function validateForm() {
			    var x = document.forms["recipientform"]["<%=XSSChallenge.RECIPIENT_PARAM%>"].value;
			    var y = document.forms["recipientform"]["<%=XSSChallenge.SEND_FLAG_PARAM%>"];
			    if (x == null || x == "") {
			        alert("Please choose an option");
			        return false;
			    }
			    if(y.checked && <%=!isAdminSession%>){
			    	alert("Only Administrators may send flags");
			    	return false;
			    }
			}
		</script>
		<%if(isAdminSession){%>
			<pre>Flag:<%=flag %></pre>	
		<%}%>
		<form method=POST name="recipientform" onsubmit="return validateForm()" autocomplete="off">
			<textarea name="<%=XSSChallenge.MESSAGE_PARAM %>" rows="20" cols="100"></textarea>
			<br/>
			<div class="radio"><input type="radio" id="user_radio" name="<%=XSSChallenge.RECIPIENT_PARAM %>" value="<%=store.getUser().getUserName() %>">Send to User</div>
			<div class="radio"><input type="radio" id="admin_radio" name="<%=XSSChallenge.RECIPIENT_PARAM %>" value="<%=XSSChallenge.RECIPIENT_ADMIN %>">Send to Admin</div>
			<div class="checkbox"><input type="checkbox" name="<%=XSSChallenge.SEND_FLAG_PARAM%>">Send Access Flag</div>
			<input type="submit" value="Send">
		</form>
		<script>
			document.forms["recipientform"].reset();
		</script>
		
		<%if(isAdminSession){ %>
			<p>
				<% 
				String adminMessage = req.getParameter(XSSChallenge.ADMIN_MESSAGE_PARAM);
				if(adminMessage != null){
					out.print(adminMessage);
				}
				%>
			</p> 
		<%} %>
		
		<%if(!isAdminSession) {%>
			<br/><br/>
			<form method=GET autocomplete="off">
				<input type="hidden" name="<%=XSSChallenge.CLEAR_MESSAGES %>" value="true">
				<input type="submit" value="Clear Messages">
			</form>
			<br/>
			<table class="table table-bordered">
				<tr>
					<th>Number</th>
					<th>Messages</th>
				</tr>
				<% 
				Queue<String> messages = chall.getMessagesForUser(store);
				if(messages != null){
					int i = 1;
					for(String s : messages){ 
						out.println("<tr><td>" + i + "</td><td>" + s + "</td></tr>");
						i++;
					}
				}%>
			</table>
		<%} %>
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