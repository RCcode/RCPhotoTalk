package com.rcplatform.phototalk.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.rcplatform.phototalk.MenueApplication;
import com.rcplatform.phototalk.bean.DetailFriend;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.RecordUser;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.utils.SQLiteUtil;

public class PhotoTalkDao {
	private static PhotoTalkDao mDao;
	public static final String RECROD_TABLE_NAME = "record";

	public static final String RECORD_ID = "record_id";

	private static String USER_RECORD;

	public static final String USER_RECORD_TABLE_NAME = "user_record";

	public static final String RECORD_S_USER_ID = "s_user_id";

	public static final String RECORD_S_USER_SUID = "s_user_suid";

	public static final String RECORD_S_USER_NICK = "s_user_nick";

	public static final String RECORD_S_USER_HEAD = "s_user_head";

	public static final String RECORD_R_USER_ID = "r_user_id";

	public static final String RECORD_R_USER_SUID = "r_user_suid";

	public static final String RECORD_R_USER_NICK = "r_user_nick";

	public static final String RECORD_R_USER_HEAD = "r_user_head";

	public static final String RECORD_TYPE = "type";

	public static final String RECORD_CREATE_TIME = "create_time";

	public static final String RECORD_UPDATE_TIME = "update_time";

	public static final String RECORD_STATU = "statu";

	public static final String RECORD_URL = "url";

	public static final String RECORD_LIMIT_TIME = "limit_time";

	public static final String RECORD_NOTICE_ID = "notice_id";

	public static final String FAIL_REQUEST = "fail_httpreauest";

	public static final String REQUEST_ID = "request_id";

	public static final String REQUEST_URL = "request_url";

	public static final String REQUEST_PARAMS = "request_params";

	private PhotoTalkDao() {

	}

	public static PhotoTalkDao getInstance() {
		if (mDao == null)
			mDao = new PhotoTalkDao();
		return mDao;
	}

	public synchronized List<Information> loadTMoreInfoRecord(Context context,
			int count, String createTime) {
		String userId ="";
		USER_RECORD = USER_RECORD_TABLE_NAME + "_" + userId;
		List<Information> list = new ArrayList<Information>();
		Information record;
		RecordUser sender;
		RecordUser receicer;
		Cursor cursor = null;
		SQLiteDatabase db = DatabaseFactory.getInstance(context).getDatabase();
		try {
			cursor = db.rawQuery("SELECT " + RECORD_ID + "," + RECORD_S_USER_ID
					+ "," + RECORD_S_USER_SUID + "," + RECORD_S_USER_NICK + ","
					+ RECORD_S_USER_HEAD + "," + RECORD_R_USER_ID + ","
					+ RECORD_R_USER_SUID + "," + RECORD_R_USER_NICK + ","
					+ RECORD_R_USER_HEAD + "," + RECORD_TYPE + ","
					+ RECORD_STATU + "," + RECORD_CREATE_TIME + ","
					+ RECORD_UPDATE_TIME + "," + RECORD_LIMIT_TIME + ","
					+ RECORD_URL + "," + RECORD_NOTICE_ID + " FROM "
					+ USER_RECORD + " WHERE " + RECORD_CREATE_TIME + " < "
					+ createTime + " ORDER BY create_time DESC limit " + count,
					null);
			while (cursor.moveToNext()) {
				record = new Information();
				record.setRecordId(cursor.getString(0));

				sender = new RecordUser();
				sender.setSuid(cursor.getString(1));
				sender.setNick(cursor.getString(3));
				sender.setHeadUrl(cursor.getString(4));
				record.setSender(sender);

				receicer = new RecordUser();
				receicer.setSuid(cursor.getString(5));
				receicer.setNick(cursor.getString(7));
				receicer.setHeadUrl(cursor.getString(8));
				record.setReceiver(receicer);

				record.setType(cursor.getInt(9));
				record.setStatu(cursor.getInt(10));
				record.setCreatetime(cursor.getLong(11));
				record.setLastUpdateTime(cursor.getLong(12));
				record.setLimitTime(cursor.getInt(13));
				record.setUrl(cursor.getString(14));
				record.setNoticeId(cursor.getString(15));
				list.add(record);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			cursor.close();
		}
		return list;
	}

	public synchronized List<Information> loadTopCountInfoRecord(
			Context context, int count) {
		String userId ="";
		USER_RECORD = USER_RECORD_TABLE_NAME + "_" + userId;
		List<Information> list = new ArrayList<Information>();
		Information record;
		RecordUser sender;
		RecordUser receicer;
		Cursor cursor = null;
		SQLiteDatabase db = DatabaseFactory.getInstance(context).getDatabase();
		try {
			String querySql = "SELECT " + RECORD_ID + "," + RECORD_S_USER_ID
					+ "," + RECORD_S_USER_SUID + "," + RECORD_S_USER_NICK + ","
					+ RECORD_S_USER_HEAD + "," + RECORD_R_USER_ID + ","
					+ RECORD_R_USER_SUID + "," + RECORD_R_USER_NICK + ","
					+ RECORD_R_USER_HEAD + "," + RECORD_TYPE + ","
					+ RECORD_STATU + "," + RECORD_CREATE_TIME + ","
					+ RECORD_UPDATE_TIME + "," + RECORD_LIMIT_TIME + ","
					+ RECORD_URL + "," + RECORD_NOTICE_ID + " FROM "
					+ USER_RECORD + " ORDER BY create_time DESC limit " + count;
			cursor = db.rawQuery(querySql, null);
			while (cursor.moveToNext()) {
				record = new Information();
				record.setRecordId(cursor.getString(0));

				sender = new RecordUser();
				sender.setSuid(cursor.getString(1));
				sender.setNick(cursor.getString(3));
				sender.setHeadUrl(cursor.getString(4));
				record.setSender(sender);

				receicer = new RecordUser();
				receicer.setSuid(cursor.getString(5));
				receicer.setNick(cursor.getString(7));
				receicer.setHeadUrl(cursor.getString(8));
				record.setReceiver(receicer);

				record.setType(cursor.getInt(9));
				record.setStatu(cursor.getInt(10));
				record.setCreatetime(cursor.getLong(11));
				record.setLastUpdateTime(cursor.getLong(12));
				record.setLimitTime(cursor.getInt(13));
				record.setUrl(cursor.getString(14));
				record.setNoticeId(cursor.getString(15));

				list.add(record);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			cursor.close();
		}
		return list;
	}

	private SQLiteStatement myInsertRecordInfoStatement;

	// private SQLiteStatement myInsertRecordUserStatement;

	public synchronized void insertInfoRecord(Context context,
			List<Information> data) {
		String userId ="";
		USER_RECORD = USER_RECORD_TABLE_NAME + "_" + userId;
		SQLiteDatabase db = DatabaseFactory.getInstance(context).getDatabase();
		try {
			myInsertRecordInfoStatement = db.compileStatement("REPLACE INTO "
					+ USER_RECORD + " ( " + RECORD_ID + "," + RECORD_S_USER_ID
					+ "," + RECORD_S_USER_SUID + "," + RECORD_S_USER_NICK + ","
					+ RECORD_S_USER_HEAD + "," + RECORD_R_USER_ID + ","
					+ RECORD_R_USER_SUID + "," + RECORD_R_USER_NICK + ","
					+ RECORD_R_USER_HEAD + "," + RECORD_TYPE + ","
					+ RECORD_STATU + "," + RECORD_CREATE_TIME + ","
					+ RECORD_UPDATE_TIME + "," + RECORD_LIMIT_TIME + ","
					+ RECORD_URL + "," + RECORD_NOTICE_ID + ")"
					+ " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			db.beginTransaction();
			Information record;
			for (int i = 0; i < data.size(); i++) {
				record = data.get(i);
				SQLiteUtil.bindString(myInsertRecordInfoStatement, 1,
						record.getRecordId());

				SQLiteUtil.bindString(myInsertRecordInfoStatement, 2, record
						.getSender().getSuid());
				SQLiteUtil.bindString(myInsertRecordInfoStatement, 4, record
						.getSender().getNick());
				SQLiteUtil.bindString(myInsertRecordInfoStatement, 5, record
						.getSender().getHeadUrl());

				SQLiteUtil.bindString(myInsertRecordInfoStatement, 6, record
						.getReceiver().getSuid());
				SQLiteUtil.bindString(myInsertRecordInfoStatement, 8, record
						.getReceiver().getNick());
				SQLiteUtil.bindString(myInsertRecordInfoStatement, 9, record
						.getReceiver().getHeadUrl());

				SQLiteUtil.bindString(myInsertRecordInfoStatement, 10,
						String.valueOf(record.getType()));
				SQLiteUtil.bindString(myInsertRecordInfoStatement, 11,
						String.valueOf(record.getStatu()));
				SQLiteUtil.bindString(myInsertRecordInfoStatement, 12,
						String.valueOf(record.getCreatetime()));
				SQLiteUtil.bindString(myInsertRecordInfoStatement, 13,
						String.valueOf(record.getLastUpdateTime()));
				SQLiteUtil.bindString(myInsertRecordInfoStatement, 14,
						String.valueOf(record.getLimitTime()));
				SQLiteUtil.bindString(myInsertRecordInfoStatement, 15,
						record.getUrl());
				SQLiteUtil.bindString(myInsertRecordInfoStatement, 16,
						record.getNoticeId());

				myInsertRecordInfoStatement.executeInsert();

			}
			db.setTransactionSuccessful();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}

	private SQLiteStatement myUpdateRecordStatement;

	public synchronized void updateRecordStatu(Context context,
			Information infoRecord) {
		String userId ="";
		USER_RECORD = USER_RECORD_TABLE_NAME + "_" + userId;
		SQLiteDatabase db = DatabaseFactory.getInstance(context).getDatabase();
		try {
			myUpdateRecordStatement = db.compileStatement("UPDATE "
					+ USER_RECORD + " SET " + RECORD_STATU + " = ?, "
					+ RECORD_UPDATE_TIME + " = ? WHERE " + RECORD_ID + " = ?");

			myUpdateRecordStatement.bindString(1, infoRecord.getStatu() + "");
			myUpdateRecordStatement.bindString(2,
					infoRecord.getLastUpdateTime() + "");
			myUpdateRecordStatement
					.bindString(3, infoRecord.getRecordId() + "");
			myUpdateRecordStatement.execute();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public synchronized Information findRecordByRecordId(Context context,
			String id) {
		String userId ="";
		USER_RECORD = USER_RECORD_TABLE_NAME + "_" + userId;
		SQLiteDatabase db = DatabaseFactory.getInstance(context).getDatabase();
		Cursor cursor = null;
		Information record = null;
		try {
			cursor = db.rawQuery("SELECT " + RECORD_ID + ", " + RECORD_STATU
					+ " FROM " + USER_RECORD + " WHERE " + RECORD_ID + " = "
					+ id, null);
			while (cursor.moveToNext()) {
				record = new Information();
				record.setRecordId(cursor.getString(0));
				record.setStatu(cursor.getInt(1));
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			cursor.close();
		}
		return record;

	}

	private SQLiteStatement myDeleteRecordStatement;

	public synchronized void deleteRecordById(Context context, String recordId) {
		String userId ="";
		USER_RECORD = USER_RECORD_TABLE_NAME + "_" + userId;
		SQLiteDatabase db = DatabaseFactory.getInstance(context).getDatabase();
		try {
			if (myDeleteRecordStatement == null) {
				myDeleteRecordStatement = db.compileStatement("DELETE FROM "
						+ USER_RECORD + " WHERE " + RECORD_ID + " = ?");
			}
			myDeleteRecordStatement.bindString(1, recordId);
			myDeleteRecordStatement.execute();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public synchronized void deleteCurrentUserTable(Context context) {
		SQLiteDatabase db = null;
		String userId ="";
		USER_RECORD = USER_RECORD_TABLE_NAME + "_" + userId;
		try {
			db = DatabaseFactory.getInstance(context).getDatabase();
			// List<InfoRecord> systemInforRecord =
			// findInfoRecordByType(MenueApiRecordType.TYPE_SYSTEM_NOTICE);
			db.execSQL("DROP TABLE IF EXISTS " + USER_RECORD);
			DatabaseFactory.getInstance(context).createTables(context);
			// insertInfoRecord(systemInforRecord);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public synchronized List<Information> findInfoRecordByType(Context context,
			int type) {
		String userId ="";
		USER_RECORD = USER_RECORD_TABLE_NAME + "_" + userId;
		List<Information> list = new ArrayList<Information>();
		Information record;
		RecordUser sender;
		RecordUser receicer;
		Cursor cursor = null;
		SQLiteDatabase db = DatabaseFactory.getInstance(context).getDatabase();
		try {
			String querySql = "SELECT " + RECORD_ID + "," + RECORD_S_USER_ID
					+ "," + RECORD_S_USER_SUID + "," + RECORD_S_USER_NICK + ","
					+ RECORD_S_USER_HEAD + "," + RECORD_R_USER_ID + ","
					+ RECORD_R_USER_SUID + "," + RECORD_R_USER_NICK + ","
					+ RECORD_R_USER_HEAD + "," + RECORD_TYPE + ","
					+ RECORD_STATU + "," + RECORD_CREATE_TIME + ","
					+ RECORD_UPDATE_TIME + "," + RECORD_LIMIT_TIME + ","
					+ RECORD_URL + "," + RECORD_NOTICE_ID + " FROM "
					+ USER_RECORD + " WHERE " + RECORD_TYPE + " = " + type;
			cursor = db.rawQuery(querySql, null);
			while (cursor.moveToNext()) {
				record = new Information();
				record.setRecordId(cursor.getString(0));

				sender = new RecordUser();
				sender.setSuid(cursor.getString(1));
//				sender.setSuUserId(cursor.getString(2));
				sender.setNick(cursor.getString(3));
				sender.setHeadUrl(cursor.getString(4));
				record.setSender(sender);

				receicer = new RecordUser();
				receicer.setSuid(cursor.getString(5));
//				receicer.setSuUserId(cursor.getString(6));
				receicer.setNick(cursor.getString(7));
				receicer.setHeadUrl(cursor.getString(8));
				record.setReceiver(receicer);

				record.setType(cursor.getInt(9));
				record.setStatu(cursor.getInt(10));
				record.setCreatetime(cursor.getLong(11));
				record.setLastUpdateTime(cursor.getLong(12));
				record.setLimitTime(cursor.getInt(13));
				record.setUrl(cursor.getString(14));
				record.setNoticeId(cursor.getString(15));

				list.add(record);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			cursor.close();
		}
		return list;
	}

	private SQLiteStatement insertFailRequestInfoStatement;

	public synchronized void insertFailRequestInfo(Context context, String url,
			String params) {
		String userId ="";
		String FAIL_REQUEST_TABLE = FAIL_REQUEST + "_" + userId;
		SQLiteDatabase db = DatabaseFactory.getInstance(context).getDatabase();
		try {
			List<Map<String, String>> list = findAllFailRequestInfo(context);
			for (Map<String, String> info : list) {
				if (info.get("url").equals(url)
						&& info.get("params").equals(params)) {
					return;
				}
			}

			insertFailRequestInfoStatement = db.compileStatement("INSERT INTO "
					+ FAIL_REQUEST_TABLE + " ( " + REQUEST_URL + ","
					+ REQUEST_PARAMS + ")" + " VALUES (?,?)");

			insertFailRequestInfoStatement.bindString(1, url);
			insertFailRequestInfoStatement.bindString(2, params);
			insertFailRequestInfoStatement.executeInsert();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public synchronized List<Map<String, String>> findAllFailRequestInfo(
			Context context) {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		Map<String, String> map = null;
		String userId ="";
		String FAIL_REQUEST_TABLE = FAIL_REQUEST + "_" + userId;
		SQLiteDatabase db = DatabaseFactory.getInstance(context).getDatabase();
		Cursor cursor = null;

		try {
			String querySql = "SELECT " + REQUEST_ID + "," + REQUEST_URL + ","
					+ REQUEST_PARAMS + " FROM " + FAIL_REQUEST_TABLE;
			cursor = db.rawQuery(querySql, null);
			while (cursor.moveToNext()) {
				map = new HashMap<String, String>();
				map.put("id", cursor.getString(0));
				map.put("url", cursor.getString(1));
				map.put("params", cursor.getString(2));
				list.add(map);
			}

		} catch (Exception ex) {
		}
		return list;
	}

	private SQLiteStatement deleteFailRequestStatement;

	public synchronized void deleteFailRequestInfoById(Context context,
			String id) {
		SQLiteDatabase db = DatabaseFactory.getInstance(context).getDatabase();
		String userId ="";
		String FAIL_REQUEST_TABLE = FAIL_REQUEST + "_" + userId;
		try {
			if (deleteFailRequestStatement == null) {
				deleteFailRequestStatement = db.compileStatement("DELETE FROM "
						+ FAIL_REQUEST_TABLE + " WHERE " + REQUEST_ID + " = ?");
			}
			deleteFailRequestStatement.bindString(1, id);
			deleteFailRequestStatement.execute();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public synchronized void insertContactFriends(Context context,
			List<Friend> friends) {
		SQLiteDatabase db = DatabaseFactory.getInstance(context).getDatabase();
		db.beginTransaction();
		String sql = "DELETE FROM "
				+ PhotoTalkDatabaseHelper.CONTACT_TABLE_NAME;
		db.execSQL(sql);

		for (Friend f : friends) {
			insertContactFriend(db, f);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	public synchronized void insertContactFriend(SQLiteDatabase db,
			Friend friend) {
		String sql = "INSERT INTO "
				+ PhotoTalkDatabaseHelper.CONTACT_TABLE_NAME
				+ " (user_id,suid,nick,head_url,rc_id,phone,app_id,rset,signature) VALUES (?,?,?,?,?,?,?,?,?)";
		db.execSQL(
				sql,
				new Object[] {  friend.getSuid(),
						friend.getNick(), friend.getHeadUrl(),
						friend.getRcId(), friend.getPhone(),
						friend.getAppId(),
						friend.getSignature() });
	}

	public synchronized void saveFriend(Context context, DetailFriend friend) {
		String suid = friend.getSuid();
		String tableName=getTableName(context, DatabaseFactory.FRIEND_TABLE_NAME);
		String sql = "SELECT _id FROM" + DatabaseFactory.FRIEND_TABLE_NAME
				+ getCurrentUser(context).getSuid() + " WHERE suid=" + suid;
		Cursor cursor=DatabaseFactory.getInstance(context).getDatabase().rawQuery(sql, null);
		if(cursor.getCount()>0){
//			sql="UPDATE "+tableName+" SET rcid=?,head_url=?,mark=?"
		}
	}

	private UserInfo getCurrentUser(Context context) {
		return null;
	}

	private String getTableName(Context context, String tableBaseName) {
		return tableBaseName + "_" + getCurrentUser(context).getSuid();
	}
}
