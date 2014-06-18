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

/** 
 * Template of forms. The content of template forms can be different
 * according to what kinds of forms included.
 * @author 
 *
 */
public class FormTemplate {
	
	public static final String TABLE_NAME = "form_template";
	public static final String COL_CODE = "code";
	public static final String COL_GROUP = "group_name";
	public static final String COL_NAME = "name";
	public static final String COL_DESCRIPTION = "description";
	public static final String COL_LABEL = "label";
	public static final String COL_CONFIG = "config";
	public static final String COL_FILE_ID = "file_id";
	
	
	public static final String TABLE_CREATE =
		"CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
		+ BaseColumns._ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "
		+ COL_CODE +" TEXT, "
		+ COL_GROUP +" TEXT, "
		+ COL_NAME + " TEXT, "
		+ COL_DESCRIPTION + " TEXT, "
		+ COL_LABEL + " TEXT, "
		+ COL_CONFIG + " TEXT, "
		+ COL_FILE_ID + " INTEGER "
		+");";
	
	public int id;
	public String code;
	public String group;
	public String name;
	public String description;
	public String label;
	public String config;
	public int fileId;
	
	/**
	 * Class constructor.
	 */
	public FormTemplate() {}
	
	/**
	 * Class constructor.
	 * @param code Form template unique code.
	 * @param group The group of specific form template.
	 * @param name Name of the form template
	 * @param description Description of the form template.
	 * @param label Label of the form template.
	 * @param config Configuration info of the form template.
	 * @param fileId Media file id.
	 */
	public FormTemplate (String code, String group, String name, String description, String label, String config, int fileId) {		
		this.code = code;
		this.group = group;
		this.name = name;
		this.description = description;
		this.label = label;
		this.config = config;
		this.fileId = fileId;
	}
	
	/**
	 * Class constructor.
	 * @param id Form template unique id (different from template code).
	 * @param group The group of specific form template.
	 * @param name Name of the form template
	 * @param description Description of the form template.
	 * @param label Label of the form template.
	 * @param config Configuration info of the form template.
	 * @param fileId Media file id.
	 */
	public FormTemplate (int id, String code, String group, String name, String description, String label, String config, int fileId) {
		this.id = id;
		this.code = code;
		this.group = group;
		this.name = name;
		this.description = description;
		this.label = label;
		this.config = config;
		this.fileId = fileId;
	}
	
	/**
	 * Store a set of values that ContentResolver can process.
	 * @return Set of values
	 */
	public ContentValues getContentValues() {
		ContentValues values = new ContentValues();
		values.put(COL_CODE, code);
		values.put(COL_GROUP, group);
		values.put(COL_NAME, name);
		values.put(COL_DESCRIPTION, description);
		values.put(COL_LABEL, label);
		values.put(COL_CONFIG, config);
		values.put(COL_FILE_ID, fileId);
		return values;
	}
	
	//select _id from form_template where code='code' 
	/** Get id by given code
	 * @param code Form template code.
	 * @return Form template id.
	 */
	public static int getIdFromCode(String code) {
		int id = -1;
		Cursor c = DBAdapter.query(TABLE_NAME, new String []{BaseColumns._ID}, 
				COL_CODE+Constants.EQUALS_STRING+Constants.SINGLE_QUOTE_STRING+code+Constants.SINGLE_QUOTE_STRING, null, null, null, null);
		if (c.getCount() == 1) { //code must be unique
			c.moveToFirst();
			id = c.getInt(0);
		} 
		c.close();
		return id;
	}
	
	/**
	 * Get the form template name by given code.
	 * @param field 
	 * @param code
	 * @return Name of file.
	 */
	public String getStringFromCode(String field, String code) {
		String name = Constants.EMPTY_STRING;
		Cursor c = DBAdapter.query(TABLE_NAME, new String[]{field}, 
				COL_CODE+Constants.EQUALS_STRING+Constants.SINGLE_QUOTE_STRING+code+Constants.SINGLE_QUOTE_STRING, null, null, null, null);
		if (c.getCount() == 1) {
			c.moveToFirst();
			name = c.getString(0);
		}
		c.close();
		return name;
	}	
	
	/**
	 * Get label info of the form template.
	 * @param code
	 * @return Label
	 */
	public static String getLabel(String code) {
		String label = Constants.EMPTY_STRING;
		Cursor c = DBAdapter.query(TABLE_NAME, new String[]{COL_LABEL}, 
				COL_CODE+Constants.EQUALS_STRING+Constants.SINGLE_QUOTE_STRING+code+Constants.SINGLE_QUOTE_STRING, null, null, null, null);
		if (c.getCount() == 1) {
			c.moveToFirst();
			label = c.getString(0);
		}
		c.close();
		return label;
	}
	
	/** Get description info of the form template.
	 * @param code
	 * @return Description info.
	 */
	public static String getDescription(String code) {
		String label = Constants.EMPTY_STRING;
		Cursor c = DBAdapter.query(TABLE_NAME, new String[]{COL_DESCRIPTION}, 
				COL_CODE+Constants.EQUALS_STRING+Constants.SINGLE_QUOTE_STRING+code+Constants.SINGLE_QUOTE_STRING, null, null, null, null);
		if (c.getCount() == 1) {
			c.moveToFirst();
			label = c.getString(0);
		}
		c.close();
		return label;
	}
	
	/** 
	 * Get all form templates which belongs to specific group.
	 * @param group The group name.
	 * @return List of form templates which belongs to the same group. 
	 */
	public FormTemplate [] getFormTemplatesByGroup(String group) {
		FormTemplate [] result = null;
		Cursor c = DBAdapter.query(TABLE_NAME, new String []{BaseColumns._ID, COL_CODE, COL_GROUP, COL_NAME, COL_DESCRIPTION, 
				COL_LABEL, COL_CONFIG, COL_FILE_ID}, 
				COL_GROUP+Constants.EQUALS_STRING+Constants.SINGLE_QUOTE_STRING+group+Constants.SINGLE_QUOTE_STRING, null, null, null, null);
		int len = c.getCount();
		if (len > 0) {
			result = new FormTemplate[len];
			c.moveToFirst();
			for (int i = 0; i < len; i++) {
				result[i] = new FormTemplate(c.getInt(0),c.getString(1),c.getString(2),c.getString(3),c.getString(4),
						 c.getString(5),c.getString(6),c.getInt(7));
				c.moveToNext();
			}
		}
		c.close();
		
		return result;
	}
	
	/** 
	 * Get the specific form template by id.
	 * @param id Form template id.
	 * @return From template object.
	 */
	public FormTemplate get(int id) {
		FormTemplate result  = null;
		Cursor c = DBAdapter.query(TABLE_NAME, new String []{BaseColumns._ID, COL_CODE, COL_GROUP, COL_NAME, COL_DESCRIPTION, 
				COL_LABEL,COL_CONFIG, COL_FILE_ID}, 
				BaseColumns._ID+Constants.EQUALS_STRING+id, null, null, null, null);
		int len = c.getCount();
		if (len == 1) {
			c.moveToFirst();
			result = new FormTemplate(c.getInt(0),c.getString(1),c.getString(2),c.getString(3),c.getString(4),
					 c.getString(5),c.getString(6),c.getInt(7));
		}
		c.close();
		return result;
	}
	
	/**
	 * Get the specific form template by code.
	 * @param code Form template code.
	 * @return From template object.
	 */
	public static FormTemplate getFormTemplateByCode(String code) {
		FormTemplate result  = null;
		
		Cursor c = DBAdapter.query(FormTemplate.TABLE_NAME, new String []{BaseColumns._ID, FormTemplate.COL_CODE, FormTemplate.COL_GROUP, FormTemplate.COL_NAME, FormTemplate.COL_DESCRIPTION,
				FormTemplate.COL_LABEL,FormTemplate.COL_CONFIG, FormTemplate.COL_FILE_ID},
				FormTemplate.COL_CODE+Constants.EQUALS_STRING+Constants.SINGLE_QUOTE_STRING+code+Constants.SINGLE_QUOTE_STRING, null, null, null, null);
		int len = c.getCount();
		if (len == 1) {
		  c.moveToFirst();
		  result = new FormTemplate(c.getInt(0),c.getString(1),c.getString(2),c.getString(3),c.getString(4),
				  c.getString(5),c.getString(6),c.getInt(7));
		}
		c.close();
		return result;
	}
}
