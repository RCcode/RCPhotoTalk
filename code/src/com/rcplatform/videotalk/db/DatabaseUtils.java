package com.rcplatform.videotalk.db;

import com.rcplatform.videotalk.bean.UserInfo;
import com.rcplatform.videotalk.utils.Constants;

public class DatabaseUtils {

	public static final String BASE_DATABASE_NAME = "pt";
	public static final String DATABASE_REQUEST = "request.db";
	public static final String DATABSE_CONTACT = "global.db";
	public static final String DATABASE_COUNTRY = "country_code.db";
	public static final String DATABASE_TIGASE_MSG = "tigase_msg.db";

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
	
	public static String getTigaseMsgDatabasePath(){
		return  Constants.Database.GLOBAL_DATABASE_PATH + "/" + BASE_DATABASE_NAME + "_" + DATABASE_TIGASE_MSG;
	}
}
