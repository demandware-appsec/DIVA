package com.demandware.vulnapp.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Collection of helper methods
 * 
 * @author Chris Smith
 *
 */
public class Helpers {

	private static final String SEP = System.getProperty("line.separator");

	public static boolean isBlank(String str){
		return str == null || str.isEmpty();
	}
	
	/**
	 * Simple MD5, no salt
	 * 
	 * @param input String to hash
	 * @return hex encoded hash
	 */
	public static String md5(String input){
		return DigestUtils.md5Hex(input);
	}

	/**
	 * SHA-1 with optional salt 
	 * 
	 * @param input String to hash
	 * @param salt bytes to use as salt for sha
	 * @return hex encoded hash
	 */
	public static String sha(String input, byte[] salt){
		String ret = "";
		try{
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			digest.reset();
			if(salt != null){
				digest.update(salt);
			}
			ret = Hex.encodeHexString(digest.digest(input.getBytes("UTF-8")));
		} catch(Throwable t){
			//yeah this is bad... but there's no way to get either of the exceptions
			throw new RuntimeException(t);
		}
		return ret;
	}

	/**
	 * pick a random integer inclusive of min, exclusive of max
	 */
	public static int randomBetween(int min, int max) {
		int lower = min;
		int upper = max;
		if(lower>upper){
			int tmp = lower;
			lower = upper;
			upper = tmp;
		}
		return SecureRandomUtil.nextInt(upper - lower) + lower;
	}

	/**
	 * Removes params and Context 
	 * 
	 * @param urlString full URL/URI
	 * @return page, inclusive of extensions
	 */
	public static String extractPageNameFromURLString(String urlString){
		if (urlString==null){
			return null;
		}
		int lastSlash = urlString.lastIndexOf("/");
		String pageAndExtensions = urlString.substring(lastSlash+1);
		int lastQuestion = pageAndExtensions.lastIndexOf("?");
		if (lastQuestion==-1){
			lastQuestion = pageAndExtensions.length();
		}
		String pageNoParams = pageAndExtensions.substring(0,lastQuestion);
		int lastdot = pageNoParams.lastIndexOf(".");
		String result;
		if(lastdot>-1){
			result = pageNoParams.substring(0, lastdot);
		} else{
			result = pageNoParams;
		}
		return result;
	}

	/**
	 * A "Ballpark true" guess
	 * 
	 * @return true if obj exists, is a boolean true, is a string "on" or an integer greater than 0, false otherwise
	 */
	public static boolean isTruthy(Object obj) {
		if(obj == null){
			return false;
		}
		String val = String.valueOf(obj);
		try{
			if(Boolean.valueOf(val)){
				return true;
			}
		} catch(Exception e){}
		try{
			if(val.equalsIgnoreCase("on")){
				return true;
			}
		} catch(Exception e){}
		try{
			if(Integer.parseInt(val)>0){
				return true;
			}
		} catch(Exception e){}

		return false;
	}

	/**
	 * Given a file, return the file's contents. 
	 * 
	 * Note that this does not buffer the string itself, so large files will cause problems.
	 * @throws IOException 
	 */
	public static String readFromFile(File f) throws IOException{
		String contents = null;
		BufferedReader br = new BufferedReader(new FileReader(f));
		StringBuilder sb = new StringBuilder();
		String line = "";
		while ((line = br.readLine()) != null){
			sb.append(line).append(SEP);
		}
		contents = sb.toString();
		br.close();
		return contents;
	}

	/**
	 * Given a file and contents, write the contents to the file. No checking is done. 
	 */
	public static void writeToFile(File f, String contents){
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(f))){
			bw.write(contents);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * given a root dir, search its immediate subdirectory for a file by name
	 * 
	 * @return file if found, null otherwise
	 */
	public static File getFileOrFolderInSubDir(File root, String name, boolean isFile){
		File search = new File(root, name);
		if(search.exists()){
			if( (isFile && search.isFile()) || (!isFile && search.isDirectory()) ){
				return search;
			}
		}

		return null;
	}

	/**
	 * this is a terrible HTML Entity encoder. it does a bare minimum job.
	 * @return an HTML encoded string of the input string
	 */
	public static String cheapoHTMLEntityEncode(String enc){
		if(enc == null){
			return null;
		}
		StringBuilder sb = new StringBuilder(enc.length());
		for(int i = 0; i < enc.length(); i++){
			char c = enc.charAt(i);
			switch(c){
			case '<':
				sb.append("&lt;");
				break;
			case '>':
				sb.append("&gt;");
				break;
			case '&':
				sb.append("&amp;");
				break;
			default:
				sb.append(c);
				break;
			}
		}
		return sb.toString();
	}

	/**
	 * this is a terrible HTML Filter. it does a bare minimum job.
	 * @return an HTML entity filtered string of the input string
	 */
	public static String cheapoHTMLFilter(String input){
		if(input == null){
			return null;
		}
		StringBuilder sb = new StringBuilder(input.length());
		for(int i = 0; i < input.length(); i++){
			char c = input.charAt(i);
			switch(c){
			case '<':
				break;
			case '>':
				break;
			case '&':
				break;
			default:
				sb.append(c);
				break;
			}
		}
		return sb.toString();
	}

	/**
	 * this is a ctf, therefore rot13.
	 * 
	 * @return rot-13 representation of the given string
	 */
	public static String rot13(String string) {
		StringBuilder sb = new StringBuilder(string.length());
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			if (c >= 'a' && c <= 'm'){
				c += 13;
			} else if  (c >= 'A' && c <= 'M'){ 
				c += 13;
			} else if  (c >= 'n' && c <= 'z'){
				c -= 13;
			} else if  (c >= 'N' && c <= 'Z'){
				c -= 13;
			}
			sb.append(c);
		}
		return sb.toString();
	}

	/**
	 * Given a root dir and a file name, search all subdirectories for the file. 
	 * 
	 * @return null if root or name is null, or if the file is not found. the found file otherwise 
	 */
	public static File findFile(File root, String name) {
		if(root == null || name == null){
			return null;
		}
		File foundFile = null;
		Queue<File> filesAndDirs = new LinkedList<File>();
		filesAndDirs.add(root);
		while(!filesAndDirs.isEmpty() && foundFile == null){
			File file = filesAndDirs.poll();
			if(file.isDirectory()){
				File[] files = file.listFiles();
				if(files != null){
					for(File f : files){
						filesAndDirs.add(f);
					}
				}
			} else{
				if(file.getName().equals(name)){
					foundFile = file;
				}
			}
		}
		return foundFile;
	}

	/**
	 * Given a filename, return its name without an extension
	 */
	public static String getFileNameWithoutExtension(String fileName){
		String fname = fileName;
		int pos = fname.lastIndexOf(".");
		if (pos > 0) {
			fname = fname.substring(0, pos);
		}
		return fname;
	}
	
	public static String getFileExtension(String fileName){
		String fname = fileName;
		int pos = fname.lastIndexOf(".");
		if (pos > 0) {
			fname = fname.substring(pos+1, fname.length());
		}
		return fname;
	}

	/**
	 * safely transforms a long to an Integer. If the value is greater than 
	 * int max, it is set to int max, if less than int min, it is set to int min,
	 * otherwise it is directly cast.
	 */
	public static int safeCastToInt(long longVal){
		int intVal = 0;
		if(longVal < Integer.MIN_VALUE){
			intVal = Integer.MIN_VALUE;
		} else if(longVal > Integer.MAX_VALUE){
			intVal = Integer.MAX_VALUE;
		} else{
			intVal = (int) longVal;
		}
		return intVal;
	}

	public static String base64Encode(String string){
		return new String(Base64.encodeBase64(string.getBytes()));
	}

	public static String base64Decode(String b64String){
		return new String(Base64.decodeBase64(b64String.getBytes()));
	}

	public static File[] lastFileModifiedByExt(File dir, String ext) {
		File[] files = dir.listFiles(new FileFilter() {          
			public boolean accept(File file) {
				return file.isFile() && Helpers.getFileExtension(file.getName()).equals(ext);
			}
		});

		Arrays.sort(files, new Comparator<File>(){
		    public int compare(File f1, File f2)
		    {
		        return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
		    } 
		});
		
		return files;
	}
	
	public static String fill(char s, int i){
		if (i > 0) {
			char[] array = new char[i];
			Arrays.fill(array, s);
			return new String(array);
		}
		return "";
	}
}
