package com.rcplatform.phototalk.proxy;

import java.util.List;

import android.content.Context;

import com.rcplatform.phototalk.api.MenueApiUrl;
import com.rcplatform.phototalk.api.PhotoTalkParams;
import com.rcplatform.phototalk.api.RCPlatformAsyncHttpClient;
import com.rcplatform.phototalk.api.RCPlatformAsyncHttpClient.RequestAction;
import com.rcplatform.phototalk.api.RCPlatformResponseHandler;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.FriendType;

public class FriendsProxy {

	public static List<Friend> getFacebookRecommendFriendsAsync(Context context, RCPlatformResponseHandler responseHandler) {
		RCPlatformAsyncHttpClient client = new RCPlatformAsyncHttpClient(RequestAction.JSON);
		PhotoTalkParams.buildBasicParams(context, client);
		client.putRequestParam("attrType", FriendType.FACEBOOK + "");
		client.post(context, MenueApiUrl.FACEBOOK_RECOMMENDS_URL, responseHandler);
		return null;
	}

	public static List<Friend> getContactRecommendFriendsAsync(Context context, RCPlatformResponseHandler responseHandler) {
		RCPlatformAsyncHttpClient client = new RCPlatformAsyncHttpClient(RequestAction.JSON);
		PhotoTalkParams.buildBasicParams(context, client);
		client.post(context, MenueApiUrl.CONTACT_RECOMMEND_URL, responseHandler);
		return null;
	}

	public static void searchFriendsAsync(Context context, RCPlatformResponseHandler responseHandler, String keyWords) {
		RCPlatformAsyncHttpClient client = new RCPlatformAsyncHttpClient(RequestAction.JSON);
		PhotoTalkParams.buildBasicParams(context, client);
		client.putRequestParam(PhotoTalkParams.SearchFriends.PARAM_KEY_KEYWORDS, keyWords);
		client.post(context, MenueApiUrl.SEARCH_FRIENDS_URL, responseHandler);
	}

	public static List<Friend>[] getMyFriend(Context context, RCPlatformResponseHandler responseHandler) {
		RCPlatformAsyncHttpClient client = new RCPlatformAsyncHttpClient(RequestAction.JSON);
		PhotoTalkParams.buildBasicParams(context, client);
		client.post(context, MenueApiUrl.GET_MY_FRIENDS_URL, responseHandler);
		return null;
	}
	
	public static void deleteFriend(Context context,RCPlatformResponseHandler responseHandler,String friendSuid){
		RCPlatformAsyncHttpClient client = new RCPlatformAsyncHttpClient(RequestAction.JSON);
		PhotoTalkParams.buildBasicParams(context, client);
		client.putRequestParam(PhotoTalkParams.DelFriends.PARAM_KEY_FRIEND_ID, friendSuid);
		client.post(context, MenueApiUrl.DELETE_FRIEND_URL, responseHandler);
	}
}
