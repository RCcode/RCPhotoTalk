package com.rcplatform.phototalk.db;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.thirdpart.bean.ThirdPartUser;

public interface PhotoTalkDatabase {
	public static final Integer NEW_INFORMATION = 1;
	public static final Integer UPDATED_INFORMATION = 2;

	public boolean hasFriend(String suid);

	public List<Friend> getThirdPartFriends(int type);

	public void saveThirdPartFriends(List<ThirdPartUser> thirdPartFriends, int type);

	public void saveRecordInfos(List<Information> recordInfos);

	public List<Information> getRecordInfos();

	public void updateInformationState(Information... informations);

	public void deleteInformation(Information information);

	public void clearInformation();

	public void updateFriendRequestInformationByFriend(Friend friend);

	public Map<String, Information> updateTempInformations(final UserInfo senderInfo, String picUrl, final long createTime, List<String> receivableUserIds,
			final List<String> allReceiverIds, int state, int totleLength);

	public void saveFriends(List<Friend> friends);

	public void addFriend(Friend friend);

	public void deleteFriend(Friend friend);

	public void saveRecommends(List<Friend> recommends);

	public List<Friend> getFriends();

	public List<Friend> getRecommends(int type);

	public List<Friend> getRecommends();

	public void updateTempInformationFail();

	public void close();

	public Friend getFriendById(String rcId);

	public void saveRecommends(List<Friend> recommends, int friendType);

	public List<Friend> getHidenFriends();

	public void updateFriend(Friend friend);

	public Map<Integer, List<Information>> filterNewInformations(Collection<Information> newInformations, UserInfo currentUser);

	public List<Information> getInformationByPage(int page, int pageSize);
	
	public int getUnSendInformationCountByUrl(String url);
	
}
