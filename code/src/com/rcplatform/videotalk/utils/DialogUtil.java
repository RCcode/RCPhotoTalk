package com.rcplatform.videotalk.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.rcplatform.videotalk.R;

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

	public static Dialog createMsgDialog(Context context, String msg, String positive, String negative) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
		dialogBuilder.setMessage(msg).setCancelable(false).setPositiveButton(positive, new DialogInterface.OnClickListener() {

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

	public static void showInformationClearConfirmDialog(Context context, int msgId, int posResId, int negResId, DialogInterface.OnClickListener listener) {
		new AlertDialog.Builder(context).setMessage(msgId).setPositiveButton(posResId, listener).setNegativeButton(negResId, listener).create().show();
	}
}
