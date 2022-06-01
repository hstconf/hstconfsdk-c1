package com.infowarelab.conference.ui.activity.inchat.view.share;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.infowarelab.conference.ConferenceApplication;
import com.infowarelab.conference.ui.view.PageViewDs;
import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.callback.CallbackManager;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.DocCommonImpl;
import com.infowarelabsdk.conference.common.impl.UserCommonImpl;
import com.infowarelabsdk.conference.domain.AnnotationBean;
import com.infowarelabsdk.conference.domain.DocBean;
import com.infowarelabsdk.conference.domain.EraserBean;
import com.infowarelabsdk.conference.domain.PageBean;
import com.infowarelabsdk.conference.domain.Point;
import com.infowarelabsdk.conference.domain.UserBean;
import com.infowarelabsdk.conference.shareDoc.DocCommon;
import com.infowarelabsdk.conference.shareDoc.callback.DSCallback;
import com.infowarelabsdk.conference.util.ColorUtil;
import com.infowarelabsdk.conference.util.Constants;

//import org.apache.log4j.Logger;
import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 文档视图
 * 
 * @author Sean.xie
 * 
 */
public class DocView4Phone extends ImageView implements OnTouchListener {
//	private Logger log=Logger.getLogger(getClass());
//	private static final float ANIMATION_WIDTH_OFFSET = 0.41010f;
//	private static final float ANIMATION_HEIGHT_OFFSET = 0.38953f;
	
	private static final float ANIMATION_WIDTH_OFFSET = 0.35010f;
	private static final float ANIMATION_HEIGHT_OFFSET = 0.32953f;
	
	private static final int POINTER = 1;
	private static final int NORMAL = 2;

	public static final String TAG = "DocView";
	private static final int DOC_DISMISS_MENU = 2001;
	private static final int DOC_DISMISS_LIST = 2002;
	/**
	 * 操作类型
	 */
	private static final int NONE = 0;
	private static final int DRAG = 1;
	private static final int ZOOM = 2;
	private int mode = NONE;

	private DocBean doc;

	/** 文档高,宽 */
	private int width = 0;
	private int height = 0;

	/** 开始注释 */
	private boolean isBeginAnno = false;

	private AnnotationBean annotation;
	
	/** 是否开始橡皮擦功能 */
	private boolean isBeginClean = false;
	
	private EraserBean eraser;

	private DocCommonImpl docCommon = (DocCommonImpl) CommonFactory.getInstance().getDocCommon();

	private DSCallback callback = new CallbackManager().getDsCallback();
	
	private Handler handler;

	private Bitmap bm = null;

	/** 图像缩放比例 */
	private Matrix matrix = new Matrix();
	private Matrix savedMatrix = new Matrix();
	private float[] matrixValues = new float[9];

	/** 开始移动的点 */
	private PointF start = new PointF();
	/** 中心点 */
	private PointF mid = new PointF();
	private float oldDist = 1f;

	/** 缩放限制 */
	private static float maxZoom = 3f;
	private static float minZoom = 0.2f;
	
	/** 当前屏幕的宽高**/
	private int mHeight = 0;
	private int mWidth = 0;
	private int rootHeight = 0;
	
	private boolean scaleIsZero = false;
	private int orientation = Configuration.ORIENTATION_PORTRAIT;
	private boolean isDoPointUp = false;
	private boolean isOffset = true;

	public DocView4Phone(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.setOnTouchListener(this);
	}

	public DocView4Phone(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setOnTouchListener(this);
	}

	public DocView4Phone(Context context) {
		super(context);
		this.setOnTouchListener(this);
	}

	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);	
		if(scaleIsZero){
			Log.d("InfowareLab.Debug", "scaleIsZero = true");
			setDoc(doc, orientation);
		}else{
			draw2Canvas(canvas, true);
		}
	}

	/**
	 * 保存图片
	 * 
	 * @throws Exception
	 */
	public void saveDoc(String path) throws Exception {
		Bitmap bitmap = bm.copy(Config.ARGB_8888, true);
		Canvas canvas = new Canvas(bitmap);
		draw2Canvas(canvas, false);
		OutputStream fos = null;
		try {
			canvas.save();
			canvas.restore();
			File file = new File(path);
			File pf = file.getParentFile();
			if (!pf.exists()) {
				pf.mkdirs();
			}
			fos = new FileOutputStream(file);
			bitmap.compress(CompressFormat.PNG, 100, fos);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			try {
				fos.flush();
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			bitmap.recycle();
			canvas = null;
		}
	}

	/**
	 * 将内容画到Canvas中
	 * 
	 * @param canvas
	 */
	private void draw2Canvas(Canvas canvas, boolean drawbm) {	
		if (drawbm) {
			canvas.setMatrix(matrix);
			if(bm != null && !bm.isRecycled()){
				if(bm.getWidth()==1&&bm.getHeight()==1){
					Paint p = new Paint();
					p.setColor(Color.WHITE);
					p.setStyle(Paint.Style.FILL);
					canvas.drawRect(0,0,width,height,p);
				}else{
					Log.d("InfowareLab.Debug", "draw2Canvas: " + bm.getWidth());
					canvas.drawBitmap(bm, 0, 0, null);
				}
			}
		}
		if (isBeginAnno) {
			drawAnnotation(canvas, annotation);
		}
		if (doc != null && null != doc.getPage()) {
			drawAnnotation(canvas, doc.getPage());
		}
		
	}

	/**
	 * 设置显示文档内容
	 * 
	 * @param doc
	 */
	public void setDoc(DocBean doc, int orientation) {
		//System.out.println("setDoc");

		Log.d("InfowareLab.Debug", "DocView4Phone.setDoc");

		this.isOffset = android.os.Build.VERSION.SDK_INT >= 24 ? false : true;
		if( null == doc)
			return ;
		
		try {
			this.doc = doc;
			this.orientation = orientation;
			
			matrix.reset();
			if( null == doc.getPage())
				return ;
			
			PageBean page = doc.getPage();
			int len = page.getLength();
			Log.d("InfowareLab.Debug", "page.getLength()=" + len);

			if(len==1){
//				bm = page.getData();
				bm = Bitmap.createBitmap(1, 1,
						Config.ARGB_8888);
				bm.eraseColor(Color.parseColor("#FFFFFF"));

				height = page.getHeight();
				width = page.getWidth();

				if(orientation == Configuration.ORIENTATION_LANDSCAPE){
					mHeight = ConferenceApplication.Screen_W-ConferenceApplication.StateBar_H-ConferenceApplication.NavigationBar_H;
					mWidth = ConferenceApplication.Screen_H;
					if(!isOffset){
						rootHeight = mHeight;
					}else{
						rootHeight = mHeight+ConferenceApplication.StateBar_H;
					}
				}else{
//					mHeight = Conference4PhoneActivity.pHeight-Conference4PhoneActivity.stateHeight;
//					mWidth = Conference4PhoneActivity.pWidth;
					mHeight = ConferenceApplication.Screen_H-ConferenceApplication.StateBar_H-ConferenceApplication.NavigationBar_H-ConferenceApplication.Bottom_H-ConferenceApplication.Top_H;
					mWidth = ConferenceApplication.Screen_W;
					if(!isOffset){
						rootHeight = mHeight;
					}else{
						rootHeight = ConferenceApplication.Top_H + mHeight+ConferenceApplication.StateBar_H;
					}
				}

//				rootHeight = Conference4PhoneActivity.topHeight + mHeight+Conference4PhoneActivity.stateHeight;
				int diff = rootHeight - mHeight;
				/*if (this.height <= mHeight && this.width <= mWidth) {
					minZoom = 1f;
					matrix.postTranslate((mWidth - this.width) / 2, (mHeight - this.height) / 2
							+ getRootView().findViewById(R.id.share_top).getHeight());
				} else {*/					
					minZoom = (float) mHeight / this.height;
					minZoom = minZoom > ((float) mWidth / this.width) ? ((float) mWidth / this.width)
							: minZoom;
										
					float scale = 1.0f * mWidth / this.width;
					if(scale >= 1.0f * mHeight / this.height){
						scale = 1.0f * mHeight / this.height;
					}
					
					//会出现scale为0的情况
					if(scale == 0){
						scale = 1;
					}
					
					if(scale != 0){
						float left = (mWidth - width * scale) / 2;
						float top = (mHeight - height * scale) / 2+diff;
//						if(isOffset){
//							top = top
//								+ Conference4PhoneActivity.topHeight
//								+ Conference4PhoneActivity.stateHeight
//									;
//						}

						
						matrix.postScale(scale, scale);		
						matrix.postTranslate(left, top);
						scaleIsZero = false;
					}else {
						scaleIsZero = true;
					}
				/*}*/
				matrix.getValues(matrixValues);
				requestLayout();
				invalidate();
			}else
			if (len > 0) {
				bm = page.getData();

				Log.d("InfowareLab.Debug", "bm=" + bm.toString());

				height = bm.getHeight();
				width = bm.getWidth();

//				Conference4PhoneActivity.pHeight = Conference4PhoneActivity.lWidth
//						- Conference4PhoneActivity.footHeight
//						- Conference4PhoneActivity.topHeight
//						;
//				Conference4PhoneActivity.lHeight = Conference4PhoneActivity.pWidth ;


				if(orientation == Configuration.ORIENTATION_LANDSCAPE){
//					mHeight = Conference4PhoneActivity.lHeight-Conference4PhoneActivity.stateHeight;
//					mWidth = Conference4PhoneActivity.lWidth;
					mHeight = ConferenceApplication.Screen_W-ConferenceApplication.StateBar_H-ConferenceApplication.NavigationBar_H;
					mWidth = ConferenceApplication.Screen_H;
					if(!isOffset){
						rootHeight = mHeight;
					}else{
						rootHeight = mHeight+ConferenceApplication.StateBar_H;
					}
				}else{
//					mHeight = Conference4PhoneActivity.pHeight-Conference4PhoneActivity.stateHeight;
//					mWidth = Conference4PhoneActivity.pWidth;
					mHeight = ConferenceApplication.Screen_H-ConferenceApplication.StateBar_H-ConferenceApplication.NavigationBar_H-ConferenceApplication.Bottom_H-ConferenceApplication.Top_H;
					mWidth = ConferenceApplication.Screen_W;
					if(!isOffset){
						rootHeight = mHeight;
					}else{
						rootHeight = ConferenceApplication.Top_H + mHeight+ConferenceApplication.StateBar_H;
					}
				}

//				rootHeight = Conference4PhoneActivity.topHeight + mHeight+Conference4PhoneActivity.stateHeight;
				int diff = rootHeight - mHeight;
					minZoom = (float) mHeight / this.height;
					minZoom = minZoom > ((float) mWidth / this.width) ? ((float) mWidth / this.width)
							: minZoom;

					float scale = 1.0f * mWidth / this.width;
					if(scale >= 1.0f * mHeight / this.height){
						scale = 1.0f * mHeight / this.height;
					}

					//会出现scale为0的情况
					if(scale == 0){
						scale = 1;
					}

					if(scale != 0){
						float left = (mWidth - width * scale) / 2;
						float top = (mHeight - height * scale) / 2+diff;

						matrix.postScale(scale, scale);
						matrix.postTranslate(left, top);
						scaleIsZero = false;
					}else {
						scaleIsZero = true;
					}
				matrix.getValues(matrixValues);
				requestLayout();
				invalidate();

			} else {
				getRootView().findViewById(R.id.shareNoDoc).setVisibility(View.VISIBLE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 图片缩放
	 *
	 * @param scale
	 */
	public void zoom(float scale) {
		if(bm != null && !bm.isRecycled()){
			float[] values = new float[9];
			matrix.getValues(values);
			if (values[Matrix.MSCALE_X] >= maxZoom && scale > 1) {
				return;
			}
			doZoom(scale, scale);
			savedMatrix.set(matrix);
			invalidate();
		}
	}

	/**
	 * 显示接收的注释
	 *
	 * @param canvas
	 * @param page
	 */
	private void drawAnnotation(Canvas canvas, PageBean page) {
		if (page == null || page.getPageID() == 0)
			return;
		ArrayList<AnnotationBean> annotations = (ArrayList<AnnotationBean>) page.getAnnotations().clone();
		if (annotations == null || annotations.isEmpty())
			return;
		try {
			for (AnnotationBean bean : annotations) {
				try {
					drawAnnotation(canvas, bean);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private Bitmap bTemp;
	private Canvas c;
	private int downX;

	private int downY;
	private boolean motionEventTypeISMoveAction;
	/**
	 * 显示本地操作的注释
	 *
	 * @param canvas
	 * @param annotation
	 */
	private void drawAnnotation(Canvas canvas, AnnotationBean annotation) {
		try {
			if (annotation != null) {
				float scaleX, scaleY;
				if(DocCommonImpl.isAnimation){
					scaleY = (float) height / (float) annotation.getHeight() * ANIMATION_HEIGHT_OFFSET;
					scaleX = (float) width / (float) annotation.getWidth() * ANIMATION_WIDTH_OFFSET;
				}else{
					scaleY = (float) height / (float) annotation.getHeight();
					scaleX = (float) width / (float) annotation.getWidth();
				}
				Paint mPaint = new Paint();
				if (annotation.getColor() != null) {
					mPaint.setColor(Color.parseColor(annotation.getColor()));
				} else {
					mPaint.setColor(Color.BLACK);
				}
				mPaint.setTextSize(annotation.getLineWidth());
				if (annotation.getFilltype() == Constants.FILL_FLAG) {
					mPaint.setStyle(Paint.Style.FILL);
				} else {
					mPaint.setStyle(Paint.Style.STROKE);
				}
				int wid = annotation.getLineWidth();
				matrix.getValues(matrixValues);

				if((wid*matrixValues[Matrix.MSCALE_X])<1){
					wid = (int) Math.ceil((1/matrixValues[Matrix.MSCALE_X]));
				}
				mPaint.setStrokeWidth(wid);
//				mPaint.setStrokeWidth(annotation.getLineWidth());
				mPaint.setAntiAlias(true);
				if (Constants.SHAPE_RECT.equals(annotation.getAnnoPattern())) {
					Rect r = new Rect();
					r.set((int) (annotation.getPoint(0).getX() * scaleX),
							(int) (annotation.getPoint(0).getY() * scaleY),
							(int) (annotation.getPoint(1).getX() * scaleX),
							(int) (annotation.getPoint(1).getY() * scaleY));
					canvas.drawRect(r, mPaint);
				} else if (Constants.SHAPE_ELLIPSE.equals(annotation.getAnnoPattern())) {
					RectF rectF = new RectF(annotation.getPoint(0).getX() * scaleX, annotation.getPoint(0).getY()
							* scaleY, annotation.getPoint(1).getX() * scaleX, annotation.getPoint(1).getY() * scaleY);
					canvas.drawOval(rectF, mPaint);
				} else if (Constants.SHAPE_POLY_LINE.equals(annotation.getAnnoPattern())) {
					List<Point> points = annotation.getPoints();
					if (points != null) {
						if (annotation.getPolygonPattern().equals(Constants.STROKE_HILIGHT)) {
							mPaint.setAlpha(128);
						}
//						Path path = new Path();
//						float x = points.get(0).getX();
//						float y = points.get(0).getY();
//						if((x * scaleX)>=width)
//							x=(((float)width/scaleX)-2);
//						if((y * scaleY)>=height)
//							y=(((float)height/scaleY)-2);
//						if(x<=0)x=1;
//						if(y<=0)y=1;
//						path.moveTo(x * scaleX, y * scaleY);
//						for (int i = 1; i < points.size(); i++) {
//							x = points.get(i).getX();
//							y = points.get(i).getY();
////							if(x>width)x=width-1;
////							if(x<=0)x=1;
////							if(y>height)y=height-1;
////							if(y<=0)y=1;
//							if(x<=0||y<=0||(x * scaleX)>=width||(y * scaleY)>=height)continue;
//							path.lineTo(x * scaleX, y * scaleY);
//						}
//						if (Constants.SHAPE_CLOSE.equals(annotation.getPolygonPattern())) {
//							path.close();
//						}
//						canvas.drawPath(path, mPaint);
						Path path = new Path();
						boolean isFrist = true;
						for (int i = 0; i < points.size(); i++) {
							float x = points.get(i).getX();
							float y = points.get(i).getY();
							if(android.os.Build.VERSION.SDK_INT > 20&&android.os.Build.VERSION.SDK_INT <23){
								if(x*scaleX>=width)x = (float)width/scaleX -1;
								if(y*scaleY*matrixValues[Matrix.MSCALE_Y]>=ConferenceApplication.Screen_H)y = (float)ConferenceApplication.Screen_H/(scaleY*matrixValues[Matrix.MSCALE_Y]) -1;
								if(x<=0)x=1;
								if(y<=0)y=1;
							}else {
								if(x*scaleX>=width)x = (float)width/scaleX -1;
								if(y*scaleY>=height)y = (float)height/scaleY -1;
								if(x<=0)x=1;
								if(y<=0)y=1;
							}
							if(isFrist){
								path.moveTo(x*scaleX, y*scaleY);
								isFrist = false;
							}else {
								path.lineTo(x * scaleX, y * scaleY);
							}
						}
						if (Constants.SHAPE_CLOSE.equals(annotation.getPolygonPattern())&&!isFrist) {
							path.close();
						}
						if(!isFrist)
						canvas.drawPath(path, mPaint);
					}
				} else if (Constants.SHAPE_LINE.equals(annotation.getAnnoPattern())) {
					float x=annotation.getPoint(0).getX();
					float y=annotation.getPoint(0).getY();
					float x1=annotation.getPoint(1).getX();
					float y1=annotation.getPoint(1).getY();
					//log.info("scaleX:"+scaleX+" scaleY:"+scaleY);
					//log.info("receive point: ("+x+","+y+") ("+x1+","+y1+")");
					//log.info("phone point: ("+x*scaleX+","+y* scaleY+") ("+x1*scaleX+","+y1* scaleY+")");
					canvas.drawLine(annotation.getPoint(0).getX() * scaleX, annotation.getPoint(0).getY() * scaleY,
							annotation.getPoint(1).getX() * scaleX, annotation.getPoint(1).getY() * scaleY, mPaint);

				} else if(Constants.SHAPE_POINTER.equals(annotation.getAnnoPattern())){
//					int zoom = 1;
//					float textSize = 14.0f;
//
//					if(width>=1080 || height >=1920){
//						zoom = 1;
//					}else if(width>=720 || height >=1280){
//						zoom = 2;
//					}else if(width>=480 || height >=800){
//						zoom = 3;
//						textSize = 12.0f;
//					}else{
//						zoom = 4;
//						textSize = 10.0f;
//					}
//
//					Options options = new Options();
//					options.inJustDecodeBounds = false;
//					options.inSampleSize = zoom;
//
//					Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.arrow, options);

					float x = annotation.getPoint(0).getX() * scaleX;
					float y = annotation.getPoint(0).getY() * scaleY;
					float lineLong = getResources().getDimension(R.dimen.height_101_80)/matrixValues[Matrix.MSCALE_X];
					float txtSize = getResources().getDimension(R.dimen.height_101_80)/matrixValues[Matrix.MSCALE_X];
					float height = getResources().getDimension(R.dimen.height_102_80)/matrixValues[Matrix.MSCALE_X];
					float lineWidth = getResources().getDimension(R.dimen.line1)/matrixValues[Matrix.MSCALE_X];
					int words = 2;
					String name = "";
					if(annotation.getRoleOrname()!=null){
						name = annotation.getRoleOrname().trim();
					}
					if(name==null||name.equals(""))name = "Attender";
					try {
						name = idgui(name, 10);
						words = getWords(name, 2);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					mPaint.setStrokeWidth(lineWidth);
			        canvas.drawLine(x, y, x+lineLong+(height/10), y+lineLong+(height/10), mPaint);

			        //画圆角矩形
			        mPaint.setStyle(Paint.Style.FILL);//充满
			        mPaint.setAntiAlias(true);// 设置画笔的锯齿效果
			        RectF oval3 = new RectF(x+lineLong, y+lineLong, x+txtSize*words/2+lineLong+height, y+height+lineLong);// 设置个新的长方形
			        canvas.drawRoundRect(oval3, height/2, height/2, mPaint);//第二个参数是x半径，第三个参数是y半径

			        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
			        textPaint.setTextSize(txtSize);
			        textPaint.setTypeface(Typeface.DEFAULT);
			        textPaint.setColor(Color.WHITE);
			        canvas.drawText(name, x+height/2+lineLong, y+(height+2*txtSize)/3+lineLong, textPaint);

//					float textSize = 18.0f;
//					Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.arrow);
//					  float s=(float)(1.0f*mWidth/(6*bitmap1.getWidth()));
//					  matrix.getValues(matrixValues);
//
//					  s = s/matrixValues[Matrix.MSCALE_X];
//					  textSize = textSize/matrixValues[Matrix.MSCALE_X];
//
//					  Matrix matrix = new Matrix();
//					  matrix.postScale(s, s); // 长和宽放大缩小的比例
//					  Bitmap bitmap = Bitmap.createBitmap(bitmap1, 0, 0, bitmap1.getWidth(),
//							  bitmap1.getHeight(), matrix, true);
//
//					mPaint.setAlpha(200);
//					canvas.drawBitmap(drawCupBitmap(bitmap, mPaint.getColor()), annotation.getPoint(0).getX() * scaleX,
//							annotation.getPoint(0).getY() * scaleY, mPaint);
//					float x = annotation.getPoint(0).getX() * scaleX;
//					float y = annotation.getPoint(0).getY() * scaleY;
//
//					Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
//					textPaint.setTextSize(textSize);
//					textPaint.setTypeface(Typeface.DEFAULT);
//					textPaint.setColor(Color.WHITE);
//					String str = annotation.getRoleOrname().trim();
//					if(str.length() > 6){
//						str = str.substring(0, 7) + "..";
//					}
//					canvas.drawText(str, annotation.getPoint(0).getX() * scaleX
//							+ bitmap.getWidth() / 4,
//							annotation.getPoint(0).getY() * scaleY + bitmap.getHeight()*4/5
//							, textPaint);


				}else if(Constants.SHAPE_POLYGON.equals(annotation.getAnnoPattern())){
					List<Point> points = annotation.getPoints();
					if (points != null) {
						if (annotation.getPolygonPattern().equals(Constants.STROKE_HILIGHT)) {
							mPaint.setAlpha(128);
						}
//						Path path = new Path();
//						path.moveTo(points.get(0).getX() * scaleX, points.get(0).getY() * scaleY);
//						for (int i = 1; i < points.size(); i++) {
//							path.lineTo(points.get(i).getX() * scaleX, points.get(i).getY() * scaleY);
//						}
//						if (Constants.SHAPE_CLOSE.equals(annotation.getPolygonPattern())) {
//							path.close();
//						}
//						canvas.drawPath(path, mPaint);
						Path path = new Path();
						boolean isFrist = true;
						for (int i = 0; i < points.size(); i++) {
							float x = points.get(i).getX();
							float y = points.get(i).getY();
							if(android.os.Build.VERSION.SDK_INT > 20&&android.os.Build.VERSION.SDK_INT <23){
								if(x*scaleX>=width)x = (float)width/scaleX -1;
								if(y*scaleY*matrixValues[Matrix.MSCALE_Y]>=ConferenceApplication.Screen_H)y = (float)ConferenceApplication.Screen_H/(scaleY*matrixValues[Matrix.MSCALE_Y]) -1;
								if(x<=0)x=1;
								if(y<=0)y=1;
							}else {
								if(x*scaleX>=width)x = (float)width/scaleX -1;
								if(y*scaleY>=height)y = (float)height/scaleY -1;
								if(x<=0)x=1;
								if(y<=0)y=1;
							}
							if(isFrist){
								path.moveTo(x*scaleX, y*scaleY);
								isFrist = false;
							}else {
								path.lineTo(x * scaleX, y * scaleY);
							}
						}
						if (Constants.SHAPE_CLOSE.equals(annotation.getPolygonPattern())&&!isFrist) {
							path.close();
						}
						if(!isFrist)
						canvas.drawPath(path, mPaint);
					}
				}else if(Constants.SHAPE_WRONG.equals(annotation.getAnnoPattern())){
					Point start = annotation.getPoint(0);
					Point end = annotation.getPoint(1);
					canvas.drawLine(start.x * scaleX, start.y * scaleY, end.x * scaleX, end.y * scaleY, mPaint);
					canvas.drawLine(start.x * scaleX, end.y * scaleY, end.x * scaleX, start.y * scaleY, mPaint);
				}else if(Constants.SHAPE_RIGHT.equals(annotation.getAnnoPattern())){
					Point start = annotation.getPoint(0);
					Point end = annotation.getPoint(1);
					Path path = new Path();
			    	path.moveTo(start.x * scaleX , end.y * scaleY - (end.y * scaleY - start.y * scaleY)/3);
			    	path.lineTo((start.x * scaleX + end.x * scaleX)/2, end.y * scaleY);
			    	path.lineTo(end.x * scaleX, start.y * scaleY);
			    	canvas.drawPath(path,mPaint);
				}else if(Constants.SHAPE_TEXT.equals(annotation.getAnnoPattern())){
					float x = annotation.getPoint(0).getX() * scaleX;
					float y = annotation.getPoint(0).getY() * scaleY;

					TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
					textPaint.setTextSize(Math.abs(annotation.getAnnText().height)*scaleY);
					textPaint.setTypeface(Typeface.DEFAULT);
					textPaint.setFakeBoldText(annotation.getAnnText().weight==700?true:false);
					textPaint.setTextSkewX(annotation.getAnnText().italic==0?0:(float) -0.25);
					textPaint.setStrikeThruText(annotation.getAnnText().strikeOut==1?true:false);
					textPaint.setUnderlineText(annotation.getAnnText().underline==1?true:false);
					if (annotation.getColor() != null) {
						textPaint.setColor(Color.parseColor(annotation.getColor()));
					} else {
						textPaint.setColor(Color.BLACK);
					}


					StaticLayout mTextLayout = new StaticLayout(annotation.getAnnText().txt, textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
					canvas.save();
					canvas.translate(x, y);
					mTextLayout.draw(canvas);
					canvas.restore();

//					Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
//					float top = fontMetrics.ascent;
//					canvas.drawText(annotation.getAnnText().txt, x, y-top, textPaint);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

//		canvas.save();
//		canvas.drawBitmap(bTemp, 0, 0, null);
//		canvas.restore();
//		bTemp.recycle();
	}

	/**
	 * 设置bitmap的颜色
	 * @param bitmap
	 * @param desColor
	 * @return
	 */
	private Bitmap drawCupBitmap(Bitmap bitmap, int desColor){
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int array []  = new int[w*h];
        int n =0;
        for(int i=0;i<h;i++){
            for(int j=0;j<w;j++){ //从上往下扫描
                int color = bitmap.getPixel(j,i);
                if(color != 0){
                    color = desColor;//设置同一颜色
                }
                array[n] = color;
                n++;
            }
        }

        return Bitmap.createBitmap(array, w, h, Config.ARGB_8888) ;
    }

	//获取当前doc X、Y方向上的缩放比例
	private float[] getDocXYScale(DocBean doc){
		float[] scaleXY = new float[2];
		//X轴变化量
		scaleXY[0] = 1.0f * doc.getPageWidth() / width;
		//Y轴变化量
		scaleXY[1] = 1.0f * doc.getPageHeight() / height;
		return scaleXY;
	}

	//初始化annotation,把第一个点放进annotation里
	private AnnotationBean initAnnotation(DocBean doc, DocCommon docCommon, Point point,String annoType){
		if(doc.getPage() != null){
			UserBean self = ((UserCommonImpl)CommonFactory.getInstance().getUserCommon()).getSelf();
			AnnotationBean annotation = new AnnotationBean();
			annotation.addPoint(point);
			annotation.setFilltype(Constants.STROKE_FLAG);
			annotation.setDocID(doc.getDocID());
			annotation.setPageID(doc.getPage().getPageID());
			annotation.setUserId(self.getUid());
			annotation.setLineWidth(Integer.valueOf(Constants.STROKE_LINE1));
			annotation.setAnnoPattern(annoType);
			if(Constants.SHAPE_POINTER.equals(annoType)){
				annotation.setBmpId(self.getUid() % 5);
				annotation.setColor(ColorUtil.getHex(annotation.getBmpId()));
			}else{
				annotation.setColor(getColor(annColor));
			}

			String username = self.getUsername();
			int usernameLenght = username.length();

			annotation.setWidth(doc.getPageWidth());
			annotation.setHeight(doc.getPageHeight());
			annotation.setRoleOrname(username.substring(0, usernameLenght));
			annotation.setPointerAnnt(true);
			return annotation;
		}else{
			return null;
		}

	}

	//移动过程中，把点加入Annotation中
	private void addPoint2Annotation(AnnotationBean annotation, Point point){
		if (Constants.SHAPE_POLY_LINE.equals(annotation.getAnnoPattern())) {
			annotation.addPoint(point);
		} else {
			annotation.replacePoint(1, point);
		}
	}

	//发送本地注释到服务器
	private void sendAnnotation2Service(final AnnotationBean annotation, final int operType){
		new Thread() {
			@Override
			public void run() {
				if(operType == DocCommon.ANNOTATION_OPT_TYPE_ADD){
					if(null != doc && null != doc.getPage()){
						docCommon.removeOneAnno(doc.getPage().getMyPreAnnotation());
					}
					int annotationId = docCommon.createPointerAnnt(annotation);
//					System.out.println("return aid:"+annotationId);
					annotation.setAnnotationID(annotationId);
				}else if (operType == DocCommon.ANNOTATION_OPT_TYPE_DEL){

					docCommon.removeOneAnno(annotation);
					/*ConferenceJni.sendAnnotation(handler, convertJson(annotation, operType)
							.getBytes().length, convertJson(annotation, operType).getBytes());*/
				}
//				Log.i("DocView", "annotation : " + annotation);
			}
		}.start();
	}

	private boolean doDrawAnno(MotionEvent event, Point point,int type){
		if(type == POINTER){
			return drawPointer(event, point);
		}else{
			return drawPolygon(event, point);
		}

	}
	//画注释
	private boolean drawPolygon(MotionEvent event, Point point){
		//log.info("Polygon1 event = "+event.getAction());
		switch (event.getAction()) {

			case MotionEvent.ACTION_DOWN:
//				//log.info("Polygon DOWN ");
				isBeginAnno = true;
				annotation = initAnnotation(doc, docCommon, point,Constants.SHAPE_POLY_LINE);
				break;

			case MotionEvent.ACTION_MOVE:
//				//log.info("Polygon MOVE ");
				if (!isBeginAnno || annotation == null)
					return false;
				//log.info("drawPolygon annotation:" + (annotation==null?"true":"false"));
				if(null != annotation){
				addPoint2Annotation(annotation, point);
				invalidate();
				}
				break;

			case MotionEvent.ACTION_UP:
				//log.info("Polygon UP");
				if (isBeginAnno) {
					isBeginAnno = false;

					if(null != annotation){
						annotation.addPoint(point);
						int annotationID = docCommon.createPolyLine(annotation);
						// //log.info("createPolygon annoID = "+annotationID);
						annotation.setAnnotationID(annotationID);
						if (null != doc && null != doc.getPage()) {
							doc.getPage().addAnnotation(annotation);
						}
					}
				}
				invalidate();
				break;
			case MotionEvent.ACTION_CANCEL:
				//log.info("Polygon cancel");
				if (isBeginAnno) {
					isBeginAnno = false;
					if(null != annotation){
						annotation.addPoint(point);
						int annotationID = docCommon.createPolyLine(annotation);
						//log.info("createPolygon annoID = "+annotationID);
						annotation.setAnnotationID(annotationID);
						if(null != doc && null != doc.getPage()){
							doc.getPage().addAnnotation(annotation);
						}
					}
				}
				invalidate();
				break;
			}

			return true;
	}
	//画标注
	private boolean drawPointer(MotionEvent event, Point point){
		switch (event.getAction()) {

				case MotionEvent.ACTION_DOWN:
					break;

				case MotionEvent.ACTION_MOVE:
					break;

				case MotionEvent.ACTION_UP:
					AnnotationBean annotation = initAnnotation(doc, docCommon, point,Constants.SHAPE_POINTER);
					if(annotation != null && !docCommon.isStartToShowPage()){
						//sendAnnotation2Service(annotation, DocCommon.ANNOTATION_OPT_TYPE_ADD);
						if(null != doc && null != doc.getPage()){
							docCommon.removeOneAnno(doc.getPage().getMyPreAnnotation());
						}else{
							docCommon.removeOneAnno(annotation);
						}
						int annotationId = docCommon.createPointerAnnt(annotation);
//						System.out.println("return aid:"+annotationId);
						annotation.setAnnotationID(annotationId);

						if(doc.getPage() != null){
							doc.getPage().addAnnotation(annotation);
							doc.getPage().setPreAnnotation(annotation);
							this.annotation = annotation;
							invalidate();
						}else{
							docCommon.removeOneAnno(annotation);
						}

					}

					break;
				}

				return true;
	}
	/**
	 * 本地画注释
	 *
	 * @param event
	 * @return
	 */
	private boolean drawAnnotation(MotionEvent event,int type) {
		matrix.getValues(matrixValues);
		float[] scaleXY = getDocXYScale(doc);
		Point point = transCuttentPoint2AbsolutePoint(event, matrixValues, scaleXY);
//		Point point = new Point(event.getRawX(), event.getRawY());
		if (!DocCommonImpl.isWhiteBoard &&point.getX() >= 0 && point.getY() >= 0 && point.getX() * ANIMATION_WIDTH_OFFSET <= doc.getPageWidth()
				&& point.getY() * ANIMATION_HEIGHT_OFFSET <= doc.getPageHeight()) {
			doDrawAnno(event, point,type);
		} else if (point.getX() >= 0 && point.getY() >= 0 && point.getX() <= doc.getPageWidth()
				&& point.getY() <= doc.getPageHeight()) {
			doDrawAnno(event, point,type);
		}else{
			//log.info("Polygon UP send");
			if (isBeginAnno && annotation != null) {
				isBeginAnno = false;

				annotation.addPoint(point);
				int annotationID = docCommon.createPolyLine(annotation);
				//log.info("createPolygon annoID = "+annotationID);
				annotation.setAnnotationID(annotationID);
//				doc.getPage().addAnnotation(annotation);
				if(null != doc && null != doc.getPage()){
					doc.getPage().addAnnotation(annotation);
				}
			}
			invalidate();
		}
		return true;
	}


	//根据获取的Point和Annotation中的值去对比，相同的话就删除page对应的Annotation
	private synchronized ArrayList<AnnotationBean> delAnnoFromPage(Point point, DocBean doc){
		ArrayList<AnnotationBean> al = new ArrayList<AnnotationBean>();
		ArrayList<AnnotationBean> annos = new ArrayList<AnnotationBean>();
		if(null != doc.getPage()){
			annos = new ArrayList<AnnotationBean>(doc.getPage().getAnnotations());
		}

		Iterator<AnnotationBean> it = annos.iterator();
		while(it.hasNext()){
			boolean flag = true;	//flag放这防止IllegalStateException，否则it.remove执行两次以上就要报这个错
			AnnotationBean anno = it.next();
			for(Point p : anno.getPoints()){
//				//log.info("dele x = "+(p.getX()-point.getX())+" y = "+(p.getY()-point.getY())+" flag = "+flag);
//				//log.info("dele x = "+Math.abs(p.getX()-point.getX())+" y = "+Math.abs(p.getY()-point.getY())+" flag = "+flag);
				if(Math.abs(p.getX()-point.getX())<ConferenceApplication.SCREEN_HEIGHT/3 && Math.abs(p.getY()-point.getY())<ConferenceApplication.SCREEN_HEIGHT/3&& flag){//加减一百是因为给的图片尺寸是几千的，太小就选不到点了
//					Log.e(TAG, "dele: "+anno.toString());
//					it.remove();	//删除annotation

					al.add(anno);
					flag = false;
				}
			}
		}
		return al;
	}

	private void doDrawEraser(MotionEvent event, Point point){
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_MOVE:
		case MotionEvent.ACTION_UP:
			isBeginClean = true;
			ArrayList<AnnotationBean> al = delAnnoFromPage(point, doc);
			if(al == null || al.isEmpty()){
				//log.info("can not find anno");
			}
			for(AnnotationBean anno : al){
				sendAnnotation2Service(anno, DocCommon.ANNOTATION_OPT_TYPE_DEL);
			}
			invalidate();
			break;
		}
	}

	//本地画橡皮擦，相当于在画透明的线
	private boolean drawEraser(MotionEvent event){
		matrix.getValues(matrixValues);
		float[] scaleXY = getDocXYScale(doc);

//		Log.e(TAG, "event.getRawX = " + event.getRawX() + ", matrixValues[Matrix.MTRANS_X] = " + matrixValues[Matrix.MTRANS_X]
//		                + ", matrixValues[Matrix.MSCALE_X] = " + matrixValues[Matrix.MSCALE_X] + ", scaleXY[0] = " + scaleXY[0] + ", event.getRawY() = " + event.getRawY() + ", matrixValues[Matrix.MTRANS_Y] = "
//		                + matrixValues[Matrix.MTRANS_Y] + ", matrixValues[Matrix.MSCALE_Y] = " + matrixValues[Matrix.MSCALE_Y] + ", scaleXY[1] = " + scaleXY[1]);

		Point point = transCuttentPoint2AbsolutePoint(event, matrixValues, scaleXY);
//		Point point = new Point(event.getRawX(), event.getRawY());
		if (DocCommonImpl.isAnimation && point.getX() >= 0 && point.getY() >= 0 && point.getX() * ANIMATION_WIDTH_OFFSET <= doc.getPageWidth()
				&& point.getY() * ANIMATION_HEIGHT_OFFSET <= doc.getPageHeight()) {
			doDrawEraser(event, point);
		} else if (point.getX() >= 0 && point.getY() >= 0 && point.getX() <= doc.getPageWidth()
				&& point.getY() <= doc.getPageHeight()) {
			doDrawEraser(event, point);
		}
		return true;
	}

	/**
	 * 把在当前在图片上获取的点     转换成     原始图片上对应的点
	 * @param event	点击事件
	 * @param matrixValues	Matrix对象的值被存放在这里
	 * @param scaleXY	page的长宽和doc长宽的比值
	 * @return
	 */
	private Point transCuttentPoint2AbsolutePoint(MotionEvent event, float[] matrixValues, float[] scaleXY){
		Point point;
		if(isOffset){
			if(DocCommonImpl.isAnimation){
				point = new Point((int) (((event.getRawX() - matrixValues[Matrix.MTRANS_X])
						/ matrixValues[Matrix.MSCALE_X] * scaleXY[0]) / ANIMATION_WIDTH_OFFSET) , (int) (((event.getRawY() - matrixValues[Matrix.MTRANS_Y])
						/ matrixValues[Matrix.MSCALE_Y] * scaleXY[1]) / ANIMATION_HEIGHT_OFFSET));
			}else{
				point = new Point((int) ((event.getRawX() - matrixValues[Matrix.MTRANS_X])
						/ matrixValues[Matrix.MSCALE_X] * scaleXY[0]), (int) ((event.getRawY() - matrixValues[Matrix.MTRANS_Y])
						/ matrixValues[Matrix.MSCALE_Y] * scaleXY[1]));
			}
		}else{
			if(DocCommonImpl.isAnimation){
				point = new Point((int) (((event.getX() - matrixValues[Matrix.MTRANS_X])
						/ matrixValues[Matrix.MSCALE_X] * scaleXY[0]) / ANIMATION_WIDTH_OFFSET) , (int) (((event.getY() - matrixValues[Matrix.MTRANS_Y])
						/ matrixValues[Matrix.MSCALE_Y] * scaleXY[1]) / ANIMATION_HEIGHT_OFFSET));
			}else{
				point = new Point((int) ((event.getX() - matrixValues[Matrix.MTRANS_X])
						/ matrixValues[Matrix.MSCALE_X] * scaleXY[0]), (int) ((event.getY() - matrixValues[Matrix.MTRANS_Y])
						/ matrixValues[Matrix.MSCALE_Y] * scaleXY[1]));
			}
		}

		//log.info("trans isAnimation = "+DocCommonImpl.isAnimation+" x1="+event.getRawX()+" y1="+event.getRawY()+" x2="+point.getX()+" y2="+point.getY());
		return point;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		//检测点击时是否菜单弹出
//		getHandler().sendEmptyMessage(DOC_DISMISS_MENU);

		if(doc != null){
			if(annTool == ToolEnum.POINTER){
				if (getParent().getParent().getParent() instanceof PageViewDs) {
					getParent().getParent().getParent().requestDisallowInterceptTouchEvent(true);
				}
				drawAnnotation(event,POINTER);
			}else if(annTool == ToolEnum.PEN){
				if (getParent().getParent().getParent() instanceof PageViewDs) {
					getParent().getParent().getParent().requestDisallowInterceptTouchEvent(true);
				}
				if(docCommon.getAnnotation() == null){
					return pointerAction(event);
				}
				drawAnnotation(event,NORMAL);
				return true;
			}else if(annTool == ToolEnum.ERASER){
				if (getParent().getParent().getParent() instanceof PageViewDs) {
					getParent().getParent().getParent().requestDisallowInterceptTouchEvent(true);
				}
				if(docCommon.getAnnotation() == null){
					return pointerAction(event);
				}
				drawEraser(event);
				return true;
			}else {
				return pointerAction(event);
			}
		}
		return true;

	}

	private boolean pointerAction(MotionEvent event){

		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			savedMatrix.set(matrix);
			matrix.getValues(matrixValues);
			if(bm != null&&(this.width * matrixValues[Matrix.MSCALE_X] -1 > mWidth||this.height * matrixValues[Matrix.MSCALE_Y] - 1> mHeight)){
				if (getParent().getParent().getParent() instanceof PageViewDs) {
					getParent().getParent().getParent().requestDisallowInterceptTouchEvent(true);
				}
			}else{
				if (getParent().getParent().getParent() instanceof PageViewDs) {
					getParent().getParent().getParent().requestDisallowInterceptTouchEvent(false);
				}
			}
			downX = (int) event.getRawX();
			downY = (int) event.getRawY();
			start.set(event.getX(), event.getY());
			mode = DRAG;
			isDoPointUp = false;
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			oldDist = spacing(event);
			if (oldDist > 10f) {
				savedMatrix.set(matrix);
				midPoint(mid, event);
				mode = ZOOM;
				if (getParent().getParent().getParent() instanceof PageViewDs) {
					getParent().getParent().getParent().requestDisallowInterceptTouchEvent(true);
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			if (mode == DRAG){
				if(!motionEventTypeISMoveAction){
					getHandler().sendEmptyMessage(DOC_DISMISS_LIST);
					break;
				}else{
					motionEventTypeISMoveAction = false;
				}
			}

			if(!isDoPointUp && Math.abs(event.getX() - start.x) >= mWidth / 8
				&& doc != null){
				if(doc.getPage() != null
						&& docCommon.getPageCount(doc.getDocID()) >1){
					if((event.getX() - start.x) > 0){
						if (doc.getPage().getStepCount()>0) {
							if(doc.getPage().getCurrentStep()==0){
								if (doc.getPage().getPageID() == 1) {
									Toast.makeText(getContext(),
											getContext().getString(R.string.prePage),
											Toast.LENGTH_SHORT).show();
								} else {
									if(docCommon.getStepCount(doc.getDocID(), doc.getPage()
											.getPageID() - 1)>0){
										long count = docCommon.getStepCount(doc.getDocID(), doc.getPage()
												.getPageID() - 1);
										docCommon.switchPageStep(doc.getDocID(), doc.getPage()
												.getPageID() - 1, count);
									}else {
										docCommon.switchPage(doc.getDocID(), doc.getPage()
												.getPageID() - 1);
									}
								}
							}else if (doc.getPage().getCurrentStep()==1) {
								docCommon.switchPage(doc.getDocID(), doc.getPage()
										.getPageID());
							}else{
								docCommon.switchPageStep(doc.getDocID(), doc.getPage()
										.getPageID(), doc.getPage().getCurrentStep()-1);
							}
						}else {
							if (doc.getPage().getPageID() == 1) {
								Toast.makeText(getContext(),
										getContext().getString(R.string.prePage),
										Toast.LENGTH_SHORT).show();
							} else {
								if(docCommon.getStepCount(doc.getDocID(), doc.getPage()
										.getPageID() - 1)>0){
									long count = docCommon.getStepCount(doc.getDocID(), doc.getPage()
											.getPageID() - 1);
									docCommon.switchPageStep(doc.getDocID(), doc.getPage()
											.getPageID() - 1, count);
								}else {
								docCommon.switchPage(doc.getDocID(), doc.getPage()
										.getPageID() - 1);
								}
							}
						}
					}else{
						if (doc.getPage().getStepCount()>0) {
							if(doc.getPage().getCurrentStep()==doc.getPage().getStepCount()){
								if (doc.getPage().getPageID() == docCommon
										.getPageCount(doc.getDocID())) {
									Toast.makeText(getContext(),
											getContext().getString(R.string.nextPage),
											Toast.LENGTH_SHORT).show();
								} else {
									docCommon.switchPage(doc.getDocID(), doc.getPage()
											.getPageID() + 1);
								}
							}else{
								docCommon.switchPageStep(doc.getDocID(), doc.getPage()
										.getPageID(), doc.getPage().getCurrentStep()+1);
							}
						}else {
							if (doc.getPage().getPageID() == docCommon
									.getPageCount(doc.getDocID())) {
								Toast.makeText(getContext(),
										getContext().getString(R.string.nextPage),
										Toast.LENGTH_SHORT).show();
							} else {
								docCommon.switchPage(doc.getDocID(), doc.getPage()
										.getPageID() + 1);
							}
						}
					}
				}

			}

			break;
		case MotionEvent.ACTION_POINTER_UP:
			savedMatrix.set(matrix);
			mode = NONE;
			isDoPointUp = true;
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode == DRAG) {
				int x = Math.abs((int)event.getRawX()-downX);
				int y = Math.abs((int)event.getRawY()-downY);
				if(x<5&&y<5){
					motionEventTypeISMoveAction = false;
					break;
				}else {
					motionEventTypeISMoveAction = true;
				}

				float dx = event.getX() - start.x;
				float dy = event.getY() - start.y;
				matrix.set(savedMatrix);

				matrix.getValues(matrixValues);
				// X轴平移距离
				float newPositionX = (matrixValues[Matrix.MTRANS_X] + dx);
				// Y轴平移距离
				float newPositionY = (matrixValues[Matrix.MTRANS_Y] + dy);

				if(bm != null&&(this.width * matrixValues[Matrix.MSCALE_X] -1 > mWidth||this.height * matrixValues[Matrix.MSCALE_Y] -1 > mHeight)){
					if (getParent().getParent().getParent() instanceof PageViewDs) {
						getParent().getParent().getParent().requestDisallowInterceptTouchEvent(true);
					}
				}else{
					if (getParent().getParent().getParent() instanceof PageViewDs) {
						getParent().getParent().getParent().requestDisallowInterceptTouchEvent(false);
					}
					return false;
				}


				/** X轴控制 Begin */
				// 图片宽度大于屏幕
				if (bm != null && this.width * matrixValues[Matrix.MSCALE_X] >= mWidth) {
					// 圖片leftLine控制
					if (newPositionX >= 0) {
						dx = 0 - matrixValues[Matrix.MTRANS_X];
					}
					// 圖片rightline控制
					else if (newPositionX < mWidth - this.width * matrixValues[Matrix.MSCALE_X]) {
						dx = mWidth - this.width * matrixValues[Matrix.MSCALE_X]
								- matrixValues[Matrix.MTRANS_X];
					}
				} else
				// 图片宽度小于屏幕
				{
					dx = 0;
					/**
					 * 小图可拖动 // 图片leftline控制 if (newPositionX < 0f) { dx = 0
					 * - matrixValues[Matrix.MTRANS_X]; } // 图片rightline控制
					 * else if (newPositionX > getRootView().getWidth() -
					 * this.width matrixValues[Matrix.MSCALE_X]) { dx =
					 * getRootView().getWidth() - this.width *
					 * matrixValues[Matrix.MSCALE_X] -
					 * matrixValues[Matrix.MTRANS_X]; }
					 */
				}
				/** X轴控制 End */

				/** Y轴控制 Begin */
				// 图片高度大于屏幕
				if (bm != null && this.height * matrixValues[Matrix.MSCALE_Y] >= mHeight) {
					// 圖片capline控制
					if (newPositionY >= rootHeight - mHeight) {
						dy = rootHeight - mHeight - matrixValues[Matrix.MTRANS_Y];
					}
					// 圖片baseline控制
					else if (newPositionY < rootHeight - this.height
							* matrixValues[Matrix.MSCALE_Y]) {
						dy = rootHeight - this.height * matrixValues[Matrix.MSCALE_Y]
								- matrixValues[Matrix.MTRANS_Y] ;
					}
				} else
				// 图片高度小于屏幕
				{
					dy = 0;
					/**
					 * 小图可拖动 // 图片baseline控制 if ((newPositionY +
					 * this.height * matrixValues[Matrix.MSCALE_Y]) >=
					 * getRootView() .getHeight()) { dy =
					 * rootHeight -
					 * matrixValues[Matrix.MTRANS_Y] - this.height
					 * matrixValues[Matrix.MSCALE_Y]; } // 图片capline控制 else
					 * if (newPositionY < rootHeight -
					 * getHeight()) { dy = rootHeight -
					 * getHeight() - matrixValues[Matrix.MTRANS_Y]; }
					 */
				}
				/** Y轴控制 End */
				matrix.postTranslate(dx, dy);
			} else if (mode == ZOOM) {
				float newDist = spacing(event);
				if (newDist > 10f) {
					matrix.set(savedMatrix);
					float scale = newDist / oldDist;
					if(bm != null && !bm.isRecycled()){
						doZoom(scale, scale);
					}
				}
			}
			break;
		}
		invalidate();
		requestLayout();
		return true; // indicate event was handled
	}

	/**
	 * 缩放图片
	 *
	 * @param scaleX
	 * @param scaleY
	 */
	private void doZoom(float scaleX, float scaleY) {
		matrix.postScale(scaleX, scaleY, mid.x, mid.y);
		matrix.getValues(matrixValues);
		boolean canZoom = false;
		matrix.set(savedMatrix);
		canZoom = scaleX >= 1f ? (matrixValues[Matrix.MSCALE_X] > maxZoom ? false : true)
				: (matrixValues[Matrix.MSCALE_X] < minZoom ? false : true);
		if (this.height * matrixValues[Matrix.MSCALE_Y] < getHeight()) {
			mid.y = getHeight() / 2 + (rootHeight - getHeight());
		}
		if (this.width * matrixValues[Matrix.MSCALE_Y] < getWidth()) {
			mid.x = getWidth() / 2;
		}
		// 正常缩放
		if (canZoom || (matrixValues[Matrix.MSCALE_X] <= maxZoom && matrixValues[Matrix.MSCALE_X] >= minZoom)) {
			// 放大倍数
			matrix.postScale(scaleX, scaleY, mid.x, mid.y);
		}
		// 小于最低缩放倍数
		else if (matrixValues[Matrix.MSCALE_X] < minZoom) {
			matrix.getValues(matrixValues);
			matrix.postScale(minZoom / matrixValues[Matrix.MSCALE_X], minZoom / matrixValues[Matrix.MSCALE_Y], mid.x,
					mid.y);
		}
		// 大于最大放大倍数
		else {
			matrix.getValues(matrixValues);
			matrix.postScale(maxZoom / matrixValues[Matrix.MSCALE_X], maxZoom / matrixValues[Matrix.MSCALE_X], mid.x,
					mid.y);
		}
		matrix.getValues(matrixValues);
		// 缩放控制图片位置
		// 图片高度小于屏幕高度
		if (this.height * matrixValues[Matrix.MSCALE_Y] <= mHeight) {
			matrix.postTranslate(0, (mHeight - this.height * matrixValues[Matrix.MSCALE_Y]) / 2
					+ (rootHeight - mHeight) - matrixValues[Matrix.MTRANS_Y]
					/*+ getResources().getDimensionPixelSize(R.dimen.height_6_80)*/);
		} else {
			// capline
			if (matrixValues[Matrix.MTRANS_Y] > (rootHeight - mHeight)) {
				matrix.postTranslate(0, (rootHeight - mHeight) - matrixValues[Matrix.MTRANS_Y]
						/*+ getResources().getDimensionPixelSize(R.dimen.height_6_80)*/);
			}
			// baseline
			else if (matrixValues[Matrix.MTRANS_Y] + this.height * matrixValues[Matrix.MSCALE_Y] < rootHeight) {
				matrix.postTranslate(0, rootHeight - matrixValues[Matrix.MTRANS_Y] - this.height
						* matrixValues[Matrix.MSCALE_Y]
						/*+ getResources().getDimensionPixelSize(R.dimen.height_6_80)*/);
			}
		}

		// 图片宽度小于屏幕宽度
		if (this.width * matrixValues[Matrix.MSCALE_X] <= getWidth()) {
			matrix.postTranslate((getWidth() - this.width * matrixValues[Matrix.MSCALE_X]) / 2
					- matrixValues[Matrix.MTRANS_X], 0);
		} else {
			// rightline
			if (matrixValues[Matrix.MTRANS_X] + this.width * matrixValues[Matrix.MSCALE_X] <= getWidth()) {
				matrix.postTranslate(getWidth()
						- (matrixValues[Matrix.MTRANS_X] + this.width * matrixValues[Matrix.MSCALE_X]), 0);
			}
			// leftline
			else if (matrixValues[Matrix.MTRANS_X] > 0) {
				matrix.postTranslate(0 - matrixValues[Matrix.MTRANS_X], 0);
			}
		}
	}

	/**
	 * 计算移动距离
	 *
	 * @param event
	 * @return
	 */
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x * x + y * y);
	}

	/**
	 * 计算中点位置
	 *
	 * @param point
	 * @param event
	 */
	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	/**
	 * 封装注释数据成json格式
	 *
	 * @param annotation
	 * @return
	 * @throws JSONException
	 */
	private String convertJson(AnnotationBean annotation, int opType) {
		StringBuilder json = new StringBuilder();
		StringBuilder arrays = new StringBuilder();
		try {
			json.append("{");

			if(opType == DocCommon.ANNOTATION_OPT_TYPE_ADD){
				json.append("\"doType\":\"").append("add").append("\",");
				json.append("\"annoPattern\":\"").append(annotation.getAnnoPattern()).append("\",");
				json.append("\"content\":{");
				json.append("\"docID\":").append(annotation.getDocID()).append(",");
				json.append("\"pageID\":").append(annotation.getPageID()).append(",");

				if (annotation.getAnnoPattern().equals(Constants.SHAPE_POLYGON)) {
					json.append("\"polygonPattern\":\"" + annotation.getPolygonPattern() + "\",");
					arrays.append(",\"array\":[");
					for (Point point : annotation.getPoints()) {
						arrays.append("{\"pointX\":" + point.getX() + ", \"pointY\":" + point.getY() + "},");
					}
					arrays.deleteCharAt(arrays.length() - 1);
					arrays.append("]");
				} else {
					json.append("\"point1X\":").append((int) (annotation.getPoint(0).getX())).append(",");
					json.append("\"point1Y\":").append((int) (annotation.getPoint(0).getY())).append(",");
					json.append("\"point2X\":").append((int) (annotation.getPoint(1).getX())).append(",");
					json.append("\"point2Y\":").append((int) (annotation.getPoint(1).getY())).append(",");
				}

				json.append("\"lineWidth\":").append(annotation.getLineWidth()).append(",");
				json.append("\"color\":\"").append(Integer.parseInt(annotation.getColor().substring(1), 16)).append("\",");
				json.append("\"fillType\":").append(annotation.getFilltype()).append(",");
				json.append("\"screenHeight\":").append(annotation.getHeight()).append(",");
				json.append("\"screenWidth\":").append(annotation.getWidth());
				json.append("}");
				json.append(arrays);
				json.append("}");
			}else if (opType == DocCommon.ANNOTATION_OPT_TYPE_DEL){
				json.append("\"doType\":\"").append("del").append("\",");
				json.append("\"annoPattern\":\"").append("polygon").append("\",");
				json.append("\"content\":{");
				json.append("\"docID\":").append(annotation.getDocID()).append(",");
				json.append("\"pageID\":").append(annotation.getPageID()).append(",");
				json.append("\"objID\":").append(annotation.getAnnotationID()).append("}");
				json.append("}");
			}


		} catch (Exception e) {
			e.printStackTrace();
		}
		return json.toString();
	}

	/**
	 * 添加注释
	 *
	 * @param annotation
	 */
	public void addAnnotation(AnnotationBean annotation) {
		if(doc != null){
			//如果添加的是指示，删除page中其他指示
			/*if(annotation.isPointerAnnt()){
				doc.getPage().removeAnnotation(annotation, true);
			}*/

			//doc.getPage().getAnnotations().add(annotation);
			invalidate();
		}
	}

	public void setHandler(Handler handler){
		this.handler = handler;
	}

	public Handler getHandler(){
		return this.handler;
	}

	/**
	 * 删除注释
	 *
	 * @param annotationID
	 */
	public void removeAnnotation(Integer annotationID) {
		if(doc != null && null != doc.getPage()){
			ArrayList<AnnotationBean> annotationBeans = doc.getPage().getAnnotations();
			if(annotationBeans != null){
				invalidate();
			}

		}

	}
	private int getWords(String s,int minSize)throws Exception{
		int changdu = s.getBytes("GBK").length;
		if(changdu > minSize){
			return changdu;
		}else {
			return minSize;
		}
	}
	private String idgui(String s,int num)throws Exception{
        int changdu = s.getBytes("GBK").length;
        if(changdu > num){
            s = s.substring(0, s.length() - 1);
            s = idgui2(s,num)+"…";
        }
        return s;
    }
	private String idgui2(String s,int num)throws Exception{
		int changdu = s.getBytes("GBK").length;
		if(changdu > num){
			s = s.substring(0, s.length() - 1);
			s = idgui2(s,num);
		}
		return s;
	}


	public enum ColorEnum{
		BLACK,GRAY,WHITE,GREEN,YELLOW,RED;
	}
	public enum ToolEnum{
		PEN,ERASER,POINTER,TOUCH;
	}

	private ColorEnum annColor = ColorEnum.BLACK;
	private ToolEnum annTool = ToolEnum.TOUCH;


	public void setAnnColor(ColorEnum color){
		annColor = color;
	}
	public void setAnnTool(ToolEnum tool){
		annTool = tool;
	}

	public ToolEnum getAnnTool(){
		return annTool;
	}


	public void removePointer(){
		if(null != docCommon && null != doc&&doc.getPage() != null && doc.getPage().getMyPreAnnotation() != null){
			docCommon.removeOneAnno(doc.getPage().getMyPreAnnotation());
			invalidate();
		}
	}

	public String getColor(ColorEnum colorId){
		if(colorId==ColorEnum.BLACK){
            return Constants.COLOR_BLACK;
        }else if(colorId==ColorEnum.GRAY){
            return Constants.COLOR_GRAY;
        }else if(colorId==ColorEnum.WHITE){
            return Constants.COLOR_WHITE;
        }else if(colorId==ColorEnum.GREEN){
            return Constants.COLOR_GREEN;
        }else if(colorId==ColorEnum.YELLOW){
            return Constants.COLOR_YELLOW;
        }else if(colorId==ColorEnum.RED){
            return Constants.COLOR_RED;
        }else{
            return Constants.COLOR_RED;
        }
	}
}
