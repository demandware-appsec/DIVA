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
package com.demandware.vulnapp.user;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.demandware.vulnapp.challenge.ChallengeInfo;
import com.demandware.vulnapp.challenge.ChallengePlan;
import com.demandware.vulnapp.challenge.impl.ChallengeFactory.ChallengeType;
import com.demandware.vulnapp.util.Helpers;

/**
 * Stores a User's state, including challenges, and activity
 * @author Chris Smith
 *
 */
public class User {
	private String userName;
	private String passwordHash;
	private final Map<ChallengeType, AccessLevel> storeMap;
	private int points;
	private String lastIP = null;
	private Long lastActivity = null;
	
	private final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss");    
	
	private enum AccessLevel{
		DENY,
		ALLOW,
		COMPLETE
	}
	
	User(String username, String passwordHash){
		this.userName = Helpers.cheapoHTMLFilter(username);
		this.passwordHash = passwordHash;
		this.storeMap = new ConcurrentHashMap<ChallengeType, AccessLevel>();
		setupMap();
		setAllowedChallenges(ChallengePlan.getInstance().getInitialAllowedChallenges());
		updatePoints();
	}
	
	/**
	 * Clears this user of all data and makes it equivalent to deleting and remaking from scratch
	 */
	public void resetUser(){
		setupMap();
		setAllowedChallenges(ChallengePlan.getInstance().getInitialAllowedChallenges());
		updatePoints();
	}
	
	private void setupMap(){
		for(ChallengeType t : ChallengeType.values()){
			this.storeMap.put(t, AccessLevel.DENY);
		}
	}

	public String getUserName() {
		return this.userName;
	}

	public String getPasswordHash() {
		return this.passwordHash;
	}
	
	public int getPoints(){
		return this.points;
	}
	
	public String getLastActivityLong(){
		return this.lastActivity == null ? "0" : Long.toString(this.lastActivity);
	}
	
	public String getLastActivity(){
		String lastAct = "Not Recorded";
		if(this.lastActivity != null){
			lastAct = sdf.format(new Date(this.lastActivity));
		}
		return lastAct;
	}
	
	public String getLastIP(){
		String lastIp = "Not Recorded";
		if(this.lastIP != null){
			lastIp = this.lastIP;
		}
		return lastIp;
	}
	
	void setCompletedChallenges(List<ChallengeType> complete) {
		for(ChallengeType t : complete){
			addChallengeAccess(t, AccessLevel.COMPLETE);
		}
		updatePoints();
	}
	
	void setAllowedChallenges(List<ChallengeType> initialAllowedChallenges) {
		for(ChallengeType t : initialAllowedChallenges){
			addChallengeAccess(t, AccessLevel.ALLOW);
		}
	}

	private void addChallengeAccess(ChallengeType type, AccessLevel level){
		this.storeMap.put(type, level);
	}
	
	public List<ChallengeType> getAllowedChallenges() {
		List<ChallengeType> challenges = new ArrayList<ChallengeType>();
		for(ChallengeType t : this.storeMap.keySet()){
			if(hasAccess(t, AccessLevel.ALLOW)){
				challenges.add(t);
			}
		}
		return challenges;
	}
	
	public List<ChallengeType> getCompletedChallenges() {
		List<ChallengeType> challenges = new ArrayList<ChallengeType>();
		for(ChallengeType t : this.storeMap.keySet()){
			if(hasAccess(t, AccessLevel.COMPLETE)){
				challenges.add(t);
			}
		}
		return challenges;
	}
	
	/**
	 * return a list containing all challenges that this session has access to 
	 */
	public List<ChallengeType> getAvailableChallenges(){
		List<ChallengeType> challenges = new ArrayList<ChallengeType>();
		for(ChallengeType t : this.storeMap.keySet()){
			if(hasAccess(t, AccessLevel.ALLOW) ||
					hasAccess(t, AccessLevel.COMPLETE)){
				challenges.add(t);
			}
		}
		return challenges;
	}
	
	public boolean hasAnyAccess(ChallengeType type){
		return hasAccess(type, AccessLevel.ALLOW) || hasAccess(type, AccessLevel.COMPLETE);
	}
	
	/**
	 * true if this session has been granted access to this ChallengeType, 
	 * false otherwise
	 */
	private boolean hasAccess(ChallengeType type, AccessLevel level){
		boolean access = false;
		AccessLevel lvl = this.storeMap.get(type);
		if(lvl != null){
			access = lvl.equals(level);
		}
		return access;
	}
	
	/**
	 * marks session object as having ability to see this challenge
	 * @return true if grant happened, false otherwise
	 */
	public boolean grantAccess(ChallengeType type){
		if(this.storeMap.get(type).equals(AccessLevel.DENY)){
			this.storeMap.put(type, AccessLevel.ALLOW);
			return true;
		}
		return false;
	}
	
	/**
	 * marks session object as having completed this challenge
	 */
	public void markComplete(ChallengeType type){
		if(this.storeMap.get(type).equals(AccessLevel.ALLOW)){
			this.storeMap.put(type, AccessLevel.COMPLETE);
		}
		updatePoints();
	}
	
	/**
	 * true if this session has successfully completed the given ChallengeType,
	 * false otherwise
	 */
	public boolean isComplete(ChallengeType type) {
		return this.storeMap.get(type).equals(AccessLevel.COMPLETE);
	}
	
	
	/**
	 * true only if all setup challenges have been completed by this session,
	 * false otherwise
	 */
	public boolean areAllChallengesComplete(){
		boolean complete = true;
		for(ChallengeInfo ci : ChallengePlan.getInstance().getChallenges()){
			if(!isComplete(ci.getChallengeType())){
				complete = false;
				break;
			}
		}
		return complete;
	}

	public void updatePoints(){
		int sum = 0;
		for(ChallengeInfo ci : ChallengePlan.getInstance().getChallenges()){
			if(isComplete(ci.getChallengeType())){
				sum += ci.getDifficulty().getPoints();
			}
		}
		this.points = sum;
	}
	
	public void updateIP(String ip){
		this.lastIP = ip;
	}
	
	public void updateLastActivity(long time){
		this.lastActivity = time;
	}
	

	@Override
	public boolean equals(Object that){
		boolean isEqual = false;
		if(that instanceof User){
			User thatUser = (User) that;
			isEqual = thatUser.userName.equals(this.userName) &&
					thatUser.passwordHash.equals(this.passwordHash);
		}
		return isEqual;
	}

	@Override
	public int hashCode(){
		return this.userName.hashCode();
	}
	
	@Override
	public String toString(){
		return this.userName;
	}
}
