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

package org.odk.collect.android.tasks;

import org.emocha.midot.R;
import org.emocha.Constants;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.core.reference.RootTranslator;
import org.javarosa.core.services.PrototypeManager;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xform.util.XFormUtils;
import org.odk.collect.android.functions.CreateRepeatNodeIdFunction;
import org.odk.collect.android.listeners.FormLoaderListener;
import org.odk.collect.android.logic.FileReferenceFactory;
import org.odk.collect.android.utilities.FileUtils;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Background task for loading a form.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class FormLoaderTask extends AsyncTask<String, String, FormLoaderTask.FECWrapper> {
    private final static String t = "FormLoaderTask";
    // eMOCHA modified:
    private final String TMP_STUDY_IDS_FILE = "testStudyIds.tmp";
    // end eMOCHA
    /**
     * Classes needed to serialize objects
     */
    public final static String[] SERIALIABLE_CLASSES =
        {
                "org.javarosa.core.model.FormDef", "org.javarosa.core.model.GroupDef",
                "org.javarosa.core.model.QuestionDef", "org.javarosa.core.model.data.DateData",
                "org.javarosa.core.model.data.DateTimeData",
                "org.javarosa.core.model.data.DecimalData",
                "org.javarosa.core.model.data.GeoPointData",
                "org.javarosa.core.model.data.helper.BasicDataPointer",
                "org.javarosa.core.model.data.IntegerData",
                "org.javarosa.core.model.data.MultiPointerAnswerData",
                "org.javarosa.core.model.data.PointerAnswerData",
                "org.javarosa.core.model.data.SelectMultiData",
                "org.javarosa.core.model.data.SelectOneData",
                "org.javarosa.core.model.data.StringData", "org.javarosa.core.model.data.TimeData",
                "org.javarosa.core.services.locale.TableLocaleSource",
                "org.javarosa.xpath.expr.XPathArithExpr", "org.javarosa.xpath.expr.XPathBoolExpr",
                "org.javarosa.xpath.expr.XPathCmpExpr", "org.javarosa.xpath.expr.XPathEqExpr",
                "org.javarosa.xpath.expr.XPathFilterExpr", "org.javarosa.xpath.expr.XPathFuncExpr",
                "org.javarosa.xpath.expr.XPathNumericLiteral",
                "org.javarosa.xpath.expr.XPathNumNegExpr", "org.javarosa.xpath.expr.XPathPathExpr",
                "org.javarosa.xpath.expr.XPathStringLiteral",
                "org.javarosa.xpath.expr.XPathUnionExpr",
                "org.javarosa.xpath.expr.XPathVariableReference"
        };

    FormLoaderListener mStateListener;
    //eMOCHA modified to be able to register our own functions
    private Context context;
    public FormLoaderTask() { }
    public FormLoaderTask(Context c) {
    	this.context = c;
    }
    //end eMOCHA

    protected class FECWrapper {
        FormEntryController controller;


        protected FECWrapper(FormEntryController controller) {
            this.controller = controller;
        }


        protected FormEntryController getController() {
            return controller;
        }


        protected void free() {
            controller = null;
        }
    }

    FECWrapper data;


    /**
     * Initialize {@link FormEntryController} with {@link FormDef} from binary or from XML. If given
     * an instance, it will be used to fill the {@link FormDef}.
     */
    @Override
    protected FECWrapper doInBackground(String... path) {
        FormEntryController fec = null;
        FormDef fd = null;
        FileInputStream fis = null;

        String formPath = path[0];
        //String instancePath = path[1];
        // eMOCHA modified:
        String xmlData = path[1];
        // contains dynamic_data
        String itemSets = (path.length > 2) ? path[2] : null;
        // end eMOCHA
        File formXml = new File(formPath);
        
        // eMOCHA modified: add dynamic data to form <instance>, if any
        String fileName = formXml.getName();
        if (itemSets != null) {	        
	        //1.- get data from intent
			String content = replaceNode(formXml, itemSets, Constants.ODK_DYNAMIC_DATA);
			if (xmlData != null) { //update existent xml data!
				xmlData = replaceNode(xmlData, itemSets, Constants.ODK_DYNAMIC_DATA);
			}
			//2.- replace dynamic_data node
			if (content != null ) {
				formXml = getFileFromString(content);
			}
        }
        File formBin = new File(Environment.getExternalStorageDirectory()
        			+ context.getString(R.string.app_path_base)
        			+ context.getString(R.string.app_odk_path)
        			+ context.getString(R.string.app_odk_cache_path)
        			+ FileUtils.getMd5Hash(formXml) + ".formdef");
        // end eMOCHA
        
        if (formBin.exists()) {
            // if we have binary, deserialize binary
            fd = deserializeFormDef(formBin);
            if (fd == null) {
                return null;
            }
        } else {
            // no binary, read from xml
            try {
                fis = new FileInputStream(formXml);
                fd = XFormUtils.getFormFromInputStream(fis);
                if (fd == null) {
                    return null;
                }
                fd.setEvaluationContext(new EvaluationContext());
                serializeFormDef(fd, formPath);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                if (fd == null) {
                    return null;
                }
            }
        }
        //eMOCHA modified: load always Evaluation Context.
        EvaluationContext ec = new EvaluationContext();
        ec.addFunctionHandler(new CreateRepeatNodeIdFunction(this.context));
        fd.setEvaluationContext(ec);
        //end eMOCHA
        
        // create FormEntryController from formdef
        FormEntryModel fem = new FormEntryModel(fd);
        fec = new FormEntryController(fem);

        // import existing data into formdef
        if (xmlData != null) {
            fd.initialize(false);
            importDataFromXml(xmlData, fec);
        } else {
            fd.initialize(true);
        }
        
        // set paths to /sdcard/odk/forms/formfilename-media/
        // This is a singleton, how do we ensure that we're not doing this
        // multiple times?
        // eMOCHA modified: 
        String mediaPath = fileName.substring(0, fileName.lastIndexOf(".")) + "-media";
        if (ReferenceManager._().getFactories().length == 0) {
            ReferenceManager._().addReferenceFactory(
           		// eMOCHA modified
                new FileReferenceFactory(Environment.getExternalStorageDirectory()
                						+ context.getString(R.string.app_path_base)
                						+ context.getString(R.string.app_odk_path)
                						+ context.getString(R.string.app_odk_forms_path)
                						+ mediaPath));
            	// end eMOCHA
            ReferenceManager._().addRootTranslator(new RootTranslator("jr://images/", "jr://file/"));
            ReferenceManager._().addRootTranslator(new RootTranslator("jr://audio/", "jr://file/"));
        }

        // clean up vars
        fis = null;
        fd = null;
        formBin = null;
        formXml = null;
        formPath = null;
        //instancePath = null;
        
        //eMOCHA modified:
        formXml = new File(Environment.getExternalStorageDirectory()
        		+ context.getString(R.string.app_path_base)
    			+ context.getString(R.string.app_odk_path)
    			+ context.getString(R.string.app_odk_cache_path)
    			+ TMP_STUDY_IDS_FILE);
        
        if (formXml.exists()) {
        	formXml.delete();
        }
        // end eMOCHA
        data = new FECWrapper(fec);
        return data;
    }


    public boolean importData(String filePath, FormEntryController fec) {
        // convert files into a byte array
        byte[] fileBytes = FileUtils.getFileAsBytes(new File(filePath));

        // get the root of the saved and template instances
        TreeElement savedRoot = XFormParser.restoreDataModel(fileBytes, null).getRoot();
        TreeElement templateRoot = fec.getModel().getForm().getInstance().getRoot().deepCopy(true);

        // weak check for matching forms
        if (!savedRoot.getName().equals(templateRoot.getName()) || savedRoot.getMult() != 0) {
            Log.e(t, "Saved form instance does not match template form definition");
            return false;
        } else {
            // populate the data model
            TreeReference tr = TreeReference.rootRef();
            tr.add(templateRoot.getName(), TreeReference.INDEX_UNBOUND);
            templateRoot.populate(savedRoot, fec.getModel().getForm());

            // populated model to current form
            fec.getModel().getForm().getInstance().setRoot(templateRoot);

            // fix any language issues
            // : http://bitbucket.org/javarosa/main/issue/5/itext-n-appearing-in-restored-instances
            if (fec.getModel().getLanguages() != null) {
                fec.getModel().getForm().localeChanged(fec.getModel().getLanguage(),
                    fec.getModel().getForm().getLocalizer());
            }

            return true;

        }
    }

    // eMOCHA: allows to import data from an xml String. replaces importData()
    public boolean importDataFromXml(String xml, FormEntryController fec) {
        // convert xml data into a byte array
        byte[] fileBytes = xml.getBytes();

        // get the root of the saved and template instances
        TreeElement savedRoot = XFormParser.restoreDataModel(fileBytes, null).getRoot();
        TreeElement templateRoot = fec.getModel().getForm().getInstance().getRoot().deepCopy(true);

        // weak check for matching forms
        if (!savedRoot.getName().equals(templateRoot.getName()) || savedRoot.getMult() != 0) {
            Log.e(t, "Saved form instance does not match template form definition");
            return false;
        } else {
            // populate the data model
            TreeReference tr = TreeReference.rootRef();
            tr.add(templateRoot.getName(), TreeReference.INDEX_UNBOUND);
            templateRoot.populate(savedRoot, fec.getModel().getForm());

            // populated model to current form
            fec.getModel().getForm().getInstance().setRoot(templateRoot);

            // fix any language issues
            // : http://bitbucket.org/javarosa/main/issue/5/itext-n-appearing-in-restored-instances
            if (fec.getModel().getLanguages() != null) {
                fec.getModel().getForm().localeChanged(fec.getModel().getLanguage(),
                    fec.getModel().getForm().getLocalizer());
            }
            return true;
        }
    }


    /**
     * Read serialized {@link FormDef} from file and recreate as object.
     * 
     * @param formDef serialized FormDef file
     * @return {@link FormDef} object
     */
    public FormDef deserializeFormDef(File formDef) {

        // TODO: any way to remove reliance on jrsp?

        // need a list of classes that formdef uses
        PrototypeManager.registerPrototypes(SERIALIABLE_CLASSES);
        FileInputStream fis = null;
        FormDef fd = null;
        try {
            // create new form def
            fd = new FormDef();
            fis = new FileInputStream(formDef);
            DataInputStream dis = new DataInputStream(fis);

            // read serialized formdef into new formdef
            fd.readExternal(dis, ExtUtil.defaultPrototypes());
            dis.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DeserializationException e) {
            e.printStackTrace();
        }

        return fd;
    }


    /**
     * Write the FormDef to the file system as a binary blog.
     * 
     * @param filepath path to the form file
     */
    public void serializeFormDef(FormDef fd, String filepath) {
        // if cache folder is missing, create it.
    	if (FileUtils.createFolder(Environment.getExternalStorageDirectory()
    								+ context.getString(R.string.app_path_base)
    								+ context.getString(R.string.app_odk_path)
    								+ context.getString(R.string.app_odk_cache_path))) {    		

            // calculate unique md5 identifier
            String hash = FileUtils.getMd5Hash(new File(filepath));
            File formDef = new File(Environment.getExternalStorageDirectory()
            						+ context.getString(R.string.app_path_base)
									+ context.getString(R.string.app_odk_path)
									+ context.getString(R.string.app_odk_cache_path)
									+ hash 
									+ ".formdef");

            // formdef does not exist, create one.
            if (!formDef.exists()) {
                FileOutputStream fos;
                try {
                    fos = new FileOutputStream(formDef);
                    DataOutputStream dos = new DataOutputStream(fos);
                    fd.writeExternal(dos);
                    dos.flush();
                    dos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    protected void onPostExecute(FECWrapper wrapper) {
        synchronized (this) {
            if (mStateListener != null && wrapper != null)
                mStateListener.loadingComplete(wrapper.getController());
        }
    }

    public void setFormLoaderListener(FormLoaderListener sl) {
        synchronized (this) {
            mStateListener = sl;
        }
    }

    public void destroy() {
       if (data != null) { //eMOCHA modified: avoid NPE
    	data.free();
    	data = null;
       }
    }

    // eMOCHA modified:
	private String replaceNode(File file, String chunk, String nodeName) {
		StringBuilder content = null;
		FileInputStream fs = null;
		try {
			fs = new FileInputStream(file);
			
			byte[] b = new byte[fs.available()];
	        fs.read(b);
	        fs.close ();
	        content = new StringBuilder(new String (b));	        
			if (content != null) {
				int start = content.indexOf("<"+nodeName+">");
				if (start >= 0) { //tag exists 
					String endTag= "</"+nodeName+">";
					int end = content.indexOf(endTag)+endTag.length();
					content.replace(start, end, chunk);
				} else { //tag doesn't exist. insert it before </data> ----> FORCING emocha to have <data> node!!
					Log.w(t,"EMOCHA: couldn't find node: "+nodeName);
					start = content.indexOf("</data>");
					content.insert(start, chunk);
				}
			}
		} catch (FileNotFoundException e) {
			Log.d(t,"EMOCHA: replaceNode() file not found: "+file.getName());
			e.printStackTrace();
			content = null;
		} catch (IOException e) {
			Log.d(t,"EMOCHA: replaceNode() IOException");
			e.printStackTrace();
			content = null;
		}
		
		return (content == null) ? null : content.toString();
	}
	
	private String replaceNode(String data, String chunk, String nodeName) {
		StringBuilder content = null;
        content = new StringBuilder(data);	        
		if (content != null) {
			int start = content.indexOf("<"+nodeName+">");
			if (start >= 0) { //tag exists 
				String endTag= "</"+nodeName+">";
				int end = content.indexOf(endTag)+endTag.length();
				content.replace(start, end, chunk);
			} else { //tag doesn't exist. insert it before </data> ----> FORCING emocha to have <data> node!!
				Log.w(t,"EMOCHA: couldn't find node: "+nodeName);
				start = content.indexOf("</data>");
				content.insert(start, chunk);
			}
		}
		return (content == null) ? null : content.toString();
	}
	
	private File getFileFromString(String content) {
		File file = null;
		FileWriter fw;
		try {
			fw = new FileWriter(Environment.getExternalStorageDirectory()
								+ context.getString(R.string.app_path_base)
								+ context.getString(R.string.app_odk_path)
								+ context.getString(R.string.app_odk_cache_path)
								+ TMP_STUDY_IDS_FILE);
			fw.write(content);
			fw.close();
			
			file = new File(Environment.getExternalStorageDirectory()
							+ context.getString(R.string.app_path_base)
							+ context.getString(R.string.app_odk_path)
							+ context.getString(R.string.app_odk_cache_path)
							+ TMP_STUDY_IDS_FILE);
			
		} catch (IOException e) {
			Log.d(t,"EMOCHA: getFileFromString() IOException");
			e.printStackTrace();
		}
		return file;
	}
	// end eMOCHA
}
