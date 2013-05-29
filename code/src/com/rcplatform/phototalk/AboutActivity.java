package com.rcplatform.phototalk;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.task.CheckUpdateTask;
import com.rcplatform.phototalk.task.CheckUpdateTask.OnUpdateCheckListener;
import com.rcplatform.phototalk.utils.SystemMessageUtil;

public class AboutActivity extends BaseActivity implements OnClickListener,
		DialogInterface.OnClickListener {
	private Button checkUpdate;
	private String updateUrl;
	private AlertDialog mUpdateDialog;
	private Button contact_us_btn, term_service_btn, compact_btn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_about);
		initView();
	}

	private void initView() {
		initBackButton(R.string.about, this);
		checkUpdate = (Button) findViewById(R.id.check_update_btn);
		checkUpdate.setOnClickListener(this);
		contact_us_btn = (Button) findViewById(R.id.contact_us_btn);
		contact_us_btn.setOnClickListener(this);
		term_service_btn = (Button) findViewById(R.id.term_service_btn);
		term_service_btn.setOnClickListener(this);
		compact_btn = (Button) findViewById(R.id.compact_btn);
		compact_btn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.check_update_btn:
			checkUpdate();
			break;
		case R.id.back:
			finish();
			break;
		case R.id.contact_us_btn:
			Intent email = new Intent(android.content.Intent.ACTION_SENDTO,Uri.fromParts("mailto", "rctalk.service@gmail.com", null));
			String emailSubject = SystemMessageUtil.getLanguage(baseContext)
					+ SystemMessageUtil.getAppName(baseContext)
					+ SystemMessageUtil.getPhoneNumber(baseContext)
					+ SystemMessageUtil.getNetworkName(baseContext)
					+ SystemMessageUtil.getImsi(baseContext);
			// email.putExtra(android.content.Intent.EXTRA_SUBJECT,
			// emailSubject);
			// 设置要默认发送的内容
			email.putExtra(android.content.Intent.EXTRA_TEXT, emailSubject);
			// 调用系统的邮件系统
			startActivity(email);

			break;
		case R.id.term_service_btn:
			break;
		case R.id.compact_btn:
			break;
		}
	}

	private void checkUpdate() {
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		CheckUpdateTask task = new CheckUpdateTask(this, false);
		task.setOnUpdateCheckListener(new OnUpdateCheckListener() {

			@Override
			public void onHasUpdate(String versionCode, String updateContent,
					String updateUrl) {
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

	private void showUpdateDialog(String versionCode, String updateContent,
			String updateUrl) {
		this.updateUrl = updateUrl;
		if (mUpdateDialog == null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this)
					.setMessage(updateContent)
					.setTitle(
							getString(R.string.update_dialog_title,
									getString(R.string.app_name), versionCode))
					.setNegativeButton(R.string.update_now, this)
					.setPositiveButton(R.string.attention_later, this);
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
