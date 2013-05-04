package com.rcplatform.phototalk.utils;

import android.content.Context;
import android.content.Intent;

import com.rcplatform.phototalk.MenueApplication;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.ServiceSimpleNotice;
import com.rcplatform.phototalk.clienservice.InformationStateChangeService;
import com.rcplatform.phototalk.galhttprequest.MD5;

public class PhotoTalkUtils {

	/**
	 * 判断当前用户是不是发送者
	 * 
	 * @param context
	 * @param record
	 * @return true表示当前用户是发送者，false表示当前用户是接受者
	 */
	public static boolean isSender(Context context, Information record) {

		if (((MenueApplication) context.getApplicationContext()).getCurrentUser().getSuid().equals(record.getSender().getSuid()))
			return true;
		return false;
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

	public static String getFilePath(Context context, String url) {
		StringBuilder sbPath = new StringBuilder();
		sbPath.append(Contract.FILE_PATH).append("/").append(MD5.encodeMD5String(url));
		return sbPath.toString();
	}

	public static void updateInformationState(Context context, String action, ServiceSimpleNotice... infos) {
		Intent intent = new Intent(context, InformationStateChangeService.class);
		intent.putExtra(InformationStateChangeService.PARAM_KEY_INFORMATION, infos);
		intent.setAction(action);
		context.startService(intent);
	}
}
