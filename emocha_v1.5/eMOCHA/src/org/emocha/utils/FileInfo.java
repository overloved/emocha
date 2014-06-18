/*******************************************************************************
 * eMOCHA - electronic Mobile Open-Source Comprehensive Health Application
 * Copyright (c) 2009 Abe Pazos - abe@ccghe.net
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
package org.emocha.utils;

//TODO get rid of this class..

/**
 * Not being used.
 * @author 
 *
 */
public class FileInfo {
	private String mPath;
	private long mLastModified;
	private long mLength;
	private String mMD5;
	
	public FileInfo(String path, long lastModified, long length, String md5) {
		mPath 			= path;
		mLastModified 	= lastModified;
		mLength 		= length;
		mMD5			= md5;
	}

	public String path() {
		return mPath;
	}
}
