package org.emocha.utils;

import java.util.Calendar;

import org.emocha.Constants;
import org.emocha.model.DBAdapter;
import org.emocha.model.Preferences;
import org.emocha.model.entities.Patient;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * MiDOTUtils class is to get an instance of patient.
 * @author Yao
 *
 */
public class MiDOTUtils {
	//private static Object lock = new Object();
	public static String DEVICE_ID = "device_id";
	public static String MIDOT_DAILY_FORM_CODE = "midot";
	public static String MIDOT_WEEKLY_FORM_CODE = "mweek";
	/**
	 * Default value for whether or not the video has been recorded.
	 */
	public static Boolean VIDEO_FLAG = false;
	/**
	 * Default value for whether the form is submitted.
	 */
	public static Boolean FORM_SUBMITTED = true;
	
	/**
	 * Retrieve patient in the patient table.
	 * @param context
	 * @return Patient object.
	 */
	public static Patient getPatient(Context context){
		Patient patient = null;

		int numPatients = DBAdapter.count(Patient.TABLE_NAME);
		if (numPatients != 1) {
			if (numPatients > 1) {
				Log.w(Constants.LOG_TAG,"more than 1 patient ?!");
				//delete?
			}
			Log.d(Constants.LOG_TAG,"creating new patient");
			patient = createExactPatient(context);
			
		} else { //retrieve The patient (only 1)
			Cursor c = DBAdapter.query(Patient.TABLE_NAME, new String [] {BaseColumns._ID,Patient.COL_PATIENT_DATA_ID, Patient.COL_CODE, 
					Patient.COL_HOUSEHOLD_ID, Patient.COL_FIRST_NAME, Patient.COL_LAST_NAME,
					Patient.COL_AGE, Patient.COL_GENDER, Patient.COL_LAST_MODIFIED },
					null, null, null, null, null);
			c.moveToFirst();
			patient = new Patient(c.getInt(0), c.getInt(1), c.getString(2), c.getInt(3), c.getString(4), 
					c.getString(5),	c.getInt(6), c.getInt(7), c.getString(8));
			
			c.close();
		}
		
		return patient;
	}
	//just create a patient, with no bound to any formData (patient_data_id). This link will be created afterwards
	/**
	 * Create a patient, with no bound to any formData (patient_data_id). This link will be created afterwards.
	 * @param context
	 * @return Patient object.
	 */
	private static Patient createExactPatient(Context context) {
		Patient p = new Patient();
		p.code = Constants.PATIENT_CODE_PREFIX+Constants.DASH_STRING + Preferences.getPhoneId(context) + Constants.DASH_STRING +
		Constants.STANDARD_DATE_FORMAT.format(Calendar.getInstance().getTime());
		p.householdId = Constants.ZERO;
		
		p.id = DBAdapter.insert(Patient.TABLE_NAME, p.getContentValues());
		return p;
	}	
}
