package com.infowarelab.conference.ui.view;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import com.infowarelab.hongshantongphone.R;
import com.infowarelab.conference.ui.adapter.ContactsAdapter4et;
import com.infowarelab.conference.ui.adapter.SortGroupMemberAdapter;
import com.infowarelabsdk.conference.domain.ContactBean;
import com.infowarelabsdk.conference.domain.MemberChip;
import com.infowarelabsdk.conference.util.Constants;
import com.infowarelabsdk.conference.util.StringUtil;
import com.infowarelabsdk.conference.util.ToastUtil;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class AutoEdit extends EditText implements OnItemClickListener, OnClickListener, TextWatcher, Callback {
    private ArrayList<ContactBean> contactList;
    //	private LinkedHashMap<Object, ContactBean> map;
    private Button confirm;
    private ListView listView;
    private ListView listView2;
    private ContactsAdapter4et adapter1;
    private SortGroupMemberAdapter adapter2;
    private LayoutInflater inflater;
    private LinkedHashMap<Object, ContactBean> map;
    private Set<String> mailPre;
    private String lastTxt;

    public AutoEdit(Context context) {
        super(context);
        init();
    }

    public AutoEdit(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AutoEdit(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();

    }

    public void setListview(ListView listView, Button confirm, Adapter adapter) {
        this.listView = listView;
        this.adapter1 = (ContactsAdapter4et) adapter;
        this.confirm = confirm;
        initListener();
    }

    public void setListview(ListView listView1, Adapter adapter1, ListView listView2, Adapter adapter2, Button confirm) {
        this.listView = listView1;
        this.listView2 = listView2;
        this.adapter1 = (ContactsAdapter4et) adapter1;
        this.adapter2 = (SortGroupMemberAdapter) adapter2;
        this.confirm = confirm;
        initListener();
    }

    private void init() {
        inflater = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        contactList = new ArrayList<ContactBean>();
        mailPre = new HashSet<String>();
        lastTxt = "";
    }

    /**
     * 设置监听器
     */
    private void initListener() {
        adapterFilter("");
        listView.setOnItemClickListener(this);
        if (listView2 != null)
            listView2.setOnItemClickListener(this);
        setOnClickListener(this);
        addTextChangedListener(this);
        setCustomSelectionActionModeCallback(this);
        setLongClickable(false);
        setTextIsSelectable(false);
        //监听按键
        this.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    int j = getSelectionEnd();
                    //如果光标前不是断词符则按Del键时实现删除原效
                    if (j <= 0 || !(getText().charAt(j - 1) == ','))
                        return false;
                    else {
                        ContactBean contact = getContactFromSpan(0, j);
                        if (null != contact) {
                            contactList.remove(contact);
//			                			adapter.removeExcludes(contact);
                            removeExcludes(contact);
                            setChips(null);
                            setSelectionEnd();
                        }
                        return true;
                    }
                }

                //换行键效果
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    String chips[] = getText().toString().trim().split(",");
                    if (chips.length > contactList.size()) {
                        searchContactInAutoMulti(chips[chips.length - 1]);
                    } else {
                        setChips(null);
                    }
                    setSelectionEnd();
                    return true;
                }
                return false;
            }
        });
    }

    public void setChips(String editing) {

        if (contactList == null || contactList.size() == 0) {
            if (editing != null && !editing.equals("")) {
                setText(editing);
            } else {
                setText("");
            }
            return;
        }
        mailPre.clear();
        //根据已选list生成字符串
        StringBuffer s = new StringBuffer();
        String mail = "", pre = "", name = "", phone = "";
        for (ContactBean contactBean : contactList) {
            name = contactBean.getName();
            mail = contactBean.getEmail();
            phone = contactBean.getPhoneNumber();
            pre = cutEmail(mail);
            if (pre.equals("")) {
                pre = phone;
            }
            if (mailPre.contains(pre)) {
                if (name != null && !name.equals("")) {
                    s.append(name).append(",");
                } else if (mail != null && !mail.equals("")) {
                    s.append(mail).append(",");
                } else if (phone != null && !phone.equals("")) {
                    s.append(phone).append(",");
                }
//				adapter.addExcludes(contactBean);
                addExcludes(contactBean);
            } else {
                if (name != null && !name.equals("")) {
                    s.append(name).append(",");
                } else if (mail != null && !mail.equals("")) {
                    s.append(mail).append(",");
                } else if (phone != null && !phone.equals("")) {
                    s.append(phone).append(",");
                }
//				adapter.addExcludes(contactBean);
                addExcludes(contactBean);
                mailPre.add(pre);
            }
        }
        if (editing != null) s.append(editing);
        if (s.toString().contains(",")) // check comman in string
        {

            SpannableStringBuilder ssb = new SpannableStringBuilder(s);
            String chips[] = s.toString().trim().split(",");
            int x = 0, j = 0;
            for (String c : chips) {
                ClipView clipView = createChannelTextView(c);
                Bitmap bitmap = clipView.setViewBitmap();
                BitmapDrawable bmpDrawable = new BitmapDrawable(getResources(), bitmap);
                bmpDrawable.setBounds(0, 0, bmpDrawable.getIntrinsicWidth(), bmpDrawable.getIntrinsicHeight());
                MemberChip chip = new MemberChip(bmpDrawable, contactList.get(j));

//				ssb.setSpan(new ImageSpan(bmpDrawable) ,x ,x + c.length() , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(chip, x, x + c.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                //使输入框中的item可以被点击
                ClickableSpan clickSpan = new ClickableSpan() {

                    @Override
                    public void onClick(View view) {

                        int i = ((EditText) view).getSelectionStart();
                        int j = ((EditText) view).getSelectionEnd();
                        Log.i("ALWAYSPRIVILIGE", i + ";" + j);
                        ContactBean contact = getContactFromSpan(i, j);
                        if (contact != null) {
                            contactList.remove(contact);
//							adapter.removeExcludes(contact);
                            removeExcludes(contact);
                            if (contact.getPhoneNumber() != null && !"".equals(contact.getPhoneNumber())) {
                                setChips(contact.getPhoneNumber());
                            } else {
                                setChips(contact.getEmail());
                            }
                        }
                    }

                };
                setMovementMethod(LinkMovementMethod.getInstance());
                ssb.setSpan(clickSpan, x, x + c.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                x = x + c.length() + 1;
                j++;
//				Log.i("AlwaysTest", "j:"+j+";length:"+chips.length+";listSize:"+contactList.size());
                if (j >= (chips.length - 1) && chips.length > contactList.size()) break;
            }
            Log.i("AlwaysTest", "准备写入");
            SpannableStringBuilder ssb1 = new SpannableStringBuilder(s);
            this.setText(ssb);
//			setText("1");
            setSelection(getText().length());
        }


    }

    /**
     * @return
     */
    private String cutEmail(String s) {
        String ss = "";
        if (s == null) return "";
        if (s.contains("@")) {
            ss = s.substring(0, s.lastIndexOf("@"));
        }
        return ss.trim();
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
     * 创建一个TextView的项并为其绘画好背景
     *
     * @param channelName
     * @return
     */
    public ClipView createChannelTextView(String channelName) {
        ClipView clipView = (ClipView) inflater.inflate(R.layout.invite_contact_edititem, null);
        clipView.setText(channelName);
        return clipView;
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
        int queryEnd = getText().subSequence(i, j).toString().length();
        MemberChip[] chips = ssb.getSpans(0, queryEnd, MemberChip.class);
        return isSelectTop ? chips[0].getContactBean() : chips[chips.length - 1].getContactBean();
    }

    public LinkedHashMap<Object, ContactBean> getContacts() {
        map = new LinkedHashMap<Object, ContactBean>();
        for (ContactBean contactBean : contactList) {
            map.put(contactBean.getName(), contactBean);
        }

        return map;
    }

    public void clearContacts() {
        contactList.clear();
//		adapter.clearExcludes();
        clearExcludes();
        setChips(null);
    }

    //输入框内容变化监听
    @Override
    public void onTextChanged(CharSequence sss, int start, int before,
                              int count) {
        Log.i("AlwaysTest", "start=" + start + "before=" + before + "count=" + count + "；s=" + sss);
        if (sss.equals("")) return;
        if (adapter1 == null && adapter2 == null) return;
        turnConfirmButtonState();
        String s = sss.toString().replaceAll("，", ",");
        //手动输入断词符
        if (count == 1 && s.charAt(s.length() - 1) == ',') {
            if (lastTxt != null) {
                if (lastTxt.equals(
                        s.toString()
                ))
                    return;
                lastTxt = s.toString();
            }
            if (start == 0) {
                Toast.makeText(getContext(), getResources().getString(R.string.autoexit_comma), Toast.LENGTH_SHORT).show();
            } else {
                String chips[] = s.toString().trim().split(",");
                if (chips.length > contactList.size()) {
                    searchContactInAutoMulti(chips[chips.length - 1]);
                } else {
                    setChips(null);
                }
            }
        } else {
            if (!s.toString().contains(",")) {
//				adapter.getFilter().filter(s.toString());
                adapterFilter(s.toString());
            } else if (s.length() > 1 && s.charAt(s.length() - 1) == ',') {
//				adapter.getFilter().filter("");
                adapterFilter("");
            } else {
                String[] ss = s.toString().split(",");
                Log.i("AlwaysTest", ss.length + "");
//				adapter.getFilter().filter(ss[ss.length-1]);
                adapterFilter(ss[ss.length - 1]);
            }
        }


    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
        if (adapter1 == null && adapter2 == null) return;
        setSelectionEnd();
    }

    @Override
    public void afterTextChanged(Editable s) {
//		Log.i("AlwaysTest","Editable="+s.toString()+";"+(s.charAt(s.length()-1)==',')+";"+(s.toString().endsWith(",")));
        String test = s.toString();
        int x = test.length() - test.replaceAll(",", "").length();
        int y = test.length() - test.replaceAll(",,", "").length();
        if (s.length() < 1 && contactList.size() > 0) {
            contactList.clear();
            setChips(null);
//			adapter.clearExcludes();
            clearExcludes();
//			adapter.getFilter().filter("");
            adapterFilter("");
        } else if (x != contactList.size() || y > 0) {
            setChips(null);
        }
        if (s.toString().endsWith(",")) {
            confirm.setText(getResources().getString(R.string.confirm));
        } else {
            confirm.setText(getResources().getString(R.string.inconf_invite_add));
        }
    }

    //list点击事件
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        if (position >= 0) {
            if (parent.getId() == listView.getId()) {
                addToList(adapter1.getItem(position));
//				adapter.addExcludes(adapter.getItem(position));
                addExcludes(adapter1.getItem(position));
                setChips(null);
            } else if (parent.getId() == listView2.getId()) {
                addToList(adapter2.getItem(position));
//				adapter.addExcludes(adapter.getItem(position));
                addExcludes(adapter2.getItem(position));
                setChips(null);
            }
        }
    }

    //监听点击事件保证每次光标重置都在edit最末尾
    @Override
    public void onClick(View v) {
        setSelectionEnd();
    }

    private void searchContactInAutoMulti(String edit) {
        ContactBean contact = null;
        if (edit.equals(" ") || edit.equals("")) {
            setChips(null);
            return;
        }

        for (ContactBean contact1 : contactList) {
            if (contact1.getEmail().equals(edit)) {
                Toast.makeText(getContext(), getResources().getString(R.string.autoexit_existed), Toast.LENGTH_SHORT).show();
//				ToastUtil.showMessage(getContext(), "已经有了", Toast.LENGTH_SHORT);	
                setChips(null);
                return;
            }
        }

        if (contact == null) {
            if (StringUtil.checkInput(edit, Constants.EDIT_EMAIL)) {
                contact = new ContactBean(edit, "", edit, false);
            } else if (StringUtil.checkInput(edit, Constants.EDIT_PHONENUMBER)) {
                contact = new ContactBean(edit, edit, "", false);
            } else {
                setChips(null);
                Toast.makeText(getContext(), getResources().getString(R.string.autoexit_wrong), Toast.LENGTH_SHORT).show();
//				ToastUtil.showMessage(getContext(), "格式不对", Toast.LENGTH_SHORT);
                return;
            }
        }
        if (contact != null) {
//			contactList.add(contact);
            addToList(contact);
            setChips(null);
        }
    }

    private void turnConfirmButtonState() {
        if (confirm == null) return;
//		if(contactList.size()>= 1){
//			confirm.setEnabled(true);
//		}else{
//			confirm.setEnabled(false);
//		}
    }

    private void addToList(ContactBean contactBean) {
//		for (ContactBean contactBean2 : contactList) {
//			if(contactBean2.getEmail().equals(contactBean.getEmail()))return;
//		}
        contactList.add(contactBean);
    }

    private void addExcludes(ContactBean c) {
        if (adapter1 != null)
            adapter1.addExcludes(c);
        if (adapter2 != null) {
            adapter2.addExcludes(c);
        }
    }

    private void removeExcludes(ContactBean c) {
        if (adapter1 != null)
            adapter1.removeExcludes(c);
        if (adapter2 != null) {
            adapter2.removeExcludes(c);
        }
    }

    private void clearExcludes() {
        if (adapter1 != null)
            adapter1.clearExcludes();
        if (adapter2 != null) {
            adapter2.clearExcludes();
        }
    }

    private void adapterFilter(String s) {
        if (adapter1 != null)
            adapter1.getFilter().filter(s);
        if (adapter2 != null) {
            adapter2.getFilter().filter(s);
        }

    }

    public boolean clickConfirm() {
        String chips[] = getText().toString().trim().split(",");
        if (chips.length > contactList.size()) {
            searchContactInAutoMulti(chips[chips.length - 1]);
            setSelectionEnd();
            return false;
        } else {
            return true;
        }

    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {

    }
}
