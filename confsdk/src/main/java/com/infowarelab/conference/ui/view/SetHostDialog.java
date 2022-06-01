package com.infowarelab.conference.ui.view;


import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.infowarelab.hongshantongphone.R;

public class SetHostDialog extends AlertDialog {
    private int width = 0;
    private int uid = 0;
    private OnResultListener onResultListener;

    public SetHostDialog(Context context) {
        super(context, R.style.style_dialog_normal);
    }

    public SetHostDialog(Context context, int width) {
        super(context, R.style.style_dialog_normal);
        this.width = width;
    }

    public SetHostDialog(Context context, int uid, int width, OnResultListener onResultListener) {
        super(context, R.style.style_dialog_normal);
        this.uid = uid;
        this.width = width;
        this.onResultListener = onResultListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a6_dialog_sethost);
        if (width > 0) {
            LinearLayout ll = (LinearLayout) findViewById(R.id.dialog_sethost_ll);
            LayoutParams params = (LayoutParams) ll.getLayoutParams();
            params.width = this.width;
            ll.setLayoutParams(params);
        }
        setCanceledOnTouchOutside(false);

        TextView tvYes = (TextView) findViewById(R.id.dialog_sethost_tv_confirm);
        tvYes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                doYes(uid);
                cancel();
            }
        });
        TextView tvNo = (TextView) findViewById(R.id.dialog_sethost_tv_cancel);
        tvNo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                doNo();
                cancel();
            }
        });

    }

    /**
     * 在AlertDialog的 onStart() 生命周期里面执行开始动画
     */
    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * 在AlertDialog的onStop()生命周期里面执行停止动画
     */
    @Override
    protected void onStop() {
        super.onStop();

    }

    public void setClickListener(OnResultListener onResultListener) {
        this.onResultListener = onResultListener;
    }

    public interface OnResultListener {
        public void doYes(int uid);

        public void doNo();
    }

    private void doYes(int uid) {
        if (onResultListener != null) {
            onResultListener.doYes(uid);
        }
    }

    private void doNo() {
        if (onResultListener != null) {
            onResultListener.doNo();
        }
    }
}
