/*******************************************************************************
 * eMOCHA - electronic Mobile Open-Source Comprehensive Health Application
 * Copyright (c) 2010  Pau Varela - pau.varela@gmail.com
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
package org.emocha.validators;


import org.emocha.Constants;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Validate some form configurations. Some forms need special requirement from 
 * patient. FormValidator comes out a result after validating each entry form.
 * @author 
 *
 */
public class FormValidator {

	//private static final String FORM_CODE = "form_code";
	//private static final String XPATH = "xpath";
	//private static final String VALUE = "value";
	
	/**
	 * Get if the form requires signatures or not.
	 * @param config
	 * @return
	 */
	public static int signaturesRequiredAmount(String config) {
		int amount = 0;
		if (config != null && !Constants.EMPTY_STRING.equals(config) && !Constants.NULL_STRING.equals(config)) {
			try {
				JSONObject conditions = new JSONObject(config);
				amount = conditions.getInt(Constants.JSON_FORM_SIGNATURES_REQUIRED);
			} catch (JSONException e) { //do nothing
				//Log.e(Constants.LOG_TAG, "signaturesRequiredAmount: "+e.getMessage());
			}
		}
		return amount;
	}
	
	/**
	 * Check if the current form template has any files.
	 * @param config
	 * @return True if has any file.
	 */
	public static boolean templateHasFiles(String config) {
		boolean result = false;
		if (config != null && !Constants.EMPTY_STRING.equals(config) && !Constants.NULL_STRING.equals(config)) {
			try {
				JSONObject conditions = new JSONObject(config);
				return (conditions.getInt(Constants.JSON_FORM_HAS_FILES) > 0);
			} catch (JSONException e) {
				//do nothing
				//Log.e(Constants.LOG_TAG, "hasFiles: "+e.getMessage());
			}
		}
		return result;
	}
}
