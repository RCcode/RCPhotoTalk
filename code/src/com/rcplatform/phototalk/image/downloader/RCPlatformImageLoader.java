package com.rcplatform.phototalk.image.downloader;

import java.io.File;

import android.content.Context;
import android.widget.ListView;

import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.drift.DriftInformation;
import com.rcplatform.phototalk.listener.DriftInformationPicListener;
import com.rcplatform.phototalk.listener.HomeRecordLoadPicListener;
import com.rcplatform.phototalk.utils.FileDownloader;
import com.rcplatform.phototalk.utils.PhotoTalkUtils;

public class RCPlatformImageLoader {

	public static synchronized void LoadPictureForList(final Context context, ListView listView, final Information record) {
		String url = record.getUrl();
		FileDownloader.getInstance().loadFile(url, PhotoTalkUtils.getFilePath(url), new HomeRecordLoadPicListener(listView, context, record));
	}

	public static synchronized void loadPictureForDriftList(final Context context, final DriftInformation record) {
		String url = record.getUrl();
		FileDownloader.getInstance().loadFile(url, PhotoTalkUtils.getFilePath(url), new DriftInformationPicListener(context, record));
	}

	public static boolean isFileExist(Context context, String url) {
		File file = new File(PhotoTalkUtils.getFilePath(url));
		return file.exists();
	}
}
