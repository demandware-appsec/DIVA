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
