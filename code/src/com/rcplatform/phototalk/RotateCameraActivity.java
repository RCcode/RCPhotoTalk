package com.rcplatform.phototalk;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.umeng.EventUtil;
import com.rcplatform.phototalk.views.CameraView;
import com.rcplatform.phototalk.views.Rotate3dAnimation;
import com.rcplatform.phototalk.views.CameraView.TakeOnSuccess;

public class RotateCameraActivity extends BaseActivity {
	private CameraView rCameraView;
	private Button close_btn, change_camera_btn, take_btn, flashlight_btn;
	private RelativeLayout view_layout;
	private float mFromDegrees = 0;
	private float mToDegrees = 180;
	private PhotoTalkApplication app;
	private int w, h;
	private Bitmap bitmap;
	private Intent intent;
	private TextView rcId_text, url_text;
	private String url;
	private ImageView top_layout;
	private ImageView back_view;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.rotate_camera);

		app = (PhotoTalkApplication) getApplication();
		top_layout = (ImageView) findViewById(R.id.top_layout);
		back_view = (ImageView) findViewById(R.id.back_view);
		w = app.getScreenWidth();
		h = app.getScreentHeight();
		if (w > h) {
			w = h;
		} else {
			h = w;
		}
		rcId_text = (TextView) findViewById(R.id.rc_id);
		rcId_text.setText("RC ID:" + getCurrentUser().getRcId());
		url_text = (TextView) findViewById(R.id.url_text);
		rcId_text.setShadowLayer(3F, 3F, 1F, Color.BLACK);
		url_text.setShadowLayer(3F, 3F, 1F, Color.BLACK);
		rCameraView = (CameraView) findViewById(R.id.rotate_camera);
		rCameraView.setTakeOnSuccess(takeOnSuccess);
		view_layout = (RelativeLayout) findViewById(R.id.view_layout);
		LinearLayout lin = new LinearLayout(this);
		view_layout.addView(lin, w, h);

		close_btn = (Button) findViewById(R.id.close_btn);
		close_btn.setOnClickListener(clickListener);
		flashlight_btn = (Button) findViewById(R.id.flashlight_btn);
		flashlight_btn.setOnClickListener(clickListener);
		change_camera_btn = (Button) findViewById(R.id.change_camera_btn);
		change_camera_btn.setOnClickListener(clickListener);
		if (Camera.getNumberOfCameras() == 1) {
			change_camera_btn.setVisibility(View.GONE);
		}
		take_btn = (Button) findViewById(R.id.take_btn);
		take_btn.setOnClickListener(clickListener);
	}

	private TakeOnSuccess takeOnSuccess = new TakeOnSuccess() {

		@Override
		public void successMethod() {
			// TODO Auto-generated method stub
			Bitmap bitmap1 = app.getEditeBitmap();
			rCameraView.setVisibility(View.GONE);
			back_view.setImageBitmap(bitmap1);
			saveBitmap();
		}
	};
	
	public void saveBitmap(){
		View view = getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		Bitmap b1 = view.getDrawingCache();
		int start = (b1.getHeight()-b1.getWidth())/2;
		bitmap = Bitmap
				.createBitmap(b1, 0, start, w, h);
		url = app.getCameraFileCachePath()+"/"+System.currentTimeMillis()+".jpg";
		saveEditedPictrue(bitmap,url);
		b1.recycle();
		Intent intent = new Intent();
		intent.putExtra("Image_url", url);
		setResult(Activity.RESULT_OK, intent);
		this.finish();
	}
	private final OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.take_btn:
				rCameraView.takePhoto();
				break;
			case R.id.change_camera_btn:
				rCameraView.changeCamera();
				if (mFromDegrees == 0) {
					mFromDegrees = 180;
					mToDegrees = 0;
					flashlight_btn.setVisibility(View.GONE);
				} else {
					mFromDegrees = 0;
					mToDegrees = 180;
					flashlight_btn.setVisibility(View.VISIBLE);
				}
				Animation animation = new Rotate3dAnimation(mFromDegrees,
						mToDegrees, change_camera_btn.getWidth() / 2,
						change_camera_btn.getHeight() / 2, 200.0f, true);
				animation.setDuration(500);
				animation.setInterpolator(new AccelerateInterpolator());
				change_camera_btn.startAnimation(animation);
				break;
			case R.id.flashlight_btn:
				if (rCameraView.setLightStatu()) {
					flashlight_btn
							.setBackgroundResource(R.drawable.flashlight_press);
				} else
					flashlight_btn
							.setBackgroundResource(R.drawable.flashlight_normal);
				break;
			case R.id.close_btn:
				finish();
				break;
			}
		}
	};

	public void saveEditedPictrue(final Bitmap bitmap, final String path) {
		// showDialog();
		new Thread(new Runnable() {

			@Override
			public void run() {
				File file = new File(path);
				try {
					if (file.exists()) {
						file.delete();
					}
					if (!file.exists())
						file.createNewFile();
					BufferedOutputStream os = new BufferedOutputStream(
							new FileOutputStream(file)); //
					// b.compress(Bitmap.CompressFormat.JPEG, 100, os);
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
					os.flush();
					os.close();
					bitmap.recycle();
					// startActivityForResult(intent, requestCode)
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
}
