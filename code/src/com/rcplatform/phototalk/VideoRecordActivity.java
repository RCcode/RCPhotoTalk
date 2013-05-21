package com.rcplatform.phototalk;

import android.content.Intent;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rcplatform.phototalk.views.PlayVidoeView;
import com.rcplatform.phototalk.views.VideoRecordView;

public class VideoRecordActivity extends BaseMediaActivity {

    private final Handler mHandler = new Handler();

    private final static int START_STOP_ON_CLICK = 0;

    private final static int SURE_ON_CLICK = 1;

    private final static int PREVIEW_ON_CLICK = 2;

    private final static int CHANGE_CAMERA_ON_CLICK = 3;

    private static String mFilePath;

    private VideoRecordView mVideoRecordView;

    private PlayVidoeView mVideoPreviewView;

    private RelativeLayout mVideoPreviewVideoGroup;

    private RelativeLayout mVideoRecordViewGroup;

    private Button mButtonStart;

    private Button mButtonPreview;

    private TextView mTextViewTimerM;

    private TextView mTextViewTimerS;

    private Button mButtonSure;

    private Button mButtonChangeCamera;

    private LinearLayout mLinerLyoutTimer;

    private PhotoTalkApplication app;

    private String seconds;

    private int count = 10;

    boolean isStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.video_record_view);
        app = (PhotoTalkApplication) getApplication();
        mVideoRecordView = (VideoRecordView) findViewById(R.id.sf_video_record_record);
        mButtonStart = (Button) findViewById(R.id.btn_video_record_start_stop);
        mTextViewTimerM = (TextView) findViewById(R.id.tv_video_record_timer_minute);
        mTextViewTimerS = (TextView) findViewById(R.id.tv_video_record_timer_second);
        mButtonSure = (Button) findViewById(R.id.btn_video_record_sure);
        mVideoPreviewVideoGroup = (RelativeLayout) findViewById(R.id.rl_video_record_preview_group);
        mButtonPreview = (Button) findViewById(R.id.btn_video_record_preview);
        mVideoRecordViewGroup = (RelativeLayout) findViewById(R.id.rl_video_record_record_group);
        mButtonChangeCamera = (Button) findViewById(R.id.btn_video_record_change_camera);
        mLinerLyoutTimer = (LinearLayout) findViewById(R.id.ll_video_record_timer);

        if (Camera.getNumberOfCameras() == 1) {
            mButtonChangeCamera.setVisibility(View.GONE);
        }
        mButtonPreview.setOnClickListener(clickListener);
        mButtonPreview.setTag(PREVIEW_ON_CLICK);
        // mButtonStart.setOnClickListener(clickListener);
        // mButtonStart.setTag(START_STOP_ON_CLICK);
        mButtonSure.setOnClickListener(clickListener);
        mButtonSure.setTag(SURE_ON_CLICK);
        mButtonChangeCamera.setOnClickListener(clickListener);
        mButtonChangeCamera.setTag(CHANGE_CAMERA_ON_CLICK);

        mFilePath = app.getCacheFilePath() + "/" + System.currentTimeMillis() + ".mp4";

        mVideoPreviewView = (PlayVidoeView) findViewById(R.id.sf_video_record_preview);

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (isOnTouchRecodButton(x, y)) {
                    startRecord();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isOnTouchRecodButton(x, y)) {
                    if (isStarted) {
                        stopRecord();
                    }

                }
                break;
            case MotionEvent.ACTION_UP:
                if (isStarted) {
                    stopRecord();
                }
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    private boolean isOnTouchRecodButton(int x, int y) {
        Rect rect = new Rect();
        mButtonStart.getHitRect(rect);
        if (rect.contains(x, y)) {
            return true;
        } else
            return false;
    }

    private final OnClickListener clickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            int tag = (Integer) v.getTag();
            switch (tag) {
                case START_STOP_ON_CLICK:
                    if (!isStarted) {
                        startRecord();
                    } else {
                        stopRecord();
                    }
                    break;
                case SURE_ON_CLICK:
                    Intent intent = new Intent(VideoRecordActivity.this, EditVideoActivity.class);
                    intent.putExtra("mFilePath", mFilePath);
                    startActivity(intent);

                    break;
                case PREVIEW_ON_CLICK:
                    mVideoPreviewView.playVideo();
                    break;
                case CHANGE_CAMERA_ON_CLICK:
                    mVideoRecordView.changeCamera();
                    break;
            }
        }

    };

    private void stopRecord() {
        mButtonChangeCamera.setVisibility(View.VISIBLE);
        mLinerLyoutTimer.setVisibility(View.GONE);
        mVideoRecordView.stopRecord(true);
        // mVideoRecordView.setVisibility(View.GONE);
        // mVideoRecordViewGroup.setVisibility(View.GONE);
        isStarted = false;
        mHandler.removeCallbacks(refeshTimer);
        // mPlayVidoeView.initMediaPlayer(mFilePath);
        // mPreviewVideoGroup.setVisibility(View.VISIBLE);
        isStarted = false;
        count = 10;
        seconds = "";
        mTextViewTimerS.setText(seconds + count);
        Intent intent = new Intent(VideoRecordActivity.this, EditVideoActivity.class);
        intent.putExtra("mFilePath", mFilePath);
        startActivity(intent);

    }

    private void startRecord() {
        isStarted = true;
        mLinerLyoutTimer.setVisibility(View.VISIBLE);
        mButtonChangeCamera.setVisibility(View.GONE);
        isStarted = true;
        mVideoRecordView.startRecord(mFilePath);
        mHandler.postDelayed(refeshTimer, 1000);
    }

    Runnable refeshTimer = new Runnable() {

        @Override
        public void run() {
            count--;
            if (count >= 0) {
                seconds = "0" + count;
                mTextViewTimerS.setText(seconds);
                mHandler.postDelayed(this, 1000);
            } else if (count < 0) {
                mVideoRecordView.stopRecord(true);
                mHandler.removeCallbacks(this);
            }
        }

    };
}
