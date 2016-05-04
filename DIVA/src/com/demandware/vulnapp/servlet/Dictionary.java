package com.demandware.vulnapp.servlet;


/**
 * Large list of random statics to hold data from non-challenge maps
 * 
 * @author Chris Smith
 *
 */
public class Dictionary {
	
	/*
	 * Parameter/HTTP Keys
	 */
	/**Parameter Name for Challenge ID*/
	public static final String CHALLENGE_PARAM = "challenge_id";
	
	/**Parameter Name for DIVA session*/
	public static final String COOKIE_SESSION_PARAM = "DIVA_token";
	
	/**Parameter Name for flag*/
	public static final String FLAG_ID_PARAM = "flag_id";
	
	/**Parameter name for session clearing*/
	public static final String CLEAR_SESSION_PARAM = "session_clear";

	/**Parameter Name for deleting user*/
	public final static String DELETE_PARAM = "deleteme";

	/*
	 * Object Keys
	 */
	
	/**Holds SessionStorage object*/
	public static final String SESSION_STORE_OBJ = "session_storage";

	/**Holds Checksum for the current challenge*/
	public static final String CHECKSUM_OBJ = "challenge_checksum";
	
	/**Holds ChallengeInfo object*/
	public static final String CURRENT_CHALLENGE_INFO_OBJ = "current_challenge_info";
	
	/**Holds Flag String object*/
	public static final String FLAG_VALUE = "flag_value";
	
	/**Holds AbstractChallenge object of the current challenge*/
	public static final String CURRENT_CHALLENGE_OBJ = "current_challenge";
	
	/**reports login issues*/
	public static final String LOGIN_PROBLEM = "login_problem";
	
	public static final String UPDATE_STATUS = "update_status";
	
	/*
	 * App Keys
	 */
	
	/**Key to receive the File path of the servlet context root*/
	public static final String SERVLET_ROOT = "servlet_root";
	
	/**Key for the root directory name of the actual "live" challenges*/
	public static final String CHALLENGES_ROOT = "challenges_root";
	
	/**Storage location in WebContent of challenges before their names are obfuscated*/
	public static final String READABLE_JSP_LOC = "readable_path";

	/**Storage folder for User Restore List*/
	public static final String RESTORE_FOLDER = "restore_folder";

	/**Last time server came up*/
	public static final String LAST_RESTART = "last_update";
	
	/*
	 * Prop File Keys 
	 */
	
	/**Key to receive the RIle path to the root of tomcat*/
	public static final String TOMCAT_ROOT = "tomcat_root";
	
	/**Name of the generated war file, including extension*/
	public static final String WAR_NAME = "war_name";

	/**Name for the debug user generator*/
	public static final String DEBUG_USER = "debug_user";


}
