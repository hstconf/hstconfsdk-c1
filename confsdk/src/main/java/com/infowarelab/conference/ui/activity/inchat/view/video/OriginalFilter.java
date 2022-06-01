package com.infowarelab.conference.ui.activity.inchat.view.video;

import android.content.Context;

import com.infowarelab.hongshantongphone.R;

public class OriginalFilter extends BaseFilter {

    public OriginalFilter(Context c) {
        super(c);

    }

    @Override
    public void setPath() {

        path1 = R.raw.base_vertex_shader;
        path2 = R.raw.base_fragment_shader;

    }

    @Override
    public void onDrawArraysPre() {

    }

    @Override
    public void onDrawArraysAfter() {

    }


}
