package com.rcplatform.phototalk.utils;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Display;
import android.view.WindowManager;

public class SystemMessageUtil {
	@SuppressLint("NewApi")
	public static int[] getScreensize(Context ctx) {
		int wh[] = new int[2];
		WindowManager manager = ((WindowManager) ctx
				.getSystemService(Context.WINDOW_SERVICE));
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			Point size = new Point();
			manager.getDefaultDisplay().getSize(size);
			wh[0] = size.x;
			wh[1] = size.y;
		} else {
			Display d = manager.getDefaultDisplay();
			wh[0] = d.getWidth();
			wh[1] = d.getHeight();
		}
		return wh;
	}

	/**
	 * 设备品牌（制造商）
	 * 
	 * @return
	 */
	public static String getPhoneBrand() {
		return android.os.Build.BRAND;
	}

	/**
	 * 设备型号
	 * 
	 * @return
	 */
	public static String getPhoneModel() {
		return android.os.Build.MODEL;
	}

	/**
	 * Unreliable for CDMA phones
	 * 
	 * @param context
	 * @return
	 */
	public static String getNetworkOperator(Context context) {
		String ret = null;
		TelephonyManager telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		if (null != telephonyManager)
			ret = telephonyManager.getNetworkOperatorName();
		return ret;
	}

	public static String getImsi(Context context) {
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService("phone");
		StringBuffer ImsiStr = new StringBuffer();
		try {
			ImsiStr.append(tm.getSubscriberId() == null ? "" : tm
					.getSubscriberId());
			while (ImsiStr.length() < 15)
				ImsiStr.append("0");
		} catch (Exception e) {
			ImsiStr.append("000000000000000");
			e.printStackTrace();
		}
		return ImsiStr.toString();
	}

	public static String getImei(Context context) {
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService("phone");
		StringBuffer tmDevice = new StringBuffer();
		try {
			tmDevice.append(tm.getDeviceId());
			while (tmDevice.length() < 15)
				tmDevice.append("0");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return tmDevice.toString().replace("null", "0000");
	}

	// 设备id
	public static String getAndroidId(Context ctx) {
		String str = null;
		try {
			str = Settings.Secure.getString(ctx.getContentResolver(),
					"android_id");
		} catch (Exception e1) {
		}
		if (str == null) {
			try {
				str = Settings.System.getString(ctx.getContentResolver(),
						"android_id");
			} catch (Exception e2) {
			}
		}
		if (str == null) {
			TelephonyManager tm = (TelephonyManager) ctx
					.getSystemService(Context.TELEPHONY_SERVICE);
			str = tm.getDeviceId();
		}
		return str;
	}

	// MAC Address
	public static String getMACAddress(Context ctx) {
		try {
			WifiManager wifiMan = (WifiManager) ctx.getSystemService("wifi");
			String mac = wifiMan.getConnectionInfo().getMacAddress();
			return mac;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static String getOsVersion(Context ctx) {
		return android.os.Build.VERSION.RELEASE + "_"
				+ android.os.Build.VERSION.SDK_INT;
	}

	public static String getTimeZone(Context ctx) {
		Configuration configuration = ctx.getResources().getConfiguration();
		Calendar calendar = Calendar.getInstance(configuration.locale);
		TimeZone timeZone = calendar.getTimeZone();
		if (timeZone == null) {
			timeZone = TimeZone.getDefault();
		}
		String name = timeZone.getID() + "/" + timeZone.getDisplayName();
		return name;

	}

	public static int getTimeZoneId(Context ctx) {
		Configuration configuration = ctx.getResources().getConfiguration();
		Calendar calendar = Calendar.getInstance(configuration.locale);
		TimeZone timeZone = calendar.getTimeZone();
		if (timeZone == null) {
			timeZone = TimeZone.getDefault();
		}
		String id = timeZone.getID();
		return timeZone.getRawOffset() / (60 * 60 * 1000);

	}

	public static String getLanguage(Context ctx) {
		Configuration configuration = ctx.getResources().getConfiguration();
		String language = configuration.locale.getLanguage();
		if (language == null) {
			language = Locale.getDefault().getDisplayLanguage();
		}
		return language;
	}

	public static String getAppName(Context ctx) {
		PackageInfo packageInfo = null;
		try {
			packageInfo = ctx.getPackageManager().getPackageInfo(
					ctx.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return packageInfo.packageName;
	}

	public static boolean isActivityFront(Activity activity) {

		ActivityManager am = (ActivityManager) activity
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> runningTasks = am.getRunningTasks(30);
		String myPkg = activity.getPackageName();
		if (!runningTasks.isEmpty()) {
			RunningTaskInfo topTask = runningTasks.get(0);
			android.content.ComponentName topActivity = topTask.topActivity;
			String packageName = topActivity.getPackageName();
			if (packageName.equals(myPkg)
					&& topActivity.getClassName().equals(
							activity.getClass().getName())) {
				return true;
			}
		}
		return false;
	}

	public static boolean isApplicationFront(Context context) {

		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> appProcesses = activityManager
				.getRunningAppProcesses();
		if (appProcesses == null) {
			return false;
		}
		final String packageName = context.getPackageName();
		for (RunningAppProcessInfo appProcess : appProcesses) {
			if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND
					&& appProcess.processName.equals(packageName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param context
	 * @return
	 */
	public static Location getLocation(Context context) {
		try {
			Location location = null;
			LocationManager lm = (LocationManager) context
					.getSystemService(Context.LOCATION_SERVICE);
			// android.permission.ACCESS_FINE_LOCATION
			location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (location != null) {
				return location;
			}
			// android.permission.ACCESS_COARSE_LOCATION
			location = lm
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if (location != null) {
				return location;
			}
			Criteria criteria = new Criteria();
			criteria.setAccuracy(1);
			criteria.setAltitudeRequired(false);
			criteria.setBearingRequired(false);
			criteria.setCostAllowed(true);
			criteria.setPowerRequirement(1);

			String provider = lm.getBestProvider(criteria, true);
			if (provider != null && provider.length() > 0) {
				location = lm.getLastKnownLocation(provider);
			}
			return location;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean isNetworkAvailable(Context context) {
		boolean ret = false;
		ConnectivityManager conMgr = (ConnectivityManager) context
				.getApplicationContext().getSystemService(
						Context.CONNECTIVITY_SERVICE);
		if (conMgr != null) {
			NetworkInfo i = conMgr.getActiveNetworkInfo();
			if (i != null && i.isConnected() && i.isAvailable()) {
				ret = true;
			}
		}
		return ret;
	}

	public static String getNetworkName(Context context) {
		String netName = "";
		ConnectivityManager conMan = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		// mobile 3G Data Network
		State mobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
				.getState();
		State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
				.getState();
		if (mobile == State.CONNECTED || mobile == State.CONNECTING) {
			netName = mobile.name();
		}
		if (wifi == State.CONNECTED || wifi == State.CONNECTING) {
			netName = wifi.name();
		}
		return netName;
	}
	
	 public static String getPhoneNumber(Context context){  
         TelephonyManager mTelephonyMgr;  
         mTelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);   
         return mTelephonyMgr.getLine1Number();  
	 }
	 
	 private static String getWebUrl(String url) {
			if (url.startsWith("market")) {
				return "http://play.google.com/store/apps/details?id="
						+ parsePackageName(url);
			} else if (url.startsWith("http")) {
				return url;
			} else {
				return "";
			}
		}
	 private static String parsePackageName(String url) {
			try {
				return url.substring(url.lastIndexOf("=") + 1);
			} catch (Exception e) {
				e.printStackTrace();
				return "";
			}
		}
		private static String getAndroidUrl(String url) {
			if (url.startsWith("market")) {
				return url;
			} else if (url.startsWith("http")) {
				return "market://details?id=" + parsePackageName(url);
			} else {
				return "";
			}
		}

		public static boolean enterPage(String url, Context packageContext) {
			if (TextUtils.isEmpty(url)) {
				return false;
			}
			if (!goGoogleplay(packageContext, url)) {
				return goWebPage(packageContext, url);
			}
			return true;
		}

		private static boolean goGoogleplay(Context packageContext, String url) {
			url = getAndroidUrl(url);
			try {
				Uri uri = Uri.parse(url);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				// 指定GooglePlay
				intent.setClassName("com.android.vending",
						"com.google.android.finsky.activities.MainActivity");
				packageContext.startActivity(intent);
				return true;
			} catch (ActivityNotFoundException e) {
			}
			try {
				Uri uri = Uri.parse(url);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				// 指定老版本GooglePlay
				intent.setClassName("com.android.vending",
						"com.android.vending.AssetInfoActivity");
				packageContext.startActivity(intent);
				return true;
			} catch (ActivityNotFoundException e) {
			}
			return false;
		}

		private static boolean goWebPage(Context packageContext, String url) {
			url = getWebUrl(url);
			try {
				Uri uri = Uri.parse(url);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				packageContext.startActivity(intent);
				return true;
			} catch (ActivityNotFoundException e) {
			}
			return false;
		}
}