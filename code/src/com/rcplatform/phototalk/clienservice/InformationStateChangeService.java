package com.rcplatform.phototalk.clienservice;

import java.io.IOException;
import java.io.InputStream;
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
import com.rcplatform.phototalk.api.RCPlatformResponse;
import com.rcplatform.phototalk.bean.ServiceSimpleNotice;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.galhttprequest.LogUtil;
import com.rcplatform.phototalk.utils.Contract;
import com.rcplatform.phototalk.utils.PrefsUtils;

public class InformationStateChangeService extends IntentService {

	public static final String PARAM_KEY_INFORMATION = "information";
	private static final String SERVICE_NAME = "information_state_change";

	public InformationStateChangeService() {
		super(SERVICE_NAME);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		ServiceSimpleNotice[] infos = getNotices(intent);
		String action = intent.getAction();
		if (action.equals(Contract.Action.ACTION_INFORMATION_STATE_CHANGE)) {
			updateState(infos);
		} else if (action.equals(Contract.Action.ACTION_INFORMATION_DELETE)) {
			if (infos != null)
				deleteInformation(infos);
			else
				deleteAllInformation();
		}

	}

	private void deleteAllInformation() {
		try {
			HttpPost post = new HttpPost(MenueApiUrl.NOTICE_CLEAR_URL);
			post.setEntity(getClearInformationEntity());
			HttpClient client = new DefaultHttpClient();
			HttpResponse res = client.execute(post);
			if (isRequestSuccess(res)) {
				LogUtil.e("clear informations success");
			} else {
				LogUtil.e("clear informations fail");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean isRequestSuccess(HttpResponse response) throws Exception {
		if (response.getStatusLine().getStatusCode() == 200) {
			InputStream in = response.getEntity().getContent();
			String content = readContent(in);
			JSONObject jsonObject = new JSONObject(content);
			int status = jsonObject.getInt(RCPlatformResponse.ResponseStatus.RESPONSE_KEY_STATUS);
			if (status == 0)
				return true;
		}
		return false;
	}

	private String readContent(InputStream in) throws IOException {
		StringBuilder sb = new StringBuilder();
		int len = 0;
		byte[] buffer = new byte[1024];
		if ((len = in.read(buffer)) != -1) {
			sb.append(new String(buffer, 0, len));
		}
		in.close();
		return sb.toString();
	}

	private ServiceSimpleNotice[] getNotices(Intent intent) {
		if (intent.hasExtra(PARAM_KEY_INFORMATION)) {
			Object[] infos = (Object[]) intent.getSerializableExtra(PARAM_KEY_INFORMATION);
			ServiceSimpleNotice[] simpleInfo = new ServiceSimpleNotice[infos.length];
			for (int i = 0; i < infos.length; i++) {
				ServiceSimpleNotice info = (ServiceSimpleNotice) infos[i];
				simpleInfo[i] = info;
			}
			return simpleInfo;
		}
		return null;
	}

	private void deleteInformation(ServiceSimpleNotice... simpleInfo) {
		try {
			HttpPost post = new HttpPost(MenueApiUrl.NOTICE_DELETE_URL);
			post.setEntity(getEntity(simpleInfo));
			HttpClient client = new DefaultHttpClient();
			HttpResponse res = client.execute(post);
			if (isRequestSuccess(res)) {
				LogUtil.e("DELETE state success");
			} else {
				LogUtil.e("delete state fail");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateState(ServiceSimpleNotice... simpleInfo) {
		try {
			HttpPost post = new HttpPost(MenueApiUrl.NOTICE_STATE_CHANGE_URL);
			post.setEntity(getEntity(simpleInfo));
			HttpClient client = new DefaultHttpClient();
			HttpResponse res = client.execute(post);
			if (isRequestSuccess(res)) {
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

	public StringEntity getClearInformationEntity() throws Exception {
		UserInfo userInfo = ((MenueApplication) getApplication()).getCurrentUser();
		JSONObject jsonParams = new JSONObject();
		jsonParams.put(PhotoTalkParams.PARAM_KEY_APP_ID, PhotoTalkParams.PARAM_VALUE_APP_ID);
		jsonParams.put(PhotoTalkParams.PARAM_KEY_DEVICE_ID, PhotoTalkParams.PARAM_VALUE_DEVICE_ID);
		jsonParams.put(PhotoTalkParams.PARAM_KEY_LANGUAGE, PhotoTalkParams.PARAM_VALUE_LANGUAGE);
		jsonParams.put(PhotoTalkParams.PARAM_KEY_TOKEN, userInfo.getToken());
		jsonParams.put(PhotoTalkParams.PARAM_KEY_USER_ID, userInfo.getSuid());
		jsonParams.put(PhotoTalkParams.ClearInformation.PARAM_KEY_NOTICE_ID, PrefsUtils.User.getUserMaxRecordInfoId(getApplicationContext(), userInfo.getEmail()));
		return new StringEntity(jsonParams.toString(), "UTF-8");
	}
}