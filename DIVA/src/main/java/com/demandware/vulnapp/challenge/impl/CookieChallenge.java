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
package com.demandware.vulnapp.challenge.impl;

import javax.servlet.http.Cookie;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.demandware.vulnapp.challenge.AbstractChallenge;
import com.demandware.vulnapp.servlet.DIVAServletRequestWrapper;
import com.demandware.vulnapp.util.Helpers;


/**
 * Cookie contains auth information, change the auth to grant access
 * 
 * @author Chris Smith
 *
 */
public class CookieChallenge extends AbstractChallenge {

	public static final String RED_HERRING = "red_herring";
	public static final String COOKIE_NAME = "challenge_access";
	public static final String ACCESS_KEY = "access_granted";
	private static final int MAX_AGE = 5*60; //5mins
	
	protected CookieChallenge(String name) {
		super(name);
	}

	public Boolean handleChallengeRequest(DIVAServletRequestWrapper req){
		boolean grantsAccess = false;
		Cookie c = getChallengeCookie(req);
		if(c != null){
			grantsAccess = doesCookieValueGrantAccess(c);
		}
		req.getResponse().addCookie(generateCookie());
		return grantsAccess;
	}
	
	private Cookie getChallengeCookie(DIVAServletRequestWrapper req) {
		Cookie[] cs = req.getCookies();
		Cookie cookie = null;
		if(cs != null){
			for(Cookie c : cs){
				if(c.getName().equals(COOKIE_NAME)){
					cookie = c;
					break;
				}
			}
		}
		return cookie;
	}

	private boolean doesCookieValueGrantAccess(Cookie c){
		boolean grant = false;
		String value = new String(Base64.decodeBase64(c.getValue()));
		try {
			JSONObject o = (JSONObject) new JSONParser().parse(value);
			grant = Helpers.isTruthy((String) o.get(ACCESS_KEY));
		} catch (ParseException e) {
			grant = false;
		}
		return grant;
	}
	
	@SuppressWarnings("unchecked")
	private Cookie generateCookie(){
		JSONObject o = new JSONObject();
		o.put(ACCESS_KEY, "false");
		String value = new String(Base64.encodeBase64(o.toJSONString().getBytes()));
		Cookie c = new Cookie(COOKIE_NAME, value);
		c.setMaxAge(MAX_AGE);
		c.setPath("/");
		return c;
	}

}
