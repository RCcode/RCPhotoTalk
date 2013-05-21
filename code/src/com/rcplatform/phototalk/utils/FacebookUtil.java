package com.rcplatform.phototalk.utils;

import android.content.Context;

import com.facebook.Session;
import com.rcplatform.phototalk.thirdpart.utils.ThirdPartAccessTokenKeeper;

public class FacebookUtil {

	public static void clearFacebookVlidated(Context context) {
		ThirdPartAccessTokenKeeper.removeFacebookAccessToekn(context);
		Session s = Session.getActiveSession();
		if (s != null) {
			s.closeAndClearTokenInformation();
		}
	}
}
