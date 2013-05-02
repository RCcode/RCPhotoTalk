package com.rcplatform.phototalk.utils;

import android.content.Context;

import com.rcplatform.phototalk.MenueApplication;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.bean.InfoRecord;

public class PhotoTalkUtils {

	/**
	 * 判断当前用户是不是发送者
	 * 
	 * @param context
	 * @param record
	 * @return true表示当前用户是发送者，false表示当前用户是接受者
	 */
	public static boolean isOwnerForReocrd(Context context, InfoRecord record) {

		if (String.valueOf(MenueApplication.getUserInfoInstall(context).getSuid()).equals(record.getSender().getSuid())) {
			return true;
		} else {
			return false;
		}
	}

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
}
