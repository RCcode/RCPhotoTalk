package com.rcplatform.phototalk.listener;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.bean.InformationState;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.drift.DriftInformation;
import com.rcplatform.phototalk.logic.controller.DriftInformationPageController;
import com.rcplatform.phototalk.utils.FileDownloader.OnLoadingListener;
import com.rcplatform.phototalk.utils.RCPlatformTextUtil;
import com.rcplatform.phototalk.utils.Utils;
import com.rcplatform.phototalk.views.RecordTimerLimitView;

public class DriftInformationPicListener implements OnLoadingListener {

	private final Context context;
	private final DriftInformation record;

	public DriftInformationPicListener(Context context, DriftInformation record) {
		this.context = context;
		this.record = record;
	}

	@Override
	public void onStartLoad() {
		record.setState(InformationState.PhotoInformationState.STATU_NOTICE_SENDING_OR_LOADING);
		updateView(View.VISIBLE, context.getString(R.string.receive_downloading), true);
	}

	@Override
	public void onDownloadSuccess() {
		if (record.getState() != InformationState.PhotoInformationState.STATU_NOTICE_DELIVERED_OR_LOADED) {
			record.setState(InformationState.PhotoInformationState.STATU_NOTICE_DELIVERED_OR_LOADED);
			PhotoTalkDatabaseFactory.getDatabase().updateDriftInformationState(record.getPicId(),
					InformationState.PhotoInformationState.STATU_NOTICE_DELIVERED_OR_LOADED);
		}
		String text = context.getString(R.string.receive_loaded, RCPlatformTextUtil.getTextFromTimeToNow(context, record.getReceiveTime()));
		updateView(View.GONE, text, true);
	}

	@Override
	public void onDownloadFail() {
		PhotoTalkDatabaseFactory.getDatabase().updateDriftInformationState(record.getPicId(),
				InformationState.PhotoInformationState.STATU_NOTICE_SEND_OR_LOAD_FAIL);
		updateView(View.GONE, context.getResources().getString(R.string.receive_fail), false);
	}

	private void updateView(int visibitity, String text, boolean isSuccess) {
		String baseTag = record.getPicId() + "";
		String buttonTag = baseTag + Button.class.getName();
		String countryTag = baseTag + ImageView.class.getName() + "country";
		ListView listView = DriftInformationPageController.getInstance().getListView();
		if (listView != null) {
			ProgressBar bar = (ProgressBar) listView.findViewWithTag(baseTag + ProgressBar.class.getName());
			if (bar != null)
				bar.setVisibility(visibitity);
			TextView statu = (TextView) listView.findViewWithTag(baseTag + TextView.class.getName());
			if (statu != null)
				statu.setText(text);
			RecordTimerLimitView timerLimitView = (RecordTimerLimitView) listView.findViewWithTag(buttonTag);
			ImageView ivCountry = (ImageView) listView.findViewWithTag(countryTag);
			if (ivCountry != null) {
				if (isSuccess) {
					ivCountry.setImageBitmap(Utils.getAssetCountryFlag(context, record.getSender().getCountry()));
					ivCountry.setVisibility(View.VISIBLE);
				} else {
					ivCountry.setVisibility(View.GONE);
				}
			}
			if (timerLimitView != null) {
				if (isSuccess) {
					timerLimitView.setText(null);
					timerLimitView.setBackgroundDrawable(null);
				} else {
					timerLimitView.setVisibility(View.VISIBLE);
					timerLimitView.setBackgroundResource(R.drawable.send_failed);
				}
			}
		}
	}
}
