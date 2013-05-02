package com.rcplatform.phototalk.thirdpart.utils;

import java.util.List;

import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.thirdpart.bean.ThirdPartFriend;

public interface GetFriendsListener {
	public void onError();
	public void onComplete(List<ThirdPartFriend> friends);

}
