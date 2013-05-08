package com.rcplatform.phototalk.adapter;

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
import com.rcplatform.phototalk.api.RCPlatformResponseHandler;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.InformationState;
import com.rcplatform.phototalk.bean.InformationType;
import com.rcplatform.phototalk.galhttprequest.LogUtil;
import com.rcplatform.phototalk.image.downloader.ImageOptionsFactory;
import com.rcplatform.phototalk.image.downloader.RCPlatformImageLoader;
import com.rcplatform.phototalk.logic.LogicUtils;
import com.rcplatform.phototalk.proxy.FriendsProxy;
import com.rcplatform.phototalk.utils.AppSelfInfo;
import com.rcplatform.phototalk.utils.RCPlatformTextUtil;
import com.rcplatform.phototalk.views.RecordTimerLimitView;

public class PhotoTalkMessageAdapter extends BaseAdapter {

	private final List<Information> data;

	private final Context context;

	private ViewHolder holder;

	private final ListView listView;

	private final ImageLoader mImageLoader;

	private Information mPressedInformation;
	private int mPressedPosition = -1;

	public PhotoTalkMessageAdapter(Context context, List<Information> data, ListView ls) {
		this.data = data;
		this.context = context;
		this.mImageLoader = ImageLoader.getInstance();
		this.listView = ls;
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
					LogUtil.e("item down");
					mPressedInformation = record;
					mPressedPosition = position;
				}
				return false;
			}
		});
		String buttonTag = record.getRecordId() + Button.class.getName();
		holder.statuButton.setTag(buttonTag);
		String statuTag = record.getRecordId() + TextView.class.getName();
		holder.statu.setTag(statuTag);
		holder.bar.setTag(record.getRecordId() + ProgressBar.class.getName());
		holder.item_new.setTag(record.getRecordId() + ImageView.class.getName());
		if (record.getType() == InformationType.TYPE_PICTURE_OR_VIDEO && record.getStatu() != InformationState.STATU_NOTICE_OPENED && !LogicUtils.isSender(context, record)) {
			holder.item_new.setVisibility(View.VISIBLE);
		} else {
			holder.item_new.setVisibility(View.GONE);
		}

		if (record.getType() == InformationType.TYPE_PICTURE_OR_VIDEO || record.getType() == InformationType.TYPE_SYSTEM_NOTICE) {
			// 如果当前用户是接收者
			if (!LogicUtils.isSender(context, record)) {
				// 状态为1，表示需要去下载
				initReceiverView(record, statuTag, buttonTag);
			} else {
				initSenderView(record);
			}
		} else if (record.getType() == InformationType.TYPE_FRIEND_REQUEST_NOTICE) {// 是通知
			holder.bar.setVisibility(View.GONE);
			holder.statuButton.stopTask();
			holder.statuButton.setText(null);
			holder.statu.setText(RCPlatformTextUtil.getTextFromTimeToNow(context, record.getReceiveTime()));
			// 如果是对方添加我
			if (!LogicUtils.isSender(context, record)) {
				holder.name.setText(context.getString(R.string.added_by_friend, record.getSender().getNick()));
				// 1. 如果更多里面设置了所有人都可以给我发送图片,那么item里面状态显示： XX 将加我为好友，并显示添加按钮
				if (record.getStatu() == InformationState.STATU_QEQUEST_ADD_NO_CONFIRM) {
					holder.statuButton.setEnabled(true);
					holder.statuButton.setBackgroundResource(R.drawable.addfriend);

				}
				// 2,如果更多里面设置了只有好友可以给我发送图片，那么item里面 状态显示： XX 将加我为好友，并显示添加按钮
				else if (record.getStatu() == InformationState.STATU_QEQUEST_ADD_NEED_CONFIRM) {
					holder.statuButton.setEnabled(true);
					holder.statuButton.setBackgroundResource(R.drawable.addfriend);
				}
				// 2.1 点击了确认添加对方为好友好友后， 添加 XX为好友，隐藏添加按钮
				else if (record.getStatu() == InformationState.STATU_QEQUEST_ADDED) {
					holder.statuButton.setEnabled(false);
					holder.statuButton.setBackgroundResource(R.drawable.added);
				}
				if (record.getStatu() != InformationState.STATU_QEQUEST_ADDED) {
					holder.statuButton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							addAsFriend(record);
						}
					});
				} else {
					holder.statuButton.setOnClickListener(null);
				}

			} else { // 我添加别人为好友
				// 1 如果对方设置了所有人都可以发送图片，那么item里面显示 状态显示：添加 XX为好友，隐藏按钮， 对应上面 1
				holder.name.setText(context.getString(R.string.added_friend, record.getReceiver().getNick()));
				if (record.getStatu() == InformationState.STATU_QEQUEST_ADD_NO_CONFIRM) {
					holder.statuButton.setBackgroundResource(R.drawable.added);
				}
				// 2 如果对方更多里面内设置了只有好友可以发送图片，那么item 里面状态显示，等待 XX 确认好友请求 ，隐藏按钮
				else if (record.getStatu() == InformationState.STATU_QEQUEST_ADD_NEED_CONFIRM) {
					holder.statuButton.setBackgroundDrawable(null);
				}
				// 2.1 如果对方确认了请求，那么那么item 里面状态显示，添加 xx 为好友，按钮显示为已添加
				else if (record.getStatu() == InformationState.STATU_QEQUEST_ADDED) {
					holder.statuButton.setBackgroundResource(R.drawable.added);
				}
			}
		} else if (record.getType() == InformationType.TYPE_SYSTEM_NOTICE) {
			holder.bar.setVisibility(View.GONE);
			holder.statuButton.setBackgroundDrawable(null);
			holder.statu.setText("system notice");
		}

		if (LogicUtils.isSender(context, record)) {
			RCPlatformImageLoader.loadImage(context, mImageLoader, ImageOptionsFactory.getListHeadOption(), record.getReceiver().getHeadUrl(), AppSelfInfo.ImageScaleInfo.bigImageWidthPx, holder.head, R.drawable.default_head);
		} else {
			RCPlatformImageLoader.loadImage(context, mImageLoader, ImageOptionsFactory.getListHeadOption(), record.getSender().getHeadUrl(), AppSelfInfo.ImageScaleInfo.bigImageWidthPx, holder.head, R.drawable.default_head);
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

	private void initReceiverView(final Information record, String statuTag, String buttonTag) {
		holder.statuButton.setBackgroundDrawable(null);
		holder.statuButton.setText(null);
		if (record.getStatu() == InformationState.STATU_NOTICE_SENDED_OR_NEED_LOADD) {
			holder.bar.setVisibility(View.VISIBLE);
			holder.statu.setText(R.string.receive_downloading);
			RCPlatformImageLoader.LoadPictureForList(context, holder.bar, holder.statu, null, mImageLoader, ImageOptionsFactory.getReceiveImageOption(), record);
			holder.statuButton.stopTask();
			// 状态为2，表示已经下载了，但是未查看，
		} else if (record.getStatu() == InformationState.STATU_NOTICE_DELIVERED_OR_LOADED) {
			if (RCPlatformImageLoader.isFileExist(context, record.getUrl())) {
				// 如果缓存文件存在
				holder.bar.setVisibility(View.GONE);
				holder.statuButton.stopTask();
				holder.statu.setText(getTimeText(R.string.receive_loaded, record.getReceiveTime()));
			} else {
				// 如果缓存文件不存在
				holder.bar.setVisibility(View.VISIBLE);
				holder.statu.setText(R.string.receive_downloading);
				RCPlatformImageLoader.LoadPictureForList(context, holder.bar, holder.statu, null, mImageLoader, ImageOptionsFactory.getReceiveImageOption(), record);
				holder.statuButton.stopTask();
			}
			// 状态为4.表示正在查看
		} else if (record.getStatu() == InformationState.STATU_NOTICE_SHOWING) {
			holder.bar.setVisibility(View.GONE);
			holder.statuButton.setBackgroundResource(R.drawable.item_time_bg);
			holder.statuButton.scheuleTask(record);
			holder.statu.setText(getTimeText(R.string.receive_loaded, record.getReceiveTime()));
			// 状态为3 表示 已经查看，
		} else if (record.getStatu() == InformationState.STATU_NOTICE_OPENED) {
			holder.bar.setVisibility(View.GONE);
			holder.statuButton.stopTask();
			holder.statu.setText(getTimeText(R.string.receive_looked, record.getReceiveTime()));

			// 状态为5 表示正在下载
		} else if (record.getStatu() == InformationState.STATU_NOTICE_LOADING) {
			holder.bar.setVisibility(View.VISIBLE);
			holder.statu.setText(R.string.receive_downloading);
			holder.statuButton.stopTask();
			// 7 下载失败
		} else if (record.getStatu() == InformationState.STATU_NOTICE_LOAD_FAIL) {
			holder.bar.setVisibility(View.GONE);
			holder.statu.setText(R.string.receive_fail);
			holder.statuButton.stopTask();
		}

	}

	private void initSenderView(Information record) {
		// 如果当前用户是发送者
		holder.statuButton.setBackgroundResource(R.drawable.send_arrows);
		holder.statuButton.setText(null);
		holder.statuButton.stopTask();
		// 状态为1 表示已经发送到服务器
		if (record.getStatu() == InformationState.STATU_NOTICE_SENDED_OR_NEED_LOADD) {
			holder.bar.setVisibility(View.GONE);
			holder.statu.setText(getTimeText(R.string.send_sended, record.getReceiveTime()));
			// 状态为2表示对方已经下载
		} else if (record.getStatu() == InformationState.STATU_NOTICE_DELIVERED_OR_LOADED) {
			holder.bar.setVisibility(View.GONE);
			holder.statu.setText(getTimeText(R.string.send_received, record.getReceiveTime()));
			// 状态为3 表示已经查看
		} else if (record.getStatu() == InformationState.STATU_NOTICE_OPENED) {
			holder.bar.setVisibility(View.GONE);
			holder.statu.setText(getTimeText(R.string.send_looked, record.getReceiveTime()));
			// 0 表示正在发送
		} else if (record.getStatu() == InformationState.STATU_NOTICE_SENDING) {
			holder.bar.setVisibility(View.VISIBLE);
			holder.statu.setText(R.string.send_sending);
			// 6 表示发送失败
		} else if (record.getStatu() == InformationState.STATU_NOTICE_SEND_FAIL) {
			holder.bar.setVisibility(View.GONE);
			holder.statu.setText(R.string.send_fail);
		}
	}

	private String getTimeText(int baseResId, long time) {
		return context.getString(baseResId, RCPlatformTextUtil.getTextFromTimeToNow(context, time));
	}

	private void addAsFriend(final Information record) {
		((BaseActivity) context).showLoadingDialog(BaseActivity.LOADING_NO_MSG, BaseActivity.LOADING_NO_MSG, false);
		FriendsProxy.addFriendFromInformation(context, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				record.setStatu(InformationState.STATU_QEQUEST_ADDED);
				RecordTimerLimitView button = (RecordTimerLimitView) listView.findViewWithTag(record.getRecordId() + Button.class.getName());
				if (button != null) {
					button.setBackgroundResource(R.drawable.added);
					button.setEnabled(false);
				}
				if (listView.findViewWithTag(record.getRecordId() + TextView.class.getName()) != null) {
					((TextView) listView.findViewWithTag(record.getRecordId() + TextView.class.getName())).setText(getStringfromResource(R.string.home_record_added) + record.getSender().getNick() + getStringfromResource(R.string.home_record_as_friend));
				}
				LogicUtils.informationFriendAdded(record);
				((BaseActivity) context).dismissLoadingDialog();
			}

			@Override
			public void onFailure(int errorCode, String content) {
				((BaseActivity) context).dismissLoadingDialog();
				((BaseActivity) context).showErrorConfirmDialog(content);
			}
		}, record);
	}

	private String getStringfromResource(int id) {
		return context.getResources().getString(id);
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
