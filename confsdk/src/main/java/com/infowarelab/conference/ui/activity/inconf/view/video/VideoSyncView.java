package com.infowarelab.conference.ui.activity.inconf.view.video;

import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

import com.infowarelab.conference.ConferenceApplication;
import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.UserCommonImpl;
import com.infowarelabsdk.conference.common.impl.VideoCommonImpl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.hardware.Camera.CameraInfo;
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
import android.widget.FrameLayout.LayoutParams;

@SuppressLint("NewApi")
public class VideoSyncView extends FrameLayout implements SurfaceHolder.Callback {

    private boolean isSvc = false;
    private static final int VIDEOP_SHRED = 4200;
    private static final int VIDEOP_PREVIEW = 4100;

    protected static final int VIDEO_SYNC = 8888;
    protected static final int VIDEO_MYSELF = 6666;
    public static boolean isSoftDrawing = false;
    /**
     * surface生命周期
     **/
    private SurfaceHolder holder;

    /**
     * 当前摄像头的方位
     * CAMERA_FACING_FRONT 前置
     * CAMERA_FACING_BACK 后置
     */
    private int currentCamera = CameraInfo.CAMERA_FACING_FRONT;//代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置 ;

    private Context activity;
    /**
     * 当前预览的channelID
     **/
    private int channelId;
    /**
     * 非本地视频全屏状态
     **/
    public static final int VIDEO_FULL = 1;
    /**
     * 非本地视频缩小状态
     **/
    public static final int VIDEO_SMALL = 2;
    /**
     * 非本地视频放大状态
     **/
    public static final int VIDEO_LARGE = 3;
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

    //	private LocalVideoView cameraVideo;
    private boolean isSupport = true;
    private SurfaceView surface;
    private TextView userName;
    private Button deleteView;
    private Button cameraView;
    private LinearLayout relative;
    private TextView tvWait;
    private LinearLayout.LayoutParams cameraParames;
    private LinearLayout.LayoutParams delParames;
    private int screenW;
    private int screenH;
    private boolean isChangeOrietation = false;
    private int pHeight;

    private IOnDeleteAndCameraClick onDeleteAndCameraClick;

    public VideoSyncView(Context context) {
        super(context);
        initParames(context, 0, 0, 0, 0);
    }

    public VideoSyncView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.activity = context;
        initParames(context, 0, 0, 0, 0);
    }

    public VideoSyncView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.activity = context;
        initParames(context, 0, 0, 0, 0);
    }

    public VideoSyncView(Context context, int textSizeSp, int textColor, int cameraSrc, int deleteSrc) {
        super(context);
        this.activity = context;
        initParames(context, 0, 0, 0, 0);
    }

    public VideoSyncView(Context context, int textSizeSp, int textColor, int cameraSrc, int deleteSrc, IOnDeleteAndCameraClick oClick) {
        super(context);
        this.activity = context;
        initParames(context, 0, 0, 0, 0);
        this.onDeleteAndCameraClick = oClick;
    }

    /**
     * 初始化布局
     * 默认字体12sp,white。
     */
    private void initParames(Context context, int textSizeSp, int textColor, int cameraSrc, int deleteSrc) {

        int txtSize = textSizeSp > 0 ? textSizeSp : 12;

        int txtColor = textSizeSp != 0 ? textSizeSp : Color.WHITE;//0是透明我们取白色

        int draCamera = cameraSrc != 0 ? cameraSrc : android.R.drawable.ic_menu_camera;

        int draDelete = deleteSrc != 0 ? deleteSrc : android.R.drawable.ic_menu_close_clear_cancel;

//		surface = new SurfaceView(context);
//        surface.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 
//        		LayoutParams.MATCH_PARENT, Gravity.CENTER));
//        holder = surface.getHolder();
//	    holder.addCallback(this);

        //**用户名设置**//
        userName = new TextView(context);
        userName.setTextSize(TypedValue.COMPLEX_UNIT_SP, txtSize);
        userName.setTextColor(txtColor);
        userName.setBackgroundColor(Color.TRANSPARENT);
        userName.setGravity(Gravity.CENTER);
        userName.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, Gravity.BOTTOM));
        //**用户名设置**//

        //**在右上角加入相机图标和删除图标**//
        relative = new LinearLayout(context);
        relative.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.TOP));

        cameraParames = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        cameraView = new Button(context);
        cameraView.setLayoutParams(cameraParames);
        cameraView.setBackgroundResource(draCamera);
        cameraView.setVisibility(View.GONE);
        relative.addView(cameraView);

        delParames = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        deleteView = new Button(context);
        cameraView.setLayoutParams(delParames);
        deleteView.setBackgroundResource(draDelete);
        deleteView.setVisibility(View.GONE);
        relative.addView(deleteView);
        //**在右上角加入相机图标和删除图标**//

//		addView(surface);
        addView(userName);
        addView(relative);

        this.cameraView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clickCamera();
            }
        });
        this.deleteView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clickDelete();
            }
        });
    }

    /**
     * 初始化界面大小
     *
     * @param height       容器高
     * @param width        宽
     * @param titleHeight  顶部距离
     * @param bottomHeight 底部距离
     */
    public void initSize(int height, int width, int titleHeight, int bottomHeight) {
        LayoutParams params = (LayoutParams) getLayoutParams();
        params.width = width;
//		params.height = height-bottomHeight;
        params.height = height;
        pHeight = params.height;
//		params.setMargins(0, titleHeight, 0, 0);
        params.setMargins(0, 0, 0, 0);
        setLayoutParams(params);
        setParams(params.width, params.height);
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
    public void resetSize(int width, int height, int channelID) {
        this.channelId = channelID;
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
        invalidate();
    }

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
//			setBackgroundResource(R.drawable.bg_video_nosupport);
            isSupport = false;
        } else {
            setBackgroundResource(0);
            isSupport = true;
        }
//		setParams(screenW, screenH);
        setSurface(width, height);
        invalidate();
    }

    public void setSurface(int w, int h) {
        if (screenH != 0 && screenW != 0 && surface != null && isSvc
//				&&videoCommon.isHardDecode()
        ) {
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
            surface.setLayoutParams(p);
        }
    }

    /**
     * 视频接收/停止接收
     *
     * @param channelID
     * @param isOpen
     */
    public void changeStatus(int channelID, boolean isOpen) {
        if (userCommon.getOwnID() == userID) {
//			getCameraVideo().setOpenPreview(true, -3);
//			HandlerThread mySurfaceThread =new HandlerThread("MySurfaceThread");
//			mySurfaceThread.start();
//			getCameraVideo().setPreviewHandler(new SurfaceHandler(mySurfaceThread.getLooper()));
//			getCameraVideo().setPreviewThread(mySurfaceThread);
        } else {
            if (videoCommon.isHardDecode()) {
                if (isOpen) {
                    channelId = channelID;
                    if (null != surface) {
                        surface.getHolder().getSurface().release();
                        removeView(surface);
                        surface = null;
                        removeWait();
                    }
                    surface = new SurfaceView(this.activity);
                    surface.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                            LayoutParams.MATCH_PARENT, Gravity.CENTER));
                    holder = surface.getHolder();
                    holder.addCallback(this);
                    addView(surface);
                    showWait();
                } else {
                    if (null != surface) {
                        surface.getHolder().getSurface().release();
                        removeView(surface);
                        surface = null;
                        removeWait();
                    }
                    videoCommon.closeVideo(channelID);
                }
            } else {
                if (isOpen) {
                    channelId = channelID;
                    if (null == surface) {
                        surface = new SurfaceView(this.activity);
                        surface.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                                LayoutParams.MATCH_PARENT, Gravity.CENTER));
                        holder = surface.getHolder();
                        holder.addCallback(this);
                        addView(surface);
                        showWait();
                    } else {
                        showWait();
                        HandlerThread surfaceThread = new HandlerThread("SurfaceHandler");
                        surfaceThread.start();
                        videoCommon.openVideo(channelId, surface);
                        videoCommon.addSurfaceHanlder(surfaceThread, new SurfaceHandler(surfaceThread.getLooper()), channelId);
                    }
                } else {
                    videoCommon.closeVideo(channelID);
                }
            }
        }
    }

    private void showWait() {
//		tvWait = new TextView(this.activity);
//		tvWait.setTextSize(TypedValue.COMPLEX_UNIT_SP,40);
//		tvWait.setTextColor(Color.BLACK);
//		tvWait.setBackgroundColor(Color.WHITE);
//		tvWait.setGravity(Gravity.CENTER);
//		tvWait.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,  
//                LayoutParams.MATCH_PARENT));
//		tvWait.setText("Wait");
//		addView(tvWait);
        if (null != surface) {
            surface.setBackgroundResource(R.drawable.bg_sharedt_wait);
        }
    }

    private void removeWait() {
//		if(null!=tvWait){
//			removeView(tvWait);
//			tvWait = null;
//		}
    }

    public void showSurface() {
//		if(null!=tvWait){
//			tvWait.setVisibility(View.GONE);
//		}
        surface.setBackgroundResource(0);
    }

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
//			Matrix matrix = new Matrix();  
//            matrix.reset();  
//            matrix.setRotate(-90); 
//            videoBit = Bitmap.createBitmap(videoBit,0,0, videoBit.getWidth(), videoBit.getHeight(),matrix, true);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        Matrix m = new Matrix();
        float scaleX, scaleY;
        int xOff = 0;
        scaleX = (float) screenW / bitWidth;
        scaleY = (float) screenH / bitHeight;
//		Log.i("VideoSyncView", "VideoSyncView:"+"screenW="+screenW+"screenH="+screenH+"\n"+"bitWidth="+bitWidth+"bitHeight="+bitHeight);
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


//	protected void startMySelfDraw(Canvas canvas2,int[] data) {
//		canvas2.setDrawFilter(pfdf); //抗锯齿 
//		if(videoBit != null){
//			videoBit.recycle();
//		}
//		if((getCameraVideo().getDegrees()/90)%2==0){
//			videoBit = Bitmap.createBitmap(data,LocalVideoView.cameraWidth,LocalVideoView.cameraHeight, Bitmap.Config.RGB_565);
//		}else {
//			videoBit = Bitmap.createBitmap(data,LocalVideoView.cameraWidth,LocalVideoView.cameraHeight, Bitmap.Config.RGB_565);
//			Matrix matrix = new Matrix();  
//			matrix.reset();  
//			matrix.setRotate(0-getCameraVideo().getDegrees()); 
//				videoBit = Bitmap.createBitmap(videoBit,0,0, videoBit.getWidth(),videoBit.getHeight(),matrix, true); 
////			if(videoBit.getWidth()*100/videoBit.getHeight()<100){
////				videoBit = Bitmap.createBitmap(videoBit,0,0, videoBit.getWidth(), videoBit.getWidth()*videoBit.getWidth()/videoBit.getHeight(),matrix, true); 
////			}else {
////				videoBit = Bitmap.createBitmap(videoBit,0,0, videoBit.getHeight()*videoBit.getHeight()/videoBit.getWidth(), videoBit.getHeight(),matrix, true); 
////				
////			}
//		}
//		float scaleX;
//		float scaleY;
//		scaleX = (float)screenW/ bitWidth;
//		scaleY = (float)screenH/ bitHeight;
//		drawPartBitmap(canvas2, scaleX, scaleY);
//	}

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        screenW = this.getWidth();
        screenH = this.getHeight();
        if (isSvc) {
            DisplayMetrics dm = getResources().getDisplayMetrics();
            videoCommon.changeSvcLevel(channelId, 0, 1.0f * screenW / dm.xdpi, 1.0f * screenH / dm.ydpi);
        }
        if (videoCommon.isHardDecode()) {
            Log.i("VideoSyncView", "VideoSyncView surfaceCreated HardDecode");
            HandlerThread surfaceThread = new HandlerThread("SurfaceHandler");
            surfaceThread.start();
            videoCommon.openVideo(channelId, surface);
            videoCommon.addSurfaceHanlder(surfaceThread, new SurfaceHandler(surfaceThread.getLooper()), channelId);
        } else {
            Log.i("VideoSyncView", "VideoSyncView surfaceCreated SoftDecode");
            HandlerThread surfaceThread = new HandlerThread("SurfaceHandler");
            surfaceThread.start();
            videoCommon.openVideo(channelId, surface);
            videoCommon.addSurfaceHanlder(surfaceThread, new SurfaceHandler(surfaceThread.getLooper()), channelId);
            drawImage = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        Log.i("VideoSyncView", "VideoSyncView surfaceChanged");
//		if(isChangeOrietation){
//			screenW=this.getWidth();
//			screenH=this.getHeight();
//			isChangeOrietation = false;
//		}
        screenW = this.getWidth();
        screenH = this.getHeight();
        if (isSvc) {
            DisplayMetrics dm = getResources().getDisplayMetrics();
            videoCommon.changeSvcLevel(channelId, 0, 1.0f * screenW / dm.xdpi, 1.0f * screenH / dm.ydpi);
        }
        this.holder = holder;
        if (bitHeight > 0 && bitWidth > 0) {
        } else {
            resetSize(LocalVideoView.cameraWidth, LocalVideoView.cameraHeight);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i("VideoSyncView", "VideoSyncView surfaceDestroyed");
        isDrawing = false;
        videoCommon.closeVideo(channelId);
//		videoCommon.closeSurfaceHanlder(channelId);
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
//					Canvas canvas1 = holder.lockCanvas();  // 获取画布
//					byte[] bitBuffer = (byte[])msg.obj;
//					if(null==canvas1||null==bitBuffer){
//						return;
//					}
//					startSyncDraw(canvas1,bitBuffer);
//					holder.unlockCanvasAndPost(canvas1);  // 解锁画布，提交图像
                    try {
                        dataQueue.put((byte[]) msg.obj);
                        if (drawImage == null) {
                            isDrawing = true;
                            drawImage = new DrawImage();
                            drawImage.start();
                        }
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
                case VIDEO_MYSELF:
//					Canvas canvas2 = holder.lockCanvas();  // 获取画布
//					int[] buffer = (int[]) msg.obj;
//					if(null==canvas2||null==buffer){
//						return;
//					}
//					startMySelfDraw(canvas2,buffer);
//		            holder.unlockCanvasAndPost(canvas2);  // 解锁画布，提交图像
//		            buffer = null;
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
//            			Log.i("VideoSyncView", "VideoSyncView dataQueueSize="+dataQueue.size()+";removehead");
                    dataQueue.poll();
                } else {
//            			Log.i("VideoSyncView", "VideoSyncView dataQueueSize="+dataQueue.size()+";Thread");
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

    public void stopDrawThread() {
        isDrawing = false;
    }

    public void changeWatchingState(boolean isWatching) {
        this.isWatching = isWatching;
    }


    //	public void setCameraVideo(LocalVideoView videoSyncView) {
//		this.cameraVideo = videoSyncView;
//	}
//	private LocalVideoView getCameraVideo(){
//		return this.cameraVideo;
//	}
    public void setVideoSize(int sizeId) {
        switch (sizeId) {
            case VIDEO_FULL:
                setLayoutParams(new android.widget.RelativeLayout.LayoutParams(400, 320));
                break;
            case VIDEO_LARGE:
                android.widget.RelativeLayout.LayoutParams params1 = (android.widget.RelativeLayout.LayoutParams) getLayoutParams();
                params1.width = 200;
                params1.height = 150;
                setLayoutParams(params1);
                break;
            case VIDEO_SMALL:
                android.widget.RelativeLayout.LayoutParams params2 = (android.widget.RelativeLayout.LayoutParams) getLayoutParams();
                params2.width = 200;
                params2.height = 150;
                setLayoutParams(params2);
                break;
        }
        resetSize(200, 150);
    }

    public void setPortrait() {
        isChangeOrietation = true;
        LayoutParams params = (LayoutParams) getLayoutParams();
        params.width = ConferenceApplication.SCREEN_HEIGHT;
        params.height = pHeight;
        params.setMargins(0, 0, 0, 0);
        params.topMargin = 0;
        setLayoutParams(params);
        setParams(params.width, params.height);
        setSurface(bitWidth, bitHeight);
    }

    public void setLandscape() {
        isChangeOrietation = true;
        LayoutParams params = (LayoutParams) getLayoutParams();
        params.width = ConferenceApplication.SCREEN_WIDTH;
        params.height = ConferenceApplication.SCREEN_HEIGHT;
        params.setMargins(0, 0, 0, 0);
        setLayoutParams(params);
        setParams(params.width, params.height);
        setSurface(bitWidth, bitHeight);
    }

    public void setPreview(int height) {
        isChangeOrietation = true;
        LayoutParams params = (LayoutParams) getLayoutParams();
        params.height = height;
        params.setMargins(0, 0, 0, 0);
        setLayoutParams(params);
        setParams(screenW, height);
        invalidate();
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

    public void setUserID(int userID) {
        this.userID = userID;
        if (userCommon.getOwnID() == userID) {
            this.cameraView.setVisibility(View.VISIBLE);
        } else {
            this.cameraView.setVisibility(View.INVISIBLE);
        }
        setUserName();
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
        this.deleteView.setTag(channelId);
        this.cameraView.setTag(channelId);
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
}
