package com.demandware.vulnapp.challenge.impl;

import com.demandware.vulnapp.challenge.AbstractChallenge;
import com.demandware.vulnapp.challenge.ChallengePlan;
import com.demandware.vulnapp.challenge.impl.ChallengeFactory.ChallengeType;
import com.demandware.vulnapp.flags.FlagManager;
import com.demandware.vulnapp.servlet.DIVAServletRequestWrapper;
import com.demandware.vulnapp.servlet.Dictionary;
import com.demandware.vulnapp.sessionmgmt.SessionManager;
import com.demandware.vulnapp.sessionmgmt.SessionStorage;
import com.demandware.vulnapp.user.User;
import com.demandware.vulnapp.user.UserManager;
import com.demandware.vulnapp.util.exception.IllegalSessionException;

/**
 * This class can only be accessed by a Fake user. Once there,
 * just enter a session id to generate a flag for another session.
 * 
 * WARNING: if you try to access this challenge from a real account,
 * you will be reset.
 * 
 * @author Chris Smith
 *
 */
public class UnfinishedChallenge extends AbstractChallenge{

	public static final String SESSION_PARAM = "flag_session";

	protected UnfinishedChallenge(String name) {
		super(name);
	}

	@Override
	public String handleChallengeRequest(DIVAServletRequestWrapper req) {
		String flag = null;
		try{
			flag = challenge(req);
		} catch(IllegalSessionException e){
			flag = "You have been reset";
		}

		return flag;
	}

	private String challenge(DIVAServletRequestWrapper req) throws IllegalSessionException{
		SessionStorage store = (SessionStorage) req.getInformation(Dictionary.SESSION_STORE_OBJ);
		User currentUser = store.getUser();

		if((UserManager.isDebugUser(currentUser))){
			throw new IllegalSessionException("Can't use this user");
		}
		
		if(!(UserManager.isChallengerUser(currentUser))){
			currentUser.resetUser();
			throw new IllegalSessionException("User: " + currentUser.getUserName() + " has been reset");
		}

		String usrFlag = "";
		String targetSession = req.getParameter(SESSION_PARAM);
		if(targetSession != null){
			SessionStorage session = SessionManager.getInstance().getStoreForToken(targetSession);
			usrFlag = FlagManager.getInstance().generateFlagForUser(session, ChallengePlan.getInstance().getChallengeForType(ChallengeType.UNFINISHED));
		}

		return usrFlag; 
	}

}
