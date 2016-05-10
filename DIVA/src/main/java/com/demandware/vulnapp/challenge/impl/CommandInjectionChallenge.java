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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;

import com.demandware.vulnapp.challenge.AbstractChallenge;
import com.demandware.vulnapp.servlet.DIVAServletRequestWrapper;
import com.demandware.vulnapp.servlet.Dictionary;


/**
 * simple bash injection attack. flag is contained within the command sent.
 * 
 * @author Chris Smith
 *
 */
public class CommandInjectionChallenge extends AbstractChallenge {
	public static final String PING_PARAM = "ping";
	public static final long DEFAULT_TIMEOUT = 5L;

	protected CommandInjectionChallenge(String name) {
		super(name);
	}

	/**
	 * @return null if the request doesn't contain the param, or a message containing the output of the command
	 */
	public String handleChallengeRequest(DIVAServletRequestWrapper req){
		String output = "";
		String pingTarget = req.getParameter(PING_PARAM);
		if(pingTarget != null){
			String command = generateCommand(req, pingTarget);
			output = getCommandOutput(command);
		}

		return output;
	}

	private String generateCommand(DIVAServletRequestWrapper req, String target) {
		StringBuilder sb = new StringBuilder();

		sb.append("export FLAG=\"Flag: ");
		String flag = (String)req.getInformation(Dictionary.FLAG_VALUE);
		sb.append(flag);
		sb.append("\" && ping -c 3 ");
		sb.append(target);

		return sb.toString();
	}

	private String getCommandOutput(String command){
		String output = null;
		try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()){
		    CommandLine cmd = new CommandLine("/bin/bash");
		    String[] args = new String[] {"-c", command};
		    cmd.addArguments(args,false);
			PumpStreamHandler psh = new PumpStreamHandler(outputStream);
			
			DefaultExecutor exec = new DefaultExecutor();
			exec.setStreamHandler(psh);
			exec.execute(cmd);
			output = outputStream.toString();
		} catch (ExecuteException e) {
			e.printStackTrace();
			output = "Could not execute command";
		} catch (IOException e) {
			e.printStackTrace();
		}
		return output;
	}
}
