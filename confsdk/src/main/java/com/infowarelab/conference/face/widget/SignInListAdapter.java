package com.infowarelab.conference.face.widget;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.infowarelab.hongshantongphone.R;
import com.infowarelab.conference.face.model.FaceRegisterInfo;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.UserCommonImpl;
import com.infowarelabsdk.conference.confctrl.UserCommon;
import com.infowarelabsdk.conference.domain.UserBean;

import java.util.ArrayList;
import java.util.List;

public class SignInListAdapter extends BaseAdapter {
	private Context context;
	private List<FaceRegisterInfo> mFaceRegisterList;
	private ViewHolder holder;
	private FaceRegisterInfo mFaceRegisterInfo;

	public boolean isChangeing = false;

	public List<FaceRegisterInfo> getFaceRegisterInfo() {
		return mFaceRegisterList;
	}

	public SignInListAdapter(Context context) {
		this.context = context;
		//this.mFaceRegisterList = faceRegisterList;
		if(this.mFaceRegisterList==null){
			this.mFaceRegisterList = new ArrayList<FaceRegisterInfo>();
		}else {
			this.mFaceRegisterList.clear();
		}
	}

	public void update() {

		notifyDataSetChanged();
	}

	public List<FaceRegisterInfo> getFaceRegisterList() {
		return mFaceRegisterList;
	}

	public List<FaceRegisterInfo> getFaceRegisterInfoList(){
		return mFaceRegisterList;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mFaceRegisterList.size();
	}

	@Override
	public FaceRegisterInfo getItem(int position) {
		// TODO Auto-generated method stub
		return mFaceRegisterList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.item_sign_in, null);
			holder.itemLayout = (LinearLayout)convertView.findViewById(R.id.item_sign_in_layout);
			holder.ll = (LinearLayout) convertView
					.findViewById(R.id.item_sign_in_ll);
			holder.signInStatus = (ImageView) convertView
					.findViewById(R.id.item_sign_in_status);

			holder.name = (TextView) convertView
					.findViewById(R.id.item_sign_in_name);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (getCount() <= position) {
			return convertView;
		}

		mFaceRegisterInfo = getItem(position);

		holder.name.setText(mFaceRegisterInfo.getName());

//		String name = userBean.getUsername();
//		try {
//			name = idgui(name, 8);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			if(name.length() > 8){
//				name = name.substring(0, 8)+"…";
//			}
//		}
//
//		if (userBean.isFouse()){
//			holder.itemLayout.setBackgroundResource(R.drawable.invate_list_bg);
//		}else {
//			holder.itemLayout.setBackgroundResource(R.drawable.selected);
//		}
//
//		holder.name.setTag(userBean.getUid());
//		holder.ll.setTag(userBean.getUid());
//		holder.mic.setTag(userBean.getUid());
//		holder.camera.setTag(userBean.getUid());
//		holder.name.setVisibility(View.VISIBLE);
//		holder.mic.setVisibility(View.VISIBLE);
//		holder.camera.setVisibility(View.VISIBLE);
//
//
//		if(userBean.getDevice()== UserCommon.DEVICE_PC){
//			holder.device.setImageResource(R.drawable.prelist_equipment_pc);
//		}else if(userBean.getDevice()== UserCommon.DEVICE_MOBILE){
//			holder.device.setImageResource(R.drawable.prelist_equipment_phone);
//		}else if (userBean.getDevice()== UserCommon.DEVICE_H323){
//			holder.device.setImageResource(R.drawable.prelist_equipment_pc);
//		}else if (userBean.getDevice()== UserCommon.DEVICE_TELEPHONE){
//			holder.device.setImageResource(R.drawable.prelist_equipment_tel);
//			name = userBean.getUsername();
//		}else if (userBean.getDevice()== UserCommon.DEVICE_MEETINGBOX){
//			holder.device.setImageResource(R.drawable.prelist_equipment_pc);
//		}else {
//			holder.device.setImageResource(R.drawable.prelist_equipment_pc);
//		}
//
//		if (userBean.getRole() == UserCommon.ROLE_HOST) {
//			holder.name.setText(name);
//			holder.role.setVisibility(View.VISIBLE);
//			if(userBean.getUid() == userCommon.getSelf().getUid()){
//				holder.role.setText("("+context.getResources().getString(R.string.attenders_host)+"&"+context.getResources().getString(R.string.attenders_me)+")");
//			}else{
//				holder.role.setText("("+context.getResources().getString(R.string.attenders_host)+")");
//			}
//		}else if (userBean.getRole() == UserCommon.ROLE_SPEAKER) {
//			holder.name.setText(name);
//			holder.role.setVisibility(View.VISIBLE);
//			if(userBean.getUid() == userCommon.getSelf().getUid()){
//				holder.role.setText("("+context.getResources().getString(R.string.attenders_speaker)+"&"+context.getResources().getString(R.string.attenders_me)+")");
//			}else{
//				holder.role.setText("("+context.getResources().getString(R.string.attenders_speaker)+")");
//			}
//		}else if (userBean.getRole() == UserCommon.ROLE_ASSISTANT) {
//			holder.name.setText(name);
//			holder.role.setVisibility(View.VISIBLE);
//			if(userBean.getUid() == userCommon.getSelf().getUid()){
//				holder.role.setText("("+context.getResources().getString(R.string.attenders_assistant)+"&"+context.getResources().getString(R.string.attenders_me)+")");
//			}else{
//				holder.role.setText("("+context.getResources().getString(R.string.attenders_assistant)+")");
//			}
//		} else if (userBean.getUid() == UserCommon.ALL_USER_ID) {
//			holder.name.setText(context.getResources().getText(R.string.attenders_all));
//			holder.role.setVisibility(View.INVISIBLE);
//			holder.device.setImageResource(R.drawable.prelist_equipment_pc);
//			holder.mic.setVisibility(View.INVISIBLE);
//			holder.camera.setVisibility(View.INVISIBLE);
//		} else if (userBean.getUid() == userCommon.getSelf().getUid()) {
//			holder.name.setText(name);
//			holder.role.setVisibility(View.VISIBLE);
//			holder.role.setText("("+context.getResources().getString(R.string.attenders_me)+")");
//		} else {
//			holder.name.setText(name);
//			holder.role.setVisibility(View.INVISIBLE);
//		}
//
//
//		if (!userBean.isHaveMic()){
//			holder.mic.setImageResource(R.drawable.prelist_microphone_disable);
//		}else if(userBean.isAudioOpen()){
//			holder.mic.setImageResource(R.drawable.prelist_microphone_open_normal);
//		}else{
//			holder.mic.setImageResource(R.drawable.prelist_microphone_normal);
//		}
//
//		if (userBean.getDevice()== UserCommon.DEVICE_TELEPHONE){
//			holder.mic.setImageResource(R.drawable.prelist_microphone_open_normal);
//		}
//
//		if(!userBean.isHaveVideo()){
//			holder.camera.setImageResource(R.drawable.prelist_camera_disable);
//		}else if(userBean.isShareVideo()||userBean.isVideoOpen()){
//			holder.camera.setImageResource(R.drawable.prelist_camera_open_normal);
//		}else{
//			holder.camera.setImageResource(R.drawable.prelist_camera_normal);
//		}

		return convertView;
	}

//	public interface OnSelectListener {
//		public void doSelect(int id);
//		public void doMic(int id);
//		public void doCam(int id);
//		public void doRelPresenter(int id);
//	}
//	public void setOnSelectListener(OnSelectListener onSelectListener) {
//		this.onSelectListener = onSelectListener;
//	}
//	public void doSelect(int id) {
//		if (onSelectListener != null) {
//			onSelectListener.doSelect(id);
//		}
//	}
//	public void doMic(int id) {
//		if (onSelectListener != null) {
//			onSelectListener.doMic(id);
//		}
//	}
//	public void doCam(int id) {
//		if (onSelectListener != null) {
//			onSelectListener.doCam(id);
//		}
//	}
//	public void doRelPresenter(int id) {
//		if (onSelectListener != null) {
//			onSelectListener.doRelPresenter(id);
//		}
//	}

	class ViewHolder {
		LinearLayout itemLayout;
		LinearLayout ll;
		ImageView signInStatus;
		TextView name;
	}
	private void hideInput(){
		InputMethodManager inputMethodManager = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if(null!=context&&null!=((Activity) context)
				.getCurrentFocus().getWindowToken()){
			inputMethodManager.hideSoftInputFromWindow(((Activity) context)
					.getCurrentFocus().getWindowToken(),
					InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}
	public void doHide(){
		isChangeing = false;
		hideInput();
		notifyDataSetChanged();
	}
	private String idgui(String s,int num)throws Exception{
        int changdu = s.getBytes("GBK").length;
        if(changdu > num){
            s = s.substring(0, s.length() - 1);
            s = idgui2(s,num)+"…";
        }
        return s;
    }
	private String idgui2(String s,int num)throws Exception{
		int changdu = s.getBytes("GBK").length;
		if(changdu > num){
			s = s.substring(0, s.length() - 1);
			s = idgui2(s,num);
		}
		return s;
	}
}
