package com.infowarelab.conference.ui.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.infowarelab.hongshantongphone.R;
import com.infowarelab.conference.ui.view.MyMultiAutoCompleteTextView;
import com.infowarelabsdk.conference.domain.ContactBean;

public class MultiAutoAdapter extends BaseAdapter implements Filterable {
    private MyMultiAutoCompleteTextView edit;
    private ArrayList<ContactBean> items;
    private ArrayList<ContactBean> suggestions;
    private ArrayList<String> mExcludeIdxs;
    private ArrayList<ContactBean> newValues;
    private LayoutInflater inflater;
    ViewHolder holder;

    public MultiAutoAdapter(MyMultiAutoCompleteTextView edit, Context context, ArrayList<ContactBean> items) {
        inflater = LayoutInflater.from(context);
        this.edit = edit;
        this.items = items;
        this.suggestions = new ArrayList<ContactBean>();
        mExcludeIdxs = new ArrayList<String>();
    }

    public void refreshAdapter(ArrayList<ContactBean> items) {
        this.items = items;
        suggestions.clear();
        edit.dismissDropDown();
        notifyDataSetChanged();
        //edit.showDropDown();
    }

    @Override
    public int getCount() {
        return suggestions.size();
    }

    @Override
    public ContactBean getItem(int position) {
        return suggestions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.invite_contact_autoitem, null);
            holder = new ViewHolder();
            holder.cname = (TextView) convertView.findViewById(R.id.invite_cname);
            holder.phoneNumber = (TextView) convertView.findViewById(R.id.invite_phonenumber);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (getCount() > position) {
            holder.cname.setText(suggestions.get(position).getName());
            holder.phoneNumber.setText(suggestions.get(position).getPhoneNumber());
        }

        return convertView;
    }

    class ViewHolder {
        TextView cname;
        TextView phoneNumber;
    }

    public void addExcludeIdxs(String phoneNumber) {
        mExcludeIdxs.add(phoneNumber);
    }

    public boolean isExcluded(String phoneNumber) {
        return mExcludeIdxs.contains(phoneNumber);
    }

    public void removeExcludeIdxs(String phoneNumber) {
        mExcludeIdxs.remove(phoneNumber);
    }

    @Override
    public Filter getFilter() {
        return nameFilter;
    }

    Filter nameFilter = new Filter() {

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            String str = ((ContactBean) resultValue).getName();
            //System.out.println("sssssssssssssss "+str);
            return str;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            newValues = new ArrayList<ContactBean>();
            if (constraint != null) {
                //suggestions.clear();
                try {
                    for (int i = 0; i < items.size(); i++) {
                        if (items.get(i).getName().toLowerCase().contains(
                                constraint.toString().toLowerCase())
                                || items.get(i).getPhoneNumber().toLowerCase().startsWith(
                                constraint.toString().toLowerCase())) {
                            if (mExcludeIdxs == null || !isExcluded(items.get(i).getPhoneNumber()))
                                //suggestions.add(items.get(i));
                                newValues.add(items.get(i));
                        }
                    }
                } catch (Exception e) {
                }
							
				/*filterResults.values = suggestions;
				filterResults.count = suggestions.size();*/
                filterResults.values = newValues;
                filterResults.count = newValues.size();
            }
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            Log.i("MultiAutoAdapter", "publishResults >>>>>>>>>>> results.values = " + results.values);

            if (results != null && results.count > 0) {
                suggestions = (ArrayList<ContactBean>) results.values;
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }

        }
    };

}
