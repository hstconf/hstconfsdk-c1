package com.infowarelab.conference.ui.view;


import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.ConferenceCommonImpl;
import com.infowarelabsdk.conference.common.impl.UserCommonImpl;


public class PopIndex5 implements PopupWindow.OnDismissListener {
    private Context context;
    private PopupWindow popupWindow;
    private OnSelectListener listener;
    private LayoutInflater inflater;

    private LinearLayout llRec, ll3;
    private ImageView ivRec;
    private TextView tvRec;

    private int cw;
    private int ch;

    public PopIndex5(final Context context, int curPage, OnSelectListener listener) {
        this.context = context;
        this.listener = listener;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.view_pop_index5, null);

        LinearLayout llRoot = (LinearLayout) view.findViewById(R.id.ll_pop_index5_root);

        Configuration mConfiguration = context.getResources().getConfiguration();
        int ori = mConfiguration.orientation;
//        if (ori == mConfiguration.ORIENTATION_LANDSCAPE) {
//            llRoot.setBackgroundResource(R.drawable.bg_pop_index5_tra);
//        } else if (ori == mConfiguration.ORIENTATION_PORTRAIT) {
            llRoot.setBackgroundResource(R.drawable.bg_pop_index5_nor);
//        }

        LinearLayout ll0 = (LinearLayout) view.findViewById(R.id.ll_pop_index5_0);
        ImageView iv0 = (ImageView) view.findViewById(R.id.iv_pop_index5_0);
        TextView tv0 = (TextView) view.findViewById(R.id.tv_pop_index5_0);

        if (curPage != 2) ll0.setVisibility(View.GONE);

//        if (curPage == 5) {
//            iv1.setImageResource(R.drawable.ic_pop_index5_info_sel);
//            tv1.setTextColor(context.getResources().getColor(R.color.index_red));
//        } else {
//            iv1.setImageResource(R.drawable.ic_pop_index5_info_nor);
//            tv1.setTextColor(context.getResources().getColor(R.color.index_white));
//        }
        ll0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectSync();
            }
        });

        LinearLayout ll1 = (LinearLayout) view.findViewById(R.id.ll_pop_index5_1);
        ImageView iv1 = (ImageView) view.findViewById(R.id.iv_pop_index5_1);
        TextView tv1 = (TextView) view.findViewById(R.id.tv_pop_index5_1);
        if (curPage == 5) {
            iv1.setImageResource(R.drawable.ic_pop_index5_info_sel);
            tv1.setTextColor(context.getResources().getColor(R.color.index_blue));
        } else {
            iv1.setImageResource(R.drawable.ic_pop_index5_info_nor);
            tv1.setTextColor(context.getResources().getColor(R.color.index_white));
        }
        ll1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectInfo();
            }
        });
        LinearLayout ll2 = (LinearLayout) view.findViewById(R.id.ll_pop_index5_2);
        ImageView iv2 = (ImageView) view.findViewById(R.id.iv_pop_index5_2);
        TextView tv2 = (TextView) view.findViewById(R.id.tv_pop_index5_2);
        if (curPage == 6) {
            iv2.setImageResource(R.drawable.ic_pop_index5_set_sel);
            tv2.setTextColor(context.getResources().getColor(R.color.index_blue));
        } else {
            iv2.setImageResource(R.drawable.ic_pop_index5_set_nor);
            tv2.setTextColor(context.getResources().getColor(R.color.index_white));
        }
        ll2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectSetting();
            }
        });

        ll3 = (LinearLayout) view.findViewById(R.id.ll_pop_index5_3);
        ll3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                onSelectRec();
            }
        });

        LinearLayout ll4 = (LinearLayout) view.findViewById(R.id.ll_pop_index5_4);
        ImageView iv4 = (ImageView) view.findViewById(R.id.iv_pop_index5_4);
        TextView tv4 = (TextView) view.findViewById(R.id.tv_pop_index5_4);
//        if (curPage == 6) {
//            iv2.setImageResource(R.drawable.ic_pop_index5_set_sel);
//            tv2.setTextColor(context.getResources().getColor(R.color.index_red));
//        } else {
//            iv2.setImageResource(R.drawable.ic_pop_index5_set_nor);
//            tv2.setTextColor(context.getResources().getColor(R.color.index_white));
//        }

        ll4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectChatting();
            }
        });

        //llRec = (LinearLayout) view.findViewById(R.id.ll_pop_index5_record);
        ivRec = (ImageView) view.findViewById(R.id.iv_pop_index5_3);
        tvRec = (TextView) view.findViewById(R.id.tv_pop_index5_3);
        if (CommonFactory.getInstance().getConferenceCommon().isSupportCloudRecord() && ((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).isHost()) {
            ll3.setVisibility(View.VISIBLE);
            boolean isRecording = CommonFactory.getInstance().getConferenceCommon().isCloudRecording();
            if (isRecording) {
                ivRec.setImageResource(R.drawable.ic_pop_index5_rec_sel);
                tvRec.setText(context.getResources().getString(R.string.index_5_4));
            } else {
                ivRec.setImageResource(R.drawable.ic_pop_index5_rec_nor);
                tvRec.setText(context.getResources().getString(R.string.index_5_3));
            }
        } else {
            ll3.setVisibility(View.GONE);
        }


        popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        cw = view.getMeasuredWidth();
        ch = view.getMeasuredHeight();

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setOnDismissListener(this);

    }

    public void setOnSelectListener(OnSelectListener listener) {
        this.listener = listener;
    }

    public void showAsDropDown(View parent) {
        Log.i("pop", "pop parent w=" + parent.getWidth() + " h=" + parent.getHeight() + " pw=" + popupWindow.getWidth() + " ph=" + popupWindow.getHeight() + " cw=" + cw + " ch=" + ch);
        popupWindow.showAsDropDown(parent,
                parent.getWidth() - cw - context.getResources().getDimensionPixelOffset(R.dimen.dp_3),
                0 - parent.getHeight() - ch - context.getResources().getDimensionPixelOffset(R.dimen.dp_3));
//        popupWindow.update();
    }

    public void dismiss() {
        popupWindow.dismiss();
    }


    public interface OnSelectListener {
        public void onSelectSync();

        public void onSelectInfo();

        public void onSelectSetting();

        public void onSelectRec();

        public void onSelectChatting();
    }

    public void onSelectInfo() {
        if (listener != null) {
            listener.onSelectInfo();
        }
        dismiss();
    }

    public void onSelectSync() {
        if (listener != null) {
            listener.onSelectSync();
        }
        dismiss();
    }

    public void onSelectSetting() {
        if (listener != null) {
            listener.onSelectSetting();
        }
        dismiss();
    }

    public void onSelectChatting() {
        if (listener != null) {
            listener.onSelectChatting();
        }
        dismiss();
    }

    public void onSelectRec() {
        if (listener != null) {
            listener.onSelectRec();
        }
    }

    @Override
    public void onDismiss() {
    }

    public boolean isShowing() {
        if (popupWindow != null) {
            return popupWindow.isShowing();
        }
        return false;
    }

    public void updateRec(boolean isRecording) {
        if (isRecording) {
            ivRec.setImageResource(R.drawable.ic_pop_index5_rec_sel);
            tvRec.setText(context.getResources().getString(R.string.index_5_4));
        } else {
            ivRec.setImageResource(R.drawable.ic_pop_index5_rec_nor);
            tvRec.setText(context.getResources().getString(R.string.index_5_3));
        }
        ll3.setEnabled(true);
    }

    public void updateRole() {
        if (((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).isHost()) {
            llRec.setVisibility(View.VISIBLE);
        } else {
            llRec.setVisibility(View.GONE);
        }
    }
}
