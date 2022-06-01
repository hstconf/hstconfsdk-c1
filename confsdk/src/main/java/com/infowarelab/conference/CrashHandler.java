package com.infowarelab.conference;

////import org.apache.log4j.Logger;

import com.infowarelab.conference.ui.activity.LogoActivity;
import com.infowarelab.hongshantongphone.R;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.os.SystemClock;
import android.widget.Toast;

/**
 * 接收系统不能处理的异常并提示
 *
 * @author joe.xiao
 * @Date 2013-9-6下午4:27:03
 * @Email joe.xiao@infowarelab.com
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static CrashHandler instance;

    private Thread.UncaughtExceptionHandler unCaughException;

    private Context mContext;

    //private static final Logger log = Logger.getLogger(CrashHandler.class);

    private CrashHandler() {
        unCaughException = Thread.getDefaultUncaughtExceptionHandler();
    }

    public static CrashHandler getInstance() {
        if (instance == null) {
            instance = new CrashHandler();
        }
        return instance;
    }

    public void init(Context context) {
        this.mContext = context;
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        //log.error(thread.getName(), ex);
        showMessage();
//		SystemClock.sleep(2000);

        Intent intent = new Intent(mContext.getApplicationContext(), LogoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent restartIntent = PendingIntent.getActivity(
                mContext.getApplicationContext(), 0, intent,
                0);
        //退出程序                                          
        AlarmManager mgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 500,
                restartIntent);

        android.os.Process.killProcess(android.os.Process.myPid());

    }

    private void showMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(mContext, mContext.getString(R.string.crashMeessage), Toast.LENGTH_LONG).show();
                Looper.loop();
            }
        }).start();
    }

}
