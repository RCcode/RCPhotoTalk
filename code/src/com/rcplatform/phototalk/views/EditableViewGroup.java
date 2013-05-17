package com.rcplatform.phototalk.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

public class EditableViewGroup extends ViewGroup {

	private int specSize_Heigth;

	private int specSize_Widht;

	private LinearLayout editTextView;

	private int x;

	private int y;

	private int lastY;

	private boolean isMove;

	private int downY;

	private int PreDownY;

	private boolean stat;

	private boolean isPopupSoftInput;

	private boolean isTuyaMode;

	public EditableViewGroup(Context context) {
		super(context);
	}

	public EditableViewGroup(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public EditableViewGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
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

		Log.i("Futao", "specSize_Widht = " + specSize_Widht
				+ " specSize_Heigth = " + specSize_Heigth);
		Log.i("Futao", "count = " + getChildCount());
		setMeasuredDimension(specSize_Widht, specSize_Heigth);
		// super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		View v = getChildAt(0);
		v.measure(MeasureSpec.makeMeasureSpec(specSize_Widht,
				MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
				specSize_Heigth, MeasureSpec.EXACTLY));
	}

	public void addEditeTextView(LinearLayout view) {
		editTextView = view;
		measureChild(editTextView);
		addView(editTextView);
		editTextView.layout(0, specSize_Heigth / 2,
				editTextView.getMeasuredWidth(), specSize_Heigth / 2
						+ editTextView.getMeasuredHeight());
//		editTextView.setFocusable(true);
		showInputMethod(((Activity) getContext()));
	}

	public void updateTextViewLoation(int buttom) {
		if (editTextView != null)
			getChildAt(1).layout(0, buttom - editTextView.getMeasuredHeight(),
					editTextView.getMeasuredWidth(), buttom);
	}

	public void measureChild(View v) {
		ViewGroup.LayoutParams params = v.getLayoutParams();
		if (params == null)
			params = new ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);

		int height = params.height;
		if (height > 0) {
			height = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
		} else {
			height = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		}
		v.measure(MeasureSpec.makeMeasureSpec(specSize_Widht,
				MeasureSpec.EXACTLY), height);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if (isPopupSoftInput || isTuyaMode) {
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
				editTextView.layout(0, y, getChildAt(1).getMeasuredWidth(), y
						+ getChildAt(1).getMeasuredHeight());
			}
			break;
		case MotionEvent.ACTION_UP:
//			if (((EditText) editTextView.getChildAt(0)) != null) {
//			}
//			if (((EditText) editTextView.getChildAt(0)).getText().equals("")
//					|| ((EditText) editTextView.getChildAt(0)).getText() == null) {
//				editTextView.setVisibility(View.GONE);
//			}

			if (!isMove && stat) {
				showInputMethod(((Activity) getContext()));
			} else {

			}
			isMove = false;
			break;
		}
		return super.dispatchTouchEvent(event);
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
		InputMethodManager imm = (InputMethodManager) activity
				.getSystemService(Context.INPUT_METHOD_SERVICE);
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
			if (editTextView != null
					&& (editTextView.getVisibility() == View.VISIBLE)) {
				editTextView.getChildAt(0).setFocusableInTouchMode(true);
				editTextView.getChildAt(0).setFocusable(true);
				editTextView.getChildAt(0).requestFocus();
				((EditText) editTextView.getChildAt(0)).setCursorVisible(true);
			}
		} else {
			if (editTextView != null
					&& (editTextView.getVisibility() == View.VISIBLE)) {
				editTextView.getChildAt(0).setFocusableInTouchMode(false);
				editTextView.getChildAt(0).setFocusable(false);
				editTextView.getChildAt(0).clearFocus();
				((EditText) editTextView.getChildAt(0)).setCursorVisible(false);
			}
		}
	}

}