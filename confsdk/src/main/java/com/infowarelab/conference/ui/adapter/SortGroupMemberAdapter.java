package com.infowarelab.conference.ui.adapter;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.infowarelab.hongshantongphone.R;
import com.infowarelab.conference.ui.tools.GroupMemberBean;
import com.infowarelabsdk.conference.domain.ContactBean;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class SortGroupMemberAdapter extends ChipsAdapter implements SectionIndexer {
    private Set<String> excludes;// 已有
    private Set<String> filtereds;// 已过滤
    private ArrayList<GroupMemberBean> suggestions;// 过滤后
    private List<GroupMemberBean> items;// 所有
    private ArrayList<GroupMemberBean> buffer;// 缓冲列表
    private String str = "";

    private Context mContext;

    public SortGroupMemberAdapter(Context mContext, List<GroupMemberBean> list) {
        this.mContext = mContext;
        this.items = list;
        this.suggestions = new ArrayList<GroupMemberBean>();
        this.buffer = new ArrayList<GroupMemberBean>();
        this.excludes = new HashSet<String>();
        this.filtereds = new HashSet<String>();
    }

    /**
     * 当ListView数据发生变化时,调用此方法来更新ListView
     *
     * @param list
     */
    public void updateListView(List<GroupMemberBean> list) {
        this.items = list;
        nameFilter.filter(str);
    }

    @Override
    public int getCount() {
        return suggestions.size();
    }

    @Override
    public GroupMemberBean getItem(int position) {
        return suggestions.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View view, ViewGroup arg2) {
        ViewHolder viewHolder = null;
        final GroupMemberBean mContent = getItem(position);
        if (view == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.a6_item_contacts_list, null);
            viewHolder.tvTitle = (TextView) view.findViewById(R.id.title);
            viewHolder.tvLetter = (TextView) view.findViewById(R.id.catalog);
            viewHolder.tvNumber = (TextView) view.findViewById(R.id.number);
            viewHolder.llLetter = (LinearLayout) view.findViewById(R.id.ll_catalog);
            viewHolder.line = (View) view.findViewById(R.id.line);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        // 根据position获取分类的首字母的Char ascii值
        int section = getSectionForPosition(position);

        // 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
        if (position == getPositionForSection(section)) {
            viewHolder.llLetter.setVisibility(View.VISIBLE);
            viewHolder.tvLetter.setText(mContent.getSortLetters());
            viewHolder.line.setVisibility(View.GONE);
        } else {
            viewHolder.llLetter.setVisibility(View.GONE);
            viewHolder.line.setVisibility(View.VISIBLE);
        }

        viewHolder.tvTitle.setText(getItem(position).getName());
        viewHolder.tvNumber.setText(getItem(position).getPhoneNumber());

        return view;

    }

    final static class ViewHolder {
        TextView tvLetter;
        TextView tvTitle;
        TextView tvNumber;
        LinearLayout llLetter;
        View line;
    }

    /**
     * 根据ListView的当前位置获取分类的首字母的Char ascii值
     */
    public int getSectionForPosition(int position) {
        return getItem(position).getSortLetters().charAt(0);
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     */
    public int getPositionForSection(int section) {
        for (int i = 0; i < getCount(); i++) {
            String sortStr = getItem(i).getSortLetters();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }

        return -1;
    }

    /**
     * 提取英文的首字母，非英文字母用#代替。
     *
     * @param str
     * @return
     */
    private String getAlpha(String str) {
        String sortStr = str.trim().substring(0, 1).toUpperCase();
        // 正则表达式，判断首字母是否是英文字母
        if (sortStr.matches("[A-Z]")) {
            return sortStr;
        } else {
            return "#";
        }
    }

    @Override
    public Object[] getSections() {
        return null;
    }

    @Override
    public Filter getFilter() {
        // TODO Auto-generated method stub
        return nameFilter;
    }

    @Override
    public void addExcludes(Object s) {
        // TODO Auto-generated method stub
        if (!"".equals(((ContactBean) s).getPhoneNumber()) && null != ((ContactBean) s).getPhoneNumber()) {
            excludes.add(((ContactBean) s).getPhoneNumber());
        }
    }

    @Override
    public boolean isExcluded(Object s) {
        // TODO Auto-generated method stub
        return excludes.contains(((ContactBean) s).getPhoneNumber());
    }

    @Override
    public void addExcludesPhone(String s) {
        // TODO Auto-generated method stub
        excludes.add(s);
    }

    @Override
    public void removeExcludes(Object s) {
        // TODO Auto-generated method stub
        if (!"".equals(((ContactBean) s).getPhoneNumber()) && null != ((ContactBean) s).getPhoneNumber()) {
            excludes.remove(((ContactBean) s).getPhoneNumber());
        }
    }

    @Override
    public void clearExcludes() {
        // TODO Auto-generated method stub
        excludes.clear();
    }

    public boolean isFiltered(String s) {
        return filtereds.contains(s);
    }

    Filter nameFilter = new Filter() {

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            str = (String) resultValue;
            return str;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint != null) {
                buffer.clear();
                filtereds.clear();
                try {
                    for (int i = 0; i < items.size(); i++) {
                        if (items.get(i).getPhoneNumber().toLowerCase().startsWith(constraint.toString().toLowerCase())) {
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
}