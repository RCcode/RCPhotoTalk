package com.rcplatform.phototalk;

import android.app.Activity;
import android.content.Intent;

public class BaseMediaActivity extends Activity {

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        // overridePendingTransition(R.anim.activity_enter_left,
        // R.anim.activity_outer_right);
    }
}
