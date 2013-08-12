package com.rcplatform.phototalk.views;

import android.content.Context;
import android.util.AttributeSet;
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
		setMeasuredDimension(getMeasureValue(widthMeasureSpec), getMeasureValue(heightMeasureSpec));
	}
	private int getMeasureValue(int measureSpec){
		int result=0;
		if(MeasureSpec.getMode(measureSpec)==MeasureSpec.EXACTLY) {
			result=MeasureSpec.getSize(measureSpec);
		}
		return result;
	}
}
