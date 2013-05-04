package com.rcplatform.phototalk.clienservice;

import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rcplatform.phototalk.MenueApplication;
import com.rcplatform.phototalk.api.MenueApiUrl;
import com.rcplatform.phototalk.api.PhotoTalkParams;
import com.rcplatform.phototalk.bean.ServiceSimpleNotice;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.galhttprequest.LogUtil;

public class InformationStateChangeService extends IntentService {

	public static final String PARAM_KEY_INFORMATION = "information";
	private static final String SERVICE_NAME = "information_state_change";

	public InformationStateChangeService() {
		super(SERVICE_NAME);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Object[] infos = (Object[]) intent.getSerializableExtra(PARAM_KEY_INFORMATION);
		ServiceSimpleNotice[] simpleInfo = new ServiceSimpleNotice[infos.length];
		for (int i = 0; i < infos.length; i++) {
			ServiceSimpleNotice info = (ServiceSimpleNotice) infos[i];
			simpleInfo[i] = info;
		}
		updateState(simpleInfo);
	}

	private void updateState(ServiceSimpleNotice... simpleInfo) {
		try {
			HttpPost post = new HttpPost(MenueApiUrl.NOTICE_STATE_CHANGE_URL);
			post.setEntity(getEntity(simpleInfo));
			HttpClient client = new DefaultHttpClient();
			HttpResponse res = client.execute(post);
			if (res.getStatusLine().getStatusCode() == 200) {
				LogUtil.e("update state success");
			} else {
				LogUtil.e("update state fail");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public StringEntity getEntity(ServiceSimpleNotice... simpleInfo) throws JSONException, UnsupportedEncodingException {
		UserInfo userInfo = ((MenueApplication) getApplication()).getCurrentUser();
		JSONObject jsonParams = new JSONObject();
		jsonParams.put(PhotoTalkParams.PARAM_KEY_APP_ID, PhotoTalkParams.PARAM_VALUE_APP_ID);
		jsonParams.put(PhotoTalkParams.PARAM_KEY_DEVICE_ID, PhotoTalkParams.PARAM_VALUE_DEVICE_ID);
		jsonParams.put(PhotoTalkParams.PARAM_KEY_LANGUAGE, PhotoTalkParams.PARAM_VALUE_LANGUAGE);
		jsonParams.put(PhotoTalkParams.PARAM_KEY_TOKEN, userInfo.getToken());
		jsonParams.put(PhotoTalkParams.PARAM_KEY_USER_ID, userInfo.getSuid());
		Gson gson = new Gson();
		JSONArray arrayInfos = new JSONArray(gson.toJson(simpleInfo, new TypeToken<ServiceSimpleNotice[]>() {
		}.getType()));
		jsonParams.put(PhotoTalkParams.InformationStateChange.PARAM_KEY_INFOS, arrayInfos);
		return new StringEntity(jsonParams.toString(), "UTF-8");
	}
}
