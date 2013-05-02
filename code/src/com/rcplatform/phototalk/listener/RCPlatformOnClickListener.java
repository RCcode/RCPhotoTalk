package com.rcplatform.phototalk.listener;

import com.rcplatform.phototalk.utils.Utils;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

public abstract class RCPlatformOnClickListener implements OnClickListener {
	private static final long CLICK_SPACING_TIME=500;
	private static long mLastClickTime;
	private Context mContext;
	public RCPlatformOnClickListener(Context context) {
		// TODO Auto-generated constructor stub\
		this.mContext=context;
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		long clickTime=System.currentTimeMillis();
		if((clickTime-mLastClickTime)>CLICK_SPACING_TIME){
			Utils.hideSoftInputKeyboard(mContext, v);
			mLastClickTime=clickTime;
			onViewClick(v);
		}
		
	}
	public abstract void onViewClick(View v);
}
