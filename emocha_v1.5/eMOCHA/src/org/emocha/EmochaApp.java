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

package org.emocha;

import javax.crypto.SecretKey;

import org.emocha.model.Preferences;
import org.emocha.security.Encryption;

import android.app.Application;
import android.content.Context;
import android.telephony.TelephonyManager;

public class EmochaApp extends Application {
	private static EmochaApp mInstance = null;

	private static SecretKey mSecret = null;

	@Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }
	
	private static void checkInstance() {
		if (mInstance == null)
			 throw new IllegalStateException("eMOCHA wasn't created yet!");
	}
	
	public static SecretKey getEncSecretKey(Context context) {
		
		//TODO: encryption key must be set by NFC adapter, via setEncSecretKey method, after reading the nfc-fob. 
		//The key on the fob must be set on the phone's user session password, at the back end admin's section and provided 
		//to the device during activation call, when the pass must be SHA1ed and saved at the preferences (Preferences.setDevicePassword(context,Encryption.getSHA1(mPwd));) 
		//Each time we read the NFC-fob, after key is validated with:
		// if (!Constants.EMPTY_STRING.equals(pwd) && Encryption.getSHA1(tPassword).equals(pwd)) {
		//we must call EmochaApp.setEncSecretKey to update the password and keep it in memory
		
		return Encryption.generateSecretKey(Preferences.getPhoneId(context)+Constants.DASH_STRING+((TelephonyManager) context
		.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId(), Preferences.getUser(mInstance));
		/* TODO uncomment after the nfc stuff is done
		checkInstance();
		return mSecret;
		*/
	}
	
	public static void setEncSecretKey(String pwd) {
		checkInstance();
		if (Constants.EMPTY_STRING.equals(pwd)) { //reset pwd
			mSecret = null; //will force log in again
		} else {
			mSecret = Encryption.generateSecretKey(pwd, Preferences.getUser(mInstance));
		}
	}
}
