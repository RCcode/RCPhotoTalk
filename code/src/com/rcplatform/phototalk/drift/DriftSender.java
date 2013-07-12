package com.rcplatform.phototalk.drift;

import java.io.Serializable;

import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.RecordUser;

public class DriftSender extends RecordUser implements Serializable{
	private String country;
	private int gender;
	private int appId;
	private int isFriend;

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public int getAppId() {
		return appId;
	}

	public void setAppId(int appId) {
		this.appId = appId;
	}

	public int getIsFriend() {
		return isFriend;
	}

	public void setIsFriend(int isFriend) {
		this.isFriend = isFriend;
	}

	public boolean isFriend() {
		return isFriend == Friend.FRIEND_ADDED;
	}
}
