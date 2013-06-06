package com.rcplatform.videotalk.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.rcplatform.videotalk.R;

public class ColorPicker extends View {

    private Paint mPaint;

    private Bitmap mColorsBitmap;

    private int width;

    private int height;

    private OnColorChangeListener mListener;

    private int mCurrentColor;

    private boolean isTouch;

    private Bitmap mButtonPress;

    private Bitmap mButtonNormal;

    private float currentY;

    public ColorPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        init();
    }

    public ColorPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        init();
    }

    public ColorPicker(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawBitmap(getColorsBitmap(), 0, 0, mPaint);
        drawButton(canvas);
    }

    private void drawButton(Canvas canvas) {
        Bitmap btnBitmap = getButton();
        canvas.drawBitmap(btnBitmap, null, getButtonLocation(btnBitmap), mPaint);
    }

    private RectF getButtonLocation(Bitmap bitmap) {
        RectF rect = new RectF();
        rect.left = 0;
        rect.right = width;
        float bili = (float) width / bitmap.getWidth();
        float offset = bitmap.getHeight() * bili / 2;
        if (currentY > offset && currentY < (height - offset)) {
            rect.top = currentY - offset / 2;
            rect.bottom = rect.top + offset * 2;
        } else if (currentY <= offset) {
            rect.top = 0;
            rect.bottom = offset * 2;
        } else if (currentY >= (height - offset)) {
            rect.top = height - offset * 2;
            rect.bottom = height;
        }
        return rect;
    }

    private Bitmap getButton() {
        if (mButtonNormal == null || mButtonNormal.isRecycled())
            mButtonNormal = BitmapFactory.decodeResource(getResources(), R.drawable.color_picker_btn_n);
        if (mButtonPress == null || mButtonPress.isRecycled())
            mButtonPress = BitmapFactory.decodeResource(getResources(), R.drawable.color_picker_btn_p);
        if (isTouch)
            return mButtonPress;
        else
            return mButtonNormal;
    }

    private void init() {
        // TODO Auto-generated method stub
        mPaint = new Paint();
        // mPaint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));  
        // mPaint.setColor(Color.TRANSPARENT);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {
            isTouch = true;
            currentY = event.getY();
            mCurrentColor = getColor((int) currentY);
            if (mListener != null)
                mListener.onColorChange(mCurrentColor);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            currentY = event.getY();
            isTouch = false;
        }
        invalidate();
        return true;
    }

    public static interface OnColorChangeListener {

        public void onColorChange(int color);
    }

    private int getColor(int y) {
        Bitmap colorsBitmap = getColorsBitmap();
        if (y >= colorsBitmap.getHeight())
            y = colorsBitmap.getHeight() - 1;
        else if (y <= 0)
            y = 1;
        return colorsBitmap.getPixel(colorsBitmap.getWidth() / 2, y);

    }

    public void setOnColorChangeListener(OnColorChangeListener onColorChangeListener) {
        this.mListener = onColorChangeListener;
    }

    private Bitmap getColorsBitmap() {
        if (mColorsBitmap == null || mColorsBitmap.isRecycled()) {
            width = getMeasuredWidth();
            height = getMeasuredHeight();
            mColorsBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            Canvas canvas = new Canvas(mColorsBitmap);
            canvas.drawColor(Color.TRANSPARENT);
            Paint colorsPaint = new Paint();
            colorsPaint.setAntiAlias(true);
            int[] colors = new int[] { Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA };
            Shader colorsShader = new LinearGradient(0, 0, 0, height, colors, null, TileMode.REPEAT);
            colorsPaint.setShader(colorsShader);
            RectF dstRect = new RectF(0, 0, width, height);
            canvas.drawRoundRect(dstRect, 20f, 20f, colorsPaint);
            // canvas.drawRect(dstRect,colorsPaint);
        }
        return mColorsBitmap;
    }

    public void recyle() {
        if (mColorsBitmap != null && !mColorsBitmap.isRecycled()) {
            mColorsBitmap.recycle();
            mColorsBitmap = null;
        }
        if (mButtonNormal != null && !mButtonNormal.isRecycled()) {

            mButtonNormal.recycle();
            mButtonNormal = null;
        }
        if (mButtonPress != null && !mButtonPress.isRecycled()) {
            mButtonPress.recycle();
            mButtonPress = null;
        }

    }
}
