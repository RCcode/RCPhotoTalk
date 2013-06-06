package com.google.android.gcm;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.rcplatform.videotalk.PhotoTalkApplication;
import com.rcplatform.videotalk.utils.Constants;

public class RCGcmUtil {

	static public void pushGcmMsg(Context ctx, String type, String toRcId, String extra) {

		// TODO Auto-generated method stub
		Log.d("GcmTask", "push to service");

		JSONObject json = new JSONObject();
		try {
			// TODO
			json.put("appId", Constants.APP_ID);
			json.put("type", type);
			// TODO 设置真是token
			PhotoTalkApplication app = (PhotoTalkApplication) ctx.getApplicationContext();

			json.put("token", app.getCurrentUser().getToken());
			json.put("fRcId", toRcId);
			json.put("deviceId", MetaHelper.getMACAddress(ctx));
			json.put("rcId", app.getCurrentUser().getRcId());
			json.put("language", "");
			json.put("extra", extra);
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
			conn = (HttpURLConnection) new URL(Constants.GCM.GCM_URL).openConnection();
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

	}

}
