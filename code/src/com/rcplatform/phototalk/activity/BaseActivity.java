package com.rcplatform.phototalk.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.rcplatform.phototalk.MenueApplication;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.api.RCPlatformAsyncHttpClient;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.Utils;

public class BaseActivity extends Activity {
	protected final int LOADING_NO_MSG = -1;
	ProgressDialog mProgressDialog;
	protected RCPlatformAsyncHttpClient httpClient;
	protected InputMethodManager mInputMethodManager;

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}


	protected void hideSoftKeyboard(View view) {
		Utils.hideSoftInputKeyboard(this, view);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		hideSoftKeyboard(getCurrentFocus());
		return super.onTouchEvent(event);
	}

	protected void startActivity(Class<? extends Activity> clazz) {
		startActivity(new Intent(this, clazz));
	}

	public void showLoadingDialog(int titleResId, int msgResId, boolean cancelAble) {
		if (mProgressDialog == null) {
			mProgressDialog = new ProgressDialog(this);
		}
		mProgressDialog.setCancelable(cancelAble);
		if (titleResId != LOADING_NO_MSG)
			mProgressDialog.setTitle(getString(titleResId));
		else
			mProgressDialog.setTitle(null);
		if (msgResId != LOADING_NO_MSG)
			mProgressDialog.setMessage(getString(msgResId));
		else
			mProgressDialog.setMessage(null);
		if (!mProgressDialog.isShowing())
			mProgressDialog.show();
	}

	public void dismissLoadingDialog() {
		if (mProgressDialog != null && mProgressDialog.isShowing())
			mProgressDialog.dismiss();
	}

	protected MenueApplication getPhotoTalkApplication() {
		return (MenueApplication) getApplication();
	}

	@Override
	protected void onDestroy() {
		dismissLoadingDialog();
		if (httpClient != null)
			httpClient.cancel(this);
		super.onDestroy();
	}

	protected void initBackButton(int textResId, OnClickListener onClickListener) {
		findViewById(R.id.title_linear_back).setOnClickListener(onClickListener);
		TextView tv = (TextView) findViewById(R.id.titleContent);
		tv.setVisibility(View.VISIBLE);
		tv.setText(textResId);
		findViewById(R.id.back).setVisibility(View.VISIBLE);
	}
	
	protected void showErrorConfirmDialog(String msg) {
		DialogUtil.createErrorInfoDialog(this, msg).show();
	}
	protected void showErrorConfirmDialog(int msgResId) {
		DialogUtil.createErrorInfoDialog(this, msgResId).show();
	}
	
	protected void initForwordButton(int resId,OnClickListener onClickListener) {
		TextView tvForward=(TextView) findViewById(R.id.choosebutton);
		tvForward.setText(resId);
		tvForward.setOnClickListener(onClickListener);
		tvForward.setVisibility(View.VISIBLE);
	}
}
