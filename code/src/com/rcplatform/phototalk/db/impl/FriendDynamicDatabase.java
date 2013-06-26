package com.rcplatform.phototalk.db.impl;

import java.util.ArrayList;
import java.util.List;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.AndroidSupport;
import com.db4o.config.EmbeddedConfiguration;
import com.rcplatform.phototalk.bean.FriendDynamic;
import com.rcplatform.phototalk.db.DatabaseUtils;

public class FriendDynamicDatabase {

	private static ObjectContainer db;

	public FriendDynamicDatabase() {
		EmbeddedConfiguration config = Db4oEmbedded.newConfiguration();
		config.common().add(new AndroidSupport());
		db = Db4oEmbedded.openFile(config, DatabaseUtils.getFriendDynamicDatabasePath());
	}

	public void saveFriendDynamics(List<FriendDynamic> list) {
		db.store(list);
		db.commit();
	}

	public List<FriendDynamic> getFriendDynamics() {
		ObjectSet<FriendDynamic> result = db.query(FriendDynamic.class);
		List<FriendDynamic> massages = new ArrayList<FriendDynamic>();
		massages.addAll(result);
		return massages;
	}

	public void clearAll() {
		List<FriendDynamic> massages =getFriendDynamics();
		int count = massages.size();
		for(int i =0 ;i<count ; i++){
			FriendDynamic massage = massages.get(i);
			db.delete(massage);
		}
		db.commit();
	}
}
