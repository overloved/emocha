/*******************************************************************************
 * eMOCHA - electronic Mobile Open-Source Comprehensive Health Application
 * Copyright (c) 2011 Pau Varela - pau.varela@gmail.com
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

import org.emocha.Constants;
import org.emocha.model.DBAdapter;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

/** The attributes and info of patient data
 * @author 
 *
 */
public class PatientData {
	public static final String TABLE_NAME = "patient_data";
	public static final String COL_PATIENT_ID = "patient_id";
	public static final String COL_FORM_DATA_ID = "form_data_id";
	
	public static final String TABLE_CREATE =
		"CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
		+ BaseColumns._ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "
		+ COL_PATIENT_ID + " INTEGER, "
		+ COL_FORM_DATA_ID +" INTEGER "
		+");";
	
	public int id;
	public int patientId;
	public int formDataId;
	
	/**
	 * Class constructor
	 */
	public PatientData() {}
	
	/**
	 * Class constructor
	 * @param patiendId
	 * @param formDataId
	 */
	public PatientData(int patiendId, int formDataId) {
		this.patientId = patiendId;
		this.formDataId = formDataId;
	}
	
	/** 
	 * Class constructor
	 * @param id Patient data id.
	 * @param patiendId
	 * @param formDataId
	 */
	public PatientData(int id, int patiendId, int formDataId) {
		this.id = id;
		this.patientId = patiendId;
		this.formDataId = formDataId;
	}
	
	/**
	 * Store a set of values of patient data.
	 * @return Set of values
	 */
	public ContentValues getContentValues(){
		ContentValues values = new ContentValues();
		values.put(COL_PATIENT_ID, patientId);
		values.put(COL_FORM_DATA_ID, formDataId);
		return values;
	}
	
	
	/**
	 * Retrieve all the data from xml
	 * @param patientId
	 * @param code
	 * @return List of result.
	 */
	public String getXmlData(int patientId, String code) {
		String result = Constants.EMPTY_STRING;
		String query = "SELECT fd.xml_data FROM form_data fd, form_template ft, patient_data pd WHERE "+
					   "pd.patient_id = "+patientId+Constants.AND_STRING+
					   "pd.form_data_id = fd._id"+Constants.AND_STRING+
					   "fd.form_template_id = ft._id"+Constants.AND_STRING+
					   "ft.code ='"+code+Constants.SINGLE_QUOTE_STRING;
		Cursor c = DBAdapter.rawQuery(query, null);
		
		int len = c.getCount();
		if (len == 1 ) { //TODO forcing 1 to 1 relationship..
			c.moveToFirst();
			result = c.getString(0); 
		}
		c.close();
		return result;
	}
	
	/**
	 * Get specific patient data by patient data id.
	 * @param id
	 * @return Patient data object.
	 */
	public PatientData get(int id) {
		PatientData pd = null;
		Cursor c = DBAdapter.query(TABLE_NAME, new String[]{COL_PATIENT_ID, COL_FORM_DATA_ID}, BaseColumns._ID+Constants.EQUALS_STRING+id, null, null, null, null);
		if (c.getCount() == 1) {
			c.moveToFirst();
			pd = new PatientData(c.getInt(0), c.getInt(1));
		}
		c.close();
		return pd;
	}
	
	/** Get specific patient data by patient id and code.
	 * @param patientId
	 * @param code
	 * @return Patient data object.
	 */
	public PatientData get(int patientId, String code) {
		PatientData pd = null;
		String query = "SELECT pd._id, pd.patient_id, pd.form_data_id FROM form_data fd, form_template ft, patient_data pd WHERE "+
					   "pd.patient_id = "+patientId+Constants.AND_STRING+
					   "pd.form_data_id = fd._id"+Constants.AND_STRING+
					   "fd.form_template_id = ft._id"+Constants.AND_STRING+
					   "ft.code ='"+code+Constants.SINGLE_QUOTE_STRING;
		Cursor c = DBAdapter.rawQuery(query, null);
		
		int len = c.getCount();
		if (len == 1 ) { //TODO forcing 1 to 1 relationship..
			c.moveToFirst();
			pd = new PatientData(c.getInt(0),c.getInt(1),c.getInt(2)); 
		}
		c.close();
		return pd;
	}
	
}
