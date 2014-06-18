/*
 * Copyright (C) 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.emocha.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import org.emocha.Constants;

import android.os.Environment;
import android.util.Log;

import com.twmacinta.util.MD5;

/**
 * Static methods used for common file operations.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * 	
 * @author Pau Varela - pau.varela@gmail.com. Few methods were deleted and some added.
 */
public class FileUtils {

	/**
	 * Retrive all the child files under the specific file. Liek a tree. 
	 * @param path
	 * @return
	 */
	public static ArrayList<FileInfo> getFilesAsArrayListRecursive(String path) {
		ArrayList<FileInfo> mFileList = new ArrayList<FileInfo>();
		File root = new File(path);
		getFilesAsArrayListRecursiveHelper(root, mFileList);
		return mFileList;
	}

	/**
	 * A helper to iterate each parent file and retrieve its current child files.
	 * @param f
	 * @param filelist
	 */
	private static void getFilesAsArrayListRecursiveHelper(File f,
			ArrayList<FileInfo> filelist) {
		if (f.isDirectory()) {
			File[] childs = f.listFiles();
			for (File child : childs) {
				getFilesAsArrayListRecursiveHelper(child, filelist);
			}
			return;
		}
		filelist.add(new FileInfo(f.getAbsolutePath(), f.lastModified(), f
				.length(), Constants.EMPTY_STRING));
	}

	// copy from the getFilesAsArrayListRecursive, but getting only the filenames
	/**
	 * Copy from the getFilesAsArrayListRecursive, but getting only the filenames.
	 * @param path The file path.
	 * @return List contains all file names under the same path.
	 */
	public static ArrayList<String> getFileNames(String path) {
		ArrayList<String> mFileList = new ArrayList<String>();
		File root = new File(path);
		getFileNamesHelper(root, mFileList);
		return mFileList;
	}

	/**
	 * A helper to iterate file list and to retrieve all the file name under the same path.
	 * @param f
	 * @param filelist
	 */
	private static void getFileNamesHelper(File f, ArrayList<String> filelist) {
		if (f.isDirectory()) {
			File[] childs = f.listFiles();
			for (File child : childs) {
				getFileNamesHelper(child, filelist);
			}
			return;
		}
		filelist.add(f.getAbsolutePath());
	}
	
	/**
	 * Create a folder when storage are ready to use.
	 * @param path The path that the folder will be created in.
	 * @return Whether created or not.
	 */
	public static boolean createFolder(String path) {
		if (storageReady()) {
			boolean made = true;
			File dir = new File(path);
			if (!dir.exists()) {
				made = dir.mkdirs();
			}
			return made;
		} else {
			return false;
		}
	}

	/**
	 * Get file names based on the file path.
	 * @param path
	 * @return
	 */
	public static String getFilename(String path) {
		int sep = path.lastIndexOf('/');
		return path.substring(sep + 1);
	}

	/**
	 * Get parent folder path.
	 * @param path The current file path.
	 * @return
	 */
	public static String getFolder(String path) {
		int sep = path.lastIndexOf('/');
		return path.substring(0, sep);
	}
	
	private static boolean storageReady() {
		String cardstatus = Environment.getExternalStorageState();
		if (cardstatus.equals(Environment.MEDIA_REMOVED)
				|| cardstatus.equals(Environment.MEDIA_UNMOUNTABLE)
				|| cardstatus.equals(Environment.MEDIA_UNMOUNTED)
				|| cardstatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Encrypt the media file by using Md5.
	 * @param file
	 * @return Encrypted string of the file.
	 */
	public static String getMd5Hash(File file) {
		try {
			return MD5.asHex(MD5.getHash(file));
		} catch (IOException e) {
			Log.e("MD5", e.getMessage());
			return null;
		}
	}

	/**
	 * Read media file and output corresponding string data.
	 * @param tPath The media file path.
	 * @return String data of the media file.
	 * @throws java.io.IOException
	 */
	public static String readFileAsString(String tPath)
			throws java.io.IOException {
		StringBuffer tData = new StringBuffer(1000);
		BufferedReader tReader = new BufferedReader(new FileReader(tPath));
		char[] tBuffer = new char[1024];
		int tChars = 0;
		while ((tChars = tReader.read(tBuffer)) != -1) {
			tData.append(tBuffer, 0, tChars);
		}
		tReader.close();
		return tData.toString();
	}
	
	/**replaces a file with the provided data.
	 * @param	data	content for the new file
	 * @param	path 	where the file is stored
	 * @return <b>true</b> when the file is replaced, <b>false</b> otherwise*/
	public static boolean replaceFile(String data, String path) {
		boolean result = false;
		File file = new File(path);
		if (file.exists()) {
			file.delete(); //delete always. method is called when there is an update
		} else { //be sure the path exists..
			File parent = file.getParentFile();
			parent.mkdirs();
		}
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			bw.write(data);
			bw.close();
			result = true;
		} catch (Exception e) {
			Log.e(Constants.LOG_TAG, "FileUtils: replaceFile(): error replacing file "+path);
			Log.e(Constants.LOG_TAG, e.getMessage());
		}
		return result;
	}
	
	/**
	 * Copy file implementation. Use fileChannel to realize.
	 * @param sourceFile The original file where comes from. 
	 * @param destFile The destination file where the original file is copied to.
	 * @throws IOException
	 */
	public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (sourceFile.exists()) {
            FileChannel src = null, dst = null;
            try {
                src = new FileInputStream(sourceFile).getChannel();
                dst = new FileOutputStream(destFile).getChannel();
                
                dst.transferFrom(src, 0, src.size());
            } catch (FileNotFoundException e) {
                Log.e(Constants.LOG_TAG, "FileNotFoundExeception while copying file");
                e.printStackTrace();
            } finally {
            	if (src != null) {
            		src.close();
            	}
            	if (dst != null) {
            		dst.close();
            	}
            }
        } else {
            Log.e(Constants.LOG_TAG, "Source file does not exist: " + sourceFile.getAbsolutePath());
        }

    }

}
