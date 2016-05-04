package com.demandware.vulnapp.challenge.impl;

import com.demandware.vulnapp.challenge.AbstractChallenge;
import com.demandware.vulnapp.servlet.DIVAServletRequestWrapper;
import com.demandware.vulnapp.servlet.Dictionary;


/**
 * Client-Side validation only server code assumes client validation worked
 * 
 * @author Chris Smith
 *
 */
public class ValidationChallenge extends AbstractChallenge {

	public static final String formParam = "state";
	public static final String formOption1 = "Motto";
	public static final String formOption2 = "Sport";
	public static final String flagOption = "Flag";
	
	protected ValidationChallenge(String name) {
		super(name);
	}
	
	public String handleChallengeRequest(DIVAServletRequestWrapper req){
		String text = "";
		String param = req.getParameter(formParam);
		if(param != null){
			switch(param){
				case formOption1:
					text = "Ense petit placidam sub libertate quietem : \"she seeks with the sword a quiet peace under liberty\"";
					break;
				case formOption2:
					text = "Basketball"; 
					break;
				case flagOption:
					text = "Flag: " + (String) req.getInformation(Dictionary.FLAG_VALUE);
					break;
				default:
					text = "Unknown option";
					break;
			}
		}
		return text;
	}
}
