package com.infowarelab.conference.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.infowarelab.conference.ui.activity.ActCustomDialog;
import com.infowarelab.conference.ui.activity.LogoActivity;
import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.util.Constants;
import com.infowarelabsdk.conference.util.FileUtil;
import com.infowarelabsdk.conference.util.MessageEvent;
import com.infowarelabsdk.conference.util.NetUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by xiaor on 2019/11/18.
 *
 * @author Ice
 * TCP Socket通信
 */

public class SocketService extends Service {

    private Notification mNotification;
    private int mNotifyCount = 0;

    /**
     * Start service or Stop service.
     */
    public enum Actions {
        START,
        STOP
    }

    /**
     * Initialize PowerManager.WakeLock
     * So that our service will not be impacted by Doze Mode.
     */
    private PowerManager.WakeLock wakeLock = null;

    /**
     * Boolean if our service is started or not.
     */
    public static boolean isServiceStarted = false;

    /*socket*/
    private Socket socket;

    /*连接线程*/
    private Thread connectThread;
    private Thread receiveThread;

    private Timer timer = new Timer();
    private OutputStream outputStream;

    private SocketBinder socketBinder = new SocketBinder();

    private TimerTask task;
    /*默认重连*/
    public boolean isReConnect = true;

    //是否处于链接状态
//    public boolean isConnect = false;
//    //连接次数
//    public int count = 0;

    /**
     * 读写输入流
     */
    private InputStream inputStream;
    private DataInputStream dis;

    /*线程状态,安全结束线程*/
    private boolean threadStatus = false;
    /*读取保存进数组*/
    byte buff[] = new byte[1024 * 1024 * 2];
    /*接收数据长度*/
    private int rcvLength;
    /*接收数据*/
    private String rcvMsg;

    private Handler handler = new Handler(Looper.getMainLooper());

    public boolean bReleaseSocket = false;
    private Lock socketlock = new ReentrantLock();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //Log.e("Test","onBind====>>>>");
        return socketBinder;
    }

    public class SocketBinder extends Binder {
        /*返回SocketService 在需要的地方可以通过ServiceConnection获取到SocketService  */
        public SocketService getService() {
            return SocketService.this;
        }
    }

    public void notifyInvited(String title, String content)
    {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(1, createRingNotification(title, content));
    }

    public void resetNotifyInvited()
    {
        mNotifyCount = 0;

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(1, createNotification());
    }

    /**
     * Method to create the notification show to the user.
     *
     * @return Notification with all params.
     */
    private Notification createNotification() {
        String notificationChannelId = "InfowareLab conference channel";

        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(
                    notificationChannelId,
                    "InfowareLab conference channel",
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setVibrationPattern(new long[]{0});
            channel.setSound(null, null);

            channel.setDescription("InfowareLab conference channel");
            //channel.enableLights(false);
            //channel.setLightColor(Color.RED);
            //channel.enableVibration(false);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(
                    this,
                    notificationChannelId
            );
        } else {
            builder = new Notification.Builder(this);
        }

        Intent intent = new Intent(getApplicationContext(), LogoActivity.class);
        PendingIntent backIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent,0);

//        Intent fullscreenIntent = new Intent(getApplicationContext(), ActCustomDialog.class);
//        PendingIntent fullscreenBackIntent = PendingIntent.getActivity(getApplicationContext(), 1, fullscreenIntent,0);

        return builder
                .setContentTitle("视频会议")
                .setContentText("红杉通视频会议系统正在为您服务。")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.icon)
                .setTicker("视频会议")
                //.setFullScreenIntent(fullscreenBackIntent, true)
                .setContentIntent(backIntent)
                .setOngoing(true)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setVibrate(new long[]{0l})
                .setSound(null)
                .setPriority(Notification.PRIORITY_MAX) // for under android 26 compatibility
                .build();

    }

    private Notification createRingNotification(String title, String context) {
        String notificationChannelId = "InfowareLab_Ring_Channel_" + mNotifyCount;

        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(
                    notificationChannelId,
                    "InfowareLab_Ring_Channel_" + mNotifyCount,
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLightColor(Color.RED);
            //channel.setVibrationPattern(new long[]{0});
            //channel.setSound(null, null);

            channel.setDescription("InfowareLab_Ring_Channel_Description_" + mNotifyCount);
            //channel.enableLights(false);
            //channel.setLightColor(Color.RED);
            //channel.enableVibration(false);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(
                    this,
                    notificationChannelId
            );
        } else {
            builder = new Notification.Builder(this);
        }

        Intent intent = new Intent(getApplicationContext(), LogoActivity.class);
        PendingIntent backIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent,0);

//        Intent fullscreenIntent = new Intent(getApplicationContext(), ActCustomDialog.class);
//        PendingIntent fullscreenBackIntent = PendingIntent.getActivity(getApplicationContext(), 1, fullscreenIntent,0);

        mNotifyCount++;

        context += "(";
        context += mNotifyCount;
        context += ")";

        return builder
                .setContentTitle(title)
                .setContentText(context)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.icon)
                .setTicker(title)
                //.setFullScreenIntent(fullscreenBackIntent, true)
                .setContentIntent(backIntent)
                .setOngoing(true)
                //.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                //.setVibrate(new long[]{0l})
                //.setSound(null)
                .setPriority(Notification.PRIORITY_MAX) // for under android 26 compatibility
                .build();

    }

    @Override
    public void onCreate() {

        Log.d("InfowareLab.Socket", "The service has been created".toUpperCase());

        mNotification = createNotification();
        startForeground(1, mNotification);

        super.onCreate();

    }

    /**
     * Method executed when the service is running.
     */
    private void startService() {

        // If the service already running, do nothing.
        if (isServiceStarted) return;

        Log.d("InfowareLab.Socket", "Starting the foreground service task");

        isServiceStarted = true;

        // we need this lock so our service gets not affected by Doze Mode
        PowerManager pm = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pm = (PowerManager) getSystemService(getApplicationContext().POWER_SERVICE);

            if (pm != null) {
                wakeLock = pm.newWakeLock(1, "EndlessService::lock");
                wakeLock.acquire(60 * 1000L /*1 minutes*/);
            }
        }

        initSocket();
    }

    /**
     * Method executed to stop the running service.
     */
    private void stopService() {
        Log.d("InfowareLab.Debug", "Stopping the foreground service");
        //Toast.makeText(this, "Service stopping", Toast.LENGTH_SHORT).show();
        try {
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
            releaseSocket();
            stopForeground(true);
            stopSelf();
        } catch (Exception e) {
            Log.d("InfowareLab.Socket", "Service stopped without being started: ${e.message}");
        }
        isServiceStarted = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("InfowareLab.Debug", "onStartCommand executed with startId: " + startId);
        if (intent != null) {
            String action = intent.getAction();
            Log.d("InfowareLab.Socket", "using an intent with action " + action);
            if (action != null) {
                if (action.equals(Actions.START.name())) startService();
                else if (action.equals(Actions.STOP.name())) stopService();
                else
                    Log.d("InfowareLab.Socket", "This should never happen. No action in the received intent");
            }
        } else {
            Log.d("InfowareLab.Socket", "with a null intent. It has been probably restarted by the system.");
        }


        // by returning this we make sure the service is restarted if the system kills the service
        return START_STICKY;

        //return super.onStartCommand(intent, flags, startId);
    }

    /*初始化socket*/
    private void initSocket() {
        if (null == socket && null == connectThread) {
            connectThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    socket = new Socket();
                    boolean bReconnect = false;
                    try {
                        /*超时时间为2秒*/
                        Log.d("InfowareLab.Socket", "Connect ip:" + PublicWay.IP + "; port:" + PublicWay.PORT);
                        socket.setKeepAlive(true);
                        socket.setTcpNoDelay(true);
                        socket.connect(new InetSocketAddress(PublicWay.IP, PublicWay.PORT), 2 * 1000);
                        socketlock.lock();
                        bReleaseSocket = false;
                        socketlock.unlock();
                        /*连接成功的话  发送心跳包*/
                        if (socket.isConnected()) {
                            Log.d("InfowareLab.Socket", "Connected!");
                            /*因为Toast是要运行在主线程的  这里是子线程  所以需要到主线程哪里去显示toast*/
                            inputStream = socket.getInputStream();
                            dis = new DataInputStream(inputStream);
//                            toastMsg("服务已连接");
                            /*开启读写线程*/
                            threadStatus = true;
                            new ReadThread().start();
//                           /*发送心跳数据*/
                            sendBeatData();
                            //发条消息
                            if (!PublicWay.isConnect) {
                                PublicWay.isConnect = true;
                                MessageEvent messageEvent = new MessageEvent();
                                messageEvent.setType(6);
                                messageEvent.setSocketConnect(true);
                                EventBus.getDefault().postSticky(messageEvent);
                            }
                        } else {
                            bReconnect = true;
                        }
                    } catch (IOException e) {
                        if (PublicWay.terminalsData.size() != 0) {
                            PublicWay.terminalsData.clear();
                        }
                        bReconnect = true;
                        socketlock.lock();
                        bReleaseSocket = false;
                        socketlock.unlock();
                        e.printStackTrace();
                        if (e instanceof SocketTimeoutException) {
//                            if (count == 4){
                            toastMsg("连接超时,暂停连接");
//                            }
//                            toastMsg("连接超时，正在重连");
                        } else if (e instanceof NoRouteToHostException) {
                            toastMsg("该地址不存在，请检查");
//                            stopSelf();
                        } else if (e instanceof ConnectException) {
                            if (NetUtil.isNetworkConnected(getApplicationContext())) {
                                toastMsg("连接异常或被拒绝，正在重连");
                            }
                        }
                    }

                    //Log.d("InfowareLab.Socket", "Disconnected");
                    //延迟60s重连
                    try {
                        connectThread.sleep(10 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (bReconnect) {
                        //发条消息
//                        if (PublicWay.isConnect && count == 4){
                        if (PublicWay.isConnect) {
                            PublicWay.isConnect = false;
                            MessageEvent messageEvent = new MessageEvent();
                            messageEvent.setType(6);
                            messageEvent.setSocketConnect(false);
                            EventBus.getDefault().postSticky(messageEvent);
                        }
//                        count ++;
                        threadStatus = false;
                        isReConnect = true;
                        releaseSocket();
                    }
                }
            });
            /*启动连接线程*/
            connectThread.start();
        } else {
            socketlock.lock();
            bReleaseSocket = false;
            socketlock.unlock();
        }
    }

    /*因为Toast是要运行在主线程的   所以需要到主线程哪里去显示toast*/
    private void toastMsg(final String msg) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, 2 * 1000).show();
            }
        });
    }

    /*发送数据*/
    public void sendOrder(final String order) {
        if (socket != null && socket.isConnected()) {
            Log.d("InfowareLab.Debug", "sendOrder: " + order);
            /*发送指令*/
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        outputStream = socket.getOutputStream();
                        if (outputStream != null) {
                            outputStream.write((order).getBytes("UTF-8"));
                            outputStream.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }).start();
        } else {
//            toastMsg("socket连接错误,请重试");
        }
    }

    /*定时发送数据*/
    private void sendBeatData() {
        if (timer == null) {
            timer = new Timer();
        }
        if (task == null) {
            task = new TimerTask() {
                @Override
                public void run() {
                    try {
                        if (!socket.isConnected()) {
                            Log.d("InfowareLab.Debug", "Ping: DISCONNECTED!");
                        }
                        String RequestID = "" + System.currentTimeMillis();
                        String name = FileUtil.readSharedPreferences(getApplicationContext(), Constants.SHARED_PREFERENCES,
                                Constants.LOGIN_NAME);
                        if (TextUtils.isEmpty(name)) {
                            name = "NO_NAME";
                        }
                        int uid = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFERENCES,
                                getApplicationContext().MODE_PRIVATE).getInt(Constants.USER_ID, 0);

                        if (name == null || name.length() <= 0 || uid <= 0)
                        {
                            Log.d("InfowareLab.Debug", "Ping: DISCONNECTED AS NOT LOGGED IN!");
                            return;
                        }

                        String deviceId = name;//DeviceIdFactory.getUUID1(getApplicationContext());
                        String siteId = FileUtil.readSharedPreferences(getApplicationContext(), Constants.SHARED_PREFERENCES,
                                Constants.SITE_ID);
                        String ping = XMLUtils.getPingXML(RequestID, uid, name, deviceId, siteId, PublicWay.longitude, PublicWay.latitude);
                        //Log.d("InfowareLab.Debug", "Ping: " + ping);
                        //Log.e("Test","ping:"+ping);
                        outputStream = socket.getOutputStream();
                        /*这里的编码方式根据你的需求去改*/
                        outputStream.write((ping).getBytes("UTF-8"));
                        outputStream.flush();
                    } catch (Exception e) {

                        Log.d("InfowareLab.Socket", "Ping: Error" + e.getMessage());

                        /*发送失败说明socket断开了或者出现了其他错误*/
                        if (timer != null) {
                            timer.cancel();
                        }
//                        toastMsg("连接断开，正在重连");
                        /*重连*/
//                        count ++;
                        threadStatus = false;
                        isReConnect = true;
                        releaseSocket();
                        e.printStackTrace();
                    }
                }
            };
        }
        try {
            if (SocketService.isServiceStarted)
                timer.schedule(task, 0, 5 * 1000);
        }
        catch (IllegalStateException exception)
        {

        }
    }


    /*释放资源*/
    public void releaseSocket() {

        socketlock.lock();
        if (bReleaseSocket) {
            socketlock.unlock();
            return;
        }
        bReleaseSocket = true;
        socketlock.unlock();

        Log.d("InfowareLab.Socket", "releaseSocket");

        if (task != null) {
            task.cancel();
            task = null;
        }
        if (timer != null) {
            timer.purge();
            timer.cancel();
            timer = null;
        }
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            outputStream = null;
        }

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
            }
            socket = null;
        }
        if (connectThread != null) {
            connectThread = null;
        }
        /*重新初始化socket*/
        if (isReConnect) {
//            if (count < 5){
            Log.d("InfowareLab.Socket", "重新初始化_IP:" + PublicWay.IP);
            Log.d("InfowareLab.Socket", "重新初始化_端口:" + PublicWay.PORT);
            initSocket();
//            }else {
//                isReConnect = false;
//                stopSelf();
//            }
        }
    }

    String _rcvMsg = "";
    String packEndTag = "</Pack>";

    /*读写线程*/
    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            //判断进程是否在运行，更安全的结束进程
            while (threadStatus && SocketService.isServiceStarted) {
                if (null != inputStream) {
                    try {
                        //inputStream 数据分段传输问题
//                        if (inputStream.available() > 0 == false){
//                            continue;
//                        }else {
//                            sleep(200);
//                        }

                        if (!socket.isConnected() || socket.isInputShutdown()) {
                            Log.d("InfowareLab.Debug", "Socket is shot down in ReadThread !");
                            sleep(100);
                            continue;
                        }

                        rcvLength = dis.read(buff);

                        //Log.d("InfowareLab.Debug", "dis.read: " + rcvLength);

                        if (rcvLength > 0) {
                            for (int i = 0; i < rcvLength; i++) {
                                if ('\n' == buff[i] || '\r' == buff[i]) {
                                    buff[i] = 0x20;
                                }
                            }
                            rcvMsg = new String(buff, 0, rcvLength, "UTF-8");
                            //接收到数据，切换主线程，显示数据
                            rcvMsg = rcvMsg.trim();
                            _rcvMsg = _rcvMsg + rcvMsg;
                            String[] strBuffer = _rcvMsg.split(packEndTag);
                            boolean packEnd = (_rcvMsg.length() > packEndTag.length() && _rcvMsg.lastIndexOf(packEndTag) == _rcvMsg.length() - packEndTag.length());
                            _rcvMsg = "";
                            for (int i = 0; i < strBuffer.length; i++) {
                                if (i < strBuffer.length - 1 || packEnd) {
                                    rcvMsg = strBuffer[i] + packEndTag;
                                    rcvMsg = rcvMsg.trim();
                                    if ((rcvMsg.trim()).indexOf("<Action>request_status</Action>".trim()) != -1)
                                        continue;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (null != onReceiveDataListener && !rcvMsg.trim().contains("request_status")) {
                                                onReceiveDataListener.onReceiveData(rcvMsg);
                                            }
                                        }
                                    });
                                } else {
                                    _rcvMsg = strBuffer[i].trim();
                                }
                            }

                            if (_rcvMsg.length() > 0)
                                Log.d("InfowareLab.Socket", "dis.read: " + _rcvMsg);
                        } else {
                            Log.d("InfowareLab.Debug", "No data read !!! ");
                        }
                    } catch (Exception e) {
                        Log.d("InfowareLab.Debug", "ReadThread exception: " + e.getMessage());

                    }
                }
            }
        }
    }

    @Override
    public void onDestroy() {

        Log.d("InfowareLab.Debug", "The service has been destroyed".toUpperCase());
//        super.onDestroy();
//        Log.i("InfowareLab.Socket", "onDestroy");
//        isReConnect = false;
//        releaseSocket();
        if (timer != null) {
            timer.purge();
            timer.cancel();
            timer = null;
        }
//        Intent service = new Intent(this, SocketService.class);
//        startService(service);

        // 如果Service被杀死，干掉通知
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager mManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            mManager.cancel(1);
        }
//        Log.d("InfowareLab.Socket", "DaemonService---->onDestroy，前台service被杀死");
//        // 重启自己
//        Intent intent = new Intent(getApplicationContext(), SocketService.class);
//        startService(intent);

        super.onDestroy();
    }

    public interface OnReceiveDataListener {
        void onReceiveData(String str);
    }

    public OnReceiveDataListener onReceiveDataListener;

    public OnReceiveDataListener getOnReceiveDataListener() {
        return onReceiveDataListener;
    }

    public void setOnReceiveDataListener(OnReceiveDataListener onReceiveDataListener) {
        this.onReceiveDataListener = onReceiveDataListener;
    }
}
