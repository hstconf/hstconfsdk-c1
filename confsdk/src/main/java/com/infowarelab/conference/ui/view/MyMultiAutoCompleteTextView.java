package com.infowarelab.conference.ui.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.infowarelab.conference.localDataCommon.ContactDataCommon;
import com.infowarelab.conference.localDataCommon.LocalCommonFactory;
import com.infowarelab.conference.localDataCommon.impl.ContactDataCommonImpl;
import com.infowarelab.conference.ui.adapter.MultiAutoAdapter;
import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.domain.ContactBean;
import com.infowarelabsdk.conference.domain.MemberChip;
import com.infowarelabsdk.conference.util.Constants;
import com.infowarelabsdk.conference.util.StringUtil;
import com.infowarelabsdk.conference.util.ToastUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class MyMultiAutoCompleteTextView extends MultiAutoCompleteTextView {
    private Context context;
    private MultiAutoAdapter autoMultiAdapter;
    private Button confirm;
    //本机通讯录联系人
    private ArrayList<ContactBean> list;

    private ContactDataCommon common;
    //已添加的联系人
    public LinkedHashMap<Object, ContactBean> map_number;
    private ContactBean contact = null;
    private Tokenizer mTokenizer;
    //是否是准备删除状态
    public static boolean isReadyDel;
    public static boolean isEmpty = true;
    private boolean flag = true;
    private String beginingDisplayKey = "";
    private LayoutInflater inflater;
    private Iterator<Object> iterator;

    public MyMultiAutoCompleteTextView(Context context) {
        super(context);
        this.context = context;
        init();
        initListener();
    }

    public MyMultiAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
        initListener();
    }

    public MyMultiAutoCompleteTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init();
        initListener();
    }

    private void init() {
        common = LocalCommonFactory.getInstance().getContactDataCommon();
        if (ContactDataCommonImpl.isFinishGetContacts) {
            list = common.getContactList();
        } else {
            list = new ArrayList<ContactBean>();
        }

        map_number = (LinkedHashMap<Object, ContactBean>) common.getMap().clone();
        inflater = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        autoMultiAdapter = new MultiAutoAdapter(this, context, list);
        setAdapter(autoMultiAdapter);
        setThreshold(1);
        setDropDownBackgroundResource(R.drawable.search_drop_bg);
        //设置选择框透明度
        getDropDownBackground().setAlpha(100);
        mTokenizer = new SpaceTokenizer();
        setTokenizer(mTokenizer);
        setCursorVisible(true);

        //保存已添加的联系人
        if (!map_number.isEmpty()) {
            isEmpty = false;
            iterator = map_number.keySet().iterator();
            while (iterator.hasNext()) {
                contact = map_number.get(iterator.next());
                contact.setReadyDel(false);

                append(contact.getName() + " ");
                if (common.getContactDel() != null && contact.getDisplayKey().equals(common.getContactDel().getDisplayKey())) {
                    contact.setReadyDel(true);
                    beginingDisplayKey = contact.getDisplayKey();
                }

                if (contact.isReadyDel()) {
                    setListDelPref(contact);
                }
                ((MultiAutoAdapter) getAdapter()).addExcludeIdxs(contact.getPhoneNumber());
            }
            setChannel();
            setSelectionEnd();
        }
    }

    /**
     * 保持已添加联系人的删除状态
     *
     * @param contact
     */
    private void setListDelPref(ContactBean contact) {
        for (ContactBean con : list) {
            if (con.getDisplayKey().equals(contact.getDisplayKey())) {
                con.setReadyDel(true);
                break;
            }
        }
    }

    public void refresh() {
        if (list == null || list.isEmpty()) {
            list = common.getContactList();
            autoMultiAdapter.refreshAdapter(list);
        }
    }


    /**
     * 设置监听器
     */
    private void initListener() {

        //监听按键
        this.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    int j = getSelectionEnd();
                    //如果光标前不是空格键则按Del键时实现删除原效
                    if (j <= 0 || !(getText().charAt(j - 1) == ' '))
                        return false;
                    else {
                        //处理对某个联系人项按一次Del事件，包括联系人间切换删除状态
                        if (ContactDataCommonImpl.preStartIndex < 0) {
                            contact = getContactFromSpan(0, j);
                            setReadyDelTurn(contact);
                            ContactDataCommonImpl.preStartIndex = j - contact.getName().length() - 1;
                            ContactDataCommonImpl.preEndIndex = j - 1;
                            setChannel();
                            common.setContactDel(contact);
                        } else {
                            //处理对某个联系人项连续按两次Del事件
                            contact = getContactFromSpan(ContactDataCommonImpl.preStartIndex, ContactDataCommonImpl.preEndIndex);
                            if (contact != null) {
                                System.out.println(contact.getName());
                            }
                            setReadyDelTurn(contact);
                            deleteClipView(contact, ContactDataCommonImpl.preStartIndex, ContactDataCommonImpl.preEndIndex);
                            common.setContactDel(null);
                        }
                        setSelectionEnd();
                        return true;
                    }
                }

                //屏蔽换行键效果
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    fixLastClip();
                    return true;
                }
                return false;
            }
        });

        //监听选择框中item的点击事件
        setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                ContactBean contact = (ContactBean) getAdapter().getItem(position);
                if (contact == null) {
                    return;
                }
                map_number.put(contact.getPhoneNumber(), contact);
                //让Adapter进行筛选使已加入的联系人不显示在下拉框中
                ((MultiAutoAdapter) getAdapter()).addExcludeIdxs(contact.getPhoneNumber());

                setChannel();
                setSelectionEnd();
            }

        });

        //监听点击事件保证每次光标重置都在edit最末尾
        this.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                setSelectionEnd();
            }
        });

        //监听输入框里面的内容变化
        this.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int end, int before,
                                      int count) {
                //System.out.println("onTextChanged "+s+" / "+end+" / "+count);
                if (count >= 1) {
                    //当在输入框点击空格时，获取与前个空格间的字段，在判定符合输入规则后生成MemberChip
                    turnConfirmButtonState();

                    if (s.charAt(end) == ' ') {
                        int i = end;
                        while (i > 0 && !(getText().charAt(i - 1) == ' ')) {
                            i--;
                        }

                        String str_edit = getText().subSequence(i, end).toString();
                        searchContactInAutoMulti(str_edit, i, end);
                        setSelectionEnd();
                    }
                } else {
                    turnConfirmButtonState();
                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                flag = false;
                setSelectionEnd();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void turnConfirmButtonState() {
        if (getText().toString().trim().length() >= 1) {
            confirm.setEnabled(true);
        } else {
            confirm.setEnabled(false);
        }
    }

    /**
     * 生成edit内容
     */
    public void setChannel() {
        if (getText().toString().charAt(getText().length() - 1) == ' ') {
            System.out.println("setChannel");
            SpannableStringBuilder ssb = new SpannableStringBuilder(getText());
            int x = 0;
            iterator = map_number.keySet().iterator();
            while (iterator.hasNext()) {
                contact = map_number.get(iterator.next());
                String display = contact.getName();
                //使重新进入该页面时保持联系人准备删除状态，切换或删除功能有效
                if (!flag && beginingDisplayKey.equals(contact.getDisplayKey())) {
                    contact.setReadyDel(true);
                    beginingDisplayKey = "";
                }

                ClipView clipView = createChannelTextView(display, contact.isReadyDel());
                Bitmap bitmap = clipView.setViewBitmap();
                BitmapDrawable bmd = new BitmapDrawable(getResources(), bitmap);
                bmd.setBounds(0, 0, bmd.getIntrinsicWidth(), bmd.getIntrinsicHeight());
                MemberChip chip = new MemberChip(bmd, contact);
                if (display.length() != 0) {
                    ssb.setSpan(chip, x, x + display.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                //使输入框中的item可以被点击
                ClickableSpan clickSpan = new ClickableSpan() {

                    @Override
                    public void onClick(View view) {
                        int i = ((EditText) view).getSelectionStart();
                        int j = ((EditText) view).getSelectionEnd();
                        //长按时出现i == j的情况，此时将i赋值为-2
                        if (i == j) {
                            i = -2;
                            //如果长按时j位置的char 为‘ ’时将j推前
                            if (j != 0 && getText().toString().charAt(j - 1) == ' ') {
                                j = j - 1;
                            }
                        }

                        contact = getContactFromSpan(i, j);
                        setReadyDelTurn(contact);

                        if (i == -2) {
                            if (j == 0) {
                                j = contact.getName().length();
                            }
                            i = j - contact.getName().length();
                        }

                        flag = false;
                        beginingDisplayKey = "";
                        if (contact.isReadyDel()) {
                            common.setContactDel(contact);
                            if (ContactDataCommonImpl.preStartIndex >= 0 && ContactDataCommonImpl.preStartIndex != i) {
                                //删除状态切换功能实现，使之前是删除状态的item改变状态
                                contact = getContactFromSpan(ContactDataCommonImpl.preStartIndex, ContactDataCommonImpl.preEndIndex);
                                contact.setReadyDel(false);
                            }

                            //当两次点击的是同一个item
                            if (ContactDataCommonImpl.preStartIndex >= 0 && ContactDataCommonImpl.preStartIndex == i) {
                                deleteClipView(contact, i, j);
                                common.setContactDel(null);
                                return;
                            }

                            //保存前一个点击item的开始preStartIndex和结束preEndIndex位置
                            ContactDataCommonImpl.preStartIndex = i;
                            ContactDataCommonImpl.preEndIndex = j;
                            setChannel();
                            return;
                        } else {
                            deleteClipView(contact, i, j);
                            common.setContactDel(null);
                            System.out.println("hhhhhhhh");
                            return;
                        }
                    }

                };
                setMovementMethod(LinkMovementMethod.getInstance());
                if (display.length() != 0) {
                    ssb.setSpan(clickSpan, x, x + display.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                x = x + display.length() + 1;
            }
            setText(ssb);
            System.out.println("setChannel End");
        }
    }

    /**
     * 始终保持edit光标在最末尾
     */
    private void setSelectionEnd() {
        setCursorVisible(false);
        CharSequence text = getText();
        if (text instanceof Spannable) {
            Spannable spanText = (Spannable) text;
            Selection.setSelection(spanText, text.length());
        }
        setCursorVisible(true);
    }

    /**
     * 从edit中获取已添加的各个联系人
     *
     * @param i
     * @param j
     * @return
     */
    public ContactBean getContactFromSpan(int i, int j) {
        boolean isSelectTop = false;
        if (i == -1 && j == -1) {
            return null;
        }

        if (i < -1) {
            i = 0;
            //当长按到最前时获取第一个contact
            if (j == 0) {
                j = getText().length();
                isSelectTop = true;
            }
        }
        SpannableStringBuilder ssb = (SpannableStringBuilder) getText().subSequence(i, j);
        MemberChip[] chips = ssb.getSpans(0, getText().subSequence(i, j).toString().length(), MemberChip.class);
        return isSelectTop ? chips[0].getContactBean() : chips[chips.length - 1].getContactBean();
    }

    /**
     * 改变联系人ContactBean的准备删除状态
     *
     * @param contact
     */
    private void setReadyDelTurn(ContactBean contact) {
        boolean isReadyDel = contact.isReadyDel();
        contact.setReadyDel(!isReadyDel);
    }

    /**
     * 在edit输入框中删除某一项的联系人
     *
     * @param contact
     * @param i
     * @param j
     */
    private void deleteClipView(ContactBean contact, int i, int j) {
        replaceText(i, j);
        ContactDataCommonImpl.preStartIndex = -1;
        ContactDataCommonImpl.preEndIndex = -1;
        map_number.remove(contact.getDisplayKey());
        ((MultiAutoAdapter) getAdapter()).removeExcludeIdxs(contact.getPhoneNumber());
    }

    /**
     * 替换edit输入框中的某个字段
     *
     * @param i
     * @param j
     */
    private void replaceText(int i, int j) {
        if (getText().toString().contains(" ")) {
            //+1是因为存在空格键' '
            getText().replace(Math.min(i, j), Math.max(i, j) + 1, "", 0, 0);
        } else {
            //当空格键已移除时
            getText().replace(Math.min(i, j), Math.max(i, j), "", 0, 0);
        }
    }

    /**
     * 创建一个TextView的项并为其绘画好背景
     *
     * @param channelName
     * @param isReadyDel
     * @return
     */
    public ClipView createChannelTextView(String channelName, boolean isReadyDel) {
        ClipView clipView = (ClipView) inflater.inflate(R.layout.invite_contact_edititem, null);
        if (isReadyDel) {
            clipView.setBackgroundResource(R.drawable.a6_bg_et_clip);
        } else {
            clipView.setBackgroundResource(R.drawable.a6_bg_et_clip);
        }

        clipView.setText(channelName);
        return clipView;
    }

    /**
     * 当点击最近联系人的列表时，将符合规则的item加入到输入框中
     *
     * @param contact
     */
    public void appendMultiAuto(ContactBean contact) {
        map_number.put(contact.getDisplayKey(), contact);
        append(contact.getName() + " ");
        setChannel();
        ((MultiAutoAdapter) getAdapter()).addExcludeIdxs(contact.getPhoneNumber());
        requestFocus();
        setSelectionEnd();
    }

    /**
     * 在本机通讯录中搜索联系人，判断输入的项是否存在其中
     *
     * @param edit
     * @param i
     * @param start
     * @return
     */
    private ContactBean searchContactInList(String edit, int i, int start) {
        if (list.isEmpty()) {
            return null;
        }

        for (ContactBean contact : list) {
            if (contact.getPhoneNumber().equals(edit) ||
                    contact.getEmail().equals(edit) ||
                    (contact.getName().equals(edit) &&
                            !map_number.containsKey(contact.getPhoneNumber()))) {
                return contact;
            }
        }

        return null;
    }

    /**
     * 确认输入的项是手机号码还是邮箱地址
     *
     * @param edit
     * @param start
     * @param end
     */
    private void searchContactInAutoMulti(String edit, int start, int end) {
        ContactBean contact;
        if (edit.equals(" ") || edit.equals("")) {
            getText().replace(start, end + 1, "");
            //ToastUtil.showMessage(context, "请输入正确的联系方式", Toast.LENGTH_SHORT);
            return;
        }

        iterator = map_number.keySet().iterator();
        while (iterator.hasNext()) {
            contact = map_number.get(iterator.next());
            if (contact.getPhoneNumber().equals(edit) ||
                    contact.getEmail().equals(edit) ||
                    //需要考虑到同名情况，此时手机号码和邮箱是联系人主键
                    (contact.getName().equals(edit) &&
                            map_number.containsKey(contact.getPhoneNumber()))) {
                getText().replace(start, end + 1, "");
                ToastUtil.showMessage(context, context.getString(R.string.preconf_create_user_exist), Toast.LENGTH_SHORT);
                return;
            }
        }

        contact = searchContactInList(edit, start, end);
        if (contact == null) {
            if (StringUtil.checkInput(edit, Constants.EDIT_EMAIL)) {
                contact = new ContactBean(edit, "", edit, false);
            } else if (StringUtil.checkInput(edit, Constants.EDIT_PHONENUMBER)) {
                contact = new ContactBean(edit, edit, "", false);
            } else {
                getText().replace(start, end + 1, "");
                ToastUtil.showMessage(context, context.getString(R.string.preconf_create_input_wrong), Toast.LENGTH_SHORT);
                return;
            }
        }

        map_number.put(contact.getDisplayKey(), contact);
        getText().replace(start, end, contact.getName());
        setChannel();
        ((MultiAutoAdapter) getAdapter()).addExcludeIdxs(contact.getPhoneNumber());
    }

    public void fixLastClip() {
        String str = getText().toString();
        append(" ");
        //setChannel();
        setSelectionEnd();
    }

    public LinkedHashMap<Object, ContactBean> getContacts() {
        return map_number;
    }

    public void setInviteButton(Button button) {
        this.confirm = button;
    }

    /**
     * Tokenizer默认间隔符号是','，现在改成' '空格符
     *
     * @author fz
     */
    public class SpaceTokenizer implements Tokenizer {

        public int findTokenStart(CharSequence text, int cursor) {
            int i = cursor;
            while (i > 0 && text.charAt(i - 1) != ' ') {
                i--;
            }
            while (i < cursor && text.charAt(i) == ' ') {
                i++;
            }

            return i;
        }

        public int findTokenEnd(CharSequence text, int cursor) {
            int i = cursor;
            int len = text.length();
            while (i < len) {
                if (text.charAt(i) == ' ') {
                    return i;
                } else {
                    i++;
                }
            }

            return len;
        }

        public CharSequence terminateToken(CharSequence text) {
            int i = text.length();
            while (i > 0 && text.charAt(i - 1) == ' ') {
                i--;
            }

            if (i > 0 && text.charAt(i - 1) == ' ') {
                return text;
            } else {
                if (text instanceof Spanned) {
                    SpannableString sp = new SpannableString(text + " ");
                    TextUtils.copySpansFrom((Spanned) text, 0, text.length(), Object.class, sp, 0);
                    return sp;
                } else {
                    return text + " ";
                }
            }
        }
    }

}

