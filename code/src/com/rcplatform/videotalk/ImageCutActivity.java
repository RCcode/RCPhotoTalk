package com.rcplatform.videotalk;

import java.io.File;
import java.io.FileOutputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.rcplatform.videotalk.R;
import com.rcplatform.videotalk.activity.BaseActivity;
import com.rcplatform.videotalk.activity.ImagePickActivity;
import com.rcplatform.videotalk.utils.Constants;
import com.rcplatform.videotalk.utils.DialogUtil;
import com.rcplatform.videotalk.utils.Utils;
import com.rcplatform.videotalk.views.HighLightView;

public class ImageCutActivity extends BaseActivity implements OnClickListener {

	public static final String REQUEST_PARAM_IMAGE = "file";

	private HighLightView mHighLight;

	private int rotateAngel;

	private int width;

	private int height;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cut_image);
		width = getIntent().getExtras().getInt(ImagePickActivity.WIDTH_KEY);
		height = getIntent().getExtras().getInt(ImagePickActivity.HEIGHT_KEY);
		Button btnNext = (Button) findViewById(R.id.btn_next);
		btnNext.setOnClickListener(this);
		Button btnCancel = (Button) findViewById(R.id.btn_cancel);
		btnCancel.setOnClickListener(this);
		mHighLight = (HighLightView) findViewById(R.id.hlv);
		mHighLight.setCropRect(width, height);
		new LoadImageTask().execute();
	}

	private File shot() throws Exception {
		if (!Utils.isExternalStorageUsable()) {
			return null;
		}
		Bitmap bmp = mHighLight.getBitmapHighLight();
		File result = Utils.createTmpPic();
		bmp.compress(CompressFormat.JPEG, 100, new FileOutputStream(result));
		bmp.recycle();
		return result;
	}

	class SaveImageTask extends AsyncTask<Void, Void, File> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		}

		@Override
		protected File doInBackground(Void... params) {
			try {
				File result = shot();
				if (!isCancelled())
					return result;
			} catch (Throwable e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(File result) {
			super.onPostExecute(result);
			dismissLoadingDialog();
			if (result != null) {
				Intent data = new Intent();
				data.setData(Uri.parse(result.getPath()));
				setResult(Activity.RESULT_OK, data);
			} else {
				DialogUtil.showToast(ImageCutActivity.this, R.string.operation_fail, Toast.LENGTH_SHORT);
			}
			finish();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		mHighLight.recyle();
		super.onDestroy();
	}

	class LoadImageTask extends AsyncTask<Void, Void, Bitmap> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, true);
		}

		@Override
		protected Bitmap doInBackground(Void... params) {
			Uri imageUri = getIntent().getData();
			Bitmap bitmap = null;
			try {
				rotateAngel = Utils.getUriImageAngel(ImageCutActivity.this, imageUri);
				int nWidth = Constants.SCREEN_WIDTH, nHeight = (int) (nWidth * ((float) height / width));
				bitmap = Utils.decodeSampledBitmapFromFile(Utils.getRealPath(ImageCutActivity.this, imageUri), nWidth, nHeight, rotateAngel);
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			}
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			dismissLoadingDialog();
			if (result == null) {
				DialogUtil.showToast(getApplicationContext(), R.string.image_unsupport, Toast.LENGTH_SHORT);
				finish();
			} else {
				mHighLight.setBitmap(result);
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_next:
			new SaveImageTask().execute();
			break;
		case R.id.btn_cancel:
			finish();
			break;
		}
	}
}
