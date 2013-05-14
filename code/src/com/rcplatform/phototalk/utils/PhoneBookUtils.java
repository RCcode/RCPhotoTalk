package com.rcplatform.phototalk.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.PhoneBook;

/**
 * 标题、简要说明. <br>
 * 类详细说明.
 * <p>
 * Copyright: Menue,Inc Copyright (c) 2013-3-4 上午11:55:35
 * <p>
 * Team:Menue Beijing
 * <p>
 * 
 * @author jelly.xiong@menue.com.cn
 * @version 1.0.0
 */
public class PhoneBookUtils {

	public static LinkedHashMap<String, String> getAllContactPhoneNumbers(Context context, PhoneBook phoneBook) {
		return null;
	}

	public static String phoneBook2Json() {

		return null;
	}

	public static LinkedHashMap<String, String> getAllContactPhoneNumbers(Context context) {
		String[] projection = new String[3];
		projection[0] = "_id";
		projection[1] = "data1";
		projection[2] = "display_name";
		Cursor c = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, null, null, null);
		LinkedHashMap<String, String> hashMap = new LinkedHashMap<String, String>();
		if (c != null) {
			while (c.moveToNext()) {
				String data = c.getString(1);
				data = PhoneBookUtils.removeNonDigits(data);
				String display_name = c.getString(2);
				// System.out.println("data=" + data + "  display_name=" +
				// display_name);
				hashMap.put("+86" + data, display_name);
			}
			c.close();
		}
		return hashMap;
	}

	public static String removeNonDigits(String data) {
		StringBuffer dataStringBuffer = new StringBuffer();
		int i = 0;
		while (i < data.length()) {
			char c = data.charAt(i);
			if (Character.isDigit(c)) {
				dataStringBuffer.append(c);
			}
			i++;
		}
		return dataStringBuffer.toString();
	}

	/**
	 * Method description
	 * 
	 * @param context
	 *            应用的上下文。
	 * @param friends
	 *            在photochat上的好友列表。
	 * @return 电话本中不在photochat上的好友列表,需要邀请的好友。
	 */
	public static List<Friend> parseFriend(Context context, List<Friend> friends) {
		return populateContactsNotOnChat(context, friends);
	}

	private static List<Friend> populateContactsNotOnChat(Context context, List<Friend> friends) {
		LinkedHashMap<String, String> allContactsMap = getAllContactPhoneNumbers(context);
		if (friends != null) {
			for (Friend friend : friends) {
				if (allContactsMap.containsKey(friend.getCellPhone())) {
					allContactsMap.remove(friend.getCellPhone());
				}
			}
		}

		Iterator iterator = allContactsMap.entrySet().iterator();
		List<Friend> contactsOnChat = new ArrayList<Friend>();
		while (iterator.hasNext()) {
			Map.Entry<String, String> entry = (Entry<String, String>) iterator.next();
			String key = entry.getKey();
			String value = entry.getValue();
			System.out.println("key=" + key);
			System.out.println("value=" + value);
			contactsOnChat.add(new Friend(value, key, null));
		}
		return contactsOnChat;
	}
	
}
