/*******************************************************************************
 * eMOCHA - electronic Mobile Open-Source Comprehensive Health Application
 * Copyright (c) 2009 Abe Pazos - abe@ccghe.net
 * 					  Pau Varela - pau.varela@gmail.com
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
package org.emocha.utils;

import java.io.File;
import java.io.IOException;

import org.emocha.Constants;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/** 
 * SD card configuration.
 * @author
 *
 */
public class Sdcard {
	public static final String TEMPLATE_TYPE = "template";
	public static final String MEDIA_TYPE = "media";
	
	
	/**
	 * Get the configured media file, and make it as JSON format.
	 * @param path
	 * @return
	 */
	public static JSONObject getConfigFile(String path) {
		JSONObject j = null;
		try {
			String s = FileUtils.readFileAsString(path);
			j = new JSONObject(s);
		} catch (IOException e) {
			Log.e(Constants.LOG_TAG, "File: ["+path+"] was not found in the device!");
			j = null;
		} catch (JSONException e) {
			Log.e(Constants.LOG_TAG, "Error while creating a new JSONObject from the file ["+path+"]");
			j = null;
		}
		return j;
	}
	
	/**verifies Sdcard & DB are in sync for the provided type
	 * @param	type	file type (TEMPLATE_TYPE | MEDIA_TYPE)
	 * @return 	<b>true</b> when sdcard is in sync, <b>false</b> otherwise*/
	public static boolean isSdcardSync(String type) {
		boolean result = true;
		org.emocha.model.entities.File file = new org.emocha.model.entities.File();
		
		String[] localFiles = file.getFilePathsByType(type);
		if (localFiles != null) {
			File f;
			for (int i = 0; i< localFiles.length; i++) {
				f = new File(localFiles[i]);
				if (!f.exists()) {
					result = false;
					break;
				} 
			}
		} else {
			result = false;
		}
		return result;
	}
	
}
