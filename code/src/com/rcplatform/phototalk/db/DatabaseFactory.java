package com.rcplatform.phototalk.db;

import com.rcplatform.phototalk.MenueApplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseFactory {

	public static final String RECORD_ID = "record_id";

	public static final String CONTACT_USERS_TABLE_NAME = "contact_users";

	public static final String USER_RECORD_TABLE_NAME = "user_record";

	public static final String FRIEND_TABLE_NAME = "friends";

	public static final String RECORD_S_USER_ID = "s_user_id";

	public static final String RECORD_S_USER_SUID = "s_user_suid";

	public static final String RECORD_S_USER_NICK = "s_user_nick";

	public static final String RECORD_S_USER_HEAD = "s_user_head";

	public static final String RECORD_R_USER_ID = "r_user_id";

	public static final String RECORD_R_USER_SUID = "r_user_suid";

	public static final String RECORD_R_USER_NICK = "r_user_nick";

	public static final String RECORD_R_USER_HEAD = "r_user_head";

	public static final String RECORD_TYPE = "type";

	public static final String RECORD_CREATE_TIME = "create_time";

	public static final String RECORD_UPDATE_TIME = "update_time";

	public static final String RECORD_STATU = "statu";

	public static final String RECORD_URL = "url";

	public static final String RECORD_LIMIT_TIME = "limit_time";

	public static final String RECORD_NOTICE_ID = "notice_id";

	public static final String FAIL_REQUEST = "fail_httpreauest";

	// public String FAIL_REQUEST_TABLE = "";

	public static final String REQUEST_ID = "request_id";

	public static final String REQUEST_URL = "request_url";

	public static final String REQUEST_PARAMS = "request_params";

	public static final String FACEBOOK_FRIEND_TABLE = "facebook";

	private SQLiteDatabase mDatabase;
	private static DatabaseFactory mFactory;

	private DatabaseFactory(Context context) {
		if (mDatabase == null) {
			mDatabase = new PhotoTalkDatabaseHelper(context).getWritableDatabase();
		}
	}

	public static DatabaseFactory getInstance(Context context) {
		if (mFactory == null)
			mFactory = new DatabaseFactory(context);
		return mFactory;
	}

	public synchronized void createTables(Context context) {
		String userId = "";
		String USER_RECORD = USER_RECORD_TABLE_NAME + "_" + userId;
		// 为每个用户创建单独的消息记录表
		mDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + USER_RECORD + "(" + RECORD_ID + " TEXT PRIMARY KEY," + RECORD_S_USER_ID + " TEXT NOT NULL," + RECORD_S_USER_SUID + " TEXT NOT NULL," + RECORD_S_USER_NICK + " TEXT," + RECORD_S_USER_HEAD + " TEXT," + RECORD_R_USER_ID + " TEXT NOT NULL," + RECORD_R_USER_SUID + " TEXT NOT NULL," + RECORD_R_USER_NICK + " TEXT," + RECORD_R_USER_HEAD + " TEXT," + RECORD_TYPE + " TEXT NOT NULL," + RECORD_CREATE_TIME + " TEXT NOT NULL," + RECORD_UPDATE_TIME + " TEXT," + RECORD_STATU + " TEXT NOT NULL," + RECORD_URL + " TEXT," + RECORD_LIMIT_TIME + " TEXT," + RECORD_NOTICE_ID + " TEXT)");

		String FAIL_REQUEST_TABLE = FAIL_REQUEST + "_" + userId;
		mDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + FAIL_REQUEST_TABLE + " ( " + REQUEST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," + REQUEST_URL + " TEXT NOT NULL," + REQUEST_PARAMS + " TEXT )");

		String FRIENDS_TABLE = FRIEND_TABLE_NAME + "_" + userId;
		mDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + FRIENDS_TABLE + " (_id INTEGER PRIMARY KEY AUTOINCREMENT,suid TEXT,rcid TEXT,name TEXT,head_url TEXT,phone TEXT,user_from INTEGER,mark TEXT)");
		String FACEBOOK_TABLE = FACEBOOK_FRIEND_TABLE + "_" + userId;
		mDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + FACEBOOK_TABLE + " (_id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT,facebook_id TEXT)");
	}

	public synchronized SQLiteDatabase getDatabase() {
		return mDatabase;
	}
}
