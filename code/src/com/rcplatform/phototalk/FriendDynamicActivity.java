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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
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
import com.rcplatform.phototalk.proxy.FriendsProxy;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.request.Request;
import com.rcplatform.phototalk.request.inf.FriendDetailListener;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.PinyinComparator;
import com.rcplatform.phototalk.utils.RCPlatformTextUtil;
import com.rcplatform.phototalk.views.HeadImageView;

public class FriendDynamicActivity extends BaseActivity {
	private ListView friendDynameicList;
	// 好友动态列表使用此adpter
	private FriendDynamicListAdpter adpter;
	private ImageButton back_btn;
	private TextView titleContent;
	private PopupWindow firendMsgPop;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friend_dynamic);
		back_btn = (ImageButton) findViewById(R.id.back);
		back_btn.setVisibility(View.VISIBLE);
		titleContent = (TextView) findViewById(R.id.titleContent);
		titleContent.setText(R.string.friend_dynamic);
		titleContent.setVisibility(View.VISIBLE);
		friendDynameicList = (ListView) findViewById(R.id.friend_dynamic_list);
		
		getFriendDynamic();
		friendDynameicList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
//				弹出popup 显示好友信息缺少添加参数 和设置信息
//				showPop()
			}
		});

	}

	public void showPop(View view) {
		if (firendMsgPop != null && firendMsgPop.isShowing()) {
			firendMsgPop.dismiss();
		}

		View detailsView = LayoutInflater.from(this).inflate(
				R.layout.friend_dynamic_pop, null, false);
		firendMsgPop = new PopupWindow(detailsView, getWindow()
				.getWindowManager().getDefaultDisplay().getWidth(),
				((Activity) this).getWindow().getWindowManager()
						.getDefaultDisplay().getHeight());
		firendMsgPop.setFocusable(true);
		firendMsgPop.setOutsideTouchable(true);
		HeadImageView headView = (HeadImageView) findViewById(R.id.friend_head);
		// 昵称
		TextView friend_nick = (TextView) detailsView
				.findViewById(R.id.friend_nick);
		// Rc Id
		TextView friend_rc_id = (TextView) detailsView
				.findViewById(R.id.friend_rc_id);

		Button friend_photo = (Button) detailsView
				.findViewById(R.id.friend_photo);
		friend_photo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (firendMsgPop.isShowing()) {
					firendMsgPop.dismiss();
				}
			}
		});
		Button add_or_send = (Button) detailsView
				.findViewById(R.id.add_or_send);
		add_or_send.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (firendMsgPop.isShowing()) {
					firendMsgPop.dismiss();
				}
			}
		});
		firendMsgPop.showAtLocation(view, Gravity.BOTTOM, 0, 0);
	}
	
	private void getFriendDynamic() {
		FriendsProxy.getMyFriendDynamic(FriendDynamicActivity.this, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				
				try {
					List<FriendDynamic> list = jsonToFriendDynamic(content);
				} catch (Exception e) {
					// TODO: handle exception
				}
			}

			@Override
			public void onFailure(int errorCode, String content) {
			}
		},0);
		}
	
	private boolean isRequestStatusOK(JSONObject jsonObject) throws JSONException {
		return jsonObject.getInt(PhotoTalkApiFactory.RESPONSE_KEY_STATUS) == PhotoTalkApiFactory.RESPONSE_STATE_SUCCESS;
	}
	
	private List<FriendDynamic> jsonToFriendDynamic(String json) throws JSONException {
		JSONObject jsonObject = new JSONObject(json);
		List<FriendDynamic> friends = null;
		if (isRequestStatusOK(jsonObject)) {
			JSONArray myFriendsArray = jsonObject.getJSONArray("trends");
			Gson gson = new Gson();
			friends = gson.fromJson(myFriendsArray.toString(), new com.google.gson.reflect.TypeToken<ArrayList<FriendDynamic>>() {
			}.getType());
			
			
//			List<Friend> friendsCache = new ArrayList<Friend>();
//			for (Friend friend : friends) {
//				friend.setLetter(RCPlatformTextUtil.getLetter(friend.getNickName()));
//				friend.setFriend(true);
//				friendsCache.add(friend);
//			}

//			TreeSet<SelectFriend> fs = new TreeSet<SelectFriend>(new PinyinComparator());
//			fs.addAll(friends);
//			friends.clear();
//			friends.addAll(fs);
//			fs.clear();
		}
			return friends;
		}
	public Friend FirendDynamicToFriend(FriendDynamic friendDynamic,boolean isOther){
		Friend friend = new Friend();
		if(isOther){
			friend.setRcId(friendDynamic.getOtherId());
		}else{
			friend.setRcId(friendDynamic.getfRcId());
		}
		return friend;
	}
	
	public class FriendDynamic{
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
	private void startFriendDetailActivity(Friend friend) {
		Intent intent = new Intent(this, FriendDetailActivity.class);
		intent.putExtra(FriendDetailActivity.PARAM_FRIEND, friend);
		if (!friend.isFriend()) {
			intent.setAction(Constants.Action.ACTION_RECOMMEND_DETAIL);
		} else {
			intent.setAction(Constants.Action.ACTION_FRIEND_DETAIL);
		}
		startActivity(intent);
	}
	
	private void getFriendInfo(Friend friend) {
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		Request.executeGetFriendDetailAsync(this, friend, new FriendDetailListener() {

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
	}
