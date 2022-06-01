package com.infowarelab.conference.ui.adapter;

import java.util.List;

import androidx.viewpager.widget.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author joe.xiao
 * @Date 2013-9-9下午4:53:14
 * @Email joe.xiao@infowarelab.com
 */
public class ConferencePagerAdapter extends PagerAdapter {

    private List<View> views;

    public ConferencePagerAdapter(List<View> views) {
        this.views = views;
    }

    @Override
    public int getCount() {
        return views.size() > 0 ? views.size() : 0;
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1 && arg0.equals(arg1);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(views.get(position));
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(views.get(position));
        return views.get(position);
    }

}
