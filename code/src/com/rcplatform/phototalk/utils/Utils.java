package com.rcplatform.phototalk.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.bean.AppInfo;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.FriendChat;
import com.rcplatform.phototalk.bean.UserInfo;

public class Utils {

	public static Map<AppInfo, UserInfo> getRCPlatformAppUsers(Context context) {
		PackageManager manager = context.getPackageManager();
		Map<AppInfo, UserInfo> appUsers = new HashMap<AppInfo, UserInfo>();
		List<PackageInfo> packages = manager
				.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
		for (PackageInfo info : packages) {
			if (info.packageName.startsWith("com.rcplatform")) {
				UserInfo userInfo = getAppLoginUser(context, info.packageName);
				if (userInfo != null) {
					AppInfo appInfo = new AppInfo();
					appInfo.setPackageName(info.packageName);
					appInfo.setAppName(info.applicationInfo.loadLabel(manager)
							.toString());
					appUsers.put(appInfo, userInfo);
				}
			}
		}
		return appUsers;
	}

	private static UserInfo getAppLoginUser(Context context, String packageName) {
		StringBuilder sbUri = new StringBuilder();
		sbUri.append("content://").append(packageName).append(".provider")
				.append("/user/0");
		Cursor cursor = context.getContentResolver().query(
				Uri.parse(sbUri.toString()), null, null, null, null);
		if (cursor != null)
			return cursorToUserInfo(cursor);
		return null;
	}

	private static UserInfo cursorToUserInfo(Cursor cursor) {
		UserInfo userInfo = null;
		if (cursor.moveToFirst()) {
			userInfo = new UserInfo();
			userInfo.setBirthday(cursor.getString(cursor
					.getColumnIndex(Contract.KEY_BIRTHDAY)));
			userInfo.setEmail(cursor.getString(cursor
					.getColumnIndex(Contract.KEY_EMAIL)));
			userInfo.setDeviceId(cursor.getString(cursor
					.getColumnIndex(Contract.KEY_DEVICE_ID)));
			userInfo.setHeadUrl(cursor.getString(cursor
					.getColumnIndex(Contract.KEY_HEADURL)));
			userInfo.setPassWord(cursor.getString(cursor
					.getColumnIndex(Contract.KEY_PASSWORD)));
			userInfo.setNick(cursor.getString(cursor
					.getColumnIndex(Contract.KEY_NICK)));
			userInfo.setPhone(cursor.getString(cursor
					.getColumnIndex(Contract.KEY_PHONE)));
			userInfo.setSex(cursor.getInt(cursor
					.getColumnIndex(Contract.KEY_SEX)));
			userInfo.setReceiveSet(cursor.getInt(cursor
					.getColumnIndex(Contract.KEY_RECEIVESET)));
			userInfo.setSignature(cursor.getString(cursor
					.getColumnIndex(Contract.KEY_SIGNATURE)));
			userInfo.setSuid(cursor.getString(cursor
					.getColumnIndex(Contract.KEY_SUID)));
			userInfo.setToken(cursor.getString(cursor
					.getColumnIndex(Contract.KEY_USER_TOKEN)));
		}
		cursor.close();
		return userInfo;
	}

	/**
	 * 判断是否存在额外存储空间
	 * 
	 * @return
	 */
	public static boolean isExternalStorageUsable() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

	public static boolean isNetworkEnable(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		if (info != null)
			return info.isAvailable();
		return false;
	}

	public static String getStatuTime(String prefix, String postfix, long time) {
		long currentTime = System.currentTimeMillis();
		long durring = currentTime - time;
		StringBuffer sb = new StringBuffer();
		sb.append(prefix + " ");
		int s = (int) (durring / 1000);
		int m = 0;
		int h = 0;
		int d = 0;

		if (s > 60) {
			m = s / 60;
		}
		if (m > 60) {
			h = m / 60;
		}
		if (h > 24) {
			d = h / 24;
		}
		if (d > 0) {
			sb.append(d + "d");
		} else if (h > 0) {
			sb.append(h + "h");
		} else if (m > 0) {
			sb.append(m + "m");
		} else if (s > 0) {
			sb.append(s + "s");
		}
		sb.append(" ago " + postfix);
		return sb.toString();

	}

	/**
	 * 创建临时图片
	 * 
	 * @return File
	 */
	public static File createTmpPic() {

		File tmpFile = new File(Environment.getExternalStorageDirectory(),
				"rcplatform/phototalk");

		if (!tmpFile.exists()) {
			if (!tmpFile.mkdirs()) {
				return null;
			}
		}
		File imageFile = new File(tmpFile.getPath() + "image_cut" + ".png");

		return imageFile;
	}

	/**
	 * 得到版本号
	 * 
	 * @param context
	 * @return
	 */
	public static int getVerCode(Context context) {
		int verCode = -1;
		try {
			verCode = context.getPackageManager().getPackageInfo(
					"com.menue.photosticker", 0).versionCode;
		} catch (NameNotFoundException e) {
			Log.e("msg", e.getMessage());
		}
		return verCode;
	}

	/**
	 * 得到版本号
	 * 
	 * @param context
	 * @return 字符串
	 */
	public static String getVerName(Context context) {
		String verName = "";
		try {
			verName = context.getPackageManager().getPackageInfo(
					"com.menue.photosticker", 0).versionName;
		} catch (NameNotFoundException e) {
			Log.e("msg", e.getMessage());
		}
		return verName;
	}

	public static int stringToInt(String str) {
		try {
			int i = Integer.parseInt(str);
			return i;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 将图片翻转180度
	 * 
	 * @param originalBitmap
	 * @return
	 */
	public static Bitmap rollingOver(Bitmap originalBitmap) {
		try {
			Bitmap bitmap = Bitmap.createBitmap(originalBitmap.getWidth(),
					originalBitmap.getHeight(), Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);
			Matrix matrix = new Matrix();

			Camera mCamera = new Camera();
			mCamera.save();
			mCamera.rotateY(180);
			mCamera.getMatrix(matrix);
			mCamera.restore();

			// 以图片的中心点为旋转中心,如果不加这两句，就是以（0,0）点为旋转中心
			matrix.preTranslate(-originalBitmap.getWidth() / 2,
					-originalBitmap.getHeight() / 2);
			matrix.postTranslate(originalBitmap.getWidth() / 2,
					originalBitmap.getHeight() / 2);

			canvas.drawBitmap(originalBitmap, matrix, null);
			if (originalBitmap != null && !originalBitmap.isRecycled()) {
				originalBitmap.recycle();
				originalBitmap = null;
			}
			return bitmap;
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Draw the view into a bitmap.
	 */
	public static Bitmap getViewBitmap(View v) {
		v.clearFocus();
		v.setPressed(false);

		boolean willNotCache = v.willNotCacheDrawing();
		v.setWillNotCacheDrawing(false);

		// Reset the drawing cache background color to fully transparent
		// for the duration of this operation
		int color = v.getDrawingCacheBackgroundColor();
		v.setDrawingCacheBackgroundColor(0);

		if (color != 0) {
			v.destroyDrawingCache();
		}
		v.buildDrawingCache();
		Bitmap cacheBitmap = v.getDrawingCache();
		if (cacheBitmap == null) {
			// Logger.getLogger().e(
			// "failed getViewBitmap(" + v + ")" + new RuntimeException());
			return null;
		}

		Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

		// Restore the view
		v.destroyDrawingCache();
		v.setWillNotCacheDrawing(willNotCache);
		v.setDrawingCacheBackgroundColor(color);

		return bitmap;
	}

	/**
	 * @param filename
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static Bitmap decodeSampledBitmapFromFile(String filename,
			int reqWidth, int reqHeight, int rotateAngel) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		// options.outHeight=200;
		// options.outWidth=200;
		BitmapFactory.decodeFile(filename, options);
		// Calculate inSampleSize
		int inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight,
				rotateAngel);
		options.inSampleSize = inSampleSize;

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;

		clearMemeryCache();
		System.gc();
		try {
			// Bitmap tempBitmap = BitmapFactory.decodeFile(filename, options);
			InputStream in = new FileInputStream(filename);
			Bitmap tempBitmap = BitmapFactory.decodeStream(in, null, options);
			in.close();
			if (rotateAngel != 0) {
				Matrix matrix = new Matrix();
				matrix.setRotate(rotateAngel, tempBitmap.getWidth() / 2,
						tempBitmap.getHeight() / 2);
				Bitmap result = Bitmap.createBitmap(tempBitmap, 0, 0,
						tempBitmap.getWidth(), tempBitmap.getHeight(), matrix,
						true);
				tempBitmap.recycle();
				tempBitmap = result;
			}
			return tempBitmap;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			clearMemeryCache();
			return null;
		}
	}

	private static void clearMemeryCache() {
		ImageLoader.getInstance().clearMemoryCache();
	}

	/**
	 * @param filename
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static Bitmap decodeSampledBitmapFromFile(InputStream is,
			int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		// Calculate inSampleSize
		// options.inSampleSize = calculateInSampleSize(options, reqWidth,
		// reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		try {
			return BitmapFactory.decodeStream(is, null, options);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight, int rotateAngel) {
		// Raw height and width of image
		final int height;
		final int width;
		if (rotateAngel == 0 || rotateAngel == 180) {
			height = options.outHeight;
			width = options.outWidth;
		} else {
			height = options.outWidth;
			width = options.outHeight;
		}
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = Math.round((float) height / (float) reqHeight);
			} else {
				inSampleSize = Math.round((float) width / (float) reqWidth);
			}

			// This offers some additional logic in case the image has a strange
			// aspect ratio. For example, a panorama may have a much larger
			// width than height. In these cases the total pixels might still
			// end up being too large to fit comfortably in memory, so we should
			// be more aggressive with sample down the image (=larger
			// inSampleSize).

			// final float totalPixels = width * height;

			// Anything more than 2x the requested pixels we'll sample down
			// further.
			// final float totalReqPixelsCap = reqWidth * reqHeight * 2;
			//
			// while (totalPixels / (inSampleSize * inSampleSize) >
			// totalReqPixelsCap) {
			// inSampleSize++;
			// }
		}
		if (inSampleSize % 2 > 0) {
			inSampleSize++;
		}
		return inSampleSize;
	}

	/**
	 * @param context
	 * @param uri
	 * @return Uri对应的文件真实路径
	 */
	public static String getRealPath(Context context, Uri uri) {
		String fileName = null;
		Uri filePathUri = uri;
		if (uri != null) {
			if (uri.getScheme().toString().compareTo("content") == 0) // content://开头的uri
			{
				String[] proj = { MediaStore.Images.Media.DATA };
				Cursor actualimagecursor = context.getContentResolver().query(
						uri, proj, null, null, null);
				int actual_image_column_index = actualimagecursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				actualimagecursor.moveToFirst();
				fileName = actualimagecursor
						.getString(actual_image_column_index);
			} else if (uri.getScheme().compareTo("file") == 0) // file:///开头的uri
			{
				fileName = filePathUri.toString();
				// fileName = filePathUri.toString().replace("file://", "");
				// // 替换file://
				// if (!fileName.startsWith("/mnt")) {
				// // 加上"/mnt"头
				// fileName += "/mnt";
				// }
			}
		}
		return fileName;
	}

	public static int getUriImageAngel(Context context, Uri imageUri) {

		Cursor cursor = null;
		int angle = 0;

		try {
			String[] filePathColumn = { MediaStore.Images.Media.DATA,
					MediaStore.Images.Media.ORIENTATION };
			ContentResolver cr = context.getContentResolver();
			cursor = cr.query(imageUri, filePathColumn, null, null, null);
			if (cursor != null) {
				cursor.moveToFirst();

				// 获取照片的旋转方向
				int orientationIndex = cursor.getColumnIndex(filePathColumn[1]);
				String orientation = cursor.getString(orientationIndex);
				if (orientation != null) {
					if (orientation.equals("90")) {
						angle = 90;
					} else if (orientation.equals("180")) {
						angle = 180;
					} else if (orientation.equals("270")) {
						angle = 270;
					}
				} else {
					angle = readBmpDegree(getRealPath(context, imageUri));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 无论什么情况，最终保证cursor得到释放
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}

		return angle;
	}

	/**
	 * 读取图片属性：旋转的角度
	 * 
	 * @param path
	 *            图片绝对路径
	 * @return degree旋转的角度
	 */
	private static int readBmpDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return degree;
		}

		return degree;
	}

	public static void hideSoftInputKeyboard(Context context, View view) {
		InputMethodManager mInputMethodManager = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (view != null)
			mInputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),
					0);
	}

	public static List<Friend> getFriendOrderByLetter(List<Friend> friends) {
		TreeSet<Friend> resultTemp = new TreeSet<Friend>(new PinyinComparator());
		resultTemp.addAll(friends);
		List<Friend> result = new ArrayList<Friend>(resultTemp);
		friends.clear();
		resultTemp.clear();
		return result;
	}

	// 生成圆形图片

	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {

		try {

			Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),

			bitmap.getHeight(), Config.ARGB_8888);
			Canvas canvas = new Canvas(output);

			Paint paint = new Paint();

			Rect rect = new Rect(0, 0, bitmap.getWidth(),

			bitmap.getHeight());

			RectF rectF = new RectF(new Rect(0, 0, bitmap.getWidth(),

			bitmap.getHeight()));

			final float roundPx = bitmap.getWidth() / 2;

			paint.setAntiAlias(true);

			canvas.drawARGB(0, 0, 0, 0);

			paint.setColor(Color.BLACK);

			canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

			paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));

			final Rect src = new Rect(0, 0, bitmap.getWidth(),

			bitmap.getHeight());

			canvas.drawBitmap(bitmap, src, rect, paint);
			if (bitmap != null && !bitmap.isRecycled()) {
				bitmap.recycle();
				bitmap = null;
			}
			return output;

		} catch (Exception e) {

			return bitmap;

		}

	}

	// 头像图片保存为正方形
	public static Bitmap getRectBitmap(Bitmap bitmap) {
		Bitmap output = null;
		if (bitmap.getWidth() > bitmap.getHeight()) {
			output = Bitmap.createBitmap(bitmap,
					bitmap.getWidth() / 2 - bitmap.getHeight() / 2, 0,
					bitmap.getHeight(), bitmap.getHeight());
		} else {
			output = Bitmap.createBitmap(bitmap, 0, bitmap.getHeight() / 2
					- bitmap.getWidth() / 2, bitmap.getWidth(),
					bitmap.getWidth());
		}
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
			bitmap = null;
		}
		return output;
	}

}
