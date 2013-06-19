package com.rcplatform.phototalk.task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.api.PhotoTalkApiUrl;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.logic.LogicUtils;
import com.rcplatform.phototalk.request.JSONConver;
import com.rcplatform.phototalk.request.PhotoTalkParams;
import com.rcplatform.phototalk.request.RCPlatformAsyncHttpClient;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.request.RCPlatformServiceError;
import com.rcplatform.phototalk.request.Request;
import com.rcplatform.phototalk.utils.RCPlatformTextUtil;

public class AddFriendTask {
	private RCPlatformAsyncHttpClient mHttpClient = new RCPlatformAsyncHttpClient();
	private Context mContext;
	private AddFriendListener mListener;
	private Request mRequest;

	public AddFriendTask(Context context, UserInfo userInfo, AddFriendListener listener, Friend... friends) {
		this.mContext = context;
		this.mListener = listener;
		mRequest = new Request(context, PhotoTalkApiUrl.ADD_FRIEND_URL, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				if (mListener != null) {
					try {
						JSONObject jsonObject = new JSONObject(content);
						int addType = jsonObject.getInt("isFriend");
						Friend friend = JSONConver.jsonToObject(jsonObject.getJSONArray("userInfo").getJSONObject(0).toString(), Friend.class);
						friend.setFriend(true);
						friend.setLetter(RCPlatformTextUtil.getLetter(friend.getNickName()));
						mListener.onFriendAddSuccess(friend, addType);
						PhotoTalkDatabaseFactory.getDatabase().addFriend(friend);
						LogicUtils.friendAdded(mContext, friend, addType);
					} catch (JSONException e) {
						e.printStackTrace();
						onFailure(RCPlatformServiceError.ERROR_CODE_REQUEST_FAIL, mContext.getString(R.string.net_error));
					}
				}
			}

			@Override
			public void onFailure(int errorCode, String content) {
				if (errorCode == RCPlatformServiceError.ERROR_CODE_FRIEND_ALREADY_ADDED) {
					if (mListener != null) {
						mListener.onAlreadyAdded();
					}
					return;
				}
				if (mListener != null)
					mListener.onFriendAddFail(errorCode, content);
			}
		});
		mRequest.putParam(PhotoTalkParams.AddFriends.PARAM_KEY_USER_SUID, userInfo.getRcId());
		buildFriends(friends);
	}

	private void buildFriends(Friend... friends) {
		JSONArray array = new JSONArray();
		for (Friend friend : friends) {
			try {
				JSONObject jsonFriend = new JSONObject();
				jsonFriend.put(PhotoTalkParams.AddFriends.PARAM_KEY_FRIEND_SUID, friend.getRcId());
				if (friend.getSource() != null){
					jsonFriend.put(PhotoTalkParams.AddFriends.PARAM_KEY_FRIEND_TYPE, friend.getSource().getAttrType());
					jsonFriend.put(PhotoTalkParams.AddFriends.PARAM_KEY_FRIEND_SOURCE_NAME, friend.getSource().getName());
					jsonFriend.put(PhotoTalkParams.AddFriends.PARAM_KEY_FRIEND_SOURCE_VALUE, friend.getSource().getValue());
				}
				array.put(jsonFriend);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		mRequest.putParam(PhotoTalkParams.AddFriends.PARAM_KEY_FRIENDS, array.toString());
	}

	public void execute() {
		mHttpClient.post(mRequest);
	}

	public static interface AddFriendListener {
		public void onFriendAddSuccess(Friend friend, int addType);

		public void onAlreadyAdded();

		public void onFriendAddFail(int statusCode, String content);
	}
}
