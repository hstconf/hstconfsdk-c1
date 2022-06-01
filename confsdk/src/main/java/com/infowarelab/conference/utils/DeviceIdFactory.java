package com.infowarelab.conference.utils;

import static android.text.TextUtils.isEmpty;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

import com.infowarelabsdk.conference.util.Constants;

import java.util.UUID;

/**
 * Created by Always on 2017/10/25.
 */

public class DeviceIdFactory {
    public static String getDeviceId(Context context) {
        String id = PreferenceServer.readSharedPreferencesStirng(context, Constants.SHARED_PREFERENCES, PreferenceServer.DEVICEID);
        if (id != null && !id.equals("")) {
            return id;
        } else {
            id = getNewId(context);
            PreferenceServer.saveSharedPreferences(context, Constants.SHARED_PREFERENCES, PreferenceServer.DEVICEID, id);
            return id;
        }

    }

    public static String getNewId(Context context) {
        StringBuilder deviceId = new StringBuilder();
        // 渠道标志
        deviceId.append("a");
        try {
            //wifi mac地址
            WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifi.getConnectionInfo();
            String wifiMac = info.getMacAddress();
            if (!isEmpty(wifiMac)) {
                deviceId.append("wifi");
                deviceId.append(wifiMac);
                return deviceId.toString();
            }
            //IMEI（imei）
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String imei = tm.getDeviceId();
            if (!isEmpty(imei)) {
                deviceId.append("imei");
                deviceId.append(imei);
                return deviceId.toString();
            }
            //序列号（sn）
            String sn = tm.getSimSerialNumber();
            if (!isEmpty(sn)) {
                deviceId.append("sn");
                deviceId.append(sn);
                return deviceId.toString();
            }
            //如果上面都没有， 则生成一个id：随机码
            String uuid = getUUID();
            if (!isEmpty(uuid)) {
                deviceId.append("id");
                deviceId.append(uuid);
                return deviceId.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            deviceId.append("id").append(getUUID());
        }
        return deviceId.toString();
    }

    /**
     * 获取可变UUID
     */
    public static String getUUID() {
        String uuid = UUID.randomUUID().toString();
        return uuid;
    }

    /**
     * UUID+设备号序列号 唯一识别码（不可变）
     */
    public static String getUUID1(Context mContext) {
        final TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice, tmSerial, tmPhone, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(mContext.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String uniqueId = deviceUuid.toString().replace("-", "");
        String uuid = "";
//        Log.e("gggg","uniqueId::"+uniqueId);
        uuid = uniqueId.substring(8, 16).replaceAll("(.{2})", "$1-").toUpperCase();
        return uuid.substring(0, uuid.length() - 1);
    }
}
