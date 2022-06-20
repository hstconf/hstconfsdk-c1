package com.infowarelab.conference.live

import android.text.SpannableStringBuilder

class LiveMessageModel : LiveMessageIntrinsicModel() {

    //Transient表示不参与序列化，json解析库也不解析该字段
    @Transient
    var spannableString : SpannableStringBuilder? = null

}