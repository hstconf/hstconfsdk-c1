package com.infowarelab.conference.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.infowarelab.conference.ui.activity.ActCustomDialog;
import com.infowarelab.conference.ui.activity.LogoActivity;
import com.infowarelab.hongshantongphone.R;

public class NotificationUtils extends ContextWrapper {
    public static final String TAG = NotificationUtils.class.getSimpleName();

    public static final String id = "infowarelab_channel_1";
    public static final String name = "notification";
    private NotificationManager manager;
    private Context mContext;

    public NotificationUtils(Context base) {
        super(base);
        mContext = base;
    }

    @RequiresApi(api = 26)
    public void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH);
        getManager().createNotificationChannel(channel);
    }

    private NotificationManager getManager() {
        if (manager == null) {
            manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        return manager;
    }

    public void sendNotificationFullScreen( String title, String content, String type, boolean fullscreen) {
        if (Build.VERSION.SDK_INT >= 26) {
            createNotificationChannel();
            Notification notification;

            if (fullscreen)
                notification = getChannelNotificationQ
                        (title, content, type);
            else
                notification = getChannelNotificationNormal
                        (title, content, type);

            getManager().notify(1, notification);
        }
    }

    public void clearAllNotifiication(){
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
        notificationManager.cancelAll();
    }

    public  Notification getChannelNotificationQ(String title, String content, String type) {
        Intent fullScreenIntent = new Intent(this, ActCustomDialog.class);
        fullScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        fullScreenIntent.putExtra("action", "callfromdevice");
        fullScreenIntent.putExtra("type", type);
        fullScreenIntent.putExtra("title", title);
        fullScreenIntent.putExtra("content", content);

        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this, 1, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, id)
                        .setSmallIcon(R.drawable.icon)
                        .setContentTitle(title)
                        .setTicker(content)
                        .setContentText(content)
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setCategory(Notification.CATEGORY_CALL)
                        .setFullScreenIntent(fullScreenPendingIntent,true);

        Notification incomingCallNotification = notificationBuilder.build();
        return incomingCallNotification;
    }

    public  Notification getChannelNotificationNormal(String title, String content, String type) {
        Intent intent = new Intent(getApplicationContext(), LogoActivity.class);
        PendingIntent backIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent,0);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, id)
                        .setSmallIcon(R.drawable.icon)
                        .setContentTitle("视频会议")
                        .setContentText("红杉通视频会议系统正在为您服务。")
                        .setTicker(content)
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setCategory(Notification.CATEGORY_CALL)
                        .setOngoing(true)
//                        .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
//                        .setVibrate(new long[]{0l})
//                        .setSound(null)
                        .setPriority(Notification.PRIORITY_MAX)
                        .setContentIntent(backIntent);

        Notification incomingCallNotification = notificationBuilder.build();
        return incomingCallNotification;
    }
}