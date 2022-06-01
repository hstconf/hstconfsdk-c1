package com.infowarelab.conference.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.infowarelab.hongshantongphone.R;

import java.util.ArrayList;
import java.util.List;

public class SpinnerAdapterDuration extends BaseAdapter {
    private Context context;
    private List<DurOption> list;
    private int[] txtRes = {
            R.string.preconf_create_duration1,
            R.string.preconf_create_duration2,
            R.string.preconf_create_duration3,
            R.string.preconf_create_duration4,
            R.string.preconf_create_duration5,
            R.string.preconf_create_duration6,
            R.string.preconf_create_duration7,
            R.string.preconf_create_duration8,
            R.string.preconf_create_duration9,
            R.string.preconf_create_duration10
    };
    private int[] durMinute = {
            120,
            60 * 24,
            60 * 24 * 3,
            60 * 24 * 5,
            60 * 24 * 10,
            60 * 24 * 30,
            60 * 24 * 30 * 3,
            60 * 24 * 30 * 6,
            60 * 24 * 365,
            (int) (68373589 - System.currentTimeMillis() / (1000 * 60))
    };
    private ViewHolder holder;
    private SpinnerItemListener listener;

    public SpinnerAdapterDuration(Context context) {
        this.context = context;
        initList();
    }

    private void initList() {
        if (this.list == null) {
            this.list = new ArrayList<>();

        } else {
            this.list.clear();
        }
        for (int i = 0; i < txtRes.length; i++) {
            DurOption op = new DurOption();
            op.res = txtRes[i];
            op.dur = durMinute[i];
            this.list.add(op);
        }
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list.size();
    }

    @Override
    public DurOption getItem(int position) {
        // TODO Auto-generated method stub
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup arg2) {
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.view_item_spinner, null);
            holder.ll = (LinearLayout) convertView
                    .findViewById(R.id.ll_item_spinner);
            holder.tv = (TextView) convertView
                    .findViewById(R.id.tv_item_spinner);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (getCount() <= position) {
            // 直接引用会导致异常
            return convertView;
        }
        DurOption op = getItem(position);
        holder.tv.setText(context.getResources().getString(op.res));
        holder.tv.setTag(op.dur);
        holder.ll.setTag(op.res);
        holder.ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View v1 = v.findViewById(R.id.tv_item_spinner);
                doSelect((int) (v.getTag()), (int) (v1.getTag()));
            }
        });

        return convertView;
    }

    class ViewHolder {
        LinearLayout ll;
        TextView tv;
    }

    public void setListener(SpinnerItemListener listener) {
        this.listener = listener;
    }

    public interface SpinnerItemListener {
        public void select(int res, int dur);
    }

    private void doSelect(int res, int dur) {
        if (listener != null) listener.select(res, dur);
    }


    class DurOption {
        int res;
        int dur;
    }
}
