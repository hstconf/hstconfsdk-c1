package com.infowarelab.hongshantongphone.floatView.float_view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.infowarelab.hongshantongphone.R;
import com.infowarelab.hongshantongphone.floatView.uitls.SystemUtils;

/**
 * FloatWindowView:悬浮窗控件V1-利用windowManger控制窗口
 *
 * @author Nonolive-杜乾 Created on 2017/12/12 - 17:16.
 * E-mail:dusan.du@nonolive.com
 */

public class FloatWindowView extends FrameLayout implements IFloatView {
    private static final String TAG = FloatWindowView.class.getSimpleName();
    private float xInView;
    private float yInView;
    private float xInScreen;
    private float yInScreen;
    private float xDownInScreen;
    private float yDownInScreen;
    private Context mContext;
    private TextView tv_info;
    private RelativeLayout videoViewWrap;
    private RelativeLayout content_wrap;
    //    private ImageView iv_zoom_btn;
    private ImageView closeBtn;

    private WindowManager mWindowManager = null;
    private WindowManager.LayoutParams mWindowParams = null;
    private FloatViewParams params = null;
    private FloatViewListener listener;
    private int statusBarHeight = 0;
    private int mMinWidth;//初始宽度
    //视频最大宽度
    //窗口高/宽比
    //sdk版本是否>=23

    public FloatWindowView(Context context) {
        super(context);
        init();
    }

    public FloatWindowView(Context mContext, FloatViewParams floatViewParams, WindowManager.LayoutParams wmParams) {
        super(mContext);
        this.params = floatViewParams;
        this.mWindowParams = wmParams;
        init();
    }

    private void init() {
        try {
            initData();
            initView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View floatView = inflater.inflate(R.layout.float_view_inner_layout, null);
        content_wrap = (RelativeLayout) floatView.findViewById(R.id.content_wrap);
        videoViewWrap = (RelativeLayout) floatView.findViewById(R.id.videoViewWrap);
        tv_info = (TextView) floatView.findViewById(R.id.tv_info);
        tv_info.setText("系统弹窗(需权限)");
        closeBtn = floatView.findViewById(R.id.iv_live_cover);

        closeBtn.setOnTouchListener(onMovingTouchListener);

        floatView.findViewById(R.id.iv_close_window).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != listener) {
                    listener.onClose();//关闭
                }
            }
        });
//        closeBtn.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (null != listener) {
//                    listener.onClose();//关闭
//                }
//            }
//        });

        int w = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        closeBtn.measure(w, h);

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) closeBtn.getLayoutParams();

        final int lastViewWidth = closeBtn.getMeasuredWidth();//params.contentWidth;
        final int lastViewHeight = closeBtn.getMeasuredHeight();//=" + closeBtn.getMeasuredHeight()(int) (lastViewWidth * mRatio);

        updateViewLayoutParams(lastViewWidth, lastViewHeight);
        addView(floatView);

        updateWindowWidthAndHeight(lastViewWidth, lastViewHeight);

        closeBtn.post(new Runnable() {
            @Override
            public void run() {
                updateWindowWidthAndHeight(lastViewWidth, lastViewHeight);
            }
        });
    }

    private void initData() {
        mContext = getContext();
        mWindowManager = SystemUtils.getWindowManager(mContext);
        statusBarHeight = params.statusBarHeight;
        mMinWidth = params.mMinWidth;
        //起点
        //isSdkGt23 = Build.VERSION.SDK_INT >= 23;
        // >=23的部分手机缩放会卡顿，系统弹窗更新位置迟缓不够平滑
    }

    private void updateViewLayoutParams(int width, int height) {
        if (content_wrap != null) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) content_wrap.getLayoutParams();
            layoutParams.height = height;
            layoutParams.width = width;
            content_wrap.setLayoutParams(layoutParams);
        }
    }

    public int getContentViewWidth() {
        return content_wrap != null ? content_wrap.getWidth() : mMinWidth;
    }

    /**
     * 更新WM的宽高大小
     */
    private synchronized void updateWindowWidthAndHeight(int width, int height) {
        if (mWindowManager != null) {
            //Log.d("InfowareLab", ">>>>>> updateWindowWidthAndHeight=" + width + "," + height);
            mWindowParams.width = width;
            mWindowParams.height = height;
            mWindowManager.updateViewLayout(this, mWindowParams);
        }
    }

    private boolean isMoving = false;
    private final OnTouchListener onMovingTouchListener = new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return onTouchEvent2(event);
        }
    };

    private long firstClickTime;//第一次点击
    private int countClick = 0;
    private final Runnable clickRunnable = new Runnable() {
        @Override
        public void run() {
            //Logger.d("dq-fw canClick=" + canClick);
            if (countClick == 1 && canClick) {
                if (listener != null) {
                    listener.onClick();
                }
            }
            countClick = 0;
        }
    };
    private boolean canClick = true;//是否可以点击
    private final Runnable canClickRunnable = new Runnable() {
        @Override
        public void run() {
            canClick = true;
        }
    };

    private Handler handler = new Handler(Looper.getMainLooper());

    //@Override
    public boolean onTouchEvent2(MotionEvent event) {
        if (isDragged) {
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isMoving = false;
                xInView = event.getX();
                yInView = event.getY();
                xDownInScreen = event.getRawX();
                yDownInScreen = event.getRawY();
                xInScreen = xDownInScreen;
                yInScreen = yDownInScreen;

                //Log.d("InfowareLab", ">>>>>> ACTION_DOWN: " + xInScreen + "," + yInScreen);

                break;
            case MotionEvent.ACTION_MOVE:

                //showZoomView();

                // 手指移动的时候更新小悬浮窗的位置
                xInScreen = event.getRawX();
                yInScreen = event.getRawY();

                //Log.d("InfowareLab", ">>>>>> ACTION_MOVE: " + xInScreen + "," + yInScreen);

                if (!isMoving) {
                    isMoving = !isClickedEvent();
                } else {
                    updateViewPosition();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isClickedEvent()) {
                    if (null != listener) {
                        listener.onClose();//关闭
                    }
                } else {
                    if (null != listener) {
                        listener.onMoved();
                    }
                    countClick = 0;
                }
                //updateEditStatus();
                isMoving = false;
                break;
            default:
                break;
        }
        return true;
    }

    private boolean isClickedEvent() {
        int scaledTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
        // 是点击事件
        return Math.abs(xDownInScreen - xInScreen) <= scaledTouchSlop
                && Math.abs(yDownInScreen - yInScreen) <= scaledTouchSlop;
//        return false;
    }

    /**
     * 更新悬浮窗位置
     */
    private void updateViewPosition() {
        int x = (int) (xInScreen - xInView);
        int y = (int) (yInScreen - yInView);
        //防止超出通知栏
        if (y < statusBarHeight) {
            y = statusBarHeight;
        }
        //更新起点
        updateWindowXYPosition(x, y);
    }

    /**
     * 更新窗体坐标位置
     *
     * @param x
     * @param y
     */
    private synchronized void updateWindowXYPosition(int x, int y) {
        if (mWindowManager != null) {
            mWindowParams.x = x;
            mWindowParams.y = y;
            //Log.d("InfowareLab", ">>>>>> updateWindowXYPosition: " + x + "," + y);

            mWindowManager.updateViewLayout(this, mWindowParams);
        }
    }

    private boolean isDragged = false;//是否正在拖拽中

    @Override
    public FloatViewParams getParams() {
        params.contentWidth = getContentViewWidth();
        params.x = mWindowParams.x;
        params.y = mWindowParams.y;
        params.width = mWindowParams.width;
        params.height = mWindowParams.height;
        return params;
    }

    @Override
    public void setFloatViewListener(FloatViewListener listener) {
        this.listener = listener;
    }

}
