package com.rcplatform.phototalk.views;

import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.utils.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class HeadImageView extends ImageView {
	public HeadImageView(Context context) {
		super(context);
	}

	public HeadImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		 super.setBackgroundDrawable(new BitmapDrawable(bm));
		super.setImageResource(R.drawable.gallery_head_bg);
	}

	@Override
	public void setBackgroundResource(int resid) {
		super.setBackgroundResource(resid);
		System.out.println("--setBackgroundResource-");
	}

	@Override
	public void setImageResource(int resId) {
		super.setBackgroundResource(resId);
		super.setImageResource(R.drawable.gallery_head_bg);
	}

}
