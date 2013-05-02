package com.rcplatform.phototalk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.rcplatform.phototalk.adapter.MyFriendExpandableListAdapter;
import com.rcplatform.phototalk.adapter.MyFriendExpandableListAdapter.ViewHolder;
import com.rcplatform.phototalk.api.MenueApiFactory;
import com.rcplatform.phototalk.api.MenueApiUrl;
import com.rcplatform.phototalk.bean.AppBean;
import com.rcplatform.phototalk.bean.DetailFriend;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.FriendChat;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.galhttprequest.GalHttpRequest;
import com.rcplatform.phototalk.galhttprequest.GalHttpRequest.GalHttpLoadTextCallBack;
import com.rcplatform.phototalk.galhttprequest.LogUtil;
import com.rcplatform.phototalk.utils.Contract;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.PhoneBookUtils;
import com.rcplatform.phototalk.utils.PinyinComparator;
import com.rcplatform.phototalk.utils.PrefsUtils;
import com.rcplatform.phototalk.utils.ShowToast;
import com.rcplatform.phototalk.views.PVPopupWindow;

/**
 * 标题、简要说明. <br>
 * 类详细说明.
 * <p>
 * Copyright: Menue,Inc Copyright (c) 2013-3-4 上午11:37:13
 * <p>
 * Team:Menue Beijing
 * <p>
 * 
 * @author jelly.xiong@menue.com.cn
 * @version 1.0.0
 */
public class MyFriendActivity extends Activity implements View.OnClickListener {

	private static final String TAG = "MyFriendsActivity";

	private TextView mTitleView;

	private View mAddFriendView;

	private View mNoFriendView;

	private ProgressBar mProgressbar;

	@Deprecated
	private View mExpandableView;

	// /////
	private ExpandableListView mExpandListView;

	private TextView tv;

	// 整个悬浮条
	RelativeLayout linear;

	// 悬浮条的下线
	View lineView;

	/**
	 * 当前打开的父节点
	 */
	private int the_group_expand_position = -1;

	/**
	 * 获取当前打开的节点的高度
	 */
	private int indicatorGroupHeight;

	/**
	 * 是否有打开的父节点
	 */
	private boolean isExpanding = false;

	private MyFriendExpandableListAdapter mExListAdapter;

	// ////
	@Deprecated
	private ListView mListView;

	@Deprecated
	private ListView mSuggestedListView;

	private ImageView mArrowView;

	private View mViewLayout;

	private View mHeadView;

	private LinkedList<Friend> suggestedFriends;

	private LinkedList<FriendChat> myFriendsChats = new LinkedList<FriendChat>();

	private LinkedList<FriendChat> myFriendX;

	private LinkedList<FriendChat> xFriendSuggested = new LinkedList<FriendChat>();

	private int THE_GROUP_EXPAND_POSITION = -1;

	private Context mContext;

	protected static final int RESPONSE_FRIEND_DETAIL_SUCCESS = 10001;

	protected static final int RESPONSE_FRIEND_ADD_SUCCESS = 10002;

	private DetailFriend detailFriend;

	private View mShowView;

	private Handler mHandler2 = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			mProgressbar.setVisibility(View.GONE);
			super.handleMessage(msg);
			switch (msg.what) {
				case MenueApiFactory.RESPONSE_STATE_SUCCESS:
				case MenueApiFactory.RESPONSE_STATE_SUCCESS_NO_MYFRIEND:

					if (xFriendSuggested != null && xFriendSuggested.size() == 0 && myFriendsChats != null && myFriendsChats.size() == 0) {
						mNoFriendView.setVisibility(View.VISIBLE);
						return;
					}

					List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
					List<LinkedList<FriendChat>> childData = new LinkedList<LinkedList<FriendChat>>();
					if (xFriendSuggested != null && xFriendSuggested.size() > 0 && myFriendsChats != null && myFriendsChats.size() == 0) {
						Map<String, String> curGroupMap = new HashMap<String, String>();
						curGroupMap.put("group_text", "好友推荐");
						groupData.add(curGroupMap);
						childData.add(xFriendSuggested);
						mExListAdapter = new MyFriendExpandableListAdapter(getApplicationContext(), groupData, childData);
						mExpandListView.setAdapter(mExListAdapter);
						mExpandListView.expandGroup(0);
					} else if (xFriendSuggested != null && xFriendSuggested.size() == 0 && myFriendsChats != null && myFriendsChats.size() > 0) {

						Map<String, String> curGroupMap = new HashMap<String, String>();
						curGroupMap.put("group_text", "我的好友");
						groupData.add(curGroupMap);
						childData.add(myFriendsChats);

						mExListAdapter = new MyFriendExpandableListAdapter(getApplicationContext(), groupData, childData);
						mExpandListView.setAdapter(mExListAdapter);
						mExpandListView.expandGroup(0);
					} else if (xFriendSuggested != null && xFriendSuggested.size() > 0 && myFriendsChats != null && myFriendsChats.size() > 0) {
						for (int i = 0; i < 2; i++) {
							Map<String, String> curGroupMap = new HashMap<String, String>();
							groupData.add(curGroupMap);
							if (i == 0) {
								curGroupMap.put("group_text", "好友推荐");
								childData.add(xFriendSuggested);
							} else {
								curGroupMap.put("group_text", "我的好友");
								childData.add(myFriendsChats);
							}
						}
						mExListAdapter = new MyFriendExpandableListAdapter(getApplicationContext(), groupData, childData);
						mExpandListView.setAdapter(mExListAdapter);
						// 自动打开各项。
						mExpandListView.expandGroup(0);
						mExpandListView.expandGroup(1);
						mExpandListView.setOnGroupClickListener(new OnGroupClickListener() {

							@Override
							public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
								// 取消第二组不可点击。
								if (groupPosition == 1) {
									return true;
								}
								return false;
							}
						});
					}

					// if (myFriendsChats != null && myFriendsChats.size() > 0) {
					// MyFriendsItemListAdapter myFriendsItemListAdapter = new
					// MyFriendsItemListAdapter(getApplicationContext(), myFriendsChats);
					// mListView.setAdapter(myFriendsItemListAdapter);
					// }

					mExListAdapter.setOnFriendAddListener(new AddFriendAdapter.OnFriendAddListener() {

						@Override
						public void addFriend(Friend friend, Handler h) {
							doFriendAdd(friend, h);
						}
					});
					mExListAdapter.setOnFriendPortraitListener(new AddFriendAdapter.OnFriendPortraitListener() {

						@Override
						public void showFriendDetail(View view, Friend friend) {
							mShowView = view;
							doFriendDetailShow(friend);
						}

					});
					break;
				case RESPONSE_FRIEND_DETAIL_SUCCESS:
					PVPopupWindow.show(MyFriendActivity.this, mShowView, detailFriend,null);
					break;
				case RESPONSE_FRIEND_ADD_SUCCESS:
					sync();
					break;
				case MenueApiFactory.LOGIN_EMAIL_ERROR:
					ShowToast.showToast(MyFriendActivity.this, getResources().getString(R.string.reg_email_no), Toast.LENGTH_LONG);
					break;
				case MenueApiFactory.LOGIN_SERVER_ERROR:
					ShowToast.showToast(MyFriendActivity.this, getResources().getString(R.string.reg_server_no), Toast.LENGTH_LONG);
					break;
				case MenueApiFactory.LOGIN_ADMIN_ERROR:
					ShowToast.showToast(MyFriendActivity.this, getResources().getString(R.string.reg_admin_no), Toast.LENGTH_LONG);
					break;
			}

		}

	};

	private View mBack;

	private TextView mTitleTextView;

	private TextView mChooseTextView;

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

	private void doFriendDetailShow(Friend friend) {

		GalHttpRequest request = GalHttpRequest.requestWithURL(this, MenueApiUrl.FRIEND_DETAIL_URL);
		// GalHttpRequest request = GalHttpRequest.requestWithURL(this, MenueApiUrl.LOGIN_URL);
		// request.setPostValueForKey(MenueApiFactory.EMAIL, email);
		// {"token":"asdasd@126.com|5289ea63123123123","userId":"JQb+lraNnlQ=","atUserId":"45V4n7AppOk=","appId":1,"deviceId":"android2323","language":"zh_CN"}

		UserInfo userInfo = PrefsUtils.LoginState.getLoginUser(this);
		request.setPostValueForKey(MenueApiFactory.TOKEN, userInfo.getToken());
		request.setPostValueForKey(MenueApiFactory.USERID, /* "JQb+lraNnlQ=" */userInfo.getSuid());// ?
		request.setPostValueForKey(MenueApiFactory.USERID_FRIEND, /* "45V4n7AppOk=" */friend.getSuid());// ?
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

						mHandler2.sendMessage(mHandler2.obtainMessage(RESPONSE_FRIEND_DETAIL_SUCCESS));
					}
				}
				catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void loadFail() {
				mHandler2.sendMessage(mHandler2.obtainMessage(MenueApiFactory.LOGIN_SERVER_ERROR));
			}
		});

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_friends_activity);

		mContext = this;
		findViewById(R.id.back).setOnClickListener(this);
		mTitleView = (TextView) findViewById(R.id.titleContent);
		mTitleView.setOnClickListener(this);
		mTitleView.setText(getResources().getString(R.string.my_firend_title));
		mAddFriendView = findViewById(R.id.choosebutton);
		mAddFriendView.setOnClickListener(this);
		mAddFriendView.setBackgroundDrawable(getResources().getDrawable(R.drawable.my_friend_add_friend));

		initTitle();

		mProgressbar = (ProgressBar) findViewById(R.id.login_progressbar);
		mNoFriendView = findViewById(R.id.my_friend_no_friend_layout);
		//
		mExpandListView = (ExpandableListView) findViewById(R.id.my_friend_expandable_listview);
		mExpandListView.setOnCreateContextMenuListener(this);
		mExpandListView.setGroupIndicator(null);
		// initExpandListView();

		mArrowView = (ImageView) findViewById(R.id.my_friend_expandable_down_arrow);

		// mHeadView =
		// LayoutInflater.from(this).inflate(R.layout.my_friends_list_head_listview,
		// null);
		// mListView.addHeaderView(mHeadView);
		// mSuggestedListView = (ListView)
		// mHeadView.findViewById(R.id.my_friend_suggested_listview);
		//

		sync();

		// downloadSuggestedFriend();
		// downloadMyFriends();
	}

	private void initTitle() {
		mBack = findViewById(R.id.back);
		mBack.setVisibility(View.VISIBLE);
		mBack.setOnClickListener(this);
		//
		mTitleTextView = (TextView) findViewById(R.id.titleContent);
		mTitleTextView.setText(getResources().getString(R.string.my_firend_title));
		mTitleTextView.setVisibility(View.VISIBLE);

		//
		mChooseTextView = (TextView) findViewById(R.id.choosebutton);
		mChooseTextView.setBackgroundDrawable(getResources().getDrawable(R.drawable.my_friend_add_friend));
		mChooseTextView.setVisibility(View.VISIBLE);
		mChooseTextView.setOnClickListener(this);
		mChooseTextView.setText("");

	}

	private void sync() {
		if (myFriendsChats != null) {
			myFriendsChats.clear();
		}
		if (xFriendSuggested != null) {
			xFriendSuggested.clear();
		}
		if (mQueryHandler == null) {
			mQueryHandler = new QueryHandler(getContentResolver());
		}
		mQueryHandler.startQuery(QUERY_TOKEN, null, ContactsContract.CommonDataKinds.Phone.CONTENT_URI, COLUMNS, null, null, null);
		mProgressbar.setVisibility(View.VISIBLE);
	}

	private void initExpandListView() {
		mExpandListView = (ExpandableListView) findViewById(R.id.my_friend_expandable_listview);
		tv = (TextView) findViewById(R.id.qq_list_textview);
		lineView = (View) findViewById(R.id.group_line);
		mExpandListView.setGroupIndicator(null);
		/*
		 * 滑动子列表时在上方显示父节点的view
		 */
		linear = (RelativeLayout) findViewById(R.id.gone_linear);
		/**
		 * 监听父节点打开的事件
		 */
		mExpandListView.setOnGroupExpandListener(new OnGroupExpandListener() {

			@Override
			public void onGroupExpand(int groupPosition) {
				the_group_expand_position = groupPosition;
				linear.setVisibility(View.VISIBLE);
				lineView.setVisibility(View.VISIBLE);
				isExpanding = true;
			}

		});
		/**
		 * 监听父节点关闭的事件
		 */
		mExpandListView.setOnGroupCollapseListener(new OnGroupCollapseListener() {

			@Override
			public void onGroupCollapse(int groupPosition) {
				if (isExpanding) {
					linear.setVisibility(View.GONE);
					lineView.setVisibility(View.GONE);
				}
				the_group_expand_position = -1;
				isExpanding = false;
			}

		});
		/***
		 * 当点击悬浮条时可以关闭打开的节点
		 */
		linear.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// linear.setVisibility(View.GONE);
				if (isExpanding) {
					linear.setVisibility(View.GONE);
					lineView.setVisibility(View.GONE);
					mExpandListView.collapseGroup(the_group_expand_position);
					isExpanding = false;
				}
			}

		});
		/**
		 * 点击父列表中的那一项
		 */
		mExpandListView.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
				// 让点击项弹回最顶部
				if (the_group_expand_position == -1) {
					mExpandListView.expandGroup(groupPosition);
					mExpandListView.setSelectedGroup(groupPosition);
					the_group_expand_position = groupPosition;
					linear.setVisibility(View.VISIBLE);
					lineView.setVisibility(View.VISIBLE);
					// /isExpanding = true;
				} else if (the_group_expand_position == groupPosition) {// 当点击的节点和打开的节点是同一个节点时，关闭此节点
					linear.setVisibility(View.GONE);
					lineView.setVisibility(View.GONE);
					mExpandListView.collapseGroup(groupPosition);
					the_group_expand_position = -1;
					// isExpanding = false;
				} else {// 如果点击节点不是打开的节点关闭之前的节点打开点击的节点
					mExpandListView.collapseGroup(the_group_expand_position);
					mExpandListView.expandGroup(groupPosition);
					mExpandListView.setSelectedGroup(groupPosition);
					the_group_expand_position = groupPosition;
				}
				return true;
			}
		});

		/**
		 * 通过setOnScrollListener来监听列表上下滑动时item显示和消失的事件
		 */
		mExpandListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				/**
				 * calculate point (0,0)
				 */
				int npos = view.pointToPosition(0, 0);
				Log.v("npos", "npos=" + npos);
				if (npos != AdapterView.INVALID_POSITION) {
					long pos = mExpandListView.getExpandableListPosition(npos);
					int childPos = ExpandableListView.getPackedPositionChild(pos);
					final int groupPos = ExpandableListView.getPackedPositionGroup(pos);
					if (childPos == AdapterView.INVALID_POSITION) {
						View groupView = mExpandListView.getChildAt(npos - mExpandListView.getFirstVisiblePosition());
						indicatorGroupHeight = groupView.getHeight();// 55
					}
					Log.v("npos", "indicatorGroupHeight=" + indicatorGroupHeight);
					// get an error data, so return now
					if (indicatorGroupHeight == 0) {
						return;
					}

					if (isExpanding) {
						// 控制悬浮条的内容变化
						// tv.setText(groupData.get(the_group_expand_position).get("group_text").toString());
						// 当悬浮条滑动到下一个节点的时候自动消失
						if (the_group_expand_position != groupPos) {
							linear.setVisibility(View.GONE);
							lineView.setVisibility(View.GONE);
						} else {
							linear.setVisibility(View.VISIBLE);
							lineView.setVisibility(View.VISIBLE);
						}
					}
				}

				if (the_group_expand_position == -1) {
					return;
				}

				/**
				 * calculate point (0,indicatorGroupHeight)
				 */
				int showHeight = t();
				// update group position
				MarginLayoutParams layoutParams = (MarginLayoutParams) linear.getLayoutParams();
				// 得到悬浮的条滑出屏幕多少
				layoutParams.topMargin = -(indicatorGroupHeight - showHeight);
				Log.v("npos", "layoutParams.topMargin=" + layoutParams.topMargin);

			}

		});

	}

	private int t() {

		int showHeight = indicatorGroupHeight;
		// 从顶部到滑动的位置总过滑动了多少个节点
		int nEndPos = mExpandListView.pointToPosition(0, indicatorGroupHeight);
		if (nEndPos != AdapterView.INVALID_POSITION) {
			long pos = mExpandListView.getExpandableListPosition(nEndPos);
			// 当前滑动到父节点的什么位置
			int groupPos = ExpandableListView.getPackedPositionGroup(pos);
			if (groupPos != the_group_expand_position) {
				View viewNext = mExpandListView.getChildAt(nEndPos - mExpandListView.getFirstVisiblePosition());
				// 悬浮条还有多少在可见（即没有滑出屏幕的部分）当滑动到下一个父列表顶部，悬浮条会随之滑出屏幕
				showHeight = viewNext.getTop();
			}
		}
		Log.v("npos", "showHeight=" + showHeight);
		return showHeight;
	}

	protected void failure(JSONObject obj) {
		DialogUtil.createMsgDialog(this, getResources().getString(R.string.login_error), getResources().getString(R.string.ok)).show();
		finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.back:
				finish();
				break;
			case R.id.choosebutton:
				Intent intent=new Intent(this, AddFriendsActivity.class);
				intent.setData(Uri.parse(""));
				startActivity(intent);
				break;
			case R.id.my_friend_expandable_layout:
				if (mSuggestedListView.getVisibility() == View.VISIBLE) {
					mSuggestedListView.setVisibility(View.GONE);
					mArrowView.setBackgroundResource(R.drawable.expander_ic_minimized);
				} else {
					mSuggestedListView.setVisibility(View.VISIBLE);
					mArrowView.setBackgroundResource(R.drawable.expander_ic_maximized);
				}
				break;

		}
	}

	private QueryHandler mQueryHandler;

	final String[] COLUMNS = new String[] { Contacts._ID, Data.DATA1, Data.DISPLAY_NAME, Data.PHOTO_ID };

	private static final int QUERY_TOKEN = 0;

	private static final int GROUP_C_MENU_DEL = 10010;

	/**
	 * 查询电话本. <br>
	 * <p>
	 * Copyright: Menue,Inc Copyright (c) 2013-3-15 下午04:22:29
	 * <p>
	 * Team:Menue Beijing
	 * <p>
	 * 
	 * @author jelly.xiong@menue.com.cn
	 * @version 1.0.0
	 */
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
				if (MyFriendActivity.this.isFinishing()) {
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
						// 同步我的好友。
						syncMyFriends(PrefsUtils.LoginState.getLoginUser(MyFriendActivity.this), phoneListJson);
						break;
				}
			}
			finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}
	}

	public void syncMyFriends(UserInfo userInfo, String phoneListJson) {

		// GalHttpRequest request = GalHttpRequest.requestWithURL(this, MenueApiUrl.LOGIN_URL);
		GalHttpRequest request = GalHttpRequest.requestWithURL(this, MenueApiUrl.GET_MY_FRIENDS_URL);
		// request.setPostValueForKey(MenueApiFactory.EMAIL, email);
		// request.setPostValueForKey(MenueApiFactory.PWD, MD5.md5Hash(psw));
		request.setPostValueForKey(MenueApiFactory.USERID, userInfo.getSuid());
		request.setPostValueForKey(MenueApiFactory.TOKEN, userInfo.getToken());
		request.setPostValueForKey(MenueApiFactory.LANGUAGE, Locale.getDefault().getLanguage());
		request.setPostValueForKey(MenueApiFactory.DEVICE_ID, android.os.Build.DEVICE);
		request.setPostValueForKey(MenueApiFactory.APP_ID, Contract.APP_ID);
		request.setPostValueForKey(MenueApiFactory.PHONE_LIST, phoneListJson);
		// no
		// String text = "{\"message\":\"成功\",\"status\":0,\"recommendUsers\":[null,null,null],\"myUsers\":[null]}";
		// a and b
		// String text =
		// "{\"message\":\"成功\",\"status\":0,\"recommendUsers\":[[{\"signature\":\"\",\"userId\":9,\"appId\":1,\"nick\":\"test2nick\",\"headUrl\":\"\",\"tacotyId\":\"\",\"phone\":\"+8611111111112\",\"createTime\":1363155023000,\"receiveSet\":0,\"userFrom\":9,\"suId\":\"45V4n7AppOk=\"},{\"signature\":\"\",\"userId\":10,\"appId\":1,\"nick\":\"testNick\",\"headUrl\":\"\",\"tacotyId\":\"\",\"phone\":\"+8611111111113\",\"createTime\":1363244628000,\"receiveSet\":0,\"userFrom\":10,\"suId\":\"l64b7owRMCs=\"}],[{\"signature\":\"\",\"userId\":1,\"appId\":1,\"nick\":\"留几手\",\"headUrl\":\"http://tp4.sinaimg.cn/1761179351/180/22821626784/1\",\"tacotyId\":\"damon\",\"phone\":\"+8618600158571\",\"createTime\":1362478912000,\"receiveSet\":1,\"userFrom\":0,\"suId\":\"DcZTw+RZVA8=\"},{\"signature\":\"\",\"userId\":7,\"appId\":1,\"nick\":\"burning\",\"headUrl\":\"\",\"tacotyId\":\"13141516\",\"phone\":\"+8611111111111\",\"createTime\":1363087135000,\"receiveSet\":0,\"userFrom\":0,\"suId\":\"JQb+lraNnlQ=\"},{\"signature\":\"\",\"userId\":8,\"appId\":1,\"nick\":\"testNick\",\"headUrl\":\"\",\"tacotyId\":\"\",\"phone\":\"+8611111111111\",\"createTime\":1363088173000,\"receiveSet\":0,\"userFrom\":0,\"suId\":\"vgvqwTIiV8k=\"},{\"signature\":\"\",\"userId\":9,\"appId\":1,\"nick\":\"test2nick\",\"headUrl\":\"\",\"tacotyId\":\"\",\"phone\":\"+8611111111112\",\"createTime\":1363155023000,\"receiveSet\":0,\"userFrom\":0,\"suId\":\"45V4n7AppOk=\"},{\"signature\":\"\",\"userId\":10,\"appId\":1,\"nick\":\"testNick\",\"headUrl\":\"\",\"tacotyId\":\"\",\"phone\":\"+8611111111113\",\"createTime\":1363244628000,\"receiveSet\":0,\"userFrom\":0,\"suId\":\"l64b7owRMCs=\"}],null],\"myUsers\":[{\"signature\":\"\",\"userId\":9,\"appId\":1,\"nick\":\"test2nick\",\"headUrl\":\"\",\"tacotyId\":\"\",\"phone\":\"+8611111111112\",\"createTime\":1363155023000,\"receiveSet\":0,\"userFrom\":0,\"suId\":\"45V4n7AppOk=\"},{\"signature\":\"\",\"userId\":10,\"appId\":1,\"nick\":\"testNick\",\"headUrl\":\"\",\"tacotyId\":\"\",\"phone\":\"+8611111111113\",\"createTime\":1363244628000,\"receiveSet\":0,\"userFrom\":0,\"suId\":\"l64b7owRMCs=\"}]}";
		// a
		// String text =
		// "{\"message\":\"成功\",\"status\":0,\"recommendUsers\":[[{\"signature\":\"\",\"userId\":9,\"appId\":1,\"nick\":\"test2nick\",\"headUrl\":\"\",\"tacotyId\":\"\",\"phone\":\"+8611111111112\",\"createTime\":1363155023000,\"receiveSet\":0,\"userFrom\":9,\"suId\":\"45V4n7AppOk=\"},{\"signature\":\"\",\"userId\":10,\"appId\":1,\"nick\":\"testNick\",\"headUrl\":\"\",\"tacotyId\":\"\",\"phone\":\"+8611111111113\",\"createTime\":1363244628000,\"receiveSet\":0,\"userFrom\":10,\"suId\":\"l64b7owRMCs=\"}],[{\"signature\":\"\",\"userId\":1,\"appId\":1,\"nick\":\"留几手\",\"headUrl\":\"http://tp4.sinaimg.cn/1761179351/180/22821626784/1\",\"tacotyId\":\"damon\",\"phone\":\"+8618600158571\",\"createTime\":1362478912000,\"receiveSet\":1,\"userFrom\":0,\"suId\":\"DcZTw+RZVA8=\"},{\"signature\":\"\",\"userId\":7,\"appId\":1,\"nick\":\"burning\",\"headUrl\":\"\",\"tacotyId\":\"13141516\",\"phone\":\"+8611111111111\",\"createTime\":1363087135000,\"receiveSet\":0,\"userFrom\":0,\"suId\":\"JQb+lraNnlQ=\"},{\"signature\":\"\",\"userId\":8,\"appId\":1,\"nick\":\"testNick\",\"headUrl\":\"\",\"tacotyId\":\"\",\"phone\":\"+8611111111111\",\"createTime\":1363088173000,\"receiveSet\":0,\"userFrom\":0,\"suId\":\"vgvqwTIiV8k=\"},{\"signature\":\"\",\"userId\":9,\"appId\":1,\"nick\":\"test2nick\",\"headUrl\":\"\",\"tacotyId\":\"\",\"phone\":\"+8611111111112\",\"createTime\":1363155023000,\"receiveSet\":0,\"userFrom\":0,\"suId\":\"45V4n7AppOk=\"},{\"signature\":\"\",\"userId\":10,\"appId\":1,\"nick\":\"testNick\",\"headUrl\":\"\",\"tacotyId\":\"\",\"phone\":\"+8611111111113\",\"createTime\":1363244628000,\"receiveSet\":0,\"userFrom\":0,\"suId\":\"l64b7owRMCs=\"}],null],\"myUsers\":[null]}";
		// b
		// String text =
		// "{\"message\":\"成功\",\"status\":0,\"recommendUsers\":[null,null,null],\"myUsers\":[{\"signature\":\"\",\"userId\":9,\"appId\":1,\"nick\":\"艾妮\",\"headUrl\":\"\",\"tacotyId\":\"\",\"phone\":\"+8611111111112\",\"createTime\":1363155023000,\"receiveSet\":0,\"userFrom\":0,\"suId\":\"45V4n7AppOk=\"},{\"signature\":\"\",\"userId\":10,\"appId\":1,\"nick\":\"毕福剑\",\"headUrl\":\"\",\"tacotyId\":\"\",\"phone\":\"+8611111111113\",\"createTime\":1363244628000,\"receiveSet\":0,\"userFrom\":0,\"suId\":\"l64b7owRMCs=\"},{\"signature\":\"\",\"userId\":10,\"appId\":1,\"nick\":\"车论\",\"headUrl\":\"\",\"tacotyId\":\"\",\"phone\":\"+8611111111113\",\"createTime\":1363244628000,\"receiveSet\":0,\"userFrom\":0,\"suId\":\"l64b7owRMCs=\"},{\"signature\":\"\",\"userId\":10,\"appId\":1,\"nick\":\"低价\",\"headUrl\":\"\",\"tacotyId\":\"\",\"phone\":\"+8611111111113\",\"createTime\":1363244628000,\"receiveSet\":0,\"userFrom\":0,\"suId\":\"l64b7owRMCs=\"},{\"signature\":\"\",\"userId\":10,\"appId\":1,\"nick\":\"低价\",\"headUrl\":\"\",\"tacotyId\":\"\",\"phone\":\"+8611111111113\",\"createTime\":1363244628000,\"receiveSet\":0,\"userFrom\":0,\"suId\":\"l64b7owRMCs=\"},{\"signature\":\"\",\"userId\":10,\"appId\":1,\"nick\":\"低价\",\"headUrl\":\"\",\"tacotyId\":\"\",\"phone\":\"+8611111111113\",\"createTime\":1363244628000,\"receiveSet\":0,\"userFrom\":0,\"suId\":\"l64b7owRMCs=\"},{\"signature\":\"\",\"userId\":10,\"appId\":1,\"nick\":\"低价\",\"headUrl\":\"\",\"tacotyId\":\"\",\"phone\":\"+8611111111113\",\"createTime\":1363244628000,\"receiveSet\":0,\"userFrom\":0,\"suId\":\"l64b7owRMCs=\"},{\"signature\":\"\",\"userId\":10,\"appId\":1,\"nick\":\"福田\",\"headUrl\":\"\",\"tacotyId\":\"\",\"phone\":\"+8611111111113\",\"createTime\":1363244628000,\"receiveSet\":0,\"userFrom\":0,\"suId\":\"l64b7owRMCs=\"},{\"signature\":\"\",\"userId\":10,\"appId\":1,\"nick\":\"格格\",\"headUrl\":\"\",\"tacotyId\":\"\",\"phone\":\"+8611111111113\",\"createTime\":1363244628000,\"receiveSet\":0,\"userFrom\":0,\"suId\":\"l64b7owRMCs=\"},{\"signature\":\"\",\"userId\":10,\"appId\":1,\"nick\":\"格格\",\"headUrl\":\"\",\"tacotyId\":\"\",\"phone\":\"+8611111111113\",\"createTime\":1363244628000,\"receiveSet\":0,\"userFrom\":0,\"suId\":\"l64b7owRMCs=\"},{\"signature\":\"\",\"userId\":10,\"appId\":1,\"nick\":\"格格\",\"headUrl\":\"\",\"tacotyId\":\"\",\"phone\":\"+8611111111113\",\"createTime\":1363244628000,\"receiveSet\":0,\"userFrom\":0,\"suId\":\"l64b7owRMCs=\"},{\"signature\":\"\",\"userId\":10,\"appId\":1,\"nick\":\"格格\",\"headUrl\":\"\",\"tacotyId\":\"\",\"phone\":\"+8611111111113\",\"createTime\":1363244628000,\"receiveSet\":0,\"userFrom\":0,\"suId\":\"l64b7owRMCs=\"}]}";
		// loadOk(text);
		request.startAsynRequestString(new GalHttpLoadTextCallBack() {

			@Override
			public void textLoaded(String text) {
				// text =
				// "{\"message\":\"成功\",\"status\":0,\"recommendUsers\":[[{\"signature\":\"\",\"userId\":9,\"appId\":1,\"nick\":\"test2nick\",\"headUrl\":\"\",\"tacotyId\":\"\",\"phone\":\"+8611111111112\",\"createTime\":1363155023000,\"receiveSet\":0,\"userFrom\":9,\"suId\":\"45V4n7AppOk=\"},{\"signature\":\"\",\"userId\":10,\"appId\":1,\"nick\":\"testNick\",\"headUrl\":\"\",\"tacotyId\":\"\",\"phone\":\"+8611111111113\",\"createTime\":1363244628000,\"receiveSet\":0,\"userFrom\":10,\"suId\":\"l64b7owRMCs=\"}],[{\"signature\":\"\",\"userId\":1,\"appId\":1,\"nick\":\"留几手\",\"headUrl\":\"http://tp4.sinaimg.cn/1761179351/180/22821626784/1\",\"tacotyId\":\"damon\",\"phone\":\"+8618600158571\",\"createTime\":1362478912000,\"receiveSet\":1,\"userFrom\":0,\"suId\":\"DcZTw+RZVA8=\"},{\"signature\":\"\",\"userId\":7,\"appId\":1,\"nick\":\"burning\",\"headUrl\":\"\",\"tacotyId\":\"13141516\",\"phone\":\"+8611111111111\",\"createTime\":1363087135000,\"receiveSet\":0,\"userFrom\":0,\"suId\":\"JQb+lraNnlQ=\"},{\"signature\":\"\",\"userId\":8,\"appId\":1,\"nick\":\"testNick\",\"headUrl\":\"\",\"tacotyId\":\"\",\"phone\":\"+8611111111111\",\"createTime\":1363088173000,\"receiveSet\":0,\"userFrom\":0,\"suId\":\"vgvqwTIiV8k=\"},{\"signature\":\"\",\"userId\":9,\"appId\":1,\"nick\":\"test2nick\",\"headUrl\":\"\",\"tacotyId\":\"\",\"phone\":\"+8611111111112\",\"createTime\":1363155023000,\"receiveSet\":0,\"userFrom\":0,\"suId\":\"45V4n7AppOk=\"},{\"signature\":\"\",\"userId\":10,\"appId\":1,\"nick\":\"testNick\",\"headUrl\":\"\",\"tacotyId\":\"\",\"phone\":\"+8611111111113\",\"createTime\":1363244628000,\"receiveSet\":0,\"userFrom\":0,\"suId\":\"l64b7owRMCs=\"}],null],\"myUsers\":[{\"signature\":\"\",\"userId\":9,\"appId\":1,\"nick\":\"test2nick\",\"headUrl\":\"\",\"tacotyId\":\"\",\"phone\":\"+8611111111112\",\"createTime\":1363155023000,\"receiveSet\":0,\"userFrom\":0,\"suId\":\"45V4n7AppOk=\"},{\"signature\":\"\",\"userId\":10,\"appId\":1,\"nick\":\"testNick\",\"headUrl\":\"\",\"tacotyId\":\"\",\"phone\":\"+8611111111113\",\"createTime\":1363244628000,\"receiveSet\":0,\"userFrom\":0,\"suId\":\"l64b7owRMCs=\"}]}";
				loadOk(text);
			}

			@Override
			public void loadFail() {
				LogUtil.e(TAG, getResources().getString(R.string.net_error));
			}
		});

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
		int type = ExpandableListView.getPackedPositionType(info.packedPosition);
		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			menu.setHeaderTitle("操作");
			menu.add(0, GROUP_C_MENU_DEL, 0, "删除");
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case GROUP_C_MENU_DEL:
				ExpandableListView.ExpandableListContextMenuInfo info_del = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
				View view = info_del.targetView;
				ViewHolder vh = (ViewHolder) view.getTag();
				doDeleteFriendById(vh.id);
				System.out.println("========" + vh.id);
				mExListAdapter.notifyDataSetChanged();
				break;
		}
		return false;
	}

	private void doDeleteFriendById(String id) {
		mProgressbar.setVisibility(View.VISIBLE);
		// GalHttpRequest request = GalHttpRequest.requestWithURL(this, MenueApiUrl.LOGIN_URL);
		GalHttpRequest request = GalHttpRequest.requestWithURL(this, MenueApiUrl.DELETE_FRIEND_URL);
		// request.setPostValueForKey(MenueApiFactory.EMAIL, email);
		// request.setPostValueForKey(MenueApiFactory.PWD, MD5.md5Hash(psw));
		UserInfo userInfo =PrefsUtils.LoginState.getLoginUser(this);
		request.setPostValueForKey(MenueApiFactory.USERID, userInfo.getSuid());
		request.setPostValueForKey(MenueApiFactory.TOKEN, userInfo.getToken());
		request.setPostValueForKey(MenueApiFactory.ATUSERID, id);
		request.setPostValueForKey(MenueApiFactory.LANGUAGE, Locale.getDefault().getLanguage());
		request.setPostValueForKey(MenueApiFactory.DEVICE_ID, android.os.Build.DEVICE);
		request.setPostValueForKey(MenueApiFactory.APP_ID, Contract.APP_ID);

		request.startAsynRequestString(new GalHttpLoadTextCallBack() {

			@Override
			public void textLoaded(String text) {

				try {
					System.out.println(text);
					JSONObject obj = new JSONObject(text);
					final int status = obj.getInt(MenueApiFactory.RESPONSE_KEY_STATUS);
					if (status == MenueApiFactory.RESPONSE_STATE_SUCCESS) {
						sync();
					} else {
						mHandler2.sendMessage(mHandler2.obtainMessage(status));
					}
				}
				catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void loadFail() {
				LogUtil.e(TAG, getResources().getString(R.string.net_error));
			}
		});

	}

	private void loadOk(String text) {

		try {
			xFriendSuggested.clear();
			myFriendsChats.clear();
			System.out.println("loadOk....");
			System.out.println(text);
			JSONObject obj = new JSONObject(text);
			final int status = obj.getInt(MenueApiFactory.RESPONSE_KEY_STATUS);
			if (status == MenueApiFactory.RESPONSE_STATE_SUCCESS //
			        || status == MenueApiFactory.RESPONSE_STATE_SUCCESS_NO_MYFRIEND//
			        || status == MenueApiFactory.RESPONSE_STATE_SUCCESS_NO_CONTACT) {
				Gson gson = new Gson();
				JSONArray jsonArray = obj.getJSONArray("recommendUsers");
				for (int i = 0; i < jsonArray.length(); i++) {
					if (jsonArray.isNull(i)) {
						continue;
					}
					LinkedList<FriendChat> fromX = gson.fromJson(jsonArray.getJSONArray(i).toString(), new TypeToken<LinkedList<FriendChat>>() {
					}.getType());
					xFriendSuggested.addAll(fromX);
				}
				System.out.println("===" + xFriendSuggested);

				// JSONArray jsonArray2 = obj.getJSONArray("myUsers");
				// for (int i = 0; i < jsonArray2.length(); i++) {
				// if (jsonArray2.isNull(i)) {
				// continue;
				// }
				Object objNull = obj.getString("myUsers");

				if (!"null".equals(objNull)) {

					myFriendsChats = gson.fromJson(obj.getJSONArray("myUsers").toString(), new TypeToken<LinkedList<FriendChat>>() {
					}.getType());
				}
				// }
				mHandler2.post(new Runnable() {

					@SuppressWarnings("unchecked")
					@Override
					public void run() {
						if (myFriendsChats != null && myFriendsChats.size() > 0) {
							if (myFriendsChats.size() == 1) {
								for (FriendChat myFriendsChat : myFriendsChats) {
									String pinying = myFriendsChat.getNick();
									if (pinying != null) {
										myFriendsChat.setLetter(PinyinComparator.getPingYin(pinying).substring(0, 1));
									}
								}
							} else {
								// 按照汉字第一个字母升序排序。
								Collections.sort(myFriendsChats, new PinyinComparator());
							}
						}
						mHandler2.sendMessage(mHandler2.obtainMessage(status));

					}
				});

			} else {
				failure(obj);
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
