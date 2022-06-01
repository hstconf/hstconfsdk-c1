package com.infowarelab.conference.ui.activity.preconf;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.infowarelab.conference.ConferenceApplication;
import com.infowarelab.conference.localDataCommon.ContactDataCommon;
import com.infowarelab.conference.localDataCommon.LocalCommonFactory;
import com.infowarelab.conference.localDataCommon.impl.ContactDataCommonImpl;
import com.infowarelab.conference.ui.activity.preconf.fragment.FragCreate;
import com.infowarelab.conference.ui.activity.preconf.fragment.FragJoin;
import com.infowarelab.conference.ui.activity.preconf.fragment.FragSet;
import com.infowarelab.conference.utils.PublicWay;
import com.infowarelab.conference.utils.SocketService;
import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.ConferenceCommonImpl;
import com.infowarelabsdk.conference.transfer.Config;
import com.infowarelabsdk.conference.util.Constants;
import com.infowarelabsdk.conference.util.FileUtil;

public class ActHome extends BaseFragmentActivity implements BaseFragment.onSwitchPageListener {
    private FragmentManager fragManager;
    private FragCreate fragCreate;
    private FragJoin fragJoin;
    private FragSet fragSet;
    private Button btnCreate;
    private Button btnJoin;
    private Button btnSet;
    private Button btnJoinPage1;
    private Button btnJoinPage2;
    private TextView tvTitleCreate;
    private TextView tvTitleSet;
    private LinearLayout llTitleJoin;
    private int currentFrag = 0;
    private boolean isSwitching;

    private Intent preconfIntent;
    public SharedPreferences preferences;
    private ContactDataCommon common;

    private String username;
    public static boolean isLogin;
    public static int position = 1;
    public static final int IS_LOGIN = 1;
    public static final int NOT_LOGIN = 0;
    public static final int FINISH = 2;


    private Handler homeHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NOT_LOGIN:
                    isLogin = false;

                    startInvitingService();

//				preconfLogin.setVisibility(View.VISIBLE);
                    break;
                case IS_LOGIN:

                    isLogin = true;

                    startInvitingService();

//				preconfLogin.setVisibility(View.GONE);
                    break;
                case FINISH:
                    finish();
                    break;
                default:
                    break;
            }
        }

        ;
    };
    private int userId = 0;


    @Override
    public void finish() {
        super.finish();
        //overridePendingTransition(0, 0);
    }

    protected void hideBottomUIMenu() {

        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            decorView.setSystemUiVisibility(uiOptions);

            // status bar is hidden, so hide that too if necessary.
            //ActionBar actionBar = getActionBar();
            //actionBar.hide();
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //hideBottomUIMenu();

        super.onCreate(savedInstanceState);

        System.gc();
        setContentView(R.layout.a6_preconf_home);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ConferenceApplication.setStatusBarColor(this, getColor(R.color.app_main_hue));
        }

        initView();
//		getContactsList();

        CommonFactory commonFactory = CommonFactory.getInstance();
        ConferenceCommonImpl conferenceCommon = (ConferenceCommonImpl) commonFactory.getConferenceCommon();
        //switchFrag(1);
        if (null != conferenceCommon) {
            if (true == conferenceCommon.mCreateConf) {
                switchFrag(1);
            } else
                switchFrag(2);
        }

    }

    private void startInvitingService() {

        int checkUserId = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFERENCES,
                getApplicationContext().MODE_PRIVATE).getInt(Constants.USER_ID, 0);

        if (checkUserId <= 0) {
            Log.d("Infowarelab.Debug", "ActHome.startInvitingService: NOT logined and stop service!");

            if (SocketService.isServiceStarted) {
                PublicWay.actionOnService(SocketService.Actions.STOP);
            }

            return;
        }

        if (SocketService.isServiceStarted) {
            Log.d("Infowarelab.Debug", "ActHome.startInvitingService: service is already started and ignored!");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                PublicWay.getIpAndPort(ActHome.this, Config.Site_URL,FileUtil.readSharedPreferences(ActHome.this,
                        Constants.SHARED_PREFERENCES, Constants.SITE_ID));

                Config.Site_URL = FileUtil.readSharedPreferences(ActHome.this,
                        Constants.SHARED_PREFERENCES, Constants.SITE);
                FileUtil.saveSharedPreferences(ActHome.this,
                        Constants.SHARED_PREFERENCES,
                        Constants.SITE_NAME, Config.getSiteName(Config.Site_URL));
                String id = FileUtil.readSharedPreferences(ActHome.this,
                        Constants.SHARED_PREFERENCES, Constants.SITE_ID);

                int userId = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFERENCES,
                        getApplicationContext().MODE_PRIVATE).getInt(Constants.USER_ID, 0);

                if (userId == 0) return;

                if (TextUtils.isEmpty(id)){
                    id = "0";
                }

                String loginName = FileUtil.readSharedPreferences(ActHome.this,
                        Constants.SHARED_PREFERENCES, Constants.LOGIN_NAME);

                if (loginName == null && loginName.length() <= 0) return;

//                if (Config.terminateRegist(DeviceIdFactory.getUUID1(ActHome.this), FileUtil.readSharedPreferences(ActHome.this,
//                        Constants.SHARED_PREFERENCES, Constants.LOGIN_NAME), Integer.parseInt(id),userId).equals("0")) {
//                    runOnUiThread(new Runnable(){
//                        @Override
//                        public void run() {
//                            Toast.makeText(ActHome.this, R.string.regist_success, Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                } else if (Config.terminateRegist(loginName/*DeviceIdFactory.getUUID1(ActHome.this)*/, loginName/*FileUtil.readSharedPreferences(ActHome.this,
//                        Constants.SHARED_PREFERENCES, Constants.LOGIN_JOINNAME)*/, Integer.parseInt(id),userId).equals("-1")) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(ActHome.this, R.string.regist_fail, Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                }
            }
        }).start();

    }

    @Override
    protected void onResume() {

        PublicWay.mActivity = this;

        isReLogin();

        if (PublicWay.mRinging){

           new Handler().post(new Runnable() {
                @Override
                public void run() {
                    PublicWay.showNotifyDialog();
                }
            });
        }

        super.onResume();
    }

    /**
     * 是否已有登录信息
     */
    private void isReLogin() {
        username = FileUtil.readSharedPreferences(this, Constants.SHARED_PREFERENCES, Constants.LOGIN_NAME);
        userId = FileUtil.readSharedPreferencesInt(this, Constants.SHARED_PREFERENCES, Constants.USER_ID);

        if (username.equals("") || userId <= 0) {
            isLogin = false;
            homeHandler.sendEmptyMessage(NOT_LOGIN);
        } else if (userId > 0){
            isLogin = true;
            homeHandler.sendEmptyMessage(IS_LOGIN);
        }
    }

    private void initView() {
        LocalCommonFactory.getInstance().setContactDataCommon(new ContactDataCommonImpl());
        common = LocalCommonFactory.getInstance().getContactDataCommon();

        fragManager = getSupportFragmentManager();
        btnCreate = (Button) findViewById(R.id.act_home_btn_bottom_1);
        btnJoin = (Button) findViewById(R.id.act_home_btn_bottom_2);
        btnSet = (Button) findViewById(R.id.act_home_btn_bottom_3);
        btnJoinPage1 = (Button) findViewById(R.id.act_home_btn_title_list_1);
        btnJoinPage2 = (Button) findViewById(R.id.act_home_btn_title_list_2);
        tvTitleCreate = (TextView) findViewById(R.id.act_home_tv_title_create);
        tvTitleSet = (TextView) findViewById(R.id.act_home_tv_title_set);
        llTitleJoin = (LinearLayout) findViewById(R.id.act_home_ll_title_list);
    }

    public void RadioClick(View v) {
        if (isSwitching)
            return;
        int id = v.getId();
        if (id == R.id.act_home_btn_bottom_1) {
            switchFrag(1);
        } else if (id == R.id.act_home_btn_bottom_2) {
            switchFrag(2);
        } else if (id == R.id.act_home_btn_bottom_3) {
            switchFrag(3);
        } else if (id == R.id.act_home_btn_title_list_1) {
            if (fragJoin != null) {
                btnJoinPage1.setBackgroundResource(R.drawable.a6_btn_left_semicircle_selected);
                btnJoinPage2.setBackgroundResource(R.drawable.a6_btn_right_semicircle_normal);
                fragJoin.switchPage(0);
            }
        } else if (id == R.id.act_home_btn_title_list_2) {
            if (fragJoin != null) {
                btnJoinPage1.setBackgroundResource(R.drawable.a6_btn_left_semicircle_normal);
                btnJoinPage2.setBackgroundResource(R.drawable.a6_btn_right_semicircle_selected);
                fragJoin.switchPage(1);
            }
        }
    }

    private void switchFrag(int which) {
        if (currentFrag == which)
            return;
//		if(which==1&&!isLogin){
//			jump2Login(FragSet.REQUESTCODE_LOGIN_CREATE);
//			return;
//		}
        isSwitching = true;
        FragmentTransaction ft;
        ft = fragManager.beginTransaction();

        if (fragCreate != null && fragCreate.isAdded())
            ft.hide(fragCreate);
        btnCreate.setBackgroundResource(R.drawable.a6_icon_home_create_normal);
        tvTitleCreate.setVisibility(View.GONE);

        if (fragJoin != null && fragJoin.isAdded())
            ft.hide(fragJoin);
        btnJoin.setBackgroundResource(R.drawable.a6_icon_home_list_normal);
        llTitleJoin.setVisibility(View.GONE);

        if (fragSet != null && fragSet.isAdded())
            ft.hide(fragSet);
        btnSet.setBackgroundResource(R.drawable.a6_icon_home_set_normal);
        tvTitleSet.setVisibility(View.GONE);

        switch (which) {
            case 1:
                btnCreate.setBackgroundResource(R.drawable.a6_icon_home_create_selected);
                tvTitleCreate.setVisibility(View.VISIBLE);
                if (fragCreate == null) {
                    fragCreate = new FragCreate();
                    fragCreate.setBaseFragmentActivity(this);
                    ft.add(R.id.act_home_fl_container, fragCreate, "Create");
                } else {
                    ft.show(fragCreate);
                }
                currentFrag = 1;
                break;
            case 2:
                btnJoin.setBackgroundResource(R.drawable.a6_icon_home_list_selected);
                llTitleJoin.setVisibility(View.VISIBLE);
                if (fragJoin == null) {
                    fragJoin = new FragJoin();
                    fragJoin.setOnSwitchPageListener(this);
                    fragJoin.setBaseFragmentActivity(this);
                    ft.add(R.id.act_home_fl_container, fragJoin, "Join");
                } else {
                    ft.show(fragJoin);
                }
                currentFrag = 2;
                break;
            case 3:
                btnSet.setBackgroundResource(R.drawable.a6_icon_home_set_selected);
                tvTitleSet.setVisibility(View.VISIBLE);
                if (fragSet == null) {
                    fragSet = new FragSet();
                    fragSet.setBaseFragmentActivity(this);
                    ft.add(R.id.act_home_fl_container, fragSet, "Contacts");
                } else {
                    ft.show(fragSet);
                }
                currentFrag = 3;
                break;

            default:
                btnSet.setBackgroundResource(R.drawable.a6_icon_home_set_selected);
                tvTitleSet.setVisibility(View.VISIBLE);
                if (fragSet == null) {
                    fragSet = new FragSet();
                    fragSet.setBaseFragmentActivity(this);
                    ft.add(R.id.act_home_fl_container, fragSet, "Contacts");
                } else {
                    ft.show(fragSet);
                }
                currentFrag = 3;
                break;
        }
        ft.commit();
        isSwitching = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (requestCode == FragSet.REQUESTCODE_LOGIN_CREATE) {
            if (resultCode == LoginActivity.RESULTCODE_LOGIN) {
                isReLogin();
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        switchFrag(1);
                    }
                });
            } else if (resultCode == LoginActivity.RESULTCODE_LOGOUT) {
                isReLogin();
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        switchFrag(2);
                    }
                });
            } else if (resultCode == RESULT_CANCELED) {

            }
        } else if (requestCode == FragSet.REQUESTCODE_LOGIN) {
            if (resultCode == LoginActivity.RESULTCODE_LOGIN) {
                isReLogin();
            } else if (resultCode == LoginActivity.RESULTCODE_LOGOUT) {
                isReLogin();
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        switchFrag(2);
                    }
                });
            }
        } else if (requestCode == ActOrganization.REQ_ORG) {
            if (resultCode == RESULT_OK && data != null) {
                String ids = data.getStringExtra("key1");
                String names = data.getStringExtra("key2");
                String emails = data.getStringExtra("key3");
                if (ids != null && !ids.equals("")) {
                    fragCreate.setAtts(ids, names, emails);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void jump2Login(int req) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("state", 1);
        intent.putExtra("turnIndex", 1);
        startActivityForResult(intent, req);
    }

    @Override
    public void onBackPressed() {

        if (fragJoin != null && fragJoin.getCurrentItem() == 1 && fragJoin.isEnterFromItem()) {
            fragJoin.setCurrentItem(0);
            fragJoin.setEnterFromItem(false);
            return;
        }
//		Intent i = new Intent(Intent.ACTION_MAIN);
//		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		i.addCategory(Intent.CATEGORY_HOME);
//		startActivity(i);
//		//退出应用后关闭所有在进程中运行的消息队列等
//		android.os.Process.killProcess(android.os.Process.myPid());
        super.onBackPressed();
    }

    /**
     * 获取当前设置成功的站点
     */
//	private void setCurrentSite(){
//		//log.info("current SITE = " +FileUtil.readSharedPreferences
//				(this, Constants.SHARED_PREFERENCES, Constants.SITE));
//		preconfCurrentsite.setText(FileUtil.readSharedPreferences
//				(this, Constants.SHARED_PREFERENCES, Constants.SITE));
//	}

    /**
     * 预先读取手机联系人的数据
     */
    private void getContactsList() {
        if (common == null) {
            common = LocalCommonFactory.getInstance().getContactDataCommon();
        }
        if (common.getContactList().isEmpty()) {
            //开启一个线程load当前手机客户端的联系人数据
            new Thread() {
                @Override
                public void run() {
                    common.getContacts(ActHome.this);
                }
            }.start();
        }
    }

    @Override
    public void doSelect(int postion) {
        // TODO Auto-generated method stub
        if (postion == 0) {
            btnJoinPage1.setBackgroundResource(R.drawable.a6_btn_left_semicircle_selected);
            btnJoinPage1.setTextColor(getResources().getColor(R.color.app_main_hue));
            btnJoinPage2.setBackgroundResource(R.drawable.a6_btn_right_semicircle_normal);
            btnJoinPage2.setTextColor(getResources().getColor(R.color.white));
        } else if (postion == 1) {
            btnJoinPage1.setBackgroundResource(R.drawable.a6_btn_left_semicircle_normal);
            btnJoinPage1.setTextColor(getResources().getColor(R.color.white));
            btnJoinPage2.setBackgroundResource(R.drawable.a6_btn_right_semicircle_selected);
            btnJoinPage2.setTextColor(getResources().getColor(R.color.app_main_hue));
        }
    }
}
