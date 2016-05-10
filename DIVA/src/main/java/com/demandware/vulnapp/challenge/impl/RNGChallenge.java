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
package com.demandware.vulnapp.challenge.impl;

import java.util.AbstractMap.SimpleEntry;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;

import com.demandware.vulnapp.challenge.AbstractChallenge;
import com.demandware.vulnapp.servlet.DIVAServletRequestWrapper;
import com.demandware.vulnapp.servlet.DivaApp;


/**
 * So much fun. determine an RNG's next two ints.
 * @author Chris Smith
 *
 */
public class RNGChallenge extends AbstractChallenge{

	public static final String GIVEN_FIRST = "given1";
	public static final String GIVEN_SECOND = "given2";
	public static final String GUESS_FIRST = "guess1";
	public static final String GUESS_SECOND = "guess2";

	private volatile AtomicLong seed; 

	protected RNGChallenge(String name) {
		super(name);
		this.seed = new AtomicLong(System.currentTimeMillis());
		beginSchedule();
	}
	
	/**
	 * submit the RandomUpdater to the top level app scheduler
	 */
	private void beginSchedule(){
		DivaApp.getInstance().submitScheduledTask(new RandomUpdater(this), RandomUpdater.DELAY, RandomUpdater.PERIOD);
	}

	/**
	 * checks that the request contains values for all 4 inputs
	 */
	public boolean hasAllGuesses(DIVAServletRequestWrapper req){
		return 	!StringUtils.isBlank(req.getParameter(RNGChallenge.GIVEN_FIRST)) ||
				!StringUtils.isBlank(req.getParameter(RNGChallenge.GIVEN_SECOND)) ||
				!StringUtils.isBlank(req.getParameter(RNGChallenge.GUESS_FIRST)) ||
				!StringUtils.isBlank(req.getParameter(RNGChallenge.GUESS_SECOND));
	}

	public Boolean handleChallengeRequest(DIVAServletRequestWrapper req){
		boolean isCorrect = true;
		if(!hasAllGuesses(req)){
			isCorrect = false;
		} else{
			String given1 = req.getParameter(RNGChallenge.GIVEN_FIRST);
			String given2 = req.getParameter(RNGChallenge.GIVEN_SECOND);
			String guess1 = req.getParameter(RNGChallenge.GUESS_FIRST);
			String guess2 = req.getParameter(RNGChallenge.GUESS_SECOND);
			try{
				int r1,r2,r3,r4;
				r1 = Integer.parseInt(given1);
				r2 = Integer.parseInt(given2);
				r3 = Integer.parseInt(guess1);
				r4 = Integer.parseInt(guess2);
				
				Random rand = new Random(this.getSeed());
				for(int guess : new int[] {r1,r2,r3,r4}){
					int test = rand.nextInt();
					if(test != guess){
						isCorrect = false;
					}
				}
			} catch(Exception e){
				isCorrect = false;
			}
		}
		return isCorrect;
	}

	public SimpleEntry<String,String> getNextRNGs(DIVAServletRequestWrapper req){
		Random rand = new Random(getSeed());
		return new SimpleEntry<String,String>(String.valueOf(rand.nextInt()), String.valueOf(rand.nextInt()));
	}
	
	private void setNewRandomSeed(long newValue){
		this.seed.set(newValue);
	}

	private long getSeed(){
		return this.seed.get();
	}

	/**
	 * Periodically sets a new seed for the RandomChallenge
	 * 
	 * @author Chris Smith
	 */
	private class RandomUpdater implements Runnable{
		private static final long PERIOD = 30L;
		private static final long DELAY = 5*60L;

		private final RNGChallenge parent;

		private RandomUpdater(RNGChallenge parent){
			this.parent = parent;
		}

		@Override
		public void run() {
			this.parent.setNewRandomSeed(System.currentTimeMillis());
		}

	}

}
