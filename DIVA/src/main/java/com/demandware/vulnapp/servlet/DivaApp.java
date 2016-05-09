package com.demandware.vulnapp.servlet;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.FilterConfig;

import com.demandware.vulnapp.challenge.ChallengePlan;
import com.demandware.vulnapp.sessionmgmt.SessionManager;
import com.demandware.vulnapp.sql.SQLManager;
import com.demandware.vulnapp.user.UserManager;
import com.demandware.vulnapp.util.Helpers;

/**
 * Main controller for all activities and management of challenges.
 * Contains all knowledge about locations and objects.
 * Manages Threads
 * Manages Setup and Teardown of everything.
 * 
 * @author Chris Smith
 *
 */
public class DivaApp {
	private static final int SCHED_THEAD_NUM = 5;
	private final ScheduledExecutorService schedulerService;
	private final ExecutorService cachedService;
	private Map<String, String> appInformation;
	private static DivaApp instance = new DivaApp();
	
	private DivaApp(){
		this.appInformation = new ConcurrentHashMap<String, String>();
		this.schedulerService = Executors.newScheduledThreadPool(SCHED_THEAD_NUM);
		this.cachedService = Executors.newCachedThreadPool();
	}
	
	public static DivaApp getInstance(){
		return instance;
	}
	
	/**
	 * builds appInformation map, setup challenge manager, 
	 * setup session manager, setup sql manager
	 */
	void setup(FilterConfig fConfig){
		File root = new File(fConfig.getServletContext().getRealPath("/"));
		
		this.appInformation.put(Dictionary.SERVLET_ROOT, root.getPath());
		this.appInformation.put(Dictionary.CHALLENGES_ROOT, "challenges");
		this.appInformation.put(Dictionary.READABLE_JSP_LOC, "readable");
		this.appInformation.put(Dictionary.WAR_NAME, root.getName() + ".war");
		this.appInformation.put(Dictionary.LAST_RESTART, Long.toString(System.currentTimeMillis()));
		
		File tomcatRoot = findTomcatRoot(root);
		File restore = tomcatRoot == null ? null : new File(tomcatRoot, "DIVA_backup_users");

		this.appInformation.put(Dictionary.TOMCAT_ROOT, tomcatRoot == null ? "" : tomcatRoot.getPath());
		this.appInformation.put(Dictionary.DEBUG_USER, String.valueOf(Helpers.isTruthy(fConfig.getInitParameter("debug.user"))));
		this.appInformation.put(Dictionary.RESTORE_FOLDER, restore == null ? "" : restore.getPath());
		
		ChallengePlan.getInstance().setupChallengePlan();
		SessionManager.getInstance().setupSessionManager();
		SQLManager.getInstance().setupSQLManager();
		UserManager.getInstance().setupUserManager();
		if(makeDebugUser()){
			UserManager.getInstance().makeDebugUser();
		}
		
		this.appInformation = Collections.unmodifiableMap(this.appInformation);
	}
	
	private final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss");    
	public String getLastRestart(){
		return sdf.format(new Date(Long.parseLong(this.appInformation.get(Dictionary.LAST_RESTART))));
	}
	
	/**
	 * returns web.xml value for init challenges, or false if key does not exist.
	 */
	public boolean makeDebugUser(){
		return Boolean.valueOf(getInformation(Dictionary.DEBUG_USER));
	}
	
	/**
	 * searches for the Tomcat root (Eclipse runs have this fail)
	 * @param start beginning directory to search in. moves up from here
	 * @return tomcat root path location
	 */
	private File findTomcatRoot(File start){
		boolean found = false;
		File search = start;
		while(!found){
			File webapps = Helpers.getFileOrFolderInSubDir(search, "webapps", false);
			if(webapps != null){
				found = true;
				System.out.println("Found Tomcat Root: " + search.getPath());
			}
			search = search.getParentFile();
			if(search == null){
				System.out.println("Could not find Tomcat Root");
				break;
			}
		}
		
		return search;
	}
	
	/**
	 * adds a runnable task to a scheduled task executor
	 * @param runnable the task to run
	 * @param delay on what delay
	 * @param periodInSeconds how often to rerun (ad infinitum)
	 */
	public void submitScheduledTask(Runnable runnable, long delay, long periodInSeconds){
		this.schedulerService.scheduleAtFixedRate(runnable, delay, periodInSeconds, TimeUnit.SECONDS);
	}
	
	/**
	 * adds a callable task to a cached executor 
	 * @param runnable task to run
	 * @return the Future associated with the callable
	 */
	public <T> Future<T> submitCachedCallable(Callable<T> runnable){
		return this.cachedService.submit(runnable);
	}
	
	/**
	 * Returns value associated with the given key, or null if not found 
	 */
	public String getInformation(String key){
		return this.appInformation.get(key);
	}
	
	/**
	 * properly clean up executors and managers.
	 */
	void teardown(){
		this.schedulerService.shutdownNow();
		this.cachedService.shutdownNow();
		ChallengePlan.getInstance().teardownChallenges();
		SessionManager.getInstance().teardownSessions();
		UserManager.getInstance().teardownUsers();
		SQLManager.getInstance().deregister();
	}

}
