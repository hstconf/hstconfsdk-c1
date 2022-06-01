package com.infowarelab.conference.ui.activity.inconf.view.video;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.Surface;

import androidx.core.app.NotificationCompat;

import com.infowarelab.conference.ConferenceApplication;
import com.infowarelab.conference.ui.activity.inconf.ConferenceActivity;
import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.VideoCommonImpl;
import com.infowarelabsdk.conference.video.AvcHardEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

public class CameraService extends Service implements Camera.PreviewCallback {
    
    private static final String TAG = "InfowareLab.CameraService";
    Context activity;

    /**
     * save device
     */
    public final static String DEVICES = "devices";

    public final static String DEVICE_NAME = "name";

    public final static String VIDEOFORMAT = "videoFormat";

    public final static String ISFIRSTOPEN = "isFirstOpen";

    public static int cameraFPS = 30;
    public static int cameraWidth = 352;
    public static int cameraHeight = 288;

    private Camera camera;
    private int currentCamera = Camera.CameraInfo.CAMERA_FACING_FRONT;// 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
    private int numOfCamera = 1;
    File file = new File("/sdcard/testView.yuv");
    private FileOutputStream fos;
    // private Handler handler;
    private SurfaceTexture surfaceTexture = null;
    private static boolean initVideo = true;
    private static boolean isShareing = true;
    private boolean isPortrait = true;
    private VideoCommonImpl videoCommon = (VideoCommonImpl) CommonFactory
            .getInstance().getVideoCommon();
    private boolean isPreview = false;
    private boolean isOpen = false;

    public static boolean isDestroyed = false;

    // Video Hard Encoder
    private AvcHardEncoder h264HwEncoderImpl;
    private static final int FRAME_RATE = 30;
    private boolean isHardCodec = true; // true
    private boolean isWriteFile = false;

    byte[] yv12buf;

    private int degrees = 90;

    private File _fr = null;
    private FileOutputStream _out = null;

    private CameraBinder cameraBinder = new CameraBinder();
    private boolean isServiceStarted = false;
    private PowerManager.WakeLock wakeLock = null;
    private int mRotation = 90;

    /**
     * Start service or Stop service.
     */
    public enum Actions {
        START,
        STOP
    }

    public CameraService() {
    }

    public class CameraBinder extends Binder {
        /*返回SocketService 在需要的地方可以通过ServiceConnection获取到SocketService  */
        public CameraService getCameraService() {
            return CameraService.this;
        }
    }

    /**
     * Method to create the notification show to the user.
     *
     * @return Notification with all params.
     */
    private Notification createNotification() {
        String notificationChannelId = "InfowareLab conference channel";

        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(
                    notificationChannelId,
                    "InfowareLab conference channel",
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setVibrationPattern(new long[]{0});
            channel.setSound(null, null);

            channel.setDescription("InfowareLab conference channel");
            //channel.enableLights(false);
            //channel.setLightColor(Color.RED);
            //channel.enableVibration(false);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(
                    this,
                    notificationChannelId
            );
        } else {
            builder = new Notification.Builder(this);
        }

        Intent intent = new Intent(getApplicationContext(), ConferenceActivity.class);
        PendingIntent backIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent,0);

        return builder
                .setContentTitle("指挥调度")
                .setContentText("可视化指挥调度系统正在为您服务。")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.icon)
                .setTicker("指挥调度")
                .setContentIntent(backIntent)
                .setOngoing(true)
                .setDefaults(NotificationCompat.FLAG_ONLY_ALERT_ONCE)
                .setVibrate(new long[]{0l})
                .setSound(null)
                .setPriority(Notification.PRIORITY_MAX) // for under android 26 compatibility
                .build();

    }


    @Override
    public void onCreate() {

        Log.d(TAG,"onCreate: startForeground");
        startForeground(1, createNotification());
        super.onCreate();
    }

    /**
     * Method executed when the service is running.
     */
    private void startService() {

        // If the service already running, do nothing.
        if (isServiceStarted) return;

        Log.d(TAG,"Starting the camera service task");

        isServiceStarted = true;

        // we need this lock so our service gets not affected by Doze Mode
        PowerManager pm = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pm = (PowerManager) getSystemService(getApplicationContext().POWER_SERVICE);

            if (pm != null) {
                wakeLock = pm.newWakeLock(1, "InfowareLab.CameraService::lock");
                wakeLock.acquire();
            }
        }

        if (camera == null){
            init();
            changeStatus(true);
        }
    }

    /**
     * Method executed to stop the running service.
     */
    private void stopService() {

        if (!isServiceStarted) return;

        Log.d(TAG,"Stopping the foreground service");
        //Toast.makeText(this, "Service stopping", Toast.LENGTH_SHORT).show();
        try {
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
            if (camera != null){
                destroyCamera();
            }
            stopForeground(true);
            stopSelf();
        } catch (Exception e) {
            Log.d(TAG,"Service stopped without being started: ${e.message}");
        }
        isServiceStarted = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartCommand executed with startId: " + startId);

        // we need this lock so our service gets not affected by Doze Mode
        PowerManager pm = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pm = (PowerManager) getSystemService(getApplicationContext().POWER_SERVICE);

            if (pm != null) {
                wakeLock = pm.newWakeLock(1, "InfowareLab.CameraService::lock");
                wakeLock.acquire();
            }
        }

        if (camera == null) {
            init();
            changeStatus(true);
        }

//        if (intent != null) {
//            String action = intent.getAction();
//            Log.d(TAG,"using an intent with action " + action);
//            if (action != null) {
//                if (action.equals(Actions.START.name())) startService();
//                else if (action.equals(Actions.STOP.name())) stopService();
//                else Log.d(TAG,"This should never happen. No action in the received intent");
//            }
//        } else {
//            Log.d(TAG,"with a null intent. It has been probably restarted by the system.");
//        }

        // by returning this we make sure the service is restarted if the system kills the service
        return START_STICKY;

        //return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {

        Log.d(TAG,"The camera service has been destroyed".toUpperCase());

//        Intent service = new Intent(this, SocketService.class);
//        startService(service);

        if (wakeLock.isHeld()) {
            wakeLock.release();
        }

        // 如果Service被杀死，干掉通知
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager mManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            mManager.cancel(1);
        }

        stopForeground(true);

        if (camera != null){
            destroyCamera();
        }

//        Log.d(TAG, "DaemonService---->onDestroy，前台service被杀死");
//
//        // 重启自己
//        Intent intent = new Intent(getApplicationContext(), SocketService.class);
//        startService(intent);

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.

        mRotation = intent.getIntExtra("rotation", 90);

        Log.d("InfowareLab.CameraService","onBind: mRotation=" + mRotation);

        return cameraBinder;
    }

    public void init(){
        Log.d(TAG, "CameraService.init()");

        if (surfaceTexture == null) surfaceTexture = new SurfaceTexture(0);

        if (h264HwEncoderImpl == null) h264HwEncoderImpl = new AvcHardEncoder();  //release ???? sunny add 2016-5-23

        isHardCodec = h264HwEncoderImpl.IsSupportHardEncode();

//        if (isWriteFile) {
//            _fr = new File("/sdcard/yv12buf.src");
//            try {
//                _out = new FileOutputStream(_fr);
//            } catch (FileNotFoundException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
    }

    public void startCamera() {

        Log.d(TAG, "CameraService.startCamera()");

        openCamera();
        if(camera==null){
            Log.d(TAG, "CameraService.startCamera() Error");
            return;
        }
        setCameraParameters(degrees);
        // start happy add for hard encode 2016-5-3
        if (Integer.parseInt(Build.VERSION.SDK) >= 16 && isHardCodec) {
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
        try {
            camera.setPreviewTexture(surfaceTexture);

        } catch (IOException e) {
            e.printStackTrace();
        }
        changePreview(true);
    }

    public void openCamera(){

        Log.d(TAG, "CameraService.openCamera()");
        try {
            if (Integer.parseInt(Build.VERSION.SDK) > 8) {
                numOfCamera = Camera.getNumberOfCameras();
                if (numOfCamera == 1) {
                    currentCamera = Camera.CameraInfo.CAMERA_FACING_BACK;
                    camera = Camera.open(); // 底层默认打开的是后置摄像头
                } else {
                    camera = Camera.open(currentCamera);

                }
            } else {
                camera = Camera.open();
            }
            videoCommon.onErrMsg(2);
        } catch (Exception e) {
            Log.e(TAG,e.getMessage());
            videoCommon.onErrMsg(1);
            camera = null;
            return;
        }
        try {
            camera.getParameters();
        } catch (Exception e) {
            Log.e(TAG,e.getMessage());
            videoCommon.onErrMsg(1);
            camera = null;
            return;
        }
    }
    /*
     * 设置相机属性
     */
    private void setCameraParameters(int degrees) {
        Log.d(TAG, "CameraService.setCameraParameters(): " + degrees);
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        List<int[]> rates = parameters.getSupportedPreviewFpsRange();
        cameraWidth = 0;
        cameraHeight = 0;
        // 取比设定值小的像素中最大的
        for (Camera.Size size : previewSizes) {
            //Log.d(TAG, "Camera.Size = " + size.width + ", " + size.height + ", "
            //        + cameraFPS);
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

        Log.d(TAG, "Camera.CameraParameters = " + cameraWidth + ", " + cameraHeight);

        int minimum =0;
        int maximum =0;
        if(rates.size()>0){
            minimum = rates.get(0)[0];
            maximum = rates.get(0)[1];
        }

        if(ConferenceApplication.localVideoWidth != cameraWidth || ConferenceApplication.localVideoHeight != cameraHeight){

            if (initVideo && videoCommon != null)
                videoCommon.initializeMyVideo(cameraWidth, cameraHeight, FRAME_RATE);
            //initVideo = false;

            ConferenceApplication.localVideoWidth = cameraWidth;
            ConferenceApplication.localVideoHeight = cameraHeight;
        }

        //setCameraOrientation(degrees,parameters);

        setCameraDisplayOrientation(currentCamera, camera);

        parameters.setPreviewSize(cameraWidth, cameraHeight);//设置预览的高度和宽度,单位为像素
        //parameters.setPreviewFrameRate(FRAME_RATE);//设置图片预览的帧速。
        parameters.setPreviewFpsRange(minimum, maximum);
        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)){
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 1连续对焦
            camera.setParameters(parameters);
            camera.cancelAutoFocus();
        }else{
            camera.setParameters(parameters);
        }
    }

    /**
     * 设置相机显示方向的详细解读
     **/
    public void setCameraDisplayOrientation(int cameraId, Camera camera) {
        // 1.获取屏幕切换角度值。
        int rotation = mRotation; //getApplicationContext().getWindowManager().getDefaultDisplay()
                //.getRotation();

        int degree = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degree = 0; break;
            case Surface.ROTATION_90: degree = 90; break;
            case Surface.ROTATION_180: degree = 180; break;
            case Surface.ROTATION_270: degree = 270; break;
        }
        // 2.获取摄像头方向。
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        // 3.设置相机显示方向。
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degree) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degree + 360) % 360;
        }

        degrees = result;

        Log.d(TAG, "setCameraDisplayOrientation: degrees=" + result);

        camera.setDisplayOrientation(result);
    }


    private void setCameraOrientation(int degrees, Camera.Parameters p){
        if (Integer.parseInt(Build.VERSION.SDK) >= 8)
            setDisplayOrientation(camera, degrees);
        else
        {
            if (getApplicationContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            {
                p.set("orientation", "portrait");
                p.set("rotation", degrees);
            }
            if (getApplicationContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
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

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (data == null) {
            return;
        }
        if (isShareing && ConferenceApplication.isVideoMeeting) {


            if (ConferenceApplication.localVideoWidth != cameraWidth || ConferenceApplication.localVideoHeight != cameraHeight) {

                Log.d(TAG, "CameraService.initializeMyVideo for camera frame: " + cameraWidth + "x" + cameraHeight);
                videoCommon.initializeMyVideo(cameraWidth, cameraHeight, 30);

                ConferenceApplication.localVideoWidth = cameraWidth;
                ConferenceApplication.localVideoHeight = cameraHeight;
            }

            Log.d(TAG, "CameraService.onPreviewFrame: " + data.length);

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
                    // log.info("landscape data length = "+data.length);
                }
            }
        }

    }

    public void flushEncoder(){
        Log.d(TAG, "CameraService.flushEncoder()");
        if(isHardCodec&&h264HwEncoderImpl.GetMediaEncoder()!= null){
            h264HwEncoderImpl.flushEncoder();
        }
    }

    public void destroyCamera() {
        Log.d(TAG, "CameraService.destroyCamera()");

        if (camera != null) {
            camera.setPreviewCallback(null);
            changePreview(false);
            camera.stopPreview();
            camera.release();// 释放资源
            h264HwEncoderImpl.releaseEncoder(); // sunny add 2016-5-19
            camera = null;
            isOpen = false;
        }
    }
    public void reStartLocalView() {
        Log.d(TAG, "CameraService.reStartLocalView()");
        // if(camera != null){
        //log.info("reStartLocalView111");
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
        Log.d(TAG, "CameraService.changeStatus()");
        if (isOpenCamera) {
            if (camera == null) {
                startCamera();
            }
        } else {
            if (camera != null) {
                destroyCamera();
            }
        }
    }

    private void changePreview(boolean state) {
        Log.d(TAG, "CameraService.changePreview()");
        try {
            if (state) {
                camera.startPreview();
                isPreview = true;
            } else {
                camera.stopPreview();
                isPreview = false;
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
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
        Log.d(TAG, "CameraService.changeCameraFacing()");
        destroyCamera();
        currentCamera = (currentCamera + 1) % numOfCamera;
        startCamera();
    }

    public void setCurrentCamera(int cameraId) {
        currentCamera = cameraId;
    }

    public boolean getCamera() {
        return camera != null;
    }

    public void setInitVideo(boolean initVideo) {
        CameraService.initVideo = initVideo;
    }

    public boolean isShareing() {
        return isShareing;
    }

    public void setShareing(boolean isShareing) {

        Log.d(TAG, "CameraService.setShareing(): " + isShareing);
        CameraService.isShareing = isShareing;
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
        Log.d(TAG, "CameraService.setCameraLandscape(): " + degrees);
        reStartLocalView();
    }
    public void setCameraLandscape270() {
        degrees = 180;
        Log.d(TAG, "CameraService.setCameraLandscape(): " + degrees);
        reStartLocalView();
    }
    public void setCameraPortrait() {
        degrees = 90;
        Log.d(TAG, "CameraService.setCameraLandscape(): " + degrees);
        reStartLocalView();
    }

    public void setParams(int width,int height) {

        Log.d(TAG, "CameraService.setParams(): " + width + "x" + height);
        
    }
    
    public boolean isPreview() {
        return isPreview;
    }

    public void switchSoftEncode(boolean isSoft){
        isHardCodec = videoCommon.isHardCodec();
        reStartLocalView();
    }

}