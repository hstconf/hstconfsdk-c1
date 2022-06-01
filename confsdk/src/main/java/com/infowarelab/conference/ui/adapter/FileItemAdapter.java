package com.infowarelab.conference.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.infowarelab.hongshantongphone.R;

public class FileItemAdapter extends BaseAdapter {
    private Context context;
    private String[] str;
    private ViewHolder holder;

    public FileItemAdapter(Context context, String[] str) {
        this.context = context;
        this.str = str;
    }

    @Override
    public int getCount() {
        return str.length;
    }

    @Override
    public Object getItem(int position) {
        return str[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.conference_share_file, null);
            holder.fileName = (TextView) convertView.findViewById(R.id.share_filename);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.fileName.setText(getItem(position).toString());

        return convertView;
    }

    public class ViewHolder {
        public TextView fileName;
    }

}
