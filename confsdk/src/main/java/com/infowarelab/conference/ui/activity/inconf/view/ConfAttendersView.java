package com.infowarelab.conference.ui.activity.inconf.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.Settings;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.infowarelab.conference.ConferenceApplication;
import com.infowarelab.conference.localDataCommon.ContactDataCommon;
import com.infowarelab.conference.localDataCommon.LocalCommonFactory;
import com.infowarelab.conference.localDataCommon.impl.ContactDataCommonImpl;
import com.infowarelab.conference.ui.activity.inconf.BaseFragment;
import com.infowarelab.conference.ui.adapter.AttendersAdapter;
import com.infowarelab.conference.ui.adapter.ChatContentAdapter;
import com.infowarelab.conference.ui.adapter.ContactsAdapter4et;
import com.infowarelab.conference.ui.adapter.SortGroupMemberAdapter;
import com.infowarelab.conference.ui.tools.CharacterParser;
import com.infowarelab.conference.ui.tools.GroupMemberBean;
import com.infowarelab.conference.ui.tools.PinyinComparator;
import com.infowarelab.conference.ui.tools.SideBar;
import com.infowarelab.conference.ui.view.AutoEdit;
import com.infowarelab.conference.ui.view.CanDragListView;
import com.infowarelab.conference.ui.view.SetHostDialog;
import com.infowarelab.conference.ui.view.SocializeDialog;
import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.ConferenceCommonImpl;
import com.infowarelabsdk.conference.common.impl.DocCommonImpl;
import com.infowarelabsdk.conference.common.impl.UserCommonImpl;
import com.infowarelabsdk.conference.confctrl.UserCommon;
import com.infowarelabsdk.conference.domain.ContactBean;
import com.infowarelabsdk.conference.domain.MessageBean;
import com.infowarelabsdk.conference.domain.UserBean;
import com.infowarelabsdk.conference.transfer.Config;
import com.infowarelabsdk.conference.util.Constants;
import com.infowarelabsdk.conference.util.FileUtil;
import com.infowarelabsdk.conference.util.NetUtil;
import com.infowarelabsdk.conference.util.StringUtil;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ConfAttendersView extends BaseFragment implements
        OnClickListener {
    public static final int EMAIL_FAILED_NOLOGIN = 60002;
    private View attendersView;
    private FrameLayout flRoot;

    private LinearLayout llAttenders;
    private CanDragListView listView;

    private TextView title;
    private LinearLayout llBack;
    private Button send;
    private EditText msgEt;
    private ListView msgList;
    private RelativeLayout layout;
    private ChatContentAdapter chatAdatper;

    private View view;


    private UserCommonImpl userCommon = (UserCommonImpl) commonFactory
            .getUserCommon();
    private DocCommonImpl docCommon = (DocCommonImpl) commonFactory
            .getDocCommon();
    private UserBean userChoosed;

    private AttendersAdapter attendersAdapter;
    private AlertDialog.Builder alertDialog;
    private static Dialog dialog;
    private boolean needSendEmail = false;
    private Handler userHandler;

    private int curPage = 1;//1.?????? 2.?????? 3.??????

    public ConfAttendersView(ICallParentView iCallParentView) {
        super(iCallParentView);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        attendersView = inflater.inflate(R.layout.conference_attenders, container, false);
        initView();
        resetUserHandler();
        return attendersView;
    }

    public void resetUserHandler() {
        initUserHandler();
        userCommon.setHandler(userHandler);
        callParentView(ACTION_SETPRIVITEGE, null);
    }

    private void initUserHandler() {
        // TODO Auto-generated method stub
        userHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UserCommon.CHAT_RECEIVE:
                        if (curPage == 1) {
                            // attendersAdapter.notifyDataSetChanged();//????????????????????????
                            attendersAdapter
                                    .update((LinkedList<UserBean>) userCommon
                                            .getUserArrayList().clone());
                        } else if (curPage == 2) {
                            MessageBean messageBean = (MessageBean) msg.obj;// ???????????????????????????
                            if (messageBean.getUid() == userChoosed.getUid()) {
                                chatAdatper.notifyDataSetChanged();
                                msgList.setSelection(chatAdatper.getCount());
                                // ??????????????????????????????
                                userCommon.setReadAllMessage(userChoosed.getUid());
                                userCommon.getUser4Phone(userChoosed.getUid())
                                        .setReadedMsg(true);
                            }
                        }

                    case UserCommon.ACTION_USER_ADD:
                        if (userCommon.getSelf().getRole() == UserCommon.ROLE_HOST) {
//						addContacts.setVisibility(View.VISIBLE);
                            docCommon.showAnnotationButton();
                            callParentView(ACTION_TRANSCHANNEL, "ACTION_USER_ADD");
                        }

                        // attendersAdapter.notifyDataSetChanged();
                        attendersAdapter.update((LinkedList<UserBean>) userCommon
                                .getUserArrayList().clone());
                        break;
                    case UserCommon.ACTION_USER_REMOVE:
                        // attendersAdapter.notifyDataSetChanged();
                        attendersAdapter.update((LinkedList<UserBean>) userCommon
                                .getUserArrayList().clone());


                        break;
                    case UserCommon.ACTION_USER_MODIFY:
                        if (userCommon.getSelf().getRole() == UserCommon.ROLE_HOST) {
//						addContacts.setVisibility(View.VISIBLE);
                            docCommon.showAnnotationButton();
                        }
//					activity.getVideoView().updateUserInfo();
                        attendersAdapter.update((LinkedList<UserBean>) userCommon
                                .getUserArrayList().clone());

                        if (userCommon.getSelf().getUid() == msg.arg1)
                            callParentView(ACTION_CHANGEAUDIOSTATE, msg.arg2);

                        break;
                    case UserCommon.ROLEUPDATE:
                        if ((Integer) msg.obj == userCommon.getOwnID()) {
                            callParentView(ACTION_ROLEUPDATE, null);
                            callPriviledge(ACTION_PRIVILEDGE_ATT, false);
//						if (userCommon.getSelf().getRole() == UserCommon.ROLE_HOST) {
//							addContacts.setVisibility(View.VISIBLE);
//						} else {
//							addContacts.setVisibility(View.GONE);
//						}
                        }
                        // attendersAdapter.notifyDataSetChanged();
                        attendersAdapter.update((LinkedList<UserBean>) userCommon
                                .getUserArrayList().clone());
                        break;
                    case UserCommon.CHAT_PRIVATE_ON:
                        if (userChoosed != null && userChoosed.getUid() == 0) {
                            return;
                        } else {
                            layout.setVisibility(View.VISIBLE);
                        }

                        break;
                    case UserCommon.CHAT_PRIVATE_OFF:
                        if (userChoosed != null && userChoosed.getUid() == 0) {
                            return;
                        } else {
                            layout.setVisibility(View.GONE);
                        }

                        break;
                    case UserCommon.CHAT_PUBLIC_ON:
                        if (userChoosed != null && userChoosed.getUid() == 0) {
                            layout.setVisibility(View.VISIBLE);
                        } else {
                            return;
                        }

                        break;
                    case UserCommon.CHAT_PUBLIC_OFF:
                        if (userChoosed != null && userChoosed.getUid() == 0) {
                            layout.setVisibility(View.GONE);
                        } else {
                            return;
                        }

                        break;
                    default:
                        break;
                }

            }

        };
    }


    /**
     * ????????????????????????
     */
    private void initView() {
        flRoot = (FrameLayout) attendersView.findViewById(R.id.attenders_fl);
        llAttenders = (LinearLayout) attendersView.findViewById(R.id.ll_inconf_attenders_list);
        listView = (CanDragListView) attendersView.findViewById(R.id.attenders_list);
        layout = (RelativeLayout) attendersView
                .findViewById(R.id.chat_editlayout);

        attendersAdapter = new AttendersAdapter(getActivity(),
                (LinkedList<UserBean>) userCommon.getUserArrayList().clone());
        listView.setAdapter(attendersAdapter);
        listView.setOnChangeListener(new CanDragListView.OnChanageHostListener() {
            @Override
            public void onChange(int uid) {
                if (uid == UserCommon.ALL_USER_ID || userCommon.getUser(uid).getRole() == UserCommon.ROLE_HOST) {
                    attendersAdapter.notifyDataSetChanged();
                } else {
                    SetHostDialog setHostDialog = new SetHostDialog(getActivity(), uid, ConferenceApplication.Screen_W * 4 / 5, new SetHostDialog.OnResultListener() {
                        @Override
                        public void doYes(int uid) {
                            if (!userCommon.isHost()) {
                                showShortToast(R.string.attenders_sethost_err1);
                            } else if (userCommon.getUser(uid).getUid() == 0) {
                                showShortToast(R.string.attenders_sethost_err2);
                            } else {
                                userCommon.setUser2Host(uid);
                                attendersAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void doNo() {
                            attendersAdapter.notifyDataSetChanged();
                        }
                    });
                    setHostDialog.show();
                }
            }

            @Override
            public void onClick(int uid) {
                if (uid == userCommon.getOwnID()) return;
                userChoosed = userCommon.getUser(uid);
                if (userChoosed.getDevice() == UserCommon.DEVICE_H323
                        || userChoosed.getDevice() == UserCommon.DEVICE_TELEPHONE) {
                    return;
                }
                userCommon.setCurrentChatingId(userChoosed.getUid());

                attendersView.findViewById(R.id.attenders_chat)
                        .setVisibility(View.VISIBLE);
//						Conference4PhoneActivity.position = Conference4PhoneActivity.ATTENDER_CHAT;
                curPage = 2;
                setPlace();
                initChatView();
            }
        });

        if (userCommon.getSelf() != null
                && userCommon.getSelf().getRole() == UserCommon.ROLE_HOST) {
//			addContacts.setVisibility(View.VISIBLE);
            callParentView(ACTION_SETPRIVITEGE, null);
        }

        setPlace();
    }

    /**
     * ?????????????????????
     */
    private void initChatView() {
        title = (TextView) attendersView.findViewById(R.id.chat_top);
        llBack = (LinearLayout) attendersView.findViewById(R.id.chat_back);
        msgList = (ListView) attendersView.findViewById(R.id.chat_list);
        msgEt = (EditText) attendersView.findViewById(R.id.chat_edit);
        send = (Button) attendersView.findViewById(R.id.chat_sendsms);
        String name = userChoosed.getUsername();
        name = cutName(name, 10);
        title.setText(getActivity().getResources().getString(R.string.attenders_ing)
                + name
                + getActivity().getResources().getString(R.string.attenders_chat));
        userCommon.setCurrentChatingId(userChoosed.getUid());
        if (userCommon.getMessageMap().get(userChoosed.getUid()) == null) {
            ArrayList<MessageBean> msg = new ArrayList<MessageBean>();
            userCommon.getMessageMap().put(userChoosed.getUid(), msg);
        }

        chatAdatper = new ChatContentAdapter(getActivity(), userCommon
                .getMessageMap().get(userChoosed.getUid()));
        msgList.setAdapter(chatAdatper);
        // ???????????????????????????????????????????????????
        msgList.setSelection(userCommon.getLastNotReadedMessage(userChoosed
                .getUid()));
        userCommon.setReadAllMessage(userChoosed.getUid());
        userCommon.getUser4Phone(userChoosed.getUid()).setReadedMsg(true);

        send.setOnClickListener(this);
        llBack.setOnClickListener(this);

        if (userChoosed.getUid() == UserCommon.ALL_USER_ID) {
            if (!userCommon.getPublicChatPriviledge()) {
                layout.setVisibility(View.GONE);
                showShortToast(R.string.not_public_chat_permission);
            } else {
                layout.setVisibility(View.VISIBLE);
            }
        } else {
            if (!userCommon.getPrivateChatPriviledge()) {
                layout.setVisibility(View.GONE);
                showShortToast(R.string.not_private_chat_permission);
            } else {
                layout.setVisibility(View.VISIBLE);
            }
        }

        msgEt.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (msgEt.isFocused()
                        && event.getAction() == MotionEvent.ACTION_UP) {
                    msgList.setSelection(userCommon
                            .getLastNotReadedMessage(userChoosed.getUid()));
                }
                return false;
            }

        });

    }


    /**
     * ??????????????????????????????
     */
    public void changeToAttendersView() {
        if (curPage == 3) {
            // ????????????????????????????????????????????????
            start = ContactDataCommonImpl.preStartIndex;
            end = ContactDataCommonImpl.preEndIndex;
            contactDel = inviteEdit.getContactFromSpan(start, end);
            common.setContactDel(contactDel);
            common.setMap(inviteEdit.getContacts());
            inviteEdit.clearContacts();
        }

        llAttenders.setVisibility(View.VISIBLE);
        attendersView.findViewById(R.id.attenders_invite).setVisibility(View.GONE);
        attendersView.findViewById(R.id.attenders_chat).setVisibility(View.GONE);

        curPage = 1;
        setPlace();

        userHandler.sendEmptyMessage(UserCommon.CHAT_RECEIVE);// ??????????????????????????????????????????
    }


    /**
     * ????????????
     */
    private void checkNet() {
        if (NetUtil.isNetworkConnected(getActivity())) {
            if (ConnectivityManager.TYPE_MOBILE == NetUtil
                    .getNetworkConnectedState(getActivity())) {
                showAlertDialog();
            }
        } else {
            showShortToast(R.string.site_error_net);
        }

    }

    /**
     * ?????????????????????wifi??????3G
     */
    private void showAlertDialog() {
        alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(getActivity().getResources().getString(
                R.string.attenders_wifi));
        alertDialog.setIcon(null);
        alertDialog.setMessage(getActivity().getResources().getString(
                R.string.attenders_tip));
        alertDialog.setPositiveButton(
                getActivity().getResources().getString(R.string.known),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.setNegativeButton(
                getActivity().getResources().getString(R.string.attenders_netsetup),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().startActivity(new Intent(
                                Settings.ACTION_WIFI_SETTINGS));
                        dialog.dismiss();
                    }
                });
        alertDialog.show();

    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_inconf_ctrl_att_add) {
            SocializeDialog dialog = new SocializeDialog(getActivity());
            dialog.setClickListener(new SocializeDialog.OnResultListener() {

                @Override
                public void doShare(int position) {
                    // TODO Auto-generated method stub
                    new SocializeTask(getActivity()).execute(position);
                }

                @Override
                public void doInvite() {
                    // TODO Auto-generated method stub
//					Conference4PhoneActivity.position = Conference4PhoneActivity.ATTENDER_INVITE;
                    curPage = 3;
                    setPlace();
                    attendersView.findViewById(R.id.attenders_invite).setVisibility(
                            View.VISIBLE);
                    llAttenders.setVisibility(View.GONE);// ??????????????????????????????????????????View???ListView???Item??????????????????
                    initInviteView();
                }
            });
            dialog.show(flRoot.getWidth());
        } else if (id == R.id.chat_sendsms) {
            sendMessage();
        } else if (id == R.id.chat_back) {
            changeToAttendersView();
        } else if (id == R.id.invite_back) {
            changeToAttendersView();
        } else if (id == R.id.conference_invite_confirmbutton) {// inviteEdit.fixLastClip();
            if (inviteEdit.clickConfirm()) {
                new MainFrameTask(getActivity()).execute();
            }
        } else if (id == R.id.tv_inconf_invite_tab1) {
            showCommonOrLocal(1);
        } else if (id == R.id.tv_inconf_invite_tab2) {
            showCommonOrLocal(2);
        }

    }

    /***************** ????????????????????? ******************/

    private AutoEdit inviteEdit;
    private TextView tvTab1, tvTab2, tvTab1Null, tvTab2Null;
    private FrameLayout flTab1, flTab2;
    private ListView inviteCommcontacts;
    private ListView lvTab2;
    private Button inviteConfirm;
    private ContactDataCommon common;
    private ContactsAdapter4et contactsAdapter;
    private ArrayList<ContactBean> list = new ArrayList<ContactBean>();
    private int start;
    private int end;
    private ContactBean contactDel;
    private Intent intent;
    private Config config;
    private String topic;
    private String confId;
    private String confPassword;
    private StringBuilder sms;
    private StringBuilder number;
    private LinearLayout llInviteBack;


    /**
     * ??????????????????????????????
     */
    private void initInviteView() {
        inviteEdit = (AutoEdit) attendersView
                .findViewById(R.id.et_inconf_invite);
        llInviteBack = (LinearLayout) attendersView.findViewById(R.id.invite_back);
        tvTab1 = (TextView) attendersView.findViewById(R.id.tv_inconf_invite_tab1);
        tvTab2 = (TextView) attendersView.findViewById(R.id.tv_inconf_invite_tab2);
        tvTab1Null = (TextView) attendersView.findViewById(R.id.inconf_invite_tv_tab1_no);
        tvTab2Null = (TextView) attendersView.findViewById(R.id.inconf_invite_tv_tab2_no);
        flTab1 = (FrameLayout) attendersView.findViewById(R.id.fl_inconf_invite_tab1);
        flTab2 = (FrameLayout) attendersView.findViewById(R.id.fl_inconf_invite_tab2);
        inviteCommcontacts = (ListView) attendersView
                .findViewById(R.id.inconf_invite_lv_tab1);
        lvTab2 = (ListView) attendersView
                .findViewById(R.id.inconf_invite_lv_tab2);
        inviteConfirm = (Button) attendersView
                .findViewById(R.id.conference_invite_confirmbutton);

        common = LocalCommonFactory.getInstance().getContactDataCommon();
        common.setHandler(inviteHandler);
        config = ((ConferenceCommonImpl) CommonFactory.getInstance()
                .getConferenceCommon()).getConfig();
        // inviteEdit.setInviteButton(inviteConfirm);
        if (!common.getMap().isEmpty()) {
            inviteConfirm.setEnabled(true);
        }

        view = LayoutInflater.from(getActivity()).inflate(R.layout.progress_dialog,
                null);
        dialog = new Dialog(getActivity(), R.style.styleSiteCheckDialog);
        dialog.setContentView(view);
        dialog.show();

        llInviteBack.setOnClickListener(this);
        tvTab1.setOnClickListener(this);
        tvTab2.setOnClickListener(this);
        inviteConfirm.setOnClickListener(this);
        // contactsAdapter = new ContactsAdapter(activity, list);
        contactsAdapter = new ContactsAdapter4et(getActivity(), list);
        inviteCommcontacts.setAdapter(contactsAdapter);


        common.getRecentContacts(getActivity());// ????????????????????????????????????
        initLocalContact();

        inviteEdit.setListview(inviteCommcontacts,
                contactsAdapter, lvTab2, adapterTab2, inviteConfirm);
    }

    private void showCommonOrLocal(int which) {
        if (which == 1) {
            if (flTab1.getVisibility() == View.VISIBLE) return;
            tvTab1.setBackgroundResource(R.drawable.a6_btn_underlinetab_down);
            tvTab2.setBackgroundResource(R.color.white);
            flTab1.setVisibility(View.VISIBLE);
            flTab2.setVisibility(View.GONE);
        } else {
            if (flTab2.getVisibility() == View.VISIBLE) return;
            tvTab2.setBackgroundResource(R.drawable.a6_btn_underlinetab_down);
            tvTab1.setBackgroundResource(R.color.white);
            flTab2.setVisibility(View.VISIBLE);
            flTab1.setVisibility(View.GONE);
        }
    }


    private Handler inviteHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            switch (msg.what) {
                case ContactDataCommon.GET_RECENTCONTACTS:
                    dialog.hide();
                    list = (ArrayList<ContactBean>) msg.obj;
                    if (null != list && list.size() > 0) {
                        tvTab1Null.setVisibility(View.GONE);
                        inviteCommcontacts.setVisibility(View.VISIBLE);
                        contactsAdapter.refreshAdapter(list);
                    } else {
                        tvTab1Null.setVisibility(View.VISIBLE);
                        inviteCommcontacts.setVisibility(View.GONE);
                    }
                    break;
                case ContactDataCommon.GET_CONTACTSLIST:
                    // inviteEdit.refresh();
                    break;
                case ContactDataCommon.EMAIL_SUCCESS:
                    showShortToast(R.string.emailSuccess);
                    break;
                case ContactDataCommon.EMAIL_FAILED:
                    showShortToast(R.string.emailFail);
                    break;
                case EMAIL_FAILED_NOLOGIN:
                    showShortToast(R.string.emailFailNoLogin);
                    break;

            }

        }

    };


    /**
     * ????????????????????????????????????????????????????????????
     **/
    private int lastFirstVisibleItem = -1;
    /**
     * ???????????????????????????
     **/
    private CharacterParser characterParser;
    /**
     * ?????????????????????ListView??????????????????
     **/
    private PinyinComparator pinyinComparator;
    private List<GroupMemberBean> SourceDateList;
    private static final String[] PHONES_PROJECTION = new String[]{
            Phone.DISPLAY_NAME, Phone.NUMBER, Phone.PHOTO_ID, Phone.CONTACT_ID};

    /**
     * ?????????????????????
     **/
    private static final int PHONES_DISPLAY_NAME_INDEX = 0;

    /**
     * ????????????
     **/
    private static final int PHONES_NUMBER_INDEX = 1;

    /**
     * ??????ID
     **/
    private static final int PHONES_PHOTO_ID_INDEX = 2;

    /**
     * ????????????ID
     **/
    private static final int PHONES_CONTACT_ID_INDEX = 3;
    private SideBar sideBar;
    private TextView tvDialog;
    private SortGroupMemberAdapter adapterTab2;
    private LinearLayout llTab2Title;
    private TextView tvTab2Title;

    private void initLocalContact() {
        llTab2Title = (LinearLayout) attendersView.findViewById(R.id.inconf_invite_ll_title);
        tvTab2Title = (TextView) attendersView.findViewById(R.id.inconf_invite_tv_catalog);
        // ???????????????????????????
        characterParser = CharacterParser.getInstance();
        pinyinComparator = new PinyinComparator();
        SourceDateList = new ArrayList<GroupMemberBean>();
        sideBar = (SideBar) attendersView.findViewById(R.id.inconf_invite_sidrbar);
        tvDialog = (TextView) attendersView.findViewById(R.id.inconf_invite_dialog);
        sideBar.setTextView(tvDialog);

        // ????????????????????????
        sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                // ??????????????????????????????
                int position = adapterTab2.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    lvTab2.setSelection(position);
                }

            }
        });
        getPhoneContacts();
        // ??????a-z?????????????????????
        Collections.sort(SourceDateList, pinyinComparator);
        adapterTab2 = new SortGroupMemberAdapter(getActivity(), SourceDateList);
        lvTab2.setAdapter(adapterTab2);
        lvTab2.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (SourceDateList.size() < 1) return;
                int section = getSectionForPosition(firstVisibleItem);
                if (firstVisibleItem != lastFirstVisibleItem) {
                    MarginLayoutParams params = (MarginLayoutParams) llTab2Title
                            .getLayoutParams();
                    params.topMargin = 0;
                    llTab2Title.setLayoutParams(params);
                    tvTab2Title.setText(SourceDateList.get(
                            getPositionForSection(section)).getSortLetters());
                }
                if (SourceDateList.size() < 2) return;
                int nextSection = getSectionForPosition(firstVisibleItem + 1);
                int nextSecPosition = getPositionForSection(+nextSection);
                if (nextSecPosition == firstVisibleItem + 1) {
                    View childView = view.getChildAt(0);
                    if (childView != null) {
                        int titleHeight = llTab2Title.getHeight();
                        int bottom = childView.getBottom();
                        MarginLayoutParams params = (MarginLayoutParams) llTab2Title
                                .getLayoutParams();
                        if (bottom < titleHeight) {
                            float pushedDistance = bottom - titleHeight;
                            params.topMargin = (int) pushedDistance;
                            llTab2Title.setLayoutParams(params);
                        } else {
                            if (params.topMargin != 0) {
                                params.topMargin = 0;
                                llTab2Title.setLayoutParams(params);
                            }
                        }
                    }
                }
                lastFirstVisibleItem = firstVisibleItem;
            }
        });

        if (SourceDateList.size() > 0) {
            tvTab2Null.setVisibility(View.GONE);
            lvTab2.setVisibility(View.VISIBLE);
            llTab2Title.setVisibility(View.VISIBLE);
        } else {
            tvTab2Null.setVisibility(View.VISIBLE);
            lvTab2.setVisibility(View.GONE);
            llTab2Title.setVisibility(View.GONE);
        }
    }

    /**
     * ??????ListView??????????????????????????????????????????Char ascii???
     */
    public int getSectionForPosition(int position) {
        return SourceDateList.get(position).getSortLetters().charAt(0);
    }

    /**
     * ???????????????????????????Char ascii????????????????????????????????????????????????
     */
    public int getPositionForSection(int section) {
        for (int i = 0; i < SourceDateList.size(); i++) {
            String sortStr = SourceDateList.get(i).getSortLetters();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return -1;
    }

    private void getPhoneContacts() {
        ContentResolver resolver = getActivity().getContentResolver();
        try {
            // ?????????????????????
            Cursor phoneCursor = resolver.query(Phone.CONTENT_URI,
                    PHONES_PROJECTION, null, null, null);
            if (phoneCursor != null) {

                while (phoneCursor.moveToNext()) {

                    // ??????????????????
                    String phoneNumber = phoneCursor
                            .getString(PHONES_NUMBER_INDEX).replace(" ", "");
                    // ?????????????????????????????????????????? ??????????????????
                    if (TextUtils.isEmpty(phoneNumber))
                        continue;

                    // ?????????????????????
                    String contactName = phoneCursor
                            .getString(PHONES_DISPLAY_NAME_INDEX);

                    // ???????????????ID
                    Long contactid = phoneCursor
                            .getLong(PHONES_CONTACT_ID_INDEX);

                    // ?????????????????????ID
                    Long photoid = phoneCursor.getLong(PHONES_PHOTO_ID_INDEX);

                    GroupMemberBean sortModel = new GroupMemberBean();
                    sortModel.setName(contactName);
                    sortModel.setPhoneNumber(phoneNumber);
                    // ?????????????????????
                    String pinyin = characterParser.getSelling(contactName);
                    String sortString = pinyin.substring(0, 1).toUpperCase();

                    // ??????????????????????????????????????????????????????
                    if (sortString.matches("[A-Z]")) {
                        sortModel.setSortLetters(sortString.toUpperCase());
                    } else {
                        sortModel.setSortLetters("#");
                    }
                    SourceDateList.add(sortModel);
                    // mContacts.add(mContact);
                }
                phoneCursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * ?????????????????????????????????
     */
    private void makeMessage() {
        String result = "";
        if (config.getConfigBean() == null
                || config.getConfigBean().getConfInfo_m_confName() == null) {
            topic = config.getMyConferenceBean().getName();
            confId = config.getMyConferenceBean().getId();
            confPassword = config.getMyConferenceBean().getConfPassword();
            result = Config.getShortUrl(confId, "4054563117");
        } else {
            topic = config.getConfigBean().getConfInfo_m_confName();
            confId = config.getConfigBean().getCourseNum();
            confPassword = config.getConfigBean().getConfInfo_m_confPassword();
            result = Config.getShortUrlByToken(confId, "4054563117", config.getConfigBean().getConfInfo_m_token());
        }
        Log.i("makeMessage", "makeMessage=" + result);
        sms = new StringBuilder();
        sms.append("???")
                .append(userCommon.getSelf().getUsername().trim())
                .append("???")
                .append(getActivity().getResources().getString(
                        R.string.attenders_invite))
                .append("\n")
                .append(getActivity().getResources().getString(
                        R.string.attenders_url))
                .append("( ")
                .append(result)
                .append(" )")
                .append(getActivity().getResources().getString(
                        R.string.attenders_enter));
        sms.append(getActivity().getResources().getString(
                R.string.attenders_end));
    }

    private void sendMessage() {
        final String message = msgEt.getText().toString().trim();
        if (message.length() > 0) {
            try {
                UserBean userbean = userCommon.getUser(userChoosed.getUid());
                boolean isPubilc = true;
                int toUid = UserCommon.ALL_USER_ID;
                if (userbean != null
                        && userbean.getUid() != UserCommon.ALL_USER_ID) {
                    toUid = userbean.getUid();
                    isPubilc = false;
                }
                System.out.println(message);

                MessageBean messageBean = new MessageBean();
                messageBean.setDate(StringUtil
                        .dateToStr(new Date(), "HH:mm:ss"));
                messageBean.setMessage(message);
                messageBean.setUsername(userCommon.getSelf().getUsername());
                messageBean.setPublic(isPubilc);
                messageBean.setUid(toUid);
                userCommon.sortMessageByTime(messageBean);// ?????????????????????????????????????????????
                CommonFactory.getInstance().getChatCommom()
                        .chatSendMsg(messageBean);

            } catch (Exception e) {
                e.printStackTrace();
            }

            chatAdatper.notifyDataSetChanged();
            msgEt.setText("");
            msgList.setSelection(chatAdatper.getCount());
        } else {
            showShortToast(R.string.attenders_chat_msgnull);
        }
    }

    public boolean getOnBackPressed() {
        if (curPage != 1) {
            changeToAttendersView();
            return false;
        } else {
            return true;
        }
    }

    public class MainFrameTask extends AsyncTask<Integer, String, Integer> {

        private Activity activity = null;
        private LinkedHashMap<Object, ContactBean> mapContacts;

        public MainFrameTask(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected void onCancelled() {
            dialog.dismiss();
            super.onCancelled();
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            makeMessage();

            common.setMap(mapContacts);
            Iterator iterator = common.getMap().entrySet().iterator();
            StringBuilder eNames = new StringBuilder();
            StringBuilder emails = new StringBuilder();
            StringBuilder ePhoneNumers = new StringBuilder();
            StringBuilder eUserIds = new StringBuilder();
            ContactBean contactBean = null;
            number = new StringBuilder();
            needSendEmail = false;

            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                contactBean = (ContactBean) entry.getValue();
                if (StringUtil.checkInput(
                        entry.getKey().toString().replaceAll("-", "").trim(),
                        Constants.EDIT_PHONENUMBER)) {// ????????????????????????-????????????????????????????????????????????????
                    number.append(entry.getKey().toString() + ";");
                    eNames.append(contactBean.getName()).append(";");
                    emails.append(contactBean.getEmail()).append(";");
                    ePhoneNumers.append(contactBean.getPhoneNumber()).append(
                            ";");
                    if (contactBean.getId() == null
                            || contactBean.getId().equals("")) {
                        contactBean.setId("");
                    }
                    eUserIds.append(contactBean.getId()).append(";");
                } else if (StringUtil.checkInput(contactBean.getPhoneNumber(),
                        Constants.EDIT_PHONENUMBER)) {// ????????????????????????-????????????????????????????????????????????????
                    number.append(contactBean.getPhoneNumber() + ";");
                    eNames.append(contactBean.getName()).append(";");
                    emails.append(contactBean.getEmail()).append(";");
                    ePhoneNumers.append(contactBean.getPhoneNumber()).append(
                            ";");
                    if (contactBean.getId() == null
                            || contactBean.getId().equals("")) {
                        contactBean.setId("");
                    }
                    eUserIds.append(contactBean.getId()).append(";");
                } else if (StringUtil.checkInput(entry.getKey().toString(),
                        Constants.EDIT_EMAIL)) {
                    if (contactBean.getName().contains("@")) {
                        int index = contactBean.getName().indexOf("@");
                        contactBean.setName(contactBean.getName().substring(0,
                                index));
                    }
                    needSendEmail = true;
                    eNames.append(contactBean.getName()).append(";");
                    emails.append(contactBean.getEmail()).append(";");
                    ePhoneNumers.append(contactBean.getPhoneNumber()).append(
                            ";");
                    if (contactBean.getId() == null
                            || contactBean.getId().equals("")) {
                        contactBean.setId("0");
                    }
                    eUserIds.append(contactBean.getId()).append(";");
                } else if (StringUtil.checkInput(contactBean.getEmail(),
                        Constants.EDIT_EMAIL)) {
                    if (contactBean.getName().contains("@")) {
                        int index = contactBean.getName().indexOf("@");
                        contactBean.setName(contactBean.getName().substring(0,
                                index));
                    }
                    needSendEmail = true;
                    eNames.append(contactBean.getName()).append(";");
                    emails.append(contactBean.getEmail()).append(";");
                    ePhoneNumers.append(contactBean.getPhoneNumber()).append(
                            ";");
                    if (contactBean.getId() == null
                            || contactBean.getId().equals("")) {
                        contactBean.setId("0");
                    }
                    eUserIds.append(contactBean.getId()).append(";");
                }

            }

            if (contactBean != null) {
                String result = Config.createAttendees(activity,
                        eNames.toString(), emails.toString(),
                        ePhoneNumers.toString(), eUserIds.toString());

                if (!needSendEmail) {
                    return null;
                }

                if (result.equals("0")) {
                    inviteHandler
                            .sendEmptyMessage(ContactDataCommon.EMAIL_SUCCESS);
                } else {
                    String username = FileUtil.readSharedPreferences(activity,
                            Constants.SHARED_PREFERENCES, Constants.LOGIN_NAME);
                    if (username.equals("")) {
                        inviteHandler
                                .sendEmptyMessage(EMAIL_FAILED_NOLOGIN);
                    } else {
                        inviteHandler
                                .sendEmptyMessage(ContactDataCommon.EMAIL_FAILED);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            dialog.show();
            mapContacts = inviteEdit.getContacts();
        }

        @Override
        protected void onPostExecute(Integer result) {
            dialog.dismiss();
            // ???????????????????????????????????????????????????????????????
            common.getMap().clear();
            inviteEdit.setText("");

            String str = number.toString().trim();
            if (!str.equals("")) {
                intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"
                        + str.substring(0, str.length() - 1)));
                intent.putExtra("sms_body", sms.toString());
                activity.startActivity(intent);
            }

            // ????????????????????????????????????????????????
//			Conference4PhoneActivity.position = Conference4PhoneActivity.CONF_CTRL;
            curPage = 1;
            changeToAttendersView();
        }

    }

    private UMShareListener shareListener = new UMShareListener() {
        /**
         * @descrption ?????????????????????
         * @param platform ????????????
         */
        @Override
        public void onStart(SHARE_MEDIA platform) {
            Log.i("shareListener", "shareListener onStart");
        }

        /**
         * @descrption ?????????????????????
         * @param platform ????????????
         */
        @Override
        public void onResult(SHARE_MEDIA platform) {
            Log.i("shareListener", "shareListener onResult");
            showShortToast(R.string.socialize_11);
        }

        /**
         * @descrption ?????????????????????
         * @param platform ????????????
         * @param t ????????????
         */
        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
            Log.i("shareListener", "shareListener onError");
            showShortToast(getActivity().getResources().getString(R.string.socialize_12) + t.getMessage());
        }

        /**
         * @descrption ?????????????????????
         * @param platform ????????????
         */
        @Override
        public void onCancel(SHARE_MEDIA platform) {
            showShortToast(R.string.socialize_13);

        }
    };


    public class SocializeTask extends AsyncTask<Integer, Integer, ShareAction> {

        private Activity activity = null;

        public SocializeTask(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected void onCancelled() {
            dialog.dismiss();
            super.onCancelled();
        }

        @Override
        protected ShareAction doInBackground(Integer... params) {
            config = ((ConferenceCommonImpl) CommonFactory.getInstance()
                    .getConferenceCommon()).getConfig();
            String result = "";
            if (config.getConfigBean() == null
                    || config.getConfigBean().getConfInfo_m_confName() == null) {
                topic = config.getMyConferenceBean().getName();
                confId = config.getMyConferenceBean().getId();
                confPassword = config.getMyConferenceBean().getConfPassword();
                result = Config.getShortUrl(confId, "4054563117");
            } else {
                topic = config.getConfigBean().getConfInfo_m_confName();
                confId = config.getConfigBean().getCourseNum();
                confPassword = config.getConfigBean().getConfInfo_m_confPassword();
                result = Config.getShortUrlByToken(confId, "4054563117", config.getConfigBean().getConfInfo_m_token());
            }
            StringBuilder sb = new StringBuilder();
            sb.append("???")
                    .append(userCommon.getSelf().getUsername().trim())
                    .append("???")
                    .append(activity.getResources().getString(
                            R.string.attenders_invite))
                    .append("\n")
                    .append(activity.getResources().getString(
                            R.string.attenders_url))
                    .append("( ")
                    .append(result)
                    .append(" )")
                    .append(activity.getResources().getString(
                            R.string.attenders_enter));
            sb.append(activity.getResources().getString(
                    R.string.attenders_end));
            UMWeb web = new UMWeb(result);
            web.setTitle(activity.getResources().getString(
                    R.string.socialize_10));//??????
            web.setThumb(new UMImage(getActivity(), R.drawable.icon));
            web.setDescription(sb.toString());//??????

            ShareAction shareAction = null;
            switch (params[0]) {
                case 1://weixin
                    shareAction = new ShareAction(activity);
                    shareAction
                            .setPlatform(SHARE_MEDIA.WEIXIN)//????????????
                            .withMedia(web)
                            .setCallback(shareListener);
                    break;
                case 2:
                    shareAction = new ShareAction(activity);
                    shareAction
                            .setPlatform(SHARE_MEDIA.WEIXIN_CIRCLE)//????????????
                            .withMedia(web)
                            .setCallback(shareListener);
                    break;
                case 3:
                    shareAction = new ShareAction(activity);
                    shareAction
                            .setPlatform(SHARE_MEDIA.QQ)//????????????
                            .withMedia(web)
                            .setCallback(shareListener);
                    break;
                case 5:
                    shareAction = new ShareAction(activity);
                    shareAction
                            .setPlatform(SHARE_MEDIA.DINGTALK)//????????????
                            .withMedia(web)
                            .setCallback(shareListener);
                    break;

                default:
                    break;
            }
            return shareAction;
        }

        @Override
        protected void onPreExecute() {
            view = LayoutInflater.from(activity).inflate(R.layout.progress_dialog,
                    null);
            dialog = new Dialog(activity, R.style.styleSiteCheckDialog);
            dialog.setContentView(view);
            dialog.show();
        }

        @Override
        protected void onPostExecute(ShareAction result) {
            dialog.dismiss();
            if (result != null) {
                result.share();
            }

        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            setPlace();
        }
    }

    private void setPlace() {
        if (curPage == 1) {
            callShowBars();
        } else {
            callHideBars();
        }
    }

    public void changeOrietation(Configuration newConfig) {
        if (curPage == 2 || curPage == 3) setParentBars(false);
//		if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
//			placeTop.setVisibility(View.VISIBLE);
//			placeBottom.setVisibility(View.VISIBLE);
//		}else if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
//			placeTop.setVisibility(View.GONE);
//			placeBottom.setVisibility(View.GONE);
//		}
    }

    private void setParentBars(boolean isShow) {
        if (isShow) {
            callShowBars();
        } else {
            callHideBars();
        }
    }

    public void gotoPublishChattingView(){
        if (0 == userCommon.getOwnID()) return;
        userChoosed = userCommon.getUser(0);
        if (userChoosed.getDevice() == UserCommon.DEVICE_H323
                || userChoosed.getDevice() == UserCommon.DEVICE_TELEPHONE) {
            return;
        }
        userCommon.setCurrentChatingId(userChoosed.getUid());

        attendersView.findViewById(R.id.attenders_chat)
                .setVisibility(View.VISIBLE);
//						Conference4PhoneActivity.position = Conference4PhoneActivity.ATTENDER_CHAT;
        curPage = 2;
        setPlace();
        initChatView();
    }
}
