/*******************************************************************************
 * eMOCHA - electronic Mobile Open-Source Comprehensive Health Application
 * Copyright (c) 2009 Abe Pazos - abe@ccghe.net
 *                    Ricardo A.B - blackbitshines@gmail.com
 *                    Pau Varela - pau.varela@gmail.com
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
package org.emocha;

import java.text.SimpleDateFormat;

/** 
 * Set constant values for global use.
 * @author Yao Huang (yao.engineering@gmail.com)
 *
 */
public class Constants {
	
	public static final int ZERO = 0;
	public static final int ONE = 1;
	public static final int MINUS_ONE = -1;
	public static final int TWO = 2;
	  
    public static final int ONE_SECOND = 1000; // milliseconds

    public static final int SERVER_URL_MIN_LENGTH = 10;

    public static final String INTENT_FILTER_ENABLE_GPS_LISTENERS = "org.emocha.ENABLE_GPS_LISTENERS";
    public static final String INTENT_FILTER_DISABLE_GPS_LISTENERS = "org.emocha.DISABLE_GPS_LISTENERS";
    public static final String INTENT_FILTER_GPS_LOCATION_UPDATE = "org.emocha.GPS_LOCATION_UPDATE";
    
    public static final String INTENT_FILTER_REGISTRATION_C2DM ="com.google.android.c2dm.intent.REGISTRATION";
    public static final String INTENT_FILTER_RECEIVE_C2DM ="com.google.android.c2dm.intent.RECEIVE";
    public static final String INTENT_FILTER_C2DM_REGISTER ="org.emocha.C2DM_REGISTER";
    
    public static final String INTENT_FILTER_START_REMINDERS = "org.emocha.midot.START_REMINDERS";
    
    public static final String ODK_FORMPATH_KEY = "formpath";     
    public static final String ODK_INSTANCEPATH_KEY = "instancepath";
    public static final String ODK_INSTANCEXML_KEY = "instancexml";
    public static final String ODK_EXTRA_DATA_KEY = "extra_data";
	public static final String ODK_PERSIST_DATA = "save_data";  //tells odk to save data,instead of waiting to onActivityResult.
	public static final String ODK_CANCELLED = "odk_cancelled"; //whether odk was cancelled or not.
	public static final String ODK_START_TIME = "start_time";   //when form was started
    
	public static final String PARAM_ID = "id";
	public static final String PARAM_REMINDER_ID = "reminder_id";
	
	public static final String JSON_FORM_GROUP = "group";
	public static final String JSON_FORM_CONDITIONS = "conditions";
	public static final String JSON_FORM_REQUIRED_FORMS = "required_forms";
	public static final String JSON_FORM_REQUIRED_ANSWERS = "required_answers";
	public static final String JSON_FORM_FORMS = "forms";
	public static final String JSON_FORM_TEMPLATE = "template";
	
	
    public static final String LOG_TAG = "EMOCHA-miDOT";
    public static final String DIALOG_TAG = "dialog";
    
    public static final String EMPTY_STRING = "";
    public static final String EQUALS_STRING = "=";
    public static final String NOT_EQUALS_STRING = "!=";
    public static final String AND_STRING = " AND ";
    public static final String OR_STRING = " OR ";
    public static final String SQL_EMPTY_STRING = "''";
    public static final String ZERO_STRING = "0";
    public static final String ONE_STRING = "1";
    public static final String MINUS_ONE_STRING = "-1";
    public static final String COMMA_STRING = ",";
    public static final String SLASH_STRING = "/";
    public static final String DASH_STRING ="-";
    public static final String BLANK_STRING =" ";
    public static final String HASH_STRING ="#";
    public static final String DESC_STRING = " DESC";
    public static final String OPEN_PARENTHESIS_STRING = "(";
    public static final String CLOSE_PARENTHESIS_STRING = ")";
    public static final String SINGLE_QUOTE_STRING ="'";
    public static final String NOT_IN_STRING = " NOT IN";
    public static final String NEW_LINE_STRING = "\n";
    public static final String NULL_STRING = "null";
    public static final String DOT_STRING = ".";
    public static final String AMPERSAND_STRING = "&";
    public static final String POST_METHOD_STRING = "POST";
    public static final String VIDEOS_STRING = "videos";
    
    public static SimpleDateFormat STANDARD_DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

    public static final String XML_OPEN_TAG = "<";
    public static final String XML_END_TAG = ">";
    public static final String XML_CLOSE_TAG = "</";
    public static final String XML_CLOSE_SINGLE_TAG = " />";
    public static final String XML_HEADER = "<?xml version='1.0' ?>";
    
    public static int COMMON_REQUEST_CODE = 1;
    
    public static final String ODK_PATIENT_FIRST_NAME = "/data/individual/first_name";
    public static final String ODK_PATIENT_LAST_NAME = "/data/individual/last_name";
    public static final String ODK_PATIENT_SEX = "/data/individual/sex";
    public static final String ODK_PATIENT_AGE = "/data/individual/age";

    //digital signature related
	public static final String JSON_FORM_SIGNATURES_REQUIRED = "signatures_required"; //number of required signatures
	public static final String JSON_FORM_SIGNATURES = "signatures";
	public static final String JSON_FORM_SIGNATURE_ORDER = "order"; //order within the signatures sequence
	public static final String JSON_FORM_SIGNATURE_WHO = "who"; // who's supposed to sign
	public static final String JSON_FORM_SIGNATURE_DESCRIPTION = "description"; //signature's description
    //
    public static final String JSON_FORM_HAS_FILES = "has_files";
    
    public static final String PATIENT_CODE_PREFIX = "P";
    
    
	public static final String ODK_DYNAMIC_DATA = "dynamic_data";
	public static final String ODK_START_HINT_MSG = "start_hint_msg";
	public static final String ODK_NEW_FORM_KEY = "newform";


}
