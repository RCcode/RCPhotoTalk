package com.rcplatform.phototalk.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.MenueApplication;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.logic.LogicUtils;
import com.rcplatform.phototalk.utils.Constants.Action;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.Utils;

public class BaseActivity extends Activity {
	public static final int LOADING_NO_MSG = -1;
	ProgressDialog mProgressDialog;
	private AlertDialog logoutDialog;
	private BroadcastReceiver mOtherDeviceLoginReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			dismissLoadingDialog();
			showReLoginDialog();
		}
	};

	private void showReLoginDialog() {
		if (logoutDialog == null) {
			DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						break;
					case DialogInterface.BUTTON_NEGATIVE:
						LogicUtils.relogin(BaseActivity.this);
						break;
					}
				}
			};
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			logoutDialog = builder.setMessage(R.string.other_device_login).setPositiveButton(R.string.cancel, listener)
					.setNegativeButton(R.string.relogin, listener).setCancelable(false).create();
		}
		logoutDialog.show();
	}

	@Override
	protected void onResume() {
		super.onResume();
		registeOtherDeviceLoginReceiver();
	}

	private void registeOtherDeviceLoginReceiver() {
		IntentFilter filter = new IntentFilter(Action.ACTION_OTHER_DEVICE_LOGIN);
		registerReceiver(mOtherDeviceLoginReceiver, filter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mOtherDeviceLoginReceiver);
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
		if (!mProgressDialog.isShowing() && !isFinishing())
			mProgressDialog.show();
	}

	public void dismissLoadingDialog() {
		if (mProgressDialog != null && mProgressDialog.isShowing())
			mProgressDialog.dismiss();
	}

	public MenueApplication getPhotoTalkApplication() {
		return (MenueApplication) getApplication();
	}

	public UserInfo getCurrentUser() {
		return getPhotoTalkApplication().getCurrentUser();
	}

	protected void initBackButton(int textResId, OnClickListener onClickListener) {
		findViewById(R.id.title_linear_back).setOnClickListener(onClickListener);
		TextView tv = (TextView) findViewById(R.id.titleContent);
		tv.setVisibility(View.VISIBLE);
		tv.setText(textResId);
		findViewById(R.id.back).setVisibility(View.VISIBLE);
	}

	public void showErrorConfirmDialog(String msg) {
		if (!isFinishing())
			DialogUtil.createErrorInfoDialog(this, msg).show();
	}

	public void showErrorConfirmDialog(int msgResId) {
		DialogUtil.createErrorInfoDialog(this, msgResId).show();
	}

	protected void initForwordButton(int resId, OnClickListener onClickListener) {
		TextView tvForward = (TextView) findViewById(R.id.choosebutton);
		tvForward.setText(resId);
		tvForward.setOnClickListener(onClickListener);
		tvForward.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onDestroy() {
		dismissLoadingDialog();
		ImageLoader.getInstance().stop();
		super.onDestroy();
	}
}
