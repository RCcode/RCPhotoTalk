package com.rcplatform.phototalk.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import com.rcplatform.phototalk.MenueApplication;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.utils.AppSelfInfo;
import com.rcplatform.phototalk.views.ColorPicker.OnColorChangeListener;

public class ColorPickerDialog {

    private final Context mContext;

    private PopupWindow mPopupWindow;

    private ColorPicker mColorPicker;

    private final MenueApplication application;

    public ColorPickerDialog(Context context) {
        // TODO Auto-generated constructor stub
        this.mContext = context;
        application = (MenueApplication) context.getApplicationContext();
        init();
    }

    private void init() {
        // TODO Auto-generated method stub
        View view = LayoutInflater.from(mContext).inflate(R.layout.color_picker_dialog, null);
        mColorPicker = (ColorPicker) view.findViewById(R.id.cp);
        mPopupWindow = new PopupWindow(mContext);
        mPopupWindow.setWidth(mContext.getResources().getDimensionPixelSize(R.dimen.color_picker_width));
        AppSelfInfo.screenHeightPx = application.getScreentHeight();
        mPopupWindow.setHeight(AppSelfInfo.screenHeightPx / 3);
        mPopupWindow.setContentView(view);
        // mPopupWindow.setAnimationStyle(R.style.dialogWindowAnim);
        mPopupWindow.setBackgroundDrawable(null);
        mPopupWindow.setTouchable(true);
        // mPopupWindow.setFocusable(true);
        mPopupWindow.setOutsideTouchable(false);
    }

    public boolean isShowing() {
        return mPopupWindow.isShowing();
    }

    public void dismiss() {
        mPopupWindow.dismiss();
        mColorPicker.recyle();
    }

    public void showDialog(View view) {
        mPopupWindow.showAsDropDown(view, view.getWidth() / 2, 20);
    }

    public void setOnColorChangeListener(OnColorChangeListener listener) {
        mColorPicker.setOnColorChangeListener(listener);
    }
}
