package com.rcplatform.phototalk;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.MediaController;

import com.rcplatform.phototalk.views.EditableViewGroup;
import com.rcplatform.phototalk.views.PlayVidoeView;

public class EditVideoActivity extends Activity {

    private Button mButtonTuya;

    private Button mButtonText;

    private Button mButtonClose;

    private Button mButtonMute;

    private Button mButtonSave;

    private Button mButtonSend;

    private String mFilePath;

    private LinearLayout editText;

    MediaController mMediaController;

    PlayVidoeView playVidoeView;

    EditableViewGroup mViewGroup;

    private PhotoTalkApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mViewGroup = (EditableViewGroup) LayoutInflater.from(this).inflate(R.layout.edit_video_view, null);

        setContentView(mViewGroup);
        playVidoeView = (PlayVidoeView) findViewById(R.id.sf_edit_video);
        app = (PhotoTalkApplication) getApplication();
        mButtonClose = (Button) findViewById(R.id.btn_edit_video_close);
        mButtonMute = (Button) findViewById(R.id.btn_edit_video_mute);
        mButtonSave = (Button) findViewById(R.id.btn_edit_video_save);
        mButtonSend = (Button) findViewById(R.id.btn_edit_video_send);
        mButtonText = (Button) findViewById(R.id.btn_edit_video_text);
        mButtonTuya = (Button) findViewById(R.id.btn_edit_video_scrawl);

        mButtonClose.setOnClickListener(mClickListener);
        mButtonMute.setOnClickListener(mClickListener);
        mButtonSave.setOnClickListener(mClickListener);
        mButtonSend.setOnClickListener(mClickListener);
        mButtonText.setOnClickListener(mClickListener);
        mButtonTuya.setOnClickListener(mClickListener);

        playVidoeView.initMediaPlayer(getIntent().getStringExtra("mFilePath"));
        playVidoeView.setPlayMode();
    };

    private final OnClickListener mClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {

                case R.id.btn_edit_video_close:
                    break;
                case R.id.btn_edit_video_mute:
                    break;
                case R.id.btn_edit_video_save:
                    break;
                case R.id.btn_edit_video_send:
                    break;
                case R.id.btn_edit_video_text:
                    if (editText == null) {
                        editText = (LinearLayout) LayoutInflater.from(EditVideoActivity.this).inflate(R.layout.edittext_view, null);
                        mViewGroup.addEditeTextView(editText);
                    } else
                        break;
            }
        }
    };
}
