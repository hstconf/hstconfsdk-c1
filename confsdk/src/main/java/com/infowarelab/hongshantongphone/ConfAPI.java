package com.infowarelab.hongshantongphone;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.infowarelab.conference.ConferenceApplication;
import com.infowarelab.conference.localDataCommon.LocalCommonFactory;
import com.infowarelab.conference.localDataCommon.impl.ContactDataCommonImpl;
import com.infowarelab.conference.model.TerminalsListBean;
import com.infowarelab.conference.ui.activity.inconf.ConferenceActivity;
import com.infowarelab.conference.ui.activity.preconf.ActHome2;
import com.infowarelab.conference.utils.DeviceIdFactory;
import com.infowarelab.conference.utils.PublicWay;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.AudioCommonImpl;
import com.infowarelabsdk.conference.common.impl.ChatCommomImpl;
import com.infowarelabsdk.conference.common.impl.ConfManageCommonImpl;
import com.infowarelabsdk.conference.common.impl.ConferenceCommonImpl;
import com.infowarelabsdk.conference.common.impl.DocCommonImpl;
import com.infowarelabsdk.conference.common.impl.FileCommonImpl;
import com.infowarelabsdk.conference.common.impl.ShareDtCommonImpl;
import com.infowarelabsdk.conference.common.impl.UserCommonImpl;
import com.infowarelabsdk.conference.common.impl.VideoCommonImpl;
import com.infowarelabsdk.conference.confctrl.ConferenceCommon;
import com.infowarelabsdk.conference.domain.ConferenceBean;
import com.infowarelabsdk.conference.domain.ConfigBean;
import com.infowarelabsdk.conference.domain.LoginBean;
import com.infowarelabsdk.conference.transfer.Config;
import com.infowarelabsdk.conference.util.Constants;
import com.infowarelabsdk.conference.util.FileUtil;
import com.infowarelabsdk.conference.util.MessageEvent;
import com.infowarelabsdk.conference.util.NetUtil;
import com.infowarelabsdk.conference.util.StringUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfAPI {
    public static final int SUCCESS = 0;
    public static final int FINISH = 4;
    public static final int LOGINFAILED = 6;
    public static final int MEETINGINVALIDATE = 7;
    public static final int MEETINGNOTJOINBEFORE = 8;
    public static final int HOSTERROR = 9;
    public static final int SPELLERROR = 10;
    public static final int GET_ERROR_MESSAGE = 11;
    public static final int JOIN_CONFERENCE = 12;
    public static final int READY_JOINCONF = 16;
    public static final int NEED_LOGIN = 1001;
    public static final int NO_CONFERENCE = 1002;
    protected static final int INIT_SDK = 101;
    protected static final int INIT_SDK_FAILED = 102;
    protected static final int CREATECONF_ERROR = -3;
    protected static final int IDENTITY_FAILED = -2;
    protected static final int CONF_CONFLICT = -5;
    protected static final int BEYOUND_MAXNUM = -7;
    protected static final int PASSWORD_NULL = -9;
    protected static final int CONF_OVER = -10;
    protected static final int NOT_HOST = -16;
    protected static final int BYOUND_STARTTIME = -17;
    protected static final int CHECK_SITE = -18;
    protected static final int UNKNOWN_ERROR = -2000;
    public static final int ERROR_GET_SITENAME = -1;

    private static ConfAPI mInstance;
    private List<TerminalsListBean> mTerminateList = new ArrayList<>();
    public Handler notifyHandler = null;
    public Handler mExternalConfHandler = null;
    private String mAttIds = "";
    private String mAttNames = "";
    private String mAttEmails = "";
    private Context mContext = null;
    private ConferenceBean mConfBean = new ConferenceBean();

    private ConferenceCommonImpl mConferenceCommon = null;
    private VideoCommonImpl mVideoCommon = null;
    private LoginBean mLoginBean = null;
    private Config mConfig;
    private String mInviteeName = "";
    private int mHighVideoWidth = 1280;
    private int mHighVideoHeight = 720;
    private int mLowVideoWidth = 640;
    private int mLowVideoHeight = 480;
    private String mResult2;
    private boolean mResult1 = false;
    private int mConfType = 0;
    private String mConfId;
    private static final int WM_TERMINAL_LIST = 8;
    public static final int WM_SUCCESS = 1;
    public static final int WM_ERROR = 2;

    private ConfAPI() {
    }

    public static ConfAPI getInstance() {
        if (mInstance == null) {
            mInstance = new ConfAPI();
            mInstance.initData();
        }
        return mInstance;
    }

    private Handler mConfHandler;
    private int mLastErrorCode = -1000;

    public int getLastError() {
        return mLastErrorCode;
    }

    private void initConfHandler() {

        if (mConfHandler == null) {

            mConfHandler = new Handler() {
                public void handleMessage(Message msg) {

                    if (mExternalConfHandler != null) {
                        notifyExternalConf(msg.what, null);
                        return;
                    }

                    switch (msg.what) {
                        case ERROR_GET_SITENAME:
                            Log.d("InfowareLab.Debug", "ConfAPI:ERROR_GET_SITENAME");
                            mLastErrorCode = HOSTERROR;
                            showErrMsg(R.string.hostError);
                            notifyApp(WM_ERROR, HOSTERROR);
                            break;
                        case NEED_LOGIN:
                            mLastErrorCode = NEED_LOGIN;
                            showErrMsg(R.string.needLogin);
                            notifyApp(WM_ERROR, NEED_LOGIN);
                            break;
                        case NO_CONFERENCE:
                            mLastErrorCode = NO_CONFERENCE;
                            Log.d("InfowareLab.Debug", "ConfAPI: NO_CONFERENCE");
                            showErrMsg(R.string.noConf);
                            notifyApp(WM_ERROR, NO_CONFERENCE);
                            break;
                        case GET_ERROR_MESSAGE:
                            Log.d("InfowareLab.Debug", "ConfAPI: GET_ERROR_MESSAGE");
                            showErrMsg(mConfig.getConfigBean().getErrorMessage());
                            notifyApp(WM_ERROR, GET_ERROR_MESSAGE);
                            break;
                        case MEETINGNOTJOINBEFORE:
                            mLastErrorCode = MEETINGINVALIDATE;
                            Log.d("InfowareLab.Debug", "ConfAPI: MEETINGNOTJOINBEFORE");
                            showErrMsg(R.string.meetingNotJoinBefore);
                            notifyApp(WM_ERROR, MEETINGINVALIDATE);
                            break;
                        case HOSTERROR:
                            mLastErrorCode = HOSTERROR;
                            Log.d("InfowareLab.Debug", "ConfAPI: HOSTERROR");
                            showErrMsg(R.string.hostError);
                            notifyApp(WM_ERROR, HOSTERROR);
                            break;
                        case SPELLERROR:
                            mLastErrorCode = SPELLERROR;
                            Log.d("InfowareLab.Debug", "ConfAPI: SPELLERROR");
                            showErrMsg(R.string.spellError);
                            notifyApp(WM_ERROR, SPELLERROR);
                            break;
                        case LOGINFAILED:
                            mLastErrorCode = LOGINFAILED;
                            Log.d("InfowareLab.Debug", "ConfAPI: LOGINFAILED");
                            showErrMsg(R.string.LoginFailed);
                            notifyApp(WM_ERROR, LOGINFAILED);
                            break;
                        case CREATECONF_ERROR:
                            mLastErrorCode = CREATECONF_ERROR;
                            Log.d("InfowareLab.Debug", "ConfAPI:CREATECONF_ERROR");
                            showErrMsg(R.string.preconf_create_error);
                            notifyApp(WM_ERROR, CREATECONF_ERROR);
                            break;
                        case MEETINGINVALIDATE:
                            mLastErrorCode = MEETINGINVALIDATE;
                            Log.d("InfowareLab.Debug", "ConfAPI: MEETINGINVALIDATE");
                            showErrMsg(R.string.overConf);
                            notifyApp(WM_ERROR, MEETINGINVALIDATE);
                            break;
                        case READY_JOINCONF:
                            Log.d("InfowareLab.Debug", "ConfAPI: READY_JOINCONF");
                            break;
                        case FINISH:
                            Log.d("InfowareLab.Debug", "ConfAPI: FINISH");
                            break;
                        case JOIN_CONFERENCE:
                            Log.d("InfowareLab.Debug", "ConfAPI.JOIN_CONFERENCE");
                            initConfHandler();
                            mConferenceCommon.setLogPath(mContext.getCacheDir() + File.separator + "Log");
                            mConferenceCommon.initSDK();
                            joinConf();
                            break;
                        case ConferenceCommon.RESULT_SUCCESS:
                            Log.d("InfowareLab.Debug", "ConfAPI.RESULT_SUCCESS: Join success");
                            notifyApp(WM_SUCCESS, SUCCESS);
                            Intent intent = new Intent(mContext, ConferenceActivity.class);
                            mContext.startActivity(intent);
                            //((AudioCommonImpl)CommonFactory.getInstance().getAudioCommon()).onOpenAudioConfirm(true);
                            break;
                        case INIT_SDK:
                            Log.d("InfowareLab.Debug", "ConfAPI.INIT_SDK: initSDK success");
                            break;
                        case INIT_SDK_FAILED:
                            mLastErrorCode = INIT_SDK_FAILED;
                            Log.d("InfowareLab.Debug", "ConfAPI.INIT_SDK_FAILED: initSDK failed");
                            showErrMsg(R.string.initSDKFailed);
                            notifyApp(WM_ERROR, INIT_SDK_FAILED);
                            break;
                        case CONF_CONFLICT:
                            mLastErrorCode = CONF_CONFLICT;
                            showErrMsg(R.string.confConflict);
                            notifyApp(WM_ERROR, CONF_CONFLICT);
                            break;
                        case IDENTITY_FAILED:
                            mLastErrorCode = IDENTITY_FAILED;
                            showErrMsg(R.string.preconf_create_error_2);
                            notifyApp(WM_ERROR, IDENTITY_FAILED);
                            break;
                        case BEYOUND_MAXNUM:
                            mLastErrorCode = BEYOUND_MAXNUM;
                            showErrMsg(R.string.preconf_create_error_2);
                            notifyApp(WM_ERROR, BEYOUND_MAXNUM);
                            break;
                        case PASSWORD_NULL:
                            mLastErrorCode = PASSWORD_NULL;
                            showErrMsg(R.string.preconf_create_error_9);
                            notifyApp(WM_ERROR, PASSWORD_NULL);
                            break;
                        case CONF_OVER:
                            mLastErrorCode = CONF_OVER;
                            showErrMsg(R.string.preconf_create_error_10);
                            notifyApp(WM_ERROR, CONF_OVER);
                            break;
                        case NOT_HOST:
                            mLastErrorCode = NOT_HOST;
                            showErrMsg(R.string.preconf_create_error_16);
                            notifyApp(WM_ERROR, NOT_HOST);
                            break;
                        case BYOUND_STARTTIME:
                            mLastErrorCode = BYOUND_STARTTIME;
                            showErrMsg(R.string.preconf_create_error_17);
                            notifyApp(WM_ERROR, BYOUND_STARTTIME);
                            break;
                        case CHECK_SITE:
                            mLastErrorCode = CHECK_SITE;
                            showErrMsg(R.string.site_error);
                            notifyApp(WM_ERROR, CHECK_SITE);
                            break;
                        case ConferenceCommon.LEAVE:
                            break;
                        case ConferenceCommon.CLOUDRECORD:
                            break;
                        default:
                            Log.d("InfowareLab.Debug", ">>>>>>Unknown error: " + msg.what);
                            mLastErrorCode = UNKNOWN_ERROR;
                            //notifyApp(WM_ERROR, UNKNOWN_ERROR);
                            break;
                    }
                }
            };

        }
        mConferenceCommon.setHandler(mConfHandler);
    }

    private void notifyExternalConf(int what, Object obj) {
        if (mExternalConfHandler == null) return;

        Message message = Message.obtain();
        message.what = what;
        message.obj = obj;
        mExternalConfHandler.sendMessage(message);
    }

    private void notifyApp(int what, int result) {
        if (notifyHandler == null) return;

        Message message = Message.obtain();
        message.what = what;
        message.arg1 = result;
        notifyHandler.sendMessage(message);
    }

    private void showErrMsg(int resId) {
        //mLastErrorCode = resId;
        Toast.makeText(mContext, resId, Toast.LENGTH_LONG).show();
    }

    private void showMsg(int resId) {
        Toast.makeText(mContext, resId, Toast.LENGTH_LONG).show();
    }

    private void showErrMsg(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
    }

    private void initData() {
        if (CommonFactory.getInstance().getConferenceCommon() == null) {
            CommonFactory.getInstance()
                    .setConfManageCommon(new ConfManageCommonImpl())
                    .setAudioCommon(new AudioCommonImpl())
                    .setConferenceCommon(new ConferenceCommonImpl())
                    .setDocCommon(new DocCommonImpl())
                    .setSdCommon(new ShareDtCommonImpl())
                    .setUserCommon(new UserCommonImpl())
                    .setVideoCommon(new VideoCommonImpl())
                    .setFileCommon(new FileCommonImpl())
                    .setChatCommom(new ChatCommomImpl());
            LocalCommonFactory.getInstance().setContactDataCommon(new ContactDataCommonImpl());
        }

        mConferenceCommon = (ConferenceCommonImpl) CommonFactory.getInstance().getConferenceCommon();
        mVideoCommon = (VideoCommonImpl) CommonFactory.getInstance().getVideoCommon();

        initConfHandler();
        EventBus.getDefault().register(this);
    }

    private void release() {
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void Event(MessageEvent messageEvent) {
        switch (messageEvent.getType()) {
            case 8: {
                Log.d("InfowareLab.Debug", "Event: getTerminalList: " + PublicWay.terminalsData.size());
                if (null != notifyHandler) {
                    Message msg = Message.obtain();
                    msg.what = WM_TERMINAL_LIST;
                    msg.obj = PublicWay.terminalsData;
                    notifyHandler.sendMessage(msg);
                }
            }
            break;
            default:
                break;
        }
    }

    private String doEncode(String s) {
        String s1 = URLEncoder.encode(s);
        return s1;
    }

    private boolean checkSite(String url) {

        if (url.trim().length() <= 0 || url.trim().equals("")) {
            showErrMsg(R.string.site_error_null);
            return false;
        } else if (url.length() > 0) {
            if (!url.contains("http://")) {
                if (!url.contains("https://") && !StringUtil.isIP(url)) {
                    String[] ss = url.split(":");
                    String s = "";
                    for (String string : ss) {
                        s = s + ":" + doEncode(string);
                    }
                    s = s.replaceFirst(":", "");
                    url = "http://" + s;
                }
            } else {
                String s = url.replace("http://", "");
                String[] ss = s.split(":");
                s = "";
                for (String string : ss) {
                    s = s + ":" + doEncode(string);
                }
                s = s.replaceFirst(":", "");
                url = "http://" + s;
            }
            String regex = "^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
            Pattern patt = Pattern.compile(regex);
            Matcher matcher = patt.matcher(url.trim());
            boolean isMatch = matcher.matches();
            if (!isMatch) {
                mLastErrorCode = CHECK_SITE;
                showErrMsg(R.string.site_error_wrong);
                return false;
            }
        } else {
            if (url.equals(FileUtil.readSharedPreferences(
                    mContext, Constants.SHARED_PREFERENCES, Constants.SITE))) {
                return false;
            }
        }
        if (true) {
            if (!NetUtil.isNetworkConnected(mContext)) {
                mLastErrorCode = HOSTERROR;
                showErrMsg(R.string.site_error_net);
                return false;
            }
        }
        return true;
    }

    public boolean initSite(String url, Context context) {
        mContext = context;

        if (checkSite(url)) {

            Thread thread = new Thread() {
                public void run() {
                    Config.SiteName = Config.getSiteName(url);
                    if (Config.SiteName.equals("") || Config.SiteName == null) {
                        Config.Site_URL = null;
                    } else {
                        Config.Site_URL = url;
                        //保存SiteName、SiteId信息
                        FileUtil.saveSharedPreferences(context, Constants.SHARED_PREFERENCES,
                                Constants.SITE_NAME, Config.SiteName);
                        FileUtil.saveSharedPreferences(context, Constants.SHARED_PREFERENCES,
                                Constants.SITE_ID, Config.SiteId);
                        FileUtil.saveSharedPreferences(context, Constants.SHARED_PREFERENCES,
                                Constants.SITE, Config.Site_URL);
                        FileUtil.saveSharedPreferences(context, Constants.SHARED_PREFERENCES,
                                Constants.HAS_LIVE_SERVER, "" + Config.HAS_LIVE_SERVER);

                    }
                }
            };
            try {
                thread.start();
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (Config.Site_URL == null)
                return false;
            else
                return true;
        } else
            return false;

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public LoginBean login(String userName, String password) {
        ConferenceCommonImpl ConfCommon = (ConferenceCommonImpl) CommonFactory.getInstance().getConferenceCommon();

        if (null == Config.Site_URL) return null;
        if (null == ConfCommon) return null;

        if (mLoginBean != null) {
            mLastErrorCode = -1000;
            return mLoginBean;
        }

        mLastErrorCode = -1000;

        LoginBean loginBean = new LoginBean();
        loginBean.setUsername(userName);
        loginBean.setPassword(password);

        mLoginBean = ConfCommon.checkUser(loginBean);

        if (mLoginBean != null) {
            FileUtil.saveSharedPreferences(mContext, Constants.SHARED_PREFERENCES, Constants.LOGIN_NAME, mLoginBean.getUsername());
            FileUtil.saveSharedPreferences(mContext, Constants.SHARED_PREFERENCES, Constants.LOGIN_PASS, password);
            FileUtil.saveSharedPreferences(mContext, Constants.SHARED_PREFERENCES, Constants.LOGIN_ROLE, mLoginBean.getCreateConfRole());
            FileUtil.saveSharedPreferences(mContext, Constants.SHARED_PREFERENCES, Constants.USER_ID, mLoginBean.getUid());
            FileUtil.saveSharedPreferences(mContext, Constants.SHARED_PREFERENCES, Constants.LOGIN_ROLE, mLoginBean.getCreateConfRole());
            FileUtil.saveSharedPreferences(mContext, Constants.SHARED_PREFERENCES, Constants.LOGIN_NICKNAME, mLoginBean.getUsername());
            FileUtil.saveSharedPreferences(mContext, Constants.SHARED_PREFERENCES, Constants.LOGIN_EXNAME, mLoginBean.getUsername());
            FileUtil.saveSharedPreferences(mContext, Constants.SHARED_PREFERENCES, Constants.LOGIN_JOINNAME, mLoginBean.getUsername());

            ConferenceActivity.isLogin = true;

            showMsg(R.string.notify_login_suc);
        } else {
            FileUtil.saveSharedPreferences(mContext, Constants.SHARED_PREFERENCES, Constants.LOGIN_NAME, "");
            FileUtil.saveSharedPreferences(mContext, Constants.SHARED_PREFERENCES, Constants.LOGIN_PASS, "");
            FileUtil.saveSharedPreferences(mContext, Constants.SHARED_PREFERENCES, Constants.LOGIN_ROLE, "");
            FileUtil.saveSharedPreferences(mContext, Constants.SHARED_PREFERENCES, Constants.USER_ID, "");

            mLastErrorCode = LOGINFAILED;

            ConferenceActivity.isLogin = false;

            showErrMsg(R.string.preconf_login_error);
        }

        return mLoginBean;
    }

    public void setLoginBean(LoginBean loginBean){
        mLoginBean = loginBean;
    }

    public void setContext(Context context){
        mContext = context;
    }

    public List<ConferenceBean> getConferenceList(String url, String userName, String password, int confListType) {

        if (confListType == 1) {
            LoginBean loginBean = new LoginBean();
            loginBean.setUid(0);
            loginBean.setUsername(userName);
            return Config.getConferenceList(mLoginBean, notifyHandler, confListType);
        } else {
            if (mLoginBean == null) {
                mLoginBean = login(userName, password);
            }
            if (mLoginBean == null) return null;
            return Config.getConferenceList(mLoginBean, mConfHandler, confListType);
        }
    }

    public boolean registerTerminal(String joinName) {

        Thread thread = new Thread() {
            public void run() {

                String ret = Config.terminateRegist(DeviceIdFactory.getUUID1(mContext), joinName, Integer.parseInt(Config.SiteId), 0);
                if (ret.equals("0")) {
                    mResult1 = true;
                    Log.d("InfowareLab.Debug", "registerTerminal is successful");
                } else if (ret.equals("-1")) {
                    mResult1 = false;
                    Log.d("InfowareLab.Debug", "registerTerminal is failed");
                }
                PublicWay.refreshInviteServer(Config.Site_URL, Config.SiteId);
            }
        };
        try {
            thread.start();
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return mResult1;
    }

    public void requestTerminalList() {

        Thread thread = new Thread() {
            public void run() {
                PublicWay.requestTerminalList(mContext);
            }
        };
        try {
            thread.start();
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sendOnlineMessage(String joinName) {

        Thread thread = new Thread() {
            public void run() {
                PublicWay.sendOnlineMessage(joinName);
            }
        };
        try {
            thread.start();
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setInviteList(List<TerminalsListBean> terminateList) {

        mTerminateList.clear();
        mTerminateList.addAll(terminateList);

        if (mTerminateList.size() > 0) {
            TerminalsListBean terminalsListBean = mTerminateList.get(0);
            if (terminalsListBean != null) {
                mInviteeName = terminalsListBean.getName();
                Log.d("InfowareLab.Debug", "setInviteList: " + mInviteeName);
            }
        }

    }

    public void sendInviteMessage(String confId) {

        if (mLoginBean == null) return;

        PublicWay.sendInviteMessage(mTerminateList, confId, mLoginBean.getUsername(), mContext);

    }

    public void launchCreateConfUI(String confName, String confPwd, int duration, int confType, String inviteeName, Bitmap inviteeFace) {
        if (mContext == null) return;
        if (mLoginBean == null) return;

        mConfType = confType;

        initVideoResolution(confType);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        inviteeFace.compress(Bitmap.CompressFormat.PNG, 100, baos);

        Intent intent = new Intent(mContext, ConferenceActivity.class);
        intent.putExtra("actionMode", 0);
        intent.putExtra("userName", mLoginBean.getUsername());
        intent.putExtra("userId", mLoginBean.getUid());
        intent.putExtra("confName", confName);
        intent.putExtra("confPwd", confPwd);
        intent.putExtra("confType", confType);
        intent.putExtra("duration", duration);
        intent.putExtra("inviteName", inviteeName);
        intent.putExtra("face", baos.toByteArray());

        mLastErrorCode = -1000;

        mContext.startActivity(intent);

        createConf(confName, confPwd, duration);

    }

    private void initVideoResolution(int confType) {
//        if (confType == 0){
//            if (mVideoCommon != null) {
//                mVideoCommon.setWidth(mHighVideoWidth);
//                mVideoCommon.setHeight(mHighVideoHeight);
//            }
//        }
//        else if (confType == 1){
//            if (mVideoCommon != null) {
//                mVideoCommon.setWidth(mLowVideoWidth);
//                mVideoCommon.setHeight(mLowVideoHeight);
//            }
//        }
    }

    public void launchJoinConfUI(int userId, String confId, String confPwd, String joinName) {
        if (mLoginBean == null) return;

        Intent intent = new Intent(mContext, ConferenceActivity.class);
        intent.putExtra("actionMode", 1);
        intent.putExtra("userId", userId);
        intent.putExtra("userName", joinName);
        intent.putExtra("confId", confId);
        intent.putExtra("confPwd", confPwd);

        mContext.startActivity(intent);
    }

    public String createConf(String confName, String confPwd, int duration) {

        if (mLoginBean == null) return null;

        String attIds = null;
        String attNames = null;
        String attEmails = null;

        //设置会议开始时间为1小时后
        mConfBean.setName(confName);
        if (mConfBean.getName() == null || mConfBean.getName().toString().equals("")) {
            mConfBean.setName("NONAME");
        }

        mConfBean.setConfPassword(confPwd);
        mConfBean.setConfType("0");
        mConfBean.setConferencePattern(1); //自由模式会议
        final Date curDate = new Date(System.currentTimeMillis() + 10 * 60 * 1000);
        mConfBean.setStartTime(StringUtil.dateToStrInLocale(curDate, "yyyy-MM-dd HH:mm:ss"));
        mConfBean.setDuration("" + duration);

        Thread thread = new Thread() {
            public void run() {
                if (duration > 120) {
                    //设置会议开始时间为1小时后
                    if (attIds != null && !attIds.equals("")) {
                        mConfId = Config.getFixedConfIdCovert(mLoginBean.getUid(), mLoginBean.getUsername(), Config.SiteId, mConfBean, attIds, attNames, attEmails);
                    } else {
                        mConfId = Config.getFixedConfId(mLoginBean.getUid(), mLoginBean.getUsername(), Config.SiteId, mConfBean);
                    }
                } else {
                    if (attIds != null && !attIds.equals("")) {
                        mConfId = Config.getConfIdCovert(mLoginBean.getUid(), mLoginBean.getUsername(), Config.SiteId, mConfBean, attIds, attNames, attEmails);
                    } else {
                        mConfId = Config.getConfId(mLoginBean.getUid(), mLoginBean.getUsername(), Config.SiteId, mConfBean);
                    }
                }
            }

            ;
        };
        try {
            thread.start();
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (mConfId.startsWith("0")) {
            mConfId = mConfId.substring(2);
            mConfBean.setId(mConfId);

            Log.d("InfowareLab.Debug", "ConfAPI.createConf: mConfId = " + mConfId);

            //sendInviteMessage(confId);

            Thread createThread = new Thread() {
                public void run() {

                    Log.d("InfowareLab.Debug", "Config.startConf: mConfId = " + mConfId);
                    String result = Config.startConf(mLoginBean.getUid(), mLoginBean.getUsername(), Config.SiteId, mConfBean);

                    if (result.equals("-1:error")) {
                        mConfHandler.sendEmptyMessage(CREATECONF_ERROR);
                    } else {
                        initConfHandler();
                        mConferenceCommon.setLogPath(ConferenceApplication.getConferenceApp().getFilePath("Log"));
                        mConferenceCommon.initSDK();
                        Config config = mConferenceCommon.getConfig();
                        mConferenceCommon.joinConference(mConferenceCommon.getParam(mLoginBean, true));
                        config.setMyConferenceBean(mConfBean);
                        ConferenceApplication.getConferenceApp().setJoined(true);
                        mConfHandler.sendEmptyMessage(READY_JOINCONF);
                    }
                }
            };

            try {
                createThread.start();
                createThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } else {
            Log.d("InfowareLab.Debug", "ConfAPI.createConf: confId = " + mConfId);
            mConfHandler.sendEmptyMessage(CREATECONF_ERROR);
        }

        return mConfId;
    }

    public void startConf(int userId, String confId, String confPwd, String joinName) {

        Log.d("InfowareLab.Debug", "ConfAPI.startConf as host: confId = " + confId + "; siteId" + Config.SiteId);

        Config config = mConferenceCommon.initConfig();

        mResult2 = "-1:error";

        Thread thread = new Thread() {
            public void run() {
                mResult2 = Config.startConf(userId, joinName, Config.SiteId, mConfBean);
            }

            ;
        };
        try {
            thread.start();
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (mResult2.equals("-1:error")) {
            mConfHandler.sendEmptyMessage(CREATECONF_ERROR);
        } else {
            initConfHandler();
            mConferenceCommon.setLogPath(ConferenceApplication.getConferenceApp().getFilePath("Log"));
            mConferenceCommon.initSDK();
            config = mConferenceCommon.getConfig();
            //mConferenceCommon.setMeetingBox();
            //mConferenceCommon.joinConference(mConferenceCommon.getParam(getLoginBean(confPwd), true));
            mConferenceCommon.joinConference(mConferenceCommon.getParam(mLoginBean, true));
            config.setMyConferenceBean(mConfBean);
            ConferenceApplication.getConferenceApp().setJoined(true);
            mConfHandler.sendEmptyMessage(READY_JOINCONF);
        }
    }

    private LoginBean getLoginBean(String pwd) {
        LoginBean loginBean = new LoginBean();

        loginBean = new LoginBean(mConfBean.getId(), mLoginBean.getUsername(), pwd);
        loginBean.setType(mConfBean.getType());
        loginBean.setUid(mLoginBean.getUid());
        return loginBean;
    }

    public void joinConf(int userId, String confId, String confPwd, String joinName) {

        Log.d("InfowareLab.Debug", "ConfAPI.joinConf: confId = " + confId);

        ConferenceBean confBean = Config.getConferenceByNumber(confId);

        if (confBean == null || !confBean.getHostID().equals(String.valueOf(userId))/*||!confBean.getStatus().equals("0")*/) {
            Log.d("InfowareLab.Debug", "ConfAPI.joinConf as guest");
        } else {
            mConfBean = confBean;
            Log.d("InfowareLab.Debug", "ConfAPI.joinConf as host: confId = " + confId);
            //Join as host
            startConf(userId, confId, confPwd, joinName);
            return;
        }

        LoginBean loginBean = new LoginBean(confId, joinName, confPwd);
        loginBean.setUid(userId);
        loginBean.setType(Config.MEETING);

        mConfig = mConferenceCommon.initConfig(loginBean);
        if (mConfig.getConfigBean() == null) {
            Log.d("InfowareLab.Debug", "configbean is null");
        }
        if (mConfig.getConfigBean().getErrorCode() == null) {
            Log.d("InfowareLab.Debug", "errorcode is null");
        }

        //Log.d("InfowareLab.Debug","joinConf " + mConfig.getConfigBean().getErrorCode() + ":" + mConfig.getConfigBean().getErrorMessage());

        if ("0".equals(mConfig.getConfigBean().getErrorCode())) {
            mConfHandler.sendEmptyMessage(JOIN_CONFERENCE);
        } else {
            mConferenceCommon.setJoinStatus(
                    ConferenceCommon.NOTJOIN);
            if ("-1".equals(mConfig.getConfigBean().getErrorCode())) {
                if (mConfig.getConfigBean().getErrorMessage().startsWith("0x0604003")) {
                    if (mConfig.getConfigBean().getErrorMessage().equals("0x0604003:you should login to meeting system! ")) {
                        //loginSystem();
                        mConfHandler.sendEmptyMessage(NEED_LOGIN);
                    } else {
                        mConfHandler.sendEmptyMessage(FINISH);
                    }
                } else {
                    mConfHandler.sendEmptyMessage(LOGINFAILED);
                }
            } else if ("-2".equals(mConfig.getConfigBean().getErrorCode())) {
                mConfHandler.sendEmptyMessage(FINISH);
            } else if ("-10".equals(mConfig.getConfigBean().getErrorCode())) {
                if (Config.HAS_LIVE_SERVER) {
                    if (confBean.getType().equals(Config.MEETING)) {
                        confBean.setType(Config.LIVE);
                        //joinConf(confBean, getLoginBean(confBean, loginBean.getPassword()));
                        return;
                    }
                }
                mConfHandler.sendEmptyMessage(MEETINGINVALIDATE);

            } else if ("-18".equals(mConfig.getConfigBean().getErrorCode())) {
                mConfHandler.sendEmptyMessage(MEETINGNOTJOINBEFORE);
            } else if (ConferenceCommon.HOSt_ERROR.equals(mConfig.getConfigBean().getErrorCode())) {
                mConfHandler.sendEmptyMessage(HOSTERROR);
            } else if (ConferenceCommon.SPELL_ERROR.equals(mConfig.getConfigBean().getErrorCode())) {
                ////log.info("spell error");
                mConfHandler.sendEmptyMessage(SPELLERROR);
            } else {
                mConfHandler.sendEmptyMessage(GET_ERROR_MESSAGE);
            }
        }
    }

    public void joinConfByForce(Context context, int userId, String confId, String joinName) {

        Log.d("InfowareLab.Debug", "ConfAPI.joinConf: confId = " + confId);

        if (TextUtils.isEmpty(Config.Site_URL)) {
            Config.SiteName = FileUtil.readSharedPreferences(context, Constants.SHARED_PREFERENCES, Constants.SITE_NAME);
            Config.Site_URL = FileUtil.readSharedPreferences(context, Constants.SHARED_PREFERENCES, Constants.SITE);

            Log.d("InfowareLab.Debug",">>>>>>joinConfByForce.config.Site_URL Reset!");
        }

        ConferenceBean confBean = Config.getConferenceByNumber(confId);

        if (confBean == null) {
            Log.d("InfowareLab.Debug", "CANNOT get the conference information: " + confId);
            return;
        }

        String confPwd = confBean.getConfPassword();

        if (confBean == null || !confBean.getHostID().equals(String.valueOf(userId))/*||!confBean.getStatus().equals("0")*/) {
            Log.d("InfowareLab.Debug", "ConfAPI.joinConf as guest");
        } else {
            mConfBean = confBean;
            Log.d("InfowareLab.Debug", "ConfAPI.joinConf as host: confId = " + confId);
            //Join as host
            startConf(userId, confId, confPwd, joinName);
            return;
        }

        LoginBean loginBean = new LoginBean(confId, joinName, confPwd);
        loginBean.setUid(userId);
        loginBean.setType(Config.MEETING);

        mConfig = mConferenceCommon.initConfig(loginBean);
        if (mConfig.getConfigBean() == null) {
            Log.d("InfowareLab.Debug", "configbean is null");
        }
        if (mConfig.getConfigBean().getErrorCode() == null) {
            Log.d("InfowareLab.Debug", "errorcode is null");
        }

        //Log.d("InfowareLab.Debug","joinConf " + mConfig.getConfigBean().getErrorCode() + ":" + mConfig.getConfigBean().getErrorMessage());

        if ("0".equals(mConfig.getConfigBean().getErrorCode())) {
            mConfHandler.sendEmptyMessage(JOIN_CONFERENCE);
        } else {
            mConferenceCommon.setJoinStatus(
                    ConferenceCommon.NOTJOIN);
            if ("-1".equals(mConfig.getConfigBean().getErrorCode())) {
                if (mConfig.getConfigBean().getErrorMessage().startsWith("0x0604003")) {
                    if (mConfig.getConfigBean().getErrorMessage().equals("0x0604003:you should login to meeting system! ")) {
                        //loginSystem();
                        mConfHandler.sendEmptyMessage(NEED_LOGIN);
                    } else {
                        mConfHandler.sendEmptyMessage(FINISH);
                    }
                } else {
                    mConfHandler.sendEmptyMessage(LOGINFAILED);
                }
            } else if ("-2".equals(mConfig.getConfigBean().getErrorCode())) {
                mConfHandler.sendEmptyMessage(FINISH);
            } else if ("-10".equals(mConfig.getConfigBean().getErrorCode())) {
                if (Config.HAS_LIVE_SERVER) {
                    if (confBean.getType().equals(Config.MEETING)) {
                        confBean.setType(Config.LIVE);
                        //joinConf(confBean, getLoginBean(confBean, loginBean.getPassword()));
                        return;
                    }
                }
                mConfHandler.sendEmptyMessage(MEETINGINVALIDATE);

            } else if ("-18".equals(mConfig.getConfigBean().getErrorCode())) {
                mConfHandler.sendEmptyMessage(MEETINGNOTJOINBEFORE);
            } else if (ConferenceCommon.HOSt_ERROR.equals(mConfig.getConfigBean().getErrorCode())) {
                mConfHandler.sendEmptyMessage(HOSTERROR);
            } else if (ConferenceCommon.SPELL_ERROR.equals(mConfig.getConfigBean().getErrorCode())) {
                ////log.info("spell error");
                mConfHandler.sendEmptyMessage(SPELLERROR);
            } else {
                mConfHandler.sendEmptyMessage(GET_ERROR_MESSAGE);
            }
        }
    }

    private void joinConf() {
        mConfig = mConferenceCommon.getConfig();
        if (mConfig != null) {
            ConfigBean configBean = mConfig.getConfigBean();
            if (configBean != null) {
                configBean.setUserInfo_m_dwStatus(ConferenceCommon.RT_STATE_RESOURCE_AUDIO);
            } else
                Log.e("InfowareLab.Debug", "configBean is null");
        } else
            Log.e("InfowareLab.Debug", "config is null");
        Log.d("InfowareLab.Debug", "Calling information:" + mConferenceCommon.getParam());
        //mConferenceCommon.setMeetingBox();
        mConferenceCommon.joinConference(mConferenceCommon.getParam());
    }

    public void setNotifyHandler(Handler notify) {
        notifyHandler = notify;
    }

    public void launchJoinConfUIWithAccepting(String confId, String confPwd, int confType, String inviteName) {

        if (mLoginBean == null) return;

        mConfType = confType;

//        initVideoResolution(confType);
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        face.compress(Bitmap.CompressFormat.PNG, 100, baos);

        Intent intent = new Intent(mContext, ConferenceActivity.class);
        intent.putExtra("actionMode", 2);
        intent.putExtra("userId", 0);
        intent.putExtra("userName", mLoginBean.getUsername());
        intent.putExtra("confId", confId);
        intent.putExtra("confPwd", confPwd);
        intent.putExtra("confType", confType);
        //intent.putExtra("face", baos.toByteArray());
        //intent.putExtra("inviteName", inviteName);

        mLastErrorCode = -1000;

        mContext.startActivity(intent);
    }

    public void launchConfListUI(Context context, String url, String userName, String password, String joinName) {

        if (context == null) return;

        Intent intent = new Intent(context, ActHome2.class);
        intent.putExtra("actionMode", 0);
        intent.putExtra("siteURL", url);
        intent.putExtra("userName", userName);
        intent.putExtra("password", password);
        intent.putExtra("joinName", joinName);

        mLastErrorCode = -1000;

        context.startActivity(intent);
    }

    public void setExternalConfHandler(Handler handler) {
        mExternalConfHandler = handler;
    }

    public void clearLoginCookie() {
        mLoginBean = null;
    }

    public int getConfType() {
        return mConfType;
    }

}


