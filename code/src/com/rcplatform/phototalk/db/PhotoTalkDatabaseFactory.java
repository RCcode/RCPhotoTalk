package com.rcplatform.phototalk.db;

import com.rcplatform.phototalk.bean.UserInfo;

public class PhotoTalkDatabaseFactory {
	private static PhotoTalkDatabase mDatabase;

	public static void open(UserInfo userInfo) {
		if (mDatabase != null)
			mDatabase.close();
		mDatabase = new PhotoTalkDb4oDatabase(userInfo);
	}

	public static PhotoTalkDatabase getDatabase() {
		return mDatabase;
	}

	public static void close() {

	}
}
