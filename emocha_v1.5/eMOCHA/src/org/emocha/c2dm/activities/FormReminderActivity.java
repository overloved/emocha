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
import org.emocha.activities.PersonalMenu;
import org.emocha.c2dm.C2DMReceiver;
import org.emocha.c2dm.tasks.ConfirmAlertTask;
import org.emocha.midot.R;
import org.emocha.model.entities.FormTemplate;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * FormReminderActivity shows an alert dialog reminding patient to fill the form
 * @author 
 *
 */
public class FormReminderActivity extends Activity {
	private static final String SHOW_FORM_REMINDER= "show_form_reminder";
	
	@Override
	protected void onCreate(Bundle b) {
	    
		super.onCreate(b);
		Intent i = getIntent();
		Bundle bundle = i.getExtras();
		
		if (bundle != null) {
			
			String msgType = bundle.getString(C2DMReceiver.PARAM_MSG_TYPE);
			
			if (C2DMReceiver.FORM_REMINDER.equals(msgType)) {
				if (bundle.containsKey(C2DMReceiver.MESSAGE) && bundle.containsKey(C2DMReceiver.ALERT_ID)) {
					String msg = bundle.getString(C2DMReceiver.MESSAGE);
					String alertId = bundle.getString(C2DMReceiver.ALERT_ID);
					String formCode = bundle.getString(FormTemplate.COL_CODE);
					
					//1.- show notification
					showGCMNotification(alertId, msg, formCode);
					//2.- confirm  alert 
					new ConfirmAlertTask(getApplicationContext()).execute(alertId);
					
					finish();
				} else {
					Log.e(Constants.LOG_TAG, "custom message without content????");
				}
			}  
			/* TODO not used
			else if (SHOW_FORM_REMINDER.equals(msgType)) {
				
				if (bundle.containsKey(C2DMReceiver.MESSAGE) && bundle.containsKey(FormTemplate.COL_CODE)) {
					showFormReminder(getString(R.string.custom_message_title), 
									bundle.getString(C2DMReceiver.MESSAGE),
									bundle.getString(FormTemplate.COL_CODE));
				} else {
					Log.e(Constants.LOG_TAG, "custom message without content????");
				}
			}
			*/
		} else {
			Log.e(Constants.LOG_TAG,"FormReminderActivity received an empty bundle???");
		}

		//add a button to be able to go back..
		setContentView(R.layout.push_notification);
		Button button = (Button) findViewById(R.id.back_button);
		button.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	            Intent i = new Intent(getApplicationContext(), PersonalMenu.class);
	            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // reuse existing task
	            startActivity(i);
	            finish();
	          }
	    });
	}

	/** Creates a notification on the status bar */
	private void showGCMNotification(String alertId, String message, String code){
		Context context = getApplicationContext();
		NotificationManager notifMgr = (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);
		
		//prepare intent to display the message	
		Intent intent = new Intent (context, FormReminderActivity.class);
		intent.putExtra(C2DMReceiver.PARAM_MSG_TYPE, SHOW_FORM_REMINDER);
		intent.putExtra(C2DMReceiver.MESSAGE, message);
		intent.putExtra(FormTemplate.COL_CODE, code);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
		NotificationCompat.Builder builder =  
		        new NotificationCompat.Builder(this)  
		        .setSmallIcon(R.drawable.icon_c2dm)  
		        .setContentTitle(getString(R.string.app_name))  
		        .setContentText(message)
		        .setContentIntent(contentIntent);  
		
		Notification notif = builder.build();
		notif.defaults = Notification.DEFAULT_ALL;
		notifMgr.notify(Integer.parseInt(alertId), notif);
	}
	
	/**Shows the received message within an alert dialog */
	/* TODO not used
	private void showFormReminder(String title, String message, final String fCode) {
		final AlertDialog alertDialog = new AlertDialog.Builder(FormReminderActivity.this).create();
		DialogInterface.OnClickListener buttonListener =
		            new DialogInterface.OnClickListener() {
		                public void onClick(DialogInterface dialog, int i) {
		                 switch (i) {
		                     case DialogInterface.BUTTON_POSITIVE:
		                    	 dialog.dismiss();
		                    	 startEmptyForm(getApplicationContext(), fCode);
		                    	 finish();
		                    	 break;
		                     case DialogInterface.BUTTON_NEGATIVE:
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
		alertDialog.setButton(getString(R.string.ok),buttonListener);
		alertDialog.setButton2(getString(R.string.cancel),buttonListener);
		alertDialog.show();
	}
	*/
	/* TODO not used
	private void showError(Context context, String title, String message) {
		final AlertDialog alertDialog = new AlertDialog.Builder(FormReminderActivity.this).create();
		DialogInterface.OnClickListener buttonListener =
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int i) {
                	switch (i) {
                    	case DialogInterface.BUTTON_POSITIVE:
                    		dialog.dismiss();
                    		finish();
                    		break;
                	}
                }
            };
        alertDialog.setIcon(R.drawable.alert_icon);
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.setButton(context.getString(R.string.ok),buttonListener);
		alertDialog.show();
	}
	*/
	
	/* TODO not used
	private void startEmptyForm(Context context, String formCode) {

		DBAdapter.init(context);
		FormTemplate ft = FormTemplate.getFormTemplateByCode(formCode);
		
		if (ft != null) {
			
			String formPath = File.getPath(ft.fileId);
		
			if (!Constants.EMPTY_STRING.equals(formPath)) {
				
				//put required info
				Bundle bundle = new Bundle();
				bundle.putString(Patient.TABLE_NAME, Integer.toString(MiDOTUtils.getPatient(context).id)); 
				bundle.putString(FormTemplate.TABLE_NAME, ft.code);
				
				//we want ODK to save the data for us (don't have onActivity result!)
				bundle.putBoolean(Constants.ODK_PERSIST_DATA, true);
				
				//TODO : in case the method is used again, MAKE SURE real instancePath is provided!! (and create the folder before calling prepareODKIntent.. 
				Intent intent = CommonUtils.prepareODKIntent(getApplicationContext(), bundle, Constants.EMPTY_STRING, Constants.EMPTY_STRING, formPath);	

				//call ODK from outside an activity.. flag is required
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getApplication().startActivity(intent);
				
			} else {
				showError(context, getString(R.string.open_template_error), getString(R.string.bad_form_code));
			}
		} else {
			showError(context, getString(R.string.open_template_error), getString(R.string.bad_template));
		}
	}
	*/
}
