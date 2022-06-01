package com.infowarelab.conference.ui.activity.inconf;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.infowarelab.conference.ConferenceApplication;
import com.infowarelab.conference.ui.activity.preconf.ActHome;
import com.infowarelab.conference.ui.view.LodingDialog;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.util.Constants;
import com.umeng.analytics.MobclickAgent;

////import org.apache.log4j.Logger;

public abstract class BaseFragmentActivity extends AppCompatActivity implements BaseFragment.ICallParentView {

    //protected Logger log = Logger.getLogger(this.getClass());
    protected LodingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        if (null != getCurrentFocus().getApplicationWindowToken()) {
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

    protected int getOrientationState() {
        Configuration mConfiguration = this.getResources().getConfiguration();
        return mConfiguration.orientation;
    }

    public void showLongToast(int resId) {
        Toast.makeText(getApplicationContext(), resId, Toast.LENGTH_LONG).show();
    }

    public void showShortToast(int resId) {
        Toast.makeText(getApplicationContext(), resId, Toast.LENGTH_SHORT).show();
    }

    public void activityForwardAnimation(Activity activity) {
        activity.overridePendingTransition(com.infowarelab.hongshantongphone.R.anim.anim_in_forward_activity, com.infowarelab.hongshantongphone.R.anim.anim_out_forward_activity);
    }

    public void activityBackAnimation(Activity activity) {
        activity.overridePendingTransition(com.infowarelab.hongshantongphone.R.anim.anim_in_back_activity, com.infowarelab.hongshantongphone.R.anim.anim_out_back_activity);
    }

    protected void closeOrietation() {
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    protected void openOrietation() {
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
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

    public void doByReq(int request, Object details) {

    }

    protected int getScreenW() {
        return ConferenceApplication.Screen_W;
    }

    protected int getScreenH() {
        return ConferenceApplication.Screen_H;
    }

    protected int getRootW() {
        return ConferenceApplication.Root_W;
    }

    protected int getRootH() {
        return ConferenceApplication.Root_H;
    }

    protected int getStateH() {
        return ConferenceApplication.StateBar_H;
    }

    protected int getKeyH() {
        return ConferenceApplication.NavigationBar_H;
    }

    protected abstract void showBottomBar();

    protected abstract void hideBottomBar();
}
