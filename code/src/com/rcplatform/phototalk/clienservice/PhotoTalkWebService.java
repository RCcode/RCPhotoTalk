package com.rcplatform.phototalk.clienservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.rcplatform.phototalk.MenueApplication;
import com.rcplatform.phototalk.request.FileRequest;
import com.rcplatform.phototalk.request.JSONRequest;
import com.rcplatform.phototalk.request.RCPlatformAsyncHttpClient;
import com.rcplatform.phototalk.request.RCPlatformAsyncHttpClient.RequestAction;

public class PhotoTalkWebService extends Service {
	private RCPlatformAsyncHttpClient mClientJson;
	private RCPlatformAsyncHttpClient mClientFile;

	public void onCreate() {
		super.onCreate();
		((MenueApplication) getApplication()).setWebService(this);
		mClientJson = new RCPlatformAsyncHttpClient(RequestAction.JSON);
		mClientFile = new RCPlatformAsyncHttpClient(RequestAction.FILE);
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void postRequest(JSONRequest request) {
		mClientJson.clearParams();
		mClientJson.putAllRequestParams(request.getParams());
		mClientJson.post(getBaseContext(), request.getUrl(), request.getResponseHandler());
	}

	public void postRequest(FileRequest request) {
		mClientFile.clearParams();
		mClientFile.putAllRequestParams(request.getParams());
		mClientFile.postFile(getBaseContext(), request.getUrl(), request.getFile(), request.getResponseHandler());
	}
}
