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
import com.rcplatform.phototalk.utils.Contract;

public class HighLightView extends View {

	private Rect mBlackRectTop;

	private Rect mBlackRectBottom;

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

	// 边界逻辑比例值
	private final int SCALE_MARGIN = 1;

	// 屏幕逻辑宽
	private final int SCALE_FULL = 16;

	public HighLightView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();

	}

	private void init() {
		mBlackBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cut_image_background);
		mPaint = new Paint();
		width = Contract.SCREEN_WIDTH;
		height = Contract.SCREEN_HEIGHT;

	}

	private void initCanvas() {
		mCanvasBitmap = Bitmap.createBitmap(width, height, Config.RGB_565);
		mCanvas = new Canvas(mCanvasBitmap);
	}

	private void initBackground() {
		// TODO Auto-generated method stub
		int transX = 0;
		int transW = 0;
		int transH = 0;
		int transY = 0;
		if (cropWidth >= cropHeight) {
			transX = width * SCALE_MARGIN / SCALE_FULL;
			transW = width * (SCALE_FULL - SCALE_MARGIN * 2) / SCALE_FULL;
			transH = transW * cropHeight / cropWidth;
			transY = (height - transH) / 2;
		} else {
			transY = height * SCALE_MARGIN / SCALE_FULL;
			transH = height * (SCALE_FULL - SCALE_MARGIN * 2) / SCALE_FULL;
			transW = transH * cropWidth / cropHeight;
			transX = (width - transW) / 2;
		}

		mTransportRect = new Rect(transX, transY, transW, transH);
		mBlackRectTop = new Rect(0, 0, width, transY);
		mBlackRectBottom = new Rect(0, height - transH - transY, width, transY);
		invalidate();
	}

	public void setBitmap(Bitmap bitmap) {
		mBitmap = new MoveImage();
		mBitmap.setBitmap(bitmap);
		bitmapRect = new RectF(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
		initCanvas();
		minZoom();
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

		return point1X > bitmapRect.left && point2X > bitmapRect.left && point1X < bitmapRect.right && point2X < bitmapRect.right
		        && point1Y > bitmapRect.top && point2Y > bitmapRect.top && point1Y < bitmapRect.bottom && point2Y < bitmapRect.bottom;
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
				CheckView();
				mode = NONE;
				break;
			case MotionEvent.ACTION_MOVE:
				if (mode == DRAG) {
					int moveX = (int) (event.getX() - prev.x);
					int moveY = (int) (event.getY() - prev.y);
					moveBitmap(moveX, moveY);
					prev.set(event.getX(), event.getY());
				} else if (mode == ZOOM) {
					float newDist = spacing(event);
					if (newDist > 10f) {
						float tScale = newDist / dist;
						Log.i(VIEW_LOG_TAG, "scale size is " + tScale);
						changeScale(tScale - 1);
						dist = newDist;
					}
				}
				break;
		}
		invalidate();
		return true;
	}

	private void CheckView() {
		if ((isWidthMin() || isHeightMin())
		        && (bitmapRect.left < mTransportRect.left || bitmapRect.top < mTransportRect.top || bitmapRect.right > mTransportRect.right || bitmapRect.bottom > mTransportRect.bottom)) {
			rebackToCentre();
		}
	}

	private void center() {
		center(true, true);
	}

	private boolean isWidthMin() {
		return bitmapRect.width() <= mMinBitmapRect.width() || Math.abs(bitmapRect.width() - mMinBitmapRect.width()) < 10;
	}

	private boolean isHeightMin() {
		return bitmapRect.height() <= mMinBitmapRect.height() || Math.abs(bitmapRect.height() - mMinBitmapRect.height()) < 10;
	}

	public Bitmap getBitmapHighLight() {
		Bitmap bitmap = Bitmap.createBitmap(mCanvasBitmap, mTransportRect.left, mTransportRect.top, mTransportRect.width(), mTransportRect.height());
		return bitmap;
	}

	/**
	 */
	protected void center(boolean horizontal, boolean vertical) {
		float height = bitmapRect.height();
		float width = bitmapRect.width();
		float deltaX = 0, deltaY = 0;
		if (vertical) {
			int screenHeight = this.height;
			if (height < screenHeight) {
				deltaY = (screenHeight - height) / 2 - bitmapRect.top;
			} else if (bitmapRect.top > 0) {
				deltaY = -bitmapRect.top;
			} else if (bitmapRect.bottom < screenHeight) {
				deltaY = this.height - bitmapRect.bottom;
			}
		}

		if (horizontal) {
			int screenWidth = this.width;
			if (width < screenWidth) {
				deltaX = (screenWidth - width) / 2 - bitmapRect.left;
			} else if (bitmapRect.left > 0) {
				deltaX = -bitmapRect.left;
			} else if (bitmapRect.right < screenWidth) {
				deltaX = screenWidth - bitmapRect.right;
			}
		}
		bitmapRect.left = deltaX;
		bitmapRect.right = bitmapRect.right + deltaX;
		bitmapRect.top = deltaY;
		bitmapRect.bottom = bitmapRect.bottom + deltaY;
		fitHighLight();
		mMinBitmapRect = new RectF(bitmapRect);
	}

	private void rebackToCentre() {
		float deltaX = 0, deltaY = 0;
		if (bitmapRect.left < mTransportRect.left) {
			deltaX = mTransportRect.left - bitmapRect.left;
		} else if (bitmapRect.right > mTransportRect.right) {
			deltaX = mTransportRect.right - bitmapRect.right;
		}
		if (bitmapRect.top < mTransportRect.top)
			deltaY = mTransportRect.top - bitmapRect.top;
		else if (bitmapRect.bottom > mTransportRect.bottom)
			deltaY = mTransportRect.bottom - bitmapRect.bottom;
		bitmapRect.left = bitmapRect.left + deltaX;
		bitmapRect.right = bitmapRect.right + deltaX;
		bitmapRect.top = bitmapRect.top + deltaY;
		bitmapRect.bottom = bitmapRect.bottom + deltaY;
	}

	private void fitHighLight() {
		if (bitmapRect.width() > bitmapRect.height()) {
			float scale = mTransportRect.width() / bitmapRect.width();
			changeScale(scale - 1);
		} else {
			float scale = mTransportRect.height() / bitmapRect.height();
			changeScale(scale - 1);
		}
	}

	private void changeScale(float scale) {
		float widthChange = bitmapRect.width() * scale;
		float heightChange = bitmapRect.height() * scale;
		if (mMinBitmapRect != null) {
			if (isBitmapBigEnough(widthChange, heightChange) || isBitmapSmallEnouth(widthChange, heightChange))
				return;
		}
		zoom(widthChange, heightChange);
	}

	private void zoom(float widthChange, float heightChange) {
		if (bitmapRect.left <= mTransportRect.left) {
			bitmapRect.right = bitmapRect.right + widthChange;
		} else if (bitmapRect.right >= mTransportRect.right) {
			bitmapRect.left = bitmapRect.left - widthChange;
		} else {
			bitmapRect.left = bitmapRect.left - widthChange / 2;
			bitmapRect.right = bitmapRect.right + widthChange / 2;
		}
		if (bitmapRect.top <= mTransportRect.top) {
			bitmapRect.bottom = bitmapRect.bottom + heightChange;
		} else if (bitmapRect.bottom >= mTransportRect.bottom) {
			bitmapRect.top = bitmapRect.top - heightChange;
		} else {
			bitmapRect.top = bitmapRect.top - heightChange / 2;
			bitmapRect.bottom = bitmapRect.bottom + heightChange / 2;
		}
	}

	private boolean isBitmapBigEnough(float widthChange, float heightChange) {
		float scale = 0;
		if (bitmapRect.width() >= bitmapRect.height() && (bitmapRect.height() + heightChange) > mTransportRect.height()) {
			scale = mTransportRect.height() / bitmapRect.height() - 1;
		} else if (bitmapRect.width() < bitmapRect.height() && (bitmapRect.width() + widthChange) > mTransportRect.width()) {
			scale = mTransportRect.width() / bitmapRect.width() - 1;
		} else {
			return false;
		}
		if (scale > 0) {
			float width = bitmapRect.width() * scale;
			float height = bitmapRect.height() * scale;
			zoom(width, height);
		}
		return true;
	}

	private boolean isBitmapSmallEnouth(float widthChange, float heightChange) {
		return (widthChange + bitmapRect.width()) < mMinBitmapRect.width();
	}

	private void drawBoarder(Canvas canvas) {

		canvas.drawBitmap(mBlackBitmap, null, mBlackRectTop, mPaint);
		canvas.drawBitmap(mBlackBitmap, null, mBlackRectBottom, mPaint);
		mPaint.setColor(getResources().getColor(R.color.cut_image_boarder));
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(boaderWidth);
		canvas.drawLine(mTransportRect.left, mTransportRect.top - boaderWidth / 2, mTransportRect.right, mTransportRect.top - boaderWidth / 2, mPaint);
		canvas.drawLine(mTransportRect.left, mTransportRect.bottom + boaderWidth / 2, mTransportRect.right, mTransportRect.bottom + boaderWidth / 2,
		                mPaint);
	}

	public Rect getTranRectInWindow() {
		int[] locations = new int[2];
		getLocationInWindow(locations);
		int locationX = locations[0];
		int locationY = locations[1];
		Rect result;
		if (height >= width) {
			result = new Rect(locationX, locationY + (height - width) / 2, locationX + width, locationY + (height - width) / 2 + width);
		} else {
			result = new Rect(locationX + (width - height) / 2 + boaderWidth, locationY + boaderWidth, locationX + (width - height) / 2 + height
			        - boaderWidth, locationY + height - boaderWidth);
		}
		return result;
	}

	public static Bitmap createRepeater(int width, int height, Bitmap src) {
		int widthCount = width / src.getWidth() + 1;
		int heightCount = height / src.getHeight() + 1;
		int count = widthCount * heightCount;
		Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		for (int idx = 0; idx < count; ++idx) {
			int hindex = idx / widthCount;
			int windex = idx % widthCount;
			canvas.drawBitmap(src, windex * src.getWidth(), hindex * src.getHeight(), null);
		}

		return bitmap;
	}

	private void minZoom() {
		float width = bitmapRect.width();
		float height = bitmapRect.height();
		if (width > mTransportRect.width() || height > mTransportRect.height()) {
			float scale = 0;
			if (width >= height) {
				scale = (width - mTransportRect.width()) / width;
			} else {
				scale = (height - mTransportRect.height()) / height;
			}
			float widthChange = width * scale;
			float heightChange = height * scale;
			bitmapRect.right = bitmapRect.right - widthChange;
			bitmapRect.bottom = bitmapRect.bottom - heightChange;
		}
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

	public void clearBitmap() {
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

	public void recyle() {
		// mCanvasBitmap.recycle();
		// mBitmap.getBitmap().recycle();
		mBlackBitmap.recycle();
	}

	private void moveBitmap(int moveX, int moveY) {
		if (bitmapRect.width() > mTransportRect.width()) {
			if (moveX < 0 && bitmapRect.right <= mTransportRect.right) {
				moveX = 0;
				float rightPading = mTransportRect.right - bitmapRect.right;
				if (rightPading > 0) {
					bitmapRect.right = bitmapRect.right + rightPading;
					bitmapRect.left = bitmapRect.left + rightPading;
				}
			} else if (moveX > 0 && bitmapRect.left >= mTransportRect.left) {
				moveX = 0;
				float leftPadding = bitmapRect.left - mTransportRect.left;
				if (leftPadding > 0) {
					bitmapRect.left = bitmapRect.left - leftPadding;
					bitmapRect.right = bitmapRect.right - leftPadding;
				}
			}
		} else if (bitmapRect.width() <= mTransportRect.width()) {
			if ((bitmapRect.left <= mTransportRect.left && moveX < 0) || bitmapRect.right > mTransportRect.right && moveX >= 0)
				moveX = 0;
		}
		if (bitmapRect.height() > mTransportRect.height()) {
			if (moveY < 0 && bitmapRect.bottom <= mTransportRect.bottom) {
				moveY = 0;
				float bottomPading = mTransportRect.bottom - bitmapRect.bottom;
				if (bottomPading > 0) {
					bitmapRect.bottom = bitmapRect.bottom + bottomPading;
					bitmapRect.top = bitmapRect.top + bottomPading;
				}
			} else if (moveY > 0 && bitmapRect.top >= mTransportRect.top) {
				moveY = 0;
				float topPadding = bitmapRect.top - mTransportRect.top;
				if (topPadding > 0) {
					bitmapRect.top = bitmapRect.top - topPadding;
					bitmapRect.bottom = bitmapRect.bottom - topPadding;
				}
			}
		} else if (bitmapRect.height() <= mTransportRect.height()) {
			if ((bitmapRect.top <= mTransportRect.top && moveY < 0) || bitmapRect.bottom >= mTransportRect.bottom && moveY >= 0) {
				moveY = 0;
				if (moveY < 0) {
					float topPadding = mTransportRect.top - bitmapRect.top;
					if (topPadding > 0) {
						bitmapRect.top = bitmapRect.top + topPadding;
						bitmapRect.bottom = bitmapRect.bottom + topPadding;
					}
				} else {
					float bottomPadding = bitmapRect.bottom - mTransportRect.bottom;
					if (bottomPadding > 0) {
						bitmapRect.top = bitmapRect.top - bottomPadding;
						bitmapRect.bottom = bitmapRect.bottom - bottomPadding;
					}
				}
			}

		}
		//
		bitmapRect.left = bitmapRect.left + moveX;
		bitmapRect.right = bitmapRect.right + moveX;
		bitmapRect.top = bitmapRect.top + moveY;
		bitmapRect.bottom = bitmapRect.bottom + moveY;
	}
}
