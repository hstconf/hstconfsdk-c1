package com.infowarelab.conference.ui.notice;

////import org.apache.log4j.Logger;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import android.content.Intent;

import com.google.gson.Gson;
import com.infowarelabsdk.conference.util.Constants;

/**
 * @author Jack.Yan@infowarelab.com
 * @description This class notifies the receiver of incoming notification packets asynchronously.
 * @date 2012-11-26
 */
public class NotificationPacketListener implements PacketListener {

    //private final Logger log = Logger.getLogger(getClass());

    private final XmppManager xmppManager;

    public NotificationPacketListener(XmppManager xmppManager) {
        this.xmppManager = xmppManager;
    }

    @Override
    public void processPacket(Packet packet) {
        //log.info("NotificationPacketListener.processPacket()...");
        //log.info("packet.toXML()=" + packet.toXML());
        if (packet instanceof Message) {
            Message message = (Message) packet;
            //log.info("NotificationPacketListener"+message.getBody());
            ReceiverMessage receiver = buildMessage(message);
            //log.info("confID="+receiver.getConfId());
            //log.info("confPassword="+receiver.getConfPassword());
            //log.info("confMessage="+receiver.getConfMessage());
            //log.info("confKey="+receiver.getConfKey());
            //log.info("confPushType="+receiver.getConfPushType());
            Intent intent = new Intent(Constants.XMPP_PUSH_ACTION);
            intent.putExtra(Constants.TAG_NOTIFICATION, receiver);
            xmppManager.getContext().sendBroadcast(intent);
        }
    }

    public ReceiverMessage buildMessage(Message message) {
        Gson gson = new Gson();
        ReceiverMessage receiver = gson.fromJson(message.getBody(), ReceiverMessage.class);
        return receiver;
    }
}
