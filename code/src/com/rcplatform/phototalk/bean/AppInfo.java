package com.rcplatform.phototalk.bean;

import java.io.Serializable;

public class AppInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String appPackage;
	private String appName;
	private int appId;
	private String grayPicUrl;
	private String colorPicUrl;
	private String appUrl;

	public String getAppPackage() {
		return appPackage;
	}

	public void setAppPackage(String appPackage) {
		this.appPackage = appPackage;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public int getAppId() {
		return appId;
	}

	public void setAppId(int appId) {
		this.appId = appId;
	}

	public String getGrayPicUrl() {
		return grayPicUrl;
	}

	public void setGrayPicUrl(String grayPicUrl) {
		this.grayPicUrl = grayPicUrl;
	}

	public String getColorPicUrl() {
		return colorPicUrl;
	}

	public void setColorPicUrl(String colorPicUrl) {
		this.colorPicUrl = colorPicUrl;
	}

	public String getAppUrl() {
		return appUrl;
	}

	public void setAppUrl(String appUrl) {
		this.appUrl = appUrl;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof AppInfo))
			return false;
		return appPackage.equals(((AppInfo) o).getAppPackage());
	}
}
