package com.infowarelab.conference.ui.notice;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

////import org.apache.log4j.Logger;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.infowarelabsdk.conference.util.Constants;

/**
 * @author Jack.Yan@infowarelab.com
 * @description Service that continues to run in background and respond to the push
 * @date 2012-11-26
 */
public class NotificationService extends Service {

    //private final Logger log = Logger.getLogger(getClass());

    private TelephonyManager telephonyManager;

    //    private WifiManager wifiManager;
    //
    //    private ConnectivityManager connectivityManager;

    private BroadcastReceiver notificationReceiver;

    private BroadcastReceiver connectivityReceiver;

    private PhoneStateListener phoneStateListener;

    private ExecutorService executorService;

    private TaskSubmitter taskSubmitter;

    private TaskTracker taskTracker;

    private XmppManager xmppManager;

    public void setNetAvailable(boolean isNetAvailable) {
        xmppManager.setNetAvailable(isNetAvailable);
    }

    public NotificationService() {
        notificationReceiver = new NoticeReceiver();
        connectivityReceiver = new ConnectivityReceiver(this);
        phoneStateListener = new PhoneStateChangeListener(this);
        executorService = Executors.newSingleThreadExecutor();
        taskSubmitter = new TaskSubmitter(this);
        taskTracker = new TaskTracker(this);
    }

    @Override
    public void onCreate() {
        //log.info("onCreate()...");
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        // wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        // connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        xmppManager = new XmppManager(this);
        NotificationService.this.start();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        //log.info("onStart()...");
    }

    @Override
    public void onDestroy() {
        //log.info("onDestroy()...");
        stop();
    }

    @Override
    public IBinder onBind(Intent intent) {
        //log.info("onBind()...");
        return null;
    }

    @Override
    public void onRebind(Intent intent) {
        //log.info("onRebind()...");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //log.info("onUnbind()...");
        return true;
    }

    public static Intent getIntent() {
        return new Intent(Constants.XMPP_PUSH_ACTION);
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public TaskSubmitter getTaskSubmitter() {
        return taskSubmitter;
    }

    public TaskTracker getTaskTracker() {
        return taskTracker;
    }

    public XmppManager getXmppManager() {
        return xmppManager;
    }

    public void connect() {
        //log.info("connect()...");
        NotificationService.this.getXmppManager().connect();
    }

    public void disconnect() {
        //log.info("disconnect()...");
        NotificationService.this.getXmppManager().disconnect();
    }

    private void registerNotificationReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.XMPP_PUSH_ACTION);
        registerReceiver(notificationReceiver, filter);
    }

    private void unregisterNotificationReceiver() {
        unregisterReceiver(notificationReceiver);
    }

    private void registerConnectivityReceiver() {
        //log.info("registerConnectivityReceiver()...");
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(android.net.ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectivityReceiver, filter);
    }

    private void unregisterConnectivityReceiver() {
        //log.info("unregisterConnectivityReceiver()...");
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_NONE);
        unregisterReceiver(connectivityReceiver);
    }

    private void start() {
        //log.info("start()...");
        registerNotificationReceiver();
        registerConnectivityReceiver();
        xmppManager.connect();
    }

    private void stop() {
        //log.info("stop()...");
        unregisterNotificationReceiver();
        unregisterConnectivityReceiver();
        xmppManager.disconnect();
        executorService.shutdown();
    }

    /**
     * Class for summit a new runnable task.
     */
    public class TaskSubmitter {

        final NotificationService notificationService;

        public TaskSubmitter(NotificationService notificationService) {
            this.notificationService = notificationService;
        }

        @SuppressWarnings("rawtypes")
        public Future submit(Runnable task) {
            Future result = null;
            if (!notificationService.getExecutorService().isTerminated()
                    && !notificationService.getExecutorService().isShutdown()
                    && task != null) {
                result = notificationService.getExecutorService().submit(task);
            }
            return result;
        }

    }

    /**
     * Class for monitoring the running task count.
     */
    public class TaskTracker {

        final NotificationService notificationService;

        public int count;

        public TaskTracker(NotificationService notificationService) {
            this.notificationService = notificationService;
            this.count = 0;
        }

        public void increase() {
            synchronized (notificationService.getTaskTracker()) {
                notificationService.getTaskTracker().count++;
                //log.info("Incremented task count to " + count);
            }
        }

        public void decrease() {
            synchronized (notificationService.getTaskTracker()) {
                notificationService.getTaskTracker().count--;
                //log.info("Decremented task count to " + count);
            }
        }

    }

}
