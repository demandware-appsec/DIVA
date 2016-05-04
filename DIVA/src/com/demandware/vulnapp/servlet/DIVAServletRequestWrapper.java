package com.demandware.vulnapp.servlet;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import com.demandware.vulnapp.challenge.AbstractChallenge;
import com.demandware.vulnapp.challenge.ChallengeInfo;
import com.demandware.vulnapp.challenge.ChallengePlan;
import com.demandware.vulnapp.challenge.ChallengePlan.UpdateStatus;
import com.demandware.vulnapp.challenge.impl.ChallengeFactory;
import com.demandware.vulnapp.challenge.impl.ChallengeFactory.ChallengeType;
import com.demandware.vulnapp.challenge.impl.XSSChallenge;
import com.demandware.vulnapp.flags.FlagManager;
import com.demandware.vulnapp.sessionmgmt.SessionManager;
import com.demandware.vulnapp.sessionmgmt.SessionStorage;
import com.demandware.vulnapp.user.LoginRegister;
import com.demandware.vulnapp.util.Helpers;
import com.demandware.vulnapp.util.exception.AccountException;
import com.demandware.vulnapp.util.exception.IllegalSessionException;

/**
 * Wraps the regular request to add knowledge of the response, and create a Map 
 * of random information. I chose against using the attribute system as it was having problems
 * during type conversion.
 * This class is also in charge of setting up knowledge of authorization for a request.
 * 
 * @author Chris Smith
 */
public class DIVAServletRequestWrapper extends HttpServletRequestWrapper{

	//almost duplicate of request's Attributes attribute sometimes bombs randomly
	private Map<String, Object> requestInformation;
	private HttpServletResponse response;
	
	DIVAServletRequestWrapper(HttpServletRequest request, HttpServletResponse response) {
		super(request); 
		this.response = response;
		requestInformation = new HashMap<String, Object>();
	}

	/**
	 * Build information object for this JSP Challenge (If not a challenge, does nothing)
	 * @throws IllegalSessionException if a user is attempting to gain access to a challenge that they don't have access to yet
	 */
	void setupJSPChallengeData() throws IllegalSessionException{
		
		SessionStorage store = getSessionStore();
		
		
		
		String challengeChecksum = Helpers.extractPageNameFromURLString(this.getRequestURI());
		ChallengeInfo currChall = ChallengePlan.getInstance().getChallengeForLinkValue(challengeChecksum);

		FlagManager flags = FlagManager.getInstance();
		String flag = null;
		AbstractChallenge thisChallenge = null;
		if(currChall != null){
			if(	!store.getUser().hasAnyAccess(currChall.getChallengeType()) && 
				!currChall.getChallengeType().equals(ChallengeType.MD5) &&
				!(currChall.getChallengeType().equals(ChallengeType.XSS) && 
				 ((XSSChallenge) ChallengeFactory.getInstance().getChallenge(ChallengeType.XSS)).hasXSSAdminCookie(this.getCookies()))){
					throw new IllegalSessionException("Out of order Challenges");
				
			}
			flag = flags.generateFlagForUser(store, currChall);
			thisChallenge = ChallengeFactory.getInstance().getChallenge(currChall.getChallengeType());
			this.setInformation(Dictionary.FLAG_VALUE, flag);
			this.setInformation(Dictionary.CURRENT_CHALLENGE_OBJ, thisChallenge);
		}
		this.setInformation(Dictionary.SESSION_STORE_OBJ, store);
		this.setInformation(Dictionary.CHECKSUM_OBJ, challengeChecksum);
		this.setInformation(Dictionary.CURRENT_CHALLENGE_INFO_OBJ, currChall);
		
		checkForFlagUpdate();

	}
	
	private void checkForFlagUpdate() {
		UpdateStatus upStat = ChallengePlan.getInstance().updateChallengeIfCorrect(this);
		this.setInformation(Dictionary.UPDATE_STATUS, upStat);
	}

	private SessionStorage getSessionStore() {
		SessionStorage store = null;
		String loginProblems = "";
		try{
			if(LoginRegister.isLoginRequest(this)){
				LoginRegister.doLogin(this);
				store = (SessionStorage) this.getInformation(Dictionary.SESSION_STORE_OBJ);
			}else if(LoginRegister.isRegisterRequest(this)){
				LoginRegister.doRegister(this);
				store = (SessionStorage) this.getInformation(Dictionary.SESSION_STORE_OBJ);
			}
		} catch(AccountException e){
			loginProblems = e.getStatus().getMessage();
		}
		
		this.setInformation(Dictionary.LOGIN_PROBLEM, loginProblems);
		
		if(store == null){
			store = SessionManager.getInstance().getStoreForRequest(this);
		}
		
		store.getUser().updateLastActivity(System.currentTimeMillis());
		store.getUser().updateIP(this.getClientIpAddr());
		return store;
	}
	
	private String getClientIpAddr() {  
        String ip = this.getHeader("X-Forwarded-For");  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = this.getHeader("Proxy-Client-IP");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = this.getHeader("WL-Proxy-Client-IP");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = this.getHeader("HTTP_CLIENT_IP");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = this.getHeader("HTTP_X_FORWARDED_FOR");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = this.getRemoteAddr();  
        }  
        return ip;  
    }  

	private void setInformation(String id, Object value){
		this.requestInformation.put(id, value);
	}
	
	public Object getInformation(String id){
		return this.requestInformation.get(id);
	}
	
	public HttpServletResponse getResponse(){
		return this.response;
	}

	public void updateSession(SessionStorage store) {
		this.setInformation(Dictionary.SESSION_STORE_OBJ, store);
	}
}
