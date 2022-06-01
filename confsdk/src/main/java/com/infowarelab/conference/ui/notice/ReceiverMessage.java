package com.infowarelab.conference.ui.notice;

import java.io.Serializable;

public class ReceiverMessage implements Serializable {

    /**
     * Conference Id
     */
    private String confId;
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

    private int confPushType = 1;


    public int getConfPushType() {
        return confPushType;
    }

    public void setConfPushType(int confPushType) {
        this.confPushType = confPushType;
    }

    public String getConfId() {
        return confId;
    }

    public void setConfId(String confId) {
        this.confId = confId;
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


}
