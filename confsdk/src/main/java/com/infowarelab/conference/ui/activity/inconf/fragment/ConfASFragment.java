package com.infowarelab.conference.ui.activity.inconf.fragment;


import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

import android.content.Context;
import android.content.res.Configuration;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.infowarelab.conference.ConferenceApplication;
import com.infowarelab.conference.ui.activity.inconf.BaseFragment;
import com.infowarelab.conference.ui.activity.inconf.view.ds.VideoSyncViewDS;
import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.ShareDtCommonImpl;
import com.infowarelabsdk.conference.shareDt.ShareDtCommon;

public class ConfASFragment extends BaseFragment implements OnClickListener {
    private ShareDtCommonImpl asCommon = (ShareDtCommonImpl) commonFactory.getSdCommon();

    private View view;
    public Handler asHandler;
    private FrameLayout flAs;
    private VideoSyncViewDS showView;
    private LinearLayout llNo;
    private LinearLayout llNonsupport;
    private LinearLayout llWait;
    private LinearLayout llClosed;
    private View placeTop, placeBottom;
    //	private Button btnSwitch;
    private boolean isSubscribe = true;
    private AudioTrack audioTrack;
    private boolean isViewed = false;

    public ConfASFragment(ICallParentView iCallParentView) {
        super(iCallParentView);
        //initHandler();
        //sub(true);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("InfowareLab.Debug", "ConfASView.onCreateView isAdded=" + isAdded() + " isVisibleToUser=" + getUserVisibleHint());
        view = inflater.inflate(R.layout.conference_ds_phone, container, false);
        initView();
        initHandler();
        return view;
    }

    private void initView() {
        Log.d("InfowareLab.Debug", "ConfASView.initView");
        flAs = (FrameLayout) view.findViewById(R.id.fl_inconf_as);
        showView = (VideoSyncViewDS) view.findViewById(R.id.video_as);
        llNo = (LinearLayout) view.findViewById(R.id.ll_as_no);
        llNonsupport = (LinearLayout) view.findViewById(R.id.ll_as_nonsupport);
        llWait = (LinearLayout) view.findViewById(R.id.ll_as_wait);
        llClosed = (LinearLayout) view.findViewById(R.id.ll_as_closed);
        placeTop = view.findViewById(R.id.view_inconf_as_place_top);
        placeBottom = view.findViewById(R.id.view_inconf_as_place_bottom);
        showView.setCkListener(new VideoSyncViewDS.OnClkListener() {
            @Override
            public void doClick() {
                callChangeBars();
            }
        });
//		btnSwitch = (Button) view.findViewById(R.id.btn_as_switch);
//		btnSwitch.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				btnSwitch.setClickable(false);
//				if(asCommon.isShared()){
//					if(isSubscribe){
//						close();
//					}else{
//						open();
//					}
//				}
//			}
//		});
        showView.post(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                initDecoder();
                initData();
                if (getUserVisibleHint()) initHandler.sendEmptyMessage(0);
            }
        });
        isSubscribe = true;
    }

    @Override
    public void onPause() {
        super.onPause();
//        if (audioTrack != null){
//            audioTrack.stop();
//            audioTrack.release();
//            audioTrack = null;
//        }
//
//        close();
    }

    private void initData() {
        Log.d("InfowareLab.Debug", "ConfASView.initData");

        if (asCommon.isShared()) {
            showAsWait();
        } else {
            showNo();
        }
    }

    Handler initHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            if (!isAdded()) return;
            if (asCommon.isShared()) {
                if (isSubscribe && ConferenceApplication.isVideoMeeting) {
                    isViewed = true;
                    open();
                } else {
                    isViewed = true;
                    close();
                }
            } else {
                stop();
            }
        }
    };

    /**
     * 注册handler
     */
    private void initHandler() {
        asHandler = new Handler() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void handleMessage(final Message msg) {
                if (!isAdded()) return;
                super.handleMessage(msg);
                switch (msg.what) {
                    case ShareDtCommon.START_SHARE_DT:
                        Log.d("InfowareLab.Debug", "ConfASView.ShareDtCommon.START_SHARE_DT");
                        isViewed = false;
//                        if (isAdded() && !isHidden()) {
//                            if (isSubscribe) {
//                                open();
//                            } else {
//                                close();
//                            }
//                        }
                        callParentView(ACTION_ASSTATE, null);
                        break;
                    case ShareDtCommon.STOP_SHARE_DT:
                        Log.d("InfowareLab.Debug", "ConfASView.ShareDtCommon.STOP_SHARE_DT");
                        stop();
                        callParentView(ACTION_ASSTATE, null);
                        break;
                    case ShareDtCommon.INIT_BROWSER:
                        Log.d("InfowareLab.Debug", "ConfASView.ShareDtCommon.INIT_BROWSER");
                        if (isSubscribe) {
                            asCommon.setShow(true);
                            if (getOrientationState() == Configuration.ORIENTATION_PORTRAIT) {
                                setPM();
                            } else {
                                setPML();
                            }
                        }
                        break;
                    case ShareDtCommon.INIT_DECODE_FAILED:
                        Log.d("InfowareLab.Debug", "ConfASView.ShareDtCommon.INIT_DECODE_FAILED");
                        showNonsupport();
                        break;
                    case ShareDtCommon.DT_READY:
                        Log.d("InfowareLab.Debug", "ConfASView.ShareDtCommon.DT_READY");
                        if (isAdded() && !isHidden()) {
                            showAs();
                        }
                        break;
                    case ShareDtCommon.CHECK_SWITCH:
//					if ((Boolean) msg.obj) {
//						btnSwitch.setText("关闭");
//					}else {
//						btnSwitch.setText("打开");
//					}
//					btnSwitch.setClickable(true);
                        break;
                    case ShareDtCommon.AUDIO:
                        //Log.d("InfowareLab.Debug", "ConfASView.ShareDtCommon.AUDIO");
                        if (audioTrack == null) {
                            int bufSize = AudioTrack.getMinBufferSize(32000, AudioFormat.CHANNEL_CONFIGURATION_STEREO, AudioFormat.ENCODING_PCM_16BIT);
                            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 32000, AudioFormat.CHANNEL_CONFIGURATION_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufSize, AudioTrack.MODE_STREAM);
                            audioTrack.setVolume(100f);
                            audioTrack.play();
                        }
                        byte[] data = (byte[]) msg.obj;
                        int length = msg.arg1;

                        Log.d("InfowareLab.Debug", ">>>>>> ShareDtCommon.AUDIO: " + data + "; " + length);

//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                            audioTrack.write(data, 0, length, AudioTrack.WRITE_NON_BLOCKING);
//                        } else
                            audioTrack.write(data, 0, length);
                        //Log.e("ttttt","data数据量::"+data.length);
                        break;
                    default:
                        break;
                }
            }
        };
        if (asCommon != null) {
            asCommon.setHandler(asHandler);
        }
    }

    /**
     * 无共享
     */
    private void showNo() {

        Log.d("InfowareLab.Debug", "ConfASView.showNo");
        showView.setVisibility(View.VISIBLE);
//		btnSwitch.setVisibility(View.GONE);
        llNonsupport.setVisibility(View.GONE);
        llClosed.setVisibility(View.GONE);
        llWait.setVisibility(View.GONE);
        llNo.setVisibility(View.VISIBLE);
    }

    /**
     * 不支持
     */
    private void showNonsupport() {
        showView.setVisibility(View.GONE);
//		btnSwitch.setVisibility(View.GONE);
        llNo.setVisibility(View.GONE);
        llClosed.setVisibility(View.GONE);
        llWait.setVisibility(View.GONE);
        llNonsupport.setVisibility(View.VISIBLE);
    }

    /**
     * 请稍候
     */
    private void showAsWait() {
        Log.d("InfowareLab.Debug", "ConfASView.showAsWait");
        llNonsupport.setVisibility(View.GONE);
        llNo.setVisibility(View.GONE);
//		btnSwitch.setVisibility(View.GONE);
        llClosed.setVisibility(View.GONE);
        showView.setVisibility(View.VISIBLE);
        llWait.setVisibility(View.VISIBLE);
    }

    /**
     * 主动关闭
     */
    private void showAsClosed() {
        Log.d("InfowareLab.Debug", "ConfASView.showAsClosed");
        llNo.setVisibility(View.GONE);
        llNonsupport.setVisibility(View.GONE);
        llWait.setVisibility(View.GONE);
//		btnSwitch.setVisibility(View.VISIBLE);
        showView.setVisibility(View.VISIBLE);
        llClosed.setVisibility(View.VISIBLE);
    }

    private void showAsClosedTab() {
        llNo.setVisibility(View.GONE);
        llNonsupport.setVisibility(View.GONE);
        llWait.setVisibility(View.VISIBLE);
//		btnSwitch.setVisibility(View.GONE);
        showView.setVisibility(View.VISIBLE);
        llClosed.setVisibility(View.GONE);
    }

    private void showAs() {
        Log.d("InfowareLab.Debug", "ConfASView.showAs");
        llNonsupport.setVisibility(View.GONE);
        llNo.setVisibility(View.GONE);
        llClosed.setVisibility(View.GONE);
        llWait.setVisibility(View.GONE);
        showView.setVisibility(View.VISIBLE);
//		btnSwitch.setVisibility(View.VISIBLE);
    }

    private void initDecoder() {
//		int parentH = ConferenceApplication.Screen_H-ConferenceApplication.StateBar_H-ConferenceApplication.NavigationBar_H-ConferenceApplication.Top_H-ConferenceApplication.Bottom_H;
//		int parentW = ConferenceApplication.Screen_W;
//		if(parentH*1.0/asCommon.getDT_HEIGHT()>parentW*1.0/asCommon.getDT_WIDTH()){
//			showView.initSize(parentW, (int) ((parentW*1.0/asCommon.getDT_WIDTH())*asCommon.getDT_HEIGHT()),parentW,parentH);
//		}else {
//			showView.initSize((int) ((parentH*1.0/asCommon.getDT_HEIGHT())*asCommon.getDT_WIDTH()), parentH,parentW,parentH);
//		}

        showView.initSize(1, 1, 1, 1);
        showView.changeStatus(true);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
        }
    }


    public void dohideView() {

        Log.d("InfowareLab.Debug", "ConfASView.dohideView");
        if (isSubscribe) {
            initHandler.removeCallbacksAndMessages(null);
            hide();
        }
    }

    public void doShowView() {
        Log.d("InfowareLab.Debug", "ConfASView.doShowView");
        if (asCommon != null) {
            asCommon.setHandler(asHandler); //1. recover the asHandler
        }
        setPlace(getOrientationState());
        initHandler.sendEmptyMessage(0);
    }

    private void open() {
        if (asCommon.getDecodeState() != 1) {
            showAsWait();
        } else {
            showAs();
        }
        asCommon.setShow(true);
        sub(true);
        if (getOrientationState() == Configuration.ORIENTATION_PORTRAIT) {
            setPM();
        } else {
            setPML();
        }
        isSubscribe = true;
    }

    private void hide() {

        Log.d("InfowareLab.Debug", "ConfASView.hide");
        setP1();
        asCommon.setShow(false);
        sub(false);
        showAsClosedTab();
    }

    private void close() {
        setP1();
        asCommon.setShow(false);
        sub(false);
        showAsClosed();
//        isSubscribe = false;
    }

    private void stop() {
        Log.d("InfowareLab.Debug", "ConfASView.stop");
        setP1();
        asCommon.setShow(false);
        //sub(false);
        showNo();
    }

    private void setP1() {
        showView.setP1();
    }

    private void setPM() {
        int parentH = ConferenceApplication.Screen_H - ConferenceApplication.StateBar_H - ConferenceApplication.NavigationBar_H - ConferenceApplication.Top_H - ConferenceApplication.Bottom_H;
//		if(flAs.getHeight()>1&&flAs.getHeight()<parentH)parentH = flAs.getHeight();
        int parentW = ConferenceApplication.Screen_W;
        if (parentH * 1.0 / asCommon.getDT_HEIGHT() > parentW * 1.0 / asCommon.getDT_WIDTH()) {
            showView.initSize(parentW, (int) ((parentW * 1.0 / asCommon.getDT_WIDTH()) * asCommon.getDT_HEIGHT()), parentW, parentH);
        } else {
            showView.initSize((int) ((parentH * 1.0 / asCommon.getDT_HEIGHT()) * asCommon.getDT_WIDTH()), parentH, parentW, parentH);
        }
    }

    private void setPML() {
        int parentH = ConferenceApplication.getConferenceApp().getWindowHeight(ORIENTATION_LANDSCAPE);//ConferenceApplication.Screen_W - ConferenceApplication.StateBar_H;
        int parentW = ConferenceApplication.getConferenceApp().getWindowWidth(ORIENTATION_LANDSCAPE);//ConferenceApplication.Screen_H - ConferenceApplication.NavigationBar_H - ConferenceApplication.StateBar_H;
        if (parentH * 1.0 / asCommon.getDT_HEIGHT() > parentW * 1.0 / asCommon.getDT_WIDTH()) {
            showView.initSize(parentW, (int) ((parentW * 1.0 / asCommon.getDT_WIDTH()) * asCommon.getDT_HEIGHT()), parentW, parentH);
        } else {
            showView.initSize((int) ((parentH * 1.0 / asCommon.getDT_HEIGHT()) * asCommon.getDT_WIDTH()), parentH, parentW, parentH);
        }
    }

    private void sub(boolean sub) {

        Log.d("InfowareLab.Debug", "ConfASView.sub=" + sub);
        if (!sub) {
            asCommon.subscribeWithMode(0);
            return;
        }

        Log.d("InfowareLab.Debug", "subscribeWithMode(1);");
        if (CommonFactory.getInstance().getConferenceCommon().isSupportSvc()) {
            asCommon.subscribeWithMode(1);
        } else {
            asCommon.subscribeWithMode(1);
        }
    }

    public void changeOrietation(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (showView.getVisibility() == View.VISIBLE && getUserVisibleHint()) {
                setPML();
            }
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (showView.getVisibility() == View.VISIBLE && getUserVisibleHint()) {
                setPM();
            }
        }
        setPlace(newConfig.orientation);
    }

    private int getOrientationState() {
        Configuration mConfiguration = this.getResources().getConfiguration();
        return mConfiguration.orientation;
    }

//    @Override
//    public void onHiddenChanged(boolean hidden) {
//        // TODO Auto-generated method stub
//        Log.e("Fragment", "Fragment 2 onHiddenChanged " + hidden);
//        super.onHiddenChanged(hidden);
//        if (hidden) {
//            dohideView();
//        } else {
//            doShowView();
//        }
//    }

    public boolean isEmpty() {
        return !asCommon.isShared();
    }

    public boolean isViewed() {
        return isViewed;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        Log.d("InfowareLab.Debug", "Fragment 2 setUserVisibleHint " + isVisibleToUser + " isAdded=" + isAdded());
        super.setUserVisibleHint(isVisibleToUser);

        if (!isAdded()) return;
        if (!isVisibleToUser) {
            dohideView();
        } else {
            callParentView(ACTION_SHARE2AS, null);
            callShowBottom();
            doShowView();
        }
    }

    private void setPlace(int orientationState) {
        if (!isAdded()) return;
        if (orientationState == Configuration.ORIENTATION_LANDSCAPE) {
            placeTop.setVisibility(View.GONE);
            placeBottom.setVisibility(View.GONE);
        } else {
            placeTop.setVisibility(View.VISIBLE);
            placeBottom.setVisibility(View.VISIBLE);
        }
    }

    private boolean isBarsShow = true;

    public void onChangeBars(boolean isShow) {
        this.isBarsShow = isShow;
    }

    @Override
    public void onAttach(Context context) {
        Log.d("InfowareLab.Debug", "ConfASView.onAttach");
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("InfowareLab.Debug", "ConfASView.onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {

        Log.d("InfowareLab.Debug", "ConfASView.onStart");
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.d("InfowareLab.Debug", "ConfASView.onResume");
        super.onResume();
    }


    @Override
    public void onStop() {
        Log.d("InfowareLab.Debug", "ConfASView.onStop");
        super.onStop();
        //stop();
        //stopAudio();
    }

    @Override
    public void onDestroyView() {
        Log.d("InfowareLab.Debug", "ConfASView.onDestroyView");
        super.onDestroyView();
        stop();
        stopAudio();
        destroyAudio();
    }

    @Override
    public void onDestroy() {
//        if (asCommon != null) {
//            asCommon.setHandler(null);
//        }
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        Log.d("InfowareLab.Debug", "ConfASView.onDetach");
        super.onDetach();
    }

    //销毁音频
    public void destroyAudio() {
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
            audioTrack = null;
        }
    }

    public void stopAudio() {
        if (audioTrack != null) {
            audioTrack.stop();
        }
    }

    public void startAudio() {
        if (audioTrack != null) {
            audioTrack.play();
        }
    }

    public void preExit() {
        stop();
        stopAudio();
        destroyAudio();
    }

    public void doSetView() {
        setViewHandler.sendEmptyMessage(0);
    }

    Handler setViewHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            //if (true == mLeaved)
            //    return;
            if (!isHidden() && asCommon.isShared() && flAs.getWidth() > 10) {
                open();
            } else {
//                close();
                setP1();
                asCommon.setShow(false);
                //showNone();
            }
        }
    };
}
