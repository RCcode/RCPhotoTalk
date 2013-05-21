package com.rcplatform.phototalk.bean;

import java.io.Serializable;

import android.content.Context;

import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.utils.RCPlatformTextUtil;

public class UserInfo implements Serializable {

	public static final int SEX_SECRET = 0;
	public static final int SEX_MALE = 1;
	public static final int SEX_FAMALE = 2;

	public static final int FIRST_TIME = 0;
	public static final int NOT_FIRST_TIME = 1;

	public static final int RECEIVE_ALL = 0;
	public static final int RECEIVE_FRIEND = 1;

	public static final int TRENDS_SHOW = 0;
	public static final int TRENDS_HIDE = 1;

	private static final long serialVersionUID = 1L;

	public static final String DEFAULT_TOKEN = "000000";
	public static final String DEFAULT_USER_ID = "0";

	private String country;

	private String email;

	private String token;

	private String birthday;

	private String deviceId;

	private String appId;

	private String rcId;

	private String cellPhone;

	private int gender;

	private String nickName;

	private String headUrl;

	private int allowsend;

	private int showRecommends;

	private int shareNews;

	private String background;

	private String tigaseId;

	private String tigasePwd;

	public String getTigaseId() {
		return tigaseId;
	}

	public void setTigaseId(String tigaseId) {
		this.tigaseId = tigaseId;
	}

	public String getBackground() {
		return background;
	}

	public void setBackground(String background) {
		this.background = background;
	}

	public UserInfo() {
		super();
	}

	public UserInfo(String nick, String email, String token, String suId) {
		this.email = email;
		this.token = token;
		this.nickName = nick;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}


	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
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

	public String getSexString(Context context) {
		String sexString = null;
		switch (gender) {
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

	public String getHeadUrl() {
		return headUrl == null ? "" : headUrl;
	}

	public void setHeadUrl(String headUrl) {
		this.headUrl = headUrl;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
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
		return rcId.equals(((UserInfo) o).getRcId());
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getCellPhone() {
		return cellPhone;
	}

	public void setCellPhone(String cellPhone) {
		this.cellPhone = cellPhone;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public int getAllowsend() {
		return allowsend;
	}

	public void setAllowsend(int allowsend) {
		this.allowsend = allowsend;
	}

	public int getShareNews() {
		return shareNews;
	}

	public void setShareNews(int shareNews) {
		this.shareNews = shareNews;
	}

	public String getTigasePwd() {
		return tigasePwd;
	}

	public void setTigasePwd(String tigasePwd) {
		this.tigasePwd = tigasePwd;
	}

	public void clone(UserInfo userInfo) {
		cellPhone = userInfo.getCellPhone();
		birthday = userInfo.getBirthday();
		rcId = userInfo.getRcId();
		token = userInfo.getToken();
		shareNews = userInfo.getShareNews();
		gender = userInfo.getGender();
		allowsend = userInfo.getAllowsend();
		headUrl = userInfo.getHeadUrl();
		deviceId = userInfo.getDeviceId();
		nickName = userInfo.getNickName();
	}

	public int getAge() {
		if (birthday != null) {
			return RCPlatformTextUtil.getAgeByBirthday(birthday);
		}
		return 0;
	}
}
