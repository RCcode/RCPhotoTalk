package com.rcplatform.phototalk.db;

import java.util.ArrayList;
import java.util.List;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.InformationState;
import com.rcplatform.phototalk.bean.InformationType;
import com.rcplatform.phototalk.bean.RecordUser;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.thirdpart.bean.ThirdPartFriend;
import com.rcplatform.phototalk.thirdpart.utils.ThirdPartUtils;

public class PhotoTalkDb4oDatabase implements PhotoTalkDatabase {

	private ObjectContainer db;

	public PhotoTalkDb4oDatabase(UserInfo userInfo) {
		db = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), DatabaseUtils.getDatabasePath(userInfo));
	}

	@Override
	public void saveThirdPartFriends(List<ThirdPartFriend> thirdPartFriends) {
		db.store(thirdPartFriends);
		db.commit();
	}

	@Override
	public void close() {
		db.close();
	}

	@Override
	public List<Friend> getThirdPartFriends(int type) {
		ThirdPartFriend example = new ThirdPartFriend();
		example.setType(type);
		ObjectSet<ThirdPartFriend> friends = db.queryByExample(example);
		return ThirdPartUtils.parserToFriends(friends, type);
	}

	@Override
	public void saveRecordInfos(List<Information> recordInfos) {
		db.store(recordInfos);
		db.commit();
	}

	@Override
	public List<Information> getRecordInfos() {
		ObjectSet<Information> infos = db.query(Information.class);
		List<Information> result = new ArrayList<Information>();
		for (Information info : infos) {
			result.add(info);
		}
		return result;
	}

	@Override
	public boolean hasFriend(String suid) {
		Friend example = new Friend();
		example.setSuid(suid);
		ObjectSet<Friend> result = db.queryByExample(example);
		if (result.size() > 0) {
			return true;
		}
		return false;
	}

	private void updateInformationState(Information information) {
		Information infoExample = new Information();
		infoExample.setRecordId(information.getRecordId());
		ObjectSet<Information> result = db.queryByExample(infoExample);
		if (result.size() > 0) {
			Information infoLocal = result.next();
			infoLocal.setStatu(information.getStatu());
			db.store(infoLocal);
		}
	}

	@Override
	public void updateInformationState(Information... informations) {
		for (Information info : informations) {
			updateInformationState(info);
		}
		if (informations.length > 0)
			db.commit();
	}

	@Override
	public void deleteInformation(Information information) {
		Information infoExample = new Information();
		infoExample.setRecordId(information.getRecordId());
		ObjectSet<Information> result = db.queryByExample(infoExample);
		if (result.size() > 0) {
			Information info = result.next();
			db.delete(info);
			db.commit();
		}
	}

	@Override
	public void clearInformation() {
		ObjectSet<Information> result = db.query(Information.class);
		for (Information info : result) {
			db.delete(info);
		}
		db.commit();
	}

	@Override
	public void updateFriendRequestInformationByFriend(Friend friend) {
		Information infoExample = new Information();
		infoExample.setSender(new RecordUser(friend.getSuid(), null, null));
		infoExample.setType(InformationType.TYPE_FRIEND_REQUEST_NOTICE);
		ObjectSet<Information> infos = db.queryByExample(infoExample);
		if (infos.size() > 0) {
			List<Information> infoCaches=new ArrayList<Information>();
			for (Information infoCache : infos) {
				infoCache.setStatu(InformationState.STATU_QEQUEST_ADDED);
				infoCaches.add(infoCache);
			}
			db.store(infoCaches);
		}
	}
}
