package com.infowarelab.conference.ui.activity.preconf;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import androidx.annotation.RequiresApi;

import com.infowarelab.conference.BaseActivity;
import com.infowarelab.conference.ui.activity.inconf.ConferenceActivity;
import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.ConferenceCommonImpl;
import com.infowarelabsdk.conference.domain.LoginBean;
import com.infowarelabsdk.conference.transfer.Config;
import com.infowarelabsdk.conference.util.Constants;
import com.infowarelabsdk.conference.util.FileUtil;
import com.infowarelabsdk.conference.util.StringUtil;

public class LoginActivity extends BaseActivity implements TextWatcher {
    private LinearLayout llLogin;
    private EditText loginName;
    private EditText loginPassword;
    private Button loginConfirm;
    private TextView tvErrMsg;

    private RelativeLayout rlLogout;
    private TextView tvId, tvNickname;
    private Button btnLogout;

    int state;
    public SharedPreferences preferences;
    private Intent intent;
    private LoginBean loginBean = null;

    public static final int LOGIN_PROGRESS_HIDE = 0;
    public static final int LOGIN_PROGRESS_SHOWE = 1;
    public static final int LOGIN_SUCCESS = 2;
    public static final int LOGIN_INPUT_ILLEGAL = 3;
    public static final int LOGIN_INPUT_NULL = 4;
    public static final String LOGIN_NAME = "NAME";
    public static final String LOGIN_PASSWORD = "PASSWORD";
    private int turnIndex = 0;//0不跳转，1跳转至createConf，2跳转至confList
    public static final int RESULTCODE_LOGIN = 10;
    public static final int RESULTCODE_LOGOUT = 11;

    public Handler loginHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOGIN_PROGRESS_HIDE:
                    hideLoading();
                    showErrMsg(R.string.preconf_login_error);
                    break;
                case LOGIN_PROGRESS_SHOWE:
                    showLoading();
                    break;
                case LOGIN_SUCCESS:
                    hideLoading();
                    FileUtil.saveSharedPreferences(LoginActivity.this, Constants.SHARED_PREFERENCES,
                            Constants.LOGIN_NAME, loginName.getText().toString().trim());
                    FileUtil.saveSharedPreferences(LoginActivity.this, Constants.SHARED_PREFERENCES,
                            Constants.LOGIN_PASS, loginPassword.getText().toString().trim());
                    if (loginBean.getNickname() != null && !loginBean.getNickname().equals("")) {
                        FileUtil.saveSharedPreferences(LoginActivity.this, Constants.SHARED_PREFERENCES,
                                Constants.LOGIN_NICKNAME, loginBean.getNickname());
                    } else {
                        FileUtil.saveSharedPreferences(LoginActivity.this, Constants.SHARED_PREFERENCES,
                                Constants.LOGIN_NICKNAME, loginName.getText().toString().trim());
                    }
                    String joinName = loginName.getText().toString().trim();
                    Log.e("ttttt", "真实姓名::" + loginBean.getRealname());
                    if (loginBean.getRealname() != null && !loginBean.getRealname().equals("")) {
//					joinName = loginBean.getRealname();
                        FileUtil.saveSharedPreferences(LoginActivity.this, Constants.SHARED_PREFERENCES,
                                Constants.LOGIN_EXNAME, loginBean.getRealname());
                    } else if (loginBean.getNickname() != null && !loginBean.getNickname().equals("")) {
//					joinName = loginBean.getNickname();
                        FileUtil.saveSharedPreferences(LoginActivity.this, Constants.SHARED_PREFERENCES,
                                Constants.LOGIN_EXNAME, loginBean.getNickname());
                    } else {
//					joinName = loginName.getText().toString().trim();
                        FileUtil.saveSharedPreferences(LoginActivity.this, Constants.SHARED_PREFERENCES,
                                Constants.LOGIN_EXNAME, loginName.getText().toString().trim());
                    }
                    FileUtil.saveSharedPreferences(LoginActivity.this,
                            Constants.SHARED_PREFERENCES,
                            Constants.LOGIN_JOINNAME, joinName);
//				FileUtil.saveSharedPreferences(LoginActivity.this, Constants.SHARED_PREFERENCES,
//						Constants.LOGIN_EXNAME, loginName.getText().toString().trim());
                    FileUtil.saveSharedPreferences(LoginActivity.this, Constants.SHARED_PREFERENCES,
                            Constants.LOGIN_ROLE, loginBean.getCreateConfRole());

                    saveUId();
                    showShortToast(R.string.notify_login_suc);
                    break;
                case LOGIN_INPUT_ILLEGAL:
                    showErrMsg(R.string.preconf_login_illegal);
                    break;
                case LOGIN_INPUT_NULL:
                    showErrMsg(R.string.preconf_login_null);
                default:
                    break;
            }
        }

        ;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preconf_login);
        state = getIntent().getIntExtra("state", 0);
        if (getIntent().hasExtra("turnIndex")) {
            turnIndex = getIntent().getIntExtra("turnIndex", 0);
        }

        initView();
    }

    private void initView() {
        llLogin = (LinearLayout) findViewById(R.id.act_preconf_login_ll_in);
        loginName = (EditText) findViewById(R.id.preconf_login_editname);
        loginPassword = (EditText) findViewById(R.id.preconf_login_editpassword);
        loginPassword.setTypeface(loginName.getTypeface());
        loginConfirm = (Button) findViewById(R.id.preconf_login_confirm);
        tvErrMsg = (TextView) findViewById(R.id.preconf_login_tv_msg);


        rlLogout = (RelativeLayout) findViewById(R.id.act_preconf_login_rl_out);
        tvId = (TextView) findViewById(R.id.act_preconf_login_tv_id);
        tvNickname = (TextView) findViewById(R.id.act_preconf_login_tv_nickname);
        btnLogout = (Button) findViewById(R.id.act_preconf_login_btn_out);

        String username = FileUtil.readSharedPreferences(this, Constants.SHARED_PREFERENCES, Constants.LOGIN_NAME);
        String usernickname = FileUtil.readSharedPreferences(this, Constants.SHARED_PREFERENCES, Constants.LOGIN_JOINNAME);
        if (username.equals("")) {
            rlLogout.setVisibility(View.GONE);
            llLogin.setVisibility(View.VISIBLE);
            loginName.setText(username);
            loginName.setSelection(loginName.getText().length());
        } else {
            String realName = FileUtil.readSharedPreferences(this, Constants.SHARED_PREFERENCES, Constants.LOGIN_EXNAME);
            tvId.setText(!TextUtils.isEmpty(realName) ? realName : username);
            tvNickname.setText(usernickname);
            llLogin.setVisibility(View.GONE);
            rlLogout.setVisibility(View.VISIBLE);
        }

        loginName.addTextChangedListener(this);
        loginPassword.addTextChangedListener(this);
    }

    private boolean checkIsNull() {
        if (StringUtil.isNullOrBlank(loginName.getText().toString())) {
            return true;
        } else if (StringUtil.isNullOrBlank(loginPassword.getText().toString())) {
            return true;
        }
        return false;
    }

    private boolean checkIsIlegal() {
        if (StringUtil.checkInput(loginName.getText().toString().trim(), Constants.PATTERN)) {
            return true;
        } else if (StringUtil.checkInput(loginName.getText().toString().trim(), Constants.PATTERN)) {
            return true;
        }
        return false;
    }

    /**
     * 获取加会参数并进行保存
     */
    private void saveUId() {
        preferences = getSharedPreferences(Constants.SHARED_PREFERENCES,
                Context.MODE_PRIVATE);
        preferences.edit().putInt(Constants.CONF_LIST_TYPE, 0)
                .putInt(Constants.USER_ID, loginBean.getUid()).commit();

    }

    public void ItemClick(View v) {
        int id = v.getId();
        if (id == R.id.act_preconf_login_ll_back) {//			hideInput(v);
            finishWithResult(RESULT_CANCELED);
        } else if (id == R.id.preconf_login_confirm) {
            hideInput(v);
            showErrMsg(-1);
            new Thread() {
                public void run() {
                    if (checkIsNull()) {
                        loginHandler.sendEmptyMessage(LOGIN_INPUT_NULL);
                    } else if (!checkIsIlegal()) {
                        loginHandler.sendEmptyMessage(LOGIN_INPUT_ILLEGAL);
                    } else {
                        loginHandler.sendEmptyMessage(LOGIN_PROGRESS_SHOWE);
                        doLogin();
                    }
                }

                ;
            }.start();
        } else if (id == R.id.act_preconf_login_btn_out) {//			hideInput(v);
            logout();
            clearUid();
            showLongToast(R.string.notify_logout_suc);

            FileUtil.saveSharedPreferences(LoginActivity.this, Constants.SHARED_PREFERENCES,
                    Constants.LOGIN_EXNAME, "");
            FileUtil.saveSharedPreferences(LoginActivity.this, Constants.SHARED_PREFERENCES,
                    Constants.LOGIN_NAME, "");
            finishWithResult(RESULTCODE_LOGOUT);
        }
    }

    /**
     * 检查登录用户名密码是否正确
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void doLogin() {
        Config.SiteName = FileUtil.readSharedPreferences(this,
                Constants.SHARED_PREFERENCES, Constants.SITE_NAME);
        loginBean = new LoginBean(null, loginName.getText().toString(),
                loginPassword.getText().toString());
        loginBean = ((ConferenceCommonImpl) CommonFactory.getInstance().getConferenceCommon())
                .checkUser(loginBean);

        if (loginBean != null) {
            ConferenceActivity.isLogin = true;
            loginHandler.sendEmptyMessage(LOGIN_SUCCESS);
            switch (turnIndex) {
                case 1:
//				intent = new Intent(this, ConfCreateActivity.class);
//				startActivity(intent);
//				setResult(1);
                    break;
                case 2:
//				intent = new Intent(this, ConferenceListActivity.class);
//				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//				startActivity(intent);
//				setResult(2);
                    break;
                case 3:
				/*intent = new Intent(this, ConfSetupActivity.class);
				startActivity(intent);*/
//				setResult(3);
                    break;
                case 4:
                    break;
                default:
//				startHomeActivity();
                    break;
            }

            finishWithResult(RESULTCODE_LOGIN);
        } else {
            loginHandler.sendEmptyMessage(LOGIN_PROGRESS_HIDE);
        }
    }

    private void checkEdit() {
        if (TextUtils.isEmpty(loginName.getText().toString().trim())
                || TextUtils.isEmpty(loginPassword.getText().toString().trim())) {
//			loginConfirm.setEnabled(false);
            loginConfirm.setEnabled(true);
        } else {
            loginConfirm.setEnabled(true);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        checkEdit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishWithResult(RESULT_CANCELED);
    }


    private void saveNickname() {
        FileUtil.saveSharedPreferences(this, Constants.SHARED_PREFERENCES,
                Constants.LOGIN_JOINNAME, tvNickname.getText().toString());
    }

    /**
     * 注销后清空保存的用户名
     */
    private void clearUid() {
        preferences = getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        preferences.edit().putInt(Constants.CONF_LIST_TYPE, 1).putInt(Constants.USER_ID, 0).commit();
    }

    public void showErrMsg(int resId) {
        if (tvErrMsg != null) {
            if (resId == -1) {
                tvErrMsg.setVisibility(View.GONE);
            } else if (resId == -2) {
                tvErrMsg.setVisibility(View.GONE);
            } else {
                tvErrMsg.setText(getResources().getString(resId) + "!");
                tvErrMsg.setVisibility(View.VISIBLE);
            }
        }
    }

}
