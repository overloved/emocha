/*******************************************************************************
 * eMOCHA - electronic Mobile Open-Source Comprehensive Health Application
 * Copyright (c) 2010  Pau Varela - pau.varela@gmail.com
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

import org.apache.http.HttpVersion;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
//import org.emocha.model.Preferences;
import org.emocha.Constants;
import org.emocha.security.EasySSLSocketFactory;

import android.content.Context;
import android.util.Log;

/**
 * Not used.
 * @author 
 *
 */
public class Client {
	//pau 20101004: not used yet. TODO: merge it with Server class.
	//private static String user;
	//private static String password;
	
	private static DefaultHttpClient httpClient = null;
	
	
	public Client(Context context) {
		//user = Preferences.getUser(context);
		//password = Preferences.getPassword(context);
	}
	
	/**@return DefaultHttpClient using custom EasySSLSocketFactory. no SSL certificate validation is performed */
	private  DefaultHttpClient getSelfSignedHttpClient(HttpParams params) {

		if (params == null) {
			params = new BasicHttpParams();
		} 
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		ClientConnectionManager clientConnectionManager;		
		
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(), 443));

		params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 1);
		params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(1));
		params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "utf8");

		clientConnectionManager = new ThreadSafeClientConnManager(params, schemeRegistry);
		return new DefaultHttpClient(clientConnectionManager, params);
	}
	
	public DefaultHttpClient getHttpClient(HttpParams params, boolean selfSSLcertificate) {
		if (httpClient == null) {
			
			if (selfSSLcertificate) { // according to app_config.json (defined in the server)				
				httpClient = getSelfSignedHttpClient(params);
				Log.w(Constants.LOG_TAG,"connecting to a server with a self-signed certificated");
			} else {
				if (params != null) {
					httpClient = new DefaultHttpClient(params);
				} else {
					httpClient = new DefaultHttpClient();
				}
			}
		}
		return httpClient;
	}
}
