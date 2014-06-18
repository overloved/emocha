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

import org.emocha.utils.ScreenEnvironment;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.views.IAVTLayout;
import org.odk.collect.android.views.QuestionView;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TableLayout.LayoutParams;

import java.util.Vector;

/**
 * SelctMultiWidget handles multiple selection fields using checkboxes.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class SelectMultiWidget extends LinearLayout implements IQuestionWidget {
    private final static int CHECKBOX_ID = 100;
    private boolean mCheckboxInit = true;
    private TextView topTextView;
    private TextView titleTextView;
    private TextView selectTextView;
    
    Vector<SelectChoice> mItems;
    Drawable mDivider;


    public SelectMultiWidget(Context context) {
        super(context);
    }


    /* (non-Javadoc)
     * @see org.odk.collect.android.widgets.IQuestionWidget#()
     */
    public void clearAnswer() {
        int j = mItems.size();
        for (int i = 0; i < j; i++) {

            // no checkbox group so find by id + offset
            CheckBox c = ((CheckBox) findViewById(CHECKBOX_ID + i));
            if (c.isChecked()) {
                c.setChecked(false);
            }
        }
    }


    public IAnswerData getAnswer() {
        Vector<Selection> vc = new Vector<Selection>();
        for (int i = 0; i < mItems.size(); i++) {
            CheckBox c = ((CheckBox) findViewById(CHECKBOX_ID + i));
            if (c.isChecked()) {
                vc.add(new Selection(mItems.get(i).getValue()));
            }

        }

        if (vc.size() == 0) {
            return null;
        } else {
            return new SelectMultiData(vc);
        }

    }


    /* (non-Javadoc)
     * @see org.odk.collect.android.widgets.IQuestionWidget#buildView(org.javarosa.form.api.FormEntryPrompt)
     */
    @SuppressWarnings("unchecked")
    public void buildView(final FormEntryPrompt prompt) {
    	
    	setGravity(Gravity.CENTER_HORIZONTAL);
    	
    	topTextView = new TextView(getContext());
    	titleTextView = new TextView(getContext());
    	selectTextView = new TextView(getContext());
    	topTextView.setText("Are you experiencing any");
    	titleTextView.setText("Symptoms?");
    	selectTextView.setText("Please select all that apply");
    	titleTextView.setTypeface(null, Typeface.BOLD);
    	topTextView.setTextColor(Color.WHITE);
    	titleTextView.setTextColor(Color.WHITE);
    	selectTextView.setTextColor(Color.WHITE);
    	
    	/** Title layout */
    	RelativeLayout layout = new RelativeLayout(getContext());
    	RelativeLayout.LayoutParams layoutParams = new RelativeLayout.
    			LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    	layout.setLayoutParams(layoutParams);
    	    	
    	RelativeLayout.LayoutParams topLayoutParams = new RelativeLayout.
  	    		LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
  	    topLayoutParams.topMargin = (int)(ScreenEnvironment.height * 0.0414);
  	    topLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
  	    topTextView.setLayoutParams(topLayoutParams);
  	    topTextView.setTextSize((int)(ScreenEnvironment.height * 0.03125));
  	    layout.addView(topTextView);
  
  	    RelativeLayout.LayoutParams titleLayoutParams = new RelativeLayout.
	    		LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	    titleLayoutParams.topMargin = (int)(ScreenEnvironment.height * 0.0883);
	    titleLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
	    titleTextView.setLayoutParams(titleLayoutParams);
  	    titleTextView.setTextSize((int)(ScreenEnvironment.height * 0.078125));
  	    layout.addView(titleTextView);
  	    
  	    RelativeLayout.LayoutParams selectLayoutParams = new RelativeLayout.
  	    		LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	    selectLayoutParams.topMargin = (int)(ScreenEnvironment.height * 0.2164);
	    selectLayoutParams.bottomMargin = (int)(ScreenEnvironment.height * 0.049);
	    selectLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
	    selectTextView.setLayoutParams(selectLayoutParams);
  	    selectTextView.setTextSize((int)(ScreenEnvironment.height * 0.03125));
  	    layout.addView(selectTextView);
  	    
	  	LinearLayout topLinearLayout = new LinearLayout(getContext());
	  	LinearLayout.LayoutParams topLinearLayoutParams = new LinearLayout.
	  			LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	  	topLinearLayout.setLayoutParams(topLinearLayoutParams);
	    
	  	/** */
  	    LinearLayout choiceLayout = new LinearLayout(getContext());
  	    LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.
			    LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	    linearLayoutParams.topMargin = ((int)(ScreenEnvironment.height * 0.049));
  	    choiceLayout.setLayoutParams(linearLayoutParams);
	    choiceLayout.setOrientation(LinearLayout.VERTICAL);
	    
	    topLinearLayout.addView(layout);
	    addView(topLinearLayout);
	    
//	    RelativeLayout bottomRelativeLayout = new RelativeLayout(getContext());
//	    RelativeLayout.LayoutParams bottomLayoutParams = new RelativeLayout.
//	    		LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//	    bottomLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
//	    bottomRelativeLayout.setLayoutParams(bottomLayoutParams);
//	    choiceLayout.setGravity(Gravity.CENTER);
//	    bottomRelativeLayout.addView(choiceLayout);
	    addView(choiceLayout);
    	
        mItems = prompt.getSelectChoices();
        setOrientation(LinearLayout.VERTICAL);
        
        
        Vector ve = new Vector();
        if (prompt.getAnswerValue() != null) {
            ve = (Vector) prompt.getAnswerValue().getValue();
        }

        if (prompt.getSelectChoices() != null) {
            for (int i = 0; i < mItems.size(); i++) {
                // no checkbox group so id by answer + offset
                CheckBox c = new CheckBox(getContext());

                // when clicked, check for readonly before toggling
                c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (!mCheckboxInit && prompt.isReadOnly()) {
                            if (buttonView.isChecked()) {
                                buttonView.setChecked(false);
                            } else {
                                buttonView.setChecked(true);                            
                            }
                        }
                    }
                });
                
                c.setId(CHECKBOX_ID + i);
                c.setPadding((int)(ScreenEnvironment.height * 0.07), 
                		(int)(ScreenEnvironment.height * 0.0078125), 
                		(int)(ScreenEnvironment.height * 0.0258), 
                		(int)(ScreenEnvironment.height * 0.0258));
//                c.setText(prompt.getSelectChoiceText(mItems.get(i)));
//                c.setTextSize(TypedValue.COMPLEX_UNIT_PX, QuestionView.APPLICATION_FONTSIZE);
//                c.setTextSize((int)(ScreenEnvironment.height * 0.043));
//                c.setTextColor(Color.WHITE);
                c.setFocusable(!prompt.isReadOnly());
                c.setEnabled(!prompt.isReadOnly());
                for (int vi = 0; vi < ve.size(); vi++) {
                    // match based on value, not key
                    if (mItems.get(i).getValue().equals(((Selection) ve.elementAt(vi)).getValue())) {
                        c.setChecked(true);
                        break;
                    }

                }

                String audioURI = null;
                if (prompt.getSelectTextForms(mItems.get(i)).contains(
                    FormEntryCaption.TEXT_FORM_AUDIO)) {
                    audioURI =
                        prompt.getSelectChoiceText(mItems.get(i), FormEntryCaption.TEXT_FORM_AUDIO);
                }

                String imageURI = null;
                if (prompt.getSelectTextForms(mItems.get(i)).contains(
                    FormEntryCaption.TEXT_FORM_IMAGE)) {
                    imageURI =
                        prompt.getSelectChoiceText(mItems.get(i), FormEntryCaption.TEXT_FORM_IMAGE);
                }

                String videoURI = null; // TODO: uncomment when video ready
                /*
                 * if (prompt.getSelectTextForms(mItems.get(i)).contains(
                 * FormEntryCaption.TEXT_FORM_IMAGE)) { imageURI =
                 * prompt.getSelectChoiceText(mItems.get(i), FormEntryCaption.TEXT_FORM_IMAGE); }
                 */
                
                LinearLayout checkboxLayout = new LinearLayout(getContext());
                LinearLayout.LayoutParams checkboxLayoutParams = new LinearLayout.
                		LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);           
                checkboxLayout.setLayoutParams(checkboxLayoutParams);
                checkboxLayout.setOrientation(HORIZONTAL);
                
                TextView checkboxTextView = new TextView(getContext());
                LinearLayout.LayoutParams checkboxTextLayoutParams = new LinearLayout.
                		LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                checkboxTextView.setLayoutParams(checkboxTextLayoutParams);
                checkboxTextView.setText(prompt.getSelectChoiceText(mItems.get(i)));
                checkboxTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, QuestionView.APPLICATION_FONTSIZE);
                checkboxTextView.setTextSize((int)(ScreenEnvironment.height * 0.043));
                checkboxTextView.setTextColor(Color.WHITE);                
                
                IAVTLayout mediaLayout = new IAVTLayout(getContext());
                LinearLayout.LayoutParams mediaLayoutParams = new LinearLayout.
                		LayoutParams((int)(ScreenEnvironment.height * 0.15625), ViewGroup.LayoutParams.WRAP_CONTENT);
                mediaLayout.setLayoutParams(mediaLayoutParams);
                
                /**
                 * Temporary padding, later do center horizontal
                 */
                mediaLayout.setPadding((int)(ScreenEnvironment.height * 0.078125), 0, 0, 0);
                
                // add. checkboxLayout contains mediaLayout(check box) & TextView of check box
                checkboxLayout.addView(mediaLayout);
                checkboxLayout.addView(checkboxTextView);
                
                mediaLayout.setAVT(c, audioURI, imageURI, videoURI);
                choiceLayout.addView(checkboxLayout);
                
                // Last, add the dividing line between elements (except for the last element)
                ImageView divider = new ImageView(getContext());
                divider.setBackgroundResource(android.R.drawable.divider_horizontal_bright);
                if (i != mItems.size() - 1) {
                    choiceLayout.addView(divider);
                }

            }
        }
        mCheckboxInit = false;
    }


    public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager =
            (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }
}
