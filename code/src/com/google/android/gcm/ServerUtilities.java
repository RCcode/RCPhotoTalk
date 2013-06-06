/*
 * Copyright 2012 Google Inc. Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.google.android.gcm;

import com.google.android.gcm.GCMRegistrar;
import com.rcplatform.videotalk.utils.Constants;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Helper class used to communicate with the demo server.
 */
public final class ServerUtilities {

	public static final String SENDER_ID = "302738588217";

	private static String serverUrl = "http://192.168.0.86:8083/phototalk/user/syncUserkey.do";

	private static String serverLogUrl = "http://push.rcplatformhk.com/gcm/boss/receivePushStatus.do";

	private final static String status = "status";

	public final static String STATUS_CREATE_USERINFO = "1";

	public final static String STATUS_RECIVIE_MSG_INSTALLED = "1001";

	public final static String STATUS_RECIVIE_MSG_NEW_APP = "1002";
	
	public final static String GCM_MSG_USER_MESSAGE = "user_message";
	
	private static AsyncTask<Void, Void, Void> mRegisterTask;

	private static AsyncTask<Void, Void, Void> mreceivePushStatusTask;

	private static String RC_ID = "";
	
	private static String USER_TOKEN ="";

	public static void register(Context ctx, String rcId, String token) {
		RC_ID = rcId;
		USER_TOKEN = token;
		GCMRegistrar.checkDevice(ctx);
		GCMRegistrar.checkManifest(ctx);
		final String regId = GCMRegistrar.getRegistrationId(ctx);
		if (regId.equals("")) {
			// Automatically registers application on startup.
			GCMRegistrar.register(ctx, SENDER_ID);
		} else {
			// Device is already registered on GCM, check server.
			if (GCMRegistrar.isRegisteredOnServer(ctx, RC_ID)) {
				// Skips registration.
			} else {
				// Try to register again, but not in the UI thread.
				// It's also necessary to cancel the thread onDestroy(),
				// hence the use of AsyncTask instead of a raw thread.
				registerUserID(ctx, regId);
			}
		}
		
		setGcmMessageCount(ctx,GCM_MSG_USER_MESSAGE,0);
	}

	public static void onDestroy(Context ctx) {
		if (mRegisterTask != null) {
			mRegisterTask.cancel(true);
		}

		if (mreceivePushStatusTask != null) {
			mreceivePushStatusTask.cancel(true);
		}

		GCMRegistrar.onDestroy(ctx);
	}

	static void registerUserID(Context ctx, String id) {
		final Context context = ctx;
		final String regId = id;
		mRegisterTask = new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				ServerUtilities.registerToService(context, regId, RC_ID);
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				mRegisterTask = null;
			}

		};
		mRegisterTask.execute(null, null, null);
	}

	static void logPushResult(final Context context, final String pushId, final String pushFlag) {
		mreceivePushStatusTask = new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				ServerUtilities.logPushResultService(context, pushId, pushFlag);
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				mreceivePushStatusTask = null;
			}

		};
		mreceivePushStatusTask.execute(null, null, null);
	}

	private static void logPushResultService(final Context context, final String pushId, final String pushFlag) {
		JSONObject json = new JSONObject();
		try {
			json.put("pushID", pushId);
			json.put("appID", Constants.APP_ID);
			json.put("packageName", MetaHelper.getAppName(context));
			json.put("status", pushFlag);
			json.put("deviceID", MetaHelper.getImsi(context));
			json.put("clientMac", MetaHelper.getMACAddress(context));
			json.put("osVersion", MetaHelper.getOsVersion(context));
			json.put("language", MetaHelper.getLanguage(context));
			json.put("timeZone", MetaHelper.getTimeZone(context));
			json.put("timeZoneID", MetaHelper.getTimeZoneId(context));
			json.put("pushResult", pushFlag);
		}
		catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		OutputStream output = null;
		InputStream is = null;
		HttpURLConnection conn = null;
		String content = null;
		try {
			conn = (HttpURLConnection) new URL(serverLogUrl).openConnection();
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			byte[] bodyBytes = json.toString().getBytes(HTTP.UTF_8);
			output = conn.getOutputStream();
			output.write(bodyBytes);
			is = conn.getInputStream();
			InputStreamReader reader = new InputStreamReader(is, "UTF-8");
			StringBuilder builder = new StringBuilder();
			char[] readChars = new char[1024];
			String temp = null;
			int result = -1;
			while ((result = reader.read(readChars, 0, 1024)) != -1) {
				temp = new String(readChars, 0, result);
				builder.append(temp);
			}
			reader.close();
			content = builder.toString();
		}
		catch (ConnectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			try {
				output.close();
			}
			catch (Exception e) {
			}
			try {
				is.close();
			}
			catch (Exception e) {
			}
			if (conn != null)
				conn.disconnect();

		}

	}

	private static void registerToService(final Context context, final String regId, final String rcId) {

		JSONObject json = new JSONObject();
		try {
			json.put("userKey", regId);
			json.put("appId",Constants.APP_ID);
			json.put("rcId", rcId);
			//TODO 设置真是token
			json.put("token", USER_TOKEN);
			json.put("deviceId", MetaHelper.getMACAddress(context));
			/*
			json.put("packageName", MetaHelper.getAppName(context));
			json.put("status", STATUS_CREATE_USERINFO);
			json.put("deviceID", MetaHelper.getImsi(context));
			json.put("clientMac", MetaHelper.getMACAddress(context));
			json.put("osVersion", MetaHelper.getOsVersion(context));
			json.put("language", MetaHelper.getLanguage(context));
			json.put("timeZone", MetaHelper.getTimeZone(context));
			json.put("timeZoneID", MetaHelper.getTimeZoneId(context));
			*/
		}
		catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		OutputStream output = null;
		InputStream is = null;
		HttpURLConnection conn = null;
		String content = null;
		try {
			conn = (HttpURLConnection) new URL(serverUrl).openConnection();
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			byte[] bodyBytes = json.toString().getBytes(HTTP.UTF_8);
			output = conn.getOutputStream();
			output.write(bodyBytes);
			is = conn.getInputStream();
			InputStreamReader reader = new InputStreamReader(is, "UTF-8");
			StringBuilder builder = new StringBuilder();
			char[] readChars = new char[1024];
			String temp = null;
			int result = -1;
			while ((result = reader.read(readChars, 0, 1024)) != -1) {
				temp = new String(readChars, 0, result);
				builder.append(temp);
			}
			reader.close();
			content = builder.toString();
		}
		catch (ConnectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			try {
				output.close();
			}
			catch (Exception e) {
			}
			try {
				is.close();
			}
			catch (Exception e) {
			}
			if (conn != null)
				conn.disconnect();

		}

		try {
			JSONObject resultJSON = new JSONObject(content);
			if (resultJSON.has(status)) {
				int flag = resultJSON.getInt(status);
				if (flag == 10000) {
					GCMRegistrar.setRegisteredOnServer(context, true,RC_ID);
				}else{
					GCMRegistrar.setRegisteredOnServer(context, false,RC_ID);
				}
			}else{
				GCMRegistrar.setRegisteredOnServer(context, false,RC_ID);
			}
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			GCMRegistrar.setRegisteredOnServer(context, false,RC_ID);
		}
	}

	/**
	 * Unregister this account/device pair within the server.
	 */
	static void unregister(final Context context, final String regId) {

	}
	
	
	static void setGcmMessageCount(final Context context,String key, int count){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		pref.edit().putInt(key, count).commit();
	}
	
	static int getGcmMessageCount(final Context context,String key){
		int count = 0;
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		count = pref.getInt(key, count);
		return count;
	}
}
