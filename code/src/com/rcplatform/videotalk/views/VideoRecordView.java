package com.rcplatform.videotalk.views;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.rcplatform.videotalk.PhotoTalkApplication;

public class VideoRecordView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "VideoRecordView";

    private SurfaceHolder holder;

    private Camera camera;

    private Context context;

    private Bitmap mBitmap;

    private int bitmapWidth;

    private int bitmapHeight;

    private int mNumCamera;

    private int mFrontCameraNum;

    private int mBackCameraNum;

    private int mCurrentCameraNum = -1;

    private static final int INVALID_CAMERA = -1;

    private boolean isOpenLight = false;

    private boolean isBackFace = true;

    private boolean isShowCamera = false;

    private PhotoTalkApplication app;

    private Camera.Parameters parameters;

    private int screenHeight;

    private int screenWidth;

    private int mOrientationHint = 0;

    // -------
    private MediaRecorder mMediaRecorder;

    private CamcorderProfile mProfile;

    private CamcorderProfile mLastProfile;

    public VideoRecordView(Context context) {
        super(context);
        init(context);
    }

    public VideoRecordView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public VideoRecordView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        screenWidth = app.getScreenWidth();
        screenHeight = app.getScreenWidth() * mProfile.videoFrameWidth / mProfile.videoFrameHeight;
        super.onMeasure(MeasureSpec.makeMeasureSpec(screenWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(screenHeight, MeasureSpec.EXACTLY));
    }

    private void init(Context context) {
        // 获得SurfaceHolder对象
        holder = getHolder();
        // 指定用于捕捉拍照事件的SurfaceHolder.Callback对象
        holder.addCallback(this);
        // 设置SurfaceHolder对象的类型
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        this.context = context;
        app = (PhotoTalkApplication) context.getApplicationContext();
        initRecordParams();
    }

    public void startRecord(String filePath) {
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.reset();// 重置
        // CamcorderProfile mProfile = CamcorderProfile.get(mCurrentCameraNum,
        // CamcorderProfile.QUALITY_HIGH);
        // CamcorderProfile profile = null;
        // Class class1 = mProfile.getClass();
        // Field fied[] = class1.getFields();
        // for (int i = 0; i < fied.length; i++) {
        // try {
        // profile = CamcorderProfile.get(mCurrentCameraNum,
        // fied[i].getInt(mProfile));
        // Log.i("Futao", "name = " + fied[i].getName() + "value = " +
        // fied[i].getInt(mProfile) + " SIZE = " + profile.videoFrameWidth +
        // " * "
        // + profile.videoFrameHeight + " VBR = " + profile.videoBitRate +
        // "aubr = " + profile.audioBitRate + "//" + profile.audioCodec
        // + "/" + profile.videoCodec);
        // Log.e("Futao", "--------------------");
        // }
        // catch (IllegalArgumentException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // catch (IllegalAccessException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // }
        camera.unlock();
        mMediaRecorder.setCamera(camera);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);// 从照相机采集视频
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);// 从麦克风获取音频
        // mMediaRecorder.setProfile(mProfile);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setVideoSize(mProfile.videoFrameWidth, mProfile.videoFrameHeight);
        mMediaRecorder.setVideoFrameRate(mProfile.videoFrameRate);
        mMediaRecorder.setVideoEncodingBitRate(800000);
        mMediaRecorder.setAudioEncodingBitRate(120000);
        mMediaRecorder.setVideoEncoder(mProfile.videoCodec);//
        mMediaRecorder.setAudioEncoder(mProfile.audioCodec);//
        if (mCurrentCameraNum == mBackCameraNum) {
            mOrientationHint = 90;
        } else if (mCurrentCameraNum == mFrontCameraNum) {
            mOrientationHint = 270;
        }
        mMediaRecorder.setOrientationHint(mOrientationHint);
        File videoFile = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".mp4");
        mMediaRecorder.setOutputFile(filePath);
        mMediaRecorder.setMaxDuration(10000);
        // 设置预览画面，这里是使用SurfaceView
        mMediaRecorder.setPreviewDisplay(getHolder().getSurface());
        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();// 开始录制
        }
        catch (IllegalStateException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }// 缓冲

    }

    private List<CamcorderProfile> getAllProfiles(int CameraId) {
        List<CamcorderProfile> profiles = new ArrayList<CamcorderProfile>();
        CamcorderProfile profile = null;
        for (int i = 0; i < 8; i++) {
            try {
                profile = CamcorderProfile.get(CameraId, i);
                profiles.add(profile);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return profiles;
    }

    private CamcorderProfile getOptmalCamcorderProfile(List<CamcorderProfile> profiles, int w, int h) {
        if (profiles == null)
            return null;

        CamcorderProfile optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        double minDiffW = Double.MAX_VALUE;
        int targetHeight = h;
        int targeWidth = w;
        // Try to find an size match aspect ratio and size
        for (CamcorderProfile profile : profiles) {
            double ratio = (double) profile.videoFrameWidth / profile.videoFrameHeight;
            if (profile.videoFrameHeight > 480)
                continue;
            if (profile.videoFrameHeight == 480) {
                if (Math.abs(profile.videoFrameWidth - targeWidth) < minDiffW) {
                    optimalSize = profile;
                    minDiffW = Math.abs(profile.videoFrameWidth - targeWidth);
                }
                continue;
            }
            if (optimalSize == null) {
                if (Math.abs(profile.videoFrameHeight - targetHeight) < minDiff) {
                    optimalSize = profile;
                    minDiff = Math.abs(profile.videoFrameHeight - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public void stopRecord(boolean startPreview) {
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            camera.lock();
            if (!startPreview)
                releaseCamera();

        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        initCamera();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // 释放手机摄像头
        stopRecord(false);
        releaseCamera();
    }

    public static int setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
        return result;
    }

    public static Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null)
            return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public static Size getOptimalPictureSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null)
            return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public boolean setLightStatu() {
        isOpenLight = !isOpenLight;
        releaseCamera();
        initCamera();
        return isOpenLight;
    }

    public boolean changeCamera() {
        isBackFace = !isBackFace;
        mLastProfile = mProfile;
        if (isBackFace) {
            mCurrentCameraNum = mBackCameraNum;
        } else {
            mCurrentCameraNum = mFrontCameraNum;
        }
        releaseCamera();
        initRecordParams();

        if (mLastProfile.videoFrameHeight != mProfile.videoFrameHeight || mLastProfile.videoFrameWidth != mProfile.videoFrameWidth) {
            getLayoutParams().width = app.getScreenWidth();
            getLayoutParams().height = app.getScreenWidth() * mProfile.videoFrameWidth / mProfile.videoFrameHeight;
            requestLayout();
            requestFocus();
        }
        initCamera();
        return isBackFace;
    }

    private void initCamera() {
        if (!isShowCamera) {
            camera = Camera.open(mCurrentCameraNum);
        }

        if (camera != null && !isShowCamera) {
            try {
                // 设置用于显示拍照影像的SurfaceHolder对象
                // camera.reconnect();
                camera.setPreviewDisplay(holder);
                parameters = camera.getParameters();

                if (isOpenLight) {
                    parameters.setFlashMode(Parameters.FLASH_MODE_ON);
                } else {
                    parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
                }

                if (Build.VERSION.SDK_INT >= 8)
                    setCameraDisplayOrientation((Activity) context, mCurrentCameraNum, camera);

                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    parameters.set("orientation", "portrait");
                }
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    parameters.set("orientation", "landscape");
                }

                parameters.setRotation(90);

                Size previewSize = getOptimalPreviewSize(parameters.getSupportedPreviewSizes(), mProfile.videoFrameWidth, mProfile.videoFrameHeight);
                if (previewSize != null)
                    parameters.setPreviewSize(previewSize.width, previewSize.height);

                List<String> focusModes = parameters.getSupportedFocusModes();
                if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
                }
                camera.setParameters(parameters);
                // 开始拍照
                camera.startPreview();
                // camera.unlock();
                // 设置保存的图像大小
                isShowCamera = true;
            }
            catch (IOException exception) {
                // 释放手机摄像头
                releaseCamera();
            }
        }

    }

    private void releaseCamera() {
        if (camera != null) {
            // camera.lock();
            camera.stopPreview();
            camera.release();
            camera = null;
        }
        if (parameters != null)
            parameters = null;
        isShowCamera = false;
    }

    private void initRecordParams() {
        mNumCamera = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

        for (int i = 0; i < mNumCamera; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mFrontCameraNum = i;
            } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mBackCameraNum = i;
            }
        }
        if (mNumCamera == INVALID_CAMERA)
            return;

        if (mCurrentCameraNum == -1) {
            mCurrentCameraNum = mBackCameraNum;
            mProfile = getOptmalCamcorderProfile(getAllProfiles(mBackCameraNum), app.getScreentHeight(), app.getScreenWidth());
        } else
            mProfile = getOptmalCamcorderProfile(getAllProfiles(mCurrentCameraNum), app.getScreentHeight(), app.getScreenWidth());

    }

}
