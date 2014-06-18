/*******************************************************************************
 * eMOCHA - electronic Mobile Open-Source Comprehensive Health Application
 * Copyright (c) 2012  Pau Varela - pau.varela@gmail.com
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
package org.emocha.tasks;


import org.emocha.model.entities.FormDataFile;
import org.emocha.utils.Server;

import android.content.Context;


/**Uploads any associated file a FormData might have.*/
public class FormDataFilesTask implements Runnable {
	
	private int mFormDataId;
	private Context mContext;
	
	public FormDataFilesTask(Context c, int fdId) {
		this.mContext = c;
		this.mFormDataId = fdId;
	}
	
	@Override
	public void run() {
		
		//retrieve form_data_files
		FormDataFile[] files = FormDataFile.getFormDataFilesReady(mFormDataId);
		
		//upload them (if any)
		if (files != null) {
			for (int i= 0; i<files.length; i++) {
				Server.uploadFormDataFile(mContext, files[i]);
			}
		}
		
	}
}
