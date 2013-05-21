package com.rcplatform.phototalk.views;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.ThumbnailUtils;
import android.provider.MediaStore.Video;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.rcplatform.phototalk.PhotoTalkApplication;

public class PlayVidoeView extends SurfaceView implements SurfaceHolder.Callback, OnPreparedListener, MediaPlayer.OnCompletionListener {

    private MediaPlayer player;

    private SurfaceHolder holder;

    private String filePath;

    private int vWidth, vHeight;

    private PhotoTalkApplication app;

    private Bitmap bitmap;

    private boolean isLoopPlayback;

    private OnStartPlayListener onStartPlayListener;

    public interface OnStartPlayListener {

        void onStart();
    }

    public PlayVidoeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Log.i("Futao", "PlayVidoeView");
    }

    public PlayVidoeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.i("Futao", "PlayVidoeView");
    }

    public PlayVidoeView(Context context) {
        super(context);
        Log.i("Futao", "PlayVidoeView");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i("Futao", "surfaceCreated");
        // if (isLoopPlayback)
        playVideo();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i("Futao", "surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i("Futao", "surfaceDestroyed");
        releasePlayer();

    }

    public void initMediaPlayer(String filePath) {
        this.filePath = filePath;
        bitmap = ThumbnailUtils.createVideoThumbnail(filePath, Video.Thumbnails.MINI_KIND);
        BitmapDrawable drawable = new BitmapDrawable(bitmap);
        setBackgroundDrawable(drawable);
        holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        player = new MediaPlayer();
        player.setOnPreparedListener(this);

        // /player.setOnCompletionListener(this);
        try {
            player.setDataSource(filePath);
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        catch (IllegalStateException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        app = (PhotoTalkApplication) getContext().getApplicationContext();
    }

    public void setFilePath(String path) {
        this.filePath = path;
        File file = new File(filePath);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        // 当prepare完成后，该方法触发，在这里我们播放视频

        // 首先取得video的宽和高
        vWidth = player.getVideoWidth();
        vHeight = player.getVideoHeight();
        Log.e("Futao", "player = " + player.getVideoWidth() + " * " + player.getVideoHeight());
        getLayoutParams().width = app.getScreenWidth();
        getLayoutParams().height = app.getScreenWidth() * vHeight / vWidth;
        requestLayout();
        requestFocus();
        if (vWidth > app.getScreenWidth() || vHeight > app.getScreentHeight()) {
            // 如果video的宽或者高超出了当前屏幕的大小，则要进行缩放
            float wRatio = vWidth / (float) app.getScreenWidth();
            float hRatio = vHeight / (float) app.getScreentHeight();

            // 选择大的一个进行缩放
            float ratio = Math.max(wRatio, hRatio);

            vWidth = (int) Math.ceil(vWidth / ratio);
            vHeight = (int) Math.ceil(vHeight / ratio);
        }
        if (isLoopPlayback)
            player.setLooping(true);
        else
            player.setLooping(false);
        player.start();
        if (onStartPlayListener != null)
            onStartPlayListener.onStart();
    }

    public void playVideo() {
        if (player != null) {
            setBackgroundDrawable(null);
            player.setDisplay(this.holder);
            player.prepareAsync();
        } else {
            initMediaPlayer(this.filePath);
            playVideo();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

        if (isLoopPlayback) {
            // playVideo();
        } else {
            player.reset();
            player.release();
            player = null;
        }
    }

    public void setPlayMode() {
        isLoopPlayback = true;
    }

    private void releasePlayer() {
        if (player != null) {
            if (player.isLooping())
                player.setLooping(false);
            if (player.isPlaying())
                player.stop();

            player.reset();
            player.release();
            player = null;
        }
    }

    private void setMuteMode(boolean mute) {
        AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, mute);
    }

    public void setOnStartPlayListener(OnStartPlayListener onStartPlayListener) {
        this.onStartPlayListener = onStartPlayListener;
    }

}
