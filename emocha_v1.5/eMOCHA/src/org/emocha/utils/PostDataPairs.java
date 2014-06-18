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

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

/**
 * PostDataPairs make a list of data pairs with key/value format.
 * Those data will be posted to the server.
 * @author 
 *
 */
public class PostDataPairs {
	List<NameValuePair> data;   	

	/**
	 * Class constructor. Initialize ArrayList to store each data pair.
	 */
	public PostDataPairs() {
		data = new ArrayList<NameValuePair>(2);   	
	}
	
	/**
	 * Add data pair to the ArrayList.
	 * @param key Key attribute of data pair.
	 * @param val Value attribute of data pair.
	 */
	public void add(String key, String val) {
		// BasicNameValuePair encapsulate an attribute/value pair. Like map.
		data.add(new BasicNameValuePair(key, val));
	}
	
	/**
	 * Get ArrayList of data pair.
	 * @return
	 */
	public List<NameValuePair> get() {
		return data;
	}
}
