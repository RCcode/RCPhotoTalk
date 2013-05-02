package com.rcplatform.phototalk.bean;

import java.util.ArrayList;

/**
 * 朋友详情 <br>
 * <p>
 * Copyright: Menue,Inc Copyright (c) 2013-3-15下午11:19:08
 * <p>
 * Team:Menue Beijing
 * <p>
 * 
 * @author jelly.xiong@menue.com.cn
 * @version 1.0.0
 */
public class DetailFriend extends Friend {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	/*
	 * { "message": "成功", "userDetail": { "signature": "", "country": "US", "nick": "test2nick", "headUrl": "",
	 * "tacotyId": "", "phone": "+8611111111112", "userState": 0, "createTime": 1363155023000, "appList": [ { "appId":
	 * 1, "picUrl": null, "appName": "photochat", "appUrl": "version" } ], "suId": "45V4n7AppOk=", "mark": "小S" },
	 * "status": 0 }
	 */
	private ArrayList<AppBean> appBeans;

	private String mark;

	public ArrayList<AppBean> getAppBeans() {
		return appBeans;
	}

	public void setAppBeans(ArrayList<AppBean> appBeans) {
		this.appBeans = appBeans;
	}

	public String getMark() {
		return mark;
	}

	public void setMark(String mark) {
		this.mark = mark;
	}

	@Override
	public String toString() {
		return "DetailFriend [appBeans=" + appBeans + ", mark=" + mark + "]";
	}

}
