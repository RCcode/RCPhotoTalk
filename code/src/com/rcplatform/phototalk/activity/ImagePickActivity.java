package com.rcplatform.phototalk.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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
	protected static final int REQUEST_CODE_CUT_IMAGE = 1014;

	private Uri mImageUri;
	private PopupWindow mImageSelectPopupWindow;

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
			if (REQUEST_CODE_CAMERA == requestCode) {
				Uri tmpUri = mImageUri;
				if (data != null && data.getData() != null) {
					tmpUri = data.getData();
				}
				String realPath = Utils.getRealPath(this, tmpUri);
				if (realPath != null) {
					onImageReceive(tmpUri, realPath);
				} else {
					onImagePickFail();
				}

			} else if (REQUEST_CODE_GALLARY == requestCode) {
				try {
					Uri tmpUri = null;
					if (data != null && data.getData() != null) {
						tmpUri = data.getData();
					}
					String realPath = Utils.getRealPath(this, tmpUri);
					if (realPath != null) {
						onImageReceive(tmpUri, realPath);
					} else {
						onImagePickFail();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (REQUEST_CODE_CUT_IMAGE == requestCode) {
				if (data != null) {
					String imagePath = data.getData().getPath();
					onImageCutSuccess(imagePath);
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

	protected void showImagePickMenu(View view) {
		if (mImageSelectPopupWindow == null) {
			View detailsView = LayoutInflater.from(this).inflate(R.layout.picker_head_source_layout, null, false);

			mImageSelectPopupWindow = new PopupWindow(detailsView, getWindow().getWindowManager().getDefaultDisplay().getWidth(), ((Activity) this).getWindow().getWindowManager().getDefaultDisplay().getHeight());

			mImageSelectPopupWindow.setFocusable(true);
			mImageSelectPopupWindow.setOutsideTouchable(true);
			mImageSelectPopupWindow.setBackgroundDrawable(new BitmapDrawable());
			ImageButton cameraBtn = (ImageButton) detailsView.findViewById(R.id.picker_head_source_camera);
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
			ImageButton gallaryBtn = (ImageButton) detailsView.findViewById(R.id.picker_head_source_gallary);
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

	protected void cutImage(Uri uri) {
		Intent intent = new Intent(this, ImageCutActivity.class);
		intent.setData(uri);
		startActivityForResult(intent, REQUEST_CODE_CUT_IMAGE);
	}
}
