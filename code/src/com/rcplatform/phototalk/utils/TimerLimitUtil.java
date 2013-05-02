package com.rcplatform.phototalk.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.InformationState;

public class TimerLimitUtil {

    private static List<Information> taskList = null;

    private static TimerLimitUtil limitUtil;

    private static ExecutorService service;

    private static Timer timer;

    private TimerLimitUtil() {
        if (taskList == null)
            taskList = new ArrayList<Information>();
        timer = new Timer();
    }

    public static synchronized TimerLimitUtil getInstence() {
        if (limitUtil == null)
            limitUtil = new TimerLimitUtil();
        return limitUtil;

    }

    public void addTask(final Information record) {
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                record.setStatu(InformationState.STATU_NOTICE_SHOWING);
                record.setLimitTime(record.getLimitTime() - 1);
                if (record.getLimitTime() <= 0) {
                    record.setDestroyed(true);
                    record.setLastUpdateTime(System.currentTimeMillis());
                    record.setStatu(InformationState.STATU_NOTICE_OPENED);
                    this.cancel();
                }
            }
        }, 0, 1000);
    }

}
