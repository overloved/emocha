/*******************************************************************************
 * eMOCHA - electronic Mobile Open-Source Comprehensive Health Application
 * Copyright (c) 2009 Abe Pazos - abe@ccghe.net
 *                    Ricardo A.B - blackbitshines@gmail.com
 *                    Pau Varela - pau.varela@gmail.com
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

import org.emocha.Constants;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * DBAdapter represents the internal database stored in smartphone.
 * DBAdapter stores all the information patients filled, including 
 * forms and media files.
 * @author 
 *
 */
public class DBAdapter {
	private static final String DATABASE_NAME 	= "eMOCHA";
	private static final int DATABASE_VERSION 	= 2; //20120108
	
	/**
	 * DBHelper instance.
	 */
	private static DBHelper sDBHelper = null;
	/**
	 * SQLiteDatabase instance.
	 */
	private static SQLiteDatabase sDB = null;


	private static Object lock = new Object();
	
	/**
	 * Initialize SQLiteOpenHelper
	 * @param tContext
	 * @throws SQLException
	 */
	public static void init(Context tContext) throws SQLException {	
		
		if (sDBHelper == null) {
			sDBHelper = new DBHelper(tContext);
		}
		sDB = getDB();
	}

	/**
	 * Retrieve the total number of rows in a specific table
	 * @param table Database table name
	 * @return The number
	 */
	public static int count(String table) {
		int count = 0;
		String query = "select count(*) from "+table;
		Cursor c = rawQuery(query, null);
		if (c.getCount() > 0) {
			c.moveToFirst();
			count = c.getInt(0);
		}
		c.close();
		return count;
	}
	
	/**
	 * Close SQLiteDatabaseHelper
	 */
	public static void destroy() {
		sDB = null;
		sDBHelper.close();
	}

	/**
	 * Insert values into table
	 * @param tablename The table name that values should be inserted in.
	 * @param values The values are being inserted.
	 * @return True or failed to insert.
	 */
	public static int insert(String tablename, ContentValues values) {
		try {
			return (int)getDB().insert(tablename, null, values);
		} catch(SQLException e) {
			Log.e(Constants.LOG_TAG, "error inserting value: "+e.getMessage());
		}
		return -1;
	}
	
	/**
	 * @param table Table name
	 * @param columns
	 * @param selection
	 * @param selectionArgs
	 * @param groupBy
	 * @param having
	 * @param orderBy
	 * @return Result set.
	 */
	//TODO: use SQLiteDatabase.query with limit parameter instead!! replace all the calls ; remove limitQuery method
	public static Cursor query(String table, String[] columns, String selection, String []selectionArgs, String groupBy, String having, String orderBy) {
		Cursor cursor = null;
		try {
			cursor = getDB().query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
		} catch(SQLException e) {
			Log.e(Constants.LOG_TAG, "error selecting value: "+e.getMessage());
		}
		return cursor;
	}
	
	public static Cursor limitQuery(String table, String[] columns, String selection, String []selectionArgs, String groupBy, String having, String orderBy, String limit) {
		Cursor cursor = null;
		try {
			cursor = getDB().query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
		} catch(SQLException e) {
			Log.e(Constants.LOG_TAG, "error selecting value: "+e.getMessage());
		}
		return cursor;
	}	
	
	/** 
	 * Run the provided SQL and returns a Cursor over the result set.
	 * @param sql SQL sentences.
	 * @param selectionArgs Conditions which is applied in SQL sentence.
	 * @return Cursor
	 */
	public static Cursor rawQuery(String sql, String[]selectionArgs ) {
		Cursor cursor = null;
		try {
			cursor = getDB().rawQuery(sql, selectionArgs);
		} catch(SQLException e) {
			Log.e(Constants.LOG_TAG, "error selecting value: "+e.toString());
		}
		return cursor;
	}
	public static int update(String tablename, ContentValues values, String whereClause, String[] whereArgs) {
		return getDB().update(tablename, values, whereClause, whereArgs);
	}
	
	public static int delete(String tablename, String whereClause, String[] whereArgs) {	
		return getDB().delete(tablename, whereClause, whereArgs);
	}
	
	public static boolean isOpen() {
		return getDB().isOpen();
	}
	
	public static void open() {
		sDB = getDB();
	}
	
	public static void close() {
		getDB().close();
	}
	
	public static void beginTransaction() {
		getDB().beginTransaction();
	}
	
	public static void endTransaction() {
		getDB().endTransaction();
	}
	
	public static void setTransactionSuccessful() {
		getDB().setTransactionSuccessful();
	}
	
	private static SQLiteDatabase getDB() {	
		synchronized (lock) {  //DB is accessed from different threads. we better synchronize it!
			if (sDB == null) {
				return sDBHelper.getWritableDatabase();
			}
			if (!sDB.isOpen()){
				return sDBHelper.getWritableDatabase();
			}
			return sDB;
		}
	}
	
	/**
	 * DBHelper is a helper class to manage database creation and verison management.
	 * @author
	 *
	 */
	private static class DBHelper extends SQLiteOpenHelper {
	    DBHelper(Context context) {
	      super(context, DATABASE_NAME, null, DATABASE_VERSION);
	      Log.d(Constants.LOG_TAG, "DB name: "+DATABASE_NAME+" - DB version: "+DATABASE_VERSION);
	    }

	    @Override
	    public void onCreate(SQLiteDatabase db) {

	    	//create tables
	    	db.execSQL(org.emocha.model.entities.Patient.TABLE_CREATE);
	    	db.execSQL(org.emocha.model.entities.PatientData.TABLE_CREATE);
	    	db.execSQL(org.emocha.model.entities.FormData.TABLE_CREATE);
	    	db.execSQL(org.emocha.model.entities.FormDataFile.TABLE_CREATE);	    	
	    	db.execSQL(org.emocha.model.entities.FormTemplate.TABLE_CREATE);
	    	db.execSQL(org.emocha.model.entities.File.TABLE_CREATE);
	    }

	    @Override
	    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	      Log.w(Constants.LOG_TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
	      switch(oldVersion) {
	        default: //do nothing until new update
	          break;
	      }

	    }
	  }

}