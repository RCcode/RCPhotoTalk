package com.rcplatform.phototalk.image.downloader;

import java.io.File;

import android.content.Context;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.galhttprequest.LogUtil;
import com.rcplatform.phototalk.listener.HomeRecordLoadPicListener;
import com.rcplatform.phototalk.utils.FileDownloader;
import com.rcplatform.phototalk.utils.PhotoTalkUtils;

public class RCPlatformImageLoader {

	public static final int IMAGE_SIZE_BIG = ImageOptionsFactory.IMAGE_TYPE_BIG;

	public static final int IMAGE_SIZE_THUMBNAIL = ImageOptionsFactory.IMAGE_TYPE_THUMBNAIL;

	public static void loadImage(Context context, ImageLoader imageLoader, String url, int imageSize, final ImageView iv, final int defaultDrawableId) {
		if (context != null) {
			final String resultUrl = ImageOptionsFactory.buildImageUrl(url, imageSize);
			LogUtil.i(resultUrl + "");
			imageLoader.displayImage(resultUrl, iv, ImageOptionsFactory.getDefaultImageOptions());

		}
	}

	public static void loadImage(Context context, ImageLoader imageLoader, DisplayImageOptions options, String url, int imageSize, final ImageView iv, final int defaultDrawableId) {
		if (context != null) {
			final String resultUrl = url;
			imageLoader.displayImage(resultUrl, iv, options);
		}
	}

	public static void displayImage(Context context, ImageView imageView, String url, ImageLoader imageLoader) {
		imageLoader.displayImage(url, imageView);
	}

	public static synchronized void LoadPictureForList(final Context context,ListView listView, ImageLoader imageLoader, DisplayImageOptions options, final Information record) {
		String url = record.getUrl();
		FileDownloader.getInstance().loadFile(url, PhotoTalkUtils.getFilePath(url), new HomeRecordLoadPicListener(listView, context, record));
	}

	public static boolean isFileExist(Context context, String url) {
		File file = new File(PhotoTalkUtils.getFilePath(url));
		return file.exists();
	}
}
