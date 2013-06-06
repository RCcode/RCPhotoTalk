package com.rcplatform.videotalk.db;

import com.rcplatform.videotalk.bean.UserInfo;
import com.rcplatform.videotalk.db.impl.CountryCodeSQLiteDatabase;
import com.rcplatform.videotalk.db.impl.PhotoTalkDb4oDatabase;
import com.rcplatform.videotalk.db.impl.PhotoTalkDb4oGlobalDatabase;
import com.rcplatform.videotalk.db.impl.RequestDb4oDatabase;

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
	
	public static synchronized CountryCodeDatabase getCountryCodeDatabase(){
		return new CountryCodeSQLiteDatabase();
	}
	public static void close() {

	}
}
