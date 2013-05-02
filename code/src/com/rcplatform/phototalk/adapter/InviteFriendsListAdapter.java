package com.rcplatform.phototalk.adapter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.bean.Friend;

public class InviteFriendsListAdapter extends BaseAdapter {

	private final List<Friend> data;

	private final Context context;

	private ViewHolder holder;

	private final ImageLoader mImageLoader;

	private final Map<Integer, Boolean> statu = new HashMap<Integer, Boolean>();

	private OnCheckBoxChangedListener mCheckBoxChangedListener;

	private OnShortMessageListenr mOnShortMessageListenr;

	private boolean isShowNumber;
	private Set<String> checkedPhones = new HashSet<String>();

	public interface OnCheckBoxChangedListener {

		void onChange(Friend friend, boolean isChecked);
	}

	public interface OnShortMessageListenr {

		void sendShortMessage(String phone);

	}

	public void setOnShortMessageListenr(
			OnShortMessageListenr onShortMessageListenr) {
		this.mOnShortMessageListenr = onShortMessageListenr;
	}

	public InviteFriendsListAdapter(Context context, List<Friend> data) {
		this.data = data;
		this.context = context;
		this.mImageLoader = ImageLoader.getInstance();
		for (int i = 0; i < data.size(); i++) {
			statu.put(i, false);
		}
	}

	/**
	 * @param context
	 * @param data
	 * @param isShowNumber
	 *            是否显示电话号码。true显示，false不显示。
	 */
	public InviteFriendsListAdapter(Context context, List<Friend> data,
			boolean isShowNumber) {
		this.data = data;
		this.context = context;
		this.isShowNumber = isShowNumber;
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
		Log.i("Futao", "statu =" + statu.size() + "position = " + position);
		final Friend friend = data.get(position);
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.invite_friend_list_item, null);
			holder = new ViewHolder();
			holder.name = (TextView) convertView
					.findViewById(R.id.diaplay_chat_name);
			holder.number = (TextView) convertView
					.findViewById(R.id.display_number);
			holder.checkBox = (CheckBox) convertView
					.findViewById(R.id.add_friend_invite_checkbox);
			holder.sendMessageBtn = (Button) convertView
					.findViewById(R.id.add_friend_invite_single_btn);
			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.name.setText(friend.getNick());

		if (isShowNumber) {
			holder.number.setVisibility(View.VISIBLE);
			holder.sendMessageBtn.setVisibility(View.GONE);
			holder.checkBox.setVisibility(View.VISIBLE);
			holder.checkBox.setOnCheckedChangeListener(null);
			holder.number.setText(friend.getPhone());
			if (checkedPhones.contains(friend.getPhone())) {
				holder.checkBox.setChecked(true);
			} else {
				holder.checkBox.setChecked(false);
			}
			holder.checkBox
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							// statu.put(index, isChecked);
							// mCheckBoxChangedListener.onChange(data.get(index),
							// isChecked);
							if (mCheckBoxChangedListener != null) {
								mCheckBoxChangedListener.onChange(friend,
										isChecked);
							}
							if (isChecked)
								checkedPhones.add(friend.getPhone());
							else
								checkedPhones.remove(friend.getPhone());
						}
					});
			// boolean checked=statu.get(position);
			// holder.checkBox.setChecked(statu.get(position));

		} else {
			holder.number.setVisibility(View.GONE);
			holder.checkBox.setVisibility(View.GONE);
			holder.sendMessageBtn.setVisibility(View.VISIBLE);
			holder.sendMessageBtn
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							if (mOnShortMessageListenr != null) {
								mOnShortMessageListenr.sendShortMessage(friend
										.getPhone());
							}
						}
					});
		}

		return convertView;
	}

	class ViewHolder {

		Button sendMessageBtn;

		TextView number;

		TextView name;

		CheckBox checkBox;
	}

	public void setOnCheckBoxChangedListener(
			OnCheckBoxChangedListener mCheckBoxChangedListener) {
		this.mCheckBoxChangedListener = mCheckBoxChangedListener;
	}

	public void setShowNumber(boolean isShowNumber) {
		this.isShowNumber = isShowNumber;
	}

}
