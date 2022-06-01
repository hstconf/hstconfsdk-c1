package com.infowarelab.conference.ui.tools;


import com.infowarelabsdk.conference.domain.ContactBean;

import android.graphics.Bitmap;


public class GroupMemberBean extends ContactBean {
    /**
     * 显示数据拼音的首字母
     **/
    private String sortLetters;

    public GroupMemberBean() {
        super();
        this.email = "";
    }

    public GroupMemberBean(String name, String number) {
        super();
        this.name = name;
        this.phoneNumber = number;
        this.email = "";
    }

    public String getSortLetters() {
        return sortLetters;
    }

    public void setSortLetters(String sortLetters) {
        this.sortLetters = sortLetters;
    }

}
