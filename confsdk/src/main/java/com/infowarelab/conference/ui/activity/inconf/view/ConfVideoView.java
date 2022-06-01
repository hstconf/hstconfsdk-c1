package com.infowarelab.conference.ui.activity.inconf.view;


import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.infowarelab.conference.ConferenceApplication;
import com.infowarelab.conference.ui.activity.inconf.BaseFragment;
import com.infowarelab.conference.ui.activity.inconf.view.video.LocalCameraPosition;
import com.infowarelab.conference.ui.activity.inconf.view.video.LocalVideoView;
import com.infowarelab.conference.ui.activity.inconf.view.video.VideoDecodeView;
import com.infowarelab.conference.ui.adapter.VideoShareAdapterByUserId;
import com.infowarelab.conference.ui.tools.NoDoubleClickListener;
import com.infowarelab.conference.ui.view.PageView;
import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.ConferenceCommonImpl;
import com.infowarelabsdk.conference.common.impl.UserCommonImpl;
import com.infowarelabsdk.conference.common.impl.VideoCommonImpl;
import com.infowarelabsdk.conference.confctrl.UserCommon;
import com.infowarelabsdk.conference.domain.UserBean;
import com.infowarelabsdk.conference.util.Constants;
import com.infowarelabsdk.conference.util.FileUtil;
import com.infowarelabsdk.conference.video.VideoCommon;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class ConfVideoView extends BaseFragment implements OnClickListener {
    private View view;
    private boolean isSupportSvc = false;
    private boolean isTile = false;
    private int curPage = 1;

    private FrameLayout flVideo;
    private PageView pvVideos;
    private ActiveChannel activeChannel;
    private RelativeLayout rlVideos, rlVideoDecoder, rlVideoEncoder;
    private LocalVideoView localCamera;
    private LinearLayout llNopermission;
    private Button btnCameraRotate;
    private LinearLayout llDots;
    private View placeTop, placeBottom;
    private LinearLayout llClose;

    private ImageView ivSwitch;
    private LinearLayout llVideos, llVideosContainer;

    private LinearLayout llAddView, llBack;
    private VideoShareAdapterByUserId adapter;
    private ListView videoShareList;

    private int savedRootHeight = 0;


    private UserCommonImpl userCommon;
    private VideoCommonImpl videoCommon;
    private ConferenceCommonImpl conferenceCommon;

    private Handler videoHandler;
    private final int CLOSE_LOCAL_VIDEO = 0;

    private int orientationState = Configuration.ORIENTATION_PORTRAIT;


    private int tileMode = 1;
    public final int VS_MODE_1 = 1;
    public final int VS_MODE_2 = 2;
    public final int VS_MODE_3 = 3;
    public final int VS_MODE_4 = 4;
    public final int VS_MODE_0 = 6;
    private Set<Integer> localPreviewSet = new HashSet<Integer>();
    private Map<Integer, String> multideviceMap = new HashMap<>();
    private Set<Integer> existingVideos;
    private boolean isLocalShare = false;

    public ConfVideoView(ICallParentView iCallParentView) {
        super(iCallParentView);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.conference_videoview_phone, container, false);
        initView();
        initVideo();
        return view;
    }

    private void initVideo() {
        checkVideo();
        initVideoHandler();
        setBottomList(false, false);
    }

    private void initView() {
        activeChannel = new ActiveChannel();
        userCommon = (UserCommonImpl) commonFactory.getUserCommon();
        videoCommon = (VideoCommonImpl) commonFactory.getVideoCommon();
        conferenceCommon = (ConferenceCommonImpl) commonFactory.getConferenceCommon();

        llAddView = (LinearLayout) view.findViewById(R.id.ll_inconf_vs_list);
        llBack = (LinearLayout) view.findViewById(R.id.video_back);
        videoShareList = (ListView) view.findViewById(R.id.lv_devices);
        llBack.setOnClickListener(this);

        ivSwitch = (ImageView) view.findViewById(R.id.iv_inconf_vs_switch);
        llVideos = (LinearLayout) view.findViewById(R.id.ll_inconf_vs_videos);
        llVideosContainer = (LinearLayout) view.findViewById(R.id.ll_inconf_vs_videos_container);

        flVideo = (FrameLayout) view.findViewById(R.id.flvideo);
        pvVideos = (PageView) view.findViewById(R.id.pv_videoroot);
        rlVideos = (RelativeLayout) view.findViewById(R.id.rl_videoroot1);
        rlVideoDecoder = (RelativeLayout) view.findViewById(R.id.rl_videoroot);
        rlVideoEncoder = (RelativeLayout) view.findViewById(R.id.rl_inconf_camera);
        localCamera = (LocalVideoView) view.findViewById(R.id.localVideo);
        llNopermission = (LinearLayout) view.findViewById(R.id.llNopermission);
        btnCameraRotate = (Button) view.findViewById(R.id.btnChangeCamera);
        llDots = (LinearLayout) view.findViewById(R.id.ll_dots);
        placeTop = view.findViewById(R.id.view_inconf_vs_place_top);
        placeBottom = view.findViewById(R.id.view_inconf_vs_place_bottom);
        llClose = (LinearLayout) view.findViewById(R.id.ll_inconf_vs_close);


        ivSwitch.setOnClickListener(this);
        rlVideoDecoder.setOnClickListener(this);

        btnCameraRotate.setOnClickListener(new NoDoubleClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                localCamera.changeCameraFacing();
            }
        });
        localCamera.setTag(0);
        setVideoViewTouch(localCamera);

        rlVideoEncoder.setVisibility(View.VISIBLE);
        rlVideoDecoder.setVisibility(View.VISIBLE);

        String defaultRes = FileUtil.readSharedPreferences(
                getActivity(), Constants.SHARED_PREFERENCES, Constants.DEFAULT_RESOLUTION);
        if (defaultRes != null && videoCommon != null) {
            if (defaultRes.equals("H")) {
                videoCommon.setResolution(1280, 720);
            } else if (defaultRes.equals("M")) {
                videoCommon.setResolution(640, 480);
            } else {
                videoCommon.setResolution(352, 288);
            }
        }

        pvVideos.setOnSkipListener(new PageView.OnSkipHSListener() {
            @Override
            public void onDot(int pages, int cur) {
                setDots(pages, cur);
            }

            @Override
            public void doSkip(int pages, int cur, int singleWidth) {
                if (!isTile) findViewActive(singleWidth, cur);
            }

        });

        flVideo.post(new Runnable() {
            @Override
            public void run() {
                if (savedRootHeight <= 0) {
                    savedRootHeight = flVideo.getHeight();
                }

                updateVideoShowEnter();
            }
        });
    }

    public void setVideoResolution() {
        String defaultRes = FileUtil.readSharedPreferences(
                getActivity(), Constants.SHARED_PREFERENCES, Constants.DEFAULT_RESOLUTION);
        if (defaultRes != null && videoCommon != null) {
            if (defaultRes.equals("H")) {
                videoCommon.setResolution(1280, 720);
            } else if (defaultRes.equals("M")) {
                videoCommon.setResolution(640, 480);
            } else {
                videoCommon.setResolution(352, 288);
            }
        }
        videoHandler.sendEmptyMessage(VideoCommon.VIDEO_LOCAL_RESTART);
    }

    public void checkVideo() {
        localPreviewSet = new HashSet<Integer>();
        existingVideos = new HashSet<Integer>();

        isSupportSvc = conferenceCommon.isSupportSvc();

        Log.d("InfowareLab.Debug", "ConfVideoView.isSupportSvc=" + isSupportSvc);

        if (isSupportSvc && videoCommon.isHardDecode()) {
            isTile = true;
        } else {
            isTile = false;
        }
        setRoleModify();
    }

    public void setRoleModify() {
        if (userCommon.isHost() || videoCommon.isVideoPreviewPriviledge()) {
            setPreviewPriviledge();
        } else if (curPage != 1) {
            changeToConfctrl();
            setPreviewPriviledge();
        }
    }

    private void setPreviewPriviledge() {
        if (userCommon.isHost() || videoCommon.isVideoPreviewPriviledge()) {
            callPriviledge(ACTION_PRIVILEDGE_VS, true);
        } else {
            callPriviledge(ACTION_PRIVILEDGE_VS, false);
        }
    }

    private int getLocalChannelId() {
        Map<Integer, Integer> syncMap = videoCommon.getSyncMap();

        for (Integer key : syncMap.keySet()) {
            if (userCommon.getSelf().getUid() == syncMap.get(key)) {
                return key;
            }
        }

        return -1;
    }

    private Map<Integer, Integer> getSyncMaps(int limitCount) {
        HashMap<Integer, Integer> mm = new HashMap<Integer, Integer>();
        if (limitCount == 0) {
            mm.putAll(videoCommon.getSyncMap());
            Iterator<Integer> it = localPreviewSet.iterator();
            while (it.hasNext()) {
                int cid = it.next();
                if (null != videoCommon.getDeviceMap().get(cid)) {
                    mm.put(cid, videoCommon.getDeviceMap().get(cid));
                }
            }
        } else {
            Iterator<Integer> it = localPreviewSet.iterator();
            int count = 1;
            while (it.hasNext()) {
                int cid = it.next();
                if (null != videoCommon.getDeviceMap().get(cid) && userCommon.getOwnID() != videoCommon.getDeviceMap().get(cid)) {
                    if (count < limitCount) {
                        mm.put(cid, videoCommon.getDeviceMap().get(cid));
                        count++;
                    }
                }
            }
            HashMap<Integer, Integer> mmm = new HashMap<Integer, Integer>();
            mmm.putAll(videoCommon.getSyncMap());
            for (HashMap.Entry<Integer, Integer> e : mmm.entrySet()) {
                if (mm.size() >= limitCount - 1) {
                    break;
                } else if (e.getValue() != userCommon.getSelf().getUid()) {
                    mm.put(e.getKey(), e.getValue());
                }
            }

        }
        synchronized (multideviceMap) {
            if (multideviceMap == null) multideviceMap = new HashMap<>();
            multideviceMap.clear();
            LinkedList<UserBean> list = (LinkedList<UserBean>) ((UserCommonImpl) CommonFactory.getInstance().getUserCommon())
                    .getUserArrayList().clone();
            Map<Integer, Integer> videoDevideMap = ((VideoCommonImpl) CommonFactory.getInstance().getVideoCommon()).getDeviceMap();
            for (UserBean userBean : list) {
                int count = 0;
                for (Iterator<Map.Entry<Integer, Integer>> it = videoDevideMap.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<Integer, Integer> item = it.next();
                    int value = item.getValue();
                    if (value == userBean.getUid()) {
                        count++;
                    }
                }
                if (count > 1) {
                    int c = 1;
                    for (Iterator<Map.Entry<Integer, Integer>> it = videoDevideMap.entrySet().iterator(); it.hasNext(); ) {
                        Map.Entry<Integer, Integer> item = it.next();
                        int key = item.getKey();
                        int value = item.getValue();
                        if (value == userBean.getUid()) {
                            multideviceMap.put(key, cutName(userBean.getUsername(), 6) + "_" + c);
                            c++;
                        }
                    }
                }
            }
        }
        return mm;
    }

    public void preExit() {
        enableCamera(false);
        videoCommon.setHandler(null);
        videoHandler = null;
        updateVideoShow(false);
    }

    private void updateVideoShowEnter() {
        if (!isAdded() || isHidden()) return;

        updateVideoShow(true && ConferenceApplication.isVideoMeeting);

    }

    private void updateVideoShow(boolean isShow) {
        if (!isAdded()) return;
        if (isShow) {
            if (isTile) {//多画面
                Map<Integer, Integer> videos = getSyncMaps(12);
                updateSyncVideosTile(videos, false);
            } else {//单画面
                Map<Integer, Integer> videos = getSyncMaps(0);
                updateSyncVideoSingle(videos, false);
            }
        } else {
            if (isTile) {//多画面
                updateSyncVideosTile(new HashMap<Integer, Integer>(), true);
            } else {//单画面
                updateSyncVideoSingle(new HashMap<Integer, Integer>(), true);
            }
        }
    }


    private void updateSyncVideosTile(Map<Integer, Integer> syncMap, boolean isHidden) {
        if (!ConferenceApplication.isVideoMeeting) {
            llClose.setVisibility(View.VISIBLE);
        } else {
            llClose.setVisibility(View.GONE);
        }
        activeChannel.lastChannel = 0;
        activeChannel.nextChannel = 0;
        if (!syncMap.containsKey(activeChannel.curChannel)) activeChannel.curChannel = 0;

        setBottomList(false, false);

        int rootW = getParentW();

        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) rlVideoDecoder.getLayoutParams();
        int num;
        if (syncMap.containsValue(userCommon.getSelf().getUid())) {
            num = syncMap.size();
        } else {
            num = syncMap.size() + 1;
        }

        removeUselessView(syncMap);

        isLocalShare = isHidden ? false : true;
        if (num < 2) {
            tileMode = VS_MODE_1;
            rlp.width = rootW;
        } else if (num == 2) {
            tileMode = VS_MODE_2;
            rlp.width = rootW;
        } else if (num == 3 && orientationState == Configuration.ORIENTATION_PORTRAIT) {
            tileMode = VS_MODE_3;
            rlp.width = rootW;
        } else {
            tileMode = VS_MODE_4;
            rlp.width = rootW * ((num - 1) / 4 + 1);
        }
        rlVideoDecoder.setLayoutParams(rlp);
        pvVideos.setScreenWidth(rootW, (num - 1) / 4 + 1);

        int count = 1;

        if (tileMode == VS_MODE_1) {
            callLocalCamera(getLocalPosition(VS_MODE_1, count, orientationState), true);
        } else if (isLocalShare) {
            callLocalCamera(getLocalPosition(tileMode, count, orientationState), true);
            count++;
        } else {
            callLocalCamera(getLocalPosition(0, count, orientationState), true);
        }
        int curP = 1;
        for (Integer key : syncMap.keySet()) {
            if (userCommon.getSelf().getUid() == syncMap.get(key)) {
                continue;
            }
            int uid = syncMap.get(key);
            addVideo(orientationState, num, count, key, uid, true);
            if (key == activeChannel.curChannel) {
                if (num > 3) curP = (count - 1) / 4 + 1;

            }
            count++;
        }

        if (activeChannel.curChannel != 0) {
            pvVideos.setCurPage(curP);
            activeChannel.curChannel = 0;
        } else {
            pvVideos.setCurPage(-1);
        }

        setDots((num - 1) / 4 + 1, curP);
        if (!isHidden())
            doTransChannel();

    }

    private void updateSyncVideoSingle(Map<Integer, Integer> syncMap, int selId) {
        activeChannel.setT2S(selId);
        updateSyncVideoSingle(syncMap, false);
    }

    private void updateSyncVideoSingle(Map<Integer, Integer> syncMap, boolean isHidden) {
        if (!ConferenceApplication.isVideoMeeting) {
            llClose.setVisibility(View.VISIBLE);
            setBottomList(false, false);
        } else {
            llClose.setVisibility(View.GONE);
        }
        if (isHidden) {
            LocalCameraPosition position = getLocalPosition(0, 0, orientationState);
            position.setShowName(false);
            callLocalCamera(position, false);
            removeUselessView(new HashMap<Integer, Integer>());
            return;
        }

        if (!syncMap.containsKey(activeChannel.lastChannel)) activeChannel.lastChannel = 0;
        if (!syncMap.containsKey(activeChannel.curChannel)) activeChannel.curChannel = 0;
        if (!syncMap.containsKey(activeChannel.nextChannel)) activeChannel.nextChannel = 0;


        setBottomList(isVidesShow, !isBarsShow);

        int uid = updateBottomList(syncMap, activeChannel.curChannel);

        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) rlVideoDecoder.getLayoutParams();
        rlp.width = getParentW();
        rlVideoDecoder.setLayoutParams(rlp);
        pvVideos.setScreenWidth(getParentW(), 1);
        setDots(1, 1);

        if (activeChannel.curChannel == 0) {
            callLocalCamera(getLocalPosition(VS_MODE_1, 0, orientationState), true);
            removeUselessView(new HashMap<Integer, Integer>());
        } else {
            callLocalCamera(new LocalCameraPosition(), false);
            HashMap<Integer, Integer> m = new HashMap<Integer, Integer>();
            if (activeChannel.curChannel != 0) {
                m.put(activeChannel.curChannel, 0);
            }
            removeUselessView(m);
            addVideo(orientationState, 1, 1, activeChannel.curChannel, uid, true);
        }

        if (!isHidden())
            doTransChannel();
    }


    private void updateSyncVideoSingleSideslip(Map<Integer, Integer> syncMap, boolean isHidden) {
        if (isHidden) {
            LocalCameraPosition position = getLocalPosition(0, 0, orientationState);
            position.setShowName(false);
            callLocalCamera(position, false);
            removeUselessView(new HashMap<Integer, Integer>());
            return;
        }

        if (!syncMap.containsKey(activeChannel.lastChannel)) activeChannel.lastChannel = 0;
        if (!syncMap.containsKey(activeChannel.curChannel)) activeChannel.curChannel = 0;
        if (!syncMap.containsKey(activeChannel.nextChannel)) activeChannel.nextChannel = 0;

        if (activeChannel.curChannel == 0) {
            callLocalCamera(getLocalPosition(VS_MODE_1, 0, orientationState), false);
            HashMap<Integer, Integer> m = new HashMap<Integer, Integer>();
            if (activeChannel.nextChannel != 0) {
                m.put(activeChannel.nextChannel, 0);
            }
            removeUselessView(m);
        } else {
            callLocalCamera(getLocalPosition(VS_MODE_1, 0, orientationState), false);
            HashMap<Integer, Integer> m = new HashMap<Integer, Integer>();
            if (activeChannel.lastChannel != 0) {
                m.put(activeChannel.lastChannel, 0);
            }
            if (activeChannel.curChannel != 0) {
                m.put(activeChannel.curChannel, 0);
            }
            if (activeChannel.nextChannel != 0) {
                m.put(activeChannel.nextChannel, 0);
            }
            removeUselessView(m);
        }

        int page = syncMap.size() + 1;
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) rlVideoDecoder.getLayoutParams();
        rlp.width = getParentW() * page;
        rlVideoDecoder.setLayoutParams(rlp);
        pvVideos.setScreenWidth(getParentW(), page);

        int count = 1;
        int curP = 1;
        for (Integer key : syncMap.keySet()) {
            if (userCommon.getSelf().getUid() == syncMap.get(key)) {
                continue;
            }
            int uid = syncMap.get(key);
            addVideoSingle(count, key, uid, false);
            if (key == activeChannel.curChannel) {
                curP = count + 1;
            }
            count++;
        }
        pvVideos.setCurPage(curP);
        setDots(page, curP);
        if (!isHidden())
            doTransChannel();
    }


    private void removeUselessView(Map<Integer, Integer> mm) {
        if (existingVideos != null && !existingVideos.isEmpty()) {
            Iterator<Integer> it = existingVideos.iterator();
            while (it.hasNext()) {
                int channelid = it.next();
                if (!mm.containsKey(channelid)) {
                    View v = rlVideoDecoder.findViewWithTag(channelid);
                    if (v != null) {
                        VideoDecodeView video = (VideoDecodeView) v.findViewById(R.id.video_item_video);
                        video.changeStatus(0, false);
                        rlVideoDecoder.removeView(v);
                    }
                    it.remove();
                }
            }
        }
    }

    public void removeAllVideo() {
        if (existingVideos != null && !existingVideos.isEmpty()) {
            Iterator<Integer> it = existingVideos.iterator();
            while (it.hasNext()) {
                int channelid = it.next();
                View v = rlVideoDecoder.findViewWithTag(channelid);
                if (v != null) {
                    VideoDecodeView video = (VideoDecodeView) v.findViewById(R.id.video_item_video);
                    video.changeStatus(0, false);
                    rlVideoDecoder.removeView(v);
                }
                it.remove();
            }
        }
        rlVideoDecoder.removeAllViews();
    }


    private VideoDecodeView findViewByChannelid(int channelid) {
        View v = rlVideoDecoder.findViewWithTag(channelid);
        if (v != null) {
            VideoDecodeView video = (VideoDecodeView) v.findViewById(R.id.video_item_video);
            return video;
        } else {
            return null;
        }

    }

    private void findViewActive(int singleWidth, int curPage) {
        activeChannel.setEmpty();
        if (existingVideos != null && !existingVideos.isEmpty()) {
            Iterator<Integer> it = existingVideos.iterator();
            while (it.hasNext()) {
                int channelid = it.next();
                View v = rlVideoDecoder.findViewWithTag(channelid);
                if (v != null) {
                    int left = v.getLeft();
                    int position = (left + 5) / singleWidth + 1;
                    if (position == curPage) {
                        VideoDecodeView video = (VideoDecodeView) v.findViewById(R.id.video_item_video);
                        if (video.getChannelId() == 0) {
                            video.changeStatus((Integer) v.getTag(), true);
                        }
                        activeChannel.curChannel = channelid;
                    } else if ((position - 1) == curPage) {//后一个
                        VideoDecodeView video = (VideoDecodeView) v.findViewById(R.id.video_item_video);
                        if (video.getChannelId() == 0) {
                            video.changeStatus(channelid, true);
                        }
                        activeChannel.nextChannel = channelid;
                    } else if ((position + 1) == curPage) {//前一个
                        VideoDecodeView video = (VideoDecodeView) v.findViewById(R.id.video_item_video);
                        if (video.getChannelId() == 0) {
                            video.changeStatus(channelid, true);
                        }
                        activeChannel.lastChannel = channelid;
                    } else {
                        VideoDecodeView video = (VideoDecodeView) v.findViewById(R.id.video_item_video);
                        video.changeStatus(0, false);
                    }
                }
            }
        }
    }

    private LocalCameraPosition getLocalPosition(int mode, int count, int orientationState) {
        int rootW = getParentW();
        int rootH = getParentH();
        LocalCameraPosition local = new LocalCameraPosition();
        if (mode == 0) {
            local.setLeft(0);
            local.setTop(0);
            local.setWidth(1);
            local.setHeight(1);
        } else if (mode == VS_MODE_1) {
            local.setWidth(rootW);
            local.setHeight(rootH);
        } else if (mode == VS_MODE_2) {
            if (orientationState == Configuration.ORIENTATION_PORTRAIT) {
                local.setTop((count - 1) * rootH / 2);
                local.setWidth(rootW);
                local.setHeight(rootH / 2);
            } else {
                local.setLeft((count - 1) * rootW / 2);
                local.setWidth(rootW / 2);
                local.setHeight(rootH);
            }
        } else if (mode == VS_MODE_3) {
            if (orientationState == Configuration.ORIENTATION_PORTRAIT) {
                local.setTop((count - 1) * rootH / 3);
                local.setWidth(rootW);
                local.setHeight(rootH / 3);
            } else {
                local.setLeft((count - 1) * rootW / 3);
                local.setWidth(rootW / 3);
                local.setHeight(rootH);
            }
        } else if (mode == VS_MODE_4) {
            local.setLeft((count - 1) % 2 * rootW / 2);
            local.setTop((count - 1) / 2 * rootH / 2);
            local.setWidth(rootW / 2);
            local.setHeight(rootH / 2);
        } else if (mode == VS_MODE_0) {
            local.setLeft(0);
            local.setTop(0);
            local.setWidth(rootW);
            local.setHeight(rootH);
        }
        return local;
    }

    private void callLocalCamera(LocalCameraPosition localCameraPosition, boolean clickable) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) rlVideoEncoder.getLayoutParams();
        params.setMargins(localCameraPosition.getLeft(), localCameraPosition.getTop(), localCameraPosition.getRight(), localCameraPosition.getBottom());
        params.width = localCameraPosition.getWidth();
        params.height = localCameraPosition.getHeight();

        //Log.d("InfowareLab.Debug", "rlVideoEncoder

        rlVideoEncoder.setLayoutParams(params);
        int padding = getResources().getDimensionPixelOffset(R.dimen.dp_2);
        localCamera.setParams(params.width - 2 * padding, params.height - 2 * padding);
        localCamera.setClickable(clickable);
        if (localCameraPosition.getWidth() > 1 && localCamera.isPreview()) {
            btnCameraRotate.setVisibility(View.VISIBLE);
        } else {
            btnCameraRotate.setVisibility(View.GONE);
        }
        if (userCommon.isHost()) {
            rlVideoEncoder.setBackgroundResource(R.drawable.bg_item_video_host);
        } else {
            rlVideoEncoder.setBackgroundResource(R.drawable.bg_item_video_nor);
        }
        if (localCameraPosition.isShowName()) {
            getActivity().findViewById(R.id.tv_inconf_camerame).setVisibility(View.VISIBLE);
        } else {
            getActivity().findViewById(R.id.tv_inconf_camerame).setVisibility(View.VISIBLE);
        }
    }


    private void addVideo(int orientation, int total, int count, int channelid, int uid, boolean clickable) {
        if (count > total) return;
        int p = getResources().getDimensionPixelOffset(R.dimen.dp_2);
        int rootW = getParentW();
        int rootH = getParentH();
        int left = 0, right = 0, top = 0, bottom = 0, w = 0, h = 0;
        if (total == 1) {
            left = 0;
            top = 0;
            right = 0;
            bottom = 0;
            w = rootW;
            h = rootH;
        } else if (total == 2) {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                left = 0;
                top = (count - 1) * rootH / 2;
                right = 0;
                bottom = 0;
                w = rootW;
                h = rootH / 2;
            } else {
                left = (count - 1) * rootW / 2;
                top = 0;
                right = 0;
                bottom = 0;
                w = rootW / 2;
                h = rootH;
            }
        } else if (total == 3 && orientation == Configuration.ORIENTATION_PORTRAIT) {
            left = 0;
            top = (count - 1) * rootH / 3;
            right = 0;
            bottom = 0;
            w = rootW;
            h = rootH / 3;
        } else {
            left = (count - 1) % 2 * rootW / 2 + ((count - 1) / 4) * rootW;
            top = ((count - 1) % 4 / 2) * (rootH / 2);
            right = 0;
            bottom = 0;
            w = rootW / 2;
            h = rootH / 2;
        }

        UserBean user = userCommon.getUser(uid);

        String name = "";
        if (multideviceMap != null && !multideviceMap.isEmpty() && multideviceMap.containsKey(channelid)) {
            name = multideviceMap.get(channelid);
        } else {
            name = user.getUsername();
        }
        boolean isHost = false;
        if (user.getRole() == UserCommon.ROLE_SPEAKER || user.getRole() == UserCommon.ROLE_HOST) {
            isHost = true;
        } else {
            isHost = false;
        }
        View v = rlVideoDecoder.findViewWithTag(channelid);
        if (v != null) {
            v.setClickable(clickable);
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) v.getLayoutParams();
            lp.setMargins(left, top, right, bottom);
            lp.width = w;
            lp.height = h;
            v.setLayoutParams(lp);
            if (isHost) {
                v.setBackgroundResource(R.drawable.bg_item_video_host);
            } else {
                v.setBackgroundResource(R.drawable.bg_item_video_nor);
            }
            VideoDecodeView video = (VideoDecodeView) v.findViewById(R.id.video_item_video);
            video.setCurSVCLvl(lp.width - 2 * p, lp.height - 2 * p);
            if (video.getChannelId() == 0) {
                video.changeStatus(channelid, true);
            }
            TextView tv = (TextView) v.findViewById(R.id.tv_item_video);
            tv.setText(name);
            tv.setVisibility(View.VISIBLE);
            setCutVideoClick(v, channelid);
        } else {
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            lp.setMargins(left, top, right, bottom);
            lp.width = w;
            lp.height = h;

            LayoutInflater inflater3 = LayoutInflater.from(getActivity());
            FrameLayout fl = (FrameLayout) inflater3.inflate(R.layout.view_item_video, null);
            fl.setClickable(clickable);
            fl.setLayoutParams(lp);
            fl.setTag(channelid);
            setVideoViewTouch(fl);
            if (isHost) {
                fl.setBackgroundResource(R.drawable.bg_item_video_host);
            } else {
                fl.setBackgroundResource(R.drawable.bg_item_video_nor);
            }
            TextView tv = (TextView) fl.findViewById(R.id.tv_item_video);
            tv.setText(name);
            tv.setVisibility(View.VISIBLE);
            setCutVideoClick(fl, channelid);
            rlVideoDecoder.addView(fl);
            VideoDecodeView video = (VideoDecodeView) fl.findViewById(R.id.video_item_video);
            video.setSvc(isSupportSvc);
            video.changeStatus(channelid, true);
            existingVideos.add(channelid);
        }
    }

    private void addVideoSingle(int count, int channelid, int uid, boolean clickable) {
        int p = getResources().getDimensionPixelOffset(R.dimen.dp_2);
        int rootW = getParentW();
        int rootH = getParentH();
        int left = 0, right = 0, top = 0, bottom = 0, w = 0, h = 0;
        left = rootW * count;
        top = 0;
        right = 0;
        bottom = 0;
        w = rootW;
        h = rootH;


        UserBean user = userCommon.getUser(uid);

        String name = "";
        if (multideviceMap != null && !multideviceMap.isEmpty() && multideviceMap.containsKey(channelid)) {
            name = multideviceMap.get(channelid);
        } else {
            name = user.getUsername();
        }

        boolean isHost = false;
        if (user.getRole() == UserCommon.ROLE_SPEAKER || user.getRole() == UserCommon.ROLE_HOST) {
            isHost = true;
        } else {
            isHost = false;
        }

        View v = rlVideoDecoder.findViewWithTag(channelid);
        if (v != null) {
            v.setClickable(clickable);
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) v.getLayoutParams();
            lp.setMargins(left, top, right, bottom);
            lp.width = w;
            lp.height = h;
            v.setLayoutParams(lp);
            if (isHost) {
                v.setBackgroundResource(R.drawable.bg_item_video_host);
            } else {
                v.setBackgroundResource(R.drawable.bg_item_video_nor);
            }
            VideoDecodeView video = (VideoDecodeView) v.findViewById(R.id.video_item_video);
            video.setCurSVCLvl(lp.width - 2 * p, lp.height - 2 * p);
            TextView tv = (TextView) v.findViewById(R.id.tv_item_video);
            tv.setText(name);
            tv.setVisibility(View.VISIBLE);
            setCutVideoClick(v, channelid);
        } else {
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            lp.setMargins(left, top, right, bottom);
            lp.width = w;
            lp.height = h;

            LayoutInflater inflater3 = LayoutInflater.from(getActivity());
            FrameLayout fl = (FrameLayout) inflater3.inflate(R.layout.view_item_video, null);
            fl.setClickable(clickable);
            fl.setLayoutParams(lp);
            fl.setTag(channelid);
            setVideoViewTouch(fl);
            if (isHost) {
                fl.setBackgroundResource(R.drawable.bg_item_video_host);
            } else {
                fl.setBackgroundResource(R.drawable.bg_item_video_nor);
            }
            TextView tv = (TextView) fl.findViewById(R.id.tv_item_video);
            tv.setText(name);
            tv.setVisibility(View.VISIBLE);
            setCutVideoClick(fl, channelid);
            rlVideoDecoder.addView(fl);
            VideoDecodeView video = (VideoDecodeView) fl.findViewById(R.id.video_item_video);
            video.setSvc(isSupportSvc);
//            video.changeStatus(channelid, true);
            existingVideos.add(channelid);
        }
    }

    private int updateBottomList(Map<Integer, Integer> syncMap, int seleteChannel) {
        int uid = 0;
        llVideosContainer.removeAllViews();
        if (userCommon.getSelf() != null) {
            uid = userCommon.getSelf().getUid();
            int tag = 0;

            if (userCommon.isHost() && syncMap.containsValue(uid)) {
                for (Integer key : syncMap.keySet()) {
                    if (userCommon.getSelf().getUid() == syncMap.get(key)) {
                        Log.d("InfowareLab.Debug", ">>>>>>ConfVideoView.updateBottomList: self key=" + key);
                        tag = key;
                        break;
                    }
                }
            }

            View view = LayoutInflater.from(getActivity()).inflate(R.layout.conference_video_item, null);
            view.setTag(tag);

            LinearLayout llroot = (LinearLayout) view.findViewById(R.id.llVideoItemRoot);
            llroot.setTag(tag);

            ImageView image = (ImageView) view.findViewById(R.id.ivVideoItem);

            final ImageView ivDel = (ImageView) view.findViewById(R.id.ivDelete);
            ivDel.setVisibility(View.GONE);
            ivDel.setTag(tag);

            TextView tvName = (TextView) view.findViewById(R.id.tvName);
            tvName.setTag(tag);
            tvName.setText(cutName(userCommon.getSelf().getUsername(), 8));

            if (seleteChannel == 0) {

                image.setImageResource(R.drawable.ic_vs_item_sel);
                tvName.setTextColor(getResources().getColor(R.color.index_blue));
            } else {
                image.setImageResource(R.drawable.ic_vs_item_nor);
                tvName.setTextColor(getResources().getColor(R.color.index_white));
            }
            llroot.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (activeChannel.curChannel != (Integer) v.getTag()) {
//						isClicked = true;
                        updateSyncVideoSingle(getSyncMaps(0), (Integer) v.getTag());
                    }
                }
            });
            if (userCommon.isHost() && syncMap.containsValue(userCommon.getSelf().getUid())) {
                llroot.setOnLongClickListener(new View.OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        View vdel = ((View) v.getParent()).findViewById(R.id.ivDelete);
                        vdel.setVisibility(View.VISIBLE);
                        return true;
                    }
                });
                ivDel.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        //Log.d("InfowareLab.Debug", ">>>>>>ConfVideoView.updateBottomList: (Self)ivDel.onClick:" + v.getTag());

                        if (videoCommon.getSyncMap().containsKey((Integer) v.getTag())
                                && userCommon.getSelf().isShareVideo()) {
                            videoCommon.closeMyVideo();
                            videoCommon.setSyncVedio((Integer) v.getTag(), false);
                            v.setVisibility(View.GONE);
                        }
                    }
                });
            }
            llVideosContainer.addView(view);
        }

        for (Integer key : syncMap.keySet()) {
            if (userCommon.getSelf().getUid() == syncMap.get(key)) {
                continue;
            }
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.conference_video_item, null);
            view.setTag(key);

            LinearLayout llroot = (LinearLayout) view.findViewById(R.id.llVideoItemRoot);
            llroot.setTag(key);

            ImageView image = (ImageView) view.findViewById(R.id.ivVideoItem);

            final ImageView ivDel = (ImageView) view.findViewById(R.id.ivDelete);
            ivDel.setVisibility(View.GONE);
            ivDel.setTag(key);

            TextView tvName = (TextView) view.findViewById(R.id.tvName);
            tvName.setTag(key);

            if (multideviceMap != null && !multideviceMap.isEmpty() && multideviceMap.containsKey(key)) {
                tvName.setText(multideviceMap.get(key));
            } else {
                tvName.setText(cutName(userCommon.getUser(syncMap.get(key)).getUsername(), 8));
            }

            if (seleteChannel == key) {
                uid = syncMap.get(key);
                image.setImageResource(R.drawable.ic_vs_item_sel);
                tvName.setTextColor(getResources().getColor(R.color.index_blue));
            } else {
                image.setImageResource(R.drawable.ic_vs_item_nor);
                tvName.setTextColor(getResources().getColor(R.color.index_white));
            }
            llroot.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (activeChannel.curChannel != (Integer) v.getTag()) {
//						isClicked = true;
                        updateSyncVideoSingle(getSyncMaps(0), (Integer) v.getTag());
                    }
                }
            });
            llroot.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    // TODO Auto-generated method stub
                    if (userCommon.isHost()) {
                        View vdel = ((View) v.getParent()).findViewById(R.id.ivDelete);
                        vdel.setVisibility(View.VISIBLE);
                    } else if (videoCommon.isVideoPreviewPriviledge() && localPreviewSet.contains((Integer) v.getTag())) {
                        View vdel = ((View) v.getParent()).findViewById(R.id.ivDelete);
                        vdel.setVisibility(View.VISIBLE);
                    }
                    return true;
                }
            });
            ivDel.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub

                    //Log.d("InfowareLab.Debug", ">>>>>>ConfVideoView.updateBottomList: ivDel.onClick:" + v.getTag());

                    int id = (Integer) v.getTag();
                    if (localPreviewSet.contains(id)) {
                        localPreviewSet.remove(id);
                        updateSyncVideoSingle(getSyncMaps(0), activeChannel.curChannel);
                    } else {
                        videoCommon.closeVideo(id);
                        videoCommon.setSyncVedio(id, false);
                    }
                }
            });
            llVideosContainer.addView(view);
        }
        return uid;
    }


    private int lastDownTag = -1;
    private long lastDoubleDownTime = 0;
    private int timeout = 400;
    private int downCount = 0;
    private float lastDownX = 0;
    private boolean isClick = false;

    private void setVideoViewTouch(View v) {
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        pvVideos.requestDisallowInterceptTouchEvent(false);
                        downCount++;
                        lastDownX = event.getX();
                        long currentTime = Calendar.getInstance().getTimeInMillis();
                        if ((int) v.getTag() == lastDownTag && (currentTime - lastDoubleDownTime < timeout)) {
                            oneClickHandler.removeCallbacksAndMessages(null);
                            doDoubleClick(lastDownTag);
                            lastDoubleDownTime = 0;
                        } else {
                            lastDownTag = (int) v.getTag();
                            lastDoubleDownTime = currentTime;
//                            if(!isTile)
//                                oneClickHandler.sendEmptyMessageDelayed(0,timeout);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (lastDoubleDownTime != 0)
                            oneClickHandler.sendEmptyMessageDelayed(0, timeout);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float x = Math.abs(event.getX() - lastDownX);
                        if (x > 5) {
                            pvVideos.requestDisallowInterceptTouchEvent(false);
                            return false;
                        }
                        break;
                }
                return false;
            }
        });
    }

    Handler oneClickHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            downCount = 0;
            lastDoubleDownTime = 0;
            callChangeBars();
        }
    };

    private void doDoubleClick(int channelId) {
        if (!isSupportSvc) return;
        activeChannel.setT2S(channelId);
        isTile = !isTile;
        uHandler.sendEmptyMessage(0);
    }

    private void setCutVideoClick(View v, int channelid) {
//        Button btn = (Button) v.findViewById(R.id.btn_item_cut);
//        if(btn==null)return;
//        if(userCommon.getSelf().getRole()==UserCommon.ROLE_HOST){
//            btn.setVisibility(View.VISIBLE);
//            btn.setTag(channelid);
//            btn.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    CutBmpDialog cutBmpDialog = new CutBmpDialog(getActivity(),ConferenceApplication.SCREEN_WIDTH>ConferenceApplication.SCREEN_HEIGHT?ConferenceApplication.SCREEN_HEIGHT*3/4:ConferenceApplication.SCREEN_WIDTH*3/4);
//                    Bitmap bmp = BitmapFactory.decodeResource(getActivity().getResources(),R.drawable.bg1024);
//                    cutBmpDialog.show(bmp);
//                }
//            });
//        }else{
//            btn.setVisibility(View.GONE);
//        }

    }


    //横竖屏切换
    public void changeOrietation(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            int deg = getActivity().getWindowManager().getDefaultDisplay().getRotation();
            orientationState = Configuration.ORIENTATION_LANDSCAPE;
            changeCameraOrietation(newConfig);
            setPlace4Orientation(orientationState);
            uHandler.sendEmptyMessage(0);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            orientationState = Configuration.ORIENTATION_PORTRAIT;
            changeCameraOrietation(newConfig);
            setPlace4Orientation(orientationState);
            uHandler.sendEmptyMessage(0);
        }
        if (curPage == 2) setParentBars(false);
        setBottomList(isVidesShow, false);
    }

    public void changeCameraOrietation(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            int deg = getActivity().getWindowManager().getDefaultDisplay().getRotation();
            localCamera.setPortrait(false);
            if (deg == Surface.ROTATION_90) {
                localCamera.setCameraLandscape();
            } else if (deg == Surface.ROTATION_270) {
                localCamera.setCameraLandscape270();
            }
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            localCamera.setPortrait(true);
            localCamera.setCameraPortrait();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.video_back) {
            getOnBackPressed();
        } else if (id == R.id.btnChangeCamera) {
            localCamera.changeCameraFacing();
        } else if (id == R.id.localVideo) {//                showVideoList();
        } else if (id == R.id.llConfVideo) {
        } else if (id == R.id.iv_inconf_ctrl_vs_add) {
            if (!videoCommon.getSyncMap().isEmpty() && !userCommon.isHost()) {
                showShortToast(R.string.disallowSync);
                return;
            }
            changeToAddlist();
        } else if (id == R.id.iv_inconf_vs_switch) {
            setBottomList(!isVidesShow, false);
        } else if (id == R.id.rl_videoroot) {
            callChangeBars();
        }
    }

    public void initVideoHandler() {
        videoHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case VideoCommon.VIDEO_LOCAL_CHANNELID:
                        Log.d("InfowareLab.Debug", "ConfVideoView.VIDEO_LOCAL_CHANNELID");
                        break;
                    case VideoCommon.VIDEO_ADD_CHANNEL:
                        Log.d("InfowareLab.Debug", "ConfVideoView.VIDEO_ADD_CHANNEL");
                        if (localPreviewSet.contains(msg.arg2)) {
                            localPreviewSet.remove(msg.arg2);
                        }
                        if (curPage == 1) {
                            updateVideoShowEnter();
                        } else if (curPage == 2) {
                            changeToAddlist();
                        }
                        beginCloudRecord(msg.arg2);
                        //callParentView(ACTION_REFRESHCAMERA, null);
                        break;
                    case VideoCommon.VIDEO_RESET_SIZE:
                        break;
                    case VideoCommon.VIDEO_DECODER_SIZE:
                        int[] param1 = (int[]) msg.obj;
                        Log.d("InfowareLab.Debug", "ConfVideoView.VIDEO_DECODER_SIZE channelid=" + msg.arg1 + " width=" + param1[0] + " height=" + param1[1]);
                        VideoDecodeView videoDecoderView = findViewByChannelid(msg.arg1);
                        if (null == videoDecoderView) {
                        } else if (!videoCommon.isHardDecode() && param1[0] * param1[1] > 1024 * 768) {
                            videoDecoderView.showSupport(false);
                        } else {
                            videoDecoderView.resetSize(param1[0], param1[1]);
                            videoDecoderView.showSupport(true);
                        }
                        break;
                    case VideoCommon.VIEDO_DATA:
                        break;
                    case VideoCommon.VIDEO_LOCAL_CHANNEL:
                        Log.d("InfowareLab.Debug", "ConfVideoView.VIDEO_LOCAL_CHANNEL");
                        int channelId = msg.arg1;
                        if ((Boolean) msg.obj) {
                            if (!localCamera.isEnabled()) {
                                videoCommon.closeMyVideo();
                                videoCommon.setSyncVedio(channelId, false);
                            } else {
                                openLocalVideo();
                                videoCommon.openMyVideo();
                                videoCommon.setSyncVedio(channelId, true);
                            }
                        } else {
                            closeLocalVideo();
                            videoCommon.closeMyVideo();
                            videoCommon.setSyncVedio(channelId, false);
                        }

                        //callParentView(ACTION_REFRESHCAMERA, null);

                        break;
                    case VideoCommon.VIDEO_REMOVE_CHANNEL:
                        Log.d("InfowareLab.Debug", "ConfVideoView.VIDEO_REMOVE_CHANNEL");
                        if (curPage == 1) {
                            updateVideoShowEnter();
                        } else if (curPage == 2) {
                            changeToAddlist();
                        }
                        stopCloudRecord(msg.arg1);

                        //callParentView(ACTION_REFRESHCAMERA, null);
                        break;
                    case CLOSE_LOCAL_VIDEO:
                        Log.d("InfowareLab.Debug", "ConfVideoView.CLOSE_LOCAL_VIDEO");
                        break;
                    case VideoCommon.VIDEO_LOCAL_RESTART:
                        Log.d("InfowareLab.Debug", "ConfVideoView.VIDEO_LOCAL_RESTART");
                        localCamera.reStartLocalView();
                        resetLocalViewSizeHandler.sendEmptyMessage(0);
                        break;
                    case VideoCommon.VIDEO_READY:
                        VideoDecodeView videoMainView = findViewByChannelid(msg.arg1);
                        if (null != videoMainView) {
                            videoMainView.showSupportReady();
                        }
                        break;
                    case VideoCommon.VIDEO_NOPERMISSION:
                        llNopermission.setVisibility(View.VISIBLE);
                        break;
                    case VideoCommon.VIDEO_PERMISSION:
                        llNopermission.setVisibility(View.GONE);
                        break;
                    case VideoCommon.VIDEO_REMOVE_DEVICE:

                        if (localPreviewSet.contains(msg.arg1)) {
                            localPreviewSet.remove(msg.arg1);
                        }
                        if (curPage == 1) {
                            updateVideoShowEnter();
                        } else if (curPage == 2) {
                            changeToAddlist();
                        }
                        break;
                    case VideoCommon.VIDEO_ADD_DEVICE:
                        if (curPage == 2) {
                            changeToAddlist();
                        }
                        break;
                    case VideoCommon.VIDEO_PREVIEW_PRIVILEDGE:
                        setPreviewPriviledge();
                        if (!videoCommon.isVideoPreviewPriviledge()) {
                            if (curPage == 1) {
                                localPreviewSet.clear();
                                if (isAdded())
                                    updateVideoShowEnter();
                            } else {
                                localPreviewSet.clear();
                                if (isAdded())
                                    changeToConfctrl();
                            }
                        }
                        break;
                    case VideoCommon.VIDEO_KEYFRAME:
                        Log.i("always", "always keyframe VIDEO_KEYFRAME");
                        if (localCamera != null)
                            localCamera.flushEncoder();
                        break;
                    case VideoCommon.VIDEO_LOCAL_RESTART_SWITCHENCODE:
                        localCamera.switchSoftEncode(msg.arg1 == 1);
                        break;
                    default:
                        break;
                }
            }
        };
        videoCommon.setHandler(videoHandler);
    }

    public boolean isLocalVideoOpened() {
        if (null == localCamera) return false;
        return localCamera.isPreview();
    }

    public void enableCamera(boolean enable) {
        Log.d("InfowareLab.Debug", "ConfVideoView.enableCamera:" + enable);
        if (enable) {
            llNopermission.setVisibility(View.GONE);
            btnCameraRotate.setVisibility(View.VISIBLE);
            localCamera.enableCamera(true);
            openLocalVideo();
        } else {
            btnCameraRotate.setVisibility(View.GONE);
            llNopermission.setVisibility(View.VISIBLE);
            localCamera.destroyCamera();
            localCamera.enableCamera(false);
        }
    }

    protected void closeLocalVideo() {
        stopLocalVideo();
        localCamera.setShareing(false);
    }

    public void closeLocalCamera() {
        localCamera.destroyCamera();
        llNopermission.setVisibility(View.VISIBLE);
    }

    public void openLocalVideo() {

        Log.d("InfowareLab.Debug", "ConfVideoView.openLocalVideo:" + localCamera.isEnabled());

//        if (!localCamera.isEnabled()){
//            closeMyVideo();
//            return;
//        }

        llNopermission.setVisibility(View.GONE);
        if (!localCamera.getCamera() || !localCamera.isPreview()) {
            localCamera.changeStatus(true);
        }
        localCamera.setShareing(true);
    }

    public void changeToConfctrl() {
        setParentBars(true);

        setBottomList(isVidesShow, false);

        llAddView.setVisibility(View.GONE);

        flVideo.setVisibility(View.VISIBLE);
        rlVideoDecoder.setVisibility(View.VISIBLE);

        setPreviewPriviledge();
        uHandler.sendEmptyMessage(0);
        curPage = 1;
        callParentView(ACTION_VIDEOPAGE, null);
    }

    public void changeToAddlist() {
        setParentBars(false);

        llAddView.setVisibility(View.VISIBLE);
//        updateSyncVideoSingle(null, -1);

        adapter = new VideoShareAdapterByUserId(getActivity(), localPreviewSet, userCommon.isHost());
        adapter.setItemCheck(new VideoShareAdapterByUserId.ItemCheck() {
            @Override
            public void checkView(int cid) {
                if (localPreviewSet.contains(cid)) {
                    localPreviewSet.remove(cid);
                } else {
                    localPreviewSet.add(cid);
                    activeChannel.setT2S(cid);
                }
                changeToConfctrl();
            }

            @Override
            public void checkSync(int cid) {
                if (videoCommon.getSyncMap().containsKey(cid)) {
                    videoCommon.closeVideo(cid);
                    videoCommon.setSyncVedio(cid, false);
                    if (localPreviewSet.contains(cid)) {
                        localPreviewSet.remove(cid);
                    }
                } else {
                    videoCommon.openVideo(cid, (SurfaceView) null);
                    videoCommon.setSyncVedio(cid, true);
                    activeChannel.setT2S(cid);
                    localPreviewSet.add(cid);
                    showShortToast(R.string.vs_synced);
                }
                changeToConfctrl();
            }
        });
        videoShareList.setAdapter(adapter);

        curPage = 2;
        callParentView(ACTION_VIDEOPAGE, null);
    }


    public void stopLocalVideo() {
        videoHandler.sendEmptyMessage(CLOSE_LOCAL_VIDEO);
    }


    public boolean getOnBackPressed() {
        if (curPage == 1) {
            return true;
        } else if (curPage == 3) {
            changeToAddlist();
            return false;
        } else {
            changeToConfctrl();
            return false;
        }
    }


    public void restartApp() {
        if (localCamera != null && (localCamera.isPreview() || localCamera.isShareing())) {
            localCamera.destroyCamera();
            localCamera.startCamera();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        // TODO Auto-generated method stub
        super.onHiddenChanged(hidden);
        if (hidden) {
            updateVideoShow(false);
        } else {
            Configuration mConfiguration = this.getResources().getConfiguration();
            orientationState = mConfiguration.orientation;
            setPlace4Orientation(orientationState);
            setBottomList(false, false);
            uHandler.sendEmptyMessage(0);
        }
    }

    Handler resetLocalViewSizeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (rlVideoEncoder.getWidth() > 1 && rlVideoEncoder.getHeight() > 1) {
                int padding = getResources().getDimensionPixelOffset(R.dimen.dp_2);
                localCamera.setParams(rlVideoEncoder.getWidth() - 2 * padding, rlVideoEncoder.getHeight() - 2 * padding);
            }
        }

    };

    Handler vHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            updateVideoShow(true);
            sHandler.sendEmptyMessage(0);
        }
    };
    Handler sHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            pvVideos.Scroll2Cur();
        }
    };
    Handler uHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            updateVideoShowEnter();
        }
    };

    private int getParentW() {
        if (orientationState == Configuration.ORIENTATION_PORTRAIT) {
            return ConferenceApplication.Root_W;
        } else {
            return ConferenceApplication.Screen_H - ConferenceApplication.StateBar_H - ConferenceApplication.NavigationBar_H;
        }
    }

    private int getParentH() {
        int w = flVideo.getWidth();
        int h = flVideo.getHeight();
        if (orientationState == Configuration.ORIENTATION_PORTRAIT) {
            if (savedRootHeight > 0) {
                return savedRootHeight;
            } else {
                return w < h ? h : w;
            }
        } else {
            return w > h ? h : w;
        }
    }

    private void setDots(int pages, int cur) {
        if (pages < 2) {
            llDots.setVisibility(View.GONE);
        } else {
            llDots.setVisibility(View.VISIBLE);
            LayoutInflater inflater3 = LayoutInflater.from(getActivity());
            llDots.removeAllViews();
            for (int i = 1; i <= pages; i++) {
                ImageView iv_image = (ImageView) inflater3.inflate(
                        R.layout.item_vp_dot, null);
                if (i == cur) {
                    iv_image.setBackgroundResource(R.drawable.r_vp_indicator_selected);
                }
                llDots.addView(iv_image);
            }
        }
    }

    public void doTransChannel() {
        int channel = 0;
        if (isTile) {
            channel = videoCommon.LOCAL_VIDEO_CHANNEL_ID;
        } else {
            channel = activeChannel.curChannel > 0 ? activeChannel.curChannel : videoCommon.LOCAL_VIDEO_CHANNEL_ID;
        }

        JSONObject json = new JSONObject();
        try {
            json.put("code", 1);
            json.put("module", "video");
            json.put("mode", isTile ? 1 : 0);
            json.put("channel", channel);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        callParentView(ACTION_TRANSCHANNEL, json.toString());
    }


    public void beginCloudRecord(int channelid) {
        if (channelid <= 0) {
            HashMap<Integer, Integer> mmm = new HashMap<Integer, Integer>();
            mmm.putAll(videoCommon.getSyncMap());
            for (HashMap.Entry<Integer, Integer> e : mmm.entrySet()) {
                videoCommon.enableCloudRecord(e.getKey());
            }
        } else {
            videoCommon.enableCloudRecord(channelid);
        }
    }

    public void stopCloudRecord(int channelid) {
        if (channelid <= 0) {
            HashMap<Integer, Integer> mmm = new HashMap<Integer, Integer>();
            mmm.putAll(videoCommon.getSyncMap());
            for (HashMap.Entry<Integer, Integer> e : mmm.entrySet()) {
                videoCommon.disableCloudRecord(e.getKey());
            }
        } else {
            videoCommon.disableCloudRecord(channelid);
        }
    }

    public int getCurPage() {
        return curPage;
    }

    private class ActiveChannel {
        int lastChannel;
        int curChannel;
        int nextChannel;

        public void setT2S(int cur) {
            if (cur == videoCommon.LOCAL_VIDEO_CHANNEL_ID) cur = 0;
            this.lastChannel = 0;
            this.curChannel = cur;
            this.nextChannel = 0;
        }

        public void setEmpty() {
            this.lastChannel = 0;
            this.curChannel = 0;
            this.nextChannel = 0;
        }
    }


    private boolean isVidesShow = false;

    private void setBottomList(boolean isShow, boolean isHidden) {
        if (!ConferenceApplication.isVideoMeeting) {
            callShowBottom();
            ivSwitch.setVisibility(View.GONE);
            llVideos.setVisibility(View.GONE);
            isVidesShow = false;
            return;
        } else if (isHidden || curPage == 2) {
            ivSwitch.setVisibility(View.GONE);
            llVideos.setVisibility(View.GONE);
            return;
        }

        if (isTile) {
            callShowBottom();
            ivSwitch.setVisibility(View.GONE);
            llVideos.setVisibility(View.GONE);
            isVidesShow = false;
        } else if (isShow) {
            callHideBottom();
            ivSwitch.setVisibility(View.VISIBLE);
            llVideos.setVisibility(View.VISIBLE);
            ivSwitch.setImageResource(R.drawable.ic_vs_switch_videos);
            isVidesShow = true;
        } else {
            callShowBottom();
            llVideos.setVisibility(View.GONE);
            ivSwitch.setVisibility(View.VISIBLE);
            ivSwitch.setImageResource(R.drawable.ic_vs_switch_indexs);
            isVidesShow = false;
        }
    }


    private void setPlace4Orientation(int orientationState) {
        if (orientationState == Configuration.ORIENTATION_LANDSCAPE) {
            placeTop.setVisibility(View.GONE);
            placeBottom.setVisibility(View.GONE);
        } else {
            placeTop.setVisibility(View.VISIBLE);
            placeBottom.setVisibility(View.VISIBLE);
        }
    }

    private void setParentBars(boolean isShow) {
        if (isShow) {
            callShowBars();
        } else {
            callHideBars();
        }
    }

    private boolean isBarsShow = true;

    public void onChangeBars(boolean isShow) {
        this.isBarsShow = isShow;
        if (isShow) {
            setBottomList(isVidesShow, false);
        } else {
            setBottomList(isVidesShow, true);
        }
    }

    public void doShowView() {
        uHandler.sendEmptyMessage(0);
    }

    public void doHideView() {
        callLocalCamera(getLocalPosition(0, 0, orientationState), false);
    }

    public void closeMyVideo() {
        //if (!userCommon.getSelf().isShareVideo()) return;

        Log.d("InfowareLab.Debug", "ConfVideoView.closeMyVideo:" + localCamera.isEnabled());

        videoCommon.closeMyVideo();

        int localChannelId = getLocalChannelId();
        if (localChannelId != -1)
            videoCommon.setSyncVedio((Integer) localChannelId, false);

        Log.d("InfowareLab.Debug", "ConfVideoView.setSyncVedio:" + localChannelId);

    }

    public void openMyVideo() {

        //if (userCommon.getSelf().isShareVideo()) return;

        Log.d("InfowareLab.Debug", "ConfVideoView.openMyVideo:" + localCamera.isEnabled());

        videoCommon.openMyVideo();

        int localChannelId = getLocalChannelId();
        if (localChannelId != -1)
            videoCommon.setSyncVedio((Integer) localChannelId, true);

        Log.d("InfowareLab.Debug", "ConfVideoView.setSyncVedio:" + localChannelId);

    }
}
