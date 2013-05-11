package com.rcplatform.phototalk.db;

import java.util.List;
import java.util.Map;

import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.thirdpart.bean.ThirdPartFriend;

public interface PhotoTalkDatabase {
	public boolean hasFriend(String suid);

	public List<Friend> getThirdPartFriends(int type);

	public void saveThirdPartFriends(List<ThirdPartFriend> thirdPartFriends);

	public void saveRecordInfos(List<Information> recordInfos);

	public List<Information> getRecordInfos();

	public void updateInformationState(Information... informations);

	public void deleteInformation(Information information);

	public void clearInformation();

	public void updateFriendRequestInformationByFriend(Friend friend);

	public Map<String, Information> updateTempInformations(UserInfo senderInfo, String picUrl, long createTime, Map<String, String> userIds);

	public void close();

}
