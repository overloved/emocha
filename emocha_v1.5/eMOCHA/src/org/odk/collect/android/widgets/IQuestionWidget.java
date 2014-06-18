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

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;

import android.content.Context;

/**
 * QuestionWidgets are the main elements in QuestionView. QuestionView is a ScrollView, so widget
 * designers don't need to worry about scrolling. Each widget does need to handle the 'ReadOnly'
 * case in BuildView().
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public interface IQuestionWidget {
	
    /**
     * Get answers patients choose from the question view.
     * @return
     */
    public IAnswerData getAnswer();
    
    /**
     * Remove the answers after being used.
     */
    public void clearAnswer();
    
    /**
     * Build question view for patient to fill. View depends on form type.
     * @param prompt Form to be built and visulized.
     */
    public void buildView(FormEntryPrompt prompt);
    /**
     * Deal with cases when context is being focused.
     * @param context
     */
    public void setFocus(Context context);
}
