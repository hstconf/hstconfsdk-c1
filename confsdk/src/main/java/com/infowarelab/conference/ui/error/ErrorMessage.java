package com.infowarelab.conference.ui.error;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.infowarelab.hongshantongphone.R;


public class ErrorMessage {
    private final int RT_ERROR_MODULE_DB = 30000;
    private final int BaseCode = RT_ERROR_MODULE_DB + 10000;
    // 错误码分段
    private final int ConfSysErrCode_Not_Group = BaseCode + 100;
    private final int ConfSysErrCode_Conf_Create_Base = BaseCode + 200;
    private final int ConfSysErrCode_Conf_Close_Base = BaseCode + 300;
    private final int ConfSysErrCode_Conf_Join_Base = BaseCode + 400;
    private final int ConfSysErrCode_User_Leave_Base = BaseCode + 500;
    private final int ConfSysErrCode_User_license_Base = BaseCode + 700;

    // 未详细分类的错误码 OnConferenceJoinConfirm && OnConferenceLeave
    private final int ConfSysErrCode_Server_Network_Error = ConfSysErrCode_Not_Group; // reconnect
    private final int ConfSysErrCode_Server_GC_Redirect = ConfSysErrCode_Not_Group + 1;

    // 会议创建
    private final int ConfSysErrCode_Server_Response_Timeout = ConfSysErrCode_Conf_Create_Base + 5; // reconnect
    private final int ConfSysErrCode_License_MaxUserNum_Of_AllConf_Limited = ConfSysErrCode_Conf_Create_Base;
    private final int ConfSysErrCode_License_MaxUserNum_Of_OneConf_Limited = ConfSysErrCode_Conf_Create_Base + 1;
    private final int ConfSysErrCode_License_Trial_Expired = ConfSysErrCode_Conf_Create_Base + 2;
    private final int ConfSysErrCode_License_Dog_Error = ConfSysErrCode_Conf_Create_Base + 3;
    private final int ConfSysErrCode_No_License = ConfSysErrCode_Conf_Create_Base + 4;
    private final int ConfSysErrCode_Conf_Expired = ConfSysErrCode_Conf_Create_Base + 6;
    private final int ConfSysErrCode_Jbh_Not_Allow = ConfSysErrCode_Conf_Create_Base + 7;
    private final int ConfSysErrCode_No_InfoSvr = ConfSysErrCode_Conf_Create_Base + 8; // reconnect
    private final int ConfSysErrCode_Invalide_ClusterID = ConfSysErrCode_Conf_Create_Base + 9;
    private final int ConfSysErrCode_Conf_Locked = ConfSysErrCode_Conf_Create_Base + 10;
    private final int ConfSysErrCode_Conf_Server_Internal_Error = ConfSysErrCode_Conf_Create_Base + 11;
    private final int ConfSysErrCode_Conf_H323_User_Cannot_Create_Conf = ConfSysErrCode_Conf_Create_Base + 12;
    private final int ConfSysErrCode_Conf_Create_Undefine_Error = ConfSysErrCode_Conf_Create_Base + 99; // reconnect

    // 会议关闭
    private final int ConfSysErrCode_Host_Closed_Conf = ConfSysErrCode_Conf_Close_Base;
    private final int ConfSysErrCode_Conf_Close_Jbh_No_Host_Join = ConfSysErrCode_Conf_Close_Base + 1;
    private final int ConfSysErrCode_Conf_Close_Server_Network_Error = ConfSysErrCode_Conf_Close_Base + 2; // reconnect
    private final int ConfSysErrCode_Conf_Close_No_More_Attendee = ConfSysErrCode_Conf_Close_Base + 3;
    private final int ConfSysErrCode_Conf_Close_Main_Conf_Closed = ConfSysErrCode_Conf_Close_Base + 4;
    private final int ConfSysErrCode_Conf_Close_Disconnect_With_MasterServer = ConfSysErrCode_Conf_Close_Base + 5;
    private final int ConfSysErrCode_Conf_Close_Ok = ConfSysErrCode_Conf_Close_Base + 98;
    private final int ConfSysErrCode_Conf_Close_Undefine_Error = ConfSysErrCode_Conf_Close_Base + 99; // reconnect

    // 会议加入OnConferenceJoinConfirm
    private final int ConfSysErrCode_Conf_Max_User_Limited = ConfSysErrCode_Conf_Join_Base;
    private final int ConfSysErrCode_Conf_TopArea_Join_SubAreaConf = ConfSysErrCode_Conf_Join_Base + 1; // disconnect
    private final int ConfSysErrCode_Conf_Area_Error = ConfSysErrCode_Conf_Join_Base + 2; // disconnect
    private final int ConfSysErrCode_Conf_Join_Undefine_Error = ConfSysErrCode_Conf_Join_Base + 99; // reconnect

    // 会议离开 OnConferenceLeave
    private final int ConfSysErrCode_User_Kicked = ConfSysErrCode_User_Leave_Base;
    private final int ConfSysErrCode_User_Leave_Noraml = ConfSysErrCode_User_Leave_Base + 1;
    private final int ConfSysErrCode_User_Login_Somewhere = ConfSysErrCode_User_Leave_Base + 2;
    private final int ConfSysErrCode_User_Leave_Network_Error = ConfSysErrCode_User_Leave_Base + 3;
    private final int ConfSysErrCode_User_Leave_Undefine_Error = ConfSysErrCode_User_Leave_Base + 99; // reconnect

    //人数超过监控点数
    private final int ConfSysErrCode_License__Pc_MaxUserNum_Of_AllConf_Limited = ConfSysErrCode_User_license_Base + 1; //pc 超过最大人数
    private final int ConfSysErrCode_License__Mobile_MaxUserNum_Of_AllConf_Limited = ConfSysErrCode_User_license_Base + 2;
    private final int ConfSysErrCode_License__Audit_MaxUserNum_Of_AllConf_Limited = ConfSysErrCode_User_license_Base + 3;
    private final int ConfSysErrCode_License__Supervisor_MaxUserNum_Of_AllConf_Limited = ConfSysErrCode_User_license_Base + 4; //监控人数

    private final static Map<Integer, String> errors = new HashMap<Integer, String>();

    public ErrorMessage(Context context) {
        errors.put(ConfSysErrCode_Server_Network_Error, context.getString(R.string.ConfSysErrCode_Server_Network_Error));
        errors.put(ConfSysErrCode_Server_GC_Redirect, context.getString(R.string.ConfSysErrCode_Server_GC_Redirect));
        errors.put(ConfSysErrCode_Server_Response_Timeout,
                context.getString(R.string.ConfSysErrCode_Server_Response_Timeout));
        errors.put(ConfSysErrCode_License_MaxUserNum_Of_AllConf_Limited,
                context.getString(R.string.ConfSysErrCode_License_MaxUserNum_Of_AllConf_Limited));
        errors.put(ConfSysErrCode_License_MaxUserNum_Of_OneConf_Limited,
                context.getString(R.string.ConfSysErrCode_License_MaxUserNum_Of_OneConf_Limited));
        errors.put(ConfSysErrCode_License_Trial_Expired,
                context.getString(R.string.ConfSysErrCode_License_Trial_Expired));
        errors.put(ConfSysErrCode_License_Dog_Error, context.getString(R.string.ConfSysErrCode_License_Dog_Error));
        errors.put(ConfSysErrCode_No_License, context.getString(R.string.ConfSysErrCode_No_License));
        errors.put(ConfSysErrCode_Conf_Expired, context.getString(R.string.ConfSysErrCode_Conf_Expired));
        errors.put(ConfSysErrCode_Jbh_Not_Allow, context.getString(R.string.ConfSysErrCode_Jbh_Not_Allow));
        errors.put(ConfSysErrCode_No_InfoSvr, context.getString(R.string.ConfSysErrCode_No_InfoSvr));
        errors.put(ConfSysErrCode_Invalide_ClusterID, context.getString(R.string.ConfSysErrCode_Invalide_ClusterID));
        errors.put(ConfSysErrCode_Conf_Locked, context.getString(R.string.ConfSysErrCode_Conf_Locked));
        errors.put(ConfSysErrCode_Conf_Server_Internal_Error,
                context.getString(R.string.ConfSysErrCode_Conf_Server_Internal_Error));
        errors.put(ConfSysErrCode_Conf_H323_User_Cannot_Create_Conf,
                context.getString(R.string.ConfSysErrCode_Conf_H323_User_Cannot_Create_Conf));
        errors.put(ConfSysErrCode_Conf_Create_Undefine_Error,
                context.getString(R.string.ConfSysErrCode_Conf_Create_Undefine_Error));
        errors.put(ConfSysErrCode_Host_Closed_Conf, context.getString(R.string.ConfSysErrCode_Host_Closed_Conf));
        errors.put(ConfSysErrCode_Conf_Close_Jbh_No_Host_Join,
                context.getString(R.string.ConfSysErrCode_Conf_Close_Jbh_No_Host_Join));
        errors.put(ConfSysErrCode_Conf_Close_Server_Network_Error,
                context.getString(R.string.ConfSysErrCode_Conf_Close_Server_Network_Error));
        errors.put(ConfSysErrCode_Conf_Close_No_More_Attendee,
                context.getString(R.string.ConfSysErrCode_Conf_Close_No_More_Attendee));
        errors.put(ConfSysErrCode_Conf_Close_Main_Conf_Closed,
                context.getString(R.string.ConfSysErrCode_Conf_Close_Main_Conf_Closed));
        errors.put(ConfSysErrCode_Conf_Close_Disconnect_With_MasterServer,
                context.getString(R.string.ConfSysErrCode_Conf_Close_Disconnect_With_MasterServer));
        errors.put(ConfSysErrCode_Conf_Close_Ok, context.getString(R.string.ConfSysErrCode_Conf_Close_Ok));
        errors.put(ConfSysErrCode_Conf_Close_Undefine_Error,
                context.getString(R.string.ConfSysErrCode_Conf_Close_Undefine_Error));
        errors.put(ConfSysErrCode_Conf_Max_User_Limited,
                context.getString(R.string.ConfSysErrCode_Conf_Max_User_Limited));
        errors.put(ConfSysErrCode_Conf_TopArea_Join_SubAreaConf,
                context.getString(R.string.ConfSysErrCode_Conf_TopArea_Join_SubAreaConf));
        errors.put(ConfSysErrCode_Conf_Area_Error, context.getString(R.string.ConfSysErrCode_Conf_Area_Error));
        errors.put(ConfSysErrCode_Conf_Join_Undefine_Error,
                context.getString(R.string.ConfSysErrCode_Conf_Join_Undefine_Error));
        errors.put(ConfSysErrCode_User_Kicked, context.getString(R.string.ConfSysErrCode_User_Kicked));
        errors.put(ConfSysErrCode_User_Leave_Noraml, context.getString(R.string.ConfSysErrCode_User_Leave_Noraml));
        errors.put(ConfSysErrCode_User_Login_Somewhere, context.getString(R.string.ConfSysErrCode_User_Login_Somewhere));
        errors.put(ConfSysErrCode_User_Leave_Network_Error,
                context.getString(R.string.ConfSysErrCode_User_Leave_Network_Error));
        errors.put(ConfSysErrCode_User_Leave_Undefine_Error,
                context.getString(R.string.ConfSysErrCode_User_Leave_Undefine_Error));
        errors.put(ConfSysErrCode_License__Mobile_MaxUserNum_Of_AllConf_Limited, context.getString(R.string.ConfSysErrCode_License__Mobile_MaxUserNum_Of_AllConf_Limited));
        errors.put(Integer.MAX_VALUE, context.getString(R.string.UnknowError));
    }

    public String getErrorMessageByCode(int code) {
        String error = errors.get(code);
        if (error == null) {
            error = errors.get(Integer.MAX_VALUE);
        }
        return error;
    }

}
