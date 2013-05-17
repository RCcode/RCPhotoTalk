package com.rcplatform.phototalk.task;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.facebook.model.GraphUser;
import com.rcplatform.phototalk.api.MenueApiUrl;
import com.rcplatform.phototalk.request.PhotoTalkParams;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.request.Request;
import com.rcplatform.phototalk.thirdpart.bean.ThirdPartFriend;
import com.rcplatform.phototalk.thirdpart.bean.ThirdPartUserInfo;

public class FacebookUploadTask {

	private RCPlatformResponseHandler mResponseHandler;
	private Request mRequest;

	public FacebookUploadTask(Context context, List<ThirdPartFriend> friends, GraphUser user) {
		mRequest = new Request(context, MenueApiUrl.SYNCHRO_THIRD_URL, mResponseHandler);
		buildPostParam(context, friends, user);

	}

	public FacebookUploadTask(Context context, List<ThirdPartFriend> friends, ThirdPartUserInfo user) {
		mRequest = new Request(context, MenueApiUrl.SYNCHRO_THIRD_URL, mResponseHandler);
		buildPostParam(context, friends, user);
	}

	private void buildPostParam(Context context, List<ThirdPartFriend> friends, ThirdPartUserInfo user) {
		buildFriendParams(friends);
		buildUserParams(user);
		buildOtherParams();
	}

	private void buildUserParams(ThirdPartUserInfo user) {
		if (user != null) {
			try {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put(PhotoTalkParams.ThirdPartBind.PARAM_KEY_ACCOUNT, user.getId());
				jsonObject.put(PhotoTalkParams.ThirdPartBind.PARAM_KEY_HEAD_URL, "123");
				jsonObject.put(PhotoTalkParams.ThirdPartBind.PARAM_KEY_NICK, user.getUserName());
				mRequest.putParam("thirdInfo", jsonObject.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private void buildUserParams(GraphUser user) {
		if (user != null) {
			try {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put(PhotoTalkParams.ThirdPartBind.PARAM_KEY_ACCOUNT, user.getId());
				jsonObject.put(PhotoTalkParams.ThirdPartBind.PARAM_KEY_HEAD_URL, "123");
				jsonObject.put(PhotoTalkParams.ThirdPartBind.PARAM_KEY_NICK, user.getName());
				mRequest.putParam("thirdInfo", jsonObject.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private void buildPostParam(Context context, List<ThirdPartFriend> friends, GraphUser user) {
		buildFriendParams(friends);
		buildUserParams(user);
		buildOtherParams();
	}

	private void buildOtherParams() {
		mRequest.putParam(PhotoTalkParams.ThirdPartBind.PARAM_KEY_THIRD_TYPE, "1");
	}

	private void buildFriendParams(List<ThirdPartFriend> friends) {
		if (friends != null && friends.size() > 0) {
			try {
				JSONArray array = new JSONArray();
				for (ThirdPartFriend friend : friends) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put(PhotoTalkParams.ThirdPartBind.PARAM_KEY_FRIEND_ID, friend.getId());
					jsonObject.put(PhotoTalkParams.ThirdPartBind.PARAM_KEY_FRIEND_URL, friend.getHeadUrl());
					jsonObject.put(PhotoTalkParams.ThirdPartBind.PARAM_KEY_FRIEND_NICK, friend.getNick());
					array.put(jsonObject);
				}
				mRequest.putParam("friendList", array.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void start() {
		mRequest.excuteAsync();
	}

	public void setResponseListener(RCPlatformResponseHandler listener) {
		this.mResponseHandler = listener;
	}
}
