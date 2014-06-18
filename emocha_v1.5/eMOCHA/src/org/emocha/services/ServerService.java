/*******************************************************************************
 * eMOCHA - electronic Mobile Open-Source Comprehensive Health Application
 * Copyright (c) 2009 Abe Pazos - abe@ccghe.net
 *                    Ricardo A.B - blackbitshines@gmail.com
 *                    Pau Varela - pau.varela@gmail.com
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
package org.emocha.services;

import java.util.ArrayList;
import java.util.Date;

import org.emocha.Constants;
import org.emocha.activities.Initial;
import org.emocha.midot.R;
import org.emocha.model.DBAdapter;
import org.emocha.model.Preferences;
import org.emocha.model.entities.File;
import org.emocha.model.entities.FormData;
import org.emocha.model.entities.FormDataFile;
import org.emocha.model.entities.FormTemplate;
import org.emocha.utils.FileUtils;
import org.emocha.utils.Sdcard;
import org.emocha.utils.Server;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.RemoteViews;

public class ServerService extends Service {

	private org.emocha.model.entities.File file = new org.emocha.model.entities.File();
	private FormData formData = new FormData();
	private static final String ALIVE_MSG = "ServerService tick";
	private static RemoteViews nmView;
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(Constants.LOG_TAG, ALIVE_MSG);

		if (Preferences.getNetworkActive(getApplicationContext())) {
			//start sync process in a separate thread
			new SyncServerTask().execute();
		}
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Preferences.destroy();
		DBAdapter.destroy();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	/**reads the application config from a JSONObject and saves it in application's Preferences 
	 * @param 	context	application context
	 * @param 	config	JSONObject received form the server
	 * @param 	lastServerAppUpd	timestamp. last time the server updated the app config
	 * @return <b>true</b> when the app config wasn't changed or was successfully updated, <b>false</b> otherwise*/
	private boolean handleAppConfig (Context context, JSONObject config, String lastServerAppUpd) throws JSONException {
		boolean result = true;
		
		if (config != null && Server.CMD_RESPONSE_OK.equals(config.getString(Server.CMD_RESPONSE_STATUS))) {
			if (config.has(Server.CMD_RESPONSE_KEYS)) {

				result = Preferences.updateAppConfig(context, config.getJSONObject(Server.CMD_RESPONSE_KEYS));
				Preferences.setLastAppConfigUpdateTimestamp(context, lastServerAppUpd);
			} else {
				Log.d(Constants.LOG_TAG, Server.CMD_RESPONSE_KEYS+ " were not found when calling get_config_by_keys method!");
				result = false;
			}
		}
		return result;
	}
	
	/**updates existent FormTemplate or creates a new one
	 * @param	JSONObject	server response with templates info. */
	private void updateFormTemplates (JSONObject jsonTemplates) throws JSONException {
		JSONArray forms = jsonTemplates.getJSONObject(Server.CMD_RESPONSE_CONFIG).getJSONArray(Constants.JSON_FORM_FORMS);
		JSONObject jf;
		JSONObject jTemplate;
		org.emocha.model.entities.File dbFile;
		ArrayList<Integer> currentForms = new ArrayList<Integer>(forms.length());

		for (int i = 0; i < forms.length(); i++) {
			jf = forms.getJSONObject(i);

			jTemplate = jf.getJSONObject(Constants.JSON_FORM_TEMPLATE);
			if (jTemplate != null) {
				
				//get file reference first!
				dbFile = updateTemplate(jTemplate);
				currentForms.add(dbFile.serverFileId);
				
				//insert vs update template
				FormTemplate template = new FormTemplate(jf.getString(FormTemplate.COL_CODE),
						jf.getString(Constants.JSON_FORM_GROUP), //col name is different
						jf.getString(FormTemplate.COL_NAME),
						jf.getString(FormTemplate.COL_DESCRIPTION),
						jf.getString(FormTemplate.COL_LABEL),
						jf.getString(Constants.JSON_FORM_CONDITIONS),
						dbFile.id);
				int id = FormTemplate.getIdFromCode(template.code);
				//insert vs. update
				if (id < 0) { 
					DBAdapter.insert(FormTemplate.TABLE_NAME, template.getContentValues());
					Log.d(Constants.LOG_TAG,(getString(R.string.form_template_insert, template.name)));
				} else { // update always. checking md5 is not enough, some values might have changed
					
					DBAdapter.update(FormTemplate.TABLE_NAME, template.getContentValues(), BaseColumns._ID+Constants.EQUALS_STRING+id, null);
					Log.d(Constants.LOG_TAG,(getString(R.string.form_template_update, template.name)));
				}
			}
		}
		
		//clean db & sdcard
		file.cleanNonUsedServerFiles(currentForms, Sdcard.TEMPLATE_TYPE);
		file.deleteUnwantedFiles(getApplicationContext());
	}

	private org.emocha.model.entities.File updateTemplate(JSONObject template) throws JSONException {
		org.emocha.model.entities.File dbFile = null;
		Context context = getApplicationContext();
		
		//prepare data
		String rPath = Constants.SLASH_STRING+template.getString(org.emocha.model.entities.File.COL_PATH);
		String lPath = Environment.getExternalStorageDirectory()
					 + context.getString(R.string.app_path_base)
					 + context.getString(R.string.app_odk_path)
					 + context.getString(R.string.app_odk_forms_path) 
					 + FileUtils.getFilename(rPath);
		String lastModified = Constants.STANDARD_DATE_FORMAT.format(new Date(Long.parseLong(template.getString(Server.CMD_RESPONSE_TS))*1000)); //convert UNIX_TIMESTAMP
		String md5 = template.getString(File.COL_MD5);
		long size = template.getLong(Server.CMD_RESPONSE_SIZE);
		int serverFileId = template.getInt(Constants.PARAM_ID);
		
		dbFile = file.getFileFromServerId(serverFileId);
		if (dbFile != null) {
			java.io.File file = new java.io.File(dbFile.path);
			if (file.exists()) {
				Log.d(Constants.LOG_TAG, "file size: "+file.length());
				// compare values (md5, size, ts, etc)
				if (!md5.equals(dbFile.md5) ||
					size != file.length() ||
					!lastModified.equals(dbFile.lastModified)) {
					dbFile = new File (Sdcard.TEMPLATE_TYPE, lPath, md5, Constants.ONE, lastModified, serverFileId, rPath);
					DBAdapter.update(File.TABLE_NAME, dbFile.getContentValues(), File.COL_SERVER_FILE_ID+Constants.EQUALS_STRING+serverFileId, null);
				}
					
			} else { //exists in db, but not in sdcard? download it again
				dbFile = new File (Sdcard.TEMPLATE_TYPE, lPath, md5, Constants.ONE, lastModified, serverFileId, rPath);
				DBAdapter.update(File.TABLE_NAME, dbFile.getContentValues(), File.COL_SERVER_FILE_ID+Constants.EQUALS_STRING+serverFileId, null);
			}
			
		} else { //dbFile doesn't exist.. create it with remote values
			dbFile = new File(Sdcard.TEMPLATE_TYPE, lPath, md5, Constants.ONE, lastModified,serverFileId,rPath);
			dbFile.id = DBAdapter.insert(File.TABLE_NAME, dbFile.getContentValues());
		}
		
		return dbFile;
	}
	
	private void showNotification(Context context, String tickertxt, String displayTxt, int max, int progress) {
		NotificationManager notifMgr = (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);

		Intent i = new Intent(context, Initial.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // reuse existing task
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, i, 0);

		// construct the Notification object.
		Notification notif = new Notification();
		notif.flags |= Notification.FLAG_AUTO_CANCEL;
		notif.tickerText = tickertxt;
		notif.icon = R.drawable.icon;
		
		nmView = new RemoteViews(context.getPackageName(), R.layout.custom_notification_layout);
		nmView.setProgressBar(R.id.progressbar, max, progress, false);
		nmView.setTextViewText(R.id.TextView01, displayTxt);

		notif.contentView = nmView;

		notif.contentIntent = contentIntent;
		notifMgr.notify(R.layout.custom_notification_layout, notif);
	}
	
	/**Looks for form templates pending to be downloaded*/
	private void updateTemplateFiles(Context context) {
		int pendingTemplates = file.getCountToDownload(Sdcard.TEMPLATE_TYPE);
		for (int i = 0; i < pendingTemplates; i++) {

			org.emocha.model.entities.File pending = file.getNextFileToDownload(Sdcard.TEMPLATE_TYPE);
			showNotification(context, getString(R.string.sync_server),
							getString(R.string.transmitting_files, i+1, pendingTemplates), 
							pendingTemplates, //total files 
							i+1); // progress
			Server.downloadFile(context, pending);

			if (pendingTemplates > 0) { // there was an update
				showNotification(context, getString(R.string.sync_complete), getString(R.string.done), 1, 1);
			}
		}
	}
	
	/**Looks for form data pending to be uploaded*/
	private void updateFormData(Context context) {
		int pendingData = formData.getCountToUpload();
		for (int i = 0; i < pendingData; i++) {
			FormData fd = formData.getFormDataToUpload();
			if (fd != null) {
				
				showNotification(context, getString(R.string.sync_server),
						getString(R.string.transmitting_files, i+1, pendingData), 
						pendingData, //total data
						i+1); // progress
				Server.uploadFormData(context, fd);
			}
		}
		
		if (pendingData > 0) { // there was an update
			showNotification(context, getString(R.string.sync_complete), getString(R.string.done), 1, 1);
		}
	}

	/**Looks for form data pending to be uploaded*/
	private void updateFormDataFiles(Context context) {
		
		FormDataFile [] pending = FormDataFile.getFailedFormDataFiles();
		if (pending != null) {
			for (int i = 0; i < pending.length; i++) {
					
				showNotification(context, getString(R.string.sync_server),
						getString(R.string.transmitting_files, i+1, pending.length), 
						pending.length, //total data
						i+1); // progress
				 Server.uploadFormDataFile(context, pending[i]);
			}
		}
		
		if (pending != null) { // there was an update
			showNotification(context, getString(R.string.sync_complete), getString(R.string.done), 1, 1);
		}
	}
	
	/**Updates both application and form configuration. when form config gets updated, 
	 * it also updates the templates, by calling UpdateTemplatesTask.
	 * Finally, form data is updated from this task as well.*/
	private class SyncServerTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			boolean result = true ;
			Context context = getApplicationContext();
			DBAdapter.init(context);
			Server.init(context);
			try {
					String gpsPos = Preferences.getGpsPos(context);
				
					// check user for c2dm registration..
					if (!Preferences.getUserValidated(context)) {
						JSONObject response = Server.checkUser();
						if (response != null  && Server.CMD_RESPONSE_OK.equals(response.getString(Server.CMD_RESPONSE_STATUS))) {
							Preferences.setUserValidated(context, true);
							
							/**/
							//TODO: replace phone id with user_session_password. 
							//      and call Preferences.setDevicePassword(context, Encryption.getSHA1(pwd))
							// 		look comments at EmochaApp.getEncSecretKey
							
							//request phone_id
							response = Server.activatePhone(Preferences.getImei(context));

							try {
								//phone_id is required for encryption; force validation again!
								String phoneId = response.getString(Server.CMD_RESPONSE_PHONE_ID);
								Preferences.setPhoneId(phoneId, context);
								Log.d(Constants.LOG_TAG, "New phone_id received: "+phoneId);

						      } catch (Exception e) { 
						    	  Preferences.setUserValidated(context, false);
						      }
							/**/
						}
					}
					
					//1.- get_server_updated_times
					JSONObject times = Server.getServerUpdateTimes(gpsPos);
					if (times != null && Server.CMD_RESPONSE_OK.equals(times.getString(Server.CMD_RESPONSE_STATUS))) {
						String lastServerAppUpd = times.getString(Preferences.LAST_APP_CONFIG_UPDATE_TS);
						String lastServerFormConfigUpd = times.getString(Preferences.LAST_FORM_CONFIG_UPDATE_TS);
						
						//2.- get app config
						String lastAppUpd = Preferences.getLastAppConfigUpdateTimestamp(context);
						if (!lastServerAppUpd.equals(lastAppUpd)) { //update only when required
							
							JSONObject config = Server.getConfigByKeys(Constants.EMPTY_STRING);
							
							if (!handleAppConfig(context, config, lastServerAppUpd)) {
								Log.w(Constants.LOG_TAG, "Error reading getApplicationConfig");
								result =  false;
							}
						}
						
						//3.- get forms config
						String lastFormConfig = Preferences.getLastFormConfigUpdateTimestamp(context);
						if (!lastServerFormConfigUpd.equals(lastFormConfig) ||
							!Sdcard.isSdcardSync(Sdcard.TEMPLATE_TYPE)) {
	
							JSONObject fConfig = Server.getFormsConfig(gpsPos);
							
							//update form_templates
							updateFormTemplates(fConfig);
							
							Log.d(Constants.LOG_TAG, "Form config updated");
							Preferences.setLastFormConfigUpdateTimestamp(context, lastServerFormConfigUpd);
							
							//do the templates update
							updateTemplateFiles(context);
						}
						result = true;
					} else {
						if (times != null) {
							Log.w(Constants.LOG_TAG, "ServerService: server response: "+times.getString(Server.CMD_RESPONSE_MSG));
						} else {
							Log.e(Constants.LOG_TAG, "ServerService: server response is null");
						}
						result = false;
					}
		
					//update pending forms
					updateFormData(context);
					
					//update pending form data files:
					updateFormDataFiles(context);
					
					
			} catch (JSONException ex) {
				ex.printStackTrace();
				Log.d(Constants.LOG_TAG, "JSONException: "+ex.getMessage());
				result = false;
			} 
			
			return result;
		}
		
		@Override
    	protected void onPostExecute(Boolean result){
			Log.d(Constants.LOG_TAG, "SyncServerTask. stopping service");
    		//stop the service
    		stopSelf();
    	}
	}
}