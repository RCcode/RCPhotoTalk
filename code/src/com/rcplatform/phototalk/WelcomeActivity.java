package com.rcplatform.phototalk;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.clienservice.InviteFriendUploadService;
import com.rcplatform.phototalk.clienservice.PTBackgroundService;
import com.rcplatform.phototalk.clienservice.PhotoTalkWebService;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.utils.Contract;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.PrefsUtils;
import com.rcplatform.phototalk.utils.Utils;

/**
 * 标题、简要说明. <br>
 * 类详细说明.
 * <p>
 * Copyright: Menue,Inc Copyright (c) 2013-3-5 下午05:29:07
 * <p>
 * Team:Menue Beijing
 * <p>
 * 
 * @author jelly.xiong@menue.com.cn
 * @version 1.0.0
 */
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading);
		startService(new Intent(this, PTBackgroundService.class));
		startService(new Intent(this, PhotoTalkWebService.class));
		checkNetwork();
		Thread th = new Thread() {
			public void run() {
				Contract.init(WelcomeActivity.this);
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

	private void executeAutoLogin() {
		UserInfo userInfo = PrefsUtils.LoginState.getLoginUser(getApplicationContext());
		// 用户已登录过，自动登录主页。
		if (userInfo != null) {
			getPhotoTalkApplication().setCurrentUser(userInfo);
			if (!Contract.START_COMPLETE) {
				PhotoTalkDatabaseFactory.getDatabase().updateTempInformationFail();
				Contract.START_COMPLETE = true;
			}
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
