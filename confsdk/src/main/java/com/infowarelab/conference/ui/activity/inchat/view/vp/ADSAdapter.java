package com.infowarelab.conference.ui.activity.inchat.view.vp;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by Always on 2018/12/12.
 */

public class ADSAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragmentList;

    private ADSAdapter(FragmentManager fm) {
        super(fm);
    }

    public ADSAdapter(FragmentManager fm, List<Fragment> list) {
        super(fm);
        this.fragmentList = list;
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }
}
