package com.infowarelab.hongshantongphone;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;

import com.infowarelab.conference.localDataCommon.LocalCommonFactory;
import com.infowarelab.conference.localDataCommon.impl.ContactDataCommonImpl;
import com.infowarelab.conference.ui.activity.inconf.ConferenceActivity;
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
import com.infowarelabsdk.conference.confmanage.IConfManage;
import com.infowarelabsdk.conference.domain.ConferenceBean;
import com.infowarelabsdk.conference.domain.LoginBean;
import com.infowarelabsdk.conference.transfer.Config;

import java.io.File;
import java.util.List;


public class EasyJoinUtil {
    private static EasyJoinUtil instance;

    private Handler handler;
    private AlertDialog loadingDialog;

    private ConfManageCommonImpl confManageCommon;
    private Context mContext;
    private String mSite;
    private String mConfId;
    private String mConfPwd;
    private String mNickName;

    private EasyJoinUtil() {
    }

    public static EasyJoinUtil getInstance() {
        if (instance == null) {
            instance = new EasyJoinUtil();
//            instance.initData();
        }
        return instance;
    }


    private void initData() {
        if (CommonFactory.getInstance().getChatCommom() == null) {
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
        }

        confManageCommon = (ConfManageCommonImpl) CommonFactory.getInstance()
                .getConfManageCommon();
        confManageCommon.initSDK();
        ((ConferenceCommonImpl) CommonFactory.getInstance()
                .getConferenceCommon()).setLogPath(Environment
                .getExternalStorageDirectory()
                + File.separator
                + "hongshantong"
                + File.separator + "Log");
    }


    public void joinConfWeb(Context context, String site, final String confId, final String confPwd, final String nickName, AlertDialog loadingDialog) {
        this.handler = new Handler();
        this.mContext = context;
        this.mSite = site;
        this.mConfId = confId;
        this.mConfPwd = confPwd;
        this.mNickName = nickName;
        if (this.loadingDialog != null && this.loadingDialog.isShowing())
            this.loadingDialog.dismiss();
        this.loadingDialog = loadingDialog;
        showLoading();

        new Thread() {
            @Override
            public void run() {
                boolean isSetSiteSucc = setSite(mSite);
                if (!isSetSiteSucc) return;
                ConferenceBean conferenceBean = getConfInfo(mConfId);
                if (conferenceBean == null) return;
                joinConf(conferenceBean, mNickName);
            }
        }.start();
    }

    private boolean setSite(final String url) {
        Config.SiteName = Config.getSiteName(url);
        if (Config.SiteName.equals("")
                || Config.SiteName == null) {
            showToast("SetSite Err = -6");
            return false;
        } else {
            Config.Site_URL = url;
            return true;
        }
    }

    private ConferenceBean getConfInfo(final String confid) {
        ConferenceBean conferenceBean = Config.getConferenceByNumber(confid);
        if (conferenceBean == null) {
            showToast("CheckConf Err ");
            return null;
        } else if (conferenceBean.getConfPassword().equals("")) {
            return conferenceBean;
        } else if (conferenceBean.getConfPassword().equals(mConfPwd)) {
            return conferenceBean;
        } else {
            showToast("CheckPwd Err ");
            return null;
        }
    }

    private void joinConf(final ConferenceBean conferenceBean, final String name) {
        ConferenceCommonImpl conferenceCommon = (ConferenceCommonImpl) CommonFactory.getInstance().getConferenceCommon();

        if (null == conferenceCommon || CommonFactory.getInstance().getChatCommom() == null) {
            CommonFactory.getInstance().setAudioCommon(new AudioCommonImpl()).setConferenceCommon(new ConferenceCommonImpl())
                    .setDocCommon(new DocCommonImpl()).setSdCommon(new ShareDtCommonImpl())
                    .setUserCommon(new UserCommonImpl()).setVideoCommon(new VideoCommonImpl()).setChatCommom(new ChatCommomImpl());
            LocalCommonFactory.getInstance().setContactDataCommon(new ContactDataCommonImpl());

            conferenceCommon = (ConferenceCommonImpl) CommonFactory.getInstance().getConferenceCommon();
        }

        int uid = 0;
        String siteid = Config.SiteId;
        LoginBean login = new LoginBean(conferenceBean.getId(), name, conferenceBean.getConfPassword());
        login.setUid(uid);

        Config config = conferenceCommon.initConfig(login);
        if ("0".equals(config.getConfigBean().getErrorCode())) {
            conferenceCommon.initSDK();
            config = conferenceCommon.getConfig();
            conferenceCommon.joinConference(conferenceCommon.getParam());
            config.setMyConferenceBean(conferenceBean);
            jump2Conf();
        } else {
            showToast("Join Err = " + config.getConfigBean().getErrorCode());
        }
    }

    public void joinConfSo(Context context, String site, String confId, String confPwd, String nickName, AlertDialog dialog) {
        this.handler = new Handler();
        this.mContext = context;
        this.mSite = site;
        this.mConfId = confId;
        this.mConfPwd = confPwd;
        this.mNickName = nickName;
        if (this.loadingDialog != null && this.loadingDialog.isShowing())
            this.loadingDialog.dismiss();
        this.loadingDialog = dialog;
        showLoading();

        confManageCommon.setiConfManage(new IConfManage() {
            @Override
            public void onLogin(int i) {

            }

            @Override
            public void onGetConfList(int i, int i1, List<ConferenceBean> list) {

            }

            @Override
            public void onGetConferenceInfo(int i, ConferenceBean conferenceBean) {
                String id = conferenceBean.getId();
                if (id != null && !id.equals("")) {
                    confManageCommon.joinConf(id, mConfPwd, mNickName);
                } else {
                    showToast("CheckConf Err = " + i);
                }
            }

            @Override
            public void onCreateConf(int i, String s) {

            }

            @Override
            public void onJoinConf(int i, ConferenceBean conferenceBean) {
                if (i == 0) {
                    jump2Conf();
                } else {
                    showToast("Join Err = " + i);
                }
            }

            @Override
            public void onSetSite(int i) {
                if (i == 0) {
                    confManageCommon.getConfInfo(mConfId);
                } else {
                    showToast("SetSite Err = " + i);
                }
            }

            @Override
            public void onUpdateUserDeviceInfo(int i) {

            }

            @Override
            public void onGetRecentInvitees(int i, String s) {

            }

            @Override
            public void onInviteAttendees(int i) {

            }

            @Override
            public void onGetConferenceCfg(int i, boolean b, boolean b1) {

            }

            @Override
            public void onGetIMOrgniztion(int i, String s) {

            }
        });
        confManageCommon.setSite(mSite);
    }

    private void showLoading() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (loadingDialog != null) loadingDialog.show();
            }
        });
    }

    private void hideLoading() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (loadingDialog != null) loadingDialog.dismiss();
            }
        });
    }

    private void jump2Conf() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(mContext, ConferenceActivity.class);
                mContext.startActivity(intent);
            }
        });
    }

    private void showToast(final String msg) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
