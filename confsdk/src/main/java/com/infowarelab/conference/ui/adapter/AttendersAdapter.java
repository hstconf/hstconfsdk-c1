package com.infowarelab.conference.ui.adapter;

import java.util.LinkedList;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.infowarelab.conference.ConferenceApplication;
import com.infowarelab.conference.ui.view.KickDialog;
import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.UserCommonImpl;
import com.infowarelabsdk.conference.confctrl.UserCommon;
import com.infowarelabsdk.conference.domain.UserBean;

public class AttendersAdapter extends BaseAdapter {
    private Context context;
    private LinkedList<UserBean> list;
    private ViewHolder holder;
    private UserBean userBean;
    private UserCommonImpl userCommon = (UserCommonImpl) CommonFactory.getInstance().getUserCommon();
    public boolean isEditing = false;
    private OnEditFinishListener editFinishListener;
    private KickDialog kickDialog;

    public AttendersAdapter(Context context, LinkedList<UserBean> list) {
        this.context = context;
        this.list = list;
    }

    public void update(LinkedList<UserBean> list) {
        if (this.list == null) {
            this.list = new LinkedList<UserBean>();
        } else {
            this.list.clear();
        }
        for (UserBean userBean : list) {
            if (userBean.getDevice() != UserCommon.DEVICE_HIDEATTENDEE && userBean.getDevice() != UserCommon.DEVICE_CLOUDRECORD) {
                this.list.add(userBean);
            }
        }
//		for (int i=1;i<20;i++){
//			UserBean userBean1 = new UserBean();
//			userBean1.setUid(i);
//			userBean1.setUsername(i+"");
//			this.list.add(userBean1);
//		}
//		this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    //构建参加者列表元素
    @Override
    public View getView(int position, View convertView, ViewGroup arg2) {
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.conference_attenders_listitem, null);
            holder.role = (ImageView) convertView.findViewById(R.id.attenders_role);
            holder.name = (TextView) convertView.findViewById(R.id.attenders_name);
            holder.tvRole = (TextView) convertView.findViewById(R.id.attenders_tv_role);
            holder.point = (TextView) convertView.findViewById(R.id.attenders_point);
            holder.video = (ImageView) convertView.findViewById(R.id.attenders_video);
            holder.host = (ImageView) convertView.findViewById(R.id.attenders_iv_hostlogo);
            holder.audio = (ImageView) convertView.findViewById(R.id.attenders_audio);
            holder.kick = (ImageView) convertView.findViewById(R.id.attenders_kick);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (getCount() <= position) {
            //直接引用会导致异常
            return convertView;
        }
        boolean isHost = userCommon.isHost();

        userBean = list.get(position);
        String name = userBean.getUsername();
        try {
            name = idgui(name, 10);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            if (name.length() > 8) {
                name = name.substring(0, 8) + "…";
            }
        }

        holder.name.setTag(userBean.getUid());
        holder.video.setVisibility(View.INVISIBLE);
        holder.audio.setVisibility(View.VISIBLE);
        holder.kick.setVisibility(View.INVISIBLE);
        holder.name.setVisibility(View.VISIBLE);
        holder.host.setVisibility(View.GONE);

        //设置设备类型图标
        if (userBean.getDevice() == UserCommon.DEVICE_PC) {
            holder.role.setImageResource(R.drawable.ic_att_device_pc);
        } else if (userBean.getDevice() == UserCommon.DEVICE_MOBILE) {
            holder.role.setImageResource(R.drawable.ic_att_device_mobile);
        } else if (userBean.getDevice() == UserCommon.DEVICE_H323) {
            holder.role.setImageResource(R.drawable.ic_att_device_h323);
        } else if (userBean.getDevice() == UserCommon.DEVICE_TELEPHONE) {
            holder.role.setImageResource(R.drawable.ic_att_device_tel);
            name = userBean.getUsername();
        } else if (userBean.getDevice() == UserCommon.DEVICE_MEETINGBOX) {
            holder.role.setImageResource(R.drawable.ic_att_device_meetingbox);
        } else if (userBean.getDevice() == UserCommon.CONF_ROLE_APPLET) {
            holder.role.setImageResource(R.drawable.ic_att_device_web);
        } else {
            holder.role.setImageResource(R.drawable.ic_att_device_pc);
        }
        if (userBean.getRole() == UserCommon.ROLE_HOST) {
            Log.i("Attender", "Attender Myvideo=" + userBean.isVideoOpen());
//			holder.role.setImageResource(R.drawable.host_img_on);
            holder.name.setText(name);
            holder.tvRole.setText("(" + context.getResources().getString(R.string.attenders_adapter_host) + ")");
            holder.tvRole.setVisibility(View.VISIBLE);
            holder.host.setVisibility(isHost ? View.VISIBLE : View.GONE);
        } else if (userBean.getRole() == UserCommon.ROLE_SPEAKER) {
//			holder.role.setImageResource(R.drawable.host_img_on);
            holder.name.setText(name);
            holder.tvRole.setText("(" + context.getResources().getString(R.string.attenders_adapter_speaker) + ")");
            holder.tvRole.setVisibility(View.VISIBLE);
            holder.kick.setVisibility(isHost ? View.VISIBLE : View.INVISIBLE);
        } else if (userBean.getRole() == UserCommon.ROLE_ASSISTANT) {
//			holder.role.setImageResource(R.drawable.host_img_on);
            holder.name.setText(name);
            holder.tvRole.setText("(" + context.getResources().getString(R.string.attenders_adapter_assistant) + ")");
            holder.tvRole.setVisibility(View.VISIBLE);
            holder.kick.setVisibility(isHost ? View.VISIBLE : View.INVISIBLE);
        } else if (userBean.getUid() == UserCommon.ALL_USER_ID) {
            userBean.setUsername(context.getResources().getString(R.string.all));
            holder.role.setImageResource(R.drawable.ic_att_device_all);
            holder.name.setText(context.getResources().getString(R.string.attenders_adapter_publicchat));
            //holder.name.setFocusable(false);
            holder.video.setVisibility(View.INVISIBLE);
            holder.audio.setVisibility(View.INVISIBLE);
            holder.tvRole.setVisibility(View.INVISIBLE);
        } else if (userBean.getUid() == userCommon.getSelf().getUid()) {
            holder.role.setImageResource(R.drawable.ic_att_device_mobile);
            holder.name.setText(name);
            holder.tvRole.setText("(" + context.getResources().getString(R.string.attenders_adapter_me) + ")");
            holder.tvRole.setVisibility(View.VISIBLE);
        } else {
            holder.name.setText(name);
            holder.tvRole.setVisibility(View.INVISIBLE);
            holder.kick.setVisibility(isHost ? View.VISIBLE : View.INVISIBLE);
        }

        holder.kick.setTag(userBean.getUid());
        holder.kick.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (kickDialog == null) {
                    kickDialog = new KickDialog(context, ConferenceApplication.SCREEN_WIDTH > ConferenceApplication.SCREEN_HEIGHT ? ConferenceApplication.SCREEN_HEIGHT * 4 / 5 : ConferenceApplication.SCREEN_WIDTH * 4 / 5);
                    kickDialog.setClickListener(new KickDialog.OnResultListener() {

                        @Override
                        public void doYes(int uid) {
                            // TODO Auto-generated method stub
                            userCommon.kickUser(uid);
                        }

                        @Override
                        public void doNo() {
                            // TODO Auto-generated method stub
                        }
                    });
                }
                if (kickDialog != null && !kickDialog.isShowing()) {
                    int uid = (Integer) v.getTag();
                    kickDialog.show(uid, userCommon.getUser(uid).getUsername());
                }
            }
        });
		

		/*holder.name.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				EditText edit = (EditText) v;
				System.out.println(v.isFocused()+"/"+edit.getText().toString().trim());
				if(!hasFocus){
					System.out.println(hasFocus+"/"+edit.getText().toString().trim()+"/"+preText);
					if(!edit.getText().toString().trim().equals(preText)){
						//userCommon.setUserName((Integer)edit.getTag(), edit.getText().toString().trim());
					}
					
				}else{
					preText = edit.getText().toString().trim();
				}
				
			}
		});*/

        //聊天消息
        if (!userBean.isReadedMsg()) {
            holder.point.setVisibility(View.VISIBLE);
//			String num = String.valueOf(userCommon.getNotReadedMessage(userBean.getUid()));
//			holder.point.setText(num);
        } else {
            holder.point.setVisibility(View.INVISIBLE);
        }

        if (userBean.isShareVideo() || userBean.isVideoOpen()) {
            holder.video.setImageResource(R.drawable.ic_att_video_on);
        } else {
            holder.video.setImageResource(R.drawable.ic_att_video_off);
        }

        if (userBean.isHaveVideo()) {
            holder.video.setVisibility(View.VISIBLE);
        }

        if (userBean.isAudioOpen()) {
            holder.audio.setImageResource(R.drawable.ic_index_1_enable_on);
        } else {
            holder.audio.setImageResource(R.drawable.ic_index_1_enable_off);
        }
        if (userBean.getDevice() == UserCommon.DEVICE_TELEPHONE) {
            holder.audio.setImageResource(R.drawable.ic_index_1_enable_on);
        }

        return convertView;
    }

    class ViewHolder {
        ImageView role;
        TextView name;
        TextView tvRole;
        TextView point;
        ImageView host;
        ImageView video;
        ImageView audio;
        ImageView kick;
    }

    private void hideInput() {
        InputMethodManager inputMethodManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != (Activity) context && null != ((Activity) context)
                .getCurrentFocus().getWindowToken()) {
            inputMethodManager.hideSoftInputFromWindow(((Activity) context)
                            .getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void doHide() {
        isEditing = false;
        hideInput();
        notifyDataSetChanged();
    }

    public interface OnEditFinishListener {
        public void onEditFinish();
    }

    private void editFinish() {
        if (editFinishListener != null) editFinishListener.onEditFinish();
    }

    public void setOnEditFinishListener(OnEditFinishListener editFinishListener) {
        this.editFinishListener = editFinishListener;
    }

    private String idgui(String s, int num) throws Exception {
        int changdu = s.getBytes("GBK").length;
        if (changdu > num) {
            s = s.substring(0, s.length() - 1);
            s = idgui2(s, num) + "…";
        }
        return s;
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
