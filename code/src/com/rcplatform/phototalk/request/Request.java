package com.rcplatform.phototalk.request;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class Request implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long createTime;
	private String url;
	private RCPlatformResponseHandler responseHandler;
	private Map<String, String> params=new HashMap<String, String>();
	public Map<String, String> getParams() {
		return params;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	public RCPlatformResponseHandler getResponseHandler() {
		return responseHandler;
	}

	public void setResponseHandler(RCPlatformResponseHandler responseHandler) {
		this.responseHandler = responseHandler;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void putParam(String key, String value) {
		if (key == null || value == null)
			return;
		params.put(key, value);
	}
}
