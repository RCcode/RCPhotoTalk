package com.rcplatform.phototalk.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.utils.Constants;

public class HighLightView extends View {

	private Rect mBlackRectTop;

	private Rect mBlackRectBottom;

	private Rect mBlackRectLeft;

	private Rect mBlackRectRight;

	private Rect mTransportRect;

	private RectF mMinBitmapRect;

	private Paint mPaint;

	private int width;

	private int height;

	private int boaderWidth = 2;

	private MoveImage mBitmap;

	float minScaleR;

	static final float MAX_SCALE = 2f;

	static final int NONE = 0;

	static final int DRAG = 1;

	static final int ZOOM = 2;

	int mode = NONE;

	PointF prev = new PointF();

	PointF mid = new PointF();

	private RectF bitmapRect;

	float dist = 1f;

	private Bitmap mBlackBitmap;

	private Bitmap mCanvasBitmap;

	private Canvas mCanvas;

	private int cropWidth;

	private int cropHeight;

	public HighLightView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();

	}

	private void init() {
		mBlackBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cut_image_background);
		mPaint = new Paint();
		width = Constants.SCREEN_WIDTH;
		height = Constants.SCREEN_HEIGHT;
		if (width == 0)
			width = 480;
		if (height == 0)
			height = 800;

	}

	private void initCanvas() {
		mCanvasBitmap = Bitmap.createBitmap(width, height, Config.RGB_565);
		mCanvas = new Canvas(mCanvasBitmap);
	}

	private void initBackground() {
		int transX = 0;
		int transW = 0;
		int transH = 0;
		int transY = 0;
		if (cropWidth >= cropHeight) {
			transX = 0;
			transW = width;
			transH = (int) (transW * (cropHeight / (float) cropWidth));
			transY = (height - transH) / 2;
		} else {
			// transY = height * SCALE_MARGIN / SCALE_FULL;
			// transH = height * (SCALE_FULL - SCALE_MARGIN * 2) / SCALE_FULL;
			// transW = transH * cropWidth / cropHeight;
			// transX = (width - transW) / 2;
		}

		mTransportRect = new Rect(transX, transY, transX + transW, transY + transH);
		mBlackRectTop = new Rect(0, 0, width, transY);
		mBlackRectBottom = new Rect(0, height - transY, width, height);
		mBlackRectLeft = new Rect(0, transY, transX, height - transY);
		mBlackRectRight = new Rect(width - transX, transY, width, height - transY);
		invalidate();
	}

	public void setBitmap(Bitmap bitmap) {
		mBitmap = new MoveImage();
		mBitmap.setBitmap(bitmap);
		bitmapRect = new RectF(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
		initCanvas();
		center();
		invalidate();

	}

	public void setCropRect(int width, int height) {
		this.cropWidth = width;
		this.cropHeight = height;
		initBackground();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mCanvasBitmap != null) {
			mCanvas.drawColor(Color.WHITE);
			if (mBitmap != null)
				drawBitmap(mCanvas);
			drawBoarder(mCanvas);
			canvas.drawBitmap(mCanvasBitmap, 0, 0, mPaint);
		}
	}

	private void drawBitmap(Canvas canvas) {
		Log.i(VIEW_LOG_TAG, bitmapRect.left + ".." + bitmapRect.top);
		canvas.drawBitmap(mBitmap.getBitmap(), null, bitmapRect, mPaint);
	}

	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	private boolean isTwoPointAllInBitmap(MotionEvent event) {
		float point1X = event.getX(0);
		float point1Y = event.getY(0);
		float point2X = event.getX(1);
		float point2Y = event.getY(1);

		return point1X > bitmapRect.left && point2X > bitmapRect.left && point1X < bitmapRect.right && point2X < bitmapRect.right && point1Y > bitmapRect.top
				&& point2Y > bitmapRect.top && point1Y < bitmapRect.bottom && point2Y < bitmapRect.bottom;
	}

	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	private boolean isTouchPointOnBitmap(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		return x > bitmapRect.left && x < bitmapRect.right && y > bitmapRect.top && y < bitmapRect.bottom;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			if (!isTouchPointOnBitmap(event))
				break;
			prev.set(event.getX(), event.getY());
			mode = DRAG;
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			dist = spacing(event);
			if (spacing(event) > 10f && isTwoPointAllInBitmap(event)) {
				midPoint(mid, event);
				mode = ZOOM;
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			// CheckView();
			mode = NONE;
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode == DRAG) {
				float moveX = event.getX() - prev.x;
				float moveY = event.getY() - prev.y;
				moveBitmap(getLandScapeMoveSize(moveX), getportraitMoveSize(moveY));
				prev.set(event.getX(), event.getY());
			} else if (mode == ZOOM) {
				float newDist = spacing(event);
				if (newDist > 10f) {
					float tScale = newDist / dist;
					changeScale(tScale - 1);
					dist = newDist;
				}
			}
			break;
		}
		invalidate();
		return true;
	}

	private float getLandScapeMoveSize(float moveX) {
		float size = 0;
		if (isWidthOutBoarder(moveX)) {
			if ((bitmapRect.left + moveX) >= mTransportRect.left) {
				size = mTransportRect.left - bitmapRect.left;
			} else if ((bitmapRect.right + moveX) <= mTransportRect.right) {
				size = mTransportRect.right - bitmapRect.right;
			} else {
				size = 0;
			}
		} else {
			size = moveX / 2;
		}
		return size;
	}

	private float getportraitMoveSize(float moveY) {
		float size = 0;
		if (isHeightOutBoarder(moveY)) {
			if ((bitmapRect.top + moveY) >= mTransportRect.top) {
				size = mTransportRect.top - bitmapRect.top;
			} else if ((bitmapRect.bottom + moveY) <= mTransportRect.bottom) {
				size = mTransportRect.bottom - bitmapRect.bottom;
			} else {
				size = 0;
			}
		} else {
			size = moveY / 2;
		}
		return size;
	}

	private boolean isWidthOutBoarder(float moveX) {
		return !(bitmapRect.left + moveX <= mTransportRect.left && bitmapRect.right + moveX >= mTransportRect.right);
	}

	private boolean isHeightOutBoarder(float moveY) {
		return !(bitmapRect.top + moveY <= mTransportRect.top && bitmapRect.bottom + moveY >= mTransportRect.bottom);
	}

	public Bitmap getBitmapHighLight() {
		Bitmap temp = Bitmap.createBitmap(mCanvasBitmap, mTransportRect.left, mTransportRect.top, mTransportRect.width(), mTransportRect.height());
		Bitmap result = Bitmap.createScaledBitmap(temp, cropWidth, cropHeight, false);
		temp.recycle();
		temp = null;
		System.gc();
		return result;
	}

	/**
	 */
	protected void center() {
		fitHighLight();
		mMinBitmapRect = new RectF(bitmapRect);
		moveToCentre();
	}

	private void moveToCentre() {
		float width = bitmapRect.width();
		float height = bitmapRect.height();
		bitmapRect.left = mTransportRect.left;
		bitmapRect.right = bitmapRect.left + width;
		bitmapRect.top = mTransportRect.top;
		bitmapRect.bottom = bitmapRect.top + height;
	}

	private void fitHighLight() {
		float scale = getMaxScale();
		if (scale > 0) {
			int widthChage = (int) (bitmapRect.width() * (scale - 1));
			int heightChange = (int) (bitmapRect.height() * (scale - 1));
			bitmapRect.right = bitmapRect.right + widthChage;
			bitmapRect.bottom = bitmapRect.bottom + heightChange;
		}
	}

	private float getMaxScale() {
		float widthScale = mTransportRect.width() / bitmapRect.width();
		float heightScale = mTransportRect.height() / bitmapRect.height();
		if (widthScale > heightScale)
			return widthScale;
		return heightScale;
	}

	private void changeScale(float scale) {
		float widthChange = bitmapRect.width() * scale;
		float heightChange = bitmapRect.height() * scale;
		if (mMinBitmapRect != null) {
			if (isBitmapSmallEnouth(widthChange, heightChange))
				return;
		}
		zoom(widthChange, heightChange);
	}

	private void zoom(float widthChange, float heightChange) {
		if (bitmapRect.left >= mTransportRect.left) {
			bitmapRect.right = bitmapRect.right + widthChange;
		} else if (bitmapRect.right <= mTransportRect.right) {
			bitmapRect.left = bitmapRect.left - widthChange;
		} else {
			float[] landscapeZoom = getLandscapeZoom(widthChange);
			bitmapRect.left = bitmapRect.left - landscapeZoom[0];
			bitmapRect.right = bitmapRect.right + landscapeZoom[1];
		}
		if (bitmapRect.top >= mTransportRect.top) {
			bitmapRect.bottom = bitmapRect.bottom + heightChange;
		} else if (bitmapRect.bottom <= mTransportRect.bottom) {
			bitmapRect.top = bitmapRect.top - heightChange;
		} else {
			float[] portraitZoom = getPortraitZoom(heightChange);
			bitmapRect.top = bitmapRect.top - portraitZoom[0];
			bitmapRect.bottom = bitmapRect.bottom + portraitZoom[1];
		}
	}

	private float[] getLandscapeZoom(float widthChange) {
		float[] scapeZoom = new float[2];
		if (Math.abs((bitmapRect.left - mTransportRect.left)) <= Math.abs(widthChange / 2) && widthChange < 0) {
			scapeZoom[0] = bitmapRect.left - mTransportRect.left;
			scapeZoom[1] = widthChange - scapeZoom[0];
		} else if (Math.abs(mTransportRect.right - bitmapRect.right) <= Math.abs(widthChange / 2) && widthChange < 0) {
			scapeZoom[1] = mTransportRect.right - bitmapRect.right;
			scapeZoom[0] = widthChange - scapeZoom[1];
		} else {
			scapeZoom[0] = widthChange / 2;
			scapeZoom[1] = widthChange / 2;
		}
		return scapeZoom;
	}

	private float[] getPortraitZoom(float heightChnage) {
		float[] portraitZoom = new float[2];
		if (Math.abs(mTransportRect.top - bitmapRect.top) <= Math.abs(heightChnage / 2) && heightChnage < 0) {
			portraitZoom[0] = bitmapRect.top - mTransportRect.top;
			portraitZoom[1] = heightChnage - portraitZoom[0];
		} else if (Math.abs(bitmapRect.bottom - mTransportRect.bottom) <= Math.abs(heightChnage / 2) && heightChnage < 0) {
			portraitZoom[1] = mTransportRect.bottom - bitmapRect.bottom;
			portraitZoom[0] = heightChnage - portraitZoom[1];
		} else {
			portraitZoom[0] = heightChnage / 2;
			portraitZoom[1] = heightChnage / 2;
		}
		return portraitZoom;
	}

	private boolean isBitmapSmallEnouth(float widthChange, float heightChange) {
		return (widthChange + bitmapRect.width()) < mMinBitmapRect.width();
	}

	private void drawBoarder(Canvas canvas) {

		canvas.drawBitmap(mBlackBitmap, null, mBlackRectTop, mPaint);
		canvas.drawBitmap(mBlackBitmap, null, mBlackRectBottom, mPaint);
		canvas.drawBitmap(mBlackBitmap, null, mBlackRectLeft, mPaint);
		canvas.drawBitmap(mBlackBitmap, null, mBlackRectRight, mPaint);
		mPaint.setColor(getResources().getColor(R.color.cut_image_boarder));
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(boaderWidth);
	}

	class MoveImage {

		private Bitmap bitmap;

		private int toLeft;

		private int toTop;

		public int getWidth() {
			return bitmap.getWidth();
		}

		public int getHeight() {
			return bitmap.getHeight();
		}

		public Bitmap getBitmap() {
			return bitmap;
		}

		public void setBitmap(Bitmap bitmap) {
			this.bitmap = bitmap;
		}

		public int getToLeft() {
			return toLeft;
		}

		public void setToLeft(int toLeft) {
			this.toLeft = toLeft;
		}

		public int getToTop() {
			return toTop;
		}

		public void setToTop(int toTop) {
			this.toTop = toTop;
		}

	}

	public void recyle() {
		mBlackBitmap.recycle();
		if (mCanvasBitmap != null && !mCanvasBitmap.isRecycled())
			mCanvasBitmap.recycle();
		if (mBitmap != null && !mBitmap.getBitmap().isRecycled())
			mBitmap.getBitmap().recycle();
		mBitmap = null;
		mCanvasBitmap = null;
		mCanvas = null;
		bitmapRect = null;
		mMinBitmapRect = null;
		System.gc();
	}

	private void moveBitmap(float moveX, float moveY) {
		bitmapRect.left = bitmapRect.left + moveX;
		bitmapRect.right = bitmapRect.right + moveX;
		bitmapRect.top = bitmapRect.top + moveY;
		bitmapRect.bottom = bitmapRect.bottom + moveY;
	}
}
