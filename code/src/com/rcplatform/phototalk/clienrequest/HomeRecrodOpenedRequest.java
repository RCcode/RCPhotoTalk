package com.rcplatform.phototalk.clienrequest;

import java.util.Map;

import android.content.Context;

import com.rcplatform.phototalk.galhttprequest.GalHttpRequest.GalHttpLoadTextCallBack;

public class HomeRecrodOpenedRequest extends BasePhotoChatRequest {

    protected Map<String, String> params;

    public HomeRecrodOpenedRequest(Context context, GalHttpLoadTextCallBack callBack, Map<String, String> params, String url) {
        super(context, callBack, url);
        this.params = params;
    }

    @Override
    public void createBody() {
        super.createBody();
        this.request.getPostData().putAll(params);
    }

}
