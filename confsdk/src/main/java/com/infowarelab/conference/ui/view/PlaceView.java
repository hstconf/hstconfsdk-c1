package com.infowarelab.conference.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class PlaceView extends View {
    public PlaceView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public PlaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public PlaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        widthMeasureSpec = widthMeasureSpec > heightMeasureSpec ? heightMeasureSpec : widthMeasureSpec;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

}
