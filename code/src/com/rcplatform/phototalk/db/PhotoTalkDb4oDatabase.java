package com.rcplatform.phototalk.db;

import java.util.List;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.thirdpart.bean.ThirdPartFriend;
import com.rcplatform.phototalk.thirdpart.utils.ThirdPartUtils;

public class PhotoTalkDb4oDatabase implements PhotoTalkDatabase {

	private ObjectContainer db;

	public PhotoTalkDb4oDatabase(UserInfo userInfo) {
		// TODO Auto-generated constructor stub
		db = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), DatabaseUtils.getDatabasePath(userInfo));
	}

	@Override
	public void saveThirdPartFriends(List<ThirdPartFriend> thirdPartFriends) {
		// TODO Auto-generated method stub
		db.store(thirdPartFriends);
		db.commit();
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		db.close();
	}

	@Override
	public List<Friend> getThirdPartFriends(int type) {
		// TODO Auto-generated method stub
		ThirdPartFriend example = new ThirdPartFriend();
		example.setType(type);
		ObjectSet<ThirdPartFriend> friends = db.queryByExample(example);
		return ThirdPartUtils.parserToFriends(friends, type);
	}

}
