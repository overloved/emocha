package org.emocha.activities;

import java.io.IOException;

import org.emocha.Constants;
import org.emocha.dialogs.EmochaDialogFragment;
import org.emocha.midot.R;
import org.emocha.utils.ScreenEnvironment;
import org.emocha.views.CameraPreview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/** Video Capture Activity implements video recording.
 * @author Yao Huang (yao.engineering@gmail.com)
 *
 */
public class VideoCaptureActivity extends Activity {
    
	private final String MP4 = ".mp4";
	private Camera mCamera;
    private CameraPreview mCameraPreview;
    private MediaRecorder mmRecorder;
    private String mInstancePath;
    private ImageView recButtonImageView;
    private ImageView whiteCircleImageView; 
    private boolean mRecording = false;
    private FrameLayout mLayout;
    private RelativeLayout.LayoutParams recParams;
	private TextView countTextView3;
	private TextView countTextView2;
	private TextView countTextView1;
	private RelativeLayout overlayLayout;
	private RelativeLayout topLayout;
	private RelativeLayout timerLayout;
	private TextView timerTextView;
	private ImageView recordingFlashImageView;
	private Button switchButton;
	private long timeSwapBuff = 0L;
	private long updatedTime = 0L;
	private long startTime = 0L;
	private long timeInMilliseconds = 0L;
	private Handler timerHandler;
	private Animation blinkAnimation;
	private CameraPosition cameraPosition = CameraPosition.Back;
	private boolean isRecordButtonClickable = true;
     
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.ODK_INSTANCEPATH_KEY, mInstancePath);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_layout); 
        timerHandler = new Handler();
            
        /** Bottom Layout */
        recButtonImageView = (ImageView) findViewById(R.id.videoRedCircle);
        whiteCircleImageView = (ImageView) findViewById(R.id.videoWhiteLine);
        
        recParams = new RelativeLayout.
        		LayoutParams((int)(ScreenEnvironment.width * 0.1458), (int)(ScreenEnvironment.width * 0.1458));
        recParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        recParams.topMargin = (int)(ScreenEnvironment.height * 0.0265625);
        recParams.bottomMargin = (int)(ScreenEnvironment.height * 0.0265625);
        recButtonImageView.setLayoutParams(recParams);
        
        RelativeLayout.LayoutParams whiteParams = new RelativeLayout.
        		LayoutParams((int)(ScreenEnvironment.width * 0.1972), (int)(ScreenEnvironment.width * 0.1972));
        whiteParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        whiteParams.topMargin = (int)(ScreenEnvironment.height * 0.0117);
        whiteParams.bottomMargin = (int)(ScreenEnvironment.height * 0.0117);
        whiteCircleImageView.setLayoutParams(whiteParams);
        
	    countTextView3 = new TextView(this);
  		countTextView3.setText("3");
  		countTextView3.setTypeface(null, Typeface.BOLD);
  		countTextView3.setTextColor(Color.WHITE);  		
  		countTextView3.setVisibility(View.INVISIBLE);
  		
  		countTextView2 = new TextView(this);
  		countTextView2.setText("2");
  		countTextView2.setTypeface(null, Typeface.BOLD);
  		countTextView2.setTextColor(Color.WHITE);  		
  		countTextView2.setVisibility(View.INVISIBLE);
  		
  		countTextView1 = new TextView(this);
  		countTextView1.setText("1");
  		countTextView1.setTypeface(null, Typeface.BOLD);
  		countTextView1.setTextColor(Color.WHITE);  		
  		countTextView1.setVisibility(View.INVISIBLE);

          
        RelativeLayout.LayoutParams count3LayoutParams = new RelativeLayout.
  	    		LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
  	    count3LayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
  	    countTextView3.setLayoutParams(count3LayoutParams);	   
  	    countTextView3.setTextSize((int)(ScreenEnvironment.height * 0.234375));
  	    countTextView3.setShadowLayer((float) 0.3, 3, 3, Color.BLACK);
  	    
  	    RelativeLayout.LayoutParams count2LayoutParams = new RelativeLayout.
	    		LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	    count2LayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
	    count2LayoutParams.topMargin = (int)(ScreenEnvironment.height * 0.265);
	    countTextView2.setLayoutParams(count2LayoutParams);	   
	    countTextView2.setTextSize((int)(ScreenEnvironment.height * 0.234375));
	    countTextView2.setShadowLayer((float) 0.3, 3, 3, Color.BLACK);
	    
	    RelativeLayout.LayoutParams count1LayoutParams = new RelativeLayout.
  	    		LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
  	    count1LayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
  	    count1LayoutParams.topMargin = (int)(ScreenEnvironment.height * 0.53);
  	    countTextView1.setLayoutParams(count1LayoutParams);	   
  	    countTextView1.setTextSize((int)(ScreenEnvironment.height * 0.234375));
  	    countTextView1.setShadowLayer((float) 0.3, 3, 3, Color.BLACK);
        
        if (savedInstanceState != null) {
        	mInstancePath = savedInstanceState.getString(Constants.ODK_INSTANCEPATH_KEY);
        } else {
        	mInstancePath = getIntent().getStringExtra(Constants.ODK_INSTANCEPATH_KEY);
        }        
        //avoid screen rotation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);   
    }
    @Override
    public void onResume() {
    	super.onResume();
    	Context context = getApplicationContext();
    	mCamera = getCamera();
        if (mCamera == null) {
        	EmochaDialogFragment eDialog = EmochaDialogFragment.create("Camera not found", "Camera not found");
        	eDialog.show(getFragmentManager(), Constants.DIALOG_TAG);
        } else {
        	mCameraPreview = new CameraPreview(context, mCamera);
        	
        	mLayout = (FrameLayout)findViewById(R.id.video_view);
        	mLayout.addView(mCameraPreview);
        	
        	/** Top Layout */
            topLayout = (RelativeLayout) findViewById(R.id.topLayout);
            FrameLayout.LayoutParams topLayoutParams = new FrameLayout.
            		LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, (int)(ScreenEnvironment.height * 0.059375));
            topLayout.setLayoutParams(topLayoutParams);
            topLayout.bringToFront();
            
            /** Timer */
            timerLayout = (RelativeLayout) findViewById(R.id.timerLayout);
            RelativeLayout.LayoutParams timerRelativeLayoutParams = new RelativeLayout.
            		LayoutParams((int)(ScreenEnvironment.height * 0.3125), ViewGroup.LayoutParams.WRAP_CONTENT);
            timerRelativeLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            timerLayout.setLayoutParams(timerRelativeLayoutParams);
            
            /** Switch Camera Button */
            switchButton = (Button) findViewById(R.id.switchCamera);
            RelativeLayout.LayoutParams switchLayoutParams = new RelativeLayout.
            		LayoutParams((int)(ScreenEnvironment.height * 0.0534375), (int)(ScreenEnvironment.height * 0.0534375));
            switchLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            switchLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            switchLayoutParams.rightMargin = (int)(ScreenEnvironment.height * 0.015625);
            switchButton.setLayoutParams(switchLayoutParams);
            
            timerTextView = (TextView) findViewById(R.id.timerText);
            RelativeLayout.LayoutParams timerLayoutParams = new RelativeLayout.
            		LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            timerLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            timerTextView.setLayoutParams(timerLayoutParams);
            timerTextView.setTextSize((int)(ScreenEnvironment.height * 0.03125));
            
            /** Flash Recording Image */
            recordingFlashImageView = (ImageView) findViewById(R.id.recordingFlashImageView);
            RelativeLayout.LayoutParams recordingFlashLayoutParams = new RelativeLayout.
            		LayoutParams((int)(ScreenEnvironment.height * 0.015625), (int)(ScreenEnvironment.height * 0.015625));
        	recordingFlashLayoutParams.addRule(RelativeLayout.LEFT_OF, timerTextView.getId());
        	recordingFlashLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        	recordingFlashLayoutParams.rightMargin = (int)(ScreenEnvironment.height * 0.015625);
        	recordingFlashImageView.setLayoutParams(recordingFlashLayoutParams);
        	        	        	
        	switchButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					int cameraCount = 0;
					CameraInfo cameraInfo = new CameraInfo();
					cameraCount = Camera.getNumberOfCameras();
									
					for (int i = 0; i < cameraCount; i++) {
						Camera.getCameraInfo(i, cameraInfo);
						if (cameraPosition == CameraPosition.Front) {
							if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
								mCamera.stopPreview();
								releaseCamera();
								mCamera = Camera.open(i);
								getHolder();
								mCamera.startPreview();
								cameraPosition = CameraPosition.Back;
								break;
							} 
						} else {
							if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
								mCamera.stopPreview();
								releaseCamera();
								mCamera = Camera.open(i);
								getHolder();
								mCamera.startPreview();
								cameraPosition = CameraPosition.Front;
								break;
							}
						}
					}	
				}
			});
         
        	recButtonImageView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {					
					if(mRecording){
						Log.e("time", String.valueOf(updatedTime));
						if (updatedTime > 1000 && isRecordButtonClickable == true) {
							isRecordButtonClickable = false;
			    			mmRecorder.stop();  // stop the recording	
							stopTimer();
							recordingFlashImageView.clearAnimation();
							switchButton.setEnabled(true);
			    			releaseMediaRecorder(); // release the MediaRecorder object		
			    			topLayout.setVisibility(View.INVISIBLE);
			    			//set video path
			    			Intent data = getIntent();
			    			data.putExtra(Constants.VIDEOS_STRING, mInstancePath);
			    			setResult(RESULT_OK, data);
							finish();
						}
		    		} else {	    			
		    			releaseCamera(); //free before using	    			
		    			if(initMediaRecorder()){

							overlayLayout = (RelativeLayout) findViewById(R.id.video_overlay);
		    				overlayLayout.setVisibility(View.VISIBLE);
		    				overlayLayout.bringToFront();
		    				overlayLayout.addView(countTextView3);
		    				overlayLayout.addView(countTextView2);
		    				overlayLayout.addView(countTextView1);
														    				
							countTextView3.setVisibility(View.VISIBLE);
		    	        	countTextView2.setVisibility(View.VISIBLE);		    	        	
		    	        	countTextView1.setVisibility(View.VISIBLE);
		    	        	countTextView2.setAlpha((float) 0.4);
		    	        	countTextView1.setAlpha((float) 0.4);
		    	        	showCountDown(); 								
							recButtonImageView.setEnabled(false);
		    	        								
		    				
		    			} else {
		    				releaseMediaRecorder();
		    			}
		    			switchButton.setEnabled(false);
		    		}
				}
			});
        }
    }
    
    private enum CameraPosition {
    	Front, Back
    }
    
    /**
     * Show count down number before starting video recording.
     */
    public void showCountDown() {
    	  	          	    
  	    Handler handler1 = new Handler();
  		Runnable runnable1 = new Runnable() {		
  			@Override
  			public void run() {
  				countTextView3.setAlpha((float) 0.4);
  				countTextView2.setAlpha(1);
  			}
  		};
  		handler1.postDelayed(runnable1, 1000);
  		
  		Runnable runnable2 = new Runnable() {		
  			@Override
  			public void run() {
  				countTextView2.setAlpha((float) 0.4);
  				countTextView1.setAlpha(1);
  			}
  		};
  		handler1.postDelayed(runnable2, 2000);
  		
  		Runnable runnable3 = new Runnable() {		
  			@Override
  			public void run() {
  				
  				Animation bottomUpAnimation = AnimationUtils.
						loadAnimation(VideoCaptureActivity.this, R.anim.bottom_up);
				overlayLayout.startAnimation(bottomUpAnimation);
  			}
  		};
  		handler1.postDelayed(runnable3, 3000);
  		
  		Runnable runnable4 = new Runnable() {
			
			@Override
			public void run() {
				
				overlayLayout.setVisibility(View.INVISIBLE);
  				startVideoRecording();
  				blinkAnimation = AnimationUtils.loadAnimation(VideoCaptureActivity.this, R.anim.blink);
				recordingFlashImageView.startAnimation(blinkAnimation);
  				recButtonImageView.setEnabled(true);
			}
		};
		handler1.postDelayed(runnable4, 4000);
	}
    
    private void startVideoRecording() {
    	recButtonImageView.setBackgroundResource(R.drawable.app_video_record_redcircle);
		recButtonImageView.setLayoutParams(recParams);
		recordingAnimation();	
		startTimer();
		mmRecorder.start();
		mRecording = true;	
    }
    
    private void startTimer() {
		startTime = SystemClock.uptimeMillis();	
		timerHandler.postDelayed(timerRunnable, 0);
    }
    
    private void stopTimer() {
    	timeSwapBuff += timeInMilliseconds;
    	timerHandler.removeCallbacks(timerRunnable);
    }
    
    private Runnable timerRunnable = new Runnable() {
		
		@Override
		public void run() {
			timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
			updatedTime = timeSwapBuff + timeInMilliseconds;
			
			int secs = (int) updatedTime / 1000;
			int mins = secs / 60;
			int hours = mins / 60;
			secs = secs % 60;
			mins = mins % 60;
			timerTextView.setText("" + String.format("%02d", hours) + ":" + String.format("%02d", mins) + ":" + String.format("%02d", secs));
			timerHandler.postDelayed(this,  0);			
		}
	};
    
    /**
     * Animation shows state that video is recording.
     */
    private void recordingAnimation() {
    	Animation mAnimation = AnimationUtils.loadAnimation(this, R.anim.tween);
		mAnimation.setFillAfter(true);
		mAnimation.setFillBefore(false);
		recButtonImageView.setAnimation(mAnimation);
		mAnimation.start();
	}

	/**
	 * Release the camera for use.
	 */
	private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }
	
	
    /**
     * Release recorder object for later use.
     */
    private void releaseMediaRecorder() {
    	if (mmRecorder != null) {
    		mmRecorder.reset();   //clear recorder configuration
    		mmRecorder.release(); //release the recorder object
    		mmRecorder = null;
    		mRecording = false;
    		//mRecButton.setChecked(false); //show the 'capture' text back   		
    		mCamera.lock();       //lock camera for later use
    	}
    }

    /** Initialize media recorder.
     * @return True - initialized.
     */
    public boolean initMediaRecorder() {
    	//prepare camera
    	mCamera = getCamera();
    	
//    	ConnectivityManager conn = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//    	State mobile = conn.getNetworkInfo(0).getState();
//    	State wifi = conn.getNetworkInfo(1).getState();
//    	
    	CamcorderProfile profile = null;
//    	
//    	if (mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING) {
//    		profile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
//    	} else if (wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING) {
    		profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
//    	} else {
//    		profile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
//    	}
    	   	
    	Camera.Parameters parameters = mCamera.getParameters();
		parameters.setPreviewSize(profile.videoFrameWidth,profile.videoFrameHeight);
		mCamera.setParameters(parameters);
		
//		try {
//			mCamera.setPreviewDisplay(mCameraPreview.getHolder());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		mCamera.startPreview();
		
		//prepare media recorder
    	boolean success = true;
    	if (mmRecorder == null) {
    		mmRecorder = new MediaRecorder();
    		
    		// Step 1: Unlock and set camera to MediaRecorder
    		mCamera.unlock();

            mmRecorder.setPreviewDisplay(mCameraPreview.getHolder().getSurface());
    		mmRecorder.setCamera(mCamera);
    		
    	    // Step 2: Set sources
    		mmRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
    		mmRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
    		mmRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
    		mmRecorder.setVideoEncodingBitRate(profile.videoBitRate);
    		mmRecorder.setVideoFrameRate(profile.videoFrameRate);
    		mmRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
    		mmRecorder.setAudioChannels(profile.audioChannels);
    		mmRecorder.setAudioEncoder(profile.audioCodec);
    		mmRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
    		
    		mInstancePath = mInstancePath + System.currentTimeMillis() + MP4;
            mmRecorder.setOutputFile(mInstancePath);

            try {
                mmRecorder.prepare();
            }
            catch (IllegalStateException e) {
            	success = false;
            	releaseMediaRecorder();
                e.printStackTrace();
            } catch (IOException e) {
				e.printStackTrace();
				releaseMediaRecorder();
				success = false;
			}
    	}
    	return success; 
	}
    
    public void getHolder() {   	
		try {
			mCamera.setPreviewDisplay(mCameraPreview.getHolder());									
		} catch (IOException e) {
			e.printStackTrace();
		}
		mCamera.setDisplayOrientation(90);
    }

	/** Get access to either the back-facing or front-facing camera.
	 * @return Camera object
	 */
	public Camera getCamera() {
		if (mCamera == null) {
			try {
//			if (Camera.getNumberOfCameras() > Constants.ONE) {
//				mCamera = Camera.open(CameraInfo.CAMERA_FACING_FRONT);
//			} else { //assume device has at least 1 camera..
//				mCamera = Camera.open(CameraInfo.CAMERA_FACING_BACK);
//			}
				
			if (cameraPosition == CameraPosition.Back) {
				mCamera = Camera.open(CameraInfo.CAMERA_FACING_FRONT);
			} else { //assume device has at least 1 camera..
				mCamera = Camera.open(CameraInfo.CAMERA_FACING_BACK);
			}

			//set orientation: force portrait
			mCamera.setDisplayOrientation(90);
			} catch (Exception e) {
				Log.e(Constants.LOG_TAG, "failed to open Camera");
		        e.printStackTrace();
			}
		}
		return mCamera;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		releaseMediaRecorder();
		releaseCamera();
		//remove camera from the view!
		mLayout.removeView(mCameraPreview);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
    }
}
