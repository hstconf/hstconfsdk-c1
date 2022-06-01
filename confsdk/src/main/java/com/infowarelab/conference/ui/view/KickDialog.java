package com.infowarelab.conference.ui.view;


import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.infowarelab.hongshantongphone.R;

public class KickDialog extends AlertDialog {
    private int width = 0;
    private int uid;
    private String name;
    private TextView tvName;
    private OnResultListener onResultListener;

    public KickDialog(Context context) {
        super(context, R.style.style_dialog_normal);
    }

    public KickDialog(Context context, int width) {
        super(context, R.style.style_dialog_normal);
        this.width = width;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_kickatt);
        if (width > 0) {
            LinearLayout ll = (LinearLayout) findViewById(R.id.dialog_kick_ll);
            LayoutParams params = (LayoutParams) ll.getLayoutParams();
            params.width = this.width;
            ll.setLayoutParams(params);
        }
        setCanceledOnTouchOutside(true);

        tvName = (TextView) findViewById(R.id.dialog_kick_tv_name);
//		tvName.setText(name);


        TextView tvYes = (TextView) findViewById(R.id.dialog_kick_tv_confirm);
        tvYes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                doYes(uid);
                cancel();
            }
        });
        TextView tvNo = (TextView) findViewById(R.id.dialog_kick_tv_cancel);
        tvNo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                doNo();
                cancel();
            }
        });

    }

    public void show(int uid, String name) {
        this.uid = uid;
        this.name = name;
        show();
    }

    @Override
    public void show() {
        super.show();
        tvName.setText(name);
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
