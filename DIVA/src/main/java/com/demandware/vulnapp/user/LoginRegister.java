package com.demandware.vulnapp.user;

import java.util.Map;

import com.demandware.vulnapp.servlet.DIVAServletRequestWrapper;
import com.demandware.vulnapp.sessionmgmt.SessionManager;
import com.demandware.vulnapp.sessionmgmt.SessionStorage;
import com.demandware.vulnapp.util.Helpers;
import com.demandware.vulnapp.util.exception.AccountException;

/**
 * Handles register/login requests
 * @author Chris Smith
 *
 */
public class LoginRegister {
	
	public final static String USERNAME_PARAM = "username";
	public final static String PASSWORD_PARAM = "password";
	public final static String REGISTER_PARAM = "register_user";
	public final static String LOGIN_PARAM = "login_user";
	
	private static final int WASTE_MIN = 100;
	private static final int WASTE_MAX = 1000;
	
	public static User doLogin(DIVAServletRequestWrapper req) throws AccountException {
		String user = req.getParameter(USERNAME_PARAM);
		String pass = req.getParameter(PASSWORD_PARAM);
		
		User usr = null;
		if(user != null && pass != null){
			usr = UserManager.getInstance().searchForUser(user, pass);
			SessionStorage store = SessionManager.getInstance().createAndStoreSession(usr);
			req.updateSession(store);
			SessionManager.setCookieInformation(req);
		}
		
		return usr;
	}
	
	public static User doRegister(DIVAServletRequestWrapper req) throws AccountException{
		String user = req.getParameter(USERNAME_PARAM);
		String pass = req.getParameter(PASSWORD_PARAM);
		
		User usr = null;
		if(user != null && pass != null){
			usr = UserManager.getInstance().makeNewUser(user, pass);
			SessionStorage store = SessionManager.getInstance().createAndStoreSession(usr);
			req.updateSession(store);
			SessionManager.setCookieInformation(req);
		}
		
		return usr;
	}
	
	/**
	 * Mostly a joke, just spends some random time so timing attacks aren't possible 
	 */
	public static void spendTime(){
		try {
			Thread.sleep(Helpers.randomBetween(WASTE_MIN, WASTE_MAX));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static boolean isLoginRequest(DIVAServletRequestWrapper req) {
		Map<String, String[]> params = req.getParameterMap();
		return 	params.containsKey(USERNAME_PARAM) && 
				params.containsKey(PASSWORD_PARAM) &&
				params.containsKey(LOGIN_PARAM);
	}
	
	public static boolean isRegisterRequest(DIVAServletRequestWrapper req) {
		Map<String, String[]> params = req.getParameterMap();
		return 	params.containsKey(USERNAME_PARAM) && 
				params.containsKey(PASSWORD_PARAM) &&
				params.containsKey(REGISTER_PARAM);
	}
}
