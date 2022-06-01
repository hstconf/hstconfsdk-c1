package com.infowarelab.conference.ui.action;

////import org.apache.log4j.Logger;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import com.infowarelab.hongshantongphone.R;
import com.infowarelab.conference.ui.view.CallattDialog.OnResultListener;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.DocCommonImpl;
import com.infowarelabsdk.conference.domain.AnnotationType;
import com.infowarelabsdk.conference.domain.EraserBean;
import com.infowarelabsdk.conference.util.Constants;


/**
 * 注释操作控制
 *
 * @author Sean.xie
 */
public class AnnotationAction implements OnClickListener {
    //private final Logger log = Logger.getLogger(getClass());

    protected CommonFactory commonFactory = CommonFactory.getInstance();
    private AnnotationType annotation = ((DocCommonImpl) commonFactory.getDocCommon()).getAnnotation();
    private EraserBean eraser = ((DocCommonImpl) commonFactory.getDocCommon()).getEraser();
    private View view;
    private boolean isAnnoIn = true;
    private ImageButton ibDrag;

    private ToggleButton colorBtn, redBtn, greenBtn, yellowBtn, eraserBtn, penBtn, pointingBtn;
    private LinearLayout colorMore;

    private OnPointerStateChange onPointerStateChange;

    public AnnotationAction(View view) {
        this.view = view;
        colorBtn = (ToggleButton) view.findViewById(R.id.annotationColor);
        redBtn = (ToggleButton) view.findViewById(R.id.annotationColorRed);
        greenBtn = (ToggleButton) view.findViewById(R.id.annotationColorGreen);
        yellowBtn = (ToggleButton) view.findViewById(R.id.annotationColorYellow);
        eraserBtn = (ToggleButton) view.findViewById(R.id.annotationEraser);
        penBtn = (ToggleButton) view.findViewById(R.id.annotationPen);
        pointingBtn = (ToggleButton) view.findViewById(R.id.annotationPointing);
        colorMore = (LinearLayout) view.findViewById(R.id.annotationColorMore);
        ibDrag = (ImageButton) view.findViewById(R.id.ibDrag);
        setViewTreeObserver();
        initAnnotationPaintColor();
    }

    private void setViewTreeObserver() {
        ViewTreeObserver vto = ibDrag.getViewTreeObserver();
        vto.addOnPreDrawListener(new OnPreDrawListener() {

            @Override
            public boolean onPreDraw() {
                int left = ibDrag.getLeft();
                if (left > 0) {
                    if (!isAnnoIn) {
                        //log.info("set drag in");
                        setDragIn();
                    }
                }
                return true;
            }
        });
    }

    private void initAnnotationPaintColor() {
        colorMore.setVisibility(View.GONE);
        colorBtn.setVisibility(view.VISIBLE);
    }

    /**
     * 设置图像背景图片
     *
     * @param id
     * @param pressedRes
     * @param shapeRes
     */
    private void setShapeBackground(int id, int pressedRes, int shapeRes) {
        view.findViewById(id).setBackgroundResource(pressedRes);
        if (annotation.getAnnoPattern().equals(Constants.SHAPE_POLYGON)) {
            annotation.setPolygonPattern(Constants.SHAPE_CLOSE);
        }
    }

    /**
     * 设置颜色背景图片
     *
     * @param id
     * @param pressedRes
     */
    private void setColorBackground(int id, int pressedRes) {
        ToggleButton tb = (ToggleButton) view.findViewById(R.id.annotationColor);
        tb.setVisibility(View.VISIBLE);
        tb.setBackgroundResource(pressedRes);
        colorMore.setVisibility(View.GONE);

        annotation.setCurrentColor(id);

        eraserBtn.setBackgroundResource(R.drawable.icon_eraser);
        eraser.setEraserClean(false);
    }

    /**
     * 显示路径
     *
     * @param path
     */
    public void setPath(String path) {
//		saveDialog.setPath(path);
    }

    @Override
    public void onClick(View v) {
        //log.info("vvvvvvvvvvvvvvvvvvvvvvvvvvvv id = "+v.getId());
        int id = v.getId();
//		boolean isChecked = ((ToggleButton)v).isChecked();
        if (id != R.id.annotationPointing) {
            pointingBtn.setBackgroundResource(R.drawable.icon_pointing);
            if (DocCommonImpl.isStartPointer) {
//				((CheckBox)view.findViewById(R.id.annotaionControl)).setBackgroundResource(R.drawable.button_startinstruction_normal);
//				((CheckBox)view.findViewById(R.id.annotaionControl)).setChecked(false);
                DocCommonImpl.isStartPointer = false;
            }
        }
        if (id == R.id.annotationPointing) {
            annotation.setPaint(false);
            eraser.setEraserClean(false);
            if (DocCommonImpl.isStartPointer) {
                pointingBtn.setBackgroundResource(R.drawable.icon_pointing);
                colorBtn.setVisibility(View.VISIBLE);
                colorMore.setVisibility(View.GONE);
                penBtn.setBackgroundResource(R.drawable.icon_pen);
                eraserBtn.setBackgroundResource(R.drawable.icon_eraser);
                DocCommonImpl.isStartPointer = false;
                if (this.onPointerStateChange != null) {
                    this.onPointerStateChange.doCheck(false);
                }
            } else {
                DocCommonImpl.isStartPointer = true;
                pointingBtn.setBackgroundResource(R.drawable.icon_pointing_on);
                colorBtn.setVisibility(View.VISIBLE);
                colorMore.setVisibility(View.GONE);
                penBtn.setBackgroundResource(R.drawable.icon_pen);
                eraserBtn.setBackgroundResource(R.drawable.icon_eraser);
                if (this.onPointerStateChange != null) {
                    this.onPointerStateChange.doCheck(true);
                }
            }
        } else if (id == R.id.annotationEraser) {
            eraser.setEraserClean(!eraser.isEraserClean());
            annotation.setPaint(false);
            if (eraser.isEraserClean()) {
                eraserBtn.setBackgroundResource(R.drawable.icon_eraser_on);
                colorBtn.setVisibility(View.VISIBLE);
                colorMore.setVisibility(View.GONE);
                penBtn.setBackgroundResource(R.drawable.icon_pen);
            } else {
                penBtn.setBackgroundResource(R.drawable.icon_pen);
                colorBtn.setVisibility(View.VISIBLE);
                colorMore.setVisibility(View.GONE);
                eraserBtn.setBackgroundResource(R.drawable.icon_eraser);
            }
        } else if (id == R.id.annotationColor) {
            colorBtn.setVisibility(View.GONE);
            colorMore.setVisibility(View.VISIBLE);
            eraserBtn.setBackgroundResource(R.drawable.icon_eraser);
            eraser.setEraserClean(false);
        } else if (id == R.id.annotationColorGreen) {
            setColorBackground(id, R.drawable.icon_color_green);
        } else if (id == R.id.annotationColorRed) {
            setColorBackground(id, R.drawable.icon_color_red);
        } else if (id == R.id.annotationColorYellow) {
            setColorBackground(id, R.drawable.icon_color_yellow);
        } else if (id == R.id.annotationPen) {
            annotation.setPaint(!annotation.isPainting());
            eraser.setEraserClean(false);
            //log.info("-=-=-=" + annotation.isPainting());
            if (annotation.isPainting()) {
                eraserBtn.setBackgroundResource(R.drawable.icon_eraser);
                penBtn.setBackgroundResource(R.drawable.icon_pen_on);
                annotation.setPaintType(annotation.getJsonField(annotation.getCurrentPen()));
                annotation.setAnnoPattern(Constants.SHAPE_POLY_LINE);
                annotation.setPolygonPattern(Constants.SHAPE_LINE);
            } else {
                penBtn.setBackgroundResource(R.drawable.icon_pen);
            }
        } else if (id == R.id.ibDrag) {
            //log.info("ibDrag");
            final RelativeLayout rlAnno = (RelativeLayout) view.findViewById(R.id.annotationLayout);
            LinearLayout llAnno = (LinearLayout) view.findViewById(R.id.llAnno);
            if (rlAnno.getVisibility() == View.VISIBLE) {
                if (llAnno.getVisibility() == View.VISIBLE) {
                    view.findViewById(R.id.llAnno).setVisibility(View.GONE);
                    view.findViewById(R.id.ibDrag).setBackgroundResource(R.drawable.anno_pull_pressed);
                } else {
                    view.findViewById(R.id.llAnno).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.ibDrag).setBackgroundResource(R.drawable.anno_pullback_pressed);
                }
            }
//			final float xDelta = (float)view.findViewById(R.id.llAnno).getWidth();
//			Animation animation = null;
//			if(isAnnoIn){
//				//log.info("anno out");
//				animation = new TranslateAnimation(0, -xDelta, 0, 0);
//				view.findViewById(R.id.ibDrag).setBackgroundResource(R.drawable.anno_pull_pressed);
//			}else{
//				//log.info("anno in");
//				animation = new TranslateAnimation(-xDelta, 0, 0, 0);
//
//				view.findViewById(R.id.ibDrag).setBackgroundResource(R.drawable.anno_pullback_pressed);
//
//			}
//			animation.setAnimationListener(new AnimationListener() {
//
//				@Override
//				public void onAnimationStart(Animation animation) {
//					// TODO Auto-generated method stub
//				}
//
//				@Override
//				public void onAnimationRepeat(Animation animation) {
//					// TODO Auto-generated method stub
//				}
//
//				@Override
//				public void onAnimationEnd(Animation animation) {
//					llAnno.clearAnimation();
//					 ibDrag = (ImageButton)view.findViewById(R.id.ibDrag);
//					int trans = view.findViewById(R.id.llAnno).getWidth();
//					int width = view.getContext().getResources().getDimensionPixelSize(R.dimen.width_4_80);
//					int height = view.getContext().getResources().getDimensionPixelSize(R.dimen.height_10_80);
//					if(isAnnoIn){
//						ibDrag.layout(ibDrag.getLeft()-trans,ibDrag.getTop(),ibDrag.getLeft()-trans+width,ibDrag.getTop()+height);
//						view.findViewById(R.id.llAnno).setVisibility(View.INVISIBLE);
//						//log.info("left in = "+ibDrag.getLeft());
//						isAnnoIn = false;
//					}else{
//						llAnno.clearAnimation();
//						ibDrag.layout(ibDrag.getLeft()+trans,ibDrag.getTop(),ibDrag.getLeft()+trans+width,ibDrag.getTop()+height);
//						//log.info("params right of llanno");
//						view.findViewById(R.id.llAnno).setVisibility(View.VISIBLE);
//						ibDrag.setVisibility(View.VISIBLE);
//						//log.info("left out = "+ibDrag.getLeft());
//						isAnnoIn = true;
//					}
//
//				}
//			});
//			animation.setFillAfter(true);
//			animation.setDuration(700);
//			animation.setRepeatCount(0);
//			llAnno.startAnimation(animation);
        }
        if (!eraser.isEraserClean() && !annotation.isPainting()) {
            DocCommonImpl.isAnnotation = false;
        } else {
            DocCommonImpl.isAnnotation = true;
        }
    }

    public void checkAnno() {
        if (!isAnnoIn) {
            setDragIn();
        }
    }

    private void setDragIn() {
        //log.info("setDragIn");
        int trans = view.findViewById(R.id.llAnno).getWidth();
        int width = view.getContext().getResources().getDimensionPixelSize(R.dimen.width_4_80);
        int height = view.getContext().getResources().getDimensionPixelSize(R.dimen.height_10_80);
        ibDrag.layout(ibDrag.getLeft() - trans, ibDrag.getTop(), ibDrag.getLeft() - trans + width, ibDrag.getTop() + height);
    }

    public void setPointerState(OnPointerStateChange onPointerStateChange) {
        this.onPointerStateChange = onPointerStateChange;
    }

    public interface OnPointerStateChange {
        public void doCheck(boolean isChecked);
    }

}
