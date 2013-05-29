package com.rcplatform.phototalk.image.downloader;

import android.graphics.Bitmap.Config;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.rcplatform.phototalk.R;

public class ImageOptionsFactory {

	private static final int DEFAULT_HEAD_DRAWABLE = R.drawable.default_head;

	private static DisplayImageOptions defaultImageOption = new DisplayImageOptions.Builder().cacheInMemory().cacheOnDisc().bitmapConfig(Config.RGB_565)
			.showStubImage(DEFAULT_HEAD_DRAWABLE).showImageOnFail(DEFAULT_HEAD_DRAWABLE).showImageForEmptyUri(DEFAULT_HEAD_DRAWABLE)
			.imageScaleType(ImageScaleType.EXACTLY).build();
	private static DisplayImageOptions mHeadImageOptions;

	public static DisplayImageOptions getHeadImageOptions() {
		if (mHeadImageOptions == null)
			mHeadImageOptions = new DisplayImageOptions.Builder().showImageForEmptyUri(R.drawable.default_head).showStubImage(R.drawable.default_head).build();
		return mHeadImageOptions;
	}

	public static DisplayImageOptions getDefaultImageOptions() {
		return defaultImageOption;
	}
}
