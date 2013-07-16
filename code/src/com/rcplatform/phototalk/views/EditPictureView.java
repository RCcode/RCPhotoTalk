package com.rcplatform.phototalk.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import com.rcplatform.phototalk.PhotoTalkApplication;
import com.rcplatform.phototalk.utils.Constants;

public class EditPictureView extends View {

	public static final int MODEl_TUYA = 0;

	public static final int MODEL_TEXT = 1;

	public static final int MODEL_NORMAL = 2;

	public int model = MODEL_NORMAL;

	private Bitmap mBitmap;

	private Bitmap bg;

	private Bitmap tempBitmap;

	private Canvas mCanvas;

	private Path mPath;

	private Paint mBitmapPaint;// 画布的画笔

	private Paint mPaint;// 真实的画笔

	private float mX, mY;// 临时点坐标
	private EditPictureListener listener;
	private static final float TOUCH_TOLERANCE = 4;

	// 保存Path路径的集合,用List集合来模拟栈，用于后退步骤
	private static List<DrawPath> savePath;

	// 保存Path路径的集合,用List集合来模拟栈,用于前进步骤
	private static List<DrawPath> canclePath;

	// 记录Path路径的对象
	private DrawPath dp;

	private int screenWidth, screenHeight;// 屏幕長寬

	private PhotoTalkApplication app;

	private final Context context;

	private int timeLimit;

	private class DrawPath {

		public Path path;// 路径

		public Paint paint;// 画笔
	}

	// 背景颜色
	// public static int color = Color.RED;
	public int color;

	public static int srokeWidth = 10;

	/**
	 * 得到画笔
	 * 
	 * @return
	 */
	public Paint getPaint() {
		return mPaint;
	}

	/**
	 * 设置画笔
	 * 
	 * @param mPaint
	 */
	public void setPaint(Paint mPaint) {
		this.mPaint = mPaint;
	}

	public void setColor(int color) {
		this.color = color;
	}

	private void init(int w, int h) {
		screenWidth = w;
		screenHeight = h;
		app = (PhotoTalkApplication) getContext().getApplicationContext();
		mBitmap = Bitmap.createBitmap(screenWidth, screenHeight,
				Bitmap.Config.ARGB_8888);
		// 保存一次一次绘制出来的图形
		mCanvas = new Canvas(mBitmap);
		mBitmapPaint = new Paint(Paint.DITHER_FLAG);
		drawBg(mCanvas);
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		// mPaint.setStyle(Paint.Style.STROKE);
		// mPaint.setStrokeJoin(Paint.Join.ROUND);// 设置外边缘
		// mPaint.setStrokeCap(Paint.Cap.SQUARE);// 形状
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);// 设置外边缘
		mPaint.setStrokeCap(Paint.Cap.ROUND);// 形状
		mPaint.setStrokeWidth(srokeWidth);// 画笔宽度
		color = Color.parseColor("#ff0000");
		mPaint.setColor(color);
		savePath = new ArrayList<DrawPath>();
		canclePath = new ArrayList<DrawPath>();
		if (Constants.SCREEN_WIDTH < 720)
			srokeWidth = 5;
	}

	private void initPaint() {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		// mPaint.setStyle(Paint.Style.STROKE);
		// mPaint.setStrokeJoin(Paint.Join.ROUND);// 设置外边缘
		// mPaint.setStrokeCap(Paint.Cap.SQUARE);// 形状
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);// 设置外边缘
		mPaint.setStrokeCap(Paint.Cap.ROUND);// 形状
		mPaint.setStrokeWidth(srokeWidth);// 画笔宽度
		mPaint.setColor(color);
	}

	public EditPictureView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		DisplayMetrics dm = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay()
				.getMetrics(dm);
		init(dm.widthPixels, dm.heightPixels);
		this.context = context;
	}

	public EditPictureView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		DisplayMetrics dm = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay()
				.getMetrics(dm);
		init(dm.widthPixels, dm.heightPixels);
		this.context = context;
	}

	public EditPictureView(Context context, int w, int h) {
		super(context);
		DisplayMetrics dm = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay()
				.getMetrics(dm);
		init(dm.widthPixels, dm.heightPixels);
		this.context = context;
	}

	public EditPictureView(Context context) {
		super(context);
		DisplayMetrics dm = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay()
				.getMetrics(dm);
		init(dm.widthPixels, dm.heightPixels);
		this.context = context;
	}

	public EditPictureView(Context context, String tempfilePath) {
		super(context);
		DisplayMetrics dm = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay()
				.getMetrics(dm);
		init(dm.widthPixels, dm.heightPixels);
		this.context = context;
	}

	@Override
	public void onDraw(Canvas canvas) {
		// 背景颜色，这里颜色应该是
		// canvas.drawColor(color);
		// 将前面已经画过得显示出来
		canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
		if (mPath != null) {
			// 实时的显示
			canvas.drawPath(mPath, mPaint);
		}
	}

	private void touch_start(float x, float y) {
		mPath.moveTo(x, y);
		mX = x;
		mY = y;
	}

	private void touch_move(float x, float y) {
		float dx = Math.abs(x - mX);
		float dy = Math.abs(mY - y);
		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
			// 从x1,y1到x2,y2画一条贝塞尔曲线，更平滑(直接用mPath.lineTo也是可以的)
			mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
			mX = x;
			mY = y;
		}
	}

	private void touch_up() {
		mPath.lineTo(mX, mY);
		mCanvas.drawPath(mPath, mPaint);
		// 将一条完整的路径保存下来(相当于入栈操作)
		savePath.add(dp);
		mPath = null;// 重新置空
	}

	/**
	 * 撤销的核心思想就是将画布清空， 将保存下来的Path路径最后一个移除掉， 重新将路径画在画布上面。
	 */
	public int undo() {

		mBitmap = Bitmap.createBitmap(screenWidth, screenHeight,
				Bitmap.Config.ARGB_8888);
		mCanvas.setBitmap(mBitmap);// 重新设置画布，相当于清空画布
		drawBg(mCanvas);
		// 清空画布，但是如果图片有背景的话，则使用上面的重新初始化的方法，用该方法会将背景清空掉…
		if (savePath != null && savePath.size() > 0) {

			DrawPath dPath = savePath.get(savePath.size() - 1);
			canclePath.add(dPath);

			// 移除最后一个path,相当于出栈操作
			savePath.remove(savePath.size() - 1);

			Iterator<DrawPath> iter = savePath.iterator();
			while (iter.hasNext()) {
				DrawPath drawPath = iter.next();
				mCanvas.drawPath(drawPath.path, drawPath.paint);
			}
			invalidate();// 刷新

		} else {
			return -1;
		}
		return savePath.size();
	}

	/**
	 * 重做的核心思想就是将撤销的路径保存到另外一个集合里面(栈)， 然后从redo的集合里面取出最顶端对象， 画在画布上面即可。
	 */
	public int redo() {
		// 如果撤销你懂了的话，那就试试重做吧。
		if (canclePath.size() < 1)
			return canclePath.size();

		mBitmap = Bitmap.createBitmap(screenWidth, screenHeight,
				Bitmap.Config.ARGB_8888);
		mCanvas.setBitmap(mBitmap);// 重新设置画布，相当于清空画布
		// 清空画布，但是如果图片有背景的话，则使用上面的重新初始化的方法，用该方法会将背景清空掉…
		drawBg(mCanvas);
		if (canclePath != null && canclePath.size() > 0) {
			// 移除最后一个path,相当于出栈操作
			DrawPath dPath = canclePath.get(canclePath.size() - 1);
			savePath.add(dPath);
			canclePath.remove(canclePath.size() - 1);

			Iterator<DrawPath> iter = savePath.iterator();
			while (iter.hasNext()) {
				DrawPath drawPath = iter.next();
				mCanvas.drawPath(drawPath.path, drawPath.paint);
			}
			invalidate();// 刷新
		}
		return canclePath.size();
	}

	// @Override
	// public boolean dispatchTouchEvent(MotionEvent event) {
	// // TODO Auto-generated method stub
	// switch (event.getAction()) {
	// case MotionEvent.ACTION_DOWN:
	// Log.i("Futao", "editeview dispatchTouchEvent ACTION_DOWN");
	// break;
	// case MotionEvent.ACTION_MOVE:
	// Log.i("Futao", "editeview dispatchTouchEvent ACTION_MOVE");
	// break;
	// case MotionEvent.ACTION_UP:
	// Log.i("Futao", "editeview dispatchTouchEvent ACTION_UP");
	// break;
	// }
	//
	// return super.dispatchTouchEvent(event);
	// }

	// @Override
	// public boolean onTouchEvent(MotionEvent event) {
	// // TODO Auto-generated method stub
	//
	// switch (event.getAction()) {
	// case MotionEvent.ACTION_DOWN:
	// Log.i("Futao", "editeview onTouchEvent ACTION_DOWN");
	// break;
	// case MotionEvent.ACTION_MOVE:
	// Log.i("Futao", "editeview onTouchEvent ACTION_MOVE");
	// break;
	// case MotionEvent.ACTION_UP:
	// Log.i("Futao", "editeview onTouchEvent ACTION_UP");
	// break;
	// }
	// return true;
	// }
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (model == MODEl_TUYA) {
			float x = event.getX();
			float y = event.getY();
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				initPaint();
				// 重置下一步操作
				canclePath = new ArrayList<DrawPath>();
				// 每次down下去重新new一个Path
				mPath = new Path();
				// 每一次记录的路径对象是不一样的
				dp = new DrawPath();
				dp.path = mPath;
				dp.paint = mPaint;
				touch_start(x, y);
				invalidate();
				break;
			case MotionEvent.ACTION_MOVE:
				touch_move(x, y);
				invalidate();
				break;
			case MotionEvent.ACTION_UP:
				touch_up();
				invalidate();
				break;
			}
			return true;
		}
		return super.onTouchEvent(event);
	}

	private void drawBg(Canvas canvas) {
		if (tempBitmap == null || tempBitmap.isRecycled()) {

			// if
			// (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			// {
			// File file = new File(tempFilePath);
			// if (!file.exists()) {
			// bg = app.getEditeBitmap();
			// } else {
			//
			// try {
			// BitmapFactory.Options options = new BitmapFactory.Options();
			// options.inSampleSize = 2;
			// bg = BitmapFactory.decodeFile(tempFilePath, options);
			// }
			// catch (Exception e) {
			// e.printStackTrace();
			// }
			// }
			// } else {
			bg = app.getEditeBitmap();
			// }
			//确保竖屏 
			if (bg != null || !bg.isRecycled()) {
				if (bg.getWidth() > bg.getHeight()) {
					Matrix matrix = new Matrix();
					matrix.reset();
					matrix.setRotate(90);
					tempBitmap = Bitmap.createBitmap(bg, 0, 0, bg.getWidth(),
							bg.getHeight(), matrix, true);
					bg.recycle();
				} else {
					tempBitmap = bg;
				}

			}

		}
		float bgWidth = tempBitmap.getWidth();
		float bgHeight = tempBitmap.getHeight();
		int left, top, right, bottom;
		int showWidth, showHeight;
//需要缩小
		if (bgWidth > Constants.SCREEN_WIDTH
				|| bgHeight > Constants.SCREEN_HEIGHT) {
			float widthScale = (bgWidth - Constants.SCREEN_WIDTH) / bgWidth;
			float heightScale = (bgHeight - Constants.SCREEN_HEIGHT) / bgHeight;
			if (widthScale > heightScale) {
				showWidth = Constants.SCREEN_WIDTH;
				showHeight = (int) (bgHeight - (bgHeight * widthScale));
				left = 0;
				right = showWidth;
				top = (Constants.SCREEN_HEIGHT - showHeight) / 2;
				bottom = Constants.SCREEN_HEIGHT - top;
			} else if(widthScale < heightScale){
				showHeight = Constants.SCREEN_HEIGHT;
				showWidth = (int) (bgWidth - (bgWidth * heightScale));
				
				left = (Constants.SCREEN_WIDTH - showWidth) / 2;;
				right = Constants.SCREEN_WIDTH - left;
				top = 0;
				bottom = Constants.SCREEN_HEIGHT;
			}else{
				left = 0;
				right = Constants.SCREEN_WIDTH;
				top = 0;
				bottom = Constants.SCREEN_HEIGHT;
			}
//			left = (Constants.SCREEN_WIDTH - showWidth) / 2;
//			right = Constants.SCREEN_WIDTH - left;
//			top = (Constants.SCREEN_HEIGHT - showHeight) / 2;
//			bottom = Constants.SCREEN_HEIGHT - top;
		} else if(bgWidth == Constants.SCREEN_WIDTH&&bgHeight==Constants.SCREEN_HEIGHT){
//			刚刚好的情况
			left = 0;
			right = Constants.SCREEN_WIDTH;
			top = 0;
			bottom = Constants.SCREEN_HEIGHT;
//			left = (int) ((Constants.SCREEN_WIDTH - bgWidth) / 2);
//			right = Constants.SCREEN_WIDTH - left;
//			top = (int) ((Constants.SCREEN_HEIGHT - bgHeight) / 2);
//			bottom = Constants.SCREEN_HEIGHT - top;
		}else{
			float widthScale = (Constants.SCREEN_WIDTH-bgWidth) / bgWidth;
			float heightScale = (Constants.SCREEN_HEIGHT-bgHeight) / bgHeight;
			if(widthScale<heightScale){
				showWidth = Constants.SCREEN_WIDTH;
				showHeight = (int) (Constants.SCREEN_HEIGHT - (bgHeight * widthScale));
				left = 0;
				right = showWidth;
				top = (Constants.SCREEN_HEIGHT - showHeight) / 2;
				bottom = Constants.SCREEN_HEIGHT - top;
			}else if(widthScale>heightScale){
				showHeight = Constants.SCREEN_HEIGHT;
				showWidth = (int) (Constants.SCREEN_WIDTH - (bgWidth * heightScale));
				
				left = (Constants.SCREEN_WIDTH - showWidth) / 2;;
				right = Constants.SCREEN_WIDTH - left;
				top = 0;
				bottom = Constants.SCREEN_HEIGHT;
			}else{
				left = 0;
				right = Constants.SCREEN_WIDTH;
				top = 0;
				bottom = Constants.SCREEN_HEIGHT;
			}
			
		}
		RectF dst = new RectF(left, top, right, bottom);
		if(listener!=null){
			listener.setViewLayout(left, top, right, bottom);
		}
		canvas.drawBitmap(tempBitmap, null, dst, mBitmapPaint);

		// tempBitmap.recycle();
		// tempBitmap = null;
	}

	public interface EditPictureListener {
		public void setViewLayout(int left, int top, int right, int bottom);
	}

	public boolean openOrCloseTuya() {
		if (model == MODEL_NORMAL || model == MODEL_TEXT) {
			model = MODEl_TUYA;
			return true;
		} else if (model == MODEl_TUYA)
			model = MODEL_NORMAL;
		return false;
	}

	// public void cacheOnDisEditedPictrue(final Bitmap bitmap) {
	// showDialog();
	// new Thread(new Runnable() {
	//
	// @Override
	// public void run() {
	// File file = new File(tempFilePath);
	// try {
	//
	// if (!file.exists())
	// file.createNewFile();
	// BufferedOutputStream os = new BufferedOutputStream(new
	// FileOutputStream(file)); //
	// // b.compress(Bitmap.CompressFormat.JPEG, 100, os);
	// bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
	// os.flush();
	// os.close();
	// handler.sendEmptyMessage(CACHE_SUCCESS);
	// }
	// catch (Exception e) {
	// handler.sendEmptyMessage(NO_SDC);
	// e.printStackTrace();
	// }
	// }
	// }).start();
	//
	// }

	public int getTimeLimit() {
		return timeLimit;
	}

	public void setTimeLimit(int timeLimit) {
		this.timeLimit = timeLimit;
	}

	public void recyle() {
		if (mBitmap != null && !mBitmap.isRecycled()) {
			mBitmap.recycle();
			mBitmap = null;
		}
		if (bg != null && !bg.isRecycled()) {
			bg.recycle();
			bg = null;
		}
		if (tempBitmap != null && !tempBitmap.isRecycled()) {
			tempBitmap.recycle();
			tempBitmap = null;
		}
	}

	public boolean hasDrawed() {
		return savePath != null && !savePath.isEmpty();
	}
}
