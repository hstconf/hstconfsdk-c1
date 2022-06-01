package com.infowarelab.conference.ui.view;


import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.infowarelab.hongshantongphone.R;

public class AddDocDialog extends AlertDialog {
    private int width = 0;
    private Button shareBtn;
    private OnResultListener onResultListener;
    //是否有桌面分享权限
    private boolean bAS = false;

    public void setbAS(boolean bAS) {
        this.bAS = bAS;
    }

    public AddDocDialog(Context context) {
        super(context, R.style.style_dialog_bottom);
    }

    public AddDocDialog(Context context, int width) {
        super(context, R.style.style_dialog_bottom);
        this.width = width;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conference_share_menu);
//        if(width>0){
//        	LinearLayout ll = (LinearLayout) findViewById(R.id.dialog_cache_ll);
//        	LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) ll.getLayoutParams();
//        	params.width = this.width;
//        	ll.setLayoutParams(params);
//        }
        setCanceledOnTouchOutside(true);

        Button btnCamera = (Button) findViewById(R.id.share_menu_camera);
        btnCamera.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                doClick(1);
                cancel();
            }
        });
        Button btnPhoto = (Button) findViewById(R.id.share_menu_photo);
        btnPhoto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                doClick(2);
                cancel();
            }
        });
        Button btnBoard = (Button) findViewById(R.id.share_menu_board);
        btnBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                doClick(3);
                cancel();
            }
        });
        shareBtn = (Button) findViewById(R.id.share_menu_shareBtn);
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Test", "66666666");
                doClick(4);
            }
        });

        Button btnCancel = (Button) findViewById(R.id.share_menu_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                cancel();
            }
        });

    }

    public void show(int width) {
        // TODO Auto-generated method stub
        Window dialogWindow = getWindow();     //设置Dialog从窗体底部弹出
        dialogWindow.setGravity(Gravity.BOTTOM);

        super.show();
        int h = getContext().getResources().getDimensionPixelOffset(R.dimen.height_20_80) + getContext().getResources().getDimensionPixelOffset(R.dimen.height_3_80) * 3 + getContext().getResources().getDimensionPixelOffset(R.dimen.height_101_80) * 2;
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = width;
        lp.height = h + 200;
        dialogWindow.setAttributes(lp);

    }


    /**
     * 在AlertDialog的 onStart() 生命周期里面执行开始动画
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (bAS) {
            shareBtn.setVisibility(View.VISIBLE);
        } else {
            shareBtn.setVisibility(View.VISIBLE);
        }
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
        public void doClick(int position);
    }

    private void doClick(int position) {
        if (onResultListener != null) {
            onResultListener.doClick(position);
        }
    }
}
