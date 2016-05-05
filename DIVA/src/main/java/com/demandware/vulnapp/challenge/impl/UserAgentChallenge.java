package com.demandware.vulnapp.challenge.impl;

import com.demandware.vulnapp.challenge.AbstractChallenge;
import com.demandware.vulnapp.servlet.DIVAServletRequestWrapper;

import eu.bitwalker.useragentutils.BrowserType;
import eu.bitwalker.useragentutils.UserAgent;

/**
 * Check that the user agent is a mobile user agent
 * 
 * @author Chris Smith
 *
 */
public class UserAgentChallenge extends AbstractChallenge{
	protected UserAgentChallenge(String name) {
		super(name);
	}

	@Override
	public Boolean handleChallengeRequest(DIVAServletRequestWrapper req) {
		UserAgent userAgent = UserAgent.parseUserAgentString(req.getHeader("User-Agent"));
		return userAgent.getBrowser().getBrowserType().equals(BrowserType.MOBILE_BROWSER);
	}

}
