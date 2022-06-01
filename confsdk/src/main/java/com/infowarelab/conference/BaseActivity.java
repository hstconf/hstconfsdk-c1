package com.infowarelab.conference;

//////import org.apache.log4j.Logger;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.infowarelab.conference.ui.activity.inconf.ConferenceActivity;
import com.infowarelab.conference.ui.activity.preconf.ActHome;
import com.infowarelab.conference.ui.view.LodingDialog;
import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.util.Constants;
import com.umeng.analytics.MobclickAgent;

public class BaseActivity extends AppCompatActivity {

    ////protected Logger log = Logger.getLogger(this.getClass());
    protected LodingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        //requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
//		mActivityManager = ActivityManager.getInstance();
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
    protected void logout() {
        SharedPreferences preferences = getSharedPreferences(Constants.SHARED_PREFERENCES,
                Context.MODE_PRIVATE);
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
        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(v, 0);
    }

    protected void hideInput(View v) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != getCurrentFocus() && null != getCurrentFocus().getApplicationWindowToken()) {
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus()
                            .getApplicationWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void setFocus(View v) {
        ((EditText) v).requestFocus();
        if (((EditText) v).getText() != null) {
            ((EditText) v).setSelection(((EditText) v).getText().toString().length());
        }
    }

    public void showLongToast(int resId) {
        Toast.makeText(getApplicationContext(), resId, Toast.LENGTH_LONG).show();
    }

    public void showShortToast(int resId) {
        Toast.makeText(getApplicationContext(), resId, Toast.LENGTH_SHORT).show();
    }

    public void activityForwardAnimation(Activity activity) {
        activity.overridePendingTransition(R.anim.anim_in_forward_activity, R.anim.anim_out_forward_activity);
    }

    public void activityBackAnimation(Activity activity) {
        activity.overridePendingTransition(R.anim.anim_in_back_activity, R.anim.anim_out_back_activity);
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

    protected void finishWithResult(int code) {
        setResult(code);
        finish();
    }
}
