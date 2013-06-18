package com.rcplatform.phototalk;

import com.rcplatform.phototalk.activity.BaseActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;

public class RCWebview extends BaseActivity implements View.OnClickListener {

	static public void startWebview(Context ctx, String url, int titleID) {
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putString("url", url);
		bundle.putInt("title", titleID);
		intent.setClass(ctx, RCWebview.class);
		intent.putExtras(bundle);
		ctx.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.rc_webview);
		Bundle bundle = new Bundle();
		bundle = this.getIntent().getExtras();
		int titleID = bundle.getInt("title");
		initBackButton(titleID, this);
		String url = bundle.getString("url");

		WebView webView = (WebView) findViewById(R.id.webview);
		webView.loadUrl(url);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case R.id.back:
				finish();
				break;
		}
	}

}
