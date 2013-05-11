package com.rcplatform.phototalk.adapter;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

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
import com.rcplatform.phototalk.galhttprequest.LogUtil;
import com.rcplatform.phototalk.galhttprequest.RCPlatformServiceError;
import com.rcplatform.phototalk.image.downloader.ImageOptionsFactory;
import com.rcplatform.phototalk.image.downloader.RCPlatformImageLoader;
import com.rcplatform.phototalk.logic.LogicUtils;
import com.rcplatform.phototalk.proxy.FriendsProxy;
import com.rcplatform.phototalk.request.JSONConver;
import com.rcplatform.phototalk.request.RCPlatformResponse;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.utils.AppSelfInfo;
import com.rcplatform.phototalk.utils.PhotoTalkUtils;
import com.rcplatform.phototalk.utils.RCPlatformTextUtil;
import com.rcplatform.phototalk.views.RecordTimerLimitView;

public class PhotoTalkMessageAdapter extends BaseAdapter {

	private final LinkedList<Information> data = new LinkedList<Information>();

	private final Context context;

	private ViewHolder holder;

	private final ImageLoader mImageLoader;

	private Information mPressedInformation;
	private int mPressedPosition = -1;

	public PhotoTalkMessageAdapter(Context context, List<Information> data) {
		this.data.addAll(data);
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		//
		final Information record = data.get(position);
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.home_user_record_list_item, null);
			holder = new ViewHolder();
			holder.head = (ImageView) convertView.findViewById(R.id.iv_record_item_head);
			holder.item_new = (ImageView) convertView.findViewById(R.id.item_new);
			holder.name = (TextView) convertView.findViewById(R.id.tv_record_item_name);
			holder.statu = (TextView) convertView.findViewById(R.id.tv_record_item_statu);
			holder.bar = (ProgressBar) convertView.findViewById(R.id.progress_home_record);
			holder.statuButton = (RecordTimerLimitView) convertView.findViewById(R.id.btn_record_item_statu_button);
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
			holder.item_new.setVisibility(View.VISIBLE);
		} else {
			holder.item_new.setVisibility(View.GONE);
		}

		if (record.getType() == InformationType.TYPE_PICTURE_OR_VIDEO || record.getType() == InformationType.TYPE_SYSTEM_NOTICE) {
			if (!LogicUtils.isSender(context, record)) {
				initPhotoInformationReceiverView(record, statuTag, buttonTag);
			} else {
				initPhotoInformationSenderView(record);
			}
		} else if (record.getType() == InformationType.TYPE_FRIEND_REQUEST_NOTICE) {// 是通知
			holder.bar.setVisibility(View.GONE);
			holder.statuButton.stopTask();
			holder.statuButton.setText(null);
			holder.statu.setText(RCPlatformTextUtil.getTextFromTimeToNow(context, record.getCreatetime()));

			// 如果是对方添加我
			if (!LogicUtils.isSender(context, record)) {
				initFriendInformationReceiverView(record);
			} else { // 我添加别人为好友
				// 1 如果对方设置了所有人都可以发送图片，那么item里面显示 状态显示：添加 XX为好友，隐藏按钮， 对应上面 1
				initFriendInformationSenderView(record);
			}
		} else if (record.getType() == InformationType.TYPE_SYSTEM_NOTICE) {
			holder.bar.setVisibility(View.GONE);
			holder.statuButton.setBackgroundDrawable(null);
			holder.statu.setText("system notice");
		}

		if (LogicUtils.isSender(context, record)) {
			RCPlatformImageLoader.loadImage(context, mImageLoader, ImageOptionsFactory.getListHeadOption(), record.getReceiver().getHeadUrl(),
					AppSelfInfo.ImageScaleInfo.bigImageWidthPx, holder.head, R.drawable.default_head);
		} else {
			RCPlatformImageLoader.loadImage(context, mImageLoader, ImageOptionsFactory.getListHeadOption(), record.getSender().getHeadUrl(),
					AppSelfInfo.ImageScaleInfo.bigImageWidthPx, holder.head, R.drawable.default_head);
		}
		if (record.getType() != InformationType.TYPE_FRIEND_REQUEST_NOTICE) {
			if (!LogicUtils.isSender(context, record)) {
				holder.name.setText(record.getSender().getNick());
			} else {
				holder.name.setText(record.getReceiver().getNick());
			}
		}
		return convertView;
	}

	private void initFriendInformationSenderView(Information record) {
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

	private void initFriendInformationReceiverView(final Information record) {

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

	private void initPhotoInformationReceiverView(final Information record, String statuTag, String buttonTag) {
		holder.statuButton.setBackgroundDrawable(null);
		holder.statuButton.setText(null);
		if (record.getStatu() == InformationState.PhotoInformationState.STATU_NOTICE_SENDED_OR_NEED_LOADD) {
			holder.bar.setVisibility(View.VISIBLE);
			holder.statu.setText(R.string.receive_downloading);
			RCPlatformImageLoader
					.LoadPictureForList(context, holder.bar, holder.statu, null, mImageLoader, ImageOptionsFactory.getReceiveImageOption(), record);
			holder.statuButton.stopTask();
			// 状态为2，表示已经下载了，但是未查看，
		} else if (record.getStatu() == InformationState.PhotoInformationState.STATU_NOTICE_DELIVERED_OR_LOADED) {
			if (RCPlatformImageLoader.isFileExist(context, record.getUrl())) {
				// 如果缓存文件存在
				holder.bar.setVisibility(View.GONE);
				holder.statuButton.stopTask();
				holder.statu.setText(getTimeText(R.string.receive_loaded, record.getCreatetime()));
			} else {
				// 如果缓存文件不存在
				holder.bar.setVisibility(View.VISIBLE);
				holder.statu.setText(R.string.receive_downloading);
				RCPlatformImageLoader.LoadPictureForList(context, holder.bar, holder.statu, null, mImageLoader, ImageOptionsFactory.getReceiveImageOption(),
						record);
				holder.statuButton.stopTask();
			}
			// 状态为4.表示正在查看
		} else if (record.getStatu() == InformationState.PhotoInformationState.STATU_NOTICE_SHOWING) {
			holder.bar.setVisibility(View.GONE);
			holder.statuButton.setVisibility(View.VISIBLE);
			holder.statuButton.setBackgroundResource(R.drawable.item_time_bg);
			holder.statuButton.scheuleTask(record);
			holder.statu.setText(getTimeText(R.string.receive_loaded, record.getCreatetime()));
			// 状态为3 表示 已经查看，
		} else if (record.getStatu() == InformationState.PhotoInformationState.STATU_NOTICE_OPENED) {
			holder.bar.setVisibility(View.GONE);
			holder.statuButton.stopTask();
			holder.statu.setText(getTimeText(R.string.receive_looked, record.getCreatetime()));

			// 状态为5 表示正在下载
		} else if (record.getStatu() == InformationState.PhotoInformationState.STATU_NOTICE_LOADING) {
			holder.bar.setVisibility(View.VISIBLE);
			holder.statu.setText(R.string.receive_downloading);
			holder.statuButton.stopTask();
			// 7 下载失败
		} else if (record.getStatu() == InformationState.PhotoInformationState.STATU_NOTICE_LOAD_FAIL) {
			holder.bar.setVisibility(View.GONE);
			holder.statu.setText(R.string.receive_fail);
			holder.statuButton.stopTask();
		}

	}

	private void initPhotoInformationSenderView(Information record) {
		// 如果当前用户是发送者

		holder.statuButton.setBackgroundResource(R.drawable.send_arrows);
		holder.statuButton.setText(null);
		holder.statuButton.stopTask();
		// 状态为1 表示已经发送到服务器
		if (record.getStatu() == InformationState.PhotoInformationState.STATU_NOTICE_SENDED_OR_NEED_LOADD) {
			holder.bar.setVisibility(View.GONE);
			holder.statu.setText(getTimeText(R.string.send_sended, record.getCreatetime()));
			// 状态为2表示对方已经下载
		} else if (record.getStatu() == InformationState.PhotoInformationState.STATU_NOTICE_DELIVERED_OR_LOADED) {
			holder.bar.setVisibility(View.GONE);
			holder.statu.setText(getTimeText(R.string.send_received, record.getCreatetime()));
			// 状态为3 表示已经查看
		} else if (record.getStatu() == InformationState.PhotoInformationState.STATU_NOTICE_OPENED) {
			holder.bar.setVisibility(View.GONE);
			holder.statu.setText(getTimeText(R.string.send_looked, record.getCreatetime()));
			// 0 表示正在发送
		} else if (record.getStatu() == InformationState.PhotoInformationState.STATU_NOTICE_SENDING) {
			holder.bar.setVisibility(View.VISIBLE);
			holder.statu.setText(R.string.send_sending);
			// 6 表示发送失败
		} else if (record.getStatu() == InformationState.PhotoInformationState.STATU_NOTICE_SEND_FAIL) {
			holder.bar.setVisibility(View.GONE);
			holder.statu.setText(R.string.send_fail);
		}
	}

	private String getTimeText(int baseResId, long time) {
		return context.getString(baseResId, RCPlatformTextUtil.getTextFromTimeToNow(context, time));
	}

	// LogicUtils.informationFriendAdded(context, record);
	private void addAsFriend(final Information record) {
		final BaseActivity activity = (BaseActivity) context;
		activity.showLoadingDialog(BaseActivity.LOADING_NO_MSG, BaseActivity.LOADING_NO_MSG, false);
		FriendsProxy.addFriendFromInformation(context, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				try {
					JSONObject jsonObject=new JSONObject(content);
					Friend friend=JSONConver.jsonToObject(jsonObject.getJSONObject("userInfo").toString(), Friend.class);
					record.setStatu(InformationState.FriendRequestInformationState.STATU_QEQUEST_ADD_CONFIRM);
					notifyDataSetChanged();
					activity.dismissLoadingDialog();
					LogicUtils.informationFriendAdded(context, record, friend);
				} catch (Exception e) {
					e.printStackTrace();
					onFailure(RCPlatformServiceError.ERROR_CODE_REQUEST_FAIL, context.getString(R.string.net_error));
				}
			}

			@Override
			public void onFailure(int errorCode, String content) {
				activity.dismissLoadingDialog();
				activity.showErrorConfirmDialog(content);
			}
		}, record);
	}

	public String getStatuTime(String prefix, String postfix, long time) {
		long currentTime = System.currentTimeMillis();
		long durring = currentTime - time;
		StringBuffer sb = new StringBuffer();
		sb.append(prefix + " ");
		int s = (int) (durring / 1000);
		int m = 0;
		int h = 0;
		int d = 0;

		if (s > 60) {
			m = s / 60;
		}
		if (m > 60) {
			h = m / 60;
		}
		if (h > 24) {
			d = h / 24;
		}
		if (d > 0) {
			sb.append(d + "d");
		} else if (h > 0) {
			sb.append(h + "h");
		} else if (m > 0) {
			sb.append(m + "m");
		} else if (s > 0) {
			sb.append(s + "s");
		}
		sb.append(" ago " + postfix);
		return sb.toString();

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
	}

	public List<Information> getData() {
		return data;
	}

	public void addData(List<Information> data) {
		for (Information info : data) {
			this.data.addFirst(info);
		}
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
