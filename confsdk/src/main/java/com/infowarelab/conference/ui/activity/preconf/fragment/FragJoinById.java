package com.infowarelab.conference.ui.activity.preconf.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.infowarelab.conference.ui.action.JoinConfByIdAction4Frag2;
import com.infowarelab.conference.ui.activity.inconf.ConferenceActivity;
import com.infowarelab.conference.ui.activity.preconf.BaseFragment;
import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.ConferenceCommonImpl;
import com.infowarelabsdk.conference.common.impl.UserCommonImpl;
import com.infowarelabsdk.conference.confctrl.ConferenceCommon;
import com.infowarelabsdk.conference.confctrl.UserCommon;
import com.infowarelabsdk.conference.domain.ConferenceBean;
import com.infowarelabsdk.conference.util.Constants;
import com.infowarelabsdk.conference.util.FileUtil;

import android.widget.ImageButton;

import me.jessyan.autosize.AutoSizeConfig;
import me.jessyan.autosize.internal.CustomAdapt;

public class FragJoinById extends BaseFragment implements View.OnFocusChangeListener,
        OnClickListener, TextWatcher, CustomAdapt {

    private static final int id = 10003;

    protected Activity mActivity;

    private View conferenceNumber;
    private LinearLayout ll1, ll2, ll3, ll4, ll5;
    private TextView tv1, tv2, tvErrMsg;
    private EditText etInput1, etInput2, etInput3;
    private Button btnJoin;
    private ImageView ivCheck;

    private CommonFactory commonFactory = CommonFactory.getInstance();
    private ConferenceCommonImpl conferenceCommon = (ConferenceCommonImpl) commonFactory.getConferenceCommon();
    private JoinConfByIdAction4Frag2 mAction;

    private SharedPreferences preferences;

    protected final static int ENTERFROMITEM = 100;
    public static final int INIT_SDK = 101;
    protected static final int INIT_SDK_FAILED = 102;
    protected static final int CONF_CONFLICT = -5;
    private int pre = 0;
    private boolean isFromItem = false;
    protected final static int AUTO_START = 200;
    private ImageButton btnReturnList;
    private boolean landscape = false;
    private int screenWidth = 0;
    private int screenHeight = 0;
    private final float screenRatio = 1.6f;

    private UserCommon userCommon = (UserCommonImpl) commonFactory
            .getUserCommon();

    public int getFragmentId() { return id; }

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
            //竖屏i
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

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        AutoSizeConfig.getInstance().setCustomFragment(true);

        mActivity = getActivity();
        initOrientation();
        if (!landscape)
            conferenceNumber = inflater.inflate(R.layout.a6_preconf_join_page_number, null);
        else
            conferenceNumber = inflater.inflate(R.layout.a6_preconf_join_page_number_land, null);
        initView();
        return conferenceNumber;
    }

    private void initView() {

        preferences = mActivity.getSharedPreferences(Constants.SHARED_PREFERENCES,
                Context.MODE_PRIVATE);

        ll1 = (LinearLayout) conferenceNumber.findViewById(R.id.view_frag_join_number_ll_1);
        ll2 = (LinearLayout) conferenceNumber.findViewById(R.id.view_frag_join_number_ll_2);
        ll3 = (LinearLayout) conferenceNumber.findViewById(R.id.view_frag_join_number_ll_3);
        ll4 = (LinearLayout) conferenceNumber.findViewById(R.id.view_frag_join_number_ll_4);
        ll5 = (LinearLayout) conferenceNumber.findViewById(R.id.view_frag_join_number_ll_5);

        etInput1 = (EditText) conferenceNumber.findViewById(R.id.view_frag_join_number_et_1);
        etInput2 = (EditText) conferenceNumber.findViewById(R.id.view_frag_join_number_et_2);
        etInput3 = (EditText) conferenceNumber.findViewById(R.id.view_frag_join_number_et_3);
        etInput3.setTypeface(etInput2.getTypeface());

        tv1 = (TextView) conferenceNumber.findViewById(R.id.view_frag_join_number_tv_1);
        tv2 = (TextView) conferenceNumber.findViewById(R.id.view_frag_join_number_tv_2);
        tvErrMsg = (TextView) conferenceNumber.findViewById(R.id.view_frag_join_number_tv_3);

        btnJoin = (Button) conferenceNumber.findViewById(R.id.view_frag_join_number_btn);

        ivCheck = (ImageView) conferenceNumber.findViewById(R.id.view_frag_join_number_iv_1);

        etInput1.setOnFocusChangeListener(this);
        etInput2.setOnFocusChangeListener(this);
        etInput3.setOnFocusChangeListener(this);

        etInput1.addTextChangedListener(this);
        etInput2.addTextChangedListener(this);

        setHandler();

        mAction = new JoinConfByIdAction4Frag2(mActivity, this, conferenceNumber);
        btnJoin.setOnClickListener(mAction);

        ll1.setOnClickListener(this);
        ll2.setOnClickListener(this);
        ll3.setOnClickListener(this);
        ll5.setOnClickListener(mAction);

        btnReturnList = conferenceNumber.findViewById(R.id.btn_return_list);
        btnReturnList.setOnClickListener(this);
    }

    public void setHandler() {
        if (handler != null && conferenceCommon != null) {
            if (conferenceCommon.getHandler() != handler) {
                //Log.d("InfowareLab.Debug", "set pager handler!!!");
                conferenceCommon.setHandler(handler);
            }
        } else {
            Log.d("InfowareLab.Debug", "pager number is null!!!");
        }
    }

    public Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ConferenceCommon.RESULT_SUCCESS:
                    Log.d("InfowareLab.Debug", "FragJoinById: Join success");
                    //昵称更改时保存，在按下进入会议后
//				FileUtil.saveSharedPreferences(activity, Constants.SHARED_PREFERENCES,
//						Constants.LOGIN_NICKNAME, etInput2.getText().toString());

                    if (mActivity == null) break;

                    Intent intent = new Intent(mActivity, ConferenceActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.putExtra("enterFromList", true);
                    mActivity.startActivity(intent);

                    //activity.overridePendingTransition(0, 0);
//				((AudioCommonImpl)commonFactory.getAudioCommon()).startAudioService();
                    //mActivity.finish();
                    mAction.missDislog();
                    //mActivity = null;
                    break;
                case ConferenceCommon.BEYOUNGMAXCOUNT:
                    Log.d("InfowareLab.Debug", "FragJoinById: BEYOUNGMAXCOUNT");
                    Toast.makeText(mActivity, mActivity.getString(R.string.beyound_maxcount), Toast.LENGTH_LONG).show();
                    mAction.missDislog();
                    break;
                case ConferenceCommon.BEYOUNGJIAMI:
                    Log.d("InfowareLab.Debug", "FragJoinById: BEYOUNGMAXCOUNT");
                    Toast.makeText(mActivity, mActivity.getString(R.string.beyound_jiami), Toast.LENGTH_LONG).show();
                    mAction.missDislog();
                    break;
                case ENTERFROMITEM:
                    checkResultIntent((ConferenceBean) msg.obj);
                    checkJoinConf();
                    break;
                case AUTO_START:
                    btnJoin.performClick();
                    break;
                case INIT_SDK:
                    Log.d("InfowareLab.Debug", "initSDK success");
                    break;
                case INIT_SDK_FAILED:
                    Log.d("InfowareLab.Debug", "FragJoinById: INIT_SDK_FAILED");
                    Toast.makeText(mActivity, mActivity.getString(R.string.initSDKFailed), Toast.LENGTH_LONG).show();
                    mAction.missDislog();
                    break;
                case CONF_CONFLICT:
                    Log.d("InfowareLab.Debug", "FragJoinById: CONF_CONFLICT");
                    Toast.makeText(mActivity, mActivity.getString(R.string.confConflict), Toast.LENGTH_LONG).show();
                    mAction.missDislog();
                    //activity.finish();
                    break;
                case ConferenceCommon.LEAVE:
                    Log.d("InfowareLab.Debug", "FragJoinById: LEAVE");
                    break;
                default:
                    //Log.d("InfowareLab.Debug", "ConfPageNumber: Join Failed");
				/*ErrorMessage errorMessage = new ErrorMessage(activity);
				String message = errorMessage.getErrorMessageByCode(msg.what);
				Log.d("InfowareLab.Debug","false code = "+msg.what +" false = "+activity.getString(R.string.ConfSysErrCode_Conf_Area_Error));
                if(!message.equals(activity.getResources().getString(R.string.UnknowError)))
				Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
//				Intent intent = new Intent(ConferenceActivity.this, LoginActivity.class);
				mAction.cancelDialog();*/
                    break;
            }
        }

    };

    /**
     * 将ConferenceBean的信息匹配到各输入框中
     *
     * @param conferenceBean
     */
    private void checkResultIntent(ConferenceBean conferenceBean) {
        int localid = preferences.getInt(Constants.USER_ID, -1);
        if (conferenceBean.getHostID().equals(String.valueOf(localid))) {
            ll5.setVisibility(View.VISIBLE);
            ivCheck.setImageResource(R.drawable.a6_icon_joinhost_normal);
            mAction.isChecked = false;
        } else {
            ll5.setVisibility(View.GONE);
            ivCheck.setImageResource(R.drawable.a6_icon_joinhost_normal);
            mAction.isChecked = false;
        }
        isFromItem = true;
        String password = conferenceBean.getConfPassword();
        String a1 = conferenceBean.getId().substring(0, 4);
        String a2 = conferenceBean.getId().substring(4);
        tv2.setText(a1 + " " + a2);
        ((EditText) etInput2).requestFocus();
        if (((EditText) etInput2).getText() != null) {
            ((EditText) etInput2).setSelection(((EditText) etInput2).getText().toString().length());
        }
        tv1.setVisibility(View.GONE);
        ll1.setVisibility(View.GONE);
        ll4.setVisibility(View.VISIBLE);
        etInput1.setText(a1 + " " + a2);
        String realName = FileUtil.readSharedPreferences(mActivity, Constants.SHARED_PREFERENCES, Constants.LOGIN_EXNAME);
        String loginName = FileUtil.readSharedPreferences(mActivity, Constants.SHARED_PREFERENCES, Constants.LOGIN_NAME);
        etInput2.setText(!TextUtils.isEmpty(realName) ? realName : loginName);
        etInput3.setText("");
        etInput3.setHint(mActivity.getResources().getString(R.string.preconf_login_password));
        tvErrMsg.setText("");
        if (TextUtils.isEmpty(password)) {
            ll3.setVisibility(View.GONE);
        } else {
            ll3.setVisibility(View.VISIBLE);
        }

        mAction.setConfBean(conferenceBean);

    }

    public void setFocus(View v) {
        ((EditText) v).requestFocus();
        if (((EditText) v).getText() != null) {
            ((EditText) v).setSelection(((EditText) v).getText().toString().length());
        }
    }

    /**
     * 清空输入框还原其状态
     */
    public void resumeLayout() {

        ll5.setVisibility(View.GONE);
        ivCheck.setImageResource(R.drawable.a6_icon_joinhost_normal);
        mAction.isChecked = false;

        isFromItem = false;
        tv1.setVisibility(View.VISIBLE);
        ll1.setVisibility(View.VISIBLE);
        ll4.setVisibility(View.GONE);
        etInput1.setText("");
        etInput1.setFocusable(true);
        etInput1.setFocusableInTouchMode(true);
        etInput2.setText("");
        etInput3.setText("");
        etInput3.setHint(mActivity.getResources().getString(R.string.conf_number_pwdhint));
        tvErrMsg.setText("");
        setFocus(etInput1);
        ll3.setVisibility(View.VISIBLE);

		/*if (null != conferenceCommon) {
			boolean bValidate = false;
			if (null != conferenceCommon.mConfId) {
				etInput1.setText(conferenceCommon.mConfId);
				bValidate = true;
			}
			if (null != conferenceCommon.mNickName) {
				etInput2.setText(conferenceCommon.mNickName);
			} else {
				//etInput2.setText("NONAME");
			}

			if (null != conferenceCommon.mConfPwd) {
				etInput3.setText(conferenceCommon.mConfPwd);
			}
		}*/
    }

    /**
     * 监听输入框来改变加会按钮的状态
     */
    private void checkJoinConf() {
        if (!TextUtils.isEmpty(etInput1.getText()) && !TextUtils.isEmpty(etInput2.getText())) {
            btnJoin.setEnabled(true);
        } else {
//			btnJoin.setEnabled(false);
            btnJoin.setEnabled(true);
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
//			activity.showInput(v);
            ((EditText) v).setCursorVisible(true);
        }
    }

    @Override
    public void onClick(View v) {
        //setFocus(((ViewGroup) v).getChildAt(1));

        if (v.getId() == R.id.btn_return_list){
            doSelect(2);
        }
    }


    @Override
    public void afterTextChanged(Editable s) {
        // TODO Auto-generated method stub

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
        pre = s.length();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        checkJoinConf();
        if (s.length() > pre && s.length() == 4) {
            etInput1.append(" ");
        }
    }

    private void showMsg(String message) {
        if (tvErrMsg != null) {
            if (message == null) {
                tvErrMsg.setVisibility(View.GONE);
            } else {
                tvErrMsg.setText(message);
                tvErrMsg.setTextColor(getResources().getColor(R.color.a6_txt_green1));
                tvErrMsg.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        // TODO Auto-generated method stub
        super.onHiddenChanged(hidden);

        if (!hidden){
            showErrMsg(null);
        }
    }

    public void showErrMsg(int resId) {
        if (tvErrMsg != null) {
            if (resId == -1) {
                tvErrMsg.setVisibility(View.GONE);
            } else {
                tvErrMsg.setText(getResources().getString(resId) + "!");
                tvErrMsg.setTextColor(getResources().getColor(R.color.a6_txt_red1));
                tvErrMsg.setVisibility(View.VISIBLE);
            }
        }
    }

    public void showErrMsg(String message) {
        if (tvErrMsg != null) {
            if (message == null) {
                tvErrMsg.setVisibility(View.GONE);
            } else {
                tvErrMsg.setText(message);
                tvErrMsg.setTextColor(getResources().getColor(R.color.a6_txt_red1));
                tvErrMsg.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showMsgInUIThread(String message) {
        getActivity().runOnUiThread(new Runnable(){
            @Override
            public void run() {
                showMsg(message);
            }
        });
    }

    private void showErrMsgInUIThread(String message) {
        getActivity().runOnUiThread(new Runnable(){
            @Override
            public void run() {
                showErrMsg(message);
            }
        });
    }

    public boolean isFromItem() {
        return isFromItem;
    }

    private void doSelect(int selectId) {
        if (switchPageListener != null) {
            switchPageListener.doSelect(selectId);
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
