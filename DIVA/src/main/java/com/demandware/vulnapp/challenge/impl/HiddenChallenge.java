package com.demandware.vulnapp.challenge.impl;

import com.demandware.vulnapp.challenge.AbstractChallenge;
import com.demandware.vulnapp.servlet.DIVAServletRequestWrapper;



/**
 * Another No-op, Hidden challenge is found by decompiling the java war
 * 
 * @author Chris Smith
 *
 */
public class HiddenChallenge extends AbstractChallenge {

	protected HiddenChallenge(String name) {
		super(name);
	}

	@Override
	public Object handleChallengeRequest(DIVAServletRequestWrapper req) {
		return null;
	}

}