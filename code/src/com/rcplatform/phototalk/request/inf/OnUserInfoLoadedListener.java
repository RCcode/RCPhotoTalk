package com.rcplatform.phototalk.request.inf;

import java.util.Map;

import com.rcplatform.phototalk.bean.AppInfo;
import com.rcplatform.phototalk.bean.UserInfo;

public interface OnUserInfoLoadedListener {
	public void onSuccess(UserInfo userInfo);

	public void onError(int errorCode, String content);
	
	public void onOthreAppUserInfoLoaded(Map<AppInfo,UserInfo> userInfos);
}
