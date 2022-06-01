package com.infowarelab.conference.ui.activity.inchat;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hd.screencapture.callback.ScreenCaptureStreamCallback;
import com.hd.screencapture.help.ScreenCaptureState;
import com.infowarelab.conference.ConferenceApplication;
import com.infowarelab.conference.PhoneStateUtil;
import com.infowarelab.conference.localDataCommon.LocalCommonFactory;
import com.infowarelab.conference.ui.activity.inchat.view.ConfASView;
import com.infowarelab.conference.ui.activity.inchat.view.ConfAttendersView;
import com.infowarelab.conference.ui.activity.inchat.view.ConfDsView;
import com.infowarelab.conference.ui.activity.inchat.view.ConfInfoView;
import com.infowarelab.conference.ui.activity.inchat.view.ConfSettingView;
import com.infowarelab.conference.ui.activity.inchat.view.ConfVideoView;
import com.infowarelab.conference.ui.activity.inchat.view.video.LocalVideoView;
import com.infowarelab.conference.ui.activity.inchat.view.video.VideoSyncView;
import com.infowarelab.conference.ui.activity.inchat.view.vp.ADSAdapter;
import com.infowarelab.conference.ui.activity.inchat.view.vp.ADSViewPager;
import com.infowarelab.conference.ui.activity.inconf.BaseFragment;
import com.infowarelab.conference.ui.activity.preconf.ActHome;
import com.infowarelab.conference.ui.tools.NoDoubleClickListener;
import com.infowarelab.conference.ui.view.CallattDialog;
import com.infowarelab.conference.ui.view.ExitDialog;
import com.infowarelab.conference.ui.view.PopIndex5;
import com.infowarelab.conference.ui.view.WifiDialog;
import com.infowarelab.conference.utils.DisplayUtil;
import com.infowarelab.hongshantongphone.ChatAPI;
import com.infowarelab.hongshantongphone.FileUtils;
import com.infowarelab.hongshantongphone.PreferenceHelp;
import com.infowarelab.hongshantongphone.R;
import com.infowarelab.hongshantongphone.ScreenCapturePresenter;
import com.infowarelab.hongshantongphone.floatView.base.BaseActivity;
import com.infowarelab.hongshantongphone.floatView.float_view.FloatWindowManager;
import com.infowarelab.hongshantongphone.floatView.float_view.LastWindowInfo;
import com.infowarelabsdk.conference.audio.AudioCommon;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.AudioCommonImpl;
import com.infowarelabsdk.conference.common.impl.ConferenceCommonImpl;
import com.infowarelabsdk.conference.common.impl.DocCommonImpl;
import com.infowarelabsdk.conference.common.impl.ShareDtCommonImpl;
import com.infowarelabsdk.conference.common.impl.UserCommonImpl;
import com.infowarelabsdk.conference.common.impl.VideoCommonImpl;
import com.infowarelabsdk.conference.confctrl.ConferenceCommon;
import com.infowarelabsdk.conference.confctrl.UserCommon;
import com.infowarelabsdk.conference.domain.ConferenceBean;
import com.infowarelabsdk.conference.domain.ConfigBean;
import com.infowarelabsdk.conference.domain.LoginBean;
import com.infowarelabsdk.conference.shareDt.ShareDtCommon;
import com.infowarelabsdk.conference.transfer.Config;
import com.infowarelabsdk.conference.util.MessageEvent;
import com.infowarelabsdk.conference.util.StringUtil;
import com.infowarelabsdk.conference.util.ToastUtil;
import com.infowarelabsdk.conference.util.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static android.bluetooth.BluetoothProfile.GATT_SERVER;
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Conference4PhoneActivity extends BaseActivity implements ConfDsView.FragmentInteraction
        , ScreenCaptureStreamCallback,BaseActivity.RequestPermissionCallBack{

    public static final int LOGINUSERERROR = 1;
    public static final int LOGINUSERNAMEERROR = 2;
    public static final int DISMISS = 3;
    public static final int FINISH = 4;
    public static final int DOCONFIG = 5;
    public static final int LOGINFAILED = 6;
    public static final int MEETINGINVALIDATE = 7;
    public static final int MEETINGNOTJOINBEFORE = 8;
    public static final int HOSTERROR = 9;
    public static final int SPELLERROR = 10;
    public static final int GET_ERROR_MESSAGE = 11;
    public static final int JOIN_CONFERENCE = 12;
    public static final int CONNTIMEOUT = 13;
    public static final int ADDPASSWORDEDITOR = 14;
    public static final int READY_JOINCONF = 16;
    public static final int NEED_LOGIN = 1001;
    public static final int NO_CONFERENCE = 1002;
    public static final int LOGIN_VALIDATE_ERRORTIP = 1003;
    protected static final int INIT_SDK = 101;
    protected static final int INIT_SDK_FAILED = 102;
    protected static final int CREATECONF_ERROR = -1;
    protected static final int IDENTITY_FAILED = -2;
    protected static final int CONF_CONFLICT = -5;
    protected static final int BEYOUND_MAXNUM = -7;
    protected static final int PASSWORD_NULL = -9;
    protected static final int CONF_OVER = -10;
    protected static final int NOT_HOST = -16;
    protected static final int BYOUND_STARTTIME = -17;
    protected static final int CHECK_SITE = -18;
    protected static final int SHOWALTER = 10;
    private static final String TAG = "InfowareLab.Debug";
    private static final int CONF_TIME_UPDATE = 2000;
    public static final int EXIT_CONF = 50;

    public int netHabit = 0;//0.未选择 1.wifi 2.net

    private FragmentManager fragmentManager;
    private ADSAdapter vpAdapter;
    private List<Fragment> vpFragments;
    private boolean isSwitching = false;
    private int curPage = 0;
    private ConfVideoView fragVideo;
    private ConfDsView fragDs;
    private ConfASView fragAs;
    private ConfAttendersView fragAtt;
    private ConfInfoView fragInfo;
    private ConfSettingView fragSetting;

    private RelativeLayout rlTopBar;
    private TextView tvTitle;
    private TextView tvConfFinish;
    private ImageView ivAttAdd, ivVsAdd, ivDsAdd;

    private LinearLayout llRecordTime;
    private TextView tvRecordTime;

    private LinearLayout llBottomBar;
    private LinearLayout llIndex1, llMic, llIndex2, llIndex3, llIndex4, llIndex5;
    private ImageView ivIndex1, ivIndex2, ivCamera, ivIndex3, ivIndex4, ivIndex5;
    private TextView tvIndex1, tvIndex2, tvCamera, tvIndex3, tvIndex4, tvIndex5;
    private PopIndex5 popIndex5;

    private ADSViewPager vpCtrl;
    private FrameLayout flCtrl;
    private LocalVideoView localVideo;
//    private View placeTop;
//    private View placeBottom;

    private AudioManager audioManager;
    private boolean isHaveStoped = false;
    public  int position = 1;
    private int state = -1;
    private boolean handsFree = true;

    private boolean isConnWifi = true;
    private boolean isConfJoined = false;
    private BroadcastReceiver breceiver = null;
    private AudioCommonImpl audioCommon;
    private ShareDtCommonImpl asCommon;
    private UserCommonImpl userCommon;
    private ConferenceCommonImpl confCommon;
    private Handler confHandler, audioHandler, asHandler; //1. added the asHandler
    private TelephonyManager tpm;

    public  static int pWidth = 0;
    public  static int pHeight = 0;
    private int bottomHeight = 0;

    private WakeLock mWakeLock = null;
    public boolean isJump2Img = false;

    public boolean isWaitingCloudCallback = false;
    //是否有桌面共享权限
    private boolean bAS = false;
    //是否正在桌面共享
    public boolean isShare = false;

    private ScreenCapturePresenter screenCapturePresenter;
    private PreferenceHelp help;

    private File _fr = null;
    private FileOutputStream _out = null;

    private boolean isMerge = false;
    private boolean isWriteFile = false;

    private byte[] sps;
    private byte[] pps;
    private ImageView ivMic;
    private TextView tvSpeaker;
    private ImageView ivSpeaker;
    private ConferenceBean mConfBean = new ConferenceBean();
    private Config config;
    private ImageView ivFace;
    private TextView tvInviteName;
    private TextView tvInviteText;
    private TextView tvConfTime;
    private String mUserName;
    private String mConfId;
    private String mConfPwd;
    private boolean mAccepted = false;
    private int mUserId = 0;
    private LinearLayout llSpeaker;
    private LinearLayout llCamera;
    private ImageView ivAccept;
    private ImageView ivHangup;
    private ImageView ivHangup2;
    private ImageView ivCameraSwitch;
    private boolean mConfConnecting = false;
    private int mActionMode = -1;
    private String mInviteName = "";
    private Handler userHandler = null;
    private int mTopMargin = 100;

    private long mConfStartTime = 0;
    private boolean mConfDisconnected = false;
    private int mRemoteUserCount = 0;
    private TextView tvMic;

    private boolean showControlByTouch = false;
    public boolean audioChecked = false;
    private Bitmap mInviteeFace = null;
    private Bitmap mInviterFace = null;

    @Override
    protected int getLayoutResId() {

        Log.d(TAG, "Conf4Phone.getLayoutId())");
        return R.layout.chat_phone;
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

    public int getNavigationBarHeight(Context context) {
        int result = 0;
        int resourceId=0;
        int rid = this.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
        if (rid!=0){
            resourceId = this.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            //CMLog.show("高度："+resourceId);
            //CMLog.show("高度："+context.getResources().getDimensionPixelSize(resourceId) +"");
            return this.getResources().getDimensionPixelSize(resourceId);
        }else
            return 0;
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

    private void getScreenWidthHeight() {
        DisplayMetrics dm = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        }else{
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

        ConferenceApplication.StateBar_H = getStatusBarHeight(this);
        if (isNavigationBarShow())
            ConferenceApplication.NavigationBar_H = getNavigationBarHeight(this);//ConferenceApplication.Screen_H - ConferenceApplication.Root_H - ConferenceApplication.StateBar_H
        else
            ConferenceApplication.NavigationBar_H = 0;

        ConferenceApplication.Root_W = ConferenceApplication.Screen_W;
        ConferenceApplication.Root_H = ConferenceApplication.Screen_H - ConferenceApplication.StateBar_H - ConferenceApplication.NavigationBar_H;
        //ConferenceApplication.Root_H = llRoot.getHeight()>0?llRoot.getHeight():ConferenceApplication.Screen_H;
        //ConferenceApplication.Root_W = llRoot.getWidth()>0?llRoot.getWidth():ConferenceApplication.Screen_W;

        ConferenceApplication.Top_H = getResources().getDimensionPixelOffset(R.dimen.height_6_80);
        ConferenceApplication.Bottom_H = getResources().getDimensionPixelOffset(R.dimen.height_7_80);

        Log.d("InfowareLab.Debug","Logo ScreenW="+ConferenceApplication.Screen_W
                +" ScreenH="+ConferenceApplication.Screen_H
                +" RootW="+ConferenceApplication.Root_W
                +" RootH="+ConferenceApplication.Root_H
                +" StateH="+ConferenceApplication.StateBar_H
                +" KeyH="+ConferenceApplication.NavigationBar_H
                +" Density="+ConferenceApplication.DENSITY
                +" Top_H="+ConferenceApplication.Top_H
                +" Bottom_H="+ConferenceApplication.Bottom_H);

        Log.d("InfowareLab.Debug","density = " + ConferenceApplication.DENSITY + ", widthPixels = " + ConferenceApplication.SCREEN_WIDTH
                + ", heightPixels = " + ConferenceApplication.SCREEN_HEIGHT);
    }


    @Override
    protected void initData() {

        Log.d(TAG, "Conf4Phone.initData())");

        if (ChatAPI.getInstance().getLastError() != -1000)
        {
            Log.d(TAG, "Conf4Phone.initData: Error and Exit");
            finish();
            return;
        }

        //注册EventBus
        EventBus.getDefault().register(this);
        floatWindowType = FloatWindowManager.FW_TYPE_ROOT_VIEW;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (NetworkInfo.State.CONNECTED == manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState()) {
            ConferenceApplication.isVideoMeeting = true;
        }else{
            ConferenceApplication.isVideoMeeting = false;
        }

        audioCommon = (AudioCommonImpl) CommonFactory.getInstance().getAudioCommon();
        userCommon = (UserCommonImpl) CommonFactory.getInstance().getUserCommon();
        confCommon = (ConferenceCommonImpl) CommonFactory.getInstance().getConferenceCommon();
        asCommon = (ShareDtCommonImpl) CommonFactory.getInstance().getSdCommon();

        screenCapturePresenter = new ScreenCapturePresenter(this);
        help = new PreferenceHelp(this);

        //initAudio();
        initTelephoneListener();
        setmRequestPermissionCallBack(this);
    }

    private void initUIbyIntentAction()
    {
        Intent intent = getIntent();

        mActionMode = intent.getIntExtra("actionMode", -1);

        if (mActionMode < 0) return;

        if (mActionMode == 0) {

            llMic.setVisibility(View.GONE);
            llSpeaker.setVisibility(View.GONE);
            llCamera.setVisibility(View.GONE);
            ivCameraSwitch.setVisibility(View.GONE);
            if (showControlByTouch)
                ivHangup.setVisibility(View.GONE);
            else
                ivHangup.setVisibility(View.VISIBLE);
            ivAccept.setVisibility(View.GONE);
            tvConfTime.setVisibility(View.GONE);
            ivHangup2.setVisibility(View.GONE);

            mInviteName = intent.getStringExtra("inviteName");

            byte buff[]=intent.getByteArrayExtra("face");
            mInviterFace = BitmapFactory.decodeByteArray(buff, 0, buff.length);

            if (ivFace != null) ivFace.setImageBitmap(mInviterFace);

            if (mInviteName.length() <= 0) {
                ivFace.setVisibility(View.GONE);
                tvInviteName.setVisibility(View.GONE);
            }
            else
            {
                ivFace.setVisibility(View.VISIBLE);
                tvInviteName.setVisibility(View.VISIBLE);
            }

            if (tvInviteName != null) tvInviteName.setText(mInviteName);
            if (tvInviteText != null) tvInviteText.setText(R.string.caller_text);

            adjustCameraSwitchPosition();
        }
        else if (mActionMode == 1){

            llMic.setVisibility(View.GONE);
            llSpeaker.setVisibility(View.GONE);
            llCamera.setVisibility(View.GONE);
            ivCameraSwitch.setVisibility(View.GONE);
            if (showControlByTouch)
                ivHangup.setVisibility(View.GONE);
            else
                ivHangup.setVisibility(View.VISIBLE);
            ivAccept.setVisibility(View.GONE);
            tvConfTime.setVisibility(View.GONE);

            adjustCameraSwitchPosition();
        }
        else if (mActionMode == 2){

            mInviteName = intent.getStringExtra("inviteName");

            byte buff[]=intent.getByteArrayExtra("face");
            mInviteeFace = BitmapFactory.decodeByteArray(buff, 0, buff.length);

            if (ivFace != null) ivFace.setImageBitmap(mInviteeFace);

            if (tvInviteName != null) tvInviteName.setText(mInviteName);
            if (tvInviteText != null) tvInviteText.setText(R.string.callee_text);

            llMic.setVisibility(View.GONE);
            llSpeaker.setVisibility(View.GONE);
            llCamera.setVisibility(View.GONE);
            ivCameraSwitch.setVisibility(View.GONE);
            ivHangup.setVisibility(View.GONE);

            tvConfTime.setVisibility(View.GONE);
            //ivHangup2.setVisibility(View.VISIBLE);
            //ivAccept.setVisibility(View.VISIBLE);

            mConfConnecting = true;

            if (showControlByTouch)
                ivHangup.setVisibility(View.GONE);
            else
                ivHangup.setVisibility(View.VISIBLE);

            ivHangup2.setVisibility(View.GONE);
            ivAccept.setVisibility(View.GONE);
            tvInviteText.setText(R.string.callee_joining);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    joinConf(mUserId, mConfId, mConfPwd, mUserName);
                }
            }).start();

            adjustCameraSwitchPosition();

        }
    }

    private void checkIntentAction() {
        Intent intent = getIntent();

        mActionMode = intent.getIntExtra("actionMode", -1);
        if (mActionMode < 0) return;

        if (mActionMode == 0) {

            mConfConnecting = true;
//            String userName = intent.getStringExtra("userName");
//            int userId = intent.getIntExtra("userId", -1);
//            if (userId <= 0) return;
//            String confName = intent.getStringExtra("confName");
//            String confPwd = intent.getStringExtra("confPwd");
//            int duration = intent.getIntExtra("duration", -1);
//            if (duration <= 0) return;
//            String attIds = intent.getStringExtra("attIds");
//            String attNames = intent.getStringExtra("attNames");
//            String attEmails = intent.getStringExtra("attEmails");

//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    createConf(userName, userId, confName, confPwd, duration, attIds, attNames, attEmails);
//                }
//            }).start();
        }
        else if (mActionMode == 1){

            mConfConnecting = true;
//            int userId = intent.getIntExtra("userId", 0);
//            String userName = intent.getStringExtra("userName");
//            String confId = intent.getStringExtra("confId");
//            String confPwd = intent.getStringExtra("confPwd");

//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    joinConf(userId, confId, confPwd, userName);
//                }
//            }).start();
        }
        else if (mActionMode == 2){

            mUserId = intent.getIntExtra("userId", 0);
            mUserName = intent.getStringExtra("userName");
            mConfId = intent.getStringExtra("confId");
            mConfPwd = intent.getStringExtra("confPwd");

            mAccepted = false;
        }
    }

    private void initHandler() {
        initConfHandler();
        initAudioHandler();
        initASHandler(); //2. initialize the asHandler
        ChatAPI.getInstance().setExternalConfHandler(confHandler);
    }

    private void initAudioHandler() {
        audioHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (audioCommon == null) return;
                switch (msg.what) {
                    case AudioCommon.MIC_ON:

                        Log.d("InfowareLab.Debug", "AudioCommon.MIC_ON");

                        showShortToast(R.string.audioon);
                        ivMic.setImageResource(R.drawable.mic_on);
                        tvMic.setText(R.string.mic_on);
                        /*
                        if (audioCommon.isMICWork()) {
                            if (!audioCommon.isRecording()) {
                                tryStartSend();
                                showShortToast(R.string.audioon);
                            }
                            showShortToast(R.string.audioon);
                            ivIndex1.setImageResource(R.drawable.ic_index_1_enable_on);
                            ivMic.setImageResource(R.drawable.mic_on);
                            tvIndex1.setTextColor(getResources().getColor(R.color.index_red));
                        } else {
                            if (!audioCommon.isRecording()){
                                tryStartSend();
                                showShortToast(R.string.audioon);
                            }
                            ivIndex1.setImageResource(R.drawable.ic_index_1_disable_on);
                            ivMic.setImageResource(R.drawable.mic_on);
                            tvIndex1.setTextColor(getResources().getColor(R.color.index_red));
                        }*/
                        audioCommon.isClickUsed = true;
                        break;
                    case AudioCommon.MIC_OFF:

                        Log.d("InfowareLab.Debug", "AudioCommon.MIC_OFF");

                        showShortToast(R.string.audiooff);
                        ivMic.setImageResource(R.drawable.mic_off);
                        tvMic.setText(R.string.mic_off);
                        /*
                        if (audioCommon.isMICWork()) {
                            if (audioCommon.isRecording()){
                                audioCommon.stopSend();
                                showShortToast(R.string.audiooff);
                            }
                            ivIndex1.setImageResource(R.drawable.ic_index_1_disable_on);
                            ivMic.setImageResource(R.drawable.mic_off);
                            tvIndex1.setTextColor(getResources().getColor(R.color.index_red));
                        } else {
                            if (audioCommon.isRecording()){
                                audioCommon.stopSend();
                                showShortToast(R.string.audiooff);
                            }
                            ivIndex1.setImageResource(R.drawable.ic_index_1_disable_off);
                            ivMic.setImageResource(R.drawable.mic_off);
                            tvIndex1.setTextColor(getResources().getColor(R.color.index_white));
                        }*/
                        audioCommon.isClickUsed = true;
                        break;
                    case AudioCommon.MIC_ENABLE:

                        if (audioCommon.isRecording()) {
                            ivIndex1.setImageResource(R.drawable.ic_index_1_enable_on);
                            ivMic.setImageResource(R.drawable.mic_on);
                            tvIndex1.setTextColor(getResources().getColor(R.color.index_red));
                        } else {
                            ivIndex1.setImageResource(R.drawable.ic_index_1_enable_off);
                            ivMic.setImageResource(R.drawable.mic_off);
                            tvIndex1.setTextColor(getResources().getColor(R.color.index_white));
                        }
                        break;
                    case AudioCommon.MIC_DISABLE:
                        if (audioCommon.isRecording()) {
                            ivIndex1.setImageResource(R.drawable.ic_index_1_disable_on);
                            ivMic.setImageResource(R.drawable.mic_off);
                            tvIndex1.setTextColor(getResources().getColor(R.color.index_red));
                        } else {
                            ivIndex1.setImageResource(R.drawable.ic_index_1_disable_off);
                            ivMic.setImageResource(R.drawable.mic_off);
                            tvIndex1.setTextColor(getResources().getColor(R.color.index_white));
                        }
                        break;
                    case AudioCommon.MIC_ENABLE_NOTICE:
                        showShortToast(R.string.audio_permission_on);
                        break;
                    case AudioCommon.MIC_DISABLE_NOTICE:
                        showShortToast(R.string.audio_permission_off);
                        break;
                    case AudioCommon.MIC_NOPERMISSION:
                        showLongToast(R.string.novoicepermission);
                        break;
                    default:
                        break;
                }
            }
        };
        audioCommon.setHandler(audioHandler);
    }

    //3. implement the asHandler processing
    private void initASHandler() {

        asHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case ShareDtCommon.START_SHARE_DT:
                        asCommon.setHandler(null);
                        switchFragment(3);
                        Log.d("InfowareLab.Debug", ".ShareDtCommon.START_SHARE_DT");
                        break;
                    case ShareDtCommon.INIT_BROWSER:
                        asCommon.setHandler(null);
                        switchFragment(3);
                        Log.d("InfowareLab.Debug", ".ShareDtCommon.INIT_BROWSER");
                    default:
                        break;
                }
            }
        };

        //asCommon.subscribeWithMode(1);
        asCommon.setHandler(asHandler);
    }

    private void initConfHandler() {
        confHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case CONF_TIME_UPDATE: {
                        long currentTime = System.currentTimeMillis();
                        long timeElapsed = currentTime - mConfStartTime;

                        long hour = (timeElapsed / (60 * 60 * 1000));
                        long min = ((timeElapsed / (60 * 1000)) - hour * 60);
                        long s = (timeElapsed / 1000 - hour * 60 * 60 - min * 60);

//                        if (ConferenceApplication.mConfId.length() > 0) {
//                            String time = String.format("(%s)：%02d:%02d:%02d", ConferenceApplication.mConfId, hour, min, s);
//
//                            tvConfTime.setText(time);
//                        }
//                        else {
                            String time = String.format("%02d:%02d:%02d", hour, min, s);

                            tvConfTime.setText(time);
 //                       }


                        if (mRemoteUserCount >= 1){
                            confHandler.sendEmptyMessageDelayed(CONF_TIME_UPDATE, 900);
                        }
                    }

                    break;
                    case NEED_LOGIN:
                        Log.d(TAG, "Conf4Phone.NEED_LOGIN");
                        showLongToast(R.string.needLogin);
                        mConfConnecting = false;
                        break;
                    case NO_CONFERENCE:
                        Log.d("InfowareLab.Debug","Conference4PhoneActivity: NO_CONFERENCE");
                        //fragJoin.hideLoading();
                        showLongToast(R.string.noConf);
                        mConfConnecting = false;
                        break;
                    case GET_ERROR_MESSAGE:
                        Log.d("InfowareLab.Debug","Conference4PhoneActivity: GET_ERROR_MESSAGE");
                        //fragJoin.hideLoading();
                        //showLongToast(config.getConfigBean().getErrorMessage());
                        mConfConnecting = false;
                        break;
                    case MEETINGNOTJOINBEFORE:
                        Log.d("InfowareLab.Debug","Conference4PhoneActivity: MEETINGNOTJOINBEFORE");
                        //fragJoin.hideLoading();
                        showLongToast(R.string.meetingNotJoinBefore);
                        mConfConnecting = false;
                        break;
                    case HOSTERROR:
                        Log.d("InfowareLab.Debug","Conference4PhoneActivity: HOSTERROR");
                        //fragJoin.hideLoading();
                        showLongToast(R.string.hostError);
                        mConfConnecting = false;
                        break;
                    case SPELLERROR:
                        Log.d("InfowareLab.Debug","Conference4PhoneActivity: SPELLERROR");
                        //fragJoin.hideLoading();
                        showLongToast(R.string.spellError);
                        mConfConnecting = false;
                        break;
                    case LOGINFAILED:
                        Log.d("InfowareLab.Debug","Conference4PhoneActivity: LOGINFAILED");
                        //fragJoin.hideLoading();
                        showLongToast(R.string.LoginFailed);
                        mConfConnecting = false;
                        break;
                    case CREATECONF_ERROR:
                        Log.d("InfowareLab.Debug","Conference4PhoneActivity: CREATECONF_ERROR");
                        //fragJoin.hideLoading();
                        showLongToast(R.string.preconf_create_error);
                        mConfConnecting = false;
                        exit();
                        break;
                    case MEETINGINVALIDATE:
                        Log.d("InfowareLab.Debug","Conference4PhoneActivity: MEETINGINVALIDATE");
                        //fragJoin.hideLoading();
                        showLongToast(R.string.overConf);
                        mConfConnecting = false;
                        break;
                    case READY_JOINCONF:
                        Log.d("InfowareLab.Debug","Conference4PhoneActivity: READY_JOINCONF");
                        break;
                    case FINISH:
                        Log.d("InfowareLab.Debug","Conference4PhoneActivity: FINISH");
                        break;
                    case JOIN_CONFERENCE:
                        Log.d("InfowareLab.Debug","Conference4PhoneActivity.JOIN_CONFERENCE");
                        confCommon.setLogPath(ConferenceApplication.getConferenceApp().getFilePath("Log"));
                        confCommon.setHandler(confHandler);
                        confCommon.initSDK();
                        joinConference();
                        break;
                    case ConferenceCommon.RESULT_SUCCESS:
                        Log.d("InfowareLab.Debug", "Conference4PhoneActivity.RESULT_SUCCESS: Join success");
                        //Intent intent = new Intent(mContext, Conference4PhoneActivity.class);
                        //mContext.startActivity(intent);
                        //((AudioCommonImpl)commonFactory.getAudioCommon()).onOpenAudioConfirm(true);
                        //showShortToast(R.string.join_suces);
                        mConfConnecting = false;
                        audioCommon.isClickUsed = false;

//                        String time = String.format("(会议号: %s)", ConferenceApplication.mConfId);
//
//                        tvConfTime.setText(time);
//                        tvConfTime.setVisibility(View.VISIBLE);

                        initAudio();
                        initRecord();

                        if (!showControlByTouch) {
                            llMic.setVisibility(View.VISIBLE);
                            llSpeaker.setVisibility(View.VISIBLE);
                            llCamera.setVisibility(View.VISIBLE);
                            ivCameraSwitch.setVisibility(View.VISIBLE);
                        }

                        //adjustCameraSwitchPosition();
                        //ivFace.setVisibility(View.GONE);

                        isConfJoined = true;

                        break;
                    case INIT_SDK:
                        Log.d("InfowareLab.Debug", "Conference4PhoneActivity.INIT_SDK: initSDK success");
                        //showLongToast(R.string.ini);
                        break;
                    case INIT_SDK_FAILED:
                        Log.d("InfowareLab.Debug", "Conference4PhoneActivity.INIT_SDK_FAILED: initSDK failed");
                        showLongToast(R.string.initSDKFailed);
                        break;
                    case CONF_CONFLICT:
                        showLongToast(R.string.confConflict);
                        mConfConnecting = false;
                        break;
                    case IDENTITY_FAILED:
                        showLongToast(R.string.preconf_create_error_2);
                        mConfConnecting = false;
                        break;
                    case BEYOUND_MAXNUM:
                        showLongToast(R.string.preconf_create_error_2);
                        mConfConnecting = false;
                        break;
                    case PASSWORD_NULL:
                        showLongToast(R.string.preconf_create_error_9);
                        mConfConnecting = false;
                        break;
                    case CONF_OVER:
                        showLongToast(R.string.preconf_create_error_10);
                        mConfConnecting = false;
                        break;
                    case NOT_HOST:
                        showLongToast(R.string.preconf_create_error_16);
                        mConfConnecting = false;
                        break;
                    case BYOUND_STARTTIME:
                        showLongToast(R.string.preconf_create_error_17);
                        mConfConnecting = false;
                        break;
                    case CHECK_SITE:
                        showLongToast(R.string.site_error);
                        mConfConnecting = false;
                        break;
                    case ConferenceCommon.LEAVE:
                        if ((Integer) msg.obj == ConferenceCommon.FORCELEAVE) {
                            showShortToast(R.string.forceLeave);
                        } else if ((Integer) msg.obj == ConferenceCommon.HOSTCLOSECONF) {
                            showShortToast(R.string.leave);
                        } else if ((Integer) msg.obj == 40100) {
                            showShortToast(R.string.offlineLeave);
                        }
                        mConfConnecting = false;
                        exit();
                        break;
                    case ConferenceCommon.CALLATT:
                        if ((Boolean) msg.obj) {
                            showCallattDialog(msg.arg1, msg.arg2);
                        } else {
                            hideCallattDialog();
                        }
                        break;
                    case ConferenceCommon.CLOUDRECORD:
                        isWaitingCloudCallback = false;

                        if ((Boolean) msg.obj) {
                            startTiming(1);
                            if (popIndex5 != null && popIndex5.isShowing()){
                                popIndex5.updateRec(true);
                                popIndex5.dismiss();
                            }
                            if (fragVideo != null) fragVideo.beginCloudRecord(0);
                            if (fragDs != null) fragDs.beginCloudRecord();
                        } else {
                            stopTiming(1);
                            if (popIndex5 != null && popIndex5.isShowing()){
                                popIndex5.updateRec(false);
                                popIndex5.dismiss();
                            }
                            if (fragVideo != null) fragVideo.stopCloudRecord(0);
                        }
                        break;
                    case EXIT_CONF:
                        exit();
                        break;
                    case 999:
                        if (homeCount == 0) {
                            //Intent intent = new Intent(Conference4PhoneActivity.this, ActHome.class);
                            //startActivity(intent);
                            homeCount++;
                        }
                        finish();
                        break;

                    default:
                        break;
                }
            }
        };
        DocCommonImpl.isAnnotation = false;
        confCommon.setHandler(confHandler);
    }

    private void adjustCameraSwitchPosition() {
        RelativeLayout.LayoutParams cameraLayoutParams = (RelativeLayout.LayoutParams)llCamera.getLayoutParams();
        RelativeLayout.LayoutParams targetLayoutParams = (RelativeLayout.LayoutParams)ivCameraSwitch.getLayoutParams();

        llCamera.measure(0,0);
        ivCameraSwitch.measure(0,0);

        //Log.d(TAG, "adjustCameraSwitchPosition:llCamera.width= " + llCamera.getMeasuredWidth());
        //Log.d(TAG, "adjustCameraSwitchPosition:ivCameraSwitch.width= " + ivCameraSwitch.getMeasuredWidth());

        targetLayoutParams.setMargins(0,0,(llCamera.getMeasuredWidth() - ivCameraSwitch.getMeasuredWidth())/2,(llCamera.getMeasuredHeight() - ivCameraSwitch.getMeasuredHeight())/2);
        //Log.d(TAG, "adjustCameraSwitchPosition: " + targetLayoutParams.leftMargin + "," + targetLayoutParams.topMargin + "," + targetLayoutParams.rightMargin  + "," + targetLayoutParams.bottomMargin);
    }

    public String createConf(String userName, int userId, String confName, String confPwd, int duration, String attIds, String attNames, String attEmails){

        if (userId <= 0) return null;

        String confId = null;

        //设置会议开始时间为1小时后
        mConfBean.setName(confName);
        if(mConfBean.getName() == null || mConfBean.getName().toString().equals("")){
            mConfBean.setName("NONAME");
        }

        mConfBean.setConfPassword(confPwd);
        mConfBean.setConfType("0");
        mConfBean.setConferencePattern(1);
        final Date curDate = new Date(System.currentTimeMillis() + 10*60*1000);
        mConfBean.setStartTime(StringUtil.dateToStrInLocale(curDate, "yyyy-MM-dd HH:mm:ss"));
        mConfBean.setDuration(""+duration);

        if(duration>120){
            //设置会议开始时间为1小时后
            if(attIds!=null&&!attIds.equals("")){
                Config.confId = Config.getFixedConfIdCovert(userId,userName,Config.SiteId, mConfBean, attIds,attNames,attEmails);
            }else{
                Config.confId = Config.getFixedConfId(userId,userName,Config.SiteId, mConfBean);
            }
        }else {
            if(attIds!=null&&!attIds.equals("")){
                Config.confId = Config.getConfIdCovert(userId,userName,Config.SiteId, mConfBean,attIds,attNames,attEmails);
            }else {
                Config.confId = Config.getConfId(userId,userName, Config.SiteId, mConfBean);
            }
        }

        confId = Config.confId;

        Log.d("InfowareLab.Debug","Conference4PhoneActivity.createConf: confId = "+confId);

        if(confId.startsWith("0")){
            confId = confId.substring(2);
            mConfBean.setId(confId);
            Config.confId = confId;

            ChatAPI.getInstance().sendInviteMessage(confId);

            Log.d("InfowareLab.Debug","Config.startConf: confId = "+Config.confId);
            String result = Config.startConf(userId,userName, Config.SiteId, mConfBean);

            if(result.equals("-1:error")){
                confHandler.sendEmptyMessage(CREATECONF_ERROR);
            }else{
                startConf(userId, Config.confId, confPwd, userName);
            }

        }else{
            Log.d("InfowareLab.Debug","ChatAPI.createConf: confId = "+confId);
            //mConfHandler.sendEmptyMessage(Integer.parseInt(confId));
        }

        return confId;
    }

    public void startConf(int userId, String confId, String confPwd, String joinName) {

        Log.d("InfowareLab.Debug","Conference4PhoneActivity.startConf: confId = "+confId);

        confCommon.setLogPath(ConferenceApplication.getConferenceApp().getFilePath("Log"));
        confCommon.initSDK();

        config = confCommon.getConfig();
        LoginBean loginBean = new LoginBean(confId, joinName,confPwd);
        loginBean.setUid(userId);

        confCommon.joinConference(confCommon.getParam(loginBean, true));
        config.setMyConferenceBean(mConfBean);
        ConferenceApplication.getConferenceApp().setJoined(true);
    }

    private void joinConference() {
        Config config = confCommon.getConfig();
        if (config != null) {
            ConfigBean configBean = config.getConfigBean();
            if (configBean != null) {
                configBean.setUserInfo_m_dwStatus(ConferenceCommon.RT_STATE_RESOURCE_AUDIO);
            } else
                Log.e("InfowareLab.Debug","configBean is null");
        } else
            Log.e("InfowareLab.Debug","config is null");
        Log.e("InfowareLab.Debug","呼叫信息:"+confCommon.getParam());
        confCommon.setMeetingBox();
        confCommon.joinConference(confCommon.getParam());
    }

    public void joinConf(int userId, String confId, String confPwd, String joinName) {

        Log.d("InfowareLab.Debug","Conference4PhoneActivity.joinConf: confId = "+confId);

        ConferenceBean confBean = Config.getConferenceByNumber(confId);

        if(confBean==null||!confBean.getHostID().equals(String.valueOf(userId))||!confBean.getStatus().equals("0")){
        }else {
            mConfBean = confBean;
            startConf(userId, confId, confPwd, joinName);
            return;
        }

        LoginBean loginBean = new LoginBean(confId, joinName, confPwd);
        loginBean.setUid(userId);
        loginBean.setType(Config.MEETING);

        config = confCommon.initConfig(loginBean);
        if (config.getConfigBean() == null) {
            Log.d("InfowareLab.Debug","configbean is null");
        }
        if (config.getConfigBean().getErrorCode() == null) {
            Log.d("InfowareLab.Debug","errorcode is null");
        }
        Log.d("InfowareLab.Debug","joinConf " + config.getConfigBean().getErrorCode() + ":" + config.getConfigBean().getErrorMessage());

        if ("0".equals(config.getConfigBean().getErrorCode())) {
            confHandler.sendEmptyMessage(JOIN_CONFERENCE);
        } else {
            confCommon.setJoinStatus(
                    ConferenceCommon.NOTJOIN);
            if ("-1".equals(config.getConfigBean().getErrorCode())) {
                if (config.getConfigBean().getErrorMessage().startsWith("0x0604003")) {
                    if (config.getConfigBean().getErrorMessage().equals("0x0604003:you should login to meeting system! ")) {
                        //loginSystem();
                        confHandler.sendEmptyMessage(NEED_LOGIN);
                    } else {
                        confHandler.sendEmptyMessage(FINISH);
                    }
                } else {
                    confHandler.sendEmptyMessage(LOGINFAILED);
                }
            } else if ("-2".equals(config.getConfigBean().getErrorCode())) {
                confHandler.sendEmptyMessage(FINISH);
            } else if ("-10".equals(config.getConfigBean().getErrorCode())) {
//                if (Config.HAS_LIVE_SERVER) {
//                    if (confBean.getType().equals(Config.MEETING)) {
//                        confBean.setType(Config.LIVE);
//                        //joinConf(confBean, getLoginBean(confBean, loginBean.getPassword()));
//                        return;
//                    }
//                }
                confHandler.sendEmptyMessage(MEETINGINVALIDATE);

            } else if ("-18".equals(config.getConfigBean().getErrorCode())) {
                confHandler.sendEmptyMessage(MEETINGNOTJOINBEFORE);
            } else if (ConferenceCommon.HOSt_ERROR.equals(config.getConfigBean().getErrorCode())) {
                confHandler.sendEmptyMessage(HOSTERROR);
            } else if (ConferenceCommon.SPELL_ERROR.equals(config.getConfigBean().getErrorCode())) {
                ////log.info("spell error");
                confHandler.sendEmptyMessage(SPELLERROR);
            } else {
                confHandler.sendEmptyMessage(GET_ERROR_MESSAGE);
            }
        }
    }

    @Override
    protected void initView() {

        Log.d(TAG, "Conf4Phone.initView");

        if (ChatAPI.getInstance().getLastError() != -1000)
        {
            Log.d(TAG, "Conf4Phone.initView: Error and Exit");
            finish();
            return;
        }

        getScreenWidthHeight();

        vpCtrl = (ADSViewPager) findViewById(R.id.vp_inconf_ctrl);
        flCtrl = (FrameLayout) findViewById(R.id.fl_inconf_ctrl);

//        placeTop = findViewById(R.id.view_inconf_ctrl_place_top);
//        placeBottom = findViewById(R.id.view_inconf_ctrl_place_bottom);

        rlTopBar = (RelativeLayout) findViewById(R.id.rl_inconf_ctrl_top);
        tvTitle = (TextView) findViewById(R.id.tv_inconf_ctrl_title);
        tvConfFinish = (TextView) findViewById(R.id.tv_inconf_ctrl_conffinish);
        ivAttAdd = (ImageView) findViewById(R.id.iv_inconf_ctrl_att_add);
        ivVsAdd = (ImageView) findViewById(R.id.iv_inconf_ctrl_vs_add);
        ivDsAdd = (ImageView) findViewById(R.id.iv_inconf_ctrl_ds_add);

        llRecordTime = (LinearLayout) findViewById(R.id.ll_inconf_ctrl_record);
        tvRecordTime = (TextView) findViewById(R.id.tv_inconf_ctrl_recordtime);

        llBottomBar = (LinearLayout) findViewById(R.id.ll_inconf_ctrl_bottom);
        llIndex1 = (LinearLayout) findViewById(R.id.ll_inconf_ctrl_index_1);
        llMic = (LinearLayout) findViewById(R.id.ll_inconf_ctrl_mic);
        llSpeaker = (LinearLayout) findViewById(R.id.ll_inconf_ctrl_volume);
        llCamera = (LinearLayout) findViewById(R.id.ll_inconf_ctrl_camera);

        llIndex2 = (LinearLayout) findViewById(R.id.ll_inconf_ctrl_index_2);
        llIndex3 = (LinearLayout) findViewById(R.id.ll_inconf_ctrl_index_3);
        llIndex4 = (LinearLayout) findViewById(R.id.ll_inconf_ctrl_index_4);
        llIndex5 = (LinearLayout) findViewById(R.id.ll_inconf_ctrl_index_5);
        ivIndex1 = (ImageView) findViewById(R.id.iv_inconf_ctrl_index_1);
        ivMic = (ImageView) findViewById(R.id.iv_inconf_ctrl_mic);
        tvMic = (TextView) findViewById(R.id.tv_inconf_ctrl_mic);
        
        ivIndex2 = (ImageView) findViewById(R.id.iv_inconf_ctrl_index_2);
        ivCamera = (ImageView) findViewById(R.id.iv_inconf_ctrl_camera);
        ivIndex3 = (ImageView) findViewById(R.id.iv_inconf_ctrl_index_3);
        ivIndex4 = (ImageView) findViewById(R.id.iv_inconf_ctrl_index_4);
        ivIndex5 = (ImageView) findViewById(R.id.iv_inconf_ctrl_index_5);
        tvIndex1 = (TextView) findViewById(R.id.tv_inconf_ctrl_index_1);
        tvIndex2 = (TextView) findViewById(R.id.tv_inconf_ctrl_index_2);
        tvCamera = (TextView) findViewById(R.id.tv_inconf_ctrl_camera);
        tvIndex3 = (TextView) findViewById(R.id.tv_inconf_ctrl_index_3);
        tvIndex4 = (TextView) findViewById(R.id.tv_inconf_ctrl_index_4);
        tvIndex5 = (TextView) findViewById(R.id.tv_inconf_ctrl_index_5);

        ivSpeaker = (ImageView) findViewById(R.id.iv_inconf_ctrl_speaker);
        tvSpeaker = (TextView) findViewById(R.id.tv_inconf_ctrl_speaker);

        ivFace = (ImageView) findViewById(R.id.iv_inconf_ctrl_profile_picture);
        tvInviteName = (TextView) findViewById(R.id.tv_inconf_ctrl_profile_name);
        tvInviteText = (TextView) findViewById(R.id.tv_inconf_ctrl_profile_text);
        tvConfTime = (TextView) findViewById(R.id.tv_inconf_ctrl_time);

        ivAccept = (ImageView) findViewById(R.id.iv_inconf_ctrl_accept);
        ivHangup = (ImageView) findViewById(R.id.iv_inconf_ctrl_hangup);
        ivHangup2 = (ImageView) findViewById(R.id.iv_inconf_ctrl_hangup2);
        ivCameraSwitch = (ImageView) findViewById(R.id.iv_inconf_ctrl_camera_switch);

        checkIntentAction();
        initUIbyIntentAction();
        
        NoDoubleClickListener noDoubleClickListener = new NoDoubleClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                int id = v.getId();
                if (id == R.id.ll_inconf_ctrl_index_1 || id == R.id.ll_inconf_ctrl_mic) {
                    if (audioCommon.isMICWork()) {

                        if (!audioCommon.isClickUsed){
                            showShortToast(R.string.audioinprocess);
                            return;
                        }

                        if (audioCommon.isRecording()) {
                            audioCommon.isClickUsed = false;
                            audioCommon.stopSend();
                            //showShortToast(R.string.audiooff);
                        } else {
                            audioCommon.isClickUsed = false;
                            audioCommon.startSend();
                            //showShortToast(R.string.audioon);
                        }
                    } else {
                        showShortToast(R.string.audiodisable);
                    }
                }  else if (id == R.id.ll_inconf_ctrl_index_2) {
                    if (curPage != 2) {
                        switchFragment(2);

                        if (fragVideo.isLocalVideoOpened()) {
                            showShortToast(R.string.cameraon);
                        } else {
                            showShortToast(R.string.cameraoff);
                        }

                        return;
                    }

                    if (fragVideo.isLocalVideoOpened()) {
                        fragVideo.enableCamera(false);
                        fragVideo.closeMyVideo();
                        ivIndex2.setImageResource(R.drawable.ic_index_2_sel_off);
                        showShortToast(R.string.cameraoff);
                    } else {
                        fragVideo.enableCamera(true);
                        //if (userCommon.getSelf().isShareVideo())
                        fragVideo.openMyVideo();;
                        ivIndex2.setImageResource(R.drawable.ic_index_2_sel);
                        showShortToast(R.string.cameraon);
                    }

                } else if (id == R.id.ll_inconf_ctrl_index_3) {
                    switchFragment(3);
                } else if (id == R.id.ll_inconf_ctrl_index_4) {
                    switchFragment(4);
                } else if (id == R.id.ll_inconf_ctrl_index_5) {
                    showIndex5Pop(v);
                } else if (id == R.id.tv_inconf_ctrl_conffinish) {
                    showExitDialog();
                }
            }
        };
        llIndex1.setOnClickListener(noDoubleClickListener);
        llMic.setOnClickListener(noDoubleClickListener);
        llIndex2.setOnClickListener(noDoubleClickListener);
        llIndex3.setOnClickListener(noDoubleClickListener);
        llIndex4.setOnClickListener(noDoubleClickListener);
        llIndex5.setOnClickListener(noDoubleClickListener);
        tvConfFinish.setOnClickListener(noDoubleClickListener);

        initAudioFilter();
        initReceiver();

        initHandler();
        initIndexFrame();
        //initRecord();

        //added for monitoring the connection status
        //resetUserHandler();

        int textHeight = DisplayUtil.getTextViewHeight(DisplayUtil.sp2px(getApplicationContext(),15));
        mTopMargin = textHeight + DisplayUtil.dip2px(getApplicationContext(), 6);

        mTopMargin += 2 * getResources().getDimensionPixelOffset(R.dimen.dp_3);

        if (fragVideo != null) fragVideo.setTopMargin(mTopMargin);

        VideoCommonImpl videoCommon =((VideoCommonImpl) CommonFactory.getInstance().getVideoCommon());

        if (ChatAPI.getInstance().getConfType() == 0){
             if (videoCommon != null){
                 Log.d(TAG, "Conf4Phone.setMaxChannelCount = 2");
                 videoCommon.setMaxChannelCount(2);
            }
            if (userCommon != null){
                Log.d(TAG, "Conf4Phone.setMaxUserCount = 2");
                userCommon.setMaxUserCount(2);
            }
        }
        else
        {
            if (videoCommon != null){
                Log.d(TAG, "Conf4Phone.setMaxChannelCount = 9");
                videoCommon.setMaxChannelCount(9);
            }

            if (userCommon != null){
                Log.d(TAG, "Conf4Phone.setMaxUserCount = 9");
                userCommon.setMaxUserCount(9);
            }
        }
    }

    private boolean isTouchPointInView(View view, int x, int y) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int left = location[0];
        int top = location[1];
        int right = left + view.getMeasuredWidth();
        int bottom = top + view.getMeasuredHeight();
         if (view.isClickable() && y >= top && y <= bottom && x >= left && x <= right) {
            return true;
        }

         return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if (!showControlByTouch) return super.dispatchTouchEvent(ev);

        if(ev.getAction()==MotionEvent.ACTION_DOWN){

            int rawX = (int) ev.getRawX();
            int rawY = (int) ev.getRawY();

            boolean inMic = isTouchPointInView(llMic, rawX, rawY);
            boolean inSpeaker = isTouchPointInView(llSpeaker, rawX, rawY);
            boolean inCamera = isTouchPointInView(llCamera, rawX, rawY);
            boolean inCameraSwitch = isTouchPointInView(ivCameraSwitch, rawX, rawY);
            boolean inHangup = isTouchPointInView(ivHangup, rawX, rawY);

            if (inMic || inSpeaker || inCamera || inCameraSwitch || inHangup)
                return super.dispatchTouchEvent(ev);

            if (ivHangup.getVisibility() == View.VISIBLE) {
                llMic.setVisibility(View.GONE);
                llSpeaker.setVisibility(View.GONE);
                llCamera.setVisibility(View.GONE);
                ivCameraSwitch.setVisibility(View.GONE);
                ivHangup.setVisibility(View.GONE);
            } else {
                llMic.setVisibility(View.VISIBLE);
                llSpeaker.setVisibility(View.VISIBLE);
                llCamera.setVisibility(View.VISIBLE);
                ivCameraSwitch.setVisibility(View.VISIBLE);
                ivHangup.setVisibility(View.VISIBLE);
            }
        }

        return super.dispatchTouchEvent(ev);
    }

    private void initIndexFrame() {

        Log.d(TAG, "Conf4Phone.initIndexFrame");

        fragmentManager = getSupportFragmentManager();

        vpFragments = new ArrayList<>();
        if (null == fragDs) {
            fragDs = new ConfDsView(this);
            ivDsAdd.setOnClickListener(fragDs);
        }
        if (null == fragAs) {
            fragAs = new ConfASView(this);
        }
        vpFragments.add(fragAs);
        vpFragments.add(fragDs);

        vpAdapter = new ADSAdapter(fragmentManager, vpFragments);
        vpCtrl.setAdapter(vpAdapter);
        vpCtrl.setVisibility(View.GONE);

        FragmentTransaction ft;
        ft = fragmentManager.beginTransaction();
        if (null == fragVideo) {
            fragVideo = new ConfVideoView(this);
            ivVsAdd.setOnClickListener(fragVideo);

//            if (mActionMode == 0){
//                if (mInviterFace != null) fragVideo.setMyFace(mInviterFace);
//            }

        }
        ft.add(R.id.fl_inconf_ctrl, fragVideo, "Video");

        if (null == fragAtt) {
            fragAtt = new ConfAttendersView(this);
        }
        ft.add(R.id.fl_inconf_ctrl, fragAtt, "Att");

        ft.hide(fragAtt).show(fragVideo).commit();
        setIndexState(2, true);
        curPage = 2;

        //openOrietation();
    }

    private void showIndex5Pop(View v) {
        popIndex5 = new PopIndex5(this, curPage, new PopIndex5.OnSelectListener() {
            @Override
            public void onSelectSync() {

            }

            @Override
            public void onSelectInfo() {
                switchFragment(5);
            }

            @Override
            public void onSelectSetting() {
                switchFragment(6);
            }

            @Override
            public void onSelectRec() {
                clickRecord();
            }

            @Override
            public void onSelectChatting() {

            }
        });
        popIndex5.showAsDropDown(v);
    }

    //4. modify the switching method
    public void switchFragment(int number) {
        shareSetHandler.removeCallbacksAndMessages(null);
        delaySkipHandler.removeCallbacksAndMessages(null);
        if (curPage == 3 && number != 3) {
            asCommon.setHandler(asHandler); //change
            fragAs.dohideView();
            shareSetHandler.sendEmptyMessage(number);
        } else if (curPage == 2 && number != 2) {
            fragVideo.doHideView();
            if (number == 3) {
                vpCtrl.setCurrentItem(0); //change
                vpCtrl.setVisibility(View.VISIBLE);
                delaySkipHandler.sendEmptyMessage(3);
            } else {
                delaySkipHandler.sendEmptyMessage(number);
            }
        } else if (curPage != 3 && number == 3) {
            vpCtrl.setCurrentItem(0); //change
            vpCtrl.setVisibility(View.VISIBLE);
            delaySkipHandler.sendEmptyMessage(3);
        } else {
            delaySkipHandler.sendEmptyMessage(number);
        }
    }

    Handler shareSetHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what != 3) {
                vpCtrl.setVisibility(View.GONE);
                delaySkipHandler.sendEmptyMessage(msg.what);
            } else if (msg.what == 3) {
                if (fragAs.isEmpty()) {
                    vpCtrl.setCurrentItem(1, false);
                    fragDs.doShowView(true, false);
                    vpCtrl.setAllowScroll(false);
                } else if (fragAs.isViewed()) {
                    int p = vpCtrl.getCurrentItem();
                    if (p == 0) {
                        fragAs.doShowView();
                        vpCtrl.setAllowScroll(true);
                    } else if (p == 1) {
                        fragDs.doShowView(true, false);
                        vpCtrl.setAllowScroll(true);
                    }
                } else {
                    vpCtrl.setCurrentItem(0, false);
                    fragAs.doShowView();
                    vpCtrl.setAllowScroll(true);
                }
            }
        }
    };

    Handler delaySkipHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what != curPage) {
                switchFt(msg.what);
                if (msg.what == 3) {
                    shareSetHandler.sendEmptyMessage(3);
                }
            }
        }
    };

    private synchronized void switchFt(int number) {
        if (isSwitching || curPage == number) {
            return;
        }
        isSwitching = true;
        FragmentTransaction ft;
//        ivAttAdd.setVisibility(View.GONE);
//        ivVsAdd.setVisibility(View.GONE);
//        ivDsAdd.setVisibility(View.GONE);
        ft = fragmentManager.beginTransaction();
        if (fragVideo != null && fragVideo.isAdded())
            ft.hide(fragVideo);
//        if (fragDs != null && fragDs.isAdded())
//            ft.hide(fragDs);
//        if (fragAs != null && fragAs.isAdded())
//            ft.hide(fragAs);
        if (fragAtt != null && fragAtt.isAdded())
            ft.hide(fragAtt);
        if (fragInfo != null && fragInfo.isAdded())
            ft.hide(fragInfo);
        if (fragSetting != null && fragSetting.isAdded())
            ft.hide(fragSetting);
        switch (number) {
            case 2:
                setIndexState(2, false);
                tvTitle.setText(getResources().getString(R.string.videoTitle));
                if (fragVideo == null) {
                    fragVideo = new ConfVideoView(this);
                    ft.add(R.id.fl_inconf_ctrl, fragVideo, "Video");
                } else {
                    ft.show(fragVideo);
                }
                if(!ConferenceApplication.isVideoMeeting){
                    ivVsAdd.setVisibility(View.GONE);
                }else if (((VideoCommonImpl) CommonFactory.getInstance().getVideoCommon()).isVideoPreviewPriviledge() || ((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).isHost()) {
                    ivVsAdd.setVisibility(View.VISIBLE);
                } else {
                    ivVsAdd.setVisibility(View.GONE);
                }
                ivVsAdd.setOnClickListener(fragVideo);
                curPage = 2;
                break;
            case 3:
                setIndexState(3, false);
                tvTitle.setText(getResources().getString(vpCtrl.getCurrentItem() == 0 ? R.string.as_title : R.string.share_title));
                if (vpCtrl.getCurrentItem() == 0) {
                    ivDsAdd.setVisibility(View.GONE);
                } else if (((DocCommonImpl) CommonFactory.getInstance().getDocCommon()).getPrivateShareDocPriviledge() || ((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).isHost()) {
                    ivDsAdd.setVisibility(View.VISIBLE);
                } else {
                    ivDsAdd.setVisibility(View.GONE);
                }
                ivDsAdd.setOnClickListener(fragDs);
                curPage = 3;
                break;
            case 4:
                setIndexState(4, false);
                tvTitle.setText(getResources().getString(R.string.attenders));
                if (fragAtt == null) {
                    fragAtt = new ConfAttendersView(this);
                    ft.add(R.id.fl_inconf_ctrl, fragAtt, "Att");
                } else {
                    ft.show(fragAtt);
                }
                if(((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).isHost()){
                    ivAttAdd.setVisibility(View.VISIBLE);
                }else{
                    ivAttAdd.setVisibility(View.GONE);
                }
                ivAttAdd.setOnClickListener(fragAtt);
                curPage = 4;
                break;
            case 5:
                setIndexState(5, false);
                tvTitle.setText(getResources().getString(R.string.more_info));
                if (fragInfo == null) {
                    fragInfo = new ConfInfoView(this);
                    ft.add(R.id.fl_inconf_ctrl, fragInfo, "Info");
                } else {
                    ft.show(fragInfo);
                }
                curPage = 5;
                break;
            case 6:
                setIndexState(5, false);
                tvTitle.setText(getResources().getString(R.string.setting_title));
                if (fragSetting == null) {
                    fragSetting = new ConfSettingView(this);
                    ft.add(R.id.fl_inconf_ctrl, fragSetting, "Setting");
                } else {
                    ft.show(fragSetting);
                }
                curPage = 6;
                break;

            default:

                break;
        }
        ft.commit();
        isSwitching = false;

        setRecordingShow();
    }

    private void setIndexState(int index, boolean init) {
        switch (index) {
            case 0:
                if (fragVideo.isLocalVideoOpened() || init)
                    ivIndex2.setImageResource(R.drawable.ic_index_2_nor);
                else
                    ivIndex2.setImageResource(R.drawable.ic_index_2_nor_off);
                ivIndex3.setImageResource(R.drawable.ic_index_3_nor);
                ivIndex4.setImageResource(R.drawable.ic_index_4_nor);
                ivIndex5.setImageResource(R.drawable.ic_index_5_nor);
                tvIndex2.setTextColor(getResources().getColor(R.color.index_white));
                tvIndex3.setTextColor(getResources().getColor(R.color.index_white));
                tvIndex4.setTextColor(getResources().getColor(R.color.index_white));
                tvIndex5.setTextColor(getResources().getColor(R.color.index_white));
                ivVsAdd.setVisibility(View.GONE);
                ivDsAdd.setVisibility(View.GONE);
                ivAttAdd.setVisibility(View.GONE);
                break;
            case 2:
                if (fragVideo.isLocalVideoOpened() || init && ChatAPI.getInstance().getConfType() == 0)
                    ivIndex2.setImageResource(R.drawable.ic_index_2_sel);
                else
                    ivIndex2.setImageResource(R.drawable.ic_index_2_sel_off);
                ivIndex3.setImageResource(R.drawable.ic_index_3_nor);
                ivIndex4.setImageResource(R.drawable.ic_index_4_nor);
                ivIndex5.setImageResource(R.drawable.ic_index_5_nor);
                tvIndex2.setTextColor(getResources().getColor(R.color.index_red));
                tvIndex3.setTextColor(getResources().getColor(R.color.index_white));
                tvIndex4.setTextColor(getResources().getColor(R.color.index_white));
                tvIndex5.setTextColor(getResources().getColor(R.color.index_white));
                ivVsAdd.setVisibility(View.VISIBLE);
                ivDsAdd.setVisibility(View.GONE);
                ivAttAdd.setVisibility(View.GONE);

                if (init) closeLocalCameraIfNeed();

                break;
            case 3:
                if (fragVideo.isLocalVideoOpened() || init)
                    ivIndex2.setImageResource(R.drawable.ic_index_2_nor);
                else
                    ivIndex2.setImageResource(R.drawable.ic_index_2_nor_off);
                ivIndex3.setImageResource(R.drawable.ic_index_3_sel);
                ivIndex4.setImageResource(R.drawable.ic_index_4_nor);
                ivIndex5.setImageResource(R.drawable.ic_index_5_nor);
                tvIndex2.setTextColor(getResources().getColor(R.color.index_white));
                tvIndex3.setTextColor(getResources().getColor(R.color.index_red));
                tvIndex4.setTextColor(getResources().getColor(R.color.index_white));
                tvIndex5.setTextColor(getResources().getColor(R.color.index_white));
                ivVsAdd.setVisibility(View.GONE);
                ivDsAdd.setVisibility(View.VISIBLE);
                ivAttAdd.setVisibility(View.GONE);
                break;
            case 4:
                if (fragVideo.isLocalVideoOpened() || init)
                    ivIndex2.setImageResource(R.drawable.ic_index_2_nor);
                else
                    ivIndex2.setImageResource(R.drawable.ic_index_2_nor_off);
                ivIndex3.setImageResource(R.drawable.ic_index_3_nor);
                ivIndex4.setImageResource(R.drawable.ic_index_4_sel);
                ivIndex5.setImageResource(R.drawable.ic_index_5_nor);
                tvIndex2.setTextColor(getResources().getColor(R.color.index_white));
                tvIndex3.setTextColor(getResources().getColor(R.color.index_white));
                tvIndex4.setTextColor(getResources().getColor(R.color.index_red));
                tvIndex5.setTextColor(getResources().getColor(R.color.index_white));
                ivVsAdd.setVisibility(View.GONE);
                ivDsAdd.setVisibility(View.GONE);
                ivAttAdd.setVisibility(View.VISIBLE);
                break;
            case 5:
                if (fragVideo.isLocalVideoOpened() || init)
                    ivIndex2.setImageResource(R.drawable.ic_index_2_nor);
                else
                    ivIndex2.setImageResource(R.drawable.ic_index_2_nor_off);
                ivIndex3.setImageResource(R.drawable.ic_index_3_nor);
                ivIndex4.setImageResource(R.drawable.ic_index_4_nor);
                ivIndex5.setImageResource(R.drawable.ic_index_5_sel);
                tvIndex2.setTextColor(getResources().getColor(R.color.index_white));
                tvIndex3.setTextColor(getResources().getColor(R.color.index_white));
                tvIndex4.setTextColor(getResources().getColor(R.color.index_white));
                tvIndex5.setTextColor(getResources().getColor(R.color.index_red));
                ivVsAdd.setVisibility(View.GONE);
                ivDsAdd.setVisibility(View.GONE);
                ivAttAdd.setVisibility(View.GONE);
                break;


            default:
                break;
        }
    }


    private WifiDialog wifidialog;

    private void checkNetType() {
        if (wifidialog != null && wifidialog.isShowing()) {
            wifidialog.cancel();
        }
        wifiHandler.removeCallbacksAndMessages(null);
        wifiHandler.sendEmptyMessageDelayed(0, 1000);
    }

    Handler wifiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            if (NetworkInfo.State.CONNECTED != manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState()) {
                showWifiDialog();
            } else {
                if (!ConferenceApplication.isVideoMeeting) {
                    ConferenceApplication.isVideoMeeting = true;
                    updateWifiOrNet();
                    if(netHabit != 0)
                        showShortToast(R.string.warnReVideo);
                }
//                isConnWifi = true;
            }
        }
    };

    private void showWifiDialog() {
        wifidialog = new WifiDialog(Conference4PhoneActivity.this, ConferenceApplication.Screen_W * 4 / 5, new WifiDialog.OnResultListener() {
            @Override
            public void doAudio() {
                netHabit = 1;
                ConferenceApplication.isVideoMeeting = false;
                updateWifiOrNet();
            }

            @Override
            public void doVideo() {
                netHabit = 2;
                ConferenceApplication.isVideoMeeting = true;
                updateWifiOrNet();
            }
        });
        wifidialog.show();
    }

    private void updateWifiOrNet() {
        if (curPage == 2) {
            if(fragVideo.getOnBackPressed()){
                fragVideo.doShowView();
            }
            if(!ConferenceApplication.isVideoMeeting){
                ivVsAdd.setVisibility(View.GONE);
            }else if (((VideoCommonImpl) CommonFactory.getInstance().getVideoCommon()).isVideoPreviewPriviledge() || ((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).isHost()) {
                ivVsAdd.setVisibility(View.VISIBLE);
            } else {
                ivVsAdd.setVisibility(View.GONE);
            }
        } else if (curPage == 3 && vpCtrl.getCurrentItem() == 0) {
            fragAs.doShowView();
        }
    }


    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onResume() {

        Log.d(TAG, "Conf4Phone.onResume");

        super.onResume();

        //ChatAPI.getInstance().setExternalConfHandler(confHandler);

        if (screenCapturePresenter.getFile() != null){
            FileUtils.DeleteFolder(screenCapturePresenter.getFile().getPath());
            FileUtils.DeleteFolder(screenCapturePresenter.getFile().getAbsolutePath());
        }
        //关闭弹窗
        if (!isShare){
            closeFloatWindow();
            LastWindowInfo.getInstance().clear();
        }
        audioCommon.startReceive();
        audioCommon.setVolume(255);
        if (mWakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "XYTEST");
            mWakeLock.acquire();
        }

        if (audioManager != null) {
            if (state == 0) {
                audioManager.setSpeakerphoneOn(true);
            } else {
                audioManager.setSpeakerphoneOn(false);
            }
        }

        if (isHaveStoped) {
            position = ActHome.position;
        }

        isHaveStoped = false;
    }

    @Override
    protected void onPause() {

        Log.d(TAG, "Conf4Phone.onPause");

        super.onPause();
        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }

        if (isJump2Img) {
            isJump2Img = false;
        } else {
            audioCommon.stopReceive();
        }
    }


    @Override
    protected void onDestroy() {

        Log.d(TAG, "Conf4Phone.onDestroy");

        EventBus.getDefault().unregister(this);

        if (breceiver != null)
            unregisterReceiver(breceiver);

        if (audioManager != null)
            audioManager.setSpeakerphoneOn(false);

        if (null != audioCommon){
            Log.d(TAG, "Conf4Phone.audioCommon.destroyVoe");
            audioCommon.destroyVoe();
            audioCommon.setHandler(null);
        }

        if (null != confCommon)
            confCommon.setHandler(null);

        if (null != asCommon)
            asCommon.setHandler(null);

        ChatAPI.getInstance().setExternalConfHandler(null);
        audioCommon = null;
        confCommon = null;
        asCommon = null;

        isConfJoined = false;
        ConferenceApplication.isConfJoined = false;

        ConferenceApplication.isConfActivityLaunched = false;

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

        Log.d(TAG, "Conf4Phone.onBackPressed");

        if (curPage == 2 && fragVideo.getOnBackPressed()) {
            showExitDialog();
        } else if (curPage == 3) {
            showExitDialog();
        } else if (curPage == 4 && fragAtt.getOnBackPressed()) {
            showExitDialog();
        } else if (curPage == 5 && fragInfo.getOnBackPressed()) {
            showExitDialog();
        } else if (curPage == 6 && fragSetting.getOnBackPressed()) {
            showExitDialog();
        }
//        if (screenCapturePresenter.isCapturing()) {
//            new AlertDialog.Builder(this)//
//                    .setMessage("Screen currently is recording! Confirm the stop?")//
//                    .setCancelable(false)//
//                    .setPositiveButton(android.R.string.ok, (dialog, which) -> //
//                            super.onBackPressed())//
//                    .setNegativeButton(android.R.string.cancel, null)//
//                    .create()//
//                    .show();
//        } else {
//            super.onBackPressed();
//        }
    }

    private void showExitDialog() {

        if (mConfConnecting)
        {
            showLongToast(R.string.confInSetup);
            return;
        }

//        ExitDialog exitDialog = new ExitDialog(this, ConferenceApplication.Screen_W * 4 / 5, new ExitDialog.OnResultListener() {
//            @Override
//            public void doYes() {
//                exit();
//            }
//
//            @Override
//            public void doNo() {
//
//            }
//        });
//        exitDialog.show();

        exit();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        curOriInfo = newConfig.orientation;
        if (popIndex5 != null && popIndex5.isShowing()) {
            popIndex5.dismiss();
        }
        //llBottomBar.setVisibility(View.VISIBLE);
        //rlTopBar.setVisibility(View.VISIBLE);
        setFrag4changeBars(true);
        if (curPage == 2) {
            fragVideo.changeOrietation(newConfig);
        } else if (curPage == 3) {
            fragVideo.changeCameraOrietation(newConfig);
            fragDs.changeOrietation(newConfig);
            fragAs.changeOrietation(newConfig);
        } else if (curPage == 4) {
            fragVideo.changeCameraOrietation(newConfig);
            fragAtt.changeOrietation(newConfig);
        } else if (curPage == 5) {
            fragVideo.changeCameraOrietation(newConfig);
            fragInfo.changeOrietation(newConfig);
        } else if (curPage == 6) {
            fragVideo.changeCameraOrietation(newConfig);
            fragSetting.changeOrietation(newConfig);
        }
  }

    int homeCount = 0;

    public void  exit() {

        Log.d(TAG, "Conf4Phone.exit");

        ConferenceApplication.isConfJoined = false;

        if (isShare){
            screenCapturePresenter.stopCapture();
            ((ShareDtCommonImpl) CommonFactory.getInstance().getSdCommon()).stopDesktopShare();
            ((ShareDtCommonImpl) CommonFactory.getInstance().getSdCommon()).setScreenInfo(0, 0, 0);
            isShare = false;
        }
        LocalCommonFactory.getInstance().getContactDataCommon().getMap().clear();
        if (fragDs != null) fragDs.removePointing();

        if (isConfJoined) {
            confHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    CommonFactory.getInstance().getConferenceCommon().leaveConference();
                }
            }, 600);
        }

        waitVideo4Exit();

        isConfJoined = false;

    }

    private void waitVideo4Exit() {
        //fragAs.preExit();
        fragVideo.preExit();
        fragDs.preExit();
        new VideoSyncStop().run();
    }
    //桌面共享
    @Override
    public void onShare() {
        //reset
        if (fragAs != null && fragAs.isAdded() && !fragAs.isHidden() && !fragAs.isEmpty()){
            ToastUtil.showMessage(mContext,"正在桌面共享，请稍后再试",5 * 1000);
            return;
        }
        closeFloatWindow();
        LastWindowInfo.getInstance().clear();
        floatWindowType = FloatWindowManager.FW_TYPE_ALERT_WINDOW;
        checkPermissionAndShow();

    }

    @Override
    public void videoHeaderByte(@NonNull byte[] sps, @NonNull byte[] pps) {
        //Log.e("YYYYYY","采集得数据::6666666666666666");
        this.sps = sps;
        this.pps = pps;
        isMerge = true;
    }

    @Override
    public void videoContentByte(@NonNull byte[] content) {
        byte[] data = null;
        Log.d("InfowareLab.Debug","ShareScreen: videoContentByte::"+content.length);
        if (isMerge){
            byte[] data1 = Utils.byteMerger(sps,pps);
            data = Utils.byteMerger(data1,content);
            ((ShareDtCommonImpl)CommonFactory.getInstance().getSdCommon()).sendScreenData(screenCapturePresenter.getWidth(),
                    screenCapturePresenter.getHeight(),24,data,data.length,true,true);
            isMerge = false;
            sps = null;
            pps = null;
        }else {
            data = content;
            byte nData1, nData2;
            byte nStreamInx;
            nData1 = data[0];
            nData2 = data[1];
            data[0] = nData2 == 0 ? (byte) 0x00 : (byte) 0xFF;
            //Is main Stream
            nStreamInx = (byte) (nData1 & 0x03);
            //get svc information
            if (nStreamInx == 0) {
                int nalUnitType = data[4] & 0x1F;
                /**
                 * 防止花屏
                 * */
                if (data[4] == 0x61){
                    data[4] = 0x41;
                }
                if (nalUnitType == 7) { //IDR frame
                    data[4] = (byte) (data[4] | (3 << 5));

                    Log.d("InfowareLab.Debug","ShareScreen: sendScreenData(I):"+data.length);

                    ((ShareDtCommonImpl)CommonFactory.getInstance().getSdCommon()).sendScreenData(screenCapturePresenter.getWidth(),
                        screenCapturePresenter.getHeight(),24,data,data.length,true,true);
                }else {

                    Log.d("InfowareLab.Debug","ShareScreen: sendScreenData(P):"+data.length);
                    ((ShareDtCommonImpl)CommonFactory.getInstance().getSdCommon()).sendScreenData(screenCapturePresenter.getWidth(),
                        screenCapturePresenter.getHeight(),24,data,data.length,true,false);
                }
            }
        }
//        if (isWriteFile){
//            try {
//                _out.write(data);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
        //Log.e("rrrrr","数据量:::"+data.length);
    }
    @Override
    public void audioContentByte(@NonNull byte[] content) {
//        ((ShareDtCommonImpl) CommonFactory.getInstance().getSdCommon()).sendAudioData(content,content.length,0);
    }

    @Override
    public void captureState(ScreenCaptureState state) {

    }

    @Override
    public void captureTime(long time) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
    /**
     * 同意授权
     */
    @Override
    public void granted() {
        ((ShareDtCommonImpl)CommonFactory.getInstance().getSdCommon()).startDesktopShare(help.getFPS(),20);
        Log.d("InfowareLab.Debug","screenshare FPS::"+help.getFPS());
        isShare = true;
        screenCapturePresenter.startCapture();
    }
    /**
     * 取消授权
     */
    @Override
    public void denied() {
    }



    @Override
    public void closeShare() {
        ((ShareDtCommonImpl) CommonFactory.getInstance().getSdCommon()).stopDesktopShare();
        ((ShareDtCommonImpl) CommonFactory.getInstance().getSdCommon()).setScreenInfo(0, 0, 0);
        screenCapturePresenter.stopCapture();
        //删除缓存文件
        if (screenCapturePresenter != null && screenCapturePresenter.getFile() != null) {
            FileUtils.deleteFile(screenCapturePresenter.getFile());
            FileUtils.DeleteFolder(screenCapturePresenter.getFile().getPath());
            FileUtils.DeleteFolder(screenCapturePresenter.getFile().getAbsolutePath());
        }
        isShare = false;

        ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        activityManager.moveTaskToFront(getTaskId(), 0);

        /**获得当前运行的task(任务)*/
//        List<ActivityManager.RunningTaskInfo> taskInfoList = activityManager.getRunningTasks(100);
//        for (ActivityManager.RunningTaskInfo taskInfo : taskInfoList) {                /**找到本应用的 task，并将它切换到前台*/
//            if (taskInfo.topActivity.getPackageName().equals(this.getPackageName())) {
//                activityManager.moveTaskToFront(taskInfo.id, 0);
//                break;
//            }
//        }

    }

    public void onHangupClick(View view) {
        showExitDialog();
    }

    public void onAcceptClick(View view) {

        mConfConnecting = true;

        ivHangup.setVisibility(View.VISIBLE);
        ivHangup2.setVisibility(View.GONE);
        ivAccept.setVisibility(View.GONE);
        tvInviteText.setText(R.string.callee_joining);

//        ivHangup.post(new Runnable() {
//            @Override
//            public void run() {
//                ChatAPI.getInstance().joinConf(mUserId, mConfId, mConfPwd, mUserName);
//
//            }
//        });

        new Thread(new Runnable() {
                @Override
                public void run() {
                    joinConf(mUserId, mConfId, mConfPwd, mUserName);
                }
            }).start();



        mAccepted = true;
    }

    /**
     * 外放模式和听筒模式 切换
     */
    private void switchAudioMode() {

        if (!handsFree) {//外放模式
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setSpeakerphoneOn(true);
            ivSpeaker.setImageResource(R.drawable.volume_on);
            tvSpeaker.setText(R.string.volume_on);
        } else {//听筒模式
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            } else {
                audioManager.setMode(AudioManager.MODE_IN_CALL);
            }
            audioManager.setSpeakerphoneOn(false);
            ivSpeaker.setImageResource(R.drawable.volume_off);
            tvSpeaker.setText(R.string.volume_off);
        }

        handsFree = !handsFree;
    }

    public void onSpeakerClick(View view) {
        switchAudioMode();
    }

    public void onSwitchCamera(View view) {
        if (fragVideo.isLocalVideoOpened()) {
            fragVideo.enableCamera(false);
            fragVideo.closeMyVideo();
            ivIndex2.setImageResource(R.drawable.ic_index_2_sel_off);
            showShortToast(R.string.cameraoff);
            ivCamera.setImageResource(R.drawable.camera_off);
            tvCamera.setText(R.string.camera_off);
        } else {

            //if (userCommon.getSelf().isShareVideo())
            fragVideo.openMyVideo();
            fragVideo.enableCamera(true);
            ivIndex2.setImageResource(R.drawable.ic_index_2_sel);
            showShortToast(R.string.cameraon);
            ivCamera.setImageResource(R.drawable.camera_on);
            tvCamera.setText(R.string.camera_on);
        }
    }

    public void onRotateCamera(View view) {

        if (fragVideo.isLocalVideoOpened())
            fragVideo.onClick(view);
    }

    class VideoSyncStop extends Thread {
        public void run() {
            while (VideoSyncView.isSoftDrawing) {
                try {
                    sleep(50);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            confHandler.sendEmptyMessage(999);
        }
    }

    ;

    public void changeSelfRole() {
        if (fragVideo != null) fragVideo.setRoleModify();
        if (fragInfo != null) fragInfo.setInfo();
        if (fragDs != null) fragDs.setRole();
        if(userCommon.isHost()){
            audioCommon.onOpenAudioConfirm(true);
        }
        changeRecordByRoleModify();
    }

    @Override
    protected void onRestart() {

        Log.d(TAG, "Conf4Phone.onRestart");

        fragVideo.initVideoHandler();
        super.onRestart();
        //llBottomBar.post(new Runnable() {
//            @Override
//            public void run() {
//                fragVideo.restartApp();
//            }
//        });
    }
    @Override
    protected void onStop() {

        Log.d(TAG, "Conf4Phone.onStop");

        super.onStop();
        isHaveStoped = true;
        ActHome.position = position;
    }

    public void getPrivilege() {
        getAudioPrivilege();
    }

    private void getAudioPrivilege() {

        Log.d("InfowareLab.Debug","getAudioPrivilege = " + audioCommon.isMICWork());

//        if (userCommon != null){
//            if (userCommon.existRemoteUsers() > 0 && !audioChecked){
//                audioChecked = true;
//                return;
//            }
//        }

//        if (audioCommon.isMICWork()) {
//            ivIndex1.setImageResource(R.drawable.ic_index_1_enable_off);
//            ivMic.setImageResource(R.drawable.mic_on);
//            tvIndex1.setTextColor(getResources().getColor(R.color.index_white));
//            if (((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).getSelf().getRole() == UserCommonImpl.ROLE_HOST) {
//                Log.i("UserCommonImpl", "SDKImpl-UserCommonImpl-getRole=1");
//                tryStartSend();
//            }
//        }
    }

    private void initAudioFilter() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (!audioManager.isWiredHeadsetOn() && !audioManager.isBluetoothScoOn() && !audioManager.isBluetoothA2dpOn()) {
            state = 0;
            handsFree = true;
        } else {
            state = 1;
            handsFree = false;
        }

        if (handsFree) {//外放模式
            ivSpeaker.setImageResource(R.drawable.volume_on);
            tvSpeaker.setText(R.string.volume_on);
        } else {//听筒模式
            ivSpeaker.setImageResource(R.drawable.volume_off);
            tvSpeaker.setText(R.string.volume_off);
        }
    }

    /**
     * 初始化音频引擎，打开声音
     */
    private void initAudio() {
        //audioCommon.createVoe(this);
        audioCommon.createVoe(getApplicationContext());
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

        audioCommon.startReceive();
        audioCommon.setVolume(255);

        if (audioManager != null) {
            if (state == 0) {
                audioManager.setSpeakerphoneOn(true);
            } else {
                audioManager.setSpeakerphoneOn(false);
            }
        }
    }

    /**
     * 电话监听
     */
    private void initTelephoneListener() {
        if (tpm == null) {
            tpm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            tpm.listen(new PhoneStateUtil(), PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    private void tryStartSend() {
        audioCommon.startSend();
    }

    private CallattDialog callattDialog;

    public void showCallattDialog(int timeout, int id) {
        if (callattDialog == null) {
            callattDialog = new CallattDialog(this, ConferenceApplication.SCREEN_WIDTH > ConferenceApplication.SCREEN_HEIGHT ? ConferenceApplication.SCREEN_HEIGHT * 4 / 5 : ConferenceApplication.SCREEN_WIDTH * 4 / 5);
            callattDialog.setClickListener(new CallattDialog.OnResultListener() {

                @Override
                public void doYes(int timeout, int id) {
                    // TODO Auto-generated method stub
                    ((ConferenceCommonImpl) CommonFactory.getInstance().getConferenceCommon()).callReson(timeout, id);
                }

                @Override
                public void doNo(int timeout, int id) {
                    // TODO Auto-generated method stub

                }
            });
        }
        if (callattDialog != null && !callattDialog.isShowing()) {
            callattDialog.showATime(timeout, id);
        }
    }

    public void hideCallattDialog() {
        if (callattDialog != null && callattDialog.isShowing()) {
            callattDialog.dismiss();
        }
    }

    public void closeLocalCamera() {
        if (fragVideo != null) fragVideo.closeLocalCamera();
    }

    public void openLocalCamera() {
        if (fragVideo != null) fragVideo.openLocalVideo();
    }

    @Override
    public void doByReq(int request, Object details) {
        if (request == R.id.ll_inconf_ctrl_bottom) {
            if ((boolean) details) {
                //llBottomBar.setVisibility(View.VISIBLE);
            } else {
                //llBottomBar.setVisibility(View.GONE);
            }
        } else if (request == ConferenceCommon.TRANSCHANNEL) {
            if (userCommon.isHost()) {
//                String msg = (String) details;
//                if (msg.equals("ACTION_USER_ADD")) {
//                    if (confCommon.getLastRecvData().equals("")) {
//                        fragVideo.doTransChannel();
//                    } else {
//                        doByReq(ConferenceCommon.TRANSCHANNEL, confCommon.getLastRecvData());
//                    }
//                } else {
//                    confCommon.transparentSendUserData((String) details);
//                }
            }
        }
    }

    private boolean isBlueOn = false;

    private void initReceiver() {
        breceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                    checkNetType();
                } else if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
                    state = intent.getIntExtra("state", 2);
                    Log.i("BroadcastReceiver", "BroadcastReceiver ConnectivityManager.ACTION_HEADSET_PLUG " + state);
                    if (state == 0) {
                        audioManager.setSpeakerphoneOn(true);
                    } else if (state == 1) {
                        audioCommon.stopReceive();
                        audioCommon.startReceive();
                        int intType = intent.getIntExtra("microphone", 0);
                        if (intType == 0) {
                            audioCommon.stopSend();
                        }
                        if (intType == 1) {
                            if (audioCommon.isMICWork() && audioCommon.isRecording()) {
                                audioCommon.stopSend();
                                audioCommon.startSend();
                            }
                        }
                        audioManager.setSpeakerphoneOn(false);
                    }
                } else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {    //蓝牙连接状态

                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1);
                    if (state == BluetoothAdapter.STATE_CONNECTED) {//连接或失联，切换音频输出（到蓝牙、或者强制仍然扬声器外放）
                        Log.i("BroadcastReceiver", "BroadcastReceiver BluetoothAdapter STATE_CONNECTED1" + " isBluetoothA2dpOn" + audioManager.isBluetoothA2dpOn() + " isBluetoothScoOn" + audioManager.isBluetoothScoOn());
                        if (!audioManager.isBluetoothScoOn()) {
                            audioManager.startBluetoothSco();
                        }
                        audioManager.setSpeakerphoneOn(false);
                    } else if (state == BluetoothAdapter.STATE_DISCONNECTED) {
                        Log.i("BroadcastReceiver", "BroadcastReceiver BluetoothAdapter STATE_DISCONNECTED" + " isBluetoothA2dpOn" + audioManager.isBluetoothA2dpOn() + " isBluetoothScoOn" + audioManager.isBluetoothScoOn());
                        audioManager.setSpeakerphoneOn(true);
                        isBlueOn = false;
                    }
                } else if (AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED.equals(action)) {
                    int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
                    Log.i("BroadcastReceiver", "BroadcastReceiver BluetoothAdapter.ACTION_SCO_AUDIO_STATE_UPDATED " + state + " " + audioManager.isBluetoothScoOn() + " " + audioManager.isBluetoothA2dpOn());
                    if (audioManager.isBluetoothScoOn() || audioManager.isBluetoothA2dpOn()) {
                        if (!audioManager.isBluetoothScoOn()) {
                            audioManager.startBluetoothSco();
                        }
                        audioManager.setSpeakerphoneOn(false);
                    } else if (AudioManager.SCO_AUDIO_STATE_CONNECTED == state) {
                        if (!audioManager.isBluetoothScoOn()) {
                            audioManager.startBluetoothSco();
                        }
                        audioManager.setSpeakerphoneOn(false);
                    } else if (AudioManager.SCO_AUDIO_STATE_DISCONNECTED == state) {
                        audioManager.setSpeakerphoneOn(true);
                    }
                } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {    //本地蓝牙打开或关闭
                    Log.i("BroadcastReceiver", "BroadcastReceiver BluetoothAdapter.ACTION_STATE_CHANGED");
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                    if (state == BluetoothAdapter.STATE_OFF || state == BluetoothAdapter.STATE_TURNING_OFF) {//断开，切换音频输出
                        Log.i("BroadcastReceiver", "BroadcastReceiver BluetoothAdapter.ACTION_STATE_CHANGED OFF");
                        audioManager.setSpeakerphoneOn(true);
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter();

        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        if (android.os.Build.VERSION.SDK_INT < 26) {
            filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
            filter.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);
        }

        registerReceiver(breceiver, filter);
    }


    private boolean isBlueConnected() {
        if (android.os.Build.VERSION.SDK_INT >= 18) {
            final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            List<BluetoothDevice> connectedDevices = bluetoothManager.getConnectedDevices(GATT_SERVER);
            Log.i("Bluetooth", "BroadcastReceiver connectedDevices.size 18+ " + connectedDevices.size());
            if (connectedDevices.size() > 0) return true;
        } else {
            BluetoothAdapter _bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            Class<BluetoothAdapter> bluetoothAdapterClass = BluetoothAdapter.class;//得到BluetoothAdapter的Class对象
            try {//得到蓝牙状态的方法
                Method method = bluetoothAdapterClass.getDeclaredMethod("getConnectionState", (Class[]) null);
                //打开权限
                method.setAccessible(true);
                int state = (int) method.invoke(_bluetoothAdapter, (Object[]) null);
                if (state == BluetoothAdapter.STATE_CONNECTED) {
                    Set<BluetoothDevice> devices = _bluetoothAdapter.getBondedDevices();
                    for (BluetoothDevice device : devices) {
                        Method isConnectedMethod = BluetoothDevice.class.getDeclaredMethod("isConnected", (Class[]) null);
                        method.setAccessible(true);
                        boolean isConnected = (boolean) isConnectedMethod.invoke(device, (Object[]) null);

                        if (isConnected) {
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    Handler buletHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            audioManager.setBluetoothScoOn(true);
            audioManager.setSpeakerphoneOn(false);
        }
    };


    private void initRecord() {
        if (userCommon.isHost() && confCommon.isSupportCloudRecord()) {
//            btnRecord.setVisibility(View.VISIBLE);
//            btnRecord.setBackgroundResource(confCommon.isCloudRecording() ? R.drawable.menu_record_on : R.drawable.menu_record_normal);
        } else {
            if (CommonFactory.getInstance().getConferenceCommon().isCloudRecording()) {
                CommonFactory.getInstance().getConferenceCommon().stopCloudRecord();
            }
//            btnRecord.setVisibility(View.GONE);
        }
    }

    public void changeRecordByRoleModify() {
        if (!userCommon.isHost() && confCommon.isCloudRecording()) {
            if (fragVideo != null) fragVideo.stopCloudRecord(0);
            confCommon.stopCloudRecord();
        }
        initRecord();
    }

    private void clickRecord() {
        if (this.isWaitingCloudCallback) return;
        this.isWaitingCloudCallback = true;
        if (confCommon.isCloudRecording()) {
            if (fragVideo != null) fragVideo.stopCloudRecord(0);
            confCommon.stopCloudRecord();
            stopTiming(1);
        } else {
            startTiming(0);
//            btnRecord.setEnabled(false);
            confCommon.beginCloudRecord();
        }
    }


    private int curOriInfo = Configuration.ORIENTATION_PORTRAIT;

    @Override
    protected void showBottomBar() {
        //llBottomBar.setVisibility(View.VISIBLE);
        tvConfFinish.setVisibility(View.VISIBLE);
    }

    @Override
    protected void hideBottomBar() {
        //llBottomBar.setVisibility(View.GONE);
        if (curOriInfo == Configuration.ORIENTATION_PORTRAIT) {
            tvConfFinish.setVisibility(View.INVISIBLE);
        } else {
            tvConfFinish.setVisibility(View.GONE);
        }
    }

    protected void hideBottomBarButFinish() {
        //llBottomBar.setVisibility(View.GONE);
        if (curOriInfo == Configuration.ORIENTATION_PORTRAIT) {
            tvConfFinish.setVisibility(View.VISIBLE);
        } else {
            tvConfFinish.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCallParentView(String action, Object obj) {
        switch (action) {
            case BaseFragment.ACTION_ADDUSER:

                Log.d("InfowareLab.Debug", "UserCommon.ACTION_USER_ADD");

                boolean firstTime = false;
                if (mRemoteUserCount <= 0) firstTime = true;
                mRemoteUserCount = userCommon.existRemoteUsers();

                if (mRemoteUserCount >= 1)
                {
                    mConfConnecting = false;

                    ivFace.setVisibility(View.GONE);
                    tvInviteName.setVisibility(View.GONE);
                    tvInviteText.setVisibility(View.GONE);
                    ConferenceApplication.isConfJoined = true;

                    if (firstTime){
                        mConfStartTime = System.currentTimeMillis();//获取系统时间
                        confHandler.sendEmptyMessageDelayed(CONF_TIME_UPDATE, 500);
                        mConfDisconnected = false;
                        tvConfTime.setVisibility(View.VISIBLE);
                        
                        //closeLocalCameraIfNeed();
                    }
                }

                break;
            case BaseFragment.ACTION_REMOVEUSER:
                Log.d("InfowareLab.Debug", "UserCommon.ACTION_USER_REMOVE");

                mRemoteUserCount = userCommon.existRemoteUsers();

                if (mRemoteUserCount == 0)
                {
                    //mConfConnecting = false;
                    ivFace.setVisibility(View.VISIBLE);
                    tvInviteName.setVisibility(View.VISIBLE);
                    tvInviteText.setVisibility(View.VISIBLE);
                    tvInviteText.setText(R.string.leaveConference);
                    //destroyTimer();
                    mConfDisconnected = true;
                    tvConfTime.setVisibility(View.GONE);
                    ConferenceApplication.isConfJoined = false;
                }
                break;
            case BaseFragment.ACTION_REFRESHCAMERA:
                if (curPage == 2) {
                    if (!fragVideo.isLocalVideoOpened()) {
                        ivIndex2.setImageResource(R.drawable.ic_index_2_sel_off);
                    } else {
                        ivIndex2.setImageResource(R.drawable.ic_index_2_sel);
                    }
                }
                else
                {
                    if (!fragVideo.isLocalVideoOpened()) {
                        ivIndex2.setImageResource(R.drawable.ic_index_2_nor_off);
                    } else {
                        ivIndex2.setImageResource(R.drawable.ic_index_2_nor);
                    }
                }
                break;
            case BaseFragment.ACTION_REFRESHAUDIO:
                if (userCommon != null) {
                    if (userCommon.isHost()) {
                        //if (!audioCommon.isRecording()) {
                            tryStartSend();
                        //}

                        audioCommon.isClickUsed = true;
                    }
                    else {
                        //if (audioCommon.isRecording())
                            audioCommon.stopSend();

                        audioCommon.isClickUsed = true;
                    }
                }
                break;
            case BaseFragment.ACTION_ASSTATE:
                if (curPage == 3) {
                    if (!fragAs.isEmpty()) {
                        vpCtrl.setAllowScroll(true);
                        if(vpCtrl.getCurrentItem() == 1){
                            vpCtrl.setCurrentItem(0, true);
                        }
                    } else if (vpCtrl.getCurrentItem() == 0) {
                        vpCtrl.setCurrentItem(1, false);
                        vpCtrl.setAllowScroll(false);
                    } else {
                        vpCtrl.setAllowScroll(false);
                    }
                }
                break;

            case BaseFragment.ACTION_SHARE2AS:
                tvTitle.setText(getResources().getString(R.string.as_title));
                ivDsAdd.setVisibility(View.GONE);
                break;
            case BaseFragment.ACTION_SHARE2DS:
                tvTitle.setText(getResources().getString(R.string.share_title));
                if (((DocCommonImpl) CommonFactory.getInstance().getDocCommon()).getPrivateShareDocPriviledge() || ((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).isHost()) {
                    ivDsAdd.setVisibility(View.VISIBLE);
                } else {
                    ivDsAdd.setVisibility(View.GONE);
                }
                break;
            case BaseFragment.ACTION_OPENORIETATION:
                openOrietation();
                break;
            case BaseFragment.ACTION_CLOSEORIETATION:
                closeOrietation();
                break;
            case BaseFragment.ACTION_SHOWEXIT:
                showExitDialog();
                break;
            case BaseFragment.ACTION_SETRESOLUTION:
                if (fragVideo != null) fragVideo.setVideoResolution();
                break;
            case BaseFragment.ACTION_HIDEPARENTBOTTOM:
//                hideBottomBar();
                break;
            case BaseFragment.ACTION_HIDEPARENTBOTTOMBUTFINISH:
//                hideBottomBarButFinish();
                break;
            case BaseFragment.ACTION_SHOWPARENTBOTTOM:
                showBottomBar();
                break;
            case BaseFragment.ACTION_SETPRIVITEGE:
                getPrivilege();
                break;
            case BaseFragment.ACTION_ROLEUPDATE:
                changeSelfRole();
                break;
            case BaseFragment.ACTION_JUMP2IMG:
                isJump2Img = true;
                break;
            case BaseFragment.ACTION_CLOSECAMERA:
                closeLocalCamera();
                break;
            case BaseFragment.ACTION_TRANSCHANNEL:
                if (userCommon.isHost()) {
//                    String msg = (String) obj;
//                    if (msg.equals("ACTION_USER_ADD")) {
//                        if (confCommon.getLastRecvData().equals("")) {
//                            fragVideo.doTransChannel();
//                        } else {
//                            doByReq(ConferenceCommon.TRANSCHANNEL, confCommon.getLastRecvData());
//                        }
//                    } else {
//                        confCommon.transparentSendUserData(msg);
//                    }
                }
                break;
            case BaseFragment.ACTION_VIDEOPAGE:
                setRecordingShow();
                break;
            case BaseFragment.ACTION_SHOWBARS:
                showBars();
                break;
            case BaseFragment.ACTION_HIDEBARS:
                hideBars();
                break;
            case BaseFragment.ACTION_CHANGEBARS:
                changeBars();
                break;
            case BaseFragment.ACTION_SHOWBOTTOM:
                showBottom();
                break;
            case BaseFragment.ACTION_HIDEBOTTOM:
                hideBottom();
                break;

            case BaseFragment.ACTION_PRIVILEDGE_VS:
                if (curPage != 2) return;
                if(!ConferenceApplication.isVideoMeeting){
                    ivVsAdd.setVisibility(View.GONE);
                }else if (((VideoCommonImpl) CommonFactory.getInstance().getVideoCommon()).isVideoPreviewPriviledge() || ((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).isHost()) {
                    ivVsAdd.setVisibility(View.VISIBLE);
                } else {
                    ivVsAdd.setVisibility(View.GONE);
                }
                break;
            case BaseFragment.ACTION_PRIVILEDGE_DS:
                if (curPage == 3 && vpCtrl.getCurrentItem() == 1) {
                    if (((DocCommonImpl) CommonFactory.getInstance().getDocCommon()).getPrivateShareDocPriviledge() || ((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).isHost()) {
                        ivDsAdd.setVisibility(View.VISIBLE);
                    } else {
                        ivDsAdd.setVisibility(View.GONE);
                    }
                }
                break;
            case BaseFragment.ACTION_PRIVILEDGE_ATT:
                if (curPage != 4) return;
                if(((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).isHost()){
                    ivAttAdd.setVisibility(View.VISIBLE);
                }else{
                    ivAttAdd.setVisibility(View.GONE);
                }
                break;

            default:
                break;
        }
    }

    private void closeLocalCameraIfNeed() {

        if (ChatAPI.getInstance().getConfType() != 0) {
            //if (fragVideo.isLocalVideoOpened()) {
            //    fragVideo.enableCamera(false);
            //    fragVideo.closeMyVideo();
                ivIndex2.setImageResource(R.drawable.ic_index_2_sel_off);
                showShortToast(R.string.cameraoff);
                ivCamera.setImageResource(R.drawable.camera_off);
                tvCamera.setText(R.string.camera_off);
            //}
        }
    }

    private void changeBars() {
        if (getOrientationState() != Configuration.ORIENTATION_LANDSCAPE) return;
        if (rlTopBar.getVisibility() == View.VISIBLE) {
            rlTopBar.setVisibility(View.GONE);
            //llBottomBar.setVisibility(View.GONE);
            setFrag4changeBars(false);
        } else {
            rlTopBar.setVisibility(View.VISIBLE);
            //llBottomBar.setVisibility(View.VISIBLE);
            setFrag4changeBars(true);
        }
    }

    private void setFrag4changeBars(boolean isShow) {
        if (curPage == 2) {
            fragVideo.onChangeBars(isShow);
        } else if (curPage == 3) {
            fragDs.onChangeBars(isShow);
            fragAs.onChangeBars(isShow);
        }
    }

    public void hideBars() {
        rlTopBar.setVisibility(View.GONE);
        //llBottomBar.setVisibility(View.GONE);
    }

    public void showBars() {
        rlTopBar.setVisibility(View.VISIBLE);
        //llBottomBar.setVisibility(View.VISIBLE);
    }

    private void showBottom() {
        if (rlTopBar.getVisibility() == View.VISIBLE) {
            //llBottomBar.setVisibility(View.VISIBLE);
        } else {
            //llBottomBar.setVisibility(View.GONE);
        }
    }

    private void hideBottom() {
        //llBottomBar.setVisibility(View.GONE);
    }


    private boolean keepRecording = false;
    private Thread timerThread;
    private Thread confTimerThread = null;

    private void startTiming(int startState) {
        if (startState == 1) {
            if ((curPage == 2 || curPage == 3) && !keepRecording) {
                keepRecording = true;
                timerThread = new Thread(recordTimerRun);
                timerThread.start();
            }
        } else if (startState == 2) {
            if (keepRecording) return;
            keepRecording = true;
            timerThread = new Thread(recordTimerRun);
            timerThread.start();
        } else {
            if (curPage == 2 || curPage == 3) {
                recordTimeHandler.sendEmptyMessage(0);
            }
        }
    }

    private void stopTiming(int stopState) {
        keepRecording = false;
        recordTimeHandler.sendEmptyMessage(-1);
    }

    Runnable recordTimerRun = new Runnable() {
        @Override
        public void run() {
            while (keepRecording){
                recordTimeHandler.sendEmptyMessage(6);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    Handler recordTimeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == -1){
                llRecordTime.setVisibility(View.GONE);
            }else{
                llRecordTime.setVisibility(llRecordTime.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            }
        }
    };

    private void setRecordingShow() {
        if (confCommon.isCloudRecording()) {
            if ((curPage == 2 && fragVideo.getCurPage() == 1) || curPage == 3) {
                startTiming(2);
            } else {
                stopTiming(2);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void Event(MessageEvent messageEvent) {
        switch (messageEvent.getType()) {
            case 0:
                bAS = messageEvent.isbAS();
                if (fragDs != null){
                    fragDs.setbAS(bAS);
                }
                //Log.e("Test", "是否有共享桌面权限::" + messageEvent.isbAS());
                if (!bAS && isShare) {
                    ((ShareDtCommonImpl) CommonFactory.getInstance().getSdCommon()).stopDesktopShare();
                    ((ShareDtCommonImpl) CommonFactory.getInstance().getSdCommon()).setScreenInfo(0, 0, 0);
                    screenCapturePresenter.stopCapture();
                    isShare = false;
                }
                break;
            case 1:
                Log.d("InfowareLab.Debug", "onVideoInfo: userId=" + messageEvent.getUserID() + "; channelId=" +  messageEvent.getChannelID());

                break;
            //公告
            case 2:
                break;
            //手否是第一次进会
            case 3:
                break;
            //主讲模式
            case 4:
                break;
            //轮循的路数
            case 5:
                int loopNum = messageEvent.getLoopNum();

                break;
            default:
                break;
        }
    }

    public void resetUserHandler() {
        initUserHandler();
        userCommon.setHandler(userHandler);
    }

    private void initUserHandler() {
        // TODO Auto-generated method stub
        userHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UserCommon.ACTION_USER_ADD: {
//                        if (userCommon.getSelf().getRole() == UserCommon.ROLE_HOST) {
//
//                        }
                        //int size = userCommon.getUserArrayList().size();


                    }
                    break;
                    case UserCommon.ACTION_USER_REMOVE:
                    {
//                        if (userCommon.getSelf().getRole() == UserCommon.ROLE_HOST) {
//
//                        }
//                        int size = userCommon.getUserArrayList().size();

                    }
                        break;
                    case UserCommon.ACTION_USER_MODIFY:

                        break;
                    case UserCommon.ROLEUPDATE:
                        if ((Integer) msg.obj == userCommon.getOwnID()) {

                        }
                        break;
                    case UserCommon.CHAT_PRIVATE_ON:

                        break;
                    case UserCommon.CHAT_PRIVATE_OFF:

                        break;
                    case UserCommon.CHAT_PUBLIC_ON:

                        break;
                    case UserCommon.CHAT_PUBLIC_OFF:

                        break;
                    default:
                        break;
                }

            }

        };
    }
}
