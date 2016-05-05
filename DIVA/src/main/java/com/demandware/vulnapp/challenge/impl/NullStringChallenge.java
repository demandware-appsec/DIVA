package com.demandware.vulnapp.challenge.impl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import com.demandware.vulnapp.challenge.AbstractChallenge;
import com.demandware.vulnapp.servlet.DIVAServletRequestWrapper;
import com.demandware.vulnapp.servlet.Dictionary;
import com.demandware.vulnapp.servlet.DivaApp;
import com.demandware.vulnapp.util.Helpers;


/**
 * Inject a null byte into the query to remove the .txt extension from
 * the customer string parser
 * 
 * @author Chris Smith
 *
 */
public class NullStringChallenge extends AbstractChallenge{
	private File baseChallengePath;
	private static final String CHALL_FOLDER = "RFIFiles";
	
	public static final String PHRACK = "phrack.txt";
	public static final String WHAT_IS = "what_is_hacker.txt";
	public static final String HACKER_ATT = "hacker_attitude.txt";
	public static final String FLAG_FILE = "flag";
	public static final String FILE_PARAM = "file";
	
	
	protected NullStringChallenge(String name) {
		super(name);
		String basedir = DivaApp.getInstance().getInformation(Dictionary.SERVLET_ROOT);
		this.baseChallengePath = new File(basedir, CHALL_FOLDER);
	}

	@Override
	public String handleChallengeRequest(DIVAServletRequestWrapper req) {
		String requested = req.getParameter(FILE_PARAM);
		String contents = "";

		if(requested != null){
			
			if(!requested.endsWith(".txt")){
				contents = "file must end with .txt";
			} else if(isNullByteSame(requested, FLAG_FILE)){
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
	
	private boolean isNullByteSame(String requested, String flagFile) {
		byte[] req = canonicalizeBytes(requested.getBytes());
		byte[] flag = canonicalizeBytes(flagFile.getBytes());
		if(req == null){
			return false;
		}
		boolean isSame = Arrays.equals(req, flag);
		return isSame;
	}
	
	private byte[] canonicalizeBytes(byte[] bytes){
		if(bytes == null){
			return null;
		}
		byte[] canon;
		int canonlen = bytes.length;
		for(int i = 0; i < bytes.length; i++){
			if(bytes[i]==(byte)0){
				canonlen = i;
				break;
			}
		}
		canon = new byte[canonlen];
		System.arraycopy(bytes, 0, canon, 0, canonlen);
		return canon;
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