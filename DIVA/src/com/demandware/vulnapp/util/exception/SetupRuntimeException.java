package com.demandware.vulnapp.util.exception;

/**
 * Thrown whenever the app encounters runtime problems during setup
 * 
 * @author Chris Smith
 *
 */
public class SetupRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 4913639409030918786L;
	
	public SetupRuntimeException(String message){
		super(message);
	}

	public SetupRuntimeException(Exception e) {
		super(e);
	}

	public SetupRuntimeException(String string, Exception e) {
		super(string,e);
	}
}
