package com.rcplatform.phototalk.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * 标题、简要说明. <br>
 * 类详细说明.
 * <p>
 * Copyright: Menue,Inc Copyright (c) 2013-1-16 下午3:09:24
 * <p>
 * Team:Menue Beijing
 * <p>
 * 
 * @author jason.wu@menue.com.cn
 * @version 1.0.0
 */
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
