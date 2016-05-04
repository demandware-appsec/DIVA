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
