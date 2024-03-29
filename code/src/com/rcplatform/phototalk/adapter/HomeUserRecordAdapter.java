package com.rcplatform.phototalk.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.AddFriendAdapter.OnFriendPortraitListener;
import com.rcplatform.phototalk.MenueApplication;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.api.MenueApiFactory;
import com.rcplatform.phototalk.api.MenueApiRecordType;
import com.rcplatform.phototalk.api.MenueApiUrl;
import com.rcplatform.phototalk.bean.InfoRecord;
import com.rcplatform.phototalk.bean.ServiceSimpleNotice;
import com.rcplatform.phototalk.db.PhotoTalkDao;
import com.rcplatform.phototalk.galhttprequest.GalHttpRequest;
import com.rcplatform.phototalk.galhttprequest.GalHttpRequest.GalHttpLoadTextCallBack;
import com.rcplatform.phototalk.image.downloader.ImageOptionsFactory;
import com.rcplatform.phototalk.image.downloader.RCPlatformImageLoader;
import com.rcplatform.phototalk.utils.AppSelfInfo;
import com.rcplatform.phototalk.utils.Contract;
import com.rcplatform.phototalk.views.RecordTimerLimitView;
import com.rcplatform.phototalk.views.RecordTimerLimitView.OnTimeEndListener;

public class HomeUserRecordAdapter extends BaseAdapter {

	private final List<InfoRecord> data;

	private final Context context;

	private ViewHolder holder;

	private final ListView listView;

	private final ImageLoader mImageLoader;

	private final MenueApplication app;

	private OnFriendPortraitListener friendPortraitListener;

	public HomeUserRecordAdapter(Context context, List<InfoRecord> data,
			ListView ls) {
		this.data = data;
		this.context = context;
		this.mImageLoader = ImageLoader.getInstance();
		this.listView = ls;
		this.app = (MenueApplication) context.getApplicationContext();
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
		//
		final InfoRecord record = data.get(position);
		final int index = position;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.home_user_record_list_item, null);
			holder = new ViewHolder();
			holder.head = (ImageView) convertView
					.findViewById(R.id.iv_record_item_head);
			holder.name = (TextView) convertView
					.findViewById(R.id.tv_record_item_name);
			holder.statu = (TextView) convertView
					.findViewById(R.id.tv_record_item_statu);
			holder.bar = (ProgressBar) convertView
					.findViewById(R.id.progress_home_record);
			holder.statuButton = (RecordTimerLimitView) convertView
					.findViewById(R.id.btn_record_item_statu_button);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		String buttonTag = record.getRecordId() + Button.class.getName();
		holder.statuButton.setTag(buttonTag);
		String statuTag = record.getRecordId() + TextView.class.getName();
		holder.statu.setTag(statuTag);

		holder.bar.setTag(record.getRecordId() + ProgressBar.class.getName());

		if (record.getType() == MenueApiRecordType.TYPE_PICTURE_OR_VIDEO) {

			// 如果当前用户是接收者
			if (!isOwnerForReocrd(record)) {
				// 状态为1，表示需要去下载
				if (record.getStatu() == MenueApiRecordType.STATU_NOTICE_SENDED_OR_NEED_LOADD) {
					holder.bar.setVisibility(View.VISIBLE);
					holder.statu.setText(R.string.home_record_pic_loading);
					// MenueImageLoader.loadImageForList(context, holder.bar,
					// holder.statu, mImageLoader,
					// ImageOptionsFactory.getReceiveImageOption(),
					// record, holder.bar.getTag(), holder.statu.getTag());
					RCPlatformImageLoader
							.LoadPictureForList(
									context,
									holder.bar,
									holder.statu,
									null,
									mImageLoader,
									ImageOptionsFactory.getReceiveImageOption(),
									record);
					holder.statuButton.stopTask();
					holder.statuButton.setText("");
					holder.statuButton
							.setBackgroundResource(R.drawable.receive_arrows_unread);

					// 状态为2，表示已经下载了，但是未查看，
				} else if (record.getStatu() == MenueApiRecordType.STATU_NOTICE_DELIVERED_OR_LOADED) {
					holder.bar.setVisibility(View.GONE);
					holder.statuButton
							.setBackgroundResource(R.drawable.receive_arrows_unread);
					holder.statuButton.stopTask();
					holder.statuButton.setText("");
					holder.statu
							.setText(getStatuTime(
									getStringfromResource(R.string.statu_received),
									getStringfromResource(R.string.statu_press_to_show),
									record.getLastUpdateTime()));
					// 状态为4.表示正在查看
				} else if (record.getStatu() == MenueApiRecordType.STATU_NOTICE_SHOWING) {
					holder.bar.setVisibility(View.GONE);
					holder.statuButton.setBackgroundDrawable(null);
					holder.statuButton.scheuleTask(record);
					holder.statuButton.setOnTimeEndListener(
							new OnTimeEndListener() {

								@Override
								public void onEnd(Object statuTag,
										Object buttonTag) {
									RecordTimerLimitView timerLimitView = (RecordTimerLimitView) listView
											.findViewWithTag(buttonTag);
									if (timerLimitView != null) {
										timerLimitView
												.setBackgroundResource(R.drawable.receive_arrows_opened);
										timerLimitView.setText("");
									}
									TextView statu = ((TextView) listView
											.findViewWithTag(statuTag));
									if (statu != null) {
										statu.setText(R.string.statu_opened_1s_ago);
									}
									record.setStatu(MenueApiRecordType.STATU_NOTICE_OPENED);
									openedNotice(context, record);
									PhotoTalkDao.getInstance().updateRecordStatu(
											context, record);
								}
							}, statuTag, buttonTag);
					holder.statu
							.setText(getStatuTime(
									getStringfromResource(R.string.statu_received),
									getStringfromResource(R.string.statu_press_to_show),
									record.getLastUpdateTime()));
					// 状态为3 表示 已经查看，
				} else if (record.getStatu() == MenueApiRecordType.STATU_NOTICE_OPENED) {
					holder.bar.setVisibility(View.GONE);
					holder.statuButton
							.setBackgroundResource(R.drawable.receive_arrows_opened);
					holder.statuButton.setText("");
					holder.statuButton.stopTask();
					holder.statu.setText(getStatuTime(
							getStringfromResource(R.string.statu_opened), "",
							record.getLastUpdateTime()));

					// 状态为5 表示正在下载
				} else if (record.getStatu() == MenueApiRecordType.STATU_NOTICE_LOADING) {
					holder.bar.setVisibility(View.VISIBLE);
					holder.statu.setText(R.string.home_record_pic_loading);
					holder.statuButton.setText("");
					holder.statuButton.stopTask();
					holder.statuButton
							.setBackgroundResource(R.drawable.receive_arrows_unread);
					// 7 下载失败
				} else if (record.getStatu() == MenueApiRecordType.STATU_NOTICE_LOAD_FAIL) {
					holder.bar.setVisibility(View.GONE);
					holder.statu.setText(R.string.home_record_load_fail);
					holder.statuButton.setText("");
					holder.statuButton.stopTask();
					holder.statuButton
							.setBackgroundResource(R.drawable.receive_arrows_unread);
				}
			} else { // 如果当前用户是发送者
						// 状态为1 表示已经发送到服务器
				if (record.getStatu() == MenueApiRecordType.STATU_NOTICE_SENDED_OR_NEED_LOADD) {
					holder.bar.setVisibility(View.GONE);
					holder.statuButton
							.setBackgroundResource(R.drawable.send_arrows);
					holder.statuButton.setText("");
					holder.statuButton.stopTask();
					holder.statu.setText(getStatuTime(
							getStringfromResource(R.string.statu_send), "",
							record.getLastUpdateTime()));
					// 状态为2表示对方已经下载
				} else if (record.getStatu() == MenueApiRecordType.STATU_NOTICE_DELIVERED_OR_LOADED) {
					holder.bar.setVisibility(View.GONE);
					holder.statuButton
							.setBackgroundResource(R.drawable.send_arrows);
					holder.statuButton.setText("");
					holder.statuButton.stopTask();
					holder.statu.setText(getStatuTime(
							getStringfromResource(R.string.statu_delivered),
							"", record.getLastUpdateTime()));
					// 状态为3 表示已经查看
				} else if (record.getStatu() == MenueApiRecordType.STATU_NOTICE_OPENED) {
					holder.bar.setVisibility(View.GONE);
					holder.statuButton
							.setBackgroundResource(R.drawable.send_arrows);
					holder.statuButton.setText("");
					holder.statuButton.stopTask();
					holder.statu.setText(getStatuTime(
							getStringfromResource(R.string.statu_opened), "",
							record.getLastUpdateTime()));
					// 0 表示正在发送
				} else if (record.getStatu() == MenueApiRecordType.STATU_NOTICE_SENDING) {
					holder.bar.setVisibility(View.VISIBLE);
					holder.statuButton
							.setBackgroundResource(R.drawable.send_arrows);
					holder.statuButton.setText("");
					holder.statuButton.stopTask();
					holder.statu.setText(R.string.home_record_pic_sending);
					// 6 表示发送失败
				} else if (record.getStatu() == MenueApiRecordType.STATU_NOTICE_SEND_FAIL) {
					holder.bar.setVisibility(View.GONE);
					holder.statuButton
							.setBackgroundResource(R.drawable.send_arrows);
					holder.statuButton.setText("");
					holder.statuButton.stopTask();
					holder.statu.setText(R.string.home_record_pic_send_fail);
				}

			}

		} else if (record.getType() == MenueApiRecordType.TYPE_FRIEND_REQUEST_NOTICE) {// 是通知
			holder.bar.setVisibility(View.GONE);
			// 如果是对方添加我
			if (!isOwnerForReocrd(record)) {
				// 1. 如果更多里面设置了所有人都可以给我发送图片,那么item里面状态显示： XX 将加我为好友，并显示添加按钮
				if (record.getStatu() == MenueApiRecordType.STATU_QEQUEST_ADD_NO_CONFIRM) {
					holder.statuButton
							.setBackgroundResource(R.drawable.addfriend);
					holder.statuButton.stopTask();
					holder.statuButton.setText("");
					holder.statu
							.setText(getStringfromResource(R.string.home_record_added_you_as_friend));
				}
				// 2,如果更多里面设置了只有好友可以给我发送图片，那么item里面 状态显示： XX 将加我为好友，并显示添加按钮
				else if (record.getStatu() == MenueApiRecordType.STATU_QEQUEST_ADD_NEED_CONFIRM) {
					holder.statuButton
							.setBackgroundResource(R.drawable.addfriend);
					holder.statuButton.stopTask();
					holder.statuButton.setText("");
					holder.statu
							.setText(getStringfromResource(R.string.home_record_will_add_you_as_friend));
				}
				// 2.1 点击了确认添加对方为好友好友后， 添加 XX为好友，隐藏添加按钮
				else if (record.getStatu() == MenueApiRecordType.STATU_QEQUEST_ADDED) {
					holder.statuButton.setBackgroundResource(R.drawable.added);
					holder.statuButton.stopTask();
					holder.statuButton.setText("");
					holder.statu
							.setText(getStringfromResource(R.string.home_record_added)
									+ record.getSender().getNick()
									+ getStringfromResource(R.string.home_record_as_friend));
				}

				if (record.getStatu() != MenueApiRecordType.STATU_QEQUEST_ADDED) {
					holder.statuButton
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									addAsFriend(record, index);
								}
							});
				}

			} else { // 我添加别人为好友
				// 1 如果对方设置了所有人都可以发送图片，那么item里面显示 状态显示：添加 XX为好友，隐藏按钮， 对应上面 1
				if (record.getStatu() == MenueApiRecordType.STATU_QEQUEST_ADD_NO_CONFIRM) {
					holder.statu
							.setText(getStringfromResource(R.string.home_record_added)
									+ record.getReceiver().getNick()
									+ getStringfromResource(R.string.home_record_as_friend));
					holder.statuButton.setBackgroundResource(R.drawable.added);
					holder.statuButton.stopTask();
					holder.statuButton.setText("");

				}
				// 2 如果对方更多里面内设置了只有好友可以发送图片，那么item 里面状态显示，等待 XX 确认好友请求 ，隐藏按钮
				else if (record.getStatu() == MenueApiRecordType.STATU_QEQUEST_ADD_NEED_CONFIRM) {
					holder.statuButton.setBackgroundDrawable(null);
					holder.statuButton.stopTask();
					holder.statuButton.setText("");
					holder.statu
							.setText(getStringfromResource(R.string.home_record_waitting_and_confirm));
				}
				// 2.1 如果对方确认了请求，那么那么item 里面状态显示，添加 xx 为好友，按钮显示为已添加
				else if (record.getStatu() == MenueApiRecordType.STATU_QEQUEST_ADDED) {
					holder.statuButton.setBackgroundResource(R.drawable.added);
					holder.statuButton.stopTask();
					holder.statuButton.setText("");
					holder.statu
							.setText(getStringfromResource(R.string.home_record_added)
									+ record.getReceiver().getNick()
									+ getStringfromResource(R.string.home_record_as_friend));
				}
			}
		} else if (record.getType() == MenueApiRecordType.TYPE_SYSTEM_NOTICE) {
			holder.bar.setVisibility(View.GONE);
			holder.statuButton.setBackgroundDrawable(null);
			holder.statu.setText("system notice");
		}
		if (String.valueOf(
				MenueApplication.getUserInfoInstall(context).getSuid())
				.equals(record.getSender().getSuid())) {
			Log.i("ABC", "URL " + position + " , "
					+ record.getReceiver().getHeadUrl());
			RCPlatformImageLoader.loadImage(context, mImageLoader,
					ImageOptionsFactory.getListHeadOption(), record
							.getReceiver().getHeadUrl(),
					AppSelfInfo.ImageScaleInfo.bigImageWidthPx, holder.head,
					R.drawable.default_head);
		} else {
			Log.i("ABC", "URL " + position + " , "
					+ record.getSender().getHeadUrl());
			RCPlatformImageLoader.loadImage(context, mImageLoader,
					ImageOptionsFactory.getListHeadOption(), record.getSender()
							.getHeadUrl(),
					AppSelfInfo.ImageScaleInfo.bigImageWidthPx, holder.head,
					R.drawable.default_head);
		}
		String text = "";
		String name = "";
		if ((1 + position) % 3 == 0) {
			text = "Received 10s ago -- press to show";
			name = "Menue";
		} else if ((1 + position) % 3 == 1) {
			text = "Opened 2s ago ";
			name = "Shanghai";
		} else if ((1 + position) % 3 == 2) {
			text = "Received 8 ago -- press to show";
			name = "Nanjing";

		}
		// holder.head.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// Friend friend = new Friend();
		// if (isOwnerForReocrd(record)) {
		// friend.setUserId(Integer.parseInt(record.getReceiver().getUserId().trim()));
		// friend.setSuid(record.getReceiver().getSuUserId());
		//
		// } else {
		// friend.setUserId(Integer.parseInt(record.getSender().getUserId().trim()));
		// friend.setSuid(record.getSender().getSuUserId());
		// }
		// if (friendPortraitListener != null)
		// friendPortraitListener.showFriendDetail(v, friend);
		// }
		// });

		if (!isOwnerForReocrd(record)) {
			holder.name.setText(record.getSender().getNick());
		} else {
			holder.name.setText(record.getReceiver().getNick());
		}
		// holder.statu.setText(text);
		return convertView;
	}

	private void addAsFriend(final InfoRecord record, final int index) {
		GalHttpRequest request = GalHttpRequest.requestWithURL(context,
				MenueApiUrl.HOME_USER_NOTICE_ADD_FRIEND);
		// request.setPostValueForKey(MenueApiFactory.USER, email);
		request.setPostValueForKey(MenueApiFactory.TOKEN, MenueApplication
				.getUserInfoInstall(context).getToken());
		request.setPostValueForKey(MenueApiFactory.USERID, MenueApplication
				.getUserInfoInstall(context).getSuid());
		request.setPostValueForKey(MenueApiFactory.LANGUAGE, Locale
				.getDefault().getLanguage());
		request.setPostValueForKey(MenueApiFactory.DEVICE_ID,
				android.os.Build.DEVICE);
		request.setPostValueForKey(MenueApiFactory.APP_ID, Contract.APP_ID);

		request.setPostValueForKey(MenueApiFactory.RECORD_NOTICE_FRIEND_ID,
				record.getSender().getSuUserId());
		request.setPostValueForKey(
				MenueApiFactory.RECORD_NOTICE_NOTICE_NOTICEID,
				record.getNoticeId());
		request.setPostValueForKey(MenueApiFactory.RECORD_NOTICE_STATU,
				record.getStatu() + "");
		request.setPostValueForKey(MenueApiFactory.RECORD_NOTICE_TYPE,
				record.getType() + "");
		request.getPostData().toString();
		request.startAsynRequestString(new GalHttpLoadTextCallBack() {

			@Override
			public void textLoaded(String text) {
				record.setStatu(MenueApiRecordType.STATU_QEQUEST_ADDED);
				PhotoTalkDao.getInstance().updateRecordStatu(context, record);
				if (listView.findViewWithTag(record.getRecordId()
						+ Button.class.getName()) != null)
					listView.findViewWithTag(
							record.getRecordId() + Button.class.getName())
							.setBackgroundResource(R.drawable.added);
				if (listView.findViewWithTag(record.getRecordId()
						+ TextView.class.getName()) != null) {
					((TextView) listView.findViewWithTag(record.getRecordId()
							+ TextView.class.getName()))
							.setText(getStringfromResource(R.string.home_record_added)
									+ record.getSender().getNick()
									+ getStringfromResource(R.string.home_record_as_friend));
				}

			}

			@Override
			public void loadFail() {
				Log.i("AAA", "add friend fail");
			}
		});

	}

	private boolean isOwnerForReocrd(InfoRecord record) {

		if (String.valueOf(
				MenueApplication.getUserInfoInstall(context).getSuid())
				.equals(record.getSender().getSuid())) {
			return true;
		} else {
			return false;
		}
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

	private void openedNotice(Context context, InfoRecord record) {
		Gson gson = new Gson();
		ServiceSimpleNotice notice = new ServiceSimpleNotice(record.getStatu()
				+ "", record.getRecordId() + "", record.getType() + "");
		List<ServiceSimpleNotice> list = new ArrayList<ServiceSimpleNotice>();
		list.add(notice);
		String s = gson.toJson(list,
				new TypeToken<List<ServiceSimpleNotice>>() {
				}.getType());

		GalHttpRequest request = GalHttpRequest.requestWithURL(context,
				MenueApiUrl.HOME_USER_NOTICE_CHANGE);
		// request.setPostValueForKey(MenueApiFactory.USER, email);
		request.setPostValueForKey(MenueApiFactory.TOKEN, MenueApplication
				.getUserInfoInstall(context).getToken());
		request.setPostValueForKey(MenueApiFactory.USERID, MenueApplication
				.getUserInfoInstall(context).getSuid());
		request.setPostValueForKey(MenueApiFactory.LANGUAGE, Locale
				.getDefault().getLanguage());
		request.setPostValueForKey(MenueApiFactory.DEVICE_ID,
				android.os.Build.DEVICE);
		request.setPostValueForKey(MenueApiFactory.APP_ID, Contract.APP_ID);

		request.setPostValueForKey(MenueApiFactory.NOTICES, s);

		request.startAsynRequestString(new GalHttpLoadTextCallBack() {

			@Override
			public void textLoaded(String text) {
				Log.i("AAA", "openedNotice" + text.toString());
			}

			@Override
			public void loadFail() {
				Log.i("AAA", "openedNotice fail");
			}
		});

	}

	class ViewHolder {

		ImageView head;

		TextView name;

		TextView statu;

		RecordTimerLimitView statuButton;

		RelativeLayout timerLayout;

		ProgressBar bar;
	}

	public List<InfoRecord> getData() {
		return data;
	}

	public void setFriendPortraitListener(
			OnFriendPortraitListener friendPortraitListener) {
		this.friendPortraitListener = friendPortraitListener;
	}

}
