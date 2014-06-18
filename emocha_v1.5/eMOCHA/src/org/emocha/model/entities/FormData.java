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

import org.emocha.Constants;
import org.emocha.model.DBAdapter;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Log;

/** Form data represents the data of the form.
 * @author 
 *
 */
public class FormData {

	public static final String TABLE_NAME = "form_data";
	public static final String COL_FORM_TEMPLATE_ID = "form_template_id";
	public static final String COL_XML_DATA = "xml_data";
	public static final String COL_DATA_PATH = "data_path";
	public static final String COL_SIGNED = "signed";
	public static final String COL_TO_UPLOAD = "to_upload";
	public static final String COL_LAST_MODIFIED = "last_modified";
	public static final String COL_SERVER_TIMESTAMP = "server_timestamp";
	public static final String COL_SERVER_FD_ID = "server_fd_id";
	
	public static final String TABLE_CREATE =
		"CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
		+ BaseColumns._ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "
		+ COL_FORM_TEMPLATE_ID +" INTEGER, "
		+ COL_XML_DATA + " TEXT, "
		+ COL_DATA_PATH + " TEXT, "
		+ COL_SIGNED + " TEXT, "
		+ COL_TO_UPLOAD + " TEXT, "
		+ COL_LAST_MODIFIED + " TEXT, "
		+ COL_SERVER_TIMESTAMP + " TEXT, "
		+ COL_SERVER_FD_ID + " INTEGER"
		+");";
	
	public int id;
	public int formTemplateId;
	public String xmlData;
	public String dataPath;
	public int signed = 0;
	public int toUpload;
	public String lastModified;
	public String serverTs = null;
	public int serverFDId = -1;

	/** Store a set of values that ContentResolver can process.
	 * @return Set of values.
	 */
	public ContentValues getContentValues(){
		ContentValues values = new ContentValues();
		values.put(COL_FORM_TEMPLATE_ID, formTemplateId);
		values.put(COL_XML_DATA, xmlData);
		values.put(COL_DATA_PATH, dataPath);
		values.put(COL_SIGNED, signed);
		values.put(COL_TO_UPLOAD, toUpload);
		values.put(COL_LAST_MODIFIED, lastModified);
		if (serverTs != null) {
			values.put(COL_SERVER_TIMESTAMP, serverTs);
		}
		values.put(COL_SERVER_FD_ID, serverFDId);
		return values;
	}
	
	/**
	 * Class constructor
	 */
	public FormData() {}
	
	/** Class constructor
	 * @param fCode Form unique identification code.
	 * @param xml Data with xml formatted.
	 * @param instancePath The path of data
	 * @param now The current time
	 * @param upload State whether the file could be uploaded or not.
	 */
	public FormData(String fCode, String xml, String instancePath, String now, int upload) {
		int fid = FormTemplate.getIdFromCode(fCode);
		if (fid > 0) {
			this.formTemplateId = fid;
			this.xmlData = xml;
			this.dataPath = instancePath;
			this.toUpload = upload;
			this.lastModified = now;
		} else {
			Log.e(Constants.LOG_TAG,"Ooopppps! Template not found for the provided code: "+fCode);
			throw new RuntimeException("Template not found for the provided code: "+fCode);
		}
			
	}
	
	/**
	 * Class constructor
	 * @param id Form unique id.
	 * @param templateId
	 * @param xml Data with xml formatted.
	 * @param dataPath The path of data
	 * @param signed
	 * @param up
	 * @param lastChange The date of data last changed
	 * @param serverTimestamp The time of server side.
	 * @param serverFDataId
	 */
	public FormData (int id, int templateId, String xml, String dataPath, int signed, int up, String lastChange, String serverTimestamp, int serverFDataId) {
		this.id = id;
		this.formTemplateId = templateId;
		this.xmlData = xml;
		this.dataPath = dataPath;
		this.signed = signed;
		this.toUpload = up;
		this.lastModified = lastChange ;
		this.serverTs = serverTimestamp;
		this.serverFDId = serverFDataId;
	}
	
	/**
	 * Prepare the form to be uploaded by server.
	 * @param fdId Form id.
	 * @param xml The form data with xml formatted.
	 * @param instancePath Path of the form.
	 * @param ts The date of form last modified.
	 * @return Form object.
	 */
	public FormData prepareToUpload(int fdId, String xml, String instancePath, String ts) {
		FormData fd = get(fdId);
		fd.xmlData = xml;
		fd.dataPath = instancePath;
		fd.lastModified = ts;
		fd.toUpload = Constants.ONE;
		return fd;
	}
	//select count(*) from form_template where to_upload=1
	/** 
	 * Get the total number of the form which are ready to be uploaded.
	 * @return The count of forms.
	 */
	public int getCountToUpload() {
		int length = 0;
		Cursor c = DBAdapter.query(TABLE_NAME, new String[]{BaseColumns._ID}, 
				COL_TO_UPLOAD+Constants.EQUALS_STRING+Constants.ONE, null, null, null, null);
		length = c.getCount();
		c.close();
		return length;
	}
	
	// select * from form_template where to_upload = 1 LIMIT 1 
	/**
	 * Get the specific one form data which are ready to be uploaded.
	 * @return Form data object.
	 */
	public FormData getFormDataToUpload() {
		FormData fd = null;
		Cursor c = DBAdapter.query(TABLE_NAME, new String[]{BaseColumns._ID, COL_FORM_TEMPLATE_ID, COL_XML_DATA, 
				COL_DATA_PATH, COL_SIGNED, COL_TO_UPLOAD, COL_LAST_MODIFIED, COL_SERVER_TIMESTAMP, COL_SERVER_FD_ID }, 
				COL_TO_UPLOAD+Constants.EQUALS_STRING+Constants.ONE, null, null, null, null);
		if (c.getCount() > 0) {
			c.moveToFirst();
			fd = new FormData(c.getInt(0), c.getInt(1), c.getString(2), c.getString(3), c.getInt(4), c.getInt(5), c.getString(6), c.getString(7), c.getInt(8));
		}
		c.close();
		return fd;
	}
	
	//update form_data set to_upload=0, server_timestamp where id=ID
	/** 
	 * Mark the form data to be uploaded.
	 * @param id Form data id.
	 * @param serverTs Server time
	 * @param sfdId The correspond form data id on server.
	 */
	public void markAsUploaded(int id, String serverTs, int sfdId) {
		ContentValues values = new ContentValues();
		values.put(COL_TO_UPLOAD, Constants.ZERO);
		values.put(COL_SERVER_TIMESTAMP, serverTs);
		values.put(COL_SERVER_FD_ID, sfdId);
		DBAdapter.update(TABLE_NAME, values, BaseColumns._ID+Constants.EQUALS_STRING+id, null);
	}
	
	/**
	 * Get the specific form name.
	 * @return Name of the form.
	 */
	public String getFormName() {
		String name = Constants.EMPTY_STRING;
		
		Cursor c = DBAdapter.query(FormTemplate.TABLE_NAME, new String[] {FormTemplate.COL_NAME}, 
				BaseColumns._ID+Constants.EQUALS_STRING+this.formTemplateId, null, null, null, null);
		if (c.getCount() > 0) {
			c.moveToFirst();
			name = c.getString(0);
		}
		c.close();
		return name;
	}
	
	/** 
	 * Get the specific form unique code.
	 * @return Unique code of the form.
	 */
	public String getFormCode() {
	String code = Constants.EMPTY_STRING;
		
		Cursor c = DBAdapter.query(FormTemplate.TABLE_NAME, new String[] {FormTemplate.COL_CODE}, 
				BaseColumns._ID+Constants.EQUALS_STRING+this.formTemplateId, null, null, null, null);
		if (c.getCount() > 0) {
			c.moveToFirst();
			code = c.getString(0);
		}
		c.close();
		return code;	
	}
	
	/** Get the form data object.
	 * @param id Form id.
	 * @return Form data object.
	 */
	public FormData get(int id) {
		FormData fd = null;

	    Cursor c = DBAdapter.query(TABLE_NAME, new String[] {BaseColumns._ID, COL_FORM_TEMPLATE_ID, COL_XML_DATA,
	        COL_DATA_PATH, COL_SIGNED, COL_TO_UPLOAD, COL_LAST_MODIFIED, COL_SERVER_TIMESTAMP, COL_SERVER_FD_ID},
	        BaseColumns._ID+Constants.EQUALS_STRING+id, null, null, null, null);
	    if (c.getCount() > 0) {
	      c.moveToFirst();
	      fd = new FormData(c.getInt(0), c.getInt(1), c.getString(2), c.getString(3), c.getInt(4), c.getInt(5), c.getString(6), c.getString(7), c.getInt(8));
	    }
	    c.close();
	    return fd;
	}
	
	/** Get the template of the specific kind of form.
	 * @param formDataId The entry form data id.
	 * @return The corresponding form template object.
	 */
	public FormTemplate getFormTemplate(int formDataId){
		 FormTemplate ft = null;
		 
		 //TODO: create one single query instead of doing 2? select ft.* from form_template ft, form_data fd where.... ft._id = fd.form_template_id?
		 FormData fd = get(formDataId);
		 Cursor c = DBAdapter.query(FormTemplate.TABLE_NAME, new String[] {BaseColumns._ID, FormTemplate.COL_CODE, 
				 					FormTemplate.COL_GROUP, FormTemplate.COL_NAME, FormTemplate.COL_DESCRIPTION, 
				 					FormTemplate.COL_LABEL, FormTemplate.COL_CONFIG, FormTemplate.COL_FILE_ID},
				 					BaseColumns._ID+Constants.EQUALS_STRING+fd.formTemplateId, null, null, null, null);
		 if (c.getCount() == 1) {
			 c.moveToFirst();
			 ft = new FormTemplate(c.getInt(0),c.getString(1),c.getString(2),c.getString(3),
					 				c.getString(4), c.getString(5),c.getString(6),c.getInt(7));
		 }
		 c.close();
		 return ft;
		 
	}
	
	//select count(*) from form_template where xml_data=''
	/*
	public static int getCountEmpty() {
		int length = 0;
		Cursor c = DBAdapter.query(TABLE_NAME, new String[]{BaseColumns._ID}, 
								COL_XML_DATA+Constants.EQUALS_STRING+Constants.SQL_EMPTY_STRING, 
								null, null, null, null);
		length = c.getCount();
		c.close();
		return length;
	}
	*/
}
