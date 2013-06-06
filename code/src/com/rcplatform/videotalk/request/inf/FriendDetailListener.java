package com.rcplatform.videotalk.request.inf;

import com.rcplatform.videotalk.bean.Friend;

public interface FriendDetailListener {
	public void onSuccess(Friend friend);

	public void onError(int errorCode, String content);
}
