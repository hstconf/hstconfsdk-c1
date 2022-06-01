package com.infowarelab.conference;


////import org.apache.log4j.Logger;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.infowarelabsdk.conference.callback.CallbackManager;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.AudioCommonImpl;

/**
 * 电话监听工具类
 *
 * @author Sean.xie
 */
public class PhoneStateUtil extends PhoneStateListener {

    private static final String TAG = PhoneStateUtil.class.getName();


    private AudioCommonImpl audioCommon = (AudioCommonImpl) CommonFactory.getInstance().getAudioCommon();


    //private final Logger log=Logger.getLogger(getClass());

    public PhoneStateUtil() {
        super();
    }


    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        super.onCallStateChanged(state, incomingNumber);
        //log.info( "phonestate 1135 " + state);
        if (CallbackManager.IS_LEAVED) return;
        if (state == TelephonyManager.CALL_STATE_IDLE) {
            Log.i("PhoneState", "PhoneState:CALL_STATE_IDLE");
//			audioCommon.startAudioService();
            audioCommon.startReceive();
            if (AudioCommonImpl.isMICOn) {
//				audioCommon.initAudioRecorder();
//				audioCommon.micOn();
                audioCommon.startSend();
            }

        } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
            Log.i("PhoneState", "PhoneState:CALL_STATE_OFFHOOK");
//			audioCommon.stopAudioService();
            audioCommon.stopReceive();
            audioCommon.stopSend();
            System.out.println("TelephonyManager.CALL_STATE_RINGING");

        }
    }

}
