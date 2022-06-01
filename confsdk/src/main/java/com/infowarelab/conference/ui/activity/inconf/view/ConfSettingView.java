package com.infowarelab.conference.ui.activity.inconf.view;

import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.infowarelab.conference.ui.activity.inconf.BaseFragment;
import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.UserCommonImpl;
import com.infowarelabsdk.conference.util.Constants;
import com.infowarelabsdk.conference.util.FileUtil;
import com.infowarelabsdk.conference.util.StringUtil;


public class ConfSettingView extends BaseFragment implements View.OnClickListener {
    private View settingView;

    private ImageView iv1, iv2, iv3, ivClear;
    private LinearLayout ll1, ll2, ll3;
    private EditText etNickName;
    private String curNickName = "";
    private View placeTop, placeBottom;

    public ConfSettingView(ICallParentView iCallParentView) {
        super(iCallParentView);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        settingView = inflater.inflate(R.layout.conference_setting, container, false);
        initView();
        return settingView;
    }

    private void initView() {
        iv1 = (ImageView) settingView.findViewById(R.id.setting_iv_r1);
        iv2 = (ImageView) settingView.findViewById(R.id.setting_iv_r2);
        iv3 = (ImageView) settingView.findViewById(R.id.setting_iv_r3);
        ivClear = (ImageView) settingView.findViewById(R.id.setting_iv_clear);
        ll1 = (LinearLayout) settingView.findViewById(R.id.setting_ll_r1);
        ll2 = (LinearLayout) settingView.findViewById(R.id.setting_ll_r2);
        ll3 = (LinearLayout) settingView.findViewById(R.id.setting_ll_r3);
        etNickName = (EditText) settingView.findViewById(R.id.setting_et_nickname);
        placeTop = settingView.findViewById(R.id.view_inconf_setting_place_top);
        placeBottom = settingView.findViewById(R.id.view_inconf_setting_place_bottom);

        ll1.setOnClickListener(this);
        ll2.setOnClickListener(this);
        ll3.setOnClickListener(this);
        ivClear.setOnClickListener(this);

        etNickName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    String txt = etNickName.getText().toString().trim();
                    if (!txt.equals("")) {
                        if (StringUtil.checkInput(txt, Constants.PATTERN)) {
                            try {
                                txt = idgui2(txt, 10);
                            } catch (Exception e) {
                            }
                            ((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).setUserName(((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).getSelf().getUid(),
                                    txt);
                            ((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).getSelf().setUsername(txt);
                            FileUtil.saveSharedPreferences(getActivity(), Constants.SHARED_PREFERENCES,
                                    Constants.LOGIN_JOINNAME, txt);
                            hideInput(etNickName);
                            etNickName.setText(txt);
                            etNickName.clearFocus();
                        } else {
                            showShortToast(R.string.illegalCharacter);
                            return true;
                        }
                    } else {
                        etNickName.setText(curNickName);
                        hideInput(etNickName);
                        etNickName.clearFocus();
                    }
                }

                return false;
            }
        });
        etNickName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String name = ((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).getSelf().getUsername().trim();
                if (hasFocus) {
                    etNickName.setText(name);
                    etNickName.setSelection(name.length());
                } else {
                    curNickName = cutName(name, 10);
                    etNickName.setText(curNickName);
                }

            }
        });
//        etNickName.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if(etNickName.getText().toString().length() > 0){
//                    ivClear.setVisibility(View.VISIBLE);
//                }else if(etNickName.isFocused()){
//                    ivClear.setVisibility(View.GONE);
//                }else{
//                    ivClear.setVisibility(View.GONE);
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });

        setResolution();
        setNickName();
        setPlace();

    }

    private void setResolution() {
        String defaultRes = FileUtil.readSharedPreferences(
                getActivity(), Constants.SHARED_PREFERENCES, Constants.DEFAULT_RESOLUTION);
        if (defaultRes == null || defaultRes.equals("")) {
            selectR(1);
        } else {
            iv1.setVisibility(View.GONE);
            iv2.setVisibility(View.GONE);
            iv3.setVisibility(View.GONE);
            if (defaultRes.equals("L")) {
                iv1.setVisibility(View.VISIBLE);
            } else if (defaultRes.equals("M")) {
                iv2.setVisibility(View.VISIBLE);
            } else {
                iv3.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setNickName() {
        String name = ((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).getSelf().getUsername().trim();
        curNickName = cutName(name, 10);
        etNickName.setText(curNickName);
    }

    private void setPlace() {
//        Configuration mConfiguration = this.getResources().getConfiguration();
//        int ori = mConfiguration.orientation;
//        if (ori == mConfiguration.ORIENTATION_LANDSCAPE) {
//            placeTop.setVisibility(View.VISIBLE);
//            placeBottom.setVisibility(View.VISIBLE);
//        } else if (ori == mConfiguration.ORIENTATION_PORTRAIT) {
//            placeTop.setVisibility(View.GONE);
//            placeBottom.setVisibility(View.GONE);
//        }
    }

    public void changeOrietation(Configuration newConfig) {
//        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
//            placeTop.setVisibility(View.VISIBLE);
//            placeBottom.setVisibility(View.VISIBLE);
//        }else if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
//            placeTop.setVisibility(View.GONE);
//            placeBottom.setVisibility(View.GONE);
//        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.setting_ll_r1) {
            selectR(1);
            callParentView(ACTION_SETRESOLUTION, "L");
        } else if (id == R.id.setting_ll_r2) {
            selectR(2);
            callParentView(ACTION_SETRESOLUTION, "M");
        } else if (id == R.id.setting_ll_r3) {
            selectR(3);
            callParentView(ACTION_SETRESOLUTION, "H");
        } else if (id == R.id.setting_iv_clear) {
            etNickName.setText("");
            etNickName.setFocusableInTouchMode(true);
            etNickName.requestFocus();
            showInput(etNickName);
        }
    }


    private void selectR(int which) {
        switch (which) {
            case 1:
                FileUtil.saveSharedPreferences(getActivity(), Constants.SHARED_PREFERENCES, Constants.DEFAULT_RESOLUTION, "L");
                iv1.setVisibility(View.VISIBLE);
                iv2.setVisibility(View.GONE);
                iv3.setVisibility(View.GONE);
                break;
            case 2:
                FileUtil.saveSharedPreferences(getActivity(), Constants.SHARED_PREFERENCES, Constants.DEFAULT_RESOLUTION, "M");
                iv1.setVisibility(View.GONE);
                iv2.setVisibility(View.VISIBLE);
                iv3.setVisibility(View.GONE);
                break;
            case 3:
                FileUtil.saveSharedPreferences(getActivity(), Constants.SHARED_PREFERENCES, Constants.DEFAULT_RESOLUTION, "H");
                iv1.setVisibility(View.GONE);
                iv2.setVisibility(View.GONE);
                iv3.setVisibility(View.VISIBLE);
                break;

            default:
                break;
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            setResolution();
            setNickName();
            setPlace();
        }
    }

    public boolean getOnBackPressed() {
        return true;
    }


    private String idgui2(String s, int num) throws Exception {
        int changdu = s.getBytes("GBK").length;
        if (changdu > num) {
            s = s.substring(0, s.length() - 1);
            s = idgui2(s, num);
        }
        return s;
    }
}
