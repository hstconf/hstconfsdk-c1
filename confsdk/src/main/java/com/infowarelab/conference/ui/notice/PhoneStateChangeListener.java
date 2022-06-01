package com.infowarelab.conference.ui.notice;

////import org.apache.log4j.Logger;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

/**
 * @author Jack.Yan@infowarelab.com
 * @description A listener class for monitoring changes in phone connection states.
 * @date 2012-11-26
 */
public class PhoneStateChangeListener extends PhoneStateListener {

    //private final Logger log = Logger.getLogger(getClass());

    private final NotificationService notificationService;

    public PhoneStateChangeListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void onDataConnectionStateChanged(int state) {
        super.onDataConnectionStateChanged(state);
        //log.info("onDataConnectionStateChanged()...");
        //log.info("Data Connection State = " + getState(state));

        if (state == TelephonyManager.DATA_CONNECTED) {
            notificationService.connect();
        }
    }

    private String getState(int state) {
        switch (state) {
            case 0: // '\0'
                return "DATA_DISCONNECTED";
            case 1: // '\001'
                return "DATA_CONNECTING";
            case 2: // '\002'
                return "DATA_CONNECTED";
            case 3: // '\003'
                return "DATA_SUSPENDED";
        }
        return "DATA_<UNKNOWN>";
    }

}
