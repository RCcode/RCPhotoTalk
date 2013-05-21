package com.rcplatform.phototalk.activity;

import java.util.List;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.facebook.FacebookException;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Request.GraphUserListCallback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton.UserInfoChangedCallback;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.galhttprequest.LogUtil;
import com.rcplatform.phototalk.thirdpart.bean.ThirdPartUser;
import com.rcplatform.phototalk.thirdpart.utils.ThirdPartUtils;
import com.rcplatform.phototalk.utils.DialogUtil;

public class FacebookAddFriendsActivity extends AddFriendBaseActivity implements UserInfoChangedCallback {

	private static final int MSG_DEAUTHORIZE_SUCCESS = 100;
	private static final int MSG_DEAUTHORIZE_ERROR = 101;
	private static final int MSG_NET_ERROR = 102;

	private UiLifecycleHelper mHelper;
	private String[] mInviteIds;

	private boolean getFacebookInfoOver = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHelper = new UiLifecycleHelper(this, mCallback);
		mHelper.onCreate(savedInstanceState);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mHelper.onActivityResult(requestCode, resultCode, data);

	}

	@Override
	protected void onPause() {
		super.onPause();
		mHelper.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();

	}

	@Override
	protected void onResume() {
		super.onResume();
		mHelper.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mHelper.onDestroy();
		Session.getActiveSession().removeCallback(mCallback);
	}

	protected void onSessionStateChange(Session session, SessionState state, Exception exception) {
		LogUtil.e("go into facebook status call back");
		if (exception != null) {
			DialogUtil.createMsgDialog(this, exception.getMessage(), getString(R.string.confirm)).show();
			return;
		}
	}

	private Session.StatusCallback mCallback = new Session.StatusCallback() {

		@Override
		public void call(Session session, SessionState state, Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	protected void onFacebookInfoLoaded(GraphUser user, List<ThirdPartUser> friends) {
		getFacebookInfoOver = true;
	}

	protected void onGetFacebookInfoError() {
		getFacebookInfoOver = true;
	}

	private void facebookRequestError() {
		onGetFacebookInfoError();
	}

	protected void sendInviteMessage(String... ids) {
		this.mInviteIds = ids;
		sendInviteToFriend();
	}

	private void sendInviteToFriend() {
		StringBuilder sbIds = new StringBuilder();
		for (String id : mInviteIds) {
			sbIds.append(id).append(",");
		}
		Bundle bundler = new Bundle();
		bundler.putString("link", "http://www.google.co.jp");
		bundler.putString("caption", "{*actor*} just posted this!");
		bundler.putString("description", "description of my link.  Click the link to find out more.");
		bundler.putString("name", "Name of this link!");
		bundler.putString("picture", "http://a3.att.hudong.com/16/10/19300001361107132082103527825.jpg");
		bundler.putString("to", sbIds.substring(0, sbIds.length() - 1));
		WebDialog dialog = new WebDialog.FeedDialogBuilder(this, Session.getActiveSession(), bundler).setOnCompleteListener(new OnCompleteListener() {

			@Override
			public void onComplete(Bundle values, FacebookException error) {
				if (error == null) {
					final String postId = values.getString("post_id");
					if (postId != null) {
						onInviteComplete(mInviteIds);
					}
				} else {
					showErrorConfirmDialog(TextUtils.isEmpty(error.getMessage()) ? getString(R.string.invite_fail) : error.getMessage());
				}
			}
		}).build();
		dialog.show();
	}

	protected void onInviteComplete(String... ids) {

	}

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			dismissLoadingDialog();
			switch (msg.what) {
			case MSG_DEAUTHORIZE_SUCCESS:
				Dialog dialog = DialogUtil.createMsgDialog(FacebookAddFriendsActivity.this, getString(R.string.deauthorize_complete),
						getString(R.string.confirm));
				dialog.setOnDismissListener(new OnDismissListener() {

					@Override
					public void onDismiss(DialogInterface dialog) {
						finish();
					}
				});
				dialog.show();
				break;
			case MSG_DEAUTHORIZE_ERROR:
				String message = (String) msg.obj;
				DialogUtil.createMsgDialog(FacebookAddFriendsActivity.this, message, getString(R.string.confirm)).show();
				break;
			case MSG_NET_ERROR:
				DialogUtil.createMsgDialog(FacebookAddFriendsActivity.this, getString(R.string.net_error), getString(R.string.confirm)).show();
				break;
			}
		};
	};

	protected void deAuthorize() {
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		Thread th = new Thread() {
			public void run() {
				try {
					Request request = new Request(Session.getActiveSession(), "me/permissions");
					request.setHttpMethod(HttpMethod.DELETE);
					Response response = request.executeAndWait();
					if (response.getConnection().getResponseCode() == 200 && response.getError() == null) {
						mHandler.sendEmptyMessage(MSG_DEAUTHORIZE_SUCCESS);
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
			};
		};
		th.start();
	}

	private void sendJoinMessageOnFacebook(String uid) {
		Bundle bundler = new Bundle();
		bundler.putString("link", "http://www.google.co.jp");
		bundler.putString("caption", "{*actor*} just posted this!");
		bundler.putString("description", "description of my link.  Click the link to find out more.");
		bundler.putString("name", "Name of this link!");
		bundler.putString("picture", "http://a3.att.hudong.com/16/10/19300001361107132082103527825.jpg");
		bundler.putString("to", uid);
		WebDialog dialog = new WebDialog.FeedDialogBuilder(this, Session.getActiveSession(), bundler).setOnCompleteListener(new OnCompleteListener() {

			@Override
			public void onComplete(Bundle values, FacebookException error) {
				if (error == null) {
					final String postId = values.getString("post_id");
					if (postId != null) {
						showErrorConfirmDialog(R.string.share_complete);
					} else {
						showErrorConfirmDialog(R.string.share_cancel);
					}
				} else {
					showErrorConfirmDialog(TextUtils.isEmpty(error.getMessage()) ? getString(R.string.share_cancel) : error.getMessage());
				}
				if (!getFacebookInfoOver)
					showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
			}
		}).build();
		dialog.show();
	}

	@Override
	public void onUserInfoFetched(final GraphUser user) {
		if (user != null) {
			sendJoinMessageOnFacebook(user.getId());
			getFacebookFriends(user);
		}
	}

	private void getFacebookFriends(final GraphUser user) {
		Request request = Request.newMyFriendsRequest(Session.getActiveSession(), new GraphUserListCallback() {

			@Override
			public void onCompleted(List<GraphUser> users, Response response) {
				if (users == null) {
					facebookRequestError();
					return;
				}
				onFacebookInfoLoaded(user, ThirdPartUtils.parserFacebookUserToThirdPartUser(users));
			}
		});
		request.executeAsync();
	}

	protected void postToWall(String userID) {
	}
}
