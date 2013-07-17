package com.rcplatform.phototalk.task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.api.PhotoTalkApiUrl;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.FriendType;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.drift.DriftInformation;
import com.rcplatform.phototalk.logic.LogicUtils;
import com.rcplatform.phototalk.request.JSONConver;
import com.rcplatform.phototalk.request.PhotoTalkParams;
import com.rcplatform.phototalk.request.RCPlatformAsyncHttpClient;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.request.RCPlatformServiceError;
import com.rcplatform.phototalk.request.Request;
import com.rcplatform.phototalk.utils.RCPlatformTextUtil;

public class SkyPoolAddFriendTask {
	private RCPlatformAsyncHttpClient mHttpClient = new RCPlatformAsyncHttpClient();
	private Context mContext;
	private SkyPoolAddFriendListener mListener;
	private Request mRequest;
	private DriftInformation information;

	public SkyPoolAddFriendTask(Context context, UserInfo userInfo,
			SkyPoolAddFriendListener skyPoolAddFriendListener,
			DriftInformation information, Friend... mFriend) {
		this.mContext = context;
		this.mListener = skyPoolAddFriendListener;
		this.information = information;
		mRequest = new Request(context, PhotoTalkApiUrl.SKY_POOL_ADD_FRIEND,
				new RCPlatformResponseHandler() {

					@Override
					public void onSuccess(int statusCode, String content) {
						if (mListener != null) {
							try {
								JSONObject jsonObject = new JSONObject(content);
//								int addType = jsonObject.getInt("isFriend");
								Friend friend = JSONConver.jsonToObject(
										jsonObject.toString(),
										Friend.class);
								friend.setFriend(true);
								friend.setLetter(RCPlatformTextUtil
										.getLetter(friend.getNickName()));
								mListener.onFriendAddSuccess(friend, friend.getAdded());
								PhotoTalkDatabaseFactory.getDatabase()
										.addFriend(friend);
								LogicUtils.friendAdded(mContext, friend,
										friend.getAdded());
							} catch (JSONException e) {
								e.printStackTrace();
								onFailure(
										RCPlatformServiceError.ERROR_CODE_REQUEST_FAIL,
										mContext.getString(R.string.net_error));
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

		mRequest.putParam(PhotoTalkParams.ReportPicture.PARAM_KEY_COUNTRY,
				userInfo.getCountry());
		mRequest.putParam(PhotoTalkParams.ReportPicture.PARAM_KEY_GENDER,
				userInfo.getGender() + "");
		mRequest.putParam(PhotoTalkParams.ReportPicture.PARAM_KEY_PICID,
				information.getPicId() + "");
		mRequest.putParam(PhotoTalkParams.ReportPicture.PARAM_KEY_REP_COUNTRY,
				information.getSender().getCountry());
		mRequest.putParam(PhotoTalkParams.ReportPicture.PARAM_KEY_REP_GENDER,
				information.getSender().getGender() + "");
		mRequest.putParam(PhotoTalkParams.AddFriends.PARAM_KEY_USER_SUID,
				userInfo.getRcId());
		buildFriends(mFriend);
	}


	private void buildFriends(Friend... friends) {
		JSONArray array = new JSONArray();
		for (Friend friend : friends) {
			try {
				JSONObject jsonFriend = new JSONObject();
				jsonFriend.put(
						PhotoTalkParams.AddFriends.PARAM_KEY_FRIEND_SUID,
						friend.getRcId());
				if (friend.getSource() != null) {
					jsonFriend.put(
							PhotoTalkParams.AddFriends.PARAM_KEY_FRIEND_TYPE,
							FriendType.SKY_POOL);
					jsonFriend
							.put(PhotoTalkParams.AddFriends.PARAM_KEY_FRIEND_SOURCE_NAME,
									friend.getSource().getName());
					jsonFriend
							.put(PhotoTalkParams.AddFriends.PARAM_KEY_FRIEND_SOURCE_VALUE,
									friend.getSource().getValue());
				}
				array.put(jsonFriend);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		mRequest.putParam(PhotoTalkParams.AddFriends.PARAM_KEY_FRIENDS,
				array.toString());
	}

	public void execute() {
		mHttpClient.post(mRequest);
	}

	public static interface SkyPoolAddFriendListener {
		public void onFriendAddSuccess(Friend friend, int addType);

		public void onAlreadyAdded();

		public void onFriendAddFail(int statusCode, String content);
	}
}
