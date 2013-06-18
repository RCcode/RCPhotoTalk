package com.rcplatform.phototalk.proxy;

import java.io.File;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;

import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.api.PhotoTalkApiUrl;
import com.rcplatform.phototalk.bean.Contacts;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.FriendType;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.request.JSONConver;
import com.rcplatform.phototalk.request.PhotoTalkParams;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.request.RCPlatformServiceError;
import com.rcplatform.phototalk.request.Request;
import com.rcplatform.phototalk.request.inf.LoadFriendsListener;
import com.rcplatform.phototalk.thirdpart.utils.ThirdPartUtils;
import com.rcplatform.phototalk.utils.ContactUtil;
import com.rcplatform.phototalk.utils.PrefsUtils;
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

	// 田镇源 发送图片时 请求好友动态
	public static void getMyFriendDynamic(Context context, RCPlatformResponseHandler responseHandler, int page, int size, String time) {
		Request request = new Request(context, PhotoTalkApiUrl.GET_FRIENDS_DYNAMIC_URL, responseHandler);
		request.putParam("page", page + "");
		request.putParam("size", size + "");
		request.putParam("time", time);
		request.excuteAsync();
	}

	// 田镇源 上传修改个人信息方法
	public static void upUserInfo(Context context, File file, RCPlatformResponseHandler responseHandler, String nick, String birthday, String sex) {
		Request request = new Request(context, PhotoTalkApiUrl.USER_INFO_UPDATE_URL, responseHandler);
		request.putParam(PhotoTalkParams.ChangeUserInfo.PARAM_KEY_NICK, nick);
		request.putParam(PhotoTalkParams.ChangeUserInfo.PARAM_KEY_BIRTHDAY, birthday);
		request.putParam(PhotoTalkParams.ChangeUserInfo.PARAM_KEY_GENDER, sex);
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

	public static void getFriends(final BaseActivity context, final LoadFriendsListener listener) {
		Thread thread = new Thread() {
			public void run() {
				if (PrefsUtils.User.hasLoadedFriends(context, context.getCurrentUser().getRcId())) {
					final List<Friend> friends = PhotoTalkDatabaseFactory.getDatabase().getFriends();
					context.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							listener.onFriendsLoaded(friends, null);
						}
					});
				}
				getMyFriendlist(context, new RCPlatformResponseHandler() {

					@Override
					public void onSuccess(int statusCode, final String content) {
						Thread thread = new Thread() {
							public void run() {
								try {
									JSONObject jsonObject = new JSONObject(content);
									JSONArray myFriendsArray = jsonObject.getJSONArray("myUsers");
									List<Friend> friends = JSONConver.jsonToFriends(myFriendsArray.toString());
									for (Friend friend : friends) {
										friend.setLetter(RCPlatformTextUtil.getLetter(friend.getNickName()));
										friend.setFriend(true);
									}
									PhotoTalkDatabaseFactory.getDatabase().saveFriends(friends);
									if (!PrefsUtils.User.hasLoadedFriends(context, context.getCurrentUser().getRcId())) {
										PrefsUtils.User.setLoadedFriends(context, context.getCurrentUser().getRcId());
										final List<Friend> localFriends = PhotoTalkDatabaseFactory.getDatabase().getFriends();
										context.runOnUiThread(new Runnable() {

											@Override
											public void run() {
												listener.onFriendsLoaded(localFriends, null);
											}
										});

									}
								} catch (Exception e) {
									e.printStackTrace();
									context.runOnUiThread(new Runnable() {

										@Override
										public void run() {
											listener.onLoadedFail(context.getString(R.string.net_error));
										}
									});

								}
							};
						};
						thread.start();
					}

					@Override
					public void onFailure(int errorCode, String content) {
						listener.onLoadedFail(content);
					}
				});
			};
		};
		thread.start();
	}

	public static void getFriendsAndRecommends(final BaseActivity context, final LoadFriendsListener listener) {
		Thread thread = new Thread() {
			public void run() {
				if (PrefsUtils.User.hasLoadedFriends(context, context.getCurrentUser().getRcId())) {
					final List<Friend> friends = PhotoTalkDatabaseFactory.getDatabase().getFriends();
					final List<Friend> recommends = PhotoTalkDatabaseFactory.getDatabase().getRecommends();
					context.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							listener.onFriendsLoaded(friends, recommends);
						}
					});
				}
				Request request = new Request(context, PhotoTalkApiUrl.GET_MY_FRIENDS_URL, new RCPlatformResponseHandler() {

					@Override
					public void onSuccess(int statusCode, final String content) {
						Thread thread = new Thread() {
							@Override
							public void run() {
								try {
									JSONObject jObj = new JSONObject(content);
									List<Friend> friends = JSONConver.jsonToFriends(jObj.getJSONArray("myUsers").toString());
									for (Friend f : friends) {
										f.setFriend(true);
										f.setLetter(RCPlatformTextUtil.getLetter(f.getNickName()));
									}
									List<Friend> recommends = JSONConver.jsonToFriends(jObj.getJSONArray("recommendUsers").toString());
									for (Friend f : recommends) {
										f.setFriend(false);
										f.setLetter(RCPlatformTextUtil.getLetter(f.getNickName()));
									}
									PhotoTalkDatabaseFactory.getDatabase().saveFriends(friends);
									PhotoTalkDatabaseFactory.getDatabase().saveRecommends(recommends);
									if (!PrefsUtils.User.hasLoadedFriends(context, context.getCurrentUser().getRcId())) {
										PrefsUtils.User.setLoadedFriends(context, context.getCurrentUser().getRcId());
										final List<Friend> myFriends = PhotoTalkDatabaseFactory.getDatabase().getFriends();
										final List<Friend> myRecommends = PhotoTalkDatabaseFactory.getDatabase().getRecommends();
										runOnUiThread(context, new Runnable() {

											@Override
											public void run() {
												listener.onFriendsLoaded(myFriends, myRecommends);
											}
										});
									}
								} catch (Exception e) {
									e.printStackTrace();
									runOnUiThread(context, new Runnable() {
										@Override
										public void run() {
											listener.onLoadedFail(context.getString(R.string.net_error));
										}
									});
								}
							}
						};
						thread.start();
					}

					@Override
					public void onFailure(int errorCode, String content) {
						listener.onLoadedFail(content);
					}
				});
				request.excuteAsync();
			};
		};
		thread.start();
	}

	public static void getRecommends(final BaseActivity context, final int type, final LoadFriendsListener listener) {
		Thread thread = new Thread() {
			public void run() {
				String url = null;
				if (type == FriendType.CONTACT) {
					url = PhotoTalkApiUrl.CONTACT_RECOMMEND_URL;
				} else {
					url = PhotoTalkApiUrl.THIRDPART_RECOMMENDS_URL;
				}
				RCPlatformResponseHandler responseHandler = loadLocalRecommends(context, type, listener);
				final Request request = new Request(context, url, responseHandler);
				request.excuteAsync();
			};
		};
		thread.start();
	}

	private static RCPlatformResponseHandler loadLocalRecommends(final Activity context, final int friendType, final LoadFriendsListener listener) {
		final List<Friend> friendsLocal = PhotoTalkDatabaseFactory.getDatabase().getFriends();
		final List<Friend> recommendsLocal = PhotoTalkDatabaseFactory.getDatabase().getRecommends(friendType);
		RCPlatformResponseHandler responseHandler = null;
		if (friendType == FriendType.CONTACT) {
			final List<Contacts> localContacts = PhotoTalkDatabaseFactory.getGlobalDatabase().getContacts();
			final boolean needServiceData = (localContacts.size() == 0);
			responseHandler = new RCPlatformResponseHandler() {

				@Override
				public void onSuccess(int statusCode, final String content) {
					Thread thread = new Thread() {
						@Override
						public void run() {
							try {
								JSONObject jObj = new JSONObject(content);
								List<Friend> recommendsService = JSONConver.jsonToFriends(jObj.getJSONArray("userList").toString());
								PhotoTalkDatabaseFactory.getDatabase().saveRecommends(recommendsService, FriendType.CONTACT);
								final List<Friend> contactsRecommends = PhotoTalkDatabaseFactory.getDatabase().getRecommends(FriendType.CONTACT);
								if (needServiceData)
									context.runOnUiThread(new Runnable() {

										@Override
										public void run() {
											listener.onFriendsLoaded(ContactUtil.getContactFriendNotRepeat(localContacts, contactsRecommends, friendsLocal),
													contactsRecommends);
										}
									});

							} catch (Exception e) {
								e.printStackTrace();
								if (needServiceData)
									context.runOnUiThread(new Runnable() {

										@Override
										public void run() {
											onFailure(RCPlatformServiceError.ERROR_CODE_REQUEST_FAIL, context.getString(R.string.net_error));
										}
									});
							}
						}
					};
					thread.start();
				}

				@Override
				public void onFailure(int errorCode, String content) {
					if (needServiceData)
						listener.onLoadedFail(content);
				}
			};
			if (listener != null && !context.isFinishing() && !needServiceData) {
				context.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						listener.onFriendsLoaded(ContactUtil.getContactFriendNotRepeat(localContacts, recommendsLocal, friendsLocal), recommendsLocal);
					}
				});
			}
		} else {
			final List<Friend> localFacebookFriends = PhotoTalkDatabaseFactory.getDatabase().getThirdPartFriends(friendType);
			final boolean needServiceData = (localFacebookFriends.size() == 0);
			responseHandler = new RCPlatformResponseHandler() {

				@Override
				public void onSuccess(int statusCode, final String content) {
					Thread thread = new Thread() {
						public void run() {
							try {
								JSONObject jObj = new JSONObject(content);
								List<Friend> recommendsService = JSONConver.jsonToFriends(jObj.getJSONArray("thirdUsers").toString());
								PhotoTalkDatabaseFactory.getDatabase().saveRecommends(recommendsService, friendType);
								final List<Friend> thirdPartRecommends = PhotoTalkDatabaseFactory.getDatabase().getRecommends(friendType);
								if (needServiceData) {
									context.runOnUiThread(new Runnable() {

										@Override
										public void run() {
											listener.onFriendsLoaded(
													ThirdPartUtils.getFriendsNotRepeat(localFacebookFriends, thirdPartRecommends, friendsLocal),
													thirdPartRecommends);
										}
									});
								}
							} catch (Exception e) {
								e.printStackTrace();
								if (needServiceData) {
									context.runOnUiThread(new Runnable() {

										@Override
										public void run() {
											onFailure(RCPlatformServiceError.ERROR_CODE_REQUEST_FAIL, context.getString(R.string.net_error));
										}
									});
								}
							}
						};
					};
					thread.start();
				}

				@Override
				public void onFailure(int errorCode, String content) {
					if (needServiceData)
						listener.onLoadedFail(content);
				}
			};
			if (listener != null && !context.isFinishing() && !needServiceData) {
				context.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						listener.onFriendsLoaded(ThirdPartUtils.getFriendsNotRepeat(localFacebookFriends, recommendsLocal, friendsLocal), recommendsLocal);
					}
				});
			}
		}
		return responseHandler;
	}

	public static void getAllRecommends(Context context, RCPlatformResponseHandler responseHandler) {
		Request request = new Request(context, PhotoTalkApiUrl.GET_ALL_RECOMMENDS_URL, responseHandler);
		request.excuteAsync();
	}
}
