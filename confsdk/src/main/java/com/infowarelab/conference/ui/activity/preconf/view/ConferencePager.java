package com.infowarelab.conference.ui.activity.preconf.view;


////import org.apache.log4j.Logger;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

public abstract class ConferencePager {

    //protected Logger log = Logger.getLogger(this.getClass());

    protected Activity mActivity;

    protected Context mContext;

    protected LayoutInflater mInflater;

    public ConferencePager(Activity activity) {
        this.mActivity = activity;
        this.mContext = activity;
        mInflater = LayoutInflater.from(mContext);
    }

    public abstract View getNewView();

    protected void showShortToast(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

}
