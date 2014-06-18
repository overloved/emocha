/*******************************************************************************
 * eMOCHA - electronic Mobile Open-Source Comprehensive Health Application
 * Copyright (c) 2012  Pau Varela - pau.varela@gmail.com
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
package org.emocha.c2dm.activities;


import org.emocha.Constants;
import org.emocha.activities.Initial;
import org.emocha.activities.PersonalMenu;
import org.emocha.c2dm.C2DMReceiver;
import org.emocha.c2dm.tasks.ConfirmAlertTask;
import org.emocha.midot.R;
import org.emocha.model.entities.File;
import org.emocha.model.entities.FormTemplate;
import org.emocha.model.entities.Patient;
import org.emocha.utils.CommonUtils;
import org.emocha.utils.FileUtils;
import org.emocha.utils.MiDOTUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * CustomMessageActivity shows the customized view when the cloud alert message
 * has been sent to patient's mobile device. 
 * @author 
 *
 */
public class CustomMessageActivity extends Activity {
	private static final String SHOW_CUSTOM_MESSAGE = "show_custom_message";
	private static final int CUSTOM_MSG_RC = 3;
	
	@Override
	protected void onCreate(Bundle b) {
	    
		super.onCreate(b);
		Intent i = getIntent();
		Bundle bundle = i.getExtras();
		
		if (bundle != null) {
			
			String msgType = bundle.getString(C2DMReceiver.PARAM_MSG_TYPE);
							
			if (C2DMReceiver.CUSTOM_MESSAGE.equals(msgType)) {
				
				if (bundle.containsKey(C2DMReceiver.MESSAGE) && bundle.containsKey(C2DMReceiver.ALERT_ID)) {
					String msg = bundle.getString(C2DMReceiver.MESSAGE);
					String alertId = bundle.getString(C2DMReceiver.ALERT_ID);
					
					//1.- show notification
					showC2dmNotification(alertId, msg);
					//2.- confirm  alert 
					new ConfirmAlertTask(getApplicationContext()).execute(alertId);
					
					finish();
				} else {
					Log.e(Constants.LOG_TAG, "custom message without content????");
				}
			} else if (SHOW_CUSTOM_MESSAGE.equals(msgType)) {
				
				if (bundle.containsKey(C2DMReceiver.MESSAGE)) {
					showCustomMessage(getApplicationContext(), getString(R.string.custom_message_title), bundle.getString(C2DMReceiver.MESSAGE));
					NotificationManager notifMgr = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
					notifMgr.cancel(Integer.parseInt(bundle.getString(C2DMReceiver.ALERT_ID)));
				} else {
					Log.e(Constants.LOG_TAG, "custom message without content????");
				}
			}
		} else {
			Log.e(Constants.LOG_TAG,"CustomMessageActivity received an empty bundle???");
		}

		//add a button to be able to go back.. we shouldn't reach this point...
		setContentView(R.layout.push_notification);
		Button button = (Button) findViewById(R.id.back_button);
		button.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	        	Intent i = new Intent(getApplicationContext(), Initial.class);
	            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // reuse existing task
	            startActivity(i);
	            finish();
	          }
	    });
	}
	
	/** Creates a notification on the status bar */
	private void showC2dmNotification(String alertId, String message){
		Context context = getApplicationContext();
		NotificationManager notifMgr = (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);
		
		//prepare intent to display the message	
		Intent intent = new Intent (context, CustomMessageActivity.class);
		intent.putExtra(C2DMReceiver.PARAM_MSG_TYPE, SHOW_CUSTOM_MESSAGE);
		intent.putExtra(C2DMReceiver.MESSAGE, message);
		intent.putExtra(C2DMReceiver.ALERT_ID, alertId);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

		Notification notif = new Notification();
		NotificationCompat.Builder builder =  
		        new NotificationCompat.Builder(this)  
		        .setSmallIcon(R.drawable.icon_c2dm)  
		        .setContentTitle(getString(R.string.app_name))  
		        .setContentText(message)
		        .setContentIntent(contentIntent);  
		
		notif = builder.build();
		notif.defaults = Notification.DEFAULT_ALL;
		notifMgr.notify(Integer.parseInt(alertId), notif);
		
	}
	
	/**Shows the received message within an alert dialog */	
	private void showCustomMessage(Context context, String title, String message) {
		final AlertDialog alertDialog = new AlertDialog.Builder(CustomMessageActivity.this).create();
		DialogInterface.OnClickListener buttonListener =
		            new DialogInterface.OnClickListener() {
		                public void onClick(DialogInterface dialog, int i) {
		                 switch (i) {
		                     case DialogInterface.BUTTON_POSITIVE:
		                    	 
		                    	 // To do: change startEmptyForm to Initial.class
		                    	 startEmptyForm(MiDOTUtils.MIDOT_DAILY_FORM_CODE);

		                    	 dialog.dismiss();
		                    	 finish();
		                     break;
		                 }
		                }
		            };
		alertDialog.setIcon(R.drawable.alert_icon);
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.setCancelable(false);
		alertDialog.setButton(context.getString(R.string.ok),buttonListener);
		alertDialog.show();
	}
	
	/**
	 * Start a new form which contains all the information that patient should be filled
	 * in at the single time.
	 * @param formCode The code of the form.
	 */
	private void startEmptyForm(String formCode) {

		//get template & its path
		FormTemplate ft = FormTemplate.getFormTemplateByCode(formCode);
		String formPath = File.getPath(ft.fileId);
		
		if (!Constants.EMPTY_STRING.equals(formPath)) {
			Context context = getApplicationContext();
			//put required info
			Bundle bundle = new Bundle();
			Patient p = MiDOTUtils.getPatient(context);
			bundle.putString(Patient.TABLE_NAME, Integer.toString(p.id)); 
			bundle.putString(FormTemplate.TABLE_NAME, ft.code);
			
			//we don't want to save data when calling from here..
			bundle.putBoolean(Constants.ODK_PERSIST_DATA, true);
			
			String instancePath = CommonUtils.getInstancePath(context);

			if (!FileUtils.createFolder(instancePath)) {
				Log.e(Constants.LOG_TAG, "Error creating instancePath!: "+instancePath);
			}
					
			Intent intent = CommonUtils.prepareODKIntent(context, bundle, Constants.EMPTY_STRING, instancePath, formPath);	
			
			//call ODK with proper request code
			startActivityForResult(intent, CUSTOM_MSG_RC);
			
		} else {
			CommonUtils.showAlertMessage(this,
					getString(R.string.open_template_error),
					getString(R.string.template_not_found_error,formPath));
		}
	}
	
	/**@param requestCode: defines which activity comes from
	 * @param resultCode: ODK response
	 * @param data: extra data provided to ODK and retrieved back here.*/
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.e(Constants.LOG_TAG, "CustomMessageActivity.onActivityResult: ");
		Intent i = new Intent(getApplicationContext(), PersonalMenu.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // reuse existing task
        startActivity(i);
        finish();
        super.onActivityResult(requestCode, resultCode, data);
	}
	
}
