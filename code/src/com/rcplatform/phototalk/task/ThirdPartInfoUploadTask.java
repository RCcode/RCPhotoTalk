package com.rcplatform.phototalk.task;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.rcplatform.phototalk.api.PhotoTalkApiUrl;
import com.rcplatform.phototalk.request.PhotoTalkParams;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.request.Request;
import com.rcplatform.phototalk.thirdpart.bean.ThirdPartUser;

public class ThirdPartInfoUploadTask {

	private Request mRequest;
	private int mFriendType;

	public ThirdPartInfoUploadTask(Context context, List<ThirdPartUser> friends, ThirdPartUser user, int type,RCPlatformResponseHandler responseHandler) {
		this.mFriendType = type;
		mRequest = new Request(context, PhotoTalkApiUrl.SYNCHRO_THIRD_URL, responseHandler);
		buildPostParam(context, friends, user);
	}

	private void buildPostParam(Context context, List<ThirdPartUser> friends, ThirdPartUser user) {
		buildFriendParams(friends);
		buildUserParams(user);
		buildOtherParams();
	}

	private void buildUserParams(ThirdPartUser user) {
		if (user != null) {
			try {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put(PhotoTalkParams.ThirdPartBind.PARAM_KEY_ACCOUNT, user.getId());
				jsonObject.put(PhotoTalkParams.ThirdPartBind.PARAM_KEY_HEAD_URL, user.getHeadUrl()+"");
				jsonObject.put(PhotoTalkParams.ThirdPartBind.PARAM_KEY_NICK, user.getNick());
				mRequest.putParam(PhotoTalkParams.ThirdPartBind.PARAM_KEY_MINE_INFO, jsonObject.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private void buildOtherParams() {
		mRequest.putParam(PhotoTalkParams.ThirdPartBind.PARAM_KEY_THIRD_TYPE, mFriendType + "");

	}

	private void buildFriendParams(List<ThirdPartUser> friends) {
		if (friends != null && friends.size() > 0) {
			try {
				JSONArray array = new JSONArray();
				for (ThirdPartUser friend : friends) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put(PhotoTalkParams.ThirdPartBind.PARAM_KEY_FRIEND_ID, friend.getId());
					jsonObject.put(PhotoTalkParams.ThirdPartBind.PARAM_KEY_FRIEND_URL, friend.getHeadUrl());
					jsonObject.put(PhotoTalkParams.ThirdPartBind.PARAM_KEY_FRIEND_NICK, friend.getNick());
					array.put(jsonObject);
				}
				mRequest.putParam(PhotoTalkParams.ThirdPartBind.PARAM_KEY_FRIEND_LIST, array.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void start() {
		mRequest.excuteAsync();
	}
}
