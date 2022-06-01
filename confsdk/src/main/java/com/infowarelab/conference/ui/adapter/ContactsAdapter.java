package com.infowarelab.conference.ui.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.domain.ContactBean;

public class ContactsAdapter extends BaseAdapter {
    private ArrayList<ContactBean> list;
    private ContactBean contact;
    private ViewHolder holder;
    private LayoutInflater inflater;
    private boolean hasEmail;
    private boolean hasPhoneNumber;

    public ContactsAdapter(Context context, ArrayList<ContactBean> list) {
        this.list = list;
        this.inflater = LayoutInflater.from(context);
    }

    private class ViewHolder {
        public TextView cname;
        public TextView phoneNumber;
        public TextView email;
        public ImageView phoneNumberImg;
        public ImageView emailImg;
    }

    public void refreshAdapter(ArrayList<ContactBean> items) {
        this.list = items;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public ContactBean getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.invite_contact_listitem, null);
            holder.cname = (TextView) convertView.findViewById(R.id.invite_contact_listcname);
            holder.phoneNumber = (TextView) convertView.findViewById(R.id.invite_contact_listphonenumber);
            holder.phoneNumberImg = (ImageView) convertView.findViewById(R.id.invite_contact_listphonenumber_img);
            holder.email = (TextView) convertView.findViewById(R.id.invite_contact_listemail);
            holder.emailImg = (ImageView) convertView.findViewById(R.id.invite_contact_listemail_img);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        contact = getItem(position);
        hasEmail = !contact.getEmail().equals("");
        hasPhoneNumber = !contact.getPhoneNumber().equals("");
        if (hasEmail) {
            holder.email.setVisibility(View.VISIBLE);
            holder.emailImg.setVisibility(View.VISIBLE);
        } else {
            holder.email.setVisibility(View.GONE);
            holder.emailImg.setVisibility(View.GONE);
        }

        if (hasPhoneNumber) {
            holder.phoneNumber.setVisibility(View.VISIBLE);
            holder.phoneNumberImg.setVisibility(View.VISIBLE);
        } else {
            holder.phoneNumber.setVisibility(View.GONE);
            holder.phoneNumberImg.setVisibility(View.GONE);
        }

        holder.cname.setText(contact.getName().toString());
        //汉字加粗
        holder.cname.getPaint().setFakeBoldText(true);

        holder.phoneNumber.setText(contact.getPhoneNumber().toString());
        holder.email.setText(contact.getEmail().toString());
        return convertView;
    }

}
