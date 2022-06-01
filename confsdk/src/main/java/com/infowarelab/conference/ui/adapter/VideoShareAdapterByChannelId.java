package com.infowarelab.conference.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.UserCommonImpl;
import com.infowarelabsdk.conference.common.impl.VideoCommonImpl;
import com.infowarelabsdk.conference.confctrl.UserCommon;
import com.infowarelabsdk.conference.domain.UserBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VideoShareAdapterByChannelId extends BaseAdapter {
    private Context context;
    private ArrayList<Map<String, Object>> devices;
    private Map<Integer, Integer> syncs;
    //	private List<UserBean> videoUsers = new ArrayList<UserBean>();
    private Set<Integer> localPreviewChannelid = new HashSet<Integer>();
    private boolean isHost;

    public VideoShareAdapterByChannelId(Context context, Set<Integer> locals, boolean isHost) {
        this.context = context;
        this.localPreviewChannelid = locals;
        this.isHost = isHost;
        initUsers();
    }

    public void initUsers() {
//		videoUsers.clear();
//		List<UserBean> users = ((UserCommonImpl)CommonFactory.getInstance().getUserCommon()).getUserArrayList();
//		for (UserBean userBean : users) {
//			if(userBean.isHaveVideo()&&userBean.getDevice()!=UserCommon.DEVICE_HIDEATTENDEE){
//				if(isHost||!userBean.isShareVideo()){
//					videoUsers.add(userBean);
//				}
//			}
//		}
        devices = new ArrayList<Map<String, Object>>();
        syncs = ((VideoCommonImpl) CommonFactory.getInstance().getVideoCommon()).getSyncMap();
        Map<Integer, Integer> devicesMap = ((VideoCommonImpl) CommonFactory.getInstance().getVideoCommon()).getDeviceMap();
        for (Map.Entry<Integer, Integer> e : devicesMap.entrySet()) {
            if (isHost || !syncs.containsKey(e.getValue())) {
                Map<String, Object> device = new HashMap<String, Object>();
                device.put("cid", e.getKey());
                device.put("uid", e.getValue());
                device.put("name", ((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).getUser(e.getValue()).getUsername());
                devices.add(device);
            }
        }


    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Map<String, Object> getItem(int position) {
        return devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {

            convertView = LayoutInflater.from(context).inflate(R.layout.conference_video_share_item, null);

        }
        Map<String, Object> device = devices.get(position);
        convertView.setTag(device.get("cid"));
        TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
        ImageView ivVideo = (ImageView) convertView.findViewById(R.id.ivSync);
        if (syncs.containsKey(device.get("cid"))) {
            ivVideo.setImageResource(R.drawable.icon_sharevideo_share);
        } else if (localPreviewChannelid.contains(device.get("cid"))) {
            ivVideo.setImageResource(R.drawable.icon_sharevideo_on);
        } else {
            ivVideo.setImageResource(R.drawable.icon_sharevideo_normal);
        }
        tvName.setText((String) device.get("name"));
        convertView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                check((Integer) v.getTag());
            }
        });

        return convertView;
    }

    private void setData(final int position, View convertView) {


    }

    private ItemCheck itemCheck;

    public interface ItemCheck {
        public void check(int uid);
    }

    public void setItemCheck(ItemCheck itemCheck) {
        this.itemCheck = itemCheck;
    }

    private void check(int uid) {
        if (this.itemCheck != null) {
            this.itemCheck.check(uid);
        }
    }
}
