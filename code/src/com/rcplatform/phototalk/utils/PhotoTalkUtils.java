package com.rcplatform.phototalk.utils;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;

import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.bean.AppInfo;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.galhttprequest.MD5;

public class PhotoTalkUtils {

	public static String getSexString(Context context, int sex) {
		String result = null;
		switch (sex) {
		case 0:
			result = context.getString(R.string.sex_secret);
			break;
		case 1:
			result = context.getString(R.string.male);
			break;
		case 2:
			result = context.getString(R.string.famale);
			break;
		}
		return result;
	}

	public static String getFilePath(String url) {
		StringBuilder sbPath = new StringBuilder();
		sbPath.append(Constants.PhotoInformationCache.FILE_PATH).append("/").append(MD5.encodeMD5String(url));
		return sbPath.toString();
	}

	public static String getUnZipDirPath(String url) {
		return getFilePath(url) + Constants.PhotoInformationCache.UNZIP_SUFFIX;
	}

	public static String getInformationTagBase(Information information) {
		return information.getReceiver().getRcId() + "|" + information.getSender().getRcId() + "|" + information.getCreatetime();
	}

	public static Friend userToFriend(UserInfo userInfo) {
		Friend friend = new Friend();
		friend.setRcId(userInfo.getRcId());
		friend.setBackground(userInfo.getBirthday());
		friend.setGender(userInfo.getGender());
		friend.setAppList(new ArrayList<AppInfo>(Constants.userApps.keySet()));
		friend.setCellPhone(userInfo.getCellPhone());
		friend.setHeadUrl(userInfo.getHeadUrl());
		friend.setBackground(userInfo.getBackground());
		friend.setFriend(true);
		friend.setNickName(userInfo.getNickName());
		return friend;
	}

	public static File getUserHead(UserInfo userInfo) {
		return new File(Constants.USER_IMAGE_DIR, userInfo.getRcId() + "/" + "head");
	}

	public static File getUserBackground(UserInfo userInfo) {
		return new File(Constants.USER_IMAGE_DIR, userInfo.getRcId() + "/" + "background");
	}
}
