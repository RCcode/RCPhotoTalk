package com.rcplatform.phototalk.image.downloader;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.drift.DriftInformation;
import com.rcplatform.phototalk.galhttprequest.LogUtil;
import com.rcplatform.phototalk.listener.DriftInformationPicListener;
import com.rcplatform.phototalk.listener.HomeRecordLoadPicListener;
import com.rcplatform.phototalk.utils.FileDownloader;
import com.rcplatform.phototalk.utils.PhotoTalkUtils;
import com.rcplatform.phototalk.utils.RCPlatformTextUtil;

public class RCPlatformImageLoader {

	public static synchronized void LoadPictureForList(final Context context, final Information record) {
		String url = record.getUrl();
		FileDownloader.getInstance().loadFile(url, PhotoTalkUtils.getFilePath(url), new HomeRecordLoadPicListener(context, record));
	}

	public static synchronized void loadPictureForDriftList(final Context context, final DriftInformation record) {
		String url = record.getUrl();
		FileDownloader.getInstance().loadFile(url, PhotoTalkUtils.getFilePath(url), new DriftInformationPicListener(context, record));
	}

	public static boolean isFileExist(Context context, String url) {
		File file = new File(PhotoTalkUtils.getFilePath(url));
		return file.exists();
	}

	public static void loadImage(Context context, ImageLoader loader, String url, ImageView iv) {
		LogUtil.e(url + "");
		loader.loadImage(url, new RCPlatformImageLoadingListener(iv, url, R.drawable.default_head));
	}

	private static class RCPlatformImageLoadingListener implements ImageLoadingListener {

		private ImageView mImageView;
		private int mDefaultResId;
		private boolean isListenerOver = false;

		public RCPlatformImageLoadingListener(ImageView imageView, String imageUrl, int defaultResId) {
			this.mImageView = imageView;
			mImageView.setTag(imageUrl);
			this.mDefaultResId = defaultResId;
		}

		private void setDefaultImage(String imageUri) {
			if (imageUri.equals(getCacheUri()))
				mImageView.setImageResource(mDefaultResId);
		}

		@Override
		public void onLoadingStarted(String imageUri, View view) {
			setDefaultImage(imageUri);
		}

		@Override
		public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
			setDefaultImage(imageUri);
		}

		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
			if (!isListenerOver) {
				String cacheUri = getCacheUri();
				if (imageUri.equals(cacheUri)) {
					isListenerOver = true;
					if (RCPlatformTextUtil.isEmpty(cacheUri) || loadedImage == null) {
						setDefaultImage(imageUri);
					} else {
						mImageView.setImageBitmap(loadedImage);
					}
				}
			}
		}

		@Override
		public void onLoadingCancelled(String imageUri, View view) {
			setDefaultImage(imageUri);
		}

		private String getCacheUri() {
			return (String) mImageView.getTag();
		}
	}
}
