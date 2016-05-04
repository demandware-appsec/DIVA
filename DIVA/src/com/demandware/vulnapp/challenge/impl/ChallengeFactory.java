package com.demandware.vulnapp.challenge.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.demandware.vulnapp.challenge.AbstractChallenge;

/**
 * Challenges are stored as pseudo-singletons by this class. 
 * Theoretically, if a challenge was designed in such a way 
 * that it could destroy itself (looking at you Compiler Challenge),
 * this implementation could recover from that.
 * 
 * @author Chris Smith
 *
 */
public class ChallengeFactory {

	/**
	 * init our "singletons"
	 * @author Chris Smith
	 *
	 */
	public static enum ChallengeType{
		COMMAND(new CommandInjectionChallenge("Command Injection Challenge")),
		COMPILER(new DynamicCompilerChallenge("Dynamic Compiler/Expression Language Challenge")),
		COOKIES(new CookieChallenge("Cookie Modification Challenge")),
		DEBUG(new DebugChallenge("Debug Code Challenge")),
		ENTROPY(new EntropyChallenge("Insufficient Entropy Challenge")),
		HARDCODE(new HardCodePasswordChallenge("Hard-Coded Password Challenge")),
		HIDDEN(new HiddenChallenge("Source Code Reversing Challenge")), 
		LOGINJECTION(new LogInjectionChallenge("Log Injection Challenge")),
		MD5(new MD5Challenge("MD5 Hash Break Challenge")),
		NULL(new NullStringChallenge("Null Byte String Challenge")),
		ORACLE(new ECBOracleChallenge("ECB Oracle Attack Challenge")),
		RFI(new RFIChallenge("Remote File Include Challenge")),
		RNG(new RNGChallenge("Random Number Generator Cracking Challenge")), 
		SQLI(new SQLIChallenge("SQL Injection Challenge")),
		TIMING(new TimingChallenge("Timing Attack Challenge")),
		UNFINISHED(new UnfinishedChallenge("It is literally impossible to get this challenge")), 
		USERAGENT(new UserAgentChallenge("User Agent Challenge")),
		VALIDATION(new ValidationChallenge("Client-Side Validation Challenge")),
		XSS(new XSSChallenge("Cross-Site Scripting/Session Stealing Challenge")),
		XXE(new XXEChallenge("XML External Entity Challenge")),
		;
		private final AbstractChallenge chal;
		
		private ChallengeType(AbstractChallenge chal){
			this.chal = chal;
		}
		
		AbstractChallenge getChallenge(){
			return this.chal;
		}
	}
	
	private final Map<ChallengeType, AbstractChallenge> challengeMap;

	private static final ChallengeFactory instance = new ChallengeFactory();
	
	private ChallengeFactory(){
		this.challengeMap = new ConcurrentHashMap<ChallengeType, AbstractChallenge>();
		setupMap();
	}
	
	private void setupMap(){
		for(ChallengeType ct : ChallengeType.values()){
			this.challengeMap.put(ct, ct.getChallenge());
		}
	}
	
	public static ChallengeFactory getInstance(){
		return instance;
	}
	
	public AbstractChallenge getChallenge(ChallengeType type) {
		return this.challengeMap.get(type);
	}
	
}
