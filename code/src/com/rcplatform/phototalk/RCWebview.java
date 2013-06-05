package com.rcplatform.phototalk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;

public class RCWebview extends Activity implements View.OnClickListener {

	static public void startWebview(Context ctx, String url) {
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putString("url", url);
		intent.setClass(ctx, RCWebview.class);
		intent.putExtras(bundle);
		ctx.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rc_webview);
		ImageButton back_btn = (ImageButton) findViewById(R.id.back);
		back_btn.setVisibility(View.VISIBLE);
		back_btn.setOnClickListener(this);

		Bundle bundle = new Bundle();

		bundle = this.getIntent().getExtras();

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
