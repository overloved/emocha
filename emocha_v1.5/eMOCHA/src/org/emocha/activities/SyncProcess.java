package org.emocha.activities;

import org.emocha.midot.R;
import org.emocha.utils.ScreenEnvironment;
import org.odk.collect.android.views.SwipeView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/** SyncProcess shows patient the form has been submitted.
 * @author Yao Huang (yao.engineering@gmail.com)
 *
 */
public class SyncProcess extends Activity {
	private ImageView sendImageView;
	private TextView sendTextView;
	private ImageView emochaLogoImageView;
	private FrameLayout frameLayout;
	private TextView swipeTextView;
	private RelativeLayout relativeLayout;
//    private GestureDetector mGestureDetector;    
//    // used to limit forward/backward swipes to one per question
//    private boolean mBeenSwiped;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.send_process);
		
		sendTextView = (TextView) findViewById(R.id.appSendTextView);
		sendImageView = (ImageView) findViewById(R.id.sendImageView);
		emochaLogoImageView = (ImageView) findViewById(R.id.emochaLogo);
//		tapTextView = (TextView) findViewById(R.id.tapTextView);
		relativeLayout = (RelativeLayout) findViewById(R.id.appSendingLayout);
		
		RelativeLayout.LayoutParams submitLayoutParams = new RelativeLayout.
	    		LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	    submitLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
	    submitLayoutParams.topMargin = (int)(ScreenEnvironment.height * 0.103125);
	    sendTextView.setLayoutParams(submitLayoutParams);
	    sendTextView.setTextSize((int)(ScreenEnvironment.height * 0.078125));
	    
	    RelativeLayout.LayoutParams emochaLayoutParams = new RelativeLayout.
	    		LayoutParams((int)(ScreenEnvironment.width * 0.35), (int)(ScreenEnvironment.height * 0.043));
	    emochaLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
	    emochaLayoutParams.topMargin = (int)(ScreenEnvironment.height * 0.85);
	    emochaLogoImageView.setLayoutParams(emochaLayoutParams);
	    
	    RelativeLayout.LayoutParams sendLayoutParams = new RelativeLayout.
	    		LayoutParams((int)(ScreenEnvironment.width * 0.57), (int)(ScreenEnvironment.width * 0.57));
	    sendLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
	    sendLayoutParams.topMargin = (int)(ScreenEnvironment.height * 0.234375);
		sendImageView.setLayoutParams(sendLayoutParams);
				
		SwipeView view = new SwipeView(getApplicationContext());
    	frameLayout = view.addSwipeBarAnimation(relativeLayout, (int)(ScreenEnvironment.height * 0.75));
		relativeLayout.addView(frameLayout);
		swipeTextView = view.addSwipeBarText(relativeLayout, frameLayout, (int)(ScreenEnvironment.height * 0.78));
		relativeLayout.addView(swipeTextView);
		
//		RelativeLayout.LayoutParams tapLayoutParams = new RelativeLayout.
//				LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//		tapLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
//		tapLayoutParams.topMargin = (int)(ScreenEnvironment.height * 0.75);
//		tapTextView.setLayoutParams(tapLayoutParams);
//		tapTextView.setTextSize((int)(ScreenEnvironment.height * 0.03125));	
//		tapTextView.setVisibility(View.INVISIBLE);
//		
//		Handler handler = new Handler();
//		Runnable runnable = new Runnable() {		
//			@Override
//			public void run() {
//				tapTextView.setVisibility(View.VISIBLE);
//				Animation blinkAnimation = AnimationUtils.loadAnimation(SyncProcess.this, R.anim.blink_slow);
//				tapTextView.startAnimation(blinkAnimation);
//			}
//		};
//		handler.postDelayed(runnable, 500);		
//		
		relativeLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent slideAnimActivity = new Intent(SyncProcess.this, TotalProgress.class);
				startActivity(slideAnimActivity);
				// overridePendingTransition(int enterAnim, int exitAnim)
				overridePendingTransition(R.anim.enter_from_right, R.anim.exit_from_left);
			}
		});
	}	
}
