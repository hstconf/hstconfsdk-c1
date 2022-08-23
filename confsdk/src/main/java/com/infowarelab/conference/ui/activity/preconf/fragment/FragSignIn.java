package com.infowarelab.conference.ui.activity.preconf.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.infowarelab.conference.face.RegisterAndRecognizeActivity;
import com.infowarelab.conference.ui.activity.preconf.BaseFragment;
import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.transfer.Config;
import com.infowarelabsdk.conference.util.Constants;
import com.infowarelabsdk.conference.util.FileUtil;
import com.infowarelabsdk.conference.util.Utils;

import me.jessyan.autosize.AutoSizeConfig;
import me.jessyan.autosize.internal.CustomAdapt;

/**
 * Created by sdvye on 2019/6/10.
 */

@SuppressLint("ValidFragment")
public class FragSignIn extends BaseFragment implements CustomAdapt {

    private View signInView;
    private  Button btnSignIn;
    private Button btnRegister;
    private ImageButton btnReturn;
    private Activity mActivity;
    private boolean landscape = false;
    private int screenWidth = 0;
    private int screenHeight = 0;
    private final float screenRatio = 1.6f;

    private int getOrientationState() {
        Configuration mConfiguration = this.getResources().getConfiguration();
        return mConfiguration.orientation;
    }

    private void initOrientation() {

        DisplayMetrics dm = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mActivity.getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        }else{
            mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        }

        int orientation = getOrientationState();

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            screenWidth = dm.widthPixels;
            screenHeight = dm.heightPixels;
        }
        else
        {
            screenWidth = dm.heightPixels;
            screenHeight = dm.widthPixels;
        }

        if (screenHeight/screenWidth > screenRatio){
            //竖屏
            if (getActivity().getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

            landscape = false;
        }
        else
        {
            if (getActivity().getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }

            landscape = true;
        }
    }


    private void initView() {

        btnSignIn = (Button) signInView.findViewById(R.id.btn_sign_in);
        setFocus();
        //站点名称
        Config.SiteName = FileUtil.readSharedPreferences(getActivity(),
                Constants.SHARED_PREFERENCES, Constants.SITE_NAME);
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isFastClick())return;
                btnSignIn.setEnabled(false);
                mActivity = getActivity();
                Intent intent = null;

                intent = new Intent(mActivity, RegisterAndRecognizeActivity.class);

                intent.putExtra("mode",0);
                mActivity.startActivity(intent);
            }
        });
        btnRegister = (Button) signInView.findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isFastClick())return;

                btnSignIn.setEnabled(false);
                mActivity = getActivity();
                Intent intent = null;

                intent = new Intent(mActivity, RegisterAndRecognizeActivity.class);

                intent.putExtra("mode",1);
                mActivity.startActivity(intent);
            }
        });

        btnReturn = (ImageButton) signInView.findViewById(R.id.btn_return);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isFastClick())return;

                //btnReturn.setEnabled(false);

                doSelect(2);
            }
        });

    }

    private void doSelect(int selectId) {
        if (switchPageListener != null) {
            switchPageListener.doSelect(selectId);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AutoSizeConfig.getInstance().setCustomFragment(true);

        mActivity = getActivity();

        initOrientation();

        signInView = inflater.inflate(R.layout.frag_sign_in, container, false);

        initView();

        return signInView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        btnSignIn.setEnabled(true);
        btnRegister.setEnabled(true);

        setFocus();
    }

    //默认选中第一个
    public void setFocus(){
        if (null != btnSignIn){
            btnSignIn.setFocusable(true);
            btnSignIn.requestFocus();
        }
    }

    @Override
    public boolean isBaseOnWidth() {
        return true;
    }

    @Override
    public float getSizeInDp() {
        return 853.33f;
    }
}
