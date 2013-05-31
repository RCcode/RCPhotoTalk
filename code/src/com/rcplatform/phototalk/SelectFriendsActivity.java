package com.rcplatform.phototalk;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.adapter.SelectedFriendsGalleryAdapter;
import com.rcplatform.phototalk.adapter.SelectedFriendsListAdapter;
import com.rcplatform.phototalk.adapter.SelectedFriendsListAdapter.OnCheckBoxChangedListener;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.logic.LogicUtils;
import com.rcplatform.phototalk.proxy.FriendsProxy;
import com.rcplatform.phototalk.request.JSONConver;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.DisplayUtil;
import com.rcplatform.phototalk.utils.RCPlatformTextUtil;
import com.rcplatform.phototalk.utils.ZipUtil;
import com.rcplatform.phototalk.views.HorizontalListViewOne;

public class SelectFriendsActivity extends BaseActivity implements
		OnClickListener {

	private ListView mFriendListView;

	private HorizontalListViewOne mGallery;

	private Button mButtonSend;

	// density 为1.5的手机上的px
	private int galleryLeftPaddingPx = 20;

	// density 为1.5的手机上的px
	private int gallerySpacePx = 25;

	private final int MSG_WHAT_ERROR = 100;

	private final int MSG_CACHE_FINISH = 200;

	private final int MSG_CHANGE = 300;

	private final int MSG_GALLERY = 400;

	private String tempFilePath;

	private PhotoTalkApplication app;

	private String timeLimit;

	private ProgressBar progressBar;

	private TextView mTvContentTitle;
	
	private ImageButton mBtBack;

	private TextView mBtAddFriend;
	private RelativeLayout send_layout;
	private List<Friend> listData;
	private List<Friend> resultData = new ArrayList<Friend>();
	private List<Friend> sendData = new ArrayList<Friend>();
	private Button seach_delete_btn;
	private EditText etSearch;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (PhotoTalkApplication) getApplication();
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
				List<Friend> friends = PhotoTalkDatabaseFactory.getDatabase()
						.getFriends();
				if (friends.size() > 1) {
					mHandler.obtainMessage(MSG_CACHE_FINISH, friends)
							.sendToTarget();
				}
				getFriends();
			};
		};
		th.start();
	}

	@SuppressWarnings("deprecation")
	private void initViewOrListener() {
		mFriendListView = (ListView) findViewById(R.id.lv_sfl_friends);
		send_layout = (RelativeLayout) findViewById(R.id.send_layout);
		mGallery = (HorizontalListViewOne) findViewById(R.id.g_sfl_added_friends);
		mButtonSend = (Button) findViewById(R.id.btn_sfl_send);
		etSearch = (EditText) findViewById(R.id.et_search);
		seach_delete_btn = (Button) findViewById(R.id.seach_delete_btn);
		seach_delete_btn.setVisibility(View.GONE);
		seach_delete_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				etSearch.setText("");
				etSearch.setFocusable(true);
				seach_delete_btn.setVisibility(View.GONE);
			}
		});
		etSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				String keyWords = s.toString().trim();
				if (TextUtils.isEmpty(keyWords)) {
					seach_delete_btn.setVisibility(View.GONE);
					listData.clear();
					listData.addAll(resultData);
				} else {
					search(keyWords);
					seach_delete_btn.setVisibility(View.VISIBLE);
				}
				mHandler.obtainMessage(MSG_CHANGE).sendToTarget();
			}
		});
		progressBar = (ProgressBar) findViewById(R.id.pb_select_friend);
		mTvContentTitle = (TextView) findViewById(R.id.titleContent);
		mTvContentTitle.setFocusable(true);
		mTvContentTitle.setFocusableInTouchMode(true);
		mBtBack = (ImageButton) findViewById(R.id.back);
		mGallery.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {
				// Friend friend = sendData.get(position);
				sendData.remove(position);
				if (sendData.size() == 0) {
					send_layout.setVisibility(View.GONE);
				} else {
					mHandler.obtainMessage(MSG_CHANGE).sendToTarget();
					// 将gallery显示最左面的内容 现在修改失败
					// mHandler.obtainMessage(MSG_GALLERY).sendToTarget();
					// mGallery.setFocusable(false);
					// mGallery.setSelection(0);
				}
			}
		});
		SelectedFriendsGalleryAdapter adapter = new SelectedFriendsGalleryAdapter(
				this, sendData);
		mGallery.setAdapter(adapter);
//		alignGalleryToLeft(mGallery);
		send_layout.setVisibility(View.GONE);
		mButtonSend.setOnClickListener(this);
		mTvContentTitle.setVisibility(View.VISIBLE);
		mTvContentTitle.setText(R.string.select_friend_title);

		mBtBack.setVisibility(View.VISIBLE);
		mBtBack.setOnClickListener(this);

		mBtAddFriend = (TextView) findViewById(R.id.choosebutton);
		mBtAddFriend.setVisibility(View.GONE);
		mBtAddFriend.setBackgroundResource(R.drawable.select_add);
		mBtAddFriend.setOnClickListener(this);

	}

	private void search(String keyWords) {
		if (keyWords != null) {
			keyWords = keyWords.toLowerCase();
		}
		List<Friend> resultRecommends = new ArrayList<Friend>();
		for (Friend friend : resultData) {
			if (friend.getNickName() != null
					&& friend.getNickName().toLowerCase().contains(keyWords)) {
				resultRecommends.add(friend);
			}
		}
		listData.clear();
		listData.addAll(resultRecommends);
	}

	private void setResultData() {
		resultData.clear();
		resultData.addAll(listData);
	}

	private void catchBitampOnSDC() {
		// 创建一个临时的隐藏文件夹
		try {
			tempFilePath = app.getSendZipFileCachePath() + "/"
					+ System.currentTimeMillis() + ".zip";
			ZipUtil.ZipFolder(app.getSendFileCachePath(), tempFilePath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		File file = new File(tempFilePath);
		if (file.exists()) {
			// 删除 录音和照片 zip 压缩包不删除
			deleteTemp();
			sendPicture(tempFilePath, timeLimit, sendData);
		} else {
			sendStringMessage(MSG_WHAT_ERROR,
					getString(R.string.receive_data_error));
		}

	}

	private void setAdapterDataSetChanged() {
//		mGallery.setSelection(0);
		int n = sendData.size();
//		gridview_layout.setLayoutParams(new LayoutParams(n*130, 130));
		((SelectedFriendsGalleryAdapter) mGallery.getAdapter())
				.notifyDataSetChanged();
		((SelectedFriendsListAdapter) mFriendListView.getAdapter())
				.notifyDataSetChanged();
	}

	private void initFriendListAdapter(List<Friend> list) {
		SelectedFriendsListAdapter adapter = new SelectedFriendsListAdapter(
				SelectFriendsActivity.this, listData, sendData);
		mFriendListView.setAdapter(adapter);
		adapter.setOnCheckBoxChangedListener(new OnCheckBoxChangedListener() {

			@Override
			public void onChange(Friend friend, boolean isChecked) {

				if (isChecked) {
					if (!sendData.contains(friend)) {
						sendData.add(friend);
					}
					send_layout.setVisibility(View.VISIBLE);
				} else {
					if (sendData.contains(friend)) {
						sendData.remove(friend);
					}
					if (sendData.size() <= 0) {
						send_layout.setVisibility(View.GONE);
					}
				}
				mHandler.obtainMessage(MSG_CHANGE).sendToTarget();
			}
		});
	}

	private void jsonToFriends(final String json) throws JSONException {
		Thread thread = new Thread() {
			public void run() {
				try {
					JSONObject jsonObject = new JSONObject(json);
					JSONArray myFriendsArray = jsonObject
							.getJSONArray("myUsers");
					List<Friend> friends = JSONConver
							.jsonToFriends(myFriendsArray.toString());
					for (Friend friend : friends) {
						friend.setLetter(RCPlatformTextUtil.getLetter(friend
								.getNickName()));
						friend.setFriend(true);
					}
					PhotoTalkDatabaseFactory.getDatabase().saveFriends(friends);
					if (mFriendListView.getAdapter() == null) {
						List<Friend> localFriends = PhotoTalkDatabaseFactory
								.getDatabase().getFriends();
						mHandler.obtainMessage(MSG_CACHE_FINISH, localFriends)
								.sendToTarget();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		};
		thread.start();

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
				DialogUtil.showToast(getApplicationContext(), (String) msg.obj,
						Toast.LENGTH_SHORT);
				break;

			case MSG_CACHE_FINISH:
				listData = (List<Friend>) msg.obj;
				setResultData();
				initFriendListAdapter(listData);
				progressBar.setVisibility(View.GONE);
				break;
			case MSG_CHANGE:
				setAdapterDataSetChanged();
				break;
			case MSG_GALLERY:
				mGallery.setFocusable(false);
				mGallery.setSelection(0);
				break;
			}
		};
	};

	private long timeSnap;

	private void sendPicture(String imagePath, final String timeLimit,
			final List<Friend> friends) {
		timeSnap = System.currentTimeMillis();
		final File file = new File(imagePath);
		LogicUtils.sendPhoto(this, timeLimit, friends, file);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_sfl_send:
			if (sendData == null || sendData.size() <= 0) {
				Toast.makeText(SelectFriendsActivity.this,
						R.string.please_select_contact, 1).show();
				return;
			} else {
				catchBitampOnSDC();
				Intent intent = new Intent(SelectFriendsActivity.this,
						HomeActivity.class);
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
			startActivity(new Intent(SelectFriendsActivity.this,
					AddFriendsActivity.class));
			break;
		}
	}

	private void alignGalleryToLeft(Gallery gallery) {
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		measureView(mButtonSend, params);

		int rightMargin = ((MarginLayoutParams) mButtonSend.getLayoutParams()).rightMargin;
		int leftMargin = ((MarginLayoutParams) mButtonSend.getLayoutParams()).leftMargin;

		int gallerLeftPaddingDip = DisplayUtil.px2dip(galleryLeftPaddingPx,
				1.5f);
		int gallerySpaceDip = DisplayUtil.px2dip(gallerySpacePx, 1.5f);

		galleryLeftPaddingPx = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, gallerLeftPaddingDip, metrics);
		gallerySpacePx = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, gallerySpaceDip, metrics);
//		mGallery.setSpacing(0);
		int w = getResources().getDisplayMetrics().widthPixels
				- mButtonSend.getMeasuredWidth() - rightMargin - leftMargin
				- galleryLeftPaddingPx;

		View itemView = LayoutInflater.from(this).inflate(
				R.layout.selected_friends_galleryt_item, null);
		measureView(itemView, params);

		int itemWidth = itemView.getMeasuredWidth();

		MarginLayoutParams layoutParams = (MarginLayoutParams) mGallery
				.getLayoutParams();
		layoutParams.setMargins(-(w - itemWidth), layoutParams.topMargin,
				layoutParams.rightMargin, layoutParams.bottomMargin);
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

		child.measure(width,
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
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
		// progressBar.setVisibility(View.VISIBLE);
		FriendsProxy.getMyFriendlist(SelectFriendsActivity.this,
				new RCPlatformResponseHandler() {

					@Override
					public void onSuccess(int statusCode, String content) {
						try {
							jsonToFriends(content);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}

					@Override
					public void onFailure(int errorCode, String content) {
						sendStringMessage(MSG_WHAT_ERROR,
								getString(R.string.net_error));
					}
				});

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
