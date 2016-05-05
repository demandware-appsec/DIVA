package com.demandware.vulnapp.challenge;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.demandware.vulnapp.challenge.impl.ChallengeFactory;
import com.demandware.vulnapp.challenge.impl.ChallengeFactory.ChallengeType;
import com.demandware.vulnapp.flags.FlagManager;
import com.demandware.vulnapp.servlet.DIVAServletRequestWrapper;
import com.demandware.vulnapp.servlet.Dictionary;
import com.demandware.vulnapp.servlet.DivaApp;
import com.demandware.vulnapp.sessionmgmt.SessionStorage;
import com.demandware.vulnapp.user.User;
import com.demandware.vulnapp.util.Helpers;
import com.demandware.vulnapp.util.exception.SetupRuntimeException;

/**
 * The meat of setup, ChallengePlan configures the order of challenges and sets them up by 
 * copying them from a readable format to their obfuscated naming convention.
 * 
 * 
 * @author Chris Smith
 *
 */
public class ChallengePlan {
	//Forced to ArrayList to maintain order
	private ArrayList<ChallengeInfo> challenges = new ArrayList<ChallengeInfo>();
	private Map<Difficulty, ArrayList<ChallengeInfo>> difficultyChallenges = new HashMap<Difficulty, ArrayList<ChallengeInfo>>();
	private Map<Difficulty, Integer> difficultyThresholds = new HashMap<Difficulty, Integer>();
	
	private static final ChallengePlan instance = new ChallengePlan();
	private static final String CHALLENGE_NAME = "challenge";
	
	public static ChallengePlan getInstance(){
		return instance;
	}
	
	
	/**
	 * sets up challenge ordering
	 */
	public void setupChallengePlan(){
		createChallengeMapping();
		setupDifficultyThresholds();
	}
	
	private static int ID = 0;
	
	private void createChallengeMapping(){
		//					actual ID in "readable dir"		override link name		ChallengeType designation	difficulty
		addChallengeMapping("debug_challenge", 				null,					ChallengeType.DEBUG, 		Difficulty.STARTER);
	
		addChallengeMapping("validation_challenge", 		null,					ChallengeType.VALIDATION,	Difficulty.EASY);
		addChallengeMapping("rfi_challenge", 				null,					ChallengeType.RFI,			Difficulty.EASY);
		addChallengeMapping("cookie_challenge", 			null,					ChallengeType.COOKIES,		Difficulty.EASY);
		addChallengeMapping("log_injection_challenge",		null,					ChallengeType.LOGINJECTION,	Difficulty.EASY);
		addChallengeMapping("hard_code_password_challenge",	null,					ChallengeType.HARDCODE,		Difficulty.EASY);
		addChallengeMapping("user_agent_challenge",			null,					ChallengeType.USERAGENT,	Difficulty.EASY);
		
		addChallengeMapping("xxe_challenge", 				null,					ChallengeType.XXE,			Difficulty.MEDIUM);
		addChallengeMapping("sqli_challenge", 				null,					ChallengeType.SQLI,			Difficulty.MEDIUM);
		addChallengeMapping("xss_challenge",			 	null,					ChallengeType.XSS,			Difficulty.MEDIUM);
		addChallengeMapping("null_byte_challenge",			null,					ChallengeType.NULL,			Difficulty.MEDIUM);
		addChallengeMapping("timing_challenge",				null,					ChallengeType.TIMING,		Difficulty.MEDIUM);
		
		addChallengeMapping("md5_crack_challenge", 			null,					ChallengeType.MD5,			Difficulty.HARD);//must be first "hard" since source given up
		addChallengeMapping("hidden_challenge", 			"supersecretchallenge", ChallengeType.HIDDEN,		Difficulty.HARD);
		addChallengeMapping("dynamic_compiler_challenge", 	null,					ChallengeType.COMPILER,		Difficulty.HARD);
		addChallengeMapping("entropy_challenge",			null,					ChallengeType.ENTROPY,		Difficulty.HARD);
		addChallengeMapping("cmd_injection_challenge", 		null,					ChallengeType.COMMAND,		Difficulty.HARD);
		
		addChallengeMapping("rng_challenge", 				null,					ChallengeType.RNG,			Difficulty.VHARD);
		addChallengeMapping("ecb_oracle_challenge", 		null,					ChallengeType.ORACLE,		Difficulty.VHARD);
		
		addChallengeMapping("unfinished_challenge",			"unfinished",			ChallengeType.UNFINISHED,	Difficulty.IMPOSSIBLE);		
	}
	
	/**
 	 * creates ChallengeInfo for this challenge and copies file from readable to obfuscated state.
	 * 
	 * @param challenge String to display to users (should be unique)
	 * @param pathName path to readable challenge (assumes in readable folder)
	 * @param linkValue once moved to obfuscated state, this is the name of the jsp
	 * @param type ChallengeType of this challenge
	 * @param initAllowed should this challenge be shown to users as available from the get-go
	 */
	private void addChallengeMapping(String pathName, String linkValue, ChallengeType type, Difficulty difficulty){
		String challenge = CHALLENGE_NAME + (++ID);
		if(linkValue == null){
			linkValue = Helpers.md5(challenge);
		}
		copyFile(pathName, linkValue);
		ChallengeInfo cInfo = new ChallengeInfo(challenge, linkValue, type, difficulty);
		this.challenges.add(cInfo);
		
		if(this.difficultyChallenges.containsKey(difficulty)){
			ArrayList<ChallengeInfo> infos = this.difficultyChallenges.get(difficulty);
			infos.add(cInfo);
		} else {
			ArrayList<ChallengeInfo> infos = new ArrayList<ChallengeInfo>();
			infos.add(cInfo);
			this.difficultyChallenges.put(difficulty, infos);
		}
	}
	
	private void setupDifficultyThresholds(){
		int threshold = 0;
		this.difficultyThresholds.put(Difficulty.STARTER, threshold);
		
		//must complete one challenge to unlock EASY
		int starters = this.difficultyChallenges.get(Difficulty.STARTER).size();
		threshold = starters;			
		this.difficultyThresholds.put(Difficulty.EASY, threshold);
		
		//must complete all previous but two challenges to unlock MEDIUM
		int easys = this.difficultyChallenges.get(Difficulty.EASY).size();
		threshold = starters + easys - 2;	
		this.difficultyThresholds.put(Difficulty.MEDIUM, threshold);
		
		//must complete all previous but two challenges to unlock HARD
		int mediums = this.difficultyChallenges.get(Difficulty.MEDIUM).size();
		threshold = starters + easys + mediums - 2;	
		this.difficultyThresholds.put(Difficulty.HARD, threshold);
		
		//must complete all previous challenges to unlock VHARD
		int hards = this.difficultyChallenges.get(Difficulty.HARD).size();
		threshold = starters + easys + mediums + hards;	
		this.difficultyThresholds.put(Difficulty.VHARD, threshold);
		
		int vhard = this.difficultyChallenges.get(Difficulty.VHARD).size();
		threshold = starters + easys + mediums + hards + vhard;	
		this.difficultyThresholds.put(Difficulty.IMPOSSIBLE, threshold);
	}
	
	public Integer getDifficultyThreshold(Difficulty diff){
		return this.difficultyThresholds.get(diff);
	}
	
	/**
	 * method to copy files from a readable format to an obfuscated name
	 */
	private void copyFile(String fromPath, String toFileName){
		String base = DivaApp.getInstance().getInformation(Dictionary.SERVLET_ROOT);
		String challPath = DivaApp.getInstance().getInformation(Dictionary.CHALLENGES_ROOT);
		String readablePath = DivaApp.getInstance().getInformation(Dictionary.READABLE_JSP_LOC);
		
		if(!fromPath.contains(".")){
			fromPath += ".jsp";
		}
		if(!toFileName.contains(".")){
			toFileName += ".jsp";
		}
		
		File from = Paths.get(base, readablePath, fromPath).toFile();
		File to = Paths.get(base, challPath, toFileName).toFile();
		
		from.getParentFile().mkdir();
		to.getParentFile().mkdir();
		
		if(!from.exists()){
			throw new SetupRuntimeException("Cannot find challenge jsp to copy: " + from.getAbsolutePath());
		}
		
		try {
			FileUtils.copyFile(from, to);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @return unmodifiable challenges list
	 */
	public List<ChallengeInfo> getChallenges(){
		return Collections.unmodifiableList(this.challenges);
	}
	
	/**
	 * Given a current challenge, find the next challenge(s) in the list and return it
	 */
	private Map<Difficulty, ArrayList<ChallengeInfo>> getNextChallenges(User user){
		Map<Difficulty, ArrayList<ChallengeInfo>> cInfos = new LinkedHashMap<Difficulty,ArrayList<ChallengeInfo>>();
		
		int completedChalls = user.getCompletedChallenges().size();
		for(Difficulty diff : this.difficultyThresholds.keySet()){
			int threshold = this.difficultyThresholds.get(diff);
			if(completedChalls >= threshold && this.difficultyChallenges.containsKey(diff)){
				ArrayList<ChallengeInfo> cInfoList = new ArrayList<ChallengeInfo>(this.difficultyChallenges.get(diff));
				cInfos.put(diff, cInfoList);
			}
		}
		
		return cInfos;
	}
	
	public List<ChallengeInfo> getChallengeForDifficulty(Difficulty diff){
		return this.difficultyChallenges.get(diff);
	}
	
	
	/**
	 * Given a challenge name, return the associated ChallengeInfo item
	 */
	private ChallengeInfo getChallengeForName(String name){
		for(ChallengeInfo c : this.challenges){
			if(c.getName().equals(name)){
				return c;
			}
		}
		return null;
	}
	
	/**
	 * Given a challenge type, return the associated ChallengeInfo
	 */
	public ChallengeInfo getChallengeForType(ChallengeType type){
		for(ChallengeInfo c : this.challenges){
			if(c.getChallengeType().equals(type)){
				return c;
			}
		}
		return null;
	}
	
	/**
	 * Given a challenge's linkValue (the obfuscated file name), return the associated ChallengeInfo
	 */
	public ChallengeInfo getChallengeForLinkValue(String linkValue){
		for(ChallengeInfo c : this.challenges){
			if(c.getLinkValue().equals(linkValue)){
				return c;
			}
		}
		return null;
	}
	
	/**
	 * Given a user-friendly challenge name and session object, return true if the challenge is complete
	 * for this session, false otherwise
	 */
	public boolean isChallengeComplete(String challengeKey, SessionStorage store){
		ChallengeInfo cInfo = getChallengeForName(challengeKey);
		ChallengeType type = cInfo.getChallengeType();
		return store.getUser().isComplete(type);
	}

	/**
	 * @return List of ChallengeTypes that are set to be shown to a new user
	 */
	public List<ChallengeType> getInitialAllowedChallenges() {
		List<ChallengeType> cs = new ArrayList<ChallengeType>();
		for(ChallengeInfo c : this.challenges){
			if(c.getDifficulty().equals(Difficulty.STARTER)){
				cs.add(c.getChallengeType());
			}
		}
		return cs;
	}
	
	
	public class UpdateStatus{
		public boolean updated;
		public boolean badFlag;
		public List<Difficulty> unlockedDiff;
		public String completedChallenge;
	}
	
	
	/**
	 * updates a session's storage values if the flag given matches the expected flag
	 * 
	 * @return UpdateStatus containing info on this process
	 */
	public UpdateStatus updateChallengeIfCorrect(DIVAServletRequestWrapper req){
		SessionStorage sessionStore = (SessionStorage)req.getInformation(Dictionary.SESSION_STORE_OBJ);
		UpdateStatus upStat = new UpdateStatus();
		upStat.updated = false;
		upStat.badFlag = true;
		
		String incomingFlag = req.getParameter(Dictionary.FLAG_ID_PARAM);
		if(!StringUtils.isBlank(incomingFlag)){
			List<ChallengeInfo> cInfos = ChallengePlan.getInstance().getChallenges();
			for(ChallengeInfo cInfo : cInfos){
				if(FlagManager.getInstance().isCorrectFlag(sessionStore, cInfo, incomingFlag)){
					List<Difficulty> unlocked = markCompleteAndUpdateChallenges(sessionStore.getUser(), cInfo);
					upStat.unlockedDiff = unlocked;
					upStat.updated = true;
					upStat.badFlag = false;
					upStat.completedChallenge = ChallengeFactory.getInstance().getChallenge(cInfo.getChallengeType()).getName();
					break;
				} else{
					upStat.updated = true;
					upStat.badFlag = true;
				}
			}
		}

		return upStat;
	}
	
	/**
	 * labels a given challenge as complete and grants access to any new challenges that have been unlocked
	 */
	public List<Difficulty> markCompleteAndUpdateChallenges(User user, ChallengeInfo cInfo){
		List<Difficulty> unlocked = new ArrayList<Difficulty>();
		user.markComplete(cInfo.getChallengeType());
		Map<Difficulty, ArrayList<ChallengeInfo>> nextCInfoMap = ChallengePlan.getInstance().getNextChallenges(user);
		for(Difficulty diff : nextCInfoMap.keySet()){
			List<ChallengeInfo> nextCInfos = nextCInfoMap.get(diff);
			if(nextCInfos != null && nextCInfos.size() > 0){
				boolean worked = false;
				for(ChallengeInfo nextCInfo : nextCInfos){
					worked |= user.grantAccess(nextCInfo.getChallengeType());
				}
				if(worked){
					unlocked.add(diff);
				}
			}
		}
		return unlocked;
	}
	
	/**
	 * correctly cleanup all challengeplan info
	 */
	public void teardownChallenges() {
		String base = DivaApp.getInstance().getInformation(Dictionary.SERVLET_ROOT);
		String challPath = DivaApp.getInstance().getInformation(Dictionary.CHALLENGES_ROOT);
		
		File challDir = Paths.get(base, challPath).toFile();
		if(challDir.isDirectory()){
			try {
				FileUtils.deleteDirectory(challDir);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
}
