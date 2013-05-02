package com.rcplatform.phototalk.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PhotoTalkDatabaseHelper extends SQLiteOpenHelper {
	private final static int VERSION = 1;
	public final static String DATABASE_NAME = "photo_talk.db";

	public PhotoTalkDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, VERSION);
		// TODO Auto-generated constructor stub
	}

	public static final String CONTACT_TABLE_NAME = "contact_users";
	public static final String PLATFORM_APP_TABLE_NAME = "platform_apps";
	public static final String FRIEND_APP_TABLE = "friend_app";

	public synchronized void createContactTable(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ CONTACT_TABLE_NAME
				+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT,user_id TEXT,suid TEXT,nick TEXT,head_url TEXT,rc_id TEXT,phone TEXT,app_id INTEGER,rset INTEGER,signature TEXT)");
		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ PLATFORM_APP_TABLE_NAME
				+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT,app_id TEXT,app_name TEXT,package TEXT,icon_url TEXT)");
		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ FRIEND_APP_TABLE
				+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT,app_id TEXT,friend_suid TEXT)");
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		createContactTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}
}
