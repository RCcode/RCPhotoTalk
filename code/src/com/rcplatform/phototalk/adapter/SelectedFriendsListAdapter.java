package com.rcplatform.phototalk.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.PhotoTalkApplication;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.FriendSourse;
import com.rcplatform.phototalk.bean.FriendType;
import com.rcplatform.phototalk.bean.UserInfo;

public class SelectedFriendsListAdapter extends BaseAdapter {

	private List<Friend> listData;
	private List<Friend> galleryData;

	private Context context;

	private String[] mLetters;

	private ViewHolder holder;

	private final ImageLoader mImageLoader;

	private UserInfo currentUser;

	// private final static Map<Integer, Boolean> statu = new HashMap<Integer,
	// Boolean>();

	private OnCheckBoxChangedListener mCheckBoxChangedListener;

	public interface OnCheckBoxChangedListener {

		void onChange(Friend friend, boolean isChecked);
	}

	public SelectedFriendsListAdapter(Context context, List<Friend> listData, List<Friend> galleryData) {
		this.listData = listData;
		this.context = context;
		this.galleryData = galleryData;
		mLetters = new String[listData.size()];
		for (int i = 0; i < mLetters.length; i++) {
			mLetters[i] = listData.get(i).getLetter();
		}
		this.mImageLoader = ImageLoader.getInstance();
		currentUser = ((PhotoTalkApplication) context.getApplicationContext()).getCurrentUser();
	}

	@Override
	public int getCount() {
		return listData != null ? listData.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		return (listData != null && listData.size() > 0) ? listData.get(position) : null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Friend friend = listData.get(position);
		// friend.setPostion(position);
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.selected_friends_list_item, null);
			holder = new ViewHolder();
			holder.head = (ImageView) convertView.findViewById(R.id.iv_sfli_head);
			holder.name = (TextView) convertView.findViewById(R.id.tv_sfli_name);
			holder.checkBox = (CheckBox) convertView.findViewById(R.id.cb_sfli);
			holder.tvLetter = (TextView) convertView.findViewById(R.id.alpha);
			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.checkBox.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (((CheckBox) v).isChecked()) {
					mCheckBoxChangedListener.onChange(friend, true);
				} else {
					mCheckBoxChangedListener.onChange(friend, false);
				}
			}
		});
		if (galleryData.contains(friend)) {
			holder.checkBox.setChecked(true);
		} else {
			holder.checkBox.setChecked(false);
		}
		mImageLoader.displayImage(friend.getHeadUrl(), holder.head);
		if (friend.getRcId().equals(currentUser.getRcId())) {
			holder.name.setText(context.getString(R.string.list_me, friend.getNickName()));
		} else {
			holder.name.setText(friend.getNickName());
		}

		String letter = friend.getLetter();
		if (!isNeedToShowLetter(position)) {
			holder.tvLetter.setVisibility(View.GONE);
		} else {
			holder.tvLetter.setVisibility(View.VISIBLE);
			letter = letter.toUpperCase();
			holder.tvLetter.setText(letter);
		}
		setFriendSourceInfo(convertView, friend);
		return convertView;

	}

	private boolean isNeedToShowLetter(int position) {
		return position > 0 ? (!mLetters[position].equals(mLetters[position - 1])) : true;
	}

	class ViewHolder {

		ImageView head;

		TextView name;

		CheckBox checkBox;

		public TextView tvLetter;
	}

	public void setOnCheckBoxChangedListener(OnCheckBoxChangedListener mCheckBoxChangedListener) {
		this.mCheckBoxChangedListener = mCheckBoxChangedListener;
	}

	public List<Friend> getData() {
		return listData;
	}

	private void setFriendSourceInfo(View convertView, Friend friend) {
		View sourceView = convertView.findViewById(R.id.linear_friend_source);
		FriendSourse source = friend.getSource();

		if (source == null) {
			sourceView.setVisibility(View.GONE);
		} else {
			sourceView.setVisibility(View.VISIBLE);
			TextView tvName = (TextView) convertView.findViewById(R.id.tv_source_name);
			TextView tvFrom = (TextView) convertView.findViewById(R.id.tv_source_from);
			switch (source.getAttrType()) {
			case FriendType.CONTACT:
				tvFrom.setText(R.string.contact_friend);
				break;
			case FriendType.FACEBOOK:
				tvFrom.setText(R.string.facebook_friend);
				break;
			case FriendType.VK:
				tvFrom.setText(R.string.vk_friend);
				break;
			default:
				tvFrom.setText(null);
				break;
			}
			tvName.setText(source.getName());
		}
	}
}
