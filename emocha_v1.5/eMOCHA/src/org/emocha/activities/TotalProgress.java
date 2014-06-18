package org.emocha.activities;

import org.emocha.midot.R;
import org.emocha.utils.MiDOTUtils;
import org.emocha.utils.ScreenEnvironment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

/** TotalProgress shows the overall progress of entire medication trial period.
 * @author Yao Huang (yao.engineering@gmail.com)
 *
 */
public class TotalProgress extends Activity {
	
	private TextView progressTextView;
	private TextView progressPhaseTextView;
	private TextView progressPerTextView;
	private ImageView emochaLogoImageView;
	private TextView tapTextView;
	private RelativeLayout progressLayout;
	private ProgressBar progressBar;
	private int progressStatus = 0;
	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.total_progress);
		
		progressTextView = (TextView) findViewById(R.id.progressText);
		progressPhaseTextView = (TextView) findViewById(R.id.phaseText);
		progressPerTextView = (TextView) findViewById(R.id.progressPer);
		emochaLogoImageView = (ImageView) findViewById(R.id.emochaLogo);
		tapTextView = (TextView) findViewById(R.id.tapTextView);
		progressLayout = (RelativeLayout) findViewById(R.id.total_progress_layout);
		progressBar = (ProgressBar) findViewById(R.id.progressBar1);
		// To do, progress bar
		
		RelativeLayout.LayoutParams progressBarLayoutParams = new RelativeLayout.
	    		LayoutParams((int)(ScreenEnvironment.width * 0.75), (int)(ScreenEnvironment.height * 0.04766));
	    progressBarLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
	    progressBarLayoutParams.topMargin = (int)(ScreenEnvironment.height * 0.45625);
	    progressBar.setLayoutParams(progressBarLayoutParams);
	    //progressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, Mode.SRC_IN);
	    
	    
	    RelativeLayout.LayoutParams progressLayoutParams = new RelativeLayout.
	    		LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	    progressLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
	    progressLayoutParams.topMargin = (int)(ScreenEnvironment.height * 0.1273);
	    progressTextView.setLayoutParams(progressLayoutParams);
	    progressTextView.setTextSize((int)(ScreenEnvironment.height * 0.078125));
	    
	    RelativeLayout.LayoutParams phaseLayoutParams = new RelativeLayout.
	    		LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	    phaseLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
	    phaseLayoutParams.topMargin = (int)(ScreenEnvironment.height * 0.0805);
	    progressPhaseTextView.setLayoutParams(phaseLayoutParams);
	    progressPhaseTextView.setTextSize((int)(ScreenEnvironment.height * 0.03125));
	    progressPhaseTextView.setTypeface(null, Typeface.BOLD);
	    
	    RelativeLayout.LayoutParams perLayoutParams = new RelativeLayout.
	    		LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	    perLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
	    perLayoutParams.topMargin = (int)(ScreenEnvironment.height * 0.31875);
	    progressPerTextView.setLayoutParams(perLayoutParams);
	    progressPerTextView.setTextSize((int)(ScreenEnvironment.height * 0.0625));
	    
	    RelativeLayout.LayoutParams tapLayoutParams = new RelativeLayout.
	    		LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	    tapLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		tapLayoutParams.topMargin = (int)(ScreenEnvironment.height * 0.75);
		tapTextView.setLayoutParams(tapLayoutParams);
		tapTextView.setTextSize((int)(ScreenEnvironment.height * 0.03125));
	    
	    RelativeLayout.LayoutParams emochaLayoutParams = new RelativeLayout.
	    		LayoutParams((int)(ScreenEnvironment.width * 0.35), (int)(ScreenEnvironment.height * 0.043));
	    emochaLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
	    emochaLayoutParams.topMargin = (int)(ScreenEnvironment.height * 0.85);
	    emochaLogoImageView.setLayoutParams(emochaLayoutParams);
	    
	    Handler handler = new Handler();
	    Runnable runnable = new Runnable() {			
			@Override
			public void run() {
				showProgressBar();
			}
		};
	    handler.postDelayed(runnable, 1000);
	    
	    progressLayout.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {			
				finish();
				MiDOTUtils.VIDEO_FLAG = false;
				Intent intent = new Intent(TotalProgress.this, Initial.class);
				startActivity(intent);
			}
		});
		    
	}
	
	/**
	 * Progress bar animation. Show percentage of the current progress.
	 */
	private void showProgressBar() {
		handler = new Handler();
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (progressStatus < 43) {
					progressStatus += 1;

					handler.post(new Runnable() {
						
						@Override
						public void run() {
							progressBar.setProgress(progressStatus);
							progressPerTextView.setText(progressStatus + "%");
						}
					});
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();	
		
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
    }

	
	
}
