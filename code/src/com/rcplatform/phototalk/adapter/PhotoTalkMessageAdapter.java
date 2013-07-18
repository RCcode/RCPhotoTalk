package com.rcplatform.phototalk.adapter;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.InformationState;
import com.rcplatform.phototalk.bean.InformationType;
import com.rcplatform.phototalk.bean.PhotoInformationType;
import com.rcplatform.phototalk.galhttprequest.LogUtil;
import com.rcplatform.phototalk.image.downloader.RCPlatformImageLoader;
import com.rcplatform.phototalk.logic.LogicUtils;
import com.rcplatform.phototalk.task.AddFriendTask;
import com.rcplatform.phototalk.task.AddFriendTask.AddFriendListener;
import com.rcplatform.phototalk.utils.PhotoTalkUtils;
import com.rcplatform.phototalk.utils.RCPlatformTextUtil;
import com.rcplatform.phototalk.views.RecordTimerLimitView;

public class PhotoTalkMessageAdapter extends BaseAdapter {

	private final LinkedList<Information> data = new LinkedList<Information>();

	private final Context context;

	private ImageLoader mImageLoader;

	private Information mPressedInformation;
	private int mPressedPosition = -1;
	private ListView mList;
	private LayoutInflater mInflater;

	public PhotoTalkMessageAdapter(Context context, List<Information> data, ListView list, ImageLoader imageLoader) {
		this.data.addAll(data);
		this.context = context;
		this.mImageLoader = imageLoader;
		this.mList = list;
		mInflater = LayoutInflater.from(context);
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		//
		final Information record = data.get(position);
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.home_user_record_list_item, null);
			holder = new ViewHolder();
			holder.head = (ImageView) convertView.findViewById(R.id.iv_record_item_head);
			holder.item_new = (ImageView) convertView.findViewById(R.id.item_new);
			holder.name = (TextView) convertView.findViewById(R.id.tv_record_item_name);
			holder.statu = (TextView) convertView.findViewById(R.id.tv_record_item_statu);
			holder.bar = (ProgressBar) convertView.findViewById(R.id.progress_home_record);
			holder.statuButton = (RecordTimerLimitView) convertView.findViewById(R.id.btn_record_item_statu_button);
			holder.ivDrift = (ImageView) convertView.findViewById(R.id.iv_country);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		convertView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					mPressedInformation = record;
					mPressedPosition = position;
				} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
					LogUtil.e("move");
				}
				return false;
			}
		});
		String tagBase = PhotoTalkUtils.getInformationTagBase(record);
		String buttonTag = tagBase + Button.class.getName();
		holder.statuButton.setTag(buttonTag);
		String statuTag = tagBase + TextView.class.getName();
		holder.statu.setTag(statuTag);
		holder.bar.setTag(tagBase + ProgressBar.class.getName());
		holder.item_new.setTag(tagBase + ImageView.class.getName());
		if (record.getType() == InformationType.TYPE_PICTURE_OR_VIDEO && record.getStatu() != InformationState.PhotoInformationState.STATU_NOTICE_OPENED
				&& !LogicUtils.isSender(context, record)) {
			if (record.isHasVoice()) {
				holder.item_new.setImageResource(R.drawable.new_item_voice);
			} else {
				holder.item_new.setImageResource(R.drawable.item_new_bg);
			}
			holder.item_new.setVisibility(View.VISIBLE);
		} else {
			holder.item_new.setVisibility(View.GONE);
		}

		if (record.getType() == InformationType.TYPE_PICTURE_OR_VIDEO || record.getType() == InformationType.TYPE_SYSTEM_NOTICE) {
			if (!LogicUtils.isSender(context, record)) {
				initPhotoInformationReceiverView(record, statuTag, buttonTag, holder);
			} else {
				initPhotoInformationSenderView(record, holder);
			}
		} else if (record.getType() == InformationType.TYPE_FRIEND_REQUEST_NOTICE) {// 是通知
			holder.bar.setVisibility(View.GONE);
			holder.statuButton.stopTask();
			holder.statuButton.setText(null);
			holder.statu.setText(RCPlatformTextUtil.getTextFromTimeToNow(context, record.getReceiveTime()));

			// 如果是对方添加我
			if (!LogicUtils.isSender(context, record)) {
				initFriendInformationReceiverView(record, holder);
			} else { // 我添加别人为好友
				// 1 如果对方设置了所有人都可以发送图片，那么item里面显示 状态显示：添加 XX为好友，隐藏按钮， 对应上面 1
				initFriendInformationSenderView(record, holder);
			}
		} else if (record.getType() == InformationType.TYPE_SYSTEM_NOTICE) {
			holder.bar.setVisibility(View.GONE);
			holder.statuButton.setBackgroundDrawable(null);
			holder.statu.setText("system notice");
		}
		if (LogicUtils.isSender(context, record)) {
			mImageLoader.displayImage(record.getReceiver().getHeadUrl(), holder.head);
		} else {
			mImageLoader.displayImage(record.getSender().getHeadUrl(), holder.head);
		}
		if (record.getType() != InformationType.TYPE_FRIEND_REQUEST_NOTICE) {
			if (!LogicUtils.isSender(context, record)) {
				holder.name.setText(record.getSender().getNick());
			} else {
				holder.name.setText(record.getReceiver().getNick());
			}
		}
		if (record.getType() == InformationType.TYPE_PICTURE_OR_VIDEO && !LogicUtils.isSender(context, record)
				&& record.getStatu() != InformationState.PhotoInformationState.STATU_NOTICE_OPENED) {
			holder.name.getPaint().setFakeBoldText(true);
		} else {
			holder.name.getPaint().setFakeBoldText(false);
		}
		if (record.getPhotoType() == PhotoInformationType.TYPE_DRIFT)
			holder.ivDrift.setImageResource(R.drawable.drift_item_icon);
		else
			holder.ivDrift.setImageBitmap(null);
		return convertView;
	}

	private void initFriendInformationSenderView(Information record, ViewHolder holder) {
		holder.statuButton.setVisibility(View.VISIBLE);
		holder.statuButton.setEnabled(false);
		if (record.getStatu() == InformationState.FriendRequestInformationState.STATU_QEQUEST_ADD_REQUEST) {
			holder.name.setText(context.getString(R.string.added_friend, record.getReceiver().getNick()));
			holder.statuButton.setBackgroundResource(R.drawable.added);
		}
		// 2 如果对方更多里面内设置了只有好友可以发送图片，那么item 里面状态显示，等待 XX 确认好友请求 ，隐藏按钮
		else if (record.getStatu() == InformationState.FriendRequestInformationState.STATU_QEQUEST_ADD_CONFIRM) {
			holder.name.setText(context.getString(R.string.added_friend_confirm, record.getReceiver().getNick()));
			holder.statuButton.setBackgroundResource(R.drawable.added);
		}
	}

	private void initFriendInformationReceiverView(final Information record, ViewHolder holder) {
		holder.statuButton.setVisibility(View.VISIBLE);
		holder.statuButton.setText(null);
		// 1. 如果更多里面设置了所有人都可以给我发送图片,那么item里面状态显示： XX 将加我为好友，并显示添加按钮
		if (record.getStatu() == InformationState.FriendRequestInformationState.STATU_QEQUEST_ADD_REQUEST) {
			holder.name.setText(context.getString(R.string.added_by_friend, record.getSender().getNick()));
			holder.statuButton.setEnabled(true);
			holder.statuButton.setBackgroundResource(R.drawable.add_friend_bg);
		}
		// 2.1 点击了确认添加对方为好友好友后， 添加 XX为好友，隐藏添加按钮
		else if (record.getStatu() == InformationState.FriendRequestInformationState.STATU_QEQUEST_ADD_CONFIRM) {
			holder.name.setText(context.getString(R.string.added_friend_back, record.getSender().getNick()));
			holder.statuButton.setEnabled(false);
			holder.statuButton.setBackgroundResource(R.drawable.added);
		}
		if (record.getStatu() == InformationState.FriendRequestInformationState.STATU_QEQUEST_ADD_REQUEST) {
			holder.statuButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					addAsFriend(record);
				}
			});
		} else {
			holder.statuButton.setOnClickListener(null);
		}

	}

	private void initPhotoInformationReceiverView(final Information record, String statuTag, String buttonTag, ViewHolder holder) {
		holder.statuButton.setBackgroundDrawable(null);
		holder.statuButton.setText(null);
		if (record.getStatu() == InformationState.PhotoInformationState.STATU_NOTICE_SENDED_OR_NEED_LOADD) {
			holder.bar.setVisibility(View.VISIBLE);
			holder.statu.setText(R.string.receive_downloading);
			RCPlatformImageLoader.LoadPictureForList(context, mList, record);
			holder.statuButton.stopTask();
			// 状态为2，表示已经下载了，但是未查看，
		} else if (record.getStatu() == InformationState.PhotoInformationState.STATU_NOTICE_DELIVERED_OR_LOADED) {
			if (RCPlatformImageLoader.isFileExist(context, record.getUrl())) {
				// 如果缓存文件存在
				holder.bar.setVisibility(View.GONE);
				holder.statuButton.stopTask();
				holder.statu.setText(getTimeText(R.string.receive_loaded, record.getReceiveTime()));
			} else {
				// 如果缓存文件不存在
				holder.bar.setVisibility(View.VISIBLE);
				holder.statu.setText(R.string.receive_downloading);
				RCPlatformImageLoader.LoadPictureForList(context, mList, record);
				holder.statuButton.stopTask();
			}
			// 状态为4.表示正在查看
		} else if (record.getStatu() == InformationState.PhotoInformationState.STATU_NOTICE_SHOWING) {
			holder.bar.setVisibility(View.GONE);
			holder.statuButton.setVisibility(View.VISIBLE);
			holder.statuButton.setBackgroundResource(R.drawable.item_time_bg);
			holder.statuButton.scheuleTask(record);
			holder.statu.setText(getTimeText(R.string.receive_loaded, record.getReceiveTime()));
			// 状态为3 表示 已经查看，
		} else if (record.getStatu() == InformationState.PhotoInformationState.STATU_NOTICE_OPENED) {
			holder.bar.setVisibility(View.GONE);
			holder.statuButton.stopTask();
			holder.statu.setText(getTimeText(R.string.receive_looked, record.getReceiveTime()));

			// 状态为5 表示正在下载
		} else if (record.getStatu() == InformationState.PhotoInformationState.STATU_NOTICE_SENDING_OR_LOADING) {
			holder.bar.setVisibility(View.VISIBLE);
			holder.statu.setText(R.string.receive_downloading);
			holder.statuButton.stopTask();
			// 7 下载失败
		} else if (record.getStatu() == InformationState.PhotoInformationState.STATU_NOTICE_SEND_OR_LOAD_FAIL) {
			holder.bar.setVisibility(View.GONE);
			holder.statu.setText(R.string.receive_fail);
			holder.statuButton.stopTask();
			holder.statuButton.setVisibility(View.VISIBLE);
			holder.statuButton.setBackgroundResource(R.drawable.send_failed);
		}
	}

	private void initPhotoInformationSenderView(Information record, ViewHolder holder) {
		// 如果当前用户是发送者
		holder.statuButton.setVisibility(View.VISIBLE);
		holder.statuButton.setBackgroundResource(R.drawable.send_arrows);
		holder.statuButton.setText(null);
		holder.statuButton.stopTask();
		// 状态为1 表示已经发送到服务器
		if (record.getStatu() == InformationState.PhotoInformationState.STATU_NOTICE_SENDED_OR_NEED_LOADD) {
			holder.bar.setVisibility(View.GONE);
			holder.statu.setText(getTimeText(R.string.send_sended, record.getReceiveTime()));
			// 状态为2表示对方已经下载
		} else if (record.getStatu() == InformationState.PhotoInformationState.STATU_NOTICE_DELIVERED_OR_LOADED) {
			holder.bar.setVisibility(View.GONE);
			holder.statu.setText(getTimeText(R.string.send_received, record.getReceiveTime()));
			// 状态为3 表示已经查看
		} else if (record.getStatu() == InformationState.PhotoInformationState.STATU_NOTICE_OPENED) {
			holder.bar.setVisibility(View.GONE);
			holder.statu.setText(getTimeText(R.string.send_looked, record.getReceiveTime()));
			// 0 表示正在发送
		} else if (record.getStatu() == InformationState.PhotoInformationState.STATU_NOTICE_SENDING_OR_LOADING) {
			holder.bar.setVisibility(View.VISIBLE);
			holder.statu.setText(R.string.send_sending);
			// 6 表示发送失败
		} else if (record.getStatu() == InformationState.PhotoInformationState.STATU_NOTICE_SEND_OR_LOAD_FAIL) {
			holder.bar.setVisibility(View.GONE);
			holder.statu.setText(R.string.send_fail);
		}
	}

	private String getTimeText(int baseResId, long time) {
		return context.getString(baseResId, RCPlatformTextUtil.getTextFromTimeToNow(context, time));
	}

	private void addAsFriend(final Information record) {
		final BaseActivity activity = (BaseActivity) context;
		activity.showLoadingDialog(false);
		Friend friend = new Friend();
		friend.setRcId(record.getSender().getRcId());
		new AddFriendTask(activity, activity.getCurrentUser(), new AddFriendListener() {

			@Override
			public void onFriendAddSuccess(Friend friend, int addType) {
				activity.dissmissLoadingDialog();
			}

			@Override
			public void onFriendAddFail(int statusCode, String content) {
				activity.dissmissLoadingDialog();
				activity.showConfirmDialog(content);
			}

			@Override
			public void onAlreadyAdded() {
				LogicUtils.friendAlreadyAdded(record);
				activity.dissmissLoadingDialog();
			}
		}, friend).execute();
	}

	class ViewHolder {
		View touchView;
		ImageView head;
		ImageView item_new;
		TextView name;
		TextView statu;
		RecordTimerLimitView statuButton;
		RelativeLayout timerLayout;
		ProgressBar bar;
		ImageView ivDrift;
	}

	public List<Information> getData() {
		return data;
	}

	public void addData(List<Information> data) {
		for (Information info : data) {
			this.data.addFirst(info);
		}
	}

	public void addDataAtLast(List<Information> data) {
		this.data.addAll(data);
	}

	public static interface OnInformationPressListener {
		public void onPress(Information information);
	}

	public Information getPressedInformation() {
		return mPressedInformation;
	}

	public void resetPressedInformation() {
		mPressedInformation = null;
		mPressedPosition = -1;
	}

	public int getPressedPosition() {
		return mPressedPosition;
	}
}
