package com.infowarelab.conference.ui.notice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

////import org.apache.log4j.Logger;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.provider.ProviderManager;

import android.content.Context;
import android.os.Handler;

import com.infowarelabsdk.conference.util.Constants;
import com.infowarelabsdk.conference.util.FileUtil;

/**
 * @author Jack.Yan@infowarelab.com
 * @description This class is to manage the XMPP connection between client and server.
 * @date 2012-11-26
 */
public class XmppManager {

    //private final Logger log = Logger.getLogger(getClass());

    private Context context;

    private NotificationService.TaskSubmitter taskSubmitter;

    private NotificationService.TaskTracker taskTracker;


    private String xmppHost;

    private int xmppPort;

    private XMPPConnection connection;

    private String username;

    private String password;

    private String mark;

    private ConnectionListener connectionListener;

    private PacketListener notificationPacketListener;

    private Handler handler;

    private List<Runnable> taskList;

    private boolean running = false;

    public boolean isNetAvailable = true;

    private Future<?> futureTask;

    private Thread reconnection;

    public XmppManager(NotificationService notificationService) {
        context = notificationService;
        taskSubmitter = notificationService.getTaskSubmitter();
        taskTracker = notificationService.getTaskTracker();

        connectionListener = new PersistentConnectionListener(this);
        notificationPacketListener = new NotificationPacketListener(this);

        handler = new Handler();
        taskList = new ArrayList<Runnable>();
        reconnection = new ReconnectionThread(this);
    }

    public Context getContext() {
        return context;
    }

    public void connect() {
        xmppHost = FileUtil.readSharedPreferences(context, Constants.SHARED_PREFERENCES, Constants.XMPP_HOST);
        if (xmppHost.contains(":")) {
            xmppHost = xmppHost.substring(0, xmppHost.lastIndexOf(":"));
        }
        try {
            xmppPort = Integer.parseInt(FileUtil.readSharedPreferences(context, Constants.SHARED_PREFERENCES, Constants.XMPP_PORT));
        } catch (Exception e) {
            xmppPort = 5222;
        }
        mark = FileUtil.readSharedPreferences(context, Constants.SHARED_PREFERENCES, Constants.XMPP_MARK);
        username = FileUtil.readSharedPreferences(context, Constants.SHARED_PREFERENCES, Constants.LOGIN_NAME);
        //TODO add site id as the only one key
        username = username + "_" + FileUtil.readSharedPreferences(context, Constants.SHARED_PREFERENCES, Constants.SITE_ID);
        password = FileUtil.readSharedPreferences(context, Constants.SHARED_PREFERENCES, Constants.LOGIN_PASS);
        if (!username.trim().equals("") || !password.trim().equals("")) {
            submitConnectTask();
            submitLoginTask();
        } else {
            //log.info("username or password is null");
            if (reconnection.isAlive()) {
                reconnection.interrupt();
                //log.info("Reconnection has been interrupted");
            }
        }
    }

    public void disconnect() {
        //log.info("disconnect()...");
        taskList.clear();
        taskTracker.count = 0;
        addTask(new TerminatePersistent());
        runTask();
    }

    private class TerminatePersistent implements Runnable {
        final XmppManager xmppManager;

        private TerminatePersistent() {
            this.xmppManager = XmppManager.this;
        }

        @Override
        public void run() {
            //log.info("terminatePersistentConnection()... run()");
            if (xmppManager.isConnected()) {
                xmppManager.getConnection().removePacketListener(
                        xmppManager.getNotificationPacketListener());
                xmppManager.getConnection().disconnect(new Presence(Type.unavailable));
                //log.info("disconnected");
            } else {
                //log.info("connection is already disconnected");
                if (reconnection.isAlive()) {
                    reconnection.interrupt();
                    //log.info("Reconnection has been interrupted");
                }
            }
        }
    }

    public XMPPConnection getConnection() {
        return connection;
    }

    public void setConnection(XMPPConnection connection) {
        this.connection = connection;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public ConnectionListener getConnectionListener() {
        return connectionListener;
    }

    public PacketListener getNotificationPacketListener() {
        return notificationPacketListener;
    }

    public void startReconnectionThread() {
        synchronized (reconnection) {
            if (!reconnection.isAlive() && isNetAvailable) {
                reconnection.setName("Xmpp Reconnection Thread");
                reconnection.start();
            } else if (!isNetAvailable && reconnection.isAlive()) {
                reconnection.interrupt();
                taskList.isEmpty();
                taskTracker.count = 0;
            }
        }
    }

    public boolean isNetAvailable() {
        return isNetAvailable;
    }

    public void setNetAvailable(boolean isNetAvailable) {
        this.isNetAvailable = isNetAvailable;
    }

    public Handler getHandler() {
        return handler;
    }

    public List<Runnable> getTaskList() {
        return taskList;
    }

    public Future<?> getFutureTask() {
        return futureTask;
    }

    public void runTask() {
        //log.info("runTask()...");
        synchronized (taskList) {
            running = false;
            futureTask = null;
            if (!taskList.isEmpty()) {
                Runnable runnable = (Runnable) taskList.get(0);
                taskList.remove(0);
                running = true;
                futureTask = taskSubmitter.submit(runnable);
                if (futureTask == null) {
                    taskTracker.decrease();
                    //log.info("runTask()...done");
                }
            }
        }
        //log.info("runTask()...done");
    }

    private boolean isConnected() {
        return connection != null && connection.isConnected();
    }

    private boolean isAuthenticated() {
        return connection != null && connection.isConnected()
                && connection.isAuthenticated();
    }


    private void submitConnectTask() {
        //log.info("submitConnectTask()...");
        addTask(new ConnectTask());
    }

    private void submitLoginTask() {
        //log.info("submitLoginTask()...");
        addTask(new LoginTask());
    }

    private void addTask(Runnable runnable) {
        //log.info("addTask(runnable)...");
        taskTracker.increase();
        synchronized (taskList) {
            if (taskList.isEmpty() && !running) {
                running = true;
                futureTask = taskSubmitter.submit(runnable);
                if (futureTask == null) {
                    taskTracker.decrease();
                }
            } else {
                runTask();//fix the server restarted and client can not login error
                taskList.add(runnable);
            }
        }
        //log.info("addTask(runnable)... done");
    }

    /**
     * A runnable task to connect the server.
     */
    private class ConnectTask implements Runnable {

        final XmppManager xmppManager;

        private ConnectTask() {
            this.xmppManager = XmppManager.this;
        }

        public void run() {
            //log.info("ConnectTask.run()...");

            if (!xmppManager.isConnected()) {
                // Create the configuration for this new connection
                ConnectionConfiguration connConfig = new ConnectionConfiguration(
                        xmppHost, xmppPort);
                // connConfig.setSecurityMode(SecurityMode.disabled);
                connConfig.setSecurityMode(SecurityMode.required);
                connConfig.setSASLAuthenticationEnabled(false);
                connConfig.setCompressionEnabled(false);

                XMPPConnection connection = new XMPPConnection(connConfig);
                xmppManager.setConnection(connection);

                try {
                    // Connect to the server
                    connection.connect();
                    //log.info("XMPP connected successfully");

                    // packet provider
                    ProviderManager.getInstance().addIQProvider(NotificationIQ.ROOT,
                            NotificationIQ.NAMESPACE,
                            new NotificationIQProvider());

                } catch (XMPPException e) {
                    //log.error("XMPP connection failed");
                    String errorMessage = e.getMessage();
                    String NetworkIsUnreachable = "Network is unreachable";
                    if (errorMessage != null && errorMessage.contains(NetworkIsUnreachable)) {
                        xmppManager.isNetAvailable = false;
                        //log.error(NetworkIsUnreachable);
                        return;
                    }
                }
            } else {
                //log.info("XMPP connected already");
                if (reconnection.isAlive()) {
                    reconnection.interrupt();
                    //log.info("Reconnection has been interrupted");
                }
            }
            xmppManager.runTask();
        }
    }

    /**
     * A runnable task to log into the server.
     */
    private class LoginTask implements Runnable {

        final XmppManager xmppManager;

        private LoginTask() {
            this.xmppManager = XmppManager.this;
        }

        public void run() {
            //log.info("LoginTask.run()...");
            //log.info("userName="+xmppManager.getUsername()+", password="+xmppManager.getPassword());
            if ("".equals(xmppManager.getPassword()) || xmppManager.getPassword() == null) {
                //log.info("password is empty!");
                if (reconnection.isAlive()) {
                    reconnection.interrupt();
                    //log.info("Reconnection has been interrupted");
                }
                xmppManager.runTask();
                return;
            }
            if (!xmppManager.isAuthenticated()) {
                try {
                    xmppManager.getConnection().login(
                            xmppManager.getUsername(),
                            xmppManager.getPassword(),
                            xmppManager.getMark());
                    //log.info("Loggedn in successfully");

                    // connection listener
                    if (xmppManager.getConnectionListener() != null) {
                        xmppManager.getConnection().addConnectionListener(
                                xmppManager.getConnectionListener());
                    }

                    // packet filter
                    PacketFilter packetFilter = new PacketTypeFilter(
                            NotificationIQ.class);
                    // packet listener
                    PacketListener packetListener = xmppManager
                            .getNotificationPacketListener();
                    connection.addPacketListener(packetListener, null);
                } catch (XMPPException e) {
                    //log.error("LoginTask.run()... xmpp error");
                    //log.error("Failed to login to xmpp server.");
                    String INVALID_CREDENTIALS_ERROR_CODE = "401";
                    String errorMessage = e.getMessage();
                    if (errorMessage != null
                            && errorMessage
                            .contains(INVALID_CREDENTIALS_ERROR_CODE)) {
                        //log.error("Please check and make sure your username and passwrod are correct");
                        return;
                    }
                    //log.info("start re-connection thread");
                    xmppManager.startReconnectionThread();

                } catch (Exception e) {
                    //log.error("LoginTask.run()... other error: Failed to login to xmpp server");
                    //log.info("start re-connection thread");
                    xmppManager.startReconnectionThread();
                }

            } else {
                //log.info("Logged in already");
                if (reconnection.isAlive()) {
                    reconnection.interrupt();
                    //log.info("Reconnection has been interrupted");
                }
            }
            xmppManager.runTask();
        }
    }

}
