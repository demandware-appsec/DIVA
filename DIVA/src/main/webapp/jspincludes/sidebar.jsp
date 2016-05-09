<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="com.demandware.vulnapp.sessionmgmt.SessionStorage"%>
<%@ page import="com.demandware.vulnapp.challenge.ChallengeInfo"%>
<%@ page import="com.demandware.vulnapp.challenge.ChallengePlan"%>
<%@ page import="com.demandware.vulnapp.user.*"%>
<%@ page
	import="com.demandware.vulnapp.challenge.ChallengePlan.UpdateStatus"%>
<%@ page import="com.demandware.vulnapp.challenge.Difficulty"%>
<%@ page import="com.demandware.vulnapp.challenge.impl.ChallengeFactory"%>
<%@ page
	import="com.demandware.vulnapp.challenge.impl.ChallengeFactory.ChallengeType"%>
<%@ page
	import="com.demandware.vulnapp.servlet.DIVAServletRequestWrapper"%>
<%@ page import="com.demandware.vulnapp.servlet.Dictionary"%>
<%@ page import="java.util.List"%>
<%
	DIVAServletRequestWrapper req = (DIVAServletRequestWrapper)request;
	SessionStorage sessionStore = (SessionStorage)req.getInformation(Dictionary.SESSION_STORE_OBJ);
	UpdateStatus update = (UpdateStatus)req.getInformation(Dictionary.UPDATE_STATUS);
	boolean completeWin = sessionStore.getUser().areAllChallengesComplete();
%>

<%if(update.updated && update.badFlag){ %>
	<script type="text/javascript">
		$(document).ready(function() {
			$('#badflag').modal('show');
		});
	</script>
	<div id="badflag" class="modal fade" tabindex="-1" role="dialog" aria-hidden="true">
  <div class="modal-dialog">
  <div class="modal-content">
      <div class="modal-header">
          <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
			Warning
      </div>
      <div class="modal-body"><br/>
      <p align="center">Incorrect Flag</p>
      
      </div>
      <div class="modal-footer">
          <div>
          <button class="btn btn-default btn-medium" data-dismiss="modal" aria-hidden="true">Close</button>
            		  </div>	
      </div>
  </div>
  </div>
</div>
<%} else if(update.updated && update.unlockedDiff != null && !update.unlockedDiff.isEmpty()){%>
	<script type="text/javascript">
		$(document).ready(function() {
			$('#unlocked').modal('show');
		});
	</script>
	
	<div id="unlocked" class="modal fade" tabindex="-1" role="dialog" aria-hidden="true">
  <div class="modal-dialog">
  <div class="modal-content">
      <div class="modal-header">
          <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
			Congratulations
      </div>
      <div class="modal-body"><br/>
      <p align="center">Completed Challenge: <b><%=update.completedChallenge %></b></p>
      <p align="center">Unlocked New Difficulty: <b><%=makeUnlockedChallengesLine(update.unlockedDiff) %></b></p>
      
      </div>
      <div class="modal-footer">
          <div>
          <button class="btn btn-default btn-medium" data-dismiss="modal" aria-hidden="true">Close</button>
            		  </div>	
      </div>
  </div>
  </div>
</div>
<%} else if(update.updated && !update.badFlag && update.completedChallenge != null){%>

<script type="text/javascript">
		$(document).ready(function() {
			$('#completed').modal('show');
		});
	</script>
	
	<div id="completed" class="modal fade" tabindex="-1" role="dialog" aria-hidden="true">
  <div class="modal-dialog">
  <div class="modal-content">
      <div class="modal-header">
          <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
			Congratulations
      </div>
      <div class="modal-body"><br/>
      <p align="center">Completed Challenge: <b><%=update.completedChallenge %></b></p>
      
      </div>
      <div class="modal-footer">
          <div>
          <button class="btn btn-default btn-medium" data-dismiss="modal" aria-hidden="true">Close</button>
            		  </div>	
      </div>
  </div>
  </div>
</div>

<%} %>

<%!
private String makeUnlockedChallengesLine(List<Difficulty> diffs){
	String line;
	StringBuilder sb = new StringBuilder();
	for(int i = 0; i < diffs.size(); i++){
		sb.append(diffs.get(i).getFormattedName());
		if(i < diffs.size()-1){
			sb.append(", ");
		}
	}
	line = sb.toString();
	return line;
}
%>

<%!
private String makeChallengeLine(ChallengeInfo cInfo, SessionStorage sessionStore, int threshold){
	StringBuilder sb = new StringBuilder();
	sb.append("<div class=\"sidebar-panel-body\" align=\"center\">");
			
		if(sessionStore.getUser().hasAnyAccess(cInfo.getChallengeType())){
			if(cInfo.getChallengeType().equals(ChallengeType.HIDDEN) && !sessionStore.getUser().isComplete(cInfo.getChallengeType())){
				sb.append("<span>You'll never find me</span>");
			} else if(cInfo.getChallengeType().equals(ChallengeType.MD5) && !sessionStore.getUser().isComplete(cInfo.getChallengeType())){
				sb.append("<span>Pattern? What pattern?</span>");
			} else{
				sb.append("<a href=\"challenges/");
				sb.append(cInfo.getLinkValue()); 
				sb.append(".jsp\"/>"); 
				
				if(ChallengePlan.getInstance().isChallengeComplete(cInfo.getName(), sessionStore)){	
					sb.append(ChallengeFactory.getInstance().getChallenge(cInfo.getChallengeType()).getName()); 
				}else{
					sb.append( cInfo.getLinkValue() ); 
				}
				
				sb.append("</a>");
			}
		} 
		
	sb.append("</div>");
	return sb.toString();
}
%>


<!-- sidebar -->
<div class="column col-sm-2 col-xs-1 sidebar-offcanvas" id="sidebar">

	<ul class="nav">
		<li><a href="#" data-toggle="offcanvas"
			class="visible-xs text-center"><i
				class="glyphicon glyphicon-chevron-right"></i></a></li>
	</ul>

	<ul class="nav hidden-xs" id="lg-menu">
			<li>
			<div class="sidebar-panel">
  				<div class="sidebar-panel-user-header" align="center"><span class="glyphicon glyphicon-user"></span>&nbsp;<strong>User</strong></div>
  				<div class="sidebar-panel-user-body" align="center">
    				<%=sessionStore.getUser().getUserName()%>
  				</div>
			</div>
		</li>
		<br/>
		<li>
			<div class="sidebar-panel">
  				<div class="sidebar-panel-session-header" align="center"><strong>Session Token</strong></div>
  				<div class="sidebar-panel-session-body" align="center">
    				<% out.print(sessionStore.getToken());%>
  				</div>
			</div>
		</li>	
		<% if(!completeWin){%>
					<br/>
			<li>
				<div class="sidebar-panel">
	  				<div class="sidebar-panel-flag-header" align="center"><span class="glyphicon glyphicon-flag"></span>&nbsp;<strong>Check Flag</strong></div>
	  				<div class="sidebar-panel-flag-body" align="center">
	    				<form class="form-horizontal" method=POST autocomplete="off">
						  <div class="form-group">
						  	<div class=" col-sm-12">
						      <input type="text" class="form-control" name="<%=Dictionary.FLAG_ID_PARAM %>" placeholder="Enter Flag">
						    </div>
						  </div>
						  <div class="form-group">
						      <button type="submit" class="btn btn-default">Check</button>
						  </div>
						</form>
	  				</div>
				</div>
			</li>
		<%}%>
		<%
		if(completeWin){
		%>
			<br/>
			<div class="sidebar-panel">
  				<div class="sidebar-panel-header won-header" align="center"><strong>You Won!</strong></div>
  				<div class="sidebar-panel-body won-body" align="center">
    				Congratulations! You've beaten the game. 
  				</div>
			</div>
		<%
		}
		%>
		<br/>
		<%
			for (Difficulty diff : Difficulty.values()) {
				if(diff.equals(Difficulty.IMPOSSIBLE)){
					if(!UserManager.isChallengerUser(sessionStore.getUser())){
						continue; //short circuit is ugly
					}
				}
				int threshold = ChallengePlan.getInstance()
						.getDifficultyThreshold(diff);

				List<ChallengeInfo> plan = ChallengePlan.getInstance()
						.getChallengeForDifficulty(diff);
				String value = "";
				if (plan != null) {
		%>

		<div class="sidebar-panel">
		<div class="sidebar-panel-channel-header" align="center"><strong>Challenge</strong></div>
		<div class="sidebar-panel-header-challenge" align="center"><strong>Difficulty: <%=diff.getFormattedName()%> (<%=diff.getPoints()%> <%=diff.getPoints() > 1 ? "Points" : "Point"%>
			Each)</strong></div>
			<!-- <div class="sidebar-panel-header sidebar-challenge" align="center">Challenge</div> -->
			<%
				if (sessionStore.getUser().getCompletedChallenges().size() >= threshold) {
					for (ChallengeInfo cInfo : plan) {
						String line = makeChallengeLine(cInfo,
								sessionStore, threshold);
						out.println(line);
					}
				}
			%>
			<%
			if (sessionStore.getUser().getCompletedChallenges().size() < threshold) {
		%>
			<div class="sidebar-panel-footer" align="center"><strong>Complete <%=threshold- sessionStore.getUser().getCompletedChallenges().size()%> more <%=threshold > 1 ? "Challenges" : "Challenge"%> to Unlock</strong></div>
		<%
			}
		%>
		</div>
		<br/>
		<%	
	}
} 
%>	</ul>


</div>

<!-- /sidebar -->


