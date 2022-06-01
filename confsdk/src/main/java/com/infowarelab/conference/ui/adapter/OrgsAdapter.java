package com.infowarelab.conference.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.transfer.Department;
import com.infowarelabsdk.conference.transfer.DepartmentUser;
import com.infowarelabsdk.conference.transfer.OrgItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class OrgsAdapter extends BaseAdapter {
    private Context context;
    private ViewHolder holder;
    private Department department = new Department();
    private Set<String> selecteds = new HashSet<>();
    private List<OrgItem> items;

    public OrgsAdapter(Context context) {
        this.context = context;
//		this.department = department;
//		this.selecteds = selecteds;
        this.items = new ArrayList<OrgItem>();
    }

    public void update(Department department, Set<String> selecteds) {

        if (null == department) return;

        this.department = department;
        this.selecteds = selecteds;
        this.items.clear();
        Hashtable<String, Department> orgs = department.getOrgs();
        if (orgs != null) {
            Iterator<String> it = orgs.keySet().iterator();
            while (it.hasNext()) {
                String id = it.next();
                Department de = orgs.get(id);
                OrgItem org = new OrgItem();
                org.setId(de.getOrgId());
                org.setName(de.getOrgName());
                org.setType(1);
                this.items.add(org);
            }
        }
        List<DepartmentUser> users = department.getUsers();
        if (users != null) {
            for (DepartmentUser user : users) {
                OrgItem org = new OrgItem();
                org.setId(user.getUserId());
                org.setName(user.getUserName());
                org.setEmails(user.getEmail());
                org.setType(2);
                this.items.add(org);
            }
        }
        notifyDataSetChanged();

    }

    public void update(Set<String> selecteds) {
        this.selecteds = selecteds;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.preconf_org_listitem, null);
            holder.ll = (LinearLayout) convertView.findViewById(R.id.item_org_ll);
            holder.tvName = (TextView) convertView.findViewById(R.id.item_org_tv_name);
            holder.ivFile = (ImageView) convertView.findViewById(R.id.item_org_iv_file);
            holder.ivEnter = (ImageView) convertView.findViewById(R.id.item_org_iv_enter);
            holder.ivCheck = (ImageView) convertView.findViewById(R.id.item_org_iv_checked);
            holder.line = convertView.findViewById(R.id.item_org_line);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final OrgItem item = items.get(position);
        holder.tvName.setText(item.getName());
        if (item.getType() == 1) {
            holder.ivFile.setVisibility(View.VISIBLE);
            holder.ivEnter.setVisibility(View.VISIBLE);
            holder.ivCheck.setVisibility(View.GONE);
        } else {
            holder.ivFile.setVisibility(View.INVISIBLE);
            holder.ivEnter.setVisibility(View.GONE);
            if (selecteds.contains(item.getId())) {
                holder.ivCheck.setVisibility(View.VISIBLE);
            } else {
                holder.ivCheck.setVisibility(View.GONE);
            }
        }
        holder.ll.setTag(position);
        holder.ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OrgItem org = items.get((Integer) v.getTag());
                check(org, v.findViewById(R.id.item_org_iv_checked));
            }
        });
        return convertView;
    }

    class ViewHolder {
        LinearLayout ll;
        TextView tvName;
        ImageView ivFile;
        ImageView ivEnter;
        ImageView ivCheck;
        View line;
    }

    private ItemCheck itemCheck;

    public interface ItemCheck {
        public void check(OrgItem org, View v);
    }

    public void setItemCheck(ItemCheck itemCheck) {
        this.itemCheck = itemCheck;
    }

    private void check(OrgItem org, View v) {
        if (this.itemCheck != null) {
            this.itemCheck.check(org, v);
        }
    }
}
