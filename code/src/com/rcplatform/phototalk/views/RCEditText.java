package com.rcplatform.phototalk.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

public class RCEditText extends EditText {

	private OnBackButtonPressListener listener;

	public static interface OnBackButtonPressListener {
		public void onBackButtonPressed();
	}

	public RCEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public RCEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RCEditText(Context context) {
		super(context);
	}

	public void setOnBackButtonPressListener(OnBackButtonPressListener listener) {
		this.listener = listener;
	}

	@Override
	public boolean onKeyPreIme(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
			if (listener != null) {
				listener.onBackButtonPressed();
			}
			return false;
		}
		return super.onKeyPreIme(keyCode, event);
	}
}
