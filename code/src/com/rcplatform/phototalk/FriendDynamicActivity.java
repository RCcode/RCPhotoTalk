package com.rcplatform.phototalk;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.gson.Gson;
import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.adapter.FriendDynamicListAdpter;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.logic.controller.InformationPageController;
import com.rcplatform.phototalk.proxy.FriendsProxy;
import com.rcplatform.phototalk.pulltorefresh.library.PullToRefreshBase;
import com.rcplatform.phototalk.pulltorefresh.library.PullToRefreshListView;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.request.Request;
import com.rcplatform.phototalk.request.inf.FriendDetailListener;
import com.rcplatform.phototalk.umeng.EventUtil;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.PrefsUtils;
import com.rcplatform.phototalk.utils.Utils;

public class FriendDynamicActivity extends BaseActivity {

	private PullToRefreshListView friendDynameicList;

	// 好友动态列表使用此adpter
	private FriendDynamicListAdpter adpter;

	private List<FriendDynamic> listDynamic;

	private ImageButton back_btn;

	private TextView titleContent;

	private PopupWindow firendMsgPop;

	private final int GET_FIRST = 0;

	private final int GET_PULLDOWN = 1;

	private final int GET_UPDOWN = 2;

	private final int UPDATE_UI = 3;

	private int pageSize = 1;

	private String time = "0";

	private static final int PAGE_SIZE = 10;

	protected static final int FAIL = 4;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friend_dynamic);
		back_btn = (ImageButton) findViewById(R.id.back);
		back_btn.setVisibility(View.VISIBLE);
		back_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				FriendDynamicActivity.this.finish();
			}
		});
		titleContent = (TextView) findViewById(R.id.titleContent);
		titleContent.setText(R.string.friend_dynamic);
		titleContent.setVisibility(View.VISIBLE);
		friendDynameicList = (PullToRefreshListView) findViewById(R.id.friend_dynamic_list);
		friendDynameicList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase refreshView) {
				// TODO Auto-generated method stub
				getFriendDynamic(1, GET_PULLDOWN);
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase refreshView) {
				// TODO Auto-generated method stub
				pageSize++;
				getFriendDynamic(pageSize, GET_UPDOWN);
			}
		});
		listDynamic = new ArrayList<FriendDynamicActivity.FriendDynamic>();
		adpter = new FriendDynamicListAdpter(FriendDynamicActivity.this, listDynamic);
		friendDynameicList.setAdapter(adpter);
		friendDynameicList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				int index = arg2 - 1;
				if (listDynamic.get(index).getType() == 1) {
					// APP INFO
					Utils.searchAppInGooglePlay(baseContext, FriendDynamicListAdpter.getAppInfos(listDynamic.get(index).getOtherName())[1]);
				} else {
					// USER INFO
					EventUtil.More_Setting.rcpt_friendsupdate_profileview(baseContext);
					toFriend(listDynamic.get(index).getOtherId());
				}
			}
		});
		getFriendDynamic(pageSize, GET_FIRST);
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
	}

	private void getFriendDynamic(final int page, final int type) {
		if (type == GET_PULLDOWN) {
			time = "0";
		} else if (type == GET_FIRST) {
			time = "0";
		}
		FriendsProxy.getMyFriendDynamic(FriendDynamicActivity.this, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				try {
					List<FriendDynamic> list = jsonToFriendDynamic(type, content);
					myHandler.obtainMessage(type, list).sendToTarget();
				} catch (Exception e) {
					myHandler.sendEmptyMessage(FAIL);
				}
			}

			@Override
			public void onFailure(int errorCode, String content) {
				myHandler.sendEmptyMessage(FAIL);
			}
		}, page, PAGE_SIZE, time);
	}

	private Handler myHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			dismissLoadingDialog();
			switch (msg.what) {
			case GET_PULLDOWN:
				List<FriendDynamic> downlist = (List<FriendDynamic>) msg.obj;
				if (downlist != null) {
					downlist.addAll(listDynamic);
					listDynamic.clear();
					listDynamic.addAll(downlist);
				}
				adpter.notifyDataSetChanged();
				friendDynameicList.onRefreshComplete();
				break;

			case GET_FIRST:
				List<FriendDynamic> list = (List<FriendDynamic>) msg.obj;
				if (list != null) {
					listDynamic.addAll(list);
				}
				adpter.notifyDataSetChanged();
				friendDynameicList.onRefreshComplete();
				break;
			case GET_UPDOWN:
				List<FriendDynamic> uplist = (List<FriendDynamic>) msg.obj;
				if (uplist != null) {
					// listDynamic.clear();
					listDynamic.addAll(uplist);
				}
				adpter.notifyDataSetChanged();
				friendDynameicList.onRefreshComplete();
				break;
			case UPDATE_UI:
				InformationPageController.getInstance().onNewTread();
				break;
			case FAIL:
				friendDynameicList.onRefreshComplete();
				break;
			}

		}

	};

	private List<FriendDynamic> jsonToFriendDynamic(int type, String json) throws JSONException {
		JSONObject jsonObject = new JSONObject(json);
		List<FriendDynamic> friends = null;
		JSONArray myFriendsArray = jsonObject.getJSONArray("trends");
		Gson gson = new Gson();
		friends = gson.fromJson(myFriendsArray.toString(), new com.google.gson.reflect.TypeToken<ArrayList<FriendDynamic>>() {
		}.getType());

		if (jsonObject.has("trendId")) {
			int n = jsonObject.getInt("trendId");
			PrefsUtils.User.saveShowedMaxTrendsId(getApplicationContext(), getCurrentUser().getRcId(), n);
			myHandler.obtainMessage(UPDATE_UI).sendToTarget();
		}
		if (jsonObject.has("time")) {
			time = jsonObject.getString("time");
		}
		return friends;
	}

	public Friend FirendDynamicToFriend(FriendDynamic friendDynamic, boolean isOther) {
		Friend friend = new Friend();
		if (isOther) {
			friend.setRcId(friendDynamic.getOtherId());
		} else {
			friend.setRcId(friendDynamic.getfRcId());
		}
		return friend;
	}

	public class FriendDynamic {

		private int type;

		private String trendId;

		private String fRcId;

		private String createTime;

		private String fRcName;

		private String fRcHead;

		private String otherId;

		private String otherName;

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public String getTrendId() {
			return trendId;
		}

		public void setTrendId(String trendId) {
			this.trendId = trendId;
		}

		public String getfRcId() {
			return fRcId;
		}

		public void setfRcId(String fRcId) {
			this.fRcId = fRcId;
		}

		public String getCreateTime() {
			return createTime;
		}

		public void setCreateTime(String createTime) {
			this.createTime = createTime;
		}

		public String getfRcName() {
			return fRcName;
		}

		public void setfRcName(String fRcName) {
			this.fRcName = fRcName;
		}

		public String getfRcHead() {
			return fRcHead;
		}

		public void setfRcHead(String fRcHead) {
			this.fRcHead = fRcHead;
		}

		public String getOtherId() {
			return otherId;
		}

		public void setOtherId(String otherId) {
			this.otherId = otherId;
		}

		public String getOtherName() {
			return otherName;
		}

		public void setOtherName(String otherName) {
			this.otherName = otherName;
		}
	}

	private void toFriend(String rcid) {
		Friend friend = new Friend();
		friend.setRcId(rcid);
		getFriendInfo(friend);
	}

	private void getFriendInfo(Friend friend) {
		Request.executeGetFriendDetailAsync(baseContext, friend, new FriendDetailListener() {

			@Override
			public void onSuccess(Friend friend) {
				dismissLoadingDialog();
				startFriendDetailActivity(friend);
			}

			@Override
			public void onError(int errorCode, String content) {
				dismissLoadingDialog();
				showErrorConfirmDialog(content);
			}
		}, false);
	}

	private void startFriendDetailActivity(Friend friend) {
		Intent intent = new Intent(baseContext, FriendDetailActivity.class);
		intent.putExtra(FriendDetailActivity.PARAM_FRIEND, friend);
		if (!friend.isFriend()) {
			intent.setAction(Constants.Action.ACTION_RECOMMEND_DETAIL);
		} else {
			intent.setAction(Constants.Action.ACTION_FRIEND_DETAIL);
		}
		baseContext.startActivity(intent);
	}
}
