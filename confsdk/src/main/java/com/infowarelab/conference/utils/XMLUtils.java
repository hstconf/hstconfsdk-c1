package com.infowarelab.conference.utils;

import android.content.Context;
import android.text.TextUtils;

import com.infowarelab.conference.model.TerminalsListBean;

import java.util.List;

/**
 * Created by xiaor on 2019/12/24.
 */

public class XMLUtils {

    //呼叫id
    public static String CONFIGID = "";
    public static String CONFIGNAME = "";
    public static int CONFERENCEPATTERN = 0;
    public static String inviteID = "";

    public static String getPingXML(String RequestID, int userID, String name, String deviceId, String siteId, double longitude, double latitude) {
        if (!TextUtils.isEmpty(deviceId) && !"0".equals(deviceId)) {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" +
                    "<Pack>" +
                    "<Action>ping</Action>" +
                    "<RequestID>" + RequestID + "</RequestID>" +
                    "<TerminalName>" + name + "</TerminalName>" +
                    "<TerminalID>" + deviceId + "</TerminalID>" +
                    "<TerminalLongitude>" + longitude + "</TerminalLongitude>" +
                    "<TerminalLatitude>" + latitude + "</TerminalLatitude>" +
                    "<SiteID>" + siteId + "</SiteID>" +
                    "<UserID>" + userID + "</UserID>" +
                    "</Pack>";
        }
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" +
                "<Pack>" +
                "<Action>ping</Action>" +
                "<RequestID>" + RequestID + "</RequestID>" +
                "<TerminalName>" + name + "</TerminalName>" +
//                "<TerminalID>" + deviceId + "</TerminalID>" +
                "<TerminalLongitude>" + longitude + "</TerminalLongitude>" +
                "<TerminalLatitude>" + latitude + "</TerminalLatitude>" +
                "<SiteID>" + siteId + "</SiteID>" +
                "</Pack>";
    }

    public static String getXml_list(String RequestID, String deviceId, String siteId) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" +
                "<Pack>" +
                "<Action>get_terminals</Action>" +
                "<RequestID>" + RequestID + "</RequestID>" +
                "<From>" + deviceId + "</From>" +
                "<SiteID>" + siteId + "</SiteID>" +
                "</Pack>";
    }

    /**
     * 邀请的命令
     */
    public static String getInvite(Context context, String RequestID, String confID, String joinName, String siteId, List<TerminalsListBean> datas) {
        //String name = FileUtil.readSharedPreferences(context, com.infowarelabsdk.conference.util.Constants.SHARED_PREFERENCES,
        //        com.infowarelabsdk.conference.util.Constants.LOGIN_JOINNAME);
        //if (TextUtils.isEmpty(name)){
        //    name = "NO_NAME";
        //}
        String invite = "";
        String self = "<Terminal>" +
                "<Name>" + joinName + "</Name>" +
                "<ID>" + DeviceIdFactory.getUUID1(context) + "</ID>" +
                "</Terminal>";
        for (TerminalsListBean bean : datas) {
            invite = invite + "<Terminal>" +
                    "<Name>" + bean.getName() + "</Name>" +
                    "<ID>" + bean.getID() + "</ID>" +
                    "</Terminal>";
        }
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" +
                "<Pack>" +
                "<Action>invite</Action>" +
                "<RequestID>" + RequestID + "</RequestID>" +
                "<From>" + self + "</From>" +
                "<To>" + invite.trim() + "</To>" +
                "<Result>holdon</Result>" +
                "<ConfID>" + confID + "</ConfID>" +
                "<SiteID>" + siteId + "</SiteID>" +
                "<InviteID>" + DeviceIdFactory.getUUID() + "</InviteID>" +
                "</Pack>";
    }

    //通知邀请服务器
    public static String getMsg(String RequestID, String selfName, String selfID, String confID, String siteId,
                                String inviteID) {
        String self = "<Terminal>" +
                "<Name>" + selfName + "</Name>" +
                "<ID>" + selfID + "</ID>" +
                "</Terminal>";
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" +
                "<Pack>" +
                "<Action>invite_response</Action>" +
                "<RequestID>" + RequestID + "</RequestID>" +
                "<From>" + self + "</From>" +
                "<Result>arrived</Result>" +
                "<ConfID>" + confID + "</ConfID>" +
                "<SiteID>" + siteId + "</SiteID>" +
                "<InviteID>" + inviteID + "</InviteID>" +
                "</Pack>";
    }
}
