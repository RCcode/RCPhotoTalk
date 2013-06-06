package com.rcplatform.videotalk.utils;

import android.content.Context;
import android.widget.Toast;

public class ShowToast {

	Context context;

	public ShowToast(Context context) {
		this.context = context;
	}

	public static void showToast(Context context, String message, int duration) {

		Toast.makeText(context, message, duration).show();
	}

	public static void showToast(Context context, String message) {

		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}

}
