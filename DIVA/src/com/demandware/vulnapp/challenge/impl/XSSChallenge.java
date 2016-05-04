package com.demandware.vulnapp.challenge.impl;


import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.servlet.http.Cookie;

import org.apache.http.client.utils.URIBuilder;

import com.demandware.vulnapp.challenge.AbstractChallenge;
import com.demandware.vulnapp.challenge.ChallengeInfo;
import com.demandware.vulnapp.challenge.ChallengePlan;
import com.demandware.vulnapp.challenge.impl.ChallengeFactory.ChallengeType;
import com.demandware.vulnapp.flags.FlagManager;
import com.demandware.vulnapp.servlet.DIVAServletRequestWrapper;
import com.demandware.vulnapp.servlet.Dictionary;
import com.demandware.vulnapp.servlet.DivaApp;
import com.demandware.vulnapp.sessionmgmt.SessionManager;
import com.demandware.vulnapp.sessionmgmt.SessionStorage;
import com.demandware.vulnapp.user.User;
import com.demandware.vulnapp.user.UserManager;
import com.demandware.vulnapp.util.Helpers;
import com.demandware.vulnapp.util.SecureRandomUtil;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.google.common.collect.EvictingQueue;


/**
 * Execute either a XSS to session stealing attack OR XSS to CSRF 
 * to get an "admin" to send a flag to you. "admin" runs as an 
 * HTMLUnit with javascript enabled
 * 
 * @author Chris Smith
 */
public class XSSChallenge extends AbstractChallenge {
	
	public static final String MESSAGE_PARAM = "message";
	public static final String RECIPIENT_PARAM = "send_to";
	public static final String RECIPIENT_ADMIN = "admin_user";
	public static final String SEND_FLAG_PARAM = "send_flag";
	public static final String CLEAR_MESSAGES = "clear_messages";
	public static final String ADMIN_MESSAGE_PARAM = "admin_message";
	
	
	private final String adminCookieValue;
	private static final String DIVA_ADMIN_NAME = "DIVA_ADMIN_SESSION";
	private static final int MESSAGE_QUEUE_SIZE = 5;
	
	
	//session to message queue
	private Map<String, Queue<String>> messageMap;
	
	protected XSSChallenge(String name) {
		super(name);
		this.adminCookieValue = SecureRandomUtil.generateRandomHexString(6);
		this.messageMap = new ConcurrentHashMap<String, Queue<String>>();
	}

	/**
	 * checks if a request's cookie list contains the xss admin's cookie
	 * 
	 * @param cs cookie list
	 * @return true if the cookie list has a valid xss admin cookie
	 */
	public boolean hasXSSAdminCookie(javax.servlet.http.Cookie[] cs){
		for(javax.servlet.http.Cookie c : cs){
			if(c.getName().equals(DIVA_ADMIN_NAME) && c.getValue().equals(this.adminCookieValue)){
				return true;
			}
		}
		return false;
	}
	
	public Object handleChallengeRequest(DIVAServletRequestWrapper req){
		if(req.getParameter(XSSChallenge.CLEAR_MESSAGES) != null){
			clearUserMessages(req);
		} else if(req.getParameter(XSSChallenge.MESSAGE_PARAM) != null && 
				req.getParameter(XSSChallenge.RECIPIENT_PARAM) != null){
			handleMessage(req);
		}
		return null;
	}

	/**
	 * removes all messages for this user
	 */
	private void clearUserMessages(DIVAServletRequestWrapper req){
		SessionStorage session = (SessionStorage)req.getInformation(Dictionary.SESSION_STORE_OBJ);
		if(this.messageMap.containsKey(session.getUser().getUserName())){
			this.messageMap.remove(session.getUser().getUserName());
		}
	}
	
	/**
	 * submits message to correct handler (either dispatched to "admin" or to a user)
	 */
	private void handleMessage(DIVAServletRequestWrapper req) {
		if(req.getParameter(XSSChallenge.RECIPIENT_PARAM).equals(XSSChallenge.RECIPIENT_ADMIN)){
			submitMessageForAdmin(req);
		}else{
			addMessageForUser(req);
		}
	}

	/**
	 * take a message from the request and send for admin consumption
	 */
	private void submitMessageForAdmin(DIVAServletRequestWrapper req) {
		String messageForAdmin = req.getParameter(XSSChallenge.MESSAGE_PARAM);
		javax.servlet.http.Cookie adminCookie = makeAdminCookie(req.getServerName());
		XSSChallengeAgent xss = new XSSChallengeAgent(adminCookie, req.getRequestURL().toString(), messageForAdmin);
		try {
			DivaApp.getInstance().submitCachedCallable(xss).get(XSSChallengeAgent.DEFAULT_TIMEOUT, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			//mostly impossible/don't care exceptions
		}
	}
	
	/**
	 * create a new xss admin cookie based on a given domain
	 */
	private javax.servlet.http.Cookie makeAdminCookie(String domain){
		javax.servlet.http.Cookie adminCookie = new javax.servlet.http.Cookie(DIVA_ADMIN_NAME, this.adminCookieValue);
		adminCookie.setDomain(domain);
		return adminCookie;
	}
	
	/**
	 * puts a message on a user's message queue 
	 */
	private void addMessageForUser(DIVAServletRequestWrapper req){
		Queue<String> messageList;
		String recipient = req.getParameter(XSSChallenge.RECIPIENT_PARAM);
		String message = req.getParameter(XSSChallenge.MESSAGE_PARAM);
		boolean sendFlag = Helpers.isTruthy(req.getParameter(XSSChallenge.SEND_FLAG_PARAM));
		if(isAdminSession(req) && sendFlag){
			message += generateFlagForRequestedUser(recipient);
		}
		if(this.messageMap.containsKey(recipient)){
			messageList = this.messageMap.get(recipient);
		} else{
			messageList = EvictingQueue.create(XSSChallenge.MESSAGE_QUEUE_SIZE);
			this.messageMap.put(recipient, messageList);
		}
		messageList.add(message);
	}
	
	/**
	 * generate a flag for a given user based on their name
	 */
	private String generateFlagForRequestedUser(String userName) {
		User usr = UserManager.getInstance().searchForUser(userName);
		if(usr == null){
			return "";
		}
		List<SessionStorage> stores = SessionManager.getInstance().getSessionsForUser(usr);
		if(stores.size() < 1){
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for(SessionStorage session : stores){
			ChallengeInfo cInfo = ChallengePlan.getInstance().getChallengeForType(ChallengeType.XSS);
			String flag = FlagManager.getInstance().generateFlagForUser(session, cInfo);
			sb.append("Flag: " + flag);
		}
		
		return sb.toString();
	}

	/**
	 * @param userSession session object for a user
	 * @return Queue of String messages for that user
	 */
	public Queue<String> getMessagesForUser(SessionStorage userSession){
		if(this.messageMap.containsKey(userSession.getUser().getUserName())){
			return this.messageMap.get(userSession.getUser().getUserName());
		}
		return null;
	}
	
	/**
	 * true if the given request is an admin session, false otherwise
	 */
	public boolean isAdminSession(DIVAServletRequestWrapper req) {
		Cookie[] cs = req.getCookies();
		if(cs != null){
			for(Cookie c : cs){
				if(c.getName().equals(DIVA_ADMIN_NAME) && c.getValue().equals(this.adminCookieValue)){
					return true;
				}
			}
		}
		return false;
	}
	
	
	/**
	 * runs once per request, immediately cleaned up.
	 * 
	 * @author Chris Smith
	 *
	 */
	private class XSSChallengeAgent implements Callable<Boolean>{
		private com.gargoylesoftware.htmlunit.util.Cookie cookie;
		private final String pageToVisit;
		private final String adminMessage;
		
		private static final long DEFAULT_TIMEOUT = 5;
		
		private XSSChallengeAgent(javax.servlet.http.Cookie sessionCookie, String pageToVisit, String adminMessage){
			this.cookie = new com.gargoylesoftware.htmlunit.util.Cookie(
					sessionCookie.getDomain(), sessionCookie.getName(), sessionCookie.getValue());
			
			this.adminMessage = adminMessage;
			
			String pageBuild = "";
			try {
				URIBuilder urb = new URIBuilder(pageToVisit);
				urb.addParameter(XSSChallenge.ADMIN_MESSAGE_PARAM, this.adminMessage);
				pageBuild = urb.build().toString();
			} catch (URISyntaxException e) {
				pageBuild = pageToVisit + "?" + XSSChallenge.ADMIN_MESSAGE_PARAM + "=" + this.adminMessage;
			}
			
			this.pageToVisit = pageBuild;
		}
		
		@Override
		public Boolean call() throws Exception {
			try(final WebClient webClient = new WebClient()){
				webClient.getCookieManager().setCookiesEnabled(true);
				webClient.getCookieManager().addCookie(this.cookie);
			    try {
					webClient.getPage(this.pageToVisit);
				} catch (FailingHttpStatusCodeException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return true;	
		}
		
	}
}