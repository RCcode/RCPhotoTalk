package com.rcplatform.phototalk.clienservice;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.rcplatform.phototalk.PhotoTalkApplication;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.request.RCPlatformAsyncHttpClient;
import com.rcplatform.phototalk.request.Request;

public class PhotoTalkWebService extends Service {
	private RCPlatformAsyncHttpClient mClient;
	private static Handler mWebServiceHandler = new Handler();

	public void onCreate() {
		super.onCreate();
		((PhotoTalkApplication) getApplication()).setWebService(this);
		mClient = new RCPlatformAsyncHttpClient();
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void post(final Request request) {
		cacheRequest(request);
		mWebServiceHandler.post(new Runnable() {

			@Override
			public void run() {
				mClient.post(request);
			}
		});
	}

	public void postNameValue(Request request) {
		cacheRequest(request);
		mClient.postNameValue(request);
	}

	private void cacheRequest(Request request) {
		if (request != null && request.isCache())
			PhotoTalkDatabaseFactory.getRequestDatabase().saveRequest(request);
	}
}
