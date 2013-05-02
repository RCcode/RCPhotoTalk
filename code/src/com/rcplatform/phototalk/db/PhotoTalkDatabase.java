package com.rcplatform.phototalk.db;

import java.util.List;

import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.thirdpart.bean.ThirdPartFriend;

public interface PhotoTalkDatabase {
	public List<Friend> getThirdPartFriends(int type);
	public void saveThirdPartFriends(List<ThirdPartFriend> thirdPartFriends);
	public void close();
}	
