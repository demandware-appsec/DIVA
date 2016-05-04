package com.demandware.vulnapp.user;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.demandware.vulnapp.util.Helpers;

/**
 * In-memory storage of User information, session info, Challenge status, etc
 * 
 * @author Chris Smith
 */
public class UserStorage {

	private static final String EXT = ".txt";
	private static final String DELIM = "|";
	private static final String NEWLINE = System.getProperty("line.separator");
	private static final int MAX_BACKUPS = 5;
	
	static void backupUsers(File folder){
		boolean issueFound = false;
		if(!folder.exists()){
			folder.mkdirs();
		}
		File backupFile = new File(folder, String.valueOf(System.currentTimeMillis()) + EXT);
		ConcurrentLinkedQueue<User> users = UserManager.getInstance().getUsers();

		try(BufferedWriter bw = new BufferedWriter(new FileWriter(backupFile))){
			for(User usr : users){
				if(!UserManager.isDebugUser(usr)){
					String line = UserManager.getInstance().convertToRestore(usr, DELIM);
					bw.write(line);
					bw.write(NEWLINE);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			issueFound = true;
		}

		File[] filelist = folder.listFiles();
		if(filelist != null){
			List<File> originalFileList = Arrays.asList(filelist);

			Collections.sort(originalFileList, new Comparator<File>(){
				public int compare(File f1, File f2)
			    {
			        return Long.compare(f2.lastModified(), f1.lastModified());
			    }
			});
			
			for(int i = MAX_BACKUPS; i < originalFileList.size(); i++){
				File f = originalFileList.get(i);
				f.delete();
			}
		}
		System.out.println("Backup File: " + 
				backupFile.getAbsolutePath() + 
				(issueFound ? " not written." : " written successfully"));
	}


	static List<User> restoreUsers(File folder) {
		List<User> userList = new ArrayList<User>();
		Map<String, User> userSet = new HashMap<String, User>();
		
		if(folder != null && folder.exists()){
			boolean issueFound = false;
			File[] fs = Helpers.lastFileModifiedByExt(folder, "txt");
			if(fs == null){
				issueFound = true;
			} else{
				for(File f : fs){
					try(BufferedReader br = new BufferedReader(new FileReader(f))){
						System.out.println("Attempting restore from file: " + f.getAbsolutePath());
						if(f.length() == 0){
							continue;
						}
						String line = "";
						while((line = br.readLine()) != null){
							if(line.trim().length() > 0){
								User usr = UserManager.getInstance().convertFromRestore(line);
								if(!UserManager.isFakeUser(usr)){
									userSet.put(usr.getUserName(), usr);
								}
							}
						}
						break;
					} catch (IOException e) {
						e.printStackTrace();
						issueFound = true;
						userSet.clear();
					} finally{
						System.out.println("Restoration " + (issueFound ? "unsuccessful" : "successful"));
					}
				}	
				
				for(User u : userSet.values()){
					userList.add(u);
				}
			}
		}
		
		return userList;
	}
	
}
