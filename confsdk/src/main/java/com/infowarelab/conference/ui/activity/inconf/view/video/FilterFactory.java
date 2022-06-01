package com.infowarelab.conference.ui.activity.inconf.view.video;

import android.content.Context;


public class FilterFactory {

    public enum FilterType {

        SkinWhiten,
        BlackWhite,
        BlackCat,
        WhiteCat,
        Healthy,
        Romance,
        Original,
        Sunrise,
        Sunset,
        Sakura,
        Latte,
        Warm,
        Calm,
        Brooklyn,
        Cool,
        Sweets,
        Amaro,
        Antique,
        Brannan,
        Beauty
    }

    public static BaseFilter createFilter(Context c, FilterType filterType) {

        BaseFilter baseFilter = null;

        switch (filterType) {
            case Original:
            default:
                baseFilter = new OriginalFilter(c);
                break;

        }

        return baseFilter;
    }


}
