package com.rcplatform.phototalk.db;

import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.utils.Contract;

public class DatabaseUtils {

	public static final String BASE_DATABASE_NAME = "phototalk_database";
	public static final String DATABASE_REQUEST = "request";

	public static String getDatabasePath(UserInfo userInfo) {
		return Contract.Database.DATABASE_PATH + "/" + BASE_DATABASE_NAME + "_" + userInfo.getRcId();
	}

	public static String getRequestDatabasePath() {
		return Contract.Database.REQUEST_DATABASE_PATH + "/" + BASE_DATABASE_NAME + "_" + DATABASE_REQUEST;
	}
}
