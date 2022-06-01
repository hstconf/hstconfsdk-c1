package com.infowarelab.conference.ui.notice;

import java.util.List;
import java.util.Properties;

////import org.apache.log4j.Logger;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

import com.infowarelabsdk.conference.transfer.Config;
import com.infowarelabsdk.conference.util.Constants;
import com.infowarelabsdk.conference.util.FileUtil;

/**
 * @author Jack.Yan@infowarelab.com
 * @description This class is to manage the notificatin service and to load the configuration.
 * @date 2012-11-26
 */
public final class ServiceManager {

    private static ActivityManager mActivityManager;
    private static List<ActivityManager.RunningServiceInfo> mServiceList;
    private static final String notificationServiceClassName = "com.infowarelab.conference.ui4hd.notice.NotificationService";

    //private final Logger log = Logger.getLogger(getClass());

    private Context context;


    private Properties props;


    private String mark;

    private String xmppHost;

    private String xmppPort;

    public ServiceManager(Context context) {
        this.context = context;

        props = loadProperties();
//        xmppHost = props.getProperty("xmppHost", "127.0.0.1");
        xmppHost = Config.Site_URL;
        xmppPort = props.getProperty("xmppPort", "5222");
        mark = props.getProperty("mark", "android");//TODO need exchange to 
        //log.info("xmppHost=" + xmppHost);
        //log.info("xmppPort=" + xmppPort);
        //log.info("mark=" + mark);

        FileUtil.saveSharedPreferences(this.context, Constants.SHARED_PREFERENCES, Constants.XMPP_HOST, xmppHost);
        FileUtil.saveSharedPreferences(this.context, Constants.SHARED_PREFERENCES, Constants.XMPP_PORT, xmppPort);
        FileUtil.saveSharedPreferences(this.context, Constants.SHARED_PREFERENCES, Constants.XMPP_MARK, mark);


        mActivityManager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        mServiceList = mActivityManager.getRunningServices(30);
    }

    public void startService() {
        Thread serviceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                context.startService(new Intent(context, NotificationService.class));
                //log.info("NotificationService is started!");
            }
        });
        serviceThread.start();
    }

    public void stopService() {
        Intent intent = NotificationService.getIntent();
        context.stopService(intent);
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        try {
            int id = context.getResources().getIdentifier("infowarelab", "raw",
                    context.getPackageName());
            props.load(context.getResources().openRawResource(id));
        } catch (Exception e) {
            //log.error("Could not find the properties file.", e);
            // e.printStackTrace();
        }
        return props;
    }

    public boolean isServiceStarted() {
        boolean result = false;
        for (int i = 0; i < mServiceList.size(); i++) {
            if (notificationServiceClassName.equals(mServiceList.get(i).service.getClassName())) {
                result = true;
            }
        }
        return result;
    }
}
