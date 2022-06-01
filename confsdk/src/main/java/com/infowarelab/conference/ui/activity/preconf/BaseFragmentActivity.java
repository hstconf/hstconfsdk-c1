package com.infowarelab.conference.ui.activity.preconf;

////import org.apache.log4j.Logger;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.infowarelab.conference.ui.activity.inconf.ConferenceActivity;
import com.infowarelab.conference.ui.view.LodingDialog;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.util.Constants;
import com.umeng.analytics.MobclickAgent;

public class BaseFragmentActivity extends FragmentActivity {

    //protected Logger log = Logger.getLogger(this.getClass());
    // protected ActivityManager mActivityManager;
    protected LodingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // mActivityManager = ActivityManager.getInstance();
        loadingDialog = new LodingDialog(this);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        MobclickAgent.onResume(this);
    }

    /**
     * 注销
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void logout() {
        SharedPreferences preferences = getSharedPreferences(
                Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        preferences.edit().putString(Constants.LOGIN_NAME, "").commit();
        preferences.edit().putString(Constants.LOGIN_NICKNAME, "").commit();
        preferences.edit().putString(Constants.LOGIN_ROLE, "").commit();
        preferences.edit().putInt(Constants.USER_ID, 0).commit();
        preferences.edit().putString(Constants.LOGIN_ROLE, "").commit();
        ConferenceActivity.isLogin = false;
        CommonFactory.getInstance().getConferenceCommon().logout();
    }

    /**
     * 弹出输入法
     *
     * @param v
     */
    public void showInput(View v) {
        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                .showSoftInput(v, 0);
    }

    public void setFocus(View v) {
        ((EditText) v).requestFocus();
        if (((EditText) v).getText() != null) {
            ((EditText) v).setSelection(((EditText) v).getText().toString()
                    .length());
        }
    }

    public void showLongToast(int resId) {
        Toast.makeText(getApplicationContext(), resId, Toast.LENGTH_LONG)
                .show();
    }

    public void showShortToast(int resId) {
        Toast.makeText(getApplicationContext(), resId, Toast.LENGTH_SHORT)
                .show();
    }

    public void showLoading() {
        if (loadingDialog != null && !loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    public void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}
