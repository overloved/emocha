package org.emocha.dialogs;


import org.emocha.midot.R;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * DialogFragment shows a dialog window which extends DialogFragment.
 * Use DialogFragment to substitute alert dialog.
 * @author 
 *
 */
public class EmochaDialogFragment extends DialogFragment {
	public static final String DIALOG_TITLE = "title";
	public static final String DIALOG_MESSAGE = "message";
	
	private DialogInterface.OnClickListener mClickListener = null;
	private boolean mCancelable = false;
	
	/**
	 * Create a new instance of EmochaDialogFragment, providing title and message as arguments.
	 * @param title The dialog title.
	 * @param message The dialog content.
	 * @return Instance of DialogFragment.
	 */
	public static EmochaDialogFragment create(String title, String message) {
		EmochaDialogFragment dialog = new EmochaDialogFragment();
        Bundle args = new Bundle();
        args.putString(DIALOG_TITLE, title);
        args.putString(DIALOG_MESSAGE, message);
        dialog.setArguments(args);
        return dialog;
    }
	
	/**allow setting customized listeners on the OK button*/
	public void setOnClickListener(DialogInterface.OnClickListener l) {
			mClickListener = l;
	}
	
	public void setCancelButton(boolean cancel) {
		mCancelable = cancel;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString(EmochaDialogFragment.DIALOG_TITLE);
        String message = getArguments().getString(EmochaDialogFragment.DIALOG_MESSAGE);

        if (mClickListener == null) {
        	mClickListener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //do nothing
                }
            };
        }
        Builder builder = new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.alert_icon)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.ok, mClickListener);
        
        if (mCancelable) {
        	builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //do nothing
                }
        	});
        }
        
        return builder.create();
    }
}
