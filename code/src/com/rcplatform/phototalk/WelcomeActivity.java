package com.rcplatform.phototalk;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gcm.ServerUtilities;
import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.clienservice.PTBackgroundService;
import com.rcplatform.phototalk.clienservice.PhotoTalkWebService;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.PrefsUtils;
import com.rcplatform.phototalk.utils.Utils;

public class WelcomeActivity extends BaseActivity {

	private static final int INIT_SUCCESS = 100;

	private static final long WAITING_TIME = 1000;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			executeAutoLogin();
		}

	};

	public void printHashKey() {

		try {
			PackageInfo info = getPackageManager().getPackageInfo("com.rcplatform.phototalk", PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				Log.d("TEMPTAGHASH KEY:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// printHashKey();
		setContentView(R.layout.loading);
		startService(new Intent(this, PTBackgroundService.class));
		startService(new Intent(this, PhotoTalkWebService.class));
		checkNetwork();
		Thread th = new Thread() {

			public void run() {
				Constants.initUI(WelcomeActivity.this);
				mHandler.sendEmptyMessageDelayed(INIT_SUCCESS, WAITING_TIME);
			};
		};
		th.start();
		// 判断版本更新
		executeUpdate();
	}

	private void checkNetwork() {
		if (!Utils.isNetworkEnable(this)) {
			DialogUtil.showToast(this, R.string.no_net, Toast.LENGTH_SHORT);
		}
	}

	private void executeUpdate() {

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			ServerUtilities.onDestroy(this);
		} catch (Exception e) {

		}
	}

	private void executeAutoLogin() {
		UserInfo userInfo = PrefsUtils.LoginState.getLoginUser(getApplicationContext());
		// 用户已登录过，自动登录主页。
		if (userInfo != null) {
			try {
				ServerUtilities.register(this, userInfo.getRcId(), userInfo.getToken());
			} catch (Exception e) {

			}
			getPhotoTalkApplication().setCurrentUser(userInfo);
			Intent intent = new Intent(WelcomeActivity.this, HomeActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		Intent intent = new Intent(WelcomeActivity.this, InitPageActivity.class);
		startActivity(intent);
		finish();
	}

}
