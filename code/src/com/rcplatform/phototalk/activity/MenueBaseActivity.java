package com.rcplatform.phototalk.activity;

import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;

import com.rcplatform.phototalk.AddFriendsActivity;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.utils.SystemMessageUtil;

public class MenueBaseActivity extends BaseActivity{
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 0, R.string.add_friend_title).setIcon(
				R.drawable.menu_add_friend);
		menu.add(0, 2, 0, R.string.contact_us).setIcon(
				R.drawable.menu_connect_our);
		menu.add(0, 3, 0, R.string.take_score).setIcon(
				R.drawable.menu_take_score);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case 1:
			Intent intent = new Intent(MenueBaseActivity.this,
					AddFriendsActivity.class);
			intent.putExtra("from", "base");
			startActivity(intent);
			break;
		case 2:
			Intent email = new Intent(
					android.content.Intent.ACTION_SENDTO, Uri
							.fromParts("mailto",
									"rctalk.service@gmail.com", null));
			String emailSubject = SystemMessageUtil
					.getLanguage(baseContext)
					+ SystemMessageUtil.getAppName(baseContext)
					+ SystemMessageUtil.getPhoneNumber(baseContext)
					+ SystemMessageUtil.getNetworkName(baseContext)
					+ SystemMessageUtil.getImsi(baseContext);
			email.putExtra(android.content.Intent.EXTRA_TEXT,
					emailSubject);
			startActivity(email);
			break;
		case 3:
			SystemMessageUtil
			.enterPage(
					"market://details?id=com.androidlord.optimizationbox",
					baseContext);
			break;
		}
		
		return super.onMenuItemSelected(featureId, item);
	}
}
