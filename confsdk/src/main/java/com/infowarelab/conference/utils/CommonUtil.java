package com.infowarelab.conference.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

//用ActivityManager管理的Activity栈来操作
public class CommonUtil{
    /**
     * 判断某个Activity 界面是否在前台
     * @param context
     * @param className 某个界面名称
     * @return
     */
//    public static boolean isForeground(Context context, String className) {
//        if (context == null || TextUtils.isEmpty(className)) {
//            return false;
//        }
//        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//        List list = am.getRunningTasks(1);
//        if (list != null && list.size() > 0) {
//            ComponentName cpn = list.get(0).getTopActivityComponent();
//            if (className.equals(cpn.getClassName())) {
//                return true;
//            }
//        }
//        return false;
//    }

    public static void moveToForeground(Context context, final Class Class){
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        //利用系统方法获取当前Task堆栈, 数目可按实际情况来规划，这里只是演示
        List<ActivityManager.RunningTaskInfo> taskInfoList = activityManager.getRunningTasks(30);

        for (ActivityManager.RunningTaskInfo taskInfo : taskInfoList) {
            //遍历找到本应用的 task，并将它切换到前台
            if (taskInfo.baseActivity.getPackageName().equals(context.getPackageName())) {
                Log.d("InfowareLab.Debug", "timerTask  pid " + taskInfo.id);
                Log.d("InfowareLab.Debug", "timerTask  processName " + taskInfo.topActivity.getPackageName());
                Log.d("InfowareLab.Debug", "timerTask  getPackageName " + context.getPackageName());
                activityManager.moveTaskToFront(taskInfo.id, ActivityManager.MOVE_TASK_WITH_HOME);
                Intent intent = new Intent(context, Class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                context.startActivity(intent);
                break;
            }
        }
    }

    public static boolean appOnForeground(Context context) {

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = context.getPackageName();
        
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();

        if (appProcesses == null)
            return false;

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(packageName) && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {


                return true;
            }
        }

        return false;
    }
}