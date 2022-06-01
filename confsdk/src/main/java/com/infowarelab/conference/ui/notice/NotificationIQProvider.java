package com.infowarelab.conference.ui.notice;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

import android.util.Log;

/**
 * @author Jack.Yan@infowarelab.com
 * @description This class parses incoming IQ packets to NotificationIQ objects.
 * @date 2012-11-23
 */
public class NotificationIQProvider implements IQProvider {
    private String tag = NotificationIQProvider.class.getSimpleName();

    public NotificationIQProvider() {
    }

    @Override
    public IQ parseIQ(XmlPullParser parser) throws Exception {

        NotificationIQ notification = new NotificationIQ();
        for (boolean done = false; !done; ) {
            int eventType = parser.next();
            if (eventType == 2) {
                if (NotificationIQ.NODE_CONFKEY.equals(parser.getName())) {
                    notification.setConfKey(parser.nextText());
                }
                if (NotificationIQ.NODE_CONFPASSWORD.equals(parser.getName())) {
                    notification.setConfPassword(parser.nextText());
                }
                if (NotificationIQ.NODE_CONFMESSAGE.equals(parser.getName())) {
                    notification.setConfMessage(parser.nextText());
                }
                if (NotificationIQ.NODE_CONFPUSHTYPE.equals(parser.getName())) {
                    int confPushType = 0;
                    try {
                        confPushType = Integer.parseInt(parser.nextText());
                    } catch (Exception e) {
                        Log.d(tag, "Integer.parseInt error,use default '0'");
                    }
                    notification.setConfPushType(confPushType);
                }
            } else if (eventType == 3
                    && NotificationIQ.ROOT.equals(parser.getName())) {
                done = true;
            }
        }

        return notification;
    }

}
