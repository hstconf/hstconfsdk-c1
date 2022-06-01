package com.infowarelab.conference;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.view.ViewCompat;

import com.tencent.bugly.crashreport.CrashReport;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.UMShareAPI;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 用于点击home键之后重新返回会议界面
 *
 * @author Sean.xie
 */
public class ConferenceApplication extends Application {

    private static final String TAG = "FloatWindow";

    public static boolean isVideoMeeting = false;
    public static int SCREEN_WIDTH = 0;
    public static int SCREEN_HEIGHT = 0;
    public static float DENSITY = 0;
    public static Handler mConfIdHandler = null;

    public static int Screen_W = 0;
    public static int Screen_H = 0;
    public static int Root_W = 0;
    public static int Root_H = 0;
    public static int StateBar_H = 0;
    public static int NavigationBar_H = 0;
    public static boolean Keep_16_9 = false;

    public static int MainWindow_W = 0;
    public static int MainWindow_H = 0;

    public static int SUB_VIDEO_W = 0;
    public static int SUB_VIDEO_H = 0;

    public static int MAIN_VIDEO_W = 0;
    public static int MAIN_VIDEO_H = 0;

    public static int Top_H = 0;
    public static int Bottom_H = 0;

    public static final int EDITION_PHONE = 2;
    public static int EDITION = EDITION_PHONE;

    public static boolean isJoinConf = false;
    public static int LEFT_MARGIN = -1;
    public static int TOP_MARGIN = -1;
    public static int localVideoWidth = 0;
    public static int localVideoHeight = 0;
    public static boolean isConfJoined = false;
    public static boolean isConfActivityLaunched = false;
    public static String mConfId;

    private static ConferenceApplication conferenceApp;

    public static ConferenceApplication getConferenceApp() {
        if (conferenceApp == null) {
            return conferenceApp = new ConferenceApplication();
        }
        return conferenceApp;
    }


    //是否加入会议标志
    private boolean joined = false;

    private boolean entered = false;

    private Map<String, String> field = new HashMap<String, String>();

    //开始会议flag
    private boolean startMeeting = false;

    private boolean isHost = true;

    /**
     * 版本
     */
    private String version;
    /**
     * SDK模式
     */
    private boolean sdkMode = true;
    /**
     * 分辨率
     */
    private String resolution;
    /**
     * 操作系统
     */
    private String os;
    /**
     * 更新类型
     */
    private int update; // 0 not need ;1 must update 2 can update
    /**
     * 更新地址
     */
    private String updateUrl;
    /**
     * 更新包名称
     */
    private String apkName;

    @Override
    public void onCreate() {
        super.onCreate();
        //initCrashHandler();
        //initUMSocial();
        //ImageView imageView = new ImageView(getApplicationContext());
        //imageView.setImageResource(R.drawable.floating_icon);

//        imageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
        // 获取当前包名
        String packageName = getApplicationContext().getPackageName();
        // 获取当前进程名
        String processName = getProcessName(android.os.Process.myPid());
        // 设置是否为上报进程
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(getApplicationContext());
        strategy.setUploadProcess(processName == null || processName.equals(packageName));
        // 初始化Bugly
        CrashReport.initCrashReport(getApplicationContext(), "9676362194", true, strategy);

    }

    /**
     * 获取进程号对应的进程名
     *
     * @param pid 进程号
     * @return 进程名
     */
    private static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }


    public static void setStatusBarColor(Activity activity, int statusColor) {
        Window window = activity.getWindow();
        //取消状态栏透明
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //添加Flag把状态栏设为可绘制模式
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //设置状态栏颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(statusColor);
        }
        //设置系统状态栏处于可见状态
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        //让view不根据系统窗口来调整自己的布局
        ViewGroup mContentView = (ViewGroup) window.findViewById(Window.ID_ANDROID_CONTENT);
        View mChildView = mContentView.getChildAt(0);
        if (mChildView != null) {
            ViewCompat.setFitsSystemWindows(mChildView, false);
            ViewCompat.requestApplyInsets(mChildView);
        }
    }

    public int getWindowWidth(int orientation) {
        if (ORIENTATION_LANDSCAPE == orientation) {
            if (LEFT_MARGIN >= 0)
                return ConferenceApplication.Screen_H - ConferenceApplication.LEFT_MARGIN - ConferenceApplication.NavigationBar_H;
            else
                return ConferenceApplication.Screen_H - ConferenceApplication.NavigationBar_H - ConferenceApplication.StateBar_H;
        } else
            return ConferenceApplication.Screen_W;
    }

    public int getWindowHeight(int orientation) {
        if (ORIENTATION_LANDSCAPE == orientation) {
            if (TOP_MARGIN >= 0)
                return ConferenceApplication.Screen_W - ConferenceApplication.TOP_MARGIN;
            else
                return ConferenceApplication.Screen_W - ConferenceApplication.StateBar_H;
        } else {
            if (TOP_MARGIN >= 0)
                return ConferenceApplication.Screen_H - ConferenceApplication.TOP_MARGIN - ConferenceApplication.NavigationBar_H;
            else
                return ConferenceApplication.Screen_H - ConferenceApplication.NavigationBar_H - ConferenceApplication.StateBar_H;

        }
    }

    private void initUMSocial() {
        UMShareAPI.get(this);
        PlatformConfig.setWeixin("wx2017efaaa680d714", "ae08f2143151176991cb687f3ef1c147");
        PlatformConfig.setQQZone("1104660765", "jD0TjQpWBtHd9v1q");
//	     PlatformConfig.setSinaWeibo("3921700954", "04b48b094faeb16683c32669824ebdad", "http://sns.whalecloud.com");
        PlatformConfig.setDing("dingoafikcirrde2bnzs32");
    }


    private void initCrashHandler() {
        CrashHandler.getInstance().init(getApplicationContext());
    }

    public boolean isJoined() {
        return joined;
    }

    public void setJoined(boolean joined) {
        this.joined = joined;
    }

    public boolean isEntered() {
        return entered;
    }

    public void setEntered(boolean entered) {
        this.entered = entered;
    }

    public void putField(String key, String value) {
        field.put(key, value);
    }

    public String getFieldValue(String key) {
        return field.get(key);
    }

    public Set<String> getFieldKeys() {
        return field.keySet();
    }

    public boolean isStartMeeting() {
        return startMeeting;
    }

    public void setStartMeeting(boolean startMeeting) {
        this.startMeeting = startMeeting;
    }

    public boolean isHost() {
        return isHost;
    }

    public void setHost(boolean isHost) {
        this.isHost = isHost;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean getSdkMode() {
        return sdkMode;
    }

    public void setSdkMode(boolean mode) {
        this.sdkMode = mode;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public int getUpdate() {
        return update;
    }

    public void setUpdate(int update) {
        this.update = update;
    }

    public String getUpdateUrl() {
        return updateUrl;
    }

    public void setUpdateUrl(String updateUrl) {
        this.updateUrl = updateUrl;
    }

    public String getApkName() {
        return apkName;
    }

    public void setApkName(String apkName) {
        this.apkName = apkName;
    }

    public String getFilePath(String folder) {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory() + File.separator + "infowarelab" + File.separator + folder;
        } else {
            return getCacheDir() + File.separator + "infowarelab" + File.separator + folder;
        }
    }

    public String getFilePath() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory() + File.separator + "infowarelab";
        } else {
            return getCacheDir() + File.separator + "infowarelab";
        }
    }

    public void setScreenInfo(int sw, int sh, int rw, int rh, int stateh, int keyh, float d) {
        SCREEN_WIDTH = sw;
        SCREEN_HEIGHT = sh;
        Root_W = rw;
        Root_H = rh;
        StateBar_H = stateh;
        NavigationBar_H = keyh;
        DENSITY = d;
    }


}
