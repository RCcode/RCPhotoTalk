package com.rcplatform.phototalk.adapter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.AddFriendAdapter.OnFriendAddListener;
import com.rcplatform.phototalk.AddFriendAdapter.OnFriendPortraitListener;
import com.rcplatform.phototalk.MenueHandler;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.api.MenueApiFactory;
import com.rcplatform.phototalk.bean.FriendChat;
import com.rcplatform.phototalk.image.downloader.ImageOptionsFactory;
import com.rcplatform.phototalk.image.downloader.RCPlatformImageLoader;
import com.rcplatform.phototalk.utils.AppSelfInfo;

/**
 * 标题、简要说明. <br>
 * 类详细说明.
 * <p>
 * Copyright: Menue,Inc Copyright (c) 2013-3-15 下午04:54:49
 * <p>
 * Team:Menue Beijing
 * <p>
 * 
 * @author jelly.xiong@menue.com.cn
 * @version 1.0.0
 */
public class MyFriendExpandableListAdapter extends BaseExpandableListAdapter {

	// 存放父列表数据
	private List<Map<String, String>> groupData;

	// 放子列表列表数据
	private List<LinkedList<FriendChat>> childData = new LinkedList<LinkedList<FriendChat>>();

	private Context mContext;

	private ImageLoader mImageLoader;

	private OnFriendAddListener mOnFriendAddListener;

	private OnFriendPortraitListener mOnFriendPortraitListener;

	public MyFriendExpandableListAdapter(Context context, List<Map<String, String>> groupData, List<LinkedList<FriendChat>> childData) {
		this.groupData = groupData;
		this.childData = childData;
		this.mContext = context;
		this.mImageLoader = ImageLoader.getInstance();
	}

	@Override
	public int getGroupCount() {
		return groupData.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return childData.get(groupPosition).size();
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return childData.get(groupPosition).get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

		View view = convertView;
		if (view == null) {
			view = LayoutInflater.from(mContext).inflate(R.layout.my_friend_group_item, null);
		}

		TextView title = (TextView) view.findViewById(R.id.content_001);
		title.setText(getGroup(groupPosition).toString());

		ImageView indicationView = (ImageView) view.findViewById(R.id.expander_title_indication_arrow);
		if (isExpanded) {
			indicationView.setBackgroundResource(R.drawable.expander_ic_maximized);
			// image.setVisibility(View.GONE);
			// title.setVisibility(View.GONE);
		} else {
			indicationView.setBackgroundResource(R.drawable.expander_ic_minimized);
			// image.setVisibility(View.VISIBLE);
			// title.setVisibility(View.VISIBLE);
		}

		if (getGroup(groupPosition).toString().equals("我的好友")) {
			indicationView.setVisibility(View.GONE);
		} else {
			indicationView.setVisibility(View.VISIBLE);
		}

		return view;

	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view, ViewGroup parent) {

		ViewHolder viewHolder = null;
		if (view == null) {
			viewHolder = new ViewHolder();
			view = LayoutInflater.from(mContext).inflate(R.layout.my_friends_list_item, null);
			// line2
			viewHolder.line2 = view.findViewById(R.id.line2);

			viewHolder.tvLetter = (TextView) view.findViewById(R.id.letter);
			viewHolder.headMyFriend = (ImageView) view.findViewById(R.id.my_friend_list_item_portrait);
			viewHolder.tvNick2 = (TextView) view.findViewById(R.id.my_friend_item_title);
			// line1
			viewHolder.line1 = view.findViewById(R.id.line1);
			viewHolder.headSuggested = (ImageView) view.findViewById(R.id.add_friend_list_item_portrait);
			viewHolder.tvNick1 = (TextView) view.findViewById(R.id.add_friend_list_item_name);
			viewHolder.tvFrom = (TextView) view.findViewById(R.id.add_friend_list_item_source_from);
			viewHolder.tvFromName = (TextView) view.findViewById(R.id.add_friend_list_item_source_name);
			viewHolder.inviteButton = (Button) view.findViewById(R.id.add_friend_button);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}
		// data
		final FriendChat mContent = childData.get(groupPosition).get(childPosition);
		viewHolder.id = mContent.getSuid();
		// 推荐好友
		if (getGroup(groupPosition).toString().equals("好友推荐")) {
			viewHolder.line1.setVisibility(View.VISIBLE);
			viewHolder.line2.setVisibility(View.GONE);

			viewHolder.tvNick1.setText("" + mContent.getNick());
			viewHolder.tvFromName.setText("" + mContent.getNick());// ?
			viewHolder.tvFrom.setText("通讯录好友");// ?
			// 我的好友
		} else if (getGroup(groupPosition).toString().equals("我的好友")) {
			viewHolder.line1.setVisibility(View.GONE);
			viewHolder.line2.setVisibility(View.VISIBLE);

			if (childPosition == 0) {
				viewHolder.tvLetter.setVisibility(View.VISIBLE);
				if (mContent.getLetter() != null) {
					viewHolder.tvLetter.setText(mContent.getLetter().toUpperCase());
				}
			} else {
				String lastCatalog = childData.get(groupPosition).get(childPosition - 1).getLetter();
				if (mContent.getLetter() != null && lastCatalog != null) {
					if (mContent.getLetter().toUpperCase().equals(lastCatalog.toUpperCase())) {
						viewHolder.tvLetter.setVisibility(View.GONE);
					} else {
						viewHolder.tvLetter.setVisibility(View.VISIBLE);
						viewHolder.tvLetter.setText(mContent.getLetter().toUpperCase());
					}
				}
			}
			viewHolder.tvNick2.setText("" + mContent.getNick());
		}

		viewHolder.inviteButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(final View v) {
				if (mOnFriendAddListener != null) {
					mOnFriendAddListener.addFriend(mContent, new MenueHandler(mContext) {

						@Override
						public void handleMessage(Message msg) {
							switch (msg.what) {
								case MenueApiFactory.RESPONSE_STATE_SUCCESS:
									v.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.my_friend_added_friend));
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
		//
		viewHolder.headSuggested.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mOnFriendPortraitListener != null) {
					mOnFriendPortraitListener.showFriendDetail(v, mContent);
				}
			}
		});
		//
		viewHolder.headMyFriend.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mOnFriendPortraitListener != null) {
					mOnFriendPortraitListener.showFriendDetail(v, mContent);
				}
			}
		});

		RCPlatformImageLoader.loadImage(mContext, mImageLoader, ImageOptionsFactory.getPublishImageOptions(), mContent.getHeadUrl(),
		                           AppSelfInfo.ImageScaleInfo.thumbnailImageWidthPx, viewHolder.headSuggested, R.drawable.default_head);

		return view;

	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return groupData.get(groupPosition).get("group_text").toString();
	}

	public final static class ViewHolder {

		public String id;

		View line1;

		View line2;

		ImageView headSuggested;

		ImageView headMyFriend;

		Button inviteButton;

		TextView tvNick2;

		View letterLayout;

		TextView tvLetter;

		TextView tvNick1;

		TextView tvFrom;

		TextView tvFromName;

	}

	public void setOnFriendAddListener(OnFriendAddListener mOnFriendAddListener) {
		this.mOnFriendAddListener = mOnFriendAddListener;
	}

	public void setOnFriendPortraitListener(OnFriendPortraitListener mOnFriendPortraitListener) {
		this.mOnFriendPortraitListener = mOnFriendPortraitListener;
	}

}
