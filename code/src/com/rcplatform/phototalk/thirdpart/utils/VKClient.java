package com.rcplatform.phototalk.thirdpart.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;

import com.perm.kate.api.Api;
import com.perm.kate.api.User;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.VKAuthorizeActivity;
import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.bean.FriendType;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.thirdpart.bean.ThirdPartUser;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.PrefsUtils;

public class VKClient {
	private BaseActivity mContext;
	private Api mApi;
	private long mUid;
	private String mToken;

	private static final int REQUEST_CODE_LOGIN = 100;
	private static final int MSG_WHAT_GETINFO_SUCCESS = 200;
	private static final int MSG_WHAT_GETINFO_ERROR = 500;

	private static final int MSG_WHAT_DEAUTHORIZE_SUCCESS = 201;
	private static final int MSG_WHAT_DEAUTHORIZE_FAIL = 501;

	private OnAuthorizeSuccessListener mAuthorizeSuccessListener;
	private OnGetThirdPartInfoSuccessListener mGetListener;
	private OnDeAuthorizeListener mDeAuthorizeListener;

	private List<ThirdPartUser> mFriends;
	private ThirdPartUser mUser;

	public VKClient(BaseActivity context) {
		mContext = context;
		if (isAuthorize()) {
			Object[] account = PrefsUtils.User.ThirdPart.getVKAccount(mContext, mContext.getCurrentUser().getRcId());
			mToken = (String) account[0];
			mApi = new Api(mToken, Constants.VK_API_ID);
			mUid = (Long) account[1];
		}
	}

	public boolean isAuthorize() {
		return ThirdPartUtils.isVKVlidated(mContext, mContext.getCurrentUser().getRcId());
	}

	public void authorize(OnAuthorizeSuccessListener listener) {
		this.mAuthorizeSuccessListener = listener;
		Intent intent = new Intent();
		intent.setClass(mContext, VKAuthorizeActivity.class);
		mContext.startActivityForResult(intent, REQUEST_CODE_LOGIN);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_LOGIN) {
			if (resultCode == Activity.RESULT_OK) {
				// авторизовались успешно
				String access_token = data.getStringExtra("token");
				mUid = data.getLongExtra("user_id", 0);
				PrefsUtils.User.ThirdPart.saveVKAccount(mContext, mContext.getCurrentUser().getRcId(), access_token, mUid);
				mApi = new Api(access_token, com.rcplatform.phototalk.utils.Constants.VK_API_ID);
				sendJoinMessage();
				if (mAuthorizeSuccessListener != null)
					mAuthorizeSuccessListener.onAuthorizeSuccess();
			}
		}
	}

	private void sendJoinMessage() {
		Thread thread = new Thread() {
			public void run() {
				try {
					List<String> attachments = new ArrayList<String>();
					attachments.add(Constants.INVITE_URL);
					mApi.createWallPost(mUid, mContext.getString(R.string.join_message), attachments, null, false, false, false, null, null, null, null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		};
		thread.start();
	};

	public void getVKInfo(final OnGetThirdPartInfoSuccessListener listener) {
		mContext.showLoadingDialog(BaseActivity.LOADING_NO_MSG, BaseActivity.LOADING_NO_MSG, false);
		this.mGetListener = listener;
		Thread thread = new Thread() {
			public void run() {
				try {
					ArrayList<User> users = mApi.getFriends(mUid, null, null, null, null);
					mFriends = ThirdPartUtils.parserVKUserToThirdPartUser(users);
					PhotoTalkDatabaseFactory.getDatabase().saveThirdPartFriends(mFriends, FriendType.VK);
					List<User> userProfiles = mApi.getProfiles(Arrays.asList(mUid), null, null, null, null, null);
					mUser = ThirdPartUtils.parserVKUserToThirdPartUser(userProfiles.get(0));
					PrefsUtils.User.ThirdPart.refreshVKSyncTime(mContext.getApplicationContext(), mContext.getCurrentUser().getRcId());
					mVKHandler.sendEmptyMessage(MSG_WHAT_GETINFO_SUCCESS);
				} catch (Exception e) {
					e.printStackTrace();
					mVKHandler.sendEmptyMessage(MSG_WHAT_GETINFO_ERROR);
				}
			};
		};
		thread.start();
	}

	private Handler mVKHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			mContext.dismissLoadingDialog();
			switch (msg.what) {
			case MSG_WHAT_GETINFO_ERROR:
				if (mGetListener != null)
					mGetListener.onGetFail();
				break;
			case MSG_WHAT_GETINFO_SUCCESS:
				if (mGetListener != null)
					mGetListener.onGetInfoSuccess(mUser, mFriends);
				break;
			case MSG_WHAT_DEAUTHORIZE_FAIL:
				if (mDeAuthorizeListener != null)
					mDeAuthorizeListener.onDeAuthorizeFail();
				break;
			case MSG_WHAT_DEAUTHORIZE_SUCCESS:
				if (mDeAuthorizeListener != null)
					mDeAuthorizeListener.onDeAuthorizeSuccess();
				break;
			}
		};
	};

	public void deAuthorize(OnDeAuthorizeListener listener) {
		mDeAuthorizeListener = listener;
		mContext.showLoadingDialog(BaseActivity.LOADING_NO_MSG, BaseActivity.LOADING_NO_MSG, false);
		Thread thread = new Thread() {
			public void run() {
				PrefsUtils.User.ThirdPart.clearVKAccount(mContext, mContext.getCurrentUser().getRcId());
				mVKHandler.sendEmptyMessage(MSG_WHAT_DEAUTHORIZE_SUCCESS);
			};
		};
		thread.start();
	}

	public void sendInviteMessage(final Collection<String> friendIds) {
		Thread thread = new Thread() {
			public void run() {

				for (String id : friendIds) {
					try {
						List<String> attachments = new ArrayList<String>();
						attachments.add(Constants.INVITE_URL);
						mApi.createWallPost(Long.parseLong(id), mContext.getString(R.string.invite_message, mContext.getCurrentUser().getRcId()), attachments,
								null, false, false, false, null, null, null, null);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
		};
		thread.start();
	}
}
