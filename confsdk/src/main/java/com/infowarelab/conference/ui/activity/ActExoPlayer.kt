package com.infowarelab.conference.ui.activity

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.infowarelab.conference.ui.view.LodingDialog
import com.infowarelab.conference.ui.activity.ActExoPlayer
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import com.infowarelab.conference.live.LiveMessageAdapter
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.google.android.exoplayer2.*
import com.infowarelab.hongshantongphone.R
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.infowarelabsdk.conference.transfer.Config
import java.util.*

/**
 * Created by Fred on 2022/3/25.
 */
class ActExoPlayer : Activity() {
    private var mFilePath: String? = null
    private var mHttpPath: String? = null
    private var mVideoLayout: StyledPlayerView? = null

    //private LibVLC libvlc;
    private var mMediaPlayer: ExoPlayer? = null
    private var mVideoWidth = 0
    private var mVideoHeight = 0
    private var timer: Timer? = null
    private var task: TimerTask? = null
    private val loadingDialog: LodingDialog? = null
    private var mBCastIsOver = false
    var mHandle: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                STATUS_OPENNING -> {
                }
                STATUS_BUFFERING -> if (mMessageText != null) {
                    mMessageText!!.text = "正在缓冲..."
                    if (mVideoLayout != null) mVideoLayout!!.visibility = View.GONE
                    if (mMessageText != null) mMessageText!!.visibility = View.VISIBLE
                }
                STATUS_PLAYING -> {

                    //stopCheckResumeBcast();
                    if (mMessageText != null) {
                        mMessageText!!.text = "正在直播..."
                    }
                    if (mMessageText != null) mMessageText!!.visibility = View.GONE
                    if (mVideoLayout != null) mVideoLayout!!.visibility = View.VISIBLE
                }
                STATUS_PAUSED -> if (mMessageText != null) {
                    mMessageText!!.text = "直播暂停。"
                }
                STATUS_STOPPED -> {
                    if (mMessageText != null) {
                        mMessageText!!.text = "直播已停止。"
                    }
                    if (mVideoLayout != null) mVideoLayout!!.visibility = View.GONE
                    if (mMessageText != null) mMessageText!!.visibility = View.VISIBLE
                }
                STATUS_BCAST_STOPPED -> {
                    if (mCurrentPlayingTime == 0) {
                        if (mMessageText != null) {
                            mMessageText!!.text = "对不起，连接直播服务器失败。"
                        }
                    } else if (mCurrentPlayingTime > 0) {
                        if (mRTMPUrl == null || mRTMPUrl!!.isEmpty()) {
                            if (mMessageText != null) {
                                mMessageText!!.text = "直播已结束。"
                                mBCastIsOver = true
                            }
                        } else if (!mBCastIsOver) {
                            if (mMessageText != null) {
                                mMessageText!!.text = "直播连接断开或直播已结束。"
                            }
                        }
                    }
                    if (mVideoLayout != null) mVideoLayout!!.visibility = View.GONE
                    if (mMessageText != null) mMessageText!!.visibility = View.VISIBLE
                }
                STATUS_ERROR -> {
                    if (mMessageText != null) {
                        val errMesage = msg.obj as String
                        if (errMesage != null && !errMesage.isEmpty()) mMessageText!!.text =
                            msg.obj as String
                    }
                    if (mVideoLayout != null) mVideoLayout!!.visibility = View.GONE
                    if (mMessageText != null) mMessageText!!.visibility = View.VISIBLE
                }
                STATUS_TIME_CHANGED -> {
                    //int time = msg.arg1;
                    if (mMessageText != null) {
                        //mMessageText.setText("正在播放..时间：" + time);
                        mMessageText!!.text = ""
                    }
                    if (mMessageText != null) mMessageText!!.visibility = View.VISIBLE
                    if (mVideoLayout != null) mVideoLayout!!.visibility = View.VISIBLE
                }
                STATUS_POSITION_CHANGED -> {
                }
                STATUS_RESET -> {
                }
            }
        }
    }
    private val mCurrentPlayingTime = 0
    private var mLastPlayingTime = 0
    private var mConfId: String? = null
    private var mRTMPUrl: String? = null
    private val recyclerView: RecyclerView? = null
    private val unreadTipsTextView: TextView? = null
    private val maxCacheCount = 5
    private val mAdapter: LiveMessageAdapter? = null
    fun startCheckResumeBcast(delay: Int) {
        if (task != null) {
            task!!.cancel()
            task = null
        }
        if (timer != null) {
            timer!!.purge()
            timer!!.cancel()
            timer = null
        }
        if (task == null) {
            task = object : TimerTask() {
                override fun run() {
                    Log.d(TAG, ">>>>>>startCheckResumeBcast: ")
                    if (mMediaPlayer == null) return
                    if (mMediaPlayer!!.playbackState != SimpleExoPlayer.STATE_ENDED && mMediaPlayer!!.playbackState != SimpleExoPlayer.STATE_IDLE) return
                    if (!checkBroadcastIsStopped()) {
                        if (mMediaPlayer != null) {

                            //mMediaPlayer.setPlayWhenReady(true);
                            Log.d(TAG, ">>>>>>try to re-play...")
                            mHandle.sendEmptyMessage(STATUS_RESET)
                        }
                    }
                }
            }
        }
        if (timer == null) timer = Timer()
        timer!!.schedule(task, delay.toLong())
    }

    fun stopCheckResumeBcast() {
        if (task != null) {
            task!!.cancel()
            task = null
        }
        if (timer != null) {
            timer!!.purge()
            timer!!.cancel()
            timer = null
        }
    }

    fun startCheckPlayTime(delay: Int) {
        if (task != null) {
            task!!.cancel()
            task = null
        }
        if (timer != null) {
            timer!!.purge()
            timer!!.cancel()
            timer = null
        }
        if (task == null) {
            task = object : TimerTask() {
                override fun run() {
                    Log.d(TAG, ">>>>>>startCheckPlayTime: $mCurrentPlayingTime")
                    if ( /*mCurrentPlayingTime != 0 && mLastPlayingTime != 0 &&*/mLastPlayingTime == mCurrentPlayingTime) {
                        Log.d(TAG, "Maybe the broadcasting is stopped and then check the Url")
                        if (checkBroadcastIsStopped()) {

                            mHandle.sendEmptyMessage(STATUS_BCAST_STOPPED)
                            return
                        }
                    }
                    mLastPlayingTime = mCurrentPlayingTime
                }
            }
        }
        if (timer == null) timer = Timer()
        timer!!.schedule(task, delay.toLong(), (5 * 1000).toLong())
    }

    private fun checkBroadcastIsStopped(): Boolean {
        if (mConfId == null) return false
        mRTMPUrl = Config.getRtmpUrlByNumberEx(mConfId, null, null)
        val stopped = mFilePath == null || mFilePath!!.isEmpty()
        mBCastIsOver = stopped
        Log.d(TAG, ">>>>>>checkBroadcastIsStopped: $stopped")
        return stopped
    }

    private var mMessageText: TextView? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.act_exo_player)
        mFilePath = intent.getStringExtra("rtmp_url")
        mHttpPath = intent.getStringExtra("http_url")
        mConfId = intent.getStringExtra("conf_id")
        mRTMPUrl = mFilePath

        //Log.d(TAG, "Playing: " + mFilePath);

        // 비디오 재생 레이아웃
        mVideoLayout = findViewById(R.id.video_layout)
        mMessageText = findViewById(R.id.tvMessage)
        with(mMessageText) { this?.setVisibility(View.GONE) }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        //setSize(mVideoWidth, mVideoHeight);
    }

    override fun onResume() {
        super.onResume()
        createPlayer()
    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    fun buildRenderersFactory(
        context: Context
    ): RenderersFactory {
        return DefaultRenderersFactory(context.applicationContext)
            .forceEnableMediaCodecAsynchronousQueueing()
    }

    /**
     * Creates MediaPlayer and plays video
     */
    private fun createPlayer() {
        releasePlayer()

        //initiate Player
        //Create a default TrackSelector
//        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
//        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
//        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
//
        val renderersFactory = buildRenderersFactory(this)

        //Create the player
        mMediaPlayer = ExoPlayer.Builder( /* context= */this)
            .setRenderersFactory(renderersFactory)
            .build()
        val playerView: StyledPlayerView = findViewById(R.id.video_layout)
        playerView.player = mMediaPlayer
        playerView.useController = false

//        RtmpDataSourceFactory rtmpDataSourceFactory = new RtmpDataSourceFactory();
//
//        // This is the MediaSource representing the media to be played.
//        MediaSource videoSource = new ExtractorMediaSource.Factory(rtmpDataSourceFactory)
//                .createMediaSource(Uri.parse(mFilePath));
        if (mFilePath != null) {
            val dataSourceFactory: DataSource.Factory = RtmpDataSource.Factory()
            val mediaSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(mFilePath!!))


            //mMediaPlayer.addListener(this);

            // Prepare the player with the source.
            mMediaPlayer!!.repeatMode = Player.REPEAT_MODE_ALL
            mMediaPlayer!!.setMediaSource(mediaSource)
        } else if (mHttpPath != null) {
            val dataSourceFactory: HttpDataSource.Factory = DefaultHttpDataSource.Factory()
            val mediaSource: MediaSource =
                ProgressiveMediaSource.Factory(dataSourceFactory) //.setDrmSessionManagerProvider(unusedMediaItem -> drmSessionManager)
                    .createMediaSource(MediaItem.fromUri(Uri.parse(mHttpPath)))
            mMediaPlayer!!.setMediaSource(mediaSource)
        }
        mMediaPlayer!!.prepare()
        mMediaPlayer!!.playWhenReady = true
        mMediaPlayer!!.play()

        //auto start playing
        //mMediaPlayer.setPlayWhenReady(true);
    }

    private fun releasePlayer() {
        if (mMediaPlayer == null) return
        if (task != null) {
            task!!.cancel()
            task = null
        }
        if (timer != null) {
            timer!!.purge()
            timer!!.cancel()
            timer = null
        }
        mMediaPlayer!!.stop()
        mMediaPlayer!!.release()
        mMediaPlayer = null
        mVideoWidth = 0
        mVideoHeight = 0
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    companion object {
        private const val TAG = "ActExoPlayer"
        private const val STATUS_OPENNING = 0
        private const val STATUS_BUFFERING = 1
        private const val STATUS_PLAYING = 2
        private const val STATUS_PAUSED = 3
        private const val STATUS_STOPPED = 4
        private const val STATUS_ERROR = 5
        private const val STATUS_END = 6
        private const val STATUS_TIME_CHANGED = 7
        private const val STATUS_POSITION_CHANGED = 8
        private const val STATUS_BCAST_STOPPED = 9
        private const val STATUS_RESET = 10
    }
}