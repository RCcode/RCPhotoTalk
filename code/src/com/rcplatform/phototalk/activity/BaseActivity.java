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
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.AddFriendsActivity;
import com.rcplatform.phototalk.PhotoTalkApplication;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.logic.LogicUtils;
import com.rcplatform.phototalk.utils.Constants.Action;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.SystemMessageUtil;
import com.rcplatform.phototalk.utils.Utils;

public class BaseActivity extends Activity {
	public static final int LOADING_NO_MSG = -1;
	ProgressDialog mProgressDialog;
	private AlertDialog logoutDialog;
	protected Context baseContext;
	private PopupWindow mImageSelectPopupWindow;
	private View view;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		baseContext = this;
		view = getWindow().getDecorView();

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
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			logoutDialog = builder.setMessage(R.string.other_device_login)
					.setNegativeButton(R.string.relogin, listener)
					.setCancelable(false).create();
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
		hideSoftKeyboard(getCurrentFocus());
		return super.onTouchEvent(event);
	}


	protected void startActivity(Class<? extends Activity> clazz) {
		startActivity(new Intent(this, clazz));
	}

	public void showLoadingDialog(int titleResId, int msgResId,
			boolean cancelAble) {
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
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("menu");// 必须创建一项
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		if (view != null) {
			showMenu(view);
		}
		return false;
	}

	protected void showMenu(View view) {
		if (mImageSelectPopupWindow == null) {
			View detailsView = LayoutInflater.from(this).inflate(
					R.layout.menu_layout, null, false);

			mImageSelectPopupWindow = new PopupWindow(detailsView, getWindow()
					.getWindowManager().getDefaultDisplay().getWidth(),
					LayoutParams.WRAP_CONTENT);

			mImageSelectPopupWindow.setFocusable(true);
			mImageSelectPopupWindow.setOutsideTouchable(true);
			mImageSelectPopupWindow.setBackgroundDrawable(new BitmapDrawable());
			Button menu_add_btn = (Button) detailsView
					.findViewById(R.id.menu_add_btn);
			menu_add_btn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					BaseActivity.this.finish();
					Intent intent = new Intent(BaseActivity.this,AddFriendsActivity.class);
					intent.putExtra("from", "base");
					startActivity(intent);
				}
			});
			Button menu_take_score_btn = (Button) detailsView
					.findViewById(R.id.menu_take_score_btn);
			menu_take_score_btn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					SystemMessageUtil.enterPage("market://details?id=com.androidlord.optimizationbox", baseContext);
				}
			});
		}
		mImageSelectPopupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
	}
}
