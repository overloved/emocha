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

import org.emocha.midot.R;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.views.QuestionView;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;

/**
 * Widget that allows user to take pictures, sounds or video and add them to the form.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class ImageWidget extends LinearLayout implements IQuestionWidget, IBinaryWidget {

    private final static String t = "MediaWidget";

    private Button mCaptureButton;
    private ImageView mImageView;

    private String mBinaryName;
    private TextView mDisplayText;

    private Uri mExternalUri;
    private String mCaptureIntent;
    private String mInstanceFolder;
    private int mRequestCode;
    private int mCaptureText;
    private int mReplaceText;
    //eMOCHA
    private String mTmpPath;
    //
    public ImageWidget(Context context, String instancePath) {
        super(context);
        //eMOCHA
        mTmpPath = Environment.getExternalStorageDirectory()
				 + context.getString(R.string.app_path_base)
				 + context.getString(R.string.app_odk_path)
				 + context.getString(R.string.app_odk_cache_path)
				 + "tmp.jpg";
        //
        
        initialize(instancePath);
    }


    private void initialize(String instancePath) {
        mInstanceFolder = instancePath.substring(0, instancePath.lastIndexOf("/") + 1);
        mExternalUri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        mCaptureIntent = android.provider.MediaStore.ACTION_IMAGE_CAPTURE;
        mRequestCode = FormEntryActivity.IMAGE_CAPTURE;
        mCaptureText = R.string.capture_image;
        mReplaceText = R.string.replace_image;
    }


    private void deleteMedia() {
        // get the file path and delete the file

        // There's only 1 in this case, but android 1.6 doesn't implement delete on
        // android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI only on
        // android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI + a #
        String[] projection = {
            Images.ImageColumns._ID
        };
        Cursor c =
            getContext().getContentResolver().query(mExternalUri, projection,
                "_data='" + mInstanceFolder + mBinaryName + "'", null, null);
        int del = 0;
        if (c.getCount() > 0) {
            c.moveToFirst();
            String id = c.getString(c.getColumnIndex(Images.ImageColumns._ID));

            Log.i(t, "attempting to delete: " + Uri.withAppendedPath(mExternalUri, id));
            del =
                getContext().getContentResolver().delete(Uri.withAppendedPath(mExternalUri, id),
                    null, null);
        }
        c.close();

        // clean up variables
        mBinaryName = null;
        Log.i(t, "Deleted " + del + " rows from media content provider");
    }


    public void clearAnswer() {
        // remove the file
        deleteMedia();
        mImageView.setImageBitmap(null);

        // reset buttons
        mCaptureButton.setText(getContext().getString(mCaptureText));
        mDisplayText.setText(getContext().getString(R.string.no_capture));
    }


    public IAnswerData getAnswer() {
        if (mBinaryName != null) {
            return new StringData(mBinaryName.toString());
        } else {
            return null;
        }
    }


    public void buildView(FormEntryPrompt prompt) {
        setOrientation(LinearLayout.VERTICAL);

        // setup capture button
        mCaptureButton = new Button(getContext());
        mCaptureButton.setText(getContext().getString(mCaptureText));
        mCaptureButton
                //.setTextSize(TypedValue.COMPLEX_UNIT_PX, QuestionView.APPLICATION_FONTSIZE);
        		.setTextSize(TypedValue.COMPLEX_UNIT_SP, QuestionView.APPLICATION_FONTSIZE);
        mCaptureButton.setPadding(20, 20, 20, 20);
        mCaptureButton.setEnabled(!prompt.isReadOnly());

        // launch capture intent on click
        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(mCaptureIntent);
                // We give the camera an absolute filename/path where to put the
                // picture because of bug:
                // http://code.google.com/p/android/issues/detail?id=1480
                // The bug appears to be fixed in Android 2.0+, but as of feb 2,
                // 2010, G1 phones only run 1.6. Without specifying the path the
                // images returned by the camera in 1.6 (and earlier) are ~1/4
                // the size. boo.

                // if this gets modified, the onActivityResult in
                // FormEntyActivity will also need to be updated.
                //i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(
                //        FileUtils.TMPFILE_PATH)));
                i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mTmpPath)));
                ((Activity) getContext()).startActivityForResult(i, mRequestCode);

            }
        });

        // finish complex layout
        addView(mCaptureButton);
        
        // retrieve answer from data model and update ui
        mDisplayText = new TextView(getContext());
        mDisplayText.setPadding(5, 0, 0, 0);

        mBinaryName = prompt.getAnswerText();
        
        if (mBinaryName != null) { //eMOCHA: create view only when mBinary is NOT null!!; addView(mCaptureButton) has also been moved up.
        	
        	mCaptureButton.setText(getContext().getString(mReplaceText));
	        mImageView = new ImageView(getContext());
	
	        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
	        int screenWidth = display.getWidth();
	        int screenHeight = display.getHeight();

	        File f = new File(mInstanceFolder + File.separator + mBinaryName);
	        //TODO it fails once in a while. tmp file doesn't get renamed properly in FormEntryActivity, 
	        //and binaryName points to an empty file..
	        Bitmap bmp = FileUtils.getBitmapScaledToDisplay(f, screenHeight, screenWidth);
	        mImageView.setImageBitmap(bmp);
	        mImageView.setPadding(10, 10, 10, 10);
	        mImageView.setAdjustViewBounds(true);
	        mImageView.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
	                Intent i = new Intent("android.intent.action.VIEW");
	                String[] projection = {
	                    "_id"
	                };
	                Cursor c =
	                    getContext().getContentResolver().query(mExternalUri, projection,
	                        "_data='" + mInstanceFolder + mBinaryName + "'", null, null);
	                if (c.getCount() > 0) {
	                    c.moveToFirst();
	                    String id = c.getString(c.getColumnIndex("_id"));
	
	                    Log.i(t, "setting view path to: " + Uri.withAppendedPath(mExternalUri, id));
	
	                    i.setDataAndType(Uri.withAppendedPath(mExternalUri, id), "image/*");
	                    getContext().startActivity(i);
	
	                }
	                c.close();
	            }
	        });
	        addView(mImageView);
        }
    }


    private String getPathFromUri(Uri uri) {
        // find entry in content provider
        Cursor c = getContext().getContentResolver().query(uri, null, null, null, null);
        c.moveToFirst();

        // get data path
        String colString = c.getString(c.getColumnIndex("_data"));
        c.close();
        return colString;
    }


    public void setBinaryData(Object binaryuri) {
        // you are replacing an answer. delete the previous image using the
        // content provider.
        if (mBinaryName != null) {
            deleteMedia();
        }
        String binarypath = getPathFromUri((Uri) binaryuri);
        File f = new File(binarypath);
        mBinaryName = f.getName();
        Log.i(t, "Setting current answer to " + f.getName());
    }


    public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager =
            (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }

}
