<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="com.demandware.vulnapp.servlet.*" %>
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
										<h3>Overview</h3>
									</div>

									<div class="panel panel-default">
										<div class="panel-heading">
											<h4>Instructions</h4>
										</div>
										<div class="panel-body">
											<p>DIVA stands for the Demandware Intentionally Vulnerable App.</p>
											<br/>
											<p>
												It is designed to test a number of Application Security vulnerabilities and provide a protected area to safely and legally hack a website. 
												<br/>
												To use the site, select any challenge on the left and get going. If you'd like to have the site save your progress, choose a username and password above and click Register.
												<b>NOTE: See hints section below for a warning about passwords.</b>
												When you return, you'll be able to use those same credentials to log back in and resume your challenges.
											</p>
											<p>
												While there are technically no rules, there are several guidelines and requests:
												<br/>
												1. Attack only this site.
												<br/>
												2. Do not use network-level attack (DDOS or similar). They will not help you.
												<br/>
												3. Should you gain control of the machine this site is hosted on, congratulations, but please do not permanently shutdown the server.
											</p>
											<p>
												Finally, there are several general hints for you to know.
												<br/>
												1. Write down all attacks. There is no guarantee that this site will lose your User information either by automated cleanup or by a fellow competitor's attack.
												<br/>
												2. Expect outages. The website make come under heavy attack and may topple over. This is normal and the site will be back up quickly.
												<br/>
												3. Do not use a password you use anywhere else. There is no guarantee that passwords can't be stolen.
												<br/>
												4. Work in teams. You will be stopped by challenges and have no way forward. use your team to discuss possible attacks and you'll have greater success.
												<br/>
												5. These challenges will get immensely difficult. A good rule of thumb is to spend 5 minutes or 5 attempts to attack a challenge. You may receive answers to early challenges by completing later challenges.
											</p>
										</div>
									</div>
								
							</div>
							<span>
								<%="Last Started: " + DivaApp.getInstance().getLastRestart() %>
							</span>
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