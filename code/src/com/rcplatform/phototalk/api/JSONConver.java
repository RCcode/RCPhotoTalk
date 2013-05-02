package com.rcplatform.phototalk.api;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.UserInfo;

public class JSONConver {
	public static UserInfo jsonToUserInfo(String json) {
		UserInfo userInfo = new Gson().fromJson(json, UserInfo.class);
		return userInfo;
	}

	public static List<Friend> jsonToFriends(String json) {
		return new Gson().fromJson(json, new TypeToken<List<Friend>>() {
		}.getType());
	}

	public static Friend jsonToFriend(String json) {
		return new Gson().fromJson(json, new TypeToken<Friend>() {
		}.getType());
	}

}
