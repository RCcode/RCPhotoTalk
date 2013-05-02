package com.rcplatform.phototalk;

import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.api.MenueApiFactory;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.FriendSourse;
import com.rcplatform.phototalk.bean.FriendType;
import com.rcplatform.phototalk.image.downloader.ImageOptionsFactory;
import com.rcplatform.phototalk.image.downloader.RCPlatformImageLoader;
import com.rcplatform.phototalk.utils.AppSelfInfo;

/**
 * 添加朋友适配器.
 * <p>
 * Copyright: Menue,Inc Copyright (c) 2013-3-4 下午05:09:25
 * <p>
 * Team:Menue Beijing
 * <p>
 * 
 * @author jelly.xiong@menue.com.cn
 * @version 1.0.0
 */
public class AddFriendAdapter extends BaseFriendAdapter<Friend> {

	private OnFriendAddListener mOnFriendAddListener;

	private OnFriendPortraitListener mOnFriendPortraitListener;

	private boolean mIsSearchView;

	private ImageLoader mImageLoader;

	public AddFriendAdapter(Context context, List<?> mFriendList) {
		super(context, mFriendList);
		mImageLoader = ImageLoader.getInstance();
	}

	@Override
	public View newView(Context context, List<?> friendLists, ViewGroup parent) {
		return LayoutInflater.from(context).inflate(R.layout.add_friend_list_item, parent, false);
	}

	@Override
	public void bindView(final Context context, View view, List<?> friendLists, int position) {

		final Friend friend = (Friend) friendLists.get(position);

		ImageView portraitImage = (ImageView) view.findViewById(R.id.add_friend_list_item_portrait);
		TextView nickTextView = (TextView) view.findViewById(R.id.add_friend_list_item_name);
		final Button addFriendBtn = (Button) view.findViewById(R.id.add_friend_button);

		View sourceView = view.findViewById(R.id.add_friend_list_item_source);
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

		RCPlatformImageLoader.loadImage(context, mImageLoader, ImageOptionsFactory.getPublishImageOptions(), friend.getHeadUrl(), AppSelfInfo.ImageScaleInfo.thumbnailImageWidthPx, portraitImage, R.drawable.default_head);
		// view friend detail.
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
			addFriendBtn.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.my_friend_add_friend));
			addFriendBtn.setClickable(true);
			// add friend
			addFriendBtn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mOnFriendAddListener != null) {
						mOnFriendAddListener.addFriend(friend, new MenueHandler(context) {

							@Override
							public void handleMessage(Message msg) {
								switch (msg.what) {
								case MenueApiFactory.RESPONSE_STATE_SUCCESS:
									addFriendBtn.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.my_friend_added_friend));
									Toast.makeText(mContext, "" + mContext.getString(R.string.firend_list_add_friend_ok), Toast.LENGTH_SHORT).show();
									addFriendBtn.setClickable(false);
									break;
								default:
									break;
								}
								super.handleMessage(msg);
							}

						});
					}
				}
			});

		} else {
			addFriendBtn.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.my_friend_added_friend));
			addFriendBtn.setClickable(false);
		}

	}

	public void setSearchView(boolean isSearchView) {
		this.mIsSearchView = isSearchView;
	}

	public void setOnFriendAddListener(OnFriendAddListener onFriendAddListener) {
		this.mOnFriendAddListener = onFriendAddListener;
	}

	public interface OnFriendAddListener {

		/**
		 * 添加好友。
		 * 
		 * @param friend
		 */
		void addFriend(Friend friend, Handler h);

	}

	public interface OnFriendPortraitListener {

		/**
		 * 显示好友详情
		 * 
		 * @param view
		 * @param friend
		 */
		void showFriendDetail(View view, Friend friend);

	}

	public void setOnFriendPortraitListener(OnFriendPortraitListener onFriendPortraitListener) {
		this.mOnFriendPortraitListener = onFriendPortraitListener;
	}

}
