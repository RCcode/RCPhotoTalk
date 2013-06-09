package com.rcplatform.phototalk.request;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;

import com.rcplatform.phototalk.PhotoTalkApplication;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.api.PhotoTalkApiUrl;
import com.rcplatform.phototalk.bean.AppInfo;
import com.rcplatform.phototalk.bean.Contacts;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.FriendType;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.InformationState;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.galhttprequest.MD5;
import com.rcplatform.phototalk.logic.MessageSender;
import com.rcplatform.phototalk.request.inf.FriendDetailListener;
import com.rcplatform.phototalk.request.inf.OnFriendsLoadedListener;
import com.rcplatform.phototalk.request.inf.OnUserInfoLoadedListener;
import com.rcplatform.phototalk.request.inf.PhotoSendListener;
import com.rcplatform.phototalk.thirdpart.utils.ThirdPartUtils;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.ContactUtil;
import com.rcplatform.phototalk.utils.PrefsUtils;
import com.rcplatform.phototalk.utils.RCPlatformTextUtil;
import com.rcplatform.phototalk.utils.RCThreadPool;
import com.rcplatform.phototalk.utils.Utils;

public class Request implements Serializable {
	private static final long serialVersionUID = 1L;
	private Context mContext;
	private long createTime;
	private String url;
	private RCPlatformResponseHandler responseHandler;
	private Map<String, String> params;
	private File file;
	private String filePath;
	private boolean cache = false;

	private void census() {
		putParam(PhotoTalkParams.ServiceCensus.PARAM_KEY_COUNTRY, Constants.COUNTRY);
		putParam(PhotoTalkParams.ServiceCensus.PARAM_KEY_OS, Constants.OS_NAME);
		putParam(PhotoTalkParams.ServiceCensus.PARAM_KEY_OS_VERSION, Constants.OS_VERSION);
		putParam(PhotoTalkParams.ServiceCensus.PARAM_KEY_TIMEZONE, Utils.getTimeZoneId(mContext) + "");
	}

	public Request() {
		createTime = System.currentTimeMillis();
		params = new HashMap<String, String>();
	}

	public Request(Context context, String url, RCPlatformResponseHandler responseHandler) {
		this();
		this.url = url;
		this.responseHandler = responseHandler;
		this.mContext = context;
		PhotoTalkParams.buildBasicParams(mContext, this);
	}

	public void setContext(Context context) {
		this.mContext = context;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	public RCPlatformResponseHandler getResponseHandler() {
		return responseHandler;
	}

	public void setResponseHandler(RCPlatformResponseHandler responseHandler) {
		this.responseHandler = responseHandler;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File mFile) {
		this.file = mFile;
		setFilePath(mFile.getPath());
	}

	public void putParam(String key, String value) {
		if (key == null || value == null)
			return;
		params.put(key, value);
	}

	public void excuteAsync() {
		if (params.size() == 0) {
			return;
		}
		((PhotoTalkApplication) mContext.getApplicationContext()).getWebService().post(this);
	}

	public void executePostNameValuePairAsync() {
		if (params.size() == 0) {
			return;
		}
		((PhotoTalkApplication) mContext.getApplicationContext()).getWebService().postNameValue(this);
	}

	public void cancel() {
		setResponseHandler(null);
	}

	/**
	 * 登陆接口，获取用户的信息
	 * 
	 * @param context
	 * @param listener
	 * @param account
	 * @param password
	 * @return
	 */
	public static void executeLogin(final Context context, final OnUserInfoLoadedListener listener, String account, String password) {
		RCPlatformResponseHandler responseHandler = new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				try {
					JSONObject jsonObject = new JSONObject(content);
					int firstTime = jsonObject.getInt("showRecommends");
					if (firstTime == UserInfo.FIRST_TIME) {
						JSONArray arrayOthers = jsonObject.getJSONArray("otherInfos");
						Map<AppInfo, UserInfo> result = new HashMap<AppInfo, UserInfo>();
						for (int i = 0; i < arrayOthers.length(); i++) {
							JSONObject jsonApp = arrayOthers.getJSONObject(i);
							AppInfo appInfo = new AppInfo();
							appInfo.setAppName(jsonApp.getString("appName"));
							UserInfo userInfo = new UserInfo();
							userInfo.setRcId(jsonApp.getString("rcId"));
							userInfo.setNickName(jsonApp.getString("nickName"));
							userInfo.setHeadUrl(jsonApp.getString("headUrl"));
							result.put(appInfo, userInfo);
						}
						if (listener != null)
							listener.onOthreAppUserInfoLoaded(result);
					} else {
						String token = jsonObject.getString("token");
						String rcId = jsonObject.getString("rcId");
						executeGetMyInfo(context, new OnUserInfoLoadedListener() {

							@Override
							public void onSuccess(UserInfo userInfo) {
								userInfo.setShowRecommends(UserInfo.NOT_FIRST_TIME);
								listener.onSuccess(userInfo);
							}

							@Override
							public void onError(int errorCode, String content) {
								onFailure(RCPlatformServiceError.ERROR_CODE_REQUEST_FAIL, context.getString(R.string.net_error));
							}

							@Override
							public void onOthreAppUserInfoLoaded(Map<AppInfo, UserInfo> userInfos) {

							}
						}, rcId, token);
					}
				} catch (Exception e) {
					e.printStackTrace();
					onFailure(RCPlatformServiceError.ERROR_CODE_REQUEST_FAIL, context.getString(R.string.net_error));
				}
			}

			@Override
			public void onFailure(int errorCode, String content) {
				listener.onError(errorCode, content);
			}
		};
		Request request = new Request(context, PhotoTalkApiUrl.LOGIN_URL, responseHandler);
		PhotoTalkParams.buildBasicParams(context, request);
		request.putParam(PhotoTalkParams.Login.PARAM_KEY_ACCOUNT, account);
		request.putParam(PhotoTalkParams.Login.PARAM_KEY_PASSWORD, MD5.encodeMD5String(password));
		request.excuteAsync();
	}

	public static void sendPhoto(final Context context, final long flag, final File file, final String timeLimit, final PhotoSendListener listener,
			final List<String> friendIds, final boolean hasVoice, final boolean hasDraw) {
		final UserInfo currentUser = ((PhotoTalkApplication) context.getApplicationContext()).getCurrentUser();
		try {
			JSONArray jsonArray = new JSONArray();
			for (String rcId : friendIds) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put(PhotoTalkParams.SendPhoto.PARAM_KEY_RECEIVER_ID, rcId);
				jsonArray.put(jsonObject);
			}
			RCPlatformResponseHandler responseHandler = null;
			if (listener != null) {
				responseHandler = new RCPlatformResponseHandler() {

					@Override
					public void onSuccess(int statusCode, String content) {
						try {
							JSONObject jsonObject = new JSONObject(content);
							String informationUrl = jsonObject.getString("picUrl");
							List<String> userIds = buildUserIds(jsonObject.getJSONArray("users"));
							long flag = jsonObject.getLong("time");
							Map<String, Information> informations = PhotoTalkDatabaseFactory.getDatabase().updateTempInformations(currentUser, informationUrl,
									flag, userIds, friendIds, InformationState.PhotoInformationState.STATU_NOTICE_SENDED_OR_NEED_LOADD,
									Integer.parseInt(timeLimit), hasVoice);
							MessageSender.getInstance().sendInformation(context, informations, userIds);
							listener.onSendSuccess(flag);
							RCThreadPool.getInstance().addTask(new Runnable() {

								@Override
								public void run() {
									int count = PhotoTalkDatabaseFactory.getDatabase().getUnSendInformationCountByUrl(file.getAbsolutePath());
									if (count == 0) {
										file.delete();
									}
								}
							});
						} catch (JSONException e) {
							e.printStackTrace();
							onFailure(RCPlatformServiceError.ERROR_CODE_REQUEST_FAIL, content);
						}
					}

					@Override
					public void onFailure(int errorCode, String content) {
						listener.onFail(flag, errorCode, content);
						UserInfo currentUser = ((PhotoTalkApplication) context.getApplicationContext()).getCurrentUser();
						PhotoTalkDatabaseFactory.getDatabase().updateTempInformations(currentUser, null, flag, null, friendIds,
								InformationState.PhotoInformationState.STATU_NOTICE_SEND_OR_LOAD_FAIL, Integer.parseInt(timeLimit), hasVoice);
					}
				};
			}
			Request request = new Request(context, PhotoTalkApiUrl.SEND_PICTURE_URL, responseHandler);
			request.setCreateTime(flag);
			request.setFile(file);
			request.putParam(PhotoTalkParams.SendPhoto.PARAM_KEY_FLAG, flag + "");
			request.putParam(PhotoTalkParams.SendPhoto.PARAM_KEY_TIME_LIMIT, timeLimit);
			request.putParam(PhotoTalkParams.SendPhoto.PARAM_KEY_USERS, jsonArray.toString());
			if (hasVoice)
				request.putParam(PhotoTalkParams.SendPhoto.PARAM_KEY_HAS_VOICE, PhotoTalkParams.SendPhoto.PARAM_VALUE_HAS_VOICE);
			else
				request.putParam(PhotoTalkParams.SendPhoto.PARAM_KEY_HAS_VOICE, PhotoTalkParams.SendPhoto.PARAM_VALUE_NO_VOICE);
			if (hasDraw)
				request.putParam(PhotoTalkParams.SendPhoto.PARAM_KEY_HAS_GRAF, PhotoTalkParams.SendPhoto.PARAM_VALUE_HAS_GRAF);
			else
				request.putParam(PhotoTalkParams.SendPhoto.PARAM_KEY_HAS_GRAF, PhotoTalkParams.SendPhoto.PARAM_VALUE_NO_GRAF);
			request.census();
			request.excuteAsync();
		} catch (Exception e) {
			e.printStackTrace();
			listener.onFail(flag, RCPlatformServiceError.ERROR_CODE_REQUEST_FAIL, context.getString(R.string.net_error));
			PhotoTalkDatabaseFactory.getDatabase().updateTempInformations(currentUser, null, flag, null, friendIds,
					InformationState.PhotoInformationState.STATU_NOTICE_SEND_OR_LOAD_FAIL, Integer.parseInt(timeLimit), hasVoice);
		}
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Context getContext() {
		return mContext;
	}

	public boolean isCache() {
		return cache;
	}

	public void setCache(boolean cache) {
		this.cache = cache;
	}

	private static List<String> buildUserIds(JSONArray array) throws JSONException {
		List<String> ids = new ArrayList<String>();
		if (array.length() > 0) {
			for (int i = 0; i < array.length(); i++) {
				String tcId = array.getString(i);
				ids.add(tcId);
			}
		}
		return ids;
	}

	public static void executeGetMyInfo(final Context context, final OnUserInfoLoadedListener listener, final String rcId, String token) {
		RCPlatformResponseHandler responseHandler = new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				try {
					JSONObject jsonObject = new JSONObject(content);
					long time = jsonObject.getLong("time");
					String lastBindPhone = jsonObject.getString("phone");
					String cellPhone = jsonObject.getString("cellPhone");
					String email = jsonObject.getString("email");
					if (RCPlatformTextUtil.isEmpty(cellPhone))
						cellPhone = null;
					if (RCPlatformTextUtil.isEmpty(lastBindPhone))
						lastBindPhone = null;
					PrefsUtils.User.MobilePhoneBind.setLastBindNumber(context, rcId, lastBindPhone);
					PrefsUtils.User.MobilePhoneBind.setLastBindPhoneTime(context, time, rcId);
					JSONObject jsonUser = jsonObject.getJSONObject("userInfo");
					UserInfo userInfo = JSONConver.jsonToObject(jsonUser.toString(), UserInfo.class);
					userInfo.setCellPhone(cellPhone);
					userInfo.setEmail(email);
					JSONArray arrayApps = jsonObject.getJSONArray("allApp");
					List<AppInfo> apps = JSONConver.jsonToAppInfos(arrayApps.toString());
					PhotoTalkDatabaseFactory.getGlobalDatabase().savePlatformAppInfos(apps);
					listener.onSuccess(userInfo);
				} catch (JSONException e) {
					e.printStackTrace();
					onFailure(RCPlatformServiceError.ERROR_CODE_REQUEST_FAIL, context.getString(R.string.net_error));
				}
			}

			@Override
			public void onFailure(int errorCode, String content) {
				listener.onError(errorCode, content);
			}
		};
		Request request = new Request(context, PhotoTalkApiUrl.GET_USER_INFO, responseHandler);
		if (rcId != null)
			request.putParam(PhotoTalkParams.PARAM_KEY_USER_ID, rcId);
		if (token != null)
			request.putParam(PhotoTalkParams.PARAM_KEY_TOKEN, token);
		request.excuteAsync();
	}

	public static void executeGetRecommends(final BaseActivity context, final int type, final OnFriendsLoadedListener listener) {

		Thread thread = new Thread() {
			@Override
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

			}
		};
		thread.start();
	}

	public static void executeLogoutAsync(Context context) {
		Request request = new Request(context, PhotoTalkApiUrl.LOGOUT_URL, null);
		request.excuteAsync();
	}

	private static RCPlatformResponseHandler loadLocalRecommends(final Activity context, final int friendType, final OnFriendsLoadedListener listener) {
		final List<Friend> recommendsLocal = PhotoTalkDatabaseFactory.getDatabase().getRecommends(friendType);
		RCPlatformResponseHandler responseHandler = null;
		if (friendType == FriendType.CONTACT) {
			final List<Contacts> localContacts = PhotoTalkDatabaseFactory.getGlobalDatabase().getContacts();
			responseHandler = new RCPlatformResponseHandler() {

				@Override
				public void onSuccess(int statusCode, String content) {
					try {
						JSONObject jObj = new JSONObject(content);
						List<Friend> recommendsService = JSONConver.jsonToFriends(jObj.getJSONArray("userList").toString());
						PhotoTalkDatabaseFactory.getDatabase().saveRecommends(recommendsService, FriendType.CONTACT);
						listener.onServiceFriendsLoaded(ContactUtil.getContactFriendNotRepeat(localContacts, recommendsService), recommendsService);
					} catch (Exception e) {
						e.printStackTrace();
						onFailure(RCPlatformServiceError.ERROR_CODE_REQUEST_FAIL, context.getString(R.string.net_error));
					}
				}

				@Override
				public void onFailure(int errorCode, String content) {
					listener.onError(errorCode, content);
				}
			};
			if (listener != null && !context.isFinishing()) {
				context.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						listener.onLocalFriendsLoaded(ContactUtil.getContactFriendNotRepeat(localContacts, recommendsLocal), recommendsLocal);
					}
				});
			}
		} else {
			final List<Friend> localFacebookFriends = PhotoTalkDatabaseFactory.getDatabase().getThirdPartFriends(friendType);
			responseHandler = new RCPlatformResponseHandler() {

				@Override
				public void onSuccess(int statusCode, String content) {
					try {
						JSONObject jObj = new JSONObject(content);
						List<Friend> recommendsService = JSONConver.jsonToFriends(jObj.getJSONArray("thirdUsers").toString());
						PhotoTalkDatabaseFactory.getDatabase().saveRecommends(recommendsService, friendType);
						listener.onServiceFriendsLoaded(ThirdPartUtils.getFriendsNotRepeat(localFacebookFriends, recommendsService), recommendsService);
					} catch (Exception e) {
						e.printStackTrace();
						onFailure(RCPlatformServiceError.ERROR_CODE_REQUEST_FAIL, context.getString(R.string.net_error));
					}
				}

				@Override
				public void onFailure(int errorCode, String content) {
					listener.onError(errorCode, content);
				}
			};
			if (listener != null && !context.isFinishing()) {
				context.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						listener.onLocalFriendsLoaded(ThirdPartUtils.getFriendsNotRepeat(localFacebookFriends, recommendsLocal), recommendsLocal);
					}
				});
			}
		}
		return responseHandler;
	}

	public static void executeGetFriendDetailAsync(final Context context, Friend friend, final FriendDetailListener listener, boolean isUpdate) {
		Friend friendCache = PhotoTalkDatabaseFactory.getDatabase().getFriendById(friend.getRcId());
		if (!isUpdate && friendCache != null && friendCache.getAppList() != null) {
			listener.onSuccess(friendCache);
			return;
		}
		RCPlatformResponseHandler responseHandler = new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				try {
					JSONObject jsonObject = new JSONObject(content);
					Friend friend = JSONConver.jsonToObject(jsonObject.getJSONObject("userDetail").toString(), Friend.class);
					friend.setLetter(RCPlatformTextUtil.getLetter(friend.getNickName()));
					PhotoTalkDatabaseFactory.getDatabase().updateFriend(friend);
					if (listener != null)
						listener.onSuccess(friend);
				} catch (JSONException e) {
					e.printStackTrace();
					onFailure(RCPlatformServiceError.ERROR_CODE_REQUEST_FAIL, context.getString(R.string.net_error));
				}

			}

			@Override
			public void onFailure(int errorCode, String content) {
				if (listener != null)
					listener.onError(errorCode, content);
			}
		};

		Request request = new Request(context, PhotoTalkApiUrl.FRIEND_DETAIL_URL, responseHandler);
		request.putParam(PhotoTalkParams.FriendDetail.PARAM_KEY_FRIEND_ID, friend.getRcId());
		if (friend.getSource() != null)
			request.putParam(PhotoTalkParams.FriendDetail.PARAM_KEY_FRIEND_TYPE, friend.getSource().getAttrType() + "");
		else
			request.putParam(PhotoTalkParams.FriendDetail.PARAM_KEY_FRIEND_TYPE, FriendType.DEFAULT + "");
		request.excuteAsync();
	}
}
