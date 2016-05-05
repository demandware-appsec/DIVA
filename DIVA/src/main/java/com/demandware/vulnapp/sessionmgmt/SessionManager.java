package com.demandware.vulnapp.sessionmgmt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.demandware.vulnapp.servlet.DIVAServletRequestWrapper;
import com.demandware.vulnapp.servlet.Dictionary;
import com.demandware.vulnapp.servlet.DivaApp;
import com.demandware.vulnapp.user.Guest;
import com.demandware.vulnapp.user.User;
import com.demandware.vulnapp.user.UserManager;
import com.demandware.vulnapp.util.Helpers;
import com.demandware.vulnapp.util.SecureRandomUtil;

/**
 * SessionManager maintains all active sessions and session objects
 * 
 * @author Chris Smith
 *
 */
public class SessionManager {
	private static final int SESSION_HEX_SIZE = 12;
	private final static SessionManager instance = new SessionManager();
	
	private Map<String, SessionStorage> sessionMap;
	
	private SessionManager(){
		this.sessionMap = new ConcurrentHashMap<String, SessionStorage>();
	}
	
	public static SessionManager getInstance(){
		return instance;
	}
	
	public void setupSessionManager(){
		DivaApp.getInstance().submitScheduledTask(new SessionCleanup(), SessionStorage.CLEANUP_TIME_SEC, SessionStorage.CLEANUP_TIME_SEC);
	}
	
	/**
	 * return the proper session cookie for a request, or null if not found
	 */
	private static Cookie getDivaCookie(HttpServletRequest request){
		Cookie cookie = null;
		Cookie[] cookies = request.getCookies();
		if(cookies != null){
			for(Cookie c : cookies){
				if(c.getName().equals(Dictionary.COOKIE_SESSION_PARAM)){
					cookie = c;
					break;
				}
			}
		}
		return cookie;
	}
	
	/**
	 * attempts to retrieve a session object for a request.
	 * 
	 * @param request incoming http request
	 * @return session object if found, null otherwise
	 */
	public SessionStorage getStoreForRequest(HttpServletRequest request){
		SessionStorage sessionStore = null;
		Cookie c = getDivaCookie(request);
		
		if(c != null && !request.getParameterMap().containsKey(Dictionary.CLEAR_SESSION_PARAM)){
			sessionStore = getStoreForToken(c.getValue());
		}
		if(request.getParameterMap().containsKey(Dictionary.CLEAR_SESSION_PARAM)){
			kill(c.getValue());
		}
		if(request.getParameterMap().containsKey(Dictionary.DELETE_PARAM)){
			UserManager.getInstance().deleteUser(sessionStore.getUser());
			kill(c.getValue());
			sessionStore = null;
		}
		if(sessionStore == null){
			User guest = UserManager.getInstance().makeGuestUser();
			sessionStore = createAndStoreSession(guest);
		}
		
		return sessionStore;
	}
	
	/**
	 * Given a session ID, return the associated session object, or null if not found
	 * Updates session object TTL
	 */
	public SessionStorage getStoreForToken(String token){
		SessionStorage store = this.sessionMap.get(token);
		if(store != null){
			store.updateTTL();
		}
		return store;
	}
	
	/**
	 * access the session object associated with the given token without updating its TTL
	 * used for session cleanup
	 */
	SessionStorage getStoreForTokenNoUpdate(String token){
		return this.sessionMap.get(token);
	}
	
	public boolean isUserActive(User usr){
		for (SessionStorage store : this.sessionMap.values()){
			if(store.getUser().equals(usr)){
				return true;
			}
		}
		return false;
	}
	
	public List<SessionStorage> getSessionsForUser(User usr){
		List<SessionStorage> stores = new ArrayList<SessionStorage>();
	
		for(SessionStorage store : this.sessionMap.values()){
			if(store.getUser().equals(usr)){
				stores.add(store);
			}
		}
		return stores;
	}
	
	/**
	 * builds a new session object, creating a new sessionID
	 * @return new session object
	 */
	public SessionStorage createAndStoreSession(User user){
		String sessionID = SecureRandomUtil.generateRandomHexString(SESSION_HEX_SIZE);
		SessionStorage ss = new SessionStorage(sessionID, user);
		addSession(ss);
		return ss;
	}
	
	/**
	 * adds a session object to the sessionmap (keyed by sessionID)
	 */
	private void addSession(SessionStorage store){
		this.sessionMap.put(store.getToken(), store);
	}
	
	public int getActiveSessions(){
		int num = 0;
		Iterator<SessionStorage> storeIt = this.sessionMap.values().iterator();
		while(storeIt.hasNext()){
			SessionStorage store = storeIt.next();
			if(store.isLive() && 
				!UserManager.isFakeUser(store.getUser()) &&
				!(store.getUser() instanceof Guest)	){
				
				num ++;
			}
		}
		return num;
	}
	
	/**
	 * returns the list of session tokens to search for dead sessions
	 * @return
	 */
	Set<String> getTokensForCleanup(){
		return this.sessionMap.keySet();
	}

	/**
	 * removes a session object by its session ID
	 */
	void kill(String token) {
		this.sessionMap.remove(token);		
	}

	/**
	 * sets the return cookie for a valid request. updates TTL
	 */
	public static void setCookieInformation(DIVAServletRequestWrapper req) {
		SessionStorage store = (SessionStorage)req.getInformation(Dictionary.SESSION_STORE_OBJ);
		if(store != null){
			String ses = store.getToken();
			Cookie c = new Cookie(Dictionary.COOKIE_SESSION_PARAM, ses);
			c.setHttpOnly(true);
			c.setMaxAge(Helpers.safeCastToInt(SessionStorage.TIME_TO_LIVE_MILLIS/1000L));
			c.setPath("/");
			req.getResponse().addCookie(c);
		}
	}

	/**
	 * cleanup any necessary session information before shutdown
	 */
	public void teardownSessions() {
		
	}


}
