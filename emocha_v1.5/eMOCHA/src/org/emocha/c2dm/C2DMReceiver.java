package org.emocha.c2dm;


import java.net.URLDecoder;
import java.util.Date;

import org.emocha.c2dm.activities.CustomMessageActivity;
import org.emocha.c2dm.activities.FormReminderActivity;
import org.emocha.Constants;
import org.emocha.model.DBAdapter;
import org.emocha.model.Preferences;
import org.emocha.model.entities.FormTemplate;
import org.emocha.utils.Server;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.c2dm.C2DMBaseReceiver;

public class C2DMReceiver extends C2DMBaseReceiver {

	public static String FORM_REMINDER = "form_reminder";
	public static String CUSTOM_MESSAGE = "custom_message";
	public static String MESSAGE = "message";
	public static String ALERT_ID = "alert_id";
	public static String C2DM_RECEIVED_TS = "cd2m_received_ts";
	public static String PARAM_MSG_TYPE = "message_type";
	private static String PARAM_FORM_CODE = "form_code";

	public C2DMReceiver() {
		super();
	}

	@Override
    public void onRegistered(Context context, String registration) {
		//1.- save preferences
		Preferences.setGCMRegistrationId(context, registration);
		
		//2.- send registrationId to the server
		JSONObject response = Server.registerAlerts(registration);
		try {
			if (response != null && Server.CMD_RESPONSE_OK.equals(response.getString(Server.CMD_RESPONSE_STATUS))) {
				Log.d(Constants.LOG_TAG, "Device registered successfully for push notifications");
				Preferences.setC2DMServerRegistered(context, true);
			} else {
				//make sure we'll register to the server later on...
				Preferences.setC2DMServerRegistered(context, false);
				
				if (response != null) {
					Log.e(Constants.LOG_TAG, "Error registering device to the server: response = "+ response.getString(Server.CMD_RESPONSE_MSG));
	        	} else {
			          Log.e(Constants.LOG_TAG, "NULL response when calling register_c2dm?");
		        }
			}
		} catch (JSONException e) {
			Log.e(Constants.LOG_TAG, "Error reading response from server, while registering");
		}
	}

    @Override
    public void onUnregistered(Context context) {
    	Log.d(Constants.LOG_TAG, "C2DMReceiver.onUnregistered");
    	//TODO do something? do we need this method? if app goes via market, should we unregister the phone from the server????
    }

	@Override
	public void onError(Context context, String errorId) {
		Log.e(Constants.LOG_TAG, "C2DMReceiver error?? errorId: "+ errorId);
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		DBAdapter.init(context);
		
		Bundle extras = intent.getExtras();
		if (extras != null) {
			
			String formCode = extras.getString(PARAM_FORM_CODE);
			String msgType = extras.getString(PARAM_MSG_TYPE);
			String alertId = extras.getString(ALERT_ID);
			String message = URLDecoder.decode(extras.getString(MESSAGE));
			
			if (FORM_REMINDER.equals(msgType)) {
	
				if (FormTemplate.getIdFromCode(formCode) >= 0 ) { // valid form code
					Intent i = new Intent(context, FormReminderActivity.class);
					i.putExtra(PARAM_MSG_TYPE, msgType);
					i.putExtra(FormTemplate.COL_CODE, formCode);
					i.putExtra(ALERT_ID, alertId);
					i.putExtra(MESSAGE, message);
					i.putExtra(C2DM_RECEIVED_TS, (new Date()).getTime()); // use one ts per request..
					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					getApplication().startActivity(i);
				} else {
					Log.w(Constants.LOG_TAG, "invalid form code: "+formCode);
				}
				
			} else if (CUSTOM_MESSAGE.equals(msgType)) {
				
				
				Intent i = new Intent(context, CustomMessageActivity.class);
				i.putExtra(PARAM_MSG_TYPE, msgType);
				i.putExtra(MESSAGE, message);
				i.putExtra(ALERT_ID, alertId);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getApplication().startActivity(i);
			}
		}
	}
}
