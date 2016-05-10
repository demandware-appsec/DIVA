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
package com.demandware.vulnapp.sessionmgmt;

import java.util.Set;

/**
 * dispatch to SessionManager to end sessions after a timeout 
 * 
 * @author Chris Smith
 *
 */
class SessionCleanup implements Runnable{
	
	@Override
	public void run() {
		Set<String> tokens = SessionManager.getInstance().getTokensForCleanup();
		for (String s : tokens){
			SessionStorage store = SessionManager.getInstance().getStoreForTokenNoUpdate(s);
			if(!store.isLive()){
				System.out.println("SessionCleanup removing store: " + s);
				SessionManager.getInstance().kill(s);
			}
		}
	}

}
