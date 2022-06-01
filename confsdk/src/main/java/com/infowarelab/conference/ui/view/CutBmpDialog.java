package com.infowarelab.conference.ui.view;


import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.DocCommonImpl;
import com.infowarelabsdk.conference.domain.DocBean;
import com.infowarelabsdk.conference.domain.PageBean;

import java.io.ByteArrayOutputStream;

public class CutBmpDialog extends AlertDialog {
    private int width = 0;
    private OnResultListener onResultListener;
    private ImageView iv;
    private Bitmap bmp;

    public CutBmpDialog(Context context) {
        super(context, R.style.style_dialog_normal);
    }

    public CutBmpDialog(Context context, int width) {
        super(context, R.style.style_dialog_normal);
        this.width = width;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a6_inconf_cutbmp);
        if (width > 0) {
            LinearLayout ll = (LinearLayout) findViewById(R.id.dialog_cutbmp_ll);
            LayoutParams params = (LayoutParams) ll.getLayoutParams();
            params.width = this.width;
            ll.setLayoutParams(params);

            iv = (ImageView) findViewById(R.id.dialog_cutbmp_iv);
            LayoutParams params1 = (LayoutParams) iv.getLayoutParams();
            params1.width = this.width;
            params1.height = this.width;
            iv.setLayoutParams(params1);
        }
        setCanceledOnTouchOutside(false);
        TextView tvYes = (TextView) findViewById(R.id.dialog_cutbmp_tv_ok);
        tvYes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
//				doYes(timeout,id);
                sharePhoto(bmp);
                cancel();
            }
        });
        TextView tvNo = (TextView) findViewById(R.id.dialog_cutbmp_tv_cnl);
        tvNo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
//				doYes(timeout,id);
                cancel();
            }
        });

    }

    /**
     * 在AlertDialog的 onStart() 生命周期里面执行开始动画
     */
    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * 在AlertDialog的onStop()生命周期里面执行停止动画
     */
    @Override
    protected void onStop() {
        super.onStop();

    }

    public void show(Bitmap bmp) {
        this.bmp = bmp;
        show();
        iv.setImageBitmap(bmp);
    }


    public void setClickListener(OnResultListener onResultListener) {
        this.onResultListener = onResultListener;
    }

    public interface OnResultListener {
        public void doYes(int timeout, int id);

        public void doNo(int timeout, int id);
    }

    private void doYes(int timeout, int id) {
        if (onResultListener != null) {
            onResultListener.doYes(timeout, id);
        }
    }

    private void doNo(int timeout, int id) {
        if (onResultListener != null) {
            onResultListener.doNo(timeout, id);
        }
    }

    @Override
    public void dismiss() {
        // TODO Auto-generated method stub
        super.dismiss();
    }

    private void sharePhoto(Bitmap photo) {
        DocCommonImpl docCommon = (DocCommonImpl) CommonFactory.getInstance().getDocCommon();
        DocBean docbean = new DocBean();
        docbean.setTitle("cutbmp1");
        docbean.setPageCount(1);
        int docID = docCommon.shareDoc(docbean);
        docbean.setDocID(docID);
        docbean.setLocal(true);
        docCommon.onShareDoc(docbean);


        PageBean page = new PageBean();
        page.setDocID(docID);
        page.setWidth(photo.getWidth());
        page.setHeight(photo.getHeight());
        page.setPageID(1);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Bitmap.CompressFormat format = null;
        format = Bitmap.CompressFormat.JPEG;
        photo.compress(format, 100, baos);
        byte[] data = baos.toByteArray();
        page.setRawDate(data);
        page.setLength(data.length);
        docCommon.newPage(page);
        docCommon.onPageData(page);
    }
}
