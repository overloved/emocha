package org.odk.collect.android.views;

import org.emocha.midot.R;
import org.emocha.utils.ScreenEnvironment;
import org.javarosa.core.model.Constants;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.utilities.QuestionData;
import org.odk.collect.android.widgets.IBinaryWidget;
import org.odk.collect.android.widgets.IQuestionWidget;
import org.odk.collect.android.widgets.WidgetFactory;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class QuestionView extends ScrollView {
	private final static String t = "QuestionView";

    private IQuestionWidget mQuestionWidget;
    private QuestionData data;
    private LinearLayout mView;
    private LinearLayout.LayoutParams mLayout;
    private FrameLayout frameLayout;
    private TextView slideTextView;
    private String mInstancePath;
    private final static int TEXTSIZE = 21;
    
    public final static int APPLICATION_FONTSIZE = 23;


    /** Class constructor
     * @param context Context of current state of activity.
     * @param instancePath The path of the current activity.
     */
    public QuestionView(Context context, String instancePath) {
        super(context);

        this.mInstancePath = instancePath;
        
    }


    /**
     * Create the appropriate view given your prompt. Exclude video record.
     * @param p The form title.
     * @param groups The group you are in as well as the questions. Questions are distributed by group names.
     */
    public void buildView(FormEntryPrompt p, FormEntryCaption[] groups) {
    	
    	RelativeLayout layout1 = new RelativeLayout(getContext());
    	LinearLayout.LayoutParams layoutParams1 = new LinearLayout.
    			LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
    	layout1.setLayoutParams(layoutParams1);
    	
        mView = new LinearLayout(getContext());
        mView.setOrientation(LinearLayout.VERTICAL);
        mView.setGravity(Gravity.TOP);

        setBackgroundColor(Color.parseColor("#0D5569"));
        
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.
        		LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        
        mView.setLayoutParams(layoutParams);
        

        mLayout =
            new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                    LinearLayout.LayoutParams.FILL_PARENT);

        // display which group you are in as well as the question
        
        AddGroupText(groups);
        AddQuestionText(p);
        AddHelpText(p);

        // if question or answer type is not supported, use text widget
        
       // mQuestionWidget = WidgetFactory.createWidgetFromPrompt(p, getContext(), mInstancePath);
        data = WidgetFactory.createWidgetFromPrompt(p, getContext(), mInstancePath);
        mQuestionWidget = data.getQuestionWidget();
        mView.addView((View) mQuestionWidget, mLayout);
        layout1.addView(mView);
        
        if (data.getIsSwipeRequired() == 1) {
        	SwipeView view = new SwipeView(getContext());
        	frameLayout = view.addSwipeBarAnimation(mView, (int)(ScreenEnvironment.height * 0.85));
			layout1.addView(frameLayout);
			slideTextView = view.addSwipeBarText(mView, frameLayout, (int)(ScreenEnvironment.height * 0.88));
			layout1.addView(slideTextView);
        }
        
        addView(layout1);
    }
    
    public IAnswerData getAnswer() {
        return mQuestionWidget.getAnswer();
    }


    public void setBinaryData(Object answer) {
        if (mQuestionWidget instanceof IBinaryWidget)
            ((IBinaryWidget) mQuestionWidget).setBinaryData(answer);
        else
            Log.e(t, "Attempted to setBinaryData() on a non-binary widget ");
    }


    public void clearAnswer() {
        mQuestionWidget.clearAnswer();
    }


    /**
     * Add a TextView containing the hierarchy of groups to which the question belongs.
     */
    private void AddGroupText(FormEntryCaption[] groups) {
        StringBuffer s = new StringBuffer("");
        String t = "";
        int i;

        // list all groups in one string
        for (FormEntryCaption g : groups) {
            i = g.getMultiplicity() + 1;
            t = g.getLongText();
            if (t != null) {
                s.append(t);
                if (g.repeats() && i > 0) {
                    s.append(" (" + i + ")");
                }
                s.append(" > ");
            }
        }

        // build view
        if (s.length() > 0) {
            TextView tv = new TextView(getContext());
            tv.setText(s.substring(0, s.length() - 3));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, TEXTSIZE - 7);
            tv.setPadding(0, 0, 0, 5);
            mView.addView(tv, mLayout);
        }
    }


    /**
     * Add a Views containing the question text, audio (if applicable), and image (if applicable).
     * To satisfy the RelativeLayout constraints, we add the audio first if it exists, then the
     * TextView to fit the rest of the space, then the image if applicable.
     */
    private void AddQuestionText(FormEntryPrompt p) {
        String imageURI = p.getImageText();
        String audioURI = p.getAudioText();
        String videoURI = null; // TODO: make this a value.

        // Add the text view. Textview always exists, regardless of whether there's text.
        TextView questionText = new TextView(getContext());
        questionText.setText(p.getLongText());
        questionText.setTextSize(TypedValue.COMPLEX_UNIT_PX, TEXTSIZE);
        questionText.setTypeface(null, Typeface.BOLD);
        questionText.setPadding(0, 0, 0, 7);
        questionText.setId(38475483); // assign random id

        // Wrap to the size of the parent view
        questionText.setHorizontallyScrolling(false);

        // Create the layout for audio, image, text
        IAVTLayout mediaLayout = new IAVTLayout(getContext());
        mediaLayout.setAVT(questionText, audioURI, imageURI, videoURI);

        mView.addView(mediaLayout, mLayout);
    }


    /**
     * Add a TextView containing the help text.
     */
    private void AddHelpText(FormEntryPrompt p) {
    	
        String s = p.getHelpText();

        if (s != null && !Constants.EMPTY_STRING.equals(s)) {
        	
        	if (p.getLongText() == null) { //eMOCHA: when hint comes alone, as a message
        		LinearLayout ll = (LinearLayout)inflate(getContext(), R.layout.single_hint, null);
            	TextView tv = (TextView)ll.findViewById(R.id.hint_text);
            	tv.setText(s);
            	ImageView iv = (ImageView)ll.findViewById(R.id.hint_icon);
            	ll.removeAllViews();
            	mView.addView(iv, mLayout);
            	mView.addView(tv, mLayout);
            	
        	} else { //eMOCHA modified: previous behaviour
        		TextView tv = new TextView(getContext());
        		tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, TEXTSIZE - 5);
                tv.setPadding(0, -5, 0, 7);
                // wrap to the widget of view
                tv.setHorizontallyScrolling(false);
                tv.setText(s);
                tv.setTypeface(null, Typeface.ITALIC);
                
                mView.addView(tv, mLayout);
        	}
        }
    }


    public void setFocus(Context context) {
        mQuestionWidget.setFocus(context);
    }
}
