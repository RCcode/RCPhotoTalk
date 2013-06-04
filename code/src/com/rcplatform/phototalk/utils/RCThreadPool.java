package com.rcplatform.phototalk.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class RCThreadPool {
	private static final RCThreadPool instance = new RCThreadPool();
	private ThreadPoolExecutor mPool;

	private RCThreadPool() {
		mPool = (ThreadPoolExecutor) Executors.newScheduledThreadPool(3);
	}

	public static synchronized RCThreadPool getInstance() {
		return instance;
	}

	public synchronized void addTask(Runnable task) {
		mPool.execute(task);
	}
}
