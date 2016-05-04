package com.demandware.vulnapp.challenge.impl;

import com.demandware.vulnapp.challenge.AbstractChallenge;
import com.demandware.vulnapp.servlet.DIVAServletRequestWrapper;
import com.demandware.vulnapp.servlet.Dictionary;
import com.demandware.vulnapp.user.password.BCrypt;
import com.demandware.vulnapp.util.SecureRandomUtil;


/**
 * Robin Hood and Little John need to execute a bank transfer 
 * from Prince John using his username and a PIN username is given, 
 * PIN must be guessed. slow string comparator allows a timing attack
 * 
 * @author Chris Smith
 *
 */
public class TimingChallenge extends AbstractChallenge{
	
	public static final String UNAME_PARAM = "remoteusername";
	public static final String PIN_PARAM = "pin";
	
	public static final Integer PIN_SIZE = 5; 
	public static final String[] possibleUserNames = new String[]{"pjohn", "princejohn", "kingjohn", "johnlackland", "jlackland"};

	private final String chosenUserName;
	private final String realPin;
	
	protected TimingChallenge(String name) {
		super(name);
		this.chosenUserName = possibleUserNames[SecureRandomUtil.nextInt(possibleUserNames.length)];
		this.realPin = generatePin(PIN_SIZE);
	}

	@Override
	public String handleChallengeRequest(DIVAServletRequestWrapper req) {
		String message = "";
		String uname = req.getParameter(UNAME_PARAM);
		String pin = req.getParameter(PIN_PARAM);
		
		if(uname != null && pin != null){
			if(stringCompare(uname, chosenUserName)){
				if(stringCompare(pin, realPin)){
					message = "Bank Transfer Successful.\nFlag: " + req.getInformation(Dictionary.FLAG_VALUE);
				} else{
					message = "Error while making transfer request";
				}
			} else{
				message = "Error while making transfer request";
			}
		}
		return message;
	}
	
	
	private boolean stringCompare(String str1, String str2){
		boolean isSame = true;
		if(str1.length() != str2.length()){
			isSame = false;
		} else{
			BCrypt bc = BCrypt.getInstance();
			
			for(int i = 0; i < str1.length(); i++){
				Byte b1 = (byte) str1.charAt(i);
				Byte b2 = (byte) str2.charAt(i);
				if(b1.equals(b2)){
					String s1 = str1.substring(0, i+1);
					String s2 = str2.substring(0, i+1);
					if(!bc.verifyPW(s1, bc.hash(s2, 10))){
						isSame = false;
						break;
					}
				}
				else{
					isSame = false;
					break;
				}
			}
		}
		return isSame;
	}
	
	//generate an n digit string
	private static String generatePin(int n){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < n; i++){
			sb.append(String.valueOf(SecureRandomUtil.nextInt(10)));
		}
		return sb.toString();
	}

}