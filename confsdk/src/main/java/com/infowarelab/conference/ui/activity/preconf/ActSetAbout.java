package com.infowarelab.conference.ui.activity.preconf;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import android.view.View;
import android.widget.Button;

import com.infowarelab.conference.BaseActivity;
import com.infowarelab.hongshantongphone.R;

public class ActSetAbout extends BaseActivity {
    private Button btnCall;
    private String tel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a6_preconf_set_about);
        initView();
    }

    private void initView() {
        btnCall = (Button) findViewById(R.id.act_preconf_about_btn);
        tel = getResources().getString(R.string.tel);
        if (tel.equals("0") || tel.equals("")) {
            btnCall.setVisibility(View.GONE);
        } else {
            btnCall.setVisibility(View.VISIBLE);
        }
    }

    public void ItemClick(View v) {
        int id = v.getId();
        if (id == R.id.act_preconf_about_ll_back) {
            finish();
        } else if (id == R.id.act_preconf_about_btn) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Intent intent = new Intent(Intent.ACTION_CALL, Uri
                    .parse("tel:" + tel));
            startActivity(intent);
        }
    }
}
