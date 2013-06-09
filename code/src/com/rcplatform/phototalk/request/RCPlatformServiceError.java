package com.rcplatform.phototalk.request;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.rcplatform.phototalk.R;

public class RCPlatformServiceError {
	public static final int ERROR_CODE_REQUEST_FAIL = -100;
	public static final int ERROR_CODE_FRIEND_ALREADY_ADDED = 21301;
	private static Map<Integer, Integer> errorMessage = new HashMap<Integer, Integer>();
	static {
		errorMessage.put(ERROR_CODE_REQUEST_FAIL, R.string.net_error);
	}

	public static String getErrorMessage(Context context, int errorCode) {
		return context.getString(errorMessage.get(Integer.valueOf(errorCode)));
	}
}
