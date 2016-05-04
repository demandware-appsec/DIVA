package com.demandware.vulnapp.challenge.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.demandware.vulnapp.challenge.AbstractChallenge;
import com.demandware.vulnapp.servlet.DIVAServletRequestWrapper;
import com.demandware.vulnapp.servlet.Dictionary;
import com.demandware.vulnapp.servlet.DivaApp;
import com.demandware.vulnapp.sessionmgmt.SessionStorage;
import com.demandware.vulnapp.sql.DBHolder;
import com.demandware.vulnapp.sql.SQLManager;
import com.demandware.vulnapp.util.Helpers;
import com.demandware.vulnapp.util.exception.SetupRuntimeException;


/**
 * Straightforward SQLi. get flag table, then flag. 
 * 
 * Each session has it's own DB. DBs close after 120 seconds 
 * and may live for that long (cleaned up after some time) 
 * 
 * @author Chris Smith
 *
 */
public class SQLIChallenge extends AbstractChallenge {

	public static final String SQL_QUERY = "lookup";
	public static final String SQL_RESET = "reset"; 


	private static final String CHALL_FOLDER = "SQLIFiles";
	private static final String TEXT_FOLDER = "text";
	private static final String IMG_FOLDER = "img";
	private static final int MAX_RESULTS = 20;
	private static final String FLAG_TABLE = "flag";

	private final List<SimpleEntry<String,String>> columns;

	private final String tableName;
	private final String columnKey;

	private final List<Mineral> minerals;
	private final SQLManager mgr;
	private final String mineralInsertStatement;

	protected SQLIChallenge(String name) {
		super(name);

		this.mgr = SQLManager.getInstance();

		this.columns = new ArrayList<SimpleEntry<String, String>>();
		populateColumns();

		this.columnKey = "mineral_name";
		this.tableName = "minerals";

		this.minerals = new ArrayList<Mineral>();
		addMinerals();

		this.mineralInsertStatement = this.createMineralInsertStatement();
	}

	/**
	 * creates column names and sizes8
	 */
	private void populateColumns(){
		this.columns.add(new SimpleEntry<String, String>("mineral_name", "VARCHAR(50)"));
		this.columns.add(new SimpleEntry<String, String>("blurb", "VARCHAR(65535)"));
		this.columns.add(new SimpleEntry<String, String>("mineral_pic", "BLOB"));
	}
	
	public List<SimpleEntry<String,String>> getColumnCopy(){
		return Collections.unmodifiableList(this.columns);
	}

	/**
	 * A Mineral object contains all of the information to be entered into the fake database
	 * 
	 * @author Chris Smith
	 */
	private class Mineral{
		private File pic;
		private String blurb;
		private String name;
		private Mineral(String mName, String blurb, String picFileName){
			this.name = mName;
			String basedir = DivaApp.getInstance().getInformation(Dictionary.SERVLET_ROOT);
			String base = Paths.get(basedir, SQLIChallenge.CHALL_FOLDER).toString();
			try {
				this.blurb = FileUtils.readFileToString(Paths.get(base, SQLIChallenge.TEXT_FOLDER, blurb).toFile());
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.pic = Paths.get(basedir, SQLIChallenge.CHALL_FOLDER, SQLIChallenge.IMG_FOLDER, picFileName).toFile();
		}
		public String getName(){
			return this.name;
		}
		public String getBlurb(){
			return this.blurb;
		}
		public File getPic(){
			return this.pic;
		}
	}
	
	/**
	 * creates a mineral object for each mineral pic/text combo and adds it to the internal list
	 */
	private void addMinerals(){
		String basedir = DivaApp.getInstance().getInformation(Dictionary.SERVLET_ROOT);
		String base = Paths.get(basedir, SQLIChallenge.CHALL_FOLDER).toString();
		File textFolder = Paths.get(base, SQLIChallenge.TEXT_FOLDER).toFile();
		File imgFolder = Paths.get(base, SQLIChallenge.IMG_FOLDER).toFile();
		File[] minFiles = textFolder.listFiles();
		if(minFiles == null){
			throw new SetupRuntimeException("Minerals files are not present");
		}
		for(File t : minFiles){
			if(t.isFile() && t.getName().endsWith(".txt")){
				String txtFile = Helpers.getFileNameWithoutExtension(t.getName());
				File i = new File(imgFolder, txtFile + ".jpg");
				if(i.exists()){
					String dispName = txtFile.replace("_", " ");
					this.minerals.add(new Mineral(dispName, t.getName(), i.getName()));
				} else{
					System.out.println("Could not find image for file: " + txtFile);
				}
			}
		}
	}

	/**
	 * Create a proper prepared statement for mineral inserts
	 */
	private String createMineralInsertStatement(){
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ");
		sb.append(this.tableName);
		sb.append(" (");
		String delim = "";
		for(SimpleEntry<String, String> e : this.columns){
			sb.append(delim);
			sb.append(e.getKey());
			delim = ", ";
		}
		sb.append(")");
		sb.append(" VALUES (");
		delim = "";
		for(int i = 0; i < this.columns.size(); i++){
			sb.append(delim);
			sb.append("?");
			delim = ",";
		}
		sb.append(")");
		return sb.toString();
	}

	/**
	 * destroys a session's DB and recreates it
	 * @param session session ID of current session
	 * @param flag flag ID for current session
	 */
	private void reset(String session, String flag){
		DBHolder holder = this.getHolderForSession(session);
		if(holder != null){
			try {
				dropTables(holder);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else{
			holder = this.makeHolderForSession(session);
		}
		try {
			createMinerals(holder);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			createFlag(holder, flag);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			setupContents(holder);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Given a session ID, return the DBHolder for that session, or null if it doesn't exist
	 */
	private DBHolder getHolderForSession(String session){
		return this.mgr.getDBHolder(session);
	}

	/**
	 * Given a session ID, create a new DBHolder and return it
	 */
	private DBHolder makeHolderForSession(String session){
		DBHolder holder = DBHolder.makeForSession(session);
		this.mgr.addDBHolder(holder);
		return holder;
	}

	/**
	 * Create the Mineral Table
	 * @param holder db holder to create table in
	 */
	private void createMinerals(DBHolder holder) throws SQLException{
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE ");
		sb.append(this.tableName);
		sb.append(" (");
		String delim = " ";
		for(SimpleEntry<String, String> e : this.columns){
			sb.append(delim);
			sb.append(e.getKey());
			sb.append(" ");
			sb.append(e.getValue());
			delim = ", ";
		}
		sb.append(")");
		String query = sb.toString();
		Connection conn = holder.getConnection();
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.executeUpdate();
		conn.close();
	}

	/**
	 * create the flag table and populate it
	 * @param holder db holder to create table in
	 * @param flag flag to insert
	 * @throws SQLException if something goes wrong
	 */
	private void createFlag(DBHolder holder, String flag) throws SQLException{
		createFlagTable(holder);
		insertFlagData(holder, flag);
	}

	/**
	 * build the flag table
	 * 
	 * @param holder db holder to create table for
	 * @throws SQLException
	 */
	private void createFlagTable(DBHolder holder) throws SQLException{
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE ");
		sb.append(SQLIChallenge.FLAG_TABLE);
		sb.append(" (NAME VARCHAR(64), ID VARCHAR(256))");
		String query = sb.toString();
		Connection conn = holder.getConnection();
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.executeUpdate();
		conn.close();
	}

	/**
	 * add flag data to the previously created flag table
	 * 
	 * @param holder db holder to add data to
	 * @param flag flag value to insert
	 * @throws SQLException
	 */
	private void insertFlagData(DBHolder holder, String flag) throws SQLException{
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ");
		sb.append(SQLIChallenge.FLAG_TABLE);
		sb.append(" (NAME, ID)");
		sb.append(" VALUES (?, ?)");
		String query = sb.toString();
		Connection conn = holder.getConnection();
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, "flag");
		stmt.setString(2, flag);
		stmt.executeUpdate();
		conn.close();
	}

	/**
	 * drops both the mineral and flag tables
	 * @param holder db holder to drop tables in
	 * @throws SQLException
	 */
	private void dropTables(DBHolder holder) throws SQLException{
		for(String t : new String[] {this.tableName, SQLIChallenge.FLAG_TABLE}){
			String query = "DROP TABLE IF EXISTS " + t;
			Connection conn = holder.getConnection();
			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.executeUpdate();
			conn.close();
		}
	}

	/**
	 * inserts mineral data
	 * @param holder
	 * @throws SQLException
	 */
	private void setupContents(DBHolder holder) throws SQLException{
		Connection conn = holder.getConnection();
		for(Mineral m : this.minerals){
			try(InputStream picIn = new FileInputStream(m.getPic())){
				PreparedStatement pre = conn.prepareStatement(this.mineralInsertStatement);
				pre.setString(1, m.getName());
				pre.setString(2, m.getBlurb());
				pre.setBinaryStream(3, picIn);
				pre.executeUpdate();
				pre.close();
			} catch(SQLException | IOException e){
				conn.close();
				throw new SetupRuntimeException("Could not create mineral table for values: " + m.getName() + " " + m.getPic(), e);
			}
		}
		conn.close();
	}


	public String handleChallengeRequest(DIVAServletRequestWrapper req){
		String output = null;
		String query = req.getParameter(SQL_QUERY);
		String rst = req.getParameter(SQL_RESET);
		if(!StringUtils.isBlank(rst)){
			SessionStorage session = (SessionStorage) req.getInformation(Dictionary.SESSION_STORE_OBJ);
			String sessionID = session.getToken();
			SQLManager.getInstance().removeDBHolder(sessionID);
		}
		if(!StringUtils.isBlank(query)){
			SessionStorage session = (SessionStorage) req.getInformation(Dictionary.SESSION_STORE_OBJ);
			String sessionID = session.getToken();
			String flag = (String) req.getInformation(Dictionary.FLAG_VALUE);
			reset(sessionID, flag);
			DBHolder holder = this.getHolderForSession(sessionID);
			if(holder != null){
				try{
					PreparedStatement ps = this.makeChallengeQuery(holder, query);
					output = generateOutputForChallengeQuery(ps);
	
					ps.getConnection().close();
				}catch(SQLException e){
					output = e.getMessage();
				}
			} else{
				output = "Could not connect to Database. Please log out and log back in";
			}
		}
		return output;
	}

	/**
	 * create table header for output
	 */
	public String makeChallengeTableHeader(){
		StringBuilder sb = new StringBuilder();
		sb.append("<tr>");
		for(SimpleEntry<String, String> e : this.columns){
			sb.append("<th>");
			sb.append(e.getKey());
			sb.append("</th>");
		}
		sb.append("</tr>");
		return sb.toString();
	}

	/**
	 * execute the vulnerable challenge query
	 * @param holder dbholder to use for query
	 * @param query query to run (user input)
	 * @return executed statement
	 * @throws SQLException if sql error occurs
	 */
	private PreparedStatement makeChallengeQuery(DBHolder holder, String query) throws SQLException{
		String sql = generateSQLChallengeStatement(query);
		Connection conn = holder.getConnection();
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.execute();
		return stmt;
	}

	/**
	 * builds the vulnerable sql statement
	 * @param query user input query
	 * @return
	 */
	private String generateSQLChallengeStatement(String query) {
		String sanitizedQuery = sanitizeQuery(query);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");

		String delim = "";
		for(SimpleEntry<String, String> e : this.columns){
			sb.append(delim);
			sb.append(e.getKey());
			delim = ", ";
		}

		sb.append(" FROM ");
		sb.append(this.tableName);
		sb.append(" WHERE ");
		sb.append(this.columnKey);
		sb.append(" like '");
		sb.append(sanitizedQuery);
		sb.append("%'");
		return sb.toString();
	}

	/**
	 * Just to mess with users, certain strings are removed
	 * 
	 * @param query user input
	 * @return query minus some strings
	 */
	private String sanitizeQuery(String query) {
		String sanitize = query.replace("SELECT", "");
		sanitize = sanitize.replace("select", "");
		sanitize = sanitize.replace("=", "");
		sanitize = sanitize.replace("!", "");
		return sanitize;
	}
	
	/**
	 * Given a statement, pull up to MAX_RESULTS from the results of the query
	 * @param ps prepared and executed statement (this method will not close this)
	 * @return String containing a formatted output string
	 * @throws SQLException
	 */
	private String generateOutputForChallengeQuery(PreparedStatement ps) throws SQLException{
		StringBuilder sb = new StringBuilder();
		ResultSet rs = ps.getResultSet();
		int i = 0;
		while(i < MAX_RESULTS && rs.next()){
			try{
				sb.append("<tr>");
				sb.append("<td>");
				String name = rs.getString(1);
				sb.append(name);
				sb.append("</td>");
				sb.append("<td>");
				String blurb = rs.getString(2);
				sb.append(blurb);
				sb.append("</td>");
				String picData = "";
				try{
					Base64InputStream pic = new Base64InputStream(rs.getBinaryStream(3), true);
					picData = IOUtils.toString(pic);
				}catch(Exception e){
					picData = e.getMessage();
				}
				sb.append("<td>");

				sb.append("<img src=\"data:image/jpg;base64,").append(picData).append("\"/>");
				sb.append("</td>");

				sb.append("</tr>");

			} catch(Exception e){
				e.printStackTrace();
			}
		}

		return sb.toString();
	}

}