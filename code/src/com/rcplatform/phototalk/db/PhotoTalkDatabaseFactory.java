package com.rcplatform.phototalk.db;

import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.db.impl.PhotoTalkDb4oGlobalDatabase;
import com.rcplatform.phototalk.db.impl.PhotoTalkDb4oDatabase;
import com.rcplatform.phototalk.db.impl.RequestDb4oDatabase;

public class PhotoTalkDatabaseFactory {
	private static PhotoTalkDatabase mDatabase;

	public synchronized static void open(UserInfo userInfo) {
		if (mDatabase != null)
			mDatabase.close();
		mDatabase = new PhotoTalkDb4oDatabase(userInfo);
	}

	public static synchronized PhotoTalkDatabase getDatabase() {
		return mDatabase;
	}

	public static synchronized RequestDatabase getRequestDatabase() {
		return RequestDb4oDatabase.getInstance();
	}

	public static synchronized GlobalDatabase getGlobalDatabase() {
		return PhotoTalkDb4oGlobalDatabase.getInstance();
	}

	public static void close() {

	}
}
