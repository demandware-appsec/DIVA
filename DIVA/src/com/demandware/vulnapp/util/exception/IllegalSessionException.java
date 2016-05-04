package com.demandware.vulnapp.util.exception;

/**
 * Thrown if we catch any session attacks or misuse
 * @author Chris Smith
 *
 */
public class IllegalSessionException extends Exception {

	private static final long serialVersionUID = -4001550277399692548L;
	
	public IllegalSessionException(String string) {
		super(string);
	}
}
