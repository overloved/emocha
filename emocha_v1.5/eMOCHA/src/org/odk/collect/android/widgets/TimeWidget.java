package org.odk.collect.android.widgets;

import java.util.Calendar;
import java.util.Date;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.TimeData;
import org.javarosa.form.api.FormEntryPrompt;

import android.content.Context;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TimePicker;

public class TimeWidget extends LinearLayout implements IQuestionWidget {

	private TimePicker mTimePicker;
	private TimePicker.OnTimeChangedListener mTimeListener;

	public TimeWidget (Context context) {
		super(context);
	}
	
	/**
     * Resets date to current time.
     */
	@Override
	public void clearAnswer() {
		final Calendar c = Calendar.getInstance();
		mTimePicker.setCurrentHour(c.get(Calendar.HOUR_OF_DAY));
		mTimePicker.setCurrentMinute(c.get(Calendar.MINUTE));
	}
	
	@Override
	public IAnswerData getAnswer() {
		// clear focus first so the timewidget gets the value in the text box
		mTimePicker.clearFocus();
        Date d = new Date(0);
        d.setHours(mTimePicker.getCurrentHour());
        d.setMinutes(mTimePicker.getCurrentMinute());
        return new TimeData(d);
	}

	@Override
	public void buildView(FormEntryPrompt p) {
		
		final FormEntryPrompt prompt = p;

		mTimePicker = new TimePicker(getContext());
        mTimePicker.setFocusable(!prompt.isReadOnly());
        mTimePicker.setEnabled(!prompt.isReadOnly());
        mTimePicker.setIs24HourView(true);
        mTimeListener = new TimePicker.OnTimeChangedListener() {
             @Override
             public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                if (!prompt.isReadOnly()) {
                        view.setCurrentHour(hourOfDay);
                        view.setCurrentMinute(minute);
                }
            }
        };
        
        mTimePicker.setOnTimeChangedListener(mTimeListener);
        
        if (prompt.getAnswerValue() != null) {
            Date d = (Date) prompt.getAnswerValue().getValue();
            mTimePicker.setCurrentHour(d.getHours());
            mTimePicker.setCurrentMinute(d.getMinutes());
        } else {
            // create date widget with now
            clearAnswer();
        }

        setGravity(Gravity.LEFT);
        addView(mTimePicker);
	}

	@Override
	public void setFocus(Context context) {
		// Hide the soft keyboard if it's showing.
        InputMethodManager inputManager =
            (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
	}
}
