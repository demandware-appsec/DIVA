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
