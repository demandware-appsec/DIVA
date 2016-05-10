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
package com.demandware.vulnapp.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.demandware.vulnapp.util.SecureRandomUtil;

/**
 * Maintains internal-only data on connections, passwords, users, etc.
 * 
 * @author Chris Smith
 *
 */
public class DBHolder {
	private final String dbDriver;
    private final String dbConnURL;
    private final String dbName;
    private final String dbUser;
    private final String dbPass;
	private long ttl; 
	
    private static final String CLOSE_DELAY = "120";
    private static final long TTL_ADDITION = 120L * 1000;
    static final long CLEANUP_TIME_SEC = 10L;
    
    private DBHolder(String dbName, String dbUser, String dbPass){
    	this(dbName, dbUser, dbPass, true);
    }
    
    private DBHolder(String dbName, String dbUser, String dbPass, boolean haveDelayedClose){
    	this.dbDriver = "org.h2.Driver";
    	this.dbName = dbName;
    	this.dbConnURL = "jdbc:h2:mem:" + this.dbName + (haveDelayedClose ? ";DB_CLOSE_DELAY="+CLOSE_DELAY : ";DB_CLOSE_DELAY=-1");
    	this.dbUser = dbUser;
    	this.dbPass = dbPass;
    	updateTTL();
    }
    
    /**
     * return a valid connection object for this DBHolder to the configured DB
     * UpdatesTTL
     */
    public Connection getConnection(){
    	updateTTL();
		Connection conn = null;
		try {
			Class.forName(this.dbDriver);
			conn = DriverManager.getConnection(this.dbConnURL, this.dbUser, this.dbPass);
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return conn;
	}
    
    public String getDBName(){
    	return this.dbName;
    }

    /**
     * DBHolder Constructor for challenge. 
     * Makes DBHolder for a given session ID using a random password
     */
	public static DBHolder makeForSession(String session) {
		String pass = SecureRandomUtil.generateRandomHexString(6);
		return new DBHolder(session, session, pass);
	}
	
	/**
	 * increases TTL for this object so it is not cleaned up
	 */
	private void updateTTL(){
		this.ttl = System.currentTimeMillis() + TTL_ADDITION;
	}
	
	long getTTL(){
		return this.ttl;
	}
}
