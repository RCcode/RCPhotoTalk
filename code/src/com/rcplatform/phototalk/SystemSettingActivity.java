package com.rcplatform.phototalk;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.proxy.UserSettingProxy;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.umeng.EventUtil;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.PrefsUtils;

public class SystemSettingActivity extends BaseActivity implements OnClickListener {

	private View mReceiveSettingView;
	private View mDynamicSettingView;
	private UserInfo mCurrentUser;
	private AlertDialog mTrendSetDialog;
	private AlertDialog mReceiveSetDialog;

	private TextView tvReceiveSet;
	private TextView tvTrendSet;

	private String[] trendSets;
	private String[] receiveSets;

	private int lastReceiveSet;
	private int lastTrendSet;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.system_setting);
		initData();
		initView();
	}

	private void initData() {
		initBackButton(R.string.setting, this);
		mCurrentUser = getPhotoTalkApplication().getCurrentUser();
		trendSets = new String[2];
		receiveSets = new String[2];
		trendSets[0] = getString(R.string.trends_share);
		trendSets[1] = getString(R.string.trends_hide);
		receiveSets[0] = getString(R.string.receive_all);
		receiveSets[1] = getString(R.string.receive_friend);
		lastReceiveSet = mCurrentUser.getAllowsend();
		lastTrendSet = mCurrentUser.getShareNews();
	}

	private void setSetText() {
		tvReceiveSet.setText(receiveSets[mCurrentUser.getAllowsend()]);
		tvTrendSet.setText(trendSets[mCurrentUser.getShareNews()]);
	}

	private void initView() {
		mReceiveSettingView = findViewById(R.id.rela_resetting);
		mDynamicSettingView = findViewById(R.id.rela_dysetting);
		mReceiveSettingView.setOnClickListener(this);
		mDynamicSettingView.setOnClickListener(this);
		tvReceiveSet = (TextView) findViewById(R.id.tv_resetting_current);
		tvTrendSet = (TextView) findViewById(R.id.tv_dysetting_current);
		setSetText();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rela_resetting:
			showReceiveSetDialog();
			break;
		case R.id.rela_dysetting:
			showTrendSetDialog();
			break;
		case R.id.back:
			finishPage();
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finishPage();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void finishPage() {
		if (hasChange()) {
			updateSetChange();
		} else
			finish();
	}

	private void updateSetChange() {
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		UserSettingProxy.updateUserSetting(this, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				PrefsUtils.User.setUserReceiveSet(getApplicationContext(), mCurrentUser.getRcId(), mCurrentUser.getAllowsend());
				PrefsUtils.User.setUserTrendSet(getApplicationContext(), mCurrentUser.getRcId(), mCurrentUser.getShareNews());
				dismissLoadingDialog();
				finish();
			}

			@Override
			public void onFailure(int errorCode, String content) {
				dismissLoadingDialog();
				showErrorConfirmDialog(content);
			}
		}, mCurrentUser);
	}

	private boolean hasChange() {
		return !(mCurrentUser.getShareNews() == lastTrendSet && mCurrentUser.getAllowsend() == lastReceiveSet);
	}

	private void showTrendSetDialog() {
		if (mTrendSetDialog == null) {
			mTrendSetDialog = DialogUtil.getAlertDialogBuilder(this).setSingleChoiceItems(trendSets, mCurrentUser.getShareNews(), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(0==which){
						EventUtil.More_Setting.rcpt_share(baseContext);
					}else if(1==which){
						EventUtil.More_Setting.rcpt_notshare(baseContext);
						
					}
					mCurrentUser.setShareNews(which);
					setSetText();
					dialog.dismiss();
				}
			}).create();
		}
		mTrendSetDialog.show();
	}

	private void showReceiveSetDialog() {
		if (mReceiveSetDialog == null) {
			mReceiveSetDialog = DialogUtil.getAlertDialogBuilder(this).setSingleChoiceItems(receiveSets, mCurrentUser.getAllowsend(), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(0==which){
						EventUtil.More_Setting.rcpt_anyone(baseContext);
					}else if(1==which){
						EventUtil.More_Setting.rcpt_onlyfirends(baseContext);
						
					}
					mCurrentUser.setAllowsend(which);
					setSetText();
					dialog.dismiss();
				}
			}).create();
		}
		mReceiveSetDialog.show();
	}
}
