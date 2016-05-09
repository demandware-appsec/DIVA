package com.demandware.vulnapp.challenge.impl;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.demandware.vulnapp.challenge.AbstractChallenge;
import com.demandware.vulnapp.servlet.DIVAServletRequestWrapper;
import com.demandware.vulnapp.servlet.Dictionary;
import com.demandware.vulnapp.servlet.DivaApp;
import com.demandware.vulnapp.util.Helpers;


/**
 * A two-parter. MD5 is found by figuring out the challenge link pattern.
 * This class also finds and allows users to download the war file for this webapp.
 * 
 * @author Chris Smith
 *
 */
public class MD5Challenge extends AbstractChallenge {
	private File b64WarLocation = null;
	private boolean b64WarMade = false;
	
	private static final String MD5_FILE_NAME = "unknown.txt";
	public static final String DOWNLOAD_PARAM = "download";
	private static final String CHALL_FOLDER = "MD5Files";
	
	protected MD5Challenge(String name) {
		super(name);
		String basedir = DivaApp.getInstance().getInformation(Dictionary.SERVLET_ROOT);
		File base = Paths.get(basedir, MD5Challenge.CHALL_FOLDER).toFile();
		if(!base.exists()){
			base.mkdirs();
		}
		this.b64WarLocation = new File(base, MD5_FILE_NAME );
		
		generateB64FileForWar();
	}

	public Object handleChallengeRequest(DIVAServletRequestWrapper req){
		String download = req.getParameter(DOWNLOAD_PARAM);
		if(!StringUtils.isBlank(download)){
			HttpServletResponse response = req.getResponse();
			response.setContentType("application/octet-stream");
	        response.setHeader("Content-Disposition",
	                "attachment;filename="+this.b64WarLocation.getName());

	        try(FileInputStream fileIn = new FileInputStream(this.b64WarLocation);
	        		ServletOutputStream out = response.getOutputStream()){
	        	
	        	IOUtils.copy(fileIn, out);
	        	
	        } catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * it is possible that the war file could not be found. return false if that happened
	 */
	public boolean b64FileMade(){
		return this.b64WarMade;
	}
	
	/**
	 * attempt to copy the contents of the war file to a base64 encoded text file
	 */
	private void generateB64FileForWar(){
		String troot = DivaApp.getInstance().getInformation(Dictionary.TOMCAT_ROOT);
		if(troot != null && !"".equals(troot)){
			File root = new File(troot);
			root.mkdirs();
			String warName = DivaApp.getInstance().getInformation(Dictionary.WAR_NAME);
			File war = Helpers.findFile(root, warName);
			if(war == null){
				war = Helpers.findFile(root, "DIVA.war");
			}
			System.out.println("Copying War from " + war.getAbsolutePath());
			if(war != null){
				try(InputStream is = new BufferedInputStream(new Base64InputStream(new FileInputStream(war), true)); 
					BufferedWriter bw = new BufferedWriter(new FileWriter(this.b64WarLocation))){
					
					IOUtils.copy(is, bw);
					this.b64WarMade = true;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}