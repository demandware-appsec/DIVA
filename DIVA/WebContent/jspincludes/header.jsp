<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="com.demandware.vulnapp.sessionmgmt.SessionStorage" %>
<%@ page import="com.demandware.vulnapp.user.LoginRegister" %> 
<%@ page import="com.demandware.vulnapp.user.UserManager" %> 
<%@ page import="com.demandware.vulnapp.servlet.DIVAServletRequestWrapper" %>
<%@ page import="com.demandware.vulnapp.servlet.Dictionary"%>
<%@ page import="com.demandware.vulnapp.util.exception.AccountException" %>
<%
	DIVAServletRequestWrapper req = (DIVAServletRequestWrapper)request;
	String loginRegisterMessage = (String)req.getInformation(Dictionary.LOGIN_PROBLEM);	 


	SessionStorage sessionStore = (SessionStorage)req.getInformation(Dictionary.SESSION_STORE_OBJ);
	boolean shouldShowLogin = UserManager.getInstance().showLogin(sessionStore.getUser());
%>

<nav class="navbar navbar-default">
  <div class="container">
    <!-- Brand and toggle get grouped for better mobile display -->
    <div class="navbar-header">
      <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar-collapse-1" aria-expanded="false">
        <span class="sr-only">Toggle navigation</span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
      </button>
	  <img src="/DIVA/img/diva_icon.png" class="navbar-brand"></img><a href="/DIVA/overview.jsp" class="navbar-brand">DIVA</a>    </div>

    <!-- Collect the nav links, forms, and other content for toggling -->
    <div class="collapse navbar-collapse" id="navbar-collapse-1">
      <ul class="nav navbar-nav">
        <li><p class="navbar-btn">
				<a href="/DIVA/overview.jsp" class="btn btn-default"><span class="glyphicon glyphicon-home"></span>&nbsp;&nbsp;Home</a>
			</p>
		</li>
        <li>
			<p class="navbar-btn">
				<a href="/DIVA/standings.jsp" class="btn btn-info"><span class="glyphicon glyphicon-stats"></span>&nbsp;&nbsp;Standings</a>
			</p>
		</li>
      </ul>
      <%if(shouldShowLogin){ %>
      	  <form class="navbar-form navbar-right" method=POST autocomplete="off" action="/DIVA/overview.jsp">
			<input type="hidden" name="<%=Dictionary.CLEAR_SESSION_PARAM %>" value="true">
			<button type="submit" class="btn btn-danger" value="Clear Session">Clear Session</button>
		  </form>
	      <form class="navbar-form navbar-right" method="POST" autocomplete="off">
	        <div class="form-group">
	          <div class="input-group">
	        		<input type="text" class="form-control" name="<%=LoginRegister.USERNAME_PARAM %>" placeholder="Username">
	          </div>
	        </div>
	        <div class="form-group">
	        	<div class="input-group">
	        		<input type="password" class="form-control" name="<%=LoginRegister.PASSWORD_PARAM %>" placeholder="Password">
	        	</div>
	        </div>
			<button type="submit" class="btn btn-success" name="<%=LoginRegister.LOGIN_PARAM %>" value="Login">Login</button>
	        <button type="submit" class="btn btn-primary" data-toggle="tooltip" data-placement="bottom" title="Provide an Username and Password to Register" name="<%=LoginRegister.REGISTER_PARAM %>" value="Register">Register</button>      
	       </form>
	       <%if(loginRegisterMessage != null && loginRegisterMessage.length() > 0){ %>
					<script type="text/javascript">
						$(document).ready(function(){
							$('#warning').modal('show');  
						});
					</script>
			<%} %>
	   <%}%>
	   <%if(!shouldShowLogin){ %>
       	<form class="navbar-form navbar-right" method=POST autocomplete="off" action="/DIVA/overview.jsp">
			<input type="hidden" name="<%=Dictionary.CLEAR_SESSION_PARAM %>" value="true">
			<button type="submit" class="btn btn-danger" value="Log Out">Log Out</button>
		</form>
       <%}%>
    </div><!-- /.navbar-collapse -->
  </div><!-- /.container-fluid -->
</nav>

<!-- Modal -->
<div id="warning" class="modal fade" role="dialog">
	<div class="modal-dialog">

		<!-- Modal content-->
		<div class="modal-content">
			<div class="modal-body">
				<div class="alert alert-danger">
					<%=loginRegisterMessage%>
				</div>
			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
			</div>
		</div>

	</div>
</div>

<%--<div style="float:right">
	<%if(shouldShowLogin){ %>
		<div>
			<form method=POST autocomplete="off">
				<span>User: </span><input type="text" name="<%=LoginRegister.USERNAME_PARAM %>">
				<br/>
				<span>Pass: </span><input type="password" name="<%=LoginRegister.PASSWORD_PARAM %>">
				<br/>
				<input type="submit" name="<%=LoginRegister.LOGIN_PARAM %>" value="Login">
				<input type="submit" name="<%=LoginRegister.REGISTER_PARAM %>" value="Register">
			</form>
			<%if(loginRegisterMessage != null && loginRegisterMessage.length() > 0){ %>
				<span style="color:red"><%=loginRegisterMessage %></span>
			<%} %>
		</div>
	<%}%>
</div>  --%>

<%--<h3>Welcome <%=sessionStore.getUser().getUserName() %><br/>Points: <%=sessionStore.getUser().getPoints() %></h3>
<h3>Session Token: <%
	out.print(sessionStore.getToken());
%>
</h3> --%> 


<%-- <form method=POST autocomplete="off" action="/DIVA/overview.jsp">
	<input type="hidden" name="<%=Dictionary.CLEAR_SESSION_PARAM %>" value="true">
	<input type="submit" value="<%=!shouldShowLogin ? "Log Out" : "Clear Session"%>">
</form> --%>