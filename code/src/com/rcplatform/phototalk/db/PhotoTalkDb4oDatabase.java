package com.rcplatform.phototalk.db;

import java.util.ArrayList;
import java.util.List;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.Information;
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
		db.delete(result);
		db.commit();
	}
}