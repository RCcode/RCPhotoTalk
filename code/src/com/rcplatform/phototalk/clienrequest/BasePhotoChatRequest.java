package com.rcplatform.phototalk.clienrequest;

import java.util.Locale;

import android.content.Context;

import com.rcplatform.phototalk.MenueApplication;
import com.rcplatform.phototalk.api.MenueApiFactory;
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
		currentUser = ((MenueApplication) context.getApplicationContext()).getCurrentUser();
		request = GalHttpRequest.requestWithURL(context, url);
	}

	@Override
	public void createBody() {
		request.setPostValueForKey(MenueApiFactory.TOKEN, currentUser.getToken());
		request.setPostValueForKey(MenueApiFactory.USERID, currentUser.getRcId());
		request.setPostValueForKey(MenueApiFactory.LANGUAGE, Locale.getDefault().getLanguage());
		request.setPostValueForKey(MenueApiFactory.DEVICE_ID, android.os.Build.DEVICE);
		request.setPostValueForKey(MenueApiFactory.APP_ID, Constants.APP_ID);
	}

	@Override
	public void postRequest() {
		createBody();
		request.startAsynRequestString(callBack);
	}

}
