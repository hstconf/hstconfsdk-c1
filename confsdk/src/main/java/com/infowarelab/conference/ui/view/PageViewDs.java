package com.infowarelab.conference.ui.view;


import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

/**
 * Created by Always on 2017/12/5.
 */

public class PageViewDs extends HorizontalScrollView {
    private int mBaseScrollX;//滑动基线。也就是点击并滑动之前的x值，以此值计算相对滑动距离。
    private int mScreenWidth;
    private int mTargetScrollX;

    private Handler mHandler;

    private ScrollViewListener mScrollViewListener;



    private boolean flag;
    private int mPageCount = 1;//页面数量

    private int mScrollX = 200;//滑动多长距离翻页
    private int mTouchBaseScrollX = 0;

    private int curPage = 1;
    private float touchDownX;
    private boolean mScrolling = false;
    private boolean mDoSkip = false;

    public void forceDoSkip() {
        onSkipHandler.removeCallbacksAndMessages(null);
        onSkipHandler.sendEmptyMessage(0);
        mDoSkip = false;
    }

    /**
     * 滚动状态:
     * IDLE=滚动停止
     * TOUCH_SCROLL=手指拖动滚动
     * FLING=滚动
     */
    enum ScrollType{IDLE,TOUCH_SCROLL,FLING};

    /**
     * 记录当前滚动的距离
     */
    private int currentX = -9999999;

    /**
     * 当前滚动状态
     */
    private ScrollType scrollType = ScrollType.IDLE;

    public interface ScrollViewListener {
        void onScrollChanged(ScrollType scrollType);
    }

    private Runnable scrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (getScrollX()==mTargetScrollX) {
                //滚动停止,取消监听线程
                scrollType = ScrollType.IDLE;

                //Log.d("InfowareLab.Debug", ">>>>>> Scroll state = IDLE!!!");

                if (mScrollViewListener!=null) {
                    mScrollViewListener.onScrollChanged(scrollType);
                }
                mHandler.removeCallbacks(this);

                if (mDoSkip)
                {
                    onSkipHandler.removeCallbacksAndMessages(null);
                    onSkipHandler.sendEmptyMessage(0);
                    mDoSkip = false;
                }

                return;
            } else {
                //手指离开屏幕,但是view还在滚动
                scrollType = ScrollType.FLING;

                smoothScrollTo(mTargetScrollX, 0);

                Log.d("InfowareLab.Debug", ">>>>>> Scroll state = FLING! mTargetScrollX = " + mTargetScrollX);

                if(mScrollViewListener!=null){
                    mScrollViewListener.onScrollChanged(scrollType);
                }
            }
            //currentX = getScrollX();
            //滚动监听间隔:milliseconds
            mHandler.postDelayed(this, 50);
        }
    };

    public PageViewDs(Context context, AttributeSet attrs) {
        super(context, attrs);
        DisplayMetrics dm = context.getApplicationContext().getResources()
                .getDisplayMetrics();
        mScreenWidth = dm.widthPixels;

        mHandler = new Handler();
        mScrollViewListener = new ScrollViewListener() {
            @Override
            public void onScrollChanged(ScrollType scrollType) {
                //TODO
            }
        };
    }

    public void setScreenWidth(int width, int pages) {

        //if (getScrollX() != mTargetScrollX) return;

        this.mScreenWidth = width;
        this.mPageCount = pages;
        curPage = curPage <= pages ? curPage : pages;
        mBaseScrollX = (curPage - 1) * width;
        mTargetScrollX = -1;
    }

    public void setCurPage(int cur) {
        move2Handler.sendEmptyMessage(cur);
    }

    public void Scroll2Cur() {
        smoothScrollTo(mBaseScrollX, 0);
    }

    /**
     * 获取页面数量
     *
     * @return
     */
    public int getPageCount() {
        return mPageCount;
    }

    /**
     * 获取相对滑动位置。由右向左滑动，返回正值；由左向右滑动，返回负值。
     *
     * @return
     */
    private int getBaseScrollX() {
        return mBaseScrollX;
    }

    /**
     * 使相对于基线移动x距离。
     *
     * @param x x为正值时右移；为负值时左移。
     */
    private void baseSmoothScrollTo(int x) {

        Log.d("InfowareLab.Debug", "baseSmoothScrollTo = " + x);

        smoothScrollTo(x, 0);

        mTargetScrollX = x;
        curPage = (x) / mScreenWidth + 1;

        mHandler.post(scrollRunnable);

//        if (x != 0) {
//            onSkipHandler.removeCallbacksAndMessages(null);
//            onSkipHandler.sendEmptyMessageDelayed(0, 300);
//        }
//        if (null == timerThread || timerThread.getState() == Thread.State.TERMINATED) {
//            timerThread = new Thread(timerRun);
//            timeNum = 1;
//            timeP = 1;
//            timerThread.start();
//        } else {
//            timeNum = 1;
//            timeP = 1;
//        }
    }
//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent event) {
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                touchDownX = event.getX();
//                mScrolling = false;
//                break;
//            case MotionEvent.ACTION_MOVE:
//                if (Math.abs(touchDownX - event.getX()) >= ViewConfiguration.get(getContext()).getScaledTouchSlop()) {
//                    mScrolling = true;
//                } else {
//                    mScrolling = false;
//                }
//                break;
//            case MotionEvent.ACTION_UP:
//                mScrolling = false;
//                break;
//        }
//        return mScrolling;
//    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mPageCount < 2) return false;
        int action = ev.getAction();
        switch (action) {
//            case MotionEvent.ACTION_DOWN:
//                timeNum = 0;
//                timeP = -1;
//                onDot(mPageCount, curPage);
//
//                this.scrollType = ScrollType.TOUCH_SCROLL;
//                mScrollViewListener.onScrollChanged(scrollType);
//                mHandler.removeCallbacks(scrollRunnable);
//
//                mTouchBaseScrollX = getScrollX();
//
//                break;
            case MotionEvent.ACTION_UP:
                int scrollX = getScrollX() - mBaseScrollX;
                if (scrollX == 0) {
                    break;
                }
                //左滑，大于一半，移到下一页
                else if (scrollX > mScrollX) {
                    //Log.d("InfowareLab.Debug", "baseSmoothScrollTo = " + mScreenWidth);
                    mBaseScrollX += mScreenWidth;
                    baseSmoothScrollTo(mBaseScrollX);
                    mDoSkip = true;
                    //mBaseScrollX += mScreenWidth;
                }
                //左滑，不到一半，返回原位
                else if (scrollX > 0) {
                    //Log.d("InfowareLab.Debug", "baseSmoothScrollTo = 0");
                    baseSmoothScrollTo(mBaseScrollX);
                    mDoSkip = false;

                }
                //右滑，不到一半，返回原位
                else if (scrollX > -mScrollX) {
                    //Log.d("InfowareLab.Debug", "baseSmoothScrollTo = 0");
                    baseSmoothScrollTo(mBaseScrollX);
                    mDoSkip = false;
                }
                //右滑，大于一半，移到下一页
                else {

                    mBaseScrollX -= mScreenWidth;
                    baseSmoothScrollTo(mBaseScrollX);
                    //mBaseScrollX -= mScreenWidth;
                    mDoSkip = true;
                }

                break;
                //return true;
        }
        return super.onTouchEvent(ev);
    }

    private OnSkipHSListener onSkipHSListener;

    public interface OnSkipHSListener {
        public void onDot(int pages, int cur);

        public void doSkip(int pages, int cur, int singleWidth);
    }

    public void setOnSkipListener(OnSkipHSListener onSkipHSListener) {
        this.onSkipHSListener = onSkipHSListener;
    }

    private void doSkip(int pages, int cur) {
        Log.i("InfowareLab.Debug", ">>>>>>doSkip curPage=" + curPage + "; pages=" + pages);
        if (this.onSkipHSListener != null) {
            this.onSkipHSListener.onDot(pages, cur);
            this.onSkipHSListener.doSkip(pages, cur, mScreenWidth);
        }
    }

    private void onDot(int pages, int cur) {
        Log.d("InfowareLab.Debug", "Always doDown curPage=" + curPage);
        if (this.onSkipHSListener != null)
            this.onSkipHSListener.onDot(pages, cur);
    }

    private int timeNum = 1;
    private int timeP = 1;
    private Thread timerThread;

    Runnable timerRun = new Runnable() {
        @Override
        public void run() {
            while (timeNum > 0) {
                if (timeNum >= 6) {
                    onDotHandler.sendEmptyMessage(2);
                    break;
                }
                timeNum += timeP;
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    Handler onDotHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            onDot(0, 0);
        }
    };
    Handler onSkipHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            doSkip(mPageCount, curPage);
        }
    };
    Handler moveHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            scrollTo(mBaseScrollX, 0);
        }
    };
    Handler move2Handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            curPage = msg.what <= mPageCount ? msg.what : curPage;
            mBaseScrollX = (curPage - 1) * mScreenWidth;
            mTargetScrollX = mBaseScrollX;
            scrollTo(mBaseScrollX, 0);

            mHandler.post(scrollRunnable);

            Log.d("InfowareLab.Debug", ">>>>>> curPage=" + curPage + ";pageNum=" + mPageCount + ";scrollTo:" + mBaseScrollX);
            //invalidate();

//            if (getScrollX() != mTargetScrollX) {
//                sendEmptyMessageDelayed(0, 50);
//                Log.d("InfowareLab.Debug", "(RETRY) getScrollX()=" + getScrollX() + ";mTargetScrollX=" + mTargetScrollX);
//            }
        }
    };
}
