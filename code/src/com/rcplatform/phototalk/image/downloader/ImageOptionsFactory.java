package com.rcplatform.phototalk.image.downloader;

import android.graphics.Bitmap.Config;
import android.text.TextUtils;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.rcplatform.phototalk.R;

public class ImageOptionsFactory {

	public static final int IMAGE_TYPE_BIG = 1;

	public static final int IMAGE_TYPE_THUMBNAIL = 2;

	// futao------------
	private static final int SMALL_PICTURE_DEFAULT_DRAWABLE = R.drawable.ic_launcher;

	private static final int THEME_PICTURE_DEFAULT_DRAWABLE = R.drawable.ic_launcher;

	private static final int FOLLOW_PROFILE_PHOTO_DRAWABLE = R.drawable.ic_launcher;

	private static final int FOLLOW_PUBLISH_PHOTO_DRAWABLE = R.drawable.default_head;

	// futao-----------
	private static DisplayImageOptions defaultImageOption = new DisplayImageOptions.Builder()
			.cacheOnDisc().bitmapConfig(Config.RGB_565)
			.showStubImage(SMALL_PICTURE_DEFAULT_DRAWABLE)
			.showImageOnFail(SMALL_PICTURE_DEFAULT_DRAWABLE)
			.showImageForEmptyUri(SMALL_PICTURE_DEFAULT_DRAWABLE)
			.imageScaleType(ImageScaleType.EXACTLY).build();

	private static DisplayImageOptions smallImageOption;

	private static DisplayImageOptions themeImageOption;

	private static DisplayImageOptions mProfileImageOption;

	private static DisplayImageOptions mPublishImageOptions;

	private static DisplayImageOptions mBigImageOptions;
	private static DisplayImageOptions mHeadImageOptions;
	// add futao

	private static DisplayImageOptions mReceiveImageOption;

	public static DisplayImageOptions getHeadImageOptions() {
		if (mHeadImageOptions == null)
			mHeadImageOptions = new DisplayImageOptions.Builder()
					.showImageForEmptyUri(R.drawable.default_head)
					.showStubImage(R.drawable.default_head).build();
		return mHeadImageOptions;
	}

	public static DisplayImageOptions getDefaultImageOptions() {
		return defaultImageOption;
	}

	public static DisplayImageOptions getSmallImageOptions() {
		if (smallImageOption == null)
			smallImageOption = new DisplayImageOptions.Builder()
					.cloneFrom(defaultImageOption)
					.showImageForEmptyUri(SMALL_PICTURE_DEFAULT_DRAWABLE)
					.showStubImage(SMALL_PICTURE_DEFAULT_DRAWABLE).build();
		return smallImageOption;
	}

	public static DisplayImageOptions getThemeImageOptions() {
		if (themeImageOption == null)
			themeImageOption = new DisplayImageOptions.Builder().cacheOnDisc()
					.imageScaleType(ImageScaleType.EXACTLY)
					.bitmapConfig(Config.RGB_565)
					.showImageForEmptyUri(THEME_PICTURE_DEFAULT_DRAWABLE)
					.showStubImage(THEME_PICTURE_DEFAULT_DRAWABLE)
					.showImageOnFail(THEME_PICTURE_DEFAULT_DRAWABLE)
					.showImageForEmptyUri(THEME_PICTURE_DEFAULT_DRAWABLE)
					.build();
		return themeImageOption;
	}

	public static DisplayImageOptions getProfileImageOptions() {
		if (mProfileImageOption == null)
			mProfileImageOption = new DisplayImageOptions.Builder()
					.cloneFrom(defaultImageOption)
					.showImageForEmptyUri(FOLLOW_PROFILE_PHOTO_DRAWABLE)
					.showStubImage(FOLLOW_PROFILE_PHOTO_DRAWABLE).build();
		return mProfileImageOption;
	}

	public static DisplayImageOptions getPublishImageOptions() {
		if (mPublishImageOptions == null)
			mPublishImageOptions = new DisplayImageOptions.Builder()
					.cloneFrom(defaultImageOption)
					.showImageForEmptyUri(FOLLOW_PUBLISH_PHOTO_DRAWABLE)
					.showStubImage(FOLLOW_PUBLISH_PHOTO_DRAWABLE).cacheOnDisc()
					.imageScaleType(ImageScaleType.NONE).build();
		return mPublishImageOptions;
	}

	public static DisplayImageOptions getBigPictureImageOptions() {
		if (mBigImageOptions == null)
			mBigImageOptions = new DisplayImageOptions.Builder().cacheOnDisc()
					.bitmapConfig(Config.RGB_565)
					.imageScaleType(ImageScaleType.EXACTLY)
					.showImageForEmptyUri(SMALL_PICTURE_DEFAULT_DRAWABLE)
					.showImageOnFail(SMALL_PICTURE_DEFAULT_DRAWABLE)
					.showStubImage(SMALL_PICTURE_DEFAULT_DRAWABLE).build();
		return mBigImageOptions;
	}

	public static DisplayImageOptions getReceiveImageOption() {
		if (mReceiveImageOption == null)
			mReceiveImageOption = new DisplayImageOptions.Builder()
					.cloneFrom(defaultImageOption)
					.showImageForEmptyUri(FOLLOW_PUBLISH_PHOTO_DRAWABLE)
					.showStubImage(FOLLOW_PUBLISH_PHOTO_DRAWABLE)
					.cacheInMemory().cacheOnDisc()
					.imageScaleType(ImageScaleType.NONE).build();
		return mReceiveImageOption;
	}

	public static DisplayImageOptions getListHeadOption() {
		if (mReceiveImageOption == null)
			mReceiveImageOption = new DisplayImageOptions.Builder()
					.cloneFrom(defaultImageOption)
					.showImageForEmptyUri(FOLLOW_PUBLISH_PHOTO_DRAWABLE)
					.showStubImage(FOLLOW_PUBLISH_PHOTO_DRAWABLE)
					.cacheInMemory().cacheOnDisc()
					.imageScaleType(ImageScaleType.NONE).build();
		return mReceiveImageOption;
	}

	public static String buildImageUrl(String url, int type) {
		if (TextUtils.isEmpty(url))
			return null;
		int index = url.lastIndexOf(".");
		if (index == -1)
			return url;
		String head = url.substring(0, index);
		String end = url.substring(index, url.length());
		StringBuilder sb = new StringBuilder();
		int needSize = 0;
		if (type == IMAGE_TYPE_BIG || type == IMAGE_TYPE_THUMBNAIL) {
			needSize = getAppImageSize(type);
		} else {
			needSize = type;
		}
		int imageSize = getImageSize(needSize);
		if (imageSize == 0)
			return url;
		sb.append(head).append("_").append(imageSize).append(end);
		return sb.toString();
	}

	private static int getAppImageSize(int type) {
		int size = 0;
		switch (type) {
		case IMAGE_TYPE_BIG:
			// futao size = AppInfo.ImageScaleInfo.bigImageWidthPx;
			break;

		case IMAGE_TYPE_THUMBNAIL:
			// futao size = AppInfo.ImageScaleInfo.thumbnailImageWidthPx;
			break;
		}
		return size;
	}

	private static int getImageSize(int imageWidthPx) {
		// TODO Auto-generated method stub
		int size = 0;
		if (imageWidthPx <= 100)
			size = 100;
		else if (imageWidthPx <= 320)
			size = 320;
		else if (imageWidthPx <= 480)
			size = 480;
		else if (imageWidthPx <= 540)
			size = 540;
		else if (imageWidthPx <= 720)
			size = 720;
		else
			size = 720;
		return size;
	}
}
