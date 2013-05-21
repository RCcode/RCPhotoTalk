package com.rcplatform.phototalk.clienrequest;

import java.util.Locale;

import android.content.Context;

import com.rcplatform.phototalk.PhotoTalkApplication;
import com.rcplatform.phototalk.api.PhotoTalkApiFactory;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.galhttprequest.GalHttpRequest;
import com.rcplatform.phototalk.galhttprequest.GalHttpRequest.GalHttpLoadTextCallBack;
import com.rcplatform.phototalk.utils.Constants;

public class BasePhotoChatRequest implements BasePhotoChatRequestInterface {

	protected Context context;

	protected GalHttpLoadTextCallBack callBack;

	protected GalHttpRequest request;

	private UserInfo currentUser;

	public BasePhotoChatRequest(Context context, GalHttpLoadTextCallBack callBack, String url) {
		super();
		this.context = context;
		this.callBack = callBack;
		currentUser = ((PhotoTalkApplication) context.getApplicationContext()).getCurrentUser();
		request = GalHttpRequest.requestWithURL(context, url);
	}

	@Override
	public void createBody() {
		request.setPostValueForKey(PhotoTalkApiFactory.TOKEN, currentUser.getToken());
		request.setPostValueForKey(PhotoTalkApiFactory.USERID, currentUser.getRcId());
		request.setPostValueForKey(PhotoTalkApiFactory.LANGUAGE, Locale.getDefault().getLanguage());
		request.setPostValueForKey(PhotoTalkApiFactory.DEVICE_ID, android.os.Build.DEVICE);
		request.setPostValueForKey(PhotoTalkApiFactory.APP_ID, Constants.APP_ID);
	}

	@Override
	public void postRequest() {
		createBody();
		request.startAsynRequestString(callBack);
	}

}
