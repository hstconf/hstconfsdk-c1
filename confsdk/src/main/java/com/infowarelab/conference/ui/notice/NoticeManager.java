package com.infowarelab.conference.ui.notice;


////import org.apache.log4j.Logger;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.infowarelab.hongshantongphone.R;
import com.infowarelab.conference.ui.activity.LogoActivity;
import com.infowarelabsdk.conference.util.Constants;

/**
 * @author Jack.Yan@infowarelab.com
 * @description This is Notice manager
 * @date 2012-11-9
 */
public class NoticeManager {
    //private final Logger log = Logger.getLogger(getClass());
    private static Context context;
    private AlarmManager alarmManager;
    private static NoticeManager dialogManager;

    public static final String ACTION_SHOW_DIALOG = "action_show_dialog";
    private int icon = R.drawable.icon;

//	public static long bufferTime=500;//used for fix system runtime error

    public static int notificationId;

    public static boolean isDelay = true;

    public NoticeManager() {
    }

    @SuppressWarnings("static-access")
    public NoticeManager(Context context) {
        this.context = context;
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public static NoticeManager getInstance(Context context) {
        if (dialogManager == null)
            dialogManager = new NoticeManager(context);
        return dialogManager;
    }

    //	public void receiveMessage(MessageBean message){
//		Log.d("DialogManager", "receiveMessage...");
//		Intent intent=new Intent();
//		intent.putExtra(MessageBean.TAG_MESSAGE, message);
//		showDialog(getPendingIntent(intent),0);//Show the dialog
//	}
    public void pushMessage(PendingIntent pendingIntent, long when) {
        alarmManager.set(AlarmManager.RTC_WAKEUP, when, pendingIntent);
    }

    public void cancelPush(PendingIntent pendingIntent) {
        alarmManager.cancel(pendingIntent);
        //log.info("closed dialog");
    }

    public PendingIntent getPendingIntent(Intent intent, int requestId) {
        intent.setClass(context, DialogReceiver.class);
        return PendingIntent.getBroadcast(context, requestId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void doDialog(Context context, Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setAction(ACTION_SHOW_DIALOG);
        intent.setClass(context, DialogActivity.class);
        context.startActivity(intent);
    }

    public void doNotification(Context context, Intent intent) {
        ReceiverMessage receiverMessage = ((ReceiverMessage) intent.getSerializableExtra(Constants.TAG_NOTIFICATION));
        String confMessage = receiverMessage.getConfMessage();
        try {
            notificationId = Integer.parseInt(receiverMessage.getConfKey());
        } catch (Exception e) {
            //log.error("ConfKey parseInt error, use '0' as default value");
            notificationId = 0;
        }
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(icon, confMessage, System.currentTimeMillis());
        notification.flags |= Notification.FLAG_AUTO_CANCEL;// auto cancel after one shot
        notification.defaults |= Notification.DEFAULT_SOUND;//play system sound
        notification.defaults |= Notification.DEFAULT_VIBRATE;//play system vibrate
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notice_notification);
        remoteViews.setTextViewText(R.id.notification_body, confMessage);
        intent.setClass(context, LogoActivity.class);// exchange to LogoActivity
        PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.contentView = remoteViews;
        notification.contentIntent = pendingIntent;
        notificationManager.notify(notificationId, notification);
        //log.info("showed notification");
    }

    public void cancelAllNotification() {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

}
