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
import com.rcplatform.phototalk.request.Request;
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
				try {
					PhotoTalkApplication app = (PhotoTalkApplication) context.getApplicationContext();
					UserInfo user = app.getCurrentUser();

					// 获取位置管理服务
					LocationManager locationManager;
					String serviceName = Context.LOCATION_SERVICE;
					locationManager = (LocationManager) context.getSystemService(serviceName);

					if (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
						Log.d("gps", "open");
					}

					String provider = LocationManager.GPS_PROVIDER; // 获取GPS信息
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

					Request request = new Request(context, PhotoTalkApiUrl.CLIENT_LOG_URL, null);
					request.putParam("rcId", user.getRcId());
					request.putParam("appId", Constants.APP_ID);
					request.putParam("deviceId", MetaHelper.getMACAddress(context));
					request.putParam("brand", MetaHelper.getPhoneBrand());
					request.putParam("model", MetaHelper.getPhoneModel());

					request.putParam("gps", latLongInfo);
					request.putParam("timezoneId", MetaHelper.getTimeZoneId(context) + "");
					request.putParam("token", user.getToken());
					request.putParam("platform", "1");
					request.putParam("language", MetaHelper.getLanguage(context));
					request.excuteAsync();
				}
				catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		thread.start();
	}
}
