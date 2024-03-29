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

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.views.IAVTLayout;
import org.odk.collect.android.views.QuestionView;

import android.content.Context;
import android.util.TypedValue;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.CompoundButton.OnCheckedChangeListener;

import java.util.Vector;

/**
 * SelectOneWidgets handles select-one fields using radio buttons.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class SelectOneWidget extends RadioGroup implements IQuestionWidget, OnCheckedChangeListener {
    private final int RANDOM_BUTTON_ID = 4853487;
    Vector<SelectChoice> mItems;

    Vector<RadioButton> buttons;


    /**
     * Class constructor.
     * @param context
     */
    public SelectOneWidget(Context context) {
        super(context);
    }


    public void clearAnswer() {
        clearCheck();
    }


    public IAnswerData getAnswer() {
        int i = getCheckedId();
        if (i == -1) {
            return null;
        } else {
            String s = mItems.elementAt(i - RANDOM_BUTTON_ID).getValue();
            return new SelectOneData(new Selection(s));
        }
    }


    public void buildView(final FormEntryPrompt prompt) {
    	mItems = prompt.getSelectChoices();
        buttons = new Vector<RadioButton>();

        String s = null;
        if (prompt.getAnswerValue() != null) {
            s = ((Selection) prompt.getAnswerValue().getValue()).getValue();
        }

        if (prompt.getSelectChoices() != null) {
            for (int i = 0; i < mItems.size(); i++) {
                RadioButton r = new RadioButton(getContext());
                r.setOnCheckedChangeListener(this);
                r.setText(prompt.getSelectChoiceText(mItems.get(i)));
                r.setTextSize(TypedValue.COMPLEX_UNIT_PX, QuestionView.APPLICATION_FONTSIZE);
                r.setId(i + RANDOM_BUTTON_ID);
                r.setEnabled(!prompt.isReadOnly());
                r.setFocusable(!prompt.isReadOnly());
                buttons.add(r);

                if (mItems.get(i).getValue().equals(s)) {
                    r.setChecked(true);
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

                IAVTLayout mediaLayout = new IAVTLayout(getContext());
                mediaLayout.setAVT(r, audioURI, imageURI, videoURI);
                addView(mediaLayout);

                // Last, add the dividing line (except for the last element)
                ImageView divider = new ImageView(getContext());
                divider.setBackgroundResource(android.R.drawable.divider_horizontal_bright);
                if (i != mItems.size() - 1) {
                    mediaLayout.addDivider(divider);
                }
            }
        }
    }


    public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager =
            (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }


    public int getCheckedId() {
        for (RadioButton button : this.buttons) {
            if (button.isChecked()) {
                return button.getId();
            }
        }
        return -1;
    }


    /* (non-Javadoc)
     * @see android.widget.CompoundButton.OnCheckedChangeListener#onCheckedChanged(android.widget.CompoundButton, boolean)
     */
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!isChecked) {
            // If it got unchecked, we don't care.
            return;
        }

        for (RadioButton button : this.buttons) {
            if (button.isChecked() && !(buttonView == button)) {
                button.setChecked(false);
            }
        }
    }
}
