package com.rcplatform.videotalk.utils;

import android.content.Context;

import com.facebook.Session;

public class FacebookUtil {

	public static void clearFacebookVlidated(Context context) {
		Session s = Session.getActiveSession();
		if (s != null) {
			s.closeAndClearTokenInformation();
		}
	}
}
