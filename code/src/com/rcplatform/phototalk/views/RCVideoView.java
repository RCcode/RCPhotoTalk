package com.rcplatform.phototalk.views;

import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.Utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.VideoView;

public class RCVideoView extends VideoView {

	public RCVideoView(Context context) {
		super(context);
	}
	public RCVideoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public RCVideoView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT + Utils.getStatusBarHeight(getContext()));
	}
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		return false;
	}
}
