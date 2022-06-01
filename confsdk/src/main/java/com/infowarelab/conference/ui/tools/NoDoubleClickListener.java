package com.infowarelab.conference.ui.tools;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.infowarelab.hongshantongphone.R;

import java.util.Calendar;

/**
 * Created by Always on 2018/3/29.
 */

public abstract class NoDoubleClickListener implements OnClickListener {
    public static final int MIN_CLICK_DELAY_TIME = 300;
    public static long lastClickTime = 0;
    public static final int MIN_TOAST_DELAY_TIME = 4000;
    public static long lastTOASTTime = 0;

    @Override
    public void onClick(View v) {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
            lastClickTime = currentTime;
            v.setEnabled(false);
            onNoDoubleClick(v);
            Message msg = new Message();
            msg.obj = v;
            uHandler.sendMessage(msg);
        } else if (currentTime - lastTOASTTime > MIN_TOAST_DELAY_TIME) {
            lastTOASTTime = currentTime;
            Toast.makeText(v.getContext(), R.string.clicktoofast, Toast.LENGTH_SHORT).show();
        }
    }

    protected abstract void onNoDoubleClick(View v);

    Handler uHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            View v = (View) msg.obj;
            v.setEnabled(true);
        }
    };
}
