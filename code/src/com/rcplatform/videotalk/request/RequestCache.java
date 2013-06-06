package com.rcplatform.videotalk.request;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RequestCache {

	private String filePath;
	private String params;
	private String requestUrl;
	private long createTime;

	public RequestCache() {
	}

	public RequestCache(Request request) {
		if (request.getFile() != null)
			this.filePath = request.getFile().getPath();
		this.requestUrl = request.getUrl();
		setAllParams(request.getParams());
		this.createTime = request.getCreateTime();
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getRequestUrl() {
		return requestUrl;
	}

	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}

	public void setAllParams(Map<String, String> params) {
		try {
			JSONArray paramsArray = new JSONArray();
			for (String key : params.keySet()) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("key", key);
				jsonObject.put("value", params.get(key));
				paramsArray.put(jsonObject);
			}
			this.params = paramsArray.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public Map<String, String> getAllParams() {
		Map<String, String> requestParams = new HashMap<String, String>();
		try {
			JSONArray paramsArray = new JSONArray(params);
			for (int i = 0; i < paramsArray.length(); i++) {
				JSONObject jsonObject = paramsArray.getJSONObject(i);
				requestParams.put(jsonObject.getString("key"), jsonObject.getString("value"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return requestParams;
	}

}
