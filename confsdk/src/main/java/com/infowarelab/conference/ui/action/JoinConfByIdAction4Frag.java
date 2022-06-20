package com.infowarelab.conference.ui.action;

////import org.apache.log4j.Logger;

import android.app.Activity;
        import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
        import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
        import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

        import androidx.annotation.RequiresApi;

import com.infowarelab.conference.ConferenceApplication;
import com.infowarelab.conference.live.LiveActivity;
import com.infowarelab.conference.ui.activity.ActExoPlayer;
import com.infowarelab.conference.ui.activity.inconf.ConferenceActivity;
import com.infowarelab.hongshantongphone.R;
        import com.infowarelab.conference.ui.activity.preconf.LoginActivity;
import com.infowarelab.conference.ui.activity.preconf.fragment.FragJoin;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.ConferenceCommonImpl;
import com.infowarelabsdk.conference.confctrl.ConferenceCommon;
import com.infowarelabsdk.conference.domain.ConferenceBean;
import com.infowarelabsdk.conference.domain.ConfigBean;
import com.infowarelabsdk.conference.domain.LoginBean;
import com.infowarelabsdk.conference.transfer.Config;
import com.infowarelabsdk.conference.util.Constants;
import com.infowarelabsdk.conference.util.FileUtil;

/**
 * @author joe.xiao
 * @Date 2013-9-11下午4:53:08
 * @Email joe.xiao@infowarelab.com
 */
public class JoinConfByIdAction4Frag implements OnClickListener {
    //private ConferencePagerNumber pagerNumber = null;
    //protected Logger log = Logger.getLogger(getClass());

    protected CommonFactory commonFactory = CommonFactory.getInstance();
    //private LinearLayout ll1, ll2, ll3;
    //private EditText mMeetingNumber, showName, mMeetingPassword;
    //private TextView tvMeetingNumber;
    //private ImageView ivCheck;

    String mJoinName = null;
    String mUserName = null;
    String mPassword = null;
    String mMeetingNumber = null;
    String mMeetingPassword = null;

    public static final int LOGINUSERERROR = 1;
    public static final int LOGINUSERNAMEERROR = 2;
    public static final int DISMISS = 3;
    public static final int FINISH = 4;
    public static final int DOCONFIG = 5;
    public static final int LOGINFAILED = 6;
    public static final int MEETINGINVALIDATE = 7;
    public static final int BCASTNOTEXIST = 17;
    public static final int MEETINGNOTJOINBEFORE = 8;
    public static final int HOSTERROR = 9;
    public static final int SPELLERROR = 10;
    public static final int GET_ERROR_MESSAGE = 11;
    public static final int JOIN_CONFERENCE = 12;
    public static final int CONNTIMEOUT = 13;
    public static final int ADDPASSWORDEDITOR = 14;
    public static final int CREATECONF_ERROR = 15;
    public static final int READY_JOINCONF = 16;
    public static final int NEED_LOGIN = 1001;
    public static final int NO_CONFERENCE = 1002;
    public static final int LOGIN_VALIDATE_ERRORTIP = 1003;
    private String result;

    private Config config;
    private ConferenceCommonImpl conferenceCommon;
    private LoginBean login;
    public SharedPreferences preferences;

    private ConferenceBean confBean;
    private String type = config.MEETING;
    private boolean isThird = false;
    private boolean isNext = true;

    private Activity mActivity;
    private FragJoin fragJoin;
    private View conferenceNumberView;
    public boolean isChecked = false;

    public JoinConfByIdAction4Frag(Activity activity, FragJoin fragJoin, View view) {
        this.mActivity = activity;
        this.fragJoin = fragJoin;
        this.conferenceNumberView = view;
        //this.pagerNumber = number;
        conferenceCommon = (ConferenceCommonImpl) commonFactory.getConferenceCommon();
        if (view != null) {
            //ll1 = (LinearLayout) view.findViewById(R.id.view_frag_join_number_ll_1);
            //ll2 = (LinearLayout) view.findViewById(R.id.view_frag_join_number_ll_4);
            //ll3 = (LinearLayout) view.findViewById(R.id.view_frag_join_number_ll_3);
            //tvMeetingNumber = (TextView) view.findViewById(R.id.view_frag_join_number_tv_2);
            //mMeetingNumber = (EditText) view.findViewById(R.id.view_frag_join_number_et_1);
            //showName = (EditText) view.findViewById(R.id.view_frag_join_number_et_2);
            //mMeetingPassword = (EditText) view.findViewById(R.id.view_frag_join_number_et_3);
            //ivCheck = (ImageView) view.findViewById(R.id.view_frag_join_number_iv_1);
        } else {
            isThird = true;
        }
    }

    /**
     * 根据ID 取得 Editor
     *
     * @param id
     * @return
     */
    protected EditText getEditorById(int id) {
        return (EditText) conferenceNumberView.findViewById(id);
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NEED_LOGIN:
                    Log.d("InfowareLab.Debug", "JoinConfById4Frag: NEED_LOGIN");
                    fragJoin.hideLoading();
//				showLongToast(R.string.needLogin);
                    fragJoin.showErrMsgNumber(R.string.needLogin);
                    Intent intent = new Intent(mActivity, LoginActivity.class);
                    Bundle data = msg.getData();
                    intent.putExtra("id", data.getString("id"));
                    intent.putExtra("username", data.getString("username"));
                    intent.putExtra("password", data.getString("password"));
                    mActivity.startActivity(intent);
//				mActivity.finish();
                    break;
                case JOIN_CONFERENCE:
                    Log.d("InfowareLab.Debug", "JoinConfById4Frag: JOIN_CONFERENCE");
//				if (android.os.Environment.getExternalStorageState().equals(
//						android.os.Environment.MEDIA_MOUNTED)){
//					conferenceCommon.setLogPath(Environment.getExternalStorageDirectory() + File.separator + "infowarelab");
//				}else{
//					conferenceCommon.setLogPath(mActivity.getCacheDir() + File.separator + "infowarelab");
//				}
                    conferenceCommon.setLogPath(ConferenceApplication.getConferenceApp().getFilePath("Log"));
                    if (fragJoin != null) fragJoin.setHandler();
                    commonFactory.getConferenceCommon().initSDK();
                    joinConference();
                    break;
                case NO_CONFERENCE:
                    Log.d("InfowareLab.Debug", "JoinConfById4Frag: NO_CONFERENCE");
                    fragJoin.hideLoading();
                    fragJoin.showErrMsgNumber(R.string.noConf);
                    break;
                case GET_ERROR_MESSAGE:
                    Log.d("InfowareLab.Debug", "JoinConfById4Frag: GET_ERROR_MESSAGE");
                    fragJoin.hideLoading();
                    fragJoin.showErrMsgNumber(config.getConfigBean().getErrorMessage());
                    break;
                case MEETINGNOTJOINBEFORE:
                    Log.d("InfowareLab.Debug", "JoinConfById4Frag: MEETINGNOTJOINBEFORE");
                    fragJoin.hideLoading();
                    fragJoin.showErrMsgNumber(R.string.meetingNotJoinBefore);
                    break;
                case HOSTERROR:
                    Log.d("InfowareLab.Debug", "JoinConfById4Frag: HOSTERROR");
                    fragJoin.hideLoading();
                    fragJoin.showErrMsgNumber(R.string.hostError);
                    break;
                case SPELLERROR:
                    Log.d("InfowareLab.Debug", "JoinConfById4Frag: SPELLERROR");
                    fragJoin.hideLoading();
                    fragJoin.showErrMsgNumber(R.string.spellError);
                    break;
                case LOGINFAILED:
                    Log.d("InfowareLab.Debug", "JoinConfById4Frag: LOGINFAILED");
                    fragJoin.hideLoading();
                    fragJoin.showErrMsgNumber(R.string.LoginFailed);
                    break;
                case CREATECONF_ERROR:
                    Log.d("InfowareLab.Debug", "JoinConfById4Frag: CREATECONF_ERROR");
                    fragJoin.hideLoading();
                    fragJoin.showErrMsgNumber(R.string.preconf_create_error);
                    break;
                case MEETINGINVALIDATE:
                    Log.d("InfowareLab.Debug", "JoinConfById4Frag: MEETINGINVALIDATE");
                    fragJoin.hideLoading();
                    fragJoin.showErrMsgNumber(R.string.overConf);
                    //mActivity.showLongToast(R.string.overConf);
                    //mActivity.finish();
                    break;
                case BCASTNOTEXIST:
                    Log.d("InfowareLab.Debug", "JoinConfById4Frag: BCASTNOTEXIST");
                    fragJoin.hideLoading();
                    fragJoin.showErrMsgNumber(R.string.bcastNotExist);
                    //mActivity.showLongToast(R.string.overConf);
                    //mActivity.finish();
                    break;
                case READY_JOINCONF:
                    Log.d("InfowareLab.Debug", "JoinConfById4Frag: READY_JOINCONF");
                    break;
                case FINISH:
                    Log.d("InfowareLab.Debug", "JoinConfById4Frag: FINISH");
                    break;
                default:
                    break;
            }
        }

        ;
    };

    @Override
    public void onClick(View v) {
        Log.d("InfowareLab.Debug", "JoinConfByIdAction4Frag: onClick");
        /*if (v.getId() == R.id.view_frag_join_number_btn) {
            fragJoin.hideInput(v);
            fragJoin.showErrMsgNumber(-1);
            if (isInputNull()) {
                return;
            }
            if (!isMatchShowname()) {
                fragJoin.showErrMsgNumber(R.string.illegalCharacter);
            } else {
                fragJoin.showLoading();
                if (ll1.getVisibility() == View.VISIBLE) {
                    meetingNum = mMeetingNumber.getText().toString().replace(" ", "");
                } else if (ll2.getVisibility() == View.VISIBLE) {
                    meetingNum = tvMeetingNumber.getText().toString().replace(" ", "");
                }
                Log.d("InfowareLab.Debug", "meetingNum = " + meetingNum);

                if (conferenceCommon == null) {
                    commonFactory.setConferenceCommon(new ConferenceCommonImpl());
                    conferenceCommon = (ConferenceCommonImpl) commonFactory.getConferenceCommon();
                }

                ConferenceBean conf = conferenceCommon.isLogin(meetingNum);
                int result = checkConf(conf);
                if (conf != null) {
                    type = conf.getType();
                }
                Log.d("InfowareLab.Debug", "result=" + result);
                if (result == 0 && !ConferenceActivity.isLogin) {
                    loginSystem();
                } else if (result == 2) {
//					mHandler.obtainMessage(NO_CONFERENCE).sendToTarget();
                    startLoginThread();
                } else {
                    startLoginThread();
                }
            }

        } else if (v.getId() == R.id.view_frag_join_number_ll_5) {
            if (isChecked) {
                ivCheck.setImageResource(R.drawable.a6_icon_joinhost_normal);
                isChecked = false;
            } else {
                ivCheck.setImageResource(R.drawable.a6_icon_joinhost_checked);
                isChecked = true;
            }
        }*/
    }

    private int checkConf(ConferenceBean conf) {
        if (conf != null) {
            int needLogin = conf.getNeedLogin();
            if (needLogin == 1)
                return 0;    //会议需要登录
            else return 1;   //会议不需要登录
        } else
            return 2;        //会议不存在

    }

    private void loginSystem() {
        Message msg = mHandler.obtainMessage(NEED_LOGIN);
        Bundle data = new Bundle();
        data.putString("id", mMeetingNumber);
        if (mUserName != null) {
            data.putString("username", mUserName);
        }
        if (mMeetingPassword != null) {
            data.putSerializable("password", mMeetingPassword);
        }
        msg.setData(data);
        msg.sendToTarget();

    }

    public void startLoginThread() {
        new Thread() {
            @Override
            public void run() {
                doLogin(getLoginBean());
            }

            ;
        }.start();
    }

    public void missDislog() {
        fragJoin.hideLoading();
    }

    private LoginBean getLoginBean() {
        login = new LoginBean(mMeetingNumber, mJoinName, mMeetingPassword);
        preferences = mActivity.getSharedPreferences(Constants.SHARED_PREFERENCES,
                Context.MODE_PRIVATE);
        login.setType(type);
        login.setUid(preferences.getInt(Constants.USER_ID, 0));
        return login;
    }

    /**
     * 登陸
     */
    public void doLogin(LoginBean login) {
        this.login = login;
        if (fragJoin != null) {
            if (confBean == null) {
                confBean = Config.getConferenceByNumber(mMeetingNumber);//fragJoin.getConfBean(meetingNum.trim());
                Log.d("InfowareLab.Debug", "===> Config.getConferenceByNumber = NULL");
            }

            if (confBean != null) {

                Log.d("InfowareLab.Debug", ">>>>>doLogin: conf status=" + confBean.getStatus());
                Log.d("InfowareLab.Debug", ">>>>>doLogin: conf type=" + confBean.getConfType());

                if (confBean.getHostID().equals(String.valueOf(login.getUid()))) {
                    if (!confBean.getConfPassword().equals(login.getPassword())) {
                        mHandler.sendEmptyMessage(SPELLERROR);
                        return;
                    }
                    if (!isChecked) {
                        startConf();
                        return;
                    }
                }
                if (!confBean.getConfType().equals("2") && confBean.getStatus().equals("0")) {
                    mHandler.sendEmptyMessage(MEETINGNOTJOINBEFORE);
                    return;
                }
            }

        }

        config = conferenceCommon.initConfig(login);
        if (config.getConfigBean() == null) {
            Log.d("InfowareLab.Debug", "configbean is null");
        }
        if (config.getConfigBean().getErrorCode() == null) {
            Log.d("InfowareLab.Debug", "errorcode is null");
        }
        Log.d("InfowareLab.Debug", "doLogin " + config.getConfigBean().getErrorCode() + ":" + config.getConfigBean().getErrorMessage());

        if ("0".equals(config.getConfigBean().getErrorCode())) {
            mHandler.sendEmptyMessage(JOIN_CONFERENCE);
        } else {
            ((ConferenceCommonImpl) commonFactory.getConferenceCommon()).setJoinStatus(
                    ConferenceCommon.NOTJOIN);
            if ("-1".equals(config.getConfigBean().getErrorCode())) {
                if (config.getConfigBean().getErrorMessage().startsWith("0x0604003")) {
                    if (config.getConfigBean().getErrorMessage().equals("0x0604003:you should login to meeting system! ")) {
                        loginSystem();
                    } else {
                        mHandler.sendEmptyMessage(FINISH);
                    }
                } else {
                    mHandler.sendEmptyMessage(LOGINFAILED);
                }
            } else if ("-2".equals(config.getConfigBean().getErrorCode())) {
                mHandler.sendEmptyMessage(FINISH);
            } else if ("-10".equals(config.getConfigBean().getErrorCode())) {
                if (Config.HAS_LIVE_SERVER) {
                    if (login.getType().equals(Config.MEETING)) {
                        login.setType(Config.LIVE);
                        doLogin(login);
                        return;
                    }
                }
                mHandler.sendEmptyMessage(MEETINGINVALIDATE);

            } else if ("-18".equals(config.getConfigBean().getErrorCode())) {
                mHandler.sendEmptyMessage(MEETINGNOTJOINBEFORE);
            } else if (ConferenceCommon.HOSt_ERROR.equals(config.getConfigBean().getErrorCode())) {
                mHandler.sendEmptyMessage(HOSTERROR);
            } else if (ConferenceCommon.SPELL_ERROR.equals(config.getConfigBean().getErrorCode())) {
                Log.d("InfowareLab.Debug", "spell error");
                mHandler.sendEmptyMessage(SPELLERROR);
            } else {
                mHandler.sendEmptyMessage(GET_ERROR_MESSAGE);
            }
            if (isThird) {
//				Intent intent = new Intent(mActivity, HomeActivity.class);
//				mActivity.startActivity(intent);
//				mActivity.finish();
            }
        }

    }

    private void startConf() {
        config = conferenceCommon.initConfig();
//		result = config.startConf(mActivity, confBean);
        int uid = mActivity.getSharedPreferences(Constants.SHARED_PREFERENCES,
                mActivity.MODE_PRIVATE).getInt(Constants.USER_ID, 0);
        String siteId = mActivity.getSharedPreferences(Constants.SHARED_PREFERENCES,
                mActivity.MODE_PRIVATE).getString(Constants.SITE_ID, "");

//		String userName = mActivity.getSharedPreferences(Constants.SHARED_PREFERENCES, mActivity.MODE_PRIVATE)
//				.getString(Constants.LOGIN_NAME, "");

        String userName = this.login.getUsername();

        this.login.setUsername(mActivity.getSharedPreferences(Constants.SHARED_PREFERENCES, mActivity.MODE_PRIVATE)
                .getString(Constants.LOGIN_NAME, ""));

        result = Config.startConf(uid, userName, siteId, confBean);

        if ("0".equals(result)) {
            conferenceCommon.setLogPath(ConferenceApplication.getConferenceApp().getFilePath("Log"));
            if (fragJoin != null) fragJoin.setHandler();
            conferenceCommon.initSDK();
            Config config = conferenceCommon.getConfig();
            conferenceCommon.joinConference(conferenceCommon.getParam(login, true));
            config.setMyConferenceBean(confBean);
            ConferenceApplication.getConferenceApp().setJoined(true);
            mHandler.sendEmptyMessage(READY_JOINCONF);
        } else {
            ((ConferenceCommonImpl) commonFactory.getConferenceCommon()).setJoinStatus(
                    ConferenceCommon.NOTJOIN);
            if ("-1".equals(result)) {
                if (config.getConfigBean().getErrorMessage().startsWith("0x0604003")) {
                    if (config.getConfigBean().getErrorMessage().equals("0x0604003:you should login to meeting system! ")) {
                        loginSystem();
                    } else {
                        mHandler.sendEmptyMessage(FINISH);
                    }
                } else {
                    mHandler.sendEmptyMessage(LOGINFAILED);
                }
            } else if ("-2".equals(result)) {
                mHandler.sendEmptyMessage(FINISH);
            } else if ("-10".equals(result)) {
                if (Config.HAS_LIVE_SERVER) {
                    if (login.getType().equals(Config.MEETING)) {
                        login.setType(Config.LIVE);
                        doLogin(login);
                        return;
                    }
                }
                mHandler.sendEmptyMessage(MEETINGINVALIDATE);

            } else if ("-18".equals(result)) {
                mHandler.sendEmptyMessage(MEETINGNOTJOINBEFORE);
            } else if (ConferenceCommon.HOSt_ERROR.equals(result)) {
                mHandler.sendEmptyMessage(HOSTERROR);
            } else if (ConferenceCommon.SPELL_ERROR.equals(result)) {
                Log.d("InfowareLab.Debug", "spell error");
                mHandler.sendEmptyMessage(SPELLERROR);
            } else {
                mHandler.sendEmptyMessage(GET_ERROR_MESSAGE);
            }
        }
    }

    private void joinConference() {
        Config config = conferenceCommon.getConfig();
        if (config != null) {
            ConfigBean configBean = config.getConfigBean();
            if (configBean != null) {
                configBean.setUserInfo_m_dwStatus(ConferenceCommon.RT_STATE_RESOURCE_AUDIO);
            } else
                Log.d("InfowareLab.Debug", "configBean is null");
        } else
            Log.d("InfowareLab.Debug", "config is null");

        config.setMyConferenceBean(confBean);
        commonFactory.getConferenceCommon().joinConference(conferenceCommon.getParam());
        ConferenceApplication.getConferenceApp().setJoined(true);
    }

    public ConferenceBean getConfBean() {
        return confBean;
    }

    public void setConfBean(ConferenceBean confBean) {

        if (confBean == null) return;

        this.confBean = confBean;

        mUserName = FileUtil.readSharedPreferences(mActivity, Constants.SHARED_PREFERENCES, Constants.LOGIN_NAME);
        mPassword = FileUtil.readSharedPreferences(mActivity, Constants.SHARED_PREFERENCES, Constants.LOGIN_PASS);

        mJoinName = FileUtil.readSharedPreferences(mActivity, Constants.SHARED_PREFERENCES, Constants.LOGIN_JOINNAME);
        if (mJoinName == null || mJoinName.equals("")) {

            if (mUserName == null || mUserName.equals("")){
                mJoinName = "未知";
            }
            else
                mJoinName = mUserName;
        }

        mMeetingNumber = confBean.getId();
        mMeetingPassword = confBean.getConfPassword();
    }

    public void cancelDialog() {
        fragJoin.hideLoading();
    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startJoinConference(){

        fragJoin.showErrMsgNumber(-1);

        if (confBean == null) {
            //fragJoin.hideLoading();
            return;
        }

        fragJoin.showLoading();

//        meetingNum = confBean.getId();

//        if (ll1.getVisibility() == View.VISIBLE) {
//            meetingNum = mMeetingNumber.getText().toString().replace(" ", "");
//        } else if (ll2.getVisibility() == View.VISIBLE) {
//            meetingNum = tvMeetingNumber.getText().toString().replace(" ", "");
//        }

        Log.d("InfowareLab.Debug", "startJoinConference: meetingNum = " + mMeetingNumber);

        if (conferenceCommon == null) {
            commonFactory.setConferenceCommon(new ConferenceCommonImpl());
            conferenceCommon = (ConferenceCommonImpl) commonFactory.getConferenceCommon();
        }

        ConferenceBean conf = conferenceCommon.isLogin(mMeetingNumber);
        int result = checkConf(conf);
        if (conf != null) {
            type = conf.getType();
        }
        Log.d("InfowareLab.Debug", "startJoinConference: result=" + result);
        if (result == 0 && !ConferenceActivity.isLogin) {
            loginSystem();
        } else if (result == 2) {
//					mHandler.obtainMessage(NO_CONFERENCE).sendToTarget();
            startLoginThread();
        } else {
            startLoginThread();
        }
        //}
    }

    private void startBCast(String rtmpUrl, String confId) {

//        Intent intent = new Intent(mActivity, ActRtmpPlayer.class);
//        intent.putExtra("rtmp_url", rtmpUrl);
//        intent.putExtra("conf_id", confId);
//        mActivity.startActivity(intent);

//        Intent intent = new Intent(mActivity, ActExoPlayer.class);
//        intent.putExtra("rtmp_url", rtmpUrl);
//        intent.putExtra("conf_id", confId);
//        mActivity.startActivity(intent);

        Intent intent = new Intent(mActivity, LiveActivity.class);
        intent.putExtra("rtmp_url", rtmpUrl);
        intent.putExtra("conf_id", confId);
        mActivity.startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startBroadcast(){

        fragJoin.showErrMsgNumber(-1);

        if (confBean == null) {
            //fragJoin.hideLoading();
            return;
        }

        fragJoin.showLoading();

        Log.d("InfowareLab.Debug", "startBroadcast: meetingNum = " + mMeetingNumber);

        if (conferenceCommon == null) {
            commonFactory.setConferenceCommon(new ConferenceCommonImpl());
            conferenceCommon = (ConferenceCommonImpl) commonFactory.getConferenceCommon();
        }

        String rtmpUrl = Config.getRtmpUrlByNumberEx(mMeetingNumber, null, null);
        if (rtmpUrl != null && false == TextUtils.isEmpty(rtmpUrl)){
            fragJoin.hideLoading();
            startBCast(rtmpUrl, mMeetingNumber);

        }else {
            mHandler.sendEmptyMessage(BCASTNOTEXIST);
        }
    }


}
