package com.rcplatform.videotalk.db.impl;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.rcplatform.videotalk.bean.CountryCode;
import com.rcplatform.videotalk.db.CountryCodeDatabase;

public class CountryCodeSQLiteDatabase implements CountryCodeDatabase {

	private SQLiteDatabase mDb;

	public CountryCodeSQLiteDatabase() {
		mDb = SQLiteDatabase.openDatabase(com.rcplatform.videotalk.db.DatabaseUtils.getCountryCodeDatabasePath(), null, SQLiteDatabase.OPEN_READWRITE);
	}

	@Override
	public List<CountryCode> getAllCountry() {
		String sql = "SELECT * FROM PhoneCoutryCode ORDER BY country_name";
		Cursor cursor = mDb.rawQuery(sql, null);
		List<CountryCode> result = new ArrayList<CountryCode>();
		while (cursor.moveToNext()) {
			CountryCode countryCode = new CountryCode();
			countryCode.setCountryCode(cursor.getString(cursor.getColumnIndex("phone_code")));
			countryCode.setCountryName(cursor.getString(cursor.getColumnIndex("country_name")));
			countryCode.setCountryShort(cursor.getString(cursor.getColumnIndex("coutry_code")));
			result.add(countryCode);
		}
		cursor.close();
		return result;
	}

	@Override
	public void close() {
		mDb.close();
	}

}
