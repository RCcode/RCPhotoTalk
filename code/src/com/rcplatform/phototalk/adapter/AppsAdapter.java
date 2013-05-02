package com.rcplatform.phototalk.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.bean.AppBean;
import com.rcplatform.phototalk.image.downloader.ImageOptionsFactory;
import com.rcplatform.phototalk.image.downloader.RCPlatformImageLoader;
import com.rcplatform.phototalk.utils.AppSelfInfo;

public class AppsAdapter extends BaseAdapter {

	private List<AppBean> appList;

	private Context context;

	public AppsAdapter(Context context, List<AppBean> appList) {
		this.appList = appList;
		this.context = context;
	}

	@Override
	public int getCount() {
		if(appList==null)
			return 0;
		return appList.size();
	}

	@Override
	public Object getItem(int position) {
		return appList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final AppBean appBean = appList.get(position);
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.my_friend_apps_list_item, null);
		}
		ImageView iconView = (ImageView) convertView.findViewById(R.id.friend_detail_apps_ic);
		iconView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// 检查当前系统是否安装此应用，下载并安装。否则，进入该应用。
				Toast.makeText(context, "HI! " + appBean.getAppName(), Toast.LENGTH_LONG).show();
			}
		});
		RCPlatformImageLoader.loadImage(context, ImageLoader.getInstance(), ImageOptionsFactory.getPublishImageOptions(), appBean.getPicUrl(),
		                           AppSelfInfo.ImageScaleInfo.thumbnailImageWidthPx, iconView, R.drawable.default_head);
		return convertView;
	}

}
