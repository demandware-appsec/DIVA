package com.demandware.vulnapp.user;

import com.demandware.vulnapp.util.SecureRandomUtil;

/**
 * A type of user whose state is not maintained after the session dies
 * @author Chris Smith
 *
 */
public class Guest extends User{

	Guest(){
		super("Guest" + SecureRandomUtil.generateRandomHexString(10), "");
	}
	
}
