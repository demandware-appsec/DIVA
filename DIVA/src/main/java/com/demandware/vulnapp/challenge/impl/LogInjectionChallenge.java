package com.demandware.vulnapp.challenge.impl;

import com.demandware.vulnapp.challenge.AbstractChallenge;
import com.demandware.vulnapp.servlet.DIVAServletRequestWrapper;
import com.demandware.vulnapp.servlet.Dictionary;


/**
 * Inject data into log to trick our log checker.
 * @author Chris Smith
 *
 */
public class LogInjectionChallenge extends AbstractChallenge{
	
	public static final String MESSAGE_PARAM = "password";
	
	protected LogInjectionChallenge(String name) {
		super(name);
	}

	@Override
	public String handleChallengeRequest(DIVAServletRequestWrapper req) {
		String responseText = "";
		String msg = req.getParameter(MESSAGE_PARAM);
		if(msg != null){
			responseText = getLogMessage((String)req.getInformation(Dictionary.FLAG_VALUE), msg);
		}
		return responseText;
	}

	private String getLogMessage(String flag, String msg) {
		String response = "";
		StringBuilder theLog = new StringBuilder();
		theLog.append("ACCESS DENIED for password " + msg);
		String log = theLog.toString();
		String[] lines = log.split("\r|\n");
		String test = lines[lines.length - 1];
		if(test.contains("ACCESS GRANTED") && !test.contains("ACCESS DENIED")){
			response = "Flag: " + flag;
		} else{
			response = "Log reports:\n" + log + "\n\n\nExpected ACCESS GRANTED";
		}
		return response;
	}

}