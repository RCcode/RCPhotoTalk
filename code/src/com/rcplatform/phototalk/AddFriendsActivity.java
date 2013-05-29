package com.rcplatform.phototalk;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost;

import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.FriendType;
import com.rcplatform.phototalk.logic.LogicUtils;
import com.rcplatform.phototalk.utils.Constants.Action;

public class AddFriendsActivity extends TabActivity implements OnClickListener {

	private TabHost mHost;
	private static final String TAB_CONTACTS = "contacts";
	private static final String TAB_FACEBOOK = "facebook";
	private static final String TAB_VK = "vkontakte";
	private static final String TAB_SEARCH = "search";

	private static TreeSet<Friend> friendsAdded;

	public static final String RESULT_PARAM_KEY_NEW_ADD_FRIENDS = "new_friends";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
	}

	private void initView() {

		Button btnContinue = (Button) findViewById(R.id.btn_continue);
		ImageView btn_continue_line = (ImageView) findViewById(R.id.btn_continue_line);
		btnContinue.setOnClickListener(this);
		if (getIntent().getData() != null || (getIntent().getStringExtra("from") != null && getIntent().getStringExtra("from").equals("base"))) {
			btnContinue.setVisibility(View.GONE);
			btn_continue_line.setVisibility(View.GONE);
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
					mHost.setCurrentTabByTag(TAB_FACEBOOK);
					break;
				case R.id.tab_vkontakte:
					mHost.setCurrentTabByTag(TAB_VK);
					break;
				case R.id.tab_search:
					mHost.setCurrentTabByTag(TAB_SEARCH);
				}
			}
		});
		mHost = getTabHost();
		addTabs();
	}

	private void addTabs() {
		mHost.addTab(mHost.newTabSpec(TAB_CONTACTS).setIndicator(TAB_CONTACTS).setContent(new Intent(this, ContactFriendRecommendActivity.class)));
		mHost.addTab(mHost.newTabSpec(TAB_FACEBOOK).setIndicator(TAB_FACEBOOK).setContent(new Intent(this, FacebookFriendRecommendActivity.class)));
		mHost.addTab(mHost.newTabSpec(TAB_VK).setIndicator(TAB_VK).setContent(new Intent(this, VKRecommendActivity.class)));
		mHost.addTab(mHost.newTabSpec(TAB_SEARCH).setIndicator(TAB_SEARCH).setContent(new Intent(this, SearchFriendsActivity.class)));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_continue:
			startActivity(new Intent(this, HomeActivity.class));
			finish();
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LogicUtils.uploadFriendInvite(this, Action.ACTION_UPLOAD_INTITE_CONTACT, FriendType.CONTACT);
		friendsAdded.clear();
		friendsAdded = null;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		WindowManager.LayoutParams params = getWindow().getAttributes();
		//
		// if () {
		//
		// // 隐藏软键盘
		//
		// getWindow().setSoftInputMode(
		//
		// WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		//
		// params.softInputMode =
		// WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN;
		// }
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

}
