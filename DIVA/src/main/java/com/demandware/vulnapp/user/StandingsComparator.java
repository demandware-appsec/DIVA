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

import java.util.Comparator;
import java.util.List;

import com.demandware.vulnapp.challenge.impl.ChallengeFactory.ChallengeType;

/**
 * Simple class to show users on the leaderboard in a sane way
 * @author Chris Smith
 *
 */
class StandingsComparator implements Comparator<User>{

	@Override
	public int compare(User u1, User u2) {
		int compare = 0;
		List<ChallengeType> t1 = u1.getCompletedChallenges();
		List<ChallengeType> t2 = u2.getCompletedChallenges();
		if(t1.size() > t2.size()){
			compare = -1;
		} else if(t1.size() < t2.size()){
			compare = 1;
		} else{
			if(UserManager.isFakeUser(u1)){
				compare = 1;
			} else if(UserManager.isFakeUser(u2)){
				compare = -1;
			} else{
				String un1 = u1.getUserName();
				String un2 = u2.getUserName();
				compare = un1.compareTo(un2);
			}
		}
		return compare;
	}
}