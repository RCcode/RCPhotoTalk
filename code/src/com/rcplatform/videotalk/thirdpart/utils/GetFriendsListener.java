package com.rcplatform.videotalk.thirdpart.utils;

import java.util.List;

import com.rcplatform.videotalk.bean.Friend;
import com.rcplatform.videotalk.thirdpart.bean.ThirdPartUser;

public interface GetFriendsListener {
	public void onError();
	public void onComplete(List<ThirdPartUser> friends);

}
