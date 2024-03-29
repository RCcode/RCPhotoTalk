package com.rcplatform.phototalk.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.widget.ProfilePictureView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.FriendSourse;
import com.rcplatform.phototalk.bean.FriendType;
import com.rcplatform.phototalk.image.downloader.ImageOptionsFactory;
import com.rcplatform.phototalk.image.downloader.RCPlatformImageLoader;
import com.rcplatform.phototalk.listener.RCPlatformOnClickListener;
import com.rcplatform.phototalk.utils.AppSelfInfo;

public class PhotoTalkFriendsAdapter extends BaseExpandableListAdapter {
	public static final int TYPE_RECOMMENDS = 0;
	public static final int TYPE_CONTACTS = 1;
	public static final int TYPE_FACEBOOK = 2;
	public static final int TYPE_FRIEND_ADDED = 3;
	public static final int TYPE_FRIEND_NEW = 4;

	public static final int TYPE_CHILD_RECOMMENT = 0;
	public static final int TYPE_CHILD_CONTACT = 1;
	public static final int TYPE_CHILD_FACEBOOK = 2;
	public static final int TYPE_CHILD_FRIEND_ADDED = 3;

	private Map<Integer, List<Friend>> mFriends;
	private List<Integer> mTitles;
	private LayoutInflater mInflater;

	private ImageLoader mImageLoader;

	private OnFriendAddListener mOnFriendAddListener;
	private OnFriendPortraitListener mOnFriendPortraitListener;
	private OnCheckBoxChangedListener mCheckBoxChangedListener;

	private Context mContext;
	private Set<Friend> mWillInvateFriends;
	private String[] mLetters;

	public PhotoTalkFriendsAdapter(Context context, Map<Integer, List<Friend>> friends, Set<Friend> willInvateFriends, ImageLoader imageLoader) {
		mInflater = LayoutInflater.from(context);
		this.mImageLoader = imageLoader;
		this.mContext = context;
		mWillInvateFriends = willInvateFriends;
		mRightClickListener = new RCPlatformOnClickListener(mContext) {

			@Override
			public void onViewClick(View v) {
				// TODO Auto-generated method stub
				Friend friend = (Friend) v.getTag();
				if (mOnFriendAddListener != null) {
					mOnFriendAddListener.addFriend(friend, null);
				}
			}
		};
		initData(friends);
	}

	private void initData(Map<Integer, List<Friend>> friends) {
		this.mFriends = friends;
		mTitles = new ArrayList<Integer>(mFriends.keySet());
		if (mTitles.contains(Integer.valueOf(TYPE_FRIEND_ADDED))) {
			List<Friend> addedFriends = mFriends.get(Integer.valueOf(TYPE_FRIEND_ADDED));
			mLetters = new String[addedFriends.size()];
			for (int i = 0; i < mLetters.length; i++) {
				mLetters[i] = addedFriends.get(i).getLetter();
			}
		}
	}

	private boolean isNeedToShowLetter(int position) {
		return position > 0 ? (!mLetters[position].equals(mLetters[position - 1])) : true;
	}

	@Override
	public int getGroupCount() {
		return mTitles.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return mFriends.get(mTitles.get(groupPosition)).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return null;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return mFriends.get(mTitles.get(groupPosition)).get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return mTitles.get(groupPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.my_friend_list_item_header, null);
		}
		TextView tvTitle = (TextView) convertView.findViewById(R.id.content_title);
		int titleType = mTitles.get(groupPosition);
		if (titleType == TYPE_RECOMMENDS) {
			tvTitle.setText(R.string.firend_list_used_photochat_friend_title);
		} else if (titleType == TYPE_FRIEND_ADDED) {
			tvTitle.setText(R.string.my_friends_added);
		} else if (titleType == TYPE_FRIEND_NEW) {
			tvTitle.setText(R.string.my_friends_new);
		} else {
			tvTitle.setText(R.string.firend_list_invite_friend_title);
		}
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		Friend friend = mFriends.get(mTitles.get(groupPosition)).get(childPosition);
		if (getChildType(groupPosition, childPosition) == TYPE_CHILD_RECOMMENT) {
			convertView = getRecommentFriendView(convertView, parent, friend);
		} else if (getChildType(groupPosition, childPosition) == TYPE_CONTACTS)
			convertView = getContactFriendView(convertView, parent, friend);
		else if (getChildType(groupPosition, childPosition) == TYPE_CHILD_FACEBOOK)
			convertView = getFacebookView(convertView, parent, friend);
		else if (getChildType(groupPosition, childPosition) == TYPE_CHILD_FRIEND_ADDED)
			convertView = getFriendView(convertView, parent, friend, childPosition);
		return convertView;
	}

	private View getContactFriendView(View convertView, ViewGroup parent, final Friend friend) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.invite_friend_list_item, null);
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.diaplay_chat_name);
			holder.number = (TextView) convertView.findViewById(R.id.display_number);
			holder.checkBox = (CheckBox) convertView.findViewById(R.id.add_friend_invite_checkbox);
			holder.sendMessageBtn = (Button) convertView.findViewById(R.id.add_friend_invite_single_btn);
			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.name.setText(friend.getNick());
		holder.number.setVisibility(View.VISIBLE);
		holder.sendMessageBtn.setVisibility(View.GONE);
		holder.checkBox.setVisibility(View.VISIBLE);
		holder.checkBox.setOnCheckedChangeListener(null);
		holder.number.setText(friend.getPhone());
		if (mWillInvateFriends.contains(friend)) {
			holder.checkBox.setChecked(true);
		} else {
			holder.checkBox.setChecked(false);
		}
		holder.checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (mCheckBoxChangedListener != null) {
					mCheckBoxChangedListener.onChange(friend, isChecked);
				}
				if (isChecked)
					mWillInvateFriends.add(friend);
				else
					mWillInvateFriends.remove(friend);
			}
		});

		return convertView;
	}

	class ViewHolder {

		Button sendMessageBtn;

		TextView number;

		TextView name;

		CheckBox checkBox;
	}

	private View getFacebookView(View convertView, ViewGroup praent, final Friend friend) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.facebook_friend_item, null);
		}
		ProfilePictureView head = (ProfilePictureView) convertView.findViewById(R.id.facebook_ppv);
		head.setProfileId(friend.getSuid());
		TextView tvName = (TextView) convertView.findViewById(R.id.tv_nick);
		tvName.setText(friend.getNick());
		CheckBox cbInvite = (CheckBox) convertView.findViewById(R.id.cb_invite);
		cbInvite.setOnCheckedChangeListener(null);
		if (!mWillInvateFriends.contains(friend)) {
			cbInvite.setChecked(false);
		} else {
			cbInvite.setChecked(true);
		}
		cbInvite.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (mCheckBoxChangedListener != null) {
					mCheckBoxChangedListener.onChange(friend, isChecked);
				}
				if (isChecked)
					mWillInvateFriends.add(friend);
				else
					mWillInvateFriends.remove(friend);
			}
		});

		return convertView;
	}

	private OnClickListener mRightClickListener;

	private View getRecommentFriendView(View convertView, ViewGroup parent, final Friend friend) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.add_friend_list_item, null);
		}
		ImageView portraitImage = (ImageView) convertView.findViewById(R.id.add_friend_list_item_portrait);
		TextView nickTextView = (TextView) convertView.findViewById(R.id.add_friend_list_item_name);
		final Button addFriendBtn = (Button) convertView.findViewById(R.id.add_friend_button);

		View sourceView = convertView.findViewById(R.id.add_friend_list_item_source);
		FriendSourse source = friend.getSource();
		if (source == null) {
			sourceView.setVisibility(View.GONE);
		} else {
			sourceView.setVisibility(View.VISIBLE);
			TextView tvFrom = (TextView) sourceView.findViewById(R.id.add_friend_list_item_source_from);
			TextView tvName = (TextView) sourceView.findViewById(R.id.add_friend_list_item_source_name);
			switch (source.getAttrType()) {
			case FriendType.CONTACT:
				tvFrom.setText(R.string.contact_friend);
				break;
			case FriendType.FACEBOOK:
				tvFrom.setText(R.string.facebook_friend);
				break;
			}
			tvName.setText(source.getName());
		}

		RCPlatformImageLoader.loadImage(mContext, mImageLoader, ImageOptionsFactory.getPublishImageOptions(), friend.getHeadUrl(), AppSelfInfo.ImageScaleInfo.thumbnailImageWidthPx, portraitImage, R.drawable.default_head);
		portraitImage.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mOnFriendPortraitListener != null) {
					mOnFriendPortraitListener.showFriendDetail(v, friend);
				}
			}
		});

		nickTextView.setText(friend.getNick());
		if (friend.getStatus() == Friend.USER_STATUS_NOT_FRIEND) {
			addFriendBtn.setEnabled(true);
			addFriendBtn.setTag(friend);
			addFriendBtn.setOnClickListener(mRightClickListener);
		} else {
			addFriendBtn.setEnabled(false);
		}
		return convertView;
	}

	private View getFriendView(View convertView, ViewGroup parent, final Friend friend, int position) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.friend_item, null);
		}
		View letterView = convertView.findViewById(R.id.linear_letter);
		if (isNeedToShowLetter(position)) {
			letterView.setVisibility(View.VISIBLE);
			TextView tvLetter = (TextView) letterView.findViewById(R.id.tv_letter);
			tvLetter.setText(friend.getLetter());
		} else {
			letterView.setVisibility(View.GONE);
		}
		ImageView ivHead = (ImageView) convertView.findViewById(R.id.iv_head);
		TextView tvNick = (TextView) convertView.findViewById(R.id.tv_nick);
		RCPlatformImageLoader.displayImage(mContext, ivHead, friend.getHeadUrl(), mImageLoader);
		tvNick.setText(friend.getNick());
		return convertView;
	}

	@Override
	public int getChildType(int groupPosition, int childPosition) {
		int type = mTitles.get(groupPosition);
		if (type == TYPE_RECOMMENDS)
			return TYPE_CHILD_RECOMMENT;
		else if (type == TYPE_FACEBOOK)
			return TYPE_CHILD_FACEBOOK;
		else if (type == TYPE_CONTACTS)
			return TYPE_CHILD_CONTACT;
		else if (type == TYPE_FRIEND_ADDED || type == TYPE_FRIEND_NEW)
			return TYPE_CHILD_FRIEND_ADDED;
		return -1;
	}

	@Override
	public int getChildTypeCount() {
		return 4;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	public interface OnFriendAddListener {
		void addFriend(Friend friend, Handler h);
	}

	/**
	 * 显示好友详情
	 * 
	 * @param view
	 * @param friend
	 */
	public interface OnFriendPortraitListener {
		void showFriendDetail(View view, Friend friend);
	}

	public interface OnCheckBoxChangedListener {
		void onChange(Friend friend, boolean isChecked);
	}

	public void setOnFriendPortraitListener(OnFriendPortraitListener onFriendPortraitListener) {
		this.mOnFriendPortraitListener = onFriendPortraitListener;
	}

	public void setOnFriendAddListener(OnFriendAddListener onFriendAddListener) {
		this.mOnFriendAddListener = onFriendAddListener;
	}

	public void setOnCheckBoxChangedListener(OnCheckBoxChangedListener mCheckBoxChangedListener) {
		this.mCheckBoxChangedListener = mCheckBoxChangedListener;
	}

	public void setListData(Map<Integer, List<Friend>> friends) {
		initData(friends);
		notifyDataSetChanged();
	}
}
