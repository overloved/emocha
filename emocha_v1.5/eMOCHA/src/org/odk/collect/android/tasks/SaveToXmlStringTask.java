/*******************************************************************************
 * eMOCHA - electronic Mobile Open-Source Comprehensive Health Application
 * Copyright (c) 2010  Pau Varela - pau.varela@gmail.com
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

package org.odk.collect.android.tasks;

import org.emocha.Constants;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.services.transport.payload.ByteArrayPayload;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.model.xform.XFormSerializingVisitor;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.listeners.FormSavedListener;
import org.odk.collect.android.utilities.EmochaResult;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * eMOCHA: based on SaveToDiskTask to return an XML String and avoid saving data to the public sdcard
 * Result contains an EmochaResult object, with the result code and the xml string(or null)
 */
public class SaveToXmlStringTask extends AsyncTask<Void, String, EmochaResult> {
    private final static String t = "SaveToXmlStringTask";

    private FormSavedListener mSavedListener;
    private Boolean mSave;
    private String xmlData = null;

    public static final int SAVED = 500;
    public static final int SAVE_ERROR = 501;
    public static final int VALIDATE_ERROR = 502;
    public static final int VALIDATED = 503;
    public static final int SAVED_AND_EXIT = 504;

    /**
     * Initialize {@link FormEntryController} with {@link FormDef} from binary or from XML. If given
     * an instance, it will be used to fill the {@link FormDef}.
     */
    @Override
    protected EmochaResult doInBackground(Void... nothing) {
    	
        // validation failed, pass specific failure
        int validateStatus = validateAnswers();
        if (validateStatus != VALIDATED) {
        	return new EmochaResult(VALIDATE_ERROR,null);
        	//return null;
        }
        FormEntryActivity.mFormEntryController.getModel().getForm().postProcessInstance();
        
        if (mSave && exportData()) {
            return new EmochaResult(SAVED_AND_EXIT,getXmlData());
        } else if (exportData()) {
            return new EmochaResult(SAVED,getXmlData());
        }
        return new EmochaResult(SAVE_ERROR, null);
    }

    public boolean exportData() {    	
        ByteArrayPayload payload;
        try {

            // assume no binary data inside the model.
            FormInstance datamodel = FormEntryActivity.mFormEntryController.getModel().getForm().getInstance();
            XFormSerializingVisitor serializer = new XFormSerializingVisitor();
            payload = (ByteArrayPayload) serializer.createSerializedPayload(datamodel);

            // convert payload to String:
            setXmlData(convertStreamToString(payload.getPayloadStream()));

        } catch (IOException e) {
            Log.e(t, "Error creating serialized payload");
            e.printStackTrace();
            return false;
        }
        // missing fda operations here. will avoid them if not needed.
        // would create problems with fda methods like createFile()
        // because they attempt to read md5 and other properties from
        // a file which we don't have (we have the content of the file only!)
/* eMOCHA: needed ?
        FileDbAdapter fda = new FileDbAdapter();
        fda.open();
        File f = new File(instancePath);
        Cursor c = fda.fetchFilesByPath(f.getAbsolutePath(), null);

        if (c != null && c.getCount() == 0) {
            fda
                    .createFile(instancePath, FileDbAdapter.TYPE_INSTANCE,
                        FileDbAdapter.STATUS_COMPLETE);

        } else {
            fda.updateFile(instancePath, FileDbAdapter.STATUS_COMPLETE);
        }
        // clean up cursor
        if (c != null) {
            c.close();
        }

        fda.close();
*/
        return true;
    }

    @Override
    protected void onPostExecute(EmochaResult result) {
        synchronized (this) {
            if (mSavedListener != null)
            	//mSavedListener.savingComplete(SAVED);
            	// eMOCHA modified: set is as saved
                mSavedListener.savingComplete(result.code); 
        }
        
    }


    public void setFormSavedListener(FormSavedListener fsl) {
        synchronized (this) {
            mSavedListener = fsl;
        }
    }

    public void setExportVars(Boolean saveAndExit) {
        mSave = saveAndExit;
    }


    /**
     * Goes through the entire form to make sure all entered answers comply with their constraints.
     * Constraints are ignored on 'jump to', so answers can be outside of constraints. We don't
     * allow saving to disk, though, until all answers conform to their constraints/requirements.
     * 
     * @param markCompleted
     * @return
     */

    private int validateAnswers() {

        FormEntryModel fem = FormEntryActivity.mFormEntryController.getModel();
        FormIndex i = fem.getFormIndex();

        FormEntryActivity.mFormEntryController.jumpToIndex(FormIndex.createBeginningOfFormIndex());

        int event;
        while ((event = FormEntryActivity.mFormEntryController.stepToNextEvent()) != FormEntryController.EVENT_END_OF_FORM) {
        	if (event != FormEntryController.EVENT_QUESTION) {
                continue;
            } else {
                int saveStatus =
                    FormEntryActivity.mFormEntryController.answerQuestion(fem.getQuestionPrompt().getAnswerValue());
                if (saveStatus != FormEntryController.ANSWER_OK) {
                	 Log.e(Constants.LOG_TAG, "question fails (qtext): "+fem.getQuestionPrompt().getQText());
                     Log.e(Constants.LOG_TAG, "question fails (helpText): "+fem.getQuestionPrompt().getHelpText());
                    return saveStatus;
                }
            }
        }

        FormEntryActivity.mFormEntryController.jumpToIndex(i);
        return VALIDATED;
    }

    /**
     * eMOCHA. converts an InputStream to an String
     * @param stream
     * @return xml-string
     */
	private String convertStreamToString(InputStream stream) {
		String carrier = "\n";
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(stream));
		StringBuilder result = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				result.append(line + carrier);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result.toString();
	}

	private synchronized void setXmlData (String data) {
		xmlData = data;
	}
	public synchronized String getXmlData() {
		return xmlData;
	}
}
