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
package org.emocha.xml;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.emocha.Constants;
import org.emocha.model.DBAdapter;
import org.emocha.model.entities.FormData;
import org.emocha.model.entities.FormDataFile;
import org.emocha.utils.CommonUtils;
import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.provider.BaseColumns;
import android.util.Xml;

/**
 * Parse form data file. Either media file or form template.
 * @author 
 *
 */
public class FormDataFileParser {
	
	private static final String ATTR_FILE = "file";
	private static final String ATTR_TYPE = "type";

	private int mFormDataId;
	private Context mContext;
	private List<String> mNodes = new ArrayList<String>();
	private StringBuilder mCurrentNode = new StringBuilder(Constants.SLASH_STRING);
	private int mLastDepth = 0;
	
	private String mType;
	private boolean mDataReady=false;
	
	
	public FormDataFileParser(Context context, int fdId){
		this.mContext = context;
		this.mFormDataId = fdId;
	}

	/**
	 * Parse xml format data
	 * @param xml
	 */
	public void parse(String xml) {
		XmlPullParser parser = Xml.newPullParser();
        try {

        	parser.setInput(new ByteArrayInputStream(xml.getBytes()), null);
            int eventType;
            while ((eventType = parser.next()) != XmlPullParser.END_DOCUMENT){
        		
            	if (eventType == XmlPullParser.START_TAG) { //get attributes
            		//update node
            		updateCurrentNode(parser.getDepth(), parser.getName());
            		
            		int totalAttr = parser.getAttributeCount();
            		if (totalAttr > 0) {
            			Map<String, String> attrs = getAttributes(parser, totalAttr);
            			// 1) get attributes: compare with expected ones
            			if (attrs.containsKey(ATTR_FILE) && 
            				Constants.ONE_STRING.equals(attrs.get(ATTR_FILE)) &&
            				attrs.containsKey(ATTR_TYPE)) {
            				
            				mType = attrs.get(ATTR_TYPE);
            				mDataReady = true;
            			}
            		}
                } else if (eventType == XmlPullParser.TEXT) { //get answer
                	if (mDataReady) {
                		
                		String xpath = getCurrentXpath();
                		String filename = parser.getText();
                		
                		FormDataFile fdf = FormDataFile.get(mFormDataId, xpath);
                		FormData fd = new FormData();
        				fd = fd.get(mFormDataId);
        				String fullpath = fd.dataPath + filename;
        				//insert vs update: insert when it doesn't exist. update only when filename has changed.
                		if (fdf == null) {
                			fdf = new FormDataFile(mFormDataId,
													fullpath,
													mType, //TODO: validate type?
													xpath,
													CommonUtils.getCurrentTime(mContext));
                			DBAdapter.insert(FormDataFile.TABLE_NAME, fdf.getContentValues());
                		} else {
                			if (!fullpath.equals(fdf.filename)) {
                				int id = fdf.id;
                				fdf = new FormDataFile(mFormDataId,
										fullpath,
										mType, //TODO: validate type?
										xpath,
										CommonUtils.getCurrentTime(mContext));
                				
                				DBAdapter.update(FormDataFile.TABLE_NAME, fdf.getContentValues(), BaseColumns._ID+Constants.EQUALS_STRING+id, null);
                			}
                		}
        				mType = Constants.EMPTY_STRING;
        				mDataReady = false;
                	}
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e); //TODO change exception.
        }
    }
	
	private String getCurrentXpath() {
		//clear stringbuilder
		mCurrentNode.delete(0, mCurrentNode.length());
		
		mCurrentNode.append(Constants.SLASH_STRING);
		for (int i=0; i< mNodes.size(); i++) {
			mCurrentNode.append(mNodes.get(i));
			if (i < mNodes.size()-1) {
				mCurrentNode.append(Constants.SLASH_STRING);
			}
		}
		return mCurrentNode.toString();
	}
	
	//update current node, according to lastNode, and lastDepth.
	private void updateCurrentNode(int depth, String nodeName) {
		if (depth > mLastDepth) {
			mNodes.add(mLastDepth, nodeName);
			mLastDepth++;
		} else {
			if (depth == mLastDepth) {
				mNodes.remove(mLastDepth-1);
				mNodes.add(mLastDepth-1, nodeName);
			} else {//depth < mLastDepth
				for (int i = 0; i<(mLastDepth - depth); i++) { //go back to the right level
					mLastDepth--;
					mNodes.remove(mLastDepth);
				}
				if (depth == mLastDepth && !(nodeName.equals(mNodes.get(depth-1)))) {
					mNodes.remove(depth-1);
					mNodes.add(depth-1, nodeName);
				}
				/*
				if (!nodeName.equals(mNodes.get(depth-1))) {
					mNodes.remove(depth-1);
					mNodes.add(depth-1, nodeName);
				}
				mNodes.remove(mLastDepth-1);
				mLastDepth--;
				*/
			}
		}
	}
	
	private Map<String,String> getAttributes(XmlPullParser parser,int totalAttr) throws Exception {
	    Map<String,String> attrs = new HashMap<String,String>(totalAttr);
	      
        for(int i=0;i<totalAttr;i++) {
            attrs.put(parser.getAttributeName(i), parser.getAttributeValue(i));
        }
	    return attrs;
	}
}
