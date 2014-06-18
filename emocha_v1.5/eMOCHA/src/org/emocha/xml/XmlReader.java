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
package org.emocha.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.emocha.Constants;
import org.xml.sax.SAXException;

import android.sax.Element;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.util.Log;
import android.util.Xml;

public class XmlReader {
	
	/**iterates recursively the provided node until it gets the targetNode
	 * @param targetNode	targetNode's name
	 * @param st	tokenizer with the child's name (root element is NOT included)
	 * @param node	Element
	 * @return the Element with the provided targetNode's name*/
	private static Element getTargetChild(String targetNode, StringTokenizer st, Element node) {
		String childName = st.nextToken();
		if (targetNode.equals(childName)){
			return node.getChild(targetNode);	
		} else {
			return getTargetChild(targetNode, st, node.getChild(childName));
		}
	}
	/**based on: http://www.ibm.com/developerworks/opensource/library/x-android/index.html?ca=dgr-lnxw82Android-XML&S_TACT=105AGX59&S_CMP=grlnxw82
	 *           Listing-8 (even more simplified)
	 * @param path	an XPath path (e.g: "/root/child/target")
	 * @param xml 	xml content as String
	 * @return the value of the provided path, or an empty String 
	 * @throws SAXException */
	//TODO: replace calls to this method and use getStringValueFromXml() instead. Make this method private
	public static List<String> getNodeValue(String path, String xml) throws SAXException {
		final List<String> result = new ArrayList<String>();
        RootElement root = null;
		String [] nodes = path.split(Constants.SLASH_STRING);
		String target = nodes[nodes.length-1]; // last node is the target
        StringTokenizer st = new StringTokenizer(path,Constants.SLASH_STRING);
		 
        if (st.countTokens() > 1) {
        	root = new RootElement(st.nextToken()); //set the root
        	
        	Element node = getTargetChild(target, st, root); // search the target

        	//called when the Element's text ends
        	node.setEndTextElementListener(new EndTextElementListener(){
                public void end(String body) {
                    result.add(body);
                }
            });
        }

       	if (root != null) {
       		Xml.parse(xml, root.getContentHandler());
       	}        	
		return result;
	}
	
	public static String getStringValueFromXml(String xml, String path) {
		String value = Constants.EMPTY_STRING;
		List<String> nodeValue;
		
		try {
			nodeValue = XmlReader.getNodeValue(path, xml);
			if (!nodeValue.isEmpty()) {
				value = nodeValue.get(0); //the method must return 1 element! 
			}
		} catch (SAXException e) {
			Log.w(Constants.LOG_TAG, "XMLReader: Error while getting a value from the xml data (it might be empty). Looking for path:"+path);
			e.printStackTrace();
		}
				
		return value;
	}
}