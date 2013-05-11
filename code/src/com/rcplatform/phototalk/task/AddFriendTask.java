package com.rcplatform.phototalk.task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.api.MenueApiUrl;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.galhttprequest.RCPlatformServiceError;
import com.rcplatform.phototalk.logic.LogicUtils;
import com.rcplatform.phototalk.request.PhotoTalkParams;
import com.rcplatform.phototalk.request.RCPlatformAsyncHttpClient;
import com.rcplatform.phototalk.request.RCPlatformAsyncHttpClient.RequestAction;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;

public class AddFriendTask {
	private RCPlatformAsyncHttpClient mHttpClient = new RCPlatformAsyncHttpClient(RequestAction.JSON);
	private Context mContext;
	private AddFriendListener mListener;
	private Friend mFriend;

	public AddFriendTask(Context context, UserInfo userInfo, AddFriendListener listener, Friend... friends) {
		this.mContext = context;
		this.mListener = listener;
		PhotoTalkParams.buildBasicParams(mContext, mHttpClient);
		mHttpClient.putRequestParam(PhotoTalkParams.AddFriends.PARAM_KEY_USER_SUID, userInfo.getSuid());
		buildFriends(friends);
		mFriend = friends[0];
	}

	private void buildFriends(Friend... friends) {
		JSONArray array = new JSONArray();
		for (Friend friend : friends) {
			try {
				JSONObject jsonFriend = new JSONObject();
				jsonFriend.put(PhotoTalkParams.AddFriends.PARAM_KEY_FRIEND_SUID, friend.getSuid());
				if (friend.getSource() != null)
					jsonFriend.put(PhotoTalkParams.AddFriends.PARAM_KEY_FRIEND_TYPE, friend.getSource().getAttrType());
				array.put(jsonFriend);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		mHttpClient.putRequestParam(PhotoTalkParams.AddFriends.PARAM_KEY_FRIENDS, array.toString());
	}

	public void execute() {
		mHttpClient.post(mContext, MenueApiUrl.ADD_FRIEND_URL, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				if (mListener != null) {
					try {
						JSONObject jsonObject = new JSONObject(content);
						int addType = jsonObject.getInt("isFriend");
						mListener.onFriendAddSuccess(addType);
						LogicUtils.friendAdded(mContext, mFriend, addType);
					} catch (JSONException e) {
						e.printStackTrace();
						onFailure(RCPlatformServiceError.ERROR_CODE_REQUEST_FAIL, mContext.getString(R.string.net_error));
					}
				}
			}

			@Override
			public void onFailure(int errorCode, String content) {
				if (mListener != null)
					mListener.onFriendAddFail(errorCode, content);
			}
		});
	}

	public static interface AddFriendListener {
		public void onFriendAddSuccess(int addType);
		public void onFriendAddFail(int statusCode, String content);
	}
}
