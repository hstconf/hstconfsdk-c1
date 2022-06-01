package com.infowarelab.conference.localDataCommon.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.infowarelab.conference.localDataCommon.ContactDataCommon;
import com.infowarelabsdk.conference.domain.ContactBean;
import com.infowarelabsdk.conference.transfer.Config;
import com.infowarelabsdk.conference.util.ContactsUtil;

public class ContactDataCommonImpl implements ContactDataCommon {
    private ArrayList<ContactBean> contactList;
    private LinkedHashMap<Object, ContactBean> map;
    private ContactBean contactDel;
    public static int preStartIndex = -1;
    public static int preEndIndex = -1;
    public static boolean isFinishGetContacts;
    protected Handler handler;

    public ContactDataCommonImpl() {
        if (contactList == null) {
            contactList = new ArrayList<ContactBean>();
            map = new LinkedHashMap<Object, ContactBean>();
        }
    }

    public Handler getHandler() {
        return handler;
    }

    @Override
    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void getContacts(Context context) {
        if (contactList.isEmpty()) {
            contactList = ContactsUtil.getContacts(context);
            isFinishGetContacts = true;
            if (getHandler() != null) {
                getHandler().sendEmptyMessage(ContactDataCommon.GET_CONTACTSLIST);
            }
        }
    }

    @Override
    public void getRecentContacts(Context context) {
        Message msg = getHandler().obtainMessage(ContactDataCommon.GET_RECENTCONTACTS, Config.getRecentContacts(context));
        getHandler().sendMessage(msg);
    }

    @Override
    public void initLocalCamera() {
        getHandler().sendEmptyMessage(1002);
    }

    @Override
    public void sendLocalCameraImage(int[] rgb, int width, int height) {
        Message msg = new Message();
        msg.what = 1001;
        msg.obj = rgb;
        msg.arg1 = width;
        msg.arg2 = height;
        getHandler().sendMessage(msg);
    }

    public ArrayList<ContactBean> getContactList() {
        return contactList;
    }

    public void setContactList(ArrayList<ContactBean> contactList) {
        this.contactList = contactList;
    }

    public LinkedHashMap<Object, ContactBean> getMap() {
        return map;
    }

    public void setMap(LinkedHashMap<Object, ContactBean> map) {
        this.map = map;
    }

    public ContactBean getContactDel() {
        return contactDel;
    }

    public void setContactDel(ContactBean contactDel) {
        this.contactDel = contactDel;
    }


}
