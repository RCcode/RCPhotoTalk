package com.rcplatform.phototalk.utils;

import java.util.ArrayList;
import java.util.List;

import com.rcplatform.phototalk.bean.AppInfo;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class PakageInfoProvider {

	private static final String TAG = "PakageInfoProvider";

	private Context context;

	private List<AppInfo> appInfos;

	private AppInfo appInfo;

	public PakageInfoProvider(Context context) {
		super();
		this.context = context;
	}

	public List<AppInfo> getAppInfo() {
		PackageManager pm = context.getPackageManager();
		List<PackageInfo> pakageinfos = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
		appInfos = new ArrayList<AppInfo>();
		for (PackageInfo packageInfo : pakageinfos) {
			appInfo = new AppInfo();
			appInfo.setAppPackage(packageInfo.packageName);
			appInfos.add(appInfo);
			appInfo = null;
		}
		return appInfos;
	}

	/**
	 * 三方应用程序的过滤器
	 * 
	 * @param info
	 * @return true 三方应用 false 系统应用
	 */
	public boolean filterApp(ApplicationInfo info) {
		if ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
			// 代表的是系统的应用,但是被用户升级了. 用户应用
			return true;
		} else if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
			// 代表的用户的应用
			return true;
		}
		return false;
	}

}
