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
package com.demandware.vulnapp.user.password;

/**
 * Nearly a direct copy of other Base64 implementations. 
 * BCrypt uses a slightly modified version than the standard Base64. 
 * (It's the first characters)
 * 
 * @author Chris Smith
 */
class OpenBSDBase64 {
	// Table for Base64 encoding
	static private final char[] BASE64_CODE = {
		'.', '/', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',	'K', 'L', 'M', 'N',
		'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',	'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
		'e', 'f', 'g', 'h',	'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
		'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5',	'6', '7', '8', '9'
	};

	// Table for Base64 decoding
	static private final byte[] INDEX_64 = {
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1,	-1, -1, -1, -1, -1, -1, //0-15
		-1, -1, -1, -1,	-1, -1, -1, -1, -1, -1, -1, -1, -1, -1,	-1, -1, //16-31
		-1, -1, -1, -1, -1, -1, -1, -1,	-1, -1, -1, -1, -1, -1,  0,  1, //32-47
		54, 55,	56, 57, 58, 59, 60, 61, 62, 63, -1, -1,	-1, -1, -1, -1, //48-63
		-1,  2,  3,  4,  5,  6,	 7,  8,  9, 10, 11, 12, 13, 14, 15, 16, //64-79
		17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, -1, -1, -1, -1, -1, //80-95
		-1, 28, 29, 30,	31, 32, 33, 34, 35, 36, 37, 38, 39, 40,	41, 42, //96-111
		43, 44, 45, 46, 47, 48, 49, 50,	51, 52, 53, -1, -1, -1, -1, -1  //112-128
	};

	
	static String encode(byte[] d, int len) throws IllegalArgumentException {
		int off = 0;
		StringBuilder rs = new StringBuilder(len*2);
		int c1;
		int c2;

		if (len <= 0 || len > d.length){
			throw new IllegalArgumentException ("Invalid len");
		}
		
		while (off < len) {
			c1 = d[off++] & 0xff;
			rs.append(BASE64_CODE[(c1 >> 2) & 0x3f]);
			c1 = (c1 & 0x03) << 4;
			if (off >= len) {
				rs.append(BASE64_CODE[c1 & 0x3f]);
				break;
			}
			c2 = d[off++] & 0xff;
			c1 |= (c2 >> 4) & 0x0f;
			rs.append(BASE64_CODE[c1 & 0x3f]);
			c1 = (c2 & 0x0f) << 2;
			if (off >= len) {
				rs.append(BASE64_CODE[c1 & 0x3f]);
				break;
			}
			c2 = d[off++] & 0xff;
			c1 |= (c2 >> 6) & 0x03;
			rs.append(BASE64_CODE[c1 & 0x3f]);
			rs.append(BASE64_CODE[c2 & 0x3f]);
		}
		return rs.toString();
	}


	private static byte char64(char x) {
		if ((int)x > INDEX_64.length){
			return -1;
		}
		return INDEX_64[(int)x];
	}


	static byte[] decode(String s, int len) throws IllegalArgumentException {
		StringBuilder rs = new StringBuilder(len*2);
		int off = 0;
		int slen = s.length();
		int olen = 0;
		byte[] ret;
		byte c1;
		byte c2;
		byte c3;
		byte c4;
		byte o;

		if (len <= 0){
			throw new IllegalArgumentException ("Invalid len");
		}
		
		while (off < slen - 1 && olen < len) {
			c1 = char64(s.charAt(off++));
			c2 = char64(s.charAt(off++));
			if (c1 == -1 || c2 == -1){
				break;
			}
			o = (byte)(c1 << 2);
			o |= (c2 & 0x30) >> 4;
			rs.append((char)o);
			if (++olen >= len || off >= slen){
				break;
			}
			c3 = char64(s.charAt(off++));
			if (c3 == -1){
				break;
			}
			o = (byte)((c2 & 0x0f) << 4);
			o |= (c3 & 0x3c) >> 2;
			rs.append((char)o);
			if (++olen >= len || off >= slen){
				break;
			}
			c4 = char64(s.charAt(off++));
			o = (byte)((c3 & 0x03) << 6);
			o |= c4;
			rs.append((char)o);
			++olen;
		}

		ret = new byte[olen];
		for (off = 0; off < olen; off++){
			ret[off] = (byte)rs.charAt(off);
		}
		return ret;
	}
}
