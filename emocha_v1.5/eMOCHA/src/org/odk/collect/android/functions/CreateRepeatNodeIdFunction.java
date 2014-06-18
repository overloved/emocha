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
package org.odk.collect.android.functions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Vector;
import org.javarosa.core.model.condition.IFunctionHandler;

import android.content.Context;
import android.util.Log;

public class CreateRepeatNodeIdFunction implements IFunctionHandler {
	private final String CREATED_PERSONS_FILENAME="createdPersons";
	private Context context;
	public CreateRepeatNodeIdFunction(Context c){
		this.context = c;
	}
	@Override
	public Object eval(Object[] args) {
		/*
		a) receives adults_missing as a parameter
  		b) reads a file (hardcoded PATH) with the created persons. if it doesn't exist, returns 0
  		c) calculates next id (created+1)
  		d) updates created persons (cache) file
		*/
		String path = context.getCacheDir()+File.separator+CREATED_PERSONS_FILENAME;
		int adultsMissing = 0;
		try {
			adultsMissing = Integer.parseInt((String)args[0]);
		} catch (NumberFormatException ne) {
			Log.w("CreateRepeatNodeIdFunction", "Error parsing adults missing, probably empty: "+ne.getMessage());
		}
		int createdPersons = getCreatedPersons(path);
		int nextId = createdPersons+1;
		
		updateCreatedPersons(path, adultsMissing, nextId);
		
		return new Double(nextId);
	}

	private int getCreatedPersons(String path) {
		int value = -1;
		try {
			FileInputStream fs = new FileInputStream(path);			
			BufferedReader br = new BufferedReader(new InputStreamReader(fs));
			
			if (fs.available() > 0){
				value = Integer.parseInt(br.readLine());
				br.close();
				fs.close();
			} else {
				value = 0;
			}
		} catch (FileNotFoundException e) {
			value = 0;	
		} catch (IOException e) {
			e.printStackTrace();
		}

		return value;
	}
	
	private void updateCreatedPersons(String path, int adultsMissing, int nextId) {
		try {
			//flush the file 
			File cpFile = new File(path);
			if (cpFile.exists()) {
				cpFile.delete();
			}
			cpFile = new File(path);
			if (nextId < adultsMissing) {
				FileOutputStream fos = new FileOutputStream(cpFile);
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
				bw.write(String.valueOf(nextId));
				bw.close();
				fos.close();
			} else {
				if (cpFile.exists()) {
					cpFile.delete();
				}
			}
		} catch (FileNotFoundException e) {	
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String getName() {
		return "createRepeatNodeId";
	}

	@Override
	@SuppressWarnings("unchecked")
	public Vector getPrototypes() {
		Class [] prototypes = {String.class};
		Vector v = new Vector();
		v.add(prototypes);
		return v;
	}

	@Override
	public boolean rawArgs() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean realTime() {
		// TODO Auto-generated method stub
		return false;
	}

}
