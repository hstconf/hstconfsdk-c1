package com.infowarelab.conference.ui.activity.preconf.view;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.infowarelab.conference.ui.action.JoinConfByIdAction4Frag;
import com.infowarelab.conference.ui.activity.inconf.ConferenceActivity;
import com.infowarelab.conference.ui.activity.preconf.BaseFragmentActivity;
import com.infowarelab.conference.ui.activity.preconf.fragment.FragJoin;
import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.ConferenceCommonImpl;
import com.infowarelabsdk.conference.common.impl.UserCommonImpl;
import com.infowarelabsdk.conference.confctrl.ConferenceCommon;
import com.infowarelabsdk.conference.confctrl.UserCommon;
import com.infowarelabsdk.conference.domain.ConferenceBean;
import com.infowarelabsdk.conference.util.Constants;
import com.infowarelabsdk.conference.util.FileUtil;

public class ConferencePagerNumber extends ConferencePager implements OnFocusChangeListener,
        OnClickListener, TextWatcher {
    private View conferenceNumber;
    private LinearLayout ll1, ll2, ll3, ll4, ll5;
    private TextView tv1, tv2, tvErrMsg;
    private EditText etInput1, etInput2, etInput3;
    private Button btnJoin;
    private ImageView ivCheck;


    private CommonFactory commonFactory = CommonFactory.getInstance();
    private ConferenceCommonImpl conferenceCommon = (ConferenceCommonImpl) commonFactory.getConferenceCommon();
    private JoinConfByIdAction4Frag mAction;
    private BaseFragmentActivity activity;
    private FragJoin fragJoin;
    private SharedPreferences preferences;

    protected final static int ENTERFROMITEM = 100;
    public static final int INIT_SDK = 101;
    protected static final int INIT_SDK_FAILED = 102;
    protected static final int CONF_CONFLICT = -5;
    private int pre = 0;
    private boolean isFromItem = false;
    protected final static int AUTO_START = 200;

    private UserCommon userCommon = (UserCommonImpl) commonFactory
            .getUserCommon();

    public ConferencePagerNumber(BaseFragmentActivity activity, FragJoin fragJoin) {
        super(activity);
        this.activity = activity;
        this.fragJoin = fragJoin;
    }

    @Override
    public View getNewView() {
        init();
        return conferenceNumber;
    }

    private void init() {
        conferenceNumber = mInflater.inflate(R.layout.a6_preconf_join_page_number, null);
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
        mAction = new JoinConfByIdAction4Frag(mActivity, fragJoin, conferenceNumber);
        btnJoin.setOnClickListener(mAction);
        ll1.setOnClickListener(this);
        ll2.setOnClickListener(this);
        ll3.setOnClickListener(this);
        ll5.setOnClickListener(mAction);

		/*
		if (null != conferenceCommon) {
			boolean bValidate = false;
			if (null != conferenceCommon.mConfId){
				etInput1.setText(conferenceCommon.mConfId);
				bValidate = true;
			}
			if (null != conferenceCommon.mNickName){
				etInput2.setText(conferenceCommon.mNickName);
			}else {
				//etInput2.setText("NONAME");
			}

			if (null != conferenceCommon.mConfPwd){
				etInput3.setText(conferenceCommon.mConfPwd);
			}

			if (bValidate == true) {
				if (null != conferenceCommon.mConfId && conferenceCommon.mConfId.length() > 0) {
					if (false == FileUtil.readSharedPreferencesBoolean(activity,
							Constants.SHARED_PREFERENCES, Constants.INVOKED))

						handler.sendEmptyMessage(AUTO_START);

					FileUtil.saveSharedPreferences(activity,
							Constants.SHARED_PREFERENCES, Constants.INVOKED, true);
				}
			}
		}*/
    }

    public void setHandler() {
        if (handler != null && conferenceCommon != null) {
            if (conferenceCommon.getHandler() != handler) {
                Log.d("InfowareLab.Debug", "set pager handler!!!");
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
                    Log.d("InfowareLab.Debug", "ConfPageNumber: Join success");
                    //昵称更改时保存，在按下进入会议后
//				FileUtil.saveSharedPreferences(activity, Constants.SHARED_PREFERENCES, 
//						Constants.LOGIN_NICKNAME, etInput2.getText().toString());

                    if (activity == null) break;

                    Intent intent = new Intent(activity, ConferenceActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.putExtra("enterFromList", true);
                    activity.startActivity(intent);
                    //activity.overridePendingTransition(0, 0);
//				((AudioCommonImpl)commonFactory.getAudioCommon()).startAudioService();
                    activity.finish();
                    mAction.missDislog();
                    activity = null;
                    break;
                case ConferenceCommon.BEYOUNGMAXCOUNT:
                    Log.d("InfowareLab.Debug", "ConfPageNumber: BEYOUNGMAXCOUNT");
                    showShortToast(mActivity.getString(R.string.beyound_maxcount));
                    mAction.missDislog();
                    break;
                case ConferenceCommon.BEYOUNGJIAMI:
                    Log.d("InfowareLab.Debug", "ConfPageNumber: BEYOUNGMAXCOUNT");
                    showShortToast(mActivity.getString(R.string.beyound_jiami));
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
                    Log.d("InfowareLab.Debug", "ConfPageNumber: INIT_SDK_FAILED");
                    showShortToast(mActivity.getString(R.string.initSDKFailed));
                    mAction.missDislog();
                    break;
                case CONF_CONFLICT:
                    Log.d("InfowareLab.Debug", "ConfPageNumber: CONF_CONFLICT");
                    Toast.makeText(activity, mActivity.getString(R.string.LoginFailed), Toast.LENGTH_LONG).show();
                    //showShortToast(mActivity.getString(R.string.LoginFailed));
                    mAction.missDislog();
                    //activity.finish();
                    break;
                case ConferenceCommon.LEAVE:
                    Log.d("InfowareLab.Debug", "ConfPageNumber: LEAVE");
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
        activity.setFocus(etInput2);
        tv1.setVisibility(View.GONE);
        ll1.setVisibility(View.GONE);
        ll4.setVisibility(View.VISIBLE);
        etInput1.setText(a1 + " " + a2);
        String realName = FileUtil.readSharedPreferences(activity, Constants.SHARED_PREFERENCES, Constants.LOGIN_EXNAME);
        String loginName = FileUtil.readSharedPreferences(activity, Constants.SHARED_PREFERENCES, Constants.LOGIN_NAME);
        etInput2.setText(!TextUtils.isEmpty(realName) ? realName : loginName);
        etInput3.setText("");
        etInput3.setHint(mContext.getResources().getString(R.string.preconf_login_password));
        tvErrMsg.setText("");
        if (TextUtils.isEmpty(password)) {
            ll3.setVisibility(View.GONE);
        } else {
            ll3.setVisibility(View.VISIBLE);
        }

        mAction.setConfBean(conferenceBean);

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
        etInput3.setHint(mContext.getResources().getString(R.string.conf_number_pwdhint));
        tvErrMsg.setText("");
        activity.setFocus(etInput1);
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
        activity.setFocus(((ViewGroup) v).getChildAt(1));
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

    public void showErrMsg(int resId) {
        if (tvErrMsg != null) {
            if (resId == -1) {
                tvErrMsg.setVisibility(View.GONE);
            } else if (resId == -2) {
                tvErrMsg.setVisibility(View.GONE);
            } else {
                tvErrMsg.setText(activity.getResources().getString(resId) + "!");
                tvErrMsg.setVisibility(View.VISIBLE);
            }
        }
    }

    public void showErrMsg(String s) {
        if (tvErrMsg != null) {
            tvErrMsg.setText(s + "!");
            tvErrMsg.setVisibility(View.VISIBLE);
        }
    }

    public boolean isFromItem() {
        return isFromItem;
    }
}
