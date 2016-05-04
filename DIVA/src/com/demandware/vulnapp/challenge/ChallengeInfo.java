package com.demandware.vulnapp.challenge;

import com.demandware.vulnapp.challenge.impl.ChallengeFactory.ChallengeType;


/**
 * Contains internal knowledge of challenges, such as their link values, Types, and initial viewability.
 * These are all set once.
 * 
 * @author Chris Smith
 *
 */
public class ChallengeInfo {
	private final String linkValue;
	private final String name;
	private final ChallengeType type;
	private final Difficulty difficulty;
	
	ChallengeInfo(String name, String linkValue, ChallengeType type, Difficulty diff){
		this.name = name;
		this.linkValue = linkValue;
		this.type = type;
		this.difficulty = diff;
	}
	
	public String getName(){
		return this.name;
	}

	public String getLinkValue() {
		return this.linkValue;
	}

	public ChallengeType getChallengeType() {
		return this.type;
	}
	
	public Difficulty getDifficulty(){
		return this.difficulty;
	}
}
