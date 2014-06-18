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

package org.odk.collect.android.activities;

import org.emocha.Constants;
import org.emocha.EmochaApp;
import org.emocha.midot.R;
import org.emocha.model.DBAdapter;
import org.emocha.model.Preferences;
import org.emocha.model.entities.FormData;
import org.emocha.model.entities.FormDataFile;
import org.emocha.model.entities.FormTemplate;
import org.emocha.model.entities.Patient;
import org.emocha.model.entities.PatientData;
import org.emocha.security.Encryption;
import org.emocha.utils.CommonUtils;
import org.emocha.utils.Date;
import org.emocha.utils.MiDOTUtils;
import org.emocha.utils.ScreenEnvironment;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.model.xform.XFormsModule;
import org.odk.collect.android.listeners.FormLoaderListener;
import org.odk.collect.android.listeners.FormSavedListener;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.tasks.FormLoaderTask;
import org.odk.collect.android.tasks.SaveToXmlStringTask;
import org.odk.collect.android.utilities.EmochaResult;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.GestureDetector;
import org.odk.collect.android.views.QuestionView;
import org.odk.collect.android.widgets.WidgetFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.AvoidXfermode;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * FormEntryActivity is responsible for displaying questions, animating transitions between
 * questions, and allowing the user to enter data.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */

/**
 * eMOCHA: changes were made to not save data into the sdcard. Generated
 * XML is sent back to eMOCHA. XML is also accepted inside INSTANCEPATH,
 * which would normally contain a path to an instance in the sd card.
 * This breaks loadingComplete(), because instancePath, where images
 * would be saved, contains no path, so media saving by ODK is broken
 * until we find a better solution. 
 */
public class FormEntryActivity extends Activity implements AnimationListener, FormLoaderListener,
        FormSavedListener {
    private static final String t = "FormEntryActivity";

    // Request codes for returning data from specified intent.
    /**
     * Request code for returning data for image capture.
     */
    public static final int IMAGE_CAPTURE = 1;
    /**
     * Request code for returning data for barcode capture.
     */
    public static final int BARCODE_CAPTURE = 2;
    /**
     * Request code for returning data for audio capture.
     */
    public static final int AUDIO_CAPTURE = 3;
    /**
     * Request code for returning data for video capture.
     */
    public static final int VIDEO_CAPTURE = 4;
    /**
     * Request code for returning data for location capture.
     */
    public static final int LOCATION_CAPTURE = 5;

    /**
     * Define the constant input string of location result.
     */
    public static final String LOCATION_RESULT = "LOCATION_RESULT";
    
    //eMOCHA
    private String mInstanceXML;
    private Bundle mExtraData; // here goes any extra data we need back. each Activity calling FormEntryActivity must know which are the provided parameters.
    
    private boolean saveData = false; // do we need to persist data here?
    private boolean newForm = true;
    // end eMOCHA
    
    /* temporaly remove
    private static final int MENU_CLEAR = Menu.FIRST;
    private static final int MENU_DELETE_REPEAT = Menu.FIRST + 1;
    private static final int MENU_LANGUAGES = Menu.FIRST + 2;
    private static final int MENU_HIERARCHY_VIEW = Menu.FIRST + 3;

    private static final int MENU_SAVE = Menu.FIRST + 4;
    */
    private static final int PROGRESS_DIALOG = 1;
    private static final int SAVING_DIALOG = 2;

    private String mFormPath;
    private String mInstancePath;
    private GestureDetector mGestureDetector;

    public static FormEntryController mFormEntryController;
    public FormEntryModel mFormEntryModel;

    private Animation mInAnimation;
    private Animation mOutAnimation;
    
    private Button submitButton;
//    private Button watchVideoButton;

    private RelativeLayout mRelativeLayout;
    private View mCurrentView;
    private View startView;
    private View endView;

    private AlertDialog mAlertDialog;
    private ProgressDialog mProgressDialog;

    // used to limit forward/backward swipes to one per question
    private boolean mBeenSwiped;
    
    private FormLoaderTask mFormLoaderTask;
    // eMOCHA modified : rename and change data type
    private SaveToXmlStringTask mSaveToXmlStringTask;
    //end eMOCHA

    enum AnimationType {
        LEFT, RIGHT, FADE, NORMAL
    }


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_entry);
        setTitle(getString(R.string.odk_app_name) + " > " + getString(R.string.loading_form));

        mRelativeLayout = (RelativeLayout) findViewById(R.id.rl);

        mBeenSwiped = false;
        mAlertDialog = null;
        mCurrentView = null;
        mInAnimation = null;
        mOutAnimation = null;
        mGestureDetector = new GestureDetector();

        // Load JavaRosa modules. needed to restore forms.
        new XFormsModule().registerModule();

        // needed to override rms property manager
        org.javarosa.core.services.PropertyManager.setPropertyManager(new PropertyManager(
                getApplicationContext()));
        
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(Constants.ODK_FORMPATH_KEY)) {
                mFormPath = savedInstanceState.getString(Constants.ODK_FORMPATH_KEY);
            }
            if (savedInstanceState.containsKey(Constants.ODK_INSTANCEPATH_KEY)) {
                mInstancePath = savedInstanceState.getString(Constants.ODK_INSTANCEPATH_KEY);
            }
            if (savedInstanceState.containsKey(Constants.ODK_NEW_FORM_KEY)) {
                newForm = savedInstanceState.getBoolean(Constants.ODK_NEW_FORM_KEY, true);
            }
            // eMOCHA modified:
            if (savedInstanceState.containsKey(Constants.ODK_INSTANCEXML_KEY)) {
                mInstanceXML = savedInstanceState.getString(Constants.ODK_INSTANCEXML_KEY);
            }
            if (savedInstanceState.containsKey(Constants.ODK_EXTRA_DATA_KEY)) {
            	mExtraData = savedInstanceState.getBundle(Constants.ODK_EXTRA_DATA_KEY);
            	//saveData = Constants.ONE_STRING.equals(mExtraData.getString(Constants.ODK_PERSIST_DATA)) ? true : false;
            	saveData = mExtraData.getBoolean(Constants.ODK_PERSIST_DATA);
            }
            // end eMOCHA
        }

        // Check to see if this is a screen flip or a new form load.
        Object data = getLastNonConfigurationInstance();
        if (data instanceof FormLoaderTask) {
            mFormLoaderTask = (FormLoaderTask) data;
        } else if (data instanceof SaveToXmlStringTask) {
            mSaveToXmlStringTask = (SaveToXmlStringTask) data;
        } else if (data == null) {
            if (!newForm) {
                mFormEntryModel = mFormEntryController.getModel();
                refreshCurrentView();
                return;
            }
            
            // Not a restart from a screen orientation change (or other).
            mFormEntryController = null;

            Intent intent = getIntent();
            if (intent != null) {
                mFormPath = intent.getStringExtra(Constants.ODK_FORMPATH_KEY);
                mInstancePath = intent.getStringExtra(Constants.ODK_INSTANCEPATH_KEY);
                
                // eMOCHA modified
                mInstanceXML = intent.getStringExtra(Constants.ODK_INSTANCEXML_KEY); 
                mExtraData = intent.getBundleExtra(Constants.ODK_EXTRA_DATA_KEY);
                mFormLoaderTask = new FormLoaderTask(getApplicationContext());

                //1.- convert extraData into xml string
                if (mExtraData != null) {
                	String externalItemset = buildItemsets(mExtraData);
                	//mFormLoaderTask.execute(mFormPath, mInstancePath, mInstanceXML, externalItemset);
                	mFormLoaderTask.execute(mFormPath, mInstanceXML, externalItemset);
                	//saveData = Constants.ONE_STRING.equals(mExtraData.getString(Constants.ODK_PERSIST_DATA)) ? true : false;
                	saveData = mExtraData.getBoolean(Constants.ODK_PERSIST_DATA);
                } else {
                	//mFormLoaderTask.execute(mFormPath, mInstancePath, mInstanceXML, null);
                	mFormLoaderTask.execute(mFormPath, mInstanceXML, null);
                }
                // end eMOCHA
                showDialog(PROGRESS_DIALOG);
            } 
        }
    }

    // receives a Bundle containing another Bundle: 
    //										{"dynamic_data",[{"node_name1",["label","value"]..},
    //											   		     {"node_name2",["label","value"]..},...
    //												  		]
    //								 		}
    // and return an xml string with an itemset to be used in a select1. e.g: 
    //	<dynamic_data>
    //		<closing_study_id_A>
    //    		<item>
    //		    	<label>Test  M1</label>
    //		    	<value>P-4-20100726224406</value>
    //	    	</item>
    //		</closing_study_id_A>
    //	</dynamic_data>    
    
    private String buildItemsets(Bundle extra) {
    	StringBuffer result = null;
    	
    	if (!getIntent().getBundleExtra(Constants.ODK_EXTRA_DATA_KEY).isEmpty()) {
    		Bundle b = getIntent().getBundleExtra(Constants.ODK_EXTRA_DATA_KEY);
    		if (b.containsKey(Constants.ODK_DYNAMIC_DATA)) {
   				Bundle selects = b.getBundle(Constants.ODK_DYNAMIC_DATA);
   				if (!selects.isEmpty()) {
   					result = new StringBuffer();
   					result.append("<dynamic_data>");
	   				//get node names
	   				Set<String> nodeNames = (Set<String>)selects.keySet();	   				 
	   				for (String nodeName : nodeNames) {
	   					result.append("<"+nodeName+">");
	   					//get values & labels
	   					Bundle values = (Bundle) selects.get(nodeName);
	   					Set<String> nodeValues = (Set<String>) values.keySet();
	   					for (String label : nodeValues) {
	   						result.append("<item><label>"+label+"</label><value>"+values.get(label)+"</value></item>");
	   					}
	   					result.append("</"+nodeName+">");
	   				}
   				}
   				result.append("</dynamic_data>");
    		}
    	}
    	return (result == null) ? null: result.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.ODK_FORMPATH_KEY, mFormPath);
        outState.putString(Constants.ODK_INSTANCEPATH_KEY, mInstancePath);
        outState.putBoolean(Constants.ODK_NEW_FORM_KEY, false);
        
        // eMOCHA modified:
        newForm = false;
        outState.putString(Constants.ODK_INSTANCEXML_KEY, mInstanceXML);
        outState.putBundle(Constants.ODK_EXTRA_DATA_KEY, mExtraData);
        //end eMOCHA
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_CANCELED) {
            // request was canceled, so do nothing
            return;
        }

        switch (requestCode) {
            case BARCODE_CAPTURE:
                String sb = intent.getStringExtra("SCAN_RESULT");
                ((QuestionView) mCurrentView).setBinaryData(sb);
                saveCurrentAnswer(false);
                break;
            case IMAGE_CAPTURE:
                // image was saved in a tmp path, and it needs to be moved before inserting it into the content provider.
            	// eMOCHA
            	File fi = new File(Environment.getExternalStorageDirectory()
            			+ getString(R.string.app_path_base)
            			+ getString(R.string.app_odk_path)
            			+ getString(R.string.app_odk_cache_path)
            			+ "tmp.jpg");
            	//
                String s = mInstancePath  + System.currentTimeMillis() + ".jpg";
                File nf = new File(s);
                
                if (!fi.renameTo(nf)) {
                    Log.e(t, "Failed to rename " + fi.getAbsolutePath());
                    //eMOCHA: do a manual copy; renameTo seems to fail quite often
                    copyFile(fi,nf);
                } else {
                    Log.i(t, "renamed " + fi.getAbsolutePath() + " to " + nf.getAbsolutePath());
                }

                // Add the new image to the Media content provider so that the
                // viewing is fast in Android 2.0+
                ContentValues values = new ContentValues(6);
                values.put(Images.Media.TITLE, nf.getName());
                values.put(Images.Media.DISPLAY_NAME, nf.getName());
                values.put(Images.Media.DATE_TAKEN, System.currentTimeMillis());
                values.put(Images.Media.MIME_TYPE, "image/jpeg");
                values.put(Images.Media.DATA, nf.getAbsolutePath());

                Uri imageuri =
                    getContentResolver().insert(Images.Media.EXTERNAL_CONTENT_URI, values);
                Log.i(t, "Inserting image returned uri = " + imageuri.toString());

                ((QuestionView) mCurrentView).setBinaryData(imageuri);
                saveCurrentAnswer(false);
                refreshCurrentView();
                // end eMOCHA
                break;
            case AUDIO_CAPTURE:
            	//emocha: original odk's behaviour
            	Uri um = intent.getData();
                ((QuestionView) mCurrentView).setBinaryData(um);
                
                saveCurrentAnswer(false);
                refreshCurrentView();
                break;
                //
            case VIDEO_CAPTURE:
            	//emocha modified: retrieve video path and give it to VideoWidget...
            	String videoPath = intent.getStringExtra(Constants.VIDEOS_STRING);
            	
            	//TODO add check (if encryption_required)
            	try {
            		Encryption.encryptFile(EmochaApp.getEncSecretKey(getApplicationContext()), videoPath);
            	} catch (IOException e) {
            		e.printStackTrace();
            		throw new RuntimeException("IOException: while encrypting file..: "+ e.getMessage()); //TODO: do something?????
            	}
            	
            	((QuestionView) mCurrentView).setBinaryData(videoPath);
            	
            	saveCurrentAnswer(false);
                refreshCurrentView();
                break;
            case LOCATION_CAPTURE:
                String sl = intent.getStringExtra(LOCATION_RESULT);
                ((QuestionView) mCurrentView).setBinaryData(sl);
                saveCurrentAnswer(false);
                break;
        }
    }

    private void copyFile(File orig, File dest) {
        try {
  	      
      	  if (!dest.exists()){
      		  dest.getParentFile().mkdirs(); //instancePath should never be null
      		  dest.createNewFile();
      	  }
      	  
      	  FileInputStream in = new FileInputStream(orig);
      	  FileOutputStream out = new FileOutputStream(dest);
         
      	  byte[] buffer = new byte[1024];
      	  int read;
      	  while ((read = in.read(buffer)) != -1) {
      		  out.write(buffer, 0, read);
      	  }
      	  in.close();
      	  out.flush();
      	  out.close();
        } catch (FileNotFoundException e) {
      	  Log.e(Constants.LOG_TAG, "FileNotFound error copying files: "+e.getMessage());
        } catch (IOException e) {
      	  Log.e(Constants.LOG_TAG, "I/O error copying files: "+e.getMessage());
        }
      }
    
    /**
     * Refreshes the current view. the controller and the displayed view can get out of sync due to
     * dialogs and restarts caused by screen orientation changes, so they're resynchronized here.
     */
    public void refreshCurrentView() {
        int event = mFormEntryModel.getEvent();

        // When we refresh, if we're at a repeat prompt then step back to the
        // last question.
        while (event == FormEntryController.EVENT_PROMPT_NEW_REPEAT
                || event == FormEntryController.EVENT_GROUP
                || event == FormEntryController.EVENT_REPEAT) {
            event = mFormEntryController.stepToPreviousEvent();
        }
        Log.e(t, "refreshing view for event: " + event);

        View current = createView(event);
        showView(current, AnimationType.FADE);
    }

/* emocha: temporaly remove
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.removeItem(MENU_CLEAR);
        menu.removeItem(MENU_DELETE_REPEAT);
        menu.removeItem(MENU_LANGUAGES);
        menu.removeItem(MENU_HIERARCHY_VIEW);
        // menu.removeItem(MENU_SUBMENU);
        menu.removeItem(MENU_SAVE);


        menu.add(0, MENU_SAVE, 0, R.string.save_all_answers).setIcon(
            android.R.drawable.ic_menu_save);
        menu.add(0, MENU_CLEAR, 0, getString(R.string.clear_answer)).setIcon(
            android.R.drawable.ic_menu_close_clear_cancel).setEnabled(
            !mFormEntryModel.isIndexReadonly() ? true : false);
        menu.add(0, MENU_DELETE_REPEAT, 0, getString(R.string.delete_repeat)).setIcon(
            R.drawable.ic_menu_clear_playlist).setEnabled(
            indexContainsRepeatableGroup(mFormEntryModel.getFormIndex()) ? true : false);
        menu.add(0, MENU_HIERARCHY_VIEW, 0, getString(R.string.view_hierarchy)).setIcon(
            R.drawable.ic_menu_goto);
        menu.add(0, MENU_LANGUAGES, 0, getString(R.string.change_language)).setIcon(
            R.drawable.ic_menu_start_conversation).setEnabled(
            (mFormEntryModel.getLanguages() == null || mFormEntryController.getModel()
                    .getLanguages().length == 1) ? false : true);
        return true;
    }
*/

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
/* emocha: temporaly remove
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_LANGUAGES:
                createLanguageDialog();
                return true;
            case MENU_CLEAR:
                createClearDialog();
                return true;
            case MENU_DELETE_REPEAT:
                createDeleteRepeatConfirmDialog();
                return true;
            case MENU_SAVE:
            	// eMOCHA modified
            	mInstanceXML = getDataAsXmlString(false);
            	if (saveData) {
            		persistXmlData();
            	}
            	// end eMOCHA
                return true;
            case MENU_HIERARCHY_VIEW:
                if (currentPromptIsQuestion()) {
                    saveCurrentAnswer(false);
                }
                Intent i = new Intent(this, FormHierarchyActivity.class);
                
                //eMOCHA: provide parameters to FHA
                i.putExtra(Constants.ODK_FORMPATH_KEY, mFormPath);
				i.putExtra(Constants.ODK_INSTANCEPATH_KEY, mInstancePath);
				i.putExtra(Constants.ODK_INSTANCEXML_KEY, mInstanceXML);
				i.putExtra(Constants.ODK_EXTRA_DATA_KEY, mExtraData);
				i.putExtra(Constants.ODK_PERSIST_DATA, saveData);
				i.putExtra(Constants.ODK_NEW_FORM_KEY, newForm);
				// we need to push the data through the activities stack
				//i.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                //end eMOCHA
                
                startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }
*/

    /**
     * @return true if the current View represents a question in the form
     */
    private boolean currentPromptIsQuestion() {
        return (mFormEntryModel.getEvent() == FormEntryController.EVENT_QUESTION);
    }


    /**
     * Attempt to save the answer to the current prompt into the data model.
     * 
     * @param evaluateConstraints
     * @return true on success, false otherwise
     */
    private boolean saveCurrentAnswer(boolean evaluateConstraints) {
        if (!mFormEntryModel.isIndexReadonly()
                && mFormEntryModel.getEvent() == FormEntryController.EVENT_QUESTION) {
            int saveStatus =
                saveAnswer(((QuestionView) mCurrentView).getAnswer(), evaluateConstraints);
            if (evaluateConstraints && saveStatus != FormEntryController.ANSWER_OK) {
                createConstraintToast(mFormEntryModel.getQuestionPrompt().getConstraintText(),
                    saveStatus);
                return false;
            }
        }
        return true;
    }

    /**
     * Clears the answer on the screen.
     */
    private void clearCurrentAnswer() {
        if (!mFormEntryModel.isIndexReadonly())
            ((QuestionView) mCurrentView).clearAnswer();
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onRetainNonConfigurationInstance() If we're loading, then we pass
     * the loading thread to our next instance.
     */
    @Override
    public Object onRetainNonConfigurationInstance() {
        // if a form is loading, pass the loader task
        if (mFormLoaderTask != null && mFormLoaderTask.getStatus() != AsyncTask.Status.FINISHED)
            return mFormLoaderTask;

        // if a form is writing to disk, pass the save to disk task
        if (mSaveToXmlStringTask != null && mSaveToXmlStringTask.getStatus() != AsyncTask.Status.FINISHED)
            return mSaveToXmlStringTask;

        // mFormEntryController is static so we don't need to pass it.
        if (mFormEntryController != null && currentPromptIsQuestion()) {
            saveCurrentAnswer(false);
        }
        return null;
    }


    /**
     * Creates a view given the View type and a prompt
     * 
     * @param prompt
     * @return newly created View
     */
    private View createView(int event) {
    	
        setTitle(getString(R.string.odk_app_name) + " > " + mFormEntryModel.getFormTitle());

        switch (event) {
            case FormEntryController.EVENT_BEGINNING_OF_FORM:           	
                startView = View.inflate(this, R.layout.form_entry_start, null);                
                setTitle(getString(R.string.odk_app_name) + " > " + mFormEntryModel.getFormTitle());               
                initFormEntryStartView();
                

//                //eMOCHA modified : replace start hint message when it exists
//                if (!getIntent().getBundleExtra(Constants.ODK_EXTRA_DATA_KEY).isEmpty()) {
//            		Bundle b = getIntent().getBundleExtra(Constants.ODK_EXTRA_DATA_KEY);
//            		if (b.containsKey(Constants.ODK_START_HINT_MSG)) {
//           				Bundle bMsg = b.getBundle(Constants.ODK_START_HINT_MSG);
//           				TextView tv = ((TextView) startView.findViewById(R.id.form_entry_start_hint));
//           				tv.setText(Html.fromHtml(bMsg.getString(Constants.ODK_START_HINT_MSG)));
//           				tv.setTextSize(20);
//            		}
//                }
                //end EMOCHA
                return startView;
            case FormEntryController.EVENT_END_OF_FORM:
                endView = View.inflate(this, R.layout.form_entry_end, null);
                initFormEntryEndView();
                
                return endView;
            case FormEntryController.EVENT_QUESTION:           	 
            	QuestionView qv = new QuestionView(this, mInstancePath);
            	qv.buildView(mFormEntryModel.getQuestionPrompt(), getGroupsForCurrentIndex());
            	
                return qv;
            default:
                Log.e(t, "Attempted to create a view that does not exist.");
                return null;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent mv) {
        boolean handled = onTouchEvent(mv);
        if (!handled) {
            return super.dispatchTouchEvent(mv);
        }
        return handled; // this is always true
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        /*
         * constrain the user to only be able to swipe (that causes a view transition) once per
         * screen with the mBeenSwiped variable.
         */
        boolean handled = false;
        if (!mBeenSwiped) {
            switch (mGestureDetector.getGesture(motionEvent)) {
                case SWIPE_RIGHT:
                    mBeenSwiped = true;
                    showPreviousView();
                    handled = true;
                    break;
                case SWIPE_LEFT:
                    mBeenSwiped = true;
                    showNextView();
                    handled = true;
                    break;
            }
        }
        return handled;
    }


    /**
     * Determines what should be displayed on the screen. Possible options are: a question, an ask
     * repeat dialog, or the submit screen. Also saves answers to the data model after checking
     * constraints.
     */
    private void showNextView() {
        if (currentPromptIsQuestion()) {
            if (!saveCurrentAnswer(true)) {
                // A constraint was violated so a dialog should be showing.
                return;
            }
        }

        if (mFormEntryModel.getEvent() != FormEntryController.EVENT_END_OF_FORM) {
            int event = getNextNotGroupEvent();

            switch (event) {
                case FormEntryController.EVENT_QUESTION:
                	
                case FormEntryController.EVENT_END_OF_FORM:
                    View next = createView(event);
                    showView(next, AnimationType.RIGHT);
                    break;
                case FormEntryController.EVENT_PROMPT_NEW_REPEAT:
                    createRepeatDialog();
                    break;
            }
        } else {
            mBeenSwiped = false;
        }
    }


    /**
     * Determines what should be displayed between a question, or the start screen and displays the
     * appropriate view. Also saves answers to the data model without checking constraints.
     */
    private void showPreviousView() {
        // The answer is saved on a back swipe, but question constraints are
        // ignored.
        if (currentPromptIsQuestion()) {
            saveCurrentAnswer(false);
        }

        if (mFormEntryModel.getEvent() != FormEntryController.EVENT_BEGINNING_OF_FORM) {
            int event = mFormEntryController.stepToPreviousEvent();

            while (event != FormEntryController.EVENT_BEGINNING_OF_FORM
                    && event != FormEntryController.EVENT_QUESTION) {
                event = mFormEntryController.stepToPreviousEvent();
            }

            View next = createView(event);
            showView(next, AnimationType.LEFT);
        } else {
            mBeenSwiped = false;
        }
    }
    
    /**
     * Initialize the end view of the form.
     */
    public void initFormEntryEndView() {
    		
    	ImageView emochaImageView = (ImageView) endView.findViewById(R.id.emochaLogo);
        RelativeLayout.LayoutParams emochaLayoutParams = new RelativeLayout.
	    		LayoutParams((int)(ScreenEnvironment.width * 0.35), (int)(ScreenEnvironment.height * 0.043));
	    emochaLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
	    emochaLayoutParams.topMargin = (int)(ScreenEnvironment.height * 0.85);
	    emochaImageView.setLayoutParams(emochaLayoutParams); 

        // Create 'save for later' button
        submitButton = (Button) endView.findViewById(R.id.save_exit_button);
        RelativeLayout.LayoutParams submitLayoutParams = new RelativeLayout.
	    		LayoutParams((int)(ScreenEnvironment.width * 0.767), (int)(ScreenEnvironment.height * 0.115625));
	    submitLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
	    submitLayoutParams.topMargin = (int)(ScreenEnvironment.height * 0.307);
	    submitButton.setLayoutParams(submitLayoutParams);
	    
        submitButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Form is marked as 'saved' here.                              
            	// eMOCHA modified : get XML and send it back to eMOCHA. 
            	MiDOTUtils.FORM_SUBMITTED = true;
            	Intent resultIntent = getIntent();
            	mInstanceXML = getDataAsXmlString(true);
            	if (saveData) {
            		submitButton.setEnabled(false);//avoid clicking twice!! it generates an extra form!
            		persistXmlData();
            	}
            	resultIntent.putExtra(Constants.ODK_INSTANCEXML_KEY, mInstanceXML);
            	resultIntent.putExtra(Constants.ODK_INSTANCEPATH_KEY, mInstancePath);
            	resultIntent.putExtra(Constants.ODK_EXTRA_DATA_KEY, mExtraData);
            	setResult(RESULT_OK, resultIntent);
            	//end eMOCHA
            }
        });
    }
    
    /**
     * Initialize the begining view of the form.
     */
    public void initFormEntryStartView() {
    	
		TextView personTitleTextView = (TextView) startView.findViewById(R.id.personTitleTextView);
		TextView dayTextView = (TextView) startView.findViewById(R.id.appStatDayText);
		TextView dateTextView = (TextView) startView.findViewById(R.id.appStatDateText);
		
		Date date = new Date();
		String[] c = date.getCurrentDate();
		dayTextView.setText(c[0]);
		dateTextView.setText(c[1]);
		
		RelativeLayout.LayoutParams titleLayoutParams = new RelativeLayout.
	    		LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	    titleLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
	    titleLayoutParams.topMargin = (int)(ScreenEnvironment.height * 0.061);
	    personTitleTextView.setLayoutParams(titleLayoutParams);
	    personTitleTextView.setTextSize((int)(ScreenEnvironment.height * 0.0315625));
		//personTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
		
		RelativeLayout.LayoutParams dayLayoutParams = new RelativeLayout.
	    		LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	    dayLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
	    dayLayoutParams.topMargin = (int)(ScreenEnvironment.height * 0.108);
	    dayTextView.setLayoutParams(dayLayoutParams);
	    dayTextView.setTextSize((int)(ScreenEnvironment.height * 0.078125));
	    
	    RelativeLayout.LayoutParams dateLayoutParams = new RelativeLayout.
	    		LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	    dateLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
	    dateLayoutParams.topMargin = (int)(ScreenEnvironment.height * 0.22);
	    dateTextView.setLayoutParams(dateLayoutParams);
	    dateTextView.setTextSize((int)(ScreenEnvironment.height * 0.03125));
	    
	    addSlideFrameLayout(startView);    
    }
    
    private void addSlideFrameLayout(View view) {
	    
	    /** Slide FrameLayout */
	    
	    FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.slideFrame);
	    RelativeLayout.LayoutParams frameLayoutParams = new RelativeLayout.
	    		LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	    frameLayoutParams.topMargin = (int)(ScreenEnvironment.height * 0.85);
	    frameLayout.setLayoutParams(frameLayoutParams);
	    
		ImageView slideLayerImageView3 = (ImageView) view.findViewById(R.id.slide_layer3);
		ImageView slideLayerImageView2 = (ImageView) view.findViewById(R.id.slide_layer2);
		ImageView slideLayerImageView1 = (ImageView) view.findViewById(R.id.slide_layer1);
		
		FrameLayout.LayoutParams slideFrameLayoutParams = new FrameLayout.
				LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, (int)(ScreenEnvironment.height * 0.03125));
		slideFrameLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
		slideLayerImageView3.setLayoutParams(slideFrameLayoutParams);
		slideLayerImageView1.setLayoutParams(slideFrameLayoutParams);
		
		// margin-left - 85.5px;
		
		FrameLayout.LayoutParams slideLayoutParams = new FrameLayout.
				LayoutParams((int)(ScreenEnvironment.width * 0.3515625), (int)(ScreenEnvironment.height * 0.03125));
		slideLayoutParams.gravity = Gravity.RIGHT;
		slideLayerImageView2.setLayoutParams(slideLayoutParams);
		
		TextView slideTextView = (TextView) view.findViewById(R.id.slideText);
		RelativeLayout.LayoutParams slideTextLayoutParams = new RelativeLayout.
				LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		slideTextLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		slideTextLayoutParams.topMargin = (int)(ScreenEnvironment.height * 0.88);
		slideTextView.setLayoutParams(slideTextLayoutParams);
		slideTextView.setTextSize((int)(ScreenEnvironment.height * 0.0234375));
		
		Animation animation = new TranslateAnimation(88.125f, -300.0f, 0.0f, 0.0f);
		animation.setDuration(2000);
		animation.setRepeatCount(Animation.INFINITE);
		slideLayerImageView2.startAnimation(animation);
    }


    /**
     * Displays the View specified by the parameter 'next', animating both the current view and next
     * appropriately given the AnimationType. Also updates the progress bar.
     */
    public void showView(View next, AnimationType from) {
    	
    	//overridePendingTransition(R.anim.enter_from_right, R.anim.exit_from_left);
        switch (from) {
            case RIGHT:
                mInAnimation = AnimationUtils.loadAnimation(this, R.anim.push_left_in);
                mOutAnimation = AnimationUtils.loadAnimation(this, R.anim.push_left_out);
                break;
            case LEFT:
                mInAnimation = AnimationUtils.loadAnimation(this, R.anim.push_right_in);
                mOutAnimation = AnimationUtils.loadAnimation(this, R.anim.push_right_out);
                break;
            case FADE:
                mInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
                mOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
                break;
            case NORMAL:
            	break;
        }

        if (mCurrentView != null) {
            mCurrentView.startAnimation(mOutAnimation);
            mRelativeLayout.removeView(mCurrentView);
        }

        mInAnimation.setAnimationListener(this);

        // We must call setMax() first because it doesn't redraw the progress
        // bar.

        // UnComment to make progress bar work.
        // WARNING: will currently slow large forms considerably
        // TODO: make the progress bar fast. Must be done in javarosa.
        // mProgressBar.setMax(mFormEntryModel.getTotalRelevantQuestionCount());
        // mProgressBar.setProgress(mFormEntryModel.getCompletedRelevantQuestionCount());

        RelativeLayout.LayoutParams lp =
            new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        lp.addRule(RelativeLayout.ABOVE, R.id.progressbar);

        mCurrentView = next;
        mRelativeLayout.addView(mCurrentView, lp);

        mCurrentView.startAnimation(mInAnimation);
        if (mCurrentView instanceof QuestionView
                && !mFormEntryModel.getQuestionPrompt().isReadOnly())
            ((QuestionView) mCurrentView).setFocus(this);
        else {
            InputMethodManager inputManager =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(mCurrentView.getWindowToken(), 0);
        }
    }


    // TODO: use managed dialogs when the bugs are fixed
    /*
     * Ideally, we'd like to use Android to manage dialogs with onCreateDialog() and
     * onPrepareDialog(), but dialogs with dynamic content are broken in 1.5 (cupcake). We do use
     * managed dialogs for our static loading ProgressDialog. The main issue we noticed and are
     * waiting to see fixed is: onPrepareDialog() is not called after a screen orientation change.
     * http://code.google.com/p/android/issues/detail?id=1639
     */

    /**
     * The dialog shows options for GPS to get location.
     */
    public void createLocationDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIcon(android.R.drawable.ic_dialog_info);
        DialogInterface.OnClickListener geopointButtonListener =
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // TODO: what?
                    Log.i("yaw", "inside form entry cancel button");
                }
            };
        mProgressDialog.setCancelable(false);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setTitle(getString(R.string.getting_location));
        mProgressDialog.setMessage(getString(R.string.please_wait));
        mProgressDialog.setButton(getString(R.string.cancel_location), geopointButtonListener);
        mProgressDialog.setButton2(getString(R.string.accept_location), geopointButtonListener);
        mProgressDialog.show();
    }


    /**
     * Creates and displays a dialog displaying the violated constraint.
     */
    private void createConstraintToast(String constraintText, int saveStatus) {
        switch (saveStatus) {
            case FormEntryController.ANSWER_CONSTRAINT_VIOLATED:
                if (constraintText == null) {
                    constraintText = getString(R.string.invalid_answer_error);
                }
                break;
            case FormEntryController.ANSWER_REQUIRED_BUT_EMPTY:
                constraintText = getString(R.string.required_answer_error);
                break;
        }

        showCustomToast(constraintText);
        mBeenSwiped = false;
    }


    /**
     * Show customized toast if the constraint violated.
     * @param message Message to show on the toast.
     */
    private void showCustomToast(String message) {
        LayoutInflater inflater =
            (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.toast_view, null);

        // set the text in the view
        TextView tv = (TextView) view.findViewById(R.id.message);
        tv.setText(message);

        Toast t = new Toast(this);
        t.setView(view);
        t.setDuration(Toast.LENGTH_SHORT);
        t.setGravity(Gravity.CENTER, 0, 0);
        t.show();
    }


    /**
     * Creates and displays a dialog asking the user if they'd like to create a repeat of the
     * current group.
     */
    private void createRepeatDialog() {
        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
        DialogInterface.OnClickListener repeatListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON1: // yes, repeat
                        mFormEntryController.newRepeat();
                        showNextView();
                        break;
                    case DialogInterface.BUTTON2: // no, no repeat
                        showNextView();
                        break;
                }
            }
        };
        if (getLastRepeatCount(getGroupsForCurrentIndex()) > 0) {
            mAlertDialog.setTitle(getString(R.string.leaving_repeat_ask));
            mAlertDialog.setMessage(getString(R.string.add_another_repeat,
                getLastGroupText(getGroupsForCurrentIndex())));
            mAlertDialog.setButton(getString(R.string.add_another), repeatListener);
            mAlertDialog.setButton2(getString(R.string.leave_repeat_yes), repeatListener);

        } else {
            mAlertDialog.setTitle(getString(R.string.entering_repeat_ask));
            mAlertDialog.setMessage(getString(R.string.add_repeat,
                getLastGroupText(getGroupsForCurrentIndex())));
            mAlertDialog.setButton(getString(R.string.entering_repeat), repeatListener);
            mAlertDialog.setButton2(getString(R.string.add_repeat_no), repeatListener);
        }
        mAlertDialog.setCancelable(false);
        mAlertDialog.show();
        mBeenSwiped = false;
    }


    /**
     * Creates and displays dialog with the given errorMsg.
     */
    private void createErrorDialog(String errorMsg, final boolean shouldExit) {
        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
        mAlertDialog.setMessage(errorMsg);
        DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON1:
                        if (shouldExit) {
                            finish();
                        }
                        break;
                }
            }
        };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.ok), errorListener);
        mAlertDialog.show();
    }


    /**
     * Creates a confirm/cancel dialog for deleting repeats.
     */
    private void createDeleteRepeatConfirmDialog() {
        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.setIcon(android.R.drawable.ic_dialog_alert);
        String name = getLastRepeatedGroupName(getGroupsForCurrentIndex());
        int repeatcount = getLastRepeatedGroupRepeatCount(getGroupsForCurrentIndex());
        if (repeatcount != -1) {
            name += " (" + (repeatcount + 1) + ")";
        }
        mAlertDialog.setTitle(getString(R.string.delete_repeat_ask));
        mAlertDialog.setMessage(getString(R.string.delete_repeat_confirm, name));
        DialogInterface.OnClickListener quitListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON1: // yes
                        FormIndex validIndex = mFormEntryController.deleteRepeat();
                        mFormEntryController.jumpToIndex(validIndex);
                        showPreviousView();
                        break;
                    case DialogInterface.BUTTON2: // no
                        break;
                }
            }
        };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.discard_group), quitListener);
        mAlertDialog.setButton2(getString(R.string.delete_repeat_no), quitListener);
        mAlertDialog.show();
    }


    /**
     * Called during a 'save and exit' command. The form is not 'done' here.
     * eMOCHA: based on saveDataToDisk(); modified to return the data as an XML string, 
     * instead of saving it to the sdcard.
     */
    private String getDataAsXmlString(boolean exit) {
    	AsyncTask<Void,String, EmochaResult> task;
    	String data = null;
    	// save current answer
    	if (!saveCurrentAnswer(true)) {
            Toast.makeText(getApplicationContext(), getString(R.string.data_saved_error),
                Toast.LENGTH_SHORT).show();
            return null;
        }
        mSaveToXmlStringTask = new SaveToXmlStringTask();
        mSaveToXmlStringTask.setFormSavedListener(this);
        
        mSaveToXmlStringTask.setExportVars(exit);
        task = mSaveToXmlStringTask.execute();
        showDialog(SAVING_DIALOG);
       
		try {
			data = task.get().content;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {

			e.printStackTrace();
		}
        return data;
    }

    private void persistXmlData() {

    	//1.- get response's data
    	if (mExtraData != null) {
			int patientId = Integer.parseInt(mExtraData.getString(Patient.TABLE_NAME));
			
			String tCode = mExtraData.getString(FormTemplate.TABLE_NAME); 
			
			DBAdapter.beginTransaction();
			try {
				//prepare xml
				FormData formData = new FormData(tCode, mInstanceXML, mInstancePath, CommonUtils.getCurrentTime(getApplicationContext()), Constants.ONE);
				
				//do the insert:
				formData.id = DBAdapter.insert(FormData.TABLE_NAME, formData.getContentValues());
				PatientData pData = new PatientData(patientId, formData.id);
				DBAdapter.insert(PatientData.TABLE_NAME, pData.getContentValues());
				
				DBAdapter.setTransactionSuccessful();
			} catch (Exception e){
				Log.e(Constants.LOG_TAG, getApplicationContext().getString(R.string.transaction_error, e.getMessage()));
			} finally {
				DBAdapter.endTransaction();
			}
    	} else {
    		Log.e(Constants.LOG_TAG, "Something is very wrong... calling persistXmlData without proper flags???");
    	}
    }

/* eMOCHA: no longer used...

    private TreeReference getFormElementRef (IFormElement fe, TreeElement te) {
        if (fe instanceof FormDef) {
          TreeReference ref = TreeReference.rootRef();
          ref.add(te.getName(), 0);
          return ref;
        } else {
          return null;//(TreeReference)fe.getBind().getReference();
        }
      }
	//take a (possibly relative) reference, and make it absolute based on its parent
	private IDataReference getAbsRef (IDataReference ref, TreeReference parentRef) {
		TreeReference tref;
		
		if (!parentRef.isAbsolute()) {
			throw new RuntimeException("XFormParser.getAbsRef: parentRef must be absolute");
		}
		
		if (ref != null) {
			tref = (TreeReference)ref.getReference();
		} else {
			tref = TreeReference.selfRef(); //only happens for <group>s with no binding
		}		
		
		tref = tref.parent(parentRef);
		if (tref == null) {
			throw new XFormParseException("FormEntryActivity: Binding path [" + tref + "] not allowed with parent binding of [" + parentRef + "]");
		}
		
		return new XPathReference(tref);
	}

*/
    /**
     * Show dialog when patient want to quit during the process.
     */
    private void createQuitDialog() {
    	String[] items =
            {
                    getString(R.string.do_not_save), getString(R.string.quit_entry),
                    getString(R.string.do_not_exit)
            };
        mAlertDialog =
            new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(
                getString(R.string.quit_application)).setItems(items,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: // discard changes and exit
                            	MiDOTUtils.FORM_SUBMITTED = false;
                            	//make sure to delete tmp files..
                            	if (mInstancePath != null && !Constants.EMPTY_STRING.equals(mInstancePath)) {
                            								
                            	         String instanceFolder = mInstancePath.substring(0, mInstancePath.lastIndexOf("/") + 1);
                            								
                            		ArrayList<String> al = FileUtils.getFilesAsArrayListRecursive(instanceFolder);
                            		FormDataFile fdf = new FormDataFile();
                            		for (String path : al) {
                            			if (!fdf.fdfExists(path)){ //remove when doesn't exist
                            				//1.- remove from content resolver (and filesystem)
                            				String[] projection = {Images.ImageColumns._ID};
                            				Cursor ci = getContentResolver().query(Images.Media.EXTERNAL_CONTENT_URI, projection,
                            									                                    "_data like '%" + path + "%'", null, null);
                            				if (ci.getCount()>0) {
                            					ci.moveToFirst();
                            					String id = ci.getString(ci.getColumnIndex(Images.ImageColumns._ID));
                            					Log.i(t, "attempting to delete unused image: "+ Uri.withAppendedPath(Images.Media.EXTERNAL_CONTENT_URI, id));
                            				        getContentResolver().delete(Uri.withAppendedPath(Images.Media.EXTERNAL_CONTENT_URI, id),null, null);
                            				}
                            			}
                            		}
                            	}
                                finish();
                                break;

                            case 1: // save and exit
                            	// eMOCHA modified
                            	mInstanceXML = getDataAsXmlString(true);
                            	
                            	//update xml and use previously loaded values before calling setResult() and finish()..
                            	//.. otherwise data is not provided to eMOCHA.
                            	MiDOTUtils.FORM_SUBMITTED = false;
                            	Intent data = getIntent();
                            	if (data != null) {
                            		
                            		data.putExtra(Constants.ODK_INSTANCEXML_KEY, mInstanceXML);
                            		
                            		data.putExtra(Constants.ODK_FORMPATH_KEY, mFormPath);
                            		data.putExtra(Constants.ODK_INSTANCEPATH_KEY, mInstancePath);
                            		data.putExtra(Constants.ODK_EXTRA_DATA_KEY, mExtraData);
                            		
                            		setResult(Activity.RESULT_OK, data);
                            	} else {
                            		Log.w(t, "EMOCHA: createQuitDialog().... getIntent() is null??!!!");
                            	}
                            	
                            	finish();
                            	// end EMOCHA
                                break;

                            case 2:// do nothing
                                break;

                        }
                    }
                }).create();
        mAlertDialog.show();
    }

    /**
     * Confirm clear dialog.
     */
    private void createClearDialog() {
        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.setIcon(android.R.drawable.ic_dialog_alert);

        mAlertDialog.setTitle(getString(R.string.clear_answer_ask));

        String question = mFormEntryModel.getQuestionPrompt().getLongText();
        if (question.length() > 50) {
            question = question.substring(0, 50) + "...";
        }

        mAlertDialog.setMessage(getString(R.string.clearanswer_confirm, question));

        DialogInterface.OnClickListener quitListener = new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON1: // yes
                        clearCurrentAnswer();
                        saveCurrentAnswer(false);
                        break;
                    case DialogInterface.BUTTON2: // no
                        break;
                }
            }
        };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.discard_answer), quitListener);
        mAlertDialog.setButton2(getString(R.string.clear_answer_no), quitListener);
        mAlertDialog.show();
    }


    /**
     * Creates and displays a dialog allowing the user to set the language for the form.
     */
    private void createLanguageDialog() {
        final String[] languages = mFormEntryModel.getLanguages();
        int selected = -1;
        if (languages != null) {
            String language = mFormEntryModel.getLanguage();
            for (int i = 0; i < languages.length; i++) {
                if (language.equals(languages[i])) {
                    selected = i;
                }
            }
        }
        mAlertDialog =
            new AlertDialog.Builder(this).setSingleChoiceItems(languages, selected,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mFormEntryController.setLanguage(languages[whichButton]);
                        dialog.dismiss();
                        if (currentPromptIsQuestion()) {
                            saveCurrentAnswer(false);
                        }
                        refreshCurrentView();
                    }
                }).setTitle(getString(R.string.change_language)).setNegativeButton(
                getString(R.string.do_not_change), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).create();
        mAlertDialog.show();
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PROGRESS_DIALOG:
                mProgressDialog = new ProgressDialog(this);
                DialogInterface.OnClickListener loadingButtonListener =
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            mFormLoaderTask.setFormLoaderListener(null);
                            mFormLoaderTask.cancel(true);
                            finish();
                        }
                    };
                mProgressDialog.setIcon(android.R.drawable.ic_dialog_info);
                mProgressDialog.setTitle(getString(R.string.loading_form));
                mProgressDialog.setMessage(getString(R.string.please_wait));
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setButton(getString(R.string.cancel_loading_form),
                    loadingButtonListener);
                return mProgressDialog;
            case SAVING_DIALOG:
                mProgressDialog = new ProgressDialog(this);
                DialogInterface.OnClickListener savingButtonListener =
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            mSaveToXmlStringTask.setFormSavedListener(null);
                            mSaveToXmlStringTask.cancel(true);
                        }
                    };
                mProgressDialog.setIcon(android.R.drawable.ic_dialog_info);
                mProgressDialog.setTitle(getString(R.string.saving_form));
                mProgressDialog.setMessage(getString(R.string.please_wait));
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setButton(getString(R.string.cancel), savingButtonListener);
                mProgressDialog.setButton(getString(R.string.cancel_saving_form),
                    savingButtonListener);
                return mProgressDialog;

        }
        return null;
    }


    /**
     * Dismiss any showing dialogs that we manage.
     */
    private void dismissDialogs() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
        dismissDialogs();
        super.onPause();
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
    	
    	//## eMOCHA user session control 
    	/*
    	Context context = getApplicationContext();
		
		if (Integer.parseInt(Preferences.getDevicePasswordOn(context)) > 0 ) {
			long now = Calendar.getInstance().getTimeInMillis();
			if (now > Preferences.getLastUserLogIn(context)+Preferences.getUserSessionTimeout(context)) {
				Intent i = new Intent(context, LogInActivity.class);
				//put required info to do a LogIn
				i.putExtra(Constants.LAUNCH_ACTIVITY, FormEntryActivity.class);
				
				i.putExtra(Constants.ODK_FORMPATH_KEY, mFormPath);
				i.putExtra(Constants.ODK_INSTANCEPATH_KEY, mInstancePath);
				i.putExtra(Constants.ODK_INSTANCEXML_KEY, mInstanceXML);
				i.putExtra(Constants.ODK_EXTRA_DATA_KEY, mExtraData);
				i.putExtra(Constants.ODK_PERSIST_DATA, saveData);
				i.putExtra(Constants.ODK_NEW_FORM_KEY, newForm);
				
				// we need to push the data through the activities stack
				i.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
				startActivity(i);
				finish();
			} else { //refresh last login
				Preferences.setLastUserLogIn(now, context);
			}
	    }
	    */
		//##
    	
        if (mFormLoaderTask != null) {
            mFormLoaderTask.setFormLoaderListener(this);
            if (mFormLoaderTask.getStatus() == AsyncTask.Status.FINISHED) {
                dismissDialog(PROGRESS_DIALOG);
                refreshCurrentView();
            }
        }
        if (mSaveToXmlStringTask != null) {
            mSaveToXmlStringTask.setFormSavedListener(this);
        }
        super.onResume();
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                createQuitDialog();
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (event.isAltPressed() && !mBeenSwiped) {
                    mBeenSwiped = true;
                    showNextView();
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (event.isAltPressed() && !mBeenSwiped) {
                    mBeenSwiped = true;
                    showPreviousView();
                    return true;
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        if (mFormLoaderTask != null) {
            mFormLoaderTask.setFormLoaderListener(null);
            // We have to call cancel to terminate the thread, otherwise it lives on and retains the
            // FEC in memory.
            mFormLoaderTask.cancel(true);
            mFormLoaderTask.destroy();
        }
        if (mSaveToXmlStringTask != null) {
            // We have to call cancel to terminate the thread, otherwise it lives on and retains the
            // FEC in memory.
            mSaveToXmlStringTask.cancel(false);
            mSaveToXmlStringTask.setFormSavedListener(null);
        }
        // eMOCHA modified: remove empty folders
        File instancesPath = new File(Environment.getExternalStorageDirectory()
        							+ getString(R.string.app_path_base)
        							+ getString(R.string.app_odk_path)
        							+ getString(R.string.app_odk_data_path));
        
				if (instancesPath.exists()) {
        	for (File file: instancesPath.listFiles()) {
        		if (file.exists() && file.isDirectory()) {
        	    	if (file.list() != null && file.list().length == 0) {
        	    		file.delete();
        	    	}
        	    }	
        	}
				}
        // end eMOCHA
        super.onDestroy();

    }


    /*
     * (non-Javadoc)
     * 
     * @see android.view.animation.Animation.AnimationListener#onAnimationEnd(android
     * .view.animation.Animation)
     */
    public void onAnimationEnd(Animation arg0) {
        mBeenSwiped = false;
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.view.animation.Animation.AnimationListener#onAnimationRepeat(
     * android.view.animation.Animation)
     */
    public void onAnimationRepeat(Animation animation) {
        // Added by AnimationListener interface.
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.view.animation.Animation.AnimationListener#onAnimationStart(android
     * .view.animation.Animation)
     */
    public void onAnimationStart(Animation animation) {
        // Added by AnimationListener interface.
    }


    /**
     * loadingComplete() is called by FormLoaderTask once it has finished loading a form.
     */
    public void loadingComplete(FormEntryController fec) {
        dismissDialog(PROGRESS_DIALOG);
        if (fec == null) {
            createErrorDialog(getString(R.string.load_error, mFormPath.substring(mFormPath
                    .lastIndexOf(Constants.SLASH_STRING) + 1)), true);
        } else {
            mFormEntryController = fec;
            mFormEntryModel = fec.getModel();

            if (mInstancePath == null || Constants.EMPTY_STRING.equals(mInstancePath)) {
            	Log.w(Constants.LOG_TAG, "opening form with empty instance path.. might cause errors if form has media questions. Make sure you provide a unique instance path.");
            } else {
            	FileUtils.createFolder(mInstancePath);
            }

            if (mInstanceXML != null) { //data exists
                // we've just loaded a saved form, so start in the hierarchy view
                Intent i = new Intent(this, FormHierarchyActivity.class);
               
                //eMOCHA: provide parameters to FHA
                i.putExtra(Constants.ODK_FORMPATH_KEY, mFormPath);
				i.putExtra(Constants.ODK_INSTANCEPATH_KEY, mInstancePath);
				i.putExtra(Constants.ODK_INSTANCEXML_KEY, mInstanceXML);
				i.putExtra(Constants.ODK_EXTRA_DATA_KEY, mExtraData);
				i.putExtra(Constants.ODK_PERSIST_DATA, saveData);
				i.putExtra(Constants.ODK_NEW_FORM_KEY, newForm);
				// we need to push the data through the activities stack
				//i.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                //end eMOCHA
                
                startActivity(i);
                return; // so we don't show the intro screen before jumping to the hierarchy
            }

            refreshCurrentView();
        }
    }


    /**
     * Show toast whether the form has been saved or not.
     */
    public void savingComplete(int saveStatus) {
        dismissDialog(SAVING_DIALOG);
        switch (saveStatus) {
        // eMOCHA modified: replace SaveToDiskTask for SaveToXmlStringTask. 
            case SaveToXmlStringTask.SAVED:
            	Toast.makeText(getApplicationContext(), getString(R.string.data_saved_ok),
                    Toast.LENGTH_SHORT).show();
                break;              
            case SaveToXmlStringTask.SAVED_AND_EXIT:
                Toast.makeText(getApplicationContext(), getString(R.string.data_saved_ok),
                    Toast.LENGTH_SHORT).show();
                finish();
                break;                
            case SaveToXmlStringTask.SAVE_ERROR:
                Toast.makeText(getApplicationContext(), getString(R.string.data_saved_error),
                    Toast.LENGTH_LONG).show();
                break;
            // eMOCHA modified
            case SaveToXmlStringTask.VALIDATE_ERROR:
            	Toast.makeText(getApplicationContext(), "Questionnaire was not completed yet. Data will not be saved. Finish the form or exit without saving",
                        Toast.LENGTH_LONG).show();
                break;
            //end eMOCHA
            case FormEntryController.ANSWER_CONSTRAINT_VIOLATED:
            case FormEntryController.ANSWER_REQUIRED_BUT_EMPTY:
                refreshCurrentView();
                createConstraintToast(mFormEntryModel.getQuestionPrompt().getConstraintText(),
                    saveStatus);
                Toast.makeText(getApplicationContext(), getString(R.string.data_saved_error),
                    Toast.LENGTH_LONG).show();
                break;
        }
    }

    /** 
     * Save the answer data the patient filled.
     * @param answer Answer data
     * @param evaluateConstraints Boolean value to check if exceeds constraints.
     * @return
     */
    public int saveAnswer(IAnswerData answer, boolean evaluateConstraints) {
        if (evaluateConstraints) {
            return mFormEntryController.answerQuestion(answer);
        } else {
            mFormEntryController.saveAnswer(mFormEntryModel.getFormIndex(), answer);
            return FormEntryController.ANSWER_OK;
        }
    }


    private FormEntryCaption[] getGroupsForCurrentIndex() {
        if (!(mFormEntryModel.getEvent() == FormEntryController.EVENT_QUESTION || mFormEntryModel
                .getEvent() == FormEntryController.EVENT_PROMPT_NEW_REPEAT))
            return null;

        int lastquestion = 1;
        if (mFormEntryModel.getEvent() == FormEntryController.EVENT_PROMPT_NEW_REPEAT)
            lastquestion = 0;
        FormEntryCaption[] v = mFormEntryModel.getCaptionHierarchy();
        FormEntryCaption[] groups = new FormEntryCaption[v.length - lastquestion];
        for (int i = 0; i < v.length - lastquestion; i++) {
            groups[i] = v[i];
        }
        return groups;
    }


    /**
     * Loops through the FormEntryController until a non-group event is found.
     * 
     * @return The event found
     */
    private int getNextNotGroupEvent() {
        int event = mFormEntryController.stepToNextEvent();

        while (event == FormEntryController.EVENT_GROUP
                || event == FormEntryController.EVENT_REPEAT) {
        	event = mFormEntryController.stepToNextEvent();
        }
        return event;
    }


    /**
     * The repeat count of closest group the prompt belongs to.
     */
    private int getLastRepeatCount(FormEntryCaption[] groups) {
        // no change
        if (getLastGroup(groups) != null) {
            return getLastGroup(groups).getMultiplicity();
        }
        return -1;

    }


    /**
     * The text of closest group the prompt belongs to.
     */
    private String getLastGroupText(FormEntryCaption[] groups) {
        // no change
        if (getLastGroup(groups) != null) {
            return getLastGroup(groups).getLongText();
        }
        return null;
    }


    /**
     * The closest group the prompt belongs to.
     * 
     * @return FormEntryCaption
     */
    private FormEntryCaption getLastGroup(FormEntryCaption[] groups) {
        if (groups == null || groups.length == 0)
            return null;
        else
            return groups[groups.length - 1];
    }


    /**
     * The name of the closest group that repeats or null.
     */
    private String getLastRepeatedGroupName(FormEntryCaption[] groups) {
        // no change
        if (groups.length > 0) {
            for (int i = groups.length - 1; i > -1; i--) {
                if (groups[i].repeats()) {
                    return groups[i].getLongText();
                }
            }
        }
        return null;
    }


    /**
     * The count of the closest group that repeats or -1.
     */
    private int getLastRepeatedGroupRepeatCount(FormEntryCaption[] groups) {
        if (groups.length > 0) {
            for (int i = groups.length - 1; i > -1; i--) {
                if (groups[i].repeats()) {
                    return groups[i].getMultiplicity();

                }
            }
        }
        return -1;
    }

    private boolean indexContainsRepeatableGroup(FormIndex index) {
        FormEntryCaption[] groups = mFormEntryModel.getCaptionHierarchy();
        if (groups.length == 0) {
            return false;
        }
        for (int i = 0; i < groups.length; i++) {
            if (groups[i].repeats())
                return true;
        }
        return false;
    }
    
}
