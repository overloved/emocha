/*******************************************************************************
 * eMOCHA - electronic Mobile Open-Source Comprehensive Health Application
 * Copyright (c) 2010 Pau Varela - pau.varela@gmail.com
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
package org.emocha.model.entities;

import java.util.Calendar;

import org.emocha.Constants;
import org.emocha.model.DBAdapter;
import org.emocha.model.Preferences;
import org.emocha.utils.CommonUtils;
import org.emocha.xml.XmlReader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Log;

/** Patient info.
 * @author 
 *
 */
public class Patient {
	public static final String PATIENT_FORM_CODE = "pcore";
	
	public static final String TABLE_NAME = "patient";
	public static final String COL_PATIENT_DATA_ID = "patient_data_id";
	public static final String COL_CODE = "code";
	public static final String COL_HOUSEHOLD_ID = "household_id";
	public static final String COL_FIRST_NAME = "first_name";
	public static final String COL_LAST_NAME = "last_name";
	public static final String COL_AGE = "age";
	public static final String COL_GENDER = "gender";
	public static final String COL_LAST_MODIFIED = "last_modified";
	
	public static final String TABLE_CREATE =
		"CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
		+ BaseColumns._ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "
		+ COL_PATIENT_DATA_ID +" INTEGER, "
		+ COL_CODE + " TEXT, "
		+ COL_HOUSEHOLD_ID + " INTEGER, "
		+ COL_FIRST_NAME + " TEXT, "
		+ COL_LAST_NAME + " TEXT, "
		+ COL_AGE + " INTEGER, "
		+ COL_GENDER + " INTEGER, "
		+ COL_LAST_MODIFIED + " TEXT"
		+");";
	
	public int id;
	public int patientDataId;
	public int householdId;
	public String code;
	public String firstName;
	public String lastName;
	public int age;
	public int gender;
	public String lastModified;

	/**
	 * Class constructor.
	 */
	public Patient() {}
	
	/** 
	 * Class constructor.
	 * @param fName First name of patient.
	 * @param lName Last name of patient.
	 */
	public Patient (String fName, String lName) {
		this.id = -1;
		this.patientDataId = -1;
		this.firstName = fName;
		this.lastName = lName;
	}
	
	/**
	 * Class constructor.
	 * @param id Patient id.
	 * @param pDataId Patient data id.
	 * @param code Patient code.
	 * @param househId Household id.
	 * @param fName First name of patient.
	 * @param lName Last name of patient.
	 * @param age Age of patient.
	 * @param sex Sex of patient.
	 * @param ts The date of the info which is last modified.
	 */
	public Patient(int id, int pDataId, String code, int househId, String fName, String lName, int age, int sex, String ts ) {
		this.id = id;
		this.patientDataId = pDataId;
		this.householdId = househId;
		this.code = code;
		this.firstName = fName;
		this.lastName = lName;
		this.age = age;
		this.gender = sex;
		this.lastModified = ts;
	}
	
	/**
	 * Store a set of values of patient.
	 * @return Set of values
	 */
	public ContentValues getContentValues(){
		ContentValues values = new ContentValues();
		values.put(COL_PATIENT_DATA_ID, patientDataId);
		values.put(COL_CODE, code);
		values.put(COL_HOUSEHOLD_ID, householdId);
		values.put(COL_FIRST_NAME, firstName);
		values.put(COL_LAST_NAME, lastName);
		values.put(COL_AGE, age);
		values.put(COL_GENDER, gender);
		values.put(COL_LAST_MODIFIED, lastModified);
		return values;
	}
	
	/**
	 * Get all patients info by household id.
	 * @param householdId 
	 * @return List of patients.
	 */
	public static Patient[] getPatientListView(int householdId) {
		Patient[] result = null;
		
		Cursor c;
		if (householdId > 0) { // get patients for the given household
			c = DBAdapter.query(TABLE_NAME, new String[] {BaseColumns._ID,COL_PATIENT_DATA_ID, COL_CODE, COL_HOUSEHOLD_ID, 
				COL_FIRST_NAME, COL_LAST_NAME, COL_AGE, COL_GENDER, COL_LAST_MODIFIED },
				COL_HOUSEHOLD_ID+Constants.EQUALS_STRING+householdId, null, null, null, null);
		} else { // get all patients
			c = DBAdapter.query(TABLE_NAME, new String[] {BaseColumns._ID,COL_PATIENT_DATA_ID, COL_CODE, COL_HOUSEHOLD_ID, 
					COL_FIRST_NAME, COL_LAST_NAME, COL_AGE, COL_GENDER, COL_LAST_MODIFIED },
					null, null, null, null, null);
		}
		
		int len = c.getCount(); 
		if (len > 0) {
			result = new Patient[len];
			c.moveToFirst();
			for (int i = 0; i < len; i++) {
				result[i] = new Patient(c.getInt(0), c.getInt(1), c.getString(2), c.getInt(3), c.getString(4), 
										c.getString(5),	c.getInt(6), c.getInt(7), c.getString(8));
				c.moveToNext();
			}
		}
		c.close();
		return result;
	}
	
	/**
	 * Get single patient by id.
	 * @param id
	 * @return Patient object.
	 */
	public Patient get(int id) {
		Patient patient = null;
		
		Cursor c = DBAdapter.query(TABLE_NAME, new String[] {BaseColumns._ID, COL_PATIENT_DATA_ID, COL_CODE, COL_HOUSEHOLD_ID, 
				COL_FIRST_NAME, COL_LAST_NAME, COL_AGE, COL_GENDER, COL_LAST_MODIFIED },
				BaseColumns._ID+Constants.EQUALS_STRING+id,	null, null, null, null);
		
		int len = c.getCount();
		if (len == 1) {
			c.moveToFirst();
			patient = new Patient(c.getInt(0), c.getInt(1), c.getString(2), c.getInt(3), c.getString(4), c.getString(5), c.getInt(6), c.getInt(7), c.getString(8));
		}
		c.close();
		return patient;
	}
	
	/**gets a patient from xml data: Inserts a new patient when it doesn't exist (patient.id < 0) 
	 * and updates patient's content from the xml data.
	 * @param 	context	application's context
	 * @param	householdId where the patient belongs
	 * @param 	patientId
	 * @param	instancePath	where form data might be stored*/
	public Patient getPatientFromXml(Context context, String xml, int householdId, int patientId, String instancePath) {
		Patient patient = null;
		
		if (patientId < 0) { //new patient
				
			patient = createPatient(context, xml, householdId, instancePath);
		} else { //existent patient
			patient = get(patientId);
			
			if (patient == null) {
				throw new RuntimeException("updating a non existent patient with id: "+patientId);
			}
		}
		patient.firstName = XmlReader.getStringValueFromXml(xml, Constants.ODK_PATIENT_FIRST_NAME);
		patient.lastName =  XmlReader.getStringValueFromXml(xml, Constants.ODK_PATIENT_LAST_NAME);
		String age = XmlReader.getStringValueFromXml(xml, Constants.ODK_PATIENT_AGE);
		if (!Constants.EMPTY_STRING.equals(age)) {
			patient.age = Integer.parseInt(age);
		}
		String sex = XmlReader.getStringValueFromXml(xml, Constants.ODK_PATIENT_SEX);
		if (!Constants.EMPTY_STRING.equals(sex)) {
			patient.gender = Integer.parseInt(sex);
		}
		//update with latest values
		patient.lastModified = Constants.STANDARD_DATE_FORMAT.format(Calendar.getInstance().getTime());
		int i = DBAdapter.update(Patient.TABLE_NAME, patient.getContentValues(), BaseColumns._ID+Constants.EQUALS_STRING+patient.id, null);
		
		Log.d(Constants.LOG_TAG, i+ " patient(s) updated!");
		
		return patient;
	}
	
	/** Create a patient by using the xml data
	 * @param context application's context.
	 * @param xml XML formatted data.
	 * @param householdId Household id where patient belongs to.
	 * @param instancePath Where the form data is stored.
	 * @return
	 */
	private Patient createPatient(Context context, String xml, int householdId, String instancePath) {
		Patient p = new Patient();
		p.code = Constants.PATIENT_CODE_PREFIX+Constants.DASH_STRING + Preferences.getPhoneId(context) + Constants.DASH_STRING +
		Constants.STANDARD_DATE_FORMAT.format(Calendar.getInstance().getTime());
		p.householdId = householdId;
		
		p.id = DBAdapter.insert(TABLE_NAME, p.getContentValues());
		
		//save data
		FormData fd = new FormData(PATIENT_FORM_CODE, xml, instancePath, CommonUtils.getCurrentTime(context), Constants.ONE);
		fd.id = DBAdapter.insert(FormData.TABLE_NAME, fd.getContentValues());
		
		//link patient - data
		PatientData pd = new PatientData(p.id,fd.id);
		p.patientDataId = DBAdapter.insert(PatientData.TABLE_NAME, pd.getContentValues());
		
		return p;
	}
	
	/**
	 * Get all the code of form template
	 * @param id
	 * @return List of result.
	 */
	public String [] getFilledFormCodes(int id) {
		String [] result = null;
		
		String query = "SELECT ft.code FROM form_template ft, form_data fd, patient_data pd WHERE " +
					   "pd.patient_id = "+id+Constants.AND_STRING +
					   "pd.form_data_id = fd._id"+Constants.AND_STRING+
					   "fd.form_template_id = ft._id";
					   
		Cursor c = DBAdapter.rawQuery(query, null);
		
		int len = c.getCount();
		if (len > 0) {
			result = new String[len];
			c.moveToFirst();
			for (int i = 0; i < len; i++) {
				result[i] = c.getString(0);
				c.moveToNext();
			}
		}
		c.close();
		return result;
	}
	
	/** Get patient code from Form data
	 * @param formDataId Specific form data id
	 * @return Patient code
	 */
	public static String getCodeFromFormDataId(int formDataId) {
		String code = Constants.EMPTY_STRING;
		
		String query = "SELECT p.code FROM patient p, patient_data pd WHERE "+
					   "pd.form_data_id = "+formDataId+Constants.AND_STRING+
					   "pd.patient_id = p._id";
		Cursor c = DBAdapter.rawQuery(query, null);
		if (c.getCount() == 1) {
			c.moveToFirst();
			code = c.getString(0);
		}
		c.close();
		return code;
	}

	/** Retrieve patient information
	 * @param code Patient code
	 * @return Patient object.
	 */
	public static Patient getFromCode(String code) {
		Patient patient = null;
		
		Cursor c = DBAdapter.query(TABLE_NAME, new String[] {BaseColumns._ID, COL_PATIENT_DATA_ID, COL_CODE, COL_HOUSEHOLD_ID, 
				COL_FIRST_NAME, COL_LAST_NAME, COL_AGE, COL_GENDER, COL_LAST_MODIFIED },
				COL_CODE+Constants.EQUALS_STRING+Constants.SINGLE_QUOTE_STRING+code+Constants.SINGLE_QUOTE_STRING,
				null, null, null, null);
		
		int len = c.getCount();
		if (len == 1) {
			c.moveToFirst();
			patient = new Patient(c.getInt(0), c.getInt(1), c.getString(2), c.getInt(3), c.getString(4), c.getString(5), c.getInt(6), c.getInt(7), c.getString(8));
		}
		c.close();
		return patient;
	}

}
