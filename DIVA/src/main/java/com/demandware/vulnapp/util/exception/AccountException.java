package com.demandware.vulnapp.util.exception;

import com.demandware.vulnapp.user.AccountStatus;

/**
 * Thrown when a user attempts an authorization illegally
 * @author Chris Smith
 *
 */
public class AccountException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7884320406329621849L;
	
	private AccountStatus stat;
	
	public AccountException(AccountStatus status){
		super();
		this.stat = status;
	}
	
	public AccountStatus getStatus(){
		return this.stat;
	}
	
}
