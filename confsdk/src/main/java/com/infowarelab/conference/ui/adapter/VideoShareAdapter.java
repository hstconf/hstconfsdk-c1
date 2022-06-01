package com.infowarelab.conference.ui.adapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.infowarelab.hongshantongphone.R;
import com.infowarelab.conference.ui.activity.inconf.Conference4PhoneActivity;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.UserCommonImpl;
import com.infowarelabsdk.conference.confctrl.UserCommon;
import com.infowarelabsdk.conference.domain.UserBean;

public class VideoShareAdapter extends BaseAdapter {
    private Context context;
    private List<UserBean> videoUsers = new ArrayList<UserBean>();
    private Set<Integer> localPreviewUsers = new HashSet<Integer>();
    private boolean isHost;

    public VideoShareAdapter(Context context, Set<Integer> users, boolean isHost) {
        this.context = context;
        this.localPreviewUsers = users;
        this.isHost = isHost;
        initUsers();
    }

    public void initUsers() {
        videoUsers.clear();
        List<UserBean> users = ((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).getUserArrayList();
        for (UserBean userBean : users) {
            if (userBean.isHaveVideo() && userBean.getDevice() != UserCommon.DEVICE_HIDEATTENDEE) {
                if (isHost || !userBean.isShareVideo()) {
                    videoUsers.add(userBean);
                }
            }
        }
//		notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return videoUsers.size();
    }

    @Override
    public Object getItem(int position) {
        return videoUsers.get(position);
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
        convertView.setTag(videoUsers.get(position).getUid());
        setData(position, convertView);
        return convertView;
    }

    private void setData(final int position, View convertView) {

        TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
        ImageView ivVideo = (ImageView) convertView.findViewById(R.id.ivSync);
        if (videoUsers.get(position).isShareVideo()) {
            ivVideo.setImageResource(R.drawable.icon_sharevideo_share);
        } else if (localPreviewUsers.contains(videoUsers.get(position).getChannelIds().get(0))) {
            ivVideo.setImageResource(R.drawable.icon_sharevideo_on);
        } else {
            ivVideo.setImageResource(R.drawable.icon_sharevideo_normal);
        }
        tvName.setText(videoUsers.get(position).getUsername());
        convertView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                check((Integer) v.getTag());
            }
        });

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
