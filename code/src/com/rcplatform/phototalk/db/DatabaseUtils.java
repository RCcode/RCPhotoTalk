package com.rcplatform.phototalk.db;

import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.utils.Constants;

public class DatabaseUtils {

	public static final String BASE_DATABASE_NAME = "pt";
	public static final String DATABASE_REQUEST = "request.db";
	public static final String DATABSE_CONTACT = "global.db";
	public static final String DATABASE_COUNTRY = "country_code.db";

	public static String getDatabasePath(UserInfo userInfo) {
		return Constants.Database.USERS_DATABASE_PATH + "/" + BASE_DATABASE_NAME + "_" + userInfo.getRcId() + ".db";
	}

	public static String getRequestDatabasePath() {
		return Constants.Database.REQUEST_DATABASE_PATH + "/" + BASE_DATABASE_NAME + "_" + DATABASE_REQUEST;
	}

	public static String getGlobalDatabasePath() {
		return Constants.Database.GLOBAL_DATABASE_PATH + "/" + BASE_DATABASE_NAME + "_" + DATABSE_CONTACT;
	}

	public static String getCountryCodeDatabasePath() {
		return Constants.Database.GLOBAL_DATABASE_PATH + "/" + BASE_DATABASE_NAME + "_" + DATABASE_COUNTRY;
	}
}
