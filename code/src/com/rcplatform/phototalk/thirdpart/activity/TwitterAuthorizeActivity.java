package com.rcplatform.phototalk.thirdpart.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TwitterAuthorizeActivity extends Activity {
	private WebView mWebView;
	public static final String RESULT_KEY_VERIFIER = "verifier";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mWebView = new WebView(this);
		setContentView(mWebView);
		loadUrl(getIntent().getData().toString());
	}

	private void loadUrl(String url) {
		mWebView.setWebViewClient(new WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				if (url.contains("oauth_verifier")) {
					String verifier = null;
					// 验证结果
					if (url.contains("tel")) {
						int index = url.lastIndexOf(":");
						verifier = url.substring(index + 1);
					} else {
						verifier = url.substring(url.lastIndexOf("&")).substring(url.substring(url.lastIndexOf("&")).lastIndexOf("=") + 1);
					}
					Intent intent = new Intent();
					intent.putExtra(RESULT_KEY_VERIFIER, verifier);
					setResult(Activity.RESULT_OK, intent);
					finish();
				}
			}
		});
		mWebView.loadUrl(url);
	}
}
