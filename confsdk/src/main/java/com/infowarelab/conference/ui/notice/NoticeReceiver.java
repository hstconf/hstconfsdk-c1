package com.infowarelab.conference.ui.notice;


////import org.apache.log4j.Logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.ConferenceCommonImpl;
import com.infowarelabsdk.conference.confctrl.ConferenceCommon;
import com.infowarelabsdk.conference.util.Constants;

/**
 * @author Jack.Yan@infowarelab.com
 * @description This class used to receive the push message and do the detail action
 * @date 2012-11-9
 */
public class NoticeReceiver extends BroadcastReceiver {
    //private final Logger log = Logger.getLogger(getClass());
    private ConferenceCommonImpl conferenceCommon;
    public static final int SHOW_TOAST = 1000;

    @Override
    public void onReceive(Context context, Intent intent) {
        //log.info("onReceive...");
        NoticeManager noticeManager = NoticeManager.getInstance(context);
        ReceiverMessage receiverMessage = (ReceiverMessage) intent.getSerializableExtra(Constants.TAG_NOTIFICATION);
        int confPushType = receiverMessage.getConfPushType();
        conferenceCommon = (ConferenceCommonImpl) CommonFactory.getInstance().getConferenceCommon();
        if (conferenceCommon.getJoinStatus() == ConferenceCommon.JOINING || conferenceCommon.getJoinStatus() == ConferenceCommon.JOINED) {
            Handler h = ((ConferenceCommonImpl) CommonFactory.getInstance().getConferenceCommon()).getHandler();
            Message msg = new Message();
            msg.what = SHOW_TOAST;
            msg.obj = receiverMessage.getConfMessage();
            h.sendMessage(msg);
        } else {
            switch (confPushType) {
                case NotificationIQ.PUSHTYPE_CREATE_CONF:
                    noticeManager.doNotification(context, intent);
                    //log.info(NotificationIQ.PUSHTYPE_CREATE_CONF+"push create conf");
                    break;
                case NotificationIQ.PUSHTYPE_MODIFY_CONF:
                    noticeManager.doNotification(context, intent);
                    //log.info(NotificationIQ.PUSHTYPE_MODIFY_CONF+"modify conf");
                    break;
                case NotificationIQ.PUSHTYPE_JOIN_REMIND:
                    noticeManager.doNotification(context, intent);
                    //log.info(NotificationIQ.PUSHTYPE_JOIN_REMIND);
                    break;
                case NotificationIQ.PUSHTYPE_CONF_CANEL:
                    noticeManager.doNotification(context, intent);
                    //log.info(NotificationIQ.PUSHTYPE_CONF_CANEL+"conf canel");
                    break;
                case NotificationIQ.PUSHTYPE_CONF_START:
                    noticeManager.doDialog(context, intent);
                    //log.info(NotificationIQ.PUSHTYPE_CONF_START+"conf start");
                    break;
                case NotificationIQ.PUSHTYPE_CONF_END:
                    noticeManager.doNotification(context, intent);
                    break;
                default:
                    //log.error("Unknown conference push type:"+confPushType);
                    break;
            }
        }
    }
}
