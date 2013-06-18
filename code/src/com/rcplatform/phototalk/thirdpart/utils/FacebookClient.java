package com.rcplatform.phototalk.thirdpart.utils;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Request.Callback;
import com.facebook.Request.GraphUserCallback;
import com.facebook.Request.GraphUserListCallback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.galhttprequest.LogUtil;
import com.rcplatform.phototalk.thirdpart.bean.ThirdPartUser;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.FacebookUtil;
import com.rcplatform.phototalk.utils.PrefsUtils;

public class FacebookClient {

	private static final int MSG_DEAUTHORIZE_SUCCESS = 100;

	private static final int MSG_DEAUTHORIZE_ERROR = 101;

	private static final int MSG_NET_ERROR = 102;

	private OnAuthorizeSuccessListener mAuthListener;

	private OnGetThirdPartInfoSuccessListener mGetInfoSuccessListener;

	private OnDeAuthorizeListener mDeAuthorizeListener;

	private UiLifecycleHelper mUiLifecycleHelper;

	private BaseActivity mContext;

	private boolean hasFriendsLoaded = false;

	private boolean hasUserInfoLoaded = false;

	private boolean isGetFacebookInfoError = false;

	private ThirdPartUser mUser;

	private List<ThirdPartUser> mFriends;

	private enum FacebookAction {
		NONE, AUTHORIZE, SEND_INVATE, GET_INFO, SEND_JOIN_MESSAGE, DE_AUTHORIZE;
	}

	private FacebookAction mAction = FacebookAction.NONE;

	public FacebookClient(BaseActivity context) {
		this.mContext = context;
		mUiLifecycleHelper = new UiLifecycleHelper(context, mSessionStatusCallback);
		if (ThirdPartUtils.isFacebookVlidate(mContext))
			openSession();
	}

	public void onCreate(Bundle savedInstanceState) {
		mUiLifecycleHelper.onCreate(savedInstanceState);
	}

	public void onResume() {
		mUiLifecycleHelper.onResume();
	}

	public void onPause() {
		mUiLifecycleHelper.onPause();
	}

	public void onDestroy() {
		mUiLifecycleHelper.onDestroy();
		Session.getActiveSession().removeCallback(mSessionStatusCallback);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		mUiLifecycleHelper.onActivityResult(requestCode, resultCode, data);
	}

	public ThirdPartUser getUserFacebookInfo() {
		return null;
	}

	public List<ThirdPartUser> getUserFacebookFriends() {
		return null;
	}

	public void sendInviteMessageToUser(final String uid, final OnInviteSuccessListener listener) {
		Bundle bundler = new Bundle();
		bundler.putString("caption", mContext.getString(R.string.invite_message, mContext.getCurrentUser().getRcId()));
		bundler.putString("description", " ");
		bundler.putString("name", mContext.getString(R.string.app_name));
		bundler.putString("picture", Constants.INVITE_JOIN_IMAGE_URL);
		bundler.putString("to", uid);
		bundler.putString("link", Constants.INVITE_URL);
		WebDialog dialog = new WebDialog.FeedDialogBuilder(mContext, Session.getActiveSession(), bundler).setOnCompleteListener(new OnCompleteListener() {

			@Override
			public void onComplete(Bundle values, FacebookException error) {
				if (error == null) {
					final String postId = values.getString("post_id");
					if (postId != null) {
//						mContext.showErrorConfirmDialog(R.string.invite_success);
						 Toast.makeText(mContext, mContext.getResources().getString(R.string.save_success), Toast.LENGTH_SHORT).show();
						if (listener != null)
							listener.onInviteSuccess(uid);
					} else {
						// mContext.showErrorConfirmDialog(R.string.invite_cancel);
					}
				} else {
					// mContext.showErrorConfirmDialog(TextUtils.isEmpty(error.getMessage())
					// ? mContext.getString(R.string.invite_fail) :
					// error.getMessage());
				}
			}
		}).build();
		dialog.show();
	}

	public void sendJoinMessage() {
		Bundle bundler = new Bundle();
		bundler.putString("caption", mContext.getString(R.string.invite_message, mContext.getCurrentUser().getRcId()));
		bundler.putString("description", " ");
		bundler.putString("name", mContext.getString(R.string.app_name));
		bundler.putString("picture", Constants.INVITE_JOIN_IMAGE_URL);
		bundler.putString("link", Constants.INVITE_URL);
		WebDialog dialog = new WebDialog.FeedDialogBuilder(mContext, Session.getActiveSession(), bundler).setOnCompleteListener(new OnCompleteListener() {

			@Override
			public void onComplete(Bundle values, FacebookException error) {
				if (error == null) {
					final String postId = values.getString("post_id");
					if (postId != null) {
						mContext.showErrorConfirmDialog(R.string.share_complete);
					} else {
						// mContext.showErrorConfirmDialog(R.string.share_cancel);
					}
				} else {
					// mContext.showErrorConfirmDialog(TextUtils.isEmpty(error.getMessage())
					// ? mContext.getString(R.string.share_cancel) :
					// error.getMessage());
				}
			}
		}).build();
		dialog.show();
	}

	private Session.StatusCallback mSessionStatusCallback = new Session.StatusCallback() {

		@Override
		public void call(Session session, SessionState state, Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	protected void onSessionStateChange(Session session, SessionState state, Exception exception) {
		LogUtil.e("go into facebook status call back");
		if (exception != null) {
			mContext.showErrorConfirmDialog(exception.getMessage());
			return;
		}
		if (state == SessionState.OPENED)
			performAction();
	}

	private void performAction() {
		FacebookAction lastAction = mAction;
		mAction = FacebookAction.NONE;
		switch (lastAction) {
		case AUTHORIZE:
			sendJoinMessage();
			mAuthListener.onAuthorizeSuccess();
			break;
		case GET_INFO:
			getFacebookInfo();
			break;
		default:
			break;
		}
	}

	private void getFacebookInfo() {
		Request.executeMeRequestAsync(Session.getActiveSession(), new GraphUserCallback() {

			@Override
			public void onCompleted(GraphUser user, Response response) {
				if (user != null && !isGetFacebookInfoError) {
					mUser = ThirdPartUtils.parserFacebookUserToThirdPartUser(user);
					hasUserInfoLoaded = true;
					if (hasFriendsLoaded)
						onFacebookInfosLoaded(mUser, mFriends);
				} else {
					isGetFacebookInfoError = true;
					onFacebookInfoLoadFail();
				}
			}
		});
		Request.executeMyFriendsRequestAsync(Session.getActiveSession(), new GraphUserListCallback() {

			@Override
			public void onCompleted(List<GraphUser> users, Response response) {
				if (users != null && !isGetFacebookInfoError) {
					mFriends = ThirdPartUtils.parserFacebookUserToThirdPartUser(users);
					hasFriendsLoaded = true;
					if (hasUserInfoLoaded)
						onFacebookInfosLoaded(mUser, mFriends);
				} else {
					isGetFacebookInfoError = true;
					onFacebookInfoLoadFail();
				}
			}
		});
	}

	public void authorize(OnAuthorizeSuccessListener listener) {
		this.mAuthListener = listener;
		mAction = FacebookAction.AUTHORIZE;
		openSession();
	}

	public void getFacebookInfo(OnGetThirdPartInfoSuccessListener listener) {
		this.mGetInfoSuccessListener = listener;
		mAction = FacebookAction.GET_INFO;
		openSession();
	}

	private void openSession() {
		Session session = Session.getActiveSession();
		if (session != null && (!session.isOpened() && !session.isClosed())) {
			session.openForRead(new Session.OpenRequest(mContext).setCallback(mSessionStatusCallback));
		} else {
			Session.openActiveSession(mContext, true, mSessionStatusCallback);
		}
	}

	private void onFacebookInfosLoaded(ThirdPartUser user, List<ThirdPartUser> friends) {
		mGetInfoSuccessListener.onGetInfoSuccess(user, friends);
	}

	private void onFacebookInfoLoadFail() {
		mGetInfoSuccessListener.onGetFail();
	}

	public void deAuthorize(OnDeAuthorizeListener listener) {
		this.mDeAuthorizeListener = listener;
		mContext.showLoadingDialog(BaseActivity.LOADING_NO_MSG, BaseActivity.LOADING_NO_MSG, false);
		Request request = new Request(Session.getActiveSession(), "me/permissions");
		request.setHttpMethod(HttpMethod.DELETE);
		request.setCallback(new Callback() {

			@Override
			public void onCompleted(Response response) {
				try {
					if (response.getConnection().getResponseCode() == 200 && response.getError() == null) {
						FacebookUtil.clearFacebookVlidated(mContext);
						PrefsUtils.User.ThirdPart.clearFacebookAccount(mContext, mContext.getCurrentUser().getRcId());
						mHandler.sendEmptyMessage(MSG_DEAUTHORIZE_SUCCESS);
						Toast.makeText(mContext, mContext.getResources().getString(R.string.save_success), Toast.LENGTH_SHORT).show();
					} else {
						if (response.getConnection().getResponseCode() != 200) {
							mHandler.sendEmptyMessage(MSG_NET_ERROR);
						} else if (response.getError() != null) {
							Message msg = mHandler.obtainMessage();
							msg.what = MSG_DEAUTHORIZE_ERROR;
							msg.obj = response.getError().getErrorMessage();
							mHandler.sendMessage(msg);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					mHandler.sendEmptyMessage(MSG_NET_ERROR);
				}
			}
		});
		request.executeAsync();
	}

	private Handler mHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			mContext.dismissLoadingDialog();
			switch (msg.what) {
			case MSG_DEAUTHORIZE_SUCCESS:
				mDeAuthorizeListener.onDeAuthorizeSuccess();
				break;
			case MSG_DEAUTHORIZE_ERROR:
				mDeAuthorizeListener.onDeAuthorizeFail();
				String message = (String) msg.obj;
				mContext.showErrorConfirmDialog(message);
				break;
			case MSG_NET_ERROR:
				mDeAuthorizeListener.onDeAuthorizeFail();
				mContext.showErrorConfirmDialog(R.string.net_error);
				break;
			}
		};
	};

	public static interface OnInviteSuccessListener {

		public void onInviteSuccess(String uid);
	}

	public boolean isAuthorize() {
		return ThirdPartUtils.isFacebookVlidate(mContext);
	}
}
