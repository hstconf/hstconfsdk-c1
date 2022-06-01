package com.infowarelab.conference.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.infowarelab.conference.utils.PublicWay;
import com.infowarelab.hongshantongphone.R;

public class ActCustomDialog extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_custom_activity_layout);

        Intent intent = getIntent();
        setTitle(intent.getStringExtra("title"));

        TextView tvTitle = findViewById(R.id.dialog_custom_titleText);
        if (null !=  tvTitle) tvTitle.setText(intent.getStringExtra("title"));
        TextView tvContent = findViewById(R.id.dialog_custom_contentText);
        if (null !=  tvContent) tvContent.setText(intent.getStringExtra("content"));
    }

    public void onCancelClick(View view) {

        PublicWay.stopRing();
        PublicWay.afterShowDialog(false);

        finish();
    }

    public void onConfirmClick(View view) {

        PublicWay.stopRing();
        PublicWay.afterShowDialog(true);

        finish();
    }
}