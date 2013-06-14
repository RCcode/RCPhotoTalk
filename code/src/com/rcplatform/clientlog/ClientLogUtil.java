package com.rcplatform.clientlog;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gcm.MetaHelper;
import com.rcplatform.phototalk.PhotoTalkApplication;
import com.rcplatform.phototalk.api.PhotoTalkApiUrl;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.utils.Constants;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class ClientLogUtil {

	public static void log(final Context context) {

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				JSONObject json = new JSONObject();
				try {
					PhotoTalkApplication app = (PhotoTalkApplication) context.getApplicationContext();
					UserInfo user = app.getCurrentUser();
					json.put("rcId", user.getRcId());
					json.put("appId", Constants.APP_ID);
					json.put("deviceID", MetaHelper.getMACAddress(context));
					json.put("brand", MetaHelper.getPhoneBrand());
					json.put("model", MetaHelper.getPhoneModel());

					// 获取位置管理服务
					LocationManager locationManager;
					String serviceName = Context.LOCATION_SERVICE;
					locationManager = (LocationManager) context.getSystemService(serviceName);

					if (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
						Log.d("gps", "open");
					}

					// 查找到服务信息
					Criteria criteria = new Criteria();
					criteria.setAccuracy(Criteria.ACCURACY_FINE); // 高精度
					criteria.setAltitudeRequired(false);
					criteria.setBearingRequired(false);
					criteria.setCostAllowed(true);
					criteria.setPowerRequirement(Criteria.POWER_LOW); // 低功耗

					String provider = locationManager.getBestProvider(criteria, true); // 获取GPS信息
					Location location = locationManager.getLastKnownLocation(provider); // 通过GPS获取位置
					String latLongInfo = "";
					int count = 0;
					while (null == location) {
						if (count > 5) {
							break;
						}
						count++;
						if (location != null) {
							double lat = location.getLatitude();
							double lng = location.getLongitude();
							latLongInfo = lat + "," + lng;
						}
						try {
							Thread.sleep(2000);
						}
						catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						location = locationManager.getLastKnownLocation(provider);
					}
					json.put("gps", latLongInfo);

					json.put("timezoneId", MetaHelper.getTimeZoneId(context));
					json.put("token", user.getToken());
					json.put("platform", "1");
					json.put("language", MetaHelper.getLanguage(context));
					json.put("timeZoneID", MetaHelper.getTimeZoneId(context));
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
					conn = (HttpURLConnection) new URL(PhotoTalkApiUrl.CLIENT_LOG_URL).openConnection();
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
				catch (Exception e) {
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
		});
		thread.start();
	}
}
