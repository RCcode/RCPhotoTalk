package com.rcplatform.phototalk.thirdpart.utils;

import java.util.ArrayList;
import java.util.List;

import twitter4j.PagableResponseList;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.activity.ActivityFunction;
import com.rcplatform.phototalk.bean.FriendType;
import com.rcplatform.phototalk.thirdpart.activity.TwitterAuthorizeActivity;
import com.rcplatform.phototalk.thirdpart.bean.ThirdPartUser;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.PrefsUtils;

public class TwitterClient extends ThirdPartClient {
	private static final int REQUEST_CODE_AUTHORIZE = 10000;

	private static final int MSG_WHAT_AUTHORIZE_FAIL = 100;
	private static final int MSG_WHAT_AUTHORIZE_WEB = 101;
	private static final int MSG_WHAT_AUTHORIZE_SUCCESS = 102;
	private static final int MSG_WHAT_GETINFO_SUCCESS = 103;
	private static final int MSG_WHAT_GETINFO_FAIL = 104;
	private static final int MSG_WHAT_JOIN_SUCCESS = 105;
	private static final int MSG_WHAT_JOIN_FAIL = 106;

	private Twitter mTwitter;
	private RequestToken mRequestToken;
	private OnAuthorizeSuccessListener mAuthorizeSuccessListener;
	private OnGetThirdPartInfoSuccessListener mGetThirdPartInfoSuccessListener;

	private String mRcId;
	private int friendPage = -1;
	private static final int PAGE_SIZE = 20;
	private ThirdPartUser mTwitterUser;;

	public TwitterClient(ActivityFunction context) {
		super(context);
		init();
	}

	private void init() {
		mTwitter = new TwitterFactory().getInstance();
		mTwitter.setOAuthConsumer(Constants.TWITTER_APP_KEY, Constants.TWITTER_APP_SECRET);
		mRcId = mContext.getCurrentUser().getRcId();
		AccessToken token = PrefsUtils.User.ThirdPart.getTwitterAccessToken(mContext.getContext(), mRcId);
		if (token != null)
			mTwitter.setOAuthAccessToken(token);
	}

	public void authorize(OnAuthorizeSuccessListener listener) {
		this.mAuthorizeSuccessListener = listener;
		mContext.showLoadingDialog(false);
		Thread thread = new Thread() {

			public void run() {
				try {
					mRequestToken = mTwitter.getOAuthRequestToken();
					Message msg = twitterHandler.obtainMessage();
					msg.what = MSG_WHAT_AUTHORIZE_WEB;
					msg.obj = mRequestToken.getAuthorizationURL() + "&force_login=true";
					twitterHandler.sendMessage(msg);
				} catch (TwitterException e) {
					e.printStackTrace();
					twitterHandler.sendEmptyMessage(MSG_WHAT_AUTHORIZE_FAIL);
				}
			};
		};
		thread.start();
	}

	public void sendJoinMessage() {
		mContext.showLoadingDialog(false);
		Thread thread = new Thread() {
			public void run() {
				try {
					StatusUpdate state = new StatusUpdate(mContext.getContext().getString(R.string.join_message, mContext.getCurrentUser().getRcId()));
					Status status = mTwitter.updateStatus(state);
					Message msg = twitterHandler.obtainMessage();
					msg.what = MSG_WHAT_JOIN_SUCCESS;
					msg.obj = status.getText();
					twitterHandler.sendMessage(msg);
				} catch (TwitterException e) {
					e.printStackTrace();
					twitterHandler.sendEmptyMessage(MSG_WHAT_JOIN_FAIL);
				}

			};
		};
		thread.start();
	}

	public void setInviteMessage() {

	}

	public void deAuthorize(OnDeAuthorizeListener listener) {
		mTwitter.shutdown();
		PrefsUtils.User.ThirdPart.clearTwitterAccount(mContext.getContext(), mContext.getCurrentUser().getRcId());
		listener.onDeAuthorizeSuccess();
		init();
	}

	public void getThirdPartInfo(OnGetThirdPartInfoSuccessListener listener) {
		mContext.showLoadingDialog(false);
		mGetThirdPartInfoSuccessListener = listener;
		Thread thread = new Thread() {
			public void run() {
				try {
					int length = 0;
					mTwitterUser = new ThirdPartUser();
					mTwitterUser.setNick(mTwitter.getScreenName());
					mTwitterUser.setId(mTwitter.getId() + "");
					mTwitterUser.setType(FriendType.TWITTER);
					List<ThirdPartUser> friends = new ArrayList<ThirdPartUser>();
					while (length == PAGE_SIZE) {
						PagableResponseList<User> friendList = mTwitter.getFriendsList(mTwitter.getId(), friendPage++);
						length = friendList.size();
						friends.addAll(ThirdPartUtils.parserTwitterUsersToThirdPartUser(friendList));
					}
					Message msg = twitterHandler.obtainMessage();
					msg.what = MSG_WHAT_GETINFO_SUCCESS;
					msg.obj = friends;
					twitterHandler.sendMessage(msg);
				} catch (Exception e) {
					e.printStackTrace();
					twitterHandler.sendEmptyMessage(MSG_WHAT_GETINFO_FAIL);
				}
			};
		};
		thread.start();
	}

	public void onAuthorizeInformationReceived(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_AUTHORIZE) {
			if (resultCode == Activity.RESULT_OK) {
				final String verifier = data.getStringExtra(TwitterAuthorizeActivity.RESULT_KEY_VERIFIER);
				Thread thread = new Thread() {
					@Override
					public void run() {
						try {
							AccessToken accessToken = mTwitter.getOAuthAccessToken(mRequestToken, verifier);
							PrefsUtils.User.ThirdPart.saveTwitterAccessToken(mContext.getContext(), mRcId, accessToken);
							twitterHandler.sendEmptyMessage(MSG_WHAT_AUTHORIZE_SUCCESS);
						} catch (TwitterException e) {
							e.printStackTrace();
							twitterHandler.sendEmptyMessage(MSG_WHAT_AUTHORIZE_FAIL);
						}
					}
				};
				thread.start();
			} else {
				mContext.dissmissLoadingDialog();
				mTwitter.shutdown();
				init();
			}
		}
	}

	public boolean isAuthorized() {
		return ThirdPartUtils.isTwitterVlidated(mContext.getContext(), mRcId);
	}

	private Handler twitterHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_WHAT_AUTHORIZE_FAIL:
				mContext.dissmissLoadingDialog();
				mContext.showConfirmDialog(R.string.net_error);
				break;
			case MSG_WHAT_AUTHORIZE_WEB:
				startTwitterAuthorizeActivity((String) msg.obj);
				break;
			case MSG_WHAT_AUTHORIZE_SUCCESS:
				mContext.dissmissLoadingDialog();
				mAuthorizeSuccessListener.onAuthorizeSuccess();
				break;
			case MSG_WHAT_GETINFO_SUCCESS:
				mGetThirdPartInfoSuccessListener.onGetInfoSuccess(mTwitterUser, (List<ThirdPartUser>) msg.obj);
				break;
			case MSG_WHAT_JOIN_SUCCESS:
				mContext.dissmissLoadingDialog();
				mContext.showConfirmDialog(R.string.share_complete);
				break;
			case MSG_WHAT_JOIN_FAIL:
				mContext.dissmissLoadingDialog();
				mContext.showConfirmDialog(R.string.net_error);
				break;
			}
		}

	};

	private void startTwitterAuthorizeActivity(String uri) {
		Intent intent = new Intent(mContext.getContext(), TwitterAuthorizeActivity.class);
		intent.setData(Uri.parse(uri));
		mContext.getActivity().startActivityForResult(intent, REQUEST_CODE_AUTHORIZE);
	}

	@Override
	protected void doSendJoinMessage(final String msg) {
		mContext.showLoadingDialog(false);
		Thread thread = new Thread() {
			public void run() {
				try {
					StatusUpdate state = new StatusUpdate(msg);
					Status status = mTwitter.updateStatus(state);
					Message msg = twitterHandler.obtainMessage();
					msg.what = MSG_WHAT_JOIN_SUCCESS;
					msg.obj = status.getText();
					twitterHandler.sendMessage(msg);
				} catch (TwitterException e) {
					e.printStackTrace();
					twitterHandler.sendEmptyMessage(MSG_WHAT_JOIN_FAIL);
				}

			};
		};
		thread.start();
	}

	@Override
	public void destroy() {
		mTwitter.shutdown();
	};
}
