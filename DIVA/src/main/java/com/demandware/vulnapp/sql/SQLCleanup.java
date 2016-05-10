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
package com.demandware.vulnapp.sql;

import java.util.Map;

/**
 * Periodically cleans DBHolders from Manager after a timeout of disuse
 * 
 * @author Chris Smith
 *
 */
public class SQLCleanup implements Runnable{

	@Override
	public void run() {
		try{
			Map<String,DBHolder> holders = SQLManager.getInstance().getConnInfo();
			for(String key : holders.keySet()){
				DBHolder holder = holders.get(key);
				if(holder.getTTL() < System.currentTimeMillis()){
					System.out.println("SQLCleanup removing key: " + key);
					holders.remove(key);
				}
			}
		} catch(Throwable t){
			//no idea how the map works on concurrent delete... so... this
			t.printStackTrace();
		}
	}

}
