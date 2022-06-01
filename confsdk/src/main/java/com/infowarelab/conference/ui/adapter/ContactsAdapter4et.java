package com.infowarelab.conference.ui.adapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.domain.ContactBean;

public class ContactsAdapter4et extends ChipsAdapter {
    private Set<String> excludes;// 已有邮箱
    private Set<String> filtereds;// 已过滤邮箱
    private ArrayList<ContactBean> suggestions;// 过滤后邮箱
    private ArrayList<ContactBean> items;// 所有邮箱
    private ArrayList<ContactBean> buffer;// 缓冲列表
    private ContactBean contact;
    private ViewHolder holder;
    private LayoutInflater inflater;
    private String str = "";
    private boolean hasEmail;
    private boolean hasPhoneNumber;

    public ContactsAdapter4et(Context context, ArrayList<ContactBean> list) {
        this.items = list;
        this.inflater = LayoutInflater.from(context);
        this.suggestions = new ArrayList<ContactBean>();
        this.buffer = new ArrayList<ContactBean>();
        this.excludes = new HashSet<String>();
        this.filtereds = new HashSet<String>();
    }

    private class ViewHolder {
        public LinearLayout rl;
        public TextView cname;
        public TextView phoneNumber;
        public TextView email;
        public ImageView phoneNumberImg;
        public ImageView emailImg;
        public View line;
    }

    public void refreshAdapter(ArrayList<ContactBean> items) {
        this.items = items;
        nameFilter.filter(str);
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
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.invite_contact_listitem,
                    null);
            holder.rl = (LinearLayout) convertView
                    .findViewById(R.id.invite_contact_list);
            holder.cname = (TextView) convertView
                    .findViewById(R.id.invite_contact_listcname);
            holder.phoneNumber = (TextView) convertView
                    .findViewById(R.id.invite_contact_listphonenumber);
            holder.phoneNumberImg = (ImageView) convertView
                    .findViewById(R.id.invite_contact_listphonenumber_img);
            holder.email = (TextView) convertView
                    .findViewById(R.id.invite_contact_listemail);
            holder.emailImg = (ImageView) convertView
                    .findViewById(R.id.invite_contact_listemail_img);
            holder.line = convertView.findViewById(R.id.invite_contact_line);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        // Log.i("AlwaysTest", "userBean="
        // + getItem(position).toString());

        contact = getItem(position);
        if (position == 0) {
            holder.line.setVisibility(View.GONE);
        } else {
            holder.line.setVisibility(View.VISIBLE);
        }

        if (position >= suggestions.size()) {
            holder.rl.setVisibility(View.GONE);
        } else {
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
            // 汉字加粗
            holder.cname.getPaint().setFakeBoldText(true);

            holder.phoneNumber.setText(contact.getPhoneNumber().toString());
            holder.email.setText(contact.getEmail().toString());

//			holder.email.setText(getItem(position).getEmail());
        }
        return convertView;
    }

    @Override
    public void addExcludes(Object s) {
        if (!"".equals(((ContactBean) s).getEmail()) && null != ((ContactBean) s).getEmail()) {
            excludes.add(((ContactBean) s).getEmail());
        } else {
            excludes.add(((ContactBean) s).getPhoneNumber());
        }
    }

    @Override
    public void addExcludesPhone(String s) {
        excludes.add(s);
    }

    @Override
    public boolean isExcluded(Object s) {
        return excludes.contains(((ContactBean) s).getEmail()) || excludes.contains(((ContactBean) s).getPhoneNumber());
    }

    public boolean isFiltered(String s) {
        return filtereds.contains(s);
    }

    @Override
    public void removeExcludes(Object s) {
//		excludes.remove(((ContactBean) s).getEmail());
        if (!"".equals(((ContactBean) s).getEmail()) && null != ((ContactBean) s).getEmail()) {
            excludes.remove(((ContactBean) s).getEmail());
        } else {
            excludes.remove(((ContactBean) s).getPhoneNumber());
        }
    }

    @Override
    public void clearExcludes() {
        excludes.clear();
    }

    @Override
    public Filter getFilter() {
        // TODO Auto-generated method stub
        return nameFilter;
    }

    Filter nameFilter = new Filter() {

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            str = (String) resultValue;
            Log.i("AlwaysTest", str);
            return str;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            Log.i("AlwaysTest", "filter：" + constraint + "。");
            FilterResults filterResults = new FilterResults();
            if (constraint != null) {
                buffer.clear();
                filtereds.clear();
                try {
                    for (int i = 0; i < items.size(); i++) {
//						if("".equals(constraint.toString().toLowerCase())){
//							if((excludes==null||!isExcluded(items.get(i)))){
//								if(!isFiltered(items.get(i).getEmail())&&null!=items.get(i).getEmail()&&!"".equals(items.get(i).getEmail().trim())){
//									buffer.add(items.get(i));
//									filtereds.add(items.get(i).getEmail());
//								}else if (!isFiltered(items.get(i).getPhoneNumber())&&null!=items.get(i).getPhoneNumber()&&!"".equals(items.get(i).getPhoneNumber().trim())) {
//									buffer.add(items.get(i));
//									filtereds.add(items.get(i).getPhoneNumber());
//								}
//							}
//							continue;
//						}
                        if (items
                                .get(i)
                                .getEmail()
                                .toLowerCase()
                                .startsWith(constraint.toString().toLowerCase())) {
                            if ((excludes == null || !isExcluded(items.get(i))) && !isFiltered(items.get(i).getEmail()) && null != items.get(i).getEmail() && !"".equals(items.get(i).getEmail().trim())) {
                                buffer.add(items.get(i));
                                filtereds.add(items.get(i).getEmail());
                                Log.i("contactsadapter", "changing:" + buffer.size() + ";" + items.get(i).getEmail());
                                continue;
                            }
                        } else if (items.get(i).getPhoneNumber().toLowerCase().startsWith(constraint.toString().toLowerCase())) {
                            if ((excludes == null || !isExcluded(items.get(i))) && !isFiltered(items.get(i).getPhoneNumber()) && null != items.get(i).getPhoneNumber() && !"".equals(items.get(i).getPhoneNumber().trim())) {
                                buffer.add(items.get(i));
                                filtereds.add(items.get(i).getPhoneNumber());
                                Log.i("contactsadapter", "changing:" + buffer.size() + ";" + items.get(i).getPhoneNumber());
                                continue;
                            }
                        }
                        if ("".equals(constraint.toString().toLowerCase()) && (excludes == null || !isExcluded(items.get(i))) && !isFiltered(items.get(i).getPhoneNumber()) && null != items.get(i).getPhoneNumber() && !"".equals(items.get(i).getPhoneNumber().trim())) {
                            buffer.add(items.get(i));
                            filtereds.add(items.get(i).getPhoneNumber());
                        }
                    }
                } catch (Exception e) {
                }
                // Log.i("AlwaysTest", "suggestionsSize:" + suggestions.size());
                filterResults.values = buffer;
                filterResults.count = buffer.size();
            }
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            if (results != null) {
                suggestions.clear();
                suggestions.addAll(buffer);
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }

        }
    };

    private void logg(String msg) {
        Log.i("ChipsAdapter", msg);
    }

}
