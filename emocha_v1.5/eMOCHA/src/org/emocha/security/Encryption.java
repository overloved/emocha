/*******************************************************************************
 * eMOCHA - electronic Mobile Open-Source Comprehensive Health Application
 * Copyright (c) 2012 Pau Varela - pau.varela@gmail.com
 * 
 * This file is part of eMOCHA.
 * 
 * eMOCHA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * eMOCHA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.emocha.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
//import javax.crypto.spec.SecretKeySpec;

import org.emocha.Constants;
import org.emocha.model.Preferences;
import org.emocha.utils.FileUtils;

import android.util.Log;

public class Encryption {
	private static final String ALGORITHM = "AES";
	private static final String KEY_ALGORITHM = "PBKDF2WithHmacSHA1";
	private static final String ENC_EXTENSION = ".enc";
	public static final String DEC_EXTENSION = ".dec";

	private static byte[] deHex(String s) {
		byte[] bc = new byte[s.length() / 2];
		for (int i = 0; i < s.length(); i += 2) {
			int b = Integer.parseInt(s.substring(i, i + 2), 16);
			byte c = (byte) b;
			bc[i / 2] = c;
		}
		return bc;
	}

	private static String asHex(byte buf[]) {
		StringBuffer strbuf = new StringBuffer(buf.length * 2);
		int i;
		for (i = 0; i < buf.length; i++) {
			if (((int) buf[i] & 0xff) < 0x10)
				strbuf.append(Constants.ZERO_STRING);
			strbuf.append(Long.toString((int) buf[i] & 0xff, 16));
		}
		return strbuf.toString();
	}

	public static String encrypt(SecretKey key, String value) {
		try {
			// Instantiate the cipher
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] encrypted = cipher.doFinal(value.getBytes());
			return asHex(encrypted);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Error: Encryption.encrypt: "
					+ ex.getMessage());
		}
	}

	public static String decrypt(SecretKey key, String value) {
		try {
			// Instantiate the cipher
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, key);
			byte[] val = deHex(value);
			byte[] original = cipher.doFinal(val);
			return new String(original);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Error: Encryption.decrypt: "
					+ ex.getMessage());
		}
	}

	public static void encryptFile(SecretKey key, String path) throws IOException {
		
		// Instantiate the cipher
		Cipher cipher;
		FileInputStream fin = null;
		FileOutputStream fout = null;
		CipherOutputStream out = null;
		try {
			cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			
			fin = new FileInputStream(path);
			fout = new FileOutputStream(path+ENC_EXTENSION);
	
			//do the crypt thing..
			out = new CipherOutputStream(fout, cipher);
            int count = 0;
            byte[] buffer = new byte[1024];
            while ((count = fin.read(buffer)) >= Constants.ZERO) {
                out.write(buffer, 0, count);
            }
            out.flush();
            out.close();
            
            //Rename the files and delete any tmp file.
            File plain = new File(path);
            File enc = new File(path+ENC_EXTENSION);
            
            FileUtils.copyFile(enc, plain);
            enc.delete();
            
            
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException("NoSuchAlgorithm: Encryption.encryptFile: "+ e.getMessage());
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			throw new RuntimeException("NoSuchPaddingException: Encryption.encryptFile: "+ e.getMessage());
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			throw new RuntimeException("InvalidKeyException: Encryption.encryptFile: "+ e.getMessage());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("FileNotFoundExceptionr: Encryption.encryptFile: "+ e.getMessage());
		} finally {
			if (fin != null) {
				fin.close();
			}
		}
	}

	public static File decryptFile(SecretKey key, String path) throws IOException {
		// Instantiate the cipher
		Cipher cipher;
		FileInputStream fin = null;
		FileOutputStream fout = null;
		CipherInputStream cin = null;
		try {
			cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, key);
			/*
			filename = path.substring(path.lastIndexOf(Constants.SLASH_STRING));
			folder = path.substring(0, path.lastIndexOf(Constants.SLASH_STRING) + Constants.ONE);
			
			filename = DEC+Constants.DASH_STRING+filename;
			*/
			fin = new FileInputStream(path);
			fout = new FileOutputStream(path+DEC_EXTENSION);
			//fout = new FileOutputStream(folder+filename);
			
			
			cin = new CipherInputStream(fin,cipher);
			int count = 0;
			byte[] buffer = new byte[1024];
            while ((count = cin.read(buffer)) >= Constants.ZERO) {
                fout.write(buffer, 0, count);
            }
            fout.flush();
            fout.close();
            
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException("NoSuchAlgorithm: Encryption.encryptFile: "+ e.getMessage());
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			throw new RuntimeException("NoSuchPaddingException: Encryption.encryptFile: "+ e.getMessage());
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			throw new RuntimeException("InvalidKeyException: Encryption.encryptFile: "+ e.getMessage());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("FileNotFoundExceptionr: Encryption.encryptFile: "+ e.getMessage());
		} finally {
			if (cin != null) {
				cin.close();
			}
		}
		
		return new File(path+DEC_EXTENSION);
	}	
	

	/* Derive the key, given password and salt. */
	public static SecretKey generateSecretKey(String password, String salt) {
		SecretKey secret = null;
		try {
			SecretKeyFactory factory = SecretKeyFactory
					.getInstance(KEY_ALGORITHM);
			KeySpec spec = new PBEKeySpec(password.toCharArray(),
					salt.getBytes(), 950, 256);
			secret = factory.generateSecret(spec);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			Log.e(Constants.LOG_TAG,
					"NoSuchAlgorithmException: " + e.getMessage());
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
			Log.e(Constants.LOG_TAG,
					"InvalidKeySpecException: " + e.getMessage());
		}

		return secret;
	}

	public static String getSHA1(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(input.getBytes(), 0, input.length());
			byte[] result = md.digest();

			return asHex(result);

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			Log.d(Constants.LOG_TAG, e.getMessage());
		}
		return null;
	}

}
