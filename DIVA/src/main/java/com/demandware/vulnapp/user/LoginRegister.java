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
package com.demandware.vulnapp.user;

import java.util.Map;

import com.demandware.vulnapp.servlet.DIVAServletRequestWrapper;
import com.demandware.vulnapp.sessionmgmt.SessionManager;
import com.demandware.vulnapp.sessionmgmt.SessionStorage;
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
