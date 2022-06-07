package com.infowarelab.conference.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.infowarelab.conference.ui.view.LodingDialog;
import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.transfer.Config;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by Fred on 2022/3/25.
 */

public class ActExoPlayer extends Activity {

    private static final String TAG = "ActExoPlayer";
    private static final int STATUS_OPENNING = 0;
    private static final int STATUS_BUFFERING = 1;
    private static final int STATUS_PLAYING = 2;
    private static final int STATUS_PAUSED = 3;
    private static final int STATUS_STOPPED = 4;
    private static final int STATUS_ERROR = 5;
    private static final int STATUS_END = 6;
    private static final int STATUS_TIME_CHANGED = 7;
    private static final int STATUS_POSITION_CHANGED = 8;
    private static final int STATUS_BCAST_STOPPED = 9;
    private static final int STATUS_RESET = 10;

    private String mFilePath = null;
    private String mHttpPath = null;

    private StyledPlayerView mVideoLayout = null;
    //private LibVLC libvlc;
    private ExoPlayer mMediaPlayer = null;
    private int mVideoWidth;
    private int mVideoHeight;
    private Timer timer = null;
    private TimerTask task = null;

    private LodingDialog loadingDialog = null;

    private boolean mBCastIsOver = false;
    Handler mHandle = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case STATUS_OPENNING:
//                    if (mMessageText != null) {
//                        mMessageText.setText("正在打开直播...");
//                    }
//                    if (mVideoLayout != null) mVideoLayout.setVisibility(View.GONE);
//                    if (mMessageText != null) mMessageText.setVisibility(View.VISIBLE);
                    break;
                case STATUS_BUFFERING:

                    if (mMessageText != null) {
                        mMessageText.setText("正在缓冲...");

                        if (mVideoLayout != null) mVideoLayout.setVisibility(View.GONE);
                        if (mMessageText != null) mMessageText.setVisibility(View.VISIBLE);
                    }

                    break;
                case STATUS_PLAYING:

                    //stopCheckResumeBcast();

                    if (mMessageText != null) {
                        mMessageText.setText("正在直播...");
                    }
                    if (mMessageText != null) mMessageText.setVisibility(View.GONE);
                    if (mVideoLayout != null) mVideoLayout.setVisibility(View.VISIBLE);

                    break;
                case STATUS_PAUSED:
                    if (mMessageText != null) {
                        mMessageText.setText("直播暂停。");
                    }
                    break;
                case STATUS_STOPPED:
                    if (mMessageText != null) {
                        mMessageText.setText("直播已停止。");
                    }
                    if (mVideoLayout != null) mVideoLayout.setVisibility(View.GONE);
                    if (mMessageText != null) mMessageText.setVisibility(View.VISIBLE);

                    //startCheckResumeBcast(5000);

                    break;
                case STATUS_BCAST_STOPPED:

                    if (mCurrentPlayingTime == 0){
                        if (mMessageText != null) {
                            mMessageText.setText("对不起，连接直播服务器失败。");
                        }
                    }
                    else if (mCurrentPlayingTime > 0) {

                        if (mRTMPUrl == null || mRTMPUrl.isEmpty()) {
                            if (mMessageText != null) {
                                mMessageText.setText("直播已结束。");
                                mBCastIsOver = true;
                            }
                        }
                        else if (!mBCastIsOver)
                        {
                            if (mMessageText != null) {
                                mMessageText.setText("直播连接断开或直播已结束。");
                            }
                        }
                    }
                    if (mVideoLayout != null) mVideoLayout.setVisibility(View.GONE);
                    if (mMessageText != null) mMessageText.setVisibility(View.VISIBLE);
                    break;
                case STATUS_ERROR:
                    if (mMessageText != null) {
                        String errMesage = (String)msg.obj;
                        if (errMesage != null && !errMesage.isEmpty())
                            mMessageText.setText((String) msg.obj);
                    }
                    if (mVideoLayout != null) mVideoLayout.setVisibility(View.GONE);
                    if (mMessageText != null) mMessageText.setVisibility(View.VISIBLE);
                    break;
                case STATUS_TIME_CHANGED:
                    //int time = msg.arg1;
                    if (mMessageText != null) {
                        //mMessageText.setText("正在播放..时间：" + time);
                        mMessageText.setText("");
                    }

                    if (mMessageText != null) mMessageText.setVisibility(View.VISIBLE);
                    if (mVideoLayout != null) mVideoLayout.setVisibility(View.VISIBLE);

                    //startCheckPlayTime();

                    break;

                case STATUS_POSITION_CHANGED:
//                    int position = msg.arg1;
//                    if (mMessageText != null) {
//                        mMessageText.setText("正在播放..位置：" + position);
//                    }
//
//                    if (mMessageText != null) mMessageText.setVisibility(View.VISIBLE);
//                    if (mVideoLayout != null) mVideoLayout.setVisibility(View.VISIBLE);

                    break;
                case STATUS_RESET:

                    break;
            }
        }
    };
    private int mCurrentPlayingTime = 0;
    private int mLastPlayingTime = 0;
    private String mConfId = null;
    private String mRTMPUrl;

    public void startCheckResumeBcast(int delay){

        if (task != null){
            task.cancel();
            task = null;
        }

        if (timer != null) {
            timer.purge();
            timer.cancel();
            timer = null;
        }

        if (task == null) {
            task = new TimerTask() {
                @Override
                public void run() {

                    Log.d(TAG, ">>>>>>startCheckResumeBcast: ");

                    if (mMediaPlayer == null) return;

                    if (mMediaPlayer.getPlaybackState() != SimpleExoPlayer.STATE_ENDED && mMediaPlayer.getPlaybackState() != SimpleExoPlayer.STATE_IDLE) return;

                    if (!checkBroadcastIsStopped()) {

                        if (mMediaPlayer != null){

                            //mMediaPlayer.setPlayWhenReady(true);
                            Log.d(TAG, ">>>>>>try to re-play...");

                            mHandle.sendEmptyMessage(STATUS_RESET);
                        }
                    }

                }
            };
        }

        if (timer == null)
            timer = new Timer();

        timer.schedule(task, delay);
    }

    public void stopCheckResumeBcast() {

        if (task != null) {
            task.cancel();
            task = null;
        }

        if (timer != null) {
            timer.purge();
            timer.cancel();
            timer = null;
        }
    }

    public void startCheckPlayTime(int delay){

        if (task != null){
            task.cancel();
            task = null;
        }

        if (timer != null) {
            timer.purge();
            timer.cancel();
            timer = null;
        }

        if (task == null) {
            task = new TimerTask() {
                @Override
                public void run() {

                    Log.d(TAG, ">>>>>>startCheckPlayTime: " + mCurrentPlayingTime);

                    if (/*mCurrentPlayingTime != 0 && mLastPlayingTime != 0 &&*/ mLastPlayingTime == mCurrentPlayingTime)
                    {
                        Log.d(TAG, "Maybe the broadcasting is stopped and then check the Url");

                        if (checkBroadcastIsStopped()) {

                            //checkBroadcastIsStopped();

//                            task = null;
//                            timer.purge();
//                            timer.cancel();
//                            timer = null;

                            mHandle.sendEmptyMessage(STATUS_BCAST_STOPPED);

                            return;
                        }
                    }

                    mLastPlayingTime = mCurrentPlayingTime;
                }
            };
        }

        if (timer == null)
            timer = new Timer();

        timer.schedule(task, delay, 5 * 1000);
    }

    private boolean checkBroadcastIsStopped() {

        if (mConfId == null) return false;

        mRTMPUrl = Config.getRtmpUrlByNumberEx(mConfId, null, null);

        boolean stopped = (mFilePath == null || mFilePath.isEmpty());

        mBCastIsOver = stopped;

        Log.d(TAG, ">>>>>>checkBroadcastIsStopped: " + stopped);

        return stopped;
    }

    private TextView mMessageText = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.act_exo_player);

        mFilePath = getIntent().getStringExtra("rtmp_url");
        mHttpPath = getIntent().getStringExtra("http_url");

        mConfId = getIntent().getStringExtra("conf_id");

        mRTMPUrl = mFilePath;

        //Log.d(TAG, "Playing: " + mFilePath);

        // 비디오 재생 레이아웃
        mVideoLayout = findViewById(R.id.video_layout);
        mMessageText = findViewById(R.id.tvMessage);

        mMessageText.setVisibility(View.GONE);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //setSize(mVideoWidth, mVideoHeight);
    }

    @Override
    protected void onResume() {
        super.onResume();
        createPlayer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releasePlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    public RenderersFactory buildRenderersFactory(
            Context context) {

        return new DefaultRenderersFactory(context.getApplicationContext())
                .forceEnableMediaCodecAsynchronousQueueing();
    }

    /**
     * Creates MediaPlayer and plays video
     */
    private void createPlayer() {

        releasePlayer();

        //initiate Player
        //Create a default TrackSelector
//        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
//        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
//        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
//

        RenderersFactory renderersFactory = buildRenderersFactory(this);

        //Create the player
        mMediaPlayer = new ExoPlayer.Builder(/* context= */ this)
                .setRenderersFactory(renderersFactory)
                .build();

        StyledPlayerView playerView = findViewById(R.id.video_layout);
        playerView.setPlayer(mMediaPlayer);

        playerView.setUseController(false);

//        RtmpDataSourceFactory rtmpDataSourceFactory = new RtmpDataSourceFactory();
//
//        // This is the MediaSource representing the media to be played.
//        MediaSource videoSource = new ExtractorMediaSource.Factory(rtmpDataSourceFactory)
//                .createMediaSource(Uri.parse(mFilePath));

        if (mFilePath != null) {
            DataSource.Factory dataSourceFactory = new RtmpDataSource.Factory();

            MediaSource mediaSource =
                    new ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(MediaItem.fromUri(mFilePath));


            //mMediaPlayer.addListener(this);

            // Prepare the player with the source.
            mMediaPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
            mMediaPlayer.setMediaSource(mediaSource);
        }
        else if (mHttpPath != null)
        {
            HttpDataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory();

            MediaSource mediaSource =
                    new ProgressiveMediaSource.Factory(dataSourceFactory)
                            //.setDrmSessionManagerProvider(unusedMediaItem -> drmSessionManager)
                            .createMediaSource(MediaItem.fromUri(Uri.parse(mHttpPath)));

            mMediaPlayer.setMediaSource(mediaSource);
        }

        mMediaPlayer.prepare();

        mMediaPlayer.setPlayWhenReady(true);

        mMediaPlayer.play();

        //auto start playing
        //mMediaPlayer.setPlayWhenReady(true);

    }

    private void releasePlayer() {
        if (mMediaPlayer == null)
            return;

        if (task != null){
            task.cancel();
            task = null;
        }

        if (timer != null) {
            timer.purge();
            timer.cancel();
            timer = null;
        }

        mMediaPlayer.stop();

        mMediaPlayer.release();

        mMediaPlayer = null;

        mVideoWidth = 0;
        mVideoHeight = 0;
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }

}
