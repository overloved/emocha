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

import org.emocha.Constants;
import org.emocha.EmochaApp;
import org.emocha.activities.VideoCaptureActivity;
import org.emocha.midot.R;
import org.emocha.security.Encryption;
import org.emocha.utils.MiDOTUtils;
import org.emocha.utils.ScreenEnvironment;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.views.SwipeView;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore.Video;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import android.graphics.Color;

/**
 * Video Widget that allows user to take pictures, sounds or video and add them to the form.
 * Count down animation starts before video animation.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 * @author Yao Huang (yao.engineering@gmail.com)
 */
public class VideoWidget extends RelativeLayout implements IQuestionWidget, IBinaryWidget {

    private final static String t = "MediaWidget";
    
    private Button mPlayButton;
    private ImageView emochaImageView;
    private TextView swipeTextView;
    private FrameLayout frameLayout;
    
    private String mBinaryName;

    private String mInstanceFolder;
    private int mRequestCode;
    
    /**/
    private Context mContext = null;
    /**/
    

    /** Class constructor
     * @param context Context of current state of activity.
     * @param instancePath The path of the current activity.
     */
    public VideoWidget(Context context, String instancePath) {
        super(context);
        mContext = context;
        initialize(instancePath);
    }


    /**
     * Initialize class
     * @param instancePath The path of the current activity.
     */
    private void initialize(String instancePath) {
        mInstanceFolder = instancePath.substring(0, instancePath.lastIndexOf("/") + 1);
        
        mRequestCode = FormEntryActivity.VIDEO_CAPTURE;
    }
    
    public Boolean isSwipeRequired() {
    	return false;
    }


    /**
     * Delete media after sync.
     */
    private void deleteMedia() {
        // get the file path and delete the file
        File f = new File(mInstanceFolder  + mBinaryName);
        if (!f.delete()) {
            Log.e(t, "Failed to delete " + f);
        }

        // clean up variables
        mBinaryName = null;
    }


    /* (non-Javadoc)
     * @see org.odk.collect.android.widgets.IQuestionWidget#clearAnswer()
     */
    public void clearAnswer() {
        // remove the file
        deleteMedia();
        // reset buttons
        mPlayButton.setEnabled(false);
    }


    public IAnswerData getAnswer() {
        if (mBinaryName != null) {
            return new StringData(mBinaryName.toString());
        } else {
            return null;
        }
    }

    public void buildView(FormEntryPrompt prompt) {
    	
        setBackgroundColor(Color.parseColor("#0D5569"));
        
        // setup capture button
        RelativeLayout.LayoutParams watchLayoutParams = new RelativeLayout.
	    		LayoutParams((int)(ScreenEnvironment.width * 0.767), (int)(ScreenEnvironment.height * 0.115625));
	    watchLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
	    watchLayoutParams.topMargin = (int)(ScreenEnvironment.height * 0.215);
    
	    emochaImageView = new ImageView(getContext());
	    emochaImageView.setBackgroundResource(R.drawable.app_emocha_logo);
	    RelativeLayout.LayoutParams emochaLogoParams = new RelativeLayout.
	    		LayoutParams((int)(ScreenEnvironment.width * 0.35), (int) (ScreenEnvironment.height * 0.043));
	    emochaLogoParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
	    emochaLogoParams.topMargin = (int)(ScreenEnvironment.height * 0.80);
	    emochaImageView.setLayoutParams(emochaLogoParams);
	    emochaImageView.setVisibility(View.INVISIBLE);
	    
        if (!MiDOTUtils.VIDEO_FLAG) { 
            	//showCountDown(); 
        	    MiDOTUtils.VIDEO_FLAG = true;
			    Intent i = new Intent(mContext, VideoCaptureActivity.class);
		    	i.putExtra(Constants.ODK_INSTANCEPATH_KEY, mInstanceFolder);
		    	((Activity) getContext()).startActivityForResult(i, mRequestCode);
		    	
        }

        // setup play button
        mPlayButton = new Button(getContext());
        mPlayButton.setBackgroundResource(R.drawable.app_watchvid_button);
        mPlayButton.setLayoutParams(watchLayoutParams);
        mPlayButton.setVisibility(View.INVISIBLE);
	
		SwipeView mView = new SwipeView(getContext());
		frameLayout = mView.addSwipeBarAnimation(this, (int)(ScreenEnvironment.height * 0.70));
		frameLayout.setVisibility(View.INVISIBLE);
		
		swipeTextView = mView.addSwipeBarText(this, frameLayout, (int)(ScreenEnvironment.height * 0.73));
		swipeTextView.setVisibility(View.INVISIBLE);
		
        // on play, launch the appropriate viewer
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	mPlayButton.setBackgroundResource(R.drawable.app_watchvid_button_onclick);
                Intent i = new Intent("android.intent.action.VIEW");
                
                //eMOCHA decrypt file prior to play it. will be deleted when coming back from the activity
                File f = null;
                try {
                	 f = Encryption.decryptFile(EmochaApp.getEncSecretKey(mContext), mInstanceFolder + "/" + mBinaryName );
                } catch (IOException e) {
                	e.printStackTrace();
                	throw new RuntimeException("IOException: while decrypting file..: "+ e.getMessage());  
                }
                
                i.setDataAndType(Uri.fromFile(f), "video/*");
                ((Activity) getContext()).startActivity(i);
            }
        });

        mBinaryName = prompt.getAnswerText();
        if (mBinaryName != null) {
            //eMOCHA remove any previously decrypted data!
        	File decrypted = new File(mInstanceFolder+mBinaryName+Encryption.DEC_EXTENSION);
        	if (decrypted.exists()) {
        		decrypted.delete();
        	}
        	mPlayButton.setVisibility(View.VISIBLE);
        	emochaImageView.setVisibility(View.VISIBLE);
        	frameLayout.setVisibility(View.VISIBLE);
        	swipeTextView.setVisibility(View.VISIBLE);
        	mPlayButton.setEnabled(true);
        } else {

        	emochaImageView.setVisibility(View.INVISIBLE);
        	mPlayButton.setVisibility(View.INVISIBLE);
        	frameLayout.setVisibility(View.INVISIBLE);
        	swipeTextView.setVisibility(View.INVISIBLE);
            mPlayButton.setEnabled(false);
        }

        // finish complex layout
        //addView(frameLayout);
        addView(mPlayButton);
        addView(emochaImageView);
        addView(frameLayout);
        addView(swipeTextView);
    }
    
    //emocha: modified to retrieve video path
    public void setBinaryData(Object videoPath) {
    	
    	File video = new File((String)videoPath);
        // you are replacing an answer. remove the media.
        if (mBinaryName != null) {
            deleteMedia();
        }
        
    	if (video.exists()) {
    		ContentValues values = new ContentValues(6);
            values.put(Video.Media.TITLE, video.getName());
            values.put(Video.Media.DISPLAY_NAME, video.getName());
            values.put(Video.Media.DATE_ADDED, System.currentTimeMillis());
            values.put(Video.Media.DATA, video.getAbsolutePath());
            Uri videoURI = getContext().getContentResolver().insert(Video.Media.EXTERNAL_CONTENT_URI, values);
            Log.i(t, "Inserting VIDEO returned uri = " + videoURI.toString());
    		
    	}
    	mBinaryName = video.getName();
    }


    /* (non-Javadoc)
     * @see org.odk.collect.android.widgets.IQuestionWidget#setFocus(android.content.Context)
     */
    public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager =
            (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }

    
        
}
