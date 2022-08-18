package com.infowarelab.conference.live

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.*
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ImageSpan
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dq.livemessage.ImageSpanCacheInstance
import com.dq.livemessage.LiveMessageRecyclerHelper
import com.dq.livemessagedemo.tool.*
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.analytics.AnalyticsListener.EventTime
import com.google.android.exoplayer2.decoder.DecoderCounters
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.util.Assertions
import com.infowarelab.conference.ui.activity.ActExoPlayer
import com.infowarelab.conference.ui.view.LodingDialog
import com.infowarelab.hongshantongphone.R
import master.flame.danmaku.controller.DrawHandler
import master.flame.danmaku.controller.IDanmakuView
import master.flame.danmaku.controller.IDanmakuView.OnDanmakuClickListener
import master.flame.danmaku.danmaku.loader.ILoader
import master.flame.danmaku.danmaku.loader.IllegalDataException
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.DanmakuTimer
import master.flame.danmaku.danmaku.model.IDanmakus
import master.flame.danmaku.danmaku.model.IDisplayer
import master.flame.danmaku.danmaku.model.android.BaseCacheStuffer
import master.flame.danmaku.danmaku.model.android.DanmakuContext
import master.flame.danmaku.danmaku.model.android.Danmakus
import master.flame.danmaku.danmaku.model.android.SpannedCacheStuffer
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import master.flame.danmaku.danmaku.parser.IDataSource
import master.flame.danmaku.danmaku.util.IOUtils
import master.flame.danmaku.ui.widget.DanmakuView
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL
import java.util.*

class LiveActivity : AppCompatActivity(), SocketManager.ConnectListener, AnalyticsListener {

    private var mControlBarP: LinearLayout? = null
    private var mControlBarL: LinearLayout? = null

    private lateinit var mSendP: TextView
    private lateinit var mSendL: TextView

    //private val mParser: BaseDanmakuParser? = null
    private val videoRatioPortrait: Float = 0.5625F
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
    private val mCurrentPlayingTime = 0
    private var mLastPlayingTime = 0
    private var mConfId: String? = null
    private var mRTMPUrl: String? = null
    private var mMessageText: TextView? = null
    private var landscape = false
    private var screenWidth = 0
    private var screenHeight = 0

    private var mDanmakuView: IDanmakuView? = null
    private var mDanmakuLayout: DanmakuView? = null
    private var mContext: DanmakuContext? = null

    companion object {
        private const val TAG = "LiveActivity"
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

    //View
    private lateinit var recyclerView: RecyclerView
    private lateinit var mAdapter: LiveMessageAdapter
    private lateinit var unreadTipsTextView: TextView

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

    //辅助类
    private val liveMessageRecyclerHelper: LiveMessageRecyclerHelper<LiveMessageModel> by lazy {
        LiveMessageRecyclerHelper<LiveMessageModel>(this)
    }

    //直播间 - 左下角公屏RV - 其中一条Item消息 - TextView的解析策略
    private val liveMessageTextViewHelper: LiveMessageTextViewHelper by lazy {
        val liveMessageTextViewHelper = LiveMessageTextViewHelper(this)
        liveMessageTextViewHelper.imageSpanStrategyList = arrayListOf()
        liveMessageTextViewHelper.imageSpanStrategyList.apply {
            //解析textview头tag的策略
            add(LiveMessageImageSpanTitleStrategy())
            //解析用户等级的策略
            add(LiveMessageImageSpanLevelStrategy())
            //解析勋章的策略
            add(LiveMessageImageSpanMedalStrategy())
        }

        liveMessageTextViewHelper.textSpanStrategyList = arrayListOf()
        liveMessageTextViewHelper.textSpanStrategyList.apply {
            //解析用户昵称的策略
            add(LiveMessageTextSpanNameStrategy())
            //解析消息体的策略
            add(LiveMessageTextSpanMessageStrategy())
        }
        liveMessageTextViewHelper
    }

    private lateinit var socketManager: SocketManager

    //测试用的。1秒内有几条新消息来
    private var messageCountInOneSecond = 0

    //测试用的。缓存区最多消息数量。实际开发时候你不需要把这个设置为全局变量
    private var maxCacheCount = 5

    private val mParser: BaseDanmakuParser = object : BaseDanmakuParser() {
        override fun parse(): IDanmakus {
            return Danmakus()
        }
    }

    private val mCacheStufferAdapter: BaseCacheStuffer.Proxy = object : BaseCacheStuffer.Proxy() {
        private var mDrawable: Drawable? = null
        override fun prepareDrawing(danmaku: BaseDanmaku, fromWorkerThread: Boolean) {
            if (danmaku.text is Spanned) { // 根据你的条件检查是否需要需要更新弹幕
                // FIXME 这里只是简单启个线程来加载远程url图片，请使用你自己的异步线程池，最好加上你的缓存池
                object : Thread() {
                    override fun run() {
                        val url = "http://www.bilibili.com/favicon.ico"
                        var inputStream: InputStream? = null
                        var drawable = mDrawable
                        if (drawable == null) {
                            try {
                                val urlConnection = URL(url).openConnection()
                                inputStream = urlConnection.getInputStream()
                                drawable = BitmapDrawable.createFromStream(inputStream, "bitmap")
                                mDrawable = drawable
                            } catch (e: MalformedURLException) {
                                e.printStackTrace()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            } finally {
                                IOUtils.closeQuietly(inputStream)
                            }
                        }
                        if (drawable != null) {
                            drawable.setBounds(0, 0, 100, 100)
                            val spannable: SpannableStringBuilder? = createSpannable(drawable)
                            danmaku.text = spannable
                            if (mDanmakuView != null) {
                                mDanmakuView!!.invalidateDanmaku(danmaku, false)
                            }
                            return
                        }
                    }
                }.start()
            }
        }

        override fun releaseResource(danmaku: BaseDanmaku) {
            // TODO 重要:清理含有ImageSpan的text中的一些占用内存的资源 例如drawable
        }
    }

    private fun createSpannable(drawable: Drawable): SpannableStringBuilder? {
        val text = "bitmap"
        val spannableStringBuilder = SpannableStringBuilder(text)
        val span = ImageSpan(drawable) //ImageSpan.ALIGN_BOTTOM);
        spannableStringBuilder.setSpan(span, 0, text.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        spannableStringBuilder.append("图文混排")
        spannableStringBuilder.setSpan(
            BackgroundColorSpan(Color.parseColor("#8A2233B1")),
            0,
            spannableStringBuilder.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        return spannableStringBuilder
    }

    private fun createParser(stream: InputStream?): BaseDanmakuParser? {
        if (stream == null) {
            return object : BaseDanmakuParser() {
                override fun parse(): Danmakus {
                    return Danmakus()
                }
            }
        }
        val loader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_BILI)
        try {
            loader.load(stream)
        } catch (e: IllegalDataException) {
            e.printStackTrace()
        }
        val parser: BaseDanmakuParser = BiliDanmukuParser()
        val dataSource = loader.dataSource
        parser.load(dataSource)
        return parser
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        initOrientation()

        setContentView(R.layout.activity_live)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        initView()

        socketManager = SocketManager()

        socketManager.mConnectListener = this

        mConfId?.let { socketManager.start(it) }

    }

    private fun getOrientationState(): Int {
        val mConfiguration = this.resources.configuration
        return mConfiguration.orientation
    }

    private fun initOrientation() {
        val dm = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindowManager().getDefaultDisplay().getRealMetrics(dm)
        } else {
            getWindowManager().getDefaultDisplay().getMetrics(dm)
        }
        val orientation: Int = getOrientationState()
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            screenWidth = dm.widthPixels
            screenHeight = dm.heightPixels

            landscape = false
        } else {
            screenWidth = dm.heightPixels
            screenHeight = dm.widthPixels
            landscape = true
        }

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR;

        //if (screenHeight / screenWidth > screenRatio) {
            //竖屏
//            if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
//                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
//            }
//            landscape = false
//        } else {
//            if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
//                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
//            }
//            landscape = true
//        }
    }

    private fun initView() {

        mFilePath = intent.getStringExtra("rtmp_url")
        mHttpPath = intent.getStringExtra("http_url")
        mConfId = intent.getStringExtra("conf_id")
        mRTMPUrl = mFilePath

        mVideoLayout = findViewById(R.id.video_layout)
        mMessageText = findViewById(R.id.tvMessage)
        with(mMessageText) { this?.setVisibility(View.GONE) }
        recyclerView = findViewById(R.id.recycler_view)
        unreadTipsTextView = findViewById(R.id.unread_tv)

        // DanmakuView

        // 设置最大显示行数
        // DanmakuView

        // 设置最大显示行数
        val maxLinesPair = HashMap<Int, Int>()
        maxLinesPair[BaseDanmaku.TYPE_SCROLL_RL] = 5 // 滚动弹幕最大显示5行

        // 设置是否禁止重叠
        val overlappingEnablePair = HashMap<Int, Boolean>()
        overlappingEnablePair[BaseDanmaku.TYPE_SCROLL_RL] = true
        overlappingEnablePair[BaseDanmaku.TYPE_FIX_TOP] = true

        mDanmakuView = findViewById<View>(R.id.sv_danmaku) as IDanmakuView
        mDanmakuLayout = findViewById(R.id.sv_danmaku)

        mContext = DanmakuContext.create()

        mContext?.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3f)
            ?.setDuplicateMergingEnabled(false)?.setScrollSpeedFactor(1.2f)?.setScaleTextSize(1.2f)
            ?.setCacheStuffer(
                SpannedCacheStuffer(),
                mCacheStufferAdapter
            ) // 图文混排使用SpannedCacheStuffer
            //        .setCacheStuffer(new BackgroundCacheStuffer())  // 绘制背景使用BackgroundCacheStuffer
            ?.setMaximumLines(maxLinesPair)
            ?.preventOverlapping(overlappingEnablePair)?.setDanmakuMargin(40)
        if (mDanmakuView != null) {
            //mParser = createParser(this.resources.openRawResource(R.raw.comments))
            mDanmakuView!!.setCallback(object : DrawHandler.Callback {
                override fun updateTimer(timer: DanmakuTimer) {}
                override fun drawingFinished() {}
                override fun danmakuShown(danmaku: BaseDanmaku) {
//                    Log.d("DFM", "danmakuShown(): text=" + danmaku.text);
                }

                override fun prepared() {
                    mDanmakuView!!.start()
                }
            })
            mDanmakuView!!.setOnDanmakuClickListener(object : OnDanmakuClickListener {
                override fun onDanmakuClick(danmakus: IDanmakus): Boolean {
                    Log.d("DFM", "onDanmakuClick: danmakus size:" + danmakus.size())
                    val latest = danmakus.last()
                    if (null != latest) {
                        Log.d("DFM", "onDanmakuClick: text of latest danmaku:" + latest.text)
                        return true
                    }
                    return false
                }

                override fun onDanmakuLongClick(danmakus: IDanmakus): Boolean {
                    return false
                }

                override fun onViewClick(view: IDanmakuView): Boolean {
                    //mMediaController.setVisibility(View.VISIBLE)
                    return false
                }
            })
            mDanmakuView!!.prepare(mParser, mContext)
            mDanmakuView!!.showFPS(false)
            mDanmakuView!!.enableDanmakuDrawingCache(true)
        }

        if (!landscape) {
            val params = mVideoLayout?.getLayoutParams() as FrameLayout.LayoutParams;
            params.width = screenWidth
            params.height = ((params.width * videoRatioPortrait).toInt())
            mVideoLayout?.setLayoutParams(params)
            mDanmakuLayout?.setLayoutParams(params)
        }

        //设置RecyclerView
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager

        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State ) {
                outRect.bottom = dip2px(7f)
            }
        })

        //RecyclerView创建Adapter
        mAdapter = LiveMessageAdapter(this, liveMessageRecyclerHelper.list)
        recyclerView.adapter = mAdapter
        //本库作者DQ：为了感知到"底部还有XX条消息"，希望你能在Adapter里实现这个方法，并回调给我的库
        mAdapter.onItemViewAttachedListener = object: LiveMessageAdapter.OnItemViewAttachedListener {
            override fun onItemViewAttached(adapterPosition: Int) {
                liveMessageRecyclerHelper.onAdapterItemViewAttached(adapterPosition)
            }
        }

        //给我们的库 liveMessageRecyclerHelper，绑定recyclerView
        liveMessageRecyclerHelper.setRecyclerView(recyclerView)
        liveMessageRecyclerHelper.setUnreadTipsView(unreadTipsTextView)
        liveMessageRecyclerHelper.messageRecyclerHelperListener = object: LiveMessageRecyclerHelper.LiveMessageRecyclerHelperListener<LiveMessageModel> {
            override fun unreadMessageCountUpdate(unreadCount: Int) {
                unreadTipsTextView.setText("还有"+unreadCount +"条未读")
            }

            override fun asyncParseSpannableString(model: LiveMessageModel) {
                //这一步是在子线程中
                model.spannableString = liveMessageTextViewHelper.displaySpannableString(model)

                model.userid?.let { model.message?.let { it1 -> addDanmaku(it, it1, true) } }
            }
        }

        //RecyclerView最短刷新时间间隔（0秒 - 2.0秒）
        liveMessageRecyclerHelper.diffRefreshDuration = (intent.getFloatExtra("minRefreshTime",0.6f) * 1000).toLong()
        //缓存区最多消息数量。实际开发时候你不需要把这个设置为全局变量
        maxCacheCount = intent.getIntExtra("maxCacheCount", 5)

        mSendP = findViewById<TextView>(R.id.insert_tv);
        mSendP?.setOnClickListener {
            //模拟来一条消息
            socketManager.receivedLiveMessage()
        }

        mSendL = findViewById<TextView>(R.id.insert_tv_l);
        mSendL?.setOnClickListener {
            //模拟来一条消息
            socketManager.receivedLiveMessage()
        }

        mControlBarP = findViewById(R.id.appbar)
        mControlBarL = findViewById(R.id.appbar_l)

        if (landscape){
            mControlBarP?.visibility = View.GONE
            mControlBarL?.visibility = View.VISIBLE
        }
        else {
            mControlBarL?.visibility = View.GONE
            mControlBarP?.visibility = View.VISIBLE
        }

//        findViewById<View>(R.id.insert_infinite_tv).setOnClickListener {
//            //模拟无限来消息
//            messageCountInOneSecond += 2
//            it as TextView
//            //it.text = "自动每秒插入"+messageCountInOneSecond+"条ing"
//            socketManager.receivedInfiniteLiveMessage()
//        }
//
//        findViewById<View>(R.id.stop_tv).setOnClickListener {
//            messageCountInOneSecond = 0
//            findViewById<TextView>(R.id.insert_infinite_tv).text = "自动每秒插入"+2+"条"
//            socketManager.stopInfiniteLiveMessage()
//        }

        unreadTipsTextView.setOnClickListener { v ->
            //点击"底部还有N条消息"
            v.visibility = View.GONE

            liveMessageRecyclerHelper.scrollToBottom()
        }
    }

    //收到服务器推送来的新消息
    override fun onLiveMessageReceived(liveMessageModel: LiveMessageModel) {

        Log.e("dq","AC 最原始收到消息")

        if (liveMessageRecyclerHelper.willInsertList.count() > maxCacheCount){
            //缓冲区满了，丢弃这条消息。你也可以判断一下if == 礼物消息 就不丢弃
            Log.e("InfowareLab.Debug","缓冲区满了，丢弃掉")
        } else {
            //准备异步拼接SpannableString。调用完这个方法，会触发asyncParseSpannableString回调
            liveMessageRecyclerHelper.prepareAsyncParseSpannable(liveMessageModel)
        }
    }

    private fun addDanmaku(userHash: String, message: CharSequence, islive: Boolean) {
        val danmaku = mContext!!.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL)
        if (danmaku == null || mDanmakuView == null) {
            return
        }

        danmaku.text = message
        danmaku.userHash = userHash
        danmaku.padding = 5
        danmaku.priority = 0 // 可能会被各种过滤器过滤并隐藏显示
        danmaku.isLive = islive
        danmaku.time = mDanmakuView!!.currentTime + 1200
        danmaku.textSize = 25f * (mParser?.getDisplayer().getDensity() - 0.6f);
        danmaku.textColor = Color.RED
        danmaku.textShadowColor = Color.WHITE
        //danmaku.underlineColor = Color.GREEN;
        //danmaku.borderColor = Color.GREEN
        mDanmakuView!!.addDanmaku(danmaku)
    }

    fun dip2px(dpValue: Float): Int {
        val scale: Float = getResources().getDisplayMetrics().density
        return (dpValue * scale + 0.5f).toInt()
    }

    private fun buildRenderersFactory(
        context: Context
    ): RenderersFactory {
        return DefaultRenderersFactory(context.applicationContext)
            .forceEnableMediaCodecAsynchronousQueueing()
    }

    private fun relayoutVideoView(){

        var videoRadio: Float = 0f

        val screenRadio = screenWidth.toFloat() / screenHeight.toFloat()

        if (mVideoWidth > 0 && mVideoHeight > 0) {
            videoRadio = mVideoWidth.toFloat() / mVideoHeight.toFloat()
        }
        else {
            return
        }

        if (!landscape) {
            val params = mVideoLayout?.getLayoutParams() as FrameLayout.LayoutParams

            if (videoRadio > screenRadio) {
                params.width = screenWidth
                params.height = (((params.width / videoRadio).toInt()))

                mVideoLayout?.setLayoutParams(params)
                mDanmakuLayout?.setLayoutParams(params)
            }
            else
            {
                params.width = screenWidth
                params.height = screenHeight;

                mVideoLayout?.setLayoutParams(params)
                mDanmakuLayout?.setLayoutParams(params)
            }
        }
        else
        {
            val params = mVideoLayout?.getLayoutParams() as FrameLayout.LayoutParams

            params.width = screenWidth
            params.height = screenHeight;

            mVideoLayout?.setLayoutParams(params)
            mDanmakuLayout?.setLayoutParams(params)
        }
    }

    // AnalyticsListener
    override fun onEvents(player: Player, events: AnalyticsListener.Events) {
        if (events.contains(AnalyticsListener.EVENT_IS_PLAYING_CHANGED)) {
//            if (player.isPlaying) {
//                lastPlayingStartTimeMs = SystemClock.elapsedRealtime()
//            } else {
//                totalPlayingTimeMs += SystemClock.elapsedRealtime() - lastPlayingStartTimeMs
//            }
        }
        if (events.contains(AnalyticsListener.EVENT_PLAYER_ERROR)) {
            // The exception is guaranteed to be an ExoPlaybackException because the underlying player is
            // an ExoPlayer instance.
//            playerError = Assertions.checkNotNull(player.playerError) as ExoPlaybackException
//            onPlayerErrorInternal(playerError)
        }
        if (events.contains(AnalyticsListener.EVENT_PLAYBACK_STATE_CHANGED)) {
//            val playbackState = player.playbackState
//            if (playbackState == Player.STATE_ENDED || playbackState == Player.STATE_IDLE) {
//                stopTest()
//            }
        }

        if (events.contains(AnalyticsListener.EVENT_VIDEO_SIZE_CHANGED)){

            mVideoWidth = player.videoSize.width
            mVideoHeight = player.videoSize.height

            relayoutVideoView()
        }
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
        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT;

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

        mMediaPlayer!!.addAnalyticsListener(this)

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


    override fun onResume() {
        super.onResume()
        createPlayer()
        if (mDanmakuView != null && mDanmakuView!!.isPrepared && mDanmakuView!!.isPaused) {
            mDanmakuView!!.resume()
        }
        mConfId?.let { socketManager.start(it) }
    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
        socketManager.stop()

        if (mDanmakuView != null && mDanmakuView!!.isPrepared) {
            mDanmakuView!!.pause()
        }

    }

    //FM.onPause -> AC.onPause -> FM.onStop -> AC.onStop -> FM.onDestroyView -> FM.onDestroy -> FM.onDetach -> AC.onDestroy
    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
        socketManager.stop()
        if (mDanmakuView != null) {
            // dont forget release!
            mDanmakuView!!.release()
            mDanmakuView = null
        }
        socketManager.mConnectListener = null
        liveMessageRecyclerHelper.destroy()
        ImageSpanCacheInstance.instance.clearAll()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        val dm = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindowManager().getDefaultDisplay().getRealMetrics(dm)
        } else {
            getWindowManager().getDefaultDisplay().getMetrics(dm)
        }

        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mDanmakuView!!.config.setDanmakuMargin(20)
            recyclerView?.visibility = View.VISIBLE;
            mControlBarL?.visibility = View.GONE
            mControlBarP?.visibility = View.VISIBLE
            //unreadTipsTextView?.visibility = View.VISIBLE
            landscape = false
            screenWidth = dm.widthPixels
            screenHeight = dm.heightPixels
            relayoutVideoView()
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mDanmakuView!!.config.setDanmakuMargin(40)
            recyclerView?.visibility = View.GONE;
            unreadTipsTextView?.visibility = View.GONE
            mControlBarP?.visibility = View.GONE
            mControlBarL?.visibility = View.VISIBLE
            landscape = true
            screenWidth = dm.widthPixels
            screenHeight = dm.heightPixels
            relayoutVideoView()
        }
    }

}