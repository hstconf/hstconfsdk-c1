package com.infowarelab.conference.ui.activity.inconf.view.video;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.UserCommonImpl;
import com.infowarelabsdk.conference.common.impl.VideoCommonImpl;

import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

@SuppressLint("NewApi")
public class VideoDecodeView extends FrameLayout implements SurfaceHolder.Callback {

    private boolean isSvc = false;
    private boolean isCreated = false;

    protected static final int VIDEO_SYNC = 8888;
    protected static final int VIDEO_MYSELF = 6666;
    public static boolean isSoftDrawing = false;
    /**
     * surface生命周期
     **/
    private SurfaceHolder holder;


    private Context activity;
    /**
     * 当前预览的channelID
     **/
    private int channelId;
    /**
     * 当前视频的用户
     **/
    private int userID;
    /**
     * 视频SDK接口
     **/
    protected CommonFactory commonFactory = CommonFactory.getInstance();
    private VideoCommonImpl videoCommon = (VideoCommonImpl) commonFactory.getVideoCommon();
    ;
    private UserCommonImpl userCommon = (UserCommonImpl) commonFactory.getUserCommon();
    private boolean isSupport = true;
    private boolean isWaiting = false;
    private SurfaceView surface;
    private TextView userName;
    private Button deleteView;
    private LinearLayout relative;
    private TextView tvWait;
    private LinearLayout.LayoutParams cameraParames;
    private LinearLayout.LayoutParams delParames;
    private int screenW;
    private int screenH;
    private int pHeight;

    private IOnDeleteAndCameraClick onDeleteAndCameraClick;
    private int surfaceWidth = 0;
    private int surfaceHeight = 0;

    public VideoDecodeView(Context context) {
        super(context);
        initParames(context, 0, 0, 0, 0);
    }

    public VideoDecodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.activity = context;
        initParames(context, 0, 0, 0, 0);
    }

    public VideoDecodeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.activity = context;
        initParames(context, 0, 0, 0, 0);
    }

    /**
     * 初始化布局
     * 默认字体12sp,white。
     */
    private void initParames(Context context, int textSizeSp, int textColor, int cameraSrc, int deleteSrc) {

//		int txtSize = textSizeSp>0?textSizeSp:12;
//
//		int txtColor = textSizeSp!=0?textSizeSp:Color.WHITE;//0是透明我们取白色
//
//
//		int draDelete = deleteSrc!=0?deleteSrc:android.R.drawable.ic_menu_close_clear_cancel;
//
//	    //**用户名设置**//
//        userName = new TextView(context);
//        userName.setTextSize(TypedValue.COMPLEX_UNIT_SP,txtSize);
//        userName.setTextColor(txtColor);
//        userName.setBackgroundColor(Color.TRANSPARENT);
//        userName.setGravity(Gravity.CENTER);
//        userName.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
//                LayoutParams.WRAP_CONTENT,Gravity.BOTTOM));
//        //**用户名设置**//
//
//        //**在右上角加入相机图标和删除图标**//
//        relative = new LinearLayout(context);
//        relative.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
//                LayoutParams.WRAP_CONTENT,Gravity.RIGHT|Gravity.TOP));
//
//
//        delParames = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
//        		LayoutParams.WRAP_CONTENT);
//        deleteView = new Button(context);
//		deleteView.setLayoutParams(delParames);
//        deleteView.setBackgroundResource(draDelete);
//        deleteView.setVisibility(View.GONE);
//        relative.addView(deleteView);
//        //**在右上角加入相机图标和删除图标**//
//
//		addView(userName);
//		addView(relative);
//
//		this.deleteView.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				clickDelete();
//			}
//		});
    }

    private PaintFlagsDrawFilter pfdf = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private ByteBuffer buffer;
    private Bitmap videoBit;
    private int bitWidth = 0;
    private int bitHeight = 0;

    /**
     * 重置视频分辨率
     *
     * @param width
     * @param height
     */
    public void resetSize(int width, int height) {
        if (!videoCommon.isHardDecode()) {
            dataQueue.clear();
        }
        bitWidth = width;
        bitHeight = height;
        if (bitWidth > 0 && bitHeight > 0) {
            videoBit = Bitmap.createBitmap(width, height, Config.RGB_565);
        }
        if (bitHeight * bitWidth > 1024 * 768 && width > 0 && !videoCommon.isHardDecode()) {
            setBackgroundResource(R.drawable.bg_video_nosupport);
            isSupport = false;
        } else {
            setBackgroundResource(0);
            isSupport = true;
        }
//		setParams(screenW, screenH);
        setSurface(width, height);
        invalidate();
    }

    public void resetSizeWithScreenSize(int width, int height) {
        if (!videoCommon.isHardDecode()) {
            dataQueue.clear();
        }
//        bitWidth = width;
//        bitHeight = height;
//        if (bitWidth > 0 && bitHeight > 0) {
//            videoBit = Bitmap.createBitmap(bitWidth, bitHeight, Config.RGB_565);
//        }
//        if (bitHeight * bitWidth > 1024 * 768 && bitWidth > 0 && !videoCommon.isHardDecode()) {
//            setBackgroundResource(R.drawable.bg_video_nosupport);
//            isSupport = false;
//        } else {
//            setBackgroundResource(0);
//            isSupport = true;
//        }
		setParams(width, height);
        if (bitWidth > 0 && bitHeight > 0)
            setSurface(bitWidth, bitHeight);
        //setSurface(surfaceWidth, surfaceHeight);
        invalidate();
    }

    public void setSurface(int w, int h) {
        if (screenH != 0 && screenW != 0 && surface != null) {
            if (w > 0 && h > 0) {
                LayoutParams p = (LayoutParams) surface.getLayoutParams();
                float ss = 1f * screenH / screenW;
                float vs = 1f * h / w;
                if (ss == vs) {
                    p.width = screenW;
                    p.height = screenH;
                } else if (ss > vs) {
                    p.width = screenW;
                    p.height = (int) (screenW * vs);
                } else {
                    p.height = screenH;
                    p.width = (int) (1f * screenH / vs);
                }
                //int padding = 1;
                p.setMargins(1,1,1,1);
                p.width -= 2;
                p.height -= 2;
                surface.setLayoutParams(p);
                surfaceWidth = p.width;
                surfaceHeight = p.height;
            } else {
//                LayoutParams p = (LayoutParams) surface.getLayoutParams();
//                p.width = LayoutParams.MATCH_PARENT;
//                p.height = LayoutParams.MATCH_PARENT;
//                surface.setLayoutParams(p);
            }
        }
    }

    /**
     * 视频接收/停止接收
     *
     * @param channelID
     * @param isOpen
     */
    public void changeStatus(int channelID, boolean isOpen) {
        if (videoCommon.isHardDecode()) {
            if (isOpen) {
                this.channelId = channelID;
                if (null != surface) {
                    surface.getHolder().getSurface().release();
                    removeView(surface);
                    surface = null;
                    removeNameCamera();
                }
                surface = new SurfaceView(this.activity);
                surface.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT, Gravity.CENTER));

                holder = surface.getHolder();
                holder.addCallback(this);
                addView(surface);
                setTag(channelID);
                showWait();
                addName();
            } else {
                if (this.channelId != 0) {
                    videoCommon.closeVideo(this.channelId);
                }
                this.channelId = 0;
                if (null != surface) {
                    surface.getHolder().getSurface().release();
                    removeView(surface);
                    surface = null;
                    removeNameCamera();
                }
            }
        } else {
            if (isOpen) {
                this.channelId = channelID;
                if (null != surface) {
                    removeNameCamera();
                    // 别人
                    setTag(channelID);
                    showWait();
                    addName();
                    if (videoCommon.getDeviceMap().containsKey(channelId)) {
                        this.userID = videoCommon.getDeviceMap().get(
                                channelId);
                        setUserName();
                    }
                    HandlerThread surfaceThread = new HandlerThread(
                            "SurfaceHandler");
                    surfaceThread.start();
                    videoCommon.openVideo(channelId, surface);
                    videoCommon.addSurfaceHanlder(surfaceThread,
                            new SurfaceHandler(surfaceThread.getLooper()),
                            channelId);
                    dataQueue.clear();
                } else {
                    surface = new SurfaceView(this.activity);
                    surface.setLayoutParams(new LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            LayoutParams.MATCH_PARENT, Gravity.CENTER));
                    holder = surface.getHolder();
                    holder.addCallback(this);
                    addView(surface);
                    setTag(channelID);
                    showWait();
                    addName();
                }
            } else {
                videoCommon.closeVideo(channelID);
            }
        }
    }

    private void showWait() {
        this.isWaiting = true;
        //if (null != surface)
            //surface.setBackgroundResource(R.drawable.bg_sharedt_wait);
        //    surface.setBackgroundColor(R.color.app_video_background);
    }

    public void showSupport(boolean isSupport) {
        this.isSupport = isSupport;
        showSupport();
    }

    private void showSupport() {
        if (isSupport) {
            if (isWaiting) {
                showWait();
            } else {
                showSurface();
            }
        } else {
            surface.setBackgroundResource(R.drawable.bg_nosupport);
        }
    }

    public void showSupportReady() {
        this.isWaiting = false;
        if (isSupport) {
            showSurface();
        } else {
            surface.setBackgroundResource(R.drawable.bg_nosupport);
        }
    }

    private void showSurface() {
        //surface.setBackgroundColor(R.color.app_video_background);
    }

    private void addName() {
        int txtSize = 12;
        int txtColor = Color.WHITE;// 0是透明我们取白色
        // **用户名设置**//
        userName = new TextView(activity);
        userName.setTextSize(TypedValue.COMPLEX_UNIT_SP, txtSize);
        userName.setTextColor(txtColor);
        userName.setBackgroundColor(Color.TRANSPARENT);
        userName.setGravity(Gravity.CENTER);
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, Gravity.BOTTOM);
        lp.leftMargin = 10;
        lp.bottomMargin = 10
//				+getResources().getDimensionPixelSize(R.dimen.view_inconf_bottom_height)
        ;
        userName.setLayoutParams(lp);
        addView(userName);
        userName.setVisibility(View.GONE);
    }

    private void removeNameCamera() {
        if (userName != null) {
            removeView(userName);
            userName = null;
        }
        if (relative != null) {
            removeView(relative);
            relative = null;
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        screenW = this.getWidth();
        screenH = this.getHeight();

        Log.d("InfowareLab.Debug", "VideoDecodeView.surfaceCreated: " + screenW + "x" + screenH);

        if (videoCommon.isHardDecode()) {
            if (isSvc) {
                DisplayMetrics dm = getResources().getDisplayMetrics();
                videoCommon.changeSvcLevel(channelId, 0, 1.0f * screenW / dm.xdpi, 1.0f * screenH / dm.ydpi);
            }
            if (videoCommon.getDeviceMap().containsKey(channelId)) {
                this.userID = videoCommon.getDeviceMap().get(channelId);
                setUserName();
            }
            videoCommon.openVideo(channelId, surface);
        } else {
            if (videoCommon.getDeviceMap().containsKey(channelId)) {
                this.userID = videoCommon.getDeviceMap().get(channelId);
                setUserName();
            }
            HandlerThread surfaceThread = new HandlerThread(
                    "SurfaceHandler");
            surfaceThread.start();
            videoCommon.openVideo(channelId, surface);
            videoCommon.addSurfaceHanlder(surfaceThread,
                    new SurfaceHandler(surfaceThread.getLooper()),
                    channelId);
            drawImage = null;
        }
        isCreated = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        screenW = this.getWidth();
        screenH = this.getHeight();

        Log.d("InfowareLab.Debug", "VideoDecodeView.surfaceChanged: " + screenW + "x" + screenH);

//		if (isSvc) {
//			DisplayMetrics dm = getResources().getDisplayMetrics();
//			videoCommon.changeSvcLevel(channelId, 0, 1.0f * screenW / dm.xdpi, 1.0f * screenH / dm.ydpi);
//		}
        this.holder = holder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        Log.d("InfowareLab.Debug", "VideoDecodeView.surfaceDestroyed: " + screenW + "x" + screenH);

        //if(!CallbackManager.IS_LEAVED) return;
        isDrawing = false;
        videoCommon.closeVideo(channelId);
    }

    private boolean isUnlock4release = false;

    public void unlock2release() {
        if (videoCommon.isHardDecode()) {
            if (drawImage != null) {
                isDrawing = false;
                isUnlock4release = true;
            } else {
                Message m = videoCommon.getHandler().obtainMessage(9999);
                videoCommon.getHandler().sendMessage(m);
            }
        } else {
            Message m = videoCommon.getHandler().obtainMessage(9999);
            videoCommon.getHandler().sendMessage(m);
        }
    }

    class SurfaceHandler extends Handler {
        public SurfaceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case VIDEO_SYNC:
                    if (!isSupport) return;
                    try {
                        dataQueue.put((byte[]) msg.obj);
                        if (drawImage == null) {
                            isDrawing = true;
                            isWatching = true;
                            drawImage = new DrawImage();
                            drawImage.start();
                        }
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
            }
            super.handleMessage(msg);
        }

    }

    private LinkedBlockingQueue<byte[]> dataQueue = new LinkedBlockingQueue<byte[]>(8);
    private DrawImage drawImage = null;
    /**
     * 是否允许绘图线程
     */
    private boolean isDrawing = false;
    /**
     * 是否处于视频界面
     */
    private boolean isWatching = false;
    private Canvas canvas1;

    class DrawImage extends Thread {
        public void run() {
            while (dataQueue != null && isDrawing) {
                if (dataQueue.size() > 5) {
                    dataQueue.poll();
                } else {
                    try {
                        if (!dataQueue.isEmpty() && isWatching) {
                            isSoftDrawing = true;
                            canvas1 = holder.lockCanvas();  // 获取画布
                            byte[] bitBuffer = dataQueue.take();
                            if (null == canvas1 || null == bitBuffer) {
                                continue;
                            }
                            startSyncDraw(canvas1, bitBuffer);
                            holder.unlockCanvasAndPost(canvas1);  // 解锁画布，提交图像
                            isSoftDrawing = false;
                        }
                        sleep(30);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    ;


    /**
     * 填充视频画面
     *
     * @param canvas2
     * @param data
     */
    protected void startSyncDraw(Canvas canvas2, byte[] data) {
//	    canvas2.setDrawFilter(pfdf); //抗锯齿
        buffer = ByteBuffer.wrap(data);
        try {
            videoBit.copyPixelsFromBuffer(buffer);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        Matrix m = new Matrix();
        float scaleX, scaleY;
        int xOff = 0;
        scaleX = (float) screenW / bitWidth;
        scaleY = (float) screenH / bitHeight;
        m.postScale(scaleX, scaleY);
        m.postTranslate(xOff, 0);
        drawPartBitmap(canvas2, scaleX, scaleY);
    }

    /**
     * 填充画面
     * scaleX < scaleY 显示完整优先
     * scaleX > scaleY 充满屏幕优先
     *
     * @param canvas
     * @param scaleX
     * @param scaleY
     */
    private void drawPartBitmap(Canvas canvas, float scaleX, float scaleY) {
        Rect src = new Rect();
        RectF dst = new RectF();
        int x1 = 0, y1 = 0, x2 = 0, y2 = 0;
        if (isSvc) {
            x1 = 0;
            y1 = 0;
            x2 = bitWidth;
            y2 = bitHeight;
            src.set(x1, y1, x2, y2);
            dst.set(0, 0, surface.getWidth(), surface.getHeight());
        } else {
            if (scaleX > scaleY) {
                x1 = 0;
                x2 = bitWidth;
                y1 = (int) ((bitHeight * scaleX - screenH) / (2 * scaleX));
                y2 = bitHeight - y1;
            } else {
                y1 = 0;
                y2 = bitHeight;
                x1 = (int) ((bitWidth * scaleY - screenW) / (2 * scaleY));
                x2 = videoBit.getWidth() - x1;
            }
            src.set(x1, y1, x2, y2);
            dst.set(0, 0, screenW, screenH);
        }
        canvas.drawBitmap(videoBit, src, dst, null);
    }


    public void stopDrawThread() {
        isDrawing = false;
    }

    public void changeWatchingState(boolean isWatching) {
        this.isWatching = isWatching;
    }


    /**
     * 设置宽高
     *
     * @param width
     * @param height
     */
    public void setParams(int width, int height) {
        screenW = width;
        screenH = height;
    }

    public void setUserName() {
        String name = userCommon.getUser(this.userID).getUsername();
        if (this.userID == userCommon.getSelf().getUid())
            name = "[我]" + name;
        userName.setText(name);
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
        this.deleteView.setTag(channelId);
    }

    public int getUserID() {
        return userID;
    }

    public int getChannelId() {
        return channelId;
    }

    public SurfaceView getSurfaceView() {
        return surface;
    }

    public interface IOnDeleteAndCameraClick {
        public void onClickDelete();

        public void onClickCamera();
    }

    public void setOnDeleteAndCameraClick(IOnDeleteAndCameraClick onclick) {
        this.onDeleteAndCameraClick = onclick;
    }

    private void clickDelete() {
        if (this.onDeleteAndCameraClick != null) onDeleteAndCameraClick.onClickDelete();
    }

    private void clickCamera() {
        if (this.onDeleteAndCameraClick != null) onDeleteAndCameraClick.onClickCamera();
    }

    public void show() {
        setVisibility(View.VISIBLE);
    }

    public void hide() {
        setVisibility(View.GONE);
    }

    public void changeSize(int w, int h) {
        if (screenW != w || screenH != h) {
            screenW = w;
            screenH = h;
            if (isSvc) {
                DisplayMetrics dm = getResources().getDisplayMetrics();
                videoCommon.changeSvcLevel(channelId, 0, 1.0f * screenW / dm.xdpi, 1.0f * screenH / dm.ydpi);
            }
            setSurface(bitWidth, bitHeight);
            invalidate();
        }

    }

    public void setCurSVCLvl(int w, int h) {
        if (screenW != w || screenH != h) {
            screenW = w;
            screenH = h;
            if (isSvc) {
                //DisplayMetrics dm = getResources().getDisplayMetrics();
                //double width = 1.0f * screenW / dm.xdpi;
                //double height = 1.0f * screenH / dm.ydpi;
                double width = 1.0f * screenW;
                double height = 1.0f * screenH;
                Log.d("InfowareLab.Debug", ">>>>>>VideoDecodeView.setCurSVCLvl: width=" + width + "x" + height);
                videoCommon.changeSvcLevel(channelId, 0, width, height);
            }
            if (surface != null && isCreated) {
                setSurface(bitWidth, bitHeight);
            }
        }
    }

    public void setwh11() {
        screenW = 1;
        screenH = 1;
        if (surface != null && isCreated) {
            setSurface(0, 0);
        }
    }

    public void setSvc(boolean svc) {
        isSvc = svc;
    }

    public void setSurfaceOnTop(boolean top)
    {
        if (surface != null){
            surface.setZOrderOnTop(top);
            surface.setZOrderMediaOverlay(top);
        }
    }

    public int getSurfaceWidth() { return surfaceWidth; }
    public int getSurfaceHeight() { return surfaceHeight; }
}
