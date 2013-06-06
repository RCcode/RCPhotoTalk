package com.rcplatform.videotalk.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.LinearLayout;

public class EditTextView extends LinearLayout {

    private boolean isMove;

    private int downY;

    private int PreDownY;

    private EditText editText;

    public EditTextView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public EditTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        return true;
    }
}
