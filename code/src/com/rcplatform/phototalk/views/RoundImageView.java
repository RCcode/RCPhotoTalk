package com.rcplatform.phototalk.views;

import com.rcplatform.phototalk.utils.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RoundImageView extends ImageView {
	public RoundImageView(Context context) {
		super(context);
	}

	public RoundImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		
		super.setImageBitmap(Utils.getRoundedCornerBitmap(bm));
	}
}
