package com.rcplatform.phototalk;

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

import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.utils.Contract;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.Utils;
import com.rcplatform.phototalk.views.HighLightView;

public class ImageCutActivity extends BaseActivity {

	public static final String REQUEST_PARAM_IMAGE = "file";

	private Button btnNext;
	private HighLightView mHighLight;
	private int rotateAngel;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cut_image);
		btnNext = (Button) findViewById(R.id.btn_next);
		mHighLight = (HighLightView) findViewById(R.id.hlv);
		btnNext.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new SaveImageTask().execute();
			}
		});

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
			// TODO Auto-generated method stub
			super.onPreExecute();
			showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		}

		@Override
		protected File doInBackground(Void... params) {
			// TODO Auto-generated method stub
			try {
				File result = shot();
				if (!isCancelled())
					return result;
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(File result) {
			// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		super.onResume();
		new LoadImageTask().execute();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mHighLight.clearBitmap();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		mHighLight.recyle();
		System.gc();
		super.onDestroy();
	}

	class LoadImageTask extends AsyncTask<Void, Void, Bitmap> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG,false);
		}

		@Override
		protected Bitmap doInBackground(Void... params) {
			// TODO Auto-generated method stub
			Uri imageUri = getIntent().getData();
			Bitmap bitmap = null;
			try {
				rotateAngel = Utils.getUriImageAngel(ImageCutActivity.this, imageUri);
				int nWidth = 0, nHeight = 0;
				nHeight = Contract.HEAD_IMAGE_WIDTH;
				nWidth = Contract.HEAD_IMAGE_WIDTH;
				bitmap = Utils.decodeSampledBitmapFromFile(Utils.getRealPath(ImageCutActivity.this, imageUri), nWidth, nHeight, rotateAngel);
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			}
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			// TODO Auto-generated method stub
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
}
