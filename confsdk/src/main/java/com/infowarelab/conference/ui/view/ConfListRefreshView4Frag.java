package com.infowarelab.conference.ui.view;

import java.util.Date;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.infowarelab.hongshantongphone.R;
import com.infowarelab.conference.ui.adapter.ConferenceListAdapter4Frag;

public class ConfListRefreshView4Frag extends ListView implements OnScrollListener {
    private final static String TAG = "ConfListListView";
    private Context context;

    private int state = DONE;

//	private boolean isListViewOver = false;

    // 实际的padding的距离与界面上偏移距离的比例
    //可以任意取一个值，经测试3比较合适
    private final static int RATIO = 3;

    /**
     * 往下拖动列表至“松开刷新”完全显示出来
     */
    private final static int RELEASE_To_REFRESH = 0;

    /**
     * 拖动列表未到“松开刷新”位置
     */
    private final static int PULL_To_REFRESH = 1;

    /**
     * 刷新
     */
    public final static int REFRESHING = 2;

    /**
     * 刷新完成
     */
    private final static int DONE = 3;

    /**
     * headview布局填充器
     */
    private LayoutInflater inflater;

    /**
     * liestview顶部布局
     */
    private LinearLayout headView;

    /**
     * 顶部“下拉刷新”或“松开刷新”
     */
    private TextView tipsTextview;

    /**
     * 最近刷新时间显示
     */
    private TextView lastUpdatedTextView;

    /**
     * 箭头
     */
    private ImageView arrowImageView;

    /**
     * 刷新时的进度条
     */
//    private ProgressBar progressBar;
    private ImageView ivLoading;
    private Animation loadingAnimation;

    private RotateAnimation animation;
    private RotateAnimation reverseAnimation;

    // 用于保证startY的值在一个完整的touch事件中只被记录一次
    private boolean isRecored;

    private int headContentWidth;
    private int headContentHeight;

    private int startY;
    private int firstItemIndex;

    private boolean isBack;
    //确保listview在获取数据之后才能刷新
    private boolean isRefreshable;

    private View mTitle;
    private boolean visible;
    private int width;
    private int height;
    private ConferenceListAdapter4Frag mAdapter;

    public ConfListRefreshView4Frag(Context context) {
        super(context);
        this.context = context;
        init();
    }


    public ConfListRefreshView4Frag(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public ConfListRefreshView4Frag(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init();
    }

    private void init() {
        initHead(context);
        initAnimation();

        setCacheColorHint(context.getResources().getColor(R.color.white));
        setOnScrollListener(this);
        state = DONE;
        isRefreshable = false;
    }

    private void initAnimation() {
        animation = new RotateAnimation(0, 180,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(250);
        animation.setFillAfter(true);

        reverseAnimation = new RotateAnimation(180, 0,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        reverseAnimation.setInterpolator(new LinearInterpolator());
        reverseAnimation.setDuration(200);
        reverseAnimation.setFillAfter(true);
    }

    private void initHead(Context context) {
        inflater = LayoutInflater.from(context);
        headView = (LinearLayout) inflater.inflate(R.layout.conflist_head, null);

        arrowImageView = (ImageView) headView
                .findViewById(R.id.head_arrowImageView);
        arrowImageView.setMinimumWidth(70);
        arrowImageView.setMinimumHeight(50);
//        progressBar = (ProgressBar) headView
//                .findViewById(R.id.head_progressBar);
        ivLoading = (ImageView) headView.findViewById(R.id.head_loadingiv);
        initLoadingImg();

        tipsTextview = (TextView) headView.findViewById(R.id.head_tipsTextView);
        lastUpdatedTextView = (TextView) headView
                .findViewById(R.id.head_lastUpdatedTextView);

        measureView(headView);
        headContentHeight = headView.getMeasuredHeight();
        headContentWidth = headView.getMeasuredWidth();

        headView.setPadding(0, -1 * headContentHeight, 0, 0);
        headView.invalidate();

        Log.i("size", "width:" + headContentWidth + " height:"
                + headContentHeight);

        addHeaderView(headView, null, false);

    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (visible) {
            drawChild(canvas, mTitle, getDrawingTime());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mTitle != null) {
            measureChild(mTitle, widthMeasureSpec, heightMeasureSpec);
            width = mTitle.getMeasuredWidth();
            height = mTitle.getMeasuredHeight();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mTitle != null) {
            mTitle.layout(0, 0, width, height);
            titleLayout(getFirstVisiblePosition());
        }
    }

    public void setTitle(View view) {
        mTitle = view;
        if (mTitle != null) {
            setFadingEdgeLength(0);
        }
        requestLayout();
    }

    public void titleLayout(int firstVisiblePosition) {
        if (mTitle == null) {
            return;
        }
        if (mAdapter == null || !(mAdapter instanceof ConferenceListAdapter4Frag)) {
            return;
        }
        int state = 0;

        state = mAdapter.getTitleState(firstVisiblePosition);
        switch (state) {
            case 0:
                visible = false;
                break;
            case 1:
                if (mTitle.getTop() != 0) {
                    mTitle.layout(0, 0, width, height);
                }
                mAdapter.setTitleText(mTitle, firstVisiblePosition);
                visible = true;
                break;
            case 2:
                View firstView = getChildAt(0);
                if (firstView != null) {
                    int bottom = firstView.getBottom();
                    int headerHeight = mTitle.getHeight();
                    int top;
                    if (bottom < headerHeight) {
                        top = (bottom - headerHeight);
                    } else {
                        top = 0;
                    }
                    mAdapter.setTitleText(mTitle, firstVisiblePosition);
                    if (mTitle.getTop() != top) {
                        mTitle.layout(0, top, width, height + top);
                    }
                    visible = true;
                }
                break;
        }
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        lastUpdatedTextView.setText(getResources().getString(R.string.confupdate) +
                new Date().toLocaleString());
        if (adapter instanceof ConferenceListAdapter4Frag) {
            mAdapter = (ConferenceListAdapter4Frag) adapter;
            super.setAdapter(adapter);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (isRefreshable) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (firstItemIndex == 0 && !isRecored) {
                        isRecored = true;
                        startY = (int) event.getY();
                        Log.v(TAG, "在down时候记录当前位置");
                    }
                    //正在刷新数据时什么都不做
                    if (state == REFRESHING) {
                        return true;
                    }
                    break;

                case MotionEvent.ACTION_UP:

                    if (state != REFRESHING /*&& state != LOADING*/) {
                        if (state == DONE) {
                            // 什么都不做
                        }
                        if (state == PULL_To_REFRESH) {
                            state = DONE;
                            changeHeaderViewByState(state);

                            Log.v(TAG, "由下拉刷新状态，到done状态");
                        }
                        if (state == RELEASE_To_REFRESH) {
                            state = REFRESHING;
                            changeHeaderViewByState(state);
                            onRefresh();

                            Log.v(TAG, "由松开刷新状态，到done状态");
                        }
                    }

                    isRecored = false;
                    isBack = false;

                    break;

                case MotionEvent.ACTION_MOVE:

                    //正在刷新时不让其下拉
                    if (state == REFRESHING) {
                        return true;
                    }

                    int tempY = (int) event.getY();

//                  //判断当listview是否拉到最底部
//                    if(isListViewOver){
//
//                    	Log.e(TAG, "1111111111111");
//                    	return true;
//                    }

                    //当ACTION_DOWN时，listview不在最上面时会进入到这个逻辑中
                    if (!isRecored && firstItemIndex == 0) {
                        Log.v(TAG, "在move时候记录下位置");
                        isRecored = true;
                        startY = tempY;
                    }

                    if (state != REFRESHING && isRecored /*&& state != LOADING*/) {

                        // 保证在设置padding的过程中，当前的位置一直是在head，否则如果当列表超出屏幕的话，当在上推的时候，列表会同时进行滚动

                        // 可以松手去刷新了
                        if (state == RELEASE_To_REFRESH) {

                            setSelection(0);

                            // 往上推了，推到了屏幕足够掩盖head的程度，但是还没有推到全部掩盖的地步
                            if (((tempY - startY) / RATIO < headContentHeight)
                                    && (tempY - startY) > 0) {
                                state = PULL_To_REFRESH;
                                changeHeaderViewByState(state);

                                Log.v(TAG, "由松开刷新状态转变到下拉刷新状态");
                            }
                            // 一下子推到顶了
                            else if (tempY - startY <= 0) {
                                state = DONE;
                                changeHeaderViewByState(state);

                                Log.v(TAG, "由松开刷新状态转变到done状态");
                            }
                            // 更新headView的position
                            headView.setPadding(0, (tempY - startY) / RATIO
                                    - headContentHeight, 0, 0);

                            return true;
                        }
                        // 还没有到达显示松开刷新的时候,DONE或者是PULL_To_REFRESH状态
                        if (state == PULL_To_REFRESH) {

//                            setSelection(0);

                            // 下拉到可以进入RELEASE_TO_REFRESH的状态
                            if ((tempY - startY) / RATIO >= headContentHeight) {
                                state = RELEASE_To_REFRESH;
                                isBack = true;
                                changeHeaderViewByState(state);

                                Log.v(TAG, "由done或者下拉刷新状态转变到松开刷新");
                            }
                            // 上推到顶了
                            else if (tempY - startY <= 0) {
                                state = DONE;
                                changeHeaderViewByState(state);

                                Log.v(TAG, "由DOne或者下拉刷新状态转变到done状态");
                            }
                            // 更新headView的position
                            headView.setPadding(0, -1 * headContentHeight
                                    + (tempY - startY) / RATIO, 0, 0);

                            //把下拉事件消化掉，不然return super.onTouchEvent会有bug
                            return true;
                        }

                        // done状态下
                        if (state == DONE) {
                            if (tempY - startY > 0) {
                                state = PULL_To_REFRESH;
                                changeHeaderViewByState(state);
                            }
                        }
                    }


                    break;
            }
        }

        return super.onTouchEvent(event);
    }


    // 当状态改变时候，调用该方法，以更新界面
    public void changeHeaderViewByState(int state) {
        switch (state) {
            case RELEASE_To_REFRESH:
                arrowImageView.setVisibility(View.VISIBLE);
//                progressBar.setVisibility(View.GONE);
                hideLoadingImg();
                tipsTextview.setVisibility(View.VISIBLE);
                lastUpdatedTextView.setVisibility(View.VISIBLE);

                arrowImageView.clearAnimation();
                arrowImageView.startAnimation(animation);

                tipsTextview.setText(getResources().getString(R.string.skrefresh));

                Log.v(TAG, "当前状态，松开刷新");
                break;
            case PULL_To_REFRESH:
//                progressBar.setVisibility(View.GONE);
                hideLoadingImg();
                tipsTextview.setVisibility(View.VISIBLE);
                lastUpdatedTextView.setVisibility(View.VISIBLE);

                arrowImageView.setVisibility(View.VISIBLE);
                // 是由RELEASE_To_REFRESH状态转变来的
                if (isBack) {
                    isBack = false;
                    arrowImageView.clearAnimation();
                    arrowImageView.startAnimation(reverseAnimation);

                    tipsTextview.setText(getResources().getString(R.string.xlrefresh));
                } else {
                    arrowImageView.clearAnimation();
                    tipsTextview.setText(getResources().getString(R.string.xlrefresh));
                }
                Log.v(TAG, "当前状态，下拉刷新");
                break;

            case REFRESHING:

                headView.setPadding(0, 0, 0, 0);

//                progressBar.setVisibility(View.VISIBLE);
                showLoadingImg();
                arrowImageView.clearAnimation();
                arrowImageView.setVisibility(View.GONE);
                tipsTextview.setText(getResources().getString(R.string.berefresh));
                lastUpdatedTextView.setVisibility(View.VISIBLE);

                Log.v(TAG, "当前状态,正在刷新...");
                break;
            case DONE:
                headView.setPadding(0, -1 * headContentHeight, 0, 0);

//                progressBar.setVisibility(View.GONE);
                hideLoadingImg();
                arrowImageView.clearAnimation();
                arrowImageView.setImageResource(R.drawable.pointer);
                tipsTextview.setText(getResources().getString(R.string.xlrefresh));
                lastUpdatedTextView.setVisibility(View.VISIBLE);

                Log.v(TAG, "当前状态，done");
                break;
        }
    }

    // 此方法直接照搬自网络上的一个下拉刷新的demo，此处是“估计”headView的width以及height
    //把view隐藏起来，下拉的时候再显示
    private void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
                    MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0,
                    MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    /**
     * 对外刷新的接口
     */
    private OnRefreshListener onRefreshListener;

    public interface OnRefreshListener {
        public void onRefresh();
    }

    public void setOnRefreshListener(OnRefreshListener l) {
        this.onRefreshListener = l;
        isRefreshable = true;
    }

    private void onRefresh() {
        if (onRefreshListener != null) {
            onRefreshListener.onRefresh();
        }
    }

    /**
     * 刷新完成后调用
     */
    public void onRefreshComplete() {
        state = DONE;
        lastUpdatedTextView.setText(getResources().getString(R.string.confupdate) +
                new Date().toLocaleString());
        changeHeaderViewByState(state);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        firstItemIndex = firstVisibleItem;
        titleLayout(firstVisibleItem);

//		if(firstVisibleItem + visibleItemCount == totalItemCount){
//			isListViewOver = true;
//		}

//		Log.e(TAG, isListViewOver + "");
//		Log.e(TAG, firstVisibleItem + "，" + visibleItemCount + "," + totalItemCount);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // TODO Auto-generated method stub

    }

    public OnRefreshListener getOnRefreshListener() {
        return onRefreshListener;
    }

    private void initLoadingImg() {
        loadingAnimation = AnimationUtils.loadAnimation(context, R.anim.a6_anim_loading);
        LinearInterpolator lir = new LinearInterpolator();
        loadingAnimation.setInterpolator(lir);
        //动画完成后，是否保留动画最后的状态，设为true
        loadingAnimation.setFillAfter(true);
    }

    private void showLoadingImg() {
        if (animation != null && ivLoading != null) {
            ivLoading.setVisibility(View.VISIBLE);
            ivLoading.startAnimation(animation);
        }
    }

    private void hideLoadingImg() {
        if (ivLoading != null) {
            ivLoading.clearAnimation();
            ivLoading.setVisibility(View.GONE);
        }
    }

}
