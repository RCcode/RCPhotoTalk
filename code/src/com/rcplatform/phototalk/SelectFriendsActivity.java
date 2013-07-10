package com.rcplatform.phototalk;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.rcplatform.phototalk.logic.LogicUtils;
import com.rcplatform.phototalk.proxy.FriendsProxy;
import com.rcplatform.phototalk.request.inf.LoadFriendsListener;
import com.rcplatform.phototalk.umeng.EventUtil;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.PhotoTalkUtils;
import com.rcplatform.phototalk.utils.PrefsUtils;
import com.rcplatform.phototalk.utils.ZipUtil;
import com.rcplatform.phototalk.views.HorizontalListView;

public class SelectFriendsActivity extends BaseActivity implements OnClickListener {

	public static final String PARAM_KEY_HASVOICE = "hasvoice";
	public static final String PARAM_KEY_HASGRAF = "hasgraf";

	private ListView mFriendListView;

	private HorizontalListView mGallery;

	private Button mButtonSend;

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

	private RelativeLayout send_layout;
	private List<Friend> listData;
	private List<Friend> resultData = new ArrayList<Friend>();
	private List<Friend> sendData = new ArrayList<Friend>();
	private Button seach_delete_btn;
	private EditText etSearch;
	private boolean hasVoice = false;
	private boolean hasGraf = false;
	private CheckBox cbStrange;
	private boolean sendToStrange = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (PhotoTalkApplication) getApplication();
		timeLimit = getIntent().getStringExtra("timeLimit");
		if (timeLimit == null) {
			timeLimit = "10";
		}
		hasVoice = getIntent().getBooleanExtra(PARAM_KEY_HASVOICE, false);
		hasGraf = getIntent().getBooleanExtra(PARAM_KEY_HASGRAF, false);
		setContentView(R.layout.select_friends_list_view);
		// 缓存要发送的图片
		initViewOrListener();
		getFriends();

	}

	private void getFriends() {
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		FriendsProxy.getFriends(this, new LoadFriendsListener() {

			@Override
			public void onLoadedFail(String reason) {
				dismissLoadingDialog();
				if (!PrefsUtils.User.hasLoadedFriends(SelectFriendsActivity.this, getCurrentUser().getRcId())) {
					showErrorConfirmDialog(reason);
				}
			}

			@Override
			public void onFriendsLoaded(List<Friend> friends, List<Friend> recommends) {
				mHandler.obtainMessage(MSG_CACHE_FINISH, friends).sendToTarget();
				dismissLoadingDialog();
			}
		});
	}

	private void initViewOrListener() {
		mFriendListView = (ListView) findViewById(R.id.lv_sfl_friends);
		send_layout = (RelativeLayout) findViewById(R.id.send_layout);
		mGallery = (HorizontalListView) findViewById(R.id.g_sfl_added_friends);
		mButtonSend = (Button) findViewById(R.id.btn_sfl_send);
		progressBar = (ProgressBar) findViewById(R.id.pb_select_friend);
		mTvContentTitle = (TextView) findViewById(R.id.titleContent);

		mBtBack = (ImageButton) findViewById(R.id.back);
		mGallery.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
				// Friend friend = sendData.get(position);
				sendData.remove(position);
				if (sendData.size() == 0) {
					send_layout.setVisibility(View.GONE);
				} else {
					setAdapterDataSetChanged();
				}
			}
		});
		SelectedFriendsGalleryAdapter adapter = new SelectedFriendsGalleryAdapter(this, sendData);
		mGallery.setAdapter(adapter);

		send_layout.setVisibility(View.GONE);
		mButtonSend.setOnClickListener(this);
		mTvContentTitle.setVisibility(View.VISIBLE);
		mTvContentTitle.setText(R.string.select_friend_title);
		mTvContentTitle.setFocusable(true);
		mTvContentTitle.setFocusableInTouchMode(true);

		mBtBack.setVisibility(View.VISIBLE);
		mBtBack.setOnClickListener(this);
		// View view=findViewById(R.id.choosebutton);
		// view.setBackgroundResource(R.drawable.select_add);
		// view.setOnClickListener(this);
		// view.setVisibility(View.VISIBLE);
		etSearch = (EditText) findViewById(R.id.et_search);
		seach_delete_btn = (Button) findViewById(R.id.seach_delete_btn);
		seach_delete_btn.setVisibility(View.GONE);
		seach_delete_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				etSearch.setText("");
				etSearch.setFocusable(true);
				seach_delete_btn.setVisibility(View.GONE);
			}
		});
		etSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
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
		cbStrange = (CheckBox) findViewById(R.id.cb_strange);
		cbStrange.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				sendToStrange = isChecked;
			}
		});
	}

	private void search(String keyWords) {
		if (keyWords != null) {
			keyWords = keyWords.toLowerCase();
		}
		List<Friend> resultRecommends = new ArrayList<Friend>();
		for (Friend friend : resultData) {
			if (friend.getNickName() != null && friend.getNickName().toLowerCase().contains(keyWords)) {
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
			tempFilePath = app.getSendZipFileCachePath() + "/" + System.currentTimeMillis() + ".zip";
			ZipUtil.ZipFolder(app.getSendFileCachePath(), tempFilePath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		File file = new File(tempFilePath);
		if (file.exists()) {
			// 删除 录音和照片 zip 压缩包不删除
			deleteTemp();
			sendPicture(tempFilePath, timeLimit, sendData, hasVoice);
		} else {
			sendStringMessage(MSG_WHAT_ERROR, getString(R.string.receive_data_error));
		}

	}

	private void setAdapterDataSetChanged() {
		((SelectedFriendsGalleryAdapter) mGallery.getAdapter()).notifyDataSetChanged();
		((SelectedFriendsListAdapter) mFriendListView.getAdapter()).notifyDataSetChanged();
	}

	private void initFriendListAdapter(List<Friend> list) {
		SelectedFriendsListAdapter adapter = new SelectedFriendsListAdapter(SelectFriendsActivity.this, listData, sendData);
		mFriendListView.setAdapter(adapter);
		adapter.setOnCheckBoxChangedListener(new OnCheckBoxChangedListener() {

			@Override
			public void onChange(Friend friend, boolean isChecked) {

				if (isChecked) {
					EventUtil.Main_Photo.rcpt_choosefriends(baseContext);
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
				setAdapterDataSetChanged();
			}
		});
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

	private void sendPicture(String imagePath, final String timeLimit, final List<Friend> friends, boolean hasVoice) {
		timeSnap = System.currentTimeMillis();
		final File file = new File(imagePath);
		if (sendToStrange) {
			friends.add(PhotoTalkUtils.getDriftFriend());
		}
		LogicUtils.sendPhoto(this, timeLimit, friends, file, hasVoice, hasGraf);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_sfl_send:
			if (sendData == null || sendData.size() <= 0) {
				Toast.makeText(SelectFriendsActivity.this, R.string.please_select_contact, Toast.LENGTH_SHORT).show();
				return;
			} else {
				EventUtil.Main_Photo.rcpt_success_send(baseContext);
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
			startActivity(new Intent(SelectFriendsActivity.this, InviteActivity.class));
			break;
		}
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
