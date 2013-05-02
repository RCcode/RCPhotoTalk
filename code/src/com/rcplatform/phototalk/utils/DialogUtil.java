package com.rcplatform.phototalk.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.rcplatform.phototalk.R;

public class DialogUtil {

	private static Toast mToast;

	private static Toast mToastImage;

	private static View mToastView;

	private static class ToastViewHolder {

		public ImageView bigImageView;

		public ImageView smallImageView;
	}

	public static void showToast(Context context, String text, int duration) {
		if (mToast == null) {
			if (context != null)
				mToast = Toast.makeText(context.getApplicationContext(), text, duration);
			else
				return;
		}
		mToast.setDuration(duration);
		mToast.setText(text);
		mToast.show();
	}

	public static void showToast(Context context, int resId, int duration) {
		if (mToast == null) {
			if (context != null)
				mToast = Toast.makeText(context.getApplicationContext(), resId, duration);
			else
				return;
		}
		mToast.setDuration(duration);
		mToast.setText(resId);
		mToast.show();
	}

	public static void showToastImage(Context context, Drawable drawable, Drawable drawResult, int duration) {
		if (mToastImage == null) {
			if (context != null)
				mToastImage = new Toast(context);
			else
				return;
		}
		if (mToastView == null) {
			if (context != null) {
				// mToastView =
				// LayoutInflater.from(context).inflate(R.layout.toast_view,
				// null);
				// ViewGroup.LayoutParams params = new
				// ViewGroup.LayoutParams(context.getResources().getDimensionPixelSize(R.dimen.toast_view_width),
				// context.getResources().getDimensionPixelSize(R.dimen.toast_view_height));
				// mToastView.setLayoutParams(params);
				// ToastViewHolder holder = new ToastViewHolder();
				// holder.bigImageView = (ImageView)
				// mToastView.findViewById(R.id.iv_toast_big);
				// holder.smallImageView = (ImageView)
				// mToastView.findViewById(R.id.iv_toast_small);
				// mToastView.setTag(holder);
			} else {
				return;
			}
		}
		ToastViewHolder holder = (ToastViewHolder) mToastView.getTag();
		holder.bigImageView.setImageDrawable(drawable);
		holder.smallImageView.setImageDrawable(drawResult);
		mToastImage.setGravity(Gravity.CENTER, 0, 0);
		mToastImage.setView(mToastView);
		mToastImage.show();
	}

	public static Dialog createMsgDialog(Context context, String message, String positive) {

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
		dialogBuilder.setMessage(message).setCancelable(false).setPositiveButton(positive, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});

		return dialogBuilder.create();
	}
	public static Dialog createMsgDialog(Context context, int message, int positive) {
		
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
		dialogBuilder.setMessage(message).setCancelable(false).setPositiveButton(positive, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		
		return dialogBuilder.create();
	}

	public static Dialog createErrorInfoDialog(Context context, int resId) {

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
		dialogBuilder.setMessage(resId).setCancelable(false).setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		return dialogBuilder.create();
	}
	public static Dialog createErrorInfoDialog(Context context, String msg) {
		
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
		dialogBuilder.setMessage(msg).setCancelable(false).setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		return dialogBuilder.create();
	}

	public static Dialog createMsgDialog(Context context, String title, String positive, String negative) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
		dialogBuilder.setMessage(title).setCancelable(false).setPositiveButton(positive, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		}).setNegativeButton(negative, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		return dialogBuilder.create();
	}

	public static AlertDialog.Builder showConfirmDialog(Context context, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		// builder.setTitle(context.getResources().getString(R.string.prompt));
		builder.setMessage(message);
		return builder;
	}

	// public static AlertDialog getConfirmDialog(Context context, int msgResId,
	// OnClickListener onClickListener) {
	// View view = LayoutInflater.from(context).inflate(
	// R.layout.confirm_dialog, null);
	// TextView tv = (TextView) view.findViewById(R.id.tv_dialo_message);
	// tv.setText(msgResId);
	// Button btnConfirm = (Button) view.findViewById(R.id.btn_confirm);
	// Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);
	// btnCancel.setOnClickListener(onClickListener);
	// btnConfirm.setOnClickListener(onClickListener);
	// AlertDialog.Builder builder = new AlertDialog.Builder(context);
	// AlertDialog dialog = builder.create();
	// dialog.show();
	// WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
	// lp.width = context.getResources().getDimensionPixelSize(
	// R.dimen.delete_tag_width);// 定义宽度
	// lp.height = context.getResources().getDimensionPixelSize(
	// R.dimen.delete_tag_height);// 定义高度
	// dialog.getWindow().setAttributes(lp);
	//
	// dialog.setContentView(view);
	// return dialog;
	// }
}
