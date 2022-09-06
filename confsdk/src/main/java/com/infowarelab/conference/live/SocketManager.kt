package com.infowarelab.conference.live

import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.infowarelab.conference.utils.DESUtil
import com.infowarelabsdk.conference.domain.LiveServiceBean
import com.infowarelabsdk.conference.transfer.Config
import com.infowarelabsdk.conference.util.StringUtil
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
//import io.crossbar.autobahn.websocket.WebSocketConnection
//import io.crossbar.autobahn.websocket.WebSocketConnectionHandler
//import io.crossbar.autobahn.websocket.exceptions.WebSocketException
//import io.crossbar.autobahn.websocket.interfaces.IWebSocket
//import io.crossbar.autobahn.websocket.types.WebSocketOptions
import okio.Buffer
import java.lang.ref.WeakReference
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs


class SocketManager {

    private var liveServiceBean: LiveServiceBean? = null
    private var mNeedToStop: Boolean? = false

    //json解析
    private val jsonAdapter: JsonAdapter<LiveMessageModel> = Moshi.Builder().build().adapter<LiveMessageModel>(LiveMessageModel::class.java)
    private var jsonBuffer: Buffer = Buffer()

    //private val mConnection: IWebSocket = WebSocketConnection()
    private val mHostname: String? = "rtcbeta.hongshantong.cn"
    private val mPort: String? = "9012"
    private val TAG: String ? = "InfowareLab.WebSocket"

    var mConnectListener: ConnectListener? = null

    private val handler = MyHandler(this)

    private class MyHandler(context: SocketManager) : Handler(Looper.getMainLooper()) {

        private val reference: WeakReference<SocketManager> = WeakReference(context)

        override fun handleMessage(msg: Message) {
            val socketManager = reference.get() as SocketManager? ?: return
            when (msg.what) {
                1 -> {
                    socketManager.receivedLiveMessage()
                }
                2 -> {
                    socketManager.receivedLiveMessage()

                    //模拟每0.5秒推送来一条数据
                    socketManager.handler.sendEmptyMessageDelayed(2 , 500)
                }
                3 -> {
                    socketManager.reconnect()
                }
            }
        }
    }

    public fun start(confId: String): Boolean {

        liveServiceBean = Config.getLiveServiceInfo(confId) ?: return false

        var msgStr = "{\"ip\":$(liveServiceBean.ip),\"uid\":\$(liveServiceBean.visitUid)}"

        //var msgStr = JSON.stringify(msgJson);

        msgStr = Uri.encode(msgStr)

        var enmsgStr: String? = DESUtil.encryption(msgStr, liveServiceBean!!.token)
        var timestamp =  liveServiceBean!!.timestamp
        var signature = DESUtil.MD5(liveServiceBean!!.token + enmsgStr).toString()
        enmsgStr = URLEncoder.encode(enmsgStr);

        var url = liveServiceBean!!.liveChatUrl+ liveServiceBean!!.liveChatContextPath+"/chat/" + liveServiceBean!!.confId+"/"+ liveServiceBean!!.nickname+"?timestamp="+timestamp+"&encryptmsg="+enmsgStr+"&signature="+signature+"&appId="+ liveServiceBean!!.appId+"&visitUid="+ liveServiceBean!!.visitUid +"&nickType="+ liveServiceBean!!.nickType

        Log.d(TAG, "SocketManager: start: url =" + url)

//        var hostname = mHostname!!
//        if (!hostname.startsWith("ws://") && !hostname.startsWith("wss://")) {
//            hostname = "ws://$hostname"
//        }
//        val port = mPort!!
//        val wsuri: String
//        wsuri = if (port.isNotEmpty()) {
//            "$hostname:$port"
//        } else {
//            hostname
//        }
        /*val connectOptions = WebSocketOptions()
        connectOptions.reconnectInterval = 5000
        try {
            mConnection.connect(url, object : WebSocketConnectionHandler() {
                override fun onOpen() {
                    Log.d(TAG, "SocketManager: onOpen")

                }
                override fun onMessage(payload: String) {
                    Log.d(TAG, "SocketManager: onMessage: $payload")
                    val messageModel = jsonAdapter.fromJson(jsonBuffer.writeUtf8(payload))

                    mConnectListener!!.onLiveMessageReceived(messageModel!!)
                }

                override fun onClose(code: Int, reason: String) {
                    Log.d(TAG, "SocketManager: onClose：$reason")

                    if (false == mNeedToStop){
                        handler.sendEmptyMessageDelayed(3 , 2000)
                    }
                }
            }, connectOptions)
        } catch (e: WebSocketException) {
            Log.d(TAG, e.toString())
        }*/

        return true
    }


    public fun stop(){
        mNeedToStop = true;
//        if (mConnection.isConnected) {
//            mConnection.sendClose()
//        }
    }

    /*public fun sendMessage(message: String): Boolean {

        if (mConnection.isConnected) {

            if(message.length > 4000){
                //pubFun.alert(platform.messagetoolong)
                return false
            }

            var method="boardcast"
            var msgStr = "{\"uid\":${liveServiceBean?.visitUid},\"method\":${method},\"data\":${Uri.encode(message)},\"nickType\":${liveServiceBean?.nickType}"

            msgStr = Uri.encode(msgStr);
            var enmsgStr: String? = DESUtil.encryption(msgStr, liveServiceBean!!.token)
            //var timestamp =  liveServiceBean!!.timestamp
            var signature = DESUtil.MD5(liveServiceBean!!.token + enmsgStr).toString()
            enmsgStr = URLEncoder.encode(enmsgStr);

            var requestStr = "{\"appId\":${liveServiceBean!!.token},\"timestamp\":${liveServiceBean!!.timestamp},\"signature\":${signature},\"encryptmsg\":${enmsgStr}"

            //var requestStr = JSON.stringify(requestJson);

            mConnection.sendMessage(requestStr)
            return true
        }
        else
        {
            reconnect()
            return false
        }
    }*/

    private fun initFakeBeanJson(): String{
        val message = LiveMessageModel()
        message.userid = "123"
        message.name = "大主播"+abs(Random().nextInt() % 999)
        message.level = abs(Random().nextInt() % 50)
        message.medal = if (Random().nextInt() % 2 == 0)  "粉丝团" else "僵尸"

        //定好好9张图的名字前缀
        val sdf = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss.SSS")
        val time = sdf.format(Date())
        message.message = if (Random().nextInt() % 2 == 0)  ("我是大主播，这是我说的话，当前时间是$time") else "有问题加我QQ"
        message.type = if (Random().nextInt() % 4 == 0) LiveMessageIntrinsicModel.TYPE_COMING else LiveMessageIntrinsicModel.TYPE_MESSAGE
        message.medalLevel = abs(Random().nextInt() % 90)

        if (Random().nextInt() % 3 == 0) {
            message.tips = "榜1"
        } else if (Random().nextInt() % 2 == 0) {
            message.tips = "VIP"
        } else {
            message.tips = null
        }

        return jsonAdapter.toJson(message)
    }

    fun reconnect(){
        /*if (mConnection != null){
            //把刚收到的消息json回调给LiveActivity。
            // 本库作者DQ建议：其实这里用两种做法：
            //1，如果这个消息通道里只有"文字消息" 没有礼物。那么我建议回调原始json给Activity。因为这样可以视情况屏蔽过多的消息（压根就不解析json，减少内存和cpu损耗）
            //2，如果这个消息通道里有"礼物消息"，那么还是需要回调model，因为毕竟不适合丢弃礼物消息。然后再：if==文字消息 {视情况丢弃一部分}
            //本demo按照方案2来，毕竟绝大多数app也没那么高的并发。等你们用户量高到一定程度再升级为方案1

            if (mConnection.isConnected) return;

            var hostname = mHostname!!
            if (!hostname.startsWith("ws://") && !hostname.startsWith("wss://")) {
                hostname = "ws://$hostname"
            }
            val port = mPort!!
            val wsuri: String
            wsuri = if (port.isNotEmpty()) {
                "$hostname:$port"
            } else {
                hostname
            }

            val connectOptions = WebSocketOptions()
            connectOptions.setReconnectInterval(5000)
            try {
                mConnection.connect(wsuri, object : WebSocketConnectionHandler() {
                    override fun onOpen() {

                    }
                    override fun onMessage(payload: String) {
                        Log.d(TAG, "SocketManager: onMessage: $payload")
                        val messageModel = jsonAdapter.fromJson(jsonBuffer.writeUtf8(payload))

                        mConnectListener!!.onLiveMessageReceived(messageModel!!)
                    }

                    override fun onClose(code: Int, reason: String) {
                        Log.d(TAG, "SocketManager: onClose：$reason")

                        if (false == mNeedToStop){
                            handler.sendEmptyMessageDelayed(3 , 2000)
                        }
                    }
                }, connectOptions)
            } catch (e: WebSocketException) {
                Log.d(TAG, e.toString())
            }
        }*/
    }

    //模拟收到直播间的《弹幕消息》，注意这里是弹幕消息。直播间其他消息注意区分：比如pk、主播下播
    fun receivedLiveMessage(){
        if (mConnectListener != null){
            //把刚收到的消息json回调给LiveActivity。
            // 本库作者DQ建议：其实这里用两种做法：
                //1，如果这个消息通道里只有"文字消息" 没有礼物。那么我建议回调原始json给Activity。因为这样可以视情况屏蔽过多的消息（压根就不解析json，减少内存和cpu损耗）
                //2，如果这个消息通道里有"礼物消息"，那么还是需要回调model，因为毕竟不适合丢弃礼物消息。然后再：if==文字消息 {视情况丢弃一部分}
                //本demo按照方案2来，毕竟绝大多数app也没那么高的并发。等你们用户量高到一定程度再升级为方案1

            val json = initFakeBeanJson();

            val messageModel = jsonAdapter.fromJson(jsonBuffer.writeUtf8(json))

            mConnectListener!!.onLiveMessageReceived(messageModel!!)
        }
    }

    //模拟一直收到消息
    fun receivedInfiniteLiveMessage(){
        handler.sendEmptyMessageDelayed(2 , 500)
    }

    //停止上面的模拟一直收到消息
    fun stopInfiniteLiveMessage(){
        handler.removeMessages(2)
    }

    interface ConnectListener {
        fun onLiveMessageReceived(liveMessageModel: LiveMessageModel)
    }
}