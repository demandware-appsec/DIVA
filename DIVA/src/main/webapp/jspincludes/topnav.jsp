<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="com.demandware.vulnapp.sessionmgmt.SessionStorage"%>
<%@ page import="com.demandware.vulnapp.user.LoginRegister"%>
<%@ page import="com.demandware.vulnapp.user.UserManager"%>
<%@ page
	import="com.demandware.vulnapp.servlet.DIVAServletRequestWrapper"%>
<%@ page import="com.demandware.vulnapp.servlet.Dictionary"%>
<%@ page import="com.demandware.vulnapp.util.exception.AccountException"%>
<%
	DIVAServletRequestWrapper req = (DIVAServletRequestWrapper)request;
	String loginRegisterMessage = (String)req.getInformation(Dictionary.LOGIN_PROBLEM);
	SessionStorage sessionStore = (SessionStorage)req.getInformation(Dictionary.SESSION_STORE_OBJ);
	boolean shouldShowLogin = UserManager.getInstance().showLogin(sessionStore.getUser());
%>

<!-- top nav -->
<div class="navbar navbar-blue navbar-static-top">
	<div class="navbar-header">
		<button class="navbar-toggle" type="button" data-toggle="collapse"
			data-target=".navbar-collapse">
			<span class="sr-only">Toggle</span> <span class="icon-bar"></span> <span
				class="icon-bar"></span> <span class="icon-bar"></span>
		</button>
		<a href="/DIVA/overview.jsp" class="navbar-brand"><b>DIVA</b></a>
	</div>
	<nav class="collapse navbar-collapse" role="navigation">

		<ul class="nav navbar-nav">
			<li><a href="/DIVA/overview.jsp"><i
					class="glyphicon glyphicon-home"></i> Home</a></li>
			<li><a href="/DIVA/standings.jsp" role="button"><i
					class="glyphicon glyphicon-stats"></i> Standings</a></li>
			<li><a href="#">Points&nbsp;&nbsp;<span class="badge"><%=sessionStore.getUser().getPoints() %></span></a></li>
		</ul>
		<%if(shouldShowLogin){ %>
		<form class="navbar-form navbar-right" method=POST autocomplete="off"
			action="/DIVA/overview.jsp">
			<input type="hidden" name="<%=Dictionary.CLEAR_SESSION_PARAM %>"
				value="true">
			<button type="submit" class="btn round btn-danger"
				value="Clear Session">
				Clear Session <span class="glyphicon glyphicon-refresh"></span>
			</button>
		</form>
		<form class="navbar-form navbar-right" method="POST"
			autocomplete="off">
			<div class="form-group">
				<div class="input-group">
					<input type="text" class="form-control round"
						name="<%=LoginRegister.USERNAME_PARAM %>" placeholder="Username">
				</div>
			</div>
			<div class="form-group">
				<div class="input-group">
					<input type="password" class="form-control"
						name="<%=LoginRegister.PASSWORD_PARAM %>" placeholder="Password">
				</div>
			</div>
			<button type="submit" class="btn round btn-primary"
				name="<%=LoginRegister.LOGIN_PARAM %>" value="Login">
				Login <span class="glyphicon glyphicon-log-in"></span>
			</button>
			<button type="submit" class="btn round btn-primary"
				data-toggle="tooltip" data-placement="bottom"
				title="Provide an Username and Password to Register"
				name="<%=LoginRegister.REGISTER_PARAM %>" value="Register">Register</button>
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
		<form class="navbar-form navbar-right" method=POST autocomplete="off"
			action="/DIVA/overview.jsp">
			<input type="hidden" name="<%=Dictionary.CLEAR_SESSION_PARAM %>"
				value="true">
			<button type="submit" class="btn round btn-danger" value="Log Out">
				Log Out <span class="glyphicon glyphicon-log-out"></span>
			</button>
		</form>
		<%}%>

	</nav>
</div>
<!-- /top nav -->

<!--post modal-->
<div id="warning" class="modal fade" tabindex="-1" role="dialog" aria-hidden="true">
  <div class="modal-dialog">
  <div class="modal-content">
      <div class="modal-header">
          <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
			Warning
      </div>
      <div class="modal-body"><br/>
      <p align="center"><%=loginRegisterMessage%></p>
      
      </div>
      <div class="modal-footer">
          <div>
          <button class="btn btn-default btn-medium" data-dismiss="modal" aria-hidden="true">Close</button>
            		  </div>	
      </div>
  </div>
  </div>
</div>