package com.rcplatform.phototalk;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.task.CheckUpdateTask;
import com.rcplatform.phototalk.task.CheckUpdateTask.OnUpdateCheckListener;

public class AboutActivity extends BaseActivity implements OnClickListener, DialogInterface.OnClickListener {
	private TextView tvCheckUpdate;
	private String updateUrl;
	private AlertDialog mUpdateDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_about);
		initView();
	}

	private void initView() {
		initBackButton(R.string.about, this);
		tvCheckUpdate = (TextView) findViewById(R.id.tv_check_update);
		tvCheckUpdate.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_check_update:
			checkUpdate();
			break;
		case R.id.title_linear_back:
			finish();
			break;
		}
	}

	private void checkUpdate() {
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		CheckUpdateTask task = new CheckUpdateTask(this, false);
		task.setOnUpdateCheckListener(new OnUpdateCheckListener() {

			@Override
			public void onHasUpdate(String versionCode, String updateContent, String updateUrl) {
				dismissLoadingDialog();
				showUpdateDialog(versionCode, updateContent, updateUrl);
			}

			@Override
			public void onNoNewVersion() {
				dismissLoadingDialog();
				showErrorConfirmDialog(getString(R.string.no_new_version));
			}

			@Override
			public void onError(int statusCode, String message) {
				dismissLoadingDialog();
				showErrorConfirmDialog(message);
			}
		});
		task.start();
	}

	private void showUpdateDialog(String versionCode, String updateContent, String updateUrl) {
		this.updateUrl = updateUrl;
		if (mUpdateDialog == null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this).setMessage(updateContent).setTitle(getString(R.string.update_dialog_title, getString(R.string.app_name), versionCode)).setNegativeButton(R.string.update_now, this).setPositiveButton(R.string.attention_later, this);
			mUpdateDialog = builder.create();
		}
		mUpdateDialog.show();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case DialogInterface.BUTTON_NEGATIVE:

			break;
		}
	}
}
