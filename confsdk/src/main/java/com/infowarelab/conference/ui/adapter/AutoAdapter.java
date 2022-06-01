package com.infowarelab.conference.ui.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.infowarelab.hongshantongphone.R;

public class AutoAdapter extends BaseAdapter implements Filterable {

    private String[] items = {"infowarelab.cn", "com", "cn"};
    private ArrayList<String> siteItems;
    private ArrayList<String> newValues;
    private LayoutInflater inflater;

    public AutoAdapter(Context context) {
        super();
        siteItems = new ArrayList<String>();
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return siteItems.size();
    }

    @Override
    public String getItem(int position) {
        return siteItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.site_setup_item, null);
            holder = new ViewHolder();
            holder.item = (TextView) convertView.findViewById(R.id.site_item);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (getCount() > position) {
            holder.item.setText(siteItems.get(position).toString());
        }

        return convertView;
    }

    class ViewHolder {
        TextView item;
    }

    @Override
    public Filter getFilter() {
        return siteFilter;
    }

    Filter siteFilter = new Filter() {

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            String str = resultValue.toString();
            return str;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            newValues = new ArrayList<String>();
            String filterText = "";
            if (constraint != null) {
                //siteItems.clear();
                try {
                    for (int i = 0; i < items.length; i++) {
                        filterText = siteWatcher(constraint.toString(), items[i]);
                        if (!filterText.equals(""))
                            //siteItems.add(filterText);
                            newValues.add(filterText);
                    }
                } catch (Exception e) {
                }
				/*filterResults.values = siteItems;
				filterResults.count = siteItems.size();*/
                filterResults.values = newValues;
                filterResults.count = newValues.size();

            }
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {

            if (results != null && results.count > 0) {
                siteItems = (ArrayList<String>) results.values;
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }

        }
    };

    private String siteWatcher(String text, String compare) {
        if (text.contains(".")) {
            String pre = text.substring(0, text.indexOf("."));
            String str = text.substring(text.indexOf(".") + 1);
            if (str.equals("")) {
                return text + compare;
            } else {
                return compare.startsWith(str) ? pre + "." + compare : "";
            }
        } else {
            return text + "." + compare;
        }
    }

}
