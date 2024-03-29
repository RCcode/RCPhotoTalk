package com.rcplatform.phototalk.activity;

import java.util.List;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.facebook.FacebookException;
import com.facebook.HttpMethod;
import com.facebook.Request;
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
import com.rcplatform.phototalk.galhttprequest.LogUtil;
import com.rcplatform.phototalk.thirdpart.bean.ThirdPartFriend;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.FacebookUtil;

public class FacebookActivity extends AddFriendBaseActivity {
	private static final int MSG_DEAUTHORIZE_SUCCESS = 100;
	private static final int MSG_DEAUTHORIZE_ERROR = 101;
	private static final int MSG_NET_ERROR = 102;

	private boolean hasUserInfoLoaed = false;
	private boolean hasFriendsLoaded = false;
	private boolean hasErrored = false;

	private UiLifecycleHelper mHelper;
	private GraphUser mUser;
	private List<ThirdPartFriend> mFriends;
	private PendingAction mAction = PendingAction.NONE;
	private String[] mInviteIds;
	private boolean hasTryLogin = false;

	private enum PendingAction {
		NONE, SEND_INVATE, GET_INFO, DE_AUTHORIZE;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mHelper = new UiLifecycleHelper(this, mCallback);
		mHelper.onCreate(savedInstanceState);
	}

	private void performAction() {
		PendingAction lastAction = mAction;
		mAction = PendingAction.NONE;
		switch (lastAction) {
		case SEND_INVATE:
			sendInviteToFriend();
			break;
		case GET_INFO:
			getFacebookInfos();
			break;
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		hasTryLogin = true;
		mHelper.onActivityResult(requestCode, resultCode, data);

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mHelper.onPause();
		hasTryLogin = false;
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mHelper.onResume();
		if (!hasTryLogin) {
			if (FacebookUtil.isFacebookVlidate(this))
				mAction = PendingAction.NONE;
			else
				mAction = PendingAction.GET_INFO;
			Session session = Session.getActiveSession();
			if (session != null && (!session.isOpened() && !session.isClosed())) {
				session.openForRead(new Session.OpenRequest(this).setPermissions(null).setCallback(mCallback));
			} else {
				Session.openActiveSession(this, true, mCallback);
			}
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mHelper.onDestroy();
		Session.getActiveSession().removeCallback(mCallback);
	}

	protected void onSessionStateChange(Session session, SessionState state, Exception exception) {
		LogUtil.e("go into facebook status call back");
		if (exception != null && mAction != PendingAction.NONE) {
			DialogUtil.createMsgDialog(this, exception.getMessage(), getString(R.string.confirm)).show();
			mAction = PendingAction.NONE;
			return;
		}
		if (session.isOpened()) {
			if (mAction != PendingAction.NONE) {
				performAction();
			}
		}
	}

	private Session.StatusCallback mCallback = new Session.StatusCallback() {

		@Override
		public void call(Session session, SessionState state, Exception exception) {
			// TODO Auto-generated method stub
			onSessionStateChange(session, state, exception);
		}
	};

	protected void onFacebookInfoLoaded(GraphUser user, List<ThirdPartFriend> friends) {

	}

	protected void onGetFacebookInfoError() {
		// TODO Auto-generated method stub

	}

	private void facebookRequestError() {
		if (!hasErrored)
			hasErrored = true;
		else
			return;
		onGetFacebookInfoError();
	}

	private void getFacebookInfos() {
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		Request.executeMeRequestAsync(Session.getActiveSession(), new GraphUserCallback() {

			@Override
			public void onCompleted(GraphUser user, Response response) {
				// TODO Auto-generated method stub\
				if (user == null) {
					facebookRequestError();
					return;
				}
				hasUserInfoLoaed = true;
				mUser = user;
				if (hasFriendsLoaded) {
					onFacebookInfoLoaded(mUser, mFriends);
				}
			}
		});
		Request request = Request.newMyFriendsRequest(Session.getActiveSession(), new GraphUserListCallback() {

			@Override
			public void onCompleted(List<GraphUser> users, Response response) {
				// TODO Auto-generated method
				if (users == null) {
					facebookRequestError();
					return;
				}
				mFriends = FacebookUtil.buildFriends(users);
				hasFriendsLoaded = true;
				if (hasUserInfoLoaed) {
					onFacebookInfoLoaded(mUser, mFriends);
				}
			}
		});
		request.executeAsync();
	}

	protected void sendInviteMessage(String... ids) {
		mAction = PendingAction.SEND_INVATE;
		this.mInviteIds = ids;
		performAction();
	}

	private void sendInviteToFriend() {
		StringBuilder sbIds = new StringBuilder();
		for (String id : mInviteIds) {
			sbIds.append(id).append(",");
		}
		WebDialog dialog = new WebDialog.RequestsDialogBuilder(this, Session.getActiveSession()).setMessage(getString(R.string.my_firend_invite_send_short_msg)).setTo(sbIds.substring(0, sbIds.length() - 1)).setOnCompleteListener(new OnCompleteListener() {
			@Override
			public void onComplete(Bundle values, FacebookException error) {
				// TODO Auto-generated method stub
				if (error != null) {
					showErrorConfirmDialog(error.getMessage());
				} else if (values != null && values.size() == 0) {
					showErrorConfirmDialog(R.string.invite_fail);
				} else {
					onInviteComplete(mInviteIds);
					mInviteIds=null;
				}
			}
		}).build();
		dialog.show();
	}

	protected void onInviteComplete(String...ids) {
		// TODO Auto-generated method stub
		
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			dismissLoadingDialog();
			switch (msg.what) {
			case MSG_DEAUTHORIZE_SUCCESS:
				Dialog dialog = DialogUtil.createMsgDialog(FacebookActivity.this, getString(R.string.deauthorize_complete), getString(R.string.confirm));
				dialog.setOnDismissListener(new OnDismissListener() {

					@Override
					public void onDismiss(DialogInterface dialog) {
						// TODO Auto-generated method stub
						finish();
					}
				});
				dialog.show();
				break;
			case MSG_DEAUTHORIZE_ERROR:
				String message = (String) msg.obj;
				DialogUtil.createMsgDialog(FacebookActivity.this, message, getString(R.string.confirm)).show();
				break;
			case MSG_NET_ERROR:
				DialogUtil.createMsgDialog(FacebookActivity.this, getString(R.string.net_error), getString(R.string.confirm)).show();
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
					// TODO Auto-generated catch block
					e.printStackTrace();
					mHandler.sendEmptyMessage(MSG_NET_ERROR);
				}
			};
		};
		th.start();

	}
}
