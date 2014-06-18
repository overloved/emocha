/*******************************************************************************
 * eMOCHA - electronic Mobile Open-Source Comprehensive Health Application
 * Copyright (c) 2009 Abe Pazos - abe@ccghe.net
 * 				 2012 Pau Varela - pau.varela@gmail.com
 * 
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
package org.emocha.async;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.emocha.Constants;
import org.emocha.utils.FileUtils;

import android.content.Context;
import android.util.Log;

/**
 * Download single file from server. File could be either media file or form template.
 * @author 
 *
 */
public class DownloadOneFile {
	
	/**
	 * Download single file.
	 * @param	context 
	 * @param	file The specific file object.
	 * @param	serverURL The Url where the file could be download from.
	 */
	public DownloadOneFile(Context context, org.emocha.model.entities.File file, String serverURL) {

		byte[] buffer = new byte[1024];
		int len = 0;
		String folder = FileUtils.getFolder(file.path);
		String fileName = FileUtils.getFilename(file.path);

		FileUtils.createFolder(folder);
		
		try {
			
			URL url = new URL(serverURL+file.remotePath);
			HttpURLConnection hc = (HttpURLConnection) url.openConnection();
			
			if (hc.getResponseCode() > 0 && hc.getResponseCode() == 200) {
				File newFile = new File(folder, fileName);
				FileOutputStream f = new FileOutputStream(newFile);
				
				InputStream in = hc.getInputStream();
				// save data in the device
				while ((len = in.read(buffer)) != -1) {
					f.write(buffer, 0, len);
				}
				f.close();
				
				file.markToDownload(file.id, false); //set as downloaded
				Log.i(Constants.LOG_TAG, "END DOWNLOAD: " + newFile.getPath());
			}
			
			hc.disconnect();
			
		} catch (IOException ioe) {
			Log.e(Constants.LOG_TAG, "IOException: "+ioe.getMessage());
		}
	}
}
