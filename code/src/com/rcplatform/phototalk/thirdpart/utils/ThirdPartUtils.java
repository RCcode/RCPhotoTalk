package com.rcplatform.phototalk.thirdpart.utils;

import java.util.ArrayList;
import java.util.List;

import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.thirdpart.bean.ThirdPartFriend;

public class ThirdPartUtils {
	public static List<Friend> parserToFriends(List<ThirdPartFriend> friends, int type) {
		List<Friend> result = new ArrayList<Friend>();
		for (ThirdPartFriend thirdFriend : friends) {
			result.add(parserToFriend(thirdFriend, type));
		}
		return result;
	}

	public static Friend parserToFriend(ThirdPartFriend friend, int type) {
		Friend result = new Friend();
		result.setRcId(friend.getId());
		result.setHeadUrl(friend.getHeadUrl());
		result.setNick(friend.getNick());
		return result;
	}

	public static List<Friend> getFriendsNotRepeat(List<Friend> facebookFriends, List<Friend> recommendFriends) {
		List<Friend> friendsRepeat = new ArrayList<Friend>();
		for (Friend friend : recommendFriends) {
			String facebookId = friend.getSource().getValue();
			for (Friend facebookFriend : facebookFriends) {
				if (facebookFriend.getRcId().equals(facebookId)) {
					friendsRepeat.add(facebookFriend);
					break;
				}
			}
		}
		if (friendsRepeat.size() > 0) {
			facebookFriends.removeAll(friendsRepeat);
		}
		return facebookFriends;
	}
}
