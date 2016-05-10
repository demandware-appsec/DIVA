/*
 * Copyright 2016 Demandware Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.demandware.vulnapp.challenge.impl;

import java.io.File;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import com.demandware.vulnapp.challenge.AbstractChallenge;
import com.demandware.vulnapp.servlet.DIVAServletRequestWrapper;
import com.demandware.vulnapp.servlet.Dictionary;
import com.demandware.vulnapp.util.Helpers;
import com.demandware.vulnapp.util.SecureRandomUtil;

/**
 * Designed to prettify xml, can also xxe and rip out a password 
 * from the user's home dir. send that password here, get a flag. 
 * 
 * @author Chris
 *
 */
public class XXEChallenge extends AbstractChallenge {

	public static final String HTML_PARAM = "pretty_print";
	public static final String PASSWORD = "password";
	
	private DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	private final String password;
	private final File flagFile;
	
	protected XXEChallenge(String name) {
		super(name);
		this.password = SecureRandomUtil.generateRandomHexString(6);
		this.flagFile = new File(".", "flag.txt");
		setupPasswordFile();
	}
	
	/**
	 * builds and writes the challenge password file
	 */
	private void setupPasswordFile(){
		StringBuilder sb = new StringBuilder();
		sb.append("No CTF is complete without this extra challenge:\n");
		sb.append(Helpers.rot13("Add ?password=" + this.password + " to the pretty printer page"));
		sb.append("\nHail Gaius Julius!\n");
		Helpers.writeToFile(this.flagFile, sb.toString());
	}
	
	public String handleChallengeRequest(DIVAServletRequestWrapper req){
		String responseText = null;
		String guessPass = req.getParameter(PASSWORD);
		if(this.password.equalsIgnoreCase(guessPass)){
			responseText = "Flag: " + (String)req.getInformation(Dictionary.FLAG_VALUE);
		} else{
			String xml = req.getParameter(HTML_PARAM);
			if(xml != null){
				responseText = this.prettyPrint(xml);
			}
		}
		return responseText;
	}
	
	/**
	 * create a simple doc builder
	 * @return new DocumentBuilder
	 * @throws ParserConfigurationException
	 */
	private DocumentBuilder getDocBuilder() throws ParserConfigurationException{
		return this.dbf.newDocumentBuilder();
	}
	
	/**
	 * Given some xml, parse and pretty print it, return the prettified version
	 */
	private String prettyPrint(String xml){
		String pretty = "";
		xml = xml.trim();
        try {
            final InputSource src = new InputSource(new StringReader(xml));
            final DocumentBuilder db = this.getDocBuilder();
            final Document doc = db.parse(src);
            final Node document = doc.getDocumentElement();
            final Boolean keepDeclaration = Boolean.valueOf(xml.startsWith("<?xml"));

            final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            final DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
            final LSSerializer writer = impl.createLSSerializer();

            writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE); // Set this to true if the output needs to be beautified.
            writer.getDomConfig().setParameter("xml-declaration", keepDeclaration); // Set this to true if the declaration is needed to be outputted.

            pretty = writer.writeToString(document);
        } catch (Exception e) {
            pretty = "Could not pretty print due to: " + e.getMessage();
        }
        return pretty;
	}

}
