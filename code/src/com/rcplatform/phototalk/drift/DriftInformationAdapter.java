package com.rcplatform.phototalk.drift;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.PhotoTalkApplication;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.InformationCategory;
import com.rcplatform.phototalk.bean.InformationState;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.galhttprequest.LogUtil;
import com.rcplatform.phototalk.image.downloader.RCPlatformImageLoader;
import com.rcplatform.phototalk.logic.LogicUtils;
import com.rcplatform.phototalk.utils.RCPlatformTextUtil;
import com.rcplatform.phototalk.utils.Utils;
import com.rcplatform.phototalk.views.RecordTimerLimitView;

public class DriftInformationAdapter extends BaseAdapter {

	private final LinkedList<DriftInformation> data = new LinkedList<DriftInformation>();

	private final Context context;
	private ImageLoader mImageLoader;
	private DriftInformation mPressedInformation;
	private int mPressedPosition = -1;
	private LayoutInflater mInflater;
	private UserInfo currentUser;
	private SparseIntArray sendedDriftShowTimes = new SparseIntArray();

	public DriftInformationAdapter(Context context, List<DriftInformation> data, ImageLoader imageLoader) {
		this.data.addAll(data);
		this.context = context;
		this.mImageLoader = imageLoader;
		mInflater = LayoutInflater.from(context);
		currentUser = ((PhotoTalkApplication) context.getApplicationContext()).getCurrentUser();
	}

	private boolean isSender(DriftInformation info) {
		return info.getSender().getRcId().equals(currentUser.getRcId());
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
		final DriftInformation record = data.get(position);
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
			holder.ivCountry = (ImageView) convertView.findViewById(R.id.iv_country);
			holder.ivCountry.setVisibility(View.GONE);
			holder.ivCountryFlag = (ImageView) convertView.findViewById(R.id.iv_country_flag);
			holder.ivCountryFlag.setVisibility(View.VISIBLE);
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
		String tagBase = record.getPicId() + "";
		String buttonTag = tagBase + Button.class.getName();
		holder.statuButton.setTag(buttonTag);
		String statuTag = tagBase + TextView.class.getName();
		holder.statu.setTag(statuTag);
		holder.bar.setTag(tagBase + ProgressBar.class.getName());
		holder.item_new.setTag(tagBase + ImageView.class.getName());
		holder.ivCountryFlag.setTag(tagBase + ImageView.class.getName() + "country");
		if (record.getState() != InformationState.PhotoInformationState.STATU_NOTICE_OPENED && !isSender(record)) {
			if (record.getInformationCate() == InformationCategory.PHOTO) {
				if (record.hasVoice()) {
					holder.item_new.setImageResource(R.drawable.new_item_voice);
				} else {
					holder.item_new.setImageResource(R.drawable.item_new_bg);
				}
			} else {
				holder.item_new.setImageResource(R.drawable.new_item_video);
			}
			holder.item_new.setVisibility(View.VISIBLE);
		} else {
			holder.item_new.setVisibility(View.GONE);
		}

		if (!isSender(record)) {
			initPhotoInformationReceiverView(record, statuTag, buttonTag, holder);
		} else {
			initPhotoInformationSenderView(record, holder);
		}
		// mImageLoader.displayImage(record.getSender().getHeadUrl(),
		// holder.head);
		RCPlatformImageLoader.loadImage(context, mImageLoader, record.getSender().getHeadUrl(), holder.head);
		if (!isSender(record) && record.getState() != InformationState.PhotoInformationState.STATU_NOTICE_OPENED) {
			holder.name.getPaint().setFakeBoldText(true);
		} else {
			holder.name.getPaint().setFakeBoldText(false);
		}
		holder.name.setText(record.getSender().getNick());
		if (record.getState() == InformationState.PhotoInformationState.STATU_NOTICE_SHOWING || LogicUtils.isSender(context, record)) {
			holder.ivCountryFlag.setVisibility(View.GONE);
		} else {
			holder.ivCountryFlag.setVisibility(View.VISIBLE);
			if (record.getSender().getCountry() != null)
				holder.ivCountryFlag.setImageBitmap(Utils.getAssetCountryFlag(context, record.getSender().getCountry()));
			else
				holder.ivCountryFlag.setImageBitmap(null);
		}

		return convertView;
	}

	private void initPhotoInformationReceiverView(final DriftInformation record, String statuTag, String buttonTag, ViewHolder holder) {
		holder.statuButton.setText(null);
		holder.statuButton.setBackgroundDrawable(null);
		if (record.getState() == InformationState.PhotoInformationState.STATU_NOTICE_SENDED_OR_NEED_LOADD) {
			holder.bar.setVisibility(View.VISIBLE);
			holder.statu.setText(R.string.receive_downloading);
			holder.statuButton.stopDriftTask();
			// holder.statuButton.setBackgroundResource(R.drawable.fish_icon);
			RCPlatformImageLoader.loadPictureForDriftList(context, record);
			// 状态为2，表示已经下载了，但是未查看，
		} else if (record.getState() == InformationState.PhotoInformationState.STATU_NOTICE_DELIVERED_OR_LOADED) {
			if (RCPlatformImageLoader.isFileExist(context, record.getUrl())) {
				// 如果缓存文件存在
				holder.bar.setVisibility(View.GONE);
				holder.statu.setText(getTimeText(R.string.receive_loaded, record.getReceiveTime()));
			} else {
				// 如果缓存文件不存在
				holder.bar.setVisibility(View.VISIBLE);
				holder.statu.setText(R.string.receive_downloading);
				RCPlatformImageLoader.loadPictureForDriftList(context, record);
			}
			holder.statuButton.stopDriftTask();
			// holder.statuButton.setBackgroundResource(R.drawable.fish_icon);
			// 状态为4.表示正在查看
		} else if (record.getState() == InformationState.PhotoInformationState.STATU_NOTICE_SHOWING) {
			holder.bar.setVisibility(View.GONE);
			holder.statu.setText(getTimeText(R.string.receive_loaded, record.getReceiveTime()));
			holder.statuButton.scheuleTask(record);
			holder.statuButton.setBackgroundResource(R.drawable.item_time_bg);
			// 状态为3 表示 已经查看，
		} else if (record.getState() == InformationState.PhotoInformationState.STATU_NOTICE_OPENED) {
			holder.bar.setVisibility(View.GONE);
			holder.statuButton.stopDriftTask();
			holder.statu.setText(getTimeText(R.string.receive_looked, record.getReceiveTime()));
			// holder.statuButton.setBackgroundResource(R.drawable.fish_icon);
			// 状态为5 表示正在下载
		} else if (record.getState() == InformationState.PhotoInformationState.STATU_NOTICE_SENDING_OR_LOADING) {
			holder.bar.setVisibility(View.VISIBLE);
			holder.statu.setText(R.string.receive_downloading);
			holder.statuButton.stopDriftTask();
			// holder.statuButton.setBackgroundResource(R.drawable.fish_icon);
			// 7 下载失败
		} else if (record.getState() == InformationState.PhotoInformationState.STATU_NOTICE_SEND_OR_LOAD_FAIL) {
			holder.bar.setVisibility(View.GONE);
			holder.statu.setText(R.string.receive_fail);
			holder.statuButton.stopDriftTask();
			holder.statuButton.setBackgroundResource(R.drawable.send_failed);
		}
	}

	private void initPhotoInformationSenderView(DriftInformation record, ViewHolder holder) {
		// 如果当前用户是发送者
		// holder.statuButton.setBackgroundResource(R.drawable.throw_icon);
		holder.statuButton.setBackgroundDrawable(null);
		holder.statuButton.setText(null);
		holder.statuButton.stopDriftTask();
		// 状态为1 表示已经发送到服务器
		if (record.getState() == InformationState.PhotoInformationState.STATU_NOTICE_SENDED_OR_NEED_LOADD) {
			holder.bar.setVisibility(View.GONE);
			int showTime = sendedDriftShowTimes.get(record.getPicId());
			if (showTime == 0)
				holder.statu.setText(getTimeText(R.string.send_sended, record.getReceiveTime()));
			else
				holder.statu.setText(RCPlatformTextUtil.getTextFromTimeToNow(context, record.getReceiveTime()) + "-"
						+ context.getString(R.string.drift_show_time, showTime));
			// 状态为2表示对方已经下载
		} else if (record.getState() == InformationState.PhotoInformationState.STATU_NOTICE_DELIVERED_OR_LOADED) {
			holder.bar.setVisibility(View.GONE);
			holder.statu.setText(getTimeText(R.string.send_received, record.getReceiveTime()));
			// 状态为3 表示已经查看
		} else if (record.getState() == InformationState.PhotoInformationState.STATU_NOTICE_OPENED) {
			holder.bar.setVisibility(View.GONE);
			holder.statu.setText(getTimeText(R.string.send_looked, record.getReceiveTime()));
			// 0 表示正在发送
		} else if (record.getState() == InformationState.PhotoInformationState.STATU_NOTICE_SENDING_OR_LOADING) {
			holder.bar.setVisibility(View.VISIBLE);
			holder.statu.setText(R.string.send_sending);
			// 6 表示发送失败
		} else if (record.getState() == InformationState.PhotoInformationState.STATU_NOTICE_SEND_OR_LOAD_FAIL) {
			holder.bar.setVisibility(View.GONE);
			holder.statu.setText(R.string.send_fail);
		}
	}

	private String getTimeText(int baseResId, long time) {
		return context.getString(baseResId, RCPlatformTextUtil.getTextFromTimeToNow(context, time));
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
		ImageView ivCountry;
		ImageView ivCountryFlag;
	}

	public List<DriftInformation> getData() {
		return data;
	}

	public void addData(List<DriftInformation> data) {
		for (DriftInformation info : data) {
			this.data.addFirst(info);
		}
	}

	public void addDataAtLast(List<DriftInformation> data) {
		this.data.addAll(data);
	}

	public static interface OnInformationPressListener {
		public void onPress(Information information);
	}

	public DriftInformation getPressedInformation() {
		return mPressedInformation;
	}

	public void resetPressedInformation() {
		mPressedInformation = null;
		mPressedPosition = -1;
	}

	public int getPressedPosition() {
		return mPressedPosition;
	}

	public void addShowTimes(SparseIntArray times) {
		for (int i = 0; i < times.size(); i++) {
			int key = times.keyAt(i);
			sendedDriftShowTimes.put(key, times.get(key));
		}
	}
}
