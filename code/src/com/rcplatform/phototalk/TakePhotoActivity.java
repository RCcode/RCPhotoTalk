package com.rcplatform.phototalk;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.widget.Button;

import com.rcplatform.phototalk.views.CameraView;
import com.rcplatform.phototalk.views.Rotate3dAnimation;

/**
 * 标题、简要说明. <br>
 * 类详细说明.
 * <p>
 * Copyright: Menue,Inc Copyright (c) 2013-2-27 上午10:38:21
 * <p>
 * Team:Menue Beijing
 * <p>
 * 
 * @author tao.fu@menue.com.cn
 * @version 1.0.0
 */
public class TakePhotoActivity extends Activity {

    private static final int TAKE_ON_CLICK = 0;

    private static final int OPEN_FLASHLIGHT_ON_CLICK = 1;

    private static final int CHANGE_CAMERA_ON_CLICK = 2;

    private static final int CLOSE_ON_CLICK = 3;

    private Button mButtonTake;

    private Button mButtonClose;

    private CameraView mCameraView;

    private Button mButtonOpenFlashLight;

    private Button mButtonChangeCamera;

    private float mFromDegrees = 0;

    private float mToDegrees = 180;
    
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.take_photo_view);
        intent = getIntent();
        mButtonTake = (Button) findViewById(R.id.btn_take_photo_take);
        mButtonOpenFlashLight = (Button) findViewById(R.id.btn_take_photo_flashlight);
        mButtonChangeCamera = (Button) findViewById(R.id.btn_take_photo_change_camera);
        mButtonClose = (Button) findViewById(R.id.btn_close_take_photo);
        mCameraView = (CameraView) findViewById(R.id.sf_camera_view);
        if (Camera.getNumberOfCameras() == 1) {
            mButtonChangeCamera.setVisibility(View.GONE);
        }
        mButtonClose.setOnClickListener(clickListener);
        mButtonClose.setTag(CLOSE_ON_CLICK);
        mButtonChangeCamera.setOnClickListener(clickListener);
        mButtonChangeCamera.setTag(CHANGE_CAMERA_ON_CLICK);
        mButtonOpenFlashLight.setOnClickListener(clickListener);
        mButtonOpenFlashLight.setTag(OPEN_FLASHLIGHT_ON_CLICK);
        mButtonTake.setOnClickListener(clickListener);
        mButtonTake.setTag(TAKE_ON_CLICK);

    }

    private final OnClickListener clickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            int tag = (Integer) v.getTag();
            switch (tag) {
                case TAKE_ON_CLICK:
                    mCameraView.takePhoto();
                    break;
                case CHANGE_CAMERA_ON_CLICK:
                    mCameraView.changeCamera();
                    if (mFromDegrees == 0) {
                        mFromDegrees = 180;
                        mToDegrees = 0;
                    } else {
                        mFromDegrees = 0;
                        mToDegrees = 180;
                    }
                    Animation animation = new Rotate3dAnimation(mFromDegrees, mToDegrees, mButtonChangeCamera.getWidth() / 2,
                            mButtonChangeCamera.getHeight() / 2, 200.0f, true);
                    animation.setDuration(500);
                    animation.setInterpolator(new AccelerateInterpolator());
                    mButtonChangeCamera.startAnimation(animation);
                    break;
                case OPEN_FLASHLIGHT_ON_CLICK:
                    if (mCameraView.setLightStatu()) {
                        mButtonOpenFlashLight.setBackgroundResource(R.drawable.flashlight_press);
                    } else
                        mButtonOpenFlashLight.setBackgroundResource(R.drawable.flashlight_normal);
                    break;
                case CLOSE_ON_CLICK:
                    finish();
                    break;

            }

        }
    };

    public void startOtherActivity() {
    	intent.setClass(this, EditPictureActivity.class);
        startActivity(intent);
    }

}
