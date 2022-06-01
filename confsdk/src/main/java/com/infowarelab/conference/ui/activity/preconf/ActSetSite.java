package com.infowarelab.conference.ui.activity.preconf;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.infowarelab.conference.BaseActivity;
import com.infowarelab.conference.ui.adapter.AutoAdapter;
import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.transfer.Config;
import com.infowarelabsdk.conference.util.Constants;
import com.infowarelabsdk.conference.util.FileUtil;
import com.infowarelabsdk.conference.util.NetUtil;
import com.infowarelabsdk.conference.util.StringUtil;

import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActSetSite extends BaseActivity implements OnClickListener, TextWatcher, OnFocusChangeListener {
    private EditText etSite;
    private Button btnConfirm;
    private TextView tvErrMsg, tvSuc;
    private ImageView ivClear;


    private AutoAdapter autoAdapter;
    private AlertDialog.Builder alertDialog;
    private Intent siteIntent;

    private String text;
    private String preSite;
    private static final int DIALOG_HIDE_ERR = 3;
    private static final int DIALOG_HIDE_SUC = 4;
    private static final int DIALOG_SHOW = 1;
    private static final int PROGRESS_HIDE = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a6_preconf_set_site);
        initView();
    }

    private void initView() {
        etSite = (EditText) findViewById(R.id.act_preconf_site_et);
        btnConfirm = (Button) findViewById(R.id.act_preconf_site_btn);
        tvErrMsg = (TextView) findViewById(R.id.act_preconf_site_tv_1);
        tvSuc = (TextView) findViewById(R.id.act_preconf_site_tv_2);
        ivClear = (ImageView) findViewById(R.id.act_preconf_site_iv_clear);
        etSite.setText(FileUtil.readSharedPreferences(
                this, Constants.SHARED_PREFERENCES, Constants.SITE));

        preSite = FileUtil.readSharedPreferences(this, Constants.SHARED_PREFERENCES, Constants.SITE);

        autoAdapter = new AutoAdapter(this);
//		etSite.setThreshold(1);
        String tel = getResources().getString(R.string.tel);
//		if(tel.equals("0")){
//		}else {
//			etSite.setAdapter(autoAdapter);
//		}
//		etSite.setDropDownBackgroundResource(R.drawable.setup_input_normal);
//		etSite.setDropDownHorizontalOffset(1);
        etSite.addTextChangedListener(this);
        etSite.setOnFocusChangeListener(this);

        btnConfirm.setOnClickListener(this);
        ivClear.setOnClickListener(this);
    }

    private Handler siteHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DIALOG_HIDE_ERR:
                    hideLoading();
                    showErrMsg(R.string.site_error);
                    break;
                case DIALOG_HIDE_SUC:
                    hideLoading();
                    showErrMsg(-2);
                    break;
                case DIALOG_SHOW:
                    showLoading();
                    showErrMsg(-1);
                    break;
                case PROGRESS_HIDE:
                    hideLoading();
                    showAlertDialog();
                    break;
                default:
                    break;
            }
        }

        ;
    };

    /**
     * 显示提示框
     */
    private void showAlertDialog() {
        alertDialog = new AlertDialog.Builder(this);
        alertDialog.setCustomTitle(LayoutInflater.from(this).inflate(R.layout.notify_dialog, null));
        alertDialog.setPositiveButton(getResources().getString(R.string.confirm),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendIntent();
                    }
                });
        alertDialog.setNegativeButton(getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FileUtil.saveSharedPreferences(ActSetSite.this, Constants.SHARED_PREFERENCES,
                                Constants.SITE_NAME, "");
                        FileUtil.saveSharedPreferences(ActSetSite.this, Constants.SHARED_PREFERENCES,
                                Constants.SITE_ID, "");
                        FileUtil.saveSharedPreferences(ActSetSite.this, Constants.SHARED_PREFERENCES,
                                Constants.SITE, "");
                        FileUtil.saveSharedPreferences(ActSetSite.this, Constants.SHARED_PREFERENCES,
                                Constants.HAS_LIVE_SERVER, "");
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void sendIntent() {
        FileUtil.saveSharedPreferences(this, Constants.SHARED_PREFERENCES, Constants.SITE, text);
        if (!preSite.equals(text)) {
            logout();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.act_preconf_site_ll_back) {
            hideInput(v);
            finishWithResult(RESULT_CANCELED);
        } else if (id == R.id.act_preconf_site_iv_clear) {
            etSite.setText("");
        } else if (id == R.id.act_preconf_site_btn) {
            hideInput(v);
            if (checkSite()) {
                siteHandler.sendEmptyMessage(DIALOG_SHOW);
                new Thread() {
                    @Override
                    public void run() {
                        Config.SiteName = Config.getSiteName(text);
                        if (Config.SiteName.equals("") || Config.SiteName == null) {
                            siteHandler.sendEmptyMessage(DIALOG_HIDE_ERR);
                        } else {
                            Config.Site_URL = text;
                            //保存SiteName、SiteId信息
                            FileUtil.saveSharedPreferences(ActSetSite.this, Constants.SHARED_PREFERENCES,
                                    Constants.SITE_NAME, Config.SiteName);
                            FileUtil.saveSharedPreferences(ActSetSite.this, Constants.SHARED_PREFERENCES,
                                    Constants.SITE_ID, Config.SiteId);
                            FileUtil.saveSharedPreferences(ActSetSite.this, Constants.SHARED_PREFERENCES,
                                    Constants.SITE, Config.Site_URL);
                            FileUtil.saveSharedPreferences(ActSetSite.this, Constants.SHARED_PREFERENCES,
                                    Constants.HAS_LIVE_SERVER, "" + Config.HAS_LIVE_SERVER);
                            if (text.equals(getResources().getString(R.string.site_hint))) {
                                siteHandler.sendEmptyMessage(PROGRESS_HIDE);
                            } else {
                                siteHandler.sendEmptyMessage(DIALOG_HIDE_SUC);
                                sendIntent();
                            }

                        }
                    }

                    ;
                }.start();

            }
        }
    }

    /**
     * 检测出入站点IP的格式
     *
     * @return
     */
    private boolean checkSite() {
        text = etSite.getText().toString().trim();
        if (text.trim().length() <= 0 || text.trim().equals("")) {
//			showShortToast(R.string.site_error_null);
            showErrMsg(R.string.site_error_null);
            return false;
        } else if (text.length() > 0) {
//			text = siteEdit.getText().toString().replace(" ", "");
//			if(!text.contains("http://")){
//				if(!text.contains("https://")&&!StringUtil.isIP(text)){
//					text="http://"+doEncode(text);
//				}
//			}else {
//				String s= text.replace("http://", "");
//				text = "http://"+doEncode(s);
//			}
            if (!text.contains("http://")) {
                if (!text.contains("https://") && !StringUtil.isIP(text)) {
//					text="http://"+InputEncode.doEncode(text);
                    String[] ss = text.split(":");
                    String s = "";
                    for (String string : ss) {
                        s = s + ":" + doEncode(string);
                    }
                    s = s.replaceFirst(":", "");
                    text = "http://" + s;
                }
            } else {
                String s = text.replace("http://", "");
                String[] ss = s.split(":");
                s = "";
                for (String string : ss) {
                    s = s + ":" + doEncode(string);
                }
                s = s.replaceFirst(":", "");
                text = "http://" + s;
            }
            String regex = "^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
            Pattern patt = Pattern.compile(regex);
            Matcher matcher = patt.matcher(text.trim());
            boolean isMatch = matcher.matches();
            if (!isMatch) {
//				showShortToast(R.string.site_error_wrong);
                showErrMsg(R.string.site_error_wrong);
                return false;
            }
        } else {
            if (text.equals(FileUtil.readSharedPreferences(
                    this, Constants.SHARED_PREFERENCES, Constants.SITE))) {
                return false;
            }
        }
        //������������
        if (true) {
            if (!NetUtil.isNetworkConnected(this)) {
//				showShortToast(R.string.site_error_net);
                showErrMsg(R.string.site_error_net);
                return false;
            }
        }
        etSite.clearFocus();
        return true;
    }

    @Override
    public void afterTextChanged(Editable arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                  int arg3) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        if (etSite.getText().toString().length() > 0) {
            btnConfirm.setEnabled(true);
            ivClear.setVisibility(View.VISIBLE);
        } else if (etSite.isFocused()) {
//			btnConfirm.setEnabled(false);
            btnConfirm.setEnabled(true);
            ivClear.setVisibility(View.GONE);
        } else {
            btnConfirm.setEnabled(true);
            ivClear.setVisibility(View.GONE);
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus && (etSite.getText().toString() == null ||
                etSite.getText().toString().trim().equals(""))) {
//			btnConfirm.setEnabled(false);
            btnConfirm.setEnabled(true);
            ivClear.setVisibility(View.GONE);
        } else {
            btnConfirm.setEnabled(true);
            ivClear.setVisibility(View.VISIBLE);
        }

    }

    public String doEncode(String s) {
        String s1 = URLEncoder.encode(s);
        return s1;
    }

    private void showErrMsg(int resId) {
        if (tvErrMsg != null) {
            if (resId == -1) {
                tvErrMsg.setVisibility(View.GONE);
                tvSuc.setVisibility(View.GONE);
            } else if (resId == -2) {
                tvErrMsg.setVisibility(View.GONE);
                tvSuc.setVisibility(View.VISIBLE);
            } else {
                tvErrMsg.setText(getResources().getString(resId) + "!");
                tvErrMsg.setVisibility(View.VISIBLE);
                tvSuc.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishWithResult(RESULT_CANCELED);
    }
}
