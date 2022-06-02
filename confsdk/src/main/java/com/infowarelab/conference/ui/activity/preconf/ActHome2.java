package com.infowarelab.conference.ui.activity.preconf;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.infowarelab.conference.ConferenceApplication;
import com.infowarelab.conference.localDataCommon.ContactDataCommon;
import com.infowarelab.conference.localDataCommon.LocalCommonFactory;
import com.infowarelab.conference.localDataCommon.impl.ContactDataCommonImpl;
import com.infowarelab.conference.ui.activity.inconf.ConferenceActivity;
import com.infowarelab.conference.ui.activity.preconf.fragment.FragCreate;
import com.infowarelab.conference.ui.activity.preconf.fragment.FragJoin;
import com.infowarelab.conference.ui.activity.preconf.fragment.FragSet;
import com.infowarelab.conference.utils.PublicWay;
import com.infowarelab.conference.utils.SocketService;
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
import com.infowarelabsdk.conference.domain.AnnotationResource;
import com.infowarelabsdk.conference.domain.AnnotationType;
import com.infowarelabsdk.conference.domain.LoginBean;
import com.infowarelabsdk.conference.transfer.Config;
import com.infowarelabsdk.conference.util.Constants;
import com.infowarelabsdk.conference.util.FileUtil;
import com.infowarelabsdk.conference.util.StringUtil;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

//import permissions.dispatcher.NeedsPermission;
//import permissions.dispatcher.OnNeverAskAgain;
//import permissions.dispatcher.OnPermissionDenied;
//import permissions.dispatcher.OnShowRationale;
//import permissions.dispatcher.PermissionRequest;
//import permissions.dispatcher.RuntimePermissions;

//@RuntimePermissions
public class ActHome2 extends BaseFragmentActivity implements BaseFragment.onSwitchPageListener {
    private static final String TAG = "InfowareLab.ActHome2";
    private FragmentManager fragManager;
    private FragCreate fragCreate;
    private FragJoin fragJoin;
    private FragSet fragSet;
    private Button btnCreate;
    private Button btnJoin;
    private Button btnSet;
    private Button btnJoinPage1;
    private Button btnJoinPage2;
    private TextView tvTitleCreate;
    private TextView tvTitleSet;
    private LinearLayout llTitleJoin;
    private int currentFrag = 0;
    private boolean isSwitching;

    private Intent preconfIntent;
    public SharedPreferences preferences;
    private ContactDataCommon common;

    private String username;
    public static boolean isLogin;
    public static int position = 1;
    public static final int IS_LOGIN = 1;
    public static final int NOT_LOGIN = 0;
    public static final int FINISH = 2;

    /**
     * 标记app是否已经获取到需要的权限
     */
    private boolean hasPermission = false;
    private AlertDialog dialog;
    private AlertDialog settingDialog;

    private static boolean flag = false;

    private Handler homeHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NOT_LOGIN:

                    ConferenceActivity.isLogin = false;

                    isLogin = false;

                    startInvitingService();

//				preconfLogin.setVisibility(View.VISIBLE);
                    break;
                case IS_LOGIN:

                    ConferenceActivity.isLogin = true;

                    isLogin = true;

                    startInvitingService();

//				preconfLogin.setVisibility(View.GONE);
                    break;
                case FINISH:
                    finish();
                    break;
                default:
                    break;
            }
        }

        ;
    };
    private int userId = 0;
    private boolean mReturnFromConf = false;

    @Override
    public void finish() {
        super.finish();
        //overridePendingTransition(0, 0);
    }

    protected void hideBottomUIMenu() {

        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            decorView.setSystemUiVisibility(uiOptions);

            // status bar is hidden, so hide that too if necessary.
            //ActionBar actionBar = getActionBar();
            //actionBar.hide();
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //hideBottomUIMenu();

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent != null)
            mReturnFromConf = intent.getBooleanExtra("returnFromConf", false);

        System.gc();
        setContentView(R.layout.a6_preconf_home_2);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ConferenceApplication.setStatusBarColor(this, getColor(R.color.app_main_hue));
        }

        //if (!mReturnFromConf)
        //    ActHome2PermissionsDispatcher.requestPermissionsWithPermissionCheck(this);

        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
        }

        flag = true;
        initView();
        checkAnnotype();

        CommonFactory commonFactory = CommonFactory.getInstance();
        ConferenceCommonImpl conferenceCommon = (ConferenceCommonImpl) commonFactory.getConferenceCommon();
        //switchFrag(1);
        if (null != conferenceCommon) {
            if (true == conferenceCommon.mCreateConf) {
                switchFrag(1);
            } else
                switchFrag(2);
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

    private void checkAnnotype() {
        if (((DocCommonImpl) CommonFactory.getInstance().getDocCommon()).getAnnotation() == null) {
            ((DocCommonImpl) CommonFactory.getInstance().getDocCommon()).setAnnotation(new AnnotationType(new AnnotationResource(R.id.annotationColorRed, R.id.annotationPen, initRes(), initJson())));
        }
    }

    private void startInvitingService() {

        int checkUserId = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFERENCES,
                getApplicationContext().MODE_PRIVATE).getInt(Constants.USER_ID, 0);

        if (checkUserId <= 0) {
            Log.d("Infowarelab.Debug", "ActHome.startInvitingService: NOT logined and stop service!");

            if (SocketService.isServiceStarted) {
                PublicWay.actionOnService(SocketService.Actions.STOP);
            }

            return;
        }

        if (SocketService.isServiceStarted) {
            Log.d("Infowarelab.Debug", "ActHome.startInvitingService: service is already started and ignored!");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                PublicWay.getIpAndPort(ActHome2.this, Config.Site_URL,FileUtil.readSharedPreferences(ActHome2.this,
                        Constants.SHARED_PREFERENCES, Constants.SITE_ID));

                Config.Site_URL = FileUtil.readSharedPreferences(ActHome2.this,
                        Constants.SHARED_PREFERENCES, Constants.SITE);
                FileUtil.saveSharedPreferences(ActHome2.this,
                        Constants.SHARED_PREFERENCES,
                        Constants.SITE_NAME, Config.getSiteName(Config.Site_URL));
                String id = FileUtil.readSharedPreferences(ActHome2.this,
                        Constants.SHARED_PREFERENCES, Constants.SITE_ID);

                int userId = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFERENCES,
                        getApplicationContext().MODE_PRIVATE).getInt(Constants.USER_ID, 0);

                if (userId == 0) return;

                if (TextUtils.isEmpty(id)){
                    id = "0";
                }

                String loginName = FileUtil.readSharedPreferences(ActHome2.this,
                        Constants.SHARED_PREFERENCES, Constants.LOGIN_NAME);

                if (loginName == null && loginName.length() <= 0) return;

//                if (Config.terminateRegist(DeviceIdFactory.getUUID1(ActHome.this), FileUtil.readSharedPreferences(ActHome.this,
//                        Constants.SHARED_PREFERENCES, Constants.LOGIN_NAME), Integer.parseInt(id),userId).equals("0")) {
//                    runOnUiThread(new Runnable(){
//                        @Override
//                        public void run() {
//                            Toast.makeText(ActHome.this, R.string.regist_success, Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                } else if (Config.terminateRegist(loginName/*DeviceIdFactory.getUUID1(ActHome.this)*/, loginName/*FileUtil.readSharedPreferences(ActHome.this,
//                        Constants.SHARED_PREFERENCES, Constants.LOGIN_JOINNAME)*/, Integer.parseInt(id),userId).equals("-1")) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(ActHome.this, R.string.regist_fail, Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                }
            }
        }).start();

    }

    @Override
    protected void onResume() {

        PublicWay.mActivity = this;

        isReLogin();

        if (PublicWay.mRinging){

           new Handler().post(new Runnable() {
                @Override
                public void run() {
                    PublicWay.showNotifyDialog();
                }
            });
        }

        super.onResume();
    }

    /**
     * 是否已有登录信息
     */
    private void isReLogin() {
        username = FileUtil.readSharedPreferences(this, Constants.SHARED_PREFERENCES, Constants.LOGIN_NAME);
        userId = FileUtil.readSharedPreferencesInt(this, Constants.SHARED_PREFERENCES, Constants.USER_ID);

        if (username.equals("") || userId <= 0) {
            ConferenceActivity.isLogin = false;
            isLogin = false;
            homeHandler.sendEmptyMessage(NOT_LOGIN);
        } else if (userId > 0){
            ConferenceActivity.isLogin = true;
            isLogin = true;
            homeHandler.sendEmptyMessage(IS_LOGIN);
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

        } catch (PackageManager.NameNotFoundException e) {
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

        LocalCommonFactory.getInstance().setContactDataCommon(new ContactDataCommonImpl());
        common = LocalCommonFactory.getInstance().getContactDataCommon();

        fragManager = getSupportFragmentManager();
        btnCreate = (Button) findViewById(R.id.act_home_btn_bottom_1);
        btnJoin = (Button) findViewById(R.id.act_home_btn_bottom_2);
        btnSet = (Button) findViewById(R.id.act_home_btn_bottom_3);
        btnJoinPage1 = (Button) findViewById(R.id.act_home_btn_title_list_1);
        btnJoinPage2 = (Button) findViewById(R.id.act_home_btn_title_list_2);
        tvTitleCreate = (TextView) findViewById(R.id.act_home_tv_title_create);
        tvTitleSet = (TextView) findViewById(R.id.act_home_tv_title_set);
        llTitleJoin = (LinearLayout) findViewById(R.id.act_home_ll_title_list);

        llTitleJoin.post(new Runnable() {
            @Override
            public void run() {
                getScreenWidthAndHeight();
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

    private void doAsReady() {

        if (mReturnFromConf) return;

        showLoading();

        Config.Site_URL = FileUtil.readSharedPreferences(ActHome2.this,
                Constants.SHARED_PREFERENCES, Constants.SITE);
        Config.SiteName = FileUtil.readSharedPreferences(ActHome2.this, Constants.SHARED_PREFERENCES, Constants.SITE_NAME);
        Config.HAS_LIVE_SERVER = FileUtil.readSharedPreferences(ActHome2.this, Constants.SHARED_PREFERENCES, Constants.HAS_LIVE_SERVER).equals("true");
        if (!CallbackManager.IS_LEAVED) {
            hideLoading();
            Intent intent = new Intent(ActHome2.this, ConferenceActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        if (!checkIntent())
        {
            hideLoading();
            finish();
            return;
        }

//        if (flag) {
//            new Thread() {
//                @Override
//                public void run() {
//                    Intent intent = null;
////                    try {
////                        Thread.sleep(1000);
////                    } catch (InterruptedException e) {
////                        e.printStackTrace();
////                    }
//                    if (ConferenceApplication.getConferenceApp().getUpdateUrl() != null
//                            && !ConferenceApplication.getConferenceApp().equals("")) {
//                        ConferenceApplication.getConferenceApp().setApkName(
//                                StringUtil.getFileName(ConferenceApplication.getConferenceApp().getUpdateUrl()));
//                    }
//                    Config.Site_URL = FileUtil.readSharedPreferences(ActHome2.this,
//                            Constants.SHARED_PREFERENCES, Constants.SITE);
//                    if (Config.Site_URL == null || Config.Site_URL.equals("")) {
//                        intent = new Intent(LogoActivity.this, SiteSetupActivity.class);
//                    } else {
//                        Config.Site_URL = FileUtil.readSharedPreferences(LogoActivity.this,
//                                Constants.SHARED_PREFERENCES, Constants.SITE);
//                        intent = new Intent(LogoActivity.this, ActHome.class);
//                    }
//                    intent.setData(getIntent().getData());
//                    startActivity(intent);
//                    finish();
//                }
//            }.start();
//        }

        isReLogin();

        hideLoading();

        if (currentFrag == 2)
        {
            if (fragJoin != null && fragJoin.isVisible()){
                fragJoin.refreshConfList();
            }
        }
    }

    private boolean checkIntent() {

        SharedPreferences preferences = getSharedPreferences(Constants.SHARED_PREFERENCES, Activity.MODE_PRIVATE);

        Intent intent = getIntent();

        if (intent != null) {
            flag = false;

            final String site = intent.getStringExtra("site");
            final String loginPass = intent.getStringExtra("password");
            final String loginName = intent.getStringExtra("userName");
            String joinName = intent.getStringExtra("joinName");
            if (joinName == null || joinName.length() <= 0) joinName = loginName;

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

                if (siteName != null && !siteName.equals("")) {

                    Log.d(TAG, "ActHome2.site = "+ site + "; siteName = "+siteName);

                    preferences.edit().putString(Constants.HAS_LIVE_SERVER, "" + Config.HAS_LIVE_SERVER)
                            .putString(Constants.SITE, site)
                            .putString(Constants.SITE_NAME, Config.SiteName)
                            .putString(Constants.SITE_ID, Config.SiteId)
                            .commit();
                } else {
                    Toast.makeText(ActHome2.this, getString(R.string.site_error), Toast.LENGTH_SHORT).show();
                    return false;
                }
            }

            if (loginName != null && !loginName.equals("")) {
                LoginBean loginBean = new LoginBean(null, loginName, loginPass);
                loginBean = doLogin(loginBean);
                if (loginBean == null) {
                    Toast.makeText(ActHome2.this, ActHome2.this.getResources().getString(R.string.preconf_login_error), Toast.LENGTH_SHORT).show();
                    finish();
                    return false;
                }

                Log.d(TAG, "ActHome2.userId = "+ loginBean.getUid());

                preferences.edit().putString(Constants.LOGIN_NAME, loginName)
                        .putString(Constants.LOGIN_PASS, loginPass)
                        .putString(Constants.LOGIN_NICKNAME, joinName)
                        .putString(Constants.LOGIN_EXNAME, loginName)
                        .putString(Constants.LOGIN_JOINNAME, joinName)
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
                        Toast.makeText(ActHome2.this, ActHome2.this.getResources().getString(R.string.preconf_login_error), Toast.LENGTH_SHORT).show();
                        finish();
                        return false;
                    }
                    preferences.edit().putString(Constants.LOGIN_ROLE, loginBean.getCreateConfRole())
                            .putInt(Constants.CONF_LIST_TYPE, 0)
                            .putString(Constants.LOGIN_NICKNAME, joinName)
                            .putString(Constants.LOGIN_EXNAME, loginBean.getUsername())
                            .putString(Constants.LOGIN_JOINNAME, joinName)
                            .putInt(Constants.USER_ID, loginBean.getUid()).commit();
                }
                else
                {
                    if (joinName.trim().length() > 0)
                    {
                        preferences.edit()
                                .putString(Constants.LOGIN_NICKNAME, joinName)
                                .putString(Constants.LOGIN_JOINNAME, joinName).commit();
                    }
                }
            }

        }
        else {
            Toast.makeText(ActHome2.this, ActHome2.this.getResources().getString(R.string.preconf_login_error), Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }

        return true;
    }

    //登录
    private LoginBean doLogin(LoginBean loginBean) {
        Config.SiteName = FileUtil.readSharedPreferences(this,
                Constants.SHARED_PREFERENCES, Constants.SITE_NAME);
        loginBean = ((ConferenceCommonImpl) CommonFactory.getInstance().getConferenceCommon())
                .checkUser(loginBean);

        return loginBean;
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

    private void getScreenWidthAndHeight() {
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

    public void RadioClick(View v) {
        if (isSwitching)
            return;
        int id = v.getId();
        if (id == R.id.act_home_btn_bottom_1) {
            switchFrag(1);
        } else if (id == R.id.act_home_btn_bottom_2) {
            switchFrag(2);
        } else if (id == R.id.act_home_btn_bottom_3) {
            switchFrag(3);
        } else if (id == R.id.act_home_btn_title_list_1) {
            if (fragJoin != null) {
                btnJoinPage1.setBackgroundResource(R.drawable.a6_btn_left_semicircle_selected);
                btnJoinPage2.setBackgroundResource(R.drawable.a6_btn_right_semicircle_normal);
                fragJoin.switchPage(0);
            }
        } else if (id == R.id.act_home_btn_title_list_2) {
            if (fragJoin != null) {
                btnJoinPage1.setBackgroundResource(R.drawable.a6_btn_left_semicircle_normal);
                btnJoinPage2.setBackgroundResource(R.drawable.a6_btn_right_semicircle_selected);
                fragJoin.switchPage(1);
            }
        }
    }

    private void switchFrag(int which) {
        if (currentFrag == which)
            return;
//		if(which==1&&!isLogin){
//			jump2Login(FragSet.REQUESTCODE_LOGIN_CREATE);
//			return;
//		}
        isSwitching = true;
        FragmentTransaction ft;
        ft = fragManager.beginTransaction();

        if (fragCreate != null && fragCreate.isAdded())
            ft.hide(fragCreate);
        btnCreate.setBackgroundResource(R.drawable.a6_icon_home_create_normal);
        tvTitleCreate.setVisibility(View.GONE);

        if (fragJoin != null && fragJoin.isAdded())
            ft.hide(fragJoin);
        btnJoin.setBackgroundResource(R.drawable.a6_icon_home_list_normal);
        llTitleJoin.setVisibility(View.GONE);

        if (fragSet != null && fragSet.isAdded())
            ft.hide(fragSet);
        btnSet.setBackgroundResource(R.drawable.a6_icon_home_set_normal);
        tvTitleSet.setVisibility(View.GONE);

        switch (which) {
            case 1:
                btnCreate.setBackgroundResource(R.drawable.a6_icon_home_create_selected);
                tvTitleCreate.setVisibility(View.VISIBLE);
                if (fragCreate == null) {
                    fragCreate = new FragCreate();
                    fragCreate.setBaseFragmentActivity(this);
                    ft.add(R.id.act_home_fl_container, fragCreate, "Create");
                } else {
                    ft.show(fragCreate);
                }
                currentFrag = 1;
                break;
            case 2:
                btnJoin.setBackgroundResource(R.drawable.a6_icon_home_list_selected);
                llTitleJoin.setVisibility(View.VISIBLE);
                if (fragJoin == null) {
                    fragJoin = new FragJoin();
                    fragJoin.setOnSwitchPageListener(this);
                    fragJoin.setBaseFragmentActivity(this);
                    ft.add(R.id.act_home_fl_container, fragJoin, "Join");
                } else {
                    ft.show(fragJoin);
                }
                currentFrag = 2;
                break;
            case 3:
                btnSet.setBackgroundResource(R.drawable.a6_icon_home_set_selected);
                tvTitleSet.setVisibility(View.VISIBLE);
                if (fragSet == null) {
                    fragSet = new FragSet();
                    fragSet.setBaseFragmentActivity(this);
                    ft.add(R.id.act_home_fl_container, fragSet, "Contacts");
                } else {
                    ft.show(fragSet);
                }
                currentFrag = 3;
                break;

            default:
                btnSet.setBackgroundResource(R.drawable.a6_icon_home_set_selected);
                tvTitleSet.setVisibility(View.VISIBLE);
                if (fragSet == null) {
                    fragSet = new FragSet();
                    fragSet.setBaseFragmentActivity(this);
                    ft.add(R.id.act_home_fl_container, fragSet, "Contacts");
                } else {
                    ft.show(fragSet);
                }
                currentFrag = 3;
                break;
        }
        ft.commit();
        isSwitching = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (requestCode == FragSet.REQUESTCODE_LOGIN_CREATE) {
            if (resultCode == LoginActivity.RESULTCODE_LOGIN) {
                isReLogin();
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        switchFrag(1);
                    }
                });
            } else if (resultCode == LoginActivity.RESULTCODE_LOGOUT) {
                isReLogin();
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        switchFrag(2);
                    }
                });
            } else if (resultCode == RESULT_CANCELED) {

            }
        } else if (requestCode == FragSet.REQUESTCODE_LOGIN) {
            if (resultCode == LoginActivity.RESULTCODE_LOGIN) {
                isReLogin();
            } else if (resultCode == LoginActivity.RESULTCODE_LOGOUT) {
                isReLogin();
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        switchFrag(2);
                    }
                });
            }
        } else if (requestCode == ActOrganization.REQ_ORG) {
            if (resultCode == RESULT_OK && data != null) {
                String ids = data.getStringExtra("key1");
                String names = data.getStringExtra("key2");
                String emails = data.getStringExtra("key3");
                if (ids != null && !ids.equals("")) {
                    fragCreate.setAtts(ids, names, emails);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void jump2Login(int req) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("state", 1);
        intent.putExtra("turnIndex", 1);
        startActivityForResult(intent, req);
    }

    @Override
    public void onBackPressed() {

        if (fragJoin != null && fragJoin.getCurrentItem() == 1 && fragJoin.isEnterFromItem()) {
            fragJoin.setCurrentItem(0);
            fragJoin.setEnterFromItem(false);
            return;
        }
//		Intent i = new Intent(Intent.ACTION_MAIN);
//		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		i.addCategory(Intent.CATEGORY_HOME);
//		startActivity(i);
//		//退出应用后关闭所有在进程中运行的消息队列等
//		android.os.Process.killProcess(android.os.Process.myPid());
        super.onBackPressed();
    }

    /**
     * 获取当前设置成功的站点
     */
//	private void setCurrentSite(){
//		//log.info("current SITE = " +FileUtil.readSharedPreferences
//				(this, Constants.SHARED_PREFERENCES, Constants.SITE));
//		preconfCurrentsite.setText(FileUtil.readSharedPreferences
//				(this, Constants.SHARED_PREFERENCES, Constants.SITE));
//	}

    /**
     * 预先读取手机联系人的数据
     */
    private void getContactsList() {
        if (common == null) {
            common = LocalCommonFactory.getInstance().getContactDataCommon();
        }
        if (common.getContactList().isEmpty()) {
            //开启一个线程load当前手机客户端的联系人数据
            new Thread() {
                @Override
                public void run() {
                    common.getContacts(ActHome2.this);
                }
            }.start();
        }
    }

    @Override
    public void doSelect(int postion) {
        // TODO Auto-generated method stub
        if (postion == 0) {
            btnJoinPage1.setBackgroundResource(R.drawable.a6_btn_left_semicircle_selected);
            btnJoinPage1.setTextColor(getResources().getColor(R.color.app_main_hue));
            btnJoinPage2.setBackgroundResource(R.drawable.a6_btn_right_semicircle_normal);
            btnJoinPage2.setTextColor(getResources().getColor(R.color.white));
        } else if (postion == 1) {
            btnJoinPage1.setBackgroundResource(R.drawable.a6_btn_left_semicircle_normal);
            btnJoinPage1.setTextColor(getResources().getColor(R.color.white));
            btnJoinPage2.setBackgroundResource(R.drawable.a6_btn_right_semicircle_selected);
            btnJoinPage2.setTextColor(getResources().getColor(R.color.app_main_hue));
        }
    }

//    /**
//     * 请求权限
//     */
//    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA,
//            Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.RECEIVE_BOOT_COMPLETED})
//    public void requestPermissions() {
//        hasPermission = true;
//    }
//
//    /**
//     * 用户拒绝授权
//     */
//    @OnPermissionDenied({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA,
//            Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE})
//    public void onPermissionRefused() {
//        showSettingDialog();
//    }
//
//    /**
//     * 阐述App获取权限的目的
//     *
//     * @param request
//     */
//    @OnShowRationale({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA,
//            Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE})
//    public void showShowRationaleForPermission(final PermissionRequest request) {
//        if (dialog == null) {
//            dialog = new AlertDialog.Builder(this).create();
//            dialog.setMessage(getString(R.string.store_permission_message));
//            dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.string_allow), new DialogInterface
//                    .OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    request.proceed();
//                }
//            });
//            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.string_not_allow), new DialogInterface
//                    .OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    request.cancel();
//                }
//            });
//            dialog.setCancelable(false);
//            dialog.setCanceledOnTouchOutside(false);
//            dialog.show();
//        } else {
//            dialog.show();
//        }
//    }
//
//    /**
//     * 用户勾选了“不再提醒”
//     */
//    @OnNeverAskAgain({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA,
//            Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE})
//    public void showNeverAskForPermission() {
//        showSettingDialog();
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        //ActHome2PermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
//        if (hasPermission) {
//            doAsReady();
//            initLog4j();
//        }
//    }

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

    private void initLog4j() {
//		final LogConfigurator logConfigurator = new LogConfigurator();
//		logConfigurator.setFileName(getFilePath("Log") + File.separator + "infowarelabPhone.log");
//		logConfigurator.setRootLevel(Level.INFO);
//		logConfigurator.setLevel("org.apache", Level.INFO);
//		logConfigurator.setFilePattern("%d - %p[%c] - %m%n");
//		logConfigurator.setLogCatPattern("%m%n");
//		logConfigurator.configure();
    }


}
