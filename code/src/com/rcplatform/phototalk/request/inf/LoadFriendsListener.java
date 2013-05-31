package com.rcplatform.phototalk.request.inf;

import java.util.List;

import com.rcplatform.phototalk.bean.Friend;

public interface LoadFriendsListener {
	public void onFriendsLoaded(List<Friend> friends,List<Friend> recommends);
	public void onLoadedFail(String reason);
}
