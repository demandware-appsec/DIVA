package com.demandware.vulnapp.user;

/**
 * When a bad login happens, report what went wrong
 * @author Chris Smith
 *
 */
public enum AccountStatus {
	USERNAME_NOT_FOUND("Username could not be found"),
	PASSWORD_NOT_FOUND("Password is not correct"),
	USERNAME_EXISTS("Username already exists"), 
	USERNAME_BLANK("Username cannot be blank");
	
	private final String message;
	private AccountStatus(String message){
		this.message = message;
	}
	
	public String getMessage(){
		return this.message;
	}
}
