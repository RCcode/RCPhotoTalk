package com.rcplatform.phototalk.db;

import java.util.List;

import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.thirdpart.bean.ThirdPartFriend;

public interface PhotoTalkDatabase {
	public boolean hasFriend(String suid);

	public List<Friend> getThirdPartFriends(int type);

	public void saveThirdPartFriends(List<ThirdPartFriend> thirdPartFriends);

	public void saveRecordInfos(List<Information> recordInfos);

	public List<Information> getRecordInfos();

	public void close();
}
