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