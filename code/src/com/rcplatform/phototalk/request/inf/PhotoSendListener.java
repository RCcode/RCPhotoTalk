package com.rcplatform.phototalk.request.inf;


public interface PhotoSendListener {
	public void onSendSuccess(long flag);

	public void onFail(long flag,int errorCode, String content);
}
