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
package org.emocha.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.apache.http.NameValuePair;
import org.emocha.Constants;
import org.emocha.midot.R;
import org.emocha.model.Preferences;
import org.emocha.model.entities.FormData;
import org.emocha.model.entities.FormTemplate;
import org.emocha.validators.FormValidator;
import org.emocha.xml.FormDataFileParser;
import org.odk.collect.android.activities.FormEntryActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

/**
 * List some common utils that are applied in the app.
 * @author 
 *
 */
public class CommonUtils {	
	
	/**sets up the intent to be used on the ODK call
	 * @param	id	needed for the onActivityResult method. it's either household_id or patient_id
	 * @param 	xml	required for editing the forms. it might be empty
	 * @param	formPath 	path to the form to open
	 * @return	Intent*/
	public static Intent prepareODKIntent(Context context, Bundle bundle, String xml, String instancePath, String formPath) {
		if (bundle == null) {
			bundle = new Bundle();
		}

		Intent tIntent = new Intent(context, FormEntryActivity.class);
		
		// 1.- put required info ("formpath", "instancepath" and "instancexml" 
		tIntent.putExtra(Constants.ODK_FORMPATH_KEY, formPath);	
		tIntent.putExtra(Constants.ODK_INSTANCEPATH_KEY, instancePath);
		tIntent.putExtra(Constants.ODK_INSTANCEXML_KEY, (Constants.EMPTY_STRING.equals(xml) ? null : xml));			
		
		// 2.- put extra data		
		tIntent.putExtra(Constants.ODK_EXTRA_DATA_KEY, bundle);
		return tIntent;
	}
	
	/**
	 * Show alert message to client.
	 * @param context
	 * @param title Alert title.
	 * @param message Alert content.
	 */
	public static void showAlertMessage(Context context, String title, String message){
		AlertDialog alertDialog = new AlertDialog.Builder(context).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.setButton(context.getString(R.string.ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,	int which) {
						return;
					}
				});
		alertDialog.show();
	}
	
	public static boolean isValidURL(String url) {
		boolean response = false;
		try {
			new URL(url);
			response = true;
		} catch (MalformedURLException ex) {
			Log.w(Constants.LOG_TAG, "invalid URL:"+url);
		}
		return response; 
	}

	/** 
	 * Get current Time
	 * @param context
	 * @return Current time.
	 */
	public static String getCurrentTime(Context context) {
		String tz = Preferences.getAppTimezone(context);
		if (tz != null){
		      Constants.STANDARD_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone(tz));
		}
		return  Constants.STANDARD_DATE_FORMAT.format(Calendar.getInstance().getTime());
	}
	
	/*builds a string with the parameters provided within the PostDataPairs pair*/
	/**
	 * Build a string with the parameters provided within the PostDataPairs pair.
	 * @param pairs
	 * @return
	 */
	public static String getParameters(PostDataPairs pairs) {
	    String result = Constants.EMPTY_STRING;
	    List<NameValuePair> params = pairs.get();
	    if (!params.isEmpty()) {
	      StringBuffer sb = new StringBuffer();
	      Iterator<NameValuePair> it = params.iterator();
	      while (it.hasNext()){
	        sb.append(sb.length() == 0 ? Constants.EMPTY_STRING : Constants.AMPERSAND_STRING);
	        NameValuePair nvp = it.next();
	            sb.append(nvp.getName()).append(Constants.EQUALS_STRING).append(nvp.getValue());
	      }
	
	      if (sb.length() > 0) {
	        result = sb.toString();
	      }
	    }
	    return result;
	  }
	  
	  /**
	   * Parse xml.
	   * @param context
	   * @param formDataId
	   * @param xml
	   */
	  public static void postProcessXForm(Context context, int formDataId, String xml) {
		  //1.- get FormTemplate's config
		  FormData fd = new FormData();
		  FormTemplate ft = fd.getFormTemplate(formDataId);

		  //2.-check conditions.
		  if (ft != null) {
			  if (FormValidator.templateHasFiles(ft.config)){

				  //3.- Parse FormdataFile...
				  FormDataFileParser parser = new FormDataFileParser(context, formDataId);
				  parser.parse(xml);
			  }
		  }
	  }
	  
	 /**
	  * Get current path of view.
	  * @param context
	  * @return String contains the path.
	  */
	  public static String getInstancePath(Context context) {
		 return Environment.getExternalStorageDirectory()
        		+ context.getString(R.string.app_path_base)
        		+ context.getString(R.string.app_odk_path)
        		+ context.getString(R.string.app_odk_data_path)
        		+ Constants.VIDEOS_STRING
        		+ Constants.SLASH_STRING;
	  }
}
