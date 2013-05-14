package com.rcplatform.phototalk.request.inf;

import com.rcplatform.phototalk.bean.Friend;

public interface FriendDetailListener {
	public void onSuccess(Friend friend);

	public void onError(int errorCode, String content);
}
