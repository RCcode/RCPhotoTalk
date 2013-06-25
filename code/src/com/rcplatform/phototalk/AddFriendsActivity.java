package com.rcplatform.phototalk;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.rcplatform.phototalk.activity.BaseTabActivity;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.FriendType;
import com.rcplatform.phototalk.logic.LogicUtils;
import com.rcplatform.phototalk.thirdpart.utils.OnAuthorizeSuccessListener;
import com.rcplatform.phototalk.thirdpart.utils.ThirdPartClient;
import com.rcplatform.phototalk.thirdpart.utils.TwitterClient;
import com.rcplatform.phototalk.umeng.EventUtil;
import com.rcplatform.phototalk.utils.Constants.Action;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.Utils;

public class AddFriendsActivity extends BaseTabActivity implements OnClickListener {

	private TabHost mHost;

	private static final String TAB_CONTACTS = "contacts";

	private static final String TAB_FACEBOOK = "facebook";

	private static final String TAB_VK = "vkontakte";

	private static final String TAB_SEARCH = "search";
	private static final String TAB_TWITTER = "twitter";

	private static TreeSet<Friend> friendsAdded;

	private LinearLayout add_friend_layout;

	private TextView find_friend_text;

	private Button add_back_btn;

	private Context ctx;

	public static final String RESULT_PARAM_KEY_NEW_ADD_FRIENDS = "new_friends";

	private Map<Integer, ThirdPartClient> thirdPartClients = new LinkedHashMap<Integer, ThirdPartClient>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ctx = this;
		setContentView(R.layout.add_friends);
		friendsAdded = new TreeSet<Friend>(new Comparator<Friend>() {

			@Override
			public int compare(Friend lhs, Friend rhs) {
				if (lhs.getRcId().equals(rhs.getRcId())) {
					return 0;
				}
				return 1;
			}
		});
		initView();
		initData();
	}

	private void initData() {
		thirdPartClients.put(R.id.btn_twitter, new TwitterClient(this));
	}

	private void initView() {
		add_friend_layout = (LinearLayout) findViewById(R.id.add_friend_layout);
		find_friend_text = (TextView) findViewById(R.id.find_friend_text);
		add_back_btn = (Button) findViewById(R.id.add_back_btn);
		add_back_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Utils.hideSoftInputKeyboard(AddFriendsActivity.this, v);
				AddFriendsActivity.this.finish();
			}
		});
		Button btnContinue = (Button) findViewById(R.id.btn_continue);
		ImageView btn_continue_line = (ImageView) findViewById(R.id.btn_continue_line);
		btnContinue.setOnClickListener(this);
		if (getIntent().getData() != null || (getIntent().getStringExtra("from") != null && getIntent().getStringExtra("from").equals("base"))) {
			btnContinue.setVisibility(View.GONE);
			btn_continue_line.setVisibility(View.GONE);
			add_friend_layout.setVisibility(View.VISIBLE);
		} else {
			find_friend_text.setVisibility(View.VISIBLE);
		}
		RadioGroup rgTabs = (RadioGroup) findViewById(R.id.rg_add_friends);
		rgTabs.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.tab_contact:
					mHost.setCurrentTabByTag(TAB_CONTACTS);
					break;
				case R.id.tab_facebook:
					EventUtil.Friends_Addfriends.rcpt_facebok(ctx);
					mHost.setCurrentTabByTag(TAB_FACEBOOK);
					break;
				case R.id.tab_vkontakte:
					EventUtil.Friends_Addfriends.rcpt_vk(ctx);
					mHost.setCurrentTabByTag(TAB_VK);
					break;
				case R.id.tab_search:
					EventUtil.Friends_Addfriends.rcpt_search(ctx);
					mHost.setCurrentTabByTag(TAB_SEARCH);
					// case R.id.tab_twitter:
					// mHost.setCurrentTabByTag(TAB_TWITTER);
					// break;
				}
			}
		});
		Button btnTwitter = (Button) findViewById(R.id.btn_twitter);
		btnTwitter.setOnClickListener(this);
		mHost = getTabHost();
		addTabs();
	}

	private void addTabs() {
		mHost.addTab(mHost.newTabSpec(TAB_CONTACTS).setIndicator(TAB_CONTACTS).setContent(new Intent(this, ContactFriendRecommendActivity.class)));
		mHost.addTab(mHost.newTabSpec(TAB_FACEBOOK).setIndicator(TAB_FACEBOOK).setContent(new Intent(this, FacebookFriendRecommendActivity.class)));
		mHost.addTab(mHost.newTabSpec(TAB_VK).setIndicator(TAB_VK).setContent(new Intent(this, VKRecommendActivity.class)));
		mHost.addTab(mHost.newTabSpec(TAB_SEARCH).setIndicator(TAB_SEARCH).setContent(new Intent(this, SearchFriendsActivity.class)));
		addTab(FriendType.TWITTER);
	}

	private void addTab(int type) {
		Intent intent = new Intent(this, ThirdPartRecommendsActivity.class);
		intent.putExtra(ThirdPartRecommendsActivity.PARAM_KEY_TYPE, type);
		mHost.addTab(mHost.newTabSpec(TAB_TWITTER).setIndicator(TAB_TWITTER).setContent(intent));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_continue:
			startActivity(new Intent(this, HomeActivity.class));
			finish();
			break;
		case R.id.btn_twitter:
			invite(R.id.btn_twitter);
			break;
		}
	}

	private void invite(final int id) {
		ThirdPartClient client = thirdPartClients.get(id);
		if (client.isAuthorized()) {
			sendInviteMessage(id);
		} else {
			client.authorize(new OnAuthorizeSuccessListener() {

				@Override
				public void onAuthorizeSuccess() {
					sendInviteMessage(id);
				}
			});
		}
	}

	private void sendInviteMessage(int id) {
		thirdPartClients.get(id).sendJoinMessage(getString(R.string.join_message, getCurrentUser().getRcId()));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LogicUtils.uploadFriendInvite(this, Action.ACTION_UPLOAD_INTITE_CONTACT, FriendType.CONTACT);
		friendsAdded.clear();
		friendsAdded = null;
		for (ThirdPartClient client : thirdPartClients.values())
			client.destroy();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		WindowManager.LayoutParams params = getWindow().getAttributes();
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && friendsAdded.size() > 0
				&& params.softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE) {
			Intent intent = new Intent();
			intent.putExtra(AddFriendsActivity.RESULT_PARAM_KEY_NEW_ADD_FRIENDS, new ArrayList<Friend>(friendsAdded));
			setResult(Activity.RESULT_OK, intent);
			finish();
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	public static TreeSet<Friend> getAddedFriends() {
		return friendsAdded;
	}

	public static void addFriend(Friend friend) {
		friendsAdded.add(friend);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		for (ThirdPartClient client : thirdPartClients.values()) {
			client.onAuthorizeInformationReceived(requestCode, resultCode, data);
		}
	}
}
