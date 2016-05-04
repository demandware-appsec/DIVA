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
