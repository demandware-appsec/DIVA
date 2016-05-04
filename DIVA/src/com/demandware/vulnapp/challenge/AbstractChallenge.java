package com.demandware.vulnapp.challenge;

import com.demandware.vulnapp.servlet.DIVAServletRequestWrapper;


/**
 * Base Impl for all Challenges
 * 
 * @author Chris Smith
 *
 */
public abstract class AbstractChallenge {
	
	private String name;
	
	
	protected AbstractChallenge(String name){
		this.name = name;
	}
	
	public String getName(){
		return this.name;
	}
	
	public abstract Object handleChallengeRequest(DIVAServletRequestWrapper req);
	
}
