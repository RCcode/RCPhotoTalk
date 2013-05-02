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
		db = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(),
				DatabaseUtils.getDatabasePath(userInfo));
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

}
