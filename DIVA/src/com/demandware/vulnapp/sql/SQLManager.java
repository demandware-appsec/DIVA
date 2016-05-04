package com.demandware.vulnapp.sql;

import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.demandware.vulnapp.servlet.DivaApp;

/**
 * Manager for SQL Connections via DBHolder
 * Two kinds of DB:
 *  1. DBHolder for SQL Injection Challenge
 *  2. DB for "System" functions like user management
 *   
 * @author Chris Smith
 */
public class SQLManager {
    private static final SQLManager instance = new SQLManager();
    
    
    private final Map<String,DBHolder> dbConnInfo;
    
    public static SQLManager getInstance(){
    	return instance;
    }
    
    private SQLManager(){
    	this.dbConnInfo = new ConcurrentHashMap<String, DBHolder>();
    }
    
    /**
     * sets up SQL cleanup task
     */
	public void setupSQLManager() {
		DivaApp.getInstance().submitScheduledTask(new SQLCleanup(), DBHolder.CLEANUP_TIME_SEC, DBHolder.CLEANUP_TIME_SEC);		
	}
    
	/**
	 * adds a DBHolder to the manager for managing, keyed by DBName
	 */
    public void addDBHolder(DBHolder holder){
    	this.dbConnInfo.put(holder.getDBName(), holder);
    }
    
    /**
     * return a DBHolder for the key (sessionID) or null, if it doesn't exist
     */
    public DBHolder getDBHolder(String key){
    	DBHolder holder = null;
    	if(this.dbConnInfo.containsKey(key)){
    		holder = this.dbConnInfo.get(key);
    	}
    	return holder;
    }
    
    /**
     * deletes a DBHolder by sessionID to force a reset 
     */
    public void removeDBHolder(String key){
    	if(this.dbConnInfo.containsKey(key)){
    		this.dbConnInfo.remove(key);
    	}
    }
    
    /**
     * returns map of DBconnections for possible cleanup
     * @return
     */
    Map<String, DBHolder> getConnInfo(){
    	return this.dbConnInfo;
    }
    
    public void deregister(){
    	Enumeration<Driver> drivers = DriverManager.getDrivers();
    	while(drivers.hasMoreElements()){
    		Driver driver = drivers.nextElement();
    		try{
    			DriverManager.deregisterDriver(driver);
    		} catch(Exception e){
    			e.printStackTrace();
    		}
    	}
    }
    
}
