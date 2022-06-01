package com.infowarelab.conference.ui.activity.inchat.view.ds;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.infowarelab.conference.ui.activity.inchat.Conference4PhoneActivity;
import com.infowarelab.conference.ui.activity.inchat.view.vp.ADSViewPager;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.ShareDtCommonImpl;

@SuppressLint("NewApi")
public class VideoSyncViewDS extends FrameLayout implements SurfaceHolder.Callback{
	
	/** surface生命周期 **/
	private SurfaceHolder holder;
	private Context activity;
	/** 视频SDK接口 **/
	protected CommonFactory commonFactory = CommonFactory.getInstance();
	private ShareDtCommonImpl asCommon =  (ShareDtCommonImpl) commonFactory.getSdCommon();;
	private LinearLayout.LayoutParams btnParames;
	private SurfaceView surface;
	private int pHeight;
	private TextView tvWait;
//	private FrameLayout relative;
	
	
	public VideoSyncViewDS(Context context) {
		super(context);
		initParames(context,0,0,0,0);
	}
	public VideoSyncViewDS(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.activity = context;
		initParames(context,0,0,0,0);
	}
	public VideoSyncViewDS(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.activity = context;
		initParames(context,0,0,0,0);
	}
	public VideoSyncViewDS(Context context,int textSizeSp,int textColor,int cameraSrc,int deleteSrc) {
		super(context);
		this.activity = context;
		initParames(context,0,0,0,0);
	}
	
	/**
	 * 初始化布局
	 * 默认字体12sp,white。
	 */
	private void initParames(Context context,int textSizeSp,int textColor,int cameraSrc,int deleteSrc){
		
//		 relative = new FrameLayout(context);
//	     relative.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,  
//	                LayoutParams.MATCH_PARENT,Gravity.RIGHT|Gravity.TOP));
//	     relative.setFocusable(false);
//	     relative.setOnTouchListener(null);
//	     addView(relative);
	}
	
	/**
	 * 初始化界面大小
	 * @param height 容器高
	 * @param width  宽
	 */
	public void initSize(int width ,int height,int sw,int sh) {
		setScreenSize(sw,sh);
		setParams(width, height);
		syncMatrix(width, height);
		refreshFrameLayout();
	}
	public void resetSize(int width,int height,int left, int right, int top, int bottom){
		Log.i("videosync", "videosync:width="+width+" height="+height+" left="+left+" right="+right+" top="+top+" bottom="+bottom);
		LayoutParams params = (LayoutParams) getLayoutParams();
		params.width = width;
		params.height = height;
		params.setMargins(left, top, right, bottom);
		setLayoutParams(params);
	}
	private void refreshFrameLayout(){
		matrix.getValues(targetValues);
        resetSize(
        		(int)(this.width*targetValues[Matrix.MSCALE_X]),
        		(int)(this.height*targetValues[Matrix.MSCALE_Y]),
        		(int)targetValues[Matrix.MTRANS_X],
        		(int)(screenW-this.width*targetValues[Matrix.MSCALE_X]-targetValues[Matrix.MTRANS_X]),
        		(int)targetValues[Matrix.MTRANS_Y],
        		(int)(screenH-this.height*targetValues[Matrix.MSCALE_Y]-targetValues[Matrix.MTRANS_Y])
        		);
	}
	private void syncMatrix(int width ,int height){
			float[] mv = new float[9];
			matrix.getValues(mv);
			mv[Matrix.MSCALE_X]=1;
			mv[Matrix.MSCALE_Y]=1;
			mv[Matrix.MTRANS_X]=(float) ((screenW-width)*1.0/2);
			mv[Matrix.MTRANS_Y]=(float) ((screenH-height)*1.0/2);
			matrix.setValues(mv);
	}
	/**
	 * 视频接收/停止接收
	 * @param isOpen
	 */
	public void changeStatus(boolean isOpen) {
			if(isOpen){
				if(null!=surface){
					surface.getHolder().getSurface().release();
					removeView(surface);
					surface = null;
				}
//				removeWait();
//				setBg(true);
				surface = new SurfaceView(this.activity);
		        surface.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, 
		        		android.view.ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));
//				LayoutParams layoutParams = new LayoutParams(this.width, 
//		        		this.height, Gravity.CENTER);
//				layoutParams.setMargins(0, (1500 - this.height)/2, 0, 0);
//				layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
//				layoutParams.topMargin = (1500 - this.width)/2;
//		        surface.setLayoutParams(new LayoutParams(this.width, 
//		        		this.height, Gravity.CENTER));
//		        surface.setLayoutParams(layoutParams);
		        surface.setZOrderMediaOverlay(true);
//		        surface.setBackgroundResource(R.drawable.bg_sharedt_wait_chat);
		        holder = surface.getHolder();
			    holder.addCallback(this);
			    addView(surface);
//			    showWait();
			}else {
				
				if(null!=surface){
					surface.getHolder().getSurface().release();
					removeView(surface);
					surface = null;
				}
//				showClosed();
//				setBg(false);
				asCommon.setIsReceive(false, null);
			}
	}
	private void showWait(){
		tvWait = new TextView(this.activity);
		tvWait.setTextSize(TypedValue.COMPLEX_UNIT_SP,40);
		tvWait.setTextColor(Color.BLACK);
		tvWait.setBackgroundColor(Color.WHITE);
		tvWait.setGravity(Gravity.CENTER);
		tvWait.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,  
                LayoutParams.MATCH_PARENT));
		tvWait.setText("Wait");
		addView(tvWait);
	}
	private void removeWait(){
		if(null!=tvWait){
			removeView(tvWait);
			tvWait = null;
		}
	}
	public void showClosed(){
		if(null!=tvWait){
			tvWait.setText("Closed");
			tvWait.setVisibility(View.VISIBLE);
		}
	}
	public void showSurface(){
		if(null!=tvWait){
			tvWait.setVisibility(View.GONE);
		}
	}
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
//		screenW=this.getWidth();
//		screenH=this.getHeight();
		Log.i("VideoSyncView", "VideoSyncView surfaceCreated");
//		drawWhite();
		if(null!=surface){
			HandlerThread surfaceThread = new HandlerThread("SurfaceHandler");
			surfaceThread.start();
			asCommon.setIsReceive(true, surface);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.i("VideoSyncView", "VideoSyncView surfaceChanged");
//		screenW=this.getWidth();
//		screenH=this.getHeight();
		this.holder=holder;
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i("VideoSyncView", "VideoSyncView surfaceDestroyed");
		
		asCommon.setIsReceive(false, null);
	}
	public void setParams(int width, int height) {
		this.width = width;
		this.height = height;
	}
	public void setScreenSize(int width, int height) {
		this.screenW = width;
		this.screenH = height;
	}

	public SurfaceView getSurfaceView(){
		return surface;
	}
	public void show(){
		setVisibility(View.VISIBLE);
	}
	public void hide(){
		setVisibility(View.GONE);
	}
	
	protected int width;
	protected int height;
	protected int screenW = Conference4PhoneActivity.pWidth;
	protected int screenH = Conference4PhoneActivity.pHeight;
	protected float minZoom = 1;
	protected float maxZoom = 3;
	protected PointF start = new PointF();
	protected PointF mid = new PointF();
	/** 图像缩放比例 */
	protected Matrix matrix = new Matrix();
	protected Matrix savedMatrix = new Matrix();
	protected float oldDist = 1f;
	protected float[] matrixValues = new float[9];
	protected float[] targetValues = new float[9];
	/**
	 * 操作类型
	 */
	protected static final int NONE = 0;
	protected static final int DRAG = 1;
	protected static final int ZOOM = 2;
	protected int mode = NONE;

	private int downX;
	private int downY;
	private boolean motionEventTypeISMoveAction;

	@Override  
    public boolean onTouchEvent(MotionEvent event) {  
  
        int action = event.getAction();  
        // 多点触摸的时候 必须加上MotionEvent.ACTION_MASK  
        switch (action & MotionEvent.ACTION_MASK) {  
        case MotionEvent.ACTION_DOWN:
        	savedMatrix.set(matrix);

			matrix.getValues(matrixValues);
			if (this.width * matrixValues[Matrix.MSCALE_X] - 1 >= screenW||this.height * matrixValues[Matrix.MSCALE_Y] - 1> screenH) {
				if (getParent().getParent().getParent() instanceof ADSViewPager) {
					getParent().getParent().getParent().requestDisallowInterceptTouchEvent(true);
				}
			}else{
				if (getParent().getParent().getParent() instanceof ADSViewPager) {
					getParent().getParent().getParent().requestDisallowInterceptTouchEvent(false);
				}
			}

			downX = (int) event.getRawX();
			downY = (int) event.getRawY();

            start.set(event.getX()+getX(), event.getY()+getY());
            // 初始为drag模式  
            mode = DRAG;
            break;  
  
        case MotionEvent.ACTION_POINTER_DOWN:
        	oldDist = spacing(event);  
        	if (oldDist > 10f) {
				savedMatrix.set(matrix);
				midPoint(mid, event);
				mode = ZOOM;
			}
            break;  
  
        case MotionEvent.ACTION_MOVE:  
            // drag模式  
            if (mode == DRAG) {
				int x = Math.abs((int)event.getRawX()-downX);
				int y = Math.abs((int)event.getRawY()-downY);
				if(x<5&&y<5){
					motionEventTypeISMoveAction = false;
					break;
				}else {
					motionEventTypeISMoveAction = true;
				}


                matrix.set(savedMatrix);
				float dx = event.getX()+getX() - start.x;
				float dy = event.getY()+getY() - start.y;

				matrix.getValues(matrixValues);
				float newPositionX = (matrixValues[Matrix.MTRANS_X] + dx);
				float newPositionY = (matrixValues[Matrix.MTRANS_Y] + dy);

				if (this.width * matrixValues[Matrix.MSCALE_X] - 1 >= screenW||this.height * matrixValues[Matrix.MSCALE_Y] - 1> screenH) {
					if (getParent().getParent().getParent() instanceof ADSViewPager) {
						getParent().getParent().getParent().requestDisallowInterceptTouchEvent(true);
					}
				}else{
					if (getParent().getParent().getParent() instanceof ADSViewPager) {
						getParent().getParent().getParent().requestDisallowInterceptTouchEvent(false);
					}

				}

				
				//当宽小于屏幕时避免平移
//				if(this.width * matrixValues[Matrix.MSCALE_X] <= screenW
//						&&(Math.abs(dx)>Math.abs(dy)))
//						{
//					dx = 0;
//				}else {

				/** X轴控制 Begin */
				
				if (this.width * matrixValues[Matrix.MSCALE_X] >= screenW) {
					// 图片宽度大于屏幕
					
					// 图片leftLine控制
					if (newPositionX >= 0) {
						dx = 0 - matrixValues[Matrix.MTRANS_X];
					}
					// 图片rightline控制
					else if (newPositionX < screenW - this.width
							* matrixValues[Matrix.MSCALE_X]) {
						dx = screenW - this.width
								* matrixValues[Matrix.MSCALE_X]
								- matrixValues[Matrix.MTRANS_X];
					}
				}else{
					// 图片宽度小于屏幕
					dx = 0;
				}
				/** X轴控制 End */

				/** Y轴控制 Begin */
				// 图片高度大于屏幕
				if (this.height * matrixValues[Matrix.MSCALE_Y] > screenH) {
					// 图片capline控制
					if (newPositionY >= 0) {
						dy = 0 - matrixValues[Matrix.MTRANS_Y];
					}
					// 图片baseline控制
					else if (newPositionY < screenH - this.height
							* matrixValues[Matrix.MSCALE_Y]) {
						dy = screenH - this.height
								* matrixValues[Matrix.MSCALE_Y]
								- matrixValues[Matrix.MTRANS_Y];
					}
				} else
				// 图片高度小于屏幕
				{
					dy = 0;
				}
				/** Y轴控制 End */
//				}
				matrix.postTranslate(dx, dy);
				refreshFrameLayout();
            } else if (mode == ZOOM) {  
            	float newDist = spacing(event);
				if (newDist > 10f) {
					matrix.set(savedMatrix);
					float scale = newDist / oldDist;
					doZoom(scale, scale);
					refreshFrameLayout();
				}
            }
           
            break;  
  
        case MotionEvent.ACTION_UP:
			if (mode == DRAG){
				if(!motionEventTypeISMoveAction){
//					getHandler().sendEmptyMessage(DOC_DISMISS_LIST);
					doClick();
					break;
				}else{
					motionEventTypeISMoveAction = false;
				}
			}
        	savedMatrix.set(matrix);
			mode = NONE;
			break;
        case MotionEvent.ACTION_POINTER_UP:  
        	savedMatrix.set(matrix);
			mode = NONE;
            break;  
        }  
  
        return true;  
  
    }
	private void doZoom(float scaleX, float scaleY) {
		matrix.postScale(scaleX, scaleY, mid.x, mid.y);
		matrix.getValues(matrixValues);
		boolean canZoom = false;
		matrix.set(savedMatrix);
		canZoom = scaleX >= 1f ? (matrixValues[Matrix.MSCALE_X] > maxZoom ? false : true)
				: (matrixValues[Matrix.MSCALE_X] < minZoom ? false : true);
		if (this.height * matrixValues[Matrix.MSCALE_Y] < screenH) {
			mid.y = screenH / 2;
		}
		if (this.width * matrixValues[Matrix.MSCALE_X] < screenW) {
			mid.x = screenW / 2;
		}
		// 正常缩放
		if (canZoom || (matrixValues[Matrix.MSCALE_X] <= maxZoom && matrixValues[Matrix.MSCALE_X] >= minZoom)) {
			// 放大倍数
			matrix.postScale(scaleX, scaleY, mid.x, mid.y);
			Log.i("AlwaysTest", "正常缩放"+matrixValues[Matrix.MSCALE_X]+","+minZoom+","+maxZoom);
		}
		// 小于最低缩放倍数
		else if (matrixValues[Matrix.MSCALE_X] < minZoom) {
			Log.i("AlwaysTest", "小于最低缩放倍数"+matrixValues[Matrix.MSCALE_X]+","+minZoom+","+maxZoom);
			matrix.getValues(matrixValues);
			matrix.postScale(minZoom / matrixValues[Matrix.MSCALE_X], minZoom / matrixValues[Matrix.MSCALE_Y], mid.x,
					mid.y);
		}
		// 大于最大放大倍数
		else {
			Log.i("AlwaysTest", "大于最大放大倍数"+matrixValues[Matrix.MSCALE_X]+","+minZoom+","+maxZoom);
			matrix.getValues(matrixValues);
			matrix.postScale(maxZoom / matrixValues[Matrix.MSCALE_X], maxZoom / matrixValues[Matrix.MSCALE_X], mid.x,
					mid.y);
		}
		matrix.getValues(matrixValues);
		// 缩放控制图片位置
		// 图片高度小于屏幕高度
		if (this.height * matrixValues[Matrix.MSCALE_Y] <= screenH) {
			matrix.postTranslate(0, (screenH - this.height * matrixValues[Matrix.MSCALE_Y]) / 2 - matrixValues[Matrix.MTRANS_Y]
					/*+ getResources().getDimensionPixelSize(R.dimen.height_6_80)*/);
		} else {
			// capline
			if (matrixValues[Matrix.MTRANS_Y] > 0) {
				matrix.postTranslate(0, 0- matrixValues[Matrix.MTRANS_Y]
						/*+ getResources().getDimensionPixelSize(R.dimen.height_6_80)*/);
			}
			// baseline
			else if (matrixValues[Matrix.MTRANS_Y] + this.height * matrixValues[Matrix.MSCALE_Y] < screenH) {
				matrix.postTranslate(0, screenH - matrixValues[Matrix.MTRANS_Y] - this.height
						* matrixValues[Matrix.MSCALE_Y]
						/*+ getResources().getDimensionPixelSize(R.dimen.height_6_80)*/);
			}
		}

		// 图片宽度小于屏幕宽度
		if (this.width * matrixValues[Matrix.MSCALE_X] <= screenW) {
			matrix.postTranslate((screenW - this.width * matrixValues[Matrix.MSCALE_X]) / 2
					- matrixValues[Matrix.MTRANS_X], 0);
		} else {
			// rightline
			if (matrixValues[Matrix.MTRANS_X] + this.width * matrixValues[Matrix.MSCALE_X] <= screenW) {
				matrix.postTranslate(screenW
						- (matrixValues[Matrix.MTRANS_X] + this.width * matrixValues[Matrix.MSCALE_X]), 0);
			}
			// leftline
			else if (matrixValues[Matrix.MTRANS_X] > 0) {
				matrix.postTranslate(0 - matrixValues[Matrix.MTRANS_X], 0);
			}
		}
	}
  
    //取两点的距离  
    private float spacing(MotionEvent event) {  
        try {  
            float x = event.getX(0) - event.getX(1);  
            float y = event.getY(0) - event.getY(1);  
            return (float) Math.sqrt(x * x + y * y);
        } catch (IllegalArgumentException ex) {  
            Log.v("TAG", ex.getLocalizedMessage());  
            return 0;  
        }  
    }  
  
    //取两点的中点  
    private void midPoint(PointF point, MotionEvent event) {  
        try {  
            float x = event.getX(0) + event.getX(1);  
            float y = event.getY(0) + event.getY(1);  
            point.set(x / 2+getX(), y / 2+getY());  
        } catch (IllegalArgumentException ex) {  
  
            //这个异常是android自带的，网上清一色的这么说。。。。  
            Log.v("TAG", ex.getLocalizedMessage());  
        }  
    }  
  
    public void setP1(){
		LayoutParams params = (LayoutParams) getLayoutParams();
		params.width = 1;
		params.height = 1;
		setLayoutParams(params);
	}
    public void setPM(int width,int height){
    	syncMatrix(width, height);
		refreshFrameLayout();
	}

	private OnClkListener onClkListener;

	public void setCkListener(OnClkListener onClkListener) {
		this.onClkListener = onClkListener;
	}
	public interface OnClkListener {
		public void doClick();
	}
	private void doClick() {
		if (onClkListener != null) {
			onClkListener.doClick();
		}
	}
}
