package com.rcplatform.phototalk.clienrequest;

import java.util.Locale;

import android.content.Context;

import com.rcplatform.phototalk.MenueApplication;
import com.rcplatform.phototalk.api.MenueApiFactory;
import com.rcplatform.phototalk.galhttprequest.GalHttpRequest;
import com.rcplatform.phototalk.galhttprequest.GalHttpRequest.GalHttpLoadTextCallBack;
import com.rcplatform.phototalk.utils.Contract;

public class BasePhotoChatRequest implements BasePhotoChatRequestInterface {

    protected Context context;

    protected GalHttpLoadTextCallBack callBack;

    protected GalHttpRequest request;

    public BasePhotoChatRequest(Context context, GalHttpLoadTextCallBack callBack, String url) {
        super();
        this.context = context;
        this.callBack = callBack;
        request = GalHttpRequest.requestWithURL(context, url);
    }

    @Override
    public void createBody() {
        request.setPostValueForKey(MenueApiFactory.TOKEN, MenueApplication.getUserInfoInstall(context).getToken());
        request.setPostValueForKey(MenueApiFactory.USERID, MenueApplication.getUserInfoInstall(context).getSuid());
        request.setPostValueForKey(MenueApiFactory.LANGUAGE, Locale.getDefault().getLanguage());
        request.setPostValueForKey(MenueApiFactory.DEVICE_ID, android.os.Build.DEVICE);
        request.setPostValueForKey(MenueApiFactory.APP_ID, Contract.APP_ID);
    }

    @Override
    public void postRequest() {
        createBody();
        request.startAsynRequestString(callBack);
    }

}
