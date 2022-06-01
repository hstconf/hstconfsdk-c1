package com.infowarelab.conference.ui.activity.preconf.view;


import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.infowarelab.conference.ui.activity.inconf.ConferenceActivity;
import com.infowarelab.hongshantongphone.R;
import com.infowarelab.conference.ui.activity.preconf.ActHome;
import com.infowarelab.conference.ui.activity.preconf.BaseFragmentActivity;
import com.infowarelab.conference.ui.activity.preconf.LoginActivity;
import com.infowarelab.conference.ui.activity.preconf.fragment.FragJoin;
import com.infowarelab.conference.ui.adapter.ConferenceListAdapter4Frag;
import com.infowarelab.conference.ui.view.ConfListRefreshView4Frag;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.ConferenceCommonImpl;
import com.infowarelabsdk.conference.domain.ConferenceBean;
import com.infowarelabsdk.conference.domain.UserBean;
import com.infowarelabsdk.conference.transfer.Config;
import com.infowarelabsdk.conference.util.Constants;
import com.infowarelabsdk.conference.util.FileUtil;

public class ConferencePagerListView extends ConferencePager {

    private View conferenceListView;
    // 没有数据时进行显示
    private LinearLayout llNoMetting;
    private ImageView ivNoMetting;
    // 加载数据时显示
    private LinearLayout llLoading;
    private ImageView ivLoading;
    private Animation animation;
    // 下拉刷新列表
    private ConfListRefreshView4Frag lvRefresh;
    private ConferenceListAdapter4Frag mConfAdapter;

    // 保存登录信息
    private SharedPreferences preferences;
    private CommonFactory commonFactory = CommonFactory.getInstance();
    private List<ConferenceBean> conferences;
    private ConferenceBean conferenceBean;
    private Activity mActivity;
    private FragJoin fragJoin;
    private SharedPreferences mPreferences;
    private UserBean userBean = new UserBean();

    // 站点是否配置成功
    private static final String ISCONFIG = "isConfig";
    private static final int PUBLIC_CONFERENCE = 1;
    private static final int MY_CONFERENCE = 0;
    private int myConfsCount;
    private boolean isRefresh;

    public ConferencePagerListView(Activity activity, FragJoin fragJoin) {
        super(activity);
        this.mActivity = activity;
        this.fragJoin = fragJoin;
    }

    @Override
    public View getNewView() {
        getUserIsLogin();
        init();
        return conferenceListView;
    }

    private void init() {

        conferenceListView = mInflater.inflate(R.layout.a6_preconf_join_page_list, null);
        llNoMetting = (LinearLayout) conferenceListView.findViewById(R.id.view_frag_join_list_ll_nomeetting);
        ivNoMetting = (ImageView) conferenceListView.findViewById(R.id.view_frag_join_list_iv_2);
        lvRefresh = (ConfListRefreshView4Frag) conferenceListView.findViewById(R.id.view_frag_join_list_lv);
        llLoading = (LinearLayout) conferenceListView.findViewById(R.id.view_frag_join_list_ll);
        ivLoading = (ImageView) conferenceListView.findViewById(R.id.view_frag_join_list_iv_1);
        animation = AnimationUtils.loadAnimation(mActivity, R.anim.a6_anim_loading);
        LinearInterpolator lir = new LinearInterpolator();
        animation.setInterpolator(lir);
        //动画完成后，是否保留动画最后的状态，设为true
        animation.setFillAfter(true);

        preferences = mActivity.getPreferences(Activity.MODE_PRIVATE);
        getConfListData();
        setListViewRefreshListener(lvRefresh);

    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.ERROR_GET_SITENAME:
                    preferences.edit().putBoolean(ISCONFIG, false).commit();
                    showShortToast(mActivity.getString(R.string.error_prompt_siteName));
                    break;

                default:
                    break;
            }
        }

        ;
    };

    /**
     * 获取用户是否登录
     */
    private void getUserIsLogin() {
        mPreferences = mActivity.getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        int uid = mPreferences.getInt(Constants.USER_ID, 0);
        String username = mPreferences.getString(Constants.LOGIN_NAME, "");

        userBean.setUid(uid);
        userBean.setUsername(username);
    }

    /**
     * 开启子线程去获取会议列表
     */
    public void getConfListData() {
        showLoadingImg();
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                sortConference(userBean);
                //Log.d(TAG, "username=" + userBean.getUsername() + "password=" + userBean.getPassword());

                if (!ConferenceActivity.isLogin) {
                    listHandler.sendMessage(listHandler.obtainMessage(PUBLIC_CONFERENCE));
                } else {
                    listHandler.sendMessage(listHandler.obtainMessage(MY_CONFERENCE));
                }
            }
        }).start();
    }

    /**
     * 显示会议会议列表
     *
     * @param conferences
     */
    private void showConfListOrNoData(List<ConferenceBean> conferences) {
        if (conferences == null) {
            llNoMetting.setVisibility(View.GONE);
            if (mConfAdapter != null) {
                mConfAdapter.setAdapterData(conferences);
            } else {
                //这里需要创建一个adapter对象，不然addHeadView后将无法显示headView
                mConfAdapter = new ConferenceListAdapter4Frag(mActivity, fragJoin, conferences, 0);
            }
            //showShortToast(mActivity.getString(R.string.site_error_net));
        } else if (conferences.isEmpty()) {
            llNoMetting.setVisibility(View.VISIBLE);
            if (mConfAdapter != null) {
                mConfAdapter.setAdapterData(conferences);
            } else {
                //这里需要创建一个adapter对象，不然addHeadView后将无法显示headView
                mConfAdapter = new ConferenceListAdapter4Frag(mActivity, fragJoin, conferences, 0);
            }


        } else {
            llNoMetting.setVisibility(View.GONE);
            if (mConfAdapter == null) {
                mConfAdapter = new ConferenceListAdapter4Frag(mActivity, fragJoin, conferences, 0);
            } else {
                mConfAdapter.setAdapterData(conferences);
            }
        }
        lvRefresh.setTitle(LayoutInflater.from(mActivity).inflate(R.layout.meeting_list_page_title, lvRefresh, false));
        lvRefresh.setAdapter(mConfAdapter);

        llLoading.setVisibility(View.GONE);
        lvRefresh.setVisibility(View.VISIBLE);
    }

    private void setListViewRefreshListener(final ConfListRefreshView4Frag listView) {
        listView.setOnRefreshListener(new ConfListRefreshView4Frag.OnRefreshListener() {
            public void onRefresh() {
                isRefresh = true;
                refreshData(listView, conferences);
            }
        });
    }

    /**
     * 刷新列表数据
     */
    public void refreshData(final ConfListRefreshView4Frag listView, final List<ConferenceBean> preConferences) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                // TODO Auto-generated method stub
                showLoadingImg();
                super.onPreExecute();
            }

            protected Void doInBackground(Void... params) {
                if (preConferences == null || preConferences.isEmpty()) {
                    sortConference(userBean);
                } else {
                    sortConference(userBean);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                hideLoadingImg();
                //log.info("conferences:" + conferences.size());
                showConfListOrNoData(conferences);
                if (mConfAdapter != null) {
                    mConfAdapter.notifyDataSetChanged();
                    listView.onRefreshComplete();
                }
                isRefresh = false;
            }

        }.execute(null, null, null);
    }

    /**
     * 获取会议列表数据
     *
     * @param userBean
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void sortConference(UserBean userBean) {
        Config.SiteName = FileUtil.readSharedPreferences(mActivity, Constants.SHARED_PREFERENCES, Constants.SITE_NAME);
        Config.Site_URL = FileUtil.readSharedPreferences(mActivity, Constants.SHARED_PREFERENCES, Constants.SITE);
        Config.HAS_LIVE_SERVER = FileUtil.readSharedPreferences(mActivity, Constants.SHARED_PREFERENCES, Constants.HAS_LIVE_SERVER).equals("true");
        conferences = null;
        conferences = Config.getConferenceList(userBean, handler, 0);
        if (conferences != null) myConfsCount = conferences.size();
//        conferences = Config.getConferenceList(userBean, handler, 1);
//        if (ConferenceActivity.isLogin) {
//            List<ConferenceBean> myConfs = Config.getConferenceList(userBean, handler, 0);
//            conferences.addAll(0, myConfs);
//            myConfsCount = myConfs.size();
//        } else {
//            myConfsCount = 0;
//        }

//		conferences = Config.getConferenceList(userBean, handler, confListType);
//		//log.info("adapter" + conferences.siz e());
//		progressList = conferences.get(Config.PROGRESSCONFS);
//		confList = conferences.get(Config.CONFS);
//		conferences = new ArrayList<ConferenceBean>();
//		conferences.addAll(confList);
//		conferences.addAll(0, progressList);
        if (commonFactory.getConferenceCommon() != null) {
            ((ConferenceCommonImpl) commonFactory.getConferenceCommon()).setConfList(conferences);
        }
    }

    private Handler listHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PUBLIC_CONFERENCE:
                    hideLoadingImg();
                    showConfListOrNoData(conferences);
                    break;
                case MY_CONFERENCE:
                    hideLoadingImg();
                    showConfListOrNoData(conferences);
                default:
                    break;
            }
        }

        ;
    };

    public void setJoin(int position) {
        if (!isRefresh) {
            conferenceBean = (ConferenceBean) lvRefresh.getAdapter().getItem(position);
            if (conferenceBean.getNeedLogin() == 1 && !ConferenceActivity.isLogin) {
                Toast.makeText(mActivity, mActivity.getString(R.string.needLogin),
                        Toast.LENGTH_SHORT).show();

                //Intent intent = new Intent(mContext, LoginActivity.class);
                //intent.putExtra("turnIndex", 2);
                //intent.putExtra("state", 1);
                //mContext.startActivity(intent);
                //mActivity.finish();
            } else {
                fragJoin.enterFromItem(conferenceBean);
            }
            //log.info("conferenceId=" + conferenceBean.getId() + "needLogin=" + conferenceBean.getNeedLogin() + "password="
            //	+ conferenceBean.getConfPassword());
        }
    }

    public int getMyConfsCount() {
        return myConfsCount;
    }

    public void refreshAdapter() {
        if (mConfAdapter != null) {
            mConfAdapter.notifyDataSetChanged();
        }
    }

    private void showLoadingImg() {
        if (animation != null && ivLoading != null) {
            ivLoading.startAnimation(animation);
        }
    }

    private void hideLoadingImg() {
        if (ivLoading != null)
            ivLoading.clearAnimation();
    }

}
