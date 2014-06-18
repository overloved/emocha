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
package org.emocha.c2dm.tasks;

import org.emocha.Constants;
import org.emocha.utils.Server;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * ConfirmAlertTask is to check if the C2DM notification has been successfully confirmed.
 * Running on the back end.
 * @author 
 *
 */
public class ConfirmAlertTask extends AsyncTask<String, Void, Boolean> {

	Context context;
	
	public ConfirmAlertTask(Context c) {
		this.context = c;
	}
	@Override
	protected Boolean doInBackground(String... arg0) {
		boolean result = false;
		String alertId = arg0[0];
		Server.init(context);
		JSONObject response = Server.confirmAlert(alertId);
		
		try {
			if (response != null && Server.CMD_RESPONSE_OK.equals(response.getString(Server.CMD_RESPONSE_STATUS))) {
				Log.d(Constants.LOG_TAG, "C2DM notification successfully confirmed: "+alertId);
				result = true;
			} else {
				if (response != null) {
					Log.e(Constants.LOG_TAG, "Error registering device to the server: response = "+ response.getString(Server.CMD_RESPONSE_MSG));
				} else {
					Log.e(Constants.LOG_TAG, "server response is null??");
				}
			}
		} catch (JSONException e) {
			Log.e(Constants.LOG_TAG, "Error reading response from server, while receiving a custom message");
		}
		return result;
	}
}
