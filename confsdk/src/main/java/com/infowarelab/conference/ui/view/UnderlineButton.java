package com.infowarelab.conference.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

import com.infowarelab.hongshantongphone.R;

public class UnderlineButton extends Button {


    private boolean selected = false;

    public UnderlineButton(Context context) {
        super(context);

    }

    public UnderlineButton(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public UnderlineButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    public void setSelected(boolean sel){
        selected = sel;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        // 获取焦点改变背景颜色.
//        int height = getHeight();// 获取对应高度
//        int width = getWidth(); // 获取对应宽度
//
//        Paint paint = new Paint();
//
//        CharSequence text = getText();
//        paint.setTypeface(Typeface.DEFAULT_BOLD);
//        paint.setAntiAlias(true);
//        paint.setTextSize(16);
//        paint.setColor(Color.parseColor("#DC0916"));
//        paint.setFakeBoldText(true);
//
//        // x坐标等于中间-字符串宽度的一半.
//        float xPos = width / 2 - paint.measureText(String.valueOf(text)) / 2;
//        float yPos = singleHeight * i + singleHeight;
//        canvas.drawText(text, xPos, yPos, paint);
//
//
        if (selected) {
            Paint paint = new Paint();

            CharSequence text = getText();
            int textWidth = (int) paint.measureText(String.valueOf(text));

            paint.setColor(getResources().getColor(R.color.app_main_hue));
            paint.setStrokeWidth(5);
            paint.setStyle(Paint.Style.FILL);
            int width = getWidth();
            int height = getHeight();
            canvas.drawLine(width / 2 - textWidth * 2, height - 3, width / 2 + textWidth * 2, height - 3, paint);

        }
    }
}