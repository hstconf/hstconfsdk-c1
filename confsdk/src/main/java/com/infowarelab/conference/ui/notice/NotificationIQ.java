package com.infowarelab.conference.ui.notice;

import java.io.Serializable;

import org.jivesoftware.smack.packet.IQ;

/**
 * @author Jack.Yan@infowarelab.com
 * @description This class represents a notification IQ packet.
 * @date 2012-11-23
 */
public class NotificationIQ extends IQ implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String ROOT = "notification";
    public static final String NAMESPACE = "androidpn:iq:notification";

    public static final String NODE_CONFKEY = "confKey";
    public static final String NODE_CONFPASSWORD = "confPassword";
    public static final String NODE_CONFMESSAGE = "confMessage";
    public static final String NODE_CONFPUSHTYPE = "confPushType";
    public static final int PUSHTYPE_Notification = 0;
    public static final int PUSHTYPE_Dialog = 1;


    /**
     * Conference push Type
     * 1 创建会议  2：修改会议 3：加会提醒  4：会议取消
     * 5：会议已开始 6：会议结束
     */
    public static final int PUSHTYPE_CREATE_CONF = 1;
    public static final int PUSHTYPE_MODIFY_CONF = 2;
    public static final int PUSHTYPE_JOIN_REMIND = 3;
    public static final int PUSHTYPE_CONF_CANEL = 4;
    public static final int PUSHTYPE_CONF_START = 5;
    public static final int PUSHTYPE_CONF_END = 6;

    /**
     * Conference key
     */
    private String confKey;
    /**
     * Conference password
     */
    private String confPassword;
    /**
     * Conference message
     */
    private String confMessage;
    /**
     * Conference push type 0:notification,1:dialog
     */
    private int confPushType = 0;

    @Override
    public String getChildElementXML() {
        StringBuilder buf = new StringBuilder();
        buf.append("<").append(ROOT).append(" xmlns=\"").append(
                NAMESPACE).append("\">");
        if (confKey != null) {
            buf.append(getNode(NODE_CONFKEY, confKey));
        }
        if (confPassword != null) {
            buf.append(getNode(NODE_CONFPASSWORD, confPassword));
        }
        if (confMessage != null) {
            buf.append(getNode(NODE_CONFMESSAGE, confMessage));
        }
        if (confPushType != -1) {
            buf.append(getNode(NODE_CONFPUSHTYPE, "" + confPushType));
        }
        buf.append("</").append(ROOT).append("> ");
        return buf.toString();
    }

    private String getNode(String node, String value) {
        StringBuffer sb = new StringBuffer();
        sb.append("<").append(node).append(">")
                .append(value)
                .append("</").append(node).append(">");
        return sb.toString();
    }

    public String getConfKey() {
        return confKey;
    }

    public void setConfKey(String confKey) {
        this.confKey = confKey;
    }

    public String getConfPassword() {
        return confPassword;
    }

    public void setConfPassword(String confPassword) {
        this.confPassword = confPassword;
    }

    public String getConfMessage() {
        return confMessage;
    }

    public void setConfMessage(String confMessage) {
        this.confMessage = confMessage;
    }

    public int getConfPushType() {
        return confPushType;
    }

    public void setConfPushType(int confPushType) {
        this.confPushType = confPushType;
    }
}
