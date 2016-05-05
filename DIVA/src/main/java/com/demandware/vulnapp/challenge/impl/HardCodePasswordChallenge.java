package com.demandware.vulnapp.challenge.impl;

import com.demandware.vulnapp.challenge.AbstractChallenge;
import com.demandware.vulnapp.servlet.DIVAServletRequestWrapper;
import com.demandware.vulnapp.servlet.Dictionary;


/**
 * A simple password, except it's very long
 * 
 * @author Chris Smith
 *
 */
public class HardCodePasswordChallenge extends AbstractChallenge{
	
	private static final String password = "TheresNoWayAnyoneWillGuessThisPasswordEverLookIts66CharactersLong";
	
	public static final String PASSWORD_PARAM = "password";
	
	protected HardCodePasswordChallenge(String name) {
		super(name);
	}

	@Override
	public String handleChallengeRequest(DIVAServletRequestWrapper req) {
		String responseText = "";
		String guessPass = req.getParameter(PASSWORD_PARAM);
		if(guessPass != null){
			if(password.equals(guessPass)){
				responseText = "Flag: " + (String)req.getInformation(Dictionary.FLAG_VALUE);
			} else{
				responseText = "Incorrect Password";
			}
		}
		return responseText;
	}

}