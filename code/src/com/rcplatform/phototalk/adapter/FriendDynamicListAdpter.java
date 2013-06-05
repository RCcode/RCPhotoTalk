package com.rcplatform.phototalk.adapter;

import java.util.List;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.FriendDynamicActivity.FriendDynamic;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.image.downloader.ImageOptionsFactory;
import com.rcplatform.phototalk.image.downloader.RCPlatformImageLoader;
import com.rcplatform.phototalk.request.Request;
import com.rcplatform.phototalk.request.inf.FriendDetailListener;
import com.rcplatform.phototalk.umeng.EventUtil;
import com.rcplatform.phototalk.utils.AppSelfInfo;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.RCPlatformTextUtil;
import com.rcplatform.phototalk.FriendDetailActivity;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.SettingsActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FriendDynamicListAdpter extends BaseAdapter {

	private Context context;

	private ViewHolder viewHolder;

	private List<FriendDynamic> list;

	ProgressDialog mProgressDialog;

	public FriendDynamicListAdpter(Context context, List<FriendDynamic> list) {
		this.context = context;
		this.list = list;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.friend_dynamic_list_item, null);
			viewHolder = new ViewHolder();
			viewHolder.headView = (ImageView) convertView.findViewById(R.id.friend_head);
			viewHolder.add_app_layout = (LinearLayout) convertView.findViewById(R.id.add_app_layout);
			viewHolder.add_friend_layout = (LinearLayout) convertView.findViewById(R.id.add_friend_layout);
			viewHolder.friendNick = (TextView) convertView.findViewById(R.id.friend_nick);
			viewHolder.friendMessage = (TextView) convertView.findViewById(R.id.friend_message);
			viewHolder.sendTime = (TextView) convertView.findViewById(R.id.send_time);
			viewHolder.appMessage = (TextView) convertView.findViewById(R.id.app_message);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		// 设置头像 昵称
		ImageLoader.getInstance().displayImage(list.get(position).getfRcHead(), viewHolder.headView);
		// viewHolder.headView
		// 为加为好友 根据状态进行设置不同的信息内容
		viewHolder.headView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				toFriend(list.get(position).getfRcId());
			}
		});
		viewHolder.friendNick.setText(list.get(position).getfRcName());
		viewHolder.friendNick.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				toFriend(list.get(position).getfRcId());
			}
		});

		if (list.get(position).getType() == 1) {

			viewHolder.add_friend_layout.setVisibility(View.GONE);
			viewHolder.add_app_layout.setVisibility(View.VISIBLE);
			viewHolder.appMessage.setText(context.getResources().getString(R.string.add_app, list.get(position).getOtherName()));
		} else {
			viewHolder.add_friend_layout.setVisibility(View.VISIBLE);
			viewHolder.add_app_layout.setVisibility(View.GONE);
			viewHolder.friendMessage.setText(context.getResources().getString(R.string.add_friend, list.get(position).getOtherName()));
		}
		viewHolder.friendMessage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				EventUtil.More_Setting.rcpt_friendsupdate_profileview(context);
				toFriend(list.get(position).getOtherId());
			}
		});

		Long time = Long.decode(list.get(position).getCreateTime());
		viewHolder.sendTime.setText(RCPlatformTextUtil.getTextFromTimeToNow(context, time));
		return convertView;
	}

	public class ViewHolder {

		private ImageView headView;

		private TextView friendNick, friendMessage, sendTime, appMessage;

		private LinearLayout add_friend_layout, add_app_layout;

		public TextView getAppMessage() {
			return appMessage;
		}

		public void setAppMessage(TextView appMessage) {
			this.appMessage = appMessage;
		}

		public LinearLayout getAdd_friend_layout() {
			return add_friend_layout;
		}

		public void setAdd_friend_layout(LinearLayout add_friend_layout) {
			this.add_friend_layout = add_friend_layout;
		}

		public LinearLayout getAdd_app_layout() {
			return add_app_layout;
		}

		public void setAdd_app_layout(LinearLayout add_app_layout) {
			this.add_app_layout = add_app_layout;
		}

		public ImageView getHeadView() {
			return headView;
		}

		public void setHeadView(ImageView headView) {
			this.headView = headView;
		}

		public TextView getFriendNick() {
			return friendNick;
		}

		public void setFriendNick(TextView friendNick) {
			this.friendNick = friendNick;
		}

		public TextView getFriendMessage() {
			return friendMessage;
		}

		public void setFriendMessage(TextView friendMessage) {
			this.friendMessage = friendMessage;
		}

		public TextView getSendTime() {
			return sendTime;
		}

		public void setSendTime(TextView sendTime) {
			this.sendTime = sendTime;
		}

	}

	private void startFriendDetailActivity(Friend friend) {
		Intent intent = new Intent(context, FriendDetailActivity.class);
		intent.putExtra(FriendDetailActivity.PARAM_FRIEND, friend);
		if (!friend.isFriend()) {
			intent.setAction(Constants.Action.ACTION_RECOMMEND_DETAIL);
		} else {
			intent.setAction(Constants.Action.ACTION_FRIEND_DETAIL);
		}
		context.startActivity(intent);
	}

	private void toFriend(String rcid) {
		Friend friend = new Friend();
		friend.setRcId(rcid);
		getFriendInfo(friend);
	}

	private void getFriendInfo(Friend friend) {
		showLoadingDialog(false);
		Request.executeGetFriendDetailAsync(context, friend, new FriendDetailListener() {

			@Override
			public void onSuccess(Friend friend) {
				dismissLoadingDialog();
				startFriendDetailActivity(friend);
			}

			@Override
			public void onError(int errorCode, String content) {
				dismissLoadingDialog();
				showErrorConfirmDialog(content);
			}
		}, false);
	}

	public void showErrorConfirmDialog(String msg) {
		DialogUtil.createErrorInfoDialog(context, msg).show();
	}

	public void showLoadingDialog(boolean cancelAble) {
		if (mProgressDialog == null) {
			mProgressDialog = new ProgressDialog(context);
		}
		mProgressDialog.setCancelable(cancelAble);
		mProgressDialog.setTitle(null);
		mProgressDialog.setMessage(null);
		if (!mProgressDialog.isShowing()) {
			mProgressDialog.show();
		}
	}

	public void dismissLoadingDialog() {
		if (mProgressDialog != null && mProgressDialog.isShowing())
			mProgressDialog.dismiss();
	}
}
