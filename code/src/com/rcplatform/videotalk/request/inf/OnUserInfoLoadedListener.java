package com.rcplatform.videotalk.request.inf;

import java.util.Map;

import com.rcplatform.videotalk.bean.AppInfo;
import com.rcplatform.videotalk.bean.UserInfo;

public interface OnUserInfoLoadedListener {
	public void onSuccess(UserInfo userInfo);

	public void onError(int errorCode, String content);
	
	public void onOthreAppUserInfoLoaded(Map<AppInfo,UserInfo> userInfos);
}
