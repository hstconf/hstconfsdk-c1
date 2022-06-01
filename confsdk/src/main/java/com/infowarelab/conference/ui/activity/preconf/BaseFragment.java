package com.infowarelab.conference.ui.activity.preconf;


import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.infowarelab.conference.ui.activity.preconf.fragment.FragCreate;
import com.infowarelab.conference.ui.view.LodingDialog;

public class BaseFragment extends Fragment {
    private BaseFragmentActivity baseFragmentActivity = null;
    protected LodingDialog loadingDialog = null;

    protected onSwitchPageListener switchPageListener;

    public interface onSwitchPageListener {
        void doSelect(int position);
    }

    public void setOnSwitchPageListener(onSwitchPageListener listener) {
        this.switchPageListener = listener;
    }

    public void setBaseFragmentActivity(BaseFragmentActivity activity) {
        this.baseFragmentActivity = activity;
    }

    protected void hideInput() {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != getActivity().getCurrentFocus().getWindowToken()) {
            inputMethodManager.hideSoftInputFromWindow(getActivity()
                            .getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void hideInput(View v) {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != getActivity().getCurrentFocus() && null != getActivity().getCurrentFocus().getApplicationWindowToken()) {
            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus()
                            .getApplicationWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void showInput(View v) {
        ((InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE)).showSoftInput(v, 0);
    }

    public void showLongToast(int resId) {
        Toast.makeText(getActivity(), resId, Toast.LENGTH_LONG).show();
    }

    public void showShortToast(int resId) {
        Toast.makeText(getActivity(), resId, Toast.LENGTH_SHORT).show();
    }

    public void showLoading() {
        if (baseFragmentActivity != null) {
            baseFragmentActivity.showLoading();
        }
        else
        {
            if (loadingDialog == null) {
                loadingDialog = new LodingDialog(getActivity());
            }

            if (loadingDialog != null && !loadingDialog.isShowing()) {
                loadingDialog.show();
            }
        }
    }

    public void hideLoading() {
        if (baseFragmentActivity != null) {
            baseFragmentActivity.hideLoading();
        }
        else
        {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
                loadingDialog = null;
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        // TODO Auto-generated method stub
        if (baseFragmentActivity != null) {
            baseFragmentActivity.hideLoading();
        }
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        if (baseFragmentActivity != null) {
            baseFragmentActivity.hideLoading();
        }

        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }

        super.onDestroyView();
    }

}
