package com.rcplatform.phototalk;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.bean.UserInfo;

public class SystemSettingActivity extends BaseActivity implements OnClickListener {

	private View mReceiveSettingView;
	private View mDynamicSettingView;
	private UserInfo mCurrentUser;
	private AlertDialog mTrendSetDialog;
	private AlertDialog mReceiveSetDialog;

	private String[] trendSets;
	private String[] receiveSets;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.system_setting);
		initData();

		initView();
	}

	private void initData() {
		mCurrentUser = getPhotoTalkApplication().getCurrentUser();
		trendSets = new String[2];
		receiveSets = new String[2];
		trendSets[0] = getString(R.string.trends_share);
		trendSets[1] = getString(R.string.trends_hide);
		receiveSets[0] = getString(R.string.receive_all);
		receiveSets[1] = getString(R.string.receive_friend);
	}

	private void initView() {
		mReceiveSettingView = findViewById(R.id.rela_resetting);
		mDynamicSettingView = findViewById(R.id.rela_dysetting);
		mReceiveSettingView.setOnClickListener(this);
		mDynamicSettingView.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rela_resetting:
			showTrendSetDialog();
			break;
		case R.id.rela_dysetting:
			showReceiveSetDialog();
			break;
		}
	}

	private void showTrendSetDialog() {
		if (mTrendSetDialog == null) {
			mTrendSetDialog = new AlertDialog.Builder(this).setSingleChoiceItems(trendSets, mCurrentUser.getTrendsSet(), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

				}
			}).create();
		}
		mTrendSetDialog.show();
	}

	private void showReceiveSetDialog() {
		if (mReceiveSetDialog == null) {
			mReceiveSetDialog = new AlertDialog.Builder(this).setSingleChoiceItems(receiveSets, mCurrentUser.getReceiveSet(), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

				}
			}).create();
		}
		mReceiveSetDialog.show();
	}
}
