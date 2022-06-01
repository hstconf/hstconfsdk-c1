package com.infowarelab.conference.ui.activity.inchat.view.video;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.infowarelab.conference.ConferenceApplication;
import com.infowarelabsdk.conference.callback.CallbackManager;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.VideoCommonImpl;
import com.infowarelabsdk.conference.domain.VideoBean;
import com.infowarelabsdk.conference.video.AvcHardEncoder;

//import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


/*SurfaceView不需要通过线程来更新视图，使用时还需要对其进行创建，销毁情况改变时进行监听，这就是需要实现Callback接口
 PreviewCallback回到接口，用于显示预览框
 */
public class LocalVideoView extends SurfaceView implements PreviewCallback, Callback {
	//private final Logger log = Logger.getLogger(getClass());
	private Context activity;


	/**
	 * save device
	 */

	public final static String DEVICE_NAME = "name";

	public static int cameraFPS = 0;
	public static int cameraWidth = 352;
	public static int cameraHeight = 288;

	private Camera camera;
	private int currentCamera = CameraInfo.CAMERA_FACING_FRONT;// 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
	private int numOfCamera = 1;
	File file = new File("/sdcard/testView.yuv");
	private FileOutputStream fos;
	// private Handler handler;
	private SurfaceHolder holder;
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
	private AvcHardEncoder  h264HwEncoderImpl = null;
	private static final int FRAME_RATE = 25;
	private boolean isHardCodec = true; // true
	private boolean isWriteFile = false;
	private boolean isEnabled = true;
	
	byte[] yv12buf;

	private int degrees = 90;
	public boolean isSmall = false;

	private File _fr = null;
	private FileOutputStream _out = null;

	private Path clipPath;
	private boolean isCircular;


	// end

	public LocalVideoView(Context context) {
		super(context);
		activity = context;
		init(context);
	}

	public LocalVideoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		activity = context;
		init(context);
	}

	public void init(Context context){

		if (isInited) return;

		this.activity = context;

		if(holder ==null){
			holder = this.getHolder();
		}
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		if (null == h264HwEncoderImpl)
			h264HwEncoderImpl = new AvcHardEncoder();  //release ???? sunny add 2016-5-23

		isHardCodec = h264HwEncoderImpl.IsSupportHardEncode();

		clipPath = new Path();

		isInited = true;
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		if (this.isCircular)
			canvas.clipPath(clipPath);
		super.dispatchDraw(canvas);
	}

	/**
	 * Crops the view in circular shape
	 * @param centerX
	 * @param centerY
	 * @param radius
	 */
	public void cropCircle(float centerX, float centerY, int radius) {
		Log.d("InfowareLab.Debug", "cropCircle: x=" + centerX + " ,y= " + centerY + ", radius=" + radius);
		clipPath.addCircle(centerX, centerY, radius, Path.Direction.CW);
	}

	/**
	 * Sets the flag for cropping the view in circular shape
	 * @param isCircular
	 */
	public void setCircular(boolean isCircular) {
		this.isCircular = isCircular;
		invalidate();
	}

	private Camera.Size getOptimalSize(int w, int h) {
		Camera.Parameters cameraParameter = camera.getParameters();
		List<Camera.Size> sizes = cameraParameter.getSupportedPreviewSizes();
		final double ASPECT_TOLERANCE = 0.1;
		// 竖屏是 h/w, 横屏是 w/h
		double targetRatio = (double) h / w;
		Camera.Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		for (Camera.Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Camera.Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}

		return optimalSize;
	}

	public Matrix calculateSurfaceHolderTransform(int width, int height) {
		// 预览 View 的大小，比如 SurfaceView
		int viewHeight = width;//configManager.getScreenResolution().y;
		int viewWidth = height;//configManager.getScreenResolution().x;

		int camHeight = cameraHeight;
		int camWidth = cameraWidth;

		// 相机选择的预览尺寸
		if (isPortrait) {
			camHeight = cameraWidth;
			camWidth = cameraHeight;
		}

		float scaleX = (float) 1.2;
		float scaleY = (float) 1.2;//, scaleY;
		//int cameraHeight = configManager.getCameraResolution().x;
		//int cameraWidth = configManager.getCameraResolution().y;
		// 计算出将相机的尺寸 => View 的尺寸需要的缩放倍数
//		float ratioPreview = (float) camWidth / camHeight;
//		float ratioView = (float) viewWidth / viewHeight;
//		float scaleX, scaleY;
//		if (ratioView < ratioPreview) {
//			scaleX = ratioPreview / ratioView;
//			scaleY = 1;
//		} else {
//			scaleX = 1;
//			scaleY = ratioView / ratioPreview;
//		}

		//loat ratioPreview = (float) camHeight / camWidth;

		//viewWidth/ratioPreview

		if (camHeight > camWidth) {
			scaleX = (float) (camHeight / camWidth * 3.6819);
			scaleY = scaleX;
		}
		else
		{
			scaleX = camWidth / camHeight;
			scaleY = scaleX;
		}

		// 计算出 View 的偏移量
		//float scaledWidth = viewWidth * scaleX;
		//float scaledHeight = viewHeight * scaleY;
		float dx = 0;//(camHeight - camWidth) / 2;
		float dy = (camHeight - camWidth) / 2;

		Matrix matrix = new Matrix();
		matrix.postScale(scaleX, scaleY);
		matrix.postTranslate(dx, dy);

		return matrix;
	}

	public int getCameraWidth() {
		return cameraWidth;
	}

	public int getCameraHeight() {
		return cameraHeight;
	}


	public void enableCamera(boolean enabled){isEnabled=enabled;}
	public boolean isEnabled(){return isEnabled;}

	public void startCamera() {
		openCamera();
		if(camera==null){
			return;
		}
		setCameraParameters(degrees);
		// start happy add for hard encode 2016-5-3
		if (Integer.parseInt(Build.VERSION.SDK) >= 16 && isHardCodec) {
			int ret;

			if (null == h264HwEncoderImpl)
				h264HwEncoderImpl = new AvcHardEncoder();


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
			camera.setPreviewDisplay(holder);
			//camera.setPreviewTexture(mSurfaceTexture);

		} catch (IOException e) {
			Log.e("InfowareLab.Debug", e.getMessage());
			e.printStackTrace();
		}
		changePreview(true);

		setBackgroundColor(0);
		Log.d("InfowareLab.Debug","start camera");
	}

	public void openCamera(){
		try {
				if (Integer.parseInt(Build.VERSION.SDK) > 8) {
					numOfCamera = Camera.getNumberOfCameras();
					if (numOfCamera == 1) {
						currentCamera = CameraInfo.CAMERA_FACING_BACK;
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
//			//log.error(e);
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
//			//log.error(e);
			videoCommon.onErrMsg(1);
			camera = null;
			return;
		}
	}
	/*
	 * 设置相机属性
	 */
	private void setCameraParameters(int degrees) {
		Parameters parameters = camera.getParameters();
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
	    if(initVideo){
	    	videoCommon.initializeMyVideo(cameraWidth, cameraHeight, FRAME_RATE);
	    	initVideo = false;
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
		if (focusModes.contains(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)){
			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 1连续对焦
			camera.setParameters(parameters);
			camera.cancelAutoFocus();
		}else{
			camera.setParameters(parameters);
		}

	}

	private void setCameraOrientation(int degrees, Parameters p){
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

		if (isShareing&& ConferenceApplication.isVideoMeeting) {

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
					if (currentCamera == CameraInfo.CAMERA_FACING_BACK) {
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
		if(isHardCodec&&h264HwEncoderImpl.GetMediaEncoder()!= null){
			h264HwEncoderImpl.flushEncoder();
		}
	}
		
		
	public void destroyCamera() {
		Log.d("InfowareLab.Debug","surfaceDestroyed !!!!!!!!!!!");
		if (camera != null) {
			Log.d("InfowareLab.Debug","camera != null !!!!!!!!!!!");
			camera.setPreviewCallback(null);
			changePreview(false);
			camera.stopPreview();
			;// 停止更新预览
			camera.release();// 释放资源
			if (h264HwEncoderImpl != null)
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
				invalidate();
				init(activity);
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
				camera.startPreview();
				isPreview = true;
			} else {
				camera.stopPreview();
				isPreview = false;
			}
		} catch (Exception e) {
			//log.error(e.getMessage());
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

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.d("InfowareLab.Debug","surfaceChanged: " + width + "x" + height);

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d("InfowareLab.Debug","surfaceCreated");
		// changeVedioState(true);
//		((Conference4PhoneActivity) activity).checkSyncVideo();
		isDestroyed = false;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if(!CallbackManager.IS_LEAVED) return;
		isDestroyed = true;
		destroyCamera();
	}

	// private void changeVedioState(boolean isOpen){
	// CommonFactory.getInstance().getUserCommon().onChangeVideoState(
	// CommonFactory.getInstance().getUserCommon().getOwnID(), isOpen);
	// }

	public boolean getCamera() {
		return camera != null;
	}

	public void setInitVideo(boolean initVideo) {
		LocalVideoView.initVideo = initVideo;
	}

	public boolean isShareing() {
		return isShareing;
	}

	public void setShareing(boolean isShareing) {
		LocalVideoView.isShareing = isShareing;
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
			reStartLocalView();
		}
		if(width<=1){
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
			params.width = 1;
			params.height = 1;
			setLayoutParams(params);
		}else {
			if(degrees%180==0){
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
			}
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
			params.width = width;
			params.height = height;
			setLayoutParams(params);
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
