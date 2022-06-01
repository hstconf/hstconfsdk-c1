package com.infowarelab.conference.ui.notice;
////import org.apache.log4j.Logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @author Jack.Yan@infowarelab.com
 * @description This receiver is used for restarting the notification service once the system boot completed
 * @date 2012-11-28
 */
public class BootStartReceiver extends BroadcastReceiver {

    //private final Logger log = Logger.getLogger(getClass());

    @Override
    public void onReceive(Context context, Intent intent) {
        //log.info("onReceive: android.intent.action.BOOT_COMPLETED");
        // Start the service
        ServiceManager serviceManager = new ServiceManager(context);
        serviceManager.startService();
    }

}
