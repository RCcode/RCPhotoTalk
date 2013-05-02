package com.rcplatform.phototalk;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.bean.AppInfo;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.clienservice.InviteFriendUploadService;
import com.rcplatform.phototalk.clienservice.PTBackgroundService;
import com.rcplatform.phototalk.utils.Contract;
import com.rcplatform.phototalk.utils.PakageInfoProvider;
import com.rcplatform.phototalk.utils.PrefsUtils;

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

	private static final int INIT_SUCCESS=100;
	
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
		startService(new Intent(this, PTBackgroundService.class));
		Thread th=new Thread(){
			public void run() {
				Contract.init(WelcomeActivity.this);
				mHandler.sendEmptyMessage(INIT_SUCCESS);
			};
		};
		th.start();
		// 判断版本更新
		executeUpdate();
	}

	private void executeUpdate() {

	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	private void executeAutoLogin() {
		UserInfo userInfo=PrefsUtils.LoginState.getLoginUser(getApplicationContext());
		// 用户已登录过，自动登录主页。
		if (userInfo!=null) {
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

	private void startUpload(){
		Intent intent=new Intent(this,InviteFriendUploadService.class);
		intent.setAction(Contract.Action.ACTION_UPLOAD_INTITE_CONTACT);
		startService(intent);
	}

}
