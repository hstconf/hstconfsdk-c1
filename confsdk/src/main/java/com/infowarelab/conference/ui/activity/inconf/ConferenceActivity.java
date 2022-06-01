package com.infowarelab.conference.ui.activity.inconf;

import static android.bluetooth.BluetoothProfile.GATT_SERVER;

import android.animation.Animator;
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
import android.graphics.Point;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.hd.screencapture.callback.ScreenCaptureStreamCallback;
import com.hd.screencapture.help.ScreenCaptureState;
import com.infowarelab.conference.ConferenceApplication;
import com.infowarelab.conference.PhoneStateUtil;
import com.infowarelab.conference.animation.Techniques;
import com.infowarelab.conference.animation.YoYo;
import com.infowarelab.conference.localDataCommon.LocalCommonFactory;
import com.infowarelab.conference.ui.activity.inconf.fragment.ConfASFragment;
import com.infowarelab.conference.ui.activity.inconf.fragment.ConfDsFragment;
import com.infowarelab.conference.ui.activity.inconf.fragment.ConfVideoFragment;
import com.infowarelab.conference.ui.activity.inconf.view.ConfAttendersView;
import com.infowarelab.conference.ui.activity.inconf.view.ConfInfoView;
import com.infowarelab.conference.ui.activity.inconf.view.ConfSettingView;
import com.infowarelab.conference.ui.activity.inconf.view.video.VideoSyncView;
import com.infowarelab.conference.ui.activity.inconf.view.vp.ADSAdapter;
import com.infowarelab.conference.ui.activity.inconf.view.vp.ADSViewPager;
import com.infowarelab.conference.ui.activity.preconf.ActHome;
import com.infowarelab.conference.ui.activity.preconf.ActHome2;
import com.infowarelab.conference.ui.tools.NoDoubleClickListener;
import com.infowarelab.conference.ui.view.CallattDialog;
import com.infowarelab.conference.ui.view.ExitDialog;
import com.infowarelab.conference.ui.view.PopIndex5;
import com.infowarelab.conference.ui.view.WifiDialog;
import com.infowarelab.conference.utils.PublicWay;
import com.infowarelab.conference.utils.SharedPreferencesUrls;
import com.infowarelab.hongshantongphone.FileUtils;
import com.infowarelab.hongshantongphone.PreferenceHelp;
import com.infowarelab.hongshantongphone.R;
import com.infowarelab.hongshantongphone.ScreenCapturePresenter;
import com.infowarelab.hongshantongphone.floatView.base.BaseActivity;
import com.infowarelab.hongshantongphone.floatView.float_view.FloatWindowManager;
import com.infowarelab.hongshantongphone.floatView.float_view.LastWindowInfo;
import com.infowarelabsdk.conference.audio.AudioCommon;
import com.infowarelabsdk.conference.callback.CallbackManager;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.AudioCommonImpl;
import com.infowarelabsdk.conference.common.impl.ConferenceCommonImpl;
import com.infowarelabsdk.conference.common.impl.DocCommonImpl;
import com.infowarelabsdk.conference.common.impl.ShareDtCommonImpl;
import com.infowarelabsdk.conference.common.impl.UserCommonImpl;
import com.infowarelabsdk.conference.common.impl.VideoCommonImpl;
import com.infowarelabsdk.conference.confctrl.ConferenceCommon;
import com.infowarelabsdk.conference.confctrl.UserCommon;
import com.infowarelabsdk.conference.shareDt.ShareDtCommon;
import com.infowarelabsdk.conference.transfer.Config;
import com.infowarelabsdk.conference.util.Constants;
import com.infowarelabsdk.conference.util.FileUtil;
import com.infowarelabsdk.conference.util.MessageEvent;
import com.infowarelabsdk.conference.util.ToastUtil;
import com.infowarelabsdk.conference.util.Utils;
import com.infowarelabsdk.conference.video.VideoCommon;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ConferenceActivity extends BaseActivity implements ConfDsFragment.FragmentInteraction
        , ScreenCaptureStreamCallback, BaseActivity.RequestPermissionCallBack {

    private static final String TAG = "InfowareLab.Debug";
    public static boolean isLogin = false;
    public int netHabit = 0;//0.未选择 1.wifi 2.net

    private FragmentManager fragmentManager;
    private ADSAdapter vpAdapter;
    private List<Fragment> vpFragments;
    private boolean isSwitching = false;
    private int curPage = 0;
    private ConfVideoFragment fragVideo;
    private ConfDsFragment fragDs;
    private ConfASFragment fragAs;
    private ConfAttendersView fragAtt;
    private ConfInfoView fragInfo;
    private ConfSettingView fragSetting;

    private RelativeLayout rlTopBar;
    private TextView tvTitle;
    private TextView tvConfFinish;
    private TextView tvReturnVideo;

    private ImageView ivAttAdd, ivVsAdd, ivDsAdd;

    private LinearLayout llRecordTime;
    private TextView tvRecordTime;

    private LinearLayout llBottomBar;
    private LinearLayout llIndex1, llIndex2, llIndex3, llIndex4, llIndex5;
    private ImageView ivIndex1, ivIndex2, ivIndex3, ivIndex4, ivIndex5;
    private TextView tvIndex1, tvIndex2, tvIndex3, tvIndex4, tvIndex5;
    private PopIndex5 popIndex5;

    private ADSViewPager vpCtrl;
    private FrameLayout flCtrl;
//    private View placeTop;
//    private View placeBottom;

    private AudioManager audioManager;
    private boolean isHaveStoped = false;
    public int position = 1;
    private int state = -1;
    private final int animationDuration = 200;

    private boolean isConnWifi = true;
    private BroadcastReceiver breceiver;
    private AudioCommonImpl audioCommon;
    private ShareDtCommonImpl asCommon;
    private UserCommonImpl userCommon;
    private ConferenceCommonImpl confCommon;
    private Handler confHandler, audioHandler, asHandler; //1. added the asHandler
    private TelephonyManager tpm;

    public static int pWidth = 0;
    public static int pHeight = 0;
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
    private boolean checkEmptyDoc = false;

    private byte[] sps;
    private byte[] pps;
    private boolean stopedOnPause = false;
    private View viewRoot;

    private int curModel = 0;
    public static final int MODEL_VS = 1; //视频平铺 PLAIN
    public static final int MODEL_DS = 2; //文档视图
    public static final int MODEL_AS = 3; //桌面共享
    public static final int MODEL_DS_VS = 4; //文档+视频
    public static final int MODEL_AS_VS = 5; //桌面共享+视频

    private int type = 0;
    private int bujuType = 0;

    private YoYo.YoYoString animationTop = null;
    private YoYo.YoYoString animationBottom = null;

    public Handler setViewHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            if (userCommon == null || userCommon.getSelf() == null) {
                setView(-100);
                return;
            }

            if (userCommon.getSelf().getRole() == UserCommon.ROLE_HOST
                    || userCommon.getSelf().getRole() == UserCommon.ROLE_SPEAKER) {
                boolean isAsEmpty = fragAs == null || fragAs.isEmpty();
                if (bujuType != ConferenceCommon.COMPONENT_TYPE_VIDEO
                        || !isAsEmpty) {
                    setView(-100);
                } else {
                    boolean isDsEmpty = fragDs == null || fragDs.isEmpty();
                    if (!isAsEmpty) {
                        bujuType = ConferenceCommon.COMPONENT_TYPE_AS;
                    } else if (isDsEmpty) {
                        bujuType = ConferenceCommon.COMPONENT_TYPE_VIDEO;
                    }

                    if (fragVideo != null && !fragVideo.isEmpty()) {
                        fragVideo.retCamera();
                    }

                    setView(bujuType);
                }
            } else {
                if (bujuType != ConferenceCommon.COMPONENT_TYPE_VIDEO || (fragAs != null && !fragAs.isEmpty())) {
                    setView(-100);
                } else {
                    setView(ConferenceCommon.COMPONENT_TYPE_VIDEO);
                }
            }
            hideLoading();
        }
    };
    private boolean isRunPage;

    Handler setPageHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            changeDSAS(msg.what);
            doSetHandler.sendEmptyMessage(20);
        }
    };

    private int curLeftPage = 1;
    private Button btnCameraRotate;
    private Timer timer = null;
    private TimerTask task = null;
    private boolean switchToChat = false;
    private boolean enterFromList = false;

    private void changeDSAS(int page) {

        /*
        if (isShare || curLeftPage == 2) {
            if (page == curLeftPage) return;
        }
        fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft;
        ft = fragmentManager.beginTransaction();
        if (page == 1) {
            if (fragDs != null && fragDs.isAdded()) {
                ft.hide(fragDs);
            }
            if (fragAs == null) {
//                fragAs = new FragAs(this);
                fragAs = new ConfASFragment();
                fragAs.setiCallParentView(this);
                ft.add(R.id.fl_inconf_content_container_left, fragAs, "As");
            } else {
                fragAs.setPM();
                ft.show(fragAs);
            }
            curLeftPage = 1;
        } else if (page == 2) {
            if (fragAs != null && fragAs.isAdded()) {
                ft.hide(fragAs);
            }
            if (fragDs == null) {
//                fragDs = new FragDs(this);
                fragDs = new FragDs();
                fragDs.setiCallParentView(this);
                ft.add(R.id.fl_inconf_content_container_left, fragDs, "Doc");
            } else {
                ft.show(fragDs);
                fragDs.initBuju();
            }
            curLeftPage = 2;
        }
//        ft.commitAllowingStateLoss();

        ft.commitAllowingStateLoss();*/

    }

    Handler doSetHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            doSetHandler.removeCallbacksAndMessages(null);

            fragVideo.doSetView();
            fragAs.doSetView();
            fragDs.doSetView();
        }
    };

    public void setView(int model) {
        if (isShare) return;

        if (!ConferenceApplication.isVideoMeeting) return;

        //Log.d(TAG, ">>>>>ConferenceActivity.setView: model=" + model);

        boolean flag = false;
        if (type == 3 || type == 5) {
            flag = true;
        } else {
            flag = false;
        }
        boolean isSingleVideo = false;
        boolean isVsEmpty = false;

        isVsEmpty = fragVideo == null || fragVideo.isEmpty();

        boolean isDsEmpty = false;
        boolean isAsEmpty = false;
        if (SharedPreferencesUrls.getInstance().getBoolean("flag", false)) {
            isDsEmpty = fragDs == null || fragDs.isEmpty() || flag;
            isAsEmpty = fragAs == null || fragAs.isEmpty();
            SharedPreferencesUrls.getInstance().putBoolean("flag", false);
        } else {
            if ((userCommon.getSelf().getRole() == UserCommon.ROLE_HOST
                    || userCommon.getSelf().getRole() == UserCommon.ROLE_SPEAKER)
                    && bujuType == ConferenceCommon.COMPONENT_TYPE_DS) {
                isDsEmpty = fragDs == null || fragDs.isEmpty();
            }else {
                isDsEmpty = fragDs == null || fragDs.isEmpty() || flag || isSingleVideo;
            }
            isAsEmpty = fragAs == null || fragAs.isEmpty() || isSingleVideo;
        }
        if (-100 == model) {
            if (userCommon == null || userCommon.getSelf() == null) {
                userCommon = (UserCommonImpl) CommonFactory.getInstance().getUserCommon();
            }
            if (isVsEmpty) {
                if (!isAsEmpty) {
                    model = MODEL_AS;
                    if (userCommon.getSelf().getRole() == UserCommon.ROLE_HOST
                            || userCommon.getSelf().getRole() == UserCommon.ROLE_SPEAKER) {
                        bujuType = ConferenceCommon.COMPONENT_TYPE_AS;
                    }else {
                        bujuType = -10086;
                    }
                } else if (!isDsEmpty) {
                    model = MODEL_DS;
                    if (userCommon.getSelf().getRole() == UserCommon.ROLE_HOST
                            || userCommon.getSelf().getRole() == UserCommon.ROLE_SPEAKER) {
                        bujuType = ConferenceCommon.COMPONENT_TYPE_DS;
                    }else {
                        bujuType = -10086;
                    }
                } else {
                    model = MODEL_VS;
                    if (userCommon.getSelf().getRole() == UserCommon.ROLE_HOST
                            || userCommon.getSelf().getRole() == UserCommon.ROLE_SPEAKER) {
                        bujuType = ConferenceCommon.COMPONENT_TYPE_VIDEO;
                    }else {
                        bujuType = -10086;
                    }
                }
            } else {
                if (!isAsEmpty) {
                    model = MODEL_AS_VS;
                    if (userCommon.getSelf().getRole() == UserCommon.ROLE_HOST
                            || userCommon.getSelf().getRole() == UserCommon.ROLE_SPEAKER) {
                        bujuType = ConferenceCommon.COMPONENT_TYPE_AS;
                    }else {
                        bujuType = -10086;
                    }
                } else if (!isDsEmpty) {
                    model = MODEL_DS_VS;
                    if (userCommon.getSelf().getRole() == UserCommon.ROLE_HOST
                            || userCommon.getSelf().getRole() == UserCommon.ROLE_SPEAKER) {
                        bujuType = ConferenceCommon.COMPONENT_TYPE_DS;
                    }else {
                        bujuType = -10086;
                    }
                } else {
                    model = MODEL_VS;
                    if (userCommon.getSelf().getRole() == UserCommon.ROLE_HOST
                            || userCommon.getSelf().getRole() == UserCommon.ROLE_SPEAKER) {
                        bujuType = ConferenceCommon.COMPONENT_TYPE_VIDEO;
                    }else {
                        bujuType = -10086;
                    }
                }
            }
            if (isAsEmpty && isDsEmpty) {
                model = MODEL_VS;
                if (userCommon.getSelf().getRole() == UserCommon.ROLE_HOST
                        || userCommon.getSelf().getRole() == UserCommon.ROLE_SPEAKER) {
                    bujuType = ConferenceCommon.COMPONENT_TYPE_VIDEO;
                }else {
                    bujuType = -10086;
                }
            }
        } else {
            if (model == ConferenceCommon.COMPONENT_TYPE_VIDEO) {
                model = MODEL_VS;
            } else if (model == ConferenceCommon.COMPONENT_TYPE_DS) {
                if (isVsEmpty) {
                    model = MODEL_DS;
                } else {
                    model = MODEL_DS_VS;
                }
            } else if (model == ConferenceCommon.COMPONENT_TYPE_AS) {
                if (isVsEmpty) {
                    model = MODEL_AS;
                } else {
                    model = MODEL_AS_VS;
                }
            }
        }


        //Log.d(TAG, ">>>>>ActConf.setView: bujuType=" + bujuType);

//        if (menuDialog != null){
//            btnState();
//        }

        if (!isShare) {
            if (model == curModel) return;
        }
        //LinearLayout.LayoutParams pleft = (LinearLayout.LayoutParams) flLeft.getLayoutParams();
        //LinearLayout.LayoutParams pright = (LinearLayout.LayoutParams) flRight.getLayoutParams();
        switch (model) {
            case MODEL_VS:
                Log.d(TAG, ">>>>>ActConf.setView: MODEL_VS");
//                cutline.setVisibility(View.GONE);
//                flLeft.setVisibility(View.GONE);
//                flRight.setVisibility(View.VISIBLE);
                break;
            case MODEL_DS:
                Log.d(TAG, ">>>>>ActConf.setView: MODEL_DS");
//                cutline.setVisibility(View.GONE);
//                flLeft.setVisibility(View.VISIBLE);
//                flRight.setVisibility(View.GONE);
                break;
            case MODEL_AS:
                Log.d(TAG, ">>>>>ActConf.setView: MODEL_AS");
//                cutline.setVisibility(View.GONE);
//                flLeft.setVisibility(View.VISIBLE);
//                flRight.setVisibility(View.GONE);
                break;
            case MODEL_AS_VS:
                Log.d(TAG, ">>>>>ActConf.setView: MODEL_AS_VS");
//                if (isShare) {
//                    cutline.setVisibility(View.GONE);
//                    flLeft.setVisibility(View.GONE);
//                    flRight.setVisibility(View.VISIBLE);
//                } else {
//                    cutline.setVisibility(View.VISIBLE);
//                    flLeft.setVisibility(View.VISIBLE);
//                    flRight.setVisibility(View.VISIBLE);
//                    pleft.weight = 1;
//                    pright.weight = 3;
//                }
                break;
            case MODEL_DS_VS:
                Log.d(TAG, ">>>>>ActConf.setView: MODEL_DS_VS");
//                cutline.setVisibility(View.VISIBLE);
//                flLeft.setVisibility(View.VISIBLE);
//                flRight.setVisibility(View.VISIBLE);
//                pleft.weight = 1;
//                pright.weight = 3;
                break;
            default:
                break;
        }

//        flLeft.setLayoutParams(pleft);
//        flRight.setLayoutParams(pright);
//        if (model == MODEL_AS || model == MODEL_AS_VS) {
//            isRunPage = false;
//            setPageHandler.sendEmptyMessage(1);
//        } else if (model == MODEL_DS || model == MODEL_DS_VS) {
//            isRunPage = true;
//            setPageHandler.sendEmptyMessage(2);
//        } else {
//            isRunPage = false;
//            setPageHandler.sendEmptyMessage(0);
//        }

    }

//            buJuDialog.setOnResultListener(new BuJuDialog.OnResultListener() {
//            @Override
//            public void selectType(int type) {
//                switch (type) {
//                    case 0:
//                        bujuType = ConferenceCommon.COMPONENT_TYPE_VIDEO;
//                        mCurrentLayoutType = bujuType;
//                        //平铺
//                        conferenceCommon.setLayout(ConferenceCommon.COMPONENT_TYPE_VIDEO, ConferenceCommon.RT_CONF_UI_TYPE_LIST_VIDEO);
//                        videoCommon.setVideoLayout(0);
//                        setView(ConferenceCommon.COMPONENT_TYPE_VIDEO);
//                        videoCommon.setCurLayoutMode(VideoCommon.LayoutMode.MODE_PLAIN);
//                        break;
//                    case 1:
//                        //主次'
//                        bujuType = ConferenceCommon.COMPONENT_TYPE_VIDEO;
//                        mCurrentLayoutType = bujuType;
//                        conferenceCommon.setLayout(ConferenceCommon.COMPONENT_TYPE_VIDEO, ConferenceCommon.RT_CONF_UI_TYPE_LIST_VIDEO);
//                        setView(ConferenceCommon.COMPONENT_TYPE_VIDEO);
//                        videoCommon.setCurLayoutMode(VideoCommon.LayoutMode.MODE_SPEAKER);
//                        if (videoCommon.getSyncMap().containsValue(userCommon.getSelf().getUid())) {
//                            for (Integer key : videoCommon.getSyncMap().keySet()) {
//                                if (userCommon.getSelf().getUid() == videoCommon.getSyncMap().get(key)) {
//                                    videoCommon.setVideoLayout(key);
//                                }
//                            }
//                        } else {
//                            for (Integer key : videoCommon.getSyncMap().keySet()) {
//                                if (key != null) {
//                                    videoCommon.setVideoLayout(key);
//                                    break;
//                                }
//                            }
//                        }
//                        break;
//                    case 2:
//                        //数据
//                        if ((fragAs == null || fragAs.isEmpty()) && (fragDs == null ||
//                                fragDs.isEmpty())) {
//                            ToastUtil.showMessage(ActConf.this, "暂无任何共享,未能切换布局", 2 * 1000);
//                            return;
//                        }
//                        conferenceCommon.setLayout(ConferenceCommon.COMPONENT_TYPE_DS, ConferenceCommon.RT_CONF_UI_TYPE_LEFT_TOP_VIDEO);
//                        if (isShare || (fragAs != null && !fragAs.isEmpty())) {
//                            bujuType = ConferenceCommon.COMPONENT_TYPE_AS;
//                            mCurrentLayoutType = bujuType;
//                            videoCommon.setVideoLayout(0);
//                            setView(ConferenceCommon.COMPONENT_TYPE_AS);
//                        } else if (fragDs != null && !fragDs.isEmpty()) {
//                            bujuType = ConferenceCommon.COMPONENT_TYPE_DS;
//                            mCurrentLayoutType = bujuType;
//                            videoCommon.setVideoLayout(0);
//                            setView(ConferenceCommon.COMPONENT_TYPE_DS);
//                            if (menuDialog != null && menuDialog.isShowing()) {
//                                btnState();
//                            }
//                        }
//
//                        break;
//                    case 3:
//                        //语音激励
//                        bujuType = ConferenceCommon.COMPONENT_TYPE_VIDEO;
//                        mCurrentLayoutType = bujuType;
//                        videoCommon.setCurLayoutMode(VideoCommon.LayoutMode.MODE_VOICE);
//                        setView(ConferenceCommon.COMPONENT_TYPE_VIDEO);
//                        conferenceCommon.setLayout(ConferenceCommon.COMPONENT_TYPE_VIDEO, ConferenceCommon.RT_CONF_UI_TYPE_LIST_VIDEO);
//                        videoCommon.setVideoLayout(-1);
//                        videoCommon.setCurLayoutMode(VideoCommon.LayoutMode.MODE_VOICE);
//                        //取第一个元素
//                        for (Integer key : videoCommon.getSyncMap().keySet()) {
//                            uid = videoCommon.getSyncMap().get(key);
//                            if (Build.MODEL.indexOf("MV200") == -1
//                                    && Build.MODEL.indexOf("MRUT33") == -1) {
//                                if (fragVs != null) fragVs.setMaxVoice(uid);
//                            } else {
//                                if (fragVs1 != null) fragVs1.setMaxVoice(uid);
//                            }
//                            return;
//                        }
//                        break;
//                    default:
//                        break;
//                }
////                //去掉SVC
////                videoCommon.disableSVC();
//            }
//        });
//    }

    @Override
    protected int getLayoutResId() {
        return R.layout.conference_phone;
    }

    @Override
    protected void initData() {

        Log.d(TAG, "Conf4Phone.initData())");

//        if (ConfAPI.getInstance().getLastError() != -1000)
//        {
//            Log.d(TAG, "Conf4Phone.initData: Error and Exit");
//            finish();
//            return;
//        }

        //注册EventBus
        EventBus.getDefault().register(this);
        floatWindowType = FloatWindowManager.FW_TYPE_ROOT_VIEW;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (NetworkInfo.State.CONNECTED == manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState()) {
            ConferenceApplication.isVideoMeeting = true;
        } else {
            ConferenceApplication.isVideoMeeting = false;
        }

        audioCommon = (AudioCommonImpl) CommonFactory.getInstance().getAudioCommon();
        userCommon = (UserCommonImpl) CommonFactory.getInstance().getUserCommon();
        confCommon = (ConferenceCommonImpl) CommonFactory.getInstance().getConferenceCommon();
        asCommon = (ShareDtCommonImpl) CommonFactory.getInstance().getSdCommon();

        screenCapturePresenter = new ScreenCapturePresenter(this);
        help = new PreferenceHelp(this);
        if (isWriteFile) {
            _fr = new File("/sdcard/screen.src");
            try {
                _out = new FileOutputStream(_fr);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        initAudio();
        initTelephoneListener();
        setmRequestPermissionCallBack(this);
    }

    private void initHandler() {
        initConfHandler();
        initAudioHandler();
        initASHandler(); //2. initialize the asHandler
    }

    private void initAudioHandler() {
        audioHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case AudioCommon.MIC_ON:
                        if (audioCommon == null) {
                            audioCommon.isClickUsed = true;
                            break;
                        }
                        Log.d("InfowareLab.Debug", ">>>>>>AudioCommon.MIC_ON: START");
                        if (audioCommon.isMICWork()) {
                            if (!audioCommon.isRecording()) {
                                tryStartSend();
                                showShortToast(R.string.audioon);
                                break;
                            }
                            Log.d("InfowareLab.Debug", ">>>>>>AudioCommon.MIC_ON: ENABLE_ON");
                            ivIndex1.setImageResource(R.drawable.ic_index_1_enable_on);
                            tvIndex1.setTextColor(getResources().getColor(R.color.index_red));
                        } else {
                            if (!audioCommon.isRecording()){
                                tryStartSend();
                                showShortToast(R.string.audioon);
                                break;
                            }
                            Log.d("InfowareLab.Debug", ">>>>>>AudioCommon.MIC_ON: DISABLE_ON");
                            ivIndex1.setImageResource(R.drawable.ic_index_1_disable_on);
                            tvIndex1.setTextColor(getResources().getColor(R.color.index_white));
                        }
                        audioCommon.isClickUsed = true;
                        break;
                    case AudioCommon.MIC_OFF:
                        if (audioCommon == null) {
                            audioCommon.isClickUsed = true;
                            break;
                        }
                        Log.d("InfowareLab.Debug", ">>>>>>AudioCommon.MIC_OFF: START");
                        if (audioCommon.isMICWork()) {
                            if (audioCommon.isRecording()){
                                audioCommon.stopSend();
                                showShortToast(R.string.audiooff);
                                break;
                            }
                            Log.d("InfowareLab.Debug", ">>>>>>AudioCommon.MIC_OFF: ENABLE_OFF");
                            ivIndex1.setImageResource(R.drawable.ic_index_1_enable_off);
                            tvIndex1.setTextColor(getResources().getColor(R.color.index_red));
                        } else {
                            if (audioCommon.isRecording()){
                                audioCommon.stopSend();
                                showShortToast(R.string.audiooff);
                                break;
                            }
                            Log.d("InfowareLab.Debug", ">>>>>>AudioCommon.MIC_OFF: DISABLE_OFF");
                            ivIndex1.setImageResource(R.drawable.ic_index_1_disable_off);
                            tvIndex1.setTextColor(getResources().getColor(R.color.index_white));
                        }
                        audioCommon.isClickUsed = true;
                        break;
                    case AudioCommon.MIC_ENABLE:
                        if (audioCommon == null) break;
                        if (audioCommon.isRecording()) {
                            ivIndex1.setImageResource(R.drawable.ic_index_1_enable_on);
                            tvIndex1.setTextColor(getResources().getColor(R.color.index_red));
                        } else {
                            ivIndex1.setImageResource(R.drawable.ic_index_1_enable_off);
                            tvIndex1.setTextColor(getResources().getColor(R.color.index_red));
                        }
                        break;
                    case AudioCommon.MIC_DISABLE:
                        if (audioCommon == null) break;
                        if (audioCommon == null)
                            if (audioCommon.isRecording()) {
                                ivIndex1.setImageResource(R.drawable.ic_index_1_disable_on);
                                tvIndex1.setTextColor(getResources().getColor(R.color.index_white));
                            } else {
                                ivIndex1.setImageResource(R.drawable.ic_index_1_disable_off);
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
                    case AudioCommon.MAX_VOICE:
                        //Log.d("InfowareLab.Debug", ">>>>>>AudioCommon.MAX_VOICE: uid=" + msg.arg1);
                        if (fragVideo != null) fragVideo.highlightVideobyMaxVoice(msg.arg1);
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

        asCommon.subscribeWithMode(1);
        asCommon.setHandler(asHandler);
    }

    private void initConfHandler() {
        confHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case ConferenceCommon.LEAVE:
                        if ((Integer) msg.obj == ConferenceCommon.FORCELEAVE) {
                            showShortToast(R.string.forceLeave);
                        } else if ((Integer) msg.obj == ConferenceCommon.HOSTCLOSECONF) {
                            showShortToast(R.string.leave);
                        } else if ((Integer) msg.obj == 40100) {
                            showShortToast(R.string.offlineLeave);
                        }
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
                            if (popIndex5 != null && popIndex5.isShowing()) {
                                popIndex5.updateRec(true);
                                popIndex5.dismiss();
                            }
                            if (fragVideo != null) fragVideo.beginCloudRecord(0);
                            if (fragDs != null) fragDs.beginCloudRecord();
                        } else {
                            stopTiming(1);
                            if (popIndex5 != null && popIndex5.isShowing()) {
                                popIndex5.updateRec(false);
                                popIndex5.dismiss();
                            }
                            if (fragVideo != null) fragVideo.stopCloudRecord(0);
                        }
                        break;
                    case 999:

//                        if (homeCount == 0) {
//                            if (!ConferenceApplication.getConferenceApp().getSdkMode()) {
//                                Intent intent = new Intent(ConferenceActivity.this, ActHome.class);
//                                startActivity(intent);
//                            }
//                            else if (enterFromList) {
//                                Intent intent = new Intent(ConferenceActivity.this, ActHome2.class);
//                                startActivity(intent);
//                            }
//
//                            homeCount++;
//                        }

                        finish();

                        BacktoURL();

                        break;

                    default:
                        break;
                }
            }


        };
        DocCommonImpl.isAnnotation = false;
        confCommon.setHandler(confHandler);
    }

    private void BacktoURL() {
        //String url = "snssdk1128://user/profile/95627491132?refer=web&gd_label=click_wap_profile_bottom&type=need_follow&needlaunchlog=1";
        try {
            Uri uri = Uri.parse(Config.BackURL);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initView() {

        Log.d(TAG, "Conf4Phone.initView");

        Intent intent = getIntent();

        if (intent != null){
            enterFromList = intent.getBooleanExtra("enterFromList", false);
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ConferenceApplication.setStatusBarColor(this, getColor(R.color.black                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       ));
        }

        getScreenWidthHeight();

        //android.view.WindowManager.LayoutParams p = getWindow().getAttributes();

        viewRoot = (View) findViewById(R.id.view_root);
//
        vpCtrl = (ADSViewPager) findViewById(R.id.vp_inconf_ctrl);
        flCtrl = (FrameLayout) findViewById(R.id.fl_inconf_ctrl);
//       placeTop = findViewById(R.id.view_inconf_ctrl_place_top);
//       placeBottom = findViewById(R.id.view_inconf_ctrl_place_bottom);

        rlTopBar = (RelativeLayout) findViewById(R.id.rl_inconf_ctrl_top);
        tvTitle = (TextView) findViewById(R.id.tv_inconf_ctrl_title);
        tvReturnVideo = (TextView) findViewById(R.id.tv_inconf_ctrl_returnvideo);
        btnCameraRotate = (Button) findViewById(R.id.btnChangeCamera);

        tvConfFinish = (TextView) findViewById(R.id.tv_inconf_ctrl_conffinish);
        ivAttAdd = (ImageView) findViewById(R.id.iv_inconf_ctrl_att_add);
        ivVsAdd = (ImageView) findViewById(R.id.iv_inconf_ctrl_vs_add);
        ivDsAdd = (ImageView) findViewById(R.id.iv_inconf_ctrl_ds_add);

        llRecordTime = (LinearLayout) findViewById(R.id.ll_inconf_ctrl_record);
        tvRecordTime = (TextView) findViewById(R.id.tv_inconf_ctrl_recordtime);

        llBottomBar = (LinearLayout) findViewById(R.id.ll_inconf_ctrl_bottom);
        llIndex1 = (LinearLayout) findViewById(R.id.ll_inconf_ctrl_index_1);
        llIndex2 = (LinearLayout) findViewById(R.id.ll_inconf_ctrl_index_2);
        llIndex3 = (LinearLayout) findViewById(R.id.ll_inconf_ctrl_index_3);
        llIndex4 = (LinearLayout) findViewById(R.id.ll_inconf_ctrl_index_4);
        llIndex5 = (LinearLayout) findViewById(R.id.ll_inconf_ctrl_index_5);
        ivIndex1 = (ImageView) findViewById(R.id.iv_inconf_ctrl_index_1);
        ivIndex2 = (ImageView) findViewById(R.id.iv_inconf_ctrl_index_2);
        ivIndex3 = (ImageView) findViewById(R.id.iv_inconf_ctrl_index_3);
        ivIndex4 = (ImageView) findViewById(R.id.iv_inconf_ctrl_index_4);
        ivIndex5 = (ImageView) findViewById(R.id.iv_inconf_ctrl_index_5);
        tvIndex1 = (TextView) findViewById(R.id.tv_inconf_ctrl_index_1);
        tvIndex2 = (TextView) findViewById(R.id.tv_inconf_ctrl_index_2);
        tvIndex3 = (TextView) findViewById(R.id.tv_inconf_ctrl_index_3);
        tvIndex4 = (TextView) findViewById(R.id.tv_inconf_ctrl_index_4);
        tvIndex5 = (TextView) findViewById(R.id.tv_inconf_ctrl_index_5);


        NoDoubleClickListener noDoubleClickListener = new NoDoubleClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                int id = v.getId();
                if (id == R.id.ll_inconf_ctrl_index_1) {
                    if (audioCommon.isMICWork() && audioCommon.isClickUsed) {
                        if (audioCommon.isRecording()) {
                            audioCommon.isClickUsed = false;
                            audioCommon.stopSend();
                            showShortToast(R.string.audiooff);
                        } else {
                            audioCommon.isClickUsed = false;
                            audioCommon.startSend();
                            showShortToast(R.string.audioon);
                        }
                    } else {
                        showShortToast(R.string.audiodisable);
                    }
                } else if (id == R.id.ll_inconf_ctrl_index_2) {
                    if (curPage != 2) {
                        switchFragment(2);

//                        if (fragVideo.isLocalVideoOpened()) {
//                            showShortToast(R.string.cameraon);
//                        } else {
//                            showShortToast(R.string.cameraoff);
//                        }

                        return;
                    }

                    if (((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).isHost() || ((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).isVideoPreviewPriviledge()) {

//                        if (videoCommon.LOCAL_VIDEO_CHANNEL_ID > 0) {
//                            if (videoCommon.LOCAL_VIDEO_CHANNEL_ID != localChannelID) {
//                                localChannelID = videoCommon.LOCAL_VIDEO_CHANNEL_ID;
//                            }
//                        }
//
//                        if (localChannelID > 0) {
//                            if (userCommon.getSelf().getRole() == UserCommon.ROLE_HOST) {
//                                videoCommon.openVideo(localChannelID, (SurfaceView) null);
//                                videoCommon.setSyncVedio(localChannelID, true);
//                                Log.d("InfowareLab.Debug", "FragVs.videoCommon.openVideo(local HOST): " + localChannelID);
//                            }
//                        }

                        if (fragVideo.isLocalVideoOpened()) {
                            fragVideo.enableCamera(false);
                            fragVideo.closeMyVideo();
                            btnCameraRotate.setVisibility(View.GONE);
                            ivIndex2.setImageResource(R.drawable.ic_index_2_sel_off);
                            showShortToast(R.string.cameraoff);
                        } else {
                            fragVideo.enableCamera(true);
                            //if (userCommon.getSelf().isShareVideo())
                                fragVideo.openMyVideo();
                            //;
                            btnCameraRotate.setVisibility(View.VISIBLE);
                            ivIndex2.setImageResource(R.drawable.ic_index_2_sel);
                            showShortToast(R.string.cameraon);
                        }
                    }

                } else if (id == R.id.ll_inconf_ctrl_index_3) {

                    if (curPage == 3) {

                        if (((DocCommonImpl) CommonFactory.getInstance().getDocCommon()).getPrivateShareDocPriviledge() || ((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).isHost())
                        {
                            if (confCommon != null) {
                                confCommon.setLayout(ConferenceCommon.COMPONENT_TYPE_DS, ConferenceCommon.RT_CONF_UI_TYPE_LEFT_TOP_VIDEO);
                                Log.d("InfowareLab.Debug", ">>>>>>setLayout: ConferenceCommon.COMPONENT_TYPE_DS");
                            }
                        }

                        fragDs.onShowShareMenu();
                    }
                    else {
                        switchFragment(3);
                        checkEmptyDoc = true;
                    }
                } else if (id == R.id.ll_inconf_ctrl_index_4) {
                    switchFragment(4);
                } else if (id == R.id.ll_inconf_ctrl_index_5) {
                    showIndex5Pop(v);
                } else if (id == R.id.tv_inconf_ctrl_conffinish) {
                    showExitDialog();
                }
                else if (id == R.id.tv_inconf_ctrl_returnvideo) {
                    switchFragment(2);
                }
                else if (id == R.id.btnChangeCamera)
                {
                    fragVideo.rotateCamera();
                }
            }
        };
        llIndex1.setOnClickListener(noDoubleClickListener);
        llIndex2.setOnClickListener(noDoubleClickListener);
        llIndex3.setOnClickListener(noDoubleClickListener);
        llIndex4.setOnClickListener(noDoubleClickListener);
        llIndex5.setOnClickListener(noDoubleClickListener);
        tvConfFinish.setOnClickListener(noDoubleClickListener);
        tvReturnVideo.setOnClickListener(noDoubleClickListener);
        btnCameraRotate.setOnClickListener(noDoubleClickListener);

        initAudioFilter();
        initReceiver();

        initHandler();
        initIndexFrame();
        initRecord();

        restartHideBarTimer();

        PublicWay.setConfActivity(this);
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

        ConferenceApplication.SUB_VIDEO_W = ConferenceApplication.Screen_W / 4;
        ConferenceApplication.SUB_VIDEO_H = ConferenceApplication.SUB_VIDEO_W / 9 * 16;

        ConferenceApplication.MAIN_VIDEO_W = ConferenceApplication.Screen_W;

        ConferenceApplication.StateBar_H = getStatusBarHeight(this);
        if (isNavigationBarShow())
            ConferenceApplication.NavigationBar_H = getNavigationBarHeight(this);//ConferenceApplication.Screen_H - ConferenceApplication.Root_H - ConferenceApplication.StateBar_H
        else
            ConferenceApplication.NavigationBar_H = 0;

        ConferenceApplication.Root_W = ConferenceApplication.Screen_W;
        ConferenceApplication.Root_H = ConferenceApplication.Screen_H - ConferenceApplication.StateBar_H - ConferenceApplication.NavigationBar_H;
        //ConferenceApplication.Root_H = llRoot.getHeight()>0?llRoot.getHeight():ConferenceApplication.Screen_H;
        //ConferenceApplication.Root_W = llRoot.getWidth()>0?llRoot.getWidth():ConferenceApplication.Screen_W;

//        if ((1.0f * ConferenceApplication.Root_W) / (1.0f * ConferenceApplication.Root_H) < 9.0f/16.0f)
//            ConferenceApplication.Keep_16_9 = true;
//        else
//            ConferenceApplication.Keep_16_9 = false;

        ConferenceApplication.Top_H = getResources().getDimensionPixelOffset(R.dimen.height_6_80);
        ConferenceApplication.Bottom_H = getResources().getDimensionPixelOffset(R.dimen.height_7_80);

        ConferenceApplication.MAIN_VIDEO_H = ConferenceApplication.Root_H - ConferenceApplication.Top_H - ConferenceApplication.Bottom_H;

//        int[] location = new int[2];
//		llRoot.getLocationOnScreen(location);
//		if(location[1]>0){
//			ConferenceApplication.StateBar_H = location[1];
//			ConferenceApplication.NavigationBar_H = getDaoHangHeight(this);//ConferenceApplication.Screen_H - ConferenceApplication.Root_H - ConferenceApplication.StateBar_H;
//		}

        Log.d("InfowareLab.Debug", "Logo ScreenW=" + ConferenceApplication.Screen_W
                + " ScreenH=" + ConferenceApplication.Screen_H
                + " RootW=" + ConferenceApplication.Root_W
                + " RootH=" + ConferenceApplication.Root_H
                + " StateH=" + ConferenceApplication.StateBar_H
                + " KeyH=" + ConferenceApplication.NavigationBar_H
                + " Density=" + ConferenceApplication.DENSITY
                + " Top_H=" + ConferenceApplication.Top_H
                + " Bottom_H=" + ConferenceApplication.Bottom_H);

        Log.d("InfowareLab.Debug", "density = " + ConferenceApplication.DENSITY + ", widthPixels = " + ConferenceApplication.SCREEN_WIDTH
                + ", heightPixels = " + ConferenceApplication.SCREEN_HEIGHT);
    }


    private void initIndexFrame() {
        fragmentManager = getSupportFragmentManager();

        vpFragments = new ArrayList<>();
        if (null == fragDs) {
            fragDs = new ConfDsFragment(this);
            ivDsAdd.setOnClickListener(fragDs);
        }
        if (null == fragAs) {
            fragAs = new ConfASFragment(this);
        }
        vpFragments.add(fragAs);
        vpFragments.add(fragDs);

        vpAdapter = new ADSAdapter(fragmentManager, vpFragments);
        vpCtrl.setAdapter(vpAdapter);
        vpCtrl.setVisibility(View.GONE);

        FragmentTransaction ft;
        ft = fragmentManager.beginTransaction();
        if (null == fragVideo) {
            fragVideo = new ConfVideoFragment(this);
            ivVsAdd.setOnClickListener(fragVideo);

        }
        ft.add(R.id.fl_inconf_ctrl, fragVideo, "Video");

        if (null == fragAtt) {
            fragAtt = new ConfAttendersView(this);
        }
        ft.add(R.id.fl_inconf_ctrl, fragAtt, "Att");

        ft.hide(fragAtt).show(fragVideo).commit();
        setIndexState(2, true);
        curPage = 2;

        openOrietation();
    }

    private void showIndex5Pop(View v) {
        popIndex5 = new PopIndex5(this, curPage, new PopIndex5.OnSelectListener() {
            @Override
            public void onSelectSync(){
//                if (curPage != 2)
//                    switchFt(2);

                fragVideo.showVsSyncUI();
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
                if (fragAtt != null){
                    if (curPage != 4) {
                        switchFragment(4);
                        switchToChat = true;
                    }
                    else
                        fragAtt.gotoPublishChattingView();
                }
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
                    Log.d("InfowareLab.DS", "ConfDsFragment.shareSetHandler: fragDs.doShowView as fragAs.isEmpty");
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
                tvReturnVideo.setVisibility(View.GONE);
                if (fragVideo.isLocalVideoOpened())
                    btnCameraRotate.setVisibility(View.VISIBLE);
                if (fragVideo == null) {
                    fragVideo = new ConfVideoFragment(this);
                    ft.add(R.id.fl_inconf_ctrl, fragVideo, "Video");
                } else {
                    ft.show(fragVideo);
                }

//                if (!ConferenceApplication.isVideoMeeting) {
//                    ivVsAdd.setVisibility(View.GONE);
//                } else if (((VideoCommonImpl) CommonFactory.getInstance().getVideoCommon()).isVideoPreviewPriviledge() || ((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).isHost()) {
//                    if (confCommon != null) {
//                        confCommon.setLayout(ConferenceCommon.COMPONENT_TYPE_VIDEO, ConferenceCommon.RT_CONF_UI_TYPE_LIST_VIDEO);
//                        Log.d("InfowareLab.Debug", ">>>>>>setLayout: ConferenceCommon.COMPONENT_TYPE_VIDEO");
//                    }
//                    ivVsAdd.setVisibility(View.VISIBLE);
//                } else {
//                    ivVsAdd.setVisibility(View.GONE);
//                }

                ivVsAdd.setVisibility(View.GONE);
                ivVsAdd.setOnClickListener(fragVideo);
                curPage = 2;
                showBars();

                if (((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).isHost())
                {
                    if (confCommon != null) {
                        confCommon.setLayout(ConferenceCommon.COMPONENT_TYPE_VIDEO, ConferenceCommon.RT_CONF_UI_TYPE_LIST_VIDEO);
                        Log.d("InfowareLab.Debug", ">>>>>>setLayout: ConferenceCommon.COMPONENT_TYPE_VIDEO");
                    }
                }

                break;
            case 3:
                setIndexState(3, false);
                tvTitle.setText(getResources().getString(vpCtrl.getCurrentItem() == 0 ? R.string.as_title : R.string.share_title));

                tvReturnVideo.setVisibility(View.VISIBLE);

                if (((DocCommonImpl) CommonFactory.getInstance().getDocCommon()).getPrivateShareDocPriviledge() || ((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).isHost())
                {
                    if (confCommon != null) {
                        confCommon.setLayout(ConferenceCommon.COMPONENT_TYPE_DS, ConferenceCommon.RT_CONF_UI_TYPE_LEFT_TOP_VIDEO);
                        Log.d("InfowareLab.Debug", ">>>>>>setLayout: ConferenceCommon.COMPONENT_TYPE_DS");
                    }
                }

//                if (vpCtrl.getCurrentItem() == 0) {
//                    ivDsAdd.setVisibility(View.GONE);
//                } else if (((DocCommonImpl) CommonFactory.getInstance().getDocCommon()).getPrivateShareDocPriviledge() || ((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).isHost()) {
//                    if (confCommon != null) {
//                        confCommon.setLayout(ConferenceCommon.COMPONENT_TYPE_DS, ConferenceCommon.RT_CONF_UI_TYPE_LEFT_TOP_VIDEO);
//                        Log.d("InfowareLab.Debug", ">>>>>>setLayout: ConferenceCommon.COMPONENT_TYPE_DS");
//                    }
//                    ivDsAdd.setVisibility(View.VISIBLE);
//                } else {
//                    ivDsAdd.setVisibility(View.GONE);
//                }
                ivDsAdd.setVisibility(View.GONE);
                ivDsAdd.setOnClickListener(fragDs);
                curPage = 3;

                btnCameraRotate.setVisibility(View.GONE);

                if (llBottomBar.getVisibility() == View.GONE || rlTopBar.getVisibility() == View.GONE)
                    showBarsWithAnimation();

                if (fragAs.isEmpty() && fragDs != null && fragDs.isEmpty() && checkEmptyDoc){
                    fragDs.onShowShareMenu();
                    checkEmptyDoc = false;
                }

                break;
            case 4:
                setIndexState(4, false);
                tvTitle.setText(getResources().getString(R.string.attenders));
                tvReturnVideo.setVisibility(View.VISIBLE);

                if (fragAtt == null) {
                    fragAtt = new ConfAttendersView(this);
                    ft.add(R.id.fl_inconf_ctrl, fragAtt, "Att");
                } else {
                    ft.show(fragAtt);
                }
//                if(((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).isHost()){
//                    ivAttAdd.setVisibility(View.VISIBLE);
//                }else{
//                    ivAttAdd.setVisibility(View.GONE);
//                }
                ivAttAdd.setVisibility(View.GONE);
                ivAttAdd.setOnClickListener(fragAtt);
                curPage = 4;

                btnCameraRotate.setVisibility(View.GONE);

                if (llBottomBar.getVisibility() == View.GONE || rlTopBar.getVisibility() == View.GONE)
                    showBarsWithAnimation();

                if (switchToChat){
                    switchToChat = false;
                    fragAtt.gotoPublishChattingView();
                }

                break;
            case 5:
                setIndexState(5, false);
                tvTitle.setText(getResources().getString(R.string.more_info));
                tvReturnVideo.setVisibility(View.VISIBLE);

                if (fragInfo == null) {
                    fragInfo = new ConfInfoView(this);
                    ft.add(R.id.fl_inconf_ctrl, fragInfo, "Info");
                } else {
                    ft.show(fragInfo);
                }
                curPage = 5;

                btnCameraRotate.setVisibility(View.GONE);

                if (llBottomBar.getVisibility() == View.GONE || rlTopBar.getVisibility() == View.GONE)
                    showBarsWithAnimation();

                break;
            case 6:
                setIndexState(5, false);
                tvTitle.setText(getResources().getString(R.string.setting_title));
                tvReturnVideo.setVisibility(View.VISIBLE);

                if (fragSetting == null) {
                    fragSetting = new ConfSettingView(this);
                    ft.add(R.id.fl_inconf_ctrl, fragSetting, "Setting");
                } else {
                    ft.show(fragSetting);
                }
                curPage = 6;

                btnCameraRotate.setVisibility(View.GONE);

                if (llBottomBar.getVisibility() == View.GONE || rlTopBar.getVisibility() == View.GONE)
                    showBarsWithAnimation();

                break;

            default:

                break;
        }
        ft.commitAllowingStateLoss();
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
                btnCameraRotate.setVisibility(View.GONE);
                break;
            case 2:
                if (fragVideo.isLocalVideoOpened() || init) {
                    btnCameraRotate.setVisibility(View.VISIBLE);
                    ivIndex2.setImageResource(R.drawable.ic_index_2_sel);
                }
                else {
                    btnCameraRotate.setVisibility(View.GONE);
                    ivIndex2.setImageResource(R.drawable.ic_index_2_sel_off);
                }

                ivIndex3.setImageResource(R.drawable.ic_index_3_nor);
                ivIndex4.setImageResource(R.drawable.ic_index_4_nor);
                ivIndex5.setImageResource(R.drawable.ic_index_5_nor);
                tvIndex2.setTextColor(getResources().getColor(R.color.index_red));
                tvIndex3.setTextColor(getResources().getColor(R.color.index_white));
                tvIndex4.setTextColor(getResources().getColor(R.color.index_white));
                tvIndex5.setTextColor(getResources().getColor(R.color.index_white));
                //ivVsAdd.setVisibility(View.VISIBLE);
                //ivDsAdd.setVisibility(View.GONE);
                ivAttAdd.setVisibility(View.GONE);
                break;
            case 3:
                if (fragVideo.isLocalVideoOpened() || init) {
                    //btnCameraRotate.setVisibility(View.VISIBLE);
                    ivIndex2.setImageResource(R.drawable.ic_index_2_nor);
                }
                else {
                    btnCameraRotate.setVisibility(View.GONE);
                    ivIndex2.setImageResource(R.drawable.ic_index_2_nor_off);
                }

                ivIndex3.setImageResource(R.drawable.ic_index_3_sel);
                ivIndex4.setImageResource(R.drawable.ic_index_4_nor);
                ivIndex5.setImageResource(R.drawable.ic_index_5_nor);
                tvIndex2.setTextColor(getResources().getColor(R.color.index_white));
                tvIndex3.setTextColor(getResources().getColor(R.color.index_red));
                tvIndex4.setTextColor(getResources().getColor(R.color.index_white));
                tvIndex5.setTextColor(getResources().getColor(R.color.index_white));
                ivVsAdd.setVisibility(View.GONE);
                //ivDsAdd.setVisibility(View.VISIBLE);
                ivDsAdd.setVisibility(View.GONE);
                ivAttAdd.setVisibility(View.GONE);
                btnCameraRotate.setVisibility(View.GONE);
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
                btnCameraRotate.setVisibility(View.GONE);
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
                btnCameraRotate.setVisibility(View.GONE);
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
                    if (netHabit != 0)
                        showShortToast(R.string.warnReVideo);
                }
//                isConnWifi = true;
            }
        }
    };

    private void showWifiDialog() {
        wifidialog = new WifiDialog(ConferenceActivity.this, ConferenceApplication.Screen_W * 4 / 5, new WifiDialog.OnResultListener() {
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
            if (fragVideo.getOnBackPressed()) {
                fragVideo.doShowView();
            }
//            if (!ConferenceApplication.isVideoMeeting) {
//                ivVsAdd.setVisibility(View.GONE);
//            } else if (((VideoCommonImpl) CommonFactory.getInstance().getVideoCommon()).isVideoPreviewPriviledge() || ((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).isHost()) {
//                ivVsAdd.setVisibility(View.VISIBLE);
//            } else {
//                ivVsAdd.setVisibility(View.GONE);
//            }
            ivVsAdd.setVisibility(View.GONE);
        } else if (curPage == 3 && vpCtrl.getCurrentItem() == 0) {
            fragAs.doShowView();
        }
    }


    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onResume() {
        super.onResume();
        if (screenCapturePresenter.getFile() != null) {
            FileUtils.DeleteFolder(screenCapturePresenter.getFile().getPath());
            FileUtils.DeleteFolder(screenCapturePresenter.getFile().getAbsolutePath());
        }
        //关闭弹窗
        if (!isShare) {
            closeFloatWindow();
            LastWindowInfo.getInstance().clear();
        }
        audioCommon.startReceive();
        audioCommon.setVolume(255);

//        if (stopedOnPause && !audioCommon.isRecording()) {
//            llIndex1.performClick();
//        }

        if (mWakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "XYTEST");
            mWakeLock.acquire();
        }

        if (state == 1) {
            Log.d("InfowareLab.Debug", ">>>>>>setSpeakerphoneOn => false");
            audioManager.setSpeakerphoneOn(false);
        } else {
            Log.d("InfowareLab.Debug", ">>>>>>setSpeakerphoneOn => true");
            audioManager.setSpeakerphoneOn(true);
        }

        if (isHaveStoped) {
            position = ActHome.position;
        }

        isHaveStoped = false;

        if (curOriInfo != getResources().getConfiguration().orientation)
        {
            curOriInfo = getResources().getConfiguration().orientation;
            onConfigurationChanged(getResources().getConfiguration());
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }

        if (isJump2Img) {
            isJump2Img = false;
        } else {
            //audioCommon.stopReceive(); 退到后台继续接收音频
        }

        if (audioCommon.isMICWork() && audioCommon.isRecording()) {
            stopedOnPause = true;
            //audioCommon.stopSend();
        } else
            stopedOnPause = false;
    }


    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        if (breceiver != null)
            unregisterReceiver(breceiver);
        if (audioManager != null)
            audioManager.setSpeakerphoneOn(false);
        if (!CallbackManager.IS_LEAVED) {
            audioCommon.destroyVoe();
        }
        //if (audioCommon != null)
        //    audioCommon.setHandler(null);
        //if (confCommon != null)
        //    confCommon.setHandler(null);

        audioCommon = null;
        confCommon = null;

        PublicWay.setConfActivity(null);

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
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
        ExitDialog exitDialog = new ExitDialog(this, ConferenceApplication.Screen_W * 4 / 5, new ExitDialog.OnResultListener() {
            @Override
            public void doYes() {
                exit();
            }

            @Override
            public void doNo() {

            }
        });
        exitDialog.show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        /*int width = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        int height = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);

        viewRoot.measure(width,height);

        int viewHeight =viewRoot.getMeasuredHeight();
        int viewWidth=viewRoot.getMeasuredWidth();
*/
        //int[] location = new int[2];
        //viewRoot.getLocationOnScreen(location);

        //if (location[0] >= 0)
         //   ConferenceApplication.LEFT_MARGIN = ConferenceApplication.

        //if (location[1] >= 0)
        //    ConferenceApplication.TOP_MARGIN = location[1];

        curOriInfo = newConfig.orientation;

        if (curOriInfo == Configuration.ORIENTATION_LANDSCAPE)
        {
            ConferenceApplication.LEFT_MARGIN = ConferenceApplication.Screen_H - width - ConferenceApplication.NavigationBar_H;
            ConferenceApplication.TOP_MARGIN = ConferenceApplication.StateBar_H;
        }
        else if (curOriInfo == Configuration.ORIENTATION_PORTRAIT)
        {
            ConferenceApplication.LEFT_MARGIN = 0;
            ConferenceApplication.TOP_MARGIN = ConferenceApplication.StateBar_H;
        }

        Log.d("InfowareLab.Debug", "onConfigurationChanged: >>>Margin =" + ConferenceApplication.LEFT_MARGIN + "," + ConferenceApplication.TOP_MARGIN);

        if (popIndex5 != null && popIndex5.isShowing()) {
            popIndex5.dismiss();
        }
//        llBottomBar.setVisibility(View.VISIBLE);
//        rlTopBar.setVisibility(View.VISIBLE);

        showBarsWithAnimation();

        setFrag4changeBars(true);
        restartHideBarTimer();

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

    public void exit() {
        if (isShare) {
            this.closeFloatWindow();
            screenCapturePresenter.stopCapture();
            ((ShareDtCommonImpl) CommonFactory.getInstance().getSdCommon()).stopDesktopShare();
            ((ShareDtCommonImpl) CommonFactory.getInstance().getSdCommon()).setScreenInfo(0, 0, 0);
            isShare = false;
        }
        LocalCommonFactory.getInstance().getContactDataCommon().getMap().clear();
        if (fragDs != null) fragDs.removePointing();
        confHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                CommonFactory.getInstance().getConferenceCommon().leaveConference();
            }
        }, 600);
        waitVideo4Exit();
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
        if (fragAs != null && fragAs.isAdded() && !fragAs.isHidden() && !fragAs.isEmpty()) {
            ToastUtil.showMessage(mContext, "正在桌面共享，请稍后再试", 5 * 1000);
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
        //Log.d("InfowareLab.Debug", "ShareScreen: videoContentByte::" + content.length);
        if (isMerge) {
            byte[] data1 = Utils.byteMerger(sps, pps);
            data = Utils.byteMerger(data1, content);
            ((ShareDtCommonImpl) CommonFactory.getInstance().getSdCommon()).sendScreenData(screenCapturePresenter.getWidth(),
                    screenCapturePresenter.getHeight(), 24, data, data.length, true, true);
            isMerge = false;
            sps = null;
            pps = null;
        } else {
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
                if (data[4] == 0x61) {
                    data[4] = 0x41;
                }
                if (nalUnitType == 7) { //IDR frame
                    data[4] = (byte) (data[4] | (3 << 5));

                    //Log.d("InfowareLab.Debug", "ShareScreen: sendScreenData(I):" + screenCapturePresenter.getWidth() + "x" + screenCapturePresenter.getHeight());
                    //Log.d("InfowareLab.Debug", "ShareScreen: sendScreenData(I):" + data.length);

                    ((ShareDtCommonImpl) CommonFactory.getInstance().getSdCommon()).sendScreenData(screenCapturePresenter.getWidth(),
                            screenCapturePresenter.getHeight(), 24, data, data.length, true, true);
                } else {

                    //Log.d("InfowareLab.Debug", "ShareScreen: sendScreenData(P):" + screenCapturePresenter.getWidth() + "x" + screenCapturePresenter.getHeight());
                    //Log.d("InfowareLab.Debug", "ShareScreen: sendScreenData(P):" + data.length);
                    ((ShareDtCommonImpl) CommonFactory.getInstance().getSdCommon()).sendScreenData(screenCapturePresenter.getWidth(),
                            screenCapturePresenter.getHeight(), 24, data, data.length, true, false);
                }
            }
        }
        if (isWriteFile) {
            try {
                _out.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
        ((ShareDtCommonImpl) CommonFactory.getInstance().getSdCommon()).startDesktopShare(help.getFPS(), 20);
        Log.d("InfowareLab.Debug", "screenshare FPS::" + help.getFPS());
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
        if (userCommon.isHost()) {
            audioCommon.onOpenAudioConfirm(true);
        }
        changeRecordByRoleModify();
    }

    @Override
    protected void onRestart() {
        fragVideo.initVideoHandler();
        super.onRestart();
        llBottomBar.post(new Runnable() {
            @Override
            public void run() {
                fragVideo.restartApp();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        isHaveStoped = true;
        ActHome.position = position;
    }

    public void getPrivilege() {
        getAudioPrivilege();
    }

    private void getAudioPrivilege() {
        //log.info("getAudioPrivilege = " + audioCommon.isMICWork());
        if (audioCommon.isMICWork()) {
            ivIndex1.setImageResource(R.drawable.ic_index_1_enable_off);
            tvIndex1.setTextColor(getResources().getColor(R.color.index_red));
            if (((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).getSelf().getRole() == UserCommonImpl.ROLE_HOST) {
                Log.i("UserCommonImpl", "SDKImpl-UserCommonImpl-getRole=1");
                tryStartSend();
            }
        }
    }

    private void initAudioFilter() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (!audioManager.isWiredHeadsetOn() && !audioManager.isBluetoothScoOn() && !audioManager.isBluetoothA2dpOn()) {
            state = 0;
        } else {
            state = 1;
        }
    }

    /**
     * 初始化音频引擎，打开声音
     */
    private void initAudio() {
        //audioCommon.createVoe(this);
        audioCommon.createVoe(getApplicationContext());
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
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
                llBottomBar.setVisibility(View.VISIBLE);
            } else {
                llBottomBar.setVisibility(View.GONE);
            }
        } else if (request == ConferenceCommon.TRANSCHANNEL) {
            if (userCommon.isHost()) {
                String msg = (String) details;
                if (msg.equals("ACTION_USER_ADD")) {
                    if (confCommon.getLastRecvData().equals("")) {
                        fragVideo.doTransChannel();
                    } else {
                        doByReq(ConferenceCommon.TRANSCHANNEL, confCommon.getLastRecvData());
                    }
                } else {
                    confCommon.transparentSendUserData((String) details);
                }
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
        if (Build.VERSION.SDK_INT < 26) {
            filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
            filter.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);
        }

        registerReceiver(breceiver, filter);
    }


    private boolean isBlueConnected() {
        if (Build.VERSION.SDK_INT >= 18) {
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
        llBottomBar.setVisibility(View.VISIBLE);
        tvConfFinish.setVisibility(View.VISIBLE);
    }

    @Override
    protected void hideBottomBar() {
        llBottomBar.setVisibility(View.GONE);
        if (curOriInfo == Configuration.ORIENTATION_PORTRAIT) {
            tvConfFinish.setVisibility(View.INVISIBLE);
        } else {
            tvConfFinish.setVisibility(View.GONE);
        }
    }

    protected void hideBottomBarButFinish() {
        llBottomBar.setVisibility(View.GONE);
        if (curOriInfo == Configuration.ORIENTATION_PORTRAIT) {
            tvConfFinish.setVisibility(View.VISIBLE);
        } else {
            tvConfFinish.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCallParentView(String action, Object obj) {
        switch (action) {
            case BaseFragment.ACTION_CHANGEAUDIOSTATE:

//                if (isShare || isHaveStoped || getWindow().getDecorView().getVisibility() != View.VISIBLE) break;
//
//                if ((int)obj > 0 && !audioCommon.isRecording() && audioCommon.isClickUsed) {
//                    Log.d("InfowareLab.Debug", ">>>>>>switch audio sate: " + (int)obj);
//                    audioCommon.isClickUsed = false;
//                    audioCommon.startSend();
//                    showShortToast(R.string.audioon);
//                }
//                else if ((int)obj <= 0 && audioCommon.isRecording() && audioCommon.isClickUsed){
//                    Log.d("InfowareLab.Debug", ">>>>>>switch audio sate(2): " + (int)obj);
//                    audioCommon.isClickUsed = false;
//                    audioCommon.stopSend();
//                    showShortToast(R.string.audiooff);
//                }

                break;
            case BaseFragment.ACTION_REFRESHCAMERA:
                if (curPage == 2) {
                    if (!fragVideo.isLocalVideoOpened()) {
                        btnCameraRotate.setVisibility(View.GONE);
                        ivIndex2.setImageResource(R.drawable.ic_index_2_sel_off);
                    } else {
                        btnCameraRotate.setVisibility(View.VISIBLE);
                        ivIndex2.setImageResource(R.drawable.ic_index_2_sel);
                    }
                } else {
                    if (!fragVideo.isLocalVideoOpened()) {
                        //btnCameraRotate.setVisibility(View.GONE);
                        ivIndex2.setImageResource(R.drawable.ic_index_2_nor_off);
                    } else {
                        //btnCameraRotate.setVisibility(View.VISIBLE);
                        ivIndex2.setImageResource(R.drawable.ic_index_2_nor);
                    }
                }
                break;
            case BaseFragment.ACTION_ASSTATE:
                if (curPage == 3) {
                    if (!fragAs.isEmpty()) {
                        vpCtrl.setAllowScroll(true);
                        if (vpCtrl.getCurrentItem() == 1) {
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
                //tvReturnVideo.setVisibility(View.GONE);
                btnCameraRotate.setVisibility(View.GONE);
                break;
            case BaseFragment.ACTION_SHARE2DS:
                tvTitle.setText(getResources().getString(R.string.share_title));
                //tvReturnVideo.setVisibility(View.GONE);
                btnCameraRotate.setVisibility(View.GONE);

//                if (((DocCommonImpl) CommonFactory.getInstance().getDocCommon()).getPrivateShareDocPriviledge() || ((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).isHost()) {
//
//                    if (confCommon != null) {
//                        confCommon.setLayout(ConferenceCommon.COMPONENT_TYPE_DS, ConferenceCommon.RT_CONF_UI_TYPE_LEFT_TOP_VIDEO);
//                        Log.d("InfowareLab.Debug", ">>>>>>setLayout: ConferenceCommon.COMPONENT_TYPE_DS");
//                    }
//
//                    ivDsAdd.setVisibility(View.VISIBLE);
//                } else {
//                    ivDsAdd.setVisibility(View.GONE);
//                }

                ivDsAdd.setVisibility(View.GONE);
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
                    String msg = (String) obj;
                    if (msg.equals("ACTION_USER_ADD")) {
                        if (confCommon.getLastRecvData().equals("")) {
                            fragVideo.doTransChannel();
                        } else {
                            doByReq(ConferenceCommon.TRANSCHANNEL, confCommon.getLastRecvData());
                        }
                    } else {
                        confCommon.transparentSendUserData(msg);
                    }
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
//                if (!ConferenceApplication.isVideoMeeting) {
//                    ivVsAdd.setVisibility(View.GONE);
//                } else if (((VideoCommonImpl) CommonFactory.getInstance().getVideoCommon()).isVideoPreviewPriviledge() || ((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).isHost()) {
//                    ivVsAdd.setVisibility(View.VISIBLE);
//                } else {
//                    ivVsAdd.setVisibility(View.GONE);
//                }
                ivVsAdd.setVisibility(View.GONE);
                break;
            case BaseFragment.ACTION_PRIVILEDGE_DS:
//                if (curPage == 3 && vpCtrl.getCurrentItem() == 1) {
//                    if (((DocCommonImpl) CommonFactory.getInstance().getDocCommon()).getPrivateShareDocPriviledge() || ((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).isHost()) {
//                        ivDsAdd.setVisibility(View.VISIBLE);
//                    } else {
//                        ivDsAdd.setVisibility(View.GONE);
//                    }
//                }
                ivDsAdd.setVisibility(View.GONE);
                break;
            case BaseFragment.ACTION_PRIVILEDGE_ATT:
                if (curPage != 4) return;
                if (((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).isHost()) {
                    ivAttAdd.setVisibility(View.VISIBLE);
                } else {
                    ivAttAdd.setVisibility(View.GONE);
                }
                break;

            default:
                break;
        }
    }

    private void restartHideBarTimer(){

        if (getOrientationState() == Configuration.ORIENTATION_PORTRAIT) {
            if (curPage != 2)
            {
                if (task != null){
                    task.cancel();
                    task = null;
                }

                if (timer != null) {
                    timer.purge();
                    timer.cancel();
                    timer = null;
                }

                return;
            }
        }
        else if (getOrientationState() == Configuration.ORIENTATION_LANDSCAPE) {
            if (curPage != 2 && curPage != 3)
            {
                if (task != null){
                    task.cancel();
                    task = null;
                }

                if (timer != null) {
                    timer.purge();
                    timer.cancel();
                    timer = null;
                }

                return;
            }
        }

        if (task != null){
            task.cancel();
            task = null;
        }

        if (timer != null) {
            timer.purge();
            timer.cancel();
            timer = null;
        }

        if (task == null) {
            task = new TimerTask() {
                @Override
                public void run() {

                    if (getOrientationState() == Configuration.ORIENTATION_PORTRAIT) {
                        if (curPage != 2)
                        {
                            if (task != null){
                                task.cancel();
                                task = null;
                            }

                            if (timer != null) {
                                timer.purge();
                                timer.cancel();
                                timer = null;
                            }

                            return;
                        }
                    }
                    else if (getOrientationState() == Configuration.ORIENTATION_LANDSCAPE) {
                        if (curPage != 2 && curPage != 3)
                        {
                            if (task != null){
                                task.cancel();
                                task = null;
                            }

                            if (timer != null) {
                                timer.purge();
                                timer.cancel();
                                timer = null;
                            }

                            return;
                        }
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideBarsWithAnimation();
                        }
                    });

                    task = null;
                    timer.purge();
                    timer.cancel();
                    timer = null;
                }
            };
        }

        timer = new Timer();
        timer.schedule(task, 5 * 1000);
    }

    private void hideBarsWithAnimation() {

        if (animationTop != null && animationTop.isRunning())
            animationTop.stop();

        if (animationBottom != null && animationBottom.isRunning())
            animationBottom.stop();

        animationBottom = YoYo.with(Techniques.SlideOutDown)
                .duration(animationDuration)
                .repeat(0)
                .pivot(YoYo.CENTER_PIVOT, YoYo.CENTER_PIVOT)
                .interpolate(new AccelerateDecelerateInterpolator())
                .withListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if ((getOrientationState() == Configuration.ORIENTATION_PORTRAIT && curPage != 2) || (getOrientationState() == Configuration.ORIENTATION_LANDSCAPE && curPage != 2 && curPage != 3)) {
                            animationBottom = YoYo.with(Techniques.SlideInUp)
                                    .duration(animationDuration)
                                    .repeat(0)
                                    .pivot(YoYo.CENTER_PIVOT, YoYo.CENTER_PIVOT)
                                    .interpolate(new AccelerateDecelerateInterpolator())
                                    .withListener(new Animator.AnimatorListener() {
                                        @Override
                                        public void onAnimationStart(Animator animation) {
                                            llBottomBar.setVisibility(View.VISIBLE);
                                        }

                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            restartHideBarTimer();
                                        }

                                        @Override
                                        public void onAnimationCancel(Animator animation) {
                                            //Toast.makeText(MyActivity.this, "canceled previous animation", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onAnimationRepeat(Animator animation) {

                                        }
                                    })
                                    .playOn(llBottomBar);
                            Log.d(TAG, "showBars @ llBottomBar: onAnimationEnd");
                        } else {
                            llBottomBar.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        //Toast.makeText(MyActivity.this, "canceled previous animation", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .playOn(llBottomBar);

        animationTop = YoYo.with(Techniques.SlideOutUp)
                .duration(animationDuration)
                .repeat(0)
                .pivot(YoYo.CENTER_PIVOT, YoYo.CENTER_PIVOT)
                .interpolate(new AccelerateDecelerateInterpolator())
                .withListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if ((getOrientationState() == Configuration.ORIENTATION_PORTRAIT && curPage != 2) || (getOrientationState() == Configuration.ORIENTATION_LANDSCAPE && curPage != 2 && curPage != 3)) {
                            animationTop = YoYo.with(Techniques.SlideInDown)
                                    .duration(animationDuration)
                                    .repeat(0)
                                    .pivot(YoYo.CENTER_PIVOT, YoYo.CENTER_PIVOT)
                                    .interpolate(new AccelerateDecelerateInterpolator())
                                    .withListener(new Animator.AnimatorListener() {
                                        @Override
                                        public void onAnimationStart(Animator animation) {
                                            rlTopBar.setVisibility(View.VISIBLE);
                                        }

                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            restartHideBarTimer();
                                        }

                                        @Override
                                        public void onAnimationCancel(Animator animation) {
                                            //Toast.makeText(MyActivity.this, "canceled previous animation", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onAnimationRepeat(Animator animation) {

                                        }
                                    })
                                    .playOn(rlTopBar);
                            Log.d(TAG, "showBars @ rlTopBar: onAnimationEnd");
                        }
                        else
                            rlTopBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        //Toast.makeText(MyActivity.this, "canceled previous animation", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .playOn(rlTopBar);
    }

    private void showBarsWithAnimation() {

        if (animationTop != null && animationTop.isRunning())
            animationTop.stop();

        if (animationBottom != null && animationBottom.isRunning())
            animationBottom.stop();

        animationBottom = YoYo.with(Techniques.SlideInUp)
                .duration(animationDuration)
                .repeat(0)
                .pivot(YoYo.CENTER_PIVOT, YoYo.CENTER_PIVOT)
                .interpolate(new AccelerateDecelerateInterpolator())
                .withListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        llBottomBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        restartHideBarTimer();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        //Toast.makeText(MyActivity.this, "canceled previous animation", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .playOn(llBottomBar);

        animationTop = YoYo.with(Techniques.SlideInDown)
                .duration(animationDuration)
                .repeat(0)
                .pivot(YoYo.CENTER_PIVOT, YoYo.CENTER_PIVOT)
                .interpolate(new AccelerateDecelerateInterpolator())
                .withListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        rlTopBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        restartHideBarTimer();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        //Toast.makeText(MyActivity.this, "canceled previous animation", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .playOn(rlTopBar);
    }

    private void changeBars() {

        if (getOrientationState() == Configuration.ORIENTATION_PORTRAIT) {
            if (curPage != 2)
                return;
        }
        else if (getOrientationState() == Configuration.ORIENTATION_LANDSCAPE){
            if (curPage != 2 && curPage != 3)
                return;
        }

        if (rlTopBar.getVisibility() == View.VISIBLE) {

            hideBarsWithAnimation();
//            rlTopBar.setVisibility(View.GONE);
//            llBottomBar.setVisibility(View.GONE);
            setFrag4changeBars(false);

            if (task != null){
                task.cancel();
                task = null;
            }

            if (timer != null) {
                timer.purge();
                timer.cancel();
                timer = null;
            }

        } else {
//            rlTopBar.setVisibility(View.VISIBLE);
//            llBottomBar.setVisibility(View.VISIBLE);

            showBarsWithAnimation();
            setFrag4changeBars(true);

            restartHideBarTimer();
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

    private void hideBars() {
        rlTopBar.setVisibility(View.GONE);
        llBottomBar.setVisibility(View.GONE);
    }

    private void showBars() {

        if (rlTopBar.getVisibility() == View.GONE || llBottomBar.getVisibility() == View.GONE)
            showBarsWithAnimation();

        //rlTopBar.setVisibility(View.VISIBLE);
        //llBottomBar.setVisibility(View.VISIBLE);

        restartHideBarTimer();
    }

    private void showBottom() {
        if (rlTopBar.getVisibility() == View.VISIBLE) {
            llBottomBar.setVisibility(View.VISIBLE);
        } else {
            llBottomBar.setVisibility(View.GONE);
        }
    }

    private void hideBottom() {
        llBottomBar.setVisibility(View.GONE);
    }


    private boolean keepRecording = false;
    private Thread timerThread;

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
            while (keepRecording) {
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
            if (msg.what == -1) {
                llRecordTime.setVisibility(View.GONE);
            } else {
                llRecordTime.setVisibility(llRecordTime.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            }
        }
    };

    private void setRecordingShow() {
        if (confCommon != null && confCommon.isCloudRecording()) {
            if ((curPage == 2 && fragVideo.getCurPage() == 1) || curPage == 3) {
                startTiming(2);
            } else {
                stopTiming(2);
            }
        }
    }

    int uid = 0;

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void Event(MessageEvent messageEvent) {
        switch (messageEvent.getType()) {
            case 0:
                bAS = messageEvent.isbAS();
                if (fragDs != null) {
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
                break;
            //公告
            case 2:
                break;
            //手否是第一次进会
            case 3:

                break;
            //主讲模式
            case 4:

                VideoCommonImpl videoCommon =((VideoCommonImpl) CommonFactory.getInstance().getVideoCommon());

                if (messageEvent.getCurSpeakerId() > 0) {

                    //主讲模式
                    videoCommon.setCurLayoutMode(VideoCommon.LayoutMode.MODE_SPEAKER);

                    if (fragVideo != null) {
                        fragVideo.setSpeak(messageEvent.getCurSpeakerId());
                        FileUtil.saveSharedPreferences(this, Constants.SHARED_PREFERENCES,"curSpeakerId", -1);
                    } else {

                        FileUtil.saveSharedPreferences(this, Constants.SHARED_PREFERENCES,"curSpeakerId", messageEvent.getCurSpeakerId());
                    }

                } else if (messageEvent.getCurSpeakerId() == -1) {
                    //语音激励模式
                    //取第一个元素
                    videoCommon.setCurLayoutMode(VideoCommon.LayoutMode.MODE_VOICE);
                    for (Integer key : videoCommon.getSyncMap().keySet()) {
                        if (0 == uid) {
                            uid = videoCommon.getSyncMap().get(key);
                            setViewHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                   if (fragVideo != null) fragVideo.setMaxVoice(uid);
                                }
                            }, 1500);
                            return;
                        }
                    }
                } else {
                    if (fragVideo != null) fragVideo.retCamera();
                }
                break;
            //轮循的路数
            case 5:
                int loopNum = messageEvent.getLoopNum();
                break;
            case 7:
            // onUserViewState: layout type
            //layout type changed
                if (messageEvent.getLayType() == 3) { // WB data view
                    if (curPage != 3 && !userCommon.isHost())
                    {
                        Log.d("InfowareLab.Debug", "Event() => onUserViewState: switchFragment to share view");
                        switchFragment(3);
                    }
                }
                else if (messageEvent.getLayType() == 8) { // video view
                    if (curPage != 2 && !userCommon.isHost())
                    {
                        Log.d("InfowareLab.Debug", "Event() => onUserViewState: switchFragment to video view");
                        switchFragment(2);
                    }
                }
                break;
            default:
                break;
        }
    }
}
