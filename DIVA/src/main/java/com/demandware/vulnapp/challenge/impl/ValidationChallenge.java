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
