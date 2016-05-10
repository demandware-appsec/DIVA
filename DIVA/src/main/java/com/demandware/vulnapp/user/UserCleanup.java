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

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.demandware.vulnapp.sessionmgmt.SessionManager;

/**
 * Removes all users who are inactive and have no points
 * 
 * @author Chris Smith
 *
 */
public class UserCleanup implements Runnable {

	@Override
	public void run() {
		ConcurrentLinkedQueue<User> users = UserManager.getInstance().getUsers();
		Iterator<User> userIt = users.iterator();
		SessionManager sMan = SessionManager.getInstance();

		while(userIt.hasNext()){
			User usr = userIt.next();
			if( !UserManager.isFakeUser(usr) &&
					!sMan.isUserActive(usr) &&
					usr.getPoints() == 0){
				
				System.out.println("Cleaned user " + usr.getUserName());
				userIt.remove();
			}
		}
	}
}
