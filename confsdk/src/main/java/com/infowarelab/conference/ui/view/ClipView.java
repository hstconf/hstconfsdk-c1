package com.infowarelab.conference.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

public class ClipView extends TextView {
    public ClipView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ClipView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ClipView(Context context) {
        super(context);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
    }

    /**
     * 绘制TextView的背景图
     *
     * @return
     */
    public Bitmap setViewBitmap() {
        int spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        measure(spec, spec);
        layout(0, 0, getMeasuredWidth() + 2, getMeasuredHeight());
        Bitmap b = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(b);
        canvas.translate(-getScrollX(), -getScrollY());
        draw(canvas);
        //创建图片缓存
        setDrawingCacheEnabled(true);
        Bitmap cacheBmp = getDrawingCache();

        Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
        destroyDrawingCache();
        return viewBmp;
    }

}
