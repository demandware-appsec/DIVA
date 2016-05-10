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
package com.demandware.vulnapp.flags;

import com.demandware.vulnapp.challenge.ChallengeInfo;
import com.demandware.vulnapp.sessionmgmt.SessionStorage;
import com.demandware.vulnapp.user.UserManager;
import com.demandware.vulnapp.util.Helpers;
import com.demandware.vulnapp.util.SecureRandomUtil;

/**
 * Factory for flags per session. Salt means allowing users to have 
 * source is OK
 * 
 * @author Chris Smith
 *
 */
public class FlagManager {
	
	private static final FlagManager instance = new FlagManager();
	private final byte[] salt;
	private static final String DELIM = "|"; 
	private static final int SALT_SIZE = 32;
	
	private FlagManager(){
		this.salt = new byte[SALT_SIZE];
		SecureRandomUtil.nextBytes(this.salt);
	}
	
	public static FlagManager getInstance(){
		return instance;
	}
	
	/**
	 * Given a session object and challenge info object, return a flag for the given user
	 * in the given challenge
	 */
	public String generateFlagForUser(SessionStorage session, ChallengeInfo cInfo){
		String sessiontoken = session.getToken();
		String flag = sessiontoken + DELIM + cInfo.getName();
		return Helpers.sha(flag, this.salt);
	}
	
	public boolean isCorrectFlag(SessionStorage session, ChallengeInfo cInfo, String testFlag){
		if(UserManager.isChallengerUser(session.getUser())){
			return false;
		}
		return generateFlagForUser(session, cInfo).equals(testFlag);
	}
}
