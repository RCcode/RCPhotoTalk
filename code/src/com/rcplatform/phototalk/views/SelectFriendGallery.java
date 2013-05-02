package com.rcplatform.phototalk.views;

import android.content.Context;
import android.graphics.Camera;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.Gallery;

public class SelectFriendGallery extends Gallery {

    private final boolean isMove = true;

    private final Camera mCamera;

    private int mWidth;

    private int mPaddingLeft;

    private boolean flag;

    private static int firstChildWidth;

    private static int firstChildPaddingLeft;

    public SelectFriendGallery(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mCamera = new Camera();
        // setAttributesValue(context, attrs);
        this.setStaticTransformationsEnabled(true);
        // TODO Auto-generated constructor stub
    }

    public SelectFriendGallery(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCamera = new Camera();
        // setAttributesValue(context, attrs);
        this.setStaticTransformationsEnabled(true);
        // TODO Auto-generated constructor stub
    }

    public SelectFriendGallery(Context context) {
        super(context);
        mCamera = new Camera();
        this.setStaticTransformationsEnabled(true);
        // TODO Auto-generated constructor stub
    }

    // @Override
    // public boolean onSingleTapUp(MotionEvent e) {
    // // TODO Auto-generated method stub
    // return false;
    // }

    // 每次drawchild的时候会调用这个方法
    @Override
    protected boolean getChildStaticTransformation(View child, Transformation t) {
        // TODO Auto-generated method stub
        t.clear();
        t.setAlpha(1.0f);
        return true;
    }

}
