package com.infowarelab.conference.ui.activity.preconf;

import android.app.AlertDialog;
import android.app.Dialog;
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
import android.widget.LinearLayout;
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
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SiteSetupActivity extends BaseActivity implements OnClickListener,
        TextWatcher, OnFocusChangeListener {
    private EditText siteEdit;
    private ImageView siteHelp;
    private ImageView siteClear;
    private Button siteButton;
    private LinearLayout siteHelpLayout;
    private TextView siteHelpUpdate;
    private TextView siteErrMsg;

    private AutoAdapter autoAdapter;
    private View view;
    private Dialog dialog;
    private AlertDialog.Builder alertDialog;
    private Intent siteIntent;

    private String text;
    private String preSite;
    private boolean isUpdateSite;
    private static final int DIALOG_HIDE = 0;
    private static final int DIALOG_SHOW = 1;
    private static final int PROGRESS_HIDE = 2;

    private TextView textView2;

    private Handler siteHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DIALOG_HIDE:
                    if (!isFinishing()) {
                        dialog.cancel();
                    }
//				showShortToast(R.string.site_error);
                    showErrMsg(R.string.site_error);
                    break;
                case DIALOG_SHOW:
                    if (dialog != null && !SiteSetupActivity.this.isFinishing()) {
                        dialog.show();
                    }
                    break;
                case PROGRESS_HIDE:
                    dialog.cancel();
                    showAlertDialog();
                    break;
                default:
                    break;
            }
        }

        ;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getExtras() == null) {
            isUpdateSite = false;
        } else {
            isUpdateSite = getIntent().getExtras().getBoolean("updateSite");
        }
        setContentView(R.layout.site_setup);
        initView();
    }

    private void initView() {
        siteEdit = (EditText) findViewById(R.id.site_setup_edit);
        siteHelp = (ImageView) findViewById(R.id.site_help_contact);
        siteClear = (ImageView) findViewById(R.id.site_setup_clear);
        siteButton = (Button) findViewById(R.id.site_setup_button);
        siteHelpLayout = (LinearLayout) findViewById(R.id.site_help_first);
        siteHelpUpdate = (TextView) findViewById(R.id.site_help_update);
        siteErrMsg = (TextView) findViewById(R.id.site_tv_errmsg);
        textView2 = (TextView) findViewById(R.id.textView2);
        String date = StringUtil.dateToStrInLocale(new Date(System.currentTimeMillis()), "yyyy");
        textView2.setText(String.format(SiteSetupActivity.this.getResources().getString(R.string.about_mid5), date));
        if (isUpdateSite) {
            siteEdit.setText(FileUtil.readSharedPreferences(
                    SiteSetupActivity.this, Constants.SHARED_PREFERENCES, Constants.SITE));
            siteHelpLayout.setVisibility(View.GONE);
            siteHelpUpdate.setVisibility(View.VISIBLE);
        } else {
//			siteEdit.setHint(getResources().getString(R.string.site_hint));
            siteEdit.setText(getResources().getString(R.string.site_hint));
            siteHelpLayout.setVisibility(View.VISIBLE);
            siteHelpUpdate.setVisibility(View.GONE);
        }

        view = LayoutInflater.from(this).inflate(R.layout.progress_dialog, null);
        dialog = new Dialog(this, R.style.styleSiteCheckDialog);
        dialog.setContentView(view);
        preSite = FileUtil.readSharedPreferences(SiteSetupActivity.this, Constants.SHARED_PREFERENCES, Constants.SITE);

        autoAdapter = new AutoAdapter(this);
//		siteEdit.setThreshold(1);
        String tel = getResources().getString(R.string.tel);
//		if(tel.equals("0")){
//		}else {
//			siteEdit.setAdapter(autoAdapter);
//		}
//		siteEdit.setDropDownBackgroundResource(R.drawable.setup_input_normal);
//		siteEdit.setDropDownHorizontalOffset(1);
        siteEdit.addTextChangedListener(this);
        siteEdit.setOnFocusChangeListener(this);

        siteHelp.setOnClickListener(this);
        siteButton.setOnClickListener(this);
        siteClear.setOnClickListener(this);
    }

    /**
     * 显示提示框
     */
    private void showAlertDialog() {
        alertDialog = new AlertDialog.Builder(this);
        alertDialog.setCustomTitle(LayoutInflater.from(SiteSetupActivity.this).inflate(R.layout.notify_dialog, null));
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
                        FileUtil.saveSharedPreferences(SiteSetupActivity.this, Constants.SHARED_PREFERENCES,
                                Constants.SITE_NAME, "");
                        FileUtil.saveSharedPreferences(SiteSetupActivity.this, Constants.SHARED_PREFERENCES,
                                Constants.SITE_ID, "");
                        FileUtil.saveSharedPreferences(SiteSetupActivity.this, Constants.SHARED_PREFERENCES,
                                Constants.SITE, "");
                        FileUtil.saveSharedPreferences(SiteSetupActivity.this, Constants.SHARED_PREFERENCES,
                                Constants.HAS_LIVE_SERVER, "");
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void sendIntent() {
        FileUtil.saveSharedPreferences(SiteSetupActivity.this, Constants.SHARED_PREFERENCES, Constants.SITE, text);
        if (isUpdateSite) {
            if (!preSite.equals(text)) {
                logout();
            }
        } else {
            siteIntent = new Intent(SiteSetupActivity.this, ActHome.class);
            startActivity(siteIntent);
        }

        finish();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.site_setup_clear) {
            siteEdit.setText("");
        } else if (id == R.id.site_help_contact) {
            showLongToast(R.string.site_help_content);
        } else if (id == R.id.site_setup_button) {
            showErrMsg(-1);
            if (checkSite()) {
                siteHandler.sendEmptyMessage(DIALOG_SHOW);
                new Thread() {
                    public void run() {
                        Config.SiteName = Config.getSiteName(text);
                        if (Config.SiteName.equals("") || Config.SiteName == null) {
                            siteHandler.sendEmptyMessage(DIALOG_HIDE);
                        } else {
                            Config.Site_URL = text;
                            //保存SiteName、SiteId信息
                            FileUtil.saveSharedPreferences(SiteSetupActivity.this, Constants.SHARED_PREFERENCES,
                                    Constants.SITE_NAME, Config.SiteName);
                            FileUtil.saveSharedPreferences(SiteSetupActivity.this, Constants.SHARED_PREFERENCES,
                                    Constants.SITE_ID, Config.SiteId);
                            FileUtil.saveSharedPreferences(SiteSetupActivity.this, Constants.SHARED_PREFERENCES,
                                    Constants.SITE, Config.Site_URL);
                            FileUtil.saveSharedPreferences(SiteSetupActivity.this, Constants.SHARED_PREFERENCES,
                                    Constants.HAS_LIVE_SERVER, "" + Config.HAS_LIVE_SERVER);
                            if (text.equals(getResources().getString(R.string.site_hint))) {
                                siteHandler.sendEmptyMessage(PROGRESS_HIDE);
                            } else {
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
        text = siteEdit.getText().toString().trim();
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
        siteEdit.clearFocus();
        return true;

//		if (siteEdit.isFocused() && (siteEdit.getText().toString() == null || 
//				siteEdit.getText().toString().trim().equals(""))) {
//			showShortToast(R.string.site_error_null);
//			return false;
//		}else if(siteEdit.getText().toString().length() > 0 || siteEdit.isFocused()){
//			text = siteEdit.getText().toString().replaceAll("\\s", "");	
//		}else{
//			text = siteEdit.getHint().toString();		
//		}
//		siteEdit.clearFocus();
//		
//		//正则匹配
//		if (StringUtil.checkInput(text, Constants.SITE_URL) ||
//				StringUtil.checkInput(text, Constants.SITE_IP)) {
//			if(NetUtil.isNetworkConnected(SiteSetupActivity.this)){
//				return true;
//			}else{
//				showShortToast(R.string.site_error_net);
//			}
//		}else {
//			showShortToast(R.string.site_error_wrong);
//		}
//		
//		return false;
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
        if (siteEdit.getText().toString().length() > 0) {
            siteClear.setVisibility(View.VISIBLE);
            siteButton.setEnabled(true);
        } else if (siteEdit.isFocused()) {
            siteClear.setVisibility(View.GONE);
            siteButton.setEnabled(true);
//			siteButton.setEnabled(false);
        } else {
            siteClear.setVisibility(View.GONE);
            siteButton.setEnabled(true);
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus && (siteEdit.getText().toString() == null ||
                siteEdit.getText().toString().trim().equals(""))) {
//			siteButton.setEnabled(false);
            siteButton.setEnabled(true);
            siteClear.setVisibility(View.GONE);
        } else {
            siteButton.setEnabled(true);
            siteClear.setVisibility(View.VISIBLE);
        }

    }

    public String doEncode(String s) {
        String s1 = URLEncoder.encode(s);
        return s1;
    }

    private void showErrMsg(int resId) {
        if (siteErrMsg != null) {
            if (resId == -1) {
                siteErrMsg.setVisibility(View.GONE);
            } else {
                siteErrMsg.setText(getResources().getString(resId) + "!");
                siteErrMsg.setVisibility(View.VISIBLE);
            }
        }
    }
}
