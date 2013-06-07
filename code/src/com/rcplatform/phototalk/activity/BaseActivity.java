package com.rcplatform.phototalk.activity;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.rcplatform.phototalk.PhotoTalkApplication;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.logic.LogicUtils;
import com.rcplatform.phototalk.utils.Constants.Action;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.Utils;
import com.umeng.analytics.MobclickAgent;

public class BaseActivity extends Activity {

	public static final int LOADING_NO_MSG = -1;
	ProgressDialog mProgressDialog;
	private AlertDialog logoutDialog;
	protected Context baseContext;
	private boolean needRelogin = true;

	protected void cancelRelogin() {
		needRelogin = false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		baseContext = this;
	}

	@Override
	public void setContentView(View view) {
		super.setContentView(view);
	}

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
			AlertDialog.Builder builder = DialogUtil.getAlertDialogBuilder(this);
			logoutDialog = builder.setMessage(R.string.other_device_login).setNegativeButton(R.string.relogin, listener).setCancelable(false).create();
		}
		logoutDialog.show();
	}

	@Override
	protected void onResume() {
		super.onResume();
		registeOtherDeviceLoginReceiver();
		MobclickAgent.onResume(this);
	}

	private void registeOtherDeviceLoginReceiver() {
		if (needRelogin) {
			IntentFilter filter = new IntentFilter(Action.ACTION_OTHER_DEVICE_LOGIN);
			registerReceiver(mOtherDeviceLoginReceiver, filter);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
		if (needRelogin) {
			unregisterReceiver(mOtherDeviceLoginReceiver);
		}
	}

	protected void hideSoftKeyboard(View view) {
		Utils.hideSoftInputKeyboard(this, view);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		hideSoftKeyboard(getCurrentFocus());
		return super.onTouchEvent(event);
	}

	protected void startActivity(Class<? extends Activity> clazz) {
		startActivity(new Intent(this, clazz));
	}

	public void showLoadingDialog(int titleResId, int msgResId, boolean cancelAble) {
		if (mProgressDialog == null) {
			if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.GINGERBREAD_MR1)
				mProgressDialog = new ProgressDialog(this, R.style.Theme_Dialog_Update);
			else
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

		if (!mProgressDialog.isShowing() && !isFinishing()) {
			mProgressDialog.show();
			mProgressDialog.setContentView(R.layout.operation_loading);
		}
	}

	public void dismissLoadingDialog() {
		if (mProgressDialog != null && mProgressDialog.isShowing())
			mProgressDialog.dismiss();
	}

	public PhotoTalkApplication getPhotoTalkApplication() {
		return (PhotoTalkApplication) getApplication();
	}

	public UserInfo getCurrentUser() {
		return getPhotoTalkApplication().getCurrentUser();
	}

	protected void initBackButton(int textResId, OnClickListener onClickListener) {
		findViewById(R.id.back).setOnClickListener(onClickListener);
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
		TextView tvForward = (TextView) findViewById(R.id.tv_choosebutton_text);
		tvForward.setBackgroundResource(resId);
		View view = findViewById(R.id.choosebutton);
		view.setOnClickListener(onClickListener);
		view.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		dismissLoadingDialog();
	}

	protected void deleteTemp() {
		PhotoTalkApplication app = (PhotoTalkApplication) getApplication();
		String tempFilePath = app.getSendFileCachePath();
		File tempPic = new File(tempFilePath);
		deleteFile(tempPic);
	}

	protected void deleteFile(File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File file2 : files) {
				deleteFile(file2);
			}
		} else {
			file.delete();
		}
	}
}
