package com.demandware.vulnapp.user;

import java.io.File;

/**
 * Writes user state to local file system regularly
 * @author Chris Smith
 *
 */
public class UserBackup implements Runnable{

	private final File restoreFolder;

	UserBackup(File restoreFolder){
		this.restoreFolder = restoreFolder;
	}

	@Override
	public void run() {
		if(this.restoreFolder != null){
			UserStorage.backupUsers(this.restoreFolder);
		}
	}
}
