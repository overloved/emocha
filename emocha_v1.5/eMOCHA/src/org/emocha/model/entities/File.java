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

import java.util.ArrayList;

import org.emocha.Constants;
import org.emocha.midot.R;
import org.emocha.model.DBAdapter;
import org.emocha.utils.FileInfo;
import org.emocha.utils.FileUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.provider.BaseColumns;
import android.util.Log;

/** 
 * File represent a file (either media or form template) which is stored in the sdcard,
 * also has its reference stored in the SQLite database. Files usually are sent from
 * the server to the device.
 * @author 
 *
 */
public class File {
	public static final String TABLE_NAME = "file";
	public static final String COL_TYPE = "type";
	public static final String COL_PATH = "path";
	public static final String COL_MD5 = "md5";
	public static final String COL_TO_DOWNLOAD = "to_download";
	public static final String COL_LAST_MODIFIED = "last_modified";
	public static final String COL_SERVER_FILE_ID = "server_file_id";
	public static final String COL_REMOTE_PATH = "remote_path";
	
	public static final String TABLE_CREATE =
		"CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
		+ BaseColumns._ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "
		+ COL_TYPE +" TEXT, "
		+ COL_PATH + " TEXT, "
		+ COL_MD5 + " TEXT, "
		+ COL_TO_DOWNLOAD + " INTEGER, "
		+ COL_LAST_MODIFIED + " TEXT, "
		+ COL_SERVER_FILE_ID+ " INTEGER, "
		+ COL_REMOTE_PATH + " TEXT"
		+");";
	
	public int id;
	public String type;
	public String path;
	public String md5;
	public int toDownload;
	public String lastModified;
	public int serverFileId;
	public String remotePath;
	
	
	/**
	 * Class constructor
	 */
	public File() {}
	
	/**
	 * @param type File type
	 * @param path File absolute path
	 * @param md5 File encrypted value
	 * @param toDownload Whether file could be download 
	 * @param lastModified The date last modified.
	 * @param serverFileId 
	 * @param remotePath The Url where file is stored in server
	 */
	public File (String type, String path, String md5, int toDownload, String lastModified, int serverFileId, String remotePath) {
		this.type = type;
		this.path = path;
		this.md5 = md5;
		this.toDownload = toDownload;
		this.lastModified = lastModified;
		this.serverFileId = serverFileId;
		this.remotePath = remotePath;
	}
	
	/**
	 * @param id File unique id
	 * @param path File absolute path
	 * @param md5 File encrypted value
	 * @param toDownload Whether file could be download 
	 * @param lastModified The date last modified.
	 * @param serverFileId 
	 * @param remotePath The Url where file is stored in server
	 */
	public File (int id, String type, String path, String md5, int toDownload, String lastModified, int serverFileId, String remotePath) {
		this.id = id;
		this.type = type;
		this.path = path;
		this.md5 = md5;
		this.toDownload = toDownload;
		this.lastModified = lastModified;
		this.serverFileId = serverFileId;
		this.remotePath = remotePath;
	}

	/** 
	 * Store a set of values that ContentResolver can process.
	 * @return Set of values
	 */
	public ContentValues getContentValues(){
		ContentValues values = new ContentValues();
		values.put(COL_TYPE, type);
		values.put(COL_PATH, path);
		values.put(COL_MD5, md5);
		values.put(COL_TO_DOWNLOAD, toDownload);
		values.put(COL_LAST_MODIFIED, lastModified);
		values.put(COL_SERVER_FILE_ID, serverFileId);
		values.put(COL_REMOTE_PATH, remotePath);
		return values;
	}
	
	//select _id from file where to_download=1 and type=type
	/**
	 * Select id from file in the internal database where to_download=1.
	 * @param type file type.
	 * @return The number of rows in the cursor.
	 */
	public int getCountToDownload(String type) {
		Cursor c = DBAdapter.query(TABLE_NAME, new String[]{BaseColumns._ID}, 
				COL_TO_DOWNLOAD+Constants.EQUALS_STRING+Constants.ONE+Constants.AND_STRING+
				COL_TYPE+Constants.EQUALS_STRING+Constants.SINGLE_QUOTE_STRING+type+Constants.SINGLE_QUOTE_STRING,
				null, null, null, null);
		int lenght = c.getCount();
		c.close();
		return lenght;
	}
	
	//select path from file where type=type
	/**
	 * Retrieve each path of file based on different types.
	 * @param type File type.
	 * @return An array contains each path of file.
	 */
	public String[] getFilePathsByType(String type) {
		String [] result = null;
		Cursor c = DBAdapter.query(TABLE_NAME, new String[]{COL_PATH},
				COL_TYPE+Constants.EQUALS_STRING+Constants.SINGLE_QUOTE_STRING+type+Constants.SINGLE_QUOTE_STRING, 
				null, null, null, null);
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
	

	/** Delete files that will not be used.
	 * @param files ArrayList contains all media files.
	 * @param type File type.
	 */
	public void cleanNonUsedServerFiles(ArrayList<Integer> files, String type){
		StringBuffer clause = new StringBuffer(Constants.OPEN_PARENTHESIS_STRING);
		for (int i = 0; i< files.size(); i++) {
			clause.append(files.get(i));
			if (i != (files.size() - 1) ) {
				clause.append(Constants.COMMA_STRING);
			}
		}
		clause.append(Constants.CLOSE_PARENTHESIS_STRING);
		if (clause.length() > 2) {
			//select _id, path from file where code NOT IN (clause) and type = TYPE
			Cursor c = DBAdapter.query(TABLE_NAME, new String [] {BaseColumns._ID, COL_PATH}, 
					COL_SERVER_FILE_ID+Constants.NOT_IN_STRING+clause.toString()
					+ Constants.AND_STRING 
					+ COL_TYPE + Constants.EQUALS_STRING+Constants.SINGLE_QUOTE_STRING+type+Constants.SINGLE_QUOTE_STRING,
					null, null, null, null);
			if (c.getCount() > 0) {
				c.moveToFirst();
				for (int i = 0; i< c.getCount(); i++) {
					/* TODO: confirm. do we really want to delete the file in the filesystem???
					java.io.File f = new java.io.File(c.getString(1));
					if (f.exists()) {
						f.delete();
					}
					*/
					DBAdapter.delete(TABLE_NAME, BaseColumns._ID+Constants.EQUALS_STRING+c.getInt(0), null);
					c.moveToNext();
				}
			}
			c.close();
		}
	}
	
	/** Remove files that server does not want anymore.
	 * @param context
	 */
	public void deleteUnwantedFiles(Context context) {
		ArrayList<String> filesToKeep = getFilesToKeep(context);
		ArrayList<FileInfo> sdcardFiles = FileUtils.getFilesAsArrayListRecursive(Environment.getExternalStorageDirectory()+context.getString(R.string.app_path_base));
		
		for(FileInfo sdcardFile : sdcardFiles) {
			String fileToDelete = sdcardFile.path();
			if (!filesToKeep.contains(fileToDelete)) {
				Log.i(Constants.LOG_TAG, "File: delete " + fileToDelete);
				new java.io.File(fileToDelete).delete();
			}	
		}
	}
	
	//select path from file where to_download != TO_DELETE
	/** Retrieve all needed path of files.
	 * @param context
	 * @return An arrayList contains all the needed path of files.
	 */
	private ArrayList<String> getFilesToKeep(Context context) {
		ArrayList<String>  result = new ArrayList<String>();
		
		//1.- downloaded files
		Cursor c = DBAdapter.query(TABLE_NAME, new String[]{COL_PATH}, null, null, null, null, null);
		int len = c.getCount();
		if (len > 0) {
			c.moveToFirst();
			for (int i = 0; i < len; i++) {
				result.add(c.getString(0));
				c.moveToNext();
			}
		}
		c.close();

		ArrayList<String> instanceFiles = FileUtils.getFileNames(Environment.getExternalStorageDirectory()
				+ context.getString(R.string.app_path_base)
				);
		if (!instanceFiles.isEmpty()) {
			for (String file: instanceFiles) {
				result.add(file);
			}
		}
		//2.- saved images
		/*
		ArrayList<String> imageFiles = FileUtils.getFileNames(Environment.getExternalStorageDirectory()
															+ context.getString(R.string.app_path_base)
															+ context.getString(R.string.app_odk_path)
															+ context.getString(R.string.app_odk_data_path));
		if (!imageFiles.isEmpty()) {
			for (String file: imageFiles) {
				result.add(file);
			}
		}
		*/
		
		
		return result;
	}
	
	//select * from file where type = 'type' and to_download= 1 LIMIT 0,1;
	/** Retrieve next file which should be download.
	 * @param type File type.
	 * @return The file will be download next.
	 */
	public File getNextFileToDownload(String type) {
		File result = null;
		Cursor c = DBAdapter.query(TABLE_NAME, new String[]{BaseColumns._ID, COL_TYPE, COL_PATH, COL_MD5, COL_TO_DOWNLOAD, 
				COL_LAST_MODIFIED, COL_SERVER_FILE_ID, COL_REMOTE_PATH },
				COL_TYPE+Constants.EQUALS_STRING+Constants.SINGLE_QUOTE_STRING+type+Constants.SINGLE_QUOTE_STRING+
				Constants.AND_STRING+COL_TO_DOWNLOAD+Constants.EQUALS_STRING+Constants.ONE,
				null, null, null, null);
		if (c.getCount() > 0) {
			c.moveToFirst();
			result = new File(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getInt(4), c.getString(5), c.getInt(6), c.getString(7));
		}
		c.close();
		return result;
	}
	
	//select * from file where server_file_id = id
	/** Get the file based on server ID.
	 * @param id ID of the file in the server
	 * @return File instance.
	 */
	public File getFileFromServerId (int id) {
		File result = null;
		Cursor c = DBAdapter.query(TABLE_NAME, new String[]{BaseColumns._ID, COL_TYPE, COL_PATH, COL_MD5, COL_TO_DOWNLOAD, 
				COL_LAST_MODIFIED, COL_SERVER_FILE_ID, COL_REMOTE_PATH },
				COL_SERVER_FILE_ID+Constants.EQUALS_STRING+id,
				null, null, null, null);
		if (c.getCount() == 1) {
			c.moveToFirst();
			result = new File(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getInt(4), c.getString(5), c.getInt(6), c.getString(7));
		}
		c.close();
		return result;
	
	}
	
	/** Store all file that can be download.
	 * @param id File ID.
	 * @param toDownload Integer represents if the file can be download
	 * @return The number of rows affected
	 */
	public int markToDownload(int id, boolean toDownload) {
		ContentValues values = new ContentValues();
		values.put(COL_TO_DOWNLOAD, toDownload ? Constants.ONE: Constants.ZERO);
		return DBAdapter.update(TABLE_NAME, values, BaseColumns._ID+Constants.EQUALS_STRING+id, null);
	}
	
	/** Get file path.
	 * @param id File ID.
	 * @return File path.
	 */
	public static String getPath(int id) {
		String result = Constants.EMPTY_STRING;
		
		Cursor c = DBAdapter.query(TABLE_NAME,new String[]{COL_PATH},BaseColumns._ID+Constants.EQUALS_STRING+id,null, null, null, null);
		if (c.getCount() == 1) {
			c.moveToFirst();
			result = c.getString(0);
		}
		c.close();
		return result;
	}
}