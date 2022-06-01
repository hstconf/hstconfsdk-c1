package com.infowarelab.conference.ui.view;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.infowarelab.hongshantongphone.R;

/**
 * Created by xiaor on 2019/12/24.
 */

public class CustomDialog extends AlertDialog implements View.OnClickListener{

    private OnResultListener onResultListener;
    private int width;
    private TextView titleText;
    private TextView contentText;
    private Button cancelBtn,confirmBtn;

    private String title,content;

    public void setOnResultListener(OnResultListener onResultListener) {
        this.onResultListener = onResultListener;
    }

    public void setTitle(String title) {
        this.title = title;
        if (titleText != null){
            titleText.setText(title);
        }
    }

    public void setContent(String content) {
        this.content = content;
        if (contentText != null){
            contentText.setText(content);
        }
    }

    public CustomDialog(Context context) {
        super(context, R.style.style_dialog_normal);
    }
    public CustomDialog(Context context, int width) {
        super(context, R.style.style_dialog_normal);
        this.width = width;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_custom_layout);
        setCanceledOnTouchOutside(true);

        titleText = (TextView)findViewById(R.id.dialog_custom_titleText);
        contentText = (TextView)findViewById(R.id.dialog_custom_contentText);
        cancelBtn = (Button)findViewById(R.id.dialog_custom_cancelBtn);
        confirmBtn = (Button)findViewById(R.id.dialog_custom_confirmBtn);

        titleText.setText(title);
        contentText.setText(content);

        cancelBtn.setOnClickListener(this);
        confirmBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.dialog_custom_cancelBtn) {
            doNo();
            cancel();
        } else if (id == R.id.dialog_custom_confirmBtn) {
            doYes();
            cancel();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        confirmBtn.setPressed(true);
        confirmBtn.setFocusable(true);
        confirmBtn.requestFocus();
    }

    public interface OnResultListener {
        public void doYes();

        public void doNo();
    }

    private void doYes() {
        if (onResultListener != null) {
            onResultListener.doYes();
        }
    }

    private void doNo() {
        if (onResultListener != null) {
            onResultListener.doNo();
        }
    }
}
