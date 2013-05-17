package com.rcplatform.phototalk.listener;

import android.content.Context;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.InformationState;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.logic.LogicUtils;
import com.rcplatform.phototalk.logic.MessageSender;
import com.rcplatform.phototalk.utils.Contract.Action;
import com.rcplatform.phototalk.utils.FileDownloader.OnLoadingListener;
import com.rcplatform.phototalk.utils.PhotoTalkUtils;
import com.rcplatform.phototalk.utils.RCPlatformTextUtil;

public class HomeRecordLoadPicListener implements OnLoadingListener {

	private final ListView listView;

	private ProgressBar bar;

	private TextView statu;

	private final Context context;

	private final Information record;

	public HomeRecordLoadPicListener(ListView listView, ProgressBar bar, TextView textView, Context context, Information record) {
		super();
		this.listView = listView;
		this.bar = bar;
		this.statu = textView;
		this.context = context;
		this.record = record;
	}

	@Override
	public void onStartLoad() {
		record.setStatu(InformationState.PhotoInformationState.STATU_NOTICE_LOADING);
		updateView(View.VISIBLE, context.getString(R.string.receive_downloading));
	}

	@Override
	public void onDownloadSuccess() {
		record.setLastUpdateTime(System.currentTimeMillis());
		if (record.getStatu() != InformationState.PhotoInformationState.STATU_NOTICE_DELIVERED_OR_LOADED) {
			record.setStatu(InformationState.PhotoInformationState.STATU_NOTICE_DELIVERED_OR_LOADED);
			PhotoTalkDatabaseFactory.getDatabase().updateInformationState(record);
			notifyServer(context, record);
		}
		record.setLastUpdateTime(System.currentTimeMillis());
		String text = context.getString(R.string.receive_loaded, RCPlatformTextUtil.getTextFromTimeToNow(context, record.getCreatetime()));
		updateView(View.GONE, text);
	}

	@Override
	public void onDownloadFail() {
		record.setStatu(InformationState.PhotoInformationState.STATU_NOTICE_LOAD_FAIL);
		PhotoTalkDatabaseFactory.getDatabase().updateInformationState(record);
		updateView(View.GONE, context.getResources().getString(R.string.receive_fail));
	}

	private static void notifyServer(Context context, Information record) {
		MessageSender.sendInformation(context, record.getSender().getTigaseId(), record.getSender().getRcId(), record);
		LogicUtils.updateInformationState(context, Action.ACTION_INFORMATION_STATE_CHANGE, record);
	}

	private void updateView(int visibitity, String text) {
		String baseTag = PhotoTalkUtils.getInformationTagBase(record);
		if (bar == null) {
			if (listView != null) {
				bar = (ProgressBar) listView.findViewWithTag(baseTag + ProgressBar.class.getName());
			}
		}
		if (bar != null) {
			String barTag = (String) bar.getTag();
			if (barTag.equals(baseTag + ProgressBar.class.getName())) {
				bar.setVisibility(visibitity);
			}
		}

		if (statu == null) {
			if (listView != null) {
				statu = (TextView) listView.findViewWithTag(baseTag + TextView.class.getName());
			}
		}

		if (statu != null) {
			String statuTag = (String) statu.getTag();
			if (statuTag.equals(baseTag + TextView.class.getName())) {
				statu.setText(text);
			}
		}
	}
}
