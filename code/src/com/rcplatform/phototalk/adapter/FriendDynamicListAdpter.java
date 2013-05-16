package com.rcplatform.phototalk.adapter;

import com.rcplatform.phototalk.R;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FriendDynamicListAdpter extends BaseAdapter {
	private Context context;
	private ViewHolder viewHolder;

	public FriendDynamicListAdpter(Context context) {
		this.context = context;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 0;
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
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.friend_dynamic_list_item, null);
			viewHolder = new ViewHolder();
			viewHolder.headView = (ImageView) convertView
					.findViewById(R.id.friend_head);
			viewHolder.friendNick = (TextView) convertView
					.findViewById(R.id.friend_nick);
			viewHolder.friendMessage = (TextView) convertView
					.findViewById(R.id.friend_message);
			viewHolder.sendTime = (TextView) convertView
					.findViewById(R.id.send_time);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		// 设置头像 昵称
		// viewHolder.headView
		// 为加为好友 根据状态进行设置不同的信息内容
		if (true) {
			viewHolder.friendMessage.setText("加入了 RC PhotoTalk");
		} else {
			//设置张三字体颜色为蓝色
			setSpannaberText(viewHolder.friendMessage,"张三");
		}

		return convertView;
	}

	public void setSpannaberText(TextView view, String str) {
		String strs = context.getResources().getString(R.string.take)+str+context.getResources().getString(R.string.add_friend);
		int start = context.getResources().getString(R.string.take).length();
		int end = start+str.length();
		 SpannableStringBuilder style=new SpannableStringBuilder(strs);
		 style.setSpan(new
 BackgroundColorSpan(Color.BLUE), start, end,
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		 view.setText(style);
	}

	public class ViewHolder {
		private ImageView headView;
		private TextView friendNick, friendMessage, sendTime;

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
}
