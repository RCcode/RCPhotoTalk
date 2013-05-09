package com.rcplatform.phototalk.request;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.RecordUser;
import com.rcplatform.phototalk.bean.ServiceRecordInfo;
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
		return convertData((List<ServiceRecordInfo>) new Gson().fromJson(json, new TypeToken<List<ServiceRecordInfo>>() {
		}.getType()));
	}

	private static List<Information> convertData(List<ServiceRecordInfo> list) {
		List<Information> data = new ArrayList<Information>();
		Information infoRecord;
		for (ServiceRecordInfo info : list) {
			infoRecord = new Information();
			infoRecord.setRecordId(info.getId().trim());
			infoRecord.setCreatetime(info.getCrtTime());
			infoRecord.setTotleLength(info.getTime());
			infoRecord.setLimitTime(info.getTime());
			infoRecord.setSender(new RecordUser(info.getSeSuid(), info.getSnick(), info.getShead()));
			infoRecord.setReceiver(new RecordUser(info.getReSuid(), info.getRnick(), info.getRhead()));
			infoRecord.setStatu(info.getState());
			infoRecord.setType(info.getType());
			infoRecord.setUrl(info.getPicUrl());
			infoRecord.setLastUpdateTime(info.getUpdTime());
			infoRecord.setReceiveTime(System.currentTimeMillis());
			data.add(infoRecord);
		}
		list.clear();
		list = null;
		return data;
	}
}
