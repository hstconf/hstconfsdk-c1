package com.infowarelab.conference.model;

import java.io.Serializable;

/**
 * Created by xiaor on 2019/12/25.
 */

public class TerminalsListBean implements Serializable{

    private String ID;
    private String Name;
    private String InnerSvrIP;
    private String InnerSvrPort;
    private String longitude;
    private String latitude;
    private boolean Online = false;
    //是否选中
    private boolean isSelected = false;
    //是否聚焦
    private boolean isFouse = false;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getInnerSvrIP() {
        return InnerSvrIP;
    }

    public void setInnerSvrIP(String innerSvrIP) {
        InnerSvrIP = innerSvrIP;
    }

    public String getInnerSvrPort() {
        return InnerSvrPort;
    }

    public void setInnerSvrPort(String innerSvrPort) {
        InnerSvrPort = innerSvrPort;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public boolean isOnline() {
        return Online;
    }

    public void setOnline(boolean online) {
        Online = online;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
    public boolean isFouse() {
        return isFouse;
    }

    public void setFouse(boolean fouse) {
        isFouse = fouse;
    }

    @Override
    public String toString() {
        return "TerminalsListBean{" +
                "ID='" + ID + '\'' +
                ", Name='" + Name + '\'' +
                ", InnerSvrIP='" + InnerSvrIP + '\'' +
                ", InnerSvrPort='" + InnerSvrPort + '\'' +
                ", longitude='" + longitude + '\'' +
                ", latitude='" + latitude + '\'' +
                ", Online=" + Online +
                '}';
    }
}
