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
