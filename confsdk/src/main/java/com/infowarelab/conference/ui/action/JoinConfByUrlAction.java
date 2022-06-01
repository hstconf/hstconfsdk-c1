package com.infowarelab.conference.ui.action;

////import org.apache.log4j.Logger;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;

import com.infowarelab.conference.BaseActivity;
import com.infowarelab.conference.ConferenceApplication;
import com.infowarelab.conference.ui.activity.inconf.ConferenceActivity;
import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.common.impl.ConferenceCommonImpl;
import com.infowarelabsdk.conference.confctrl.ConferenceCommon;
import com.infowarelabsdk.conference.domain.LoginBean;
import com.infowarelabsdk.conference.transfer.Config;
import com.infowarelabsdk.conference.util.Constants;
import com.infowarelabsdk.conference.util.FileUtil;

public class JoinConfByUrlAction extends BaseAction {
    //private Logger log = Logger.getLogger(getClass());
    private Config config;
    private LoginBean loginBean;
    protected ConferenceCommonImpl conferenceCommon = (ConferenceCommonImpl) commonFactory.getConferenceCommon();

    public JoinConfByUrlAction(BaseActivity mActivity) {
        super(mActivity);
    }

    public void setLoginBean(String confId, String name, String psd) {
        loginBean = new LoginBean(confId, name, psd);
        joinConf();
    }

    private void joinConf() {
        getSiteUrl();
        config = conferenceCommon.initConfig(loginBean);
        conferenceCommon.getConfig().setLoginBean(loginBean);
        //log.info("doLogin " + config.getConfigBean().getErrorCode() + ":" + config.getConfigBean().getErrorMessage());
        if ("0".equals(config.getConfigBean().getErrorCode())) {
            mHandler.sendEmptyMessage(JoinConfByIdAction4Frag.DISMISS);
            mHandler.sendEmptyMessage(JoinConfByIdAction4Frag.JOIN_CONFERENCE);

        } else {
            conferenceCommon.setJoinStatus(ConferenceCommon.NOTJOIN);
            if ("-1".equals(config.getConfigBean().getErrorCode())) {
                if (config.getConfigBean().getErrorMessage().startsWith("0x0604003")) {

                    mHandler.sendEmptyMessage(JoinConfByIdAction4Frag.FINISH);
                } else {
                    mHandler.sendEmptyMessage(JoinConfByIdAction4Frag.LOGINFAILED);
                }
            } else if ("-2".equals(config.getConfigBean().getErrorCode())) {
                mHandler.sendEmptyMessage(JoinConfByIdAction4Frag.FINISH);
            } else if ("-10".equals(config.getConfigBean().getErrorCode())) {
                mHandler.sendEmptyMessage(JoinConfByIdAction4Frag.MEETINGINVALIDATE);
            } else if ("-18".equals(config.getConfigBean().getErrorCode())) {
                mHandler.sendEmptyMessage(JoinConfByIdAction4Frag.MEETINGNOTJOINBEFORE);
            } else if (ConferenceCommon.HOSt_ERROR.equals(config.getConfigBean().getErrorCode())) {
                mHandler.sendEmptyMessage(JoinConfByIdAction4Frag.HOSTERROR);
            } else if (ConferenceCommon.SPELL_ERROR.equals(config.getConfigBean().getErrorCode())) {
                mHandler.sendEmptyMessage(JoinConfByIdAction4Frag.SPELLERROR);
            } else {
                mHandler.sendEmptyMessage(JoinConfByIdAction4Frag.GET_ERROR_MESSAGE);
            }
        }
    }

    private void getSiteUrl() {
        //log.info("ConferenceApplication.Site_URL === " + Config.Site_URL);
        if (Config.Site_URL == null || Config.Site_URL.equals("")) {
            Config.Site_URL = "m.infowarelab.cn";
            FileUtil.saveSharedPreferences(this.mActivity, Constants.SHARED_PREFERENCES, Constants.SITE,
                    Config.Site_URL);
        } else {
            //log.info("conference");
        }
    }

    private void joinConference() {
        saveConferenceInfo(loginBean);
        Intent intent = new Intent(mActivity, ConferenceActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        Uri uri = mActivity.getIntent().getData();
        intent.setData(uri);
        intent.putExtra("enterFromList", true);
        mActivity.startActivity(intent);
        ConferenceApplication.isJoinConf = true;
//		FileUtil.saveSharedPreferences(mActivity, Constants.SHARED_PREFERENCES, Constants.USER_NAME,
//				getContentById(R.id.username));

    }

    /**
     * save conferenceInfo for next time when update
     **/
    private void saveConferenceInfo(LoginBean loginBean) {
        FileUtil.saveObject(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/Infowarelab/conferenceObject", loginBean);
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case JoinConfByIdAction4Frag.LOGINUSERNAMEERROR:
                    Toast.makeText(mActivity, R.string.illegalCharacter, Toast.LENGTH_LONG).show();
//				mActivity.findViewById(R.id.loginbutton).setEnabled(true);
                    break;
                case JoinConfByIdAction4Frag.JOIN_CONFERENCE:
                    joinConference();
                    break;
                case JoinConfByIdAction4Frag.DISMISS:
                    // mActivity.findViewById(R.id.progressbar).setVisibility(View.INVISIBLE);
                    break;
                case JoinConfByIdAction4Frag.LOGINFAILED:
                    Toast.makeText(mActivity, R.string.LoginFailed, Toast.LENGTH_LONG).show();
                    break;
                case JoinConfByIdAction4Frag.FINISH:
                    // Intent intent = new Intent(mActivity,
                    // LoginUsermActivity.class);
                    // String data = "http://" + ConferenceApplication.Site_URL +
                    // "/index?";
                    // data += "confKey=" + ((TextView)
                    // mActivity.findViewById(R.id.conferenceId)).getEditableText();
                    // intent.setData(Uri.parse(data));
                    // mActivity.startmActivity(intent);
                    // mActivity.finish();
                    break;
                case JoinConfByIdAction4Frag.MEETINGINVALIDATE:
                    Toast.makeText(mActivity, R.string.meetingInvalidate, Toast.LENGTH_LONG).show();
                    break;
                case JoinConfByIdAction4Frag.MEETINGNOTJOINBEFORE:
                    Toast.makeText(mActivity, R.string.meetingNotJoinBefore, Toast.LENGTH_LONG).show();
                    break;
                case JoinConfByIdAction4Frag.HOSTERROR:
                    Toast.makeText(mActivity, R.string.hostError, Toast.LENGTH_LONG).show();
                    break;
                case JoinConfByIdAction4Frag.SPELLERROR:
                    Toast.makeText(mActivity, R.string.spellError, Toast.LENGTH_LONG).show();
                    break;
                case JoinConfByIdAction4Frag.GET_ERROR_MESSAGE:
                    Toast.makeText(mActivity, config.getConfigBean().getErrorMessage(), Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }

        }

        ;
    };

}
