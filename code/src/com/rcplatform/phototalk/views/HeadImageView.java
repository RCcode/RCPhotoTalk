package com.rcplatform.phototalk.views;

import com.rcplatform.phototalk.utils.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;

public class HeadImageView extends ImageView {
	public HeadImageView(Context context) {
		super(context);
	}

	public HeadImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		// TODO Auto-generated method stub
//		Bitmap bitmap = Utils.getRectBitmap(bm);
		super.setImageBitmap(Utils.getRoundedCornerBitmap(bm));
//		super.setImageBitmap(bm);
	}
}
