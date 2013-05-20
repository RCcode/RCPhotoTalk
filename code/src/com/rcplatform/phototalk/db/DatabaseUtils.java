package com.rcplatform.phototalk.db;

import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.utils.Constants;

public class DatabaseUtils {

	public static final String BASE_DATABASE_NAME = "phototalk_database";
	public static final String DATABASE_REQUEST = "request";
	public static final String DATABSE_CONTACT = "global";

	public static String getDatabasePath(UserInfo userInfo) {
		return Constants.Database.USERS_DATABASE_PATH + "/" + BASE_DATABASE_NAME + "_" + userInfo.getRcId();
	}

	public static String getRequestDatabasePath() {
		return Constants.Database.REQUEST_DATABASE_PATH + "/" + BASE_DATABASE_NAME + "_" + DATABASE_REQUEST;
	}

	public static String getGlobalDatabasePath() {
		return Constants.Database.GLOBAL_DATABASE_PATH + "/" + BASE_DATABASE_NAME + "_" + DATABSE_CONTACT;
	}
}
