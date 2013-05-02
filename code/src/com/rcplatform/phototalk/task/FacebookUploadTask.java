package com.rcplatform.phototalk.task;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.facebook.model.GraphUser;
import com.rcplatform.phototalk.api.MenueApiUrl;
import com.rcplatform.phototalk.api.PhotoTalkParams;
import com.rcplatform.phototalk.api.RCPlatformAsyncHttpClient;
import com.rcplatform.phototalk.api.RCPlatformAsyncHttpClient.RequestAction;
import com.rcplatform.phototalk.api.RCPlatformResponseHandler;
import com.rcplatform.phototalk.thirdpart.bean.ThirdPartFriend;
import com.rcplatform.phototalk.thirdpart.bean.ThirdPartUserInfo;

public class FacebookUploadTask {

	/**
	 * {"token":"000000","userId":"DcZTw+RZVA8=","attrType":1,"thirdInfo":{
	 * "account"
	 * :"facebook账户id","headUrl":"http://12312312312312312.jpg"},"friendList"
	 * :[{"friendAccount"
	 * :"2","headUrl":"http://aaaaaaaa0","friendName":"3123昵称"}
	 * ,{"friendAccount":
	 * "1","headUrl":"http://aaaaaaaa1","friendName":"2323-昵称"}
	 * ],"language":"zh_CN","deviceId":"123456"}
	 */
	private RCPlatformAsyncHttpClient mClient;
	private Context mContext;
	private RCPlatformResponseHandler mResponseHandler;

	public FacebookUploadTask(Context context, List<ThirdPartFriend> friends, GraphUser user) {
		// TODO Auto-generated constructor stub
		this.mContext = context;
		mClient = new RCPlatformAsyncHttpClient(RequestAction.JSON);
		buildPostParam(context, friends, user);

	}

	public void cancel() {
		mClient.cancel();
	}

	public FacebookUploadTask(Context context, List<ThirdPartFriend> friends, ThirdPartUserInfo user) {
		// TODO Auto-generated constructor stub
		this.mContext = context;
		mClient = new RCPlatformAsyncHttpClient(RequestAction.JSON);
		buildPostParam(context, friends, user);
	}

	private void buildPostParam(Context context, List<ThirdPartFriend> friends, ThirdPartUserInfo user) {
		// TODO Auto-generated method stub
		PhotoTalkParams.buildBasicParams(context, mClient);
		buildFriendParams(friends);
		buildUserParams(user);
		buildOtherParams();
	}

	private void buildUserParams(ThirdPartUserInfo user) {
		// TODO Auto-generated method stub
		if (user != null) {
			try {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("account", user.getId());
				jsonObject.put("headUrl", "123");
				jsonObject.put("nick", user.getUserName());
				mClient.putRequestParam("thirdInfo", jsonObject.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void buildUserParams(GraphUser user) {
		// TODO Auto-generated method stub
		if (user != null) {
			try {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("account", user.getId());
				jsonObject.put("headUrl", "123");
				jsonObject.put("nick", user.getName());
				mClient.putRequestParam("thirdInfo", jsonObject.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void buildPostParam(Context context, List<ThirdPartFriend> friends, GraphUser user) {
		// TODO Auto-generated method stub
		PhotoTalkParams.buildBasicParams(context, mClient);
		buildFriendParams(friends);
		buildUserParams(user);
		buildOtherParams();
	}

	private void buildOtherParams() {
		// TODO Auto-generated method stub
		mClient.putRequestParam("attrType", "1");
	}

	private void buildFriendParams(List<ThirdPartFriend> friends) {
		// TODO Auto-generated method stub
		if (friends != null && friends.size() > 0) {
			try {
				JSONArray array = new JSONArray();
				for (ThirdPartFriend friend : friends) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("friendAccount", friend.getId());
					jsonObject.put("headUrl", friend.getHeadUrl());
					jsonObject.put("friendName", friend.getNick());
					array.put(jsonObject);
				}
				mClient.putRequestParam("friendList", array.toString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void start() {
		mClient.post(mContext, MenueApiUrl.SYNCHRO_THIRD_URL, mResponseHandler);
	}

	public void setResponseListener(RCPlatformResponseHandler listener) {
		this.mResponseHandler = listener;
	}
}
