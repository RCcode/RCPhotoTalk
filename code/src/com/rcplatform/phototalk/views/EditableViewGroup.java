package com.rcplatform.phototalk.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class EditableViewGroup extends RelativeLayout {

	private int specSize_Heigth;

	private int specSize_Widht;

	private LinearLayout editTextView;

	private int x;

	private int y;

	private int screenHeight;

	private boolean isMove;

	private int downY;

	private int PreDownY;

	private boolean stat;

	private boolean isPopupSoftInput;

	private boolean isTuyaMode;

	private int layoutY = 0;
	private int layoutH = 0;

	public EditableViewGroup(Context context) {
		super(context);
		Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
		screenHeight = display.getHeight();
	}

	public EditableViewGroup(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
		screenHeight = display.getHeight();
	}

	public EditableViewGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
		screenHeight = display.getHeight();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		View v = getChildAt(0);
		v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		specSize_Widht = MeasureSpec.getSize(widthMeasureSpec);
		specSize_Heigth = MeasureSpec.getSize(heightMeasureSpec);

		setMeasuredDimension(specSize_Widht, specSize_Heigth);

		View v = getChildAt(0);
		v.measure(MeasureSpec.makeMeasureSpec(specSize_Widht, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(specSize_Heigth, MeasureSpec.EXACTLY));
	}

	public void addEditeTextView(LinearLayout view, int location) {
		editTextView = view;
		measureChild(editTextView);
		addView(editTextView);
		editTextView.layout(0, location, editTextView.getMeasuredWidth(), location + editTextView.getMeasuredHeight());
		layoutY = location;
		layoutH = location + editTextView.getMeasuredHeight();
		// editTextView.setFocusable(true);
		showInputMethod(((Activity) getContext()));
	}

	public void updateTextViewLoation(int buttom) {
		if (editTextView != null)
			getChildAt(1).layout(0, buttom - editTextView.getMeasuredHeight(), editTextView.getMeasuredWidth(), buttom);
	}

	public void measureChild(View v) {
		ViewGroup.LayoutParams params = v.getLayoutParams();
		if (params == null)
			params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

		int height = params.height;
		if (height > 0) {
			height = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
		} else {
			height = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		}
		v.measure(MeasureSpec.makeMeasureSpec(specSize_Widht, MeasureSpec.EXACTLY), height);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if ( isTuyaMode) {
			return super.dispatchTouchEvent(event);
		}
		x = (int) event.getX();
		y = (int) event.getY();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downY = (int) event.getY();
			stat = isMoveView(editTextView, x, y);
			if (stat) {
			}
			break;
		case MotionEvent.ACTION_MOVE:
			PreDownY = (int) event.getY();
			if (stat && Math.abs(PreDownY - downY) > 5) {
				isMove = true;
				stat = false;
			}
			if (isMove) {
				if (y < 120) {
					y = 120;
				} else if (y > screenHeight - 190) {
					y = screenHeight - 190;
				}
				layoutY = y;
				layoutH = y + getChildAt(1).getMeasuredHeight();
				editTextView.layout(0, layoutY, getChildAt(1).getMeasuredWidth(), layoutH);
			}
			break;
		case MotionEvent.ACTION_UP:

			if (!isMove && stat) {
				showInputMethod(((Activity) getContext()));
			} else {

			}
			isMove = false;
			break;
		}
		return super.dispatchTouchEvent(event);
	}

	public void setLastLayout() {
		if (null != editTextView) {
			editTextView.layout(0, layoutY, getChildAt(1).getMeasuredWidth(), layoutH);
		}
	}

	private boolean isMoveView(View v, int downX, int downY) {
		if (v == null)
			return false;
		Rect rect = new Rect();
		v.getHitRect(rect);
		if (rect.contains(downX, downY)) {
			return true;
		} else
			return false;

	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);
		Log.i("ABC", "onSizeChanged" + w + "," + h + ", " + oldw + "," + oldh);
	}

	public static void showInputMethod(Activity activity) {
		final View view = activity.getWindow().peekDecorView();
		InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
		imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
	}

	public boolean isPopupSoftInput() {
		return isPopupSoftInput;
	}

	public boolean isTuyaMode() {
		return isTuyaMode;
	}

	public void setTuyaMode(boolean isTuyaMode) {
		this.isTuyaMode = isTuyaMode;
	}

	public void setPopupSoftInput(boolean isPopupSoftInput) {
		this.isPopupSoftInput = isPopupSoftInput;
		if (isPopupSoftInput) {
			if (editTextView != null && (editTextView.getVisibility() == View.VISIBLE)) {
				editTextView.getChildAt(0).setFocusableInTouchMode(true);
				editTextView.getChildAt(0).setFocusable(true);
				editTextView.getChildAt(0).requestFocus();
				((EditText) editTextView.getChildAt(0)).setCursorVisible(true);
			}
		} else {
			if (editTextView != null && (editTextView.getVisibility() == View.VISIBLE)) {
				editTextView.getChildAt(0).setFocusableInTouchMode(false);
				editTextView.getChildAt(0).setFocusable(false);
				editTextView.getChildAt(0).clearFocus();
				((EditText) editTextView.getChildAt(0)).setCursorVisible(false);
			}
		}
	}
}