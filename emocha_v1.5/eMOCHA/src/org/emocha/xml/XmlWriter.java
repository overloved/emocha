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


import org.emocha.Constants;
import org.xml.sax.SAXException;


public class XmlWriter {
	
    // provided serializers require to know the xml structure and we should define a writer per each form!
	/**raw String replacement in the xml content. Sets the provided value to the given path. 
	 * Values must be within a <tag>value</tag>, or empty <tag /> 
	 * @param path	path to the node to be replaced
	 * @param xml	xml content
	 * @param value	the value
	 * @return modified xml or null when the path wasn't found.
	 * @throws SAXException */
	public static String setNodeValue(String path, String xml, String value) throws SAXException {
		StringBuilder result = null;
		String [] nodes = path.split(Constants.SLASH_STRING);
		String tagName = nodes[nodes.length-1]; // last node is the target
      
        if (!(XmlReader.getNodeValue(path, xml)).isEmpty()) { //the node exists
    		String openTag = Constants.XML_OPEN_TAG+tagName+Constants.XML_END_TAG;
            String closeTag = Constants.XML_CLOSE_TAG+tagName+Constants.XML_END_TAG;
        	int start = xml.indexOf(openTag);
    		int end = xml.indexOf(closeTag);
    		
    		if (start > 0 && end > 0 ) { //<tag></tag>
    			result = new StringBuilder(xml);
    			result.replace(start+openTag.length(), end, value);
    		} else { //<tag />
				openTag = Constants.XML_OPEN_TAG+tagName+Constants.XML_CLOSE_SINGLE_TAG;
				start = xml.indexOf(openTag);
				if (start > 0) {
					result = new StringBuilder(xml);
					result.replace(start, start+openTag.length(), Constants.XML_OPEN_TAG+tagName+Constants.XML_END_TAG+value+Constants.XML_CLOSE_TAG+tagName+Constants.XML_END_TAG);
				}
    		}
        } 		
		return result.toString();
	}
}
