package com.infowarelab.conference.ui.action;

import java.io.File;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.infowarelab.conference.BaseActivity;
import com.infowarelab.conference.ConferenceApplication;
import com.infowarelab.conference.ui.activity.inconf.ConferenceActivity;
import com.infowarelab.hongshantongphone.R;
import com.infowarelab.conference.ui.activity.preconf.ActHome;
import com.infowarelab.conference.ui.activity.preconf.LoginActivity;
import com.infowarelabsdk.conference.common.impl.ConferenceCommonImpl;
import com.infowarelabsdk.conference.confctrl.ConferenceCommon;
import com.infowarelabsdk.conference.domain.ConferenceBean;
import com.infowarelabsdk.conference.domain.ConfigBean;
import com.infowarelabsdk.conference.domain.LoginBean;
import com.infowarelabsdk.conference.transfer.Config;
import com.infowarelabsdk.conference.util.Constants;
import com.infowarelabsdk.conference.util.StringUtil;

/**
 * @author joe.xiao
 * @Date 2013-9-11下午4:53:08
 * @Email joe.xiao@infowarelab.com
 */
public class JoinConfByIdAction extends BaseAction implements OnClickListener {

    private EditText mMeetingNumber, showName, mMeetingPassword;

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
    public static final int CREATECONF_ERROR = 15;
    public static final int READY_JOINCONF = 16;
    public static final int NEED_LOGIN = 1001;
    public static final int NO_CONFERENCE = 1002;
    public static final int LOGIN_VALIDATE_ERRORTIP = 1003;
    private String result;

    private Config config;
    private ConferenceCommonImpl conferenceCommon;
    private LoginBean login;
    private View view;
    private Dialog dialog;
    private Activity mActivity;
    public SharedPreferences preferences;
    private String meetingNum;
    private ConferenceBean confBean;
    private String type = config.MEETING;

    private boolean isThird = false;

    private boolean isNext = true;

    public JoinConfByIdAction(BaseActivity activity, LoginBean login, Dialog dialog) {
        super(activity);
        this.mActivity = activity;
        this.dialog = dialog;
        this.login = login;
        conferenceCommon = (ConferenceCommonImpl) commonFactory.getConferenceCommon();
    }

    public JoinConfByIdAction(BaseActivity activity, View view) {
        super(activity);
        this.mActivity = activity;
        conferenceCommon = (ConferenceCommonImpl) commonFactory.getConferenceCommon();
        if (view != null) {
            mMeetingNumber = (EditText) view.findViewById(R.id.join_meeting_number);
            showName = (EditText) view.findViewById(R.id.join_meeting_showname);
            mMeetingPassword = (EditText) view.findViewById(R.id.join_meeting_password);
        } else {
            isThird = true;
        }
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NEED_LOGIN:
                    if (dialog != null) {
                        dialog.cancel();
                    }
                    showLongToast(R.string.needLogin);
                    Intent intent = new Intent(mActivity, LoginActivity.class);
                    Bundle data = msg.getData();
                    intent.putExtra("id", data.getString("id"));
                    intent.putExtra("username", data.getString("username"));
                    intent.putExtra("password", data.getString("password"));
                    mActivity.startActivity(intent);
                    mActivity.finish();
                    break;
                case JOIN_CONFERENCE:
//				if (android.os.Environment.getExternalStorageState().equals( 
//						android.os.Environment.MEDIA_MOUNTED)){
//					conferenceCommon.setLogPath(Environment.getExternalStorageDirectory() + File.separator + "infowarelab");
//				}else{
//					conferenceCommon.setLogPath(mActivity.getCacheDir() + File.separator + "infowarelab");
//				}
                    //conferenceCommon.setHandler(mHandler);
                    conferenceCommon.setLogPath(ConferenceApplication.getConferenceApp().getFilePath("Log"));
                    commonFactory.getConferenceCommon().initSDK();
                    joinConference();
                    break;
                case NO_CONFERENCE:
                    if (dialog != null) {
                        dialog.cancel();
                    }
                    showLongToast(R.string.noConf);
                    break;
                case GET_ERROR_MESSAGE:
                    if (dialog != null) {
                        dialog.cancel();
                    }
                    //showLongToast(config.getConfigBean().getErrorMessage());
                    break;
                case MEETINGNOTJOINBEFORE:
                    if (dialog != null) {
                        dialog.cancel();
                    }
                    showLongToast(R.string.meetingNotJoinBefore);
                    break;
                case HOSTERROR:
                    if (dialog != null) {
                        dialog.cancel();
                    }
                    showLongToast(R.string.hostError);
                    break;
                case SPELLERROR:
                    if (dialog != null) {
                        dialog.cancel();
                    }
                    showLongToast(R.string.spellError);
                    break;
                case LOGINFAILED:
                    if (dialog != null) {
                        dialog.cancel();
                    }
                    showLongToast(R.string.LoginFailed);
                    break;
                case CREATECONF_ERROR:
                    if (dialog != null) {
                        dialog.cancel();
                    }
                    showLongToast(R.string.preconf_create_error);
                    break;
                case MEETINGINVALIDATE:
                    if (dialog != null) {
                        dialog.cancel();
                    }
                    showLongToast(R.string.overConf);
                    break;
                case READY_JOINCONF:
                    break;
                case FINISH:

                    break;
                default:
                    break;
            }
        }

        ;
    };

    @Override
    public void onClick(View v) {
        //log.info("onClick");
        if (v.getId() == R.id.join_meeting_button) {
            view = LayoutInflater.from(mActivity
            ).inflate(R.layout.progress_dialog, null);
            dialog = new Dialog(mActivity, R.style.styleSiteCheckDialog);
            dialog.setContentView(view);

            if (!isMatchShowname()) {
                showLongToast(R.string.illegalCharacter);
            } else {
                dialog.show();
                meetingNum = mMeetingNumber.getText().toString().replace(" ", "");
                //log.info("meetingNum = "+meetingNum);

                if (conferenceCommon == null) {
                    commonFactory.setConferenceCommon(new ConferenceCommonImpl());
                    conferenceCommon = (ConferenceCommonImpl) commonFactory.getConferenceCommon();
                }

                ConferenceBean conf = conferenceCommon.isLogin(meetingNum);
                int result = checkConf(conf);
                if (conf != null) {
                    type = conf.getType();
                }
                //log.info("result=" + result);
                if (result == 0 && !ConferenceActivity.isLogin) {
                    loginSystem();
                } else if (result == 2) {
//					mHandler.obtainMessage(NO_CONFERENCE).sendToTarget();
                    startLoginThread();
                } else {
                    startLoginThread();
                }
            }

        }
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
        data.putString("id", meetingNum);
        if (showName != null) {
            data.putString("username", showName.getText().toString());
        }
        if (mMeetingPassword != null) {
            data.putSerializable("password", mMeetingPassword.getText().toString());
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
        if (dialog != null) {
            dialog.cancel();
        }
    }

    private boolean isMatchShowname() {
        if (StringUtil.checkInput(showName.getText().toString(), Constants.PATTERN)) {
            return true;
        } else {
            return false;
        }
    }

    private LoginBean getLoginBean() {
        login = new LoginBean(meetingNum.trim(), showName.getText().toString().trim(),
                mMeetingPassword.getText().toString().trim());
        preferences = mActivity.getSharedPreferences(Constants.SHARED_PREFERENCES,
                Context.MODE_PRIVATE);
        login.setType(type);
        login.setUid(preferences.getInt(Constants.USER_ID, 0));
        return login;
    }


    public void startJoinThread(final LoginBean login) {
        new Thread() {
            @Override
            public void run() {
                doLogin(login);
            }

            ;
        }.start();
    }


    private ConferenceBean tpConfBean;

    /**
     * 登陸
     */
    public void doLogin(LoginBean login) {
        this.login = login;

        final String confID1 = login.getConferenceId();
        Thread thread = new Thread() {
            public void run() {
                tpConfBean = Config.getConferenceByNumber(confID1);
            }

            ;
        };

        try {
            thread.start();
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (tpConfBean == null || !tpConfBean.getHostID().equals(String.valueOf(login.getUid())) || !tpConfBean.getStatus().equals("0")) {
        } else {
            confBean = tpConfBean;
            startConf();
            return;
        }


        config = conferenceCommon.initConfig(login);
        if (config.getConfigBean() == null) {
            //log.info("configbean is null");
        }
        if (config.getConfigBean().getErrorCode() == null) {
            //log.info("errorcode is null");
        }
        //log.info("doLogin " + config.getConfigBean().getErrorCode() + ":" + config.getConfigBean().getErrorMessage());

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
                //log.info("spell error");
                mHandler.sendEmptyMessage(SPELLERROR);
            } else {
                mHandler.sendEmptyMessage(GET_ERROR_MESSAGE);
            }
            if (isThird) {
//                Intent intent = new Intent(mActivity, ActHome.class);
//                mActivity.startActivity(intent);
//                mActivity.finish();
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
        String userName = "";
        if (login == null || TextUtils.isEmpty(login.getRealname())) {
            userName = mActivity.getSharedPreferences(Constants.SHARED_PREFERENCES, mActivity.MODE_PRIVATE)
                    .getString(Constants.LOGIN_NAME, "");
        } else {
            userName = login.getRealname();
        }
        result = Config.startConf(uid, userName, siteId, confBean);

        if (result.equals("-1:error")) {
            mHandler.sendEmptyMessage(CREATECONF_ERROR);
        }
        else if ("-2".equals(config.getConfigBean().getErrorCode())) {
                mHandler.sendEmptyMessage(FINISH);
        }
        else if ("-10".equals(config.getConfigBean().getErrorCode())) {
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
                //log.info("spell error");
                mHandler.sendEmptyMessage(SPELLERROR);
        }
        else {
//			if (android.os.Environment.getExternalStorageState().equals( 
//					android.os.Environment.MEDIA_MOUNTED)){
//				conferenceCommon.setLogPath(Environment.getExternalStorageDirectory() + File.separator + "infowarelab");
//			}else{
//				conferenceCommon.setLogPath(mActivity.getCacheDir() + File.separator + "infowarelab");
//			}
            //conferenceCommon.setHandler(mHandler);
            conferenceCommon.setLogPath(ConferenceApplication.getConferenceApp().getFilePath("Log"));
            conferenceCommon.initSDK();
            Config config = conferenceCommon.getConfig();
            conferenceCommon.joinConference(conferenceCommon.getParam(login, true));
            config.setMyConferenceBean(confBean);
            ConferenceApplication.getConferenceApp().setJoined(true);
            mHandler.sendEmptyMessage(READY_JOINCONF);
        }
    }

    private void joinConference() {
        Config config = conferenceCommon.getConfig();
        if (config != null) {
            ConfigBean configBean = config.getConfigBean();
            if (configBean != null) {
                configBean.setUserInfo_m_dwStatus(ConferenceCommon.RT_STATE_RESOURCE_AUDIO);
            } //else
            //log.info("configBean is null");
        } //else
        //log.info("config is null");

        commonFactory.getConferenceCommon().joinConference(conferenceCommon.getParam());
        ConferenceApplication.getConferenceApp().setJoined(true);
    }

    public ConferenceBean getConfBean() {
        return confBean;
    }

    public void setConfBean(ConferenceBean confBean) {
        this.confBean = confBean;
    }

    public void cancelDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

}
