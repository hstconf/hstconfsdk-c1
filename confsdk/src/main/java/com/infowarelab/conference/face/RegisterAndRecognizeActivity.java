package com.infowarelab.conference.face;

import android.Manifest;
import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;

import android.os.FileUtils;
import android.text.Editable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arcsoft.face.ActiveFileInfo;
import com.arcsoft.face.VersionInfo;
import com.arcsoft.face.enums.RuntimeABI;
import com.infowarelab.conference.ConferenceApplication;
import com.infowarelab.hongshantongphone.R;
import com.infowarelab.conference.animation.Techniques;
import com.infowarelab.conference.animation.YoYo;
import com.infowarelab.conference.face.common.Constants;
import com.infowarelab.conference.face.faceserver.CompareResult;
import com.infowarelab.conference.face.faceserver.FaceServer;
import com.infowarelab.conference.face.model.DrawInfo;
import com.infowarelab.conference.face.model.FacePreviewInfo;
import com.infowarelab.conference.face.util.ConfigUtil;
import com.infowarelab.conference.face.util.DrawHelper;
import com.infowarelab.conference.face.util.camera.CameraHelper;
import com.infowarelab.conference.face.util.camera.CameraListener;
import com.infowarelab.conference.face.util.face.FaceHelper;
import com.infowarelab.conference.face.util.face.FaceListener;
import com.infowarelab.conference.face.util.face.LivenessType;
import com.infowarelab.conference.face.util.face.RecognizeColor;
import com.infowarelab.conference.face.util.face.RequestFeatureStatus;
import com.infowarelab.conference.face.util.face.RequestLivenessStatus;
import com.infowarelab.conference.face.widget.FaceRectView;
import com.infowarelab.conference.face.widget.FaceSearchResultAdapter;
import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;
import com.infowarelab.conference.face.widget.SignInListAdapter;
import com.infowarelab.conference.utils.SharedPreferencesUrls;
import com.infowarelab.conference.utils.XMLUtils;
import com.infowarelabsdk.conference.util.ToastUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import me.jessyan.autosize.internal.CustomAdapt;

public class RegisterAndRecognizeActivity extends BaseActivity implements ViewTreeObserver.OnGlobalLayoutListener, CustomAdapt {
    private static final String TAG = "InfowareLab.Face";
    private static final int MAX_DETECT_NUM = 10;
    private static final int MIN_DETECT_SIZE = 32;

    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    // 在线激活所需的权限
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.READ_PHONE_STATE
    };
    boolean libraryExists = true;
    // Demo 所需的动态库文件
    private static final String[] LIBRARIES = new String[]{
            // 人脸相关
            "libarcsoft_face_engine.so",
            "libarcsoft_face.so",
            // 图像库相关
            "libarcsoft_image_util.so",
    };


    /**
     * 当FR成功，活体未成功时，FR等待活体的时间
     */
    private static final int WAIT_LIVENESS_INTERVAL = 100;
    /**
     * 失败重试间隔时间（ms）
     */
    private static final long FAIL_RETRY_INTERVAL = 1000;
    /**
     * 出错重试最大次数
     */
    private static final int MAX_RETRY_TIME = 3;

    private CameraHelper cameraHelper;
    private DrawHelper drawHelper;
    private Camera.Size previewSize;
    /**
     * 优先打开的摄像头，本界面主要用于单目RGB摄像头设备，因此默认打开前置
     */
    private Integer rgbCameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;

    /**
     * VIDEO模式人脸检测引擎，用于预览帧人脸追踪
     */
    private FaceEngine ftEngine;
    /**
     * 用于特征提取的引擎
     */
    private FaceEngine frEngine;
    /**
     * IMAGE模式活体检测引擎，用于预览帧人脸活体检测
     */
    private FaceEngine flEngine;

    private int ftInitCode = -1;
    private int frInitCode = -1;
    private int flInitCode = -1;
    private FaceHelper faceHelper;
    private List<CompareResult> compareResultList;
    private FaceSearchResultAdapter adapter;
    private SignInListAdapter mSignInListAdapter;
    /**
     * 活体检测的开关
     */
    private boolean livenessDetect = true;
    /**
     * 注册人脸状态码，准备注册
     */
    private static final int REGISTER_STATUS_READY = 0;
    /**
     * 注册人脸状态码，注册中
     */
    private static final int REGISTER_STATUS_PROCESSING = 1;
    /**
     * 注册人脸状态码，注册结束（无论成功失败）
     */
    private static final int REGISTER_STATUS_DONE = 2;

    private int registerStatus = REGISTER_STATUS_DONE;
    /**
     * 用于记录人脸识别相关状态
     */
    private ConcurrentHashMap<Integer, Integer> requestFeatureStatusMap = new ConcurrentHashMap<>();
    /**
     * 用于记录人脸特征提取出错重试次数
     */
    private ConcurrentHashMap<Integer, Integer> extractErrorRetryMap = new ConcurrentHashMap<>();
    /**
     * 用于存储活体值
     */
    private ConcurrentHashMap<Integer, Integer> livenessMap = new ConcurrentHashMap<>();
    /**
     * 用于存储活体检测出错重试次数
     */
    private ConcurrentHashMap<Integer, Integer> livenessErrorRetryMap = new ConcurrentHashMap<>();

    private CompositeDisposable getFeatureDelayedDisposables = new CompositeDisposable();
    private CompositeDisposable delayFaceTaskCompositeDisposable = new CompositeDisposable();
    /**
     * 相机预览显示的控件，可为SurfaceView或TextureView
     */
    private View previewView;
    /**
     * 绘制人脸框的控件
     */
    private FaceRectView faceRectView;

    private Switch switchLivenessDetect;

    /**
     * 识别阈值
     */
    private static final float SIMILAR_THRESHOLD = 0.8F;
    /**
     * 所需的所有权限信息
     */

    /**
     * 0:从上往下 1:从下往上
     */
    Animation mTop2Bottom, mBottom2Top;

    private ImageView mIvScan;
    private TextView mTvSignInTitle;
    private TextView mTvSignInMessage;
    private TextView mTvSignInListTitle;
    private EditText mEtName;
    private Button mBtnConfirm;
    private Button mBtnDelete;
    private String mStrName = "";
    private LinearLayout mLLSignInMessage;
    private LinearLayout mLLSignInList;
    private int mMode = 0;

    private final int mVideoWidth = 1280;
    private final int mVideoHeight = 720;
    private final int mViewFinderWidth = 747;
    private final int mViewFinderHeight = 747;

    private final int mRightBarWidth = 371;

    private final int mCenterX = 791;
    private final int mCenterY = 614;

    private YoYo.YoYoString animationMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_and_recognize);

//        libraryExists = checkSoFile(LIBRARIES);
//        ApplicationInfo applicationInfo = getApplicationInfo();
//        Log.i(TAG, "onCreate: " + applicationInfo.nativeLibraryDir);
//        if (!libraryExists) {
//            showToast(getString(R.string.library_not_found));
//        }else {
//            VersionInfo versionInfo = new VersionInfo();
//            int code = FaceEngine.getVersion(versionInfo);
//            Log.i(TAG, "onCreate: getVersion, code is: " + code + ", versionInfo is: " + versionInfo);
//        }

        ConfigUtil.setDefaultFtOrient(DetectFaceOrientPriority.ASF_OP_0_ONLY);

        activeEngine(null);

        //保持亮屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams attributes = getWindow().getAttributes();
            attributes.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            getWindow().setAttributes(attributes);
        }

        // Activity启动后就锁定为启动时的方向
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        //本地人脸库初始化
        FaceServer.getInstance().init(this);

        initView();

        mIvScan = findViewById(R.id.scan_line);

        mTop2Bottom = new TranslateAnimation(TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.ABSOLUTE, 0f, TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0.7f);

        mBottom2Top = new TranslateAnimation(TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.ABSOLUTE, 0f, TranslateAnimation.RELATIVE_TO_PARENT, 0.7f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0f);

        mBottom2Top.setRepeatMode(Animation.RESTART);
        mBottom2Top.setInterpolator(new LinearInterpolator());
        mBottom2Top.setDuration(1500);
        mBottom2Top.setFillEnabled(true);//使其可以填充效果从而不回到原地
        mBottom2Top.setFillAfter(true);//不回到起始位置
        //如果不添加setFillEnabled和setFillAfter则动画执行结束后会自动回到远点
        mBottom2Top.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mIvScan.startAnimation(mTop2Bottom);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mTop2Bottom.setRepeatMode(Animation.RESTART);
        mTop2Bottom.setInterpolator(new LinearInterpolator());
        mTop2Bottom.setDuration(1500);
        mTop2Bottom.setFillEnabled(true);
        mTop2Bottom.setFillAfter(true);
        mTop2Bottom.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mIvScan.startAnimation(mBottom2Top);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mIvScan.startAnimation(mTop2Bottom);
    }

    /**
     * 检查能否找到动态链接库，如果找不到，请修改工程配置
     *
     * @param libraries 需要的动态链接库
     * @return 动态库是否存在
     */
    private boolean checkSoFile(String[] libraries) {
        File dir = new File(getApplicationInfo().nativeLibraryDir);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return false;
        }
        List<String> libraryNameList = new ArrayList<>();
        for (File file : files) {
            libraryNameList.add(file.getName());
        }
        boolean exists = true;
        for (String library : libraries) {
            exists &= libraryNameList.contains(library);
        }
        return exists;
    }

    /**
     * 激活引擎
     *
     * @param view
     */
    public void activeEngine(final View view) {

        if (SharedPreferencesUrls.getInstance().getBoolean("activated", false))
        {
            Log.d(TAG, "activeEngine: already actived and ignored.");
            //InfowareLab.Face
            return;
        }

        if (!libraryExists) {
            showToast(getString(R.string.library_not_found));
            return;
        }
        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
            return;
        }
//        if (view != null) {
//            view.setClickable(false);
//        }

        Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
            //RuntimeABI runtimeABI = FaceEngine.getRuntimeABI();
            //Log.i(TAG, "subscribe: getRuntimeABI() " + runtimeABI);

            //long start = System.currentTimeMillis();
            int activeCode = FaceEngine.activeOnline(RegisterAndRecognizeActivity.this, Constants.APP_ID, Constants.SDK_KEY);
            //Log.i(TAG, "subscribe cost: " + (System.currentTimeMillis() - start));
            emitter.onNext(activeCode);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer activeCode) {
                        if (activeCode == ErrorInfo.MOK) {
                            Log.d(TAG, getString(R.string.active_success));
                            SharedPreferencesUrls.getInstance().putBoolean("activated", true);
                        } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                            Log.d(TAG, getString(R.string.already_activated));
                            SharedPreferencesUrls.getInstance().putBoolean("activated", true);
                        } else {
                            showToast(getString(R.string.active_failed, activeCode));
                            Log.d(TAG, getString(R.string.active_failed, activeCode));
                        }

                        if (view != null) {
                            view.setClickable(true);
                        }
                        ActiveFileInfo activeFileInfo = new ActiveFileInfo();
                        int res = FaceEngine.getActiveFileInfo(RegisterAndRecognizeActivity.this, activeFileInfo);
                        if (res == ErrorInfo.MOK) {
                            Log.i(TAG, activeFileInfo.toString());
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        showToast(e.getMessage());
                        if (view != null) {
                            view.setClickable(true);
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    protected void hideInput(View v) {
        InputMethodManager inputMethodManager = (InputMethodManager) getApplicationContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != getCurrentFocus()&&null != getCurrentFocus().getApplicationWindowToken()) {
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus()
                            .getApplicationWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
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
        ConferenceApplication.X_DPI = dm.xdpi;
        ConferenceApplication.Y_DPI = dm.ydpi;

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
                + " XDPI = " + ConferenceApplication.X_DPI
                + " YDPI = " + ConferenceApplication.Y_DPI
                + " Top_H=" + ConferenceApplication.Top_H
                + " Bottom_H=" + ConferenceApplication.Bottom_H);

        Log.d("InfowareLab.Debug", "density = " + ConferenceApplication.DENSITY + ", widthPixels = " + ConferenceApplication.SCREEN_WIDTH
                + ", heightPixels = " + ConferenceApplication.SCREEN_HEIGHT);
    }

    private void initView() {

        getScreenWidthHeight();

        previewView = findViewById(R.id.single_camera_texture_preview);

//        float ratioX = ConferenceApplication.SCREEN_WIDTH / 1920.0f;
//        float ratioY = ConferenceApplication.SCREEN_HEIGHT / 1200.0f;
//
//        int left = (int) ((mCenterX - mVideoWidth/2) * ratioX);
//        int right = (int) ((ConferenceApplication.SCREEN_WIDTH - mCenterX - mVideoWidth/2) * ratioX);
//        int top = (int) ((mCenterY - mVideoHeight/2) * ratioY);
//        int bottom = (int) ((ConferenceApplication.SCREEN_HEIGHT - mCenterY - mVideoHeight/2) * ratioY);
//
//        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) previewView.getLayoutParams();
//        params.setMargins(left, top, right, bottom);
//        params.width = mVideoWidth;
//        params.height = mVideoHeight;
//        previewView.setLayoutParams(params);

        //在布局结束后才做初始化操作
        previewView.getViewTreeObserver().addOnGlobalLayoutListener(this);

        faceRectView = findViewById(R.id.single_camera_face_rect_view);
        //faceRectView.setLayoutParams(params);

        switchLivenessDetect = findViewById(R.id.single_camera_switch_liveness_detect);
        switchLivenessDetect.setChecked(livenessDetect);
        switchLivenessDetect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                livenessDetect = isChecked;
            }
        });
        RecyclerView recyclerShowFaceInfo = findViewById(R.id.single_camera_recycler_view_person);
        compareResultList = new ArrayList<>();
        adapter = new FaceSearchResultAdapter(compareResultList, this);
        recyclerShowFaceInfo.setAdapter(adapter);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int spanCount = (int) (dm.widthPixels / (getResources().getDisplayMetrics().density * 100 + 0.5f));
        recyclerShowFaceInfo.setLayoutManager(new GridLayoutManager(this, spanCount));
        recyclerShowFaceInfo.setItemAnimator(new DefaultItemAnimator());

        mSignInListAdapter = new SignInListAdapter(getBaseContext());

        ListView signInList = findViewById(R.id.sign_in_listview);
        signInList.setAdapter(mSignInListAdapter);

        mTvSignInTitle = findViewById(R.id.sign_in_title);
        mEtName = findViewById(R.id.et_name);
        mBtnConfirm = findViewById(R.id.btn_confirm);
        mBtnDelete = findViewById(R.id.btn_delete);
        mTvSignInMessage = findViewById(R.id.sign_in_message);
        mTvSignInListTitle = findViewById(R.id.sign_in_list_title);
        mLLSignInMessage = findViewById(R.id.ll_sign_in_message);
        mLLSignInList  = findViewById(R.id.ll_sign_in_listview);

        Intent intent = getIntent();
        if (intent != null){
            mMode = intent.getIntExtra("mode", 0);

            if (mMode == 1){
                mTvSignInTitle.setText(R.string.face_register_title);
                //signInList.setVisibility(View.GONE);
                //mTvSignInListTitle.setVisibility(View.GONE);
                mLLSignInMessage.setVisibility(View.GONE);
                mLLSignInList.setVisibility(View.GONE);
                mEtName.setVisibility(View.VISIBLE);
                mBtnDelete.setVisibility(View.VISIBLE);
                mBtnConfirm.setVisibility(View.VISIBLE);

                //mEtName.setFocusable(true);
                //mEtName.requestFocus();

                //hideInput(mEtName);

                //mEtName.setFocusable(true);
                //mEtName.requestFocus();
                //mBtnConfirm.requestFocus();

            }
            else
            {
                mTvSignInTitle.setText(R.string.face_sign_in_title);
                //signInList.setVisibility(View.VISIBLE);
                //mTvSignInListTitle.setVisibility(View.VISIBLE);
                mLLSignInList.setVisibility(View.VISIBLE);
                mLLSignInMessage.setVisibility(View.GONE);
                mEtName.setVisibility(View.GONE);
                mBtnDelete.setVisibility(View.GONE);
                mBtnConfirm.setVisibility(View.GONE);

                mTvSignInListTitle.setText(String.format(getResources().getString(R.string.face_sign_in_list_title), String.valueOf(mSignInListAdapter.getCount())));
            }
        }

    }

    /**
     * 初始化引擎
     */
    private void initEngine() {
        ftEngine = new FaceEngine();
        ftInitCode = ftEngine.init(this, DetectMode.ASF_DETECT_MODE_VIDEO, ConfigUtil.getFtOrient(this),
                MIN_DETECT_SIZE, MAX_DETECT_NUM, FaceEngine.ASF_FACE_DETECT);

        frEngine = new FaceEngine();
        frInitCode = frEngine.init(this, DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_0_ONLY,
                MIN_DETECT_SIZE, MAX_DETECT_NUM, FaceEngine.ASF_FACE_RECOGNITION);

        flEngine = new FaceEngine();
        flInitCode = flEngine.init(this, DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_0_ONLY,
                MIN_DETECT_SIZE, MAX_DETECT_NUM, FaceEngine.ASF_LIVENESS);

        Log.i(TAG, "initEngine:  init: " + ftInitCode);

        if (ftInitCode != ErrorInfo.MOK) {
            String error = getString(R.string.specific_engine_init_failed, "ftEngine", ftInitCode);
            Log.i(TAG, "initEngine: " + error);
            showToast(error);
        }
        if (frInitCode != ErrorInfo.MOK) {
            String error = getString(R.string.specific_engine_init_failed, "frEngine", frInitCode);
            Log.i(TAG, "initEngine: " + error);
            showToast(error);
        }
        if (flInitCode != ErrorInfo.MOK) {
            String error = getString(R.string.specific_engine_init_failed, "flEngine", flInitCode);
            Log.i(TAG, "initEngine: " + error);
            showToast(error);
        }
    }

    /**
     * 销毁引擎，faceHelper中可能会有特征提取耗时操作仍在执行，加锁防止crash
     */
    private void unInitEngine() {
        if (ftInitCode == ErrorInfo.MOK && ftEngine != null) {
            synchronized (ftEngine) {
                int ftUnInitCode = ftEngine.unInit();
                Log.i(TAG, "unInitEngine: " + ftUnInitCode);
            }
        }
        if (frInitCode == ErrorInfo.MOK && frEngine != null) {
            synchronized (frEngine) {
                int frUnInitCode = frEngine.unInit();
                Log.i(TAG, "unInitEngine: " + frUnInitCode);
            }
        }
        if (flInitCode == ErrorInfo.MOK && flEngine != null) {
            synchronized (flEngine) {
                int flUnInitCode = flEngine.unInit();
                Log.i(TAG, "unInitEngine: " + flUnInitCode);
            }
        }
    }

    @Override
    public void onBackPressed() {
        setVisible(false);
        finish();
    }

    @Override
    protected void onDestroy() {

        if (cameraHelper != null) {
            cameraHelper.release();
            cameraHelper = null;
        }

        unInitEngine();
        if (getFeatureDelayedDisposables != null) {
            getFeatureDelayedDisposables.clear();
        }
        if (delayFaceTaskCompositeDisposable != null) {
            delayFaceTaskCompositeDisposable.clear();
        }
        if (faceHelper != null) {
            ConfigUtil.setTrackedFaceCount(this, faceHelper.getTrackedFaceCount());
            faceHelper.release();
            faceHelper = null;
        }

        FaceServer.getInstance().unInit();
        super.onDestroy();
    }

    /**
     * 求两个点之间的距离
     *
     * @return
     */
    public static double getDisForTwoSpot(float x1, float y1, float x2, float y2) {
        float width, height;
        if (x1 > x2) {
            width = x1 - x2;
        } else {
            width = x2 - x1;
        }

        if (y1 > y2) {
            height = y2 - y1;
        } else {
            height = y2 - y1;
        }
        return Math.sqrt((width * width) + (height * height));
    }

    private void initCamera() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        final FaceListener faceListener = new FaceListener() {
            @Override
            public void onFail(Exception e) {
                Log.e(TAG, "onFail: " + e.getMessage());
            }

            //请求FR的回调
            @Override
            public void onFaceFeatureInfoGet(@Nullable final FaceFeature faceFeature, final Integer requestId, final Integer errorCode) {

                if (mMode == 1) return;

                //FR成功
                if (faceFeature != null) {
//                    Log.i(TAG, "onPreview: fr end = " + System.currentTimeMillis() + " trackId = " + requestId);
                    Integer liveness = livenessMap.get(requestId);
                    //不做活体检测的情况，直接搜索
                    if (!livenessDetect) {
                        searchFace(faceFeature, requestId);
                    }
                    //活体检测通过，搜索特征
                    else if (liveness != null && liveness == LivenessInfo.ALIVE) {
                        searchFace(faceFeature, requestId);
                    }
                    //活体检测未出结果，或者非活体，延迟执行该函数
                    else {
                        if (requestFeatureStatusMap.containsKey(requestId)) {
                            Observable.timer(WAIT_LIVENESS_INTERVAL, TimeUnit.MILLISECONDS)
                                    .subscribe(new Observer<Long>() {
                                        Disposable disposable;

                                        @Override
                                        public void onSubscribe(Disposable d) {
                                            disposable = d;
                                            getFeatureDelayedDisposables.add(disposable);
                                        }

                                        @Override
                                        public void onNext(Long aLong) {
                                            onFaceFeatureInfoGet(faceFeature, requestId, errorCode);
                                        }

                                        @Override
                                        public void onError(Throwable e) {

                                        }

                                        @Override
                                        public void onComplete() {
                                            getFeatureDelayedDisposables.remove(disposable);
                                        }
                                    });
                        }
                    }

                }
                //特征提取失败
                else {
                    if (increaseAndGetValue(extractErrorRetryMap, requestId) > MAX_RETRY_TIME) {
                        extractErrorRetryMap.put(requestId, 0);

                        String msg;
                        // 传入的FaceInfo在指定的图像上无法解析人脸，此处使用的是RGB人脸数据，一般是人脸模糊
                        if (errorCode != null && errorCode == ErrorInfo.MERR_FSDK_FACEFEATURE_LOW_CONFIDENCE_LEVEL) {
                            msg = getString(R.string.low_confidence_level);
                        } else {
                            msg = "ExtractCode:" + errorCode;
                        }
                        faceHelper.setName(requestId, getString(R.string.recognize_failed_notice, msg));
                        // 在尝试最大次数后，特征提取仍然失败，则认为识别未通过
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                        retryRecognizeDelayed(requestId);
                    } else {
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.TO_RETRY);
                    }
                }
            }

            @Override
            public void onFaceLivenessInfoGet(@Nullable LivenessInfo livenessInfo, final Integer requestId, Integer errorCode) {
                if (livenessInfo != null) {
                    int liveness = livenessInfo.getLiveness();
                    livenessMap.put(requestId, liveness);
                    // 非活体，重试
                    if (liveness == LivenessInfo.NOT_ALIVE) {
                        //faceHelper.setName(requestId, getString(R.string.recognize_failed_notice, "NOT_ALIVE"));
                        // 延迟 FAIL_RETRY_INTERVAL 后，将该人脸状态置为UNKNOWN，帧回调处理时会重新进行活体检测
                        retryLivenessDetectDelayed(requestId);
                    }
                } else {
                    if (increaseAndGetValue(livenessErrorRetryMap, requestId) > MAX_RETRY_TIME) {
                        livenessErrorRetryMap.put(requestId, 0);
                        String msg;
                        // 传入的FaceInfo在指定的图像上无法解析人脸，此处使用的是RGB人脸数据，一般是人脸模糊
                        if (errorCode != null && errorCode == ErrorInfo.MERR_FSDK_FACEFEATURE_LOW_CONFIDENCE_LEVEL) {
                            msg = getString(R.string.low_confidence_level);
                        } else {
                            msg = "ProcessCode:" + errorCode;
                        }
                        faceHelper.setName(requestId, getString(R.string.recognize_failed_notice, msg));
                        retryLivenessDetectDelayed(requestId);
                    } else {
                        livenessMap.put(requestId, LivenessInfo.UNKNOWN);
                    }
                }
            }


        };


        CameraListener cameraListener = new CameraListener() {
            @Override
            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
                Camera.Size lastPreviewSize = previewSize;
                previewSize = camera.getParameters().getPreviewSize();
                drawHelper = new DrawHelper(previewSize.width, previewSize.height, previewView.getWidth(), previewView.getHeight(), displayOrientation
                        , cameraId, isMirror, false, false);

                if (Build.MODEL.indexOf("HDX2") != -1){
                    drawHelper.setMirrorHorizontal(true);
                    //ConfigUtil.setDefaultFtOrient(DetectFaceOrientPriority.ASF_OP_ALL_OUT);
                }

                Log.d(TAG, "onCameraOpened: " + drawHelper.toString());
                // 切换相机的时候可能会导致预览尺寸发生变化
                if (faceHelper == null ||
                        lastPreviewSize == null ||
                        lastPreviewSize.width != previewSize.width || lastPreviewSize.height != previewSize.height) {
                    Integer trackedFaceCount = null;
                    // 记录切换时的人脸序号
                    if (faceHelper != null) {
                        trackedFaceCount = faceHelper.getTrackedFaceCount();
                        faceHelper.release();
                    }

                    Rect detectRect = new Rect((previewSize.width-mViewFinderWidth)/2,
                            (previewSize.height-mViewFinderHeight)/2,
                            (previewSize.width+mViewFinderWidth)/2,
                            (previewSize.height+mViewFinderHeight)/2);

                    faceHelper = new FaceHelper.Builder()
                            .ftEngine(ftEngine)
                            .frEngine(frEngine)
                            .flEngine(flEngine)
                            .frQueueSize(MAX_DETECT_NUM)
                            .flQueueSize(MAX_DETECT_NUM)
                            .previewSize(previewSize)
                            .detectArea(detectRect)
                            .faceListener(faceListener)
                            .trackedFaceCount(trackedFaceCount == null ? ConfigUtil.getTrackedFaceCount(RegisterAndRecognizeActivity.this.getApplicationContext()) : trackedFaceCount)
                            .build();
                }
            }


            @Override
            public void onPreview(final byte[] nv21, Camera camera) {

                if (faceRectView != null) {
                    faceRectView.clearFaceInfo();
                }

                //获得检测到的人脸列表（最大的人脸）
                List<FacePreviewInfo> facePreviewInfoList = faceHelper.onPreviewFrame(nv21);

                if (facePreviewInfoList != null && faceRectView != null && drawHelper != null) {

                    drawPreviewInfo(facePreviewInfoList);

                    if (facePreviewInfoList.size() > 0)
                    {
                        FacePreviewInfo facePreviewInfo = facePreviewInfoList.get(0);
                        double distance = getDisForTwoSpot(mVideoWidth/2.0f, mVideoHeight/2.0f,
                                facePreviewInfo.getFaceInfo().getRect().centerX(),
                                facePreviewInfo.getFaceInfo().getRect().centerY());

                        if (distance > mViewFinderWidth/2.0f)
                        {
                            runOnUiThread(() -> {

                                mTvSignInMessage.setText(R.string.face_sign_in_message_too_far);

                                if (mLLSignInMessage.getVisibility() != View.VISIBLE)
                                    mLLSignInMessage.setVisibility(View.VISIBLE);

                            });

                            //Log.d(TAG, "face is too far: distance=" + distance );
                        }
                        else if (mTvSignInMessage.getText().toString().equalsIgnoreCase(getResources().getString(R.string.face_sign_in_message_too_far))){
                            mTvSignInMessage.setText("");

                            if (mLLSignInMessage.getVisibility() == View.VISIBLE)
                                mLLSignInMessage.setVisibility(View.GONE);
                        }
                    }
                }

                registerFace(nv21, facePreviewInfoList);
                clearLeftFace(facePreviewInfoList);

                if (facePreviewInfoList != null && facePreviewInfoList.size() > 0 && previewSize != null) {
                    for (int i = 0; i < facePreviewInfoList.size(); i++) {
                        Integer status = requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId());
                        /**
                         * 在活体检测开启，在人脸识别状态不为成功或人脸活体状态不为处理中（ANALYZING）且不为处理完成（ALIVE、NOT_ALIVE）时重新进行活体检测
                         */
                        if (livenessDetect && (status == null || status != RequestFeatureStatus.SUCCEED)) {
                            Integer liveness = livenessMap.get(facePreviewInfoList.get(i).getTrackId());
                            if (liveness == null
                                    || (liveness != LivenessInfo.ALIVE && liveness != LivenessInfo.NOT_ALIVE && liveness != RequestLivenessStatus.ANALYZING)) {
                                livenessMap.put(facePreviewInfoList.get(i).getTrackId(), RequestLivenessStatus.ANALYZING);
                                faceHelper.requestFaceLiveness(nv21, facePreviewInfoList.get(i).getFaceInfo(), previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, facePreviewInfoList.get(i).getTrackId(), LivenessType.RGB);
                            }
                        }
                        /**
                         * 对于每个人脸，若状态为空或者为失败，则请求特征提取（可根据需要添加其他判断以限制特征提取次数），
                         * 特征提取回传的人脸特征结果在{@link FaceListener#onFaceFeatureInfoGet(FaceFeature, Integer, Integer)}中回传
                         */
                        if (status == null
                                || status == RequestFeatureStatus.TO_RETRY) {
                            requestFeatureStatusMap.put(facePreviewInfoList.get(i).getTrackId(), RequestFeatureStatus.SEARCHING);
                            faceHelper.requestFaceFeature(nv21, facePreviewInfoList.get(i).getFaceInfo(), previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, facePreviewInfoList.get(i).getTrackId());
//                            Log.i(TAG, "onPreview: fr start = " + System.currentTimeMillis() + " trackId = " + facePreviewInfoList.get(i).getTrackedFaceCount());
                        }
                        else if (status == RequestFeatureStatus.SUCCEED)
                        {
                            String name = faceHelper.getName(facePreviewInfoList.get(i).getTrackId());

                            if (name != null && name.length() > 0) {
                                runOnUiThread(() -> {

                                    //Log.d(TAG, ">>>>>> Sign In Already(2)!");

                                    String oldMessage = String.format(getResources().getString(R.string.face_sign_in_message), name);

                                    if (mTvSignInMessage.getText().toString().equalsIgnoreCase(oldMessage))
                                        return;

                                    mTvSignInMessage.setText(String.format(getResources().getString(R.string.face_sign_in_message_2), name));

                                    if (mLLSignInMessage.getVisibility() != View.VISIBLE)
                                        mLLSignInMessage.setVisibility(View.VISIBLE);
                                });
                            }

                        }
                    }
                }
            }

            @Override
            public void onCameraClosed() {
                Log.i(TAG, "onCameraClosed: ");
            }

            @Override
            public void onCameraError(Exception e) {
                Log.i(TAG, "onCameraError: " + e.getMessage());
            }

            @Override
            public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {
                if (drawHelper != null) {
                    drawHelper.setCameraDisplayOrientation(displayOrientation);
                }
                Log.i(TAG, "onCameraConfigurationChanged: " + cameraID + "  " + displayOrientation);
            }
        };

        cameraHelper = new CameraHelper.Builder()
                .previewViewSize(new Point(previewView.getMeasuredWidth(), previewView.getMeasuredHeight()))
                .rotation(getWindowManager().getDefaultDisplay().getRotation())
                .specificCameraId(rgbCameraID != null ? rgbCameraID : Camera.CameraInfo.CAMERA_FACING_FRONT)
                .isMirror(false)
                .previewOn(previewView)
                .cameraListener(cameraListener)
                .previewSize(new Point(mVideoWidth, mVideoHeight))
                .build();
        cameraHelper.init();
        cameraHelper.start();
    }

    private void registerFace(final byte[] nv21, final List<FacePreviewInfo> facePreviewInfoList) {

        //Log.d(TAG, "registerFace: " + facePreviewInfoList.size());

        if (mMode != 1) return;

        if (registerStatus == REGISTER_STATUS_READY && facePreviewInfoList != null && facePreviewInfoList.size() > 0) {

            registerStatus = REGISTER_STATUS_PROCESSING;

            Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {

                boolean success = FaceServer.getInstance().registerNv21(RegisterAndRecognizeActivity.this, nv21.clone(), previewSize.width, previewSize.height,
                        facePreviewInfoList.get(0).getFaceInfo(), mStrName);

                emitter.onNext(success);
            })
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Boolean>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(Boolean success) {
                            String result = success ? mStrName + "人脸录入成功!" : mStrName + "人脸录入失败!";
                            showToast(result);
                            registerStatus = REGISTER_STATUS_DONE;
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            showToast(mStrName + "人脸录入失败!");
                            registerStatus = REGISTER_STATUS_DONE;
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }
    }

    private void drawPreviewInfo(List<FacePreviewInfo> facePreviewInfoList) {
        List<DrawInfo> drawInfoList = new ArrayList<>();
        for (int i = 0; i < facePreviewInfoList.size(); i++) {
            String name = faceHelper.getName(facePreviewInfoList.get(i).getTrackId());
            Integer liveness = livenessMap.get(facePreviewInfoList.get(i).getTrackId());
            Integer recognizeStatus = requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId());

            // 根据识别结果和活体结果设置颜色
            int color = RecognizeColor.COLOR_UNKNOWN;
            if (recognizeStatus != null) {
                if (recognizeStatus == RequestFeatureStatus.FAILED) {
                    color = RecognizeColor.COLOR_FAILED;
                }
                if (recognizeStatus == RequestFeatureStatus.SUCCEED) {
                    color = RecognizeColor.COLOR_SUCCESS;
                }
            }
            if (liveness != null && liveness == LivenessInfo.NOT_ALIVE) {
                color = RecognizeColor.COLOR_FAILED;
            }

            drawInfoList.add(new DrawInfo(drawHelper.adjustRect(facePreviewInfoList.get(i).getFaceInfo().getRect()),
                    GenderInfo.UNKNOWN, AgeInfo.UNKNOWN_AGE, liveness == null ? LivenessInfo.UNKNOWN : liveness, color,
                    name == null ? /*String.valueOf(facePreviewInfoList.get(i).getTrackId())*/"" : name));
        }
        drawHelper.draw(faceRectView, drawInfoList);
    }

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {
        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
            if (isAllGranted) {
                initEngine();
                initCamera();
            } else {
                showToast(getString(R.string.permission_denied));
            }
        }
    }

    /**
     * 删除已经离开的人脸
     *
     * @param facePreviewInfoList 人脸和trackId列表
     */
    private void clearLeftFace(List<FacePreviewInfo> facePreviewInfoList) {
        if (compareResultList != null) {
            for (int i = compareResultList.size() - 1; i >= 0; i--) {
                if (!requestFeatureStatusMap.containsKey(compareResultList.get(i).getTrackId())) {
                    compareResultList.remove(i);
                    adapter.notifyItemRemoved(i);
                }
            }
        }
        if (facePreviewInfoList == null || facePreviewInfoList.size() == 0) {
            requestFeatureStatusMap.clear();
            livenessMap.clear();
            livenessErrorRetryMap.clear();
            extractErrorRetryMap.clear();
            if (getFeatureDelayedDisposables != null) {
                getFeatureDelayedDisposables.clear();
            }
            return;
        }
        Enumeration<Integer> keys = requestFeatureStatusMap.keys();
        while (keys.hasMoreElements()) {
            int key = keys.nextElement();
            boolean contained = false;
            for (FacePreviewInfo facePreviewInfo : facePreviewInfoList) {
                if (facePreviewInfo.getTrackId() == key) {
                    contained = true;
                    break;
                }
            }
            if (!contained) {
                requestFeatureStatusMap.remove(key);
                livenessMap.remove(key);
                livenessErrorRetryMap.remove(key);
                extractErrorRetryMap.remove(key);
            }
        }


    }

    private void searchFace(final FaceFeature frFace, final Integer requestId) {

        //Log.d(TAG, "searchFace: " + requestId);
        Observable
                .create((ObservableOnSubscribe<CompareResult>) emitter -> {
//                        Log.i(TAG, "subscribe: fr search start = " + System.currentTimeMillis() + " trackId = " + requestId);
                    CompareResult compareResult = FaceServer.getInstance().getTopOfFaceLib(frFace);
//                        Log.i(TAG, "subscribe: fr search end = " + System.currentTimeMillis() + " trackId = " + requestId);
                    emitter.onNext(compareResult);

                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CompareResult>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(CompareResult compareResult) {
                        if (compareResult == null || compareResult.getUserName() == null) {
                            requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                            faceHelper.setName(requestId, "VISITOR " + requestId);
                            return;
                        }

//                        Log.i(TAG, "onNext: fr search get result  = " + System.currentTimeMillis() + " trackId = " + requestId + "  similar = " + compareResult.getSimilar());
                        if (compareResult.getSimilar() > SIMILAR_THRESHOLD) {
                            boolean isAdded = false;
                            if (compareResultList == null) {
                                requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                                faceHelper.setName(requestId, "VISITOR " + requestId);
                                return;
                            }
                            for (CompareResult compareResult1 : compareResultList) {
                                if (compareResult1.getTrackId() == requestId) {
                                    isAdded = true;
                                    break;
                                }
                            }
                            if (!isAdded) {
                                //对于多人脸搜索，假如最大显示数量为 MAX_DETECT_NUM 且有新的人脸进入，则以队列的形式移除
                                if (compareResultList.size() >= MAX_DETECT_NUM) {
                                    compareResultList.remove(0);
                                    adapter.notifyItemRemoved(0);
                                }
                                //添加显示人员时，保存其trackId
                                compareResult.setTrackId(requestId);
                                compareResultList.add(compareResult);
                                adapter.notifyItemInserted(compareResultList.size() - 1);
                            }

                            requestFeatureStatusMap.put(requestId, RequestFeatureStatus.SUCCEED);
                            faceHelper.setName(requestId, compareResult.getUserName());

                            if (compareResult.getSignInResult() == 0) {

                                //Log.d(TAG, ">>>>>> Sign In Success!");

                                FaceServer.getInstance().updateFaceRegisterInfoList(mSignInListAdapter.getFaceRegisterList());
                                mSignInListAdapter.update();
                                mTvSignInListTitle.setText(String.format(getResources().getString(R.string.face_sign_in_list_title), String.valueOf(mSignInListAdapter.getCount())));
                                mTvSignInMessage.setText(String.format(getResources().getString(R.string.face_sign_in_message), compareResult.getUserName()));

                                mLLSignInMessage.setVisibility(View.VISIBLE);

                                animationMessage = YoYo.with(Techniques.SlideInUp)
                                        .duration(500)
                                        .repeat(0)
                                        .pivot(YoYo.CENTER_PIVOT, YoYo.CENTER_PIVOT)
                                        .interpolate(new AccelerateDecelerateInterpolator())
                                        .withListener(new Animator.AnimatorListener() {
                                            @Override
                                            public void onAnimationStart(Animator animation) {

                                            }

                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                mLLSignInMessage.setVisibility(View.VISIBLE);
                                            }

                                            @Override
                                            public void onAnimationCancel(Animator animation) {
                                                //Toast.makeText(MyActivity.this, "canceled previous animation", Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onAnimationRepeat(Animator animation) {

                                            }
                                        })
                                        .playOn(mLLSignInMessage);

                            }
                            else if (compareResult.getSignInResult() == 1) {

                                //Log.d(TAG, ">>>>>> Sign In Already(1)!");

                                String oldMessage = String.format(getResources().getString(R.string.face_sign_in_message), compareResult.getUserName());

                                if (mTvSignInMessage.getText().toString().equalsIgnoreCase(oldMessage))
                                    return;

                                FaceServer.getInstance().updateFaceRegisterInfoList(mSignInListAdapter.getFaceRegisterList());
                                mSignInListAdapter.update();
                                mTvSignInListTitle.setText(String.format(getResources().getString(R.string.face_sign_in_list_title), String.valueOf(mSignInListAdapter.getCount())));

                                mTvSignInMessage.setText(String.format(getResources().getString(R.string.face_sign_in_message_2), compareResult.getUserName()));

                                if (mLLSignInMessage.getVisibility() != View.VISIBLE)
                                    mLLSignInMessage.setVisibility(View.VISIBLE);
                            }

                        } else {
                            faceHelper.setName(requestId, "未录入");
                            retryRecognizeDelayed(requestId);

                            mTvSignInMessage.setText(getResources().getString(R.string.face_sign_in_message_failed));

                            if (mLLSignInMessage.getVisibility() != View.VISIBLE)
                                mLLSignInMessage.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        faceHelper.setName(requestId, "未录入");
                        retryRecognizeDelayed(requestId);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    /**
     * 将准备注册的状态置为{@link #REGISTER_STATUS_READY}
     *
     * @param view 注册按钮
     */
    public void register(View view) {
        if (registerStatus == REGISTER_STATUS_DONE) {
            registerStatus = REGISTER_STATUS_READY;
        }
    }

    /**
     * 切换相机。注意：若切换相机发现检测不到人脸，则极有可能是检测角度导致的，需要销毁引擎重新创建或者在设置界面修改配置的检测角度
     *
     * @param view
     */
    public void switchCamera(View view) {
        if (cameraHelper != null) {
            boolean success = cameraHelper.switchCamera();
            if (!success) {
                showToast(getString(R.string.switch_camera_failed));
            } else {
                showLongToast(getString(R.string.notice_change_detect_degree));
            }
        }
    }

    /**
     * 在{@link #previewView}第一次布局完成后，去除该监听，并且进行引擎和相机的初始化
     */
    @Override
    public void onGlobalLayout() {
        previewView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
        } else {
            initEngine();
            initCamera();
        }
    }

    /**
     * 将map中key对应的value增1回传
     *
     * @param countMap map
     * @param key      key
     * @return 增1后的value
     */
    public int increaseAndGetValue(Map<Integer, Integer> countMap, int key) {
        if (countMap == null) {
            return 0;
        }
        Integer value = countMap.get(key);
        if (value == null) {
            value = 0;
        }
        countMap.put(key, ++value);
        return value;
    }

    /**
     * 延迟 FAIL_RETRY_INTERVAL 重新进行活体检测
     *
     * @param requestId 人脸ID
     */
    private void retryLivenessDetectDelayed(final Integer requestId) {
        Observable.timer(FAIL_RETRY_INTERVAL, TimeUnit.MILLISECONDS)
                .subscribe(new Observer<Long>() {
                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                        delayFaceTaskCompositeDisposable.add(disposable);
                    }

                    @Override
                    public void onNext(Long aLong) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        // 将该人脸状态置为UNKNOWN，帧回调处理时会重新进行活体检测
//                        if (livenessDetect) {
//                            faceHelper.setName(requestId, Integer.toString(requestId));
//                        }
                        livenessMap.put(requestId, LivenessInfo.UNKNOWN);
                        delayFaceTaskCompositeDisposable.remove(disposable);
                    }
                });
    }

    /**
     * 延迟 FAIL_RETRY_INTERVAL 重新进行人脸识别
     *
     * @param requestId 人脸ID
     */
    private void retryRecognizeDelayed(final Integer requestId) {
        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
        Observable.timer(FAIL_RETRY_INTERVAL, TimeUnit.MILLISECONDS)
                .subscribe(new Observer<Long>() {
                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                        delayFaceTaskCompositeDisposable.add(disposable);
                    }

                    @Override
                    public void onNext(Long aLong) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        // 将该人脸特征提取状态置为FAILED，帧回调处理时会重新进行活体检测
                        //faceHelper.setName(requestId, Integer.toString(requestId));
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.TO_RETRY);
                        delayFaceTaskCompositeDisposable.remove(disposable);
                    }
                });
    }

    public void onConfirm(View view) {

        mStrName = mEtName.getText().toString();

        if (mStrName.length() <= 0){
            ToastUtil.showMessage(this,"人脸名字不能为空",2000);
            return;
        }

        if (registerStatus == REGISTER_STATUS_DONE) {
            registerStatus = REGISTER_STATUS_READY;
        }
        else
            ToastUtil.showMessage(this,"录入操作进行进行中，请稍候重试。",2000);
    }

    public void onDeleteAll(View view) {

        FaceServer.getInstance().clearAllFaces(this);
        requestFeatureStatusMap.clear();

        ToastUtil.showMessage(this,"录入人脸数据全部清除。",2000);
    }

    public void onReturn(View view) {

        onBackPressed();
    }


    @Override
    public boolean isBaseOnWidth() {
        return true;
    }

    @Override
    public float getSizeInDp() {
        return 853;
    }
}
