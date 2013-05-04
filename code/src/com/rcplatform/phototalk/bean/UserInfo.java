package com.rcplatform.phototalk.bean;

import java.io.Serializable;

import android.content.Context;

import com.rcplatform.phototalk.R;

/**
 * 标题、简要说明. <br>
 * 类详细说明.
 * <p>
 * Copyright: Menue,Inc Copyright (c) 2013-3-1 下午05:37:22
 * <p>
 * Team:Menue Beijing
 * <p>
 * 
 * @author jelly.xiong@menue.com.cn
 * @version 1.0.0
 */
public class UserInfo implements Serializable {

	public static final int SEX_SECRET = 0;
	public static final int SEX_MALE = 1;
	public static final int SEX_FAMALE = 2;

	public static final int FIRST_TIME = 0;
	
	public static final int RECEIVE_ALL=0;
	public static final int RECEIVE_FRIEND=1;
	
	public static final int TRENDS_SHOW=0;
	public static final int TRENDS_HIDE=1;

	private static final long serialVersionUID = 1L;

	public static final String DEFAULT_TOKEN = "000000";
	public static final String DEFAULT_USER_ID = "0";

	private String country;

	private String passWord;

	private String email;

	private String token;

	private String birthday;

	private long lastTime;

	private String suid;

	private String deviceId;

	private String registAppId;

	private String rcId;

	private String phone;

	private int sex;

	private String age;

	private String signature;

	private String nick;

	private String headUrl;

	private int receiveSet;

	private int showRecommends;
	
	private int trendsSet;
	
	public int getTrendsSet() {
		return trendsSet;
	}

	public void setTrendsSet(int trendsSet) {
		this.trendsSet = trendsSet;
	}

	public UserInfo() {
		super();
	}

	public UserInfo(String nick, String passWord, String email, String token, String suId) {
		this.passWord = passWord;
		this.email = email;
		this.token = token;
		this.suid = suId;
		this.nick = nick;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getPassWord() {
		return passWord;
	}

	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public long getLastTime() {
		return lastTime;
	}

	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getRegistAppId() {
		return registAppId;
	}

	public void setRegistAppId(String registAppId) {
		this.registAppId = registAppId;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getSexString(Context context) {
		String sexString = null;
		switch (sex) {
		case SEX_SECRET:
			sexString = context.getString(R.string.sex_secret);
			break;
		case SEX_MALE:
			sexString = context.getString(R.string.male);
			break;
		case SEX_FAMALE:
			sexString = context.getString(R.string.famale);
			break;
		}
		return sexString;
	}

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}

	public String getSignature() {
		return signature == null ? "" : signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getNick() {
		return nick == null ? "" : nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getHeadUrl() {
		return headUrl == null ? "" : headUrl;
	}

	public void setHeadUrl(String headUrl) {
		this.headUrl = headUrl;
	}

	public int getReceiveSet() {
		return receiveSet;
	}

	public void setReceiveSet(int receiveSet) {
		this.receiveSet = receiveSet;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getSuid() {
		return suid;
	}

	public void setSuid(String suid) {
		this.suid = suid;
	}

	public int getShowRecommends() {
		return showRecommends;
	}

	public void setShowRecommends(int showRecommends) {
		this.showRecommends = showRecommends;
	}

	public String getRcId() {
		return rcId;
	}

	public void setRcId(String rcId) {
		this.rcId = rcId;
	}
	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		return suid.equals(((UserInfo)o).getSuid());
	}
	public void clone(UserInfo userInfo){
		suid=userInfo.getSuid();
		passWord=userInfo.getPassWord();
		phone=userInfo.getPhone();
		birthday=userInfo.getBirthday();
		rcId=userInfo.getRcId();
		token=userInfo.getToken();
		trendsSet=userInfo.getTrendsSet();
		sex=userInfo.getSex();
		receiveSet=userInfo.getReceiveSet();
		headUrl=userInfo.getHeadUrl();
		deviceId=userInfo.getDeviceId();
		nick=userInfo.getNick();
	}
}
