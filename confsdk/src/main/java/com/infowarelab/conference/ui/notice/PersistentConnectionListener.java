package com.infowarelab.conference.ui.notice;

////import org.apache.log4j.Logger;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;

/**
 * @author Jack.Yan@infowarelab.com
 * @description A listener class for monitoring connection closing and reconnection events.
 * @date 2012-11-26
 */
public class PersistentConnectionListener implements ConnectionListener {

    //private final Logger log = Logger.getLogger(getClass());

    private final XmppManager xmppManager;

    public PersistentConnectionListener(XmppManager xmppManager) {
        this.xmppManager = xmppManager;
    }

    @Override
    public void connectionClosed() {
        //log.info("connectionClosed()...");
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        //log.info("connectionClosedOnError()...");
        if (xmppManager.getConnection() != null
                && xmppManager.getConnection().isConnected()) {
            xmppManager.getConnection().disconnect(new Presence(Type.unavailable));
            //log.info("Connection is closed");
        }
        xmppManager.startReconnectionThread();
    }

    @Override
    public void reconnectingIn(int seconds) {
        //log.info("reconnectingIn()...");
    }

    @Override
    public void reconnectionFailed(Exception e) {
        //log.info("reconnectionFailed()...");
    }

    @Override
    public void reconnectionSuccessful() {
        //log.info("reconnectionSuccessful()...");
    }

}
