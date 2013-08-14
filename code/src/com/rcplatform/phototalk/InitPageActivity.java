package com.rcplatform.phototalk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.umeng.EventUtil;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.views.PageIndicator;

public class InitPageActivity extends BaseActivity implements OnGestureListener, OnTouchListener {

	public static final String REQUEST_PARAM_RELOGIN = "relogin";

	private PageIndicator mPageIndicator;

	public static final int REQUEST_CODE_LOGIN = 100;

	private AlphaAnimation inAnimation_Alpha;

	private AlphaAnimation outAnimation_Alpha;

	private GestureDetector mGestureDetector;

	private static final int FLING_MIN_DISTANCE = 100;

	private static final int FLING_MIN_VELOCITY = 200;

	private int numView = 0;

	private ViewFlipper pager;

	private TextView init_message_title, init_message_text;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.init_page);
		cancelRelogin();
		initView();
		if (getIntent().getBooleanExtra(REQUEST_PARAM_RELOGIN, false) || (Constants.userApps != null && Constants.userApps.size() > 0)) {
			startLoginActivity();
		}
	}

	private void initView() {
		mGestureDetector = new GestureDetector(this);
		mGestureDetector.setIsLongpressEnabled(true);
		inAnimation_Alpha = new AlphaAnimation(0.0f, 1.0f);
		inAnimation_Alpha.setDuration(400);
		outAnimation_Alpha = new AlphaAnimation(1.0f, 0.0f);
		outAnimation_Alpha.setDuration(400);
		init_message_title = (TextView) findViewById(R.id.init_message_title);
		init_message_text = (TextView) findViewById(R.id.init_message_text);
		init_message_title.setShadowLayer(3F, 3F, 1F, Color.BLACK);
		init_message_text.setShadowLayer(3F, 3F, 1F, Color.BLACK);
		Button mLoginButton = (Button) findViewById(R.id.init_page_login_button);
		mLoginButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startLoginActivity();
				EventUtil.Register_Login_Invite.rcpt_loginbutton(baseContext);
			}
		});
		Button mSignupButton = (Button) findViewById(R.id.init_page_signup_button);
		mSignupButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent loginIntent = new Intent(InitPageActivity.this, LoginActivity.class);
				loginIntent.putExtra(Constants.KEY_LOGIN_PAGE, false);
				startActivityForResult(loginIntent, REQUEST_CODE_LOGIN);
				EventUtil.Register_Login_Invite.rcpt_registerbutton(baseContext);
			}
		});
		pager = (ViewFlipper) findViewById(R.id.intro_pager);
		pager.setOnTouchListener(this);
		pager.setLongClickable(true);
		mPageIndicator = (PageIndicator) findViewById(R.id.page_indicator_other);
		mPageIndicator.setDotCount(pager.getChildCount());
	}

	private void startLoginActivity() {
		Intent loginIntent = new Intent(InitPageActivity.this, LoginActivity.class);
		loginIntent.putExtra(Constants.KEY_LOGIN_PAGE, true);
		startActivityForResult(loginIntent, REQUEST_CODE_LOGIN);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (intent.hasExtra(PlatformEditActivity.PARAM_FROM_PLATFORM_EDIT)) {
			startActivity(InviteActivity.class);
			finish();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			UserInfo userInfo = (UserInfo) data.getSerializableExtra(LoginActivity.RESULT_KEY_USERINFO);
			if (userInfo.getShowRecommends() == UserInfo.NOT_FIRST_TIME) {
				startActivity(HomeActivity.class);
			} else {
				startActivity(InviteActivity.class);
			}
			finish();
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return mGestureDetector.onTouchEvent(event);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
			pager.setInAnimation(inAnimation_Alpha);
			pager.setOutAnimation(outAnimation_Alpha);
			if (numView < pager.getChildCount() - 1) {
				numView++;
				onCheckMain(numView);
			}
		} else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
			pager.setInAnimation(inAnimation_Alpha);
			pager.setOutAnimation(outAnimation_Alpha);
			if (numView > 0) {
				numView--;
				onCheckMain(numView);
			}
		}

		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	public void onCheckMain(int n) {
		switch (n) {
		case 0:
			init_message_title.setText(R.string.init_message_title_one);
			init_message_text.setText(R.string.init_message_text_one);
			break;
		case 1:
			init_message_title.setText(R.string.init_message_title_two);
			init_message_text.setText(R.string.init_message_text_two);
			break;
		case 2:
			init_message_title.setText(R.string.init_message_title_three);
			init_message_text.setText(R.string.init_message_text_three);
			break;
		}
		pager.setDisplayedChild(n);
		mPageIndicator.setActiveDot(n);
	}
	// class IntroAdapter extends PagerAdapter {
	// private List<View> views = new ArrayList<View>();
	//
	// public IntroAdapter() {
	// initPager();
	// }
	//
	// private void initPager() {
	// for (int i = 0; i < GUIDE_PAGE_COUNT; i++) {
	// ImageView iv = new ImageView(InitPageActivity.this);
	// iv.setLayoutParams(new
	// ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
	// ViewGroup.LayoutParams.MATCH_PARENT));
	// iv.setScaleType(ScaleType.FIT_XY);
	// iv.setImageResource(R.drawable.login_background);
	// views.add(iv);
	// }
	// }
	//
	// @Override
	// public int getCount() {
	// return views.size();
	// }
	//
	// @Override
	// public boolean isViewFromObject(View arg0, Object arg1) {
	// return arg0 == arg1;
	// }
	//
	// @Override
	// public void destroyItem(ViewGroup container, int position, Object object)
	// {
	// container.removeView(views.get(position));
	// }
	//
	// @Override
	// public Object instantiateItem(ViewGroup container, int position) {
	// container.addView(views.get(position));
	// return views.get(position);
	// }
	// }
}
