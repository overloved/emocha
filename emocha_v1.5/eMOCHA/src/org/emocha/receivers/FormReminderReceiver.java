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
package org.emocha.receivers;

import java.util.Date;

import org.emocha.c2dm.C2DMReceiver;
import org.emocha.c2dm.activities.FormReminderActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Initialize a broadcast receiver to handle event.
 * @author Yao
 *
 */
public class FormReminderReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent arg1) {
		Bundle bundle = arg1.getExtras();
	
		//read params and start activity
		Intent intent = new Intent (context, FormReminderActivity.class);
		intent.putExtras(bundle);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(C2DMReceiver.C2DM_RECEIVED_TS, (new Date()).getTime());
		context.startActivity(intent);
	} 
	
}
