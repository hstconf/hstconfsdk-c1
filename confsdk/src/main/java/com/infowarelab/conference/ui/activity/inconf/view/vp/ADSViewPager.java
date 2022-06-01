package com.infowarelab.conference.ui.activity.inconf.view.vp;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by Always on 2018/12/12.
 */

public class ADSViewPager extends ViewPager {
    public static final int ValidTime = 300;

    public ADSViewPager(Context context) {
        super(context);
    }

    public ADSViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private boolean isAllowScroll = true;

    public void setAllowScroll(boolean allowScroll) {
        this.isAllowScroll = allowScroll;
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        super.requestDisallowInterceptTouchEvent(this.isAllowScroll ? disallowIntercept : true);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //        boolean flag = false;
//        if(ev.getAction()==MotionEvent.ACTION_DOWN){
//            flag = super.dispatchTouchEvent(ev);
//            Log.i("touch","touch ViewPager dispatchTouchEvent ACTION_DOWN "+flag);
//        }else if(ev.getAction()==MotionEvent.ACTION_MOVE){
//            flag = super.dispatchTouchEvent(ev);
//            Log.i("touch","touch ViewPager dispatchTouchEvent ACTION_MOVE "+flag);
//        }else if(ev.getAction()==MotionEvent.ACTION_UP){
//            flag = super.dispatchTouchEvent(ev);
//            Log.i("touch","touch ViewPager dispatchTouchEvent ACTION_UP "+flag);
//        }
//        return flag;
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!this.isAllowScroll) return false;
        boolean flag = false;
        String action = "";
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                action = "ACTION_DOWN";
//                float x = ev.getX();
//                if (isBorder(x)) {
//                    flag = true;
//                } else {
//                    flag = false;
//                }
//                flag = false;
                flag = super.onInterceptTouchEvent(ev);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                action = "ACTION_POINTER_DOWN";
                flag = super.onInterceptTouchEvent(ev);
                break;
            case MotionEvent.ACTION_UP:
                action = "ACTION_UP";
                flag = super.onInterceptTouchEvent(ev);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                action = "ACTION_POINTER_UP";
                flag = super.onInterceptTouchEvent(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                action = "ACTION_MOVE";
//                flag = false;
                flag = super.onInterceptTouchEvent(ev);
                break;
        }
        Log.i("touch", "touch ViewPager onInterceptTouchEvent " + action + " " + flag);
        return flag;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!this.isAllowScroll) return false;
        return super.onTouchEvent(ev);
    }

    private int canExpense = 0;
    private float downX = 0;
    private float downY = 0;

//    @Override
//    public boolean onTouchEvent(MotionEvent ev) {
//        boolean flag = false;
//        String action = "";
//        switch (ev.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                action = "ACTION_DOWN";
//                canExpense = 0;
//                downX = ev.getX();
//                downY = ev.getY();
//
//                flag = isBorder(downX) ? super.onTouchEvent(ev) : false;
//                break;
//
//            case MotionEvent.ACTION_POINTER_DOWN:
//                action = "ACTION_POINTER_DOWN";
//                flag = super.onTouchEvent(ev);
//                break;
//            case MotionEvent.ACTION_UP:
//                action = "ACTION_UP";
//                flag = super.onTouchEvent(ev);
//                break;
//            case MotionEvent.ACTION_POINTER_UP:
//                action = "ACTION_POINTER_UP";
//                flag = super.onTouchEvent(ev);
//                break;
//            case MotionEvent.ACTION_MOVE:
//                action = "ACTION_MOVE";
//                float x = ev.getX();
//                float y = ev.getY();
//                if (canExpense == 0) {
//                    if ((downX - x) > 10) {
//                        canExpense = 1;
//                    } else if ((downY - y) > 10) {
//                        canExpense = 2;
//                    }
//                }
//                flag = canExpense == 2 ? false : super.onTouchEvent(ev);
//                break;
//        }
//
//
//        Log.i("touch", "touch ViewPager onTouchEvent " + action+" "+flag);
//        return flag;
//    }

    private boolean isBorder(float x) {
        return true;
//        return Math.abs(getWidth() - x) < (getWidth() / 7) || Math.abs(0 - x) < (getWidth() / 7);
    }

}
