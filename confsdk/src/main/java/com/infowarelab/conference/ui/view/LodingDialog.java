package com.infowarelab.conference.ui.view;


import com.infowarelab.hongshantongphone.R;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

/**
 * 自定义过场动画，主要用户数据加载时，显示等待progress
 * Created by 程果 on 2016/3/16.
 */
public class LodingDialog extends AlertDialog {

    private ImageView progressImg;
    //旋转动画
    private Animation animation;

    public LodingDialog(Context context) {
        super(context, R.style.style_dialog_loading);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a6_dialog_loading);

        //点击imageview外侧区域，动画不会消失
        setCanceledOnTouchOutside(false);

        progressImg = (ImageView) findViewById(R.id.view_dialog_loading_iv);
        //加载动画资源
        animation = AnimationUtils.loadAnimation(getContext(), R.anim.a6_anim_loading);
        LinearInterpolator lir = new LinearInterpolator();
        animation.setInterpolator(lir);
        //动画完成后，是否保留动画最后的状态，设为true
        animation.setFillAfter(true);
    }

    /**
     * 在AlertDialog的 onStart() 生命周期里面执行开始动画
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (animation != null) {
            progressImg.startAnimation(animation);
        }
    }

    /**
     * 在AlertDialog的onStop()生命周期里面执行停止动画
     */
    @Override
    protected void onStop() {
        super.onStop();

        progressImg.clearAnimation();
    }
}
