package com.rcplatform.phototalk.request;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.rcplatform.phototalk.MenueApplication;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.api.MenueApiUrl;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.galhttprequest.MD5;
import com.rcplatform.phototalk.galhttprequest.RCPlatformServiceError;
import com.rcplatform.phototalk.logic.LogicUtils;
import com.rcplatform.phototalk.request.inf.FriendDetailListener;
import com.rcplatform.phototalk.request.inf.OnUserInfoLoadedListener;

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

	public Request() {
	}

	public Request(Context context, String url, RCPlatformResponseHandler responseHandler) {
		this.url = url;
		this.responseHandler = responseHandler;
		this.mContext = context;
		params = new HashMap<String, String>();
		PhotoTalkParams.buildBasicParams(mContext, this);
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

	/**
	 * 登陆接口，获取用户的信息
	 * 
	 * @param context
	 * @param listener
	 * @param account
	 * @param password
	 * @return
	 */
	public static Request login(final Context context, final OnUserInfoLoadedListener listener, String account, String password) {
		RCPlatformResponseHandler responseHandler = new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				try {
					UserInfo user = buildUserInfo(content);
					user.setShowRecommends(UserInfo.NOT_FIRST_TIME);
					user.setEmail("iop@iop.com");
					listener.onSuccess(user);
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
		Request request = new Request(context, MenueApiUrl.TIGASE_LOGIN_URL, responseHandler);
		PhotoTalkParams.buildBasicParams(context, request);
		request.putParam(PhotoTalkParams.Login.PARAM_KEY_ACCOUNT, account);
		request.putParam(PhotoTalkParams.Login.PARAM_KEY_PASSWORD, MD5.encodeMD5String(password));
		return request;
	}

	public static Request addFriend(final Context context, FriendDetailListener listener, Friend... friend) {
		RCPlatformResponseHandler responseHandler = new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				try {
					JSONObject jsonObject = new JSONObject(content);
					int addType = jsonObject.getInt("isFriend");
					// listener.onSuccess(friend)
					// LogicUtils.friendAdded(context, mFriend, addType);
				} catch (JSONException e) {
					e.printStackTrace();
					onFailure(RCPlatformServiceError.ERROR_CODE_REQUEST_FAIL, context.getString(R.string.net_error));
				}
			}

			@Override
			public void onFailure(int errorCode, String content) {

			}
		};
		Request request = new Request(context, MenueApiUrl.ADD_FRIEND_URL, responseHandler);
		buildFriends(request, friend);
		return request;
	}

	private static void buildFriends(Request request, Friend... friends) {
		JSONArray array = new JSONArray();
		for (Friend friend : friends) {
			try {
				JSONObject jsonFriend = new JSONObject();
				jsonFriend.put(PhotoTalkParams.AddFriends.PARAM_KEY_FRIEND_SUID, friend.getRcId());
				if (friend.getSource() != null)
					jsonFriend.put(PhotoTalkParams.AddFriends.PARAM_KEY_FRIEND_TYPE, friend.getSource().getAttrType());
				array.put(jsonFriend);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		request.putParam(PhotoTalkParams.AddFriends.PARAM_KEY_FRIENDS, array.toString());
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
		userInfo.setTigasePassword(jsonObject.getString("tgpwd"));
		userInfo.setRcId(jsonObject.getString("rcId"));
		userInfo.setEmail(jsonObject.optString("email", null));
		userInfo.setNick(jsonObject.optString("nick", null));
		return userInfo;
	}
}
