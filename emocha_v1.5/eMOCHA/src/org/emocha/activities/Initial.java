package org.emocha.activities;

import org.emocha.Constants;
import org.emocha.midot.R;
import org.emocha.model.DBAdapter;
import org.emocha.model.Preferences;
import org.emocha.model.entities.File;
import org.emocha.model.entities.FormData;
import org.emocha.model.entities.FormTemplate;
import org.emocha.model.entities.Patient;
import org.emocha.model.entities.PatientData;

import org.emocha.services.ServerService;
import org.emocha.utils.CommonUtils;
import org.emocha.utils.FileUtils;
import org.emocha.utils.MiDOTUtils;
import org.emocha.utils.ScreenEnvironment;
import org.emocha.utils.Server;
import com.google.android.c2dm.C2DMessaging;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Initial is an initial class of miDOT which shows the main page
 * with miDOT and emocha Logos.
 * 
 * @author Yao Huang
 * 
 */
public class Initial extends Activity {
	
	private static final int MAIN_MENU_RC = 1;
	private FormData formData = null;
	private PatientData pData = new PatientData();
	private Patient patient = null;
	private PendingIntent serverService = null;
	
	private ImageView appMidotLogoImageView;
	private ImageView appEmochaLogoImageView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Context context = getApplicationContext();
		DBAdapter.init(context);
		
		appMidotLogoImageView = (ImageView) findViewById(R.id.appMidotLogo);
		appEmochaLogoImageView = (ImageView) findViewById(R.id.appEmochaTM);
		
		Display display = getWindowManager().getDefaultDisplay();
		ScreenEnvironment.width = display.getWidth();
	    ScreenEnvironment.height = display.getHeight();
	    
	    RelativeLayout.LayoutParams midotLogoParams = new RelativeLayout.
	    		LayoutParams((int) (ScreenEnvironment.width * 0.57), (int) (ScreenEnvironment.width * 0.57));
	    midotLogoParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
	    midotLogoParams.topMargin = (int) (ScreenEnvironment.height * 0.215);
	    appMidotLogoImageView.setLayoutParams(midotLogoParams);
	    
	    RelativeLayout.LayoutParams emochaLogoParams = new RelativeLayout.
	    		LayoutParams((int)(ScreenEnvironment.width * 0.35), (int) (ScreenEnvironment.height * 0.043));
	    emochaLogoParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
	    emochaLogoParams.topMargin = (int)(ScreenEnvironment.height * 0.85);
	    appEmochaLogoImageView.setLayoutParams(emochaLogoParams);
	    
//		Handler handler = new Handler();
//		handler.postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				Intent intent = new Intent(Initial.this, PersonalMenu.class);
//				startActivity(intent);
//				
//			}			
//		}, 3000);
	    
	  //1.- get the patient to be used onActivityResult()
    	patient = MiDOTUtils.getPatient(context);
		
		//start ServerService (gets config values and updates form templates)
    	AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		if (Preferences.getNetworkActive(context)) {
			serverService = PendingIntent.getService(context, 0, new Intent(context, ServerService.class), 0);
			am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 
							SystemClock.elapsedRealtime(), 
							Preferences.getUpdateConfigInterval(context), serverService);
		} else { //cancel the alarm when network is off!
			if (serverService != null) {
				am.cancel(serverService);
			}
		}
	    
	    appMidotLogoImageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startEmptyForm(MiDOTUtils.MIDOT_DAILY_FORM_CODE);
//				Intent intent = new Intent(Initial.this, SyncProcess.class);
//				startActivity(intent);
			}
		});
	}
	
	@Override
	protected void onResume() {
		Context context = getApplicationContext();
		
		//GCM registration
	    checkGCMRegistration(context);
		super.onResume();
	}
	
	/** Check whether current phone has been registered in server.
	 * @param context
	 */
	private void checkGCMRegistration(Context context) {
		if(Preferences.getUserValidated(context)) {
			if (Constants.EMPTY_STRING.equals(Preferences.getGCMRegistrationId(context))) {
				C2DMessaging.register(context, Preferences.getGoogleApiProjectId(context));
			} else {
				if (!Preferences.getC2DMServerRegistered(context)) { //phone was not registered yet in the server..
				Server.registerAlerts(Preferences.getGCMRegistrationId(context));
				}
			}
		}
	} 	
	
	/** Start an empty form.
	 * @param formCode The identification of which the form should be loaded.
	 */
	private void startEmptyForm(String formCode) {

		//get template & its path
		FormTemplate ft = FormTemplate.getFormTemplateByCode(formCode);
		String formPath = File.getPath(ft.fileId);
		
		if (!Constants.EMPTY_STRING.equals(formPath)) {
			Context context = getApplicationContext();
			//put required info
			Bundle bundle = new Bundle();
			bundle.putString(Patient.TABLE_NAME, Integer.toString(patient.id)); 
			bundle.putString(FormTemplate.TABLE_NAME, ft.code);
			
			//we don't want to save data when calling from here..
			bundle.putBoolean(Constants.ODK_PERSIST_DATA, false);
			
			String instancePath = CommonUtils.getInstancePath(context);

			if (!FileUtils.createFolder(instancePath)) {
				Log.e(Constants.LOG_TAG, "Error creating instancePath!: "+instancePath);
			}
					
			Intent intent = CommonUtils.prepareODKIntent(context, bundle, Constants.EMPTY_STRING, instancePath, formPath);	
			
			//call ODK with proper request code
			startActivityForResult(intent, MAIN_MENU_RC); 
		} else {
			CommonUtils.showAlertMessage(this,
					getString(R.string.open_template_error),
					getString(R.string.template_not_found_error,formPath));
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Context context = getApplicationContext();
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == MAIN_MENU_RC) { // 
				 Bundle response = data.getExtras();
	
				if (response != null && response.containsKey(Constants.ODK_INSTANCEXML_KEY)) {
					
					//1.- get response's data
					Bundle bundle = response.getBundle(Constants.ODK_EXTRA_DATA_KEY);
					//save data only when required
					if (!bundle.getBoolean(Constants.ODK_PERSIST_DATA)) {
					
						String xml = response.getString(Constants.ODK_INSTANCEXML_KEY);
						String instancePath = response.getString(Constants.ODK_INSTANCEPATH_KEY);
						
						int patientId = Integer.parseInt(bundle.getString(Patient.TABLE_NAME));
						
						String tCode = bundle.getString(FormTemplate.TABLE_NAME); 
						
						DBAdapter.beginTransaction();
						try {
							//prepare xml
							formData = new FormData(tCode, xml, instancePath,  CommonUtils.getCurrentTime(context), Constants.ONE);
							
							//do the insert: EXACT doesn't edit forms.
							formData.id = DBAdapter.insert(FormData.TABLE_NAME, formData.getContentValues());
							pData = new PatientData(patientId, formData.id);
							DBAdapter.insert(PatientData.TABLE_NAME, pData.getContentValues());
							
							//look for form associated files
							CommonUtils.postProcessXForm(context, formData.id, xml);
							
							DBAdapter.setTransactionSuccessful();
						} catch (Exception e){
							Log.e(Constants.LOG_TAG, context.getString(R.string.transaction_error, e.getMessage()));
						} finally {
							DBAdapter.endTransaction();
						}
					}	
				} else {
					Log.w(Constants.LOG_TAG, "ODK response contains no data");
				}
			} 
		} else {
			Log.w(Constants.LOG_TAG, getText(R.string.odk_cancelled).toString());
		}
		super.onActivityResult(requestCode, resultCode, data);
		if (MiDOTUtils.FORM_SUBMITTED) {
			Intent intent = new Intent(Initial.this, SyncProcess.class);
			startActivity(intent);
		} 
	}
}
