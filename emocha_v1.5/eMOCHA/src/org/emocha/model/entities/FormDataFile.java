/*******************************************************************************
 * eMOCHA - electronic Mobile Open-Source Comprehensive Health Application
 * Copyright (c) 2012 Pau Varela - pau.varela@gmail.com
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

/**
 * FormDatafile represents any file associated to a form. Stored in the database
 * but relationship is with the FormData entity. Files are usually sent to the 
 * server from the devices.
 * @author 
 *
 */
public class FormDataFile {
	public static final String TABLE_NAME = "form_data_file";
	public static final String COL_FORM_DATA_ID = "form_data_id";
	public static final String COL_FILENAME = "filename";
	public static final String COL_TYPE = "type";
	public static final String COL_XPATH = "xpath";
	public static final String COL_LAST_MODIFIED = "last_modified";
	public static final String COL_TO_UPLOAD = "to_upload";
	
	public static final String TYPE_SIGNATURE = "signature";
	//TODO add more types and validate them on FormDataFile creation!
	
	public static final int STATUS_FAILED = Constants.TWO;
	
	public static final String TABLE_CREATE =
			"CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
			+ BaseColumns._ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COL_FORM_DATA_ID +" INTEGER, "
			+ COL_FILENAME + " TEXT, "
			+ COL_TYPE + " TEXT, "
			+ COL_XPATH + " TEXT, "
			+ COL_LAST_MODIFIED + " TEXT, "
			+ COL_TO_UPLOAD + " TEXT"
			+");";
	
	public int id;
	public int formDataId;
	public String filename;
	public String type;
	public String xpath;
	public String lastModified;
	public int toUpload;
	
	public FormDataFile() {}
	
	/**
	 * Class constructor.
	 * @param fdId Form data id.
	 * @param filename File name.
	 * @param type File type.
	 * @param xpath File path.
	 * @param lastModified The time last modified.
	 */
	public FormDataFile(int fdId, String filename, String type, String xpath, String lastModified) {
		this.formDataId = fdId;
		this.filename = filename;
		this.type = type;
		this.xpath = xpath;
		this.lastModified = lastModified;
		this.toUpload = 1;
	}

	/**
	 * @param id Form data file id.
	 * @param fdId Form data id.
	 * @param filename File name.
	 * @param type File type.
	 * @param xpath File path.
	 * @param lastModified The time last modified.
	 */
	public FormDataFile(int id, int fdId, String filename, String type, String xpath, String lastModified) {
		this.id = id;	
		this.formDataId = fdId;
		this.filename = filename;
		this.type = type;
		this.xpath = xpath;
		this.lastModified = lastModified;
		this.toUpload = 1;
	}
	
	/** 
	 * Store a set of values that ContentResolver can process.
	 * @return Set of values.
	 */
	public ContentValues getContentValues(){
		ContentValues values = new ContentValues();
		values.put(COL_FORM_DATA_ID, formDataId);
		values.put(COL_FILENAME, filename);
		values.put(COL_TYPE, type);
		values.put(COL_XPATH, xpath);
		values.put(COL_LAST_MODIFIED, lastModified);
		values.put(COL_TO_UPLOAD, toUpload);
		return values;
	}
	
	/**retrieve FormDataFiles ready to be uploaded for a given form_data_id*/
	public static FormDataFile[] getFormDataFilesReady(int fdId) {
		FormDataFile fdf[] = null;
		
		Cursor c = DBAdapter.query(TABLE_NAME, new String[] {BaseColumns._ID, COL_FORM_DATA_ID, COL_FILENAME, COL_TYPE, COL_XPATH, COL_LAST_MODIFIED}, 
				COL_FORM_DATA_ID+Constants.EQUALS_STRING+fdId+Constants.AND_STRING+
				COL_TO_UPLOAD+Constants.EQUALS_STRING+Constants.ONE,
				null, null, null, null);
		
		int length = c.getCount();
		if (length > 0) {
			fdf = new FormDataFile[length];
			c.moveToFirst();
			for(int i=0; i<length; i++) {
				fdf[i] = new FormDataFile(c.getInt(0),c.getInt(1),c.getString(2),c.getString(3), c.getString(4), c.getString(5));
				c.moveToNext();
			}
		}
		c.close();
		return fdf;
	}

	
	
	/**
	 * Retrieve FormDataFiles which are failed to upload.
	 * @return
	 */
	public static FormDataFile getNextFailedFormDataFile() {
		FormDataFile fdf = null;
		
		Cursor c = DBAdapter.query(TABLE_NAME, new String[] {BaseColumns._ID, COL_FORM_DATA_ID, COL_FILENAME, 
				COL_TYPE, COL_XPATH, COL_LAST_MODIFIED}, 
				COL_TO_UPLOAD+Constants.EQUALS_STRING+STATUS_FAILED,
				null, null, null, null);
		
		if (c.getCount() > 0) {
			c.moveToFirst();
			fdf = new FormDataFile(c.getInt(0),c.getInt(1),c.getString(2),c.getString(3), c.getString(4), c.getString(5));
		}
		c.close();
		return fdf;
	}
	
	/**retrieve FormDataFiles ready to be uploaded for a given form_data_id*/
	public static FormDataFile[] getFailedFormDataFiles() {
		FormDataFile fdf[] = null;
		
		Cursor c = DBAdapter.query(TABLE_NAME, new String[] {BaseColumns._ID, COL_FORM_DATA_ID, COL_FILENAME, 
				COL_TYPE, COL_XPATH, COL_LAST_MODIFIED}, 
				COL_TO_UPLOAD+Constants.EQUALS_STRING+STATUS_FAILED,
				null, null, null, null);
		
		int length = c.getCount();
		if (length > 0) {
			fdf = new FormDataFile[length];
			c.moveToFirst();
			for(int i=0; i<length; i++) {
				fdf[i] = new FormDataFile(c.getInt(0),c.getInt(1),c.getString(2),c.getString(3), c.getString(4), c.getString(5));
				c.moveToNext();
			}
		}
		c.close();
		return fdf;
	}
	
	public static FormDataFile get(int fdId, String xpath) {
		FormDataFile result = null;

		Cursor c = DBAdapter.query(TABLE_NAME, new String[] {BaseColumns._ID, COL_FORM_DATA_ID, COL_FILENAME, 
				COL_TYPE, COL_XPATH, COL_LAST_MODIFIED}, 
				COL_FORM_DATA_ID+Constants.EQUALS_STRING+fdId+Constants.AND_STRING+
				COL_XPATH+Constants.EQUALS_STRING+Constants.SINGLE_QUOTE_STRING+xpath+Constants.SINGLE_QUOTE_STRING,
				null, null, null, null);
		//TODO issue with signature. there is no xpath for this type and we might have more than 1.. which one to choose?
		//so far there are no signature's update, but it could (will) be an issue in the future..
		if (c.getCount() == 1) {
			c.moveToFirst();
			result = new FormDataFile(c.getInt(0),c.getInt(1),c.getString(2),c.getString(3), c.getString(4), c.getString(5));
		}
		c.close();
		return result;
	}
	
	public boolean fdfExists(String path) {
		Cursor c = DBAdapter.limitQuery(FormDataFile.TABLE_NAME,new String[]{BaseColumns._ID}, FormDataFile.COL_FILENAME+Constants.EQUALS_STRING+Constants.SINGLE_QUOTE_STRING+path+Constants.SINGLE_QUOTE_STRING, null, null, null, null, null);
		int length = c.getCount();
		c.close();
		return (length > 0);
	}

	
}
 