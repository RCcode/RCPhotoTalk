package com.rcplatform.phototalk.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.bean.AppInfo;
import com.rcplatform.phototalk.image.downloader.RCPlatformImageLoader;

public class AppAdapter extends BaseAdapter {
	private List<AppInfo> mApps;
	private int mAppIconWidth;
	private Context mContext;
	private ImageLoader mImageLoader;

	public AppAdapter(Context context, List<AppInfo> apps, ImageLoader imageLoader) {
		this.mApps = apps;
		this.mContext = context;
		mAppIconWidth = context.getResources().getDimensionPixelSize(R.dimen.app_icon_width);
		this.mImageLoader = imageLoader;
	}

	@Override
	public int getCount() {
		return mApps.size();
	}

	@Override
	public Object getItem(int position) {
		return mApps.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = new ImageView(mContext);
			LayoutParams params = new LayoutParams(mAppIconWidth, mAppIconWidth);
			convertView.setLayoutParams(params);
			((ImageView) convertView).setScaleType(ScaleType.FIT_XY);
		}
		AppInfo appInfo = mApps.get(position);
		ImageView iv = (ImageView) convertView;
		mImageLoader.displayImage(appInfo.getColorPicUrl(), iv);
		return convertView;
	}

}
