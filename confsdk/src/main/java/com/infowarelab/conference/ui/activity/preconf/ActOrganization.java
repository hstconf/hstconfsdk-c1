package com.infowarelab.conference.ui.activity.preconf;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.infowarelab.conference.BaseActivity;
import com.infowarelab.conference.ui.adapter.OrgsAdapter;
import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.transfer.Config;
import com.infowarelabsdk.conference.transfer.Department;
import com.infowarelabsdk.conference.transfer.OrgItem;
import com.infowarelabsdk.conference.util.Constants;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

public class ActOrganization extends BaseActivity implements OnClickListener {
    public static Set<String> lastUsers;
    public static Hashtable<String, String> lastUserandnames;
    public final static int REQ_ORG = 95;
    private LinearLayout llSelecteds, llBack;
    private TextView tvCurOrg, tvNull, tv2Root;
    private ListView lvOrgs;
    private Button btnConfirm;
    private ImageView ivBack;

    private int userId;
    private String siteId;

    private Department rootDepartment;
    private Department curDepartment;

    private OrgsAdapter orgsAdapter;

    private Set<String> selectedUsers;
    private Hashtable<String, String> selectedUserandnames;
    //保存邮箱
    private Hashtable<String, String> selectedUserandemils;

    private Hashtable<String, String> keys;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a6_preconf_organization);
        initView();

    }

    private void initView() {
        selectedUsers = new HashSet<String>();
        selectedUserandnames = new Hashtable<String, String>();
        selectedUserandemils = new Hashtable<>();
        keys = new Hashtable<String, String>();

        llSelecteds = (LinearLayout) findViewById(R.id.view_frag_org_ll_1);
        llBack = (LinearLayout) findViewById(R.id.act_preconf_org_ll_back);
        lvOrgs = (ListView) findViewById(R.id.view_frag_org_lv);
        tvCurOrg = (TextView) findViewById(R.id.view_frag_org_tv_curorg);
        tvNull = (TextView) findViewById(R.id.view_frag_org_tv_null);
        tv2Root = (TextView) findViewById(R.id.view_frag_org_tv_toroot);
        ivBack = (ImageView) findViewById(R.id.view_frag_org_iv_back);
        btnConfirm = (Button) findViewById(R.id.view_frag_org_btn_confirm);

        orgsAdapter = new OrgsAdapter(this);
        orgsAdapter.setItemCheck(new OrgsAdapter.ItemCheck() {
            @Override
            public void check(OrgItem org, View v) {
                if (org.getType() == 1) {
                    toNextDepartment(curDepartment.getOrgs().get(org.getId()));
                } else {
                    if (selectedUsers.contains(org.getId())) {
                        selectedUsers.remove(org.getId());
                        v.setVisibility(View.GONE);
                        removeClip(org.getId());
                    } else {
                        selectedUsers.add(org.getId());
                        selectedUserandnames.put(org.getId(), org.getName());
                        selectedUserandemils.put(org.getId(), org.getEmails());
                        v.setVisibility(View.VISIBLE);
                        addClip(org);
                    }
                }
            }
        });
        lvOrgs.setAdapter(orgsAdapter);

        tvCurOrg.setText("Root");
        tvNull.setVisibility(View.GONE);

        llBack.setOnClickListener(this);
        tv2Root.setOnClickListener(this);
        ivBack.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);

        orgHandler.sendEmptyMessage(0);
    }

    private void getSiteConfig() {
        userId = this.getSharedPreferences(Constants.SHARED_PREFERENCES,
                this.MODE_PRIVATE).getInt(Constants.USER_ID, 0);
        siteId = this.getSharedPreferences(Constants.SHARED_PREFERENCES,
                this.MODE_PRIVATE).getString(Constants.SITE_ID, "");
    }


    private void toRootDepartment() {
        tvCurOrg.setText("Root");
        updateList(new Department());
        keys.clear();
        curDepartment = rootDepartment;
        getRootOrgnization();
    }


    private void toNextDepartment(Department next) {
        if (next == null || next.getOrgId() == null) {
            return;
        }

        tvCurOrg.setText(next.getOrgName());
        updateList(new Department());

        keys.put(curDepartment.getOrgId(), next.getOrgId());

        curDepartment = next;
        getDepartment();
    }

    private void toPreDepartment() {

        if (rootDepartment == null) return;

        Department department = getOrg(rootDepartment);
        keys.remove(department.getOrgId());

        tvCurOrg.setText(department.getOrgName());
        updateList(new Department());

        curDepartment = department;
        getDepartment();
    }

    private Department getOrg(Department department) {
        if (keys.containsKey(department.getOrgId())) {
            Department department1 = department.getOrgs().get(keys.get(department.getOrgId()));
            if (keys.containsKey(department1.getOrgId())) {
                return getOrg(department1);
            } else {
                return department;
            }
        } else {
            return department;
        }
    }

    private void updateList(Department department) {
        orgsAdapter.update(department, selectedUsers);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.view_frag_org_iv_back) {
            toPreDepartment();
        } else if (id == R.id.view_frag_org_tv_toroot) {
            toRootDepartment();
        } else if (id == R.id.act_preconf_org_ll_back) {
            finishWithResult(RESULT_CANCELED);
        } else if (id == R.id.view_frag_org_btn_confirm) {
            if (selectedUsers.size() > 0) {
                lastUsers = new HashSet<>(selectedUsers);
                lastUserandnames = new Hashtable<>(selectedUserandnames);
                Intent intent = new Intent();
                intent.putExtra("key1", getSelectedIds());
                intent.putExtra("key2", getSelectedNames());
                intent.putExtra("key3", getSelectedEmails());
                setResult(RESULT_OK, intent);
                finish();
            } else {
                finishWithResult(RESULT_CANCELED);
            }
        }

    }


    private void getRootOrgnization() {
        showLoading();
        new Thread() {
            @Override
            public void run() {
                if (lastUsers != null && !lastUsers.isEmpty() && lastUserandnames != null && !lastUserandnames.isEmpty()) {
                    selectedUsers.addAll(lastUsers);
                    selectedUserandnames.putAll(lastUserandnames);
                    Iterator<String> it = selectedUserandnames.keySet().iterator();
                    while (it.hasNext()) {
                        String id = it.next();
                        if (selectedUsers.contains(id)) {
                            addClip(new OrgItem(id, selectedUserandnames.get(id)));
                        }
                    }
                }
                String xml = Config.getSiteOrgnization(userId, siteId, "1");
                rootDepartment = Department.getDepartmentFromXml(xml);
                if (rootDepartment != null) {
                    rootDepartment.setOrgId("1");
                    rootDepartment.setOrgCode("0001");
                    rootDepartment.setOrgName("Root");
                    rootDepartment.setOrgSequence(0);
                }
                orgHandler.sendEmptyMessage(1);
            }
        }.start();
    }

    private void getDepartment() {
        showLoading();
        new Thread() {
            @Override
            public void run() {
                String xml = Config.getSiteOrgnization(userId, siteId, curDepartment.getOrgId());
                Department.getNextFromXML(xml, curDepartment);
                orgHandler.sendEmptyMessage(2);
            }
        }.start();
    }

    Handler orgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    getSiteConfig();
                    getRootOrgnization();
                    break;
                case 1:
                    hideLoading();
                    curDepartment = rootDepartment;
                    updateList(curDepartment);
                    //Log.e("ttttt", "用户量::" + curDepartment.getUsers().size());
                    //Log.i("always", "always department=" + rootDepartment.toString());
                    break;
                case 2:
                    hideLoading();
                    updateList(curDepartment);
                    //Log.i("always", "always department=" + curDepartment.toString());
                    break;

                default:
                    break;
            }

        }
    };

    private TextView createClipTv(OrgItem org) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, getResources().getDimensionPixelOffset(R.dimen.dp_14));
        lp.setMargins(10, 0, 0, 0);
        TextView clipView = (TextView) getLayoutInflater().inflate(R.layout.org_item_tv, null);
        clipView.setText(org.getName());
        clipView.setTag(org.getId());
        clipView.setLayoutParams(lp);
        clipView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedUsers.remove((String) v.getTag());
                removeClip((String) v.getTag());
                orgsAdapter.update(selectedUsers);
            }
        });
        return clipView;
    }

    private void addClip(OrgItem org) {
        View v = llSelecteds.findViewWithTag(org.getId());
        if (v == null) {
            llSelecteds.addView(createClipTv(org));
        }
    }

    private void removeClip(String id) {
        View v = llSelecteds.findViewWithTag(id);
        if (v != null) {
            llSelecteds.removeView(v);
        }
    }

    private String getSelectedIds() {
        StringBuffer s = new StringBuffer();
        for (String str : selectedUsers) {
            s.append(str).append(";");
        }
        return s.toString();
    }

    private String getSelectedEmails() {
        StringBuffer s = new StringBuffer();
        for (String key : selectedUserandemils.keySet()) {
            if (selectedUsers.contains(key)) {
                s.append(selectedUserandemils.get(key) + ";");
            }
        }
        Log.e("ttttt", "邮箱:" + s.toString());
        return s.toString();
    }

    private String getSelectedNames() {
        StringBuffer s = new StringBuffer();
        Iterator<String> it = selectedUserandnames.keySet().iterator();
        while (it.hasNext()) {
            String id = it.next();
            if (selectedUsers.contains(id)) {
                s.append(selectedUserandnames.get(id)).append(";");
            }
        }
        Log.e("ttttt", "选择的名字::" + s.toString());
        return s.toString();
    }
}
