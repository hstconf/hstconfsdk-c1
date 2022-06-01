package com.infowarelab.conference.localDataCommon;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import android.content.Context;
import android.os.Handler;

import com.infowarelabsdk.conference.domain.ContactBean;


public interface ContactDataCommon {

    public final static int GET_RECENTCONTACTS = 101;

    public final static int GET_CONTACTSLIST = 102;

    public final static int EMAIL_SUCCESS = 103;

    public final static int EMAIL_FAILED = 104;

    public void getContacts(Context context);

    public void getRecentContacts(Context context);

    public void initLocalCamera();

    public ArrayList<ContactBean> getContactList();

    public void setContactList(ArrayList<ContactBean> contactList);

    public void sendLocalCameraImage(int[] rgb, int width, int height);

    public LinkedHashMap<Object, ContactBean> getMap();

    public void setMap(LinkedHashMap<Object, ContactBean> map);

    public ContactBean getContactDel();

    public void setContactDel(ContactBean contactDel);

    /**
     * 设置UI Handler
     *
     * @param handler
     */
    public void setHandler(Handler handler);

}
