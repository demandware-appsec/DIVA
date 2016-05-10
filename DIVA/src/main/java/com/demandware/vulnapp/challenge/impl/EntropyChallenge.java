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
import com.demandware.vulnapp.util.SecureRandomUtil;
import com.demandware.vulnapp.util.exception.SetupRuntimeException;


/**
 * Low entropy makes this challenge easier to guess 
 * 
 * @author Chris Smith
 *
 */
public class EntropyChallenge extends AbstractChallenge{
	
	public static final String GET_TOKEN_PARAM = "get_token";
	public static final String TOKEN_GUESS_PARAM = "guess_token";
	
	private static final int MAX_UPPER = 2000;
	private static final String[] choiceCharacters = new String[]{"B", "C", "D", "E", "F", "G"};
	private static final int numChoice = 4;
	
	private final String defaultBase;
	protected EntropyChallenge(String name) {
		super(name);
		defaultBase = SecureRandomUtil.generateRandomHexString(numChoice/2);
		if(defaultBase.length() != numChoice){
			throw new SetupRuntimeException("Generated incorrect defaultBase size");
		}
	}

	@Override
	public String handleChallengeRequest(DIVAServletRequestWrapper req) {
		String responseText = "";
		String tokens = req.getParameter(GET_TOKEN_PARAM);
		String guess = req.getParameter(TOKEN_GUESS_PARAM);
		
		if(guess != null){
			String answer = generateToken();
			if(answer.equals(guess)){
				responseText = "Flag: " + (String)req.getInformation(Dictionary.FLAG_VALUE);
			}else{
				responseText = "Incorrect. Was looking for "+ answer;
			}
			
		} else if(tokens != null){
			try{
				int tokNum = Integer.parseInt(tokens);
				if(tokNum > MAX_UPPER){
					responseText = "OK that's enough tokens for you";
					tokNum = MAX_UPPER;
				}
				responseText += generateTokens(tokNum);
				
			} catch(NumberFormatException e){
				responseText = "Could not understand Get Tokens request";
			}
		}
		
		return responseText;
	}

	private String generateTokens(int tokNum) {
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i< tokNum; i++){
			sb.append(generateToken()).append("\n");
		}
		
		return sb.toString();
	}

	private String generateToken() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < numChoice; i++){
			sb.append(defaultBase.charAt(i));
			int choice = SecureRandomUtil.nextInt(choiceCharacters.length);
			sb.append(choiceCharacters[choice]);
		}
		return sb.toString();
	}

}