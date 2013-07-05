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
import com.rcplatform.phototalk.galhttprequest.LogUtil;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.PrefsUtils;
import com.rcplatform.phototalk.utils.Utils;
import com.rcplatform.phototalk.utils.Constants.ApplicationStartMode;

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
				LogUtil.d("TEMPTAGHASH KEY:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
				// LogUtil.d(Base64.encodeToString(md.digest(),
				// Base64.DEFAULT));
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

	}

	private void startBackgroundService() {
		startService(new Intent(this, PTBackgroundService.class));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		printHashKey();
		setContentView(R.layout.loading);
		startBackgroundService();
		cancelRelogin();
		checkNetwork();
		Thread th = new Thread() {

			public void run() {
				LogUtil.e("welcome thread start");
				Constants.initUI(WelcomeActivity.this);
				Constants.initCountryDatabase(WelcomeActivity.this);
				mHandler.sendEmptyMessageDelayed(INIT_SUCCESS, WAITING_TIME);
			};
		};
		th.start();
		LogUtil.e("welcome oncreate over");
	}

	private void checkNetwork() {
		if (!Utils.isNetworkEnable(this)) {
			DialogUtil.showToast(this, R.string.no_net, Toast.LENGTH_SHORT);
		}
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
			getPhotoTalkApplication().setCurrentUser(userInfo);
			Intent intent = new Intent(WelcomeActivity.this, HomeActivity.class);
			int startMode = getIntent().getIntExtra(ApplicationStartMode.APPLICATION_START_KEY, -1);
			if (startMode != -1) {
				intent.putExtra(ApplicationStartMode.APPLICATION_START_KEY, startMode);
			}
			startActivity(intent);
			finish();
			return;
		}
		Intent intent = new Intent(WelcomeActivity.this, InitPageActivity.class);
		startActivity(intent);
		finish();
	}

}
