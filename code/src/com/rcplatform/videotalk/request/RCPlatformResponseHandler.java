package com.rcplatform.videotalk.request;


public interface RCPlatformResponseHandler {
	public void onFailure(int errorCode,String content);
	public void onSuccess(int statusCode,String content);
}
