package com.demandware.vulnapp.challenge.impl;

import java.io.File;
import java.io.IOException;

import com.demandware.vulnapp.challenge.AbstractChallenge;
import com.demandware.vulnapp.servlet.DIVAServletRequestWrapper;
import com.demandware.vulnapp.servlet.Dictionary;
import com.demandware.vulnapp.servlet.DivaApp;
import com.demandware.vulnapp.util.Helpers;

/**
 * user can read several hacker zines, or choose flag file to include it in the page. 
 * 
 * @author Chris Smith
 */
public class RFIChallenge extends AbstractChallenge {
	private File baseChallengePath;
	private static final String CHALL_FOLDER = "RFIFiles";
	
	public static final String PHRACK = "phrack.txt";
	public static final String WHAT_IS = "what_is_hacker.txt";
	public static final String HACKER_ATT = "hacker_attitude.txt";
	public static final String FLAG_FILE = "flag.txt";
	public static final String FILE_PARAM = "file";

	protected RFIChallenge(String name) {
		super(name);
		String basedir = DivaApp.getInstance().getInformation(Dictionary.SERVLET_ROOT);
		this.baseChallengePath = new File(basedir, CHALL_FOLDER);
	}
	
	public String handleChallengeRequest(DIVAServletRequestWrapper req){
		String requested = req.getParameter(FILE_PARAM);
		String contents = "";

		if(requested != null){

			if(requested.equalsIgnoreCase(FLAG_FILE)){
				contents = "Flag: " + (String) req.getInformation(Dictionary.FLAG_VALUE);

			} else{
				try{
					File f = new File(this.baseChallengePath, requested);
					if(isGoodFile(f)){
						contents = Helpers.readFromFile(f);
					} else{
						contents = "File is not an allowed file";
					}
				} catch(IOException e){
					contents = "Could not find file";
				} 
			}
		}
		return contents;
	}
	
	/**
	 * only allow RFI in the given directory
	 * return true if the requested file is in the allowed dir
	 */
	private boolean isGoodFile(File f) throws IOException {
		File test = f.getCanonicalFile().getAbsoluteFile();
		return test.getAbsolutePath().startsWith(this.baseChallengePath.getAbsolutePath());
	}

}