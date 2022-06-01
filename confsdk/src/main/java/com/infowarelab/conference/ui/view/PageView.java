package com.infowarelab.conference.ui.view;


import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

/**
 * Created by Always on 2017/12/5.
 */

public class PageView extends HorizontalScrollView {
    private int mBaseScrollX;//滑动基线。也就是点击并滑动之前的x值，以此值计算相对滑动距离。
    private int mScreenWidth;

    private boolean flag;
    private int mPageCount = 1;//页面数量

    private int mScrollX = 200;//滑动多长距离翻页

    private int curPage = 1;
    private int cachePage = 1;
    private float touchDownX;
    private boolean mScrolling = false;

    public PageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        DisplayMetrics dm = context.getApplicationContext().getResources()
                .getDisplayMetrics();
        mScreenWidth = dm.widthPixels;
    }

    public void setScreenWidth(int width, int pages) {
        this.mScreenWidth = width;
        this.mPageCount = pages;
//        curPage = curPage<=pages?curPage:1;
//        mBaseScrollX = (curPage-1)*width;

        if (cachePage > 1 && cachePage <= pages) {
            curPage = cachePage;
            cachePage = 1;
            mBaseScrollX = (curPage - 1) * mScreenWidth;
            moveHandler.sendEmptyMessage(0);
        } else {
            curPage = curPage <= pages ? curPage : pages;
            mBaseScrollX = (curPage - 1) * width;
        }
    }

    public void setCurPage(int cur) {
        move2Handler.sendEmptyMessage(cur < 0 ? curPage : cur);
    }


    public int setPagesWithCache(int width, int pages) {
        this.mScreenWidth = width;
        this.mPageCount = pages;
        cachePage = curPage;
//        curPage = curPage<=pages?curPage:pages;
        mBaseScrollX = 0;
        onDotHandler.sendEmptyMessage(2);
        return 0;
    }

    public void Scroll2Cur() {
        smoothScrollTo(mBaseScrollX, 0);
    }


    public void setPages(int pages, int width) {
        mPageCount = pages;
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
        return getScrollX() - mBaseScrollX;
    }

    /**
     * 使相对于基线移动x距离。
     *
     * @param x x为正值时右移；为负值时左移。
     */
    private void baseSmoothScrollTo(int x) {
        smoothScrollTo(x + mBaseScrollX, 0);
        curPage = (x + mBaseScrollX) / mScreenWidth + 1;
        if (x != 0) doSkip(mPageCount, curPage);
        if (null == timerThread || timerThread.getState() == Thread.State.TERMINATED) {
            timerThread = new Thread(timerRun);
            timeNum = 1;
            timeP = 1;
            timerThread.start();
        } else {
            timeNum = 1;
            timeP = 1;
        }
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
            case MotionEvent.ACTION_MOVE:

                break;
            case MotionEvent.ACTION_DOWN:
                timeNum = 0;
                timeP = -1;
                onDot(mPageCount, curPage);
                break;
            case MotionEvent.ACTION_UP:
                int scrollX = getBaseScrollX();
                if (scrollX == 0) {

                }
                //左滑，大于一半，移到下一页
                else if (scrollX > mScrollX) {
                    baseSmoothScrollTo(mScreenWidth);
                    mBaseScrollX += mScreenWidth;
                }
                //左滑，不到一半，返回原位
                else if (scrollX > 0) {
                    baseSmoothScrollTo(0);
                }
                //右滑，不到一半，返回原位
                else if (scrollX > -mScrollX) {
                    baseSmoothScrollTo(0);
                }
                //右滑，大于一半，移到下一页
                else {
                    baseSmoothScrollTo(-mScreenWidth);
                    mBaseScrollX -= mScreenWidth;
                }
                return true;
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
        Log.i("Always", "Always doSkip curPage=" + curPage);
        if (this.onSkipHSListener != null) {
            this.onSkipHSListener.onDot(pages, cur);
            this.onSkipHSListener.doSkip(pages, cur, mScreenWidth);
        }
    }

    private void onDot(int pages, int cur) {
        Log.i("Always", "Always doDown curPage=" + curPage);
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
            onDot(1, 1);
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
            scrollTo(mBaseScrollX, 0);
            doSkip(mPageCount, curPage);
        }
    };
}
