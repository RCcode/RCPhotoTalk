package com.rcplatform.videotalk.clienservice;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.rcplatform.videotalk.PhotoTalkApplication;
import com.rcplatform.videotalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.videotalk.galhttprequest.LogUtil;
import com.rcplatform.videotalk.request.RCPlatformAsyncHttpClient;
import com.rcplatform.videotalk.request.Request;

public class PhotoTalkWebService extends Service {
	private RCPlatformAsyncHttpClient mClient;
	private static Handler mWebServiceHandler = new Handler();

	public void onCreate() {
		super.onCreate();
		((PhotoTalkApplication) getApplication()).setWebService(this);
		mClient = new RCPlatformAsyncHttpClient();
		LogUtil.e("webservice service oncreate over");
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
