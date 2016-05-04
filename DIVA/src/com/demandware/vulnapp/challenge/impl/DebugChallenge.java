package com.demandware.vulnapp.challenge.impl;

import com.demandware.vulnapp.challenge.AbstractChallenge;
import com.demandware.vulnapp.servlet.DIVAServletRequestWrapper;


/**
 * No-op. Flag is in text of HTML
 * @author Chris Smith
 *
 */
public class DebugChallenge extends AbstractChallenge{
	protected DebugChallenge(String name) {
		super(name);
	}

	@Override
	public Object handleChallengeRequest(DIVAServletRequestWrapper req) {
		return null;
	}

}