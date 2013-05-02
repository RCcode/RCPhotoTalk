package com.rcplatform.phototalk.db;

import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.utils.Contract;

public class DatabaseUtils {

	public static final String BASE_DATABASE_NAME = "phototalk_database";

	public static String getDatabasePath(UserInfo userInfo) {
		return Contract.DATABASE_PATH + "/" + BASE_DATABASE_NAME + "_" + userInfo.getEmail();
	}
}
