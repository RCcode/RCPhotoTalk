package com.rcplatform.phototalk.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.galhttprequest.LogUtil;

public class VideoRecordProgressView extends View {
	private float currentAngel;
	private Paint mPaint;
	private RectF mRect;
	private int progressPading = 10;
	private static final int PROGRESS_WIDTH = 10;

	public VideoRecordProgressView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public VideoRecordProgressView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public VideoRecordProgressView(Context context) {
		super(context);
		init();
	}

	private void init() {
		mPaint = new Paint();
		mPaint.setStyle(Style.STROKE);
		mPaint.setColor(getResources().getColor(R.color.video_progress));
		mPaint.setAntiAlias(true);
		mPaint.setStrokeWidth(PROGRESS_WIDTH);
	}

	class VideoTimeCountDownAnimation extends Animation {
		private int startAngel;
		private int endAngel;

		public VideoTimeCountDownAnimation(int startAngel, int endAngel, long duration) {
			setDuration(duration);
			this.startAngel = startAngel;
			this.endAngel = endAngel+5;
			setInterpolator(new LinearInterpolator());
		}

		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {
			super.applyTransformation(interpolatedTime, t);

			float currentAngel = startAngel + interpolatedTime * (endAngel - startAngel);
			LogUtil.e(interpolatedTime + "....." + currentAngel);
			VideoRecordProgressView.this.setCurrentAngel(currentAngel);
		}
	}

	public void setCurrentAngel(float currentAngel) {
		this.currentAngel = currentAngel;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mRect == null)
			mRect = new RectF(progressPading + PROGRESS_WIDTH / 2, progressPading + PROGRESS_WIDTH / 2, getMeasuredWidth() - progressPading - PROGRESS_WIDTH
					/ 2, getMeasuredHeight() - progressPading - PROGRESS_WIDTH / 2);
		canvas.drawArc(mRect, -90, currentAngel, false, mPaint);
	}

	public void startAnimation(int startAngel, int endAngel, long duration) {
		Animation animation = new VideoTimeCountDownAnimation(startAngel, endAngel, duration);
		startAnimation(animation);
	}

	public void resetAnimation() {
		clearAnimation();
		this.currentAngel = 0;
		invalidate();
	}
}
