package com.infowarelab.conference.ui.notice;
////import org.apache.log4j.Logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * @author Jack.Yan@infowarelab.com
 * @description A broadcast receiver to handle the changes in network connection states.
 * @date 2012-11-26
 */
public class ConnectivityReceiver extends BroadcastReceiver {

    //private final Logger log = Logger.getLogger(getClass());

    private NotificationService notificationService;

    public ConnectivityReceiver(NotificationService notificationService) {

        this.notificationService = notificationService;
    }

    public ConnectivityReceiver() {
        //log.info("empty constructor");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //log.info("ConnectivityReceiver.onReceive()...");
        String action = intent.getAction();
        //log.info("action=" + action);

        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null) {
            //log.info("Network Type  = " + networkInfo.getTypeName());
            //log.info("Network State = " + networkInfo.getState());
            if (networkInfo.isConnected()) {
                //log.info("Network connected");
                notificationService.setNetAvailable(true);
                notificationService.connect();
            }
        } else {
            //log.error("Network unavailable");
            notificationService.setNetAvailable(false);
            notificationService.disconnect();
        }
    }

}
