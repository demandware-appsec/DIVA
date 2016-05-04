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