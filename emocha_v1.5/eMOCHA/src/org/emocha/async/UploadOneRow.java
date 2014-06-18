/*******************************************************************************
 * eMOCHA - electronic Mobile Open-Source Comprehensive Health Application
 * Copyright (c) 2009 Abe Pazos - abe@ccghe.net
 * 				 2012 Pau Varela - pau.varela@gmail.com
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
package org.emocha.async;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.apache.http.NameValuePair;
import org.emocha.Constants;
import org.emocha.model.Preferences;
import org.emocha.model.entities.FormData;
import org.emocha.model.entities.FormDataFile;
import org.emocha.model.entities.Patient;
import org.emocha.utils.MiDOTUtils;
import org.emocha.utils.PostDataPairs;
import org.emocha.utils.Server;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

public class UploadOneRow {
	private final String PARAM_HOUSEHOLD_CODE = "household_code";
	private final String PARAM_PATIENT_CODE = "patient_code";
	private final String PARAM_FORM_CODE = "form_code";
	private final String PARAM_XML_CONTENT = "xml_content";
	private final String PARAM_FILE_PATH = "file_path";
	
	
	private final String twoHyphens ="--";
	private final String boundary = "******************";
	private final String crlf ="\r\n";


	/**
	 * Class constructor.
	 * @param context
	 * @param pending
	 * @param serverURL The server url that the data will be uploaded to.
	 * @param postData The data that will be sent to the server. 
	 */
	public UploadOneRow(Context context, FormData pending, String serverURL, PostDataPairs postData) {
		//avoid strict mode
//		if (android.os.Build.VERSION.SDK_INT > 9) {
//			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//			StrictMode.setThreadPolicy(policy);
//		}
		HttpURLConnection hc = null;
		
		try {
			URL url = new URL(serverURL);
			
			hc = (HttpURLConnection) url.openConnection();
			// set the flag indicating whether this URLConnection allows output.
			hc.setDoOutput(true);
			// Set the flag indicating whether this URLConnection allows input.
			hc.setDoInput(true);
			// Set whether this connection allows to use caches or not.
			hc.setUseCaches(false);
			// Set the request command which will be sent to the remote HTTP server.
			hc.setRequestMethod(Constants.POST_METHOD_STRING);
			
			// Set the value of the specified request header field.
			hc.setRequestProperty("Connection", "Keep-Alive");
			hc.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary); //tell connection what boundary will be used

			DataOutputStream os = new DataOutputStream(hc.getOutputStream());

			PostDataPairs pdp = prepareRequest(context, pending, postData, Preferences.getUser(context));
			sendRequestParameters(os, pdp);
			
			String jsonResponse = Server.convertStreamToString(hc.getInputStream());
			os.close();
			JSONObject response = new JSONObject(jsonResponse);
			
			if (response.getString(Server.CMD_RESPONSE_STATUS).equals(Server.CMD_RESPONSE_OK)) {
				pending.markAsUploaded(pending.id, response.getString(Server.CMD_RESPONSE_TS), response.getInt(FormDataFile.COL_FORM_DATA_ID));
				//upload form data files. FormData will be deleted from there.
				Server.uploadFormDataFiles(context, pending.id);
			} else {
				Log.e(Constants.LOG_TAG, "Error uploading: " + pending.id + Constants.BLANK_STRING + response.getString(Server.CMD_RESPONSE_MSG));				
			}

		} catch (IOException ioe) {
			Log.e(Constants.LOG_TAG, "IOException: "+ioe.getMessage());
		} catch (JSONException je) {
			Log.e(Constants.LOG_TAG, "JSONException: "+je.getMessage());
		} catch (Exception e) {
			Log.e(Constants.LOG_TAG, "Exception: " + e.getMessage());
		} finally {
			if (hc != null) {
				hc.disconnect();
			}
		}
	}

	//writes request parameters to the outputstream
	/**
	 * Writes request parameters to the outputstream
	 * @param os Outputstreams the request parameters will be written to.
	 * @param pdp The data pair.
	 * @throws IOException
	 */
	private void sendRequestParameters(DataOutputStream os, PostDataPairs pdp) throws IOException {
		List<NameValuePair> params = pdp.get();
		StringBuffer sb = new StringBuffer();
		if (!params.isEmpty()) {
			Iterator<NameValuePair> it = params.iterator();
			while (it.hasNext()){
				NameValuePair nvp = it.next();
				sb.append(twoHyphens+boundary+crlf);
				sb.append("Content-Disposition: form-data; name=\""+nvp.getName()+"\"" + crlf);
				sb.append(crlf);
				sb.append(nvp.getValue());
				sb.append(crlf);
			}
		}
		os.writeBytes(sb.toString());
		os.flush(); //make sure data is written
	}
	
	/**
	 * Pre-process request that would post to server.
	 * @param context
	 * @param pending FormData object.
	 * @param postData PostDataPairs object.
	 * @param deviceId Mobile device id.
	 * @return ArrayList contains all information.
	 */
	private PostDataPairs prepareRequest(Context context, FormData pending, PostDataPairs postData, String deviceId) {
		// get patient, and household from it
		String patientCode = Patient.getCodeFromFormDataId(pending.id);
				
		postData.add(PARAM_HOUSEHOLD_CODE, Constants.EMPTY_STRING); //this project has no household!    
		postData.add(PARAM_PATIENT_CODE, patientCode);
		postData.add(PARAM_FORM_CODE, pending.getFormCode());
		String xmlContent = pending.xmlData;
		
		/*
		if(Preferences.getDataEncryptionOn(context)) {
			xmlContent = Encryption.decrypt(EmochaApp.getEncSecretKey(context), xmlContent);
		}
		*/
		postData.add(PARAM_XML_CONTENT, xmlContent);
		
		postData.add(PARAM_FILE_PATH, pending.getFormName());
		
		postData.add(MiDOTUtils.DEVICE_ID, deviceId); 
		postData.add(Patient.COL_LAST_MODIFIED, pending.lastModified);
		postData.add(FormData.COL_TO_UPLOAD, Integer.toString(pending.toUpload));
		return postData;
	}
}
