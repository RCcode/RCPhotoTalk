package com.rcplatform.phototalk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.rcplatform.phototalk.utils.Contract;

/**
 * 此页面为测试页面。
 * <p>
 * Copyright: Menue,Inc Copyright (c) 2013-3-8 下午05:57:04
 * <p>
 * Team:Menue Beijing
 * <p>
 * 
 * @author jelly.xiong@menue.com.cn
 * @version 1.0.0
 */
public class TestInterfaceMainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	public void registerPage(View view) {
		Intent loginIntent = new Intent(this, LoginActivity.class);
		loginIntent.putExtra(Contract.KEY_LOGIN_PAGE, false);
		startActivity(loginIntent);
	}

	public void loginPage(View view) {
		Intent loginIntent = new Intent(this, LoginActivity.class);
		loginIntent.putExtra(Contract.KEY_LOGIN_PAGE, true);
		startActivity(loginIntent);
	}

	public void findFriendsPage(View view) {
		Intent loginIntent = new Intent(this, MyFriendFindActivity.class);
		startActivity(loginIntent);
	}

	public void myFriendsPage(View view) {
		Intent loginIntent = new Intent(this, MyFriendActivity.class);
		startActivity(loginIntent);
	}

	public void searchPage(View view) {
		Intent loginIntent = new Intent(this, SearchFriendsActivity.class);
		startActivity(loginIntent);
	}

	public void sendToPage(View view) {
		Intent loginIntent = new Intent(this, MyFriendActivity.class);
		startActivity(loginIntent);
	}

	public void pickFriend(View view) {
		Intent loginIntent = new Intent(this, PickFriendActivity.class);
		startActivity(loginIntent);
	}

	public void morePage(View view) {
		Intent loginIntent = new Intent(this, SettingsActivity.class);
		startActivity(loginIntent);
	}

	public void addFriendPage(View view) {
		Intent loginIntent = new Intent(this, AddFriendActivity.class);
		startActivity(loginIntent);
	}

}
