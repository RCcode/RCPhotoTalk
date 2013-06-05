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
import com.rcplatform.phototalk.utils.FileDownloader.OnLoadingListener;
import com.rcplatform.phototalk.utils.PhotoTalkUtils;
import com.rcplatform.phototalk.utils.RCPlatformTextUtil;

public class HomeRecordLoadPicListener implements OnLoadingListener {

	private final ListView listView;

	private final Context context;

	private final Information record;

	public HomeRecordLoadPicListener(ListView listView, Context context, Information record) {
		this.listView = listView;
		this.context = context;
		this.record = record;
	}

	@Override
	public void onStartLoad() {
		record.setStatu(InformationState.PhotoInformationState.STATU_NOTICE_SENDING_OR_LOADING);
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
		String text = context.getString(R.string.receive_loaded, RCPlatformTextUtil.getTextFromTimeToNow(context, record.getReceiveTime()));
		updateView(View.GONE, text);
	}

	@Override
	public void onDownloadFail() {
		record.setStatu(InformationState.PhotoInformationState.STATU_NOTICE_SEND_OR_LOAD_FAIL);
		PhotoTalkDatabaseFactory.getDatabase().updateInformationState(record);
		updateView(View.GONE, context.getResources().getString(R.string.receive_fail));
	}

	private static void notifyServer(Context context, Information record) {
		if (!record.getReceiver().getRcId().equals(record.getSender().getRcId())) {
			MessageSender.getInstance().sendInformation(context, record.getSender().getTigaseId(), record.getSender().getRcId(), record);
		}
		LogicUtils.serviceCensus(context, record);
	}

	private void updateView(int visibitity, String text) {
		String baseTag = PhotoTalkUtils.getInformationTagBase(record);
		if (listView != null) {
			ProgressBar bar = (ProgressBar) listView.findViewWithTag(baseTag + ProgressBar.class.getName());
			if (bar != null)
				bar.setVisibility(visibitity);
			TextView statu = (TextView) listView.findViewWithTag(baseTag + TextView.class.getName());
			if (statu != null)
				statu.setText(text);

		}
	}
}
