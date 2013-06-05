package com.rcplatform.phototalk.clienservice;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
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
import com.rcplatform.phototalk.PhotoTalkApplication;
import com.rcplatform.phototalk.api.PhotoTalkApiUrl;
import com.rcplatform.phototalk.bean.ServiceCensus;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.galhttprequest.LogUtil;
import com.rcplatform.phototalk.request.PhotoTalkParams;
import com.rcplatform.phototalk.request.RCPlatformResponse;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.PrefsUtils;
import com.rcplatform.phototalk.utils.Utils;

public class CensusService extends IntentService {

	public static final String PARAM_KEY_INFORMATION = "information";
	private static final String SERVICE_NAME = "information_state_change";

	public CensusService() {
		super(SERVICE_NAME);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		ServiceCensus[] infos = getNotices(intent);
		try {
			sendRequest(PhotoTalkApiUrl.INFORMATION_CENSUS_URL, getEntity(infos));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendRequest(String url, HttpEntity entity) {
		try {
			HttpPost post = new HttpPost(url);
			post.setEntity(entity);
			HttpClient client = new DefaultHttpClient();
			HttpResponse res = client.execute(post);
			if (isRequestSuccess(res)) {
				LogUtil.e("update informations success");
			} else {
				LogUtil.e("update informations fail");
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
			if (status == RCPlatformResponse.ResponseStatus.RESPONSE_VALUE_SUCCESS)
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

	private ServiceCensus[] getNotices(Intent intent) {
		if (intent.hasExtra(PARAM_KEY_INFORMATION)) {
			Object[] infos = (Object[]) intent.getSerializableExtra(PARAM_KEY_INFORMATION);
			ServiceCensus[] simpleInfo = new ServiceCensus[infos.length];
			for (int i = 0; i < infos.length; i++) {
				ServiceCensus info = (ServiceCensus) infos[i];
				simpleInfo[i] = info;
			}
			return simpleInfo;
		}
		return null;
	}

	public StringEntity getEntity(ServiceCensus... simpleInfo) throws JSONException, UnsupportedEncodingException {
		UserInfo userInfo = ((PhotoTalkApplication) getApplication()).getCurrentUser();
		JSONObject jsonParams = new JSONObject();
		jsonParams.put(PhotoTalkParams.PARAM_KEY_APP_ID, PhotoTalkParams.PARAM_VALUE_APP_ID);
		jsonParams.put(PhotoTalkParams.PARAM_KEY_DEVICE_ID, PhotoTalkParams.PARAM_VALUE_DEVICE_ID);
		jsonParams.put(PhotoTalkParams.PARAM_KEY_LANGUAGE, PhotoTalkParams.PARAM_VALUE_LANGUAGE);
		jsonParams.put(PhotoTalkParams.PARAM_KEY_TOKEN, userInfo.getToken());
		jsonParams.put(PhotoTalkParams.PARAM_KEY_USER_ID, userInfo.getRcId());
		jsonParams.put(PhotoTalkParams.ServiceCensus.PARAM_KEY_COUNTRY, Constants.COUNTRY);
		jsonParams.put(PhotoTalkParams.ServiceCensus.PARAM_KEY_OS, Constants.OS_NAME);
		jsonParams.put(PhotoTalkParams.ServiceCensus.PARAM_KEY_OS_VERSION, Constants.OS_VERSION);
		jsonParams.put(PhotoTalkParams.ServiceCensus.PARAM_KEY_TIMEZONE, Utils.getTimeZoneId(this));
		Gson gson = new Gson();
		JSONArray arrayInfos = new JSONArray(gson.toJson(simpleInfo, new TypeToken<ServiceCensus[]>() {
		}.getType()));
		jsonParams.put(PhotoTalkParams.ServiceCensus.PARAM_KEY_INFOS, arrayInfos);
		return new StringEntity(jsonParams.toString(), "UTF-8");
	}
}
