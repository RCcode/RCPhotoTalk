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

import android.content.Context;

import com.rcplatform.phototalk.MenueApplication;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.api.MenueApiUrl;
import com.rcplatform.phototalk.bean.AppInfo;
import com.rcplatform.phototalk.bean.Contacts;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.InformationState;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.galhttprequest.MD5;
import com.rcplatform.phototalk.galhttprequest.RCPlatformServiceError;
import com.rcplatform.phototalk.logic.MessageSender;
import com.rcplatform.phototalk.request.inf.OnFriendsLoadedListener;
import com.rcplatform.phototalk.request.inf.OnUserInfoLoadedListener;
import com.rcplatform.phototalk.request.inf.PhotoSendListener;
import com.rcplatform.phototalk.utils.ContactUtil;

public class Request implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Context mContext;
	private long createTime;
	private String url;
	private RCPlatformResponseHandler responseHandler;
	private Map<String, String> params;;
	private File file;
	private String filePath;
	private boolean cache = false;

	private static final int MSG_WHAT_LOCAL_RECOMMENDS_LOADED = 20000;

	public Request() {
		createTime = System.currentTimeMillis();
	}

	public Request(Context context, String url, RCPlatformResponseHandler responseHandler) {
		this();
		this.url = url;
		this.responseHandler = responseHandler;
		this.mContext = context;
		params = new HashMap<String, String>();
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
		((MenueApplication) mContext.getApplicationContext()).getWebService().post(this);
	}

	public void excutePostNameValuePairAsync() {
		if (params.size() == 0) {
			return;
		}
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
					UserInfo user = buildUserInfo(content);
					user.setShowRecommends(UserInfo.NOT_FIRST_TIME);
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
					}, user.getRcId());
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
		Request request = new Request(context, MenueApiUrl.LOGIN_URL, responseHandler);
		PhotoTalkParams.buildBasicParams(context, request);
		request.putParam(PhotoTalkParams.Login.PARAM_KEY_ACCOUNT, account);
		request.putParam(PhotoTalkParams.Login.PARAM_KEY_PASSWORD, MD5.encodeMD5String(password));
		request.excuteAsync();
	}

	public static void sendPhoto(final Context context, final long flag, File file, String timeLimit, final PhotoSendListener listener,
			final List<String> friendIds) {
		final UserInfo currentUser = ((MenueApplication) context.getApplicationContext()).getCurrentUser();
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
									flag, userIds, friendIds, InformationState.PhotoInformationState.STATU_NOTICE_SENDED_OR_NEED_LOADD);
							MessageSender.sendInformation(context, informations, userIds);
							listener.onSendSuccess(flag);
						} catch (JSONException e) {
							e.printStackTrace();
							onFailure(RCPlatformServiceError.ERROR_CODE_REQUEST_FAIL, content);
						}
					}

					@Override
					public void onFailure(int errorCode, String content) {
						listener.onFail(flag, errorCode, content);
						UserInfo currentUser = ((MenueApplication) context.getApplicationContext()).getCurrentUser();
						PhotoTalkDatabaseFactory.getDatabase().updateTempInformations(currentUser, null, flag, null, friendIds,
								InformationState.PhotoInformationState.STATU_NOTICE_SEND_FAIL);
					}
				};
			}
			Request request = new Request(context, MenueApiUrl.SEND_PICTURE_URL, responseHandler);
			request.setCreateTime(flag);
			request.setFile(file);
			request.putParam(PhotoTalkParams.SendPhoto.PARAM_KEY_FLAG, flag + "");
			request.putParam(PhotoTalkParams.SendPhoto.PARAM_KEY_TIME_LIMIT, timeLimit);
			request.putParam(PhotoTalkParams.SendPhoto.PARAM_KEY_USERS, jsonArray.toString());
			request.excuteAsync();
		} catch (Exception e) {
			e.printStackTrace();
			listener.onFail(flag, RCPlatformServiceError.ERROR_CODE_REQUEST_FAIL, context.getString(R.string.net_error));
			PhotoTalkDatabaseFactory.getDatabase().updateTempInformations(currentUser, null, flag, null, friendIds,
					InformationState.PhotoInformationState.STATU_NOTICE_SEND_FAIL);
		}
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	private static UserInfo buildUserInfo(String content) throws Exception {
		UserInfo userInfo = new UserInfo();
		JSONObject jsonObject = new JSONObject(content);
		userInfo.setToken(jsonObject.getString("token"));
		userInfo.setTigaseId(jsonObject.getString("tgId"));
		userInfo.setTigasePwd(jsonObject.getString("tgpwd"));
		userInfo.setRcId(jsonObject.getString("rcId"));
		userInfo.setEmail(jsonObject.optString("email", null));
		userInfo.setNickName(jsonObject.optString("nickName", null));
		return userInfo;
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

	public static void executeGetMyInfo(final Context context, final OnUserInfoLoadedListener listener, String rcId) {
		RCPlatformResponseHandler responseHandler = new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				try {
					JSONObject jsonObject = new JSONObject(content);
					JSONObject jObj = jsonObject.getJSONObject("userInfo");
					JSONObject jsonUser = jObj.getJSONObject("userInfo");
					UserInfo userInfo = JSONConver.jsonToObject(jsonUser.toString(), UserInfo.class);
					JSONArray arrayApps = jObj.getJSONArray("allApp");
					List<AppInfo> apps = JSONConver.jsonToList(arrayApps.toString(), AppInfo.class);
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
		Request request = new Request(context, MenueApiUrl.GET_USER_INFO, responseHandler);
		if (rcId != null)
			request.putParam(PhotoTalkParams.PARAM_KEY_USER_ID, rcId);
		request.excuteAsync();
	}

	public static void executeGetRecommends(final BaseActivity context, final int type, final OnFriendsLoadedListener listener) {

		Thread thread = new Thread() {
			@Override
			public void run() {
				final List<Friend> recommends = PhotoTalkDatabaseFactory.getDatabase().getRecommends(type);
				final List<Contacts> localContacts = ContactUtil.getContacts(context);
				final RCPlatformResponseHandler responseHandler = new RCPlatformResponseHandler() {

					@Override
					public void onSuccess(int statusCode, String content) {
						try {
							JSONObject jObj = new JSONObject(content);
							List<Friend> recommend = JSONConver.jsonToList(jObj.getJSONArray("userList").toString(), Friend.class);
							listener.onServiceFriendsLoaded(ContactUtil.getContactFriendNotRepeat(localContacts, recommend), recommend);
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
							listener.onLocalFriendsLoaded(ContactUtil.getContactFriendNotRepeat(localContacts, recommends), recommends);
						}
					});
				}
				Request request = new Request(context, "", responseHandler);
				request.excuteAsync();
			}
		};
		thread.start();
	}
}
