package com.rcplatform.videotalk.request.inf;

import java.util.List;

import com.rcplatform.videotalk.bean.Friend;

public interface LoadFriendsListener {
	public void onFriendsLoaded(List<Friend> friends,List<Friend> recommends);
	public void onLoadedFail(String reason);
}
