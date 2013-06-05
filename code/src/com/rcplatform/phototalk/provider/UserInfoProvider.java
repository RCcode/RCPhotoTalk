package com.rcplatform.phototalk.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.net.Uri;

import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.PrefsUtils;

public class UserInfoProvider extends ContentProvider {
	private static UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	private static final int CODE_LOGIN_USER = 0;
	private static final int CODE_USERS = 1;
	static {
		mUriMatcher.addURI(Constants.Provider.PROVIDER_AUTHORITY, Constants.Provider.PROVIDER_USERS_PATH, CODE_USERS);
		mUriMatcher.addURI(Constants.Provider.PROVIDER_AUTHORITY, Constants.Provider.PROVIDER_LOGIN_USER_PATH, CODE_LOGIN_USER);
	}

	@Override
	public boolean onCreate() {
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		if (mUriMatcher.match(uri) == CODE_LOGIN_USER) {
			final UserInfo userInfo = PrefsUtils.LoginState.getLoginUser(getContext());
			if (userInfo != null) {
				Cursor cursor = new AbstractCursor() {
					private String[] columnNames = new String[] { Constants.KEY_RCID, Constants.KEY_EMAIL, Constants.KEY_USER_TOKEN, Constants.KEY_NICK,
							Constants.KEY_HEADURL, Constants.KEY_SEX, Constants.KEY_BIRTHDAY, Constants.KEY_APP_ID };

					@Override
					public boolean isNull(int column) {
						return false;
					}

					@Override
					public String getString(int column) {
						String result = null;
						result = getColumnString(userInfo, column);
						return result;
					}

					@Override
					public short getShort(int column) {
						return 0;
					}

					@Override
					public long getLong(int column) {
						return 0;
					}

					@Override
					public int getInt(int column) {
						if (column == 5) {
							return userInfo.getGender();
						}
						return -1;
					}

					@Override
					public float getFloat(int column) {
						return 0;
					}

					@Override
					public double getDouble(int column) {
						return 0;
					}

					@Override
					public int getCount() {
						return 1;
					}

					@Override
					public String[] getColumnNames() {
						return columnNames;
					}
				};
				return cursor;
			}
		}
		return null;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}

	private String getColumnString(UserInfo userInfo, int column) {
		String result = null;
		switch (column) {
		case 0:
			result = userInfo.getRcId();
			break;
		case 1:
			result = userInfo.getEmail();
			break;
		case 2:
			result = userInfo.getToken();
			break;
		case 3:
			result = userInfo.getNickName();
			break;
		case 4:
			result = userInfo.getHeadUrl();
			break;
		case 6:
			result = userInfo.getBirthday();
			break;
		case 7:
			result = Constants.APP_ID;
			break;
		}
		return result;
	}
}
