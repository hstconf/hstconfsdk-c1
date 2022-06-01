package com.infowarelab.conference.ui.activity.inconf;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.fragment.app.Fragment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.infowarelab.conference.ConferenceApplication;
import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.common.CommonFactory;

public abstract class BaseFragment extends Fragment {
    public static final String ACTION_HIDEPARENTBOTTOM = "action_hideparentbottom";
    public static final String ACTION_HIDEPARENTBOTTOMBUTFINISH = "action_hideparentbottombutfinish";
    public static final String ACTION_SHOWPARENTBOTTOM = "action_showparentbottom";
    public static final String ACTION_CLOSEORIETATION = "action_closeorietation";
    public static final String ACTION_OPENORIETATION = "action_openorietation";
    public static final String ACTION_SHOWEXIT = "action_showexit";
    public static final String ACTION_ROLEUPDATE = "action_roleupdate";
    public static final String ACTION_SETPRIVITEGE = "action_setprivilege";
    public static final String ACTION_SETRESOLUTION = "action_setresolution";
    public static final String ACTION_TRANSCHANNEL = "action_transchannel";
    public static final String ACTION_CLOSECAMERA = "action_closecamera";
    public static final String ACTION_JUMP2IMG = "action_jump2img";
    public static final String ACTION_VIDEOPAGE = "action_videopage";
    public static final String ACTION_ASSTATE = "action_asstate";
    public static final String ACTION_SHARE2AS = "action_share2as";
    public static final String ACTION_SHARE2DS = "action_share2ds";
    public static final String ACTION_SWITCH2AS = "action_switch2as";
    public static final String ACTION_REFRESHCAMERA = "action_refreshcamera";

    public static final String ACTION_PRIVILEDGE_VS = "action_priviledge_vs";
    public static final String ACTION_PRIVILEDGE_DS = "action_priviledge_ds";
    public static final String ACTION_PRIVILEDGE_ANN = "action_priviledge_ann";
    public static final String ACTION_PRIVILEDGE_ATT = "action_priviledge_att";


    public static final String ACTION_CHANGEBARS = "action_changebars";
    public static final String ACTION_SHOWBOTTOM = "action_showbottom";
    public static final String ACTION_HIDEBOTTOM = "action_hidebottom";
    public static final String ACTION_SHOWBARS = "action_showbars";
    public static final String ACTION_HIDEBARS = "action_hidebars";

    public static final String ACTION_CHANGEAUDIOSTATE = "action_changeaudiostate";
    public static final String ACTION_ADDUSER = "action_adduser";
    public static final String ACTION_REMOVEUSER = "action_removeuser";
    public static final String ACTION_REFRESHAUDIO = "action_refreshaudio";

    protected CommonFactory commonFactory = CommonFactory.getInstance();
    protected Intent intent;

    public BaseFragment(ICallParentView iCallParentView) {
        this.iCallParentView = iCallParentView;
    }

    /**
     * 弹出输入法
     *
     * @param v
     */
    public void showInput(View v) {
        ((InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE)).showSoftInput(v, 0);
    }

    protected void hideInput(View v) {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != getActivity().getCurrentFocus() && null != getActivity().getCurrentFocus().getApplicationWindowToken()) {
            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus()
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
        Toast.makeText(getActivity(), resId, Toast.LENGTH_LONG).show();
    }

    public void showShortToast(int resId) {
        Toast.makeText(getActivity(), resId, Toast.LENGTH_SHORT).show();
    }

    public void showShortToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    public void activityForwardAnimation(Activity activity) {
        activity.overridePendingTransition(R.anim.anim_in_forward_activity, R.anim.anim_out_forward_activity);
    }

    public void activityBackAnimation(Activity activity) {
        activity.overridePendingTransition(R.anim.anim_in_back_activity, R.anim.anim_out_back_activity);
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

    protected String cutName(String s, int l) {
        try {
            s = idgui(s, l);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            if (s.length() > 6) {
                s = s.substring(0, 6) + "...";
            }
        }
        return s;
    }

    private String idgui(String s, int num) throws Exception {
        int changdu = s.getBytes("GBK").length;
        if (changdu > num) {
            s = s.substring(0, s.length() - 1);
            s = idgui2(s, num) + "…";
        }
        return s;
    }

    private String idgui2(String s, int num) throws Exception {
        int changdu = s.getBytes("GBK").length;
        if (changdu > num) {
            s = s.substring(0, s.length() - 1);
            s = idgui2(s, num);
        }
        return s;
    }

    private ICallParentView iCallParentView;

    public void setiCallParentView(ICallParentView iCallParentView) {
        this.iCallParentView = iCallParentView;
    }

    public interface ICallParentView {
        void onCallParentView(String msg, Object obj);
    }

    protected void callParentView(String msg, Object obj) {
        if (iCallParentView != null) {
            iCallParentView.onCallParentView(msg, obj);
        }
    }

    protected void setBottomHeight(View v) {
        RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) v.getLayoutParams();
        p.height = (getRootW() / 5) * 8 / 11;
        v.setLayoutParams(p);
    }


    protected void callChangeBars() {
        callParentView(ACTION_CHANGEBARS, null);
    }

    protected void callShowBottom() {
        callParentView(ACTION_SHOWBOTTOM, null);
    }

    protected void callHideBottom() {
        callParentView(ACTION_HIDEBOTTOM, null);
    }

    protected void callShowBars() {
        callParentView(ACTION_SHOWBARS, null);
    }

    protected void callHideBars() {
        callParentView(ACTION_HIDEBARS, null);
    }

    protected void callPriviledge(String module, boolean isEnable) {
        callParentView(module, isEnable);
    }


}
