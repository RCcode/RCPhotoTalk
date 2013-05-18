package com.rcplatform.phototalk.request;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rcplatform.phototalk.bean.AppInfo;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.Information;
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

	public static List<Information> jsonToInformations(String json) {
		return new Gson().fromJson(json, new TypeToken<List<Information>>() {
		}.getType());
	}

	public static String informationToJSON(Information... informations) {
		return new Gson().toJson(informations);
	}

	public static List<AppInfo> jsonToAppInfos(String json) {
		return new Gson().fromJson(json, new TypeToken<List<AppInfo>>() {
		}.getType());
	}

	public static <T> List<T> jsonToList(String json, Class<T> clazz) {
		return new Gson().fromJson(json, new TypeToken<List<Information>>() {
		}.getType());
	}

	public static <T> T jsonToObject(String json, Class<T> clazz) {
		return new Gson().fromJson(json, clazz);
	}
}
