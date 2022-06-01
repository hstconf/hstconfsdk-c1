package com.infowarelab.conference.ui.notice;
////import org.apache.log4j.Logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DialogReceiver extends BroadcastReceiver {
    //private final Logger log = Logger.getLogger(getClass());
    @Override
    public void onReceive(Context context, Intent intent) {
        //log.info("onReceiver...");
        NoticeManager noticeManager = NoticeManager.getInstance(context);
        noticeManager.doDialog(context, intent);
    }

}
