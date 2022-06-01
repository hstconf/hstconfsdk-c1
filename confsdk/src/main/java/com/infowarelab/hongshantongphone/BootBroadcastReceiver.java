package com.infowarelab.hongshantongphone;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.infowarelab.conference.ui.activity.LogoActivity;

public class BootBroadcastReceiver extends BroadcastReceiver {

    static final String action_boot ="android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("InfowareLab.Debug", ">>>>>>BootBroadcastReceiver:");
        //PublicWay.getIpAndPort(context.getApplicationContext(), Config.Site_URL, FileUtil.readSharedPreferences(context,
        //        Constants.SHARED_PREFERENCES, Constants.SITE_ID));

//        Intent intent2 = new Intent(context, LogoActivity.class);
//        context.startActivity(intent2);

//        String packageName = "com.infowarelab.hongshantongphone";
//        Intent intent2 = context.getPackageManager().getLaunchIntentForPackage(packageName);
//        context.startActivity(intent2);

        if (intent.getAction().equals(action_boot)) {
            // 开机启动的Activity
            Intent activityIntent = new Intent(context, LogoActivity.class);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // 启动Activity
            context.startActivity(activityIntent);
        }

        // 开机启动的Service
//        Intent serviceIntent = new Intent(context, StartOnBootService.class);
//        // 启动Service
//        context.startService(serviceIntent);

    }
}
