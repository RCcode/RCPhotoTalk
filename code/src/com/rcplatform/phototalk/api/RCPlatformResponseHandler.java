package com.rcplatform.phototalk.api;


public interface RCPlatformResponseHandler {
	public void onFailure(int errorCode,String content);
	public void onSuccess(int statusCode,String content);
}
