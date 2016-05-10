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
