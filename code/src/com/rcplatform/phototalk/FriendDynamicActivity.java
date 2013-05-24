package com.rcplatform.phototalk;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.gson.Gson;
import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.adapter.FriendDynamicListAdpter;
import com.rcplatform.phototalk.api.PhotoTalkApiFactory;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.SelectFriend;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.logic.controller.InformationPageController;
import com.rcplatform.phototalk.proxy.FriendsProxy;
import com.rcplatform.phototalk.pulltorefresh.library.PullToRefreshBase;
import com.rcplatform.phototalk.pulltorefresh.library.PullToRefreshListView;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.request.Request;
import com.rcplatform.phototalk.request.inf.FriendDetailListener;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.PinyinComparator;
import com.rcplatform.phototalk.utils.PrefsUtils;
import com.rcplatform.phototalk.utils.RCPlatformTextUtil;
import com.rcplatform.phototalk.views.HeadImageView;

public class FriendDynamicActivity extends BaseActivity {
	private PullToRefreshListView friendDynameicList;
	// 好友动态列表使用此adpter
	private FriendDynamicListAdpter adpter;
	private List<FriendDynamic> listDynamic;
	private ImageButton back_btn;
	private TextView titleContent;
	private PopupWindow firendMsgPop;
	private final int GET_PULLDOWN = 1;
	private final int GET_UPDOWN = 2;
	private final int UPDATE_UI = 3;
	private int pageSize = 1;
	private String time = "0";

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
		friendDynameicList
				.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

					@Override
					public void onPullDownToRefresh(
							PullToRefreshBase refreshView) {
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
		adpter = new FriendDynamicListAdpter(FriendDynamicActivity.this,
				listDynamic);
		friendDynameicList.setAdapter(adpter);

		getFriendDynamic(pageSize, GET_PULLDOWN);

	}

	private void getFriendDynamic(final int page, final int type) {
		if (type == GET_PULLDOWN) {
			time = "0";
		}
		FriendsProxy.getMyFriendDynamic(
				FriendDynamicActivity.this,
				new RCPlatformResponseHandler() {
					@Override
					public void onSuccess(int statusCode, String content) {
						try {
							List<FriendDynamic> list = jsonToFriendDynamic(content);
							myHandler.obtainMessage(type, list).sendToTarget();
						} catch (Exception e) {
							// TODO: handle exception
						}
					}

					@Override
					public void onFailure(int errorCode, String content) {
					}
				}, PrefsUtils.User.getShowedMaxTrendsId(
						getApplicationContext(), getCurrentUser().getRcId()),
				page, 10, time);
	}

	private Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case GET_PULLDOWN:
				List<FriendDynamic> downlist = (List<FriendDynamic>) msg.obj;
				if (downlist != null) {
					listDynamic.addAll(downlist);
				}
				friendDynameicList.onRefreshComplete();
				adpter.notifyDataSetChanged();
				break;
			case GET_UPDOWN:
				List<FriendDynamic> uplist = (List<FriendDynamic>) msg.obj;
				if (uplist != null) {
					listDynamic.clear();
					listDynamic.addAll(uplist);
				}
				friendDynameicList.onRefreshComplete();
				adpter.notifyDataSetChanged();
				break;
			case UPDATE_UI:
				InformationPageController.getInstance().onNewTread();
				break;
			}

		}

	};

	private List<FriendDynamic> jsonToFriendDynamic(String json)
			throws JSONException {
		JSONObject jsonObject = new JSONObject(json);
		List<FriendDynamic> friends = null;
		JSONArray myFriendsArray = jsonObject.getJSONArray("trends");
		Gson gson = new Gson();
		friends = gson
				.fromJson(
						myFriendsArray.toString(),
						new com.google.gson.reflect.TypeToken<ArrayList<FriendDynamic>>() {
						}.getType());

		if (jsonObject.has("trendId")) {
			int n = jsonObject.getInt("trendId");
			PrefsUtils.User.saveShowedMaxTrendsId(getApplicationContext(),
					getCurrentUser().getRcId(), n);
			myHandler.obtainMessage(UPDATE_UI).sendToTarget();
		}
		if (jsonObject.has("time")) {
			time = jsonObject.getString("time");
		}
		return friends;
	}

	public Friend FirendDynamicToFriend(FriendDynamic friendDynamic,
			boolean isOther) {
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
}
