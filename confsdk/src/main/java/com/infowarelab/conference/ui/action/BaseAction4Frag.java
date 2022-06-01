package com.infowarelab.conference.ui.action;


////import org.apache.log4j.Logger;

import android.widget.EditText;
import android.widget.Toast;

import com.infowarelab.conference.ui.activity.preconf.BaseFragmentActivity;
import com.infowarelabsdk.conference.common.CommonFactory;

/**
 * @author joe.xiao
 * @Date 2013-9-11下午4:43:01
 * @Email joe.xiao@infowarelab.com
 */
public class BaseAction4Frag {

    //protected Logger log = Logger.getLogger(getClass());

    protected CommonFactory commonFactory = CommonFactory.getInstance();

    protected BaseFragmentActivity mActivity;


    public BaseAction4Frag(BaseFragmentActivity activity) {
        this.mActivity = activity;

    }

    protected void showLongToast(int resId) {
        Toast.makeText(mActivity, resId, Toast.LENGTH_LONG).show();

    }

    protected void showLongToast(String message) {
        Toast.makeText(mActivity, message, Toast.LENGTH_LONG).show();
    }

    /**
     * 根据ID 取得 Editor
     *
     * @param id
     * @return
     */
    protected EditText getEditorById(int id) {
        return (EditText) mActivity.findViewById(id);
    }

    /**
     * 根据ID 获取文本
     *
     * @param id
     * @return
     */
    protected String getContentById(int id) {
        return isNotNull(id) ? getEditorById(id).getText().toString() : "";
    }

    /**
     * 检测是否存在指定组件
     *
     * @param id
     * @return
     */
    protected boolean isNotNull(int id) {
        if (getEditorById(id) != null)
            return getEditorById(id).getText() == null ? false : getEditorById(id).getText().toString().length() > 0;
        return false;
    }
}
