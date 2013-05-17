package com.rcplatform.phototalk;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.adapter.SelectedFriendsGalleryAdapter;
import com.rcplatform.phototalk.adapter.SelectedFriendsListAdapter;
import com.rcplatform.phototalk.adapter.SelectedFriendsListAdapter.OnCheckBoxChangedListener;
import com.rcplatform.phototalk.api.MenueApiFactory;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.SelectFriend;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.logic.LogicUtils;
import com.rcplatform.phototalk.proxy.FriendsProxy;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.DisplayUtil;
import com.rcplatform.phototalk.utils.PinyinComparator;
import com.rcplatform.phototalk.utils.RCPlatformTextUtil;
import com.rcplatform.phototalk.utils.ZipUtil;

public class SelectFriendsActivity extends BaseActivity implements OnClickListener {

	private ListView mFriendListView;

	private Gallery mGallery;

	private Button mButtonSend;

	// density 为1.5的手机上的px
	private int galleryLeftPaddingPx = 20;

	// density 为1.5的手机上的px
	private int gallerySpacePx = 25;

	private int mDisplayableCount;

	private UserInfo mUserInfo;

	private final int MSG_WHAT_ERROR = 100;

	private final int MSG_CACHE_FINISH = 200;

	private final int MSG_SEND_SUCCESS = 300;

	private String tempFilePath;

	private MenueApplication app;

	private String timeLimit;

	private ProgressBar progressBar;

	private TextView mTvContentTitle;

	private ImageButton mBtBack;

	private TextView mBtAddFriend;
	private List<SelectFriend> list;
	private List<SelectFriend> sendData = new ArrayList<SelectFriend>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (MenueApplication) getApplication();
		timeLimit = getIntent().getStringExtra("timeLimit");
		if (timeLimit == null) {
			timeLimit = "10";
		}
		setContentView(R.layout.select_friends_list_view);
		// 缓存要发送的图片
		initViewOrListener();
		getLocalFriends();

	}

	private void getLocalFriends() {
		Thread th = new Thread() {
			public void run() {
				List<Friend> friends = PhotoTalkDatabaseFactory.getDatabase().getFriends();
				List<SelectFriend> seleFriends = new ArrayList<SelectFriend>();
				for (Friend friend : friends) {
					SelectFriend seleFriend = SelectFriend.parseSelectFriend(friend);
					seleFriends.add(seleFriend);
				}
				mHandler.obtainMessage(MSG_CACHE_FINISH, seleFriends).sendToTarget();
				getFriends();
			};
		};
		th.start();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		progressBar.setVisibility(View.VISIBLE);
	}

	private void initViewOrListener() {
		mFriendListView = (ListView) findViewById(R.id.lv_sfl_friends);
		mGallery = (Gallery) findViewById(R.id.g_sfl_added_friends);
		mButtonSend = (Button) findViewById(R.id.btn_sfl_send);
		progressBar = (ProgressBar) findViewById(R.id.pb_select_friend);
		mTvContentTitle = (TextView) findViewById(R.id.titleContent);

		mBtBack = (ImageButton) findViewById(R.id.back);
		mGallery.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
				list.get(sendData.get(position).getPosition()).setIsChosed(false);
				((SelectedFriendsListAdapter) mFriendListView.getAdapter()).notifyDataSetChanged();
				sendData.remove(position);
				((SelectedFriendsGalleryAdapter) mGallery.getAdapter()).notifyDataSetChanged();
				// 修改
				mGallery.setNextFocusRightId(mGallery.getNextFocusLeftId());
			}
		});
		SelectedFriendsGalleryAdapter adapter = new SelectedFriendsGalleryAdapter(this, sendData);
		mGallery.setAdapter(adapter);
		alignGalleryToLeft(mGallery);

		mButtonSend.setOnClickListener(this);
		mTvContentTitle.setVisibility(View.VISIBLE);
		mTvContentTitle.setText(R.string.select_friend_title);

		mBtBack.setVisibility(View.VISIBLE);
		mBtBack.setBackgroundResource(R.drawable.base_back_arrow);
		mBtBack.setOnClickListener(this);

		mBtAddFriend = (TextView) findViewById(R.id.choosebutton);
		mBtAddFriend.setVisibility(View.GONE);
		mBtAddFriend.setBackgroundResource(R.drawable.select_add);
		mBtAddFriend.setOnClickListener(this);
	}

	private void catchBitampOnSDC() {
		// 创建一个临时的隐藏文件夹
		try {
			tempFilePath = app.getSendZipFileCachePath() +"/"+System.currentTimeMillis()+ ".zip";
			ZipUtil.ZipFolder(app.getSendFileCachePath(), tempFilePath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		File file = new File(tempFilePath);
		if (file.exists()) {
			sendPicture(tempFilePath, timeLimit, sendData);
		} else {
			sendStringMessage(MSG_WHAT_ERROR, getString(R.string.receive_data_error));
		}

	}

	private void initFriendListAdapter(List<SelectFriend> list) {
		SelectedFriendsListAdapter adapter = new SelectedFriendsListAdapter(SelectFriendsActivity.this, list);
		mFriendListView.setAdapter(adapter);
		adapter.setOnCheckBoxChangedListener(new OnCheckBoxChangedListener() {

			@Override
			public void onChange(SelectFriend friend, boolean isChecked, int index) {
				SelectFriend myFriend = (SelectFriend) friend;
				myFriend.setChoseposition(index);
				if (isChecked) {
					if (!sendData.contains(friend)) {
						sendData.add(myFriend);
					}
				} else {
					sendData.remove(myFriend);
				}
				((SelectedFriendsGalleryAdapter) mGallery.getAdapter()).notifyDataSetChanged();
				if (sendData.size() > mDisplayableCount)
					mGallery.setSelection(sendData.size() - mDisplayableCount);
				else {
					mGallery.setSelection(0);
				}
			}
		});
	}

	private List<SelectFriend> jsonToFriends(String json) throws JSONException {
		JSONObject jsonObject = new JSONObject(json);
		if (isRequestStatusOK(jsonObject)) {
			JSONArray myFriendsArray = jsonObject.getJSONArray("myUsers");
			Gson gson = new Gson();
			List<SelectFriend> friends = gson.fromJson(myFriendsArray.toString(), new com.google.gson.reflect.TypeToken<ArrayList<SelectFriend>>() {
			}.getType());
			List<Friend> friendsCache = new ArrayList<Friend>();
			for (Friend friend : friends) {
				friend.setLetter(RCPlatformTextUtil.getLetter(friend.getNickName()));
				friend.setFriend(true);
				friendsCache.add(friend);
			}
			PhotoTalkDatabaseFactory.getDatabase().saveFriends(friendsCache);
			SelectFriend user = new SelectFriend();
			user.setNickName(app.getCurrentUser().getNickName());
			user.setRcId(app.getCurrentUser().getRcId());
			user.setHeadUrl(app.getCurrentUser().getHeadUrl());
			friends.add(user);

			TreeSet<SelectFriend> fs = new TreeSet<SelectFriend>(new PinyinComparator());
			fs.addAll(friends);
			friends.clear();
			friends.addAll(fs);
			fs.clear();
			return friends;
		} else {
			sendStringMessage(MSG_WHAT_ERROR, jsonObject.getString(MenueApiFactory.RESPONSE_KEY_MESSAGE));
			return null;
		}
	}

	private void sendStringMessage(int what, String content) {
		Message msg = mHandler.obtainMessage();
		msg.what = what;
		msg.obj = content;
		mHandler.sendMessage(msg);
	}

	private final Handler mHandler = new Handler() {

		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_WHAT_ERROR:
				progressBar.setVisibility(View.GONE);
				DialogUtil.showToast(getApplicationContext(), (String) msg.obj, Toast.LENGTH_SHORT);
				break;

			case MSG_CACHE_FINISH:
				list = (List<SelectFriend>) msg.obj;
				initFriendListAdapter(list);
				progressBar.setVisibility(View.GONE);
				break;
			case MSG_SEND_SUCCESS:
				// Intent intent = new Intent(SelectFriendsActivity.this,
				// HomeActivity.class);
				// intent.putExtra("from", this.getClass().getName());
				// intent.putExtra("time", timeSnap);
				// startActivity(intent);
				break;
			}
		};
	};

	private long timeSnap;

	public void deleteTemp() {
		MenueApplication app = (MenueApplication) getApplication();
		String tempFilePath = app.getSendFileCachePath();
		File tempPic = new File(tempFilePath);
		deleteFile(tempPic);
	}

	public void deleteFile(File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File file2 : files) {
				deleteFile(file2);
			}
		} else {
			System.out.println("select----->" + file.getPath());
			file.delete();
		}
	}

	private void sendPicture(String imagePath, final String timeLimit, final List<SelectFriend> friends) {
		timeSnap = System.currentTimeMillis();
		final File file = new File(imagePath);
		List<Friend> fs = new ArrayList<Friend>();
		fs.addAll(friends);
		LogicUtils.sendPhoto(this, timeLimit, fs, file);
		// FriendsProxy.postZip(
		// SelectFriendsActivity.this,
		// file,
		// new RCPlatformResponseHandler() {
		//
		// @Override
		// public void onSuccess(int statusCode, String content) {
		// // TODO Auto-generated method stub
		// deleteTemp();
		// mHandler.obtainMessage(MSG_SEND_SUCCESS).sendToTarget();
		// }
		//
		// @Override
		// public void onFailure(int errorCode, String content) {
		// // TODO Auto-generated method stub
		// sendStringMessage(MSG_WHAT_ERROR,
		// getString(R.string.net_error));
		// }
		// },
		// String.valueOf(timeSnap), desc, timeLimit,
		// buildUserArray(friends, timeSnap));

	}

	private boolean isRequestStatusOK(JSONObject jsonObject) throws JSONException {
		return jsonObject.getInt(MenueApiFactory.RESPONSE_KEY_STATUS) == MenueApiFactory.RESPONSE_STATE_SUCCESS;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_sfl_send:
			if (sendData == null || sendData.size() <= 0) {
				Toast.makeText(SelectFriendsActivity.this, R.string.please_select_contact, 1).show();
				return;
			} else {
				catchBitampOnSDC();
				Intent intent = new Intent(SelectFriendsActivity.this, HomeActivity.class);
				intent.putExtra("from", this.getClass().getName());
				intent.putExtra("time", timeSnap);
				startActivity(intent);
			}
			break;
		case R.id.back:
			app.deleteSendFileCache(tempFilePath);
			finish();
			break;

		case R.id.choosebutton:
			startActivity(new Intent(SelectFriendsActivity.this, AddFriendActivity.class));
			break;
		}
	}

	private void alignGalleryToLeft(Gallery gallery) {
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		measureView(mButtonSend, params);

		int rightMargin = ((MarginLayoutParams) mButtonSend.getLayoutParams()).rightMargin;
		int leftMargin = ((MarginLayoutParams) mButtonSend.getLayoutParams()).leftMargin;

		int gallerLeftPaddingDip = DisplayUtil.px2dip(galleryLeftPaddingPx, 1.5f);
		int gallerySpaceDip = DisplayUtil.px2dip(gallerySpacePx, 1.5f);

		galleryLeftPaddingPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, gallerLeftPaddingDip, metrics);
		gallerySpacePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, gallerySpaceDip, metrics);

		mGallery.setSpacing(gallerySpacePx);
		int w = getResources().getDisplayMetrics().widthPixels - mButtonSend.getMeasuredWidth() - rightMargin - leftMargin - galleryLeftPaddingPx;

		View itemView = LayoutInflater.from(this).inflate(R.layout.selected_friends_galleryt_item, null);
		measureView(itemView, params);

		int itemWidth = itemView.getMeasuredWidth();

		mDisplayableCount = (w) / (itemWidth + gallerySpacePx);

		MarginLayoutParams layoutParams = (MarginLayoutParams) mGallery.getLayoutParams();
		layoutParams.setMargins(-(w - itemWidth), layoutParams.topMargin, layoutParams.rightMargin, layoutParams.bottomMargin);
	}

	public void measureView(View child, ViewGroup.LayoutParams params) {
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if (p == null) {
			p = params;
		}
		int width = p.width;
		if (width > 0) {
			width = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
		} else {
			width = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		}

		child.measure(width, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			//
			if (tempFilePath != null) {
				app.deleteSendFileCache(tempFilePath);
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private void getFriends() {
		FriendsProxy.getMyFriendlist(SelectFriendsActivity.this, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				try {
					List<SelectFriend> friends = jsonToFriends(content);
					if (friends != null && friends.size() > 0) {
						mHandler.obtainMessage(MSG_CACHE_FINISH, friends).sendToTarget();
					}
				} catch (JSONException e) {
					e.printStackTrace();
//					sendStringMessage(MSG_WHAT_ERROR, getString(R.string.receive_data_error));
				}
			}

			@Override
			public void onFailure(int errorCode, String content) {
				sendStringMessage(MSG_WHAT_ERROR, getString(R.string.net_error));
			}
		});

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
