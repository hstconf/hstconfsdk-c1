package com.infowarelab.conference.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.infowarelab.conference.BaseActivity;
import com.infowarelab.conference.ConferenceApplication;
import com.infowarelab.conference.localDataCommon.LocalCommonFactory;
import com.infowarelab.conference.localDataCommon.impl.ContactDataCommonImpl;
import com.infowarelab.conference.ui.action.JoinConfByIdAction;
import com.infowarelab.conference.ui.activity.inconf.ConferenceActivity;
import com.infowarelab.conference.ui.activity.preconf.ActHome;
import com.infowarelab.conference.ui.activity.preconf.SiteSetupActivity;
import com.infowarelab.conference.ui.activity.preconf.view.ConferencePagerNumber;
import com.infowarelab.conference.ui.error.ErrorMessage;
import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.callback.CallbackManager;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.AudioCommonImpl;
import com.infowarelabsdk.conference.common.impl.ChatCommomImpl;
import com.infowarelabsdk.conference.common.impl.ConferenceCommonImpl;
import com.infowarelabsdk.conference.common.impl.DocCommonImpl;
import com.infowarelabsdk.conference.common.impl.ShareDtCommonImpl;
import com.infowarelabsdk.conference.common.impl.UserCommonImpl;
import com.infowarelabsdk.conference.common.impl.VideoCommonImpl;
import com.infowarelabsdk.conference.confctrl.ConferenceCommon;
import com.infowarelabsdk.conference.domain.AnnotationResource;
import com.infowarelabsdk.conference.domain.AnnotationType;
import com.infowarelabsdk.conference.domain.LoginBean;
import com.infowarelabsdk.conference.transfer.Config;
import com.infowarelabsdk.conference.util.Constants;
import com.infowarelabsdk.conference.util.FileUtil;
import com.infowarelabsdk.conference.util.StringUtil;

import java.io.File;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

//import org.apache.log4j.Level;
//import de.mindpipe.android.logging.log4j.LogConfigurator;

@RuntimePermissions
public class LogoActivity extends BaseActivity {

    private static final String TAG = "InfowareLab.Debug";
    /**
     * 标记app是否已经获取到需要的权限
     */
    private boolean hasPermission = false;
    private AlertDialog dialog;
    private AlertDialog settingDialog;

    private static boolean flag = false;

    public static final String CONF_ID = "confKey";
    public static final String USER_NAME = "nickname";
    public static final String PASSWORD = "confPWD";
    public static final String SITE = "siteURL";
    public static final String LOGIN_NAME = "userName";
    public static final String LOGIN_PASS = "userPWD";
    public static final String BACK_URL = "backURL";
    private LinearLayout llRoot;
    private TextView textView1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //hideBottomUIMenu();

        setContentView(R.layout.logo);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            ConferenceApplication.setStatusBarColor(this, getColor(R.color.white));
//        }

        LogoActivityPermissionsDispatcher.requestPermissionsWithPermissionCheck(this);

        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
        }

        flag = true;
        initView();
        checkAnnotype();
    }

    protected void hideBottomUIMenu() {

        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_IMMERSIVE;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    private void initView() {
        if (getIntent() != null && getIntent().getData() != null) {
            Config.Site_URL = getIntent().getData().getHost();
        } else {
            SharedPreferences preferences = getPreferences(Activity.MODE_PRIVATE);
            Config.Site_URL = preferences.getString(Constants.SITE, "");
        }
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            ConferenceApplication.getConferenceApp().setVersion(info.versionName);
            if (this.getPackageName().compareToIgnoreCase("com.infowarelab.hongshantongphone") == 0) {
                ConferenceApplication.getConferenceApp().setSdkMode(false);
            } else
                ConferenceApplication.getConferenceApp().setSdkMode(true);

        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        if (CommonFactory.getInstance().getChatCommom() == null) {
            CommonFactory.getInstance().setAudioCommon(new AudioCommonImpl()).setConferenceCommon(new ConferenceCommonImpl())
                    .setDocCommon(new DocCommonImpl()).setSdCommon(new ShareDtCommonImpl())
                    .setUserCommon(new UserCommonImpl()).setVideoCommon(new VideoCommonImpl()).setChatCommom(new ChatCommomImpl());
            LocalCommonFactory.getInstance().setContactDataCommon(new ContactDataCommonImpl());
        }
        DocCommonImpl.mWidth = getWindowManager().getDefaultDisplay().getWidth();
        DocCommonImpl.mHeight = getWindowManager().getDefaultDisplay().getHeight();
        llRoot = (LinearLayout) findViewById(R.id.logo_ll_root);
        textView1 = (TextView) findViewById(R.id.textView1);
        Date date = new Date(); //获取当前的系统时间。
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy"); //使用了默认的格式创建了一个日期格式化对象。
        String time = dateFormat.format(date); //可以把日期转换转指定格式的字符串
        textView1.setText(String.format(getResources().getString(R.string.about_mid5), time));
        llRoot.post(new Runnable() {
            @Override
            public void run() {
                getScreenWitdhHeight();
                ConferenceApplication.getConferenceApp().setOs(Build.VERSION.RELEASE);
                String resolution = ConferenceApplication.SCREEN_WIDTH + "x" + ConferenceApplication.SCREEN_HEIGHT;
                ConferenceApplication.getConferenceApp().setResolution(resolution);
                if (hasPermission) {
                    doAsReady();
                    initLog4j();
                }
            }
        });
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen",
                "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public int getDaoHangHeight(Context context) {
        int result = 0;
        int resourceId = 0;
        int rid = this.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
        if (rid != 0) {
            resourceId = this.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            //CMLog.show("高度："+resourceId);
            //CMLog.show("高度："+context.getResources().getDimensionPixelSize(resourceId) +"");
            return this.getResources().getDimensionPixelSize(resourceId);
        } else
            return 0;
    }

    //获取是否存在NavigationBar
    public static boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {
        }
        return hasNavigationBar;
    }

    public boolean isNavigationBarShow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            Point realSize = new Point();
            display.getSize(size);
            display.getRealSize(realSize);
            return realSize.y != size.y;
        } else {
            boolean menu = ViewConfiguration.get(this).hasPermanentMenuKey();
            boolean back = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            if (menu || back) {
                return false;
            } else {
                return true;
            }
        }
    }

    private void getScreenWitdhHeight() {
        DisplayMetrics dm = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        } else {
            getWindowManager().getDefaultDisplay().getMetrics(dm);
        }
        ConferenceApplication.DENSITY = dm.density;
        if (dm.widthPixels > dm.heightPixels) {
            ConferenceApplication.SCREEN_WIDTH = dm.widthPixels;
            ConferenceApplication.SCREEN_HEIGHT = dm.heightPixels;
        } else {
            ConferenceApplication.SCREEN_WIDTH = dm.heightPixels;
            ConferenceApplication.SCREEN_HEIGHT = dm.widthPixels;
        }
        ConferenceApplication.Screen_W = ConferenceApplication.SCREEN_HEIGHT;
        ConferenceApplication.Screen_H = ConferenceApplication.SCREEN_WIDTH;

//		int[] location = new int[2] ;
//		llRoot.getLocationOnScreen(location);
//		if(location[1]>0){
//			ConferenceApplication.StateBar_H = location[1];
//			ConferenceApplication.NavigationBar_H = getDaoHangHeight(this);//ConferenceApplication.Screen_H - ConferenceApplication.Root_H - ConferenceApplication.StateBar_H;
//		}
//		else
//		{
        ConferenceApplication.StateBar_H = getStatusBarHeight(this);
        if (isNavigationBarShow())
            ConferenceApplication.NavigationBar_H = getDaoHangHeight(this);//ConferenceApplication.Screen_H - ConferenceApplication.Root_H - ConferenceApplication.StateBar_H
        else
            ConferenceApplication.NavigationBar_H = 0;
//		}


        ConferenceApplication.Root_W = ConferenceApplication.Screen_W;
        ConferenceApplication.Root_H = ConferenceApplication.Screen_H - ConferenceApplication.StateBar_H - ConferenceApplication.NavigationBar_H;
        //ConferenceApplication.Root_H = llRoot.getHeight()>0?llRoot.getHeight():ConferenceApplication.Screen_H;
        //ConferenceApplication.Root_W = llRoot.getWidth()>0?llRoot.getWidth():ConferenceApplication.Screen_W;

        ConferenceApplication.Top_H = getResources().getDimensionPixelOffset(R.dimen.height_6_80);
        ConferenceApplication.Bottom_H = getResources().getDimensionPixelOffset(R.dimen.height_7_80);

        Log.d("InfowareLab.Debug", "Logo ScreenW=" + ConferenceApplication.Screen_W
                + " ScreenH=" + ConferenceApplication.Screen_H
                + " RootW=" + ConferenceApplication.Root_W
                + " RootH=" + ConferenceApplication.Root_H
                + " StateH=" + ConferenceApplication.StateBar_H
                + " KeyH=" + ConferenceApplication.NavigationBar_H
                + " Density=" + ConferenceApplication.DENSITY
                + " Top_H=" + ConferenceApplication.Top_H
                + " Bottom_H=" + ConferenceApplication.Bottom_H);

        //log.info("density = " + ConferenceApplication.DENSITY + ", widthPixels = " + ConferenceApplication.SCREEN_WIDTH
        //	+ ", heightPixels = " + ConferenceApplication.SCREEN_HEIGHT);
    }


//	private void getScreenWitdhHeight() {
//		DisplayMetrics dm = new DisplayMetrics();
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//			getWindowManager().getDefaultDisplay().getRealMetrics(dm);
//		}else{
//			getWindowManager().getDefaultDisplay().getMetrics(dm);
//		}
//		ConferenceApplication.DENSITY = dm.density;
//		if (dm.widthPixels > dm.heightPixels) {
//			ConferenceApplication.SCREEN_WIDTH = dm.widthPixels;
//			ConferenceApplication.SCREEN_HEIGHT = dm.heightPixels;
//		} else {
//			ConferenceApplication.SCREEN_WIDTH = dm.heightPixels;
//			ConferenceApplication.SCREEN_HEIGHT = dm.widthPixels;
//		}
//		ConferenceApplication.Screen_W = ConferenceApplication.SCREEN_HEIGHT;
//		ConferenceApplication.Screen_H = ConferenceApplication.SCREEN_WIDTH;
//		ConferenceApplication.Root_W = llRoot.getWidth()>0?llRoot.getWidth():ConferenceApplication.Screen_W;
//		ConferenceApplication.Root_H = llRoot.getHeight()>0?llRoot.getHeight():ConferenceApplication.Screen_H;
//		int[] location = new int[2] ;
//		llRoot.getLocationOnScreen(location);
//		if(location[1]>0){
//			ConferenceApplication.StateBar_H = location[1];
//			ConferenceApplication.NavigationBar_H =ConferenceApplication.Screen_H - ConferenceApplication.Root_H - ConferenceApplication.StateBar_H;
//		}
//
//		ConferenceApplication.Top_H = getResources().getDimensionPixelOffset(R.dimen.height_6_80);
//		ConferenceApplication.Bottom_H = getResources().getDimensionPixelOffset(R.dimen.height_7_80);
//
//		Log.i("Logo","Logo ScreenW="+ConferenceApplication.Screen_W
//				+" ScreenH="+ConferenceApplication.Screen_H
//				+" RootW="+ConferenceApplication.Root_W
//				+" RootH="+ConferenceApplication.Root_H
//				+" StateH="+ConferenceApplication.StateBar_H
//				+" KeyH="+ConferenceApplication.NavigationBar_H
//				+" Density="+ConferenceApplication.DENSITY
//				+" Top_H="+ConferenceApplication.Top_H
//				+" Bottom_H="+ConferenceApplication.Bottom_H);
//
//		//log.info("density = " + ConferenceApplication.DENSITY + ", widthPixels = " + ConferenceApplication.SCREEN_WIDTH
//				+ ", heightPixels = " + ConferenceApplication.SCREEN_HEIGHT);
//	}

    private void doAsReady() {
        Config.Site_URL = FileUtil.readSharedPreferences(LogoActivity.this,
                Constants.SHARED_PREFERENCES, Constants.SITE);
        Config.SiteName = FileUtil.readSharedPreferences(LogoActivity.this, Constants.SHARED_PREFERENCES, Constants.SITE_NAME);
        Config.HAS_LIVE_SERVER = FileUtil.readSharedPreferences(LogoActivity.this, Constants.SHARED_PREFERENCES, Constants.HAS_LIVE_SERVER).equals("true");
        if (!CallbackManager.IS_LEAVED) {
            Intent intent = new Intent(LogoActivity.this, ConferenceActivity.class);
            startActivity(intent);

            finish();
            return;
        }
        checkIntent();
        if (flag) {
            new Thread() {
                @Override
                public void run() {
                    Intent intent = null;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (ConferenceApplication.getConferenceApp().getUpdateUrl() != null
                            && !ConferenceApplication.getConferenceApp().equals("")) {
                        ConferenceApplication.getConferenceApp().setApkName(
                                StringUtil.getFileName(ConferenceApplication.getConferenceApp().getUpdateUrl()));
                    }
                    Config.Site_URL = FileUtil.readSharedPreferences(LogoActivity.this,
                            Constants.SHARED_PREFERENCES, Constants.SITE);
                    if (Config.Site_URL == null || Config.Site_URL.equals("")) {
                        intent = new Intent(LogoActivity.this, SiteSetupActivity.class);
                    } else {
                        Config.Site_URL = FileUtil.readSharedPreferences(LogoActivity.this,
                                Constants.SHARED_PREFERENCES, Constants.SITE);
                        intent = new Intent(LogoActivity.this, ActHome.class);
                    }
                    intent.setData(getIntent().getData());
                    startActivity(intent);
                    finish();
                }
            }.start();
        }
    }

    private void checkIntent() {
        SharedPreferences preferences = getSharedPreferences(Constants.SHARED_PREFERENCES, Activity.MODE_PRIVATE);
        //log.info("save login uid = "+preferences.getInt(Constants.USER_ID, 0));
        Intent intent = getIntent();
        int state = intent.getIntExtra("state", 0);
        System.out.println("state :" + state);
        if (state != 0) {
            flag = false;
            String confID = intent.getStringExtra("confid");
            String password = intent.getStringExtra("password");
            final String userName = intent.getStringExtra("userName");
            String loginName = intent.getStringExtra("loginName");
            String loginPass = intent.getStringExtra("loginPass");
            String type = intent.getStringExtra("type");
            final String site = intent.getStringExtra("site");
            if (site != null && !site.equals("") && !site.equals(Config.Site_URL)) {
                Thread thread = new Thread() {
                    public void run() {
                        Config.SiteName = Config.getSiteName(site);

                    }

                    ;
                };

                try {
                    thread.start();
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String siteName = Config.SiteName;
                Config.Site_URL = site;
                //log.info("site = "+Config.Site_URL);
                if (siteName != null && !siteName.equals("")) {
                    //log.info("siten = "+site + " siteName = "+siteName);

                    preferences.edit().putString(Constants.HAS_LIVE_SERVER, "" + Config.HAS_LIVE_SERVER)
                            .putString(Constants.SITE, site)
                            .putString(Constants.SITE_NAME, Config.SiteName)
                            .putString(Constants.SITE_ID, Config.SiteId)
                            .commit();
                } else {
                    Toast.makeText(LogoActivity.this, getString(R.string.site_error), Toast.LENGTH_SHORT).show();
                    checkSiteUrl();
                    return;
                }
            }
            LoginBean login = new LoginBean(confID, userName, password);
            if (type != null && !type.equals("")) {
                login.setType(type);
            }

            login.setUid(preferences.getInt(Constants.USER_ID, 0));
            setJoinConfHandler(userName);
            //log.info("start check login!!!!");
            if (loginName != null && !loginName.equals("")) {
                LoginBean loginBean = new LoginBean(null, loginName, loginPass);
                loginBean = doLogin(loginBean);
                if (loginBean == null) {
                    Toast.makeText(LogoActivity.this, LogoActivity.this.getResources().getString(R.string.preconf_login_error), Toast.LENGTH_SHORT).show();
                    Intent loginIntent = new Intent(LogoActivity.this, ActHome.class);
                    startActivity(loginIntent);
                    finish();
                    return;
                }

//				HomeActivity.isLogin = true;
                login.setUid(loginBean.getUid());
                preferences.edit().putString(Constants.LOGIN_NAME, loginName)
                        .putString(Constants.LOGIN_PASS, loginPass)
                        .putString(Constants.LOGIN_NICKNAME, loginName)
                        .putString(Constants.LOGIN_EXNAME, loginName)
                        .putString(Constants.LOGIN_JOINNAME, loginName)
                        .putString(Constants.LOGIN_ROLE, loginBean.getCreateConfRole())
                        .putInt(Constants.USER_ID, loginBean.getUid())
                        .commit();


            } else {
                String name = preferences.getString(Constants.LOGIN_NAME, "");
                String pass = preferences.getString(Constants.LOGIN_PASS, "");
                if (name.trim().length() > 0) {
                    Config.SiteName = FileUtil.readSharedPreferences(this,
                            Constants.SHARED_PREFERENCES, Constants.SITE_NAME);
                    LoginBean loginBean = new LoginBean(null, name, pass);
                    loginBean = ((ConferenceCommonImpl) CommonFactory.getInstance().getConferenceCommon())
                            .checkUser(loginBean);
                    if (loginBean == null) {
                        Toast.makeText(LogoActivity.this, LogoActivity.this.getResources().getString(R.string.preconf_login_error), Toast.LENGTH_SHORT).show();
                        Intent loginIntent = new Intent(LogoActivity.this, ActHome.class);
                        startActivity(loginIntent);
                        finish();
                        return;
                    }
                    login.setUid(loginBean.getUid());
                    preferences.edit().putString(Constants.LOGIN_ROLE, loginBean.getCreateConfRole())
                            .putInt(Constants.CONF_LIST_TYPE, 0)
                            .putString(Constants.LOGIN_NICKNAME, loginBean.getUsername())
                            .putString(Constants.LOGIN_EXNAME, loginBean.getUsername())
                            .putString(Constants.LOGIN_JOINNAME, loginBean.getUsername())
                            .putInt(Constants.USER_ID, loginBean.getUid()).commit();
                }


            }
            new JoinConfByIdAction(this, null).startJoinThread(login);
        } else {
            String scheme = intent.getScheme();
            Uri uri = intent.getData();
            //System.out.println("scheme:" + scheme);
            if (uri != null) {

                Log.d(TAG, " >>>>>> scheme:" + scheme + " " + uri.toString());

                flag = false;
                String host = uri.getHost();
                String dataString = intent.getDataString();
                String confID = uri.getQueryParameter(CONF_ID);
                String userName = uri.getQueryParameter(USER_NAME);
                String password = uri.getQueryParameter(PASSWORD);
                String site1 = uri.getQueryParameter(SITE);

                Config.BackURL = uri.getQueryParameter(BACK_URL);
                Log.d("InfowareLab.Debug", ">>>Config.BackURL = " + Config.BackURL);

                if (site1.startsWith("http://") || site1.startsWith("https://")) {
                } else {
                    site1 = "http://" + site1;
                }
                final String site = site1;
                String loginName = uri.getQueryParameter(LOGIN_NAME);
                String loginPass = uri.getQueryParameter(LOGIN_PASS);
//                System.out.println("host:" + host);
//                System.out.println("dataString:" + dataString);
//                System.out.println(CONF_ID + confID);
//                System.out.println(USER_NAME + userName);
//                System.out.println(PASSWORD + password);
//                System.out.println(SITE + site);
//                System.out.println(LOGIN_NAME + loginName);
//                System.out.println(LOGIN_PASS + loginPass);
                if (site != null && !site.equals("") && !site.equals("http://")
//	            		&& !site.equals(Config.Site_URL)
                ) {
                    System.out.println("IF");
                    Thread thread = new Thread() {
                        public void run() {
                            Config.SiteName = Config.getSiteName(site);

                        }

                        ;
                    };

                    try {
                        thread.start();
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    String siteName = Config.SiteName;
                    Config.Site_URL = site;
                    //log.info("site = "+Config.Site_URL);
                    if (siteName != null && !siteName.equals("")) {
                        //log.info("siten = "+site + " siteName = "+siteName);

                        preferences.edit().putString(Constants.HAS_LIVE_SERVER, "" + Config.HAS_LIVE_SERVER)
                                .putString(Constants.SITE, site)
                                .putString(Constants.SITE_NAME, Config.SiteName)
                                .putString(Constants.SITE_ID, Config.SiteId)
                                .commit();
                    } else {
                        Toast.makeText(LogoActivity.this, getString(R.string.site_error), Toast.LENGTH_SHORT).show();
                        checkSiteUrl();
                        return;
                    }
                } else {
                    System.out.println("ELSE");
                    return;
                }
                LoginBean login = new LoginBean(confID, userName, password);

                login.setUid(preferences.getInt(Constants.USER_ID, 0));
                setJoinConfHandler(userName);
                if (loginName != null && !loginName.equals("")) {
                    LoginBean loginBean = new LoginBean(null, loginName, loginPass);
                    loginBean = doLogin(loginBean);
                    if (loginBean == null) {
                        Toast.makeText(LogoActivity.this, LogoActivity.this.getResources().getString(R.string.preconf_login_error), Toast.LENGTH_SHORT).show();
                        Intent loginIntent = new Intent(LogoActivity.this, ActHome.class);
                        startActivity(loginIntent);
                        finish();
                        return;
                    }

//					HomeActivity.isLogin = true;
                    login.setUid(loginBean.getUid());
                    preferences.edit().putString(Constants.LOGIN_NAME, loginName)
                            .putString(Constants.LOGIN_PASS, loginPass)
                            .putString(Constants.LOGIN_NICKNAME, loginName)
                            .putString(Constants.LOGIN_EXNAME, loginName)
                            .putString(Constants.LOGIN_JOINNAME, loginName)
                            .putString(Constants.LOGIN_ROLE, loginBean.getCreateConfRole())
                            .putInt(Constants.USER_ID, loginBean.getUid())
                            .commit();


                } else {
                    String name = preferences.getString(Constants.LOGIN_NAME, "");
                    String pass = preferences.getString(Constants.LOGIN_PASS, "");
                    if (name.trim().length() > 0) {
                        Config.SiteName = FileUtil.readSharedPreferences(this,
                                Constants.SHARED_PREFERENCES, Constants.SITE_NAME);
                        LoginBean loginBean = new LoginBean(null, name, pass);
                        loginBean = ((ConferenceCommonImpl) CommonFactory.getInstance().getConferenceCommon())
                                .checkUser(loginBean);
                        if (loginBean == null) {
                            Toast.makeText(LogoActivity.this, LogoActivity.this.getResources().getString(R.string.preconf_login_error), Toast.LENGTH_SHORT).show();
                            Intent loginIntent = new Intent(LogoActivity.this, ActHome.class);
                            startActivity(loginIntent);
                            finish();
                            return;
                        }
                        login.setUid(loginBean.getUid());
                        preferences.edit().putString(Constants.LOGIN_ROLE, loginBean.getCreateConfRole())
                                .putInt(Constants.CONF_LIST_TYPE, 0)
                                .putString(Constants.LOGIN_NICKNAME, loginBean.getUsername())
                                .putString(Constants.LOGIN_EXNAME, loginBean.getUsername())
                                .putString(Constants.LOGIN_JOINNAME, loginBean.getUsername())
                                .putInt(Constants.USER_ID, loginBean.getUid()).commit();
                    }


                }
                new JoinConfByIdAction(this, null).startJoinThread(login);
            }
        }
    }

    //登录
    private LoginBean doLogin(LoginBean loginBean) {
        Config.SiteName = FileUtil.readSharedPreferences(this,
                Constants.SHARED_PREFERENCES, Constants.SITE_NAME);
        loginBean = ((ConferenceCommonImpl) CommonFactory.getInstance().getConferenceCommon())
                .checkUser(loginBean);

        return loginBean;
    }

    private void setJoinConfHandler(final String userName) {
        Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                boolean isJoin = false;
                switch (msg.what) {
                    case ConferenceCommon.RESULT_SUCCESS:
                        //log.info("join success");
                        isJoin = true;
                        //昵称更改时保存，在按下进入会议后
                        FileUtil.saveSharedPreferences(LogoActivity.this, Constants.SHARED_PREFERENCES,
                                Constants.LOGIN_NICKNAME, userName);

                        Intent intent = new Intent(LogoActivity.this, ConferenceActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
//					((AudioCommonImpl)commonFactory.getAudioCommon()).startAudioService();
                        finish();
//					mAction.missDislog();
                        break;
                    case ConferencePagerNumber.INIT_SDK:
                        //log.info("initSDK success");
                        break;
                    default:
//                        if (!isJoin) {
//                            //log.info("error code = "+msg.what);
//                            ErrorMessage errorMessage = new ErrorMessage(LogoActivity.this);
//                            String message = errorMessage.getErrorMessageByCode(msg.what);
//                            Intent homeIntent = new Intent(LogoActivity.this, ActHome.class);
//                            startActivity(homeIntent);
//                            finish();
//                            Toast.makeText(LogoActivity.this, message, Toast.LENGTH_LONG).show();
//                        }
                        break;
                }
            }

            ;
        };
        ((ConferenceCommonImpl) CommonFactory.getInstance().getConferenceCommon()).setHandler(handler);
    }

    protected void checkSiteUrl() {
        Intent intent = null;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (ConferenceApplication.getConferenceApp().getUpdateUrl() != null
                && !ConferenceApplication.getConferenceApp().equals("")) {
            ConferenceApplication.getConferenceApp().setApkName(
                    StringUtil.getFileName(ConferenceApplication.getConferenceApp().getUpdateUrl()));
        }

        if (Config.Site_URL == null || Config.Site_URL.equals("")) {
            intent = new Intent(LogoActivity.this, SiteSetupActivity.class);
        } else {
            Config.Site_URL = FileUtil.readSharedPreferences(LogoActivity.this,
                    Constants.SHARED_PREFERENCES, Constants.SITE);
            intent = new Intent(LogoActivity.this, ActHome.class);
        }
        intent.setData(getIntent().getData());
        startActivity(intent);
        finish();

    }

    @Override
    protected void onDestroy() {
        if (dialog != null && dialog.isShowing()) {
            dialog.cancel();
        }
        if (settingDialog != null && settingDialog.isShowing()) {
            settingDialog.cancel();
        }
        super.onDestroy();
    }

    private void checkAnnotype() {
        if (((DocCommonImpl) CommonFactory.getInstance().getDocCommon()).getAnnotation() == null) {
            ((DocCommonImpl) CommonFactory.getInstance().getDocCommon()).setAnnotation(new AnnotationType(new AnnotationResource(R.id.annotationColorRed, R.id.annotationPen, initRes(), initJson())));
        }
    }

    private Map<Integer, Integer> initRes() {
        Map<Integer, Integer> res = new HashMap<Integer, Integer>();
        res.put(R.id.annotationPen, R.drawable.pen);
        res.put(R.id.annotationColorBlack, R.drawable.colorblack);
        res.put(R.id.annotationColorBlue, R.drawable.colorblue);
        res.put(R.id.annotationColorGreen, R.drawable.colorgreen);
        res.put(R.id.annotationColorRed, R.drawable.colorred);
        res.put(R.id.annotationColorYellow, R.drawable.coloryellow);
        res.put(R.id.annotationColorWhite, R.drawable.colorwhite);
        return res;
    }

    private Map<Integer, String> initJson() {
        Map<Integer, String> jsons = new HashMap<Integer, String>();
        jsons.put(R.id.annotationPen, Constants.PAINT_TYPE_PEN);
        jsons.put(R.id.annotationColorBlack, Constants.COLOR_BLACK);
        jsons.put(R.id.annotationColorBlue, Constants.COLOR_BLUE);
        jsons.put(R.id.annotationColorGreen, Constants.COLOR_GREEN);
        jsons.put(R.id.annotationColorRed, Constants.COLOR_RED);
        jsons.put(R.id.annotationColorYellow, Constants.COLOR_YELLOW);
        jsons.put(R.id.annotationColorWhite, Constants.COLOR_WHITE);
        return jsons;
    }

    /**
     * 请求权限
     */
    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.RECEIVE_BOOT_COMPLETED})
    public void requestPermissions() {
        hasPermission = true;
    }

    /**
     * 用户拒绝授权
     */
    @OnPermissionDenied({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE})
    public void onPermissionRefused() {
        showSettingDialog();
    }

    /**
     * 阐述App获取权限的目的
     *
     * @param request
     */
    @OnShowRationale({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE})
    public void showShowRationaleForPermission(final PermissionRequest request) {
        if (dialog == null) {
            dialog = new AlertDialog.Builder(this).create();
            dialog.setMessage(getString(R.string.store_permission_message));
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.string_allow), new DialogInterface
                    .OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    request.proceed();
                }
            });
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.string_not_allow), new DialogInterface
                    .OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    request.cancel();
                }
            });
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } else {
            dialog.show();
        }
    }

    /**
     * 用户勾选了“不再提醒”
     */
    @OnNeverAskAgain({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE})
    public void showNeverAskForPermission() {
        showSettingDialog();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LogoActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
        if (hasPermission) {
            doAsReady();
            initLog4j();
        }
    }

    private void showSettingDialog() {
        if (settingDialog == null) {
            settingDialog = new AlertDialog.Builder(this).create();
            settingDialog.setMessage(getString(R.string.string_open_setting_tip));
            settingDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.string_goto), new DialogInterface
                    .OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_SETTINGS);
                    startActivity(intent);
                    finish();
                }
            });
            settingDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.string_not_goto), new DialogInterface
                    .OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            settingDialog.setCancelable(false);
            settingDialog.setCanceledOnTouchOutside(false);
            settingDialog.show();
        } else {
            settingDialog.show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (hasPermission) {
            getApplication().onTerminate();
        }
    }

    private void initLog4j() {
//		final LogConfigurator logConfigurator = new LogConfigurator();
//		logConfigurator.setFileName(getFilePath("Log") + File.separator + "infowarelabPhone.log");
//		logConfigurator.setRootLevel(Level.INFO);
//		logConfigurator.setLevel("org.apache", Level.INFO);
//		logConfigurator.setFilePattern("%d - %p[%c] - %m%n");
//		logConfigurator.setLogCatPattern("%m%n");
//		logConfigurator.configure();
    }

    private String getFilePath(String folder) {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory() + File.separator + "infowarelab" + File.separator + folder;
        } else {
            return getCacheDir() + File.separator + "infowarelab" + File.separator + folder;
        }
    }

}
