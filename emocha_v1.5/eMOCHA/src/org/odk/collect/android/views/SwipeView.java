package org.odk.collect.android.views;

import org.emocha.midot.R;
import org.emocha.utils.ScreenEnvironment;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SwipeView extends RelativeLayout {
		
	public SwipeView(Context context) {
		super(context);
	}

	public FrameLayout addSwipeBarAnimation(View mView, int topMargin) {
    	/** swipe bar */
        
        FrameLayout frameLayout = new FrameLayout(getContext());
	    RelativeLayout.LayoutParams frameLayoutParams = new RelativeLayout.
	    		LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	    frameLayoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, mView.getId());
	    //frameLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
	    //frameLayoutParams.topMargin = (int)(ScreenEnvironment.height * 0.85);
	    frameLayoutParams.topMargin = topMargin;
	    frameLayout.setLayoutParams(frameLayoutParams);
	    
	    ImageView slideLayerImageView3 = new ImageView(getContext());
	    slideLayerImageView3.setBackgroundResource(R.drawable.swipe3);
	    ImageView slideLayerImageView2 = new ImageView(getContext());
		slideLayerImageView2.setBackgroundResource(R.drawable.swipe2_animate);
		ImageView slideLayerImageView1 = new ImageView(getContext());
		slideLayerImageView1.setBackgroundResource(R.drawable.swipe1);
	    
	    FrameLayout.LayoutParams slideFrameLayoutParams = new FrameLayout.
				LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, (int)(ScreenEnvironment.height * 0.015625));
		slideFrameLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
		slideLayerImageView3.setLayoutParams(slideFrameLayoutParams);
		slideLayerImageView1.setLayoutParams(slideFrameLayoutParams);
		
		FrameLayout.LayoutParams slideLayoutParams = new FrameLayout.
		LayoutParams((int)(ScreenEnvironment.width * 0.3515625), (int)(ScreenEnvironment.height * 0.015625));
		slideLayoutParams.gravity = Gravity.RIGHT;
		slideLayerImageView2.setLayoutParams(slideLayoutParams);
				
		TextView slideTextView = new TextView(getContext());
		RelativeLayout.LayoutParams slideTextLayoutParams = new RelativeLayout.
				LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		slideTextLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		slideTextLayoutParams.topMargin = (int)(ScreenEnvironment.height * 0.88);
		slideTextLayoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, frameLayout.getId());
		slideTextLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		slideTextView.setLayoutParams(slideTextLayoutParams);
		slideTextView.setText(getResources().getText(R.string.swipe_info));
		slideTextView.setTextColor(getResources().getColor(R.color.white));
		
		frameLayout.addView(slideLayerImageView3);
		frameLayout.addView(slideLayerImageView2);
		frameLayout.addView(slideLayerImageView1);
			
		Animation animation = new TranslateAnimation(88.125f, -300.0f, 0.0f, 0.0f);
		animation.setDuration(2000);
		animation.setRepeatCount(Animation.INFINITE);
		slideLayerImageView2.startAnimation(animation);
		
		return frameLayout;
	}
	
	public TextView addSwipeBarText(View mView, FrameLayout frameLayout, int topMargin) {
		TextView slideTextView = new TextView(getContext());
		RelativeLayout.LayoutParams slideTextLayoutParams = new RelativeLayout.
				LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		slideTextLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		slideTextLayoutParams.topMargin = topMargin;
		slideTextLayoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, frameLayout.getId());
		slideTextView.setLayoutParams(slideTextLayoutParams);
		slideTextView.setText(getResources().getText(R.string.swipe_info));
		slideTextView.setTextColor(getResources().getColor(R.color.white));
		slideTextView.setTextSize((int)(ScreenEnvironment.height * 0.0234375));
		return  slideTextView;
	}
}
