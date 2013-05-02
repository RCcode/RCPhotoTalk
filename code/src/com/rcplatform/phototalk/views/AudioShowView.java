package com.rcplatform.phototalk.views;

import com.rcplatform.phototalk.R;

import android.content.Context;
import android.graphics.Color;
import android.view.ContextMenu;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AudioShowView extends LinearLayout{
	ImageView imageView;
	TextView textView;
	Context context;
	public AudioShowView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.context = context;
		
		this.setOrientation(VERTICAL);
		getView();
	}
	public void getView(){
		this.setBackgroundResource(R.drawable.com_facebook_picker_list_selector_disabled);
//		imageView = new ImageView(context);
//		imageView.setBackgroundResource(R.drawable.base_go_btn);
//		this.addView(imageView);
		textView = new TextView(context);
		LinearLayout.LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity=Gravity.CENTER_HORIZONTAL;
		textView.setBackgroundColor(Color.GREEN);
		textView.setLayoutParams(params);
		this.addView(textView,params);
		
	};
	public void setChanceText(String text){
		textView.setText(text+"s");
	}
}
