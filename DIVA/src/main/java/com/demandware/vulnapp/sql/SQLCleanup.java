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
