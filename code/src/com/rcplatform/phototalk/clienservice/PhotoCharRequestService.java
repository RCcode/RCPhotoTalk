package com.rcplatform.phototalk.clienservice;

import java.util.Locale;
import java.util.Map;

import android.content.Context;

import com.rcplatform.phototalk.MenueApplication;
import com.rcplatform.phototalk.api.MenueApiFactory;
import com.rcplatform.phototalk.api.PhotoTalkParams;
import com.rcplatform.phototalk.api.RCPlatformAsyncHttpClient;
import com.rcplatform.phototalk.api.RCPlatformAsyncHttpClient.RequestAction;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.galhttprequest.GalHttpRequest;
import com.rcplatform.phototalk.galhttprequest.GalHttpRequest.GalHttpLoadTextCallBack;
import com.rcplatform.phototalk.galhttprequest.GalHttpRequest.PhotoChatHttpLoadTextCallBack;
import com.rcplatform.phototalk.utils.Contract;

public class PhotoCharRequestService {

    private static PhotoCharRequestService service;

    private PhotoCharRequestService() {

    }

    public static PhotoCharRequestService getInstence() {
        if (service == null) {
            service = new PhotoCharRequestService();
        }
        return service;
    }

    // 获取主界面notice 的request

    public void postRequest(Context context, GalHttpLoadTextCallBack callBack, Map<String, String> params, String url) {
        GalHttpRequest request = GalHttpRequest.requestWithURL(context, url);
        RCPlatformAsyncHttpClient client=new RCPlatformAsyncHttpClient(RequestAction.JSON);
        PhotoTalkParams.buildBasicParams(context, client);
        if (params != null)
            request.getPostData().putAll(params);
        request.startAsynRequestString(callBack);
    }

    public void postRequestByTimestamp(Context context, PhotoChatHttpLoadTextCallBack callBack, Map<String, String> params, String url, long time,
            byte[] data) {
    	UserInfo currentUser=((MenueApplication)context.getApplicationContext()).getCurrentUser();
        GalHttpRequest request = GalHttpRequest.requestWithURL(context, url);
        request.setPostValueForKey(MenueApiFactory.TOKEN, currentUser.getToken());
        request.setPostValueForKey(MenueApiFactory.USERID,currentUser.getSuid());
        request.setPostValueForKey(MenueApiFactory.LANGUAGE, Locale.getDefault().getLanguage());
        request.setPostValueForKey(MenueApiFactory.DEVICE_ID, android.os.Build.DEVICE);
        request.setPostValueForKey(MenueApiFactory.APP_ID, Contract.APP_ID);
        request.getPostData().putAll(params);
        request.startAsynUploadImage(callBack, time, data);
    }
}
