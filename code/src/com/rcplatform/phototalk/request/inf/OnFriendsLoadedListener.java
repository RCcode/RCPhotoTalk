package com.rcplatform.phototalk.request.inf;

import java.util.List;

import com.rcplatform.phototalk.bean.Friend;

public interface OnFriendsLoadedListener {
	public void onLocalFriendsLoaded(List<Friend> friends, List<Friend> recommends);

	public void onServiceFriendsLoaded(List<Friend> friends, List<Friend> recommends);

	public void onError(int errorCode, String content);
}
