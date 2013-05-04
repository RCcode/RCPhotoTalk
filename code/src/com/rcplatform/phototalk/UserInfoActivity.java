package com.rcplatform.phototalk;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.bean.UserInfo;

public class UserInfoActivity extends BaseActivity implements OnClickListener {
	private TextView user_Email;
	private TextView user_Phone;
	private TextView user_rcId;
	private TextView mTitleTextView;
	private RelativeLayout faceBook_layout;
	private RelativeLayout vK_layout;
	private Button reset_pw_btn;
	private Button login_out_btn;
	private View mBack;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_info);
		initTitle();
		initView();
		setTextView();
	}

	private void initTitle() {
		mBack = findViewById(R.id.back);
		mBack.setVisibility(View.VISIBLE);
		mBack.setOnClickListener(this);
		mTitleTextView = (TextView) findViewById(R.id.titleContent);
		mTitleTextView.setText(getResources().getString(R.string.user_message));
		mTitleTextView.setVisibility(View.VISIBLE);
	}

	public void initView() {
		user_Email = (TextView) findViewById(R.id.user_email);
		user_Phone = (TextView) findViewById(R.id.user_phone);
		user_rcId = (TextView) findViewById(R.id.user_rcid);
		faceBook_layout = (RelativeLayout) findViewById(R.id.user_facebook_layout);
		faceBook_layout.setOnClickListener(this);
		vK_layout = (RelativeLayout) findViewById(R.id.user_vk_layout);
		vK_layout.setOnClickListener(this);
		reset_pw_btn = (Button) findViewById(R.id.reset_pw_btn);
		reset_pw_btn.setOnClickListener(this);
		login_out_btn = (Button) findViewById(R.id.login_out_btn);
		login_out_btn.setOnClickListener(this);
	}

	public void setTextView() {
		UserInfo userInfo = getPhotoTalkApplication().getCurrentUser();
		user_Email.setText(userInfo.getEmail());
		user_Phone.setText(userInfo.getPhone());
		user_rcId.setText(userInfo.getRcId());
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.user_facebook_layout:

			break;
		case R.id.user_vk_layout:

			break;
		case R.id.reset_pw_btn:
			startActivity(ChangePasswordActivity.class);
			break;
		case R.id.login_out_btn:
			Intent intent = new Intent(this, HomeActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			// 清楚数据操作
			this.finish();
			break;
		case R.id.back:
			startActivity(new Intent(this, SettingsActivity.class));
			this.finish();
			break;
		}
	}
}
