package com.rcplatform.phototalk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.perm.kate.api.Api;
import com.perm.kate.api.User;
import com.rcplatform.phototalk.activity.AddFriendBaseActivity;
import com.rcplatform.phototalk.adapter.PhotoTalkFriendsAdapter;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.FriendType;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.logic.LogicUtils;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.request.Request;
import com.rcplatform.phototalk.request.inf.OnFriendsLoadedListener;
import com.rcplatform.phototalk.task.ThirdPartInfoUploadTask;
import com.rcplatform.phototalk.thirdpart.bean.ThirdPartUser;
import com.rcplatform.phototalk.thirdpart.utils.ThirdPartUtils;
import com.rcplatform.phototalk.utils.Constants.Action;
import com.rcplatform.phototalk.utils.PrefsUtils;

public class VKRecommendActivity extends AddFriendBaseActivity {

	private static final int REQUEST_CODE_LOGIN = 1;
	private static final int MSG_WHAT_VK_LOADED_FAIL = 100;
	private static final int MSG_WHAT_INVITE_SUCCESS = 101;
	private static final int MSG_WHAT_INVITE_FAIL = 102;

	private View loginLayout;
	private View recommendsLayout;
	private Button btnLogin;

	private Api api;
	private long userId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vk_recommends);
		setItemType(PhotoTalkFriendsAdapter.TYPE_VK);
		initView();
	}

	private void initView() {
		initAddFriendsView();
		loginLayout = findViewById(R.id.login_layout);
		recommendsLayout = findViewById(R.id.friends_layout);
		btnLogin = (Button) findViewById(R.id.login_button);
		btnLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(VKRecommendActivity.this, VKAuthorizeActivity.class);
				startActivityForResult(intent, REQUEST_CODE_LOGIN);
			}
		});
		Object[] vkAccount = PrefsUtils.User.ThirdPart.getVKAccount(this, getCurrentUser().getRcId());
		if (vkAccount != null) {
			api = new Api((String) vkAccount[0], com.rcplatform.phototalk.utils.Constants.VK_API_ID);
			userId = (Long) vkAccount[1];
			showRecommendsLayout();
			getVkRecommends();
		} else {
			showLoginLayout();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_LOGIN) {
			if (resultCode == RESULT_OK) {
				// авторизовались успешно
				showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
				String access_token = data.getStringExtra("token");
				userId = data.getLongExtra("user_id", 0);
				PrefsUtils.User.ThirdPart.saveVKAccount(this, getCurrentUser().getRcId(), access_token, userId);
				api = new Api(access_token, com.rcplatform.phototalk.utils.Constants.VK_API_ID);
				sendJoinMessage();
				showRecommendsLayout();
				getVkInfos();
			}
		}
	}

	private void getVkInfos() {
		Thread thread = new Thread() {
			public void run() {
				try {
					ArrayList<User> users = api.getFriends(userId, null, null, null, null);
					List<ThirdPartUser> vkFriends = ThirdPartUtils.parserVKUserToThirdPartUser(users);
					PhotoTalkDatabaseFactory.getDatabase().saveThirdPartFriends(vkFriends, FriendType.VK);
					List<User> userProfiles = api.getProfiles(Arrays.asList(userId), null, null, null, null, null);
					ThirdPartUser user = ThirdPartUtils.parserVKUserToThirdPartUser(userProfiles.get(0));
					PrefsUtils.User.ThirdPart.refreshVKSyncTime(getApplicationContext(), getCurrentUser().getRcId());
					uploadVKInfos(user, vkFriends);
				} catch (Exception e) {
					e.printStackTrace();
					vkHandler.sendEmptyMessage(MSG_WHAT_VK_LOADED_FAIL);
				}
			};
		};
		thread.start();
	}

	private void uploadVKInfos(ThirdPartUser vkUser, final List<ThirdPartUser> vkFriends) {
		new ThirdPartInfoUploadTask(VKRecommendActivity.this, vkFriends, vkUser, FriendType.VK, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				getVkRecommends();
			}

			@Override
			public void onFailure(int errorCode, String content) {
				dismissLoadingDialog();
				onRecommendsLoaded(new ArrayList<Friend>(), ThirdPartUtils.parserToFriends(vkFriends, FriendType.VK));
			}
		}).start();
	}

	private void getVkRecommends() {
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		Request.executeGetRecommends(this, FriendType.VK, new OnFriendsLoadedListener() {

			@Override
			public void onServiceFriendsLoaded(List<Friend> friends, List<Friend> recommends) {
				if (mList.getExpandableListAdapter() != null && mList.getExpandableListAdapter().getGroupCount() > 0)
					return;
				dismissLoadingDialog();
				onRecommendsLoaded(recommends, friends);
			}

			@Override
			public void onLocalFriendsLoaded(List<Friend> friends, List<Friend> recommends) {
				if (friends.size() > 0 || recommends.size() > 0) {
					dismissLoadingDialog();
					onRecommendsLoaded(recommends, friends);
				}
			}

			@Override
			public void onError(int errorCode, String content) {
				dismissLoadingDialog();
			}
		});
	}

	private void onRecommendsLoaded(List<Friend> recommends, List<Friend> inviteFriends) {
		this.recommendFriends = recommends;
		this.inviteFriends = inviteFriends;
		setListData(recommendFriends, inviteFriends, mList);
	}

	private void showRecommendsLayout() {
		loginLayout.setVisibility(View.GONE);
		recommendsLayout.setVisibility(View.VISIBLE);
	}

	private void showLoginLayout() {
		loginLayout.setVisibility(View.VISIBLE);
		recommendsLayout.setVisibility(View.GONE);
	}

	private Handler vkHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			dismissLoadingDialog();
			if (msg.what == MSG_WHAT_VK_LOADED_FAIL)
				showErrorConfirmDialog(R.string.vk_load_fail);
			else if (msg.what == MSG_WHAT_INVITE_SUCCESS)
				showErrorConfirmDialog(R.string.invite_success);
			else if (msg.what == MSG_WHAT_INVITE_FAIL)
				showErrorConfirmDialog(R.string.invite_fail);
		};
	};

	protected void onInviteButtonClick(final java.util.Set<Friend> willInvateFriends) {
		if (willInvateFriends != null && willInvateFriends.size() > 0) {
			Thread thread = new Thread() {
				public void run() {

					for (Friend f : willInvateFriends) {
						try {
							api.sendMessage(Long.parseLong(f.getRcId()), 0, getString(R.string.my_firend_invite_send_short_msg),
									getString(R.string.invate_friend), null, null, null, null, null, null, null);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
			};
			thread.start();
			showErrorConfirmDialog(R.string.invite_success);
			uploadInviteInfo(willInvateFriends);
		}
	};

	private void uploadInviteInfo(java.util.Set<Friend> willInvateFriends) {
		String[] ids = new String[willInvateFriends.size()];
		Iterator<Friend> itFs = willInvateFriends.iterator();
		int i = 0;
		while (itFs.hasNext()) {
			ids[i] = itFs.next().getRcId();
			i++;
		}
		LogicUtils.uploadFriendInvite(this, Action.ACTION_UPLOAD_INTITE_THIRDPART, FriendType.VK, ids);
	}

	private void sendJoinMessage() {
		Thread thread = new Thread() {
			public void run() {
				try {
					api.sendMessage(userId, 0, getString(R.string.third_part_join_content), getString(R.string.third_part_join_title), null, null, null, null,
							null, null, null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		};
		thread.start();
	};
}
