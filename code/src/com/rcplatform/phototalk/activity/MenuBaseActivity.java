package com.rcplatform.phototalk.activity;

import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gcm.MetaHelper;
import com.rcplatform.phototalk.AddFriendsActivity;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.umeng.EventUtil;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.SystemMessageUtil;

public class MenuBaseActivity extends BaseActivity {

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 0, R.string.add_friend_title).setIcon(R.drawable.menu_add_friend);
		menu.add(0, 2, 0, R.string.contact_us).setIcon(R.drawable.menu_connect_our);
		menu.add(0, 3, 0, R.string.take_score).setIcon(R.drawable.menu_take_score);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
			case 1:
				EventUtil.Main_Photo.rcpt_menu_feedback(baseContext);
				Intent intent = new Intent(MenuBaseActivity.this, AddFriendsActivity.class);
				intent.putExtra("from", "base");
				startActivity(intent);
				break;
			case 2:
				EventUtil.Main_Photo.rcpt_menu_feedback(baseContext);
				Intent email = new Intent(android.content.Intent.ACTION_SENDTO, Uri.fromParts("mailto", Constants.FEEDBACK_EMAIL, null));
				String emailSubject = "\n\n\n\n\n\n"+SystemMessageUtil.getAppName(baseContext) + "\n" + MetaHelper.getAppVersionName(baseContext) + "\n"
				        + SystemMessageUtil.getPhoneBrand() + "\n" + SystemMessageUtil.getPhoneModel() + "\n"
				        + SystemMessageUtil.getOsVersion(baseContext) + "\n\n";
				email.putExtra(android.content.Intent.EXTRA_TEXT, emailSubject);
				
				email.putExtra(Intent.EXTRA_SUBJECT,  "["+ SystemMessageUtil.getAppName(baseContext) +"-"+ MetaHelper.getAppVersionName(baseContext)+"]");// 邮件标题  
				startActivity(email);
				break;
			case 3:
				EventUtil.Main_Photo.rcpt_menu_rate(baseContext);
				SystemMessageUtil.enterPage("market://details?id=com.rcplatform.phototalk", baseContext);
				break;
		}

		return super.onMenuItemSelected(featureId, item);
	}
}
