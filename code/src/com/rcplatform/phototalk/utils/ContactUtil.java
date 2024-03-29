package com.rcplatform.phototalk.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;

import com.rcplatform.phototalk.bean.Contacts;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.FriendType;
import com.rcplatform.phototalk.utils.ContactQuery.OnContactsQueryCompleteListener;

public class ContactUtil {

	public static void getContactsAsync(Context context, OnContactsQueryCompleteListener listener) {
		String[] projections = new String[] { Phone.NUMBER, Phone.DISPLAY_NAME };
		AsyncQueryHandler handler = new ContactQuery(context.getContentResolver(), listener);
		handler.startQuery(0, null, ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projections, null, null, null);
	}

	public static List<Contacts> getContacts(Context context) {
		ContentResolver resolver = context.getContentResolver();
		String[] projections = new String[] { Phone.NUMBER, Phone.DISPLAY_NAME };
		Cursor cursor = resolver.query(Phone.CONTENT_URI, projections, null, null, null);
		List<Contacts> contacts = new ArrayList<Contacts>();
		while (cursor.moveToNext()) {
			Contacts c = new Contacts();
			c.setMobilePhoneNumber(cursor.getString(cursor.getColumnIndex(Phone.NUMBER)));
			c.setName(cursor.getString(cursor.getColumnIndex(Phone.DISPLAY_NAME)));
			contacts.add(c);
		}
		cursor.close();
		return contacts;
	}

	public static List<Friend> parserContacts(Collection<Contacts> contacts) {
		List<Friend> friends = new ArrayList<Friend>();
		for (Contacts contact : contacts) {
			Friend friend = paserContact(contact);
			friend.setSuid(contact.getMobilePhoneNumber());
			friends.add(friend);
		}
		return friends;
	}

	public static Friend paserContact(Contacts contact) {
		return new Friend(contact.getName(), contact.getMobilePhoneNumber(), null);
	}

	public static List<Friend> getContactFriendNotRepeat(Collection<Contacts> contacts, List<Friend> friends) {
		List<Contacts> contactRepeat = new ArrayList<Contacts>();
		for (Friend friend : friends) {
			String mobilePhoneNumber = friend.getSource().getValue();
			for (Contacts contact : contacts) {
				if (contact.getMobilePhoneNumber().equals(mobilePhoneNumber)) {
					contactRepeat.add(contact);
					break;
				}
			}
		}
		if (contactRepeat.size() > 0)
			contacts.removeAll(contactRepeat);
		return parserContacts(contacts);
	}
}
