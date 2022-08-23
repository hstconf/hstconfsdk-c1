package com.infowarelab.conference.ui.activity.preconf.fragment;


import java.util.LinkedList;
import java.util.List;

import com.infowarelab.conference.ui.action.JoinConfByIdAction4Frag;
import com.infowarelab.conference.ui.activity.inconf.ConferenceActivity;
import com.infowarelab.conference.ui.activity.preconf.view.BroadcastPagerListView;
import com.infowarelab.conference.ui.view.UnderlineButton;
import com.infowarelab.hongshantongphone.R;
import com.infowarelab.conference.ui.activity.preconf.BaseFragment;
import com.infowarelab.conference.ui.activity.preconf.BaseFragmentActivity;
import com.infowarelab.conference.ui.activity.preconf.view.ConferencePagerListView;
import com.infowarelab.conference.ui.activity.preconf.view.ConferencePagerNumber;
import com.infowarelab.conference.ui.adapter.ConferencePagerAdapter;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.ConferenceCommonImpl;
import com.infowarelabsdk.conference.confctrl.ConferenceCommon;
import com.infowarelabsdk.conference.domain.ConferenceBean;
import com.infowarelabsdk.conference.util.Constants;
import com.infowarelabsdk.conference.util.FileUtil;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.RequiresApi;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;

import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import me.jessyan.autosize.AutoSizeConfig;
import me.jessyan.autosize.internal.CustomAdapt;

public class FragJoin extends BaseFragment implements OnClickListener, CustomAdapt {
    private static final String TAG = "InfowareLab.Debug";
    private static final int id = 10001;
    private View joinView;
    private ViewPager mConferencePager;
    private List<View> views;
    private ConferencePagerAdapter mPagerAdapter;
    private ConferencePagerListView mConferencePagerListView;
    private BroadcastPagerListView  mBroadcastPagerListView;
    //private ConferencePagerNumber mConferencePagerNumber;

    private TextView tvHistory;
    private TextView tvFaceSignIn;

    private boolean isEnterFromItem = false;

    private CommonFactory commonFactory = CommonFactory.getInstance();
    private ConferenceCommonImpl conferenceCommon = (ConferenceCommonImpl) commonFactory.getConferenceCommon();
    private JoinConfByIdAction4Frag mAction;
    private Activity mActivity;
    private SharedPreferences preferences;

    protected final static int ENTERFROMITEM = 100;
    protected final static int BCASTFROMITEM = 103;

    public static final int INIT_SDK = 101;
    protected static final int INIT_SDK_FAILED = 102;
    protected static final int CONF_CONFLICT = -5;
    private int pre = 0;
    private final boolean isFromItem = false;
    private final float topBarRatioPortrait = 0.2933333f;
    private final float topBarRatioLandscape = 0.122886f;
    private final float screenRatio = 1.6f;

    public Handler handler = new Handler() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ConferenceCommon.RESULT_SUCCESS:
                    Log.d("InfowareLab.Debug", "FragJoin: Join success");
                    //昵称更改时保存，在按下进入会议后
    //				FileUtil.saveSharedPreferences(activity, Constants.SHARED_PREFERENCES,
    //						Constants.LOGIN_NICKNAME, etInput2.getText().toString());

                    if (mActivity == null) break;

                    Intent intent = new Intent(mActivity, ConferenceActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.putExtra("enterFromList", true);
                    mActivity.startActivity(intent);
                    //activity.overridePendingTransition(0, 0);
//				((AudioCommonImpl)commonFactory.getAudioCommon()).startAudioService();
                    //mActivity.finish();
                    mAction.missDislog();
                    //mActivity = null;
                    break;
                case ConferenceCommon.BEYOUNGMAXCOUNT:
                    Log.d("InfowareLab.Debug", "FragJoin: BEYOUNGMAXCOUNT");
                    Toast.makeText(mActivity, mActivity.getString(R.string.beyound_maxcount), Toast.LENGTH_LONG).show();
                    //showShortToast(mActivity.getString(R.string.beyound_maxcount));
                    mAction.missDislog();
                    break;
                case ConferenceCommon.BEYOUNGJIAMI:
                    Log.d("InfowareLab.Debug", "FragJoin: BEYOUNGMAXCOUNT");
                    Toast.makeText(mActivity, mActivity.getString(R.string.beyound_jiami), Toast.LENGTH_LONG).show();
                    //showShortToast(mActivity.getString(R.string.beyound_jiami));
                    mAction.missDislog();
                    break;
                case ENTERFROMITEM:
                    //checkResultIntent((ConferenceBean) msg.obj);
                    //checkJoinConf();
                    mAction.setConfBean((ConferenceBean) msg.obj);
                    mAction.startJoinConference();
                    break;
                case BCASTFROMITEM:
                    //checkResultIntent((ConferenceBean) msg.obj);
                    //checkJoinConf();
                    mAction.setConfBean((ConferenceBean) msg.obj);
                    mAction.startBroadcast();
                    break;
                case INIT_SDK:
                    Log.d("InfowareLab.Debug", "initSDK success");
                    break;
                case INIT_SDK_FAILED:
                    Log.d("InfowareLab.Debug", "FragJoin: INIT_SDK_FAILED");
                    Toast.makeText(mActivity, mActivity.getString(R.string.LoginFailed), Toast.LENGTH_LONG).show();
                    //showShortToast(mActivity.getString(R.string.initSDKFailed));
                    mAction.missDislog();
                    break;
                case CONF_CONFLICT:
                    Log.d("InfowareLab.Debug", "FragJoin: CONF_CONFLICT");
                    Toast.makeText(mActivity, mActivity.getString(R.string.confConflict), Toast.LENGTH_LONG).show();
                    //showShortToast(mActivity.getString(R.string.LoginFailed));
                    mAction.missDislog();
                    //activity.finish();
                    break;
                case ConferenceCommon.LEAVE:
                    Log.d("InfowareLab.Debug", "FragJoin: LEAVE");
                    break;
                default:
                    //Log.d("InfowareLab.Debug", "ConfPageNumber: Join Failed");
				/*ErrorMessage errorMessage = new ErrorMessage(activity);
				String message = errorMessage.getErrorMessageByCode(msg.what);
				Log.d("InfowareLab.Debug","false code = "+msg.what +" false = "+activity.getString(R.string.ConfSysErrCode_Conf_Area_Error));
                if(!message.equals(activity.getResources().getString(R.string.UnknowError)))
				Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
//				Intent intent = new Intent(ConferenceActivity.this, LoginActivity.class);
				mAction.cancelDialog();*/
                    break;
            }
        }

    };
    private LinearLayout llTopBar = null;
    private boolean landscape = false;
    private int screenWidth = 0;
    private int screenHeight = 0;
    private Button btnOneKeyConf;
    private Button btnCreateConf;
    private Button btnJoinConf;

    private UnderlineButton btnConfList;
    private UnderlineButton btnBCastList;


    public int getFragmentId() { return id; }
    public boolean isLandscape() { return landscape; }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        AutoSizeConfig.getInstance().setCustomFragment(true);

        mActivity = getActivity();

        initOrientation();

        // TODO Auto-generated method stub

        if (landscape)
            joinView = inflater.inflate(R.layout.a6_preconf_join_land, container, false);
        else
            joinView = inflater.inflate(R.layout.a6_preconf_join, container, false);

        initView();

        setHandler();

        mAction = new JoinConfByIdAction4Frag(mActivity, this, joinView);

        //btnJoin.setOnClickListener(mAction);

        return joinView;
    }

    public void setHandler() {

        if (handler != null && conferenceCommon != null) {
            if (conferenceCommon.getHandler() != handler) {
                Log.d("InfowareLab.Debug", "fragJoin: set pager handler!");
                conferenceCommon.setHandler(handler);
            }
        } else {
            Log.d("InfowareLab.Debug", "fragJoin: handler is null!!!");
        }
    }

    public Handler getHandler() {
        return handler;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v.getId() == R.id.btn_one_key_conf){
            doSelect(4);
        }else if (v.getId() == R.id.btn_create_conf){
            doSelect(1);
        }else if (v.getId() == R.id.btn_join_conf){
            doSelect(3);
        }else if (v.getId() == R.id.btn_conf_list){
            switchPage(0);
        }else if (v.getId() == R.id.btn_bcast_list){
            switchPage(1);
        }else if (v.getId() == R.id.tv_history){
            doSelect(5);
        }else if (v.getId() == R.id.tv_face_sign_in){
            doSelect(6);
        }
    }

    private void initView() {

        mConferencePager = joinView.findViewById(R.id.frag_join_vp);
        llTopBar = joinView.findViewById(R.id.ll_top_bar);
        btnOneKeyConf = joinView.findViewById(R.id.btn_one_key_conf);
        btnCreateConf = joinView.findViewById(R.id.btn_create_conf);
        btnJoinConf = joinView.findViewById(R.id.btn_join_conf);

        btnConfList = joinView.findViewById(R.id.btn_conf_list);
        btnBCastList = joinView.findViewById(R.id.btn_bcast_list);

        btnConfList.setSelected(true);
        btnBCastList.setSelected(false);

        btnConfList.setOnClickListener(this);
        btnBCastList.setOnClickListener(this);

        if (!landscape) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) llTopBar.getLayoutParams();
            params.width = screenWidth;
            params.height = (int) (params.width * topBarRatioPortrait);
            llTopBar.setLayoutParams(params);
        }
        else
        {
//            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) llTopBar.getLayoutParams();
//            params.width = screenHeight;
//            params.height = (int) (params.width * topBarRatioLandscape);
//            llTopBar.setLayoutParams(params);
        }

        btnOneKeyConf.setOnClickListener(this);
        btnCreateConf.setOnClickListener(this);
        btnJoinConf.setOnClickListener(this);

        tvHistory = joinView.findViewById(R.id.tv_history);
        tvHistory.setOnClickListener(this);

        tvFaceSignIn = joinView.findViewById(R.id.tv_face_sign_in);
        tvFaceSignIn.setOnClickListener(this);


        initPageView();
    }

    private int getOrientationState() {
        Configuration mConfiguration = this.getResources().getConfiguration();
        return mConfiguration.orientation;
    }

    private void initOrientation() {

        DisplayMetrics dm = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mActivity.getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        }else{
            mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        }

        int orientation = getOrientationState();

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            screenWidth = dm.widthPixels;
            screenHeight = dm.heightPixels;
        }
        else
        {
            screenWidth = dm.heightPixels;
            screenHeight = dm.widthPixels;
        }

        if (screenHeight/screenWidth > screenRatio){
            //竖屏
            if (getActivity().getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

            landscape = false;
        }
        else
        {
            if (getActivity().getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }

            landscape = true;
        }
    }

    private void initPageView() {
        mConferencePagerListView = new ConferencePagerListView(getActivity(), this);
        //mConferencePagerNumber = new ConferencePagerNumber((BaseFragmentActivity) getActivity(), this);
        mBroadcastPagerListView = new BroadcastPagerListView(getActivity(), this);

        views = new LinkedList<View>();

        views.add(mConferencePagerListView.getNewView());
        //views.add(mConferencePagerNumber.getNewView());
        views.add(mBroadcastPagerListView.getNewView());

        mPagerAdapter = new ConferencePagerAdapter(views);
        mConferencePager.setAdapter(mPagerAdapter);
        mConferencePager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int postion) {

                checkPosition(postion);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                // TODO Auto-generated method stub
            }
        });

//        CommonFactory commonFactory = CommonFactory.getInstance();
//        ConferenceCommonImpl conferenceCommon = (ConferenceCommonImpl) commonFactory.getConferenceCommon();
//        if (null != conferenceCommon) {
//            if (null != conferenceCommon.mConfId) {
//                switchPage(1);
//            }
//        }

    }

    public void switchPage(int position) {
//		if(index == 1){
//			mConferencePagerNumber.resumeLayout();
//		}
        mConferencePager.setCurrentItem(position);

        if (position == 0){
            tvHistory.setText(getResources().getString(R.string.socialize_14));
            tvHistory.setVisibility(View.VISIBLE);
        }
        else {
            //tvHistory.setText(getResources().getString(R.string.socialize_15));
            tvHistory.setVisibility(View.GONE);
        }

    }

    public void enterFromItem(ConferenceBean conferenceBean) {

        if (conferenceBean == null) return;

        isEnterFromItem = true;

        Log.d(TAG, "FragJoin: enterFromItem:" + conferenceBean.getId());

        //mConferencePager.setCurrentItem(1); //上次改错了

        Message msg = handler.obtainMessage(ENTERFROMITEM, conferenceBean);
        handler.sendMessage(msg);
    }

    public void bcastFromItem(ConferenceBean conferenceBean) {

        if (conferenceBean == null) return;

        isEnterFromItem = true;

        Log.d(TAG, "FragJoin: bcastFromItem:" + conferenceBean.getId());

        //mConferencePager.setCurrentItem(1); //上次改错了

        Message msg = handler.obtainMessage(BCASTFROMITEM, conferenceBean);
        handler.sendMessage(msg);
    }

    public ConferenceBean getConfBean(String condId) {
        CommonFactory commonFactory = CommonFactory.getInstance();
        ConferenceCommonImpl conferenceCommon = (ConferenceCommonImpl) commonFactory.getConferenceCommon();
        if (null != conferenceCommon) {
            return conferenceCommon.getConfById(condId);
        }
        return null;
        //return mConferencePagerListView.getConferenceById(condId);
    }

    public void setJoin(int position) {
        mConferencePagerListView.setJoin(position);
    }

    public void setBcast(int position) {
        mBroadcastPagerListView.setBCast(position);
    }

    private void checkPosition(int postion) {
        if (postion == 0) {
            if (getActivity().getCurrentFocus() != null) {
                //切换到会议列表时总是关闭输入法
//				InputMethodManager inputMethodManager = (InputMethodManager) 
//						getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//				inputMethodManager.hideSoftInputFromWindow(
//						getActivity().getCurrentFocus().getWindowToken(),
//				InputMethodManager.HIDE_NOT_ALWAYS);
            }
            mConferencePagerListView.refreshAdapter();
            btnConfList.setSelected(true);
            btnBCastList.setSelected(false);

            btnConfList.setTextColor(getResources().getColor(R.color.app_main_hue));
            btnBCastList.setTextColor(getResources().getColor(R.color.grey));

            btnConfList.invalidate();
            btnBCastList.invalidate();

            tvHistory.setText(getResources().getString(R.string.socialize_14));
            tvHistory.setVisibility(View.VISIBLE);

        } else if (postion == 1) {
            //mConferencePagerNumber.resumeLayout();
            mBroadcastPagerListView.refreshAdapter();

            btnConfList.setSelected(false);
            btnBCastList.setSelected(true);

            btnConfList.setTextColor(getResources().getColor(R.color.grey));
            btnBCastList.setTextColor(getResources().getColor(R.color.app_main_hue));

            btnConfList.invalidate();
            btnBCastList.invalidate();

            tvHistory.setVisibility(View.GONE);
            //tvHistory.setText(getResources().getString(R.string.socialize_15));
        }
        //doSelect(postion);
    }

    public ConferencePagerListView getmConferencePagerListView() {
        return mConferencePagerListView;
    }

//    public void onMyBackPressed() {
//        if (mConferencePager.getCurrentItem() == 1 && isEnterFromItem) {
//            mConferencePager.setCurrentItem(0);
//            isEnterFromItem = false;
//            return;
//        }
//    }

    public int getCurrentItem() {
        return mConferencePager.getCurrentItem();
    }

    public void setCurrentItem(int postion) {
        mConferencePager.setCurrentItem(postion);
    }

    public boolean isEnterFromItem() {
        return isEnterFromItem;
    }

    public void setEnterFromItem(boolean isEnterFromItem) {
        this.isEnterFromItem = isEnterFromItem;
    }

    public void refreshConfList() {
        if (mConferencePagerListView != null) mConferencePagerListView.getConfListData();
    }

    private void doSelect(int selectId) {
        if (switchPageListener != null) {
            switchPageListener.doSelect(selectId);
        }
    }

    public void showErrMsgNumber(int id) {
//        if (mConferencePagerNumber != null) {
//            mConferencePagerNumber.showErrMsg(id);
//        }

        if (id < 0) return;

        Toast.makeText(mActivity, mActivity.getString(id), Toast.LENGTH_LONG).show();
    }

    public void showErrMsgNumber(String s) {
//        if (mConferencePagerNumber != null) {
//            mConferencePagerNumber.showErrMsg(s);
//        }

        Toast.makeText(mActivity, s, Toast.LENGTH_LONG).show();
    }

    public void showLoading() {
        super.showLoading();
    }

    public void hideLoading() {
        super.hideLoading();
    }

    public void hideInput(View v) {
        super.hideInput(v);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        // TODO Auto-generated method stub
        super.onHiddenChanged(hidden);
        if (!hidden) {
            refreshConfList();
            //if (mConferencePagerListView != null) mConferencePagerListView.refreshAdapter();
            //if (mConferencePagerNumber != null) mConferencePagerNumber.resumeLayout();
        }
    }

    @Override
    public boolean isBaseOnWidth() {
        return true;
    }

    @Override
    public float getSizeInDp() {
        return 853.33f;
    }

//    public boolean isFromItem() {
//        if (mConferencePagerNumber != null) {
//            return mConferencePagerNumber.isFromItem();
//        } else {
//            return false;
//        }
//    }
}
