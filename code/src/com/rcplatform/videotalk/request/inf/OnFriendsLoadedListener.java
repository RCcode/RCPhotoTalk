package com.rcplatform.videotalk.request.inf;

import java.util.List;

import com.rcplatform.videotalk.bean.Friend;

public interface OnFriendsLoadedListener {
	public void onLocalFriendsLoaded(List<Friend> friends, List<Friend> recommends);

	public void onServiceFriendsLoaded(List<Friend> friends, List<Friend> recommends);

	public void onError(int errorCode, String content);
}
