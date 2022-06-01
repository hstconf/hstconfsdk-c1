package com.infowarelab.conference.ui.view;


import com.infowarelab.hongshantongphone.R;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class ClearCacheDialog extends AlertDialog {
    private int width = 0;
    private OnResultListener onResultListener;

    public ClearCacheDialog(Context context) {
        super(context, R.style.style_dialog_normal);
    }

    public ClearCacheDialog(Context context, int width) {
        super(context, R.style.style_dialog_normal);
        this.width = width;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a6_preconf_set_cache);
        if (width > 0) {
            LinearLayout ll = (LinearLayout) findViewById(R.id.dialog_cache_ll);
            LayoutParams params = (LayoutParams) ll.getLayoutParams();
            params.width = this.width;
            ll.setLayoutParams(params);
        }
        setCanceledOnTouchOutside(true);

        TextView tvYes = (TextView) findViewById(R.id.dialog_cache_tv_confirm);
        tvYes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                doYes();
                cancel();
            }
        });
        TextView tvNo = (TextView) findViewById(R.id.dialog_cache_tv_cancel);
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
