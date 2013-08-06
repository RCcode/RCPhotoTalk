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
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.Constants.Action;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.PrefsUtils;
import com.rcplatform.phototalk.utils.Utils;
import com.umeng.analytics.MobclickAgent;

public class BaseActivity extends Activity implements ActivityFunction {

	public static final int LOADING_NO_MSG = -1;
	ProgressDialog mProgressDialog;
	private AlertDialog logoutDialog;
	protected Context baseContext;
	private boolean needRelogin = true;
	private ActivityFunction functionImpl;
	private String rcId;

	public String getCurrentUserRcId() {
		return rcId;
	}

	protected void cancelRelogin() {
		needRelogin = false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		baseContext = this;
		functionImpl = new ActivityFunctionBasic(this);
		if (getCurrentUser() != null)
			rcId = getCurrentUser().getRcId();
	}

	public int getStartMode() {
		return getIntent().getIntExtra(Constants.ApplicationStartMode.APPLICATION_START_KEY, Constants.ApplicationStartMode.APPLCATION_START_NORMAL);
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

	@Deprecated
	public void showLoadingDialog(int titleResId, int msgResId, boolean cancelAble) {
		showLoadingDialog(cancelAble);
	}

	@Deprecated
	public void dismissLoadingDialog() {
		dissmissLoadingDialog();
	}

	public PhotoTalkApplication getPhotoTalkApplication() {
		return functionImpl.getPhotoTalkApplication();
	}

	public UserInfo getCurrentUser() {
		return functionImpl.getCurrentUser();
	}

	protected void initBackButton(int textResId, OnClickListener onClickListener) {
		findViewById(R.id.back).setOnClickListener(onClickListener);
		TextView tv = (TextView) findViewById(R.id.titleContent);
		tv.setVisibility(View.VISIBLE);
		tv.setText(textResId);
		findViewById(R.id.back).setVisibility(View.VISIBLE);
	}

	@Deprecated
	public void showErrorConfirmDialog(String msg) {
		showConfirmDialog(msg);
	}

	@Deprecated
	public void showErrorConfirmDialog(int msgResId) {
		showConfirmDialog(msgResId);
	}

	protected void initForwordButton(int resId, OnClickListener onClickListener) {
		View view = findViewById(R.id.choosebutton);
		view.setBackgroundResource(resId);
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

	@Override
	public Context getContext() {
		return functionImpl.getContext();
	}

	@Override
	public void showLoadingDialog(boolean cancelAble) {
		functionImpl.showLoadingDialog(cancelAble);
	}

	@Override
	public void dissmissLoadingDialog() {
		functionImpl.dissmissLoadingDialog();
	}

	@Override
	public Activity getActivity() {
		return functionImpl.getActivity();
	}

	@Override
	public void showConfirmDialog(String msg) {
		functionImpl.showConfirmDialog(msg);
	}

	@Override
	public void showConfirmDialog(int resId) {
		functionImpl.showConfirmDialog(resId);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		Constants.initUIData(this);
		if (needRelogin) {
			UserInfo userInfo = PrefsUtils.LoginState.getLoginUser(this);
			getPhotoTalkApplication().setCurrentUser(userInfo);
		}
	}
}
