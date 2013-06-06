package com.rcplatform.videotalk.image.downloader;

import java.io.File;

import android.content.Context;
import android.widget.ListView;

import com.rcplatform.videotalk.bean.Information;
import com.rcplatform.videotalk.listener.HomeRecordLoadPicListener;
import com.rcplatform.videotalk.utils.FileDownloader;
import com.rcplatform.videotalk.utils.PhotoTalkUtils;

public class RCPlatformImageLoader {

	public static synchronized void LoadPictureForList(final Context context, ListView listView, final Information record) {
		String url = record.getUrl();
		FileDownloader.getInstance().loadFile(url, PhotoTalkUtils.getFilePath(url), new HomeRecordLoadPicListener(listView, context, record));
	}

	public static boolean isFileExist(Context context, String url) {
		File file = new File(PhotoTalkUtils.getFilePath(url));
		return file.exists();
	}
}
