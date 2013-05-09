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
	
	public void updateTempInformations(List<Information> informations,long flag);
	
	public List<Information> getRecordInfos();

	public void updateInformationState(Information...informations);
	
	public void deleteInformation(Information information);
	
	public void clearInformation();
	
	public void updateFriendRequestInformationByFriend(Friend friend);
	
	public void close();
	
}
