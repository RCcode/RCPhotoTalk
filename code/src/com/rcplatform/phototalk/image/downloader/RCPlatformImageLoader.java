package com.rcplatform.phototalk.image.downloader;

import android.content.Context;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.bean.InfoRecord;
import com.rcplatform.phototalk.galhttprequest.LogUtil;
import com.rcplatform.phototalk.listener.HomeRecordLoadPicListener;

public class RCPlatformImageLoader {

	public static final int IMAGE_SIZE_BIG = ImageOptionsFactory.IMAGE_TYPE_BIG;

	public static final int IMAGE_SIZE_THUMBNAIL = ImageOptionsFactory.IMAGE_TYPE_THUMBNAIL;

	private static final float IMAGE_QUALITY = 1 / 2;

	private static int count;

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
	public static void displayImage(Context context,ImageView imageView,String url,ImageLoader imageLoader){
		imageLoader.displayImage(url, imageView);
	}
	public static synchronized void LoadPictureForList(final Context context, final ProgressBar bar, final TextView statu, ListView listView, ImageLoader imageLoader, DisplayImageOptions options, final InfoRecord record) {

		imageLoader.loadImage(record.getUrl(), options, new HomeRecordLoadPicListener(listView, bar, statu, context, record));
	}

	public static String getStatuTime(String prefix, String postfix, long time) {
		long currentTime = System.currentTimeMillis();
		long durring = currentTime - time;
		StringBuffer sb = new StringBuffer();
		sb.append(prefix + " ");
		int s = (int) (durring / 1000);
		int m = 0;
		int h = 0;
		int d = 0;

		if (s > 60) {
			m = s / 60;
		}
		if (m > 60) {
			h = m / 60;
		}
		if (h > 24) {
			d = h / 24;
		}
		if (d > 0) {
			sb.append(d + "d");
		} else if (h > 0) {
			sb.append(h + "h");
		} else if (m > 0) {
			sb.append(m + "m");
		} else if (s > 0) {
			sb.append(s + "s");
		}
		sb.append(" ago " + postfix);
		return sb.toString();

	}
}
