package com.rcplatform.phototalk.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.rcplatform.phototalk.ImageCutActivity;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.Utils;

public class ImagePickActivity extends BaseActivity {

	protected static final int REQUEST_CODE_GALLARY = 1012;

	protected static final int REQUEST_CODE_CAMERA = 1013;

	protected static final int CROP_PICTURE = 1015;

	public static final int CROP_NONE = 0;

	public static final int CROP_BACKGROUND_IMAGE = 1;

	public static final int CROP_HEAD_IMAGE = 2;

	private Uri mImageUri;

	private PopupWindow mImageSelectPopupWindow;

	private int cropMode;

	static public final String WIDTH_KEY = "width";

	static public final String HEIGHT_KEY = "height";

	private final int HEAD_HEIGHT = 160;

	private final int HEAD_WIDTH = 160;

	private final int BACKGROUND_HEIGHT = 350;

	private final int BACKGROUND_WIDTH = 710;

	protected void startCamera() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		ContentValues values = new ContentValues();
		mImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
		startActivityForResult(intent, REQUEST_CODE_CAMERA);
	}

	protected void startGallary() {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(intent, REQUEST_CODE_GALLARY);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {

			if (REQUEST_CODE_CAMERA == requestCode || REQUEST_CODE_GALLARY == requestCode) {
				if (data != null && data.getData() != null) {
					mImageUri = data.getData();
				}
				if (mImageUri != null)
					onImageReceive(mImageUri, Utils.getRealPath(this, mImageUri));
				// switch (cropMode) {
				// case CROP_BACKGROUND_IMAGE:
				// cutImage(mImageUri, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
				// break;
				// case CROP_HEAD_IMAGE:
				// cutImage(mImageUri, HEAD_WIDTH, HEAD_HEIGHT);
				// break;
				// }
			} else if (CROP_PICTURE == requestCode) {
				if (data != null && data.getData() != null) {
					mImageUri = data.getData();
				}
				String realPath = Utils.getRealPath(this, mImageUri);
				if (realPath != null) {
					onImageReceive(mImageUri, realPath);
				} else {
					onImagePickFail();
				}
			}
		}
	}

	protected void onImageCutSuccess(String tmpPath) {
		// TODO Auto-generated method stub

	}

	protected void onImageReceive(Uri imageBaseUri, String imagePath) {

	}

	protected void onImagePickFail() {

	}

	protected void showImagePickMenu(View view, int mode) {
		cropMode = mode;
		if (mImageSelectPopupWindow == null) {
			View detailsView = LayoutInflater.from(this).inflate(R.layout.picker_head_source_layout, null, false);

			mImageSelectPopupWindow = new PopupWindow(detailsView, getWindow().getWindowManager().getDefaultDisplay().getWidth(), ((Activity) this).getWindow()
					.getWindowManager().getDefaultDisplay().getHeight());

			mImageSelectPopupWindow.setFocusable(true);
			mImageSelectPopupWindow.setOutsideTouchable(true);
			mImageSelectPopupWindow.setBackgroundDrawable(new BitmapDrawable());
			Button cameraBtn = (Button) detailsView.findViewById(R.id.picker_head_source_camera);
			cameraBtn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (!Utils.isExternalStorageUsable()) {
						DialogUtil.showToast(getApplicationContext(), R.string.no_sdc, Toast.LENGTH_SHORT);
						return;
					}
					mImageSelectPopupWindow.dismiss();
					startCamera();
				}
			});
			Button gallaryBtn = (Button) detailsView.findViewById(R.id.picker_head_source_gallary);
			gallaryBtn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (!Utils.isExternalStorageUsable()) {
						DialogUtil.showToast(getApplicationContext(), R.string.no_sdc, Toast.LENGTH_SHORT);
						return;
					}
					mImageSelectPopupWindow.dismiss();
					startGallary();
				}
			});
			Button cancelBtn = (Button) detailsView.findViewById(R.id.picker_head_cancel);
			cancelBtn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mImageSelectPopupWindow.isShowing()) {
						mImageSelectPopupWindow.dismiss();
					}
				}
			});
		}
		mImageSelectPopupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
	}

	protected void showImage(String path, Uri imageUri, ImageView imageView) {
		new LoadImageTask(imageView).execute(imageUri, Uri.parse(path));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public class LoadImageTask extends AsyncTask<Uri, Void, Bitmap> {

		private ImageView mImageView;

		public LoadImageTask(ImageView imageView) {
			this.mImageView = imageView;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		}

		@Override
		protected Bitmap doInBackground(Uri... params) {
			Uri imageUri = params[0];
			String headPath = params[1].getPath();
			Bitmap bitmap = null;
			try {
				int rotateAngel = Utils.getUriImageAngel(ImagePickActivity.this, imageUri);
				int nWidth = 0, nHeight = 0;
				nHeight = mImageView.getHeight();
				nWidth = mImageView.getWidth();
				bitmap = Utils.decodeSampledBitmapFromFile(headPath, nWidth, nHeight, rotateAngel);
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			} catch (Exception e) {
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
			} else {
				mImageView.setImageBitmap(result);
			}
		}
	}

	protected void cutImage(Uri uri, int width, int height) {
		Intent intent = new Intent(this, ImageCutActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt(WIDTH_KEY, width);
		bundle.putInt(HEIGHT_KEY, height);
		intent.putExtras(bundle);
		intent.setData(uri);
		startActivityForResult(intent, CROP_PICTURE);
	}
}
