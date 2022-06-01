package com.infowarelab.conference.ui.view;


import com.infowarelab.hongshantongphone.R;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class CallattDialog extends AlertDialog {
    private int width = 0;
    private OnResultListener onResultListener;
    private int timeout = 30;
    private int id = 2333;
    private boolean runing = false;
    private TextView tvTime;
    private int seconds = 0;

    public CallattDialog(Context context) {
        super(context, R.style.style_dialog_normal);
    }

    public CallattDialog(Context context, int width) {
        super(context, R.style.style_dialog_normal);
        this.width = width;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a6_inconf_callatt);
        if (width > 0) {
            LinearLayout ll = (LinearLayout) findViewById(R.id.dialog_callatt_ll);
            LayoutParams params = (LayoutParams) ll.getLayoutParams();
            params.width = this.width;
            ll.setLayoutParams(params);
        }
        setCanceledOnTouchOutside(false);
        tvTime = (TextView) findViewById(R.id.dialog_callatt_tv_second);
        TextView tvYes = (TextView) findViewById(R.id.dialog_callatt_tv_confirm);
        tvYes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                doYes(timeout, id);
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

    public void showATime(int timeout, int id) {
        this.timeout = timeout;
        this.id = id;
        if (this.timeout > 0) {
            super.show();
            this.seconds = timeout;
            runing = true;
            new Thread(new MyThread1()).start();
        }
    }

    public void setClickListener(OnResultListener onResultListener) {
        this.onResultListener = onResultListener;
    }

    public interface OnResultListener {
        public void doYes(int timeout, int id);

        public void doNo(int timeout, int id);
    }

    private void doYes(int timeout, int id) {
        if (onResultListener != null) {
            onResultListener.doYes(timeout, id);
        }
    }

    private void doNo(int timeout, int id) {
        if (onResultListener != null) {
            onResultListener.doNo(timeout, id);
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    tvTime.setText(seconds + "");
                    seconds--;
                    if (seconds <= 0) {
                        runing = false;
                        seconds = 60;
                        dismiss();
                    }
                    break;
            }
        }
    };

    public class MyThread1 implements Runnable {      // thread
        @Override
        public void run() {
            while (runing) {
                try {
                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);
                    Thread.sleep(1000);     // sleep 1000ms    
                } catch (Exception e) {
                }
            }
        }
    }

    @Override
    public void dismiss() {
        // TODO Auto-generated method stub
        runing = false;
        super.dismiss();
    }

}
