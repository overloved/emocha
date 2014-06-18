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
package org.emocha.model;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.TimeZone;

import org.emocha.Constants;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import com.twmacinta.util.MD5;

/**
 * Preferences class is to configure all the information of the mobile device.
 * Such as identifying the advice, connecting to remote server, setting and 
 * getting the device password, etc.
 * @author 
 *
 */
public class Preferences {
	private static final String PREF_SERVER_URL = "pref_server_url";
	private static final String PREF_API_PASSWORD = "pref_api_password";
	private static final String PREF_NET_ACTIVE = "pref_net_active";
	private static final String PREF_LAST_GPS_POS = "pref_sys_last_gps_pos";
	private static final String PREF_PHONE_ID = "pref_sys_phone_id";
	private static final String PREF_USER_VALIDATED = "pref_user_validated";
	
	// following values are returned by the server to determinate last time there was an update in the server side:
	public static final String LAST_APP_CONFIG_UPDATE_TS = "last_app_config_upd";
	public static final String LAST_FORM_CONFIG_UPDATE_TS = "last_form_config_upd";
	
	// ## app config keys: ##
	private static final String APP_SELF_SIGNED_SSL = "self_signed_ssl"; // allows self signed ssl certificates.
	private static final String APP_UPD_DOWNLOAD_INTERVAL = "interval_upd_download";
	private static final String APP_CONFIG_UPDATE_INTERVAL="interval_config_update";
	private static final String APP_GPS_UPDATE_INTERVAL="interval_update_gps";
	
	//enables login screen from server side
	private static final String APP_DEVICE_PASSWORD_ON = "device_password_on";
	private static final String APP_DEVICE_PASSWORD = "device_password";
	
	///##### start EXACT specific ########///
	private static final String APP_GPS_DATA_UPDATE_INTERVAL="interval_update_gps_data";
	private static final String APP_GPS_MIN_DISTANCE = "min_gps_distance";
	private static final String APP_GPS_MAX_DISTANCE = "max_gps_distance";
	private static final String PREF_LAST_GPS_TS = "last_gps_ts";
	public static final String APP_GCM_REGISTRATION_ID = "c2dm_registration_id";
	private static final String APP_TIME_ZONE = "app_time_zone";
	private static final String APP_C2DM_SERVER_REGISTERED = "c2dm_server_registered";
	private static final String APP_FORM_REMINDER_DELAY_INTERVAL = "form_reminder_delay_interval";
	
	private static final String APP_GENERATE_ALARMS_LAST_RUN ="generate_alarm_last_run";
	private static final String APP_MAX_FORM_DISPLAY_TIME ="max_form_display_time";
	///##### end EXACT specific ########///
	
	//GCM messaging:
	private static final String APP_GOOGLE_API_PROJECT_ID = "google_api_project_id";
	
	
	private static String imei;
	private static String user;

	private static SharedPreferences sPrefs = null;
	
	/**
	 * Get the imei number of the mobile phone, and encrypt it for validation and security.
	 * @param context
	 */
	public static void init(Context context) {
		if (sPrefs == null) {
			sPrefs = PreferenceManager.getDefaultSharedPreferences(context);

			// Get the hashed IMEI code
			TelephonyManager phoneManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			imei = phoneManager.getDeviceId();

			MD5 md5 = new MD5();
			try {
				md5.Update(imei, null);
				user = md5.asHex();
			} catch (UnsupportedEncodingException e) {
				user = Constants.EMPTY_STRING;
			}
		}
	}

	public static void destroy() {
		sPrefs = null;
	}

	public static String getServerURL(Context context) {
	    init(context);
	    
	    //return sPrefs.getString(PREF_SERVER_URL, "http://192.168.1.37/midot");
	    return sPrefs.getString(PREF_SERVER_URL, "https://secure.emocha.org/dev/midot_es");
	}
	
	public static void setServerURL(Context context, String value) {
		init(context);
		SharedPreferences.Editor editor = sPrefs.edit();
		editor.putString(PREF_SERVER_URL, value);
		editor.commit();
	}
	
	public static String getServerPassword(Context context) {
		init(context);
		return sPrefs.getString(PREF_API_PASSWORD, Constants.EMPTY_STRING);
	}

	public static String getImei(Context context) {
		init(context);
		return imei;
	}
	
	public static String getUser(Context context) {
		init(context);
		return user;
	}

	public static Boolean getNetworkActive(Context context) {
		init(context);
		return sPrefs.getBoolean(PREF_NET_ACTIVE, true); //enabled by default
	}

	public static synchronized String getGpsPos(Context context) {
		init(context);
		return sPrefs.getString(PREF_LAST_GPS_POS, Constants.DOT_STRING);
	}

	public static synchronized void setGpsPos(String pos, Context context) {
		init(context);
		SharedPreferences.Editor editor = sPrefs.edit();
		editor.putString(PREF_LAST_GPS_POS, pos);
		editor.commit();
	}

	
	public static String getPhoneId(Context context) {
		init(context);
		return sPrefs.getString(PREF_PHONE_ID, Constants.ZERO_STRING);
	}

	public static void setPhoneId(String id, Context context) {
		init(context);
		SharedPreferences.Editor editor = sPrefs.edit();
		editor.putString(PREF_PHONE_ID, id);
		editor.commit();
	}
		
	public static String getLastAppConfigUpdateTimestamp(Context context) {
		init(context);
		return sPrefs.getString(LAST_APP_CONFIG_UPDATE_TS, Constants.ZERO_STRING);
	}
	
	public static void setLastAppConfigUpdateTimestamp(Context context, String ts) {
		init(context);
		SharedPreferences.Editor editor = sPrefs.edit();
		editor.putString(LAST_APP_CONFIG_UPDATE_TS, ts);
		editor.commit();
	}
	
	public static String getLastFormConfigUpdateTimestamp(Context context) {
		init(context);
		return sPrefs.getString(LAST_FORM_CONFIG_UPDATE_TS, Constants.ZERO_STRING);
	}
	
	public static void setLastFormConfigUpdateTimestamp(Context context, String ts) {
		init(context);
		SharedPreferences.Editor editor = sPrefs.edit();
		editor.putString(LAST_FORM_CONFIG_UPDATE_TS, ts);
		editor.commit();
	}
	
	//PREF_USER_VALIDATED
	public static boolean getUserValidated(Context context) {
		init(context);
		return sPrefs.getBoolean(PREF_USER_VALIDATED, false);
	}
	
	public static void setUserValidated(Context context, boolean value) {
		init(context);
		SharedPreferences.Editor editor = sPrefs.edit();
		editor.putBoolean(PREF_USER_VALIDATED, value);
		editor.commit();
	}

	
	//## application config values ##

	// APP_SELF_SIGNED_SSL
	public static boolean getSelfSignedSSLCertificate(Context context) {
		init(context);
		return (Integer.parseInt(sPrefs.getString(APP_SELF_SIGNED_SSL, Constants.MINUS_ONE_STRING)) > 0);
	}
	
	private static void setSelfSignedSSLCertificate(Context context, String value) {
		init(context);
		SharedPreferences.Editor editor = sPrefs.edit();
		editor.putString(APP_SELF_SIGNED_SSL, value);
		editor.commit();
	}
	
	//APP_SERVER_UPDOWN_INTERVAL
	public static Long getServerUpDownInterval(Context context) {
		init(context);
		return sPrefs.getLong(APP_UPD_DOWNLOAD_INTERVAL, 60*Constants.ONE_SECOND);
		
	}
	private static void setServerUpDownInterval(Context context, Long value) {
		init(context);
		SharedPreferences.Editor editor = sPrefs.edit();
		editor.putLong(APP_UPD_DOWNLOAD_INTERVAL, value*Constants.ONE_SECOND);
		editor.commit();
	}
	
	//APP_CONFIG_UPDATE_INTERVAL
	public static Long getUpdateConfigInterval(Context context) {
		init(context);
		return sPrefs.getLong(APP_CONFIG_UPDATE_INTERVAL, 120*Constants.ONE_SECOND);
		
	}
	private static void setUpdateConfigInterval(Context context, Long value) {
		init(context);
		SharedPreferences.Editor editor = sPrefs.edit();
		editor.putLong(APP_CONFIG_UPDATE_INTERVAL, value*Constants.ONE_SECOND);
		editor.commit();
	}
		
	//APP_GPS_UPDATE_INTERVAL
	public static Long getUpdateGpsInterval(Context context) {
		init(context);
		return sPrefs.getLong(APP_GPS_UPDATE_INTERVAL, 60*Constants.ONE_SECOND);
		
	}
	private static void setUpdateGpsInterval(Context context, Long value) {
		init(context);
		SharedPreferences.Editor editor = sPrefs.edit();
		editor.putLong(APP_GPS_UPDATE_INTERVAL, value*Constants.ONE_SECOND);
		editor.commit();
	}
	
	//APP_DEVICE_PASSWORD_ON
	public static String getDevicePasswordOn(Context context) {
		init(context);
		return sPrefs.getString(APP_DEVICE_PASSWORD_ON, Constants.MINUS_ONE_STRING);
	}
	
	private static void setDevicePasswordOn(Context context, String value) {
		init(context);
		SharedPreferences.Editor editor = sPrefs.edit();
		editor.putString(APP_DEVICE_PASSWORD_ON, value);
		editor.commit();
	}
	// ## end application config values
	
	///##### start EXACT specific ########///
	//APP_GPS_DATA_UPDATE_INTERVAL
	public static Long getUpdateGpsDataInterval(Context context) {
		init(context);
		return sPrefs.getLong(APP_GPS_DATA_UPDATE_INTERVAL, 60*Constants.ONE_SECOND);
	}
	private static void setUpdateGpsDataInterval(Context context, Long value) {
		init(context);
		SharedPreferences.Editor editor = sPrefs.edit();
		editor.putLong(APP_GPS_DATA_UPDATE_INTERVAL, value*Constants.ONE_SECOND);
		editor.commit();
	}
	//APP_GPS_MIN_DISTANCE
	public static Long getGpsMinDistance(Context context) {
		init(context);
		return sPrefs.getLong(APP_GPS_MIN_DISTANCE, 200);
	}
	private static void setGpsMinDistance(Context context, Long value) {
		init(context);
		SharedPreferences.Editor editor = sPrefs.edit();
		editor.putLong(APP_GPS_MIN_DISTANCE, value);
		editor.commit();
	}
	
	//APP_GPS_MAX_DISTANCE
	public static Long getGpsMaxDistance(Context context) {
		init(context);
		return sPrefs.getLong(APP_GPS_MAX_DISTANCE, 10000);
	}
	private static void setGpsMaxDistance(Context context, Long value) {
		init(context);
		SharedPreferences.Editor editor = sPrefs.edit();
		editor.putLong(APP_GPS_MAX_DISTANCE, value);
		editor.commit();
	}
	
	//PREF_LAST_GPS_TS: used to control how often do we record points....
	public static synchronized Long getLastGpsTimestamp(Context context) {
		init(context);
		return sPrefs.getLong(PREF_LAST_GPS_TS, Constants.ZERO);
	}

	public static synchronized void setLastGpsTimestamp(Long ts, Context context) {
		init(context);
		SharedPreferences.Editor editor = sPrefs.edit();
		editor.putLong(PREF_LAST_GPS_TS, ts);
		editor.commit();
	}
	
	//APP_TIME_ZONE
	public static String getAppTimezone(Context context) {
		init(context);
		return sPrefs.getString(APP_TIME_ZONE, null);
	}
	private static void setAppTimezone(Context context, String value) {
		init(context);
		SharedPreferences.Editor editor = sPrefs.edit();
		editor.putString(APP_TIME_ZONE, value);
		editor.commit();
	}
	
	//APP_FORM_REMINDER_DELAY_INTERVAL
	public static int getFormReminderDelayInterval(Context context) {
		init(context);
		return sPrefs.getInt(APP_FORM_REMINDER_DELAY_INTERVAL, 900*Constants.ONE_SECOND); //default 15min
	}
	private static void setFormReminderDelayInterval(Context context, int value) {
		init(context);
		SharedPreferences.Editor editor = sPrefs.edit();
		editor.putInt(APP_FORM_REMINDER_DELAY_INTERVAL, value*Constants.ONE_SECOND);
		editor.commit();
	}
	
	//APP_GENERATE_ALARM_LAST_RUN
	public static String getGenerateAlarmsLastRun(Context context) {
		init(context);
		return sPrefs.getString(APP_GENERATE_ALARMS_LAST_RUN, Constants.EMPTY_STRING);
	}
	
	public static void setGenerateAlarmsLastRun(Context context, String value) {
		init(context);
		SharedPreferences.Editor editor = sPrefs.edit();
		editor.putString(APP_GENERATE_ALARMS_LAST_RUN, value);
		editor.commit();
	}
	//APP_MAX_FORM_DISPLAY_TIME
	public static long getMaxFormDisplayTime(Context context) {
		init(context);
		return sPrefs.getLong(APP_MAX_FORM_DISPLAY_TIME, 1800*Constants.ONE_SECOND);
	}
	
	private static void setMaxFormDisplayTime(Context context, long value) {
		init(context);
		SharedPreferences.Editor editor = sPrefs.edit();
		editor.putLong(APP_MAX_FORM_DISPLAY_TIME, value*Constants.ONE_SECOND);
		editor.commit();
	}
	///##### end  specific ########///
	
	//APP_C2DM_SERVER_REGISTERED
	public static boolean getC2DMServerRegistered(Context context) {
		init(context);
		return sPrefs.getBoolean(APP_C2DM_SERVER_REGISTERED, false);
	}
	
	public static void setC2DMServerRegistered(Context context, boolean value){
		init(context);
		SharedPreferences.Editor editor = sPrefs.edit();
		editor.putBoolean(APP_C2DM_SERVER_REGISTERED, value);
		editor.commit();
	}
	
	//APP_GOOGLE_API_PROJECT_ID
	public static String getGoogleApiProjectId(Context context) {
		init(context);
		return sPrefs.getString(APP_GOOGLE_API_PROJECT_ID, Constants.EMPTY_STRING);
	}

	private static void setGoogleApiProjectId(Context context, String value){
		init(context);
		SharedPreferences.Editor editor = sPrefs.edit();
		editor.putString(APP_GOOGLE_API_PROJECT_ID, value);
		editor.commit();
	}
	
	//APP_C2DM_REGISTRATION_ID
	public static String getGCMRegistrationId(Context context) {
		init(context);
		return sPrefs.getString(APP_GCM_REGISTRATION_ID, Constants.EMPTY_STRING);
	}
	public static void setGCMRegistrationId(Context context, String value) {
		init(context);
		SharedPreferences.Editor editor = sPrefs.edit();
		editor.putString(APP_GCM_REGISTRATION_ID, value);
		editor.commit();
	}
	
	
	/**
	 * Get deveice password.
	 * @param context
	 * @return Device password.
	 */
	public static String getDevicePassword(Context context) {
		init(context);
		return sPrefs.getString(APP_DEVICE_PASSWORD, Constants.EMPTY_STRING);
	}

	/** 
	 * Set password to the device.
	 * @param context
	 * @param value The password should be set.
	 */
	public static void setDevicePassword(Context context, String value) {
		init(context);
		SharedPreferences.Editor editor = sPrefs.edit();
		editor.putString(APP_DEVICE_PASSWORD, value);
		editor.commit();
	}
	
	/**updates application config in the SharedPreferences.
	 * @param	context	the application context
	 * @return <b>true</b> when the update is successful, <b>false</b> otherwise*/
	@SuppressWarnings("unchecked")
	public static boolean updateAppConfig(Context context, JSONObject config) {
		boolean response = true;
		try {
			Iterator<String> it = config.keys();
			while (it.hasNext()) {
				String key = it.next();
				if (APP_SELF_SIGNED_SSL.equals(key)) {
					setSelfSignedSSLCertificate(context, config.getString(APP_SELF_SIGNED_SSL));
				} else if (APP_UPD_DOWNLOAD_INTERVAL.equals(key)) {
					setServerUpDownInterval(context, config.getLong(APP_UPD_DOWNLOAD_INTERVAL));
				} else if(APP_CONFIG_UPDATE_INTERVAL.equals(key)){
					setUpdateConfigInterval(context, config.getLong(APP_CONFIG_UPDATE_INTERVAL));
				} else if(APP_GPS_UPDATE_INTERVAL.equals(key)){
					setUpdateGpsInterval(context, config.getLong(APP_GPS_UPDATE_INTERVAL));
				} else if (APP_DEVICE_PASSWORD_ON.equals(key)) {
					setDevicePasswordOn(context, config.getString(APP_DEVICE_PASSWORD_ON));
				}
				//EXACT specific
				else if(APP_GPS_DATA_UPDATE_INTERVAL.equals(key)){
					setUpdateGpsDataInterval(context, config.getLong(APP_GPS_DATA_UPDATE_INTERVAL));
				} else if (APP_GPS_MIN_DISTANCE.equals(key)) {
					setGpsMinDistance(context, config.getLong(APP_GPS_MIN_DISTANCE));
				} else if (APP_GPS_MAX_DISTANCE.equals(key)) {
					setGpsMaxDistance(context, config.getLong(APP_GPS_MAX_DISTANCE));
				} else if (APP_TIME_ZONE.equals(key)) {
					setAppTimezone(context, config.getString(APP_TIME_ZONE));
					//change app time zone..
					Constants.STANDARD_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone(getAppTimezone(context)));
				} else if (APP_GOOGLE_API_PROJECT_ID.equals(key)) {
					setGoogleApiProjectId(context, config.getString(APP_GOOGLE_API_PROJECT_ID));
					//Log.d(Constants.LOG_TAG, "APP_GOOGLE_API_PROJECT_ID updated to: "+getGoogleApiProjectId(context));//
				} /*else if (APP_C2DM_SENDER_ID.equals(key)) {
					setC2DMSenderId(context, config.getString(APP_C2DM_SENDER_ID));
				}*/ else if (APP_FORM_REMINDER_DELAY_INTERVAL.equals(key)) {
					setFormReminderDelayInterval(context, config.getInt(APP_FORM_REMINDER_DELAY_INTERVAL));
				} else if (APP_MAX_FORM_DISPLAY_TIME.equals(key)) {
					setMaxFormDisplayTime(context, config.getLong(APP_MAX_FORM_DISPLAY_TIME));
					//Log.d(Constants.LOG_TAG, "APP_MAX_FORM_DISPLAY_TIME updated to: "+getMaxFormDisplayTime(context));
				}
				// end EXACT
				/*
				else {
					Log.w(Constants.LOG_TAG,"key ["+key+"] is defined in the server but not used in the device!");
				}
				*/
			}
		} catch (JSONException ex) {
//			Log.w(Constants.LOG_TAG, "updateAppConfig error: "+ex.getMessage());
			response = false;
		} 
		return response;
	}
}
