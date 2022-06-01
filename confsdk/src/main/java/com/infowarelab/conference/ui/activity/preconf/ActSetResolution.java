package com.infowarelab.conference.ui.activity.preconf;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.infowarelab.conference.BaseActivity;
import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.util.Constants;
import com.infowarelabsdk.conference.util.FileUtil;

public class ActSetResolution extends BaseActivity {
    private ImageView iv1, iv2, iv3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a6_preconf_set_resolution);
        initView();
    }

    private void initView() {
        iv1 = (ImageView) findViewById(R.id.act_preconf_resolution_iv_r_1);
        iv2 = (ImageView) findViewById(R.id.act_preconf_resolution_iv_r_2);
        iv3 = (ImageView) findViewById(R.id.act_preconf_resolution_iv_r_3);
        String defaultRes = FileUtil.readSharedPreferences(
                this, Constants.SHARED_PREFERENCES, Constants.DEFAULT_RESOLUTION);
        if (defaultRes == null || defaultRes.equals("")) {
            selectR(1);
        } else {
            iv1.setVisibility(View.GONE);
            iv2.setVisibility(View.GONE);
            iv3.setVisibility(View.GONE);
            if (defaultRes.equals("L")) {
                iv1.setVisibility(View.VISIBLE);
            } else if (defaultRes.equals("M")) {
                iv2.setVisibility(View.VISIBLE);
            } else {
                iv3.setVisibility(View.VISIBLE);
            }
        }
    }

    public void ItemClick(View v) {
        int id = v.getId();
        if (id == R.id.act_preconf_resolution_ll_back) {
            finishWithResult(RESULT_CANCELED);
        } else if (id == R.id.act_preconf_resolution_ll_r_1) {
            selectR(1);
            finishWithResult(RESULT_OK);
        } else if (id == R.id.act_preconf_resolution_ll_r_2) {
            selectR(2);
            finishWithResult(RESULT_OK);
        } else if (id == R.id.act_preconf_resolution_ll_r_3) {
            selectR(3);
            finishWithResult(RESULT_OK);
        }
    }

    private void selectR(int which) {
        switch (which) {
            case 1:
                FileUtil.saveSharedPreferences(this, Constants.SHARED_PREFERENCES, Constants.DEFAULT_RESOLUTION, "L");
                iv1.setVisibility(View.VISIBLE);
                iv2.setVisibility(View.GONE);
                iv3.setVisibility(View.GONE);
                break;
            case 2:
                FileUtil.saveSharedPreferences(this, Constants.SHARED_PREFERENCES, Constants.DEFAULT_RESOLUTION, "M");
                iv1.setVisibility(View.GONE);
                iv2.setVisibility(View.VISIBLE);
                iv3.setVisibility(View.GONE);
                break;
            case 3:
                FileUtil.saveSharedPreferences(this, Constants.SHARED_PREFERENCES, Constants.DEFAULT_RESOLUTION, "H");
                iv1.setVisibility(View.GONE);
                iv2.setVisibility(View.GONE);
                iv3.setVisibility(View.VISIBLE);
                break;

            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishWithResult(RESULT_CANCELED);
    }
}
