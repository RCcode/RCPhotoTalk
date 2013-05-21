package com.rcplatform.phototalk.utils;

import android.content.Context;

import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.bean.Information;
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
	
	public static String getInformationTagBase(Information information){
			return information.getReceiver().getRcId()+"|"+information.getSender().getRcId()+"|"+information.getCreatetime();
	}
}
