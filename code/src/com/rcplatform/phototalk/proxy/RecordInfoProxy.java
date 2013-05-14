package com.rcplatform.phototalk.proxy;

import java.util.List;

import com.rcplatform.phototalk.MenueApplication;
import com.rcplatform.phototalk.api.MenueApiUrl;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.request.PhotoTalkParams;
import com.rcplatform.phototalk.request.RCPlatformAsyncHttpClient;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.request.RCPlatformAsyncHttpClient.RequestAction;
import com.rcplatform.phototalk.utils.PrefsUtils;

import android.content.Context;

public class RecordInfoProxy {
	public static List<Information> getAllRecordInfos(Context context,
			RCPlatformResponseHandler responseHandler) {
		RCPlatformAsyncHttpClient client = new RCPlatformAsyncHttpClient();
		PhotoTalkParams.buildBasicParams(context, client);
		client.putRequestParam(PhotoTalkParams.RecordInfo.PARAM_MAX_RECORD_ID,
				getMaxRecordInfoId(context) + "");
		client.post(context, MenueApiUrl.USER_NOTICES_URL, responseHandler);
		return null;
	}

	private static int getMaxRecordInfoId(Context context) {
		return PrefsUtils.User.getUserMaxRecordInfoId(context,
				((MenueApplication) context.getApplicationContext())
						.getCurrentUser().getEmail());
	}
}
