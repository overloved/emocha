/*
 * Copyright (C) 2009 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.widgets;

import org.javarosa.core.model.Constants;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.utilities.QuestionData;

import android.content.Context;

/**
 * Convenience class that handles creation of widgets.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class WidgetFactory {
	
    private static boolean flag;
	/**
     * Returns the appropriate QuestionWidget for the given FormEntryPrompt.
     * 
     * @param pe prompt element to be rendered
     * @param context Android context
     * @param instancePath path to the instance file
     */
    static public QuestionData createWidgetFromPrompt(FormEntryPrompt fep, Context context,
            String instancePath) {
    	IQuestionWidget questionWidget = null;
    	QuestionData data = new QuestionData(questionWidget, 1);
        switch (fep.getControlType()) {
            case Constants.CONTROL_INPUT:
                switch (fep.getDataType()) {
                    case Constants.DATATYPE_DATE:
                        questionWidget = new DateWidget(context);
                        data.setQuestionWidget(questionWidget);
                        data.setIsSwipeRequired(1);
                        break;
                    case Constants.DATATYPE_DECIMAL:
                        questionWidget = new DecimalWidget(context);
                        data.setQuestionWidget(questionWidget);
                        data.setIsSwipeRequired(1);
                        break;
                    case Constants.DATATYPE_INTEGER:
                        questionWidget = new IntegerWidget(context);
                        data.setQuestionWidget(questionWidget);
                        data.setIsSwipeRequired(1);
                        break;
                    case Constants.DATATYPE_GEOPOINT:
                        questionWidget = new GeoPointWidget(context);
                        data.setQuestionWidget(questionWidget);
                        data.setIsSwipeRequired(1);
                        break;
                    case Constants.DATATYPE_BARCODE:
                        questionWidget = new BarcodeWidget(context);
                        data.setQuestionWidget(questionWidget);
                        data.setIsSwipeRequired(1);
                        break;
                    /*eMOCHA*/
                    case Constants.DATATYPE_TIME:
                    	questionWidget = new TimeWidget(context);
                    	data.setQuestionWidget(questionWidget);
                        data.setIsSwipeRequired(1);
                    	break;
                    /**/
                    default:
                        questionWidget = new StringWidget(context);
                        data.setQuestionWidget(questionWidget);
                        data.setIsSwipeRequired(1);
                        break;
                }
                break;
            case Constants.CONTROL_IMAGE_CHOOSE:
                questionWidget = new ImageWidget(context, instancePath);
                data.setQuestionWidget(questionWidget);
                data.setIsSwipeRequired(0);
                break;
            case Constants.CONTROL_AUDIO_CAPTURE:
                questionWidget = new AudioWidget(context, instancePath);
                data.setQuestionWidget(questionWidget);
                data.setIsSwipeRequired(1);
                break;
            case Constants.CONTROL_VIDEO_CAPTURE:
                questionWidget = new VideoWidget(context, instancePath);
                data.setQuestionWidget(questionWidget);
                data.setIsSwipeRequired(0);
                break;
            case Constants.CONTROL_SELECT_ONE:
                questionWidget = new SelectOneWidget(context);
                data.setQuestionWidget(questionWidget);
                data.setIsSwipeRequired(1);
                break;
            case Constants.CONTROL_SELECT_MULTI:
                questionWidget = new SelectMultiWidget(context);
                data.setQuestionWidget(questionWidget);
                data.setIsSwipeRequired(1);
                break;
            case Constants.CONTROL_TRIGGER:
                questionWidget = new TriggerWidget(context);
                data.setQuestionWidget(questionWidget);
                data.setIsSwipeRequired(1);
                break;
            default:
                questionWidget = new StringWidget(context);
                data.setQuestionWidget(questionWidget);
                data.setIsSwipeRequired(1);
                break;
        }
        questionWidget.buildView(fep);        
        return data;
    }

}
