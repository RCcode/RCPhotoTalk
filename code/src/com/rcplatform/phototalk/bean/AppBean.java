package com.rcplatform.phototalk.bean;

/**
 * 应用基本信息实体。
 * <p>
 * Copyright: Menue,Inc Copyright (c) 2013-3-15 下午11:22:45
 * <p>
 * Team:Menue Beijing
 * <p>
 * 
 * @author jelly.xiong@menue.com.cn
 * @version 1.0.0
 */
public class AppBean {

	/*
	 * { "appId": 1, "picUrl": null, "appName": "photochat", "appUrl": "version" }
	 */
	private String appId;

	private String picUrl;

	private String appName;

	private String version;

	//
	private String status;

	private String appUrl;

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getPicUrl() {
		return picUrl;
	}

	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAppUrl() {
		return appUrl;
	}

	public void setAppUrl(String appUrl) {
		this.appUrl = appUrl;
	}

	@Override
	public String toString() {
		return "AppBean [appId=" + appId + ", picUrl=" + picUrl + ", appName=" + appName + ", version=" + version + ", status=" + status
		        + ", appUrl=" + appUrl + "]";
	}

}
