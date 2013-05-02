package com.rcplatform.phototalk;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.rcplatform.phototalk.AddFriendAdapter.OnFriendPortraitListener;
import com.rcplatform.phototalk.adapter.InviteFriendsListAdapter;
import com.rcplatform.phototalk.api.MenueApiFactory;
import com.rcplatform.phototalk.api.MenueApiUrl;
import com.rcplatform.phototalk.bean.AppBean;
import com.rcplatform.phototalk.bean.DetailFriend;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.galhttprequest.GalHttpRequest;
import com.rcplatform.phototalk.galhttprequest.GalHttpRequest.GalHttpLoadTextCallBack;
import com.rcplatform.phototalk.utils.Contract;
import com.rcplatform.phototalk.utils.PhoneBookUtils;
import com.rcplatform.phototalk.utils.PrefsUtils;
import com.rcplatform.phototalk.utils.ShowToast;
import com.rcplatform.phototalk.views.HorizontalListView;
import com.rcplatform.phototalk.views.PVPopupWindow;

/**
 * 推荐好友列表 <br>
 * 用于查阅当前电话本中正在使用PVchat的好友。
 * <p>
 * Copyright: Menue,Inc Copyright (c) 2013-3-4 上午11:37:00
 * <p>
 * Team:Menue Beijing
 * <p>
 * 
 * @author jelly.xiong@menue.com.cn
 * @version 1.0.0
 */
public class PickFriendActivity extends Activity implements View.OnClickListener {

	private static final String TAG = "PickFriendActivity";

	private static final int QUERY_TOKEN = 0;

	private static final int QUERY_EMAIL_TOKEN = 1;

	private static final int QUERY_PHOTO_TOKEN = 2;

	private ProgressBar mProgressbar;

	private ListView mListView;

	private View mBottomLayout;

	@Deprecated
	private TextView mSelectedView;

	private HorizontalListView mBottomHScrollListView;

	private Gallery mGallery;

	private int mDisplayableCount;

	private Button mInviteBtn;

	private TextView mTitleTextView;

	private View mBack;

	private TextView mChooseTextView;

	private View mAddFriendLabel;

	private QueryHandler mQueryHandler;

	final String[] COLUMNS = new String[] { Contacts._ID, Data.DATA1, Data.DISPLAY_NAME, Data.PHOTO_ID };

	private Context mContext;

	private List<Friend> friends;

	private List<Friend> inviteFriends;

	private ArrayList<Friend> invitedLists = new ArrayList<Friend>();

	private HashMap<String, Friend> hashMap = new HashMap<String, Friend>();

	private BottomHScrollBarAdapter mBHAdapter;

	private View mShowView;

	private DetailFriend detailFriend;

	protected static final int RESPONSE_FRIEND_DETAIL_SUCCESS = 10001;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				// 返回
				case MenueApiFactory.RESPONSE_STATE_SUCCESS:
				case MenueApiFactory.RESPONSE_STATE_SUCCESS_NO_FRIEND:
					mProgressbar.setVisibility(View.GONE);
					// friends.clear();
					//

					// 如果通讯录里已经有人使用此产品则显示此页，如没有，则直接进入邀请界面。
					if (friends != null && friends.size() > 0) {
						mTitleTextView.setText(getResources().getText(R.string.firend_list_add_friend_title));
						mAddFriendLabel.setVisibility(View.VISIBLE);
						mChooseTextView.setVisibility(View.VISIBLE);
						mBottomLayout.setVisibility(View.GONE);
						AddFriendAdapter addFriendAdapter = new AddFriendAdapter(getApplicationContext(), friends);
						addFriendAdapter.setOnFriendAddListener(new AddFriendAdapter.OnFriendAddListener() {

							@Override
							public void addFriend(Friend friend, Handler handler) {
								// 添加好友。
								doFriendAdd(friend, handler);
							}

						});
						addFriendAdapter.setOnFriendPortraitListener(new OnFriendPortraitListener() {

							@Override
							public void showFriendDetail(View v, Friend friend) {
								mShowView = v;
								doFriendDetailShow(friend);
							}

						});
						mListView.setAdapter(addFriendAdapter);
					} else {
						// 无推荐好友，显示邀请页面。
						mBack.setVisibility(View.VISIBLE);
						mTitleTextView.setText(getResources().getText(R.string.firend_list_invite_friend_title));
						mAddFriendLabel.setVisibility(View.GONE);
						mBottomLayout.setVisibility(View.VISIBLE);
						// InviteFriendAdapter inviteFriendAdapter = new InviteFriendAdapter(getApplicationContext(),
						// inviteFriends);
						InviteFriendsListAdapter inviteFriendsListAdapter = new InviteFriendsListAdapter(getApplicationContext(), inviteFriends);
						inviteFriendsListAdapter.setShowNumber(true);
						mListView.setAdapter(inviteFriendsListAdapter);

						inviteFriendsListAdapter.setOnCheckBoxChangedListener(new InviteFriendsListAdapter.OnCheckBoxChangedListener() {

							@Override
							public void onChange(Friend friend, boolean isChecked) {
								if (isChecked) {
									if (!invitedLists.contains(friend))
										invitedLists.add(friend);
								} else {
									invitedLists.remove(friend);
								}
								mBHAdapter.notifyDataSetChanged();
								// ((SelectedFriendsGalleryAdapter) mGallery.getAdapter()).notifyDataSetChanged();
								// if (inviteLists.size() > mDisplayableCount)
								// mGallery.setSelection(inviteLists.size() - mDisplayableCount);
								// else {
								// mGallery.setSelection(0);
								// }
							}

						});
						// startActivity(new Intent(mContext, InviteFriendActivity.class));
					}
					mBHAdapter = new BottomHScrollBarAdapter(getApplicationContext(), invitedLists);
					mBottomHScrollListView.setAdapter(mBHAdapter);
					break;
				case RESPONSE_FRIEND_DETAIL_SUCCESS:
					PVPopupWindow.show(PickFriendActivity.this, mShowView, detailFriend,null);
					break;

				// 密码错误
				case MenueApiFactory.LOGIN_PASSWORD_ERROR:

					ShowToast.showToast(PickFriendActivity.this, getResources().getString(R.string.reg_pwd_no_email_yes), Toast.LENGTH_LONG);
					break;

				// 邮箱没有注册
				// case MenueApiFactory.LOGIN_EMAIL_ERROR:

				// ShowToast.showToast(PickFriendActivity.this, getResources().getString(R.string.reg_email_no),
				// Toast.LENGTH_LONG);
				// break;

				// 服务器异常
				case MenueApiFactory.LOGIN_SERVER_ERROR:

					ShowToast.showToast(PickFriendActivity.this, getResources().getString(R.string.reg_server_no), Toast.LENGTH_LONG);
					break;

				// 管理员不允许客户端登录
				case MenueApiFactory.LOGIN_ADMIN_ERROR:

					ShowToast.showToast(PickFriendActivity.this, getResources().getString(R.string.reg_admin_no), Toast.LENGTH_LONG);
					break;

				default:
					break;
			}
		}

	};

	private void doFriendDetailShow(Friend friend) {

		GalHttpRequest request = GalHttpRequest.requestWithURL(this, MenueApiUrl.FRIEND_DETAIL_URL);
		// GalHttpRequest request = GalHttpRequest.requestWithURL(this, MenueApiUrl.LOGIN_URL);
		// request.setPostValueForKey(MenueApiFactory.EMAIL, email);
		// {"token":"asdasd@126.com|5289ea63123123123","userId":"JQb+lraNnlQ=","atUserId":"45V4n7AppOk=","appId":1,"deviceId":"android2323","language":"zh_CN"}

		UserInfo userInfo = PrefsUtils.LoginState.getLoginUser(this);
		request.setPostValueForKey(MenueApiFactory.TOKEN, userInfo.getToken());
		request.setPostValueForKey(MenueApiFactory.USERID, userInfo.getSuid());// ?
		request.setPostValueForKey(MenueApiFactory.USERID_FRIEND, friend.getSuid());// ?
		request.setPostValueForKey(MenueApiFactory.LANGUAGE, Locale.getDefault().getLanguage());
		request.setPostValueForKey(MenueApiFactory.DEVICE_ID, android.os.Build.DEVICE);
		request.setPostValueForKey(MenueApiFactory.APP_ID, Contract.APP_ID);

		request.startAsynRequestString(new GalHttpLoadTextCallBack() {

			@Override
			public void textLoaded(String text) {
				try {
					System.out.println(text);
					// text =
					// "{\"message\":\"成功\",\"userDetail\":{\"signature\":\"\",\"country\":\"US\",\"nick\":\"test2nick\",\"headUrl\":\"\",\"tacotyId\":\"\",\"phone\":\"+8611111111112\",\"userState\":0,\"createTime\":1363155023000,\"appList\":[{\"appId\":1,\"picUrl\":null,\"appName\":\"photochat\",\"appUrl\":\"version\"},{\"appId\":2,\"picUrl\":null,\"appName\":\"photochat\",\"appUrl\":\"version\"},{\"appId\":3,\"picUrl\":null,\"appName\":\"photochat\",\"appUrl\":\"version\"},{\"appId\":4,\"picUrl\":null,\"appName\":\"photochat\",\"appUrl\":\"version\"},{\"appId\":5,\"picUrl\":null,\"appName\":\"photochat\",\"appUrl\":\"version\"},{\"appId\":6,\"picUrl\":null,\"appName\":\"photochat\",\"appUrl\":\"version\"},{\"appId\":7,\"picUrl\":null,\"appName\":\"photochat\",\"appUrl\":\"version\"},{\"appId\":8,\"picUrl\":null,\"appName\":\"photochat\",\"appUrl\":\"version\"},{\"appId\":9,\"picUrl\":null,\"appName\":\"photochat\",\"appUrl\":\"version\"},{\"appId\":10,\"picUrl\":null,\"appName\":\"photochat\",\"appUrl\":\"version\"},{\"appId\":11,\"picUrl\":null,\"appName\":\"photochat\",\"appUrl\":\"version\"},{\"appId\":12,\"picUrl\":null,\"appName\":\"photochat\",\"appUrl\":\"version\"}],\"suId\":\"45V4n7AppOk=\",\"mark\":\"小S\"},\"status\":0}";
					JSONObject obj = new JSONObject(text);
					int state = obj.getInt(MenueApiFactory.RESPONSE_KEY_STATUS);
					if (state == MenueApiFactory.RESPONSE_STATE_SUCCESS) {
						JSONObject detailObj = obj.getJSONObject("userDetail");

						detailFriend = new Gson().fromJson(obj.getJSONObject("userDetail").toString(), DetailFriend.class);

						ArrayList<AppBean> appLists = new Gson().fromJson(detailObj.getJSONArray("appList").toString(),
						                                                  new TypeToken<ArrayList<AppBean>>() {
						                                                  }.getType());

						detailFriend.setAppBeans(appLists);

						mHandler.sendMessage(mHandler.obtainMessage(RESPONSE_FRIEND_DETAIL_SUCCESS));
					}
				}
				catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void loadFail() {
				mHandler.sendMessage(mHandler.obtainMessage(MenueApiFactory.LOGIN_SERVER_ERROR));
			}
		});

	}

	private void doFriendAdd(Friend friend, final Handler handler) {

		GalHttpRequest request = GalHttpRequest.requestWithURL(this, MenueApiUrl.FRIEND_ADD_URL);
		// GalHttpRequest request = GalHttpRequest.requestWithURL(this, MenueApiUrl.LOGIN_URL);
		// request.setPostValueForKey(MenueApiFactory.EMAIL, email);
		// {"token":"asdasd@126.com|5289ea63123123123","userId":1,"atUserId":"加密id"}
		/*
		 * { "token": "asdasd@126.com|5289ea63123123123", "appId": 1, "deviceId": "android2323", "seUserId": 8,
		 * "seSuid": "vgvqwTIiV8k=", "snick": "aaa", "shead": "123123123.hgh", "friends":
		 * "[{'reUserId':7,'reSuid':'asdasd','rnick':'xiaoxiao','rhead':'123123.jhp','rset':0}]" }
		 */

		String reSuid = friend.getSuid();
		String rnick = friend.getNick();
		String rhead = friend.getHeadUrl();
		JsonObject object = new JsonObject();
		object.addProperty("reSuid", reSuid);
		object.addProperty("rnick", rnick);
		object.addProperty("rhead", rhead);
		JsonArray jsonArray = new JsonArray();
		jsonArray.add(object);
		String sfriends = jsonArray.toString();
		System.out.println("===" + sfriends);

		UserInfo userInfo = PrefsUtils.LoginState.getLoginUser(this);
		request.setPostValueForKey(MenueApiFactory.TOKEN, userInfo.getToken());
		request.setPostValueForKey(MenueApiFactory.DEVICE_ID, android.os.Build.DEVICE);
		request.setPostValueForKey(MenueApiFactory.APP_ID, Contract.APP_ID);
		request.setPostValueForKey(MenueApiFactory.SESUID, userInfo.getSuid());
		request.setPostValueForKey(MenueApiFactory.SNICK, userInfo.getNick());
		request.setPostValueForKey(MenueApiFactory.SHEAD_URL, userInfo.getHeadUrl());
		request.setPostValueForKey(MenueApiFactory.FRIENDS, sfriends);
		request.setPostValueForKey(MenueApiFactory.LANGUAGE, Locale.getDefault().getLanguage());

		request.startAsynRequestString(new GalHttpLoadTextCallBack() {

			@Override
			public void textLoaded(String text) {
				try {
					System.out.println(text);
					JSONObject obj = new JSONObject(text);
					int state = obj.getInt(MenueApiFactory.RESPONSE_KEY_STATUS);
					if (state == MenueApiFactory.RESPONSE_STATE_SUCCESS) {
						handler.sendMessage(handler.obtainMessage(state));
					}
				}
				catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void loadFail() {
				handler.sendMessage(handler.obtainMessage(MenueApiFactory.LOGIN_SERVER_ERROR));
			}
		});

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pick_friend_activity);
		mContext = this;
		initTitle();
		mProgressbar = (ProgressBar) findViewById(R.id.login_progressbar);
		mListView = (ListView) findViewById(R.id.list_friend);

		mBottomLayout = findViewById(R.id.add_friend_bottom_pandle_layout);
		mSelectedView = (TextView) findViewById(R.id.add_friend_bottom_panele_text);
		mBottomHScrollListView = (HorizontalListView) findViewById(R.id.add_friend_bottom_pandle_scroller);

		mInviteBtn = (Button) findViewById(R.id.add_friend_bottom_invite_button);
		mInviteBtn.setOnClickListener(this);

		mQueryHandler = new QueryHandler(getContentResolver());
		mQueryHandler.startQuery(QUERY_TOKEN, null, ContactsContract.CommonDataKinds.Phone.CONTENT_URI, COLUMNS, null, null, null);
		mProgressbar.setVisibility(View.VISIBLE);

	}

	private void initTitle() {
		mBack = findViewById(R.id.back);
		mBack.setOnClickListener(this);

		mTitleTextView = (TextView) findViewById(R.id.titleContent);
		mTitleTextView.setVisibility(View.VISIBLE);

		//
		mChooseTextView = (TextView) findViewById(R.id.choosebutton);
		mChooseTextView.setBackgroundDrawable(getResources().getDrawable(R.drawable.base_go_btn));
		mChooseTextView.setText("");
		mChooseTextView.setVisibility(View.VISIBLE);
		mChooseTextView.setOnClickListener(this);

		mAddFriendLabel = findViewById(R.id.add_friend_titlebar_label);

	}

	private void pickUserInfosByPhoneNumber(UserInfo userInfo, String phoneList) {
		GalHttpRequest request = GalHttpRequest.requestWithURL(this, MenueApiUrl.FRIEND_QUERY_BY_PHONENUM_URL);
		// GalHttpRequest request = GalHttpRequest.requestWithURL(this, MenueApiUrl.LOGIN_URL);
		// request.setPostValueForKey(MenueApiFactory.EMAIL, email);
		request.setPostValueForKey(MenueApiFactory.TOKEN, userInfo.getToken());
		request.setPostValueForKey(MenueApiFactory.USERID, userInfo.getSuid());
		request.setPostValueForKey(MenueApiFactory.LANGUAGE, Locale.getDefault().getLanguage());
		request.setPostValueForKey(MenueApiFactory.DEVICE_ID, android.os.Build.DEVICE);
		// request.setPostValueForKey(MenueApiFactory.APP_ID, Contract.APP_ID);
		request.setPostValueForKey(MenueApiFactory.PHONE_LIST, phoneList);

		request.startAsynRequestString(new GalHttpLoadTextCallBack() {

			@Override
			public void textLoaded(String text) {
				try {
					// text =
					// "{\"message\":\"成功\",\"time\":15311222656,\"status\":0,\"userList\":[{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"123456789\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"15311222565\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"},{\"type\":1,\"nameChat\":\"jelly\",\"name\":\"小马\",\"display\":\"lucy\",\"number\":\"13312345678\",\"headUri\":\"http://avatar.csdn.net/9/D/2/1_catherine880619.jpg\"}]}";
					System.out.println(text);
					JSONObject obj = new JSONObject(text);
					int state = obj.getInt(MenueApiFactory.RESPONSE_KEY_STATUS);
					if (state == MenueApiFactory.RESPONSE_STATE_SUCCESS) {
						friends = new Gson().fromJson(obj.getJSONArray("userList").toString(), new TypeToken<List<Friend>>() {
						}.getType());
						System.out.println("" + friends.toString());
					}
					inviteFriends = PhoneBookUtils.parseFriend(getApplicationContext(), friends);
					mHandler.sendMessage(mHandler.obtainMessage(state));

				}
				catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void loadFail() {
				mHandler.sendMessage(mHandler.obtainMessage(MenueApiFactory.LOGIN_SERVER_ERROR));
			}
		});
	}

	class QueryHandler extends AsyncQueryHandler {

		public QueryHandler(ContentResolver cr) {
			super(cr);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			try {
				if (this != mQueryHandler) {
					Log.d(TAG, "onQueryComplete: discard result, the query handler is reset!");
					return;
				}
				if (PickFriendActivity.this.isFinishing()) {
					return;
				}
				switch (token) {
					case QUERY_TOKEN:
						Set<String> phoneList = new HashSet<String>();
						while (cursor.moveToNext()) {
							int contactId = cursor.getInt(0);
							String data = cursor.getString(1);
							data = PhoneBookUtils.removeNonDigits(data);
							String display_name = cursor.getString(2);
							int photoid = cursor.getInt(3);
							// startPhoto(contactId, photoid);
							phoneList.add("+86" + data);
						}
						String phoneListJson = new Gson().toJson(phoneList);
						System.out.println("jsonArray=" + phoneListJson);
						pickUserInfosByPhoneNumber(PrefsUtils.LoginState.getLoginUser(PickFriendActivity.this), phoneListJson);

						break;
					case QUERY_EMAIL_TOKEN:
						while (cursor.moveToNext()) {
							String email = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
							System.out.println("email=" + email);

						}
						mProgressbar.setVisibility(View.GONE);
						break;
					case QUERY_PHOTO_TOKEN:
						while (cursor.moveToNext()) {
							String email = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
							System.out.println("email=" + email);

						}
						mProgressbar.setVisibility(View.GONE);
						break;
				}
			}
			finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}

		@Deprecated
		private void startPhoto(int contactId, int photoid) {
			mQueryHandler.startQuery(QUERY_EMAIL_TOKEN, null, ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
			                         ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=" + contactId, null, null);
			//
			Bitmap contactPhoto = null;
			if (photoid > 0) {
				Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
				InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(), uri);
				contactPhoto = BitmapFactory.decodeStream(input);
			} else {
				contactPhoto = BitmapFactory.decodeResource(getResources(), R.drawable.edit_fields_background);
			}
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.back:
				finish();
				break;
			case R.id.choosebutton:
				startActivity(new Intent(this, HomeActivity.class));
				finish();
				break;
			case R.id.add_friend_bottom_invite_button:
				// 群发送短信。
				StringBuilder mobiles = new StringBuilder();
				for (Friend f : invitedLists) {
					mobiles.append(f.getPhone()).append(";");
				}
				System.out.println(mobiles.toString());
				String msg = String.format(getResources().getString(R.string.my_firend_invite_send_short_msg), "mark.",
				                           android.os.Build.VERSION.RELEASE, "http://www.menue.com/photochat/", "123458755");
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.putExtra("address", mobiles.toString());
				intent.putExtra("sms_body", msg);
				intent.setType("vnd.android-dir/mms-sms");
				startActivity(intent);
				break;

		}
	}

	class BottomHScrollBarAdapter extends BaseFriendAdapter<Friend> {

		public BottomHScrollBarAdapter(Context mContext, List<?> mFriendList) {
			super(mContext, mFriendList);
		}

		@Override
		public View newView(Context context, List<?> friendLists, ViewGroup parent) {
			return LayoutInflater.from(parent.getContext()).inflate(R.layout.invite_friend_bottom_list_item, null);
		}

		@Override
		public void bindView(Context context, View view, List<?> friendLists, int position) {
			Friend friend = (Friend) friendLists.get(position);
			TextView labelView = (TextView) view.findViewById(R.id.label);
			labelView.setText(friend.getNick() + ",  ");
		}

	}

}
