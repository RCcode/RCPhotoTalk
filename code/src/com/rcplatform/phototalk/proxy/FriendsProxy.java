package com.rcplatform.phototalk.proxy;

import java.io.File;
import java.util.List;

import android.content.Context;

import com.rcplatform.phototalk.api.MenueApiFactory;
import com.rcplatform.phototalk.api.MenueApiUrl;
import com.rcplatform.phototalk.api.PhotoTalkParams;
import com.rcplatform.phototalk.api.RCPlatformAsyncHttpClient;
import com.rcplatform.phototalk.api.RCPlatformAsyncHttpClient.RequestAction;
import com.rcplatform.phototalk.api.RCPlatformResponseHandler;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.FriendType;
import com.rcplatform.phototalk.bean.Information;

public class FriendsProxy {

	public static List<Friend> getFacebookRecommendFriendsAsync(
			Context context, RCPlatformResponseHandler responseHandler) {
		RCPlatformAsyncHttpClient client = new RCPlatformAsyncHttpClient(
				RequestAction.JSON);
		PhotoTalkParams.buildBasicParams(context, client);
		client.putRequestParam("attrType", FriendType.FACEBOOK + "");
		client.post(context, MenueApiUrl.FACEBOOK_RECOMMENDS_URL,
				responseHandler);
		return null;
	}

	public static List<Friend> getContactRecommendFriendsAsync(Context context,
			RCPlatformResponseHandler responseHandler) {
		RCPlatformAsyncHttpClient client = new RCPlatformAsyncHttpClient(
				RequestAction.JSON);
		PhotoTalkParams.buildBasicParams(context, client);
		client.post(context, MenueApiUrl.CONTACT_RECOMMEND_URL, responseHandler);
		return null;
	}

	public static void searchFriendsAsync(Context context,
			RCPlatformResponseHandler responseHandler, String keyWords) {
		RCPlatformAsyncHttpClient client = new RCPlatformAsyncHttpClient(
				RequestAction.JSON);
		PhotoTalkParams.buildBasicParams(context, client);
		client.putRequestParam(
				PhotoTalkParams.SearchFriends.PARAM_KEY_KEYWORDS, keyWords);
		client.post(context, MenueApiUrl.SEARCH_FRIENDS_URL, responseHandler);
	}

	public static List<Friend>[] getMyFriend(Context context,
			RCPlatformResponseHandler responseHandler) {
		RCPlatformAsyncHttpClient client = new RCPlatformAsyncHttpClient(
				RequestAction.JSON);
		PhotoTalkParams.buildBasicParams(context, client);
		client.post(context, MenueApiUrl.GET_MY_FRIENDS_URL, responseHandler);
		return null;
	}
	public static void getUserInfo(Context context,
			RCPlatformResponseHandler responseHandler) {
		RCPlatformAsyncHttpClient client = new RCPlatformAsyncHttpClient(
				RequestAction.JSON);
		PhotoTalkParams.buildBasicParams(context, client);
		client.post(context, MenueApiUrl.GET_USER_INFO, responseHandler);
	}
	//田镇源 发送图片时 请求好友列表 
	public static void getMyFriendlist(Context context,
			RCPlatformResponseHandler responseHandler) {
		RCPlatformAsyncHttpClient client = new RCPlatformAsyncHttpClient(
				RequestAction.JSON);
		PhotoTalkParams.buildBasicParams(context, client);
		client.post(context, MenueApiUrl.GET_FRIENDS_URL, responseHandler);
	}
//	田镇源 上传zip方法
	public static void postZip(Context context,File file,
			RCPlatformResponseHandler responseHandler,String head_url,String time,String nick,String desc,String timeLimit,String user_appary) {
		RCPlatformAsyncHttpClient client = new RCPlatformAsyncHttpClient(
				RequestAction.FILE);
		PhotoTalkParams.buildBasicParams(context, client);
		client.putRequestParam(MenueApiFactory.HEAD_URL, head_url);
		client.putRequestParam(MenueApiFactory.TIME, time);
		client.putRequestParam(MenueApiFactory.NICK, nick);
//		client.putRequestParam(MenueApiFactory.IMAGE_TYPE, "jpg");
		client.putRequestParam(MenueApiFactory.DESC, desc);
		client.putRequestParam(MenueApiFactory.TIME_LIMIT, timeLimit);
		client.putRequestParam(MenueApiFactory.USER_ARRAY, user_appary);
//		client.putRequestParam(key, value)
		client.postFile(context, MenueApiUrl.SEND_PICTURE_URL, file, responseHandler);
	}
//	田镇源 上传修改个人信息方法
	public static void upUserInfo(Context context,File file,
			RCPlatformResponseHandler responseHandler,String nick,String birthday,String sex) {
		RCPlatformAsyncHttpClient client = new RCPlatformAsyncHttpClient(
				RequestAction.FILE);
		PhotoTalkParams.buildBasicParams(context, client);
		client.putRequestParam(MenueApiFactory.NICK, nick);
		client.putRequestParam(MenueApiFactory.BIRTHDAY, birthday);
		client.putRequestParam(MenueApiFactory.SEX, sex);
		client.postFile(context, MenueApiUrl.USER_INFO_UPDATE_URL, file, responseHandler);
	}
	public static void upUserBackgroundImage(Context context,File file,
			RCPlatformResponseHandler responseHandler) {
		RCPlatformAsyncHttpClient client = new RCPlatformAsyncHttpClient(
				RequestAction.FILE);
		PhotoTalkParams.buildBasicParams(context, client);
		client.postFile(context, MenueApiUrl.USER_INFO_UPDATE_URL, file, responseHandler);
	}

	public static void deleteFriend(Context context,
			RCPlatformResponseHandler responseHandler, String friendSuid) {
		RCPlatformAsyncHttpClient client = new RCPlatformAsyncHttpClient(
				RequestAction.JSON);
		PhotoTalkParams.buildBasicParams(context, client);
		client.putRequestParam(PhotoTalkParams.DelFriends.PARAM_KEY_FRIEND_ID,
				friendSuid);
		client.post(context, MenueApiUrl.DELETE_FRIEND_URL, responseHandler);
	}

	public static void updateFriendRemark(Context context,
			RCPlatformResponseHandler responseHandler, String friendSuid,
			String remark) {
		RCPlatformAsyncHttpClient client = new RCPlatformAsyncHttpClient(
				RequestAction.JSON);
		PhotoTalkParams.buildBasicParams(context, client);
		client.putRequestParam(
				PhotoTalkParams.UpdateFriendRemark.PARAM_KEY_REMARK, remark);
		client.putRequestParam(
				PhotoTalkParams.UpdateFriendRemark.PARAM_KEY_FRIEND_ID,
				friendSuid);
		client.post(context, MenueApiUrl.UPDATE_FRIEND_REMARK_URL,
				responseHandler);
	}

	public static Friend getFriendDetail(Context context,
			RCPlatformResponseHandler responseHandler, String friendSuid) {
		RCPlatformAsyncHttpClient client = new RCPlatformAsyncHttpClient(
				RequestAction.JSON);
		PhotoTalkParams.buildBasicParams(context, client);
		client.putRequestParam(
				PhotoTalkParams.FriendDetail.PARAM_KEY_FRIEND_ID, friendSuid);
		client.post(context, MenueApiUrl.FRIEND_DETAIL_URL, responseHandler);
		return null;
	}

	public static void addFriendFromInformation(Context context,
			RCPlatformResponseHandler responseHandler, Information info) {
		RCPlatformAsyncHttpClient client = new RCPlatformAsyncHttpClient(
				RequestAction.JSON);
		PhotoTalkParams.buildBasicParams(context, client);
		client.putRequestParam(
				PhotoTalkParams.AddFriendFromInformation.PARAM_KEY_INFORMATION_ID,
				info.getRecordId());
		client.putRequestParam(
				PhotoTalkParams.AddFriendFromInformation.PARAM_KEY_INFORMATION_TYPE,
				info.getType() + "");
		client.putRequestParam(
				PhotoTalkParams.AddFriendFromInformation.PARAM_KEY_INFORMATION_STATE,
				info.getStatu() + "");
		client.putRequestParam(
				PhotoTalkParams.AddFriendFromInformation.PARAM_KEY_FRIEND_SUID,
				info.getSender().getSuid());
		client.post(context, MenueApiUrl.ADD_FRIEND_FROM_INFORMATION, responseHandler);
	}
}
