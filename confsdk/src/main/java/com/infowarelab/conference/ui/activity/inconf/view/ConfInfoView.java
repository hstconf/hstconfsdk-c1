package com.infowarelab.conference.ui.activity.inconf.view;

import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.infowarelab.conference.ui.activity.inconf.BaseFragment;
import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.ConferenceCommonImpl;
import com.infowarelabsdk.conference.common.impl.UserCommonImpl;
import com.infowarelabsdk.conference.confctrl.UserCommon;
import com.infowarelabsdk.conference.transfer.Config;


public class ConfInfoView extends BaseFragment implements View.OnClickListener {
    private View infoView;

    private TextView tvTopic, tvId, tvHost, tvPwd, tvHostpwd;
    private Button btnExit;
    private LinearLayout llPwd, llHostpwd;
    private View placeTop, placeBottom;

    public ConfInfoView(ICallParentView iCallParentView) {
        super(iCallParentView);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        infoView = inflater.inflate(R.layout.conference_info, container, false);
        initView();
        return infoView;
    }

    private void initView() {
        tvTopic = (TextView) infoView.findViewById(R.id.info_tv_topic);
        tvId = (TextView) infoView.findViewById(R.id.info_tv_id);
        tvHost = (TextView) infoView.findViewById(R.id.info_tv_host);
        tvPwd = (TextView) infoView.findViewById(R.id.info_tv_pwd);
        tvHostpwd = (TextView) infoView.findViewById(R.id.info_tv_hostpwd);
        btnExit = (Button) infoView.findViewById(R.id.info_btn_exit);
        llPwd = (LinearLayout) infoView.findViewById(R.id.info_ll_pwd);
        llHostpwd = (LinearLayout) infoView.findViewById(R.id.info_ll_hostpwd);
        placeTop = infoView.findViewById(R.id.view_inconf_info_place_top);
        placeBottom = infoView.findViewById(R.id.view_inconf_info_place_bottom);

        setInfo();
        setPlace();
    }

    public void setInfo() {
        Config config = ((ConferenceCommonImpl) CommonFactory.getInstance()
                .getConferenceCommon()).getConfig();
        UserCommonImpl userCommon = (UserCommonImpl) CommonFactory.getInstance().getUserCommon();
        if (userCommon.getSelf() != null && userCommon.getSelf().getRole() == UserCommon.ROLE_HOST) {
            llHostpwd.setVisibility(View.VISIBLE);
        } else {
            llHostpwd.setVisibility(View.GONE);
        }
        String chairName = "";
        String pre = "";
        String end = "";
        if (config.getConfigBean() == null ||
                config.getConfigBean().getConfInfo_m_confName() == null) {
            pre = config.getMyConferenceBean().getId().substring(0, 4);
            end = config.getMyConferenceBean().getId().substring(4);
            chairName = config.getMyConferenceBean().getHostName();
            tvTopic.setText(config.getMyConferenceBean().getName());
            tvPwd.setText(config.getMyConferenceBean().getConfPassword());
        } else {
            pre = config.getConfigBean().getCourseNum().substring(0, 4);
            end = config.getConfigBean().getCourseNum().substring(4);
            chairName = config.getConfigBean().getConfInfo_m_chairName();
            tvTopic.setText(config.getConfigBean().getConfInfo_m_confName());
            tvPwd.setText(config.getConfigBean().getConfInfo_m_confPassword());
            tvHostpwd.setText(config.getConfigBean().getConfInfo_m_hostKey());
        }
        try {
            chairName = userCommon.getHost().getUsername();
        } catch (Exception e) {
            e.printStackTrace();
        }

        tvId.setText(pre + " " + end);

        if (chairName != null) {
            tvHost.setText(chairName);
        }

        if (TextUtils.isEmpty(tvPwd.getText().toString())) {
            llPwd.setVisibility(View.GONE);
        }

        if (TextUtils.isEmpty(tvHostpwd.getText().toString())) {
            llHostpwd.setVisibility(View.GONE);
        }

        btnExit.setOnClickListener(this);
        btnExit.setVisibility(View.GONE);
    }

    private void setPlace() {
//        Configuration mConfiguration = this.getResources().getConfiguration();
//        int ori = mConfiguration.orientation;
//        if (ori == mConfiguration.ORIENTATION_LANDSCAPE) {
//            placeTop.setVisibility(View.VISIBLE);
//            placeBottom.setVisibility(View.VISIBLE);
//        } else if (ori == mConfiguration.ORIENTATION_PORTRAIT) {
//            placeTop.setVisibility(View.GONE);
//            placeBottom.setVisibility(View.GONE);
//        }
    }

    public void changeOrietation(Configuration newConfig) {
//        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
//            placeTop.setVisibility(View.VISIBLE);
//            placeBottom.setVisibility(View.VISIBLE);
//        }else if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
//            placeTop.setVisibility(View.GONE);
//            placeBottom.setVisibility(View.GONE);
//        }
    }


    @Override
    public void onClick(View v) {
        callParentView(ACTION_SHOWEXIT, null);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!isHidden()) {
            setInfo();
            setPlace();
        }
    }

    public boolean getOnBackPressed() {
        return true;
    }
}
