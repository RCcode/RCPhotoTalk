package com.rcplatform.phototalk.request.inf;

import com.rcplatform.phototalk.bean.UserInfo;

public interface OnUserInfoLoadedListener {
	public void onSuccess(UserInfo userInfo);

	public void onError(int errorCode, String content);
}
