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
