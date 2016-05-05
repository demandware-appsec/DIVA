package com.demandware.vulnapp.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Queue;

import com.demandware.vulnapp.challenge.ChallengeInfo;
import com.demandware.vulnapp.challenge.ChallengePlan;
import com.demandware.vulnapp.challenge.impl.ChallengeFactory.ChallengeType;
import com.demandware.vulnapp.servlet.DIVAServletRequestWrapper;

/**
 * handles leaderboard requests to show standings
 * 
 * @author Chris Smith
 *
 */
public class Standings {
	private static final int DEFAULT_TOP = 10;
	public static final String TOP_PARAM = "top_standings";
	
	public static int getMaxPoints(){
		int pts = 0;
		for(ChallengeType t : ChallengeType.values()){
			ChallengeInfo cInfo = ChallengePlan.getInstance().getChallengeForType(t);
			pts += cInfo.getDifficulty().getPoints();
		}
		return pts;
	}
	
	public static int getTotalChallenges(){
		return ChallengeType.values().length;
	}
	
	public static ArrayList<User> handleRequest(DIVAServletRequestWrapper req){
		String standings = req.getParameter(TOP_PARAM);
		ArrayList<User> users = null;
		int top = DEFAULT_TOP;
		try{
			top = Integer.parseInt(standings);
		} catch(Exception e){}
		
		users = getUsersStandings(top);
		
		return users;
	}
	
	private static ArrayList<User> getUsersStandings(int topN){
		ArrayList<User> top = new ArrayList<User>(topN+1);
		Queue<User> users = UserManager.getInstance().getUsers();
		ArrayList<User> sort = new ArrayList<User>(users.size()+1);
		for(User u : users){
			sort.add(u);
		}
		Collections.sort(sort, new StandingsComparator());
		int sortLen = sort.size();
		for(int i = 0; i < topN; i++){
			if(i >= sortLen){
				break;
			}
			top.add(sort.get(i));
		}
		return top;
	}
}
