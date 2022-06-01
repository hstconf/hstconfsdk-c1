package com.infowarelab.conference.ui.activity.inchat.view.video;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.infowarelab.conference.ConferenceApplication;
import com.infowarelab.hongshantongphone.ChatAPI;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.VideoCommonImpl;
import com.infowarelabsdk.conference.video.AvcHardEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class GLCameraView extends GLSurfaceView implements Camera.PreviewCallback {

    private boolean inited = false;
    public GLCameraView.GLRenderer renderer = null;
    public BaseFilter mCurrentFilter = null;
    //private CameraCore mCameraHelper;
    //private static final String TAG = "aaaaa";
    private Context c;
    private int mTextureId;
    private SurfaceTexture mSurfaceTexture = null;
    private float[] mSTMatrix = new float[16];
    //private boolean mRecordingEnabled ;
    private  FilterFactory.FilterType type;

    public static int cameraFPS = 0;
    public static int cameraWidth = 640;
    public static int cameraHeight = 480;

    private Camera camera;
    private int currentCamera = Camera.CameraInfo.CAMERA_FACING_FRONT;// 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
    private int numOfCamera = 1;
    File file = new File("/sdcard/testView.yuv");
    private FileOutputStream fos;
    // private Handler handler;
    private static boolean initVideo = true;
    private static boolean isShareing = false;
    private boolean isPortrait = true;
    private int frameSize;
    private int[] rgba;
    private VideoCommonImpl videoCommon = (VideoCommonImpl) CommonFactory
            .getInstance().getVideoCommon();
    private boolean isPreview = false;
    private boolean isOpen = false;

    public static boolean isDestroyed = false;
    public static boolean isInited = false;

    // Video Hard Encoder
    private AvcHardEncoder h264HwEncoderImpl = null;
    private static final int FRAME_RATE = 25;
    private boolean isHardCodec = true; // true
    private boolean isWriteFile = false;
    private boolean isEnabled = true;

    byte[] yv12buf;

    private int degrees = 90;
    public boolean isSmall = false;

    private File _fr = null;
    private FileOutputStream _out = null;
    private Context activity;

    // end


    public GLCameraView(Context context) {
        super(context);

        init(context);

    }

    public GLCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    private void init(Context context) {

        if (inited) return;

        this.c = context;

        activity = context;

        setEGLContextClientVersion(2);

        type = FilterFactory.FilterType.Original;
        renderer = new GLCameraView.GLRenderer(this, type);

        setRenderer(renderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);

		if (null == h264HwEncoderImpl)
			h264HwEncoderImpl = new AvcHardEncoder();  //release ???? sunny add 2016-5-23
		isHardCodec = h264HwEncoderImpl.IsSupportHardEncode();

        inited = true;
    }

    public boolean rendererCreated(){return renderer != null;}

    public class GLRenderer implements Renderer, SurfaceTexture.OnFrameAvailableListener {

        GLSurfaceView surfaceView;

        //private final Queue<Runnable> runOnDraw;
        //private final Queue<Runnable> runOnDrawEnd;

        public GLRenderer(GLSurfaceView surfaceView, FilterFactory.FilterType type) {

            this.surfaceView = surfaceView;

            mCurrentFilter = FilterFactory.createFilter(c,type);

            //runOnDraw = new LinkedList<>();
            //runOnDrawEnd = new LinkedList<>();
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {

            Log.d("InfowareLab.Debug","GLCameraView.onSurfaceCreated: " + config.toString());

            mCurrentFilter.createProgram();
            mTextureId = BaseFilter.bindTexture();
            mSurfaceTexture = new SurfaceTexture(mTextureId);
            mSurfaceTexture.setOnFrameAvailableListener(this);

            if (camera != null && mSurfaceTexture != null && isEnabled) {
                try {
                    //camera.setPreviewDisplay(holder);
                    Log.d("InfowareLab.Debug","camera.setPreviewTexture");
                    camera.setPreviewTexture(mSurfaceTexture);

                } catch (IOException e) {
                    Log.e("InfowareLab.Debug", e.getMessage());
                    e.printStackTrace();
                }

                if (!isPreview() && isEnabled()){
                    Log.d("InfowareLab.Debug", "startPreview in onSurfaceCreated");
                    changePreview(true);
                }
            }
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {

            if (width <= 1 || height <= 1) return;

            Log.d("InfowareLab.Debug","GLCameraView.onSurfaceChanged: " + width + "x" + height);

            GLES20.glViewport(0, 0, width, height);

            mCurrentFilter.onInputSizeChanged(width,height);

            //mCameraHelper.startPreview(mSurfaceTexture);
        }

        /**
         * 关于预览出现镜像，旋转等问题，有两种方案:
         * 1.在相机预览的地方进行调整
         * 2.通过opengl的矩阵变换在绘制的时候进行调整
         * 这里我采用了前者
         */

        @Override
        public void onDrawFrame(GL10 gl) {

            //runAll(runOnDraw);
            if (mSurfaceTexture == null) return;

            mSurfaceTexture.updateTexImage();

            mSurfaceTexture.getTransformMatrix(mSTMatrix);
            mCurrentFilter.draw(mTextureId,mSTMatrix);

            //runAll(runOnDrawEnd);
        }

        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            //Log.d("InfowareLab.Debug","GLCameraView.onFrameAvailable");
            surfaceView.requestRender();

        }


//        void runOnDraw(final Runnable runnable) {
//            synchronized (runOnDraw) {
//                runOnDraw.add(runnable);
//            }
//        }
//
//        void runOnDrawEnd(final Runnable runnable) {
//            synchronized (runOnDrawEnd) {
//                runOnDrawEnd.add(runnable);
//            }
//        }

        private void runAll(Queue<Runnable> queue) {
            synchronized (queue) {
                while (!queue.isEmpty()) {
                    queue.poll().run();
                }
            }
        }

    }


    public void updateFilter(final FilterFactory.FilterType type){

        this.type = type;

        //renderer.runOnDraw(() -> {


            mCurrentFilter.releaseProgram();
            mCurrentFilter = FilterFactory.createFilter(c,type);

            //调整预览画面
            mCurrentFilter.createProgram();
            mCurrentFilter.onInputSizeChanged(getWidth(),getHeight());
            //调整录像画面
            //hwRecorderWrapper.updateFilter(type);

            Log.v("aaaaa","updateFilter:"+ Thread.currentThread());

        //});

    }

    public void enableBeauty(boolean enableBeauty){

        if (enableBeauty){

            type = FilterFactory.FilterType.Beauty;

        }else{

            type = FilterFactory.FilterType.Original;
        }

        updateFilter(type);
    }

    public void enableCamera(boolean enabled){isEnabled=enabled;}
    public boolean isEnabled(){return isEnabled;}

    public void startCamera() {

        openCamera();

        if (camera == null) {
            return;
        }

        setCameraParameters(degrees);

        if (isPortrait)
            mCurrentFilter.setTextureSize(new Size(cameraHeight, cameraWidth));
        else
            mCurrentFilter.setTextureSize(new Size(cameraWidth, cameraHeight));

        // start happy add for hard encode 2016-5-3
		if (Integer.parseInt(Build.VERSION.SDK) >= 16 && isHardCodec) {

            if (null == h264HwEncoderImpl)
                h264HwEncoderImpl = new AvcHardEncoder();  //release ???? sunny add 2016-5-23
            isHardCodec = h264HwEncoderImpl.IsSupportHardEncode();

			int ret;
			if (isPortrait)
				ret = h264HwEncoderImpl.initEncoder(cameraHeight, cameraWidth);
			else
				ret = h264HwEncoderImpl.initEncoder(cameraWidth, cameraHeight);
			if (ret == 1)
				isHardCodec = false; // sunny add 2016-5-19
		}
        // end

        camera.setPreviewCallback(this);

		if (mSurfaceTexture != null && !isPreview()) {
            try {
                //camera.setPreviewDisplay(holder);
                Log.d("InfowareLab.Debug","camera.setPreviewTexture at startCamera()");
                camera.setPreviewTexture(mSurfaceTexture);

            } catch (IOException e) {
                Log.e("InfowareLab.Debug", e.getMessage());
                e.printStackTrace();
            }
            changePreview(true);
        }

        setBackgroundColor(0);
        Log.d("InfowareLab.Debug","start camera");
    }

    public void openCamera(){
        try {
            if (Integer.parseInt(Build.VERSION.SDK) > 8) {
                numOfCamera = Camera.getNumberOfCameras();
                if (numOfCamera == 1) {
                    currentCamera = Camera.CameraInfo.CAMERA_FACING_BACK;
                    camera = Camera.open(); // 底层默认打开的是后置摄像头
                } else {
                    Log.i("RickTest", "currentCamera"+currentCamera);

                    camera = Camera.open(currentCamera);

                }
            } else {
                camera = Camera.open();
            }
            videoCommon.onErrMsg(2);
        } catch (Exception e) {
            Log.e("InfowareLab.Debug",e.getMessage());
//			Log.i("RickTest", "Exception"+e.getLocalizedMessage());
//			e.printStackTrace();
//			Toast.makeText(activity, activity.getResources().getString(R.string.nocamerapermission), Toast.LENGTH_LONG).show();
            videoCommon.onErrMsg(1);
            camera = null;
            return;
        }
        try {
            camera.getParameters();
        } catch (Exception e) {
            Log.e("InfowareLab.Debug",e.getMessage());
            videoCommon.onErrMsg(1);
            camera = null;
            return;
        }
    }
    /*
     * 设置相机属性
     */
    private void setCameraParameters(int degrees) {
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        List<int[]> rates = parameters.getSupportedPreviewFpsRange();
        cameraWidth = 0;
        cameraHeight = 0;
        // 取比设定值小的像素中最大的
        for (Camera.Size size : previewSizes) {
            Log.d("InfowareLab.Debug","Camera.Size = " + size.width + ", " + size.height + ", "
                    + cameraFPS);
            if (size.width * size.height <= videoCommon.getWidth()
                    * videoCommon.getHeight()
                    && size.height >= 0&&size.width%16==0&&size.height%16==0) {
                if (cameraWidth == 0) {
                    cameraWidth = size.width;
                    cameraHeight = size.height;
                }
                if (size.width*size.height >= cameraWidth*cameraHeight) {
                    cameraWidth = size.width;
                    cameraHeight = size.height;
                }
            }
        }
        // 如果设定值实在太小，取所支持的最小像素
        if (cameraWidth == 0) {
            for (Camera.Size size : previewSizes) {
                if (size.height >= 0&&size.width%16==0&&size.height%16==0) {
                    if (cameraWidth == 0) {
                        cameraWidth = size.width;
                        cameraHeight = size.height;
                    }
                    if (size.width*size.height <= cameraWidth*cameraHeight) {
                        cameraWidth = size.width;
                        cameraHeight = size.height;
                    }
                }
            }
        }
        Log.d("InfowareLab.Debug","Camera.CameraParameters = " + cameraWidth + ", " + cameraHeight);
        frameSize = cameraWidth * cameraHeight;
        int minimum = 0;
        int maximum = 0;
        int retMininum = 0;
        int retMaximum = 0;
        if (rates.size() > 0) {
            minimum = rates.get(0)[0];
            maximum = rates.get(0)[1];
            retMininum = rates.get(0)[0];
            retMaximum = rates.get(0)[1];
            for (int[] fps : rates){
                if (minimum < fps[0]){
                    minimum = fps[0];
                }
                if (maximum < fps[1]){
                    maximum = fps[1];
                }
            }
        }

        setCameraOrientation(degrees,parameters);
        parameters.setPreviewSize(cameraWidth, cameraHeight);//设置预览的高度和宽度,单位为像素
//	    parameters.setPreviewFrameRate(FRAME_RATE);//设置图片预览的帧速。
//	    parameters.setPreviewFpsRange(minimum, maximum);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (minimum > 0 && maximum > 0){
                    parameters.setPreviewFpsRange(minimum,maximum);
                }
            }else {
                if (retMininum > 0 && retMaximum > 0){
                    parameters.setPreviewFpsRange(retMininum,retMaximum);
                }
            }
        }catch (RuntimeException e){
            if (retMininum > 0 && retMaximum > 0){
                parameters.setPreviewFpsRange(retMininum,retMaximum);
            }
        }
        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)){
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 1连续对焦
            camera.setParameters(parameters);
            camera.cancelAutoFocus();
        }else{
            try {
                camera.setParameters(parameters);
            }
            catch (RuntimeException e)
            {
                Camera.Parameters appliedParam = camera.getParameters();
                cameraWidth = appliedParam.getPreviewSize().width;
                cameraHeight = appliedParam.getPreviewSize().height;

                Log.d("InfowareLab.Debug","Camera.CameraParameters(FAILED) = " + cameraWidth + ", " + cameraHeight);

            }
        }

        if(initVideo){
            videoCommon.initializeMyVideo(cameraWidth, cameraHeight, FRAME_RATE);
            initVideo = false;
        }

    }

    private void setCameraOrientation(int degrees, Camera.Parameters p){
        if (Integer.parseInt(Build.VERSION.SDK) >= 8)
            setDisplayOrientation(camera, degrees);
        else
        {
            if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            {
                p.set("orientation", "portrait");
                p.set("rotation", degrees);
            }
            if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            {
                p.set("orientation", "landscape");
                p.set("rotation", degrees);
            }
        }
    }
    private void setDisplayOrientation(Camera mCamera, int angle) {
        Method downPolymorphic;
        try
        {
            downPolymorphic = mCamera.getClass().getMethod("setDisplayOrientation", new Class[] { int.class });
            if (downPolymorphic != null)
                downPolymorphic.invoke(mCamera, new Object[] { angle });
        }
        catch (Exception e1)
        {
        }
    }
    /**
     * 相应地，在surfaceDestroyed中也需要释放该Camera对象。 我们将首先调用stopPreview，以确保应该释放的资源都被清理。
     */

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

        if (data == null) {
            return;
        }

        //Log.d("InfowareLab.Debug","onPreviewFrame data length = "+data.length);

        if (isShareing && ConferenceApplication.isVideoMeeting && ConferenceApplication.isConfJoined) {

            if (isPortrait) {//竖屏

                if (isHardCodec && Integer.parseInt(Build.VERSION.SDK) >= 16)
                {
                    if (h264HwEncoderImpl.GetMediaEncoder() == null)
                        h264HwEncoderImpl.initEncoder(cameraHeight, cameraWidth);

                    yv12buf = h264HwEncoderImpl.getAdapterYv12bufPortrait(data, cameraWidth, cameraHeight, currentCamera);
                    h264HwEncoderImpl.offerEncoderAndSend(yv12buf, videoCommon);
                }
                else //soft ware encoding
                {
                    if (currentCamera == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        yv12buf = rotateYUV420SPBackfacing(data, cameraWidth, cameraHeight);
                    } else {
                        yv12buf = rotateYUV420SPFrontfacing(data, cameraWidth, cameraHeight);
                    }

                    videoCommon.sendMyVideoData(yv12buf, yv12buf.length, false,	cameraHeight, cameraWidth, false);
                }
                //end
            } else {//横屏
                if (isHardCodec && Integer.parseInt(Build.VERSION.SDK) >= 16)//hardware encode.....
                {
                    if (h264HwEncoderImpl.GetMediaEncoder() == null)
                        h264HwEncoderImpl.initEncoder(cameraWidth, cameraHeight);

                    yv12buf = h264HwEncoderImpl.getAdapterYv12bufLandscape(data, cameraWidth, cameraHeight, degrees);
                    h264HwEncoderImpl.offerEncoderAndSend(yv12buf, videoCommon);
                }
                else  //software encode
                {
                    if (degrees == 0) {
                        yv12buf = changeYUV420SP2P(data, cameraWidth, cameraHeight);
                    } else {
                        yv12buf = Rotate180YUV420SP2P(data, cameraWidth, cameraHeight);
                    }
                    videoCommon.sendMyVideoData(yv12buf, yv12buf.length, false, cameraWidth, cameraHeight, false);
                    // Log.d("InfowareLab.Debug","landscape data length = "+data.length);
                }
            }
        }

    }

    public void flushEncoder(){
        //if(isHardCodec&&h264HwEncoderImpl.GetMediaEncoder()!= null){
            //h264HwEncoderImpl.flushEncoder();
        //}
    }


    public void destroyCamera() {
        Log.d("InfowareLab.Debug","surfaceDestroyed !!!!!!!!!!!");
        if (camera != null) {
            Log.d("InfowareLab.Debug","camera != null !!!!!!!!!!!");
            camera.setPreviewCallback(null);
            changePreview(false);
            camera.stopPreview();
            // 停止更新预览
            camera.release();// 释放资源
            h264HwEncoderImpl.releaseEncoder(); // sunny add 2016-5-19
            h264HwEncoderImpl = null;

            camera = null;
            isOpen = false;
        }
    }
    public void reStartLocalView() {
        // if(camera != null){
        Log.d("InfowareLab.Debug","reStartLocalView111");
        if (camera == null) {
            changeStatus(true);
        } else {
            if (isDestroyed) {
                destroyCamera();
                currentCamera = (currentCamera + 2) % numOfCamera;
                startCamera();
            }else {
                destroyCamera();
                startCamera();
//				videoCommon.exChange(cameraHeight, cameraWidth);
            }
        }
    }

    public void setStatus(boolean isMove) {
        if (isMove) {
            camera.setPreviewCallback(null);
            changePreview(false);
        } else {
            camera.setPreviewCallback(this);
            changePreview(true);
        }
    }

    public void changeStatus(boolean isOpenCamera) {
        if (isOpenCamera) {
            if (camera == null) {
                //invalidate();
                //init(activity);
                startCamera();
            }
        } else {
            if (camera != null) {
                destroyCamera();
            }
        }
    }

    private void changePreview(boolean state) {
        try {
            if (state) {
                Log.d("InfowareLab.Debug","startPreview:" + state );
                //setRenderer(renderer);
                //setRenderMode(RENDERMODE_WHEN_DIRTY);
                camera.startPreview();
                isPreview = true;
            } else {
                Log.d("InfowareLab.Debug","stopPreview:" + state );
                //setRenderer(null);
                //setRenderMode(RENDERMODE_WHEN_DIRTY);
                camera.stopPreview();
                isPreview = false;
            }
        } catch (Exception e) {
            Log.e("InfowareLab.Debug",e.getMessage());
        }
    }

    private byte[] rotateYUV420SPFrontfacing(byte[] src, int width, int height) {
        byte[] des = new byte[src.length];
        int wh = width * height;
        int uv = wh / 4;
        // 旋转Y
        int k = 0;
        for (int i = width - 1; i >= 0; i--) {
            for (int j = 0; j < height; j++) {
                des[k++] = src[width * j + i];
            }
        }
        for (int i = width - 2; i >= 0; i -= 2) {
            for (int j = 0; j < height / 2; j++) {
                des[k] = src[wh + width * j + i + 1];
                des[k + uv] = src[wh + width * j + i];
                k++;
            }
        }
        return des;
    }

    public static byte[] rotateYUV420SPBackfacing(byte[] src, int width,
                                                  int height) {
        byte[] des = new byte[src.length];
        int wh = width * height;
        int uv = wh / 4;
        // 旋转Y
        int k = 0;
        for (int i = 0; i < width; i++) {
            for (int j = height - 1; j >= 0; j--) {
                des[k] = src[width * j + i];
                k++;
            }
        }

        for (int i = 0; i < width; i += 2) {
            for (int j = height / 2 - 1; j >= 0; j--) {
                des[k] = src[wh + width * j + i + 1];
                des[k + uv] = src[wh + width * j + i];
                k++;
            }
        }

        return des;

    }

    private byte[] changeYUV420SP2P(byte[] src, int width, int height) {
        System.gc();
        byte[] yv12buf = new byte[src.length];
        int wh = width * height;
        int uv = wh / 4;

        System.arraycopy(src, 0, yv12buf, 0, wh);

        int k = 0;
        for(int i = 0 ; i < wh / 2; i += 2) {
            yv12buf[wh + k] = src[wh + i + 1];
            yv12buf[wh + uv + k] = src[wh + i];
            k++;
        }
        return yv12buf;
    }

    private byte[] Rotate180YUV420SP2P(byte[] src, int width, int height) {
        byte[] yv12buf = new byte[src.length];
        int wh = width * height;
        int uv = wh / 4;
        int k = 0;

        for(int i = 0; i < wh; i++) {
            yv12buf[k++] = src[wh - i];
        }

        for(int i = wh * 3/2 -1; i >= wh; i -= 2) {
            yv12buf[k] = src[i];
            yv12buf[uv + k] = src[i-1];
            k++;
        }
        return yv12buf;
    }

//	private byte[] changeYUV420SP2P(byte[] data, int length) {
//		System.gc();
//		int pixNum = length*2/3;
//		byte[] yv12buf = new byte[length];
//		System.arraycopy(data, 0, yv12buf, 0, pixNum);
//		int index = pixNum;
//		for (int i = pixNum + 1; i < length; i += 2) {
//			yv12buf[index++] = data[i];
//		}
//		for (int i = pixNum; i < length; i += 2) {
//			yv12buf[index++] = data[i];
//		}
//		return yv12buf;
//	}

    public byte[] decodeYUV420SP2RGB(byte[] yuv420sp, int width, int height) {
        final int frameSize = width * height;

        byte[] rgbBuf = new byte[frameSize * 3];

        // if (rgbBuf == null) throw new
        // NullPointerException("buffer 'rgbBuf' is null");
        if (rgbBuf.length < frameSize * 3)
            throw new IllegalArgumentException("buffer 'rgbBuf' size "
                    + rgbBuf.length + " < minimum " + frameSize * 3);

        if (yuv420sp == null)
            throw new NullPointerException("buffer 'yuv420sp' is null");

        if (yuv420sp.length < frameSize * 3 / 2)
            throw new IllegalArgumentException("buffer 'yuv420sp' size "
                    + yuv420sp.length + " < minimum " + frameSize * 3 / 2);

        int i = 0, y = 0;
        int uvp = 0, u = 0, v = 0;
        int y1192 = 0, r = 0, g = 0, b = 0;

        for (int j = 0, yp = 0; j < height; j++) {
            uvp = frameSize + (j >> 1) * width;
            u = 0;
            v = 0;
            for (i = 0; i < width; i++, yp++) {
                y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                y1192 = 1192 * y;
                r = (y1192 + 1634 * v);
                g = (y1192 - 833 * v - 400 * u);
                b = (y1192 + 2066 * u);

                if (r < 0)
                    r = 0;
                else if (r > 262143)
                    r = 262143;
                if (g < 0)
                    g = 0;
                else if (g > 262143)
                    g = 262143;
                if (b < 0)
                    b = 0;
                else if (b > 262143)
                    b = 262143;

                rgbBuf[yp * 3] = (byte) (r >> 10);
                rgbBuf[yp * 3 + 1] = (byte) (g >> 10);
                rgbBuf[yp * 3 + 2] = (byte) (b >> 10);
            }
        }// for
        return rgbBuf;
    }// decodeYUV420Sp2RGB

    public void changeCameraFacing() {
        destroyCamera();
        // camera = Camera.open((currentCamera + 1) % numOfCamera);
        currentCamera = (currentCamera + 1) % numOfCamera;
        startCamera();
        Log.d("InfowareLab.Debug","CameraInfo.CAMERA_FACING_FRONT ================");

    }

    public void setCurrentCamera(int cameraId) {
        currentCamera = cameraId;
    }

//	public void releaseCamera() {
//		if (camera != null) {
//			camera.setPreviewCallback(null);
//			camera.stopPreview();
//			camera.release();
//			h264HwEncoderImpl.releaseEncoder(); // sunny add 2016-5-19
//			camera = null;
//		}
//	}
    

    // private void changeVedioState(boolean isOpen){
    // CommonFactory.getInstance().getUserCommon().onChangeVideoState(
    // CommonFactory.getInstance().getUserCommon().getOwnID(), isOpen);
    // }

    public boolean getCamera() {
        return camera != null;
    }

    public int getCameraWidth() {
        return cameraWidth;
    }

    public int getCameraHeight() {
        return cameraHeight;
    }

    public void setInitVideo(boolean initVideo) {
        GLCameraView.initVideo = initVideo;
    }

    public boolean isShareing() {
        return isShareing;
    }

    public void setShareing(boolean isShareing) {
        GLCameraView.isShareing = isShareing;
    }

    public void setPortrait(boolean isPortrait) {
        this.isPortrait = isPortrait;
        if (isPortrait) {
            videoCommon.exChange(cameraHeight, cameraWidth);
        } else {
            videoCommon.exChange(cameraWidth, cameraHeight);
        }
    }

    public void setCameraLandscape() {
        degrees = 0;
        reStartLocalView();
    }
    public void setCameraLandscape270() {
        degrees = 180;
        reStartLocalView();
    }
    public void setCameraPortrait() {
        degrees = 90;
        reStartLocalView();
    }

    //	public void setParams(int width, int height) {
//		if (width == 1) {
//			isSmall = true;
//		} else {
//			isSmall = false;
//		}
//		LayoutParams params = (LayoutParams) getLayoutParams();
//		params.width = width;
//		params.height = height;
//		setLayoutParams(params);
//	}
    public void setParams(int width,int height) {
        if(width>1&&camera==null&&isEnabled()){
            if (ChatAPI.getInstance().getConfType() == 0)
                reStartLocalView();
        }
        if(width<=1){
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
            params.width = 1;
            params.height = 1;
            setLayoutParams(params);
        }else {
            /*if(degrees%180==0){
                if((1.0f*cameraWidth/cameraHeight)>(1.0f*width/height)){
                    height = (int) ((1.0f*cameraHeight/cameraWidth)*width);
                }else{
                    width = (int) ((1.0f*cameraWidth/cameraHeight)*height);
                }
            }else {
                if((1.0f*cameraHeight/cameraWidth)>(1.0f*width/height)){
                    height = (int) ((1.0f*cameraWidth/cameraHeight)*width);
                }else{
                    width = (int) ((1.0f*cameraHeight/cameraWidth)*height);
                }
            }*/
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();

            params.setMargins(0,0,0,0);

            params.width = width;//RelativeLayout.LayoutParams.MATCH_PARENT;
            params.height = height;//RelativeLayout.LayoutParams.MATCH_PARENT;
            setLayoutParams(params);

            Log.d("InfowareLab.Debug","GLCameraView.setParams: " + width + "x" + height);

        }
    }


    public boolean isPreview() {
        return isPreview;
    }

    public void switchSoftEncode(boolean isSoft){
        isHardCodec = videoCommon.isHardCodec();
        reStartLocalView();
    }
}
