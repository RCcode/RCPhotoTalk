package com.rcplatform.phototalk.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.utils.PhotoTalkUtils;
import com.rcplatform.phototalk.views.HeadImageView;

public class SelectedFriendsGalleryAdapter extends BaseAdapter {

	private List<Friend> data;

	private Context context;

	private ViewHolder holder;

	private final ImageLoader mImageLoader;

	private OnGalleryItemClickListener clickListener;

	public interface OnGalleryItemClickListener {

		void onClick(int positon);
	}

	public SelectedFriendsGalleryAdapter(Context context, List<Friend> data) {
		this.data = data;
		this.context = context;
		this.mImageLoader = ImageLoader.getInstance();
	}

	@Override
	public int getCount() {
		return data != null ? data.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		return (data != null && data.size() > 0) ? data.get(position) : null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Friend friend = data.get(position);
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.selected_friends_galleryt_item, null);
			holder = new ViewHolder();
			holder.head = (HeadImageView) convertView.findViewById(R.id.iv_sfgi_head);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (friend.equals(PhotoTalkUtils.getDriftFriend()))
			holder.head.setImageResource(R.drawable.send_to_strangers);
		else
			mImageLoader.displayImage(friend.getHeadUrl(), holder.head);
		return convertView;
	}

	class ViewHolder {

		HeadImageView head;

	}

	public List<Friend> getData() {
		return data;
	}

	public void setOnGalleryItemClickListener(OnGalleryItemClickListener clickListener) {
		this.clickListener = clickListener;
	}

}
