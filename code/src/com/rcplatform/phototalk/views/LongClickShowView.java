package com.rcplatform.phototalk.views;

import java.io.File;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.DiscCacheUtil;
import com.rcplatform.phototalk.MenueApplication;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.InformationType;
import com.rcplatform.phototalk.image.downloader.ImageOptionsFactory;
import com.rcplatform.phototalk.image.downloader.RCPlatformImageLoader;
import com.rcplatform.phototalk.utils.AppSelfInfo;
import com.rcplatform.phototalk.views.PlayVidoeView.OnStartPlayListener;
import com.rcplatform.phototalk.views.RecordTimerLimitView.OnTimeEndListener;

public class LongClickShowView extends Dialog {

    public Rect invaildRange; // 无效区域，

    public boolean invalidTouch;

    private String mLastFilePath = "";

    private RecordTimerLimitView glTimer;

    private LayoutParams params;

    private MenueApplication app;

    private GLTimer timer;

    private RelativeLayout contentView;

    public LongClickShowView(Context context, int theme) {
        super(context, theme);
        this.getWindow().setWindowAnimations(R.style.ContentOverlay);
        app = (MenueApplication) context.getApplicationContext();
    }

    public LongClickShowView(Context context) {
        super(context);

    }

    /**
     * Helper class for creating a custom dialog
     */
    public static class Builder {

        private final Context context;

        private static ImageView mImageView;

        private static PlayVidoeView mPlayVidoeView;

        private int layoutResId;

        private LongClickShowView dialog;

        public Builder(Context context, int layoutResId) {
            this.context = context;
            this.layoutResId = layoutResId;
        }

        public Builder(Context context, RelativeLayout view) {
            this.context = context;
        }

        /**
         * Create the custom dialog
         */
        public LongClickShowView create() {
            if (dialog == null)
                dialog = new LongClickShowView(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            if (dialog.contentView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                dialog.contentView = (RelativeLayout) inflater.inflate(layoutResId, null);
                mImageView = (ImageView) dialog.contentView.findViewById(R.id.iv_rts_pic);
                mPlayVidoeView = (PlayVidoeView) dialog.contentView.findViewById(R.id.pv_rts_video);

                // contentView.setBackgroundColor(Color.parseColor("#00000000"));
                dialog.setContentView(dialog.contentView);
            } else {
                dialog.setContentView(dialog.contentView);
            }
            Log.i("ABC", "DIALOG = " + dialog.toString());
            /*
             * contentView.setOnClickListener(new View.OnClickListener(){
             * @Override public void onClick(View view) { dialog.hide(); } });
             */
            return dialog;
        }

    }

    private String url = "";

    public void ShowDialog(Information info) {
        if (info.getUrl() == null)
            return;
        if (glTimer == null) {
            glTimer = new RecordTimerLimitView(getContext());
            params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.setMargins(0, 10, 40, 0);
            glTimer.setTextSize(56);
            glTimer.setTextColor(Color.RED);
            glTimer.setOnTimeEndListener(new OnTimeEndListener() {

                @Override
                public void onEnd(Object statuTag, Object buttonTag) {
                    hideDialog();
                }
            }, null, null);
            Builder.mPlayVidoeView.setOnStartPlayListener(new OnStartPlayListener() {

                @Override
                public void onStart() {
                    glTimer.setVisibility(View.VISIBLE);
                }
            });
            contentView.addView(glTimer, params);
        }

        if (!info.getUrl().equals(mLastFilePath)) {
            if (info.getType() == InformationType.TYPE_PICTURE_OR_VIDEO) {// 图片
                glTimer.setVisibility(View.VISIBLE);
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/1111.jpg";
                Bitmap b = BitmapFactory.decodeFile(filePath);
                Builder.mPlayVidoeView.setVisibility(View.GONE);
                Builder.mImageView.setVisibility(View.VISIBLE);
                url = info.getUrl();
                RCPlatformImageLoader.loadImage(getContext(), ImageLoader.getInstance(), ImageOptionsFactory.getPublishImageOptions(), url,
                                           AppSelfInfo.ImageScaleInfo.bigImageWidthPx, Builder.mImageView, R.drawable.default_head);
            } else { // 视频
                glTimer.setVisibility(View.GONE);
                Builder.mImageView.setVisibility(View.GONE);
                Builder.mPlayVidoeView.setVisibility(View.VISIBLE);
                Builder.mPlayVidoeView.initMediaPlayer(app.getCacheFilePath() + "/" + "1363169831996.mp4");
            }
        } else {

        }
        glTimer.scheuleTask(info);
        show();
        mLastFilePath = info.getUrl();
        // b.recycle();
    }

    public void hideDialog() {
        File f = DiscCacheUtil.findInCache(url, new UnlimitedDiscCache(app.cacheDir, new Md5FileNameGenerator()));
        // Log.i("Futao", f.getAbsolutePath().toString());
        // if (f.exists())
        // f.delete();
        hide();
    }

}
