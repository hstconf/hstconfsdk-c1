package com.infowarelab.conference.ui.notice;

////import org.apache.log4j.Logger;

/**
 * @author Jack.Yan@infowarelab.com
 * @description A thread class for re-connecting to the server.
 * @date 2012-11-26
 */
public class ReconnectionThread extends Thread {

    //private final Logger log = Logger.getLogger(getClass());

    private final XmppManager xmppManager;

    private int waiting;

    ReconnectionThread(XmppManager xmppManager) {
        this.xmppManager = xmppManager;
        this.waiting = 0;
    }

    public void run() {
        try {
            while (!isInterrupted()) {
                //log.info("Trying to reconnect in " + waiting()
                //  + " seconds");
                Thread.sleep((long) waiting() * 1000L);
                xmppManager.connect();
                waiting++;
            }
        } catch (final InterruptedException e) {
            xmppManager.getHandler().post(new Runnable() {
                public void run() {
                    xmppManager.getConnectionListener().reconnectionFailed(e);
                }
            });
        }
    }

    private int waiting() {
        if (waiting > 20) {
            return 600;
        }
        if (waiting > 13) {
            return 300;
        }
        return waiting <= 7 ? 10 : 60;
    }
}
