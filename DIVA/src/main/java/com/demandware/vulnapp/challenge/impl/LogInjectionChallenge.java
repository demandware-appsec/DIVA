/*
 * Copyright 2016 Demandware Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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