package com.rcplatform.phototalk.image.downloader;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.rcplatform.phototalk.R;

public class ImageOptionsFactory {

	private static final int DEFAULT_HEAD_DRAWABLE = R.drawable.default_head;
	private static final int DEFAULT_USERINFO_BACK_DRAWABLE = R.drawable.user_detail_bg;

	private static DisplayImageOptions defaultImageOption = new DisplayImageOptions.Builder().cacheInMemory().cacheOnDisc()
			.showStubImage(DEFAULT_HEAD_DRAWABLE).showImageOnFail(DEFAULT_HEAD_DRAWABLE).showImageForEmptyUri(DEFAULT_HEAD_DRAWABLE).build();
	private static DisplayImageOptions mHeadImageOptions;
	private static DisplayImageOptions mBackImageOptions;

	public static DisplayImageOptions getHeadImageOptions() {
		if (mHeadImageOptions == null)
			mHeadImageOptions = new DisplayImageOptions.Builder().showImageForEmptyUri(R.drawable.default_head).showStubImage(R.drawable.default_head).build();
		return mHeadImageOptions;
	}

	public static DisplayImageOptions getUserBackImageOptions() {
		if (mHeadImageOptions == null)
			mHeadImageOptions = new DisplayImageOptions.Builder().showImageForEmptyUri(R.drawable.user_detail_bg).showStubImage(R.drawable.user_detail_bg)
					.showImageOnFail(R.drawable.user_detail_bg).build();
		return mBackImageOptions;
	}

	public static DisplayImageOptions getDefaultImageOptions() {
		return defaultImageOption;
	}
}
