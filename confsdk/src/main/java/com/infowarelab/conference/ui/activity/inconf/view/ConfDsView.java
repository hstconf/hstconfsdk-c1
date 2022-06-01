package com.infowarelab.conference.ui.activity.inconf.view;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.infowarelab.conference.ConferenceApplication;
import com.infowarelab.conference.ui.activity.inconf.BaseFragment;
import com.infowarelab.conference.ui.activity.inconf.ImageActivity;
import com.infowarelab.conference.ui.activity.inconf.view.share.DocView4Phone;
import com.infowarelab.conference.ui.view.AddDocDialog;
import com.infowarelab.conference.ui.view.PageViewDs;
import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.ConferenceCommonImpl;
import com.infowarelabsdk.conference.common.impl.DocCommonImpl;
import com.infowarelabsdk.conference.common.impl.UserCommonImpl;
import com.infowarelabsdk.conference.confctrl.UserCommon;
import com.infowarelabsdk.conference.domain.AnnotationBean;
import com.infowarelabsdk.conference.domain.AnnotationType;
import com.infowarelabsdk.conference.domain.DocBean;
import com.infowarelabsdk.conference.domain.EraserBean;
import com.infowarelabsdk.conference.shareDoc.DocCommon;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.List;

public class ConfDsView extends BaseFragment implements OnClickListener, OnTouchListener {
    private View view;

    private RelativeLayout rlRoot;
    private View placeTop, placeBottom;

    private PageViewDs pvDocs;
    private LinearLayout llLastDoc, llNextDoc, llCurDoc;
    private FrameLayout flCurDoc;
    private LinearLayout llPageTurn;
    private Button btnLast, btnNext;

    private DocView4Phone shareView;
    private ImageView ivAnntools;
    private TextView tvPageProgress;

    private LinearLayout shareNoDoc;

    private LinearLayout llBottom;

    private DocCommonImpl docCommon;
    private UserCommonImpl userCommon;
    private DocBean curDoc;
    private List<DocBean> docMap;
    private ActiveDocid activeDocid;

    //    private int orientation = Configuration.ORIENTATION_PORTRAIT;
    private static final int DOC_DISMISS_MENU = 2001;
    private static final int DOC_DISMISS_LIST = 2002;
    /**
     * 用来与外部activity交互的
     */
    private FragmentInteraction listterner;
    //是否有桌面分享权限
    private boolean bAS = false;
    private FrameLayout flShowData;

    public void setbAS(boolean bAS) {
        this.bAS = bAS;
    }

    public ConfDsView(ICallParentView iCallParentView) {
        super(iCallParentView);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("InfowareLab.Debug", "ConfDsView.onCreateView isAdded=" + isAdded() + " isVisibleToUser=" + getUserVisibleHint());
        view = inflater.inflate(R.layout.conference_shareview_phone, container, false);
        initView();
        docCommon.setHandler(docHandler);
        initAnnoationAction();
        return view;
    }

    @Override
    public void onDestroyView() {
        Log.d("InfowareLab.Debug", "ConfDsView.onDestroyView");
        //if (null != docCommon) docCommon.setHandler(null);
        super.onDestroyView();
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        //Log.i("Fragment", "Fragment 2 setUserVisibleHint " + isVisibleToUser + " isAdded=" + isAdded());
        Log.d("InfowareLab.DS", "ConfDsView.setUserVisibleHint =" + isVisibleToUser);

        super.setUserVisibleHint(isVisibleToUser);

        if (!isAdded()) return;
        if (!isVisibleToUser) {
            doHideView();
        } else {
            callParentView(ACTION_SHARE2DS, null);
            doShowView(true, true);
        }
    }


    Handler dsHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            //Log.d("InfowareLab.Debug", "ConfDsView.handleMessage");
            doShowDoc();
        }
    };

    public void doHideView() {

        Log.d("InfowareLab.DS", "doHideView");

        shareNoDoc.setVisibility(View.GONE);
        llCurDoc.setVisibility(View.VISIBLE);
    }


    public void doShowView(boolean hasCorrect, boolean isScrollIn) {

        Log.d("InfowareLab.DS", "doShowView: hasCorrect=" + hasCorrect + "; isScrollIn=" + isScrollIn);

        if (!isAdded()) return;
        if (hasCorrect) {
            setPlace4Orientation(getOrientationState());
            setPagesSize(getOrientationState());
            setBottomAnn(this.isShowAnn, this.isBarsShow);
        }
        if (isScrollIn) {
            docMap = docCommon.getDocMapList();

            Log.d("InfowareLab.DS", "doShowView: docCommon.getDocMapList():" + docMap.size());

            if (docMap != null && !docMap.isEmpty()) {

                curDoc = docCommon.getCurrentDoc();

                if (curDoc != null)
                    Log.d("InfowareLab.DS", "doShowView: curDoc: " + curDoc.getTitle() + "; " + curDoc.getDocID());

                if (curDoc == null) {
                    curDoc = docMap.get(0);
                    Log.d("InfowareLab.DS", "doShowView: switchDoc the first: " + docMap.get(0).getTitle() + "; " + docMap.get(0).getDocID());
                    docCommon.switchDoc(docMap.get(0).getDocID());
                    return;
                }
                else if (curDoc != null && curDoc.getDocID() != docMap.get(0).getDocID()){
                    if (userCommon.getSelf().getRole() == UserCommon.ROLE_HOST) {

                        Log.d("InfowareLab.DS", "doShowView: switchDoc to first(HOST): " + docMap.get(0).getTitle() + "; " + docMap.get(0).getDocID());
                        docCommon.switchDoc(docMap.get(0).getDocID());
                    }
                }
            } else {
                Log.d("InfowareLab.DS", "doShowView: curDoc is NULL!");
                curDoc = null;
            }
        } else {
            curDoc = docCommon.getCurrentDoc();
            if (curDoc != null)
                Log.d("InfowareLab.DS", "doShowView(No scroll in): curDoc: " + curDoc.getTitle() + "; " + curDoc.getDocID());
        }
        dsHandler.sendEmptyMessage(0);
    }


    private void doShowDoc() {

        Log.d("InfowareLab.DS", "ConfDsView.doShowDoc()");

        if (!isAdded()) return;
        if (curDoc == null) {
            curDoc = docCommon.getCurrentDoc();
        }
        if (curDoc != null) {
            setCurHSV(curDoc.getDocID());

            if (curDoc.getPage() != null) {
                tvPageProgress.setText(curDoc.getPage().getPageID() + "/" + curDoc.getPageCount());
            }

            shareView.setOnTouchListener(shareView);
            shareView.setDoc(curDoc, getOrientationState());

            shareView.setVisibility(View.VISIBLE);

            if (!getUserVisibleHint()) {
                llCurDoc.setVisibility(View.VISIBLE);
            }

            showDocHandler.sendEmptyMessage(0);
            docCommon.setStartToShowPage(false);
            if (!isHidden())
                doTransChannel();

            beginCloudRecord();

            shareNoDoc.setVisibility(View.GONE);
        } else {
            shareNoDoc.setVisibility(View.VISIBLE);
        }
    }


    private void setCurHSV(int docid) {
        activeDocid.setEmpty();
        docMap = docCommon.getDocMapList();

        Log.d("InfowareLab.DS", "setCurHSV: docid=" + docid);

        if (docMap != null && !docMap.isEmpty()) {
            for (DocBean doc : docMap) {
                if (activeDocid.curId != 0) {
                    activeDocid.nextId = doc.getDocID();
                    break;
                } else if (doc.getDocID() == docid) {
                    activeDocid.curId = doc.getDocID();
                } else {
                    activeDocid.lastId = doc.getDocID();
                }
            }
        }
        if (activeDocid.curId == 0) {
            shareNoDoc.setVisibility(View.VISIBLE);
        } else if (activeDocid.lastId == 0 && activeDocid.nextId == 0) {
            llLastDoc.setVisibility(View.GONE);
            llNextDoc.setVisibility(View.GONE);
            llCurDoc.setVisibility(View.VISIBLE);
            Log.d("InfowareLab.DS", "pvDocs.setScreenWidth(1): w1=" + getParentW(getOrientationState()));
            pvDocs.setScreenWidth(getParentW(getOrientationState()), 1);
        } else if (activeDocid.lastId == 0) {
            llLastDoc.setVisibility(View.GONE);
            llNextDoc.setVisibility(View.VISIBLE);
            llCurDoc.setVisibility(View.VISIBLE);
            Log.d("InfowareLab.DS", "pvDocs.setScreenWidth(2): w2=" + getParentW(getOrientationState()));
            pvDocs.setScreenWidth(getParentW(getOrientationState()), 2);
            pvDocs.setCurPage(1);
            pvDocs.requestLayout();
            //pvDocs.requestChildFocus(flCurDoc,flCurDoc);
        } else if (activeDocid.nextId == 0) {
            llLastDoc.setVisibility(View.VISIBLE);
            llNextDoc.setVisibility(View.GONE);
            llCurDoc.setVisibility(View.VISIBLE);
            Log.d("InfowareLab.DS", "pvDocs.setScreenWidth(2): w3=" + getParentW(getOrientationState()));
            pvDocs.setScreenWidth(getParentW(getOrientationState()), 2);
            pvDocs.setCurPage(2);
            pvDocs.requestLayout();
            //pvDocs.requestChildFocus(flCurDoc,flCurDoc);
        } else {
            llLastDoc.setVisibility(View.VISIBLE);
            llCurDoc.setVisibility(View.VISIBLE);
            llNextDoc.setVisibility(View.VISIBLE);
            Log.d("InfowareLab.DS", "pvDocs.setScreenWidth(3): w=" + getParentW(getOrientationState()));
            pvDocs.setScreenWidth(getParentW(getOrientationState()), 3);
            pvDocs.setCurPage(2);
            pvDocs.requestLayout();
            //pvDocs.requestChildFocus(flCurDoc,flCurDoc);
        }

    }

    Handler showDocHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            llCurDoc.setVisibility(View.GONE);
            if (curDoc != null && curDoc.getPage() != null && curDoc.getPageCount() > 1 && shareView.getAnnTool() == DocView4Phone.ToolEnum.TOUCH) {
                llPageTurn.setVisibility(View.VISIBLE);
            } else {
                llPageTurn.setVisibility(View.GONE);
            }
            if ((curDoc != null && curDoc.isLocal()) || userCommon.getSelf().getRole() == UserCommon.ROLE_HOST) {
                tvDel.setVisibility(View.VISIBLE);
            } else {
                tvDel.setVisibility(View.INVISIBLE);
            }
        }
    };


    private void initView() {
        activeDocid = new ActiveDocid();
        docCommon = (DocCommonImpl) CommonFactory.getInstance().getDocCommon();
        userCommon = (UserCommonImpl) CommonFactory.getInstance().getUserCommon();

        rlRoot = (RelativeLayout) view.findViewById(R.id.share_root);
        placeTop = view.findViewById(R.id.view_inconf_ds_place_top);
        placeBottom = view.findViewById(R.id.view_inconf_ds_place_bottom);

        shareNoDoc = (LinearLayout) view.findViewById(R.id.shareNoDoc);

        flShowData = (FrameLayout) view.findViewById(R.id.showData);
        pvDocs = (PageViewDs) view.findViewById(R.id.pv_inconf_ds);
        llLastDoc = (LinearLayout) view.findViewById(R.id.ll_inconf_ds_lastpage);
        flCurDoc = (FrameLayout) view.findViewById(R.id.fl_inconf_ds_curpage);
        llNextDoc = (LinearLayout) view.findViewById(R.id.ll_inconf_ds_nextpage);
        llCurDoc = (LinearLayout) view.findViewById(R.id.ll_inconf_ds_curpage);
        llPageTurn = (LinearLayout) view.findViewById(R.id.ll_inconf_ds_pageturn);
        btnLast = (Button) view.findViewById(R.id.btn_inconf_ds_lastpage);
        btnNext = (Button) view.findViewById(R.id.btn_inconf_ds_nextpage);

        shareView = (DocView4Phone) view.findViewById(R.id.conf_doc_4phone_doc);
        ivAnntools = (ImageView) view.findViewById(R.id.share_iv_tools);
        tvPageProgress = (TextView) view.findViewById(R.id.share_tv_page);

        llBottom = (LinearLayout) view.findViewById(R.id.share_bottom);
        shareView.setHandler(docHandler);
        shareNoDoc.setOnClickListener(this);
        ivAnntools.setOnClickListener(this);
        btnLast.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        view.setOnTouchListener(this);
        pvDocs.setOnSkipListener(new PageViewDs.OnSkipHSListener() {
            @Override
            public void onDot(int pages, int cur) {
            }

            @Override
            public void doSkip(int pages, int cur, int singleWidth) {
                Message msg = new Message();
                msg.what = 0;
                msg.arg1 = pages;
                msg.arg2 = cur;
                skipHandler.sendMessage(msg);
            }

        });

//
//        pvDocs.post(new Runnable() {
//            @Override
//            public void run() {
//                doShowView(true,false);
//            }
//        });
    }

    Handler skipHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            int id = 0;
            int pages = msg.arg1;
            int cur = msg.arg2;

            Log.d("InfowareLab.Debug", ">>>>>>doSkip: total page:" + pages + "; current=" + cur);

            if (pages == 3) {
                if (cur == 1) {
                    id = activeDocid.lastId;
                } else if (cur == 3) {
                    id = activeDocid.nextId;
                }
            } else if (pages == 2) {
                if (cur == 1 && activeDocid.lastId != 0) {
                    id = activeDocid.lastId;
                } else if (cur == 2 && activeDocid.nextId != 0) {
                    id = activeDocid.nextId;
                }
            }
            if (id == 0) return;
            //setCurHSV(id);
            docCommon.switchDoc(id);
        }
    };

    private Handler docHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            DocBean doc = null;
            switch (msg.what) {
                case DocCommon.Doc_OPEN:
//                    curDoc = (DocBean) msg.obj;
//                    openDoc(curDoc);
                    Log.d("InfowareLab.DS", "DocCommon.Doc_OPEN: " + ((DocBean) msg.obj).getTitle() + "; " + ((DocBean) msg.obj).getDocID());

                    if (docCommon == null) break;

                    LinkedHashMap<Integer, DocBean> list = docCommon.getDocMap();

                    if (list == null || docMap == null) break;

                    if (list != null){
                        if (list.size() != docMap.size()) {
                            Log.d("InfowareLab.Debug", "DocCommon.Doc_OPEN => refresh the doc list");

                            if (isVisible())
                                dsHandler.sendEmptyMessage(0);

                        }
                    }

                    break;
                case DocCommon.DOC_SHOW:
                    Log.d("InfowareLab.DS", "DocCommon.DOC_SHOW");
                    showDoc(msg, null);

                    break;
                case DocCommon.DOC_CLOSE:
                    if (((LinkedHashMap<Integer, DocBean>) docCommon.getDocMap().clone()).size() < 1) {
                        closeDoc();
                        Log.i("InfowareLab.Debug", "DocCommon.DOC_CLOSEALL ");
                    } else {
                        Log.i("InfowareLab.Debug", "DocCommon.DOC_CLOSE ");
                    }
                    break;
                case DocCommon.NEW_PAGE:
                    setPagecount(msg);
                    Log.d("InfowareLab.Debug", "DocCommon.NEW_PAGE " + msg.arg1 + "/" + msg.arg2);
                    break;
                case DocCommon.DOC_DISMISS_SHAREBUTTON:
                    callParentView(ACTION_PRIVILEDGE_DS, null);
                    break;
                case DocCommon.DOC_SHOW_SHAREBUTTON:
                    callParentView(ACTION_PRIVILEDGE_DS, null);
                    break;
                case DocCommon.ANNOTATION_OPT_TYPE_ADD:
                    AnnotationBean annotation = (AnnotationBean) msg.obj;
                    shareView.addAnnotation(annotation);
                    if (annotation != null && annotation.isPointerAnnt()
                            && annotation.getUserId() == userCommon.getSelf().getUid()
                            && docCommon.getDoc(annotation.getDocID()) != null && null != docCommon.getDoc(annotation.getDocID()).getPage()) {//可能有在传标注消息时关闭了该标注文档的情况
                        docCommon.getDoc(annotation.getDocID()).getPage().setPreAnnotation(annotation);
                    }
                    break;
                case DocCommon.ANNOTATION_OPT_TYPE_DEL:
                    shareView.removeAnnotation((Integer) msg.obj);
                    break;
                case DocCommon.DOC_ANNOTATION_BAR_SHOW:
                    if (!isHidden() && docCommon.getDocMap().size() > 0) {
                        enableAnnotation();
                    }
                    break;
                case DocCommon.DOC_ANNOTATION_BAR_HIDDEN:
                    if (!isHidden()) {
                        disableAnnotation();
                    }
                    break;
                case DOC_DISMISS_MENU:
                    break;
                case DOC_DISMISS_LIST:
                    callChangeBars();
                    break;
                case DocCommon.DOC_DISMISS_SEEKBAR:
                    break;
            }
        }

    };

    public void changeOrietation(Configuration newConfig) {
        if (!isAdded()) return;
        setPlace4Orientation(newConfig.orientation);
        setPagesSize(newConfig.orientation);
        setBottomAnn(isShowAnn, isBarsShow);
        doShowView(false, false);
    }

    private void setPagecount(Message msg) {
        if (curDoc != null) {
            if (msg.arg1 == curDoc.getDocID()) {
                if (msg.arg2 > curDoc.getPageCount()) {
                    curDoc.setPageCount(msg.arg2);
                }
                if (curDoc.getPage() != null) {
                    tvPageProgress.setText(curDoc.getPage().getPageID() + "/" + curDoc.getPageCount());
                }
            }
        }
    }

    /**
     * 关闭文档
     */
    private void closeDoc() {
        if (null != shareNoDoc)
            shareNoDoc.setVisibility(View.VISIBLE);
        Log.d("InfowareLab.Debug", "@closeDoc: shareView.setVisibility(View.INVISIBLE)");
        if (null != shareView)
            shareView.setVisibility(View.INVISIBLE);
        setBottomAnn(false, isBarsShow);
        if (null != docCommon)
            docCommon.setCurrentDoc(null);
        if (docMap != null)
            docMap.clear();
        if (!isHidden())
            doTransChannel();
    }

    /**
     * 显示文档视图
     *
     * @param msg
     * @param docbean
     */
    public void showDoc(Message msg, DocBean docbean) {
        shareNoDoc.setVisibility(View.INVISIBLE);
        llCurDoc.setVisibility(View.GONE);

        Log.d("InfowareLab.DS", "showDoc(msg): msg.obj=" + msg.obj);

        shareView.setVisibility(View.VISIBLE);

        //增加页码相关代码
        if (msg != null) {
            curDoc = docCommon.getDoc((Integer) msg.obj);
            Log.d("InfowareLab.DS", "showDoc(msg): curDoc = " + curDoc.getTitle() + ";" + curDoc.getDocID());

        } else {
            curDoc = docbean;
        }

        if (curDoc == null) {
            return;
        } else {
            doShowView(false, false);
            return;
        }

//
//        if (curDoc.getPage() != null) {
//            tvPageProgress.setText(curDoc.getPage().getPageID() + "/" + curDoc.getPageCount());
//        }
//
//        shareView.setOnTouchListener(shareView);
//        shareView.setDoc(curDoc, getOrientationState());
//        docCommon.setStartToShowPage(false);
//        if (!isHidden())
//            doTransChannel();
//
//        beginCloudRecord();
    }

    private int getParentW(int orientation) {
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return ConferenceApplication.Root_W;
        } else {
            return ConferenceApplication.getConferenceApp().getWindowWidth(Configuration.ORIENTATION_LANDSCAPE);//ConferenceApplication.Screen_H - ConferenceApplication.StateBar_H -  - ConferenceApplication.NavigationBar_H;
        }
    }

    private int getParentH(int orientation) {
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return ConferenceApplication.Root_H - getResources().getDimensionPixelOffset(R.dimen.height_6_80) - getResources().getDimensionPixelOffset(R.dimen.height_7_80);
        } else {
            return ConferenceApplication.getConferenceApp().getWindowHeight(Configuration.ORIENTATION_LANDSCAPE);//ConferenceApplication.Screen_W - ConferenceApplication.StateBar_H;
        }
    }

    private int getOrientationState() {
        Configuration mConfiguration = this.getResources().getConfiguration();
        return mConfiguration.orientation;
    }


    private void addImageShare(int index) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Bundle bundle;
            intent = new Intent();
            bundle = new Bundle();
            bundle.putString("flag", index + "");
            if (index == ImageActivity.PHOTO_REQUEST_TAKEPHOTO) {
                callParentView(ACTION_CLOSECAMERA, null);
            }
            intent.setClass(getActivity(), ImageActivity.class);
            intent.putExtras(bundle);
            callParentView(ACTION_JUMP2IMG, null);
            startActivityForResult(intent, 0);
        } else {
            showLongToast(R.string.noSDcard);
        }

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_inconf_ds_lastpage) {
            if (curDoc == null || curDoc.getPage() == null || curDoc.getPageCount() <= 1) return;
            if (curDoc.getPage().getStepCount() > 0) {
                if (curDoc.getPage().getCurrentStep() == 0) {
                    if (curDoc.getPage().getPageID() == 1) {
                        showShortToast(R.string.prePage);
                    } else {
                        if (docCommon.getStepCount(curDoc.getDocID(), curDoc.getPage()
                                .getPageID() - 1) > 0) {
                            long count = docCommon.getStepCount(curDoc.getDocID(), curDoc.getPage()
                                    .getPageID() - 1);
                            docCommon.switchPageStep(curDoc.getDocID(), curDoc.getPage()
                                    .getPageID() - 1, count);
                        } else {
                            docCommon.switchPage(curDoc.getDocID(), curDoc.getPage()
                                    .getPageID() - 1);
                        }
                    }
                } else if (curDoc.getPage().getCurrentStep() == 1) {
                    docCommon.switchPage(curDoc.getDocID(), curDoc.getPage()
                            .getPageID());
                } else {
                    docCommon.switchPageStep(curDoc.getDocID(), curDoc.getPage()
                            .getPageID(), curDoc.getPage().getCurrentStep() - 1);
                }
            } else {
                if (curDoc.getPage().getPageID() == 1) {
                    showShortToast(R.string.prePage);
                } else {
                    if (docCommon.getStepCount(curDoc.getDocID(), curDoc.getPage()
                            .getPageID() - 1) > 0) {
                        long count = docCommon.getStepCount(curDoc.getDocID(), curDoc.getPage()
                                .getPageID() - 1);
                        docCommon.switchPageStep(curDoc.getDocID(), curDoc.getPage()
                                .getPageID() - 1, count);
                    } else {
                        docCommon.switchPage(curDoc.getDocID(), curDoc.getPage()
                                .getPageID() - 1);
                    }
                }
            }
        } else if (id == R.id.btn_inconf_ds_nextpage) {
            if (curDoc == null || curDoc.getPage() == null || curDoc.getPageCount() <= 1) return;
            if (curDoc.getPage().getStepCount() > 0) {
                if (curDoc.getPage().getCurrentStep() == curDoc.getPage().getStepCount()) {
                    if (curDoc.getPage().getPageID() == docCommon
                            .getPageCount(curDoc.getDocID())) {
                        showShortToast(R.string.nextPage);
                    } else {
                        docCommon.switchPage(curDoc.getDocID(), curDoc.getPage()
                                .getPageID() + 1);
                    }
                } else {
                    docCommon.switchPageStep(curDoc.getDocID(), curDoc.getPage()
                            .getPageID(), curDoc.getPage().getCurrentStep() + 1);
                }
            } else {
                if (curDoc.getPage().getPageID() == docCommon
                        .getPageCount(curDoc.getDocID())) {
                    showShortToast(R.string.nextPage);
                } else {
                    docCommon.switchPage(curDoc.getDocID(), curDoc.getPage()
                            .getPageID() + 1);
                }
            }
        } else if (id == R.id.iv_inconf_ctrl_ds_add) {
            AddDocDialog dialog = new AddDocDialog(getActivity());
            dialog.setbAS(ConferenceCommonImpl.ISSHARE_AUTHORITY);
            dialog.setClickListener(new AddDocDialog.OnResultListener() {
                @Override
                public void doClick(int position) {
                    //Log.e("Test", "position::" + position);
                    if (position == 1) {
                        addImageShare(ImageActivity.PHOTO_REQUEST_TAKEPHOTO);
                    } else if (position == 2) {
                        addImageShare(ImageActivity.PHOTO_REQUEST_GALLERY);
                    } else if (position == 3) {
                        docCommon.newBoard();
                    } else if (4 == position) {
//                            //桌面共享
                        listterner.onShare();
                        dialog.dismiss();
                    }
                }
            });
            dialog.show(ConferenceApplication.Root_W);
        } else if (id == R.id.share_iv_tools) {
            setBottomAnn(!isShowAnn, isBarsShow);
        } else if (id == R.id.share_bottom_ll_black) {
            setAnnColor(DocView4Phone.ColorEnum.BLACK);
        } else if (id == R.id.share_bottom_ll_gray) {
            setAnnColor(DocView4Phone.ColorEnum.GRAY);
            //            case R.id.share_bottom_ll_white:
//                setAnnColor(DocView4Phone.ColorEnum.WHITE);
//                break;
        } else if (id == R.id.share_bottom_ll_green) {
            setAnnColor(DocView4Phone.ColorEnum.GREEN);
        } else if (id == R.id.share_bottom_ll_yellow) {
            setAnnColor(DocView4Phone.ColorEnum.YELLOW);
        } else if (id == R.id.share_bottom_ll_red) {
            setAnnColor(DocView4Phone.ColorEnum.RED);
        } else if (id == R.id.share_bottom_iv_pen) {
            setAnnTool(DocView4Phone.ToolEnum.PEN);
        } else if (id == R.id.share_bottom_iv_eraser) {
            setAnnTool(DocView4Phone.ToolEnum.ERASER);
        } else if (id == R.id.share_bottom_iv_pointer) {
            setAnnTool(DocView4Phone.ToolEnum.POINTER);
        } else if (id == R.id.tv_inconf_ds_del) {
            if (curDoc != null) {
                docCommon.closeDoc(curDoc.getDocID());
                docCommon.onCloseDoc(curDoc.getDocID());
            }
        } else if (id == R.id.shareNoDoc) {
            callChangeBars();
        }

    }


    private LinearLayout llColorBlack, llColorGray, llColorGreen, llColorYellow, llColorRed;
    private ImageView ivToolPen, ivToolEraser, ivToolPointer;
    private TextView tvDel;


    private void initAnnoationAction() {
        tvDel = (TextView) view.findViewById(R.id.tv_inconf_ds_del);

        ivToolEraser = (ImageView) view.findViewById(R.id.share_bottom_iv_eraser);
        ivToolPen = (ImageView) view.findViewById(R.id.share_bottom_iv_pen);
        ivToolPointer = (ImageView) view.findViewById(R.id.share_bottom_iv_pointer);

        llColorBlack = (LinearLayout) view.findViewById(R.id.share_bottom_ll_black);
        llColorGray = (LinearLayout) view.findViewById(R.id.share_bottom_ll_gray);
//        llColorWhite = (LinearLayout) view.findViewById(R.id.share_bottom_ll_white);
        llColorGreen = (LinearLayout) view.findViewById(R.id.share_bottom_ll_green);
        llColorYellow = (LinearLayout) view.findViewById(R.id.share_bottom_ll_yellow);
        llColorRed = (LinearLayout) view.findViewById(R.id.share_bottom_ll_red);

        tvDel.setOnClickListener(this);

        ivToolPen.setOnClickListener(this);
        ivToolEraser.setOnClickListener(this);
        ivToolPointer.setOnClickListener(this);

        llColorBlack.setOnClickListener(this);
        llColorGray.setOnClickListener(this);
//        llColorWhite.setOnClickListener(this);
        llColorGreen.setOnClickListener(this);
        llColorYellow.setOnClickListener(this);
        llColorRed.setOnClickListener(this);

        setAnnTool(DocView4Phone.ToolEnum.TOUCH);
        setAnnColor(DocView4Phone.ColorEnum.RED);
//        disableAnnotation();

        if (docCommon.getPrivateAnnoPriviledge() ||
                (userCommon.getSelf() != null && userCommon.getSelf().getRole() == UserCommon.ROLE_HOST)) {
            enableAnnotation();
        } else {
            disableAnnotation();
        }
    }

    private void resetAnnTools() {
        setAnnTool(DocView4Phone.ToolEnum.TOUCH);
        AnnotationType type = docCommon.getAnnotation();
        if (type != null) type.setPaint(false);
        EraserBean eraser = docCommon.getEraser();
        if (eraser != null) eraser.setEraserClean(false);
        removePointing();
    }

    private void enableAnnotation() {
        ivAnntools.setVisibility(View.VISIBLE);
    }

    private void disableAnnotation() {
        DocCommonImpl.isStartPointer = false;
        if (!docCommon.isStartToShowPage()) {
            if (curDoc != null && curDoc.getPage() != null && curDoc.getPage().getMyPreAnnotation() != null) {
                docCommon.removeOneAnno(curDoc.getPage().getMyPreAnnotation());
            }
            shareView.invalidate();
        }

        ((DocCommonImpl) commonFactory.getDocCommon()).getAnnotation().setPaint(false);
        ((DocCommonImpl) commonFactory.getDocCommon()).getEraser().setEraserClean(false);

        ivAnntools.setVisibility(View.GONE);
        setBottomAnn(false, isBarsShow);
    }

    public void removePointing() {
        if (!isAdded()) return;
        shareView.removePointer();
    }


    private void setAnnColor(DocView4Phone.ColorEnum color) {
        if (color == DocView4Phone.ColorEnum.BLACK) {
            llColorBlack.setBackgroundResource(R.drawable.bg_anncolor_selected);
            llColorGray.setBackgroundResource(android.R.color.transparent);
//            llColorWhite.setBackgroundResource(android.R.color.transparent);
            llColorGreen.setBackgroundResource(android.R.color.transparent);
            llColorYellow.setBackgroundResource(android.R.color.transparent);
            llColorRed.setBackgroundResource(android.R.color.transparent);
        } else if (color == DocView4Phone.ColorEnum.GRAY) {
            llColorGray.setBackgroundResource(R.drawable.bg_anncolor_selected);
            llColorBlack.setBackgroundResource(android.R.color.transparent);
//            llColorWhite.setBackgroundResource(android.R.color.transparent);
            llColorGreen.setBackgroundResource(android.R.color.transparent);
            llColorYellow.setBackgroundResource(android.R.color.transparent);
            llColorRed.setBackgroundResource(android.R.color.transparent);
        } else if (color == DocView4Phone.ColorEnum.WHITE) {
//            llColorWhite.setBackgroundResource(R.drawable.bg_anncolor_selected);
            llColorBlack.setBackgroundResource(android.R.color.transparent);
            llColorGray.setBackgroundResource(android.R.color.transparent);
            llColorGreen.setBackgroundResource(android.R.color.transparent);
            llColorYellow.setBackgroundResource(android.R.color.transparent);
            llColorRed.setBackgroundResource(android.R.color.transparent);
        } else if (color == DocView4Phone.ColorEnum.GREEN) {
            llColorGreen.setBackgroundResource(R.drawable.bg_anncolor_selected);
            llColorBlack.setBackgroundResource(android.R.color.transparent);
            llColorGray.setBackgroundResource(android.R.color.transparent);
//            llColorWhite.setBackgroundResource(android.R.color.transparent);
            llColorYellow.setBackgroundResource(android.R.color.transparent);
            llColorRed.setBackgroundResource(android.R.color.transparent);
        } else if (color == DocView4Phone.ColorEnum.YELLOW) {
            llColorYellow.setBackgroundResource(R.drawable.bg_anncolor_selected);
            llColorBlack.setBackgroundResource(android.R.color.transparent);
            llColorGray.setBackgroundResource(android.R.color.transparent);
//            llColorWhite.setBackgroundResource(android.R.color.transparent);
            llColorGreen.setBackgroundResource(android.R.color.transparent);
            llColorRed.setBackgroundResource(android.R.color.transparent);
        } else if (color == DocView4Phone.ColorEnum.RED) {
            llColorRed.setBackgroundResource(R.drawable.bg_anncolor_selected);
            llColorBlack.setBackgroundResource(android.R.color.transparent);
            llColorGray.setBackgroundResource(android.R.color.transparent);
//            llColorWhite.setBackgroundResource(android.R.color.transparent);
            llColorGreen.setBackgroundResource(android.R.color.transparent);
            llColorYellow.setBackgroundResource(android.R.color.transparent);
        } else {
            color = DocView4Phone.ColorEnum.RED;
            llColorRed.setBackgroundResource(R.drawable.bg_anncolor_selected);
            llColorBlack.setBackgroundResource(android.R.color.transparent);
            llColorGray.setBackgroundResource(android.R.color.transparent);
//            llColorWhite.setBackgroundResource(android.R.color.transparent);
            llColorGreen.setBackgroundResource(android.R.color.transparent);
            llColorYellow.setBackgroundResource(android.R.color.transparent);
        }

        shareView.setAnnColor(color);
    }

    private void setAnnTool(DocView4Phone.ToolEnum tool) {
        if (tool == shareView.getAnnTool()) {
            tool = DocView4Phone.ToolEnum.TOUCH;
        }
        if (tool == DocView4Phone.ToolEnum.PEN) {
            ivToolPen.setImageResource(R.drawable.icon_ds_pen_on);
            ivToolEraser.setImageResource(R.drawable.icon_ds_eraser_normal);
            ivToolPointer.setImageResource(R.drawable.icon_ds_pointer_normal);
            llPageTurn.setVisibility(View.GONE);
        } else if (tool == DocView4Phone.ToolEnum.ERASER) {
            ivToolPen.setImageResource(R.drawable.icon_ds_pen_normal);
            ivToolEraser.setImageResource(R.drawable.icon_ds_eraser_on);
            ivToolPointer.setImageResource(R.drawable.icon_ds_pointer_normal);
            llPageTurn.setVisibility(View.GONE);
        } else if (tool == DocView4Phone.ToolEnum.POINTER) {
            ivToolPen.setImageResource(R.drawable.icon_ds_pen_normal);
            ivToolEraser.setImageResource(R.drawable.icon_ds_eraser_normal);
            ivToolPointer.setImageResource(R.drawable.icon_ds_pointer_on);
            llPageTurn.setVisibility(View.GONE);
        } else if (tool == DocView4Phone.ToolEnum.TOUCH) {
            ivToolPen.setImageResource(R.drawable.icon_ds_pen_normal);
            ivToolEraser.setImageResource(R.drawable.icon_ds_eraser_normal);
            ivToolPointer.setImageResource(R.drawable.icon_ds_pointer_normal);
            if (curDoc != null && curDoc.getPage() != null && curDoc.getPageCount() > 1) {
                llPageTurn.setVisibility(View.VISIBLE);
            } else {
                llPageTurn.setVisibility(View.GONE);
            }
        } else {
            tool = DocView4Phone.ToolEnum.TOUCH;
            ivToolPen.setImageResource(R.drawable.icon_ds_pen_normal);
            ivToolEraser.setImageResource(R.drawable.icon_ds_eraser_normal);
            ivToolPointer.setImageResource(R.drawable.icon_ds_pointer_normal);
            if (curDoc != null && curDoc.getPage() != null && curDoc.getPageCount() > 1) {
                llPageTurn.setVisibility(View.VISIBLE);
            } else {
                llPageTurn.setVisibility(View.GONE);
            }
        }
        shareView.setAnnTool(tool);
    }

    public void doTransChannel() {
        int docId = 0;
        int pageId = 0;
        if (docCommon == null) return;
        if (docCommon.getCurrentDoc() != null && curDoc != null && null != curDoc.getPage()) {
            docId = curDoc.getDocID();
            pageId = curDoc.getPage().getPageID();
        }

        JSONObject json = new JSONObject();
        try {
            json.put("code", 1);
            json.put("module", "curDoc");
            json.put("docId", docId);
            json.put("pageId", pageId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        callParentView(ACTION_TRANSCHANNEL, json.toString());

    }

    public void beginCloudRecord() {
        if (!CommonFactory.getInstance().getConferenceCommon().isCloudRecording()) return;
        if (curDoc != null && curDoc.getPage() != null) {
            docCommon.cloudRecord(curDoc.getDocID(), curDoc.getPage().getPageID());
        }
    }


    private boolean isShowAnn = false;

    private void setBottomAnn(boolean isShowAnn, boolean isBarShow) {
        if (!isAdded()) return;
        if (!isBarShow || (!docCommon.getPrivateAnnoPriviledge() && !((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).isHost())) {
            llBottom.setVisibility(View.GONE);
            ivAnntools.setVisibility(View.GONE);
            return;
        }
        if (isShowAnn) {
            callHideBottom();
            llBottom.setVisibility(View.VISIBLE);
//            llPageTurn.setVisibility(View.GONE);
            ivAnntools.setVisibility(View.VISIBLE);
            ivAnntools.setImageResource(R.drawable.icon_ds_tools_on);
            tvPageProgress.setVisibility(View.GONE);
            this.isShowAnn = true;
        } else {
            resetAnnTools();
            callShowBottom();
            llBottom.setVisibility(View.GONE);
//            if(curDoc!=null&&curDoc.getPage()!=null&&curDoc.getPageCount()>1){
//                llPageTurn.setVisibility(View.VISIBLE);
//            }else{
//                llPageTurn.setVisibility(View.GONE);
//            }
            ivAnntools.setVisibility(View.VISIBLE);
            ivAnntools.setImageResource(R.drawable.icon_ds_tools_normal);
            tvPageProgress.setVisibility(View.VISIBLE);
            this.isShowAnn = false;
        }
    }

    private void setPlace4Orientation(int orientationState) {
        if (!isAdded()) return;
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) ivAnntools.getLayoutParams();
        if (orientationState == Configuration.ORIENTATION_LANDSCAPE) {
            placeTop.setVisibility(View.GONE);
            placeBottom.setVisibility(View.GONE);
            lp.bottomMargin = getResources().getDimensionPixelOffset(R.dimen.height_7_80) + getResources().getDimensionPixelOffset(R.dimen.width_2_80);
        } else {
            placeTop.setVisibility(View.VISIBLE);
            placeBottom.setVisibility(View.VISIBLE);
            lp.bottomMargin = getResources().getDimensionPixelOffset(R.dimen.width_2_80);
        }

        ivAnntools.setLayoutParams(lp);
    }

    private void setPagesSize(int orientation) {
        if (!isAdded()) return;
        int w = getParentW(orientation);
        int h = getParentH(orientation);

        LinearLayout.LayoutParams lp0 = (LinearLayout.LayoutParams) flShowData.getLayoutParams();
        lp0.height = h;
        lp0.width = w;
        flShowData.setLayoutParams(lp0);
        flShowData.setVisibility(View.VISIBLE);

        LinearLayout.LayoutParams lp1 = (LinearLayout.LayoutParams) llLastDoc.getLayoutParams();
        lp1.height = h;
        lp1.width = w;
        llLastDoc.setLayoutParams(lp1);
        llLastDoc.setVisibility(View.VISIBLE);
        LinearLayout.LayoutParams lp3 = (LinearLayout.LayoutParams) llNextDoc.getLayoutParams();
        lp3.height = h;
        lp3.width = w;
        llNextDoc.setLayoutParams(lp3);
        llNextDoc.setVisibility(View.VISIBLE);
        LinearLayout.LayoutParams lp2 = (LinearLayout.LayoutParams) flCurDoc.getLayoutParams();
        lp2.height = h;
        lp2.width = w;
        flCurDoc.setLayoutParams(lp2);
        llCurDoc.setVisibility(View.VISIBLE);
        Log.d("InfowareLab.Debug", "pvDocs.setScreenWidth(3): w=" + w);

        pvDocs.setScreenWidth(w, 30);
        //pvDocs.setCurPage(2);
    }

    private boolean isBarsShow = true;

    public void onChangeBars(boolean isShow) {
        this.isBarsShow = isShow;
        if (isShow) {
            setBottomAnn(isShowAnn, true);
        } else {
            setBottomAnn(isShowAnn, false);
        }
    }

    public void setRole() {
        if (!isAdded()) return;
        if ((curDoc != null && curDoc.isLocal()) || userCommon.getSelf().getRole() == UserCommon.ROLE_HOST) {
            tvDel.setVisibility(View.VISIBLE);
        } else {
            tvDel.setVisibility(View.INVISIBLE);
        }
    }


    private class ActiveDocid {
        int lastId;
        int curId;
        int nextId;

        public void setEmpty() {
            this.lastId = 0;
            this.curId = 0;
            this.nextId = 0;
        }
    }

    /**
     * 定义了所有activity必须实现的接口
     */
    public interface FragmentInteraction {
        /**
         * Fragment 向Activity传递指令，这个方法可以根据需求来定义
         */
        void onShare();
    }

    /**
     * 当FRagmen被加载到activity的时候会被回调
     *
     * @param activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof FragmentInteraction) {
            listterner = (FragmentInteraction) activity;
        } else {
            throw new IllegalArgumentException("activity must implements FragmentInteraction");
        }
    }

    @Override
    public void onDestroy() {
//        if (docCommon != null) {
//            docCommon.setHandler(null);
//        }
        super.onDestroy();
    }

    public void preExit() {
        closeDoc();
    }

}
