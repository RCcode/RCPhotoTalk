package com.rcplatform.phototalk.proxy;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;

import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.api.PhotoTalkApiFactory;
import com.rcplatform.phototalk.api.PhotoTalkApiUrl;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.galhttprequest.RCPlatformServiceError;
import com.rcplatform.phototalk.request.JSONConver;
import com.rcplatform.phototalk.request.PhotoTalkParams;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.request.Request;
import com.rcplatform.phototalk.request.inf.OnFriendsLoadedListener;
import com.rcplatform.phototalk.utils.RCPlatformTextUtil;

public class FriendsProxy {

	public static List<Friend> getContactRecommendFriendsAsync(Context context, RCPlatformResponseHandler responseHandler) {
		Request request = new Request(context, PhotoTalkApiUrl.CONTACT_RECOMMEND_URL, responseHandler);
		request.excuteAsync();
		return null;
	}

	public static void searchFriendsAsync(Context context, RCPlatformResponseHandler responseHandler, String keyWords) {
		Request request = new Request(context, PhotoTalkApiUrl.SEARCH_FRIENDS_URL, responseHandler);
		request.putParam(PhotoTalkParams.SearchFriends.PARAM_KEY_KEYWORDS, keyWords);
		request.excuteAsync();
	}

	public static void getMyFriend(final Activity context, final OnFriendsLoadedListener listener) {
		Thread thread = new Thread() {
			@Override
			public void run() {
				final List<Friend> friends = PhotoTalkDatabaseFactory.getDatabase().getFriends();
				final List<Friend> recommends = PhotoTalkDatabaseFactory.getDatabase().getRecommends();
				if (!context.isFinishing()) {
					context.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							listener.onLocalFriendsLoaded(friends, recommends);
							loadFriendsFromService(context, listener);
						}
					});
				}
			}
		};
		thread.start();
	}

	private static void loadFriendsFromService(final Activity context, final OnFriendsLoadedListener listener) {
		Request request = new Request(context, PhotoTalkApiUrl.GET_MY_FRIENDS_URL, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, final String content) {
				Thread thread = new Thread() {
					@Override
					public void run() {
						try {
							JSONObject jObj = new JSONObject(content);
							final List<Friend> mFriends = JSONConver.jsonToFriends(jObj.getJSONArray("myUsers").toString());
							for (Friend f : mFriends) {
								f.setFriend(true);
								f.setLetter(RCPlatformTextUtil.getLetter(f.getNickName()));
							}
							final List<Friend> mRecommends = JSONConver.jsonToFriends(jObj.getJSONArray("recommendUsers").toString());
							for (Friend f : mRecommends)
								f.setFriend(false);
							PhotoTalkDatabaseFactory.getDatabase().saveFriends(mFriends);
							PhotoTalkDatabaseFactory.getDatabase().saveRecommends(mRecommends);
							List<Friend> delFriends = PhotoTalkDatabaseFactory.getDatabase().getHidenFriends();
							for (Friend f : delFriends) {
								if (mFriends.contains(f))
									mFriends.remove(mFriends.indexOf(f));
								if (mRecommends.contains(f))
									mRecommends.remove(mRecommends.indexOf(f));
							}
							Collections.sort(mFriends, new Comparator<Friend>() {

								@Override
								public int compare(Friend lhs, Friend rhs) {
									return lhs.getLetter().compareTo(rhs.getLetter());
								}
							});
							runOnUiThread(context, new Runnable() {

								@Override
								public void run() {
									listener.onServiceFriendsLoaded(mFriends, mRecommends);
								}
							});

						} catch (Exception e) {
							e.printStackTrace();
							runOnUiThread(context, new Runnable() {

								@Override
								public void run() {
									listener.onError(RCPlatformServiceError.ERROR_CODE_REQUEST_FAIL, context.getString(R.string.net_error));
								}
							});
						}
					}
				};
				thread.start();
			}

			@Override
			public void onFailure(int errorCode, String content) {
				listener.onError(errorCode, content);
			}
		});
		request.excuteAsync();
	}

	private static void runOnUiThread(Activity context, Runnable task) {
		context.runOnUiThread(task);
	}

	public static void getUserInfo(Context context, RCPlatformResponseHandler responseHandler) {
		Request request = new Request(context, PhotoTalkApiUrl.GET_USER_INFO, responseHandler);
		request.excuteAsync();
	}

	// 田镇源 发送图片时 请求好友列表
	public static void getMyFriendlist(Context context, RCPlatformResponseHandler responseHandler) {
		Request request = new Request(context, PhotoTalkApiUrl.GET_FRIENDS_URL, responseHandler);
		request.excuteAsync();
	}

	// 田镇源 上传修改个人信息方法
	public static void upUserInfo(Context context, File file, RCPlatformResponseHandler responseHandler, String nick, String birthday, String sex) {
		Request request = new Request(context, PhotoTalkApiUrl.USER_INFO_UPDATE_URL, responseHandler);
		request.putParam(PhotoTalkApiFactory.NICK, nick);
		request.putParam(PhotoTalkApiFactory.BIRTHDAY, birthday);
		request.putParam(PhotoTalkApiFactory.SEX, sex);
		if (file != null)
			request.setFile(file);
		request.executePostNameValuePairAsync();
	}

	// 上传头像
	public static void upUserInfoHeadImage(Context context, File file, RCPlatformResponseHandler responseHandler) {
		Request request = new Request(context, PhotoTalkApiUrl.USER_INFO_HEAD_IMAGE_URL, responseHandler);
		request.setFile(file);
		request.excuteAsync();
	}

	public static void upUserBackgroundImage(Context context, File file, RCPlatformResponseHandler responseHandler) {
		Request request = new Request(context, PhotoTalkApiUrl.USER_BACKGROUND_UPDATE_URL, responseHandler);
		request.setFile(file);
		request.excuteAsync();
	}

	public static void deleteFriend(Context context, RCPlatformResponseHandler responseHandler, String friendSuid) {
		Request request = new Request(context, PhotoTalkApiUrl.DELETE_FRIEND_URL, responseHandler);
		request.putParam(PhotoTalkParams.DelFriends.PARAM_KEY_FRIEND_ID, friendSuid);
		request.excuteAsync();
	}

	public static void updateFriendRemark(Context context, RCPlatformResponseHandler responseHandler, String friendSuid, String remark) {
		Request request = new Request(context, PhotoTalkApiUrl.UPDATE_FRIEND_REMARK_URL, responseHandler);
		request.putParam(PhotoTalkParams.UpdateFriendRemark.PARAM_KEY_REMARK, remark);
		request.putParam(PhotoTalkParams.UpdateFriendRemark.PARAM_KEY_FRIEND_ID, friendSuid);
		request.excuteAsync();
	}

	public static Friend getFriendDetail(Context context, RCPlatformResponseHandler responseHandler, String friendSuid) {
		Request request = new Request(context, PhotoTalkApiUrl.FRIEND_DETAIL_URL, responseHandler);
		request.putParam(PhotoTalkParams.FriendDetail.PARAM_KEY_FRIEND_ID, friendSuid);
		request.excuteAsync();
		return null;
	}

	public static void addFriendFromInformation(Context context, RCPlatformResponseHandler responseHandler, Information info) {
		Request request = new Request(context, PhotoTalkApiUrl.ADD_FRIEND_FROM_INFORMATION, responseHandler);
		JSONArray array = new JSONArray();
		array.put(info.getSender().getRcId());
		request.putParam(PhotoTalkParams.AddFriendFromInformation.PARAM_KEY_FRIEND_IDS, array.toString());
		request.excuteAsync();
	}

	public static void deleteRecommendFriend(Context context, RCPlatformResponseHandler responseHandler, Friend friend) {
		Request request = new Request(context, PhotoTalkApiUrl.DELETE_RECOMMEND_URL, responseHandler);
		request.putParam(PhotoTalkParams.DelRecommend.PARAM_KEY_FRIEND_ID, friend.getRcId());
		request.putParam(PhotoTalkParams.DelRecommend.PARAM_KEY_RECOMMEND_TYPE, friend.getSource().getAttrType() + "");
		request.excuteAsync();
	}
}
