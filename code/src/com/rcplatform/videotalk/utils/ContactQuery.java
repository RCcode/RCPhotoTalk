package com.rcplatform.videotalk.utils;

import java.util.HashSet;
import java.util.Set;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Phone;

import com.rcplatform.videotalk.bean.Contacts;

public class ContactQuery extends AsyncQueryHandler {

	private OnContactsQueryCompleteListener mListener;

	public static interface OnContactsQueryCompleteListener {
		public void onContacksQueryComplete(Set<Contacts> contacts);
	}

	public ContactQuery(ContentResolver cr, OnContactsQueryCompleteListener listener) {
		super(cr);
		this.mListener = listener;
	}

	@Override
	protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
		super.onQueryComplete(token, cookie, cursor);
		Set<Contacts> contacts = new HashSet<Contacts>();
		while (cursor.moveToNext()) {
			Contacts c = new Contacts();
			c.setMobilePhoneNumber(cursor.getString(cursor.getColumnIndex(Phone.NUMBER)));
			c.setName(cursor.getString(cursor.getColumnIndex(Phone.DISPLAY_NAME)));
			if (!contacts.contains(c))
				contacts.add(c);
		}
		cursor.close();
		mListener.onContacksQueryComplete(contacts);
	}
}
