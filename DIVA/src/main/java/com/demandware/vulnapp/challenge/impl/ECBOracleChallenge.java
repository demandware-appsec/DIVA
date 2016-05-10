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

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import com.demandware.vulnapp.challenge.AbstractChallenge;
import com.demandware.vulnapp.servlet.DIVAServletRequestWrapper;
import com.demandware.vulnapp.servlet.Dictionary;
import com.demandware.vulnapp.util.exception.SetupRuntimeException;

/**
 * Execute a Chosen-platintext attack to reveal the AES-ECB encrypted string
 * 
 * @author Chris Smith
 *
 */
public class ECBOracleChallenge extends AbstractChallenge {
	public static final String FLAG_PARAM = "encrypted_flag";

	public static final String INPUT_VAL = "input";
	
	private final String encryptionType;
	private final String keyType;
	private final Cipher eCipher;
	private final Cipher dCipher;
	
	protected ECBOracleChallenge(String name) {
		super(name);
		this.keyType = "AES";
		this.encryptionType = "AES/ECB/NoPadding";
		try{
			SecretKey key = generateKey();
			this.eCipher = Cipher.getInstance(this.encryptionType);
			this.eCipher.init(Cipher.ENCRYPT_MODE, key);
			this.dCipher = Cipher.getInstance(this.encryptionType);
			this.dCipher.init(Cipher.DECRYPT_MODE, key);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
			throw new SetupRuntimeException(e);
		}
		tryRoundtrip();
	}
	
	/**
	 * it's easy to mess up the encryption depending on implementation 
	 * and some odd changes for certain JVMs. This is a sanity check.
	 */
	private void tryRoundtrip(){
		try{
			String test = "foobar";
			String enc = encryptValue(test);
			String dec = decryptValue(enc);
			if(!test.equals(dec)){
				throw new SetupRuntimeException("ECB roundtrip failed");
			}
		} catch(Exception e){
			throw new SetupRuntimeException(e);
		}
	}
	
	/**
	 * Generate some key such that we don't know the key value so noone can just
	 * use the key
	 */
	private SecretKey generateKey() throws NoSuchAlgorithmException{
		KeyGenerator keyGen = KeyGenerator.getInstance(this.keyType);
		return keyGen.generateKey();
	}

	/**
	 * entrance to challenge
	 */
	public String handleChallengeRequest(DIVAServletRequestWrapper req){
		String value = null;
		String flag = (String)req.getInformation(Dictionary.FLAG_VALUE);
		String input = req.getParameter(FLAG_PARAM);;
		String toBeEnc = generateStringForEncryption(input, flag);
		if(toBeEnc != null){
			try{
				value = encryptValue(toBeEnc);
			} catch(Exception e){
				value = "Error encrypting";
			}
		}
		return value;
	}

	/**
	 * combine user input and flag values
	 */
	private String generateStringForEncryption(String input, String flag) {
		StringBuilder sb = new StringBuilder();
		sb.append(ECBOracleChallenge.INPUT_VAL);
		sb.append("=");
		sb.append(input);
		sb.append(";Flag=");
		sb.append(flag);
		sb.append(";");
		return sb.toString();
	}

	/**
	 * encrypt the given input with this object's cipher
	 * 
	 * @return base64 encoded string of the encrypted value
	 * @throws UnsupportedEncodingException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	private String encryptValue(String input) throws UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException {
		input = input.trim();
		byte[] inputBytes = input.getBytes();
		int paddedSize = inputBytes.length + (16 - (inputBytes.length % 16));
		inputBytes = Arrays.copyOf(inputBytes, paddedSize);
		byte[] enc = this.eCipher.doFinal(inputBytes);
		return Hex.encodeHexString(enc);
	}

	/**
	 * test method, user will never decrypt
	 * @throws DecoderException 
	 */
	private String decryptValue(String str) throws IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, DecoderException {
		byte[] dec = Hex.decodeHex(str.toCharArray());
		byte[] decBytes = this.dCipher.doFinal(dec);
		return new String(decBytes).trim();
	}

}
