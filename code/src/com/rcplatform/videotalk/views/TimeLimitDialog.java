package com.rcplatform.videotalk.views;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.rcplatform.videotalk.R;

public class TimeLimitDialog extends Dialog {

    public TimeLimitDialog(Context context, int theme) {
        super(context, theme);
        // this.getWindow().setWindowAnimations(R.style.timerlimitdialog);
    }

    public TimeLimitDialog(Context context) {
        super(context);

    }

    public static class Builder {

        private final Context context;

        private View contentView;

        private int layoutResId;

        private TimeLimitDialog dialog;

        public Builder(Context context, int layoutResId) {
            this.context = context;
            this.layoutResId = layoutResId;
        }

        public Builder(Context context, View view) {
            this.context = context;
            this.contentView = view;
        }

        /**
         * Create the custom dialog
         */
        public TimeLimitDialog create() {
            if (dialog == null)
                dialog = new TimeLimitDialog(context, R.style.timerlimitdialog);
            if (this.contentView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                // contentView = inflater.inflate(layoutResId, null);
                // contentView.setBackgroundColor(Color.parseColor("#00000000"));
                dialog.setContentView(layoutResId);
            } else {
                dialog.setContentView(contentView);
            }
            Log.i("ABC", "DIALOG = " + dialog.toString());
            return dialog;
        }

        public View getContentView() {
            return contentView;
        }
    }

}
