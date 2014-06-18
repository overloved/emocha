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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.emocha.Constants;
import org.emocha.EmochaApp;
import org.emocha.async.DownloadOneFile;
import org.emocha.async.UploadOneRow;
import org.emocha.c2dm.C2DMReceiver;
import org.emocha.model.DBAdapter;
import org.emocha.model.Preferences;
import org.emocha.model.entities.FormData;
import org.emocha.model.entities.FormDataFile;
import org.emocha.security.EasySSLSocketFactory;
import org.emocha.security.Encryption;
import org.emocha.tasks.FormDataFilesTask;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.c2dm.C2DMBaseReceiver;

import android.content.Context;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Socket connecting server and fronend.
 * @author Yao
 *
 */
public class Server {

	private static String user;
	private static String password;
	private static String serverURL;
	private static boolean selfSSLcertificate = false;
	
	//server API methods
	private static final String CMD_ACTIVATE_PHONE       = "/api/activate_phone";
	private static final String CMD_UPLOAD_FORM_DATA	 = "/api/upload_form_data";
	private static final String CMD_UPLOAD_FORM_DATA_FILE = "/api/upload_form_data_file";
	
	private static final String CMD_GET_SERVER_UPD_TIMES = "/api/get_server_updated_times";
	private static final String CMD_GET_FORM_CONFIG      = "/api/get_form_config";
	private static final String CMD_GET_MEDIA_FILES      = "/api/get_media";
	//private static final String CMD_GET_CONFIG_BY_KEY    = "/api/get_config_by_key";
	private static final String CMD_GET_CONFIG_BY_KEYS   = "/api/get_config_by_keys";
	private static final String CMD_CHECK_USER           = "/api/check_user";
	private static final String CMD_CONFIRM_ALERT      = "/api/confirm_alert";
	private static final String CMD_REGISTER_ALERTS      = "/api/register_alerts_id";
	  
	private static final String GPS_STRING = "gps";
	
	public static final String CMD_RESPONSE_PHONE_ID = "phone_id";
	public static final String CMD_RESPONSE_MSG = "msg";
	public static final String CMD_RESPONSE_OK = "OK";
	public static final String CMD_RESPONSE_STATUS = "status";
	public static final String CMD_RESPONSE_CONFIG= "config";
	public static final String CMD_RESPONSE_TS = "ts";
	public static final String CMD_RESPONSE_SIZE = "size";
	public static final String CMD_RESPONSE_KEYS = "keys";
	
	private static final String CMD_REQUEST_USR = "usr";
	private static final String CMD_REQUEST_PWD = "pwd";
	//private static final String CMD_REQUEST_KEY = "key";
	
	public static final String PARAM_DATA = "data";
	
	private static DefaultHttpClient httpClient = null;
	
	public static void init(Context context) {
		serverURL = Preferences.getServerURL(context);
		user = Preferences.getUser(context);
		password = Preferences.getServerPassword(context);
		selfSSLcertificate = Preferences.getSelfSignedSSLCertificate(context);
	}
	
	/**
	 * Send authentication info along with the data pair.
	 * @param cmd
	 * @param pairs
	 * @return
	 */
	public static JSONObject sendAuthenticated(String cmd, PostDataPairs pairs) {
		pairs.add(CMD_REQUEST_USR, user);
		pairs.add(CMD_REQUEST_PWD, password);
		return send(cmd, pairs);
	}

	/**
	 * Send data pair to server.
	 * @param cmd
	 * @param pairs
	 * @return
	 */
	public static JSONObject send(String cmd, PostDataPairs pairs) {
		try {
			URL url = new URL(serverURL+cmd);
			
			HttpURLConnection hc = (HttpURLConnection) url.openConnection();
			hc.setDoOutput(true);
			hc.setDoInput(true);
			hc.setRequestMethod(Constants.POST_METHOD_STRING);
			OutputStreamWriter osw = new OutputStreamWriter(hc.getOutputStream());
			osw.write(CommonUtils.getParameters(pairs));
			osw.flush();
			
			String jsonResponse = convertStreamToString(hc.getInputStream());
			JSONObject jObject = new JSONObject(jsonResponse);
			osw.close();
			hc.disconnect();
			return jObject;
		
		} catch (IOException ioe) {
			Log.e(Constants.LOG_TAG, "IOException: "+ioe.getMessage());
		} catch (JSONException je) {
			Log.e(Constants.LOG_TAG, "JSONException: "+je.getMessage());
		} catch (Exception e) {
			Log.e(Constants.LOG_TAG, "Exception: " + e.getMessage());
		}
		return null;
	}

	public static JSONObject sendMultipart(String cmd, MultipartEntity pairs) {
		HttpClient client = getHttpClient(null);
		
		try {
			HttpPost post = new HttpPost(serverURL + cmd);
			post.setEntity(pairs);
			HttpResponse response = client.execute(post);

			HttpEntity entity = response.getEntity();
			InputStream stream = entity.getContent();
			String jsonResponse = convertStreamToString(stream);
			stream.close();
			if (entity != null) {
				entity.consumeContent();
			}
			JSONObject jObject = new JSONObject(jsonResponse);

			return jObject;
		} catch (Exception e) {
			Log.e(Constants.LOG_TAG, "sendMultipart: Exception ERR. " + e.getMessage());
		}
		return null;
	}

	/**
	 * Send authentication information.
	 * @param cmd
	 * @param mpe
	 * @return
	 */
	public static JSONObject sendAuthenticated(String cmd, MultipartEntity mpe) {
		try {
			mpe.addPart(CMD_REQUEST_USR, new StringBody(user));
			mpe.addPart(CMD_REQUEST_PWD, new StringBody(password));
		} catch (UnsupportedEncodingException e) {
			Log.e(Constants.LOG_TAG, "sendAuthenticated error while setting multipartEntity data: "+e.getMessage());
		}
		
		return sendMultipart(cmd, mpe);
	}
	
	/**
	 * Activate the mobile advice by sending the imei number.
	 * @param imei
	 * @return
	 */
	public static JSONObject activatePhone(String imei) {
		PostDataPairs data = new PostDataPairs();
		data.add("imei", imei);
		return send(CMD_ACTIVATE_PHONE, data);
	}

	/**
	 * Upload form data to the server.
	 * @param context
	 * @param fd
	 */
	public static void uploadFormData(Context context, FormData fd) {
		
		PostDataPairs data = new PostDataPairs();
		data.add(CMD_REQUEST_USR, user);
		data.add(CMD_REQUEST_PWD, password);
		
		new UploadOneRow(context, fd, serverURL + CMD_UPLOAD_FORM_DATA, data);
	}

	/**
	 * Download single file from server.
	 * @param context
	 * @param file
	 */
	public static void downloadFile(Context context, org.emocha.model.entities.File file) {
		new DownloadOneFile(context, file, serverURL);
	}

	/**
	 * To convert the InputStream to String we use the BufferedReader.readLine()
	 * method. 
	 */
	 /*We iterate until the BufferedReader return null which means
	 * there's no more data to read. Each line will appended to a StringBuilder
	 * and returned as String.
	 */
	public static String convertStreamToString(InputStream stream) {
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(stream));
		StringBuilder result = new StringBuilder();
		String line = null;

		try {
			while ((line = reader.readLine()) != null) {
				result.append(line + Constants.NEW_LINE_STRING);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				stream.close();
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return result.toString();
	}
	
	/**@return DefaultHttpClient using custom EasySSLSocketFactory. no SSL certificate validation is performed */
	private static DefaultHttpClient getSelfSignedHttpClient(HttpParams params) {

		if (params == null) {
			params = new BasicHttpParams();
		} 
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		ClientConnectionManager clientConnectionManager;		
		
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(), 443));

		params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 1);
		params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(1));
		params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "utf8");

		clientConnectionManager = new ThreadSafeClientConnManager(params, schemeRegistry);
		return new DefaultHttpClient(clientConnectionManager, params);
	}
	
	/**
	 * Get HTTP Client.
	 * @param params
	 * @return
	 */
	public static DefaultHttpClient getHttpClient(HttpParams params) {
		if (httpClient == null) {
			
			if (selfSSLcertificate) {		
				httpClient = getSelfSignedHttpClient(params);
				Log.w(Constants.LOG_TAG,"connecting to a server with a self-signed certificated");
			} else {
				if (params != null) {
					httpClient = new DefaultHttpClient(params);
				} else {
					httpClient = new DefaultHttpClient();
				}
			}
		}
		return httpClient;
	}
	
	// pau 20100916 new api mehods
	public static JSONObject getServerUpdateTimes(String gpsPos) {
		PostDataPairs data = new PostDataPairs();
		data.add(GPS_STRING, gpsPos);
		return sendAuthenticated(CMD_GET_SERVER_UPD_TIMES, data);
	}
	
	public static JSONObject getFormsConfig(String gpsPos) {
		PostDataPairs data = new PostDataPairs();
		data.add(GPS_STRING, gpsPos);
		return sendAuthenticated(CMD_GET_FORM_CONFIG, data);
	}
	
	public static JSONObject getMediaFiles(String gpsPos) {
		PostDataPairs data = new PostDataPairs();
		data.add(GPS_STRING, gpsPos);
		return sendAuthenticated(CMD_GET_MEDIA_FILES, data);
	}

	public static boolean isSelfSignedSSLCertificate(){
		return selfSSLcertificate;
	}
	
	// config keys
	/*
	public static JSONObject getConfigByKey(String key) {
		PostDataPairs data = new PostDataPairs();
		data.add(CMD_REQUEST_KEY, key);
		return sendAuthenticated(CMD_GET_CONFIG_BY_KEY, data);
	}
	*/
	/**
	 * Configure keys.
	 * @param keys
	 * @return
	 */
	public static JSONObject getConfigByKeys(String keys) {
		PostDataPairs data = new PostDataPairs();
		data.add(CMD_RESPONSE_KEYS, keys);
		return sendAuthenticated(CMD_GET_CONFIG_BY_KEYS, data);
	}
	
	/**
	 * Check if the user is autenticated.
	 * @return
	 */
	public static JSONObject checkUser() {
		return sendAuthenticated(CMD_CHECK_USER, new PostDataPairs());
	}
	/*
	public static JSONObject registerC2DM(String registrationId){
		PostDataPairs data = new PostDataPairs();
		data.add(C2DMBaseReceiver.EXTRA_REGISTRATION_ID, registrationId);
		return sendAuthenticated(CMD_REGISTER_C2DM, data);
	}
	*/
	/**
	 * Send registration alert request to server.
	 * @param registrationId
	 * @return
	 */
	public static JSONObject registerAlerts(String registrationId){
		PostDataPairs data = new PostDataPairs();
		data.add(C2DMBaseReceiver.EXTRA_REGISTRATION_ID, registrationId);
		return sendAuthenticated(CMD_REGISTER_ALERTS, data);
	}
	
	/**
	 * Send alert confirmation request to server.
	 * @param alertId
	 * @return
	 */
	public static JSONObject confirmAlert(String alertId) {
		PostDataPairs data = new PostDataPairs();
		data.add(C2DMReceiver.ALERT_ID, alertId);
		return sendAuthenticated(CMD_CONFIRM_ALERT, data);
	}
	
	//sends files (if any) associated to a form data; deletes data from device if config says so
	/**
	 * Sends files (if any) associated to a form data; deletes data from device if config says so.
	 * @param context
	 * @param fdId Specific form data id.
	 */
	public static void uploadFormDataFiles(Context context, int fdId) {
		new FormDataFilesTask(context, fdId).run();
	}
	
	//sends 1 form data file (most likely due a previous failure)
	/**
	 * Upload form data file to server.
	 * @param context
	 * @param fdf
	 */
	public static void uploadFormDataFile(Context context, FormDataFile fdf) {
		MultipartEntity mpe = new MultipartEntity();
		
		//get info.
		FormData fd = new FormData();
		fd = fd.get(fdf.formDataId);
		if(fd == null) {
			Log.e(Constants.LOG_TAG, "Associated FormData entry does not exist.");
			return;
		}
		
		File file = new File (fdf.filename);
		//decrypt data.. be carefull: assuming all files are encrypted... and they are video files (.mp4)
		File decFile = null;
		try {
			decFile = Encryption.decryptFile(EmochaApp.getEncSecretKey(context), fdf.filename);

			//overwrite encrypted file: server required .mp4 extensions
			FileUtils.copyFile(decFile, file);
			
			if (file.exists()) {
				mpe.addPart(PARAM_DATA, new FileBody(file));
			} else { //something is wrong.. form points to a deleted file??
				Log.e(Constants.LOG_TAG, "Trying to upload non existent file?");
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(Constants.LOG_TAG, "error decrypting file.. stop sending data.");
			return;
		}
		
		
		// set parameters:
		try {
			mpe.addPart(FormDataFile.COL_FORM_DATA_ID, new StringBody(Integer.toString(fd.serverFDId)));
			mpe.addPart(FormDataFile.COL_FILENAME, new StringBody(file.getName()));
			mpe.addPart(FormDataFile.COL_TYPE, new StringBody(fdf.type));
			mpe.addPart(FormDataFile.COL_XPATH, new StringBody(fdf.xpath));
			mpe.addPart(FormDataFile.COL_LAST_MODIFIED, new StringBody(fdf.lastModified));
		} catch (UnsupportedEncodingException e) {
			Log.e(Constants.LOG_TAG, "uploadFormDataFile: error setting multipartentity data: "+e.getMessage());
			return;
		}

		JSONObject response = sendAuthenticated(CMD_UPLOAD_FORM_DATA_FILE, mpe);
	
		try {
			if (response != null && response.getString(Server.CMD_RESPONSE_STATUS).equals(Server.CMD_RESPONSE_OK)) {
				//remove data from device
				//File f = new File (fdf.filename);
				if (!file.delete()){
					Log.d(Constants.LOG_TAG, "removing non existent file..."+fdf.filename);
				}
				
				DBAdapter.delete(FormDataFile.TABLE_NAME, BaseColumns._ID+Constants.EQUALS_STRING+fdf.id, null);
				
				//delete associated FormData
				DBAdapter.delete(FormData.TABLE_NAME, BaseColumns._ID+Constants.EQUALS_STRING+fd.id, null);
				
			} else {
				//leave it for the serverService
				fdf.toUpload = FormDataFile.STATUS_FAILED;
				DBAdapter.update(FormDataFile.TABLE_NAME, fdf.getContentValues(), BaseColumns._ID+Constants.EQUALS_STRING+fdf.id, null);
			}
		} catch (JSONException e) {
			Log.e(Constants.LOG_TAG, "uploadFormDataFile: JSON Exception while reading response: "+e.getMessage());
		}

		//always delete decrypted data
		if (decFile != null && !decFile.delete()) {
			Log.d(Constants.LOG_TAG, "decrypted file does not exist.. data sent was wrong?");
		}

	}
	
}
