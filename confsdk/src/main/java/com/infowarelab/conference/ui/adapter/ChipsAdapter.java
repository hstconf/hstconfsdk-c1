package com.infowarelab.conference.ui.adapter;


import android.widget.BaseAdapter;
import android.widget.Filterable;

public abstract class ChipsAdapter extends BaseAdapter implements Filterable {

    public abstract void addExcludes(Object s);

    public abstract boolean isExcluded(Object s);

    public abstract void addExcludesPhone(String s);

    public abstract void removeExcludes(Object s);

    public abstract void clearExcludes();
}
