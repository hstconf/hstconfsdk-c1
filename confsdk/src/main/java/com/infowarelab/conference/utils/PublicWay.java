package com.infowarelab.conference.utils;

import static android.content.Context.NOTIFICATION_SERVICE;
import static android.content.Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

import com.infowarelab.conference.ConferenceApplication;
import com.infowarelab.conference.model.TerminalsListBean;
import com.infowarelab.conference.ui.activity.ActCustomDialog;
import com.infowarelab.conference.ui.activity.LogoActivity;
import com.infowarelab.conference.ui.activity.inconf.ConferenceActivity;
import com.infowarelab.conference.ui.activity.preconf.ActHome;
import com.infowarelab.conference.ui.view.CustomDialog;
import com.infowarelab.hongshantongphone.ConfAPI;
import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.callback.CallbackManager;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.ConferenceCommonImpl;
import com.infowarelabsdk.conference.common.impl.UserCommonImpl;
import com.infowarelabsdk.conference.domain.ConferenceBean;
import com.infowarelabsdk.conference.domain.LoginBean;
import com.infowarelabsdk.conference.transfer.Config;
import com.infowarelabsdk.conference.util.Constants;
import com.infowarelabsdk.conference.util.FileUtil;
import com.infowarelabsdk.conference.util.MessageEvent;
import com.infowarelabsdk.conference.util.NetUtil;
import com.infowarelabsdk.conference.util.ToastUtil;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.AsyncSocket;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.Util;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.ConnectCallback;
import com.koushikdutta.async.callback.DataCallback;

import org.greenrobot.eventbus.EventBus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by xiaor on 2019/11/18.
 */

public class PublicWay {

    //公共参数
    public static double latitude = 0.00;
    public static double longitude = 0.00;
    public static String IP = "";
    public static int PORT = 10002;

    //关于会议
    private static ConferenceCommonImpl conferenceCommon;

    public static Activity mActivity;
    private static Activity mConfActivity = null;
    public static Intent mFullscreenIntent = null;

    private static ConferenceBean confBean;

    //是否通知了
    private static boolean isInform = false;
    //socket是否连接
    public static boolean isConnect = false;

    public static List<TerminalsListBean> terminalsData = new ArrayList<>();
    //private static MediaPlayer mediaPlayer = null;

    private static Context mContext = null;

    /*socket*/
    private static AsyncSocket asyncSocket = null;
    private static String mJoinName;
    private static String mSendCmd;

    public static boolean mRinging = false;
    private static Timer mTimer = null;
    private static TimerTask mTask = null;

    static DataCallback mDataCallback = new DataCallback() {
        @Override
        public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) {
            String rcvMsg = new String(bb.getAllByteArray());
            //Log.d("InfowareLab.Debug","onDataAvailable: Received Message " + rcvMsg);
            formatXML(0, rcvMsg);
        }
    };

    static CompletedCallback mCompletedCallback = new CompletedCallback() {

        @Override
        public void onCompleted(Exception ex) {
            if (ex != null) {
                //throw new RuntimeException(ex);
                Log.d("InfowareLab.Debug", "onCompleted: Error: " + ex.getMessage());
            }
            //else
            //    Log.d("InfowareLab.Debug","onCompleted: Successfully send message.");
        }
    };

    static CompletedCallback mEndCompletedCallback = new CompletedCallback() {

        @Override
        public void onCompleted(Exception ex) {
            if (ex != null) {
                //throw new RuntimeException(ex);
                Log.d("InfowareLab.Debug", "mEndCompletedCallback: Error: " + ex.getMessage());
            } else
                Log.d("InfowareLab.Debug", "mEndCompletedCallback");
        }
    };

    static CompletedCallback mCloseCompletedCallback = new CompletedCallback() {

        @Override
        public void onCompleted(Exception ex) {
            if (ex != null) {
                //throw new RuntimeException(ex);
                Log.d("InfowareLab.Debug", "mCloseCompletedCallback: Error: " + ex.getMessage());
            } else
                Log.d("InfowareLab.Debug", "mCloseCompletedCallback");
        }
    };
    private static CustomDialog dialog = null;
    private static NotificationUtils mNotificationUtils;
    private static Ringtone ringtone;
    private static int mNotifyCount = 0;
    private static String mTitle = "";
    private static String mContent = "";

    /**
     * 判断服务是否运行
     */
    public static boolean isServiceRunning(Activity activity, final String className) {
        ActivityManager activityManager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> info = activityManager.getRunningServices(Integer.MAX_VALUE);
        if (info == null || info.size() == 0) return false;
        for (ActivityManager.RunningServiceInfo aInfo : info) {
            if (className.equals(aInfo.service.getClassName())) return true;
        }
        return false;
    }

    /**
     * Start service if the service is not running.
     *
     * @param action : Enum of Action.
     */
    public static void actionOnService(SocketService.Actions action) {
        Intent intent = new Intent(mActivity, SocketService.class);
        intent.setAction(action.name());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("InfowareLab.Debug", "Starting the service in >= 26 Mode");
            mActivity.bindService(intent, mConnection, mActivity.BIND_ABOVE_CLIENT);
            mActivity.startForegroundService(intent);
            return;
        }

        Log.d("InfowareLab.Debug", "Starting the service in < 26 Mode");
        mActivity.bindService(intent, mConnection, mActivity.BIND_ABOVE_CLIENT);
        mActivity.startService(intent);
    }

    public static SocketService.SocketBinder socketBinder;

    /*获取ip,port接口*/
    public static boolean getIpAndPort(Activity mActivity1, String url, String siteId) {

        mActivity = mActivity1;
        String result = "-1";
        final StringBuffer m_url = new StringBuffer(url + "/meeting/remoteServlet?funcName=getInvitingServer&siteId=" + siteId);
        try {
            Log.d("InfowareLab.Debug", "getIpAndPort: url=" + m_url);
            String response = NetUtil.doGet(m_url.toString());
            Log.d("InfowareLab.Debug", "getIpAndPort: response=" + response);
            if (response != null && !response.equals("")) {
                DocumentBuilderFactory domfac = DocumentBuilderFactory
                        .newInstance();
                DocumentBuilder dombuilder = domfac.newDocumentBuilder();
                Document doc = dombuilder.parse(new InputSource(
                        new StringReader(response)));
                Element root = doc.getDocumentElement();
                if (root == null) {
                    return false;
                }
                if (root.getElementsByTagName("return").item(0).getFirstChild().getNodeValue().equals("0")) {
                    result = root.getElementsByTagName("result").item(0).getTextContent();
                    String[] ss = result.split(":");
                    if (ss.length == 1) {
                        IP = ss[0];
                        PORT = 10002;
                        Log.d("InfowareLab.Socket", "ip:" + IP + "; port:" + PORT);
                        return true;
                    } else if (ss.length > 1) {
                        IP = ss[0];
                        PORT = Integer.parseInt(ss[1]);
                        Log.d("InfowareLab.Socket", "ip:" + IP + "; port:" + PORT);
                        return true;
                    }
                }
            }
        } catch (Exception e) {

            e.printStackTrace();
        } finally {
            /*先判断 Service是否正在运行 如果正在运行  给出提示  防止启动多个service*/
            Log.d("InfowareLab.Socket", "finally IP = " + IP);
            Log.d("InfowareLab.Socket", "finally PORT = " + PORT);
            if (PublicWay.isServiceRunning(mActivity, "com.infowarelab.conference.utils.SocketService")) {
                actionOnService(SocketService.Actions.STOP);
//                if (socketBinder != null){
//                    Log.d("InfowareLab.Socket","releaseSocket");
//                    socketBinder.getService().count = 0;
//                    socketBinder.getService().isReConnect = true;
//                    socketBinder.getService().bReleaseSocket = false;
//                    socketBinder.getService().releaseSocket();
//                }else {
//                    Log.d("InfowareLab.Socket","bindService & startService");
//                    Intent intent = new Intent(mActivity, SocketService.class);
//                    mActivity.bindService(intent,mConnection,mActivity.BIND_ABOVE_CLIENT);
//                    mActivity.startService(intent);
//                }
//                return true;
            }
            /*启动service*/
//                Log.d("InfowareLab.Socket","bindService & startService");
//                Intent intent = new Intent(mActivity, SocketService.class);
//                mActivity.bindService(intent,mConnection,mActivity.BIND_ABOVE_CLIENT);
//                mActivity.startService(intent);
            actionOnService(SocketService.Actions.START);

        }
        return false;
    }

    public static boolean refreshInviteServer(String url, String siteId) {
        String result = "-1";
        final StringBuffer m_url = new StringBuffer(url + "/meeting/remoteServlet?funcName=getInvitingServer&siteId=" + siteId);
        try {
            Log.d("InfowareLab.Debug", "refreshInviteServer: url=" + m_url);
            String response = NetUtil.doGet(m_url.toString());
            Log.d("InfowareLab.Debug", "refreshInviteServer: response=" + response);
            if (response != null && !response.equals("")) {
                DocumentBuilderFactory domfac = DocumentBuilderFactory
                        .newInstance();
                DocumentBuilder dombuilder = domfac.newDocumentBuilder();
                Document doc = dombuilder.parse(new InputSource(
                        new StringReader(response)));
                Element root = doc.getDocumentElement();
                if (root == null) {
                    return false;
                }
                if (root.getElementsByTagName("return").item(0).getFirstChild().getNodeValue().equals("0")) {
                    result = root.getElementsByTagName("result").item(0).getTextContent();
                    String[] ss = result.split(":");
                    if (ss.length == 1) {
                        IP = ss[0];
                        PORT = 10002;
                        Log.d("InfowareLab.Debug", "refreshInviteServer: response=" + IP + ":" + PORT);
                        return true;
                    } else if (ss.length > 1) {
                        IP = ss[0];
                        PORT = Integer.parseInt(ss[1]);
                        Log.d("InfowareLab.Debug", "refreshInviteServer: response=" + IP + ":" + PORT);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            Log.e("InfowareLab.Debug", e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public static ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            socketBinder = (SocketService.SocketBinder) binder;
            if (socketBinder != null) {
                socketBinder.getService().setOnReceiveDataListener(new SocketService.OnReceiveDataListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onReceiveData(String str) {
//                        String[] strBuffer = str.split("\n");
//                        for(int i = 0; i < strBuffer.length; i ++) {
//                            formatXML(0, strBuffer[i]);
//                        }
                        Log.d("InfowareLab.Debug", "onReceiveData:" + str);
                        formatXML(0, str);
                    }
                });
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    static ConferenceBean tpConfBean = null;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static void formatXML(int type, String xml) {
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(new StringReader(xml.trim()));
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        try {
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                    case XmlPullParser.END_TAG:
                        if (0 == type) {
                            if ("Action".equals(parser.getName())
                                    && !"invite".equals(parser.nextText().toLowerCase())) {

                                formatXML(1, xml);
                                if (parser.getEventType() != XmlPullParser.END_TAG) {
                                    if ("invite_response".equals(parser.nextText().toLowerCase())) {
                                        return;
                                    }
                                }
                                return;
                            } else if ("ConfID".equals(parser.getName())) {

                                String confID = parser.nextText();

                                isInform = false;
                                XMLUtils.CONFIGID = confID.substring(0, 8);

                                Log.d("InfowareLab.Debug", "confID: " + XMLUtils.CONFIGID);

                                //XMLUtils.CONFIGNAME = confID.substring(8,confID.length()).replace("会议","").trim();

                                Log.d("InfowareLab.Debug", "Inviter name: " + XMLUtils.CONFIGNAME);

                                //Bitmap bitmap= BitmapFactory.decodeResource(mContext.getResources(), R.drawable.caller_face);

                                //ConfAPI.getInstance().launchJoinConfUIWithAccepting(XMLUtils.CONFIGID,"",ConfAPI.getInstance().getConfType(),XMLUtils.CONFIGNAME);

                                CommonFactory commonFactory = CommonFactory.getInstance();
                                conferenceCommon = (ConferenceCommonImpl) commonFactory.getConferenceCommon();
                                Thread thread = new Thread() {
                                    public void run() {

                                        if (TextUtils.isEmpty(Config.Site_URL)) {
                                            Config.SiteName = FileUtil.readSharedPreferences(mActivity, Constants.SHARED_PREFERENCES, Constants.SITE_NAME);
                                            Config.Site_URL = FileUtil.readSharedPreferences(mActivity, Constants.SHARED_PREFERENCES, Constants.SITE);

                                            Log.d("InfowareLab.Debug",">>>>>>PublicWay.config.Site_URL Reset!");
                                        }

                                        tpConfBean = Config.getConferenceByNumber(XMLUtils.CONFIGID);
                                    };
                                };
                                try {
                                    thread.start();
                                    thread.join();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if (tpConfBean != null){
                                    confBean = tpConfBean;
                                    XMLUtils.CONFERENCEPATTERN = tpConfBean.getConferencePattern();

                                    Log.d("InfowareLab.Debug", "confBean: " + confBean.getId());

                                    //SharedPreferencesUrls.getInstance().putBoolean("isPopup",false);
                                    //SharedPreferencesUrls.getInstance().putBoolean("isPopup1",false);
                                    if (mActivity != null){
                                        showDialog(mActivity.getResources().getString(R.string.zd_invite_title),
                                                String.format(mActivity .getResources().getString(R.string.dialog_content),XMLUtils.CONFIGNAME));
                                    }/*else if (ActConf.mActivity != null){
//                                    mActivity = ActConf.mActivity;
//                                    showDialog(ActConf.mActivity .getResources().getString(R.string.zd_invite_title),
//                                            String.format(ActConf.mActivity .getResources().getString(R.string.dialog_content),XMLUtils.configName));
                                    }else if (ActMain.mActivity != null){
                                        mActivity = ActMain.mActivity;
                                        showDialog(mActivity.getResources().getString(R.string.zd_invite_title),
                                                String.format(ActMain.mActivity .getResources().getString(R.string.dialog_content),XMLUtils.CONFIGNAME));
                                    }else if (MicSetActivity.mActivity != null){
                                        mActivity = MicSetActivity.mActivity;
                                        showDialog(mActivity .getResources().getString(R.string.zd_invite_title),
                                                String.format(MicSetActivity.mActivity .getResources().getString(R.string.dialog_content),XMLUtils.CONFIGNAME));
                                    }else if (ActSound.mActivity != null){
                                        mActivity = ActSound.mActivity;
                                        showDialog(mActivity .getResources().getString(R.string.zd_invite_title),
                                                String.format(ActSound.mActivity .getResources().getString(R.string.dialog_content),XMLUtils.CONFIGNAME));
                                    }*/
                                }
                                else
                                    Log.d("InfowareLab.Socket", "tpConfBean = null");

                            } else if ("InviteID".equals(parser.getName())) {
                                XMLUtils.inviteID = parser.nextText();
                            } else if ("Name".equals(parser.getName())) {
                                XMLUtils.CONFIGNAME = parser.nextText();
                            }
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (socketBinder != null) {
                                        String name = (mActivity.getSharedPreferences(Constants.SHARED_PREFERENCES,
                                                Context.MODE_PRIVATE)).getString(Constants.LOGIN_JOINNAME, "");
                                        String siteId = FileUtil.readSharedPreferences(mActivity, Constants.SHARED_PREFERENCES,
                                                Constants.SITE_ID);
                                        if (!isInform &&
                                                !TextUtils.isEmpty(XMLUtils.inviteID)) {
                                            socketBinder.getService().sendOrder(XMLUtils.getMsg("" + System.currentTimeMillis(), name,
                                                    DeviceIdFactory.getUUID1(mActivity), XMLUtils.CONFIGID, siteId, XMLUtils.inviteID));
                                            isInform = true;
                                        }
                                    }
                                }
                            }, 500);
                        } else if (1 == type) {
                            if ("Action".equals(parser.getName())) {
                                if (!"terminals_response".equals(parser.nextText().toLowerCase())) {
                                    break;
                                }
                                if (parser.getEventType() != XmlPullParser.END_TAG) {
                                    return;
                                }
                                if (terminalsData.size() != 0) {
                                    terminalsData.clear();
                                }
                                //解析xml
                                try {
                                    TerminalsListBean bean = null;
                                    XmlPullParserFactory pullParserFactory = XmlPullParserFactory.newInstance();
                                    //获取XmlPullParser的实例
                                    XmlPullParser xmlPullParser = pullParserFactory.newPullParser();
                                    //设置输入流  xml文件
                                    xmlPullParser.setInput(new ByteArrayInputStream(xml.getBytes()), "UTF-8");
                                    //开始
                                    int eventType = xmlPullParser.getEventType();
                                    while (eventType != XmlPullParser.END_DOCUMENT) {
                                        String nodeName = xmlPullParser.getName();
                                        switch (eventType) {
                                            case XmlPullParser.START_DOCUMENT:
                                                //文档开始
                                                break;
                                            case XmlPullParser.START_TAG:

                                                //开始节点
                                                if ("Terminal".equals(nodeName)) {
                                                    bean = new TerminalsListBean();
                                                } else if ("ID".equals(nodeName)) {
                                                    bean.setID(xmlPullParser.nextText());
                                                } else if ("Name".equals(nodeName)) {
                                                    bean.setName(xmlPullParser.nextText());
                                                } else if ("InnerSvrIP".equals(nodeName)) {
                                                    bean.setInnerSvrIP(xmlPullParser.nextText());
                                                } else if ("InnerSvrPort".equals(nodeName)) {
                                                    bean.setInnerSvrPort(xmlPullParser.nextText());
                                                } else if ("longitude".equals(nodeName)) {
                                                    bean.setLongitude(xmlPullParser.nextText());
                                                } else if ("latitude".equals(nodeName)) {
                                                    bean.setLatitude(xmlPullParser.nextText());
                                                } else if ("Online".equals(nodeName)) {
                                                    if ("true".equals(xmlPullParser.nextText())) {
                                                        bean.setOnline(true);
                                                    } else {
                                                        bean.setOnline(false);
                                                    }
                                                }
                                                break;
                                            //结束节点
                                            case XmlPullParser.END_TAG:
                                                if ("Terminal".equals(nodeName)) {
                                                    if (bean.isOnline())
                                                        terminalsData.add(0, bean);
                                                    else
                                                        terminalsData.add(bean);
                                                    bean = null;
                                                }
                                                break;
                                            default:
                                                break;
                                        }
                                        eventType = xmlPullParser.next();
                                    }
                                    Log.d("InfowareLab.Debug", "terminalsData = " + terminalsData.size());

                                    //刷新列表
                                    MessageEvent messageEvent = new MessageEvent();
                                    messageEvent.setType(8);
                                    EventBus.getDefault().postSticky(messageEvent);
                                } catch (Exception e) {
                                    Log.d("InfowareLab.Socket", "Failed to get terminal list: " + e.getMessage());
                                    e.printStackTrace();
                                    //ToastUtil.showMessage(mActivity,"获取列表失败，请重试",2 * 1000);
                                }
                            }
                        }
                        break;
                    default:
                        break;
                }
                event = parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    private static void startConf() {

        String userName = FileUtil.readSharedPreferences(
                mActivity, Constants.SHARED_PREFERENCES, Constants.LOGIN_JOINNAME);

        if (TextUtils.isEmpty(userName)) {
            FileUtil.readSharedPreferences(
                    mActivity, Constants.SHARED_PREFERENCES, Constants.LOGIN_NAME);
        }

        if (TextUtils.isEmpty(userName)) {
            userName = "NO_NAME";
        }

        SharedPreferences preferences = mActivity.getSharedPreferences(
                Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        int uid = preferences.getInt(Constants.USER_ID, 0);

        LoginBean bean = new LoginBean();
        bean.setConferenceId(XMLUtils.CONFIGID);
        bean.setUsername(userName);
        bean.setUid(uid);

        ConfAPI.getInstance().setContext(mActivity);
        ConfAPI.getInstance().setLoginBean(bean);
        ConfAPI.getInstance().joinConfByForce(mActivity, uid, XMLUtils.CONFIGID, userName);

        /*
        //conferenceCommon.setHandler(mHandler);
        conferenceCommon.setLogPath(((ConferenceApplication) mActivity.getApplication()).getFilePath("Log"));
        conferenceCommon.initSDK();
        LoginBean bean = new LoginBean();
        bean.setConferenceId(XMLUtils.CONFIGID);
        bean.setUid(0);
        String userName = FileUtil.readSharedPreferences(
                mActivity, Constants.SHARED_PREFERENCES, Constants.LOGIN_JOINNAME);
        if (TextUtils.isEmpty(userName)) {
            userName = "NO_NAME";
        }
        bean.setUsername(userName);
        String nickName = "";
        if (!TextUtils.isEmpty(FileUtil.readSharedPreferences(mActivity,
                Constants.SHARED_PREFERENCES, Constants.LOGIN_NAME))) {
            nickName = FileUtil.readSharedPreferences(mActivity,
                    Constants.SHARED_PREFERENCES, Constants.LOGIN_NAME);
        }
        //更新登录用户的信息
        ((ConferenceCommonImpl) CommonFactory.getInstance().getConferenceCommon())
                .checkUser(getLoginBean(""));
        if (nickName.equals(confBean.getHostName()) || nickName.equals(confBean.getCreatorName())) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SharedPreferences preferences = mActivity.getSharedPreferences(
                            Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE);
                    int uid = preferences.getInt(Constants.USER_ID, 0);
                    String siteId = mActivity.getSharedPreferences(Constants.SHARED_PREFERENCES,
                            mActivity.MODE_PRIVATE).getString(Constants.SITE_ID, "");
                    //Log.d("InfowareLab.Socket", "Failed to get terminal list: " + e.getMessage());
                    //Log.e("999999","会议Id::"+XMLUtils.CONFIGID);
                    if (!Config.startConf(uid, FileUtil.readSharedPreferences(
                            mActivity, Constants.SHARED_PREFERENCES, Constants.LOGIN_JOINNAME), siteId, confBean).equals("-1:error")) {
                        conferenceCommon.setMeetingBox();
                        conferenceCommon.joinConference(conferenceCommon.getParam(getLoginBean(""), true));
                    }
                }
            }).start();
        } else {
            //Log.e("ldy","测试会议加入configId::"+XMLUtils.CONFIGID);
            String parme = Config.getConfigParam(bean, Config.MEETING);
            //Log.e("tttttt","会议参数::"+parme);
            conferenceCommon.setMeetingBox();
            conferenceCommon.joinConference(parme);
        }*/
    }

    static Handler handler = new Handler(Looper.getMainLooper());
    Handler notifyHandler = null;


    @SuppressLint("NewApi")
    public static void afterShowDialog(boolean confirmed) {
        if (confirmed){
            if (CallbackManager.IS_LEAVED){
                startConf();
                //dialog.dismiss();
            }
            else {

                if (mConfActivity != null) {
                    ((ConferenceActivity) mConfActivity).exit();
                    ((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).getUsersList().clear();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startConf();
                        }
                    }, 2500);
                }
                else
                    startConf();
            }

            handler.post(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.showMessage(mActivity,"您正在加入"+XMLUtils.CONFIGNAME+"的会议...",1000);
                }
            });
        }
        else
        {
            XMLUtils.CONFIGID = "";
            XMLUtils.CONFIGNAME = "";
            XMLUtils.CONFERENCEPATTERN = 1;
            isInform = true;
            //SharedPreferencesUrls.getInstance().putString("configId","");
        }

        //mNotificationUtils.clearAllNotifiication();
        //mNotificationUtils.sendNotificationFullScreen("", "", "InfowareLab Conference", false);

        Log.d("InfowareLab.Debug", ">>>afterShowDialog");

        stopRing();
    }

    public static void startRing(String title, String content) throws IOException {

        //if (mediaPlayer != null) resetMediaPlay();

        if (mRinging) return;

        Log.d("InfowareLab.Debug", ">>>startRing...");

        AudioManager audioMgr = (AudioManager) PublicWay.socketBinder.getService().getSystemService(Context.AUDIO_SERVICE);

        audioMgr.setMode(AudioManager.MODE_RINGTONE);

        audioMgr.setSpeakerphoneOn(true);

        try {
            int maxVolume = audioMgr.getStreamMaxVolume(AudioManager.STREAM_RING);

            audioMgr.setStreamVolume(AudioManager.STREAM_RING, maxVolume, AudioManager.FLAG_PLAY_SOUND);

            Log.d("InfowareLab.Debug", ">>>setStreamVolume: " + maxVolume);
        }
        catch (java.lang.SecurityException e)
        {
            ;
        }

        Uri ringUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

        ringtone = RingtoneManager.getRingtone(mActivity, ringUri);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ringtone.setLooping(true);
        }

        ringtone.play();

        mRinging = true;

        mNotifyCount = 0;

        if (mTask != null){
            mTask.cancel();
            mTask = null;
        }

        if (mTimer != null) {
            mTimer.purge();
            mTimer.cancel();
            mTimer = null;
        }

        if (mTask == null) {
            mTask = new TimerTask() {
                @Override
                public void run() {

               mNotifyCount++;

               Log.d("InfowareLab.Debug", ">>>getService().notifyInvited(): " + mNotifyCount);

               PublicWay.socketBinder.getService().notifyInvited(title, content);

               if (mNotifyCount > 30)
               {
                   stopRing();
               }
                }
            };
        }

        mTimer = new Timer();
        mTimer.schedule(mTask, 0, 1 * 1000);

        //播放器
//        if (mediaPlayer == null)
//            mediaPlayer = MediaPlayer.create(mActivity, R.raw.ringtone);
//
//        if (!mediaPlayer.isPlaying()) {
//            Log.e("InfowareLab.Debug", "PublicWay.mediaPlayer.start()");
//            mediaPlayer.setLooping(true);
//            //mediaPlayer.prepare();
//            mediaPlayer.start();
//        }
//
//        mediaPlayer = new MediaPlayer();
//
//        Uri myUri = Uri.parse("android.resource://" + PublicWay.socketBinder.getService().getPackageName() + "/" + R.raw.ringtone);
//        mediaPlayer.setDataSource(PublicWay.socketBinder.getService(), myUri);

//        AssetFileDescriptor afd = PublicWay.socketBinder.getService().getResources().openRawResourceFd(R.raw.ringtone);
//        try {
//            mediaPlayer.setDataSource(afd.getFileDescriptor());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        afd.close();

        /*
        mediaPlayer.prepare();
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
            }
        });*/
    }

    /**
     * 重置播放器
     */
    public static void stopRing() {
//        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
//            Log.e("InfowareLab.Debug", "PublicWay.mediaPlayer.release()");
//            mediaPlayer.pause();
//            mediaPlayer.stop();
//            mediaPlayer.release();
//            mediaPlayer = null;
//        }

        if (!mRinging) return;

        Log.e("InfowareLab.Debug", ">>>stopRing: " + mNotifyCount);

        if (mTask != null){
            mTask.cancel();
            mTask = null;
        }

        if (mTimer != null) {
            mTimer.purge();
            mTimer.cancel();
            mTimer = null;
        }

        ringtone.stop();
        mRinging = false;

        PublicWay.socketBinder.getService().resetNotifyInvited();

        if (dialog != null) {
            Log.d("InfowareLab.Debug", "showNotifyDialog: Dialog is showing!!!");
            dialog.dismiss();
            dialog = null;
        }
    }

    public static void showDialog(String title, String content) {

        if (mRinging)
        {
            Log.d("InfowareLab.Debug", "showDialog: already ringing and ignored!");
            return;
        }

        if (!CallbackManager.IS_LEAVED)
        {
            Log.d("InfowareLab.Debug", "showDialog: already in conference!");
            return;
        }

        Log.d("InfowareLab.Debug", ">>>showDialog");

        mTitle = title;
        mContent = content;

        if (!CommonUtil.appOnForeground(socketBinder.getService()))
            CommonUtil.moveToForeground(socketBinder.getService(), LogoActivity.class);

//        if (CommonUtil.appOnForeground(socketBinder.getService()))
//        {
//            Log.d("InfowareLab.Debug", ">>>App On Foreground: showDialog");
////
//////            Intent i = new Intent(socketBinder.getService(), ActCustomDialog.class);
//////            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_BROUGHT_TO_FRONT);
//////            i.putExtra("title", title);
//////            i.putExtra("content", content);
////
////            showNotifyDialog(title, content);
//        }

        showNotifyDialog(title, content);

        try {
            startRing(title, content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //socketBinder.getService().notifyInvited();

//        mNotificationUtils = new NotificationUtils(mActivity);
//        mNotificationUtils.clearAllNotifiication();
//        mNotificationUtils.sendNotificationFullScreen(title, content, "InfowareLab Conference", true);


//
//        if (mFullscreenIntent != null)
//        {
//            mFullscreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_BROUGHT_TO_FRONT);
//            mFullscreenIntent.putExtra("title", title);
//            mFullscreenIntent.putExtra("content", content);
//            mActivity.startActivity(mFullscreenIntent);
//        }

    }

    private static boolean showNotifyDialog(String title, String content){

        if (dialog != null) {
            Log.d("InfowareLab.Debug", "showNotifyDialog: Dialog is showing!!!");
            dialog.dismiss();
            dialog = null;
        }

         //if (dialog == null) {
        dialog = new CustomDialog(mActivity, 0);
        //dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        //dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR);
        //dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
        dialog.setTitle(title);
        dialog.setContent(content);
        dialog.setOnResultListener(new CustomDialog.OnResultListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void doYes() {
                // TODO Auto-generated method stub
                afterShowDialog(true);
            }
            @Override
            public void doNo() {
                // TODO Auto-generated method stub
                afterShowDialog(false);
                XMLUtils.CONFIGID = "";
                XMLUtils.CONFIGNAME = "";
                XMLUtils.CONFERENCEPATTERN = 1;
                isInform = true;
                SharedPreferencesUrls.getInstance().putString("configId","");
            }
        });
        if (!dialog.isShowing()) {
            dialog.show();
        }
       // }

        if (!dialog.isShowing())
        {
            Log.d("InfowareLab.Debug", "ERROR: CANNOT SHOW DIALOG!!!");
            dialog.dismiss();
            dialog = null;
            return false;
        }
        else {
            Log.d("InfowareLab.Debug", "ERROR: SHOW DIALOG!!!");
            return true;
        }

//        else {
//            dialog.setTitle(title);
//            dialog.setContent(content);
//            if (!dialog.isShowing()){
//                dialog.show();
//            }
//        }
    }

    public static void showNotifyDialog(){

        if (mTitle.length() <= 0 || mContent.length() <= 0)
        {
            Log.d("InfowareLab.Debug", "showNotifyDialog: Empty content");
            return;
        }

//        if (dialog != null && dialog.isShowing()) {
//            Log.d("InfowareLab.Debug", "showNotifyDialog: Dialog is showing!!!");
//            return;
//        }

        if (dialog != null)
        {
            Log.d("InfowareLab.Debug", "showNotifyDialog: Dialog is dismiss for invisible!!!");
            dialog.dismiss();
            dialog = null;
        }

        dialog = new CustomDialog(mActivity, 0);
        //dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        //dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR);
        //dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
        dialog.setTitle(mTitle);
        dialog.setContent(mContent);

        dialog.setOnResultListener(new CustomDialog.OnResultListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void doYes() {
                // TODO Auto-generated method stub
                afterShowDialog(true);
            }
            @Override
            public void doNo() {
                // TODO Auto-generated method stub
                afterShowDialog(false);
                XMLUtils.CONFIGID = "";
                XMLUtils.CONFIGNAME = "";
                XMLUtils.CONFERENCEPATTERN = 1;
                isInform = true;
                SharedPreferencesUrls.getInstance().putString("configId","");
            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    private static LoginBean getLoginBean(String pwd) {
        LoginBean loginBean = new LoginBean();
        String showName = FileUtil.readSharedPreferences(mActivity,
                Constants.SHARED_PREFERENCES, Constants.LOGIN_JOINNAME);
        SharedPreferences preferences = mActivity.getSharedPreferences(
                Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        int uid = preferences.getInt(Constants.USER_ID, 0);
        loginBean = new LoginBean(confBean.getId(), showName, pwd);
        loginBean.setType(confBean.getType());
        loginBean.setUid(uid);
        return loginBean;
    }

    public static void requestTerminalList(Context context) {
        mContext = context;
        Log.d("InfowareLab.Debug", "requestTerminalList: " + PublicWay.IP + ":" + PublicWay.PORT);

        if (asyncSocket != null && asyncSocket.isOpen()) {
            //Log.d("InfowareLab.Debug","socket is already opened.");
            sendTerminalListRequest(asyncSocket);
        } else {

            AsyncServer.getDefault().connectSocket(new InetSocketAddress(PublicWay.IP, PublicWay.PORT), new ConnectCallback() {
                @Override
                public void onConnectCompleted(Exception ex, final AsyncSocket socket) {

                    if (ex != null)
                        Log.e("InfowareLab.Debug", "onConnectCompleted error: " + ex.getMessage());

                    if (socket != null) {
                        asyncSocket = socket;
                        sendTerminalListRequest(socket);
                    }
                }
            });
        }

    }

    private static void sendOnlineMessage(AsyncSocket socket) {

        //Log.d("InfowareLab.Debug","sendOnlineMessage" );

        if (mContext == null) return;

        String RequestID = "" + System.currentTimeMillis();
        String deviceId = DeviceIdFactory.getUUID1(mContext);
        String siteId = Config.SiteId;

        String requestCmd = XMLUtils.getPingXML(RequestID, 0, mJoinName, deviceId, siteId, PublicWay.longitude, PublicWay.latitude);
        //Log.d("InfowareLab.Debug","[Client] sendOnlineMessage = " + requestCmd);

        Util.writeAll(socket, requestCmd.getBytes(), mCompletedCallback);

        socket.setDataCallback(mDataCallback);

        socket.setClosedCallback(mCloseCompletedCallback);

        socket.setEndCallback(mEndCompletedCallback);
    }


    private static void sendTerminalListRequest(AsyncSocket socket) {

        Log.d("InfowareLab.Debug", "sendTerminalListRequest");

        if (mContext == null) return;

        String RequestID = "" + System.currentTimeMillis();
        String deviceId = DeviceIdFactory.getUUID1(mContext);
        String siteId = Config.SiteId;

        String requestCmd = XMLUtils.getXml_list(RequestID, deviceId, siteId);
        Log.d("InfowareLab.Debug", "[Client] requestCmd = " + requestCmd);

        Util.writeAll(socket, requestCmd.getBytes(), new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                if (ex != null) throw new RuntimeException(ex);
                Log.d("InfowareLab.Debug", "[Client] Successfully send get-terminal-list request");
            }
        });

        socket.setDataCallback(mDataCallback);

        socket.setClosedCallback(mCloseCompletedCallback);

        socket.setEndCallback(mEndCompletedCallback);
    }

    public static void sendOnlineMessage(String joinName) {

        //Log.d("InfowareLab.Debug","sendOnlineMessage: " + PublicWay.IP + ":" + PublicWay.PORT );

        mJoinName = joinName;

        if (asyncSocket != null && asyncSocket.isOpen()) {
            //Log.d("InfowareLab.Debug","socket is already opened.");
            sendOnlineMessage(asyncSocket);
        } else {

            AsyncServer.getDefault().connectSocket(new InetSocketAddress(PublicWay.IP, PublicWay.PORT), new ConnectCallback() {
                @Override
                public void onConnectCompleted(Exception ex, final AsyncSocket socket) {

                    if (ex != null)
                        Log.e("InfowareLab.Debug", "handleConnectCompleted error: " + ex.getMessage());

                    if (socket != null) {
                        asyncSocket = socket;
                        sendOnlineMessage(socket);
                    }
                }
            });
        }
    }

    public static void sendInviteMessage(List<TerminalsListBean> terminateList, String confId, String joinName, Context context) {
        Log.d("InfowareLab.Debug", "sendInviteMessage: " + PublicWay.IP + ":" + PublicWay.PORT);

        if (terminateList.size() <= 0) return;

        String RequestID = "" + System.currentTimeMillis();
        mSendCmd = XMLUtils.getInvite(context, RequestID, confId, joinName, Config.SiteId, terminateList);

        if (asyncSocket != null && asyncSocket.isOpen()) {
            //Log.d("InfowareLab.Debug","socket is already opened.");
            sendInviteMessage(asyncSocket);
        } else {

            AsyncServer.getDefault().connectSocket(new InetSocketAddress(PublicWay.IP, PublicWay.PORT), new ConnectCallback() {
                @Override
                public void onConnectCompleted(Exception ex, final AsyncSocket socket) {

                    if (ex != null)
                        Log.e("InfowareLab.Debug", "sendInviteMessage.onConnectCompleted error: " + ex.getMessage());

                    if (socket != null) {
                        asyncSocket = socket;
                        sendInviteMessage(socket);
                    }
                }
            });
        }
    }

    private static void sendInviteMessage(AsyncSocket socket) {

        Log.d("InfowareLab.Debug", "[Client] sendInviteMessage = " + mSendCmd);

        Util.writeAll(socket, mSendCmd.getBytes(), new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                if (ex != null) throw new RuntimeException(ex);
                Log.d("InfowareLab.Debug", "[Client] Successfully send invite message");
            }
        });

        socket.setDataCallback(mDataCallback);

        socket.setClosedCallback(mCloseCompletedCallback);

        socket.setEndCallback(mEndCompletedCallback);
    }

    public static void setConfActivity(Activity confActivity) { mConfActivity = confActivity; }
}