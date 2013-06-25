package com.rcplatform.phototalk.thirdpart.utils;

import java.util.ArrayList;
import java.util.List;

import twitter4j.PagableResponseList;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.facebook.SharedPreferencesTokenCachingStrategy;
import com.facebook.model.GraphUser;
import com.perm.kate.api.User;
import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.FriendType;
import com.rcplatform.phototalk.thirdpart.bean.ThirdPartUser;
import com.rcplatform.phototalk.utils.PrefsUtils;

public class ThirdPartUtils {
	public static List<Friend> parserToFriends(List<ThirdPartUser> friends, int type) {
		List<Friend> result = new ArrayList<Friend>();
		for (ThirdPartUser thirdFriend : friends) {
			result.add(parserToFriend(thirdFriend, type));
		}
		return result;
	}

	public static Friend parserToFriend(ThirdPartUser friend, int type) {
		Friend result = new Friend();
		result.setRcId(friend.getId());
		result.setHeadUrl(friend.getHeadUrl());
		result.setNickName(friend.getNick());
		return result;
	}

	public static List<Friend> getFriendsNotRepeat(List<Friend> thirdPartFriends, List<Friend> recommendFriends, List<Friend> friends) {
		List<Friend> friendsRepeat = new ArrayList<Friend>();
		for (Friend friend : recommendFriends) {
			String facebookId = friend.getSource().getValue();
			for (Friend facebookFriend : thirdPartFriends) {
				if (facebookFriend.getRcId().equals(facebookId)) {
					friendsRepeat.add(facebookFriend);
					break;
				}
			}
		}
		for (Friend friend : friends) {
			if (friend.getSource() != null) {
				String facebookId = friend.getSource().getValue();
				for (Friend facebookFriend : thirdPartFriends) {
					if (facebookFriend.getRcId().equals(facebookId)) {
						friendsRepeat.add(facebookFriend);
						break;
					}
				}
			}
		}
		if (friendsRepeat.size() > 0) {
			thirdPartFriends.removeAll(friendsRepeat);
		}
		return thirdPartFriends;
	}

	public static ThirdPartUser parserFacebookUserToThirdPartUser(GraphUser user) {
		ThirdPartUser thirdPartUser = new ThirdPartUser();
		thirdPartUser.setId(user.getId());
		thirdPartUser.setNick(TextUtils.isEmpty(user.getName()) ? (user.getFirstName() + " " + user.getLastName()) : user.getName());
		thirdPartUser.setType(FriendType.FACEBOOK);
		return thirdPartUser;
	}

	public static List<ThirdPartUser> parserFacebookUserToThirdPartUser(List<GraphUser> users) {
		List<ThirdPartUser> thirdPartUsers = new ArrayList<ThirdPartUser>();
		for (GraphUser user : users) {
			thirdPartUsers.add(parserFacebookUserToThirdPartUser(user));
		}
		return thirdPartUsers;
	}

	public static ThirdPartUser parserVKUserToThirdPartUser(User user) {
		ThirdPartUser thirdPartFriend = new ThirdPartUser();
		thirdPartFriend.setId(user.uid + "");
		thirdPartFriend.setNick(TextUtils.isEmpty(user.nickname) ? (user.first_name + " " + user.last_name) : user.nickname);
		thirdPartFriend.setHeadUrl(getVKUserHeadUrl(user));
		thirdPartFriend.setType(FriendType.VK);
		return thirdPartFriend;
	}

	public static List<ThirdPartUser> parserTwitterUsersToThirdPartUser(PagableResponseList<twitter4j.User> users) {
		List<ThirdPartUser> result = new ArrayList<ThirdPartUser>();
		for (int i = 0; i < users.size(); i++) {
			twitter4j.User user = users.get(i);
			ThirdPartUser thirdPartUser = new ThirdPartUser();
			thirdPartUser.setHeadUrl(user.getProfileImageURL());
			thirdPartUser.setId(user.getId() + "");
			thirdPartUser.setNick(user.getScreenName());
			thirdPartUser.setType(FriendType.TWITTER);
			result.add(thirdPartUser);
		}
		return result;

	}

	public static String getVKUserHeadUrl(User user) {
		if (!TextUtils.isEmpty(user.photo))
			return user.photo;
		if (!TextUtils.isEmpty(user.photo_medium_rec))
			return user.photo_medium_rec;
		if (!TextUtils.isEmpty(user.photo_medium))
			return user.photo_medium;
		if (!TextUtils.isEmpty(user.photo_big))
			return user.photo_big;
		return null;
	}

	public static List<ThirdPartUser> parserVKUserToThirdPartUser(List<User> users) {
		List<ThirdPartUser> thirdPartUsers = new ArrayList<ThirdPartUser>();
		for (User user : users) {
			thirdPartUsers.add(parserVKUserToThirdPartUser(user));
		}
		return thirdPartUsers;
	}

	public static boolean isVKVlidated(Context context, String pref) {
		return PrefsUtils.User.ThirdPart.getVKAccount(context, pref) != null;
	}

	public static boolean isTwitterVlidated(Context context, String pref) {
		return PrefsUtils.User.ThirdPart.getTwitterAccessToken(context, pref) != null;
	}

	public static boolean isFacebookVlidate(Context context) {
		SharedPreferencesTokenCachingStrategy strategy = new SharedPreferencesTokenCachingStrategy(context);
		Bundle bundle = strategy.load();
		boolean vlidated = false;
		if (bundle != null) {
			String token = bundle.getString(SharedPreferencesTokenCachingStrategy.TOKEN_KEY);
			long expirationDate = bundle.getLong(SharedPreferencesTokenCachingStrategy.EXPIRATION_DATE_KEY);
			if (token != null && !isTokenExpiration(expirationDate))
				vlidated = true;
		}
		return vlidated;
	}

	private static boolean isTokenExpiration(long expirationDate) {
		return System.currentTimeMillis() > expirationDate;
	}
}
