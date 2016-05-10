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

/**
 * A labeling system to mark the points and layout of all challenges
 * 
 * @author Chris Smith
 *
 */
public enum Difficulty {
	STARTER(1, "Starter"),
	EASY(2, "Easy"),
	MEDIUM(3, "Medium"),
	HARD(4, "Hard"),
	VHARD(5, "Very Hard"),
	IMPOSSIBLE(10, "Impossible")
	;
	
	private final int points;
	private final String fName;
	
	Difficulty(int points, String fName){
		this.points = points;
		this.fName = fName;
	}
	
	public int getPoints(){
		return this.points;
	}
	
	public String getFormattedName(){
		return this.fName;
	}
}
