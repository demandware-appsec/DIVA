package com.demandware.vulnapp.user;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

import com.demandware.vulnapp.challenge.ChallengeInfo;
import com.demandware.vulnapp.challenge.ChallengePlan;
import com.demandware.vulnapp.challenge.impl.ChallengeFactory.ChallengeType;
import com.demandware.vulnapp.servlet.Dictionary;
import com.demandware.vulnapp.servlet.DivaApp;
import com.demandware.vulnapp.user.password.BCrypt;
import com.demandware.vulnapp.util.Helpers;
import com.demandware.vulnapp.util.SecureRandomUtil;
import com.demandware.vulnapp.util.exception.AccountException;

/**
 * Controls user generation, lookup, and logins
 * 
 * @author Chris Smith
 */
public class UserManager {
	private final ConcurrentLinkedQueue<User> currentUsers;
	private final File restoreFolder;
	
	private User debugUser = null;
	
	private final static long CLEANUP_TIME_SEC = 2L*60*60; //2 hours
	private final static long BACKUP_TIME_SEC = 1L*60; //1 min
	
	private final static UserManager instance = new UserManager();

	public static UserManager getInstance(){
		return instance;
	}
	
	private UserManager(){
		this.currentUsers = new ConcurrentLinkedQueue<User>();
		String loc = DivaApp.getInstance().getInformation(Dictionary.RESTORE_FOLDER);
		if(loc.length() == 0){
			this.restoreFolder = new File("./DIVA_backup_users/");
		}else{
			this.restoreFolder = new File(loc);
		}
		System.out.println("RESU: " + this.restoreFolder);
	}

	public boolean showLogin(User user){
		return (user instanceof Guest);
	}
	
	public User searchForUser(String userName, String password) throws AccountException{
		User user = searchForUser(userName);
		if(user == null){
			throw new AccountException(AccountStatus.USERNAME_NOT_FOUND);
		}
		if(!BCrypt.getInstance().verifyPW(password, user.getPasswordHash())){
			throw new AccountException(AccountStatus.PASSWORD_NOT_FOUND);
		}
		return user;
	}
	
	public User makeNewUser(String userName, String password) throws AccountException{
		if(Helpers.isBlank(userName)){
			throw new AccountException(AccountStatus.USERNAME_BLANK);
		}
		
		User usr = searchForUser(userName);
		if(usr != null){
			throw new AccountException(AccountStatus.USERNAME_EXISTS);
		}
		String passwordHash = BCrypt.getInstance().hash(password);
		User user = new User(userName, passwordHash);
		this.currentUsers.add(user);
		return user;
	}
	
	public User searchForUser(String userName){
		User user = null;
		for(User usr : this.currentUsers){
			if(usr.getUserName().equals(userName)){
				user = usr;
				break;
			}
		}
		
		return user;
	}
	
	public void makeDebugUser() {
		boolean done = false;
		while(!done){
			String user = SecureRandomUtil.generateRandomHexString(3);
			String pass = SecureRandomUtil.generateRandomHexString(12);
			try {
				this.debugUser = makeNewUser(user, pass);
				System.out.println("Created admin user: " + user + " with password: " + pass);
				done = true;
			} catch (AccountException e) {
				System.out.println("User: " + user + " already exists?! Trying a new debug user");
			}
		}
		for(ChallengeInfo cInfo : ChallengePlan.getInstance().getChallenges()){
			if(cInfo.getChallengeType().equals(ChallengeType.RNG)){
				continue;
			}
			ChallengePlan.getInstance().markCompleteAndUpdateChallenges(this.debugUser, cInfo);
		}
	}
	
	User getDebugUser(){
		return this.debugUser;
	}
	
	private void makeChallengerUsers(){
		for(ChallengerUser usr : ChallengerUser.values()){
			usr.makeChallengerUser();
		}
	}
	
	public User makeGuestUser(){
		return new Guest();
	}
	
	public static boolean isDebugUser(User usr){
		return usr.equals(UserManager.getInstance().getDebugUser());
	}
	
	public static boolean isFakeUser(User usr) {
		return isDebugUser(usr) || isChallengerUser(usr);
	}
	
	public static boolean isChallengerUser(User usr){
		for(ChallengerUser chall : ChallengerUser.values()){
			if(usr.getUserName().equals(chall.getUserName())){
				return true;
			}
		}
		return false;
	}

	public void setupUserManager() {
		List<User> users = UserStorage.restoreUsers(this.restoreFolder);
		this.currentUsers.addAll(users);
		makeChallengerUsers();
		DivaApp.getInstance().submitScheduledTask(new UserBackup(this.restoreFolder), BACKUP_TIME_SEC, BACKUP_TIME_SEC);
		DivaApp.getInstance().submitScheduledTask(new UserCleanup(), CLEANUP_TIME_SEC, CLEANUP_TIME_SEC);
	}
	
	public int getTotalUsers(){
		return this.currentUsers.size();
	}
	
	public boolean deleteUser(User usr){
		return this.currentUsers.remove(usr);
	}
	
	ConcurrentLinkedQueue<User> getUsers(){
		return this.currentUsers;
	}
	
	User convertFromRestore(String restoreLine){
		String work = restoreLine.trim();
		String delim = work.substring(0, 1);													//first char is delim
		work = work.substring(1);
		String[] b64parts = work.split(Pattern.quote(delim));									//split to 4 parts
		User usr = null;
		try{
			usr = restoreNew(b64parts, delim);
		}catch(Exception e){
			usr = restoreOld(b64parts, delim);
		}
		
		return usr;
	}
	
	User restoreNew(String[] b64parts, String delim){
		String user = Helpers.base64Decode(b64parts[0].trim());									//b64 decode user
		String pass = Helpers.base64Decode(b64parts[1].trim());									//b64 decode pass
		User usr = new User(user, pass);
		
		String lastact = Helpers.base64Decode(b64parts[2].trim());								//b64 decode lastactivity
		String lastip = Helpers.base64Decode(b64parts[3].trim());								//b64 decode lastip
		usr.updateLastActivity(Long.parseLong(lastact));
		usr.updateIP(lastip);
		
		if(b64parts.length > 4){
			String strAllow = Helpers.base64Decode(b64parts[4].trim());							//b64 decode allowed
			List<ChallengeType> allowed = convertStringToChallengeList(strAllow, delim);		//convert allowed to Challenges
			usr.setAllowedChallenges(allowed);
		}
		if(b64parts.length > 5){
			String strComplete = Helpers.base64Decode(b64parts[5].trim());						//b64 decode complete
			List<ChallengeType> complete = convertStringToChallengeList(strComplete, delim);	//convert complete to Challenges
			usr.setCompletedChallenges(complete);
		}
		return usr;
	}
	
	User restoreOld(String[] b64parts, String delim){
		String user = Helpers.base64Decode(b64parts[0].trim());									//b64 decode user
		String pass = Helpers.base64Decode(b64parts[1].trim());									//b64 decode pass
		User usr = new User(user, pass);
		
		if(b64parts.length > 2){
			String strAllow = Helpers.base64Decode(b64parts[2].trim());							//b64 decode allowed
			List<ChallengeType> allowed = convertStringToChallengeList(strAllow, delim);		//convert allowed to Challenges
			usr.setAllowedChallenges(allowed);
		}
		if(b64parts.length > 3){
			String strComplete = Helpers.base64Decode(b64parts[3].trim());						//b64 decode complete
			List<ChallengeType> complete = convertStringToChallengeList(strComplete, delim);	//convert complete to Challenges
			usr.setCompletedChallenges(complete);
		}
		return usr;
	}
	
	String convertToRestore(User user, String delim){
		StringBuilder sb = new StringBuilder();
		sb.append(delim);			 															//first define the delimiter as the first char 
		sb.append(Helpers.base64Encode(user.getUserName())).append(delim); 						//b64 username + delim
		sb.append(Helpers.base64Encode(user.getPasswordHash())).append(delim); 					//b64 pass + delim
		sb.append(Helpers.base64Encode(user.getLastActivityLong())).append(delim);				//b64 lastActivity + delim
		sb.append(Helpers.base64Encode(user.getLastIP())).append(delim);						//b64 last ip + delim
		String allowed = convertChallengeTypesToString(user.getAllowedChallenges(), delim);		//convert allowed to a string
		sb.append(Helpers.base64Encode(allowed)).append(delim);									//b64 allowed string
		String complete = convertChallengeTypesToString(user.getCompletedChallenges(), delim);	//convert complete to a string
		sb.append(Helpers.base64Encode(complete)).append(delim);								//b64 complete string
		return sb.toString();
	}

	private String convertChallengeTypesToString(List<ChallengeType> allowedChallenges, String delim) {
		StringBuilder sb = new StringBuilder();
		for(ChallengeType t : allowedChallenges){
			sb.append(t.name()).append(delim);
		}
		return sb.toString();
	}

	private List<ChallengeType> convertStringToChallengeList(String string, String delim){
		String[] challs = string.split(Pattern.quote(delim));
		List<ChallengeType> challenges = new ArrayList<ChallengeType>();
		for(String s : challs){
			if("".equals(s)){
				continue;
			}
			ChallengeType t = ChallengeType.valueOf(s);
			challenges.add(t);
		}
		return challenges;
	}

	public void teardownUsers() {
		UserStorage.backupUsers(this.restoreFolder);
	}
}
