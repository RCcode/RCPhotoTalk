package com.rcplatform.phototalk.utils;

import java.util.HashSet;
import java.util.Set;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Phone;

import com.rcplatform.phototalk.bean.Contacts;

public class ContactQuery extends AsyncQueryHandler {
	
	private OnContactsQueryCompleteListener mListener;
	
	public static interface OnContactsQueryCompleteListener{
		public void onContacksQueryComplete(Set<Contacts> contacts);
	}

	public ContactQuery(ContentResolver cr,OnContactsQueryCompleteListener listener) {
		super(cr);
		// TODO Auto-generated constructor stub
		this.mListener=listener;
	}
	@Override
	protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
		// TODO Auto-generated method stub
		super.onQueryComplete(token, cookie, cursor);
		Set<Contacts> contacts=new HashSet<Contacts>();
		while(cursor.moveToNext()){
			Contacts c=new Contacts();
			c.setMobilePhoneNumber(cursor.getString(cursor.getColumnIndex(Phone.NUMBER)));
			c.setName(cursor.getString(cursor.getColumnIndex(Phone.DISPLAY_NAME)));
			contacts.add(c);
		}
		cursor.close();
		mListener.onContacksQueryComplete(contacts);
	}
}
