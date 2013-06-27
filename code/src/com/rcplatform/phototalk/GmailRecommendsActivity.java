package com.rcplatform.phototalk;

import java.util.Date;
import java.util.List;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;

import com.rcplatform.phototalk.activity.AddFriendBaseActivity;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.galhttprequest.LogUtil;

public class GmailRecommendsActivity extends AddFriendBaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getEmailContact();
	}

	private List<Friend> getEmailContact() {
		ContentResolver cr = getContentResolver();
		Cursor emailCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", null,
				null);
		while (emailCur.moveToNext()) {
			// This would allow you get several email addresses
			// if the email addresses were stored in an array
			String email = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
			String emailType = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
			LogUtil.e(email + "......" + emailType);
		}
		emailCur.close();
		return null;
	}

}
