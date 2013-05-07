package com.rcplatform.phototalk.task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.rcplatform.phototalk.api.MenueApiUrl;
import com.rcplatform.phototalk.api.PhotoTalkParams;
import com.rcplatform.phototalk.api.RCPlatformAsyncHttpClient;
import com.rcplatform.phototalk.api.RCPlatformAsyncHttpClient.RequestAction;
import com.rcplatform.phototalk.api.RCPlatformResponseHandler;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.UserInfo;

public class AddFriendTask {
	private RCPlatformAsyncHttpClient mHttpClient = new RCPlatformAsyncHttpClient(RequestAction.JSON);
	private Context mContext;
	private RCPlatformResponseHandler mResponseHandler;

	public AddFriendTask(Context context, UserInfo userInfo, RCPlatformResponseHandler responseHandler, Friend... friends) {
		// TODO Auto-generated constructor stub
		this.mContext = context;
		this.mResponseHandler = responseHandler;
		PhotoTalkParams.buildBasicParams(mContext, mHttpClient);
		mHttpClient.putRequestParam(PhotoTalkParams.AddFriends.PARAM_KEY_USER_SUID, userInfo.getSuid());
		buildFriends(friends);
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		mHttpClient.putRequestParam(PhotoTalkParams.AddFriends.PARAM_KEY_FRIENDS, array.toString());
	}

	public void execute() {
		mHttpClient.post(mContext, MenueApiUrl.ADD_FRIEND_URL, mResponseHandler);
	}
}
