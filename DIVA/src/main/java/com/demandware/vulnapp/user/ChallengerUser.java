package com.demandware.vulnapp.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.demandware.vulnapp.challenge.ChallengeInfo;
import com.demandware.vulnapp.challenge.ChallengePlan;
import com.demandware.vulnapp.challenge.Difficulty;
import com.demandware.vulnapp.challenge.impl.ChallengeFactory.ChallengeType;
import com.demandware.vulnapp.util.SecureRandomUtil;
import com.demandware.vulnapp.util.exception.AccountException;

/**
 * Creates a few users to rate real users against. Red herrings abound
 * @author Chris Smith
 *
 */
public enum ChallengerUser {
			//username	password			Max Difficulty		Further Challenges to disable
	APPSEC	("AppSec",						Difficulty.VHARD							),
	
	LEET	("l337",						Difficulty.HARD								),
	
	SKIDDIE	("Skiddie",	"immaHaxorULulz",	Difficulty.MEDIUM							),
										
	NOOB	("n00b",	"baseball",			Difficulty.EASY								),
										
	ANON	("Anon", 	"WeAreLegion",		Difficulty.STARTER							),
	;
	
	private final String username;
	private final String password;
	private final List<ChallengeType> challengesAllowed;

	private ChallengerUser(String username, Difficulty maxDiff, ChallengeType... additionalRemoved ){
		this(username, SecureRandomUtil.generateRandomHexString(3), maxDiff, additionalRemoved);
	}
	
	private ChallengerUser(String username, String password, Difficulty maxDiff, ChallengeType... additionalRemoved ){
		this.username = username;
		this.password = password;

		List<ChallengeType> allowed = new ArrayList<ChallengeType>();
		Difficulty[] orderedDifficulty = new Difficulty[]{Difficulty.STARTER, Difficulty.EASY, 
														Difficulty.MEDIUM, Difficulty.HARD, Difficulty.VHARD};
		
		for(Difficulty diff : orderedDifficulty){
			List<ChallengeInfo> cInfos = ChallengePlan.getInstance().getChallengeForDifficulty(diff);
			for(ChallengeInfo cInfo : cInfos){
				allowed.add(cInfo.getChallengeType());
			}
			
			if(maxDiff.equals(diff)){
				break;		
			}
		}
		
		this.challengesAllowed = removeChallenges(allowed, additionalRemoved);
	}
	
	public String getUserName(){
		return this.username;
	}
	
	private List<ChallengeType> removeChallenges(List<ChallengeType> allowed, ChallengeType... remove) {
		if(remove != null){
			for(ChallengeType type : remove){
				allowed.remove(type);
			}
		}
		return Collections.unmodifiableList(allowed);
	}
	
	void makeChallengerUser(){
		User chall = UserManager.getInstance().searchForUser(this.username);
		if(chall == null){
			try {
				chall = UserManager.getInstance().makeNewUser(this.username, this.password);
			} catch (AccountException e) {
				e.printStackTrace();
			}
			
			for(ChallengeType type : this.challengesAllowed){
				ChallengePlan.getInstance().markCompleteAndUpdateChallenges(chall, ChallengePlan.getInstance().getChallengeForType(type));
			}
		}
	}
	
}
