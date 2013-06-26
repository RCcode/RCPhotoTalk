package com.rcplatform.phototalk.thirdpart.utils;

import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.activity.ActivityFunction;
import com.rcplatform.phototalk.utils.DialogUtil;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public abstract class ThirdPartClient {

	protected ActivityFunction mContext;

	public ThirdPartClient(ActivityFunction activityFunction) {
		mContext = activityFunction;
	}

	public abstract void authorize(OnAuthorizeSuccessListener listener);

	public abstract void sendJoinMessage();

	public final void sendJoinMessage(String msg) {
		showShareConfirmDialog(msg);
	}
	public final void sendJoinMessage(int resId) {
		showShareConfirmDialog(mContext.getContext().getString(resId));
	}
	
	protected abstract void doSendJoinMessage(String msg);
	
	public abstract void setInviteMessage();

	public abstract void deAuthorize(OnDeAuthorizeListener listener);

	public abstract void getThirdPartInfo(OnGetThirdPartInfoSuccessListener listener);

	public abstract void onAuthorizeInformationReceived(int requestCode, int resultCode, Intent data);

	public abstract boolean isAuthorized();

	private void showShareConfirmDialog(String msg) {
		View view = LayoutInflater.from(mContext.getContext()).inflate(R.layout.share_edit, null);
		final EditText etShare = (EditText) view.findViewById(R.id.et_share_text);
		etShare.setText(mContext.getContext().getString(R.string.join_message, mContext.getCurrentUser().getRcId()));
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == DialogInterface.BUTTON_POSITIVE) {
					String shareText = etShare.getText().toString().trim();
					if (TextUtils.isEmpty(shareText)) {
						dialog.dismiss();
					} else {
						doSendJoinMessage(shareText);
					}
				}
			}
		};
		AlertDialog shareEditDialog = DialogUtil.getAlertDialogBuilder(mContext.getContext()).setTitle(R.string.share_title)
				.setNegativeButton(R.string.cancel, listener).setPositiveButton(R.string.confirm, listener).setView(view).create();
		shareEditDialog.show();
	}
	public abstract void destroy();
}
