package com.rcplatform.phototalk.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;

public class SoundManager {

	private static SoundManager instance = null;

	private SoundPool soundPool = null;

	private HashMap<String, Integer> soundMap;

	private final int MAX_SOUND_STREAM = 100;

	private final int QUALITY_DEFAULT = 0;

	private final long MAX_SOUND_LENGTH = 10000l;

	private final int PRIORITY_DEFAULT = 0;

	private SoundManager() {
		soundMap = new HashMap<String, Integer>();
		soundPool = new SoundPool(MAX_SOUND_STREAM, AudioManager.STREAM_MUSIC, QUALITY_DEFAULT);
	}

	static public SoundManager getInstance() {
		if (null == instance) {
			instance = new SoundManager();
		}
		return instance;
	}

	private Integer loadStream(String filePath, long offset) {
		Integer stream = -1;
		stream = soundMap.get(filePath);
		if (null == stream) {
			File file = new File(filePath);
			FileInputStream fis;
			try {
				fis = new FileInputStream(file);
				stream = soundPool.load(fis.getFD(), offset, MAX_SOUND_LENGTH, PRIORITY_DEFAULT);
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			soundMap.put(filePath, stream);
		}

		return stream;
	}

	public void play(String filePath, long offset) {
		final int stream = loadStream(filePath, offset).intValue();
		
		
			
		soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
				// TODO Auto-generated method stub
				if(sampleId == stream){
					soundPool.play(stream, 10, 10, 1, 0, 1.0f);
				}
			}
		});
	}

	public void stop(String filePath) {
		Integer streamID = soundMap.get(filePath);
		if (null != streamID) {
			soundPool.stop(streamID.intValue());
		}
	}

	public void allStop() {
		Iterator iter = soundMap.entrySet().iterator();
		while (iter.hasNext()) {
			HashMap.Entry entry = (HashMap.Entry) iter.next();
			Integer val = (Integer) entry.getValue();
			soundPool.stop(val.intValue());
		}
	}

	public void release(String filePath) {
		Integer streamID = soundMap.get(filePath);
		if (null != streamID) {
			soundPool.stop(streamID.intValue());
			soundPool.unload(streamID.intValue());
			soundMap.remove(filePath);
		}
	}

}
