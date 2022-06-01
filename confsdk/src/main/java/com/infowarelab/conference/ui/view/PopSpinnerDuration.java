package com.infowarelab.conference.ui.view;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.infowarelab.conference.ui.adapter.SpinnerAdapterDuration;
import com.infowarelab.hongshantongphone.R;

import java.util.List;


public class PopSpinnerDuration implements PopupWindow.OnDismissListener {
    private Context context;
    private PopupWindow popupWindow;
    private OnSelectListener listener;
    private LayoutInflater inflater;
    private ListView lvSpinner;
    private SpinnerAdapterDuration adapter;
    private View parent;


    public PopSpinnerDuration(final Context context, int width, int singleHeight, int count) {
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.view_pop_spinner, null);
        popupWindow = new PopupWindow(view, width, (count < 5 ? count : 5) * singleHeight);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOnDismissListener(this);

        lvSpinner = (ListView) view.findViewById(R.id.lv_spinner);
        adapter = new SpinnerAdapterDuration(context);
        adapter.setListener(new SpinnerAdapterDuration.SpinnerItemListener() {
            @Override
            public void select(int res, int dur) {
                onSelect(res, dur);
            }
        });
        lvSpinner.setAdapter(adapter);
    }

    public void setOnSelectListener(OnSelectListener listener) {
        this.listener = listener;
    }

    public void showAsDropDown(View parent) {
        this.parent = parent;

        popupWindow.showAsDropDown(parent, 0,
                0);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.update();
        parent.setBackgroundResource(R.drawable.bg_pop_et);
    }

    public void dismiss() {
        popupWindow.dismiss();
    }


    public interface OnSelectListener {
        public void onSelect(int res, int dur);
    }

    public void onSelect(int res, int dur) {
        if (listener != null) {
            listener.onSelect(res, dur);
        }
        dismiss();
    }

    @Override
    public void onDismiss() {
        if (parent != null) {
            parent.setBackgroundResource(R.drawable.a6_et_common);
        }
    }
}
