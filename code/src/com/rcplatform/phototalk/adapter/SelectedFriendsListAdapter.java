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
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.bean.SelectFriend;

public class SelectedFriendsListAdapter extends BaseAdapter {

	private final List<SelectFriend> data;

	private final Context context;

	private final String[] mLetters;

	private ViewHolder holder;

	private final ImageLoader mImageLoader;

	// private final static Map<Integer, Boolean> statu = new HashMap<Integer,
	// Boolean>();

	private OnCheckBoxChangedListener mCheckBoxChangedListener;

	public interface OnCheckBoxChangedListener {

		void onChange(SelectFriend friend, boolean isChecked, int position);
	}

	public SelectedFriendsListAdapter(Context context, List<SelectFriend> data) {
		this.data = data;
		this.context = context;
		mLetters = new String[data.size()];
		for (int i = 0; i < mLetters.length; i++) {
			mLetters[i] = data.get(i).getLetter();
		}
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
		SelectFriend friend = data.get(position);
		// friend.setPostion(position);
		final int index = position;
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

		data.get(index).setPosition(index);
		holder.checkBox.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (((CheckBox) v).isChecked()) {
					data.get(index).setIsChosed(true);
					mCheckBoxChangedListener.onChange(data.get(index), true, index);
				} else {
					data.get(index).setIsChosed(false);
					mCheckBoxChangedListener.onChange(data.get(index), false, index);
				}
			}
		});
		holder.checkBox.setChecked(data.get(index).getIsChosed());
		mImageLoader.displayImage(friend.getHeadUrl(), holder.head);
		holder.name.setText(friend.getNickName());
		String letter = friend.getLetter();
		if (!isNeedToShowLetter(position)) {
			holder.tvLetter.setVisibility(View.GONE);
		} else {
			holder.tvLetter.setVisibility(View.VISIBLE);
			holder.tvLetter.setText(letter);
		}

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

	public List<SelectFriend> getData() {
		return data;
	}
}
