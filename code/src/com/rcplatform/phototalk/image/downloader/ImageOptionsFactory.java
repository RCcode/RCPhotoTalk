package com.rcplatform.phototalk.image.downloader;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.rcplatform.phototalk.R;

public class ImageOptionsFactory {

	private static final int DEFAULT_HEAD_DRAWABLE = R.drawable.default_head;
	private static final int DEFAULT_USERINFO_BACK_DRAWABLE = R.drawable.user_detail_bg;
	private static DisplayImageOptions mCircleImageOption;

	private static DisplayImageOptions defaultImageOption = new DisplayImageOptions.Builder().cacheInMemory().cacheOnDisc()
			.showStubImage(DEFAULT_HEAD_DRAWABLE).showImageOnFail(DEFAULT_HEAD_DRAWABLE).showImageForEmptyUri(DEFAULT_HEAD_DRAWABLE).build();
	private static DisplayImageOptions mHeadImageOptions;
	private static DisplayImageOptions mBackImageOptions;

	public static DisplayImageOptions getHeadImageOptions() {
		if (mHeadImageOptions == null)
			mHeadImageOptions = new DisplayImageOptions.Builder().showImageForEmptyUri(R.drawable.default_head).showImageOnFail(DEFAULT_HEAD_DRAWABLE).build();
		return mHeadImageOptions;
	}

	public static DisplayImageOptions getUserBackImageOptions() {
		if (mBackImageOptions == null)
			mBackImageOptions = new DisplayImageOptions.Builder().showImageForEmptyUri(R.drawable.user_detail_bg).showStubImage(R.drawable.user_detail_bg)
					.showImageOnFail(R.drawable.user_detail_bg).cacheInMemory().cacheOnDisc().build();
		return mBackImageOptions;
	}

	public static DisplayImageOptions getDefaultImageOptions() {
		return defaultImageOption;
	}

	public static DisplayImageOptions getCircleImageOption() {
		if (mCircleImageOption == null)
			mCircleImageOption = new DisplayImageOptions.Builder().showImageForEmptyUri(DEFAULT_HEAD_DRAWABLE).showImageOnFail(DEFAULT_HEAD_DRAWABLE)
					.showStubImage(DEFAULT_HEAD_DRAWABLE).displayer(new RoundedBitmapDisplayer(90)).cacheInMemory().cacheOnDisc().build();
		return mCircleImageOption;
	}
}
