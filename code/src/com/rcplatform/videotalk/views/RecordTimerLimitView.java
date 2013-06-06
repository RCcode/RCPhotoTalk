package com.rcplatform.videotalk.views;

import java.util.Timer;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.TextView;

import com.rcplatform.videotalk.bean.Information;
import com.rcplatform.videotalk.galhttprequest.LogUtil;

public class RecordTimerLimitView extends TextView {

    private int mSeconds;

    private Information infoRecord;

    private OnTimeEndListener endListener;

    private Object statuTag;

    private Object buttonTag;

    public interface OnTimeEndListener {

        void onEnd(Object statuTag, Object buttonTag);
    }

    public RecordTimerLimitView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public RecordTimerLimitView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecordTimerLimitView(Context context) {
        super(context);
    }

    public RecordTimerLimitView(Context context, int seconds) {
        super(context);
        this.mSeconds = seconds;
    }

    public void initParm(int seconds) {
        mSeconds = seconds;

    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
    }

    public void scheuleTask(Information info) {
        handler.removeCallbacks(timerTask);
        this.infoRecord = info;
        mSeconds = info.getLimitTime();
        handler.post(timerTask);
    }

    public void setOnTimeEndListener(OnTimeEndListener listener, Object statuTag, Object buttonTag) {
        this.endListener = listener;
        this.statuTag = statuTag;
        this.buttonTag = buttonTag;
    }

    Runnable timerTask = new Runnable() {

        @Override
        public void run() {

            if (infoRecord.getLimitTime() <= 0) {
                handler.removeCallbacks(this);
                if (endListener != null) {
                    endListener.onEnd(statuTag, buttonTag);
                }
                return;
            } else {
            	LogUtil.i(infoRecord.getLimitTime() + "");
                setText(infoRecord.getLimitTime() + "");
            }

            handler.postDelayed(this, 1000);
        }
    };

    public void stopTask() {
        handler.removeCallbacks(timerTask);
    }

    Handler handler = new Handler() {

        @Override
        public void handleMessage(android.os.Message msg) {
        }
    };

    private Timer timer;

    public int getRemainingTime() {
        return mSeconds;
    }

    public void stopTimeTask() {
        handler.removeCallbacks(timerTask);
    }

}
