package com.rcplatform.phototalk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * 添加好友页面
 * <p>
 * Copyright: Menue,Inc Copyright (c) 2013-3-11 上午09:49:24
 * <p>
 * Team:Menue Beijing
 * <p>
 * 
 * @author jelly.xiong@menue.com.cn
 * @version 1.0.0
 */
public class AddFriendActivity extends Activity implements View.OnClickListener {

	private View mInviteFriendView;

	private View mSearchView;

	private View mPhoneBookAddView;

	private View mFacebookAddView;

	private View mBack;

	private TextView mTitleTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_friend_activity);

		initTitle();

		mSearchView = findViewById(R.id.add_friend_search_layout);
		mSearchView.setOnClickListener(this);
		mPhoneBookAddView = findViewById(R.id.add_friend_from_phonebook_layout);
		mPhoneBookAddView.setOnClickListener(this);
		mFacebookAddView = findViewById(R.id.add_friend_from_facebook_layout);
		mFacebookAddView.setOnClickListener(this);

	}

	private void initTitle() {
		mBack = findViewById(R.id.back);
		mBack.setVisibility(View.VISIBLE);
		mBack.setOnClickListener(this);

		mTitleTextView = (TextView) findViewById(R.id.titleContent);
		mTitleTextView.setText(getResources().getText(R.string.firend_list_add_friend_title));
		mTitleTextView.setVisibility(View.VISIBLE);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.back:
				finish();
				break;
			case R.id.add_friend_search_layout:
				startActivity(new Intent(this, SearchFriendsActivity.class));
				break;
			case R.id.add_friend_from_phonebook_layout:
				startActivity(new Intent(this, ContactFriendRecommendActivity.class).setAction("action.meneu.phonebook_friend"));
				break;
			case R.id.add_friend_from_facebook_layout:
				startActivity(new Intent(this, ContactFriendRecommendActivity.class).setAction("action.meneu.facebook_friend"));
				break;
		}
	}

}
