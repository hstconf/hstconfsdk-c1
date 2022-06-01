package com.infowarelab.conference.ui.view;


import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.UserCommonImpl;

public class ExitDialog extends AlertDialog {
    private int width = 0;
    private OnResultListener onResultListener;

    public ExitDialog(Context context) {
        super(context, R.style.style_dialog_normal);
    }

    public ExitDialog(Context context, int width) {
        super(context, R.style.style_dialog_normal);
        this.width = width;
    }

    public ExitDialog(Context context, int width, OnResultListener onResultListener) {
        super(context, R.style.style_dialog_normal);
        this.width = width;
        this.onResultListener = onResultListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_dialog_exit);
        if (width > 0) {
            LinearLayout ll = (LinearLayout) findViewById(R.id.dialog_exit_ll);
            LayoutParams params = (LayoutParams) ll.getLayoutParams();
            params.width = this.width;
            ll.setLayoutParams(params);
        }
        setCanceledOnTouchOutside(false);

        TextView tvTitle = (TextView) findViewById(R.id.dialog_exit_title);
        tvTitle.setText(((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).isHost() ? R.string.about_leave_host : R.string.exitTip);

        TextView tvYes = (TextView) findViewById(R.id.dialog_exit_tv_confirm);
        tvYes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                doYes();
                cancel();
            }
        });
        TextView tvNo = (TextView) findViewById(R.id.dialog_exit_tv_cancel);
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
        public void doYes();

        public void doNo();
    }

    private void doYes() {
        if (onResultListener != null) {
            onResultListener.doYes();
        }
    }

    private void doNo() {
        if (onResultListener != null) {
            onResultListener.doNo();
        }
    }
}
