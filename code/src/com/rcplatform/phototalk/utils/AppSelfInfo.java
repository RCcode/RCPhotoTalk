package com.rcplatform.phototalk.utils;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import android.app.Activity;
import android.os.Environment;
import android.util.DisplayMetrics;

public class AppSelfInfo {

	public static final String SINA_APP_KEY = "3861940940";
	public static final String SINA_REDIRECT_URL = "http://www.sina.com";

	public static final String TENCENT_APP_KEY = "801115505";
	public static final String TENCENT_APP_SECRET = "be1dd1410434a9f7d5a2586bab7a6829";
	public static final String TWITTER_APP_KEY = "avqYEua265Ce3K3bBP4Q";
	public static final String TWITTER_APP_SECRET = "xQFTT41dw3AcBqhs3H2SmB2ZPxxUsAB9tUxB4Y6g";

	private static final String PATH_MIRROR = "menue/mirror_result.jpg";
	private static final String PATH_PHOTO = "menue/photo_temp.jpg";
	private static final String PATH_CUT_TEMP = "menue/cut_temp.jpg";
	// private static final String PATH_SHARE_TEMP="menue/share_temp.jpg";

	public static int screenWidthPx;
	public static int screenHeightPx;
	public static String language;
	public static String country;
	public static File photoTempFile;
	public static File mirrorResultFile;
	public static File cutTempFile;

	// public static File shareTempFile;

	public static void initApplicationInfo(Activity context) {
		DisplayMetrics dm = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(dm);
		screenWidthPx = dm.widthPixels;
		screenHeightPx = dm.heightPixels;
		language = Locale.getDefault().getLanguage();
		country = Locale.getDefault().getCountry();
		if (language.equals("zh")) {
			language = language + "_" + country;
		}
		try {
			initTempFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		initImageScaleInfo(context);

	}

	public static void initImageScaleInfo(Activity context) {
		// ImageScaleInfo.thumbnailImageWidthPx = (screenWidthPx
		// - context.getResources().getDimensionPixelSize(
		// R.dimen.detail_image_padding) * 2 - context
		// .getResources()
		// .getDimensionPixelSize(R.dimen.detail_gv_padding) * 2) / 3;
		// // ImageScaleInfo.circleUserHeadRadius=(int)
		// // ((screenWidthPx*((float)98/720))/2);
		// ImageScaleInfo.circleUserHeadRadius = context.getResources()
		// .getDimensionPixelSize(R.dimen.user_icon_height) / 2;
		// ImageScaleInfo.bigImageWidthPx = screenWidthPx
		// - context.getResources().getDimensionPixelSize(
		// R.dimen.image_scan_item_padding) * 2;
		// ImageScaleInfo.imageScanHeadImageWidth = context.getResources()
		// .getDimensionPixelSize(R.dimen.image_scan_head_width);
	}

	public static void initTempFile() throws IOException {
		if (isExternalStorageUsable()) {
			photoTempFile = new File(Environment.getExternalStorageDirectory(),
					PATH_PHOTO);
			mirrorResultFile = new File(
					Environment.getExternalStorageDirectory(), PATH_MIRROR);
			cutTempFile = new File(Environment.getExternalStorageDirectory(),
					PATH_CUT_TEMP);
			if (!photoTempFile.exists()) {
				photoTempFile.createNewFile();

			}
			if (!mirrorResultFile.exists()) {
				mirrorResultFile.createNewFile();

			}
			if (!cutTempFile.exists()) {
				cutTempFile.createNewFile();
			}

		}
	}

	public static class ImageScaleInfo {
		public static int bigImageWidthPx;
		public static int thumbnailImageWidthPx;
		public static int circleUserHeadRadius;
		public static int imageScanHeadImageWidth;
	}

	public static boolean isExternalStorageUsable() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

}
