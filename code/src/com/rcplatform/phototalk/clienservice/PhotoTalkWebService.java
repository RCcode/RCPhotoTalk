package com.rcplatform.phototalk.clienservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.rcplatform.phototalk.MenueApplication;
import com.rcplatform.phototalk.request.RCPlatformAsyncHttpClient;
import com.rcplatform.phototalk.request.Request;

public class PhotoTalkWebService extends Service {
	private RCPlatformAsyncHttpClient mClient;

	public void onCreate() {
		super.onCreate();
		((MenueApplication) getApplication()).setWebService(this);
		mClient = new RCPlatformAsyncHttpClient();
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void post(Request request) {
		mClient.post(request);

	}

	public void postNameValue(Request request) {

	}
}
