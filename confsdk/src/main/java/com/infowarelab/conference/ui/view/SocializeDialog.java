package com.infowarelab.conference.ui.view;


import com.infowarelab.hongshantongphone.R;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class SocializeDialog extends AlertDialog {
    private LinearLayout ll2, llWechat, llQq, llDing, llSms;
    private OnResultListener onResultListener;
    private ImageView ivWechat;
    private ImageView ivQq;
    private ImageView ivDing;
    private ImageView ivSms;

    public SocializeDialog(Context context) {
        super(context, R.style.style_dialog_bottom);
    }

    public SocializeDialog(Context context, int width) {
        super(context, R.style.style_dialog_bottom);
//    	this.width = width;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conference_socialize_menu);
//        if(width>0){
//        	LinearLayout ll = (LinearLayout) findViewById(R.id.dialog_cache_ll);
//        	LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) ll.getLayoutParams();
//        	params.width = this.width;
//        	ll.setLayoutParams(params);
//        }
        setCanceledOnTouchOutside(true);

        ll2 = (LinearLayout) findViewById(R.id.socialize_menu_ll2);
        llWechat = (LinearLayout) findViewById(R.id.socialize_menu_ll_wechat);
        llQq = (LinearLayout) findViewById(R.id.socialize_menu_ll_qq);
        llDing = (LinearLayout) findViewById(R.id.socialize_menu_ll_ding);
        llSms = (LinearLayout) findViewById(R.id.socialize_menu_ll_sms);
        ivWechat = (ImageView) findViewById(R.id.socialize_menu_iv_wechat);
        ivQq = (ImageView) findViewById(R.id.socialize_menu_iv_qq);
        ivDing = (ImageView) findViewById(R.id.socialize_menu_iv_ding);
        ivSms = (ImageView) findViewById(R.id.socialize_menu_iv_sms);


        llWechat.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                doShare(1);
                cancel();
            }
        });
        llQq.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                doShare(3);
                cancel();
            }
        });
        llDing.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                doShare(5);
                cancel();
            }
        });
        llSms.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//        		// TODO Auto-generated method stub
//				doInvite();
//				cancel();
            }
        });

    }

    public void show(int width) {
        // TODO Auto-generated method stub
        Window dialogWindow = getWindow();     //设置Dialog从窗体底部弹出
        dialogWindow.setGravity(Gravity.BOTTOM);

        super.show();
        setImgWH(ivWechat, width);
        setImgWH(ivQq, width);
        setImgWH(ivDing, width);
        setImgWH(ivSms, width);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = width;
        lp.height = width / 3;
        dialogWindow.setAttributes(lp);

    }

    private void setImgWH(ImageView v, int w) {
        LayoutParams p = (LayoutParams) v.getLayoutParams();
        p.width = w / 7;
        p.height = w / 7;
        v.setLayoutParams(p);
    }

    private void showShares(boolean a) {
        if (a) {
            Animation mShowAction = AnimationUtils.loadAnimation(
                    getContext(), R.anim.anim_in_forward_activity);
            ll2.startAnimation(mShowAction);
            ll2.setVisibility(View.VISIBLE);
//			Animation mShowAction1 = AnimationUtils.loadAnimation(
//					getContext(), R.anim.anim_out_forward_activity);
//			ll1.startAnimation(mShowAction1);
//			ll1.setVisibility(View.GONE);
        } else {
//			Animation mShowAction = AnimationUtils.loadAnimation(
//					getContext(), R.anim.anim_in_back_activity);
//			ll1.startAnimation(mShowAction);
            Animation mShowAction1 = AnimationUtils.loadAnimation(
                    getContext(), R.anim.anim_out_back_activity);
            ll2.startAnimation(mShowAction1);
            ll2.setVisibility(View.GONE);
//			ll1.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 在AlertDialog的 onStart() 生命周期里面执行开始动画
     */
    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * 在AlertDialog的onStop()生命周期里面执行停止动画
     */
    @Override
    protected void onStop() {
        super.onStop();

    }

    public void setClickListener(OnResultListener onResultListener) {
        this.onResultListener = onResultListener;
    }

    public interface OnResultListener {
        public void doInvite();

        public void doShare(int position);
    }

    private void doInvite() {
        if (onResultListener != null) {
            onResultListener.doInvite();
        }
    }

    private void doShare(int position) {
        if (onResultListener != null) {
            onResultListener.doShare(position);
        }
    }
}
