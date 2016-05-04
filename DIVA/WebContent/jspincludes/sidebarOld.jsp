<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="com.demandware.vulnapp.sessionmgmt.SessionStorage" %>
<%@ page import="com.demandware.vulnapp.challenge.ChallengeInfo" %>
<%@ page import="com.demandware.vulnapp.challenge.ChallengePlan" %>
<%@ page import="com.demandware.vulnapp.challenge.ChallengePlan.UpdateStatus" %>
<%@ page import="com.demandware.vulnapp.challenge.Difficulty" %>
<%@ page import="com.demandware.vulnapp.challenge.impl.ChallengeFactory" %>
<%@ page import="com.demandware.vulnapp.challenge.impl.ChallengeFactory.ChallengeType" %>
<%@ page import="com.demandware.vulnapp.servlet.DIVAServletRequestWrapper" %>
<%@ page import="com.demandware.vulnapp.servlet.Dictionary"%>
<%@ page import="java.util.List" %> 
<%
	DIVAServletRequestWrapper req = (DIVAServletRequestWrapper)request;
	SessionStorage sessionStore = (SessionStorage)req.getInformation(Dictionary.SESSION_STORE_OBJ);
	UpdateStatus update = (UpdateStatus)req.getInformation(Dictionary.UPDATE_STATUS);
	boolean completeWin = sessionStore.getUser().areAllChallengesComplete();
%>


<br/>
<% if(!completeWin){%>
	<h4>Complete Each Challenge By Finding <br/>A Flag And Entering It Here</h4>
	<form method=POST autocomplete="off">
		Flag ID:
		<input type="text" name="<%=Dictionary.FLAG_ID_PARAM %>" size="42">
		<br/>
		<input type="submit" value="Submit">
	</form>
	<br/>
<%}%>

<%if(update.updated && update.badFlag){ %>
	<span id="badflag">Incorrect Flag</span><br/>
<%} else if(update.updated && update.unlockedDiff != null && !update.unlockedDiff.isEmpty()){%>
	<span id="unlocked">Unlocked New Difficulty: <%=makeUnlockedChallengesLine(update.unlockedDiff) %></span><br/>
<%} %>


<%
if(completeWin){
%>
	<h2>You Have Won! Congratulations!</h2>
	<span>For recognition, please submit a writeup of how you beat these challenges to AppSec@demandware.com</span>
	<br/><br/>
<%
}
%>
<div>
	<div class="panel panel-primary">
		<div class="panel-heading">
			<h3 class="panel-title">Information</h3>
		</div>
		<ul class="list-group">
			<li class="list-group-item"><b>User</b>: <%=sessionStore.getUser().getUserName()%></li>
			<li class="list-group-item"><b>Session Token</b>: <% out.print(sessionStore.getToken()); %></li>
		</ul>
	</div>
</div>

<div>
	<div class="panel panel-primary">
		<div class="panel-heading">
			<h3 class="panel-title">Points</h3>
		</div>
		<ul class="list-group">
			<li class="list-group-item"><h3><%= sessionStore.getUser().getPoints() %></h3></li>
		</ul>
		
	</div>
</div>
<%
	for (Difficulty diff : Difficulty.values()) {
		int threshold = ChallengePlan.getInstance()
				.getDifficultyThreshold(diff);

		List<ChallengeInfo> plan = ChallengePlan.getInstance()
				.getChallengeForDifficulty(diff);
		String value = "";
		if (plan != null) {
%>

	<span>Difficulty: <%=diff.getFormattedName() %></span><br />
	<span><%=diff.getPoints() %> <%=diff.getPoints()>1 ? "Points": "Point"%>
		Each </span>
	<table border="1"
		style="width: 100%; text-align: center; border-collapse: collapse;">
		<tr>
			<th>Challenge</th>
		</tr>
		<%
			if(sessionStore.getUser().getPoints() >= threshold){
				for(ChallengeInfo cInfo : plan){
					String line = makeChallengeLine(cInfo, sessionStore, threshold);
					out.println(line);
				}
			}
		%>
	</table>
	<%
		if(sessionStore.getUser().getPoints() < threshold){
		%>
	<span>Unlocked with <%=threshold %> <%=threshold>1 ? "Points": "Point"%></span>
	<br />
	<%
		}
		%>
	<br />
	<%	
	}
} 
%>
</div>
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
	sb.append("<tr>");
		sb.append("<td>");
			
		if(sessionStore.getUser().hasAnyAccess(cInfo.getChallengeType())){
			if(cInfo.getChallengeType().equals(ChallengeType.HIDDEN) && !sessionStore.getUser().isComplete(cInfo.getChallengeType())){
				sb.append("<span>You'll never find me</span>");
			} else if(cInfo.getChallengeType().equals(ChallengeType.MD5) && !sessionStore.getUser().isComplete(cInfo.getChallengeType())){
				sb.append("<span>Pattern? What pattern?</span>");
			} else{
				sb.append("<a href=\"/DIVA/challenges/");
				sb.append(cInfo.getLinkValue()); 
				sb.append(".jsp\"/>"); 
				
				if(ChallengePlan.getInstance().isChallengeComplete(cInfo.getName(), sessionStore)){	
					sb.append(ChallengeFactory.getInstance().getChallenge(cInfo.getChallengeType()).getName()); 
				}else{
					sb.append(cInfo.getLinkValue()); 
				}
				
				sb.append("</a>");
			}
		} else{
			sb.append("<span>Complete Previous Challenge To Unlock</span>");
		}
		
		sb.append("</td>");
	sb.append("</tr>");
	return sb.toString();
}
%>

