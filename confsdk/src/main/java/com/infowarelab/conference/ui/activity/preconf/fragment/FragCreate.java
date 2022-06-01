package com.infowarelab.conference.ui.activity.preconf.fragment;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectChangeListener;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.infowarelab.conference.ConferenceApplication;
import com.infowarelab.conference.localDataCommon.impl.ContactDataCommonImpl;
import com.infowarelab.conference.ui.activity.inconf.ConferenceActivity;
import com.infowarelab.conference.ui.activity.preconf.ActOrganization;
import com.infowarelab.conference.ui.activity.preconf.BaseFragment;
import com.infowarelab.conference.ui.activity.preconf.LoginActivity;
import com.infowarelab.conference.ui.view.PopSpinnerDuration;
import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.AudioCommonImpl;
import com.infowarelabsdk.conference.common.impl.ConferenceCommonImpl;
import com.infowarelabsdk.conference.confctrl.ConferenceCommon;
import com.infowarelabsdk.conference.domain.ConferenceBean;
import com.infowarelabsdk.conference.domain.LoginBean;
import com.infowarelabsdk.conference.transfer.Config;
import com.infowarelabsdk.conference.util.Constants;
import com.infowarelabsdk.conference.util.FileUtil;
import com.infowarelabsdk.conference.util.StringUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FragCreate extends BaseFragment implements OnClickListener, OnFocusChangeListener, RadioGroup.OnCheckedChangeListener, CompoundButton.OnCheckedChangeListener {
    private View createView;

    private static final int id = 10002;

    private LinearLayout ll1, ll2;
    private EditText et1;
    private Button btnCreate, btnOrg;
    private TextView tvErrMsg;

    private TimePickerView pvTime;

    //	private View view;
//	private Dialog createDialog;
    private View focusView;
    private AlertDialog.Builder alertDialog;

    private Intent createIntent;
    public SharedPreferences preferences;
    private CommonFactory commonFactory = CommonFactory.getInstance();
    private ConferenceCommonImpl conferenceCommon = (ConferenceCommonImpl) commonFactory.getConferenceCommon();
    private Config config;
    private final ConferenceBean conferenceBean = new ConferenceBean();
    private LoginBean login;

    private String confId;
    private String result = "";
    public String topic = "";
    public String password = "";

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
    protected static final int AUTO_START = 200;

    public Handler createHandler;

    private String attIds = "";
    private String attNames = "";
    private String attEmails = "";

    private int duration = 120;
    private boolean landscape = false;
    private int screenWidth = 0;
    private int screenHeight = 0;
    private Activity mActivity;
    private final float screenRatio = 1.6f;
    private TextView tvStartTime;
    private TextView tvEndTime;
    private ImageButton btnReturnList;
    private RadioGroup rgConfTime;
    private LinearLayout llStartTime;
    private LinearLayout llEndTime;
    private View vStartTime;
    private View vEndTime;

    private RadioButton rbInstantConf;
    private RadioButton rbScheduleConf;

    Date startDate;
    Date endDate;
    private boolean startTimeSetting = false;
    private boolean hidden;

    public int getFragmentId() { return id; }

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
            //竖屏i
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

    private String getTime(Date date) {//可根据需要自行截取数据显示
        Log.d("getTime()", "choice date millis: " + date.getTime());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return format.format(date);
    }

    private void initTimePicker() {//Dialog 模式下，在底部弹出
        pvTime = new TimePickerBuilder(mActivity, new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                Log.d("InfowareLab.Debug", "onTimeSelect: " + date.toString());

                if (startTimeSetting) {
                    startDate = date;
                    tvStartTime.setText(getTime(date));

                    if (startDate.after(endDate)){

                        Calendar cal = Calendar.getInstance();
                        cal.setTime(startDate);
                        cal.add(Calendar.HOUR, 2);//24小时制

                        endDate = cal.getTime();
                        tvEndTime.setText(getTime(endDate));
                    }
                }
                else {
                    endDate = date;
                    tvEndTime.setText((getTime(date)));
                }

            }
        })
                .setTimeSelectChangeListener(new OnTimeSelectChangeListener() {
                    @Override
                    public void onTimeSelectChanged(Date date) {

                    }
                })
                .setType(new boolean[]{true, true, true, true, true, false})
                .isDialog(true) //默认设置false ，内部实现将DecorView 作为它的父控件。
                .addOnCancelClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.i("pvTime", "onCancelClickListener");
                    }
                })
                .setItemVisibleCount(5) //若设置偶数，实际值会加1（比如设置6，则最大可见条目为7）
                .setLineSpacingMultiplier(2.0f)
                .isAlphaGradient(true)
                .build();

        Dialog mDialog = pvTime.getDialog();
        if (mDialog != null) {

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM);

            params.leftMargin = 0;
            params.rightMargin = 0;
            pvTime.getDialogContainerLayout().setLayoutParams(params);

            Window dialogWindow = mDialog.getWindow();
            if (dialogWindow != null) {
                dialogWindow.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim);//修改动画样式
                dialogWindow.setGravity(Gravity.BOTTOM);//改成Bottom,底部显示
                dialogWindow.setDimAmount(0.3f);
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        // TODO Auto-generated method stub
        super.onHiddenChanged(hidden);

        if (!hidden){
            showErrMsg(null);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mActivity = getActivity();

        initOrientation();

        if (!landscape)
            // TODO Auto-generated method stub
            createView = inflater.inflate(R.layout.a6_preconf_create, container, false);
        else
            createView = inflater.inflate(R.layout.a6_preconf_create_land, container, false);

        commonFactory = CommonFactory.getInstance();
        conferenceCommon = (ConferenceCommonImpl) commonFactory.getConferenceCommon();

        initView();
        ContactDataCommonImpl.preStartIndex = -1;
        ContactDataCommonImpl.preEndIndex = -1;
        initCreateHandler();

        conferenceCommon.setHandler(createHandler);
        String realName = FileUtil.readSharedPreferences(getActivity(), Constants.SHARED_PREFERENCES, Constants.LOGIN_EXNAME);
        String loginName = FileUtil.readSharedPreferences(getActivity(), Constants.SHARED_PREFERENCES, Constants.LOGIN_NAME);
        et1.setHint(!TextUtils.isEmpty(realName) ? realName + getResources().getString(R.string.preconf_create_topic_default) :
                loginName + getResources().getString(R.string.preconf_create_topic_default));

        initTimePicker();

        return createView;
    }

    private void initView() {
        ll1 = (LinearLayout) createView.findViewById(R.id.view_frag_create_ll_1);
        ll2 = (LinearLayout) createView.findViewById(R.id.view_frag_create_ll_2);
        //ll4 = (LinearLayout) createView.findViewById(R.id.view_frag_create_ll_4);
        et1 = (EditText) createView.findViewById(R.id.view_frag_create_et_1);
        //et2 = (EditText) createView.findViewById(R.id.view_frag_create_et_2);
        btnCreate = (Button) createView.findViewById(R.id.view_frag_create_btn);
        btnOrg = (Button) createView.findViewById(R.id.view_frag_create_btn_org);
        tvErrMsg = (TextView) createView.findViewById(R.id.view_frag_create_tv_2);
        //tvDuration = (TextView) createView.findViewById(R.id.view_frag_create_tv_4);

        tvStartTime = createView.findViewById(R.id.tv_start_time);
        tvEndTime = createView.findViewById(R.id.tv_end_time);

        tvStartTime.setOnClickListener(this);
        tvEndTime.setOnClickListener(this);

//		view = LayoutInflater.from(getActivity()).inflate(R.layout.preconf_create_dialog, null);
//		createDialog = new Dialog(getActivity(), R.style.styleSiteCheckDialog);
//		createDialog.setContentView(view);

        et1.setText(topic);
        //et2.setText(password);
        tvErrMsg.setVisibility(View.GONE);

        btnCreate.setOnClickListener(this);
        btnOrg.setOnClickListener(this);
        ll1.setOnClickListener(this);
        ll2.setOnClickListener(this);
        //ll4.setOnClickListener(this);
        et1.setOnFocusChangeListener(this);
        //et2.setOnFocusChangeListener(this);

        btnReturnList = createView.findViewById(R.id.btn_return_list);
        btnReturnList.setOnClickListener(this);

        rgConfTime = createView.findViewById(R.id.rg_conf_time);
        rgConfTime.setOnCheckedChangeListener(this);

        rbInstantConf = createView.findViewById(R.id.instant_conf);
        rbScheduleConf = createView.findViewById(R.id.schedule_conf);

        //rbInstantConf.setOnCheckedChangeListener(this);
        //rbScheduleConf.setOnCheckedChangeListener(this);

        //rbInstantConf.setChecked(true);
        //rbScheduleConf.setChecked(false);

        llStartTime = createView.findViewById(R.id.ll_start_time);
        llEndTime = createView.findViewById(R.id.ll_end_time);

        vStartTime = createView.findViewById(R.id.v_start_time);
        vEndTime = createView.findViewById(R.id.v_end_time);

        llStartTime.setVisibility(View.GONE);
        llEndTime.setVisibility(View.GONE);
        vStartTime.setVisibility(View.GONE);
        vEndTime.setVisibility(View.GONE);

        btnCreate.setText("立即开会");

    }

    private void initCreateHandler() {

        createHandler = new Handler() {
            public void handleMessage(Message msg) {
                Log.d("InfowareLab.Debug", "what = " + msg.what);
                switch (msg.what) {
                    case ConferenceCommon.RESULT_SUCCESS:
                        Log.d("InfowareLab.Debug", "join success");
                        hideLoading();
                        showMsg(null);
                        createIntent = new Intent(getActivity(), ConferenceActivity.class);
                        createIntent.putExtra("enterFromList", true);
                        startActivity(createIntent);
                        //getActivity().finish();
                        ((AudioCommonImpl) commonFactory.getAudioCommon()).onOpenAudioConfirm(true);
                        break;
                    case INIT_SDK:
                        Log.d("InfowareLab.Debug", "initSDK success");
                        break;
                    case INIT_SDK_FAILED:
                        hideLoading();
                        showLongToast(R.string.initSDKFailed);
                        showErrMsg(R.string.initSDKFailed);
                        break;
                    case CONF_CONFLICT:
                        hideLoading();
                        showLongToast(R.string.confConflict);
                        showErrMsg(R.string.confConflict);
                        break;
                    case CREATECONF_ERROR:
                        hideLoading();
                        showLongToast(R.string.preconf_create_error);
                        showErrMsg(R.string.preconf_create_error);
                        break;
                    case IDENTITY_FAILED:
                        hideLoading();
                        showLongToast(R.string.preconf_create_error_2);
                        showErrMsg(R.string.preconf_create_error_2);
                        break;
                    case BEYOUND_MAXNUM:
                        hideLoading();
                        showLongToast(R.string.preconf_create_error_7);
                        showErrMsg(R.string.preconf_create_error_2);
                        break;
                    case PASSWORD_NULL:
                        hideLoading();
                        showLongToast(R.string.preconf_create_error_9);
                        showErrMsg(R.string.preconf_create_error_9);
                        break;
                    case CONF_OVER:
                        hideLoading();
                        showLongToast(R.string.preconf_create_error_10);
                        showErrMsg(R.string.preconf_create_error_10);
                        break;
                    case NOT_HOST:
                        hideLoading();
                        showAlertDialog();
                        showLongToast(R.string.preconf_create_error_16);
                        showErrMsg(R.string.preconf_create_error_10);
                        break;
                    case BYOUND_STARTTIME:
                        hideLoading();
                        showLongToast(R.string.preconf_create_error_17);
                        showErrMsg(R.string.preconf_create_error_17);
                        break;
                    case CHECK_SITE:
                        hideLoading();
                        showLongToast(R.string.site_error);
                        showErrMsg(R.string.site_error);
                        break;
                    case SHOWALTER:
                        hideLoading();
                        showAlertDialog();
                        break;
                    case ConferenceCommon.LEAVE:
                        break;
                    default:
                        //showLongToast(R.string.preconf_notknown_error);
//					showErrMsg(R.string.preconf_notknown_error);
                        break;
                }
            }

            ;
        };
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.view_frag_create_btn) {
            hideInput(v);
            if (et1.getText().toString().length() <= 0) {
                String topic = FileUtil.readSharedPreferences(getActivity(),
                        Constants.SHARED_PREFERENCES, Constants.CONF_TOPIC);
                if (null != topic && topic.length() > 0)
                    et1.setText(topic);
            }

            topic = et1.getText().toString();

            if (TextUtils.isEmpty(topic))
            {
                topic = et1.getHint().toString();
            }

            if (TextUtils.isEmpty(topic))
            {
                topic = "未知会议主题";
            }

//            if (et2.getText().toString().length() <= 0) {
//                String confPwd = FileUtil.readSharedPreferences(getActivity(),
//                        Constants.SHARED_PREFERENCES, Constants.CONF_PWD);
//                if (null != confPwd && confPwd.length() > 0)
//                    et2.setText(confPwd);
//            }

            //String confPwd = FileUtil.readSharedPreferences(getActivity(),
            //        Constants.SHARED_PREFERENCES, Constants.CONF_PWD);

            if (null != topic && topic.length() > 0)
                if (!StringUtil.checkInput(et1.getText().toString(), Constants.PATTERN)) {
                    showShortToast(R.string.preconf_create_topicerror);
                    showErrMsg(R.string.preconf_create_topicerror);
                    return;
                }

            showLoading();

            //新建会议
            new Thread() {
                @Override
                public void run() {
                    String url = FileUtil.readSharedPreferences(getActivity(),
                            Constants.SHARED_PREFERENCES, Constants.SITE);
                    Config.SiteName = FileUtil.readSharedPreferences(getActivity(),
                            Constants.SHARED_PREFERENCES, Constants.SITE_NAME);

                    boolean bCreateConf = FileUtil.readSharedPreferencesBoolean(getActivity(),
                            Constants.SHARED_PREFERENCES, Constants.CREATE_CONF);

                    if (Config.SiteName.equals("") || Config.SiteName == null) {
                        createHandler.sendEmptyMessage(CHECK_SITE);
                        return;
                    }

                    String loginName = FileUtil.readSharedPreferences(
                            getActivity(), Constants.SHARED_PREFERENCES, Constants.LOGIN_NAME);
                    String loginPassword = FileUtil.readSharedPreferences(getActivity(),
                            Constants.SHARED_PREFERENCES, Constants.LOGIN_PASS);

                    if (loginName != null && loginName.length() > 0) {
                        login = new LoginBean(null, loginName, loginPassword);
                        login = ((ConferenceCommonImpl) CommonFactory.getInstance().getConferenceCommon())
                                .checkUser(login);//更新登录用户的信息

                        if (login == null) {
                            //当无网络或站点不可用时会出现login值为空的情况
                            //if (bCreateConf) {
                                //showShortToast(R.string.preconf_create_error_2);
                                //showErrMsg(R.string.preconf_create_error_2);
                            showErrMsgInUIThread("用户登录失败，请检查您的账号和密码");
                            createHandler.sendEmptyMessage(IDENTITY_FAILED);
                            return;
                            //}
                        }

                        //Login successfully ------------>

                        if (login.getNickname() != null && !login.getNickname().equals("")) {
                            FileUtil.saveSharedPreferences(getActivity(), Constants.SHARED_PREFERENCES,
                                    Constants.LOGIN_NICKNAME, login.getNickname());
                        }

                        if (login.getRealname() != null && !login.getRealname().equals("")) {
                            FileUtil.saveSharedPreferences(getActivity(), Constants.SHARED_PREFERENCES,
                                    Constants.LOGIN_EXNAME, login.getRealname());
                        } else if (login.getNickname() != null && !login.getNickname().equals("")) {

                            FileUtil.saveSharedPreferences(getActivity(), Constants.SHARED_PREFERENCES,
                                    Constants.LOGIN_EXNAME, login.getNickname());
                        }

                        FileUtil.saveSharedPreferences(getActivity(), Constants.SHARED_PREFERENCES,
                                Constants.CONF_LIST_TYPE, 0);

                        FileUtil.saveSharedPreferences(getActivity(), Constants.SHARED_PREFERENCES,
                                Constants.USER_ID, login.getUid());

                        //保存登录用户的角色权限信息，并通过匹配字符串检测是否有开会权限
                        FileUtil.saveSharedPreferences(getActivity(), Constants.SHARED_PREFERENCES,
                                Constants.LOGIN_ROLE, login.getCreateConfRole());

                        //-------------------------------------------------------------
                    }

                    String confId = FileUtil.readSharedPreferences(
                            getActivity(), Constants.SHARED_PREFERENCES, Constants.CONF_ID);

                    if (null == confId || confId.length() <= 0)
                        confId = "0";

                    if (bCreateConf && FileUtil.readSharedPreferences(getActivity(), Constants.SHARED_PREFERENCES,
                            Constants.LOGIN_ROLE).contains("meeting.role.init.host;")) {
                        confId = sendConfRequire(true, confId);
                        Handler confIdHandler = ConferenceApplication.getConferenceApp().mConfIdHandler;
                        if (confIdHandler != null) {
                            Message message = confIdHandler.obtainMessage();
                            message.what = 1000;
                            message.obj = confId;
                            confIdHandler.sendMessage(message);
                        }
                        ;

                    } else if (!bCreateConf) {
                        sendConfRequire(false, confId);
                    } else {
                        createHandler.sendEmptyMessage(SHOWALTER);
                    }
                }
            }.start();
        } else if (id == R.id.view_frag_create_btn_org) {
            setAtts("", "", "");
            Intent in = new Intent(getActivity(), ActOrganization.class);
            getActivity().startActivityForResult(in, ActOrganization.REQ_ORG);
        } else if (id == R.id.view_frag_create_et_1) {
            et1.requestFocus();
        } else if (id == R.id.tv_start_time) {

            startTimeSetting = true;
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);

            pvTime.setDate(calendar);

            /* pvTime.show(); //show timePicker*/
            pvTime.show(v);//弹出时间选择器，传递参数过去，回调的时候则可以绑定此view
        }else if (id == R.id.tv_end_time) {

            startTimeSetting = false;
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(endDate);

            pvTime.setDate(calendar);

            pvTime.show(v);//弹出时间选择器，传递参数过去，回调的时候则可以绑定此view
        }
        else if (id == R.id.btn_return_list) {
            doSelect(2);
        }
    }

//    private void showPopDuration(View v) {
//        PopSpinnerDuration popSpinnerDuration = new PopSpinnerDuration(getActivity(), v.getWidth(), v.getHeight(), 10);
//        popSpinnerDuration.setOnSelectListener(new PopSpinnerDuration.OnSelectListener() {
//            @Override
//            public void onSelect(int res, int dur) {
//                tvDuration.setText(getResources().getString(res));
//                duration = dur;
//            }
//        });
//        popSpinnerDuration.showAsDropDown(v);
//    }

    /**
     * 显示提示框
     */
    private void showAlertDialog() {
        alertDialog = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.notify_dialog, null);
        view.findViewById(R.id.notify_dialog_title).setVisibility(View.GONE);
        ((TextView) view.findViewById(R.id.notify_dialog_message)).setText(getResources().getString(
                R.string.preconf_forbidden));
        alertDialog.setCustomTitle(view);
        alertDialog.setPositiveButton(getResources().getString(R.string.known),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        alertDialog.setNegativeButton(getResources().getString(R.string.preconf_create_qiehuan),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createIntent = new Intent(getActivity(), LoginActivity.class);
                        createIntent.putExtra("switchID", true);
                        createIntent.putExtra("state", 1);
                        createIntent.putExtra("turnIndex", 4);
                        startActivity(createIntent);
                    }
                });
        alertDialog.show();
    }

    /**
     * 发送开启会议的申请获取会议ID
     */
    private String sendConfRequire(boolean create, String inputConfiId) {
        if (true) {
            config = conferenceCommon.initConfig();
            //设置会议开始时间为1小时后
            //final Date curDate = new Date(System.currentTimeMillis() + 60*60*1000);
            //设置会议主题
            conferenceBean.setName(topic);

            if (conferenceBean.getName() == null || conferenceBean.getName().toString().equals("")) {
                conferenceBean.setName(et1.getHint().toString());
            }

            //设置会议密码
            //conferenceBean.setConfPassword(et2.getText().toString());
            conferenceBean.setConfPassword("");

            //config.setStartTime(StringUtil.dateToStrInGMT(curDate, "yyyy-MM-dd hh:mm:ss"));
            conferenceBean.setConfType("0");
            conferenceBean.setConferencePattern(1);

            //		confId = Config.getConfId(ConfCreateActivity.this, conferenceBean);
            int uid = getActivity().getSharedPreferences(Constants.SHARED_PREFERENCES,
                    getActivity().MODE_PRIVATE).getInt(Constants.USER_ID, 0);
            String siteId = getActivity().getSharedPreferences(Constants.SHARED_PREFERENCES,
                    getActivity().MODE_PRIVATE).getString(Constants.SITE_ID, "");
            String userName = getActivity().getSharedPreferences(Constants.SHARED_PREFERENCES, getActivity().MODE_PRIVATE)
                    .getString(Constants.LOGIN_NAME, "");

            if (rbScheduleConf.isChecked())
            {
                conferenceBean.setStartTime(StringUtil.dateToStrInLocale(startDate, "yyyy-MM-dd HH:mm:ss"));

                long diff = endDate.getTime() - startDate.getTime();//这样得到的差值是毫秒级别

                if (diff <= 0)
                    duration = 120;
                else
                    duration = (int) (diff / (1000 * 60));

                conferenceBean.setDuration("" + duration);

                if (attIds != null && !attIds.equals("")) {
                    confId = Config.getFixedConfIdCovert(uid, userName, siteId, conferenceBean, attIds, attNames, attEmails);
                } else {
                    confId = Config.getFixedConfId(uid, userName, siteId, conferenceBean);
                }
            }
            else if (rbInstantConf.isChecked())
            {
                if (attIds != null && !attIds.equals("")) {
                    confId = Config.getConfIdCovert(uid, userName, siteId, conferenceBean, attIds, attNames, attEmails);
                } else {
                    confId = Config.getConfId(uid, userName, siteId, conferenceBean);
                }
            }
            else
            {
                //设置会议开始时间为1小时后
                final Date curDate = new Date(System.currentTimeMillis() + 10 * 60 * 1000);
                //			int duration = (int) (68373589 - System.currentTimeMillis()/(1000*60));
                conferenceBean.setStartTime(StringUtil.dateToStrInLocale(curDate, "yyyy-MM-dd HH:mm:ss"));
                conferenceBean.setDuration("" + duration);
                if (attIds != null && !attIds.equals("")) {
                    confId = Config.getFixedConfIdCovert(uid, userName, siteId, conferenceBean, attIds, attNames, attEmails);
                } else {
                    confId = Config.getFixedConfId(uid, userName, siteId, conferenceBean);
                }
            }

            if (confId.startsWith("0")) {
                confId = confId.substring(2);
                conferenceBean.setId(confId);

                if (confId.length() <= 8) {

                    if (rbScheduleConf.isChecked()) {
                        showMsgInUIThread("您的预约会议创建成功！会议号：" + confId);
                    }
                    else {
                        showMsgInUIThread("您的立即会议创建成功！正在启动和加入会议..");
                    }
                }

                if (rbInstantConf.isChecked()) {
                    result = Config.startConf(uid, userName, siteId, conferenceBean);

                    if (result.equals("-1:error")) {

                        showErrMsgInUIThread("对不起，您的立即会议启动失败！");

                        createHandler.sendEmptyMessage(CREATECONF_ERROR);

                    } else {

                        startConf();
                    }
                }
                else
                {
                    hideLoading();
                }

            } else {
                hideLoading();

                if (rbScheduleConf.isChecked()) {
                    showErrMsgInUIThread("对不起，您的预约会议创建失败！");
                }
                else {
                    showErrMsgInUIThread("对不起，您的立即会议创建失败！");
                }

                Log.d("InfowareLab.Debug", "FragCreate.confId = " + Integer.parseInt(confId));
                createHandler.sendEmptyMessage(Integer.parseInt(confId));
            }
            return confId;
        } else {
            confId = inputConfiId;
            startConf();
            return inputConfiId;
        }
    }

    /**
     * 开启会议
     */
    private void startConf() {
//		if (android.os.Environment.getExternalStorageState().equals( 
//				android.os.Environment.MEDIA_MOUNTED)){
//			conferenceCommon.setLogPath(Environment.getExternalStorageDirectory() + File.separator + "infowarelab");
//		}else{
//			conferenceCommon.setLogPath(getActivity().getCacheDir() + File.separator + "infowarelab");
//		}

        if (conferenceCommon.getHandler() != createHandler) {
            Log.d("InfowareLab.Debug", "===> reset create handler");
            conferenceCommon.setHandler(createHandler);
        }

        conferenceCommon.setLogPath(ConferenceApplication.getConferenceApp().getFilePath("Log"));
        conferenceCommon.initSDK();

        Config config = conferenceCommon.getConfig();
        login = new LoginBean(confId, FileUtil.readSharedPreferences(getActivity(), Constants.SHARED_PREFERENCES, Constants.LOGIN_NAME),
                "");

        conferenceCommon.joinConference(conferenceCommon.getParam(login, true));

        config.setMyConferenceBean(conferenceBean);
        ConferenceApplication.getConferenceApp().setJoined(true);
    }

    private void setFocusView(View v) {
        this.focusView = v;
    }

    private View getFocusView() {
        return focusView;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            setFocusView(v);
//			showInput(v);
        }
    }

    private void showErrMsg(int resId) {
        if (tvErrMsg != null) {
            if (resId == -1) {
                tvErrMsg.setVisibility(View.GONE);
            } else {
                tvErrMsg.setText(getResources().getString(resId) + "!");
                tvErrMsg.setTextColor(getResources().getColor(R.color.a6_txt_red1));
                tvErrMsg.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showMsg(String message) {
        if (tvErrMsg != null) {
            if (message == null) {
                tvErrMsg.setVisibility(View.GONE);
            } else {
                tvErrMsg.setText(message);
                tvErrMsg.setTextColor(getResources().getColor(R.color.a6_txt_green1));
                tvErrMsg.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showErrMsg(String message) {
        if (tvErrMsg != null) {
            if (message == null) {
                tvErrMsg.setVisibility(View.GONE);
            } else {
                tvErrMsg.setText(message);
                tvErrMsg.setTextColor(getResources().getColor(R.color.a6_txt_red1));
                tvErrMsg.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showMsgInUIThread(String message) {
        getActivity().runOnUiThread(new Runnable(){
            @Override
            public void run() {
                showMsg(message);
            }
        });
    }

    private void showErrMsgInUIThread(String message) {
        getActivity().runOnUiThread(new Runnable(){
            @Override
            public void run() {
                showErrMsg(message);
            }
        });
    }

    public void setAtts(String ids, String names, String emails) {
        this.attIds = ids;
        this.attNames = names;
        this.attEmails = emails;
    }

    private void doSelect(int selectId) {
        if (switchPageListener != null) {
            switchPageListener.doSelect(selectId);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == R.id.instant_conf){

            btnCreate.setText("立即开会");

            llStartTime.setVisibility(View.GONE);
            llEndTime.setVisibility(View.GONE);
            vStartTime.setVisibility(View.GONE);
            vEndTime.setVisibility(View.GONE);
        } else if (checkedId == R.id.schedule_conf){

            btnCreate.setText("预约会议");

            llStartTime.setVisibility(View.VISIBLE);
            vStartTime.setVisibility(View.VISIBLE);
            llEndTime.setVisibility(View.VISIBLE);
            vEndTime.setVisibility(View.VISIBLE);

            Calendar calendar = Calendar.getInstance();
            Date curDate = new Date(System.currentTimeMillis());
            calendar.setTime(curDate);
            calendar.add( Calendar.HOUR, 2);//获取当前时间减去一天，一天前。

            startDate = calendar.getTime();
            tvStartTime.setText(getTime(startDate));

            calendar.add( Calendar.HOUR, 2);
            endDate = calendar.getTime();
            tvEndTime.setText(getTime(endDate));
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

//        if (buttonView.getId() == R.id.instant_conf && isChecked){
//            llStartTime.setVisibility(View.GONE);
//            llEndTime.setVisibility(View.GONE);
//            vStartTime.setVisibility(View.GONE);
//        }
//        else if (buttonView.getId() == R.id.schedule_conf && isChecked){
//            llStartTime.setVisibility(View.VISIBLE);
//            vStartTime.setVisibility(View.VISIBLE);
//            llEndTime.setVisibility(View.VISIBLE);
//            vEndTime.setVisibility(View.VISIBLE);
//
//            Calendar calendar = Calendar.getInstance();
//            calendar.add( Calendar.HOUR, 2);//获取当前时间减去一天，一天前。
//
//            tvStartTime.setText(getTime(calendar.getTime()));
//
//            calendar.add( Calendar.HOUR, 2);
//            tvEndTime.setText(getTime(calendar.getTime()));
//        }
    }
}
