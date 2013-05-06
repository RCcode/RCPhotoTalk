package com.rcplatform.phototalk.views;

import java.util.Timer;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.bean.Information;

public class GLTimer extends TextView {

    private int mSeconds;

    private Information infoRecord;

    private OnTimeEndListener endListener;

    public interface OnTimeEndListener {

        void onEnd();
    }

    public GLTimer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public GLTimer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GLTimer(Context context) {
        super(context);
    }

    public GLTimer(Context context, int seconds) {
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
        this.infoRecord = info;
        // setVisibility(View.VISIBLE);
        handler.removeCallbacks(timerTask);
        mSeconds = info.getLimitTime();
        setText("" + mSeconds);
        setVisibility(View.VISIBLE);
        info.setOpened(true);
        handler.postDelayed(timerTask, 1000);
    }

    public void setOnTimeEndListener(OnTimeEndListener listener) {
        this.endListener = listener;
    }

    public void reset(int second) {
        mSeconds = second;
        setText(mSeconds + "");
    }

    Runnable timerTask = new Runnable() {

        @Override
        public void run() {

            mSeconds--;
            infoRecord.setLimitTime(mSeconds);
            if (mSeconds < 0) {
                // setVisibility(View.INVISIBLE);
//                setBackgroundResource(R.drawable.receive_arrows_opened);
                handler.removeCallbacks(this);
                if (endListener != null)
                    endListener.onEnd();
                infoRecord.setDestroyed(true);
                return;
            } else {
                setText(mSeconds + "");
            }

            handler.postDelayed(this, 1000);
        }
    };

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

    @Override
    public void setVisibility(int visibility) {
        // TODO Auto-generated method stub
        super.setVisibility(visibility);
        Log.i("Futao", visibility + "----------");
    }
}
