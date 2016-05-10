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
package com.demandware.vulnapp.sessionmgmt;

import com.demandware.vulnapp.user.User;

/**
 * SessionStorage holds all of the challenge access authorization 
 * information for a session.
 * 
 * @author Chris Smith
 *
 */
public class SessionStorage {

	private final String token;
	private final User user;
	
	private long ttl;
	
	static long TIME_TO_LIVE_MILLIS = 1L * 60 * 60 * 1000; //1 hour in millis
	static long CLEANUP_TIME_SEC = 60L;
	
	SessionStorage(String token, User user){
		this.user = user;
		this.token = token;
		updateTTL();
	}
	
	/**
	 * @return session ID
	 */
	public String getToken(){
		return this.token;
	}
	
	/**
	 * @return user for this session object
	 */
	public User getUser(){
		return this.user;
	}

	/**
	 * true if this session is still "alive"
	 */
	public boolean isLive(){
		return this.ttl > System.currentTimeMillis();
	}
	
	/**
	 * updates the sessions life expectancy, managed in SessionManager
	 */
	void updateTTL(){
		this.ttl = System.currentTimeMillis() + TIME_TO_LIVE_MILLIS;
	}

}